# Adversarial code review: limits on regular-expression functions (xacml4j)

You are an independent reviewer. Do not use tools. Do not edit files. Answer
in one message. Review only the diff below. It is the complete diff of this
change. The full reactor passes on it: 7,765 tests, 0 failures, 0 errors, 22 skipped; conformance
unchanged.

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

End with a verdict line: `VERDICT: APPROVE` or `VERDICT: REQUEST_CHANGES`.

## Diff

```diff
