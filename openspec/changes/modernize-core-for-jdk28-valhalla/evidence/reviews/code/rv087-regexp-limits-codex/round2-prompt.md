# Adversarial code review, round 2: limits on regular-expression functions (xacml4j)

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

A first run of this round gave no report (the network dropped while the
reviewer ran); this packet replaces it.

The full reactor passes: 7,775 tests, 0 failures, 0 errors, 22 skipped.

## Questions

Report findings ordered by severity (HIGH, MEDIUM, LOW), each with file,
line from the diff, a concrete failing scenario, and a suggested fix. Report
only defects you can show from the diff and the facts above.

1. Can a match still run much longer than `maxMatchMillis`? Consider code
   in `java.util.regex` that does not call `charAt` for a long time, the
   1,024-call interval, `Pattern.compile` of a hostile pattern, and
   `subSequence`/`toString` of the wrapper.
2. Is the deadline arithmetic right (`System.nanoTime()` overflow, zero or
   negative limits, the counter)?
3. Does the wrapper change a match result (surrogate pairs, regions,
   `find()` semantics) compared with matching the `String` directly?
4. Is the cache bounded and is its LRU order right? Is the per-thread cache
   a memory problem with many threads?
5. Does the Indeterminate result leak the pattern or the input?
6. Do the tests pin the limits? Name a missing test.
7. `repetitionWeight`: can a pattern have a small weight and still loop
   for long without reading input? Can a harmless pattern be rejected
   because the scanner misreads it (escapes, classes, quoting, group
   syntax such as `(?<name>` or `(?=`)?
   Is there another way than comments mode to hide a quantifier from
   the scanner (a `[` or `]` that the scanner and `java.util.regex` read
   differently, a flag set in another way)?

End with a verdict line: `VERDICT: APPROVE` or `VERDICT: REQUEST_CHANGES`.

## Diff

```diff
