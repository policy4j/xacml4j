# Adversarial code review, round 2: type-bag functions without arguments (xacml4j)

You are an independent reviewer. Do not use tools. Do not edit files. Answer
in one message. Review only the diff below. It is the complete diff of this
change. The full reactor passes on it (totals below).

## Project

xacml4j is a Java 21 implementation of OASIS XACML 2.0/3.0. A function is a
static Java method with annotations. `DefaultFunctionInvocation` builds the
Java argument array for the reflective call from the list of XACML
arguments.

## Defect

A `type-bag` function called without arguments (for example
`string-bag()`) failed with a `NullPointerException`, so the expression was
Indeterminate. 18 of the 19 registered bag ids failed (17 standard ids, two
legacy ids); `double-bag` had a local null check.

Normative text (XACML 3.0 Plus Errata 01, A.3.10, fetched 2026-10-07):

urn:oasis:names:tc:xacml:x.x:function:type-bag
This function SHALL take any number of arguments of ‘type’ 
and return a bag of ‘type’ values containing 
the values of the arguments. An application of this function to zero arguments 
SHALL produce an empty bag of the specified 
data-type.

## Cause and change

`FunctionSpec.invokeWithList` normalizes the argument list first. For a
variadic parameter without arguments the normalized list holds a single
`null`. `DefaultFunctionInvocation` turned that into an array with one
`null` element and then passed `null` instead of the array to the method.
Now it passes an empty array of the component type of the variadic
parameter.

Facts you cannot see in the diff:

- `varArgComponentType` is the component type of the last Java parameter
  when the method is declared with `...`. Both production call sites of the
  constructor pass it. The two-argument constructor passes `null`; it is
  used by tests.
- `FunctionParamSpec.isOptional()` of a variadic parameter is true exactly
  when `@XacmlFuncParamVarArg(min=0)`.
- `LogicalFunctions` (`or`, `and`, `n-of`) and `BagFunctions.doubleBag` have
  their own `null` checks for the variadic array; they stay.
- `BagOfValues.Builder.attribute(Value...)` iterates the array.

## Round 1 result and what was done

Codex: APPROVE, no defect, one missing test. After the review the author
found a side effect and narrowed the change:

1. Argument counts are not validated before the call unless runtime
   validation is on (`EvaluationContext.isValidateFuncParamsAtRuntime()`,
   off by default), and nothing in the production code calls
   `FunctionSpec.validateParametersAndThrow` when a policy is loaded. (The
   round-1 prompt said that functions with a minimum of variadic arguments
   are checked before the invocation. That was wrong.) So with the round-1
   change `integer-add()` (declared `min=2`) would have received an empty
   array and returned 0, where it failed before.
   Now only a variadic parameter that allows zero arguments gets the empty
   array: `spec.getParamSpecAt(numOfParms - 1).isOptional()`. The
   parameter spec of a variadic parameter is optional exactly when its
   minimum is 0. A parameter with a minimum keeps `null`, as before.
2. New tests: `bagFunctionWithArgumentsKeepsItsValues` (the missing test of
   round 1) and `functionWithAMinimumOfVariadicArgumentsStillFailsWithoutThem`
   (`integer-add()` is Indeterminate with processing-error; `any-of(f)` is
   Indeterminate with syntax-error, both as before the change).

The full reactor passes: 7,745 tests, 0 failures, 0 errors, 22 skipped.

## Questions

Report findings ordered by severity (HIGH, MEDIUM, LOW), each with file,
line from the diff, a concrete failing scenario, and a suggested fix. Report
only defects you can show from the diff and the facts above.

1. Is the new array handling correct for: no variadic argument, one
   argument that is a real `null`, one non-null argument, several
   arguments, and a `null` component type?
2. Can a function that worked before now get an empty array where it needs
   `null`, or a wrong array type?
3. Do the tests pin the behaviour through the registry (not by calling the
   Java method directly)? Name a missing test.

End with a verdict line: `VERDICT: APPROVE` or `VERDICT: REQUEST_CHANGES`.

## Diff

```diff
