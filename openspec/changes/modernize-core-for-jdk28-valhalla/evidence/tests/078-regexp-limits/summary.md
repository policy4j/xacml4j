# Run 078: limits on the regular-expression functions (bead the.4.51)

Date: 2026-10-07. Ruling: run 061 D17, option (a)
(`builds/070-maintainer-rulings`). Source of the bead: run 063 (security
sweep). JDK: Oracle GraalVM 21.0.6+8.1
(`java version "21.0.6" 2025-01-21 LTS`). Review:
`reviews/code/rv087-regexp-limits-codex`.

## Defect

| | Before |
| --- | --- |
| Match time | `java.util.regex` backtracks without a limit. `(.*a){20}$` on `"a" x 40 + "!"` does not return within 3 seconds on this JDK (`regex-probe.txt`). The pattern comes from the policy, the input can come from the request. |
| Input and pattern length | no limit |
| Pattern cache (`...RegExpFunctions.cacheRegExps=true`) | per-thread `HashMap`, never evicted |

## Change

`xacml-core/.../function/impl/RegExpFunctions.java`:

| Limit | Default | System property (`org.xacml4j.v30.policy.function.impl.RegExpFunctions` + suffix) | Enforced |
| --- | --- | --- | --- |
| pattern length | 10,000 characters | `.maxPatternLength` | before the pattern is rewritten or compiled |
| input length | 100,000 characters | `.maxInputLength` | before the pattern is rewritten or compiled |
| time of one match, compilation included | 1,000 ms | `.maxMatchMillis` | the clock starts before the pattern is rewritten and compiled and is checked after `Pattern.compile`; then `DeadlineCharSequence.charAt` reads the clock once per 1,024 calls and throws after the deadline; the clock is checked once more after `find()`, and a late result is discarded |
| nested counted repetitions | 1,000,000 | `.maxRepetition` | `repetitionWeight`: the largest product of the minimum counts of `{n}`, `{n,}`, `{n,m}` that are nested in each other; checked before the pattern is compiled. A weight that does not fit in a `long` and a pattern that can not be scanned are rejected whatever the limit is |
| cached patterns per thread | 256 | `.cacheSize` | `PatternCache`: access-ordered `LinkedHashMap` with `removeEldestEntry` |

- Beyond a limit: `EvaluationException` with status processing-error and
  the fixed message `Regular expression evaluation limit exceeded`
  (Indeterminate). The message names neither the pattern nor the input.
- `matches(String, String)` keeps its signature and uses
  `Limits.from(System::getProperty)`, read once. A value that is not a
  number between 1 and its maximum throws `IllegalArgumentException` with
  the property name while the class initializes (an unparsable value does
  not fall back to the default). `.cacheSize` is read through the same
  check (`cacheSize(lookup)`). `matches(String, String, Limits)` and
  `Limits.from(lookup)` are package-private for the tests.
- `matches(String, String, Limits, LongSupplier)` takes the clock; the other overloads pass
  `System::nanoTime`. The tests give their own clock readings.
- The largest values that can be configured: pattern length
  `Integer.MAX_VALUE / 8` (a pattern can become up to four times as long
  while it is rewritten), cache size `Integer.MAX_VALUE - 1` (the cache
  evicts when its size is above its limit). The other limits take their
  whole range.
- The cache is still used only when `.cacheRegExps` is true (unchanged).

The repetition bound exists because the time limit works through reads of
the input: a group that matches the empty string is repeated its minimum
count without a read (`regex-probe.txt`, second table). `*`, `+` and `?`
stop on an empty iteration and need no bound.

How `repetitionWeight` reads a pattern (class `RepetitionScanner`):

