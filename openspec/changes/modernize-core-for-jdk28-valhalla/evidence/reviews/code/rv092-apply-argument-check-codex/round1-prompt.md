# Adversarial code review: function arguments are checked when an Apply is built (xacml4j)

You are an independent reviewer. Do not use tools. Do not run commands. Do
not edit files. Answer in one message. Review only the diff below. It is
the complete diff of this change. The full reactor passes on it:
7,939 tests, 0 failures, 0 errors, 20 skipped. Conformance unchanged: 937 tests, 12 skipped.

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

## Change

- `Apply`: the constructor calls `typeError(spec, arguments)`. It first
  asks every argument for its static type (`getEvaluatesTo()`); when that
  throws, the type is not known before the evaluation and nothing is
  shown. Then it calls the existing
  `FunctionSpec.validateParametersAndThrow`. A `SyntaxException` from it
  is a type error; it is logged at WARN and kept. `evaluate` throws a
  `FunctionInvocationException` (status processing-error, a message that
  names the function id) when a type error was kept. New accessor
  `isTypeCorrect()`.
- `AttributeDesignatorFunctions.resolve` (the return type of the
  functions `attribute-designator` and `attribute-selector`): it returned
  the data type; the functions return a bag of it. With the new check
  every use of these functions as a bag looked wrong (5 tests of an
  existing class failed) until this was fixed.
- Two existing test classes follow the new behaviour:
  - `ApplyTest` verifies the one call of `validateParametersAndThrow` on
    its mocked function.
  - `HigherOrderVariadicFunctionsTest`: its invalid expressions were
    Indeterminate with status syntax-error when runtime validation was
    off (the function was invoked and its own check threw a
    `SyntaxException`) and processing-error when it was on. Now
    processing-error in both modes, and the function is not invoked; the
    test asserts `isTypeCorrect()` is false and that the context was not
    asked for the validation mode.

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
- Mutation check of the final code. M1, no check in the constructor (the
  code before the change): `ApplyArgumentValidationTest` 18 failures,
  `HigherOrderVariadicFunctionsTest` 19, `ApplyTest` 2,
  `InvalidApplyPolicyUnmarshallerTest` 4, `InvalidApplyDecisionTest` 6
  (`expected: <INDETERMINATE_P> but was: <PERMIT>`, `expected:
  <INDETERMINATE_D> but was: <DENY>`). M2, `evaluate` ignores the kept
  error: 17, 19, 2 and 6 failures in the four classes that evaluate. M3,
  the resolver returns the data type again: 1 failure in
  `ApplyArgumentValidationTest` and 5 in the existing
  `AttributeResolutionSafetyNetTest`.

## Questions

Report findings ordered by severity (HIGH, MEDIUM, LOW), each with file,
line from the diff, a concrete failing scenario, and a suggested fix. Report
only defects you can show from the diff and the facts above.

1. False positive: an expression that is type correct by the declaration
   of its function and that the check marks as wrong, so that a policy
   that worked is Indeterminate now. Think of optional and variadic
   parameters, calls without arguments, function references, nested
   expressions, and the three resolvers.
2. False negative that the diff could close: an expression built through
   `Apply` that is not type correct and still evaluates to a value.
3. Is processing-error the right status for every case that the check
   finds, given section 7.19.2 and the two conformance tests?
4. Do the edits of the two existing test classes weaken them?
5. Does the status message or the WARN line carry request data?
6. Is "the type of an argument is not known before the evaluation, so
   nothing is shown" a hole? Who controls it?
7. Do the new tests pin the behaviour? Name a missing test.
8. Does the spec text say what the code does?

End with a verdict line: `VERDICT: APPROVE` or `VERDICT: REQUEST_CHANGES`.

## Diff

(the complete diff of the change followed here)
