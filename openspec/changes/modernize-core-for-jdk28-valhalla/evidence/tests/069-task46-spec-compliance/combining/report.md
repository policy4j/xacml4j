# Conductor-copied native final report

`BaseCompositeDecisionRule.java` is not edited by me: all four edits I attempted on it were rejected before anything was written. I stopped there; nothing has been compiled or run since your RED run.

**Saved production files**
- `OnPermitApplySecondPolicyCombiningAlgorithm.java`: `combine` now follows CS01 §2.1.
  - Fewer than 2 or more than 3 children: processing-error status, Indeterminate{DP}, no child evaluated.
  - First child NotApplicable, Deny or Indeterminate{D}: NotApplicable with two children, otherwise the decision of the third child.
  - First child Permit: the decision of the second child.
  - Anything else (including plain Indeterminate): Indeterminate{DP} with no further evaluation, so the first child's status stays.
  - Class name, URN and constructor are unchanged; the Javadoc gained the profile reference.
- `DefaultXacml30DecisionCombiningAlgorithms.java`: one line registers the algorithm as a policy-combining algorithm.

**Saved tests (unchanged since TESTS_READY)**
- `OnPermitApplySecondDecisionTableTest.java`
- `DecisionCombiningAlgorithmRegistryTest.java`
- `OnPermitApplySecondPolicyTest.java`

**Expected result (from source reading, not executed)**
- 263 of the 264 red tests should turn green with these two files.
- `discardedFirstChildDoesNotAddObligationsToALaterSiblingDecision` stays red until the composite staging lands.

**Not done**
- The Deny, nested and parent-own obligation/advice cases were not added.
- `combining/report.md` was not written.

**Handoff for the staging change**
- **Identity constraint:** these tests assert that the algorithm receives the same context instance passed to `evaluate`, so a wrapper cannot go to `combineDecisions` for the outermost composite:
  - `PolicyTest:344,382,413`
  - `PolicySetTest:341,379,410`
  - `PolicyIDReferenceTest:282`
  - `PolicySetIDReferenceTest:200`
- **Mock-root constraint:** the two reference tests also verify exact calls on a mock root: one `addAdvices`, one `addObligations`, then `addEvaluationResult`, and nothing else.
- **Design I was applying:** stage only when an ancestor context is a `PolicySet.PolicySetDescendantEvaluationContext`.
  - Detect it by walking `getParentContext()` on `DescendantEvaluationContext` instances only, never calling the root.
  - Pass the wrapper to `combineDecisions` and `evaluateAdvicesAndObligations`.
  - On success, publish the lists that match the decision to the original context, then call `addEvaluationResult`.
  - Wrap the extended-indeterminate context the same way and never publish it.
- **Wrapper detail:** it must override `isExtendedIndeterminateEval()` to delegate, because `DescendantEvaluationContext` does not.
- **Why the outermost composite needs no staging:** the PDP reads the root context only for the final decision.

ALGORITHM_READY_COMBINING_20261004
