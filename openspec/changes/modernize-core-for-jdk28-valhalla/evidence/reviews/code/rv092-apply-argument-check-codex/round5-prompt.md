# Adversarial code review, round 5: function arguments are checked when an Apply is built (xacml4j)

You are an independent reviewer. Do not use tools. Do not run commands. Do
not edit files. Answer in one message. Review only the diff below. It is
the complete diff of this change as it stands after four rounds. It is
smaller than the diff of round 4. The full reactor passes on it:
7,951 tests, 0 failures, 0 errors, 20 skipped. Conformance unchanged: 937 tests, 12 skipped.

## Project

xacml4j is a Java 21 implementation of OASIS XACML 2.0/3.0. `Apply` is the
object model of `<Apply FunctionId="...">`: a function (`FunctionSpec`)
and a list of argument expressions. Policies come from policy authors and
are trusted; requests are not.

## Normative text (XACML 3.0 core, errata 01)

Section 7.4: "Valid expressions SHALL be type correct, which means that
the types of each of the elements contained within <Apply> elements SHALL
agree with the respective argument types of the function that is named by
the FunctionId attribute. [...] XACML defines an evaluation result of
"Indeterminate", which is said to be the result of an invalid expression,
or an operational error occurring during the evaluation of the
expression."

Section 7.19.2: "If a policy that contains invalid syntax is evaluated by
the XACML PDP at the time a decision request is received, then the result
of that policy SHALL be "Indeterminate" with a StatusCode value of
"urn:oasis:names:tc:xacml:1.0:status:syntax-error". If a policy that
contains invalid static data-types is evaluated by the XACML PDP at the
time a decision request is received, then the result of that policy SHALL
be "Indeterminate" with a StatusCode value of
"urn:oasis:names:tc:xacml:1.0:status:processing-error"."

## Defect (bug policy4j-the.4.56)

`Apply` stored the function and the arguments without a check. The
function was invoked with whatever the policy gave. A probe before the
fix, through `Apply.builder(...).build().evaluate(context)`:

| Expression | Result before the fix |
| --- | --- |
| `integer-add(5)` (needs at least two) | value 5 |
| `string-equal(a, b, c)` | value false |
| `integer-bag-size(string-bag(a, b))` | value 2 |
| `integer-add()` | Indeterminate processing-error (NullPointerException) |
| `string-equal(a)` | Indeterminate processing-error (IndexOutOfBoundsException) |
| `integer-add(a, 1)` | Indeterminate processing-error (ArrayStoreException) |
| `integer-bag(1, a)` | Indeterminate processing-error (ArrayStoreException) |
| `not()` | Indeterminate processing-error (IndexOutOfBoundsException) |

Through the PDP, a Permit rule with the condition
`integer-equal(integer-add(5), 5)` gave Permit.

## Decision between two designs

Design A, reject the expression when it is built
(`PolicySyntaxException`), was implemented first and run. It broke four
conformance tests: IIC003 and IIC014 of the XACML 2.0 and 3.0 suites. Their
policies hold `string-equal(string, bag)` and `integer-add(integer,
string)`, and the expected response is Indeterminate with status
processing-error. So such a policy has to load.

Design B, the one in the diff: check once when the expression is built,
keep the result, and make the evaluation Indeterminate with status
processing-error without invoking the function.

## Change (final)

- `Apply`: the constructor decides once whether the expression has a type
  error, and keeps the function id of the wrong expression
  (`Optional<String> typeError`):
  1. an argument that is an `Apply` with a type error, or a variable
     reference whose chain of definitions ends in one, makes this
     expression a type error (`typeErrorInside`; the chain is followed to
     its end);
  2. an argument whose static type cannot be told (`getEvaluatesTo()`
     throws) makes this expression a type error;
  3. a `SyntaxException` from the existing
     `FunctionSpec.validateParametersAndThrow` makes it a type error; any
     other `RuntimeException` from that check too (the arguments are not
     shown to fit).
  Each case is logged at WARN when the policy is loaded.
- `Apply.evaluate`: a type error throws a `FunctionInvocationException`
  (status processing-error, a message that names the function id of the
  wrong expression). The function is not invoked and no argument is
  evaluated. Otherwise the function is invoked as before.
- New accessor `isTypeCorrect()`.
- `AttributeDesignatorFunctions.resolve` (the return type of the
  functions `attribute-designator` and `attribute-selector`): it returned
  the data type; the functions return a bag of it.
- Two existing test classes follow the new behaviour:
  - `ApplyTest` verifies the one call of `validateParametersAndThrow` on
    its mocked function.
  - `HigherOrderVariadicFunctionsTest`: its invalid expressions were
    Indeterminate with status syntax-error when runtime validation was
    off and processing-error when it was on. Now processing-error in
    both modes, and the function is not invoked.

