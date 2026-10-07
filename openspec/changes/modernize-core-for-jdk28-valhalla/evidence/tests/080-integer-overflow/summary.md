# Run 080: integer arithmetic does not wrap (bead the.4.59, ruling D4)

Date: 2026-10-07. Ruling: run 061 D4, option (a)
(`builds/070-maintainer-rulings`): an integer result outside 64 bits is
Indeterminate with status processing-error. JDK: Oracle GraalVM 21.0.6+8.1
(`java version "21.0.6" 2025-01-21 LTS`). Review:
`reviews/code/rv089-integer-overflow-kimi`.

## Placement

The plan lists ruling D4 under task 5.4, which also replaces the integer
type with the donor type. This run applies the ruling to the current
`IntegerVal` in phase 4, as a fix of an existing deviation. That is a
choice of this run, not of the plan. Task 5.4 keeps the rest of its scope
(one owner of `xs:integer`, strict integer lexing, product identity, null
stubs) and has to keep the tests of this run green on the donor types.

## Defect

| Call | Before | Now |
| --- | --- | --- |
| `integer-add(9223372036854775807, 1)` | `-9223372036854775808` | Indeterminate, processing-error |
| `integer-subtract(-9223372036854775808, 1)` | `9223372036854775807` | Indeterminate, processing-error |
| `integer-multiply(4294967296, 4294967296)` | `0` | Indeterminate, processing-error |
| `integer-abs(-9223372036854775808)` | `-9223372036854775808` | Indeterminate, processing-error |
| `integer-divide(-9223372036854775808, -1)` | `-9223372036854775808` | Indeterminate, processing-error |

The "Before" column is what the Java `long` operators give. The red run
shows only that no error was raised for these calls; it does not print the
wrapped values.

## Change

`xacml-core/.../function/impl/ArithmeticFunctions.java`,
`xacml-core/.../types/IntegerVal.java`:

| Function | Now |
| --- | --- |
| `integer-add`, `integer-multiply` | exact `long` arithmetic; when a step leaves the range, the result is computed again with `BigInteger` and taken with `longValueExact()`, so an intermediate value outside the range does not matter when the result is inside it |
| `integer-subtract`, `integer-abs` | `IntegerVal.subtract`, `IntegerVal.abs` with `Math.subtractExact`, `Math.absExact` |
| `integer-divide` | `Math.divideExact`; the zero divisor is rejected before it, as before |
| `integer-mod` | unchanged; the remainder of the smallest integer by `-1` is `0` |

- Outside the range: `EvaluationException` with status processing-error
  and the message `<function> result is outside the xs:integer range`. The
  message names the function, not the operands. The `ArithmeticException`
  is the cause of the exception.
- `IntegerVal.add`, `subtract`, `multiply`, `abs` throw
  `ArithmeticException` instead of wrapping. A text search over the main
  files that name `IntegerVal` found callers only in `ArithmeticFunctions`
  (`subtract`, `abs`).

## Tests

- New `IntegerOverflowTest` (44 tests), through the default function
  registry: 26 calls of the six functions whose result is inside the
  range, among them the largest and smallest results, the largest square,
  and four calls with an intermediate value outside the range; 17 calls
  whose result is outside the range, each Indeterminate with
  processing-error, the message above, and an `ArithmeticException` in
  the cause chain; the four methods of `IntegerVal`.
- `FunctionSafetyNet43Test`: rows 35 and 36 (decision C3) are no longer
  disabled and require processing-error. The class has no disabled test
  now (122 tests, 0 skipped).
- Red first: `red-surefire-summary.txt` (the first version of the test,
  before the review added one row and the cause assertion). `IntegerOverflowTest` 43 tests, 18
  failures (the 17 calls outside the range and the test of the value
  type); `FunctionSafetyNet43Test` 122 tests, 2 failures (rows 35 and 36).
  The 25 calls inside the range passed on the old code: wrapping
  arithmetic gives the right result whenever the result fits.
- Gate: `surefire-summary.txt`, gate 2 (clean build, after the review
  notes). Full reactor on JDK 21: 7,829 tests, 0 failures, 0 errors, 20
  skipped (before: 7,785 tests, 22 skipped; 44 new tests, and the two C3
  rows run). Conformance 937 tests
  with 12 skipped, unchanged.

## Spec

`specs/function-semantics/spec.md`: new requirement "Integer arithmetic
does not wrap" with five scenarios.
`openspec validate modernize-core-for-jdk28-valhalla --strict` passes.

## Review

`reviews/code/rv089-integer-overflow-kimi`: Kimi, two rounds, APPROVE in
both. Round 1 had four LOW notes; two led to changes (the cause of the
exception is kept; one more boundary row). Round 2 had two LOW notes on
style, not changed. Codex was started twice and gave no report. One
model family reviewed this run.

## JMH

Not measured. `integer-add` and `integer-multiply` use `Math.addExact` and
`Math.multiplyExact` per operand in place of `+` and `*`; the `BigInteger`
path runs only after a step has left the range. No JMH module exists yet
(task 9.2). Recorded as unmeasured, not as accepted performance.

## Limits

- Not changed: the factories of `IntegerVal` (`of(BigInteger)`,
  `of(Number)`, `ofAny(DoubleVal)`), which truncate a value that does not
  fit, and integer lexing. Both stay with task 5.4.
- A call of `integer-add` or `integer-multiply` without arguments still
  fails with a `NullPointerException` (processing-error): the number of
  arguments of a variadic function is not checked (bug
  policy4j-the.4.56).
- The caller list of the `IntegerVal` methods comes from a text search,
  not from a type-checked reference search (Serena was unavailable in
  this session). The full reactor passes.
- Other Java code that called `IntegerVal.add`, `subtract`, `multiply` or
  `abs` and relied on wrapping now gets an `ArithmeticException`.
