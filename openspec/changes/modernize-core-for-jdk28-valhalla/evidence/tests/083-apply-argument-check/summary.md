# Run 083: function arguments are checked when an Apply is built (bead the.4.56)

Date: 2026-10-08. Source of the bead: run 076 (found while fixing
the.4.53). JDK: Oracle GraalVM 21.0.6+8.1
(`java version "21.0.6" 2025-01-21 LTS`). Review:
`reviews/code/rv092-apply-argument-check-codex`.

## Decision basis

The bead listed three options and asked for a maintainer decision. The
recommendation of this session was option a, reject at policy load. It
was carried out on the maintainer's standing instruction to implement the
recommendations, and it failed on the conformance suite (see "Design A").
The design that was kept checks at the same moment and answers at
evaluation. The maintainer can still ask for a strict mode that rejects.

## Normative text

XACML 3.0 core, errata 01
(https://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html,
fetched in run 074; excerpt in `oasis-xacml30-errata01-7.4-7.19.2.txt`):

- Section 7.4: "Valid expressions SHALL be type correct, which means that
  the types of each of the elements contained within <Apply> elements
  SHALL agree with the respective argument types of the function that is
  named by the FunctionId attribute."
- Section 7.19.2: "If a policy that contains invalid static data-types is
  evaluated by the XACML PDP at the time a decision request is received,
  then the result of that policy SHALL be "Indeterminate" with a
  StatusCode value of
  "urn:oasis:names:tc:xacml:1.0:status:processing-error"."

## Defect

`xacml-core/.../policy/Apply` stored the function and the arguments
without a check, and the function was invoked with whatever the policy
gave. `probe-before.txt`: `integer-add(5)` was 5, `string-equal(a, b, c)`
was false, `integer-bag-size(string-bag(a, b))` was 2. Other wrong calls
were Indeterminate by a failure inside the invocation
(`NullPointerException`, `IndexOutOfBoundsException`,
`ArrayStoreException`), not by a check. Through the PDP a Permit rule
with the condition `integer-equal(integer-add(5), 5)` gave Permit
(`mutation-check.txt`, M1).

## Design A, not kept

`Apply.Builder.build()` threw `PolicySyntaxException`.
`design-a-reject-at-build.txt`: 31 tests of the reactor failed, among them
IIC003 and IIC014 of both conformance suites ("Cannot import ... policy").
Their policies hold `string-equal(string, bag)` and
`integer-add(integer, string)`, and the expected response is Indeterminate
with status processing-error. Such a policy has to load.

## Change

- `Apply`: the constructor decides once whether the expression has a type
  error. Three rules:
  1. an argument that is an expression with a type error, directly or at
     the end of a chain of variable references, makes this expression a
     type error, also when its function would not evaluate that argument
     (`or(true, x)`);
  2. an argument whose type can not be told when the expression is built
     (its `getEvaluatesTo()` throws) makes this expression a type error;
  3. the arguments are checked with the existing
     `FunctionSpec.validateParametersAndThrow`. A failure of that check
     of any kind is a type error.
  Each case is logged at WARN when the policy is loaded.
- `Apply.evaluate`: a type error throws a `FunctionInvocationException`
  with status processing-error and the message
  `Function="<id>" is applied to arguments that do not fit it`, with the
  id of the innermost wrong expression. The function is not invoked and
  no argument is evaluated.
- `Apply.isTypeCorrect()`: false when a type error was found.
- `AttributeDesignatorFunctions.resolve`: the functions
  `attribute-designator` and `attribute-selector` return a bag; their
  declared type was the data type itself. With the check every use of
  them as a bag was a type error until this was corrected.
- An unknown function id is rejected when the expression is built, as
  before.
- No method of the new code returns null (maintainer note of 2026-10-08).

## Tests

- New `ApplyArgumentValidationTest` (40 tests, xacml-core): 16 expressions
  that are not type correct, 13 that are (among them `or()`, `and()`,
  `n-of(0)`, `integer-bag()`), a wrong expression inside a right one, a
  type error behind the deciding operand of `or`, `and` and `n-of`, a
  type error in a variable definition and behind chains of 1, 64, 65 and
  500 variable references, an unknown function, the bag type of the two
  designator functions, an argument of unknown type (alone, next to
  another violation, in a higher-order function, with a data type that
  does not exist), and a type-correct operand that fails at evaluation
  behind a deciding operand (it is still skipped).
- New `InvalidApplyDecisionTest` (13 tests, xacml-pdp): a Permit rule and
  a Deny rule with such a condition (six conditions) are Indeterminate{P}
  and Indeterminate{D} with processing-error; a right condition gives
  Permit.
- New `InvalidApplyPolicyUnmarshallerTest` (8 tests, xacml-xml): the XACML
  2.0 and 3.0 policy mappers load such a policy, mark the expression and
  give processing-error; an unknown function is rejected.
