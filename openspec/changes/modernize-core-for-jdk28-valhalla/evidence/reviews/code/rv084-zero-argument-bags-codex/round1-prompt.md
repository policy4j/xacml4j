# Adversarial code review: type-bag functions without arguments (xacml4j)

You are an independent reviewer. Do not use tools. Do not edit files. Answer
in one message. Review only the diff below. It is the complete diff of this
change. The full reactor passes on it: 7,735 tests, 0 failures, 0 errors,
22 skipped; conformance unchanged by this change.

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
- Functions with a minimum number of variadic arguments are checked before
  the invocation (`@XacmlFuncParamVarArg(min=...)`).
- `LogicalFunctions` (`or`, `and`, `n-of`) and `BagFunctions.doubleBag` have
  their own `null` checks for the variadic array; they stay.
- `BagOfValues.Builder.attribute(Value...)` iterates the array.

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
