# Adversarial code review, round 4: function arguments are checked when an Apply is built (xacml4j)

You are an independent reviewer. Do not use tools. Do not run commands. Do
not edit files. Answer in one message. Review only the diff below. It is
the complete diff of this change after the fixes of rounds 1, 2 and 3. The full
reactor passes on it:
7,949 tests, 0 failures, 0 errors, 20 skipped. Conformance unchanged: 937 tests, 12 skipped.

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

- `Apply`: the constructor first looks at its arguments: when one of
  them is an `Apply` with a type error, or a reference to a variable
  whose definition is one, this expression has a type error too
  (`typeErrorInside`), and the function id of the inner expression is
  kept for the status message. A chain of variable references is
  followed to its end. Otherwise it calls `check(spec, arguments)`, with
  three outcomes. It asks every argument for its static type
  (`getEvaluatesTo()`); an argument for which that throws is replaced, in
  the check only, by `ArgumentOfUnknownType.INSTANCE`, whose type fits
  every parameter that takes a value or a bag. Then it calls the existing
  `FunctionSpec.validateParametersAndThrow` on that list. A
  `SyntaxException` from it is a type error (logged at WARN): outcome
  TYPE_ERROR. Otherwise AT_EVALUATION when an argument was replaced, and
  TYPE_CORRECT when none was.
- The four parameter specifications that take a value or a bag accept
  `ArgumentOfUnknownType.TYPE`. The specification of a function-reference
  parameter does not. `validateParametersAndThrow` skips the function's
  additional validator when the list holds such a stand-in (that
  validator reads the types of all arguments together).
- `Apply.evaluate`: TYPE_ERROR throws a `FunctionInvocationException`
  (status processing-error, a message that names the function id) without
  invoking the function. AT_EVALUATION, for a function that does not
  evaluate its own arguments: the arguments are evaluated in their order,
  their values are checked with `validateParametersAndThrow`, a failure
  throws the same exception, and otherwise the function is invoked with
  the values. A function that evaluates its own arguments (`or`, `and`,
  `n-of`, the higher-order functions) is invoked as before.
- New accessor `isTypeCorrect()`: false for TYPE_ERROR only.
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

## Round 1 result (Codex: REQUEST_CHANGES) and what was done

MEDIUM: an argument of unknown type switched the whole check off, so
`integer-bag-size(designator, 99)` and `integer-bag-size(designator)` on a
bag of strings (a designator function whose data type is an expression)
were invoked and gave a value.

