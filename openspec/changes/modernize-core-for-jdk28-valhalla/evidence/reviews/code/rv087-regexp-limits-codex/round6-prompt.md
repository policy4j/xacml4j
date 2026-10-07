# Adversarial code review, round 6: limits on regular-expression functions (xacml4j)

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

## Round 3 result and what was done

Codex: REQUEST_CHANGES with one finding. It found no further way to hide
a repetition from the scanner, and it accepted the documented partial
scope: finding 2 of round 2 stays open as policy4j-the.4.58.

1. MEDIUM: counted quantifiers that stand next to each other were
   multiplied (`(?:){1000}{1001}` had weight 1,001,000 and was rejected).
   Fixed: a counted quantifier multiplies the weight of the group that was
   just closed, or 1; after a quantifier the weight is 1 again, because
   `java.util.regex` repeats an empty atom of its own there.
   `(?:){1000}{1001}` has weight 1,001 and `a{2}{3}` has 3. The quantifier
   branch moved into `RepetitionScanner.repeated`. Test:
   `quantifiersNextToEachOtherDoNotMultiply`.
2. Note of round 3 on `resultThatComesAfterTheTimeLimitIsNotUsed` (it did
   not isolate the check after `find()`, and its calibration included
   warm-up). The test no longer measures time. `matches` has a
   package-private overload with a clock (`LongSupplier`), which
   `DeadlineCharSequence` uses too; the other overloads pass
   `System::nanoTime`. The test gives the clock readings and asserts the
   number of clock reads: on time (3 reads, result returned), late after
   the match (3 reads, Indeterminate), late after the compilation (2
   reads), late while reading 2,000 characters (3 reads: the third is at
   character 1,024), on time while reading (4 reads).
   Measured while doing this: on this JVM the time of empty repetitions is
   not proportional to their count (`(?:(?:){30000}){30000}` returned in
   34 ms, `(?:(?:){10000}){10000}` in 121 to 171 ms). Times are therefore
   not used any more as proof of a parsing rule.
3. New test `scannerRulesAreTheRulesOfJavaUtilRegex`: each rule of the
   scanner is shown on `java.util.regex` by what a pattern matches:
   `^(?:a){2}{3}$` matches `aa` and not `aaaaaa`; `^({3})a$` matches `a`
   and not `{3}a`; `^\c[(a){2}$` matches U+001B followed by `aa`;
   `^(?:ab)\Q\E{2}$` matches `abab` and not `ab`; `^[]a(]{3}$` matches
   `]a(`; `^[^]a(]{3}$` matches `xyz` and not `x]z`; `(?:a)\E{2}` is a
   syntax error.
4. Mutation check (the production file was changed for each run only and
   put back; 19 tests per run, one failure each):

   | Mutation | Failing test |
   | --- | --- |
   | M1: the clock is not checked after `find()` | `resultThatComesAfterTheTimeLimitIsNotUsed` (expected 3 clock reads, was 2) |
   | M2: a quantifier after a quantifier multiplies | `quantifiersNextToEachOtherDoNotMultiply` (expected 1,001, was 1,001,000) |
   | M3: the character after `\c` is not part of the escape | `repetitionsCanNotBeHiddenFromTheBound` (timed out after 20 s) |

5. Not done: a test in a fresh JVM for the system properties (named as
   missing in rounds 2 and 3). The static limits are
   `Limits.from(System::getProperty)`; `Limits.from` is tested with a
   lookup. Recorded as a limit of the run.

## Round 4 result and what was done

Codex: REQUEST_CHANGES with two findings. The finding of round 3 is
closed, the clock overload keeps the production behaviour, and the scope
decision (policy4j-the.4.58 open) stands.

1. MEDIUM: with `.maxRepetition` set to `Long.MAX_VALUE` a weight that
   saturates at `Long.MAX_VALUE` was not above the limit, so
   `(?:(?:(?:){2147483647}){2147483647}){3}` and a pattern in comments
   mode were accepted. Fixed in `matches`: the largest weight is rejected
   whatever the limit is
   (`weight == Long.MAX_VALUE || weight > limits.maxRepetition`). Test:
   `weightThatDoesNotFitIsBeyondEveryLimit` (limit `Long.MAX_VALUE`: the
   overflow pattern and `(?x)a` are Indeterminate inside a 20 s preemptive
   timeout; `(?:(?:){1000}){1000}` still matches).
2. LOW: `limitsAreReadFromProperties` did not exercise the configured time
   limit. It now uses the clock overload: with `.maxMatchMillis=60000` a
   match whose last clock reading is 60,000 ms after the start is
   accepted, and one nanosecond later it is Indeterminate; the same pair
   for the default of 1,000 ms.

3. Also changed by the author after round 4: `.cacheSize` is read through
   the same check as the limits (`cacheSize(lookup)`). Before, it was
   read with `Integer.getInteger`: 0 or a negative value failed only
   when a thread first used the cache, and an unparsable value gave the
   default. Test: `propertyThatIsNotAPositiveNumberIsRejected`.

Mutation check for the two fixes (20 tests per run, made before change 3):

| Mutation | Failing tests |
| --- | --- |
| M4: the largest weight is compared with the limit like any other weight | `weightThatDoesNotFitIsBeyondEveryLimit` (timed out after 20 s) |
| M5: the property `.maxMatchMillis` is not read | `limitsAreReadFromProperties` (limit exceeded at 60,000 ms), `propertyThatIsNotAPositiveNumberIsRejected` |

## Round 5 result and what was done

Codex: REQUEST_CHANGES with two findings at the top of the configurable
range. Both findings of round 4 are closed.

1. MEDIUM: `.cacheSize=2147483647` switched eviction off (`size() >
   maxSize` is never true). Fixed: the largest cache size is
   `Integer.MAX_VALUE - 1` (`LARGEST_CACHE_SIZE`), checked in
   `cacheSize(lookup)` and in the constructor of `PatternCache`.
2. LOW: with `.maxPatternLength=2147483647` and a pattern of 2^30
   characters, `new StringBuilder(2 * length)` in `withoutQuoting` got a
   negative capacity. Fixed in two places: the builder starts with
   `length` and grows; and the largest pattern length that can be
   configured is `Integer.MAX_VALUE / 8` (`LARGEST_MAX_PATTERN_LENGTH`),
   checked in `Limits.from` and in the constructor of `Limits`. The
   rewriting before compilation (`covertXacmlToJavaSyntax`, then
   `withoutQuoting`) makes a pattern at most four times as long (each step
   at most doubles it), so every intermediate length fits in an `int`.

Test: `largestValuesThatCanBeConfigured` (both largest values accepted,
one above rejected, also through the two constructors; the input length,
the time and the repetition limit still take their whole range).

The full reactor passes on the diff below: 7,785 tests, 0 failures, 0 errors, 22 skipped.

## Questions

Report findings ordered by severity (HIGH, MEDIUM, LOW), each with file,
line from the diff, a concrete failing scenario, and a suggested fix. Report
only defects you can show from the diff and the facts above.

1. Are the two findings of round 5 closed?
2. Any other defect in the diff. The scope decision of round 3 stands
   (policy4j-the.4.58 open).

End with a verdict line: `VERDICT: APPROVE` or `VERDICT: REQUEST_CHANGES`.

## Diff

```diff
