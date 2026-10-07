# Adversarial code review: integer arithmetic does not wrap (xacml4j)

You are an independent reviewer. Do not use tools. Do not edit files. Answer
in one message. Review only the diff below. It is the complete diff of this
change. The full reactor passes on it: 7,828 tests, 0 failures, 0 errors,
20 skipped (before: 7,785 tests, 22 skipped; the two tests that are no
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

## Questions

Report findings ordered by severity (HIGH, MEDIUM, LOW), each with file,
line from the diff, a concrete failing scenario (function and operands),
and a suggested fix. Report only defects you can show from the diff and
the facts above.

1. Is there a call of one of the six integer functions that still returns
   a wrapped or otherwise wrong value, or that is Indeterminate although
   its mathematical result fits in 64 bits?
2. Is the fallback to `BigInteger` right (zero among the factors, signs,
   the order of the operands, the catch of `ArithmeticException` from
   `longValueExact`)? Can it be made to use much time or memory by the
   operands?
3. Does a new `ArithmeticException` from `IntegerVal` escape somewhere as
   something else than Indeterminate processing-error?
4. Do the tests pin the boundaries? Name a missing test.
5. Does the spec requirement say what the code does?

End with a verdict line: `VERDICT: APPROVE` or `VERDICT: REQUEST_CHANGES`.

## Diff

```diff