Fix: the check waits for the values (see "Change"). New test
`argumentWhoseTypeIsKnownOnlyAtEvaluationIsCheckedAtEvaluation`, red
before the fix (`integer-bag-size` on the bag of strings: "Expected
EvaluationException to be thrown, but nothing was thrown"). It covers a
bag of another type, one argument too many, a bag where a value is needed,
and two valid uses. New test
`functionThatEvaluatesItsOwnArgumentsKeepsDoingSo`: `or(true, <argument of
unknown type>)` is true and the second argument is not evaluated.

Not done, with the reason:

- The number of arguments is not checked apart from the types when a type
  is unknown. The existing validator checks both in one pass over the
  parameter specifications, and the minimum and maximum of a variadic
  parameter are not reachable from `Apply`. The case is closed at
  evaluation instead: the arguments are evaluated, then the call is
  rejected, and the function is not invoked. So for that case the
  arguments are evaluated before the rejection; the spec says so.
- A function that evaluates its own arguments is not given evaluated
  values: that would evaluate an argument that the function may skip
  (maintainer ruling D11: a deciding operand wins over a failed one).
  These functions check the operands that they evaluate.

## Round 2 result (Codex: REQUEST_CHANGES) and what was done

MEDIUM: a type error that is known when the expression is built could
hide behind a deciding operand. `or(true, string-equal("a",
string-bag("a")))` was true, and a Permit rule with that condition gave
Permit.

Fix: an expression that holds an expression with a type error has a type
error (section 7.4: expressions compose greater expressions; a valid
expression is type correct). The whole expression is Indeterminate with
status processing-error and nothing is evaluated. New tests, red before
the fix: `expressionThatHoldsATypeErrorHasATypeError`,
`typeErrorBehindADecidingOperandIsNotSkipped` (`or`, `and`, `n-of`, and
two levels), `typeErrorInAVariableDefinitionIsFoundThroughTheReference`,
and a row of `InvalidApplyDecisionTest` through the PDP for the Permit
and the Deny effect (`expected: <INDETERMINATE_P> but was: <PERMIT>`,
`expected: <INDETERMINATE_D> but was: <DENY>`). An argument whose type is
unknown is not a type error and stays unevaluated behind a deciding
operand, as before.

The unit of this change is the expression, and the author asks the
reviewer to judge that scope. Section 7.19.2 speaks of "a policy that
contains invalid static data-types". Not done: a policy is not made
Indeterminate as a whole when the wrong expression is somewhere that the
evaluation of the request does not reach:

- a rule that the rule-combining algorithm does not evaluate, or whose
  target does not match;
- an obligation or advice expression whose FulfillOn or AppliesTo does
  not match the decision;
- a variable definition that no evaluated expression refers to.

Reason: in each case the standard evaluation rules give the result that
they would give if the wrong expression were Indeterminate. A rule that a
combining algorithm does not evaluate is one that cannot change its
result; an obligation that is not fulfilled is not part of the response.
So no decision is less safe than "the invalid expression is
Indeterminate", which is what section 7.4 says ("an evaluation result of
"Indeterminate", which is said to be the result of an invalid
expression"). Making the whole policy Indeterminate would change the
policy evaluation and is recorded as a limit of the run.

Also changed after round 2: the DEBUG line of the check at evaluation no
longer prints the reason (it can hold a value of the request).

## Round 3 result (Codex: REQUEST_CHANGES) and what was done

1. MEDIUM, a chain of more than 64 variable references hid a type error:
   the cut is removed; the chain is followed to its end. It ends because
   a `VariableDefinition` takes its expression in its constructor and has
   no setter, so a definition exists before a reference to it can be
   built. Test `typeErrorIsFoundThroughALongChainOfVariableReferences`
   (1, 64, 65 and 500 references) and a row of `InvalidApplyDecisionTest`
   through the PDP with 65; red before the fix (`<PERMIT>`, `<DENY>`).
2. MEDIUM, an argument of unknown type switched off the check of what was
   known: the check when the expression is built now covers the other
   arguments and the number of arguments (see "Change"). Test
   `knownViolationNextToAnArgumentOfUnknownTypeIsATypeError`: `or(true,
   "a", <unknown>)`, one argument too many, a missing argument, and an
   argument of unknown type where a function reference is needed; red
   before the fix. Test
   `higherOrderFunctionWithABagOfUnknownTypeIsLeftToTheFunction` pins that
   a valid use is not marked.
3. The reviewer judged the expression-level scope sound and asked that
   the limit stays stated; it is in the limits of the run. The spec text
   now says that only what depends on the unknown type waits, and that
   variable references are followed whatever their number.

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
- `FunctionSpec.isRequiresLazyParamEval()` is true for a function that
  gets its arguments unevaluated (`or`, `and`, `n-of`, the higher-order
  functions). For every other function `FunctionSpec.invoke` evaluates
  all arguments in their order before it calls the function; a value
  evaluates to itself.
- `VariableReference.getDefinition().getExpression()` is the expression of
  the variable definition; `VariableReference.evaluate` evaluates it.
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
- Mutation check of the final code. M1, no check of the own arguments in
  the constructor (the code before the change):
  `ApplyArgumentValidationTest` 22 failures of 40,
  `HigherOrderVariadicFunctionsTest` 19, `ApplyTest` 2,
  `InvalidApplyPolicyUnmarshallerTest` 4, `InvalidApplyDecisionTest` 10
  of 11. M2, `evaluate` ignores a type error: 21, 19, 2 and 10. M3, the
  resolver returns the data type again: 1 and 5
  (`AttributeResolutionSafetyNetTest`). M4, no check at evaluation: 1.
  M5, a type error inside is not inherited: 4, 4 (XML) and 4 (PDP). M6,
  an argument of unknown type switches the whole check at build off: 1.

## Questions

Report findings ordered by severity (HIGH, MEDIUM, LOW), each with file,
line from the diff, a concrete failing scenario, and a suggested fix. Report
only defects you can show from the diff and the facts above.

1. Are the two MEDIUM findings of round 3 closed?
2. Can the stand-in mark a valid expression as a type error (false
   positive), or let a known violation pass? Look at each of the four
   parameter specifications in the diff, at the function-reference
   parameter, and at the skipped additional validator.
3. Does the stand-in change anything for a caller of
   `validateParametersAndThrow` or `isValidParamType` other than `Apply`?
4. Any other defect in the diff. Name a missing test. Does the spec text
   say what the code does?

End with a verdict line: `VERDICT: APPROVE` or `VERDICT: REQUEST_CHANGES`.

## Diff

(the complete diff of the change followed here)