- Changed tests, both in xacml-core:
  - `ApplyTest`: its two tests verify every call on a mocked function.
    They now verify the one call of `validateParametersAndThrow` too. A
    third test, asked for in review round 5, makes that call throw an
    `IllegalStateException`: a type error, and no invocation. It passed
    at once.
  - `HigherOrderVariadicFunctionsTest`: 10 test methods in two modes
    evaluate expressions that are not type correct. With runtime
    validation off their status was syntax-error (the function was
    invoked and its own check threw). It is processing-error in both
    modes now, and the function is not invoked. The helper asserts
    `isTypeCorrect()` is false and that the context was not asked for
    the validation mode; the assertion that it was asked is removed.
- Red first:
  - `red-surefire-summary.txt`: the first version of the new core class
    against `Apply` without the check (33 tests, 18 failures).
  - `red-3-surefire-summary.txt`: a type error inside an expression
    (review round 2; through the PDP `expected: <INDETERMINATE_P> but
    was: <PERMIT>`).
  - `red-4-surefire-summary.txt`: a long chain of variable references,
    and a known violation next to an argument of unknown type (review
    round 3).
  - `red-2-surefire-summary.txt`: an argument of unknown type gave a
    value (review round 1). The fix of that round, a check at
    evaluation, was replaced by rule 2 after round 4; the test for the
    case is now `argumentWhoseTypeIsNotKnownWhenTheExpressionIsBuiltIsATypeError`.
  - The classes of xacml-pdp and xacml-xml were written after the check;
    `mutation-check.txt` M1 is their run against the code before it, and
    M4 is the run for the case of review round 4.
- `mutation-check.txt`: five mutations of the final code, all detected.
- Gate: `surefire-summary.txt`, six clean builds; the last one is the
  tree as committed: 7,952 tests, 0 failures, 0 errors, 20 skipped.
  Conformance 937 tests with 12 skipped, unchanged: IIC003 and IIC014
  pass as before.

## Spec

`specs/function-semantics/spec.md`: new requirement "Function arguments
are checked when the expression is built" with fourteen scenarios.
`openspec validate modernize-core-for-jdk28-valhalla --strict` passes.

## Review

`reviews/code/rv092-apply-argument-check-codex`: Codex, five rounds.
Rounds 1 to 4 REQUEST_CHANGES, one or two MEDIUM findings each: an
argument of unknown type switched the check off; a type error behind a
deciding operand was skipped; a cut of the variable chain and a known
violation next to an unknown type; the additional validator of a
higher-order function was skipped next to an unknown type. Rounds 1, 3
and 4 were corners of one mechanism (a check that waited for the
evaluation); it was removed after round 4 and replaced by rule 2. Round 5
APPROVE with one LOW on the spec wording and one missing test, both
applied.

## JMH

Not measured. The check runs once per `<Apply>` when a policy is loaded.
An evaluation reads one field more. No JMH module exists yet (task 9.2).

## Limits

- Behaviour change: an expression that is not type correct and gave a
  value before is Indeterminate now. A policy that relied on
  `integer-add` with one argument or on a surplus argument changes its
  decisions. Each such expression is named in a WARN line when the policy
  is loaded.
- Behaviour change: the bag of an `attribute-designator` or
  `attribute-selector` function whose data-type argument is not a
  literal could be used before, when the types fitted at evaluation. The
  expression that takes it is a type error now. No test of the reactor
  and no conformance case used that form. A data-type literal that names
  no data type gave syntax-error from the function; it gives
  processing-error from the expression that takes the bag.
- Status change: the higher-order functions with wrong arguments were
  syntax-error with runtime validation off; they are processing-error
  now, as section 7.19.2 has it for invalid static data types.
- Ruling D11 is narrowed in one point: a type error is not skipped behind
  a deciding operand. `or(true, x)` with a type error in `x` was true and
  is Indeterminate now. A type-correct operand that fails at evaluation
  is still skipped.
- Section 7.19.2 speaks of the policy. This run makes the expression
  Indeterminate and leaves the rest to the standard rules. A policy is
  not Indeterminate as a whole when the wrong expression is in a rule
  that the combining algorithm does not evaluate or whose target does
  not match, in an obligation or advice that is not fulfilled, or in a
  variable definition that is not referenced. In each case the result is
  the one that the rules give for an Indeterminate expression.
- A check that fails for another reason than a mismatch makes the
  expression a type error too. A defect in a validator would show as an
  Indeterminate expression with a WARN line, not as a wrong value.
- `Match` still rejects a function that does not fit when it is built
  (`IllegalArgumentException`), and an unknown function id is still
  rejected at load. Section 7.19 gives Indeterminate for both when the
  policy is evaluated; not changed here.
- `FunctionSpec.invoke` called directly, without `Apply`, is checked only
  with the runtime-validation flag, as before.
- The WARN lines name policy content (the function, the parameter, the
  argument expression). No request data: the check runs when the policy
  is loaded.
