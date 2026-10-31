# rv014: task 2.4 review, xacml-pdp JUnit 5 + Mockito lane (Agy)

Date: 2026-10-01. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity,
Gemini 3.8 Flash (High) (the report header names "claude-code/gemini-2.5-pro";
recorded as reported). Session `policy4j--g1`, pane 3. Read-only.

Scope: the xacml-pdp test migration done in a private lane copy (20 files,
`<lane>/pdp` against `<lane>/pdp.base`), before it is applied to the worktree.

Verdict: REQUEST_CHANGES.

Findings (reviewer):
1. DefaultEvaluationContextHandlerTest ~314-328, 340-365, 371-399: verify
   statements after handler.resolve() inside assertThrows lambdas never run;
   the lane summary claims they are verified.
2. RequestContextHandlerChainTest ~45-48, 59-73: EasyMock record-phase calls
   left as direct calls on Mockito mocks; verifications adjusted to times(2)
   and duplicated InOrder steps that include the test's own calls.
   Conductor check: confirmed in source.
3. DefaultPolicyReferenceResolverTest ~67-68, 82-83: direct
   `repository.addPolicyRepositoryListener(Mockito.any())` in setup;
   listener and getPolicy verifications dropped; unused captor.
4. DefaultPolicyInformationPointTest ~257-279: InOrder dropped and calls
   unverified.
5. MultipleResourcesViaXacmlXPathHandlerTest ~339-340, 395-396: duplicate
   consecutive verify on requestDecision.

Resolution: consolidated with rv015 into one repair packet for the lane.
