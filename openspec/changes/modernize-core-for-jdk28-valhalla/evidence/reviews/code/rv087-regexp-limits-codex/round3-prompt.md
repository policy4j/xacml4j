# Adversarial code review, round 3: limits on regular-expression functions (xacml4j)

You are an independent reviewer. Do not use tools. Do not edit files. Answer
in one message. Review only the diff below. It is the complete diff of this
change. The full reactor passes on it (totals below); conformance unchanged.

## Project

xacml4j is a Java 21 implementation of OASIS XACML 2.0/3.0.
`RegExpFunctions` implements the six `*-regexp-match` functions. The
pattern comes from the policy; the input can come from the request. The
XACML pattern is rewritten to `java.util.regex` syntax and matched with
`Matcher.find()`.

## Defect (security sweep)

`java.util.regex` backtracks. A pattern such as `(.*a){20}$` on
`"a" x 40 + "!"` does not return within 3 seconds on JDK 21.0.6 (measured
with a stand-alone probe; the classic `(a+)+$` returns at once on that
JDK). One such match holds a decision thread. The optional per-thread
pattern cache (system property `...RegExpFunctions.cacheRegExps`) was an
unbounded `HashMap`.

## Maintainer ruling this change implements (2026-10-07, D17)

Bound the regex input length and the evaluation time; bounded LRU pattern
cache. The ruling gives no numbers. The defaults below were chosen by the
author and are open to change: pattern 10,000 characters, input 100,000
characters, 1,000 ms per match, 256 cached patterns per thread.

## Change

- `Limits(maxPatternLength, maxInputLength, maxMatchMillis)`; the public
  `matches(String, String)` uses limits read once from system properties
  (`<class name>.maxPatternLength`, `.maxInputLength`, `.maxMatchMillis`,
  `.cacheSize`), with the defaults above.
- Length checks before the pattern is rewritten or compiled.
- Time limit: the input is wrapped in `DeadlineCharSequence`; `charAt`
  reads the clock once per 1,024 calls and throws a private unchecked
  exception after the deadline; `matches` turns it into the result below.
- Beyond a limit: `EvaluationException` with status processing-error and
  the fixed message `Regular expression evaluation limit exceeded`.
- `PatternCache`: access-ordered `LinkedHashMap` with `removeEldestEntry`,
  one per thread, used only when the existing cache property is set.

Facts you cannot see in the diff:

- `Pattern.matcher(CharSequence)` keeps the sequence and reads it with
  `charAt` and `length`, also while it backtracks.
- `EvaluationException(Status, String)` copies the message into the status
  message; an `EvaluationException` from a function becomes Indeterminate
  with that status.
- `covertXacmlToJavaSyntax` is unchanged (it rewrites Unicode block names,
  class subtraction, and `$`).
- `or`/`and` let a later deciding operand win over a failed one, so
  `or(regexp-match(slow), true)` is true after at most one time limit.
- The file imports `org.xacml4j.v30.types.*`, which is why it writes
  `java.lang.String`, `java.lang.Integer`, `java.lang.Long` in full.

## Round 1 result and what was done

Codex: REQUEST_CHANGES with three findings.

1. HIGH: `(?:(?:){2147483647}){2147483647}` on `""` loops without a call of
   `charAt`, so the deadline never fires. Measured on JDK 21.0.6 with a
   stand-alone probe (3 s cap per pattern):

   | pattern | input | result |
   | --- | --- | --- |
   | `(?:(?:){2147483647}){2147483647}` | `""` | no result within 3 s |
   | `(?:(?:(?:){1000}){1000}){1000}` | `""` | true after 1,069 ms |
   | `(?:(?:){1000}){1000}` | `""` | true after 7 ms |
   | `(?:){2147483647}` | `""` | true after 43 ms |
   | `(a?){2147483647}`, `(?:a*){2147483647}`, `(?:){5,2147483647}` | `""` | true at once |

   So the work that the deadline cannot see is the minimum count of a
   counted repetition around something that matches without reading input,
   and it multiplies when such repetitions are nested. Fixed with a bound
   that is checked before the pattern is compiled: `repetitionWeight`
   scans the pattern (escapes, `\Q..\E`, character classes, groups) and
   returns the largest product of the minimum counts of counted
   repetitions that are nested in each other. Above `maxRepetition`
   (default 1,000,000, about 7 ms in the probe) the function is
   Indeterminate. Running the match in another thread or process was not
   chosen: a thread cannot be stopped, and a process per match is not an
   option for a PDP.
