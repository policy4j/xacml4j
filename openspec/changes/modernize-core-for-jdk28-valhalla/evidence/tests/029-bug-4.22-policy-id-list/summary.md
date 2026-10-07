# Task 4.22: ReturnPolicyIdList

## Implementation and tests

`BaseCompositeDecisionRule.evaluate()` had `!indeterminate || decision != NOT_APPLICABLE`, which is true for almost every decision. It now records results only when the combined decision is determinate and not `NOT_APPLICABLE`. `RootEvaluationContext.addEvaluationResult()` also filters indeterminate and `NOT_APPLICABLE` results. A core test proves only applicable determinate decisions enter the list.

The direct IIIG301 response comparison passes with the expected three references: `InternalPolicy4`, `InternalPolicyset`, and `policyset`. IIIG300 does not match its vendored expected response: actual is 3 references (`policy2`, `policy4`, `policyset`), expected is 2 (`policy4`, `policyset`). Policy2 evaluates to Permit while the root result is Deny. Permanent fixture-driven tests assert the fully applicable policy IDs for IIIG300 and IIIG301; both pass under the OASIS §5.48 rule. The original broken predicate collected still more results; after filtering NotApplicable/Indeterminate, this is reduced to three.

## Specification conflict / not ready

OASIS XACML 3.0 Plus Errata 01 §5.48 says the requested list includes all fully applicable policies whether or not their effect after rule combining matches the final decision ([official OASIS specification](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-en.html)). IIIG300's expected response omits the fully applicable Permit Policy2. Returning only references with the same decision as the root would make the fixture pass but would contradict that normative text and the existing `RequestContext.isReturnPolicyIdList()` contract. No fixture or skip list was edited. IIIG300 therefore remains an adjudication blocker; IIIG301 passes.

The normal full conformance reactor passes its unchanged enabled case set: 347 OASIS 2.0, 347 OASIS 3.0, 17 RSA 2008, and 2 examples. IIIG300/301 are beyond the regular IIIG001–006 loop; direct checks are recorded in `direct-policyid-tests.log`. IIA005 remains excluded. The vendored fixture manifest is byte-identical to baseline; both vendored trees are byte-identical to baseline.