| Construct | Rule | Source of the rule |
| --- | --- | --- |
| `\Q..\E` | removed before the scan by `withoutQuoting`: quoted characters become escaped literals, an empty quote disappears | `Pattern.RemoveQEQuoting`; `patternWithoutQuotingMeansTheSame` compares `find()` of both forms |
| escapes | two characters; three for `\cX`; to the closing brace for `\p{`, `\P{`, `\x{`, `\N{`; to `>` for `\k<` | `Pattern.escape` |
| character class | nested with `[`; a `]` directly after `[` or `[^` is a literal | `Pattern.clazz`; probe 5 |
| counted quantifier | counted wherever it stands outside a class or an escape; it multiplies the weight of the group that was just closed, or 1: at the start of a group and after another quantifier `java.util.regex` repeats an empty atom of its own | `Pattern.sequence`, `Pattern.closure`; `scannerRulesAreTheRulesOfJavaUtilRegex` |
| comments mode (`(?x)`, `(?ix:`) | largest weight, rejected: a `#` comment could hide a quantifier | not followed by the scanner |

The ruling gives no numbers. The five defaults were chosen in this run and
are a proposal: they need the maintainer's confirmation.

## Tests

- `RegExpLimitsTest` (21 tests): defaults; a match inside the limits and
  exactly at both length limits; `(.*a){20}$` stopped by a 50 ms limit in
  less than 2 s; nested counted repetitions of an empty group and of `\A`
  rejected before the match, and accepted at the bound; the weight of ten
  patterns; comments mode rejected; five constructs that hid a repetition
  from the first scanner, rejected (`\c[`, an empty quote, a quantifier
  directly after `(`, a class with a leading `]` after `[` and after
  `[^`) and seven weights for them; quantifiers next to each other do not
  multiply; braces in a class are no repetition; the pattern without
  quoting means the same (13 patterns on 18 inputs); the rules of the
  scanner shown on `java.util.regex` by what seven patterns match and do
  not match; the
  clock checks, with given clock readings and counted clock reads (on
  time, late after the match, late after the compilation, late while
  reading, on time while reading); limits read from properties, each at
  its configured value and one beyond it, the time limit through the
  clock; a weight that does not fit is beyond the largest limit; a
  property that is not a positive number rejected, also the cache size;
  the largest values that can be configured; input and pattern one character
  above the limit; limits must be positive; `string-regexp-match` through
  the registry at and above the default input limit; LRU order of the
  cache.
- The two tests whose patterns do not return without the bound run inside
  `assertTimeoutPreemptively` (20 s).
- Red first: `red-surefire-summary.txt`.
  - Run 1 (limits declared but not enforced): 8 tests, 6 failures. The
    time-limit test still used `(a+)+$`, which JDK 21 evaluates at once,
    so its failure there says only that no exception was thrown. No red
    run was made with `(.*a){20}$`, because without the limit it only runs
    into the 20 s test timeout.
  - Run 2 (tests of review round 2 against the round-2 code, with two
    compile shims): 17 tests, 5 failures, of which two show the defects
    (`repetitionsCanNotBeHiddenFromTheBound` timed out after 20 s,
    `bracesInACharacterClassAreNotARepetition` got 1,000,001) and three
    come from the shims.
  - The repetition tests of round 1 were not run against the round-1
    code; the probe shows the behaviour without the bound.
- Mutation check after rounds 3 and 4: `mutation-check.txt`. Five
  guards were removed one at a time (the clock check after `find()`, the
  rule for quantifiers next to each other, the `\c` rule, the rejection
  of the largest weight, the time-limit property). Each time the test of
  that guard fails.
- Gate: `surefire-summary.txt`, gate 8 (clean build). Full reactor on
  JDK 21: 7,785 tests, 0 failures, 0 errors, 22 skipped. Conformance 937
  tests with 12 skipped, unchanged.

## Spec

`specs/request-limits/spec.md`: requirement "Regular-expression matching
is bounded" with ten scenarios. Its last sentence says what is not
bounded. `openspec validate modernize-core-for-jdk28-valhalla --strict`
passes.

## Review