2. MEDIUM: compilation had no budget. The clock now starts before the
   pattern is rewritten and compiled, and the deadline is checked after
   `Pattern.compile` and then used for the match. Compilation still cannot
   be interrupted; the pattern length limit bounds it.
3. LOW: the timing test allowed 20 seconds. It now asserts less than 2
   seconds for a 50 ms limit (the 20 s preemptive timeout stays as a
   safety net for the build). New tests:
   `nestedCountedRepetitionsAreBoundedBeforeTheMatch`,
   `repetitionWeightIsTheLargestProductOfNestedMinimumCounts`. Tests of the
   system properties in a fresh JVM were not added.

4. After the fix the scanner was split into a small class
   (`RepetitionScanner`: `weight`, `skipEscape`, `skipCharacterClass`,
   `quantified`, `countedMinimum`), because the single method was too
   complex to review. While doing that the author found that comments mode
   could hide a quantifier from the scanner: in `(?x)` a `#` starts a
   comment, so in `(?x)(?:(?:){100000} # [` followed by a newline and
   `){100000}` the scanner would read `[` as the start of a character class
   and skip the second quantifier. A pattern with a flag group that turns
   `x` on (`(?x)`, `(?ix:`) now gets the largest weight and is rejected.
   Tests: `commentsModeIsRejectedBecauseItCanHideAQuantifier`, and a row
   with group prefixes (`(?<year>`, `(?=`, `(?!`, `(?i)`).

## Round 2 result and what was done

Codex: REQUEST_CHANGES with three findings.

1. HIGH: escape handling could hide a repetition from the scanner
   (`\c[` read as the start of a class; an empty `\Q\E` between a group
   and its quantifier). Fixed:
   - `skipEscape` takes the character after `\c` as part of the escape.
   - Quoting is removed before the scan, in the way `java.util.regex`
     does it before it parses (`withoutQuoting`, written after the private
     method `Pattern.RemoveQEQuoting`): an empty quote disappears, a quoted
     character becomes an escaped literal. `skipEscape` has no quoting
     case any more.
   While fixing it the author found two more ways and closed them:
   - A counted quantifier directly after `(` (or `|`, or a flag group) was
     not counted. `java.util.regex` repeats an empty atom there. The
     scanner now counts a counted quantifier wherever it stands outside a
     class or an escape; it multiplies the weight of what stands before it
     (1 at the start of a group).
   - `]` directly after `[` or `[^` is a literal in `java.util.regex`. The
     first scanner closed the class there. A `(` in the rest of the class
     then opened a group for the scanner only, and the weight of the real
     enclosing group was lost. `skipCharacterClass` now follows the rule of
     `Pattern.clazz`.
   Measured on JDK 21.0.6 (stand-alone probe; outer count 3 against 3,000,
     inner count 3,000, input in the last column):

   | construct | pattern (large) | small | large | input |
   | --- | --- | --- | --- | --- |
   | control escape before a group | `\c[(?:(?:){3000}){3000}` | 1 ms | 16 ms | U+001B |
   | empty quote before a quantifier | `(?:(?:){3000})\Q\E{3000}` | 0 ms | 56 ms | empty |
   | quantifier directly after `(` | `({3000}){3000}` | 0 ms | 13 ms | empty |
   | quantifier after a flag group | `(?:(?i){3000}){3000}` | 0 ms | 14 ms | empty |
   | class with a leading `]` | `(?:(?:){3000}[]a(]{0}){3000}` | 0 ms | 30 ms | empty |

   All compile and return true. A lone `\E` is a syntax error. Tests:
   `repetitionsCanNotBeHiddenFromTheBound`,
   `patternWithoutQuotingMeansTheSame` (the pattern without quoting gives
   the same `find()` results as the pattern with it, 13 patterns on 18
   inputs).