## Rounds 1 to 4 (all REQUEST_CHANGES) and where the design went

- Round 1: an argument of unknown type switched the whole check off.
- Round 2: a type error behind a deciding operand (`or(true, wrong)`) was
  skipped. Fixed by rule 1 above (kept).
- Round 3: a cut of the variable chain at 64; and a known violation next
  to an argument of unknown type. The cut is gone (kept).
- Round 4: with an argument of unknown type the additional validator of a
  higher-order function was skipped, so
  `or(true, any-of(string-equal, 1, <bag of unknown type>))` was true.

Rounds 1, 3 and 4 were three corners of one thing: an expression that was
"checked later" because the type of an argument was not known. That
machinery (a check at evaluation, a stand-in argument, changes in four
parameter specifications and in `FunctionSpecBuilder`) is removed. Rule 2
replaces it: every expression has a type that is known when the policy is
loaded, or the expression that takes it is a type error.

What rule 2 costs: the bag of an `attribute-designator` or
`attribute-selector` function whose data-type argument is not a literal
could be used before, when the types happened to fit at evaluation. It is
Indeterminate now. The same for a data-type literal that names no data
type (before: status syntax-error from the function; now: processing-error
from the expression that takes the bag). No test of the reactor and no
conformance case used either.

The scope stays the expression, as the reviewer accepted in round 3: a
policy is not Indeterminate as a whole when the wrong expression is in a
rule that is not evaluated, in an obligation that is not fulfilled, or in
a variable that is not referenced.

Facts you cannot see in the diff:

- `FunctionSpec.validateParametersAndThrow(List<Expression>)` existed
  before. It normalizes the arguments (an optional parameter without an
  argument gets its default or null; a variadic parameter without an
  argument gets one null), validates every parameter against the static
  type of its argument, rejects surplus and missing arguments with a
  `PolicySyntaxException` (a `SyntaxException`, unchecked, status
  syntax-error), and runs the function's extra validator (the
  higher-order functions have one). Before this change its only caller in
  production code was the validator of `map`.
- `VariableReference.getDefinition().getExpression()` is the expression of
  the variable definition. `VariableDefinition` takes its expression in
  its constructor and has no setter, so a chain of references ends.
- `Apply.getEvaluatesTo()` is `spec.resolveReturnType(arguments)`: the
  declared return type, or the result of a resolver for three functions
  (`map`, `attribute-designator`, `attribute-selector`). A resolver can
  throw for arguments it can not read.
- `FunctionInvocationException(spec, message, params)` has status
  processing-error and the formatted message.
- `Apply` is built in production code by the two XML policy mappers only
  (policy load). The request does not reach the constructor.
- An unknown function id is rejected when the expression is built
  (`PolicySyntaxException`), before and after this change.
- `Match` (the object model of `<Match>`) checks its function and types in
  its constructor and throws `IllegalArgumentException`. Not changed.
- The evaluation context has a flag `isValidateFuncParamsAtRuntime()`
  (default false) that makes `FunctionSpec.invoke` validate on every call.
  Not changed.
- Mutation check of the final code (`ApplyArgumentValidationTest` 40
  tests, `InvalidApplyDecisionTest` 13, `InvalidApplyPolicyUnmarshallerTest`
  8, `HigherOrderVariadicFunctionsTest` 54, `ApplyTest` 2). M1, no call of
  `validateParametersAndThrow` in the constructor: 22, 10, 4, 19 and 2
  failures. M2, `evaluate` ignores a type error: 24, 12, 2 and 19. M3, the
  resolver returns the data type again: 1, and 5 in the existing
  `AttributeResolutionSafetyNetTest`. M4, rules 2 and the "any other
  exception" part of rule 3 switched off: 3 in the core class and 2 in the
  PDP class (`expected: <INDETERMINATE_P> but was: <PERMIT>` for the
  round-4 expression). M5, rule 1 switched off: 5, 6 and 4 (XML).

## Questions

Report findings ordered by severity (HIGH, MEDIUM, LOW), each with file,
line from the diff, a concrete failing scenario, and a suggested fix. Report
only defects you can show from the diff and the facts above.

1. Give an expression, built through `Apply`, that is not type correct
   or holds such an expression, and that still evaluates to a value. If
   you find none, say so.
2. False positive: an expression that is type correct by the declaration
   of its function, with arguments whose types are known, and that the
   check marks as wrong. (Rule 2 marks arguments of unknown type on
   purpose; is that rule a defect given the cost that is stated?)
3. Does the fail-closed part of rule 3 (any other exception of the check
   is a type error) create a risk that you can show?
4. Any other defect in the diff. Name a missing test. Does the spec text
   say what the code does?

End with a verdict line: `VERDICT: APPROVE` or `VERDICT: REQUEST_CHANGES`.

## Diff

(the complete diff of the change followed here)
