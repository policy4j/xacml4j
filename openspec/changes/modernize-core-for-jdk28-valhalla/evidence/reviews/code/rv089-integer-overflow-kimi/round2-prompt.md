# Adversarial code review, round 2: integer arithmetic does not wrap (xacml4j)

You are an independent reviewer. Do not use tools. Do not edit files. Answer
in one message. Review only the diff below. It is the complete diff of this
change. The full reactor passes on it: 7,829 tests, 0 failures, 0 errors, 20 skipped
(before the change: 7,785 tests, 22 skipped; the two tests that are no
longer skipped are rows 35 and 36 below). Conformance unchanged: 937 tests,
12 skipped.

## Project

xacml4j is a Java 21 implementation of OASIS XACML 2.0/3.0 (compiled with
`--release 21`). `ArithmeticFunctions` holds the XACML arithmetic functions
of Appendix A.3.2. `IntegerVal` is the value of `xs:integer`; it holds a
Java `Long`.

## Defect

The integer functions used the Java `long` operators, which wrap:
`integer-add(9223372036854775807, 1)` returned `-9223372036854775808`,
`integer-abs(-9223372036854775808)` returned the same negative number,
`integer-multiply(4294967296, 4294967296)` returned `0`,
`integer-divide(-9223372036854775808, -1)` returned
`-9223372036854775808`. A policy then compares a wrong value without an
error.

## Maintainer ruling this change implements (2026-10-07, D4)

"Integer overflow outside 64 bits is Indeterminate processing-error" (the
other option, arbitrary precision, was not chosen). The plan lists the
ruling under a later task that also replaces the integer type; this change
applies the ruling to the current type now. Not in scope, and left to that
later task: the factories of `IntegerVal` (`of(BigInteger)`, `of(Number)`,
`ofAny(DoubleVal)`), which truncate, and integer lexing.

## Change

- `integer-add`, `integer-multiply`: exact `long` arithmetic; when a step
  leaves the range, the result is computed again with `BigInteger` and
  taken with `longValueExact()`, so that an intermediate value outside the
  range does not matter when the result is inside it.
- `integer-subtract`, `integer-abs`: through `IntegerVal.subtract` and
  `IntegerVal.abs`, which now use `Math.subtractExact` and `Math.absExact`.
- `integer-divide`: `Math.divideExact` (the zero divisor is rejected before
  it, as before).
- `integer-mod`: unchanged. In Java the remainder of the smallest `long`
  by `-1` is `0`.
- A result outside the range: `EvaluationException` with status
  processing-error and the message `<function> result is outside the
  xs:integer range`.
- `IntegerVal.add`, `subtract`, `multiply`, `abs` throw
  `ArithmeticException` instead of wrapping.

Facts you cannot see in the diff:

- A function is called through `DefaultFunctionInvocation`. It wraps
  every exception of the function in a `FunctionInvocationException`; the
  first `EvaluationException` in the cause chain keeps its status and
  message, and any other exception becomes processing-error. So an
  `IllegalArgumentException` from `Preconditions.checkArgument` (zero
  divisor) and an `ArithmeticException` from `%` by zero were and are
  Indeterminate with processing-error.
- `EvaluationException(Status, String)`: the tests read the status message
  through `e.getStatus().getMessage()`.
- A text search over the main files of `xacml-core`, `xacml-pdp`,
  `xacml-xml`, `xacml-json` and `xacml-test` that name `IntegerVal` found
  these callers only: `IntegerVal.subtract` and `IntegerVal.abs` in
  `ArithmeticFunctions`. `IntegerVal.add` and `IntegerVal.multiply` have
  no caller there. This is a search result, not a type-checked
  reference list.
- The number of arguments of a variadic function is not checked before
  the call (a separate open bug). A call of `integer-add` without
  arguments fails with a `NullPointerException` in the loop, before and
  after this change.
- `double-to-integer` already rejects a value outside the range (an
  earlier change).
- Red run of the new tests on the old code: `IntegerOverflowTest` 43
  tests, 18 failures (the 17 rows outside the range and the test of the
  value type); the 25 rows inside the range passed, because wrapping
  arithmetic gives the right result whenever the result fits.
  `FunctionSafetyNet43Test` rows 35 and 36 failed.

## Round 1 result and what was done

Kimi: APPROVE with four LOW notes.

1. L1, the `ArithmeticException` was dropped: `outsideIntegerRange` now
   takes it and passes it as the cause of the `EvaluationException`. The
   status message is unchanged. `IntegerOverflowTest` asserts that an
   `ArithmeticException` is in the cause chain of each of the 17 calls
   outside the range.
2. L2, public methods of `IntegerVal` now throw: no code change; recorded
   as a limit of the run.
3. L3, the largest square inside the range was not pinned: row
   `integer-multiply(3037000499, 3037000499) = 9223372030926249001` added.
4. L4, zero divisor: `FunctionSafetyNet43Test` rows 39 and 43 already
   require processing-error for `integer-divide(1, 0)` and
   `integer-mod(5, 0)`; they are outside this diff and pass.

## Questions

Report findings ordered by severity (HIGH, MEDIUM, LOW), each with file,
line from the diff, a concrete failing scenario, and a suggested fix.

1. Are L1 and L3 closed as described?
2. Does the cause that is now attached change the status or the status
   message of the result, given the facts above about
   `DefaultFunctionInvocation`?
3. Any other defect in the diff.

End with a verdict line: `VERDICT: APPROVE` or `VERDICT: REQUEST_CHANGES`.

## Diff

```diff