`reviews/code/rv087-regexp-limits-codex`: Codex, six rounds. Rounds 1 to 5
REQUEST_CHANGES; every finding was fixed except the second HIGH of round
2, which is bug policy4j-the.4.58. The reviewer accepted that scope in
round 3. Round 6 APPROVE, no finding.

A second review by another model family (Grok) was started on the
approved diff. It had given no report after 20 minutes when this run was
recorded. If it reports, its record is
`reviews/code/rv089-regexp-limits-grok`.

## Course of rounds 2 to 5

After the round-2 report the scanner was first removed from the working
tree, on the reasoning that a scanner which can be bypassed should not be
kept. That would have reopened finding 1 of round 1 (the stall of nested
counted repetitions). The removal was undone before any test or review
ran on it: the reviewed round-2 file was restored from its diff and then
hardened as described above.

The first test of the late result measured the time of a slow pattern and
set the limit to a quarter of it. Probe 6 showed that such times are not
stable on this JVM, and round 3 noted that the test did not isolate the
check after `find()`. It was replaced by a test with given clock readings.
The timing version had failed against the round-2 code
(`red-surefire-summary.txt`, end).

Round 4 found that a limit of `Long.MAX_VALUE` accepted a weight that
had saturated at the same value. The largest weight is now rejected
whatever the limit is.

Round 5 found two values at the top of the `int` range that switched a
check off or overflowed (cache size, pattern length). Both now end below
it.

## JMH

Not measured. Every match now reads its input through
`DeadlineCharSequence` (one decrement and one branch per character, one
clock read per 1,024 characters) instead of a `String`, and every pattern
is scanned once before it is compiled. No JMH module exists yet (task
9.2). Recorded as unmeasured, not as accepted performance.

## Limits

- OPEN, bug policy4j-the.4.58 (priority 1, waits for the maintainer): a
  pattern can make the matcher work without a read of the input in
  another way than counted repetitions. 26 groups with two zero-width
  alternatives each (`(?:(?=)|(?=))`, then `(?!)`) take 2,092 ms on an
  empty input (`regex-probe.txt`, probe 4); 40 groups were not run and
  would take 2^14 times longer by extrapolation. Such a match is not
  stopped. Only its late result is discarded. The pattern is normally a
  literal of the policy, but a policy can take it from a request
  attribute.
- `Pattern.compile` cannot be interrupted. Its time counts against the
  limit and is checked when it is over; the pattern length limit bounds
  it.
- `repetitionWeight` is a scanner written after four methods of
  `java.util.regex.Pattern`, not that parser itself. It was compared with
  them by reading and by the probes and tests named above. That it reads
  every pattern as JDK 21 does is not proven, and another JDK can read a
  pattern differently.
- The scanner takes the product of nested minimum counts also where
  `java.util.regex` stops a loop early (a group with an alternative or
  an optional part, probe 4). It errs towards rejection there.
- The weight is a maximum over repetitions that stand next to each
  other, not a sum. A pattern at the length limit can hold about 700 of
  them inside one repetition; that case ran in 48 ms (`regex-probe.txt`,
  third probe).
- `ripwire --quality-delta` flagged the first version of the scanner
  (one method, complexity 48 against a bar of 15); it was split into
  `RepetitionScanner`. After round 3 it still flags `withoutQuoting`
  (complexity 30). That method is kept in the shape of
  `Pattern.RemoveQEQuoting`, branch for branch, so that the two can be
  compared; it was not split.
- The static limits are read once per JVM. The tests read limits through
  `Limits.from(lookup)`; no test starts a fresh JVM with the system
  properties set.
- An `Error` from the regular-expression engine, for example a
  `StackOverflowError`, is not caught. Whether one can occur inside the
  default limits was not tested in this run.
- The limits are per JVM, not per PDP.
- With the cache enabled, each thread holds up to `cacheSize` compiled
  patterns.
