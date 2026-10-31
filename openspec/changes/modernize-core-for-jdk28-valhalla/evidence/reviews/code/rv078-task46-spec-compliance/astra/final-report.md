FINAL REPORT (policy4j_code_review_codex_astra, serving variant UNVERIFIED)

**APPROVE — bounded task 4.6 review.** No reachable defect requiring changes was established in the final 18-file snapshot for `modernize-core-for-jdk28-valhalla`, Beads `policy4j-the.4.6`.

Requested review configuration: Astra. Native configuration pin was not independently inspected; the exact serving variant remains **UNVERIFIED**.

The original snapshot matched 18/18 hashes. After the announced revision, I verified that only `NaNFixtureAdjudicationTest.java` changed, then matched **18/18 v2 hashes before continuing and again at completion**. Final manifest SHA-256: `0cc1b38ba61598891343f526ca9ed9a69264f377f67a7fa470094423e1d07801`. The superseded test compilation failure is not counted as successful verification.

Combining review conclusions:

1. **CS01 §2.1 behavior is implemented.** [OnPermitApplySecondPolicyCombiningAlgorithm.java:64](xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/OnPermitApplySecondPolicyCombiningAlgorithm.java:64) rejects invalid arity before child evaluation, selects the second child after Permit, selects NotApplicable/third child after N/D/I_D, and returns I_DP without further evaluation after I_P/I_DP/plain I. That final branch leaves the first child’s reported status intact. The independent table tests check all 36 pairs, 216 triples, legacy Indeterminate cases, invalid arities, call order and complete deciding-child status. [OnPermitApplySecondDecisionTableTest.java:180](xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/OnPermitApplySecondDecisionTableTest.java:180)

2. **Nested response staging follows the supplied Core §7.18 oracle.** [BaseCompositeDecisionRule.java:143](xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java:143) stages child responses and the composite’s own responses together. Publication occurs only for the final matching Permit/Deny, after the composite’s response expressions succeed. NotApplicable, Indeterminate and expression-failure returns bypass publication. The extended-Indeterminate path creates a separate buffer that is never published, at line 188. Nested publication goes through the delegate, preserving enclosing buffers.

3. **Context forwarding is coherent in the inspected production paths.** The wrapper forwards the extended-Indeterminate flag and replaces its delegate once in ancestry walks at [BaseCompositeDecisionRule.java:277](xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java:277). Current policy/rule/reference, variable access and status writes retain inherited delegation. I traced the supporting policy, reference and descendant contexts; no introduced reference-resolution or status-loss defect was established. The outermost composite retains its original combining context.

4. **Registration preserves the intended boundary.** The provider adds the policy algorithm only. Exact-set tests cover the 23 baseline URNs plus this addition through explicit defaults and discovery, and reject lookup as a rule algorithm. [DecisionCombiningAlgorithmRegistryTest.java:452](xacml-core/src/test/java/org/xacml4j/v30/policy/combine/DecisionCombiningAlgorithmRegistryTest.java:452)

Prior findings and follow-ups:

- **Discarded-child leakage: resolved for the retained reproducer.** Real-policy tests cover discarded Permit and Deny branches, later siblings, and the nested parent’s own matching responses. [OnPermitApplySecondPolicyTest.java:258](xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/OnPermitApplySecondPolicyTest.java:258)
- **Previous P3 fixture-guard independence gap: resolved.** Suite, disposition and case ID now vary independently. The harness binds adjudication to the named fixture and rejects a different Permit fixture. [NaNFixtureAdjudicationTest.java:254](xacml-conformance-tests/src/test/java/org/xacml4j/v20/NaNFixtureAdjudicationTest.java:254)
- **Full NaN response comparisons: resolved in v2.** The helper marshals the actual `ResponseContext` through the production JAXB marshaller before invoking the typed comparator. [NaNFixtureAdjudicationTest.java:270](xacml-conformance-tests/src/test/java/org/xacml4j/v20/NaNFixtureAdjudicationTest.java:270)
- **Numeric acceptance retained.** The four numeric production hashes are unchanged from my prior review. Tests now exercise full and empty wrong-type bags with generic validation disabled at [DoubleBagSetEqualityTest.java:329](xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/DoubleBagSetEqualityTest.java:329); modulo-zero row 43 is enabled. Primitive policy equality, separate Java equality, NaN multiplicity and the recorded integer truncation convention remain accepted.
- **XPath wording: resolved.** Failure requires Content to be present; missing Content explicitly takes precedence. [function-semantics/spec.md:94](openspec/changes/modernize-core-for-jdk28-valhalla/specs/function-semantics/spec.md:94)
- **Manifest preservation retained.** Its hash is unchanged from the previously verified 880-row manifest: all 864 prior enabled identities remain, with both IIC020 cases bringing enabled coverage to 866.

Verification: reviewer execution comprised read-only hashing, source/diff inspection, receipt parsing and scoped `ast-grep scan`—exit 0, no diagnostics. No builds, tests, external research or writes were performed.

The supplied completed [reactor receipt:3](openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/069-task46-spec-compliance/reactor-green-receipt.json:3) records JDK21 `clean verify`, exit 0, **7,589 tests, zero failures/errors, 30 skips**. I independently summed its suite entries and module totals; both agree. It includes 289 decision-table tests, 12 combining policy tests, 10 NaN adjudication tests and six remaining FunctionSafetyNet43 skips. The supplied [preservation receipt](openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/069-task46-spec-compliance/source-preservation.json:1) reports all 2,911 vendored files unchanged and 3,736/3,753 existing baseline files unchanged.

Residual limits: new combining response assertions inspect ID sets, not complete assignment values. Dedicated nested reference-plus-failing-response-expression coverage would strengthen the staging proof; source tracing and general reference-suite success are not that specific test. Other zero-argument bag functions, integer overflow, n-ary union, real malformed XPath and controlled performance/JDK28 verification remain outside this approval. This verdict does not establish whole-engine compliance or close gate 4.7.

DONE_FINAL_ASTRA_20261004