2. HIGH: zero-width branching, `"(?:(?=)|(?=))".repeat(40) + "(?!)"` on
   `""`. Confirmed by measurement: 26 groups give `false` after 2,092 ms.
   NOT FIXED in this change. No bound for it is known to the author that
   is sound for `java.util.regex` and does not reject ordinary patterns;
   the real fixes are another engine, a parser for the XPath pattern
   grammar, or an abandoned worker thread, and each is a design decision.
   It is filed as bug policy4j-the.4.58 (priority 1, security, four
   options, waits for the maintainer). What this change does about it:
   - the deadline is checked again after `find()`, so a result that comes
     late is discarded and the function is Indeterminate
     (`resultThatComesAfterTheTimeLimitIsNotUsed`); the CPU time is spent
     all the same;
   - the Javadoc of `repetitionWeight` and the spec say that only counted
     repetitions are bounded.
   The spec requirement now reads: the limits bound the pattern length,
   the input length, nested counted repetitions, and the time of work
   that reads the input; work of a pattern that does not read the input
   is not stopped, only its late result is discarded.
3. MEDIUM: `[]a{1000001}]` was rejected. Fixed by the class rule above.
   Test: `bracesInACharacterClassAreNotARepetition`.

Tests that were named as missing:

- System properties: `Limits.from(lookup)` reads them; the static limits
  are `Limits.from(System::getProperty)`. A value that is not a number
  between 1 and the maximum now throws `IllegalArgumentException` (before:
  an unparsable value silently gave the default). That exception is thrown
  while the class initializes. Tests: `limitsAreReadFromProperties`,
  `propertyThatIsNotAPositiveNumberIsRejected`. No fresh-JVM test.
- The two tests whose patterns do not return without the bound are now
  inside `assertTimeoutPreemptively` (20 s), so a regression fails instead
  of hanging the build.

Red run: the new tests were run against the round-2 code with two compile
shims (`withoutQuoting` returning its argument, `Limits.from` returning the
defaults): 17 tests, 5 failures (`repetitionsCanNotBeHiddenFromTheBound`
timed out after 20 s; `bracesInACharacterClassAreNotARepetition` got
1,000,001; the quoting and the two property tests failed on the shims).
`resultThatComesAfterTheTimeLimitIsNotUsed` was run separately against that
code and failed: "match 409 ms, limit 102 ms", nothing thrown.

The full reactor passes on the diff below: 7,781 tests, 0 failures, 0 errors, 22 skipped.

## Questions

Report findings ordered by severity (HIGH, MEDIUM, LOW), each with file,
line from the diff, a concrete failing scenario, and a suggested fix. Report
only defects you can show from the diff and the facts above.

1. Can a counted repetition still be hidden from `repetitionWeight`, so
   that a pattern with a small weight repeats an empty atom more than
   `maxRepetition` times? Compare `withoutQuoting` with
   `Pattern.RemoveQEQuoting`, `skipEscape` with `Pattern.escape`,
   `skipCharacterClass` with `Pattern.clazz` (also `&&`), the group and
   flag handling with `Pattern.group0`, and `countedMinimum` with
   `Pattern.closure`.
2. Can a harmless pattern now be rejected because the scanner counts a
   repetition that `java.util.regex` does not make?
3. Is the late-result check right? Can it turn a correct, timely result
   into Indeterminate?
4. `Limits.from`: parsing, bounds, and the failure while the class
   initializes. Is that failure mode a defect?
5. Do the new tests prove what their names say? Name a missing test.
6. Finding 2 of round 2 stays open and is deferred to the maintainer
   (policy4j-the.4.58). Is it described accurately above, and is there a
   sound bound for it that fits in this change? Say plainly whether you
   accept that scope for this change. If you do not, the verdict is
   REQUEST_CHANGES.

End with a verdict line: `VERDICT: APPROVE` or `VERDICT: REQUEST_CHANGES`.

## Diff

```diff
