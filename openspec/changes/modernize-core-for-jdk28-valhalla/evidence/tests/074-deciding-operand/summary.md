# Run 074: a deciding operand wins over a failed operand (bead the.4.12)

Date: 2026-10-07. Ruling: run 061 D11 (`builds/070-maintainer-rulings`).
JDK: Oracle GraalVM 21.0.6+8.1 (`java version "21.0.6" 2025-01-21 LTS`).
Normative text: XACML 3.0 Plus Errata 01, sections A.3.5 and A.3.12,
fetched from OASIS on 2026-10-07
(`oasis-xacml30-errata01-A.3.5-A.3.12.txt`). Reviews:
`reviews/code/rv081-deciding-operand-codex`,
`reviews/code/rv082-deciding-operand-grok`.

## Findings before the change

| # | Finding | Proof |
| --- | --- | --- |
| 1 | `@XacmlFuncSpec(evaluateArguments=false)` was never read. `JavaMethodToFunctionSpecConverter` did not call `FunctionSpecBuilder.lazyArgEval()`, on this branch and on `master`. `FunctionSpec.invokeWithList` evaluated every argument before the call, so `or` and `and` were not lazy through the engine: `or(true, <error>)` was Indeterminate. | red rows `or TE`, `and FE` |
| 2 | `or`, `and`, the six higher-order functions: the first operand or element that failed ended the evaluation, even when a later one decided the result. | red rows `or ET`, `and EF`, `any-of`, `all-of`, ... |
| 3 | `n-of` evaluated every operand before the call and did not stop early. | red rows `n-of 1 TE`, `n-of 2 FFE` |
| 4 | `or()` and `and()` without arguments failed with a `NullPointerException` (Indeterminate), because the invocation passes `null` for a variadic parameter without arguments. A.3.5 requires False and True. | red rows `or ""`, `and ""` |
| 5 | Quantifier order of `any-of-all` (bead note): the code applies `any-of` to each value of the second bag and the whole first bag. This matches the Errata 01 text. No change. | `oasis-xacml30-errata01-A.3.5-A.3.12.txt` |

## Behaviour

Operands: T true, F false, E failed.

| Function | Operands | Before | After |
| --- | --- | --- | --- |
| `or` | T, E | Indeterminate | True; E is not evaluated |
| `or` | E, T | Indeterminate | True |
| `or` | E, F | Indeterminate | Indeterminate with the status of E; F is evaluated |
| `and` | F, E | Indeterminate | False; E is not evaluated |
| `and` | E, F | Indeterminate | False |
| `n-of` 2 | T, T, E | Indeterminate | True; E is not evaluated |
| `n-of` 2 | E, T, T | Indeterminate | True |
| `n-of` 2 | E, F, F | Indeterminate | False (2 cannot be reached) |
| `n-of` 2 | F, F, E | Indeterminate | False; E is not evaluated |
| `n-of` 2 | E, T, F | Indeterminate | Indeterminate with the status of E |
| `or`, `and` | none | Indeterminate | False, True |
| `any-of`, `any-of-any` | predicate fails for one element, true for a later one | Indeterminate | True |
| `all-of`, `all-of-all` | predicate fails for one element or pair, false for a later one | Indeterminate | False |
| `all-of-any`, `any-of-all` | one `any-of` is Indeterminate, a later one is False | Indeterminate | False |

The status of an Indeterminate result is the status of the first operand
that failed, as before.

## Code

| File | Change |
| --- | --- |
| `xacml-core/.../function/impl/JavaMethodToFunctionSpecConverter.java` | Calls `b.lazyArgEval()` when the annotation has `evaluateArguments=false`. |
| `xacml-core/.../function/impl/DecidingOperands.java` (new, package-private) | `anyTrue`, `allTrue`, `atLeast(n, ...)`: evaluate operands in order, return at the deciding operand, keep the first failure, throw it when nothing decided. |
| `xacml-core/.../function/impl/LogicalFunctions.java` | `or`, `and`, `n-of` use `DecidingOperands`. `n-of` has `evaluateArguments=false`; its first parameter is an `Expression` that is evaluated first. A `null` variadic array is an empty operand list. |
| `xacml-core/.../function/impl/HigherOrderFunctions.java` | `any-of`, `all-of`, `any-of-any`, `all-of-any`, `any-of-all`, `all-of-all` use `DecidingOperands`. `map` is unchanged. |

