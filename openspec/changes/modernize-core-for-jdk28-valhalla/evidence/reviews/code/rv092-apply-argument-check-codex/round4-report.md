1. **MEDIUM — The stand-in still hides known constraints enforced by the additional validator.**  
 File: `xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java:439`.

 Consider:
 ```text
 or(true, any-of(string-equal, 1, stringBagOfUnknownType()))
 ```
 The integer operand cannot match `string-equal`’s string parameter, regardless of the unknown bag’s eventual element type. The generic higher-order operand checks accept the value and stand-in, but the unconditional return skips the additional validation that compares those operands with the referenced function.

 Consequently, the inner expression is left for evaluation instead of marked TYPE_ERROR. Its declared return type is boolean, so the outer `or` passes its check and returns true without evaluating it. A Permit condition can therefore hide this known type violation.

 **Fix:** preserve additional checks that can be decided from known arguments, including referenced-function compatibility. Defer only comparisons requiring the unknown type.

 **Missing test:** build this expression and assert that both Applies have `isTypeCorrect() == false`; evaluation must produce processing-error without evaluating arguments. Include the Permit/Deny PDP cases.

2. **LOW — The specification omits the function-reference exception.**  
 File: `openspec/changes/modernize-core-for-jdk28-valhalla/specs/function-semantics/spec.md:260`.

 The requirement says an argument with an unknown type SHALL fit its parameter during construction. However, `knownViolationNextToAnArgumentOfUnknownTypeIsATypeError` expressly rejects such an argument in the function-reference position of `any-of`.

 **Fix:** qualify this rule as applying to parameters taking values or bags, and state that an unknown argument does not satisfy a function-reference parameter. The existing test already covers the implemented behavior.

The reference-chain finding from round 3 is closed: the traversal has no numerical cutoff. The second finding is closed for ordinary parameter types and argument counts, but remains open for the additional-validator path above.

For the four changed parameter specifications, the explicit identity check accepts the stand-in and preserves the previous checks for every ordinary type. No false positive is demonstrated there. Rejecting the stand-in as a function reference is intentional. The remaining false negative comes from skipping the entire additional validator.

Other callers using ordinary expressions and types retain their previous checks. A caller explicitly supplying `ArgumentOfUnknownType.TYPE` now receives `true` from all four `isValidParamType` methods. The additional-validator bypass also applies to any caller supplying the marker expression; it is not restricted to an Apply call. Apply keeps the marker out of its stored arguments, so no marker propagation to the existing `map` caller is shown.

The specification’s promise that only checks depending on the unknown type wait is stronger than the unconditional additional-validator skip implements.

Verification: supplied diff and facts only. The reported reactor and conformance results were not independently run.

VERDICT: REQUEST_CHANGES
