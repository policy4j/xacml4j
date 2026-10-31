No confirmed functional defect. One nonblocking specification mismatch:

1. **LOW — `openspec/changes/modernize-core-for-jdk28-valhalla/specs/function-semantics/spec.md:260`**

 “Every expression SHALL have a type that is known when the policy is loaded” exceeds what the implementation enforces.

 Concrete scenario: `stringBagOfUnknownType()` builds an `attribute-designator` whose `isTypeCorrect()` is true while `getEvaluatesTo()` throws. The new test explicitly asserts both behaviors. Only a containing `Apply` rejects that unknown return type.

 Suggested fix: say “Every supplied argument to an `Apply` SHALL have a type that is known when the expression is built.” This describes the implemented boundary.

Answers to the four questions:

1. **Invalid expression producing a value:** none demonstrated from the diff and supplied facts. Recorded errors propagate through nested `Apply` expressions and complete variable-reference chains, including skipped operands.

2. **False positive with known argument types:** none demonstrated. Rule 2 is an explicit compatibility restriction with a stated cost; the supplied evidence does not establish that restriction as a defect.

3. **Fail-closed validator exceptions:** the broad catch permanently rejects a call if its validator throws an unexpected exception. A faulty validator could therefore reject valid arguments, but no such production failure is demonstrated here.

4. **Missing test:** independently exercise the final `RuntimeException` catch in `typeErrorOf`: use known, matching argument types and a validator that throws `IllegalStateException`; assert processing-error, no invocation, and no argument evaluation. The combined M4 mutation does not independently prove coverage of this branch.

The quoted XACML text directly supports processing-error for invalid static data types. It does not explicitly prescribe construction-time caching, unknown-type rejection, containment propagation, or processing-error for every arity violation. Those are implementation interpretations. The supplied conformance results support the shown static-type cases.

Verification was limited to the supplied diff and facts; test results were supplied, not independently executed.

VERDICT: APPROVE