Only `or`, `and`, and `n-of` carry `evaluateArguments=false` in production
code, so no other function changes from eager to lazy.

## Tests

- New `LaterDecidingOperandTest` (xacml-core, 67 tests), all through the
  function URN and `FunctionSpec.invoke`, the path that `Apply` uses:
  - 33 rows for `or`, `and`, `n-of`: result, status of the Indeterminate
    (the first operand that failed), and which operands are evaluated;
  - 30 rows for the higher-order functions with a test predicate that
    fails with missing-attribute or processing-error for chosen values;
  - the six examples of A.3.12 and the two mixed examples with the bags
    exchanged (`examplesOfTheSpecificationEvaluateToTrue`);
  - quantifier order with `integer-equal`; `n-of` with a failed integer
    (no operand evaluated); number of predicate applications up to the
    deciding element.
- New `LaterDecidingOperandPdpTest` (xacml-pdp, 7 tests): the rule at
  decision level with a required attribute that is missing (M) and a
  division by zero (E): `or(M, true)` is Permit with status ok;
  `and(M, false)` is NotApplicable; `or(M, false)` is Indeterminate with
  missing-attribute; `and(or(M, true), E)` is Indeterminate with
  processing-error; `n-of(2, M, true, true)` is Permit; a Permit rule
  with condition `or(M, true)` whose obligation or advice divides by zero
  is Indeterminate{P} with processing-error.
- Red first: `red-surefire-summary.txt` (55 rows: 7 failures, 25 errors).
  The tests added in the review rounds passed when written; they pin
  behaviour, they were not red.
- Gate: `surefire-summary.txt`, gate 3. Full reactor on JDK 21: 7,745
  tests, 0 failures, 0 errors, 22 skipped. Conformance 937 tests with 12
  skipped; the change against run 071 (927 and 14) belongs to run 075.

## Spec

`specs/function-semantics/spec.md`: new requirement "A deciding argument
wins over a failed argument" with seven scenarios.
`openspec validate modernize-core-for-jdk28-valhalla --strict` passes.

## Reviews

| Lane | Rounds | Final verdict | Record |
| --- | --- | --- | --- |
| Codex | 3 | APPROVE (round 3) | `reviews/code/rv081-deciding-operand-codex` |
| Grok grok-4.7 | 3 runs (one without a report) | APPROVE (round 4) | `reviews/code/rv082-deciding-operand-grok` |

No review finding changed production code. Two findings of Codex about a
stale status in the context were answered with decision-level tests; the
second rested on a wrong statement in the prompt. Grok's finding that
`any-of-all` applies its predicate in the wrong order was answered with the
`any-of-all` example of A.3.12, which the proposed order would fail; Grok
agreed in its next round. The reviews added 7 parameter rows and 4 tests
to `LaterDecidingOperandTest` and all 7 tests of
`LaterDecidingOperandPdpTest`.

## JMH

Not measured. `or` and `and` no longer evaluate arguments after the
deciding one, and each call now goes through one lambda per operand. No
benchmark in the tree covers `or`, `and`, `n-of`, or the higher-order
functions. Recorded as unmeasured, not as accepted performance; the JMH
decision belongs to gate 4.7.

## Limits

- An attribute reference with `MustBePresent=true` records its
  missing-attribute status in the context before it throws. When `or`
  then returns True, that status stays in the context. No path that a
  request can reach reports it: the code that catches a later failure
  (condition, target, obligations and advice in `BaseDecisionRule`)
  overwrites the status, and `LaterDecidingOperandPdpTest` shows it for a
  condition, an obligation, and an advice. Bug policy4j-the.4.55 was
  opened for this during the review on a wrong reading of the code and
  carries the correction.
- `LogicalFunctions.nof` now takes `Expression` as its first Java
  parameter (was `IntegerVal`). Java code that calls the static method
  directly must be compiled again; calls through the registry are not
  affected.
- `map` still fails on the first element that fails.
- No exception type is exempt from the rule. A future evaluation limit
  (bead the.4.50) must not be an exception that `or` can defer.
