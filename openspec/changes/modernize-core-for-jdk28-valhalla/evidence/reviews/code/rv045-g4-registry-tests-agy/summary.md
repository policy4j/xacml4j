# rv045: task 4.4 review, registry tests (Agy)

Date: 2026-10-02. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity,
Gemini 3.8 Flash (High). Session `policy4j--g1`, pane 3. Read-only.

Scope: g4reg lane patch (310 lines): TypeRegistryTest,
DecisionCombiningAlgorithmRegistryTest, FunctionProviderServiceLoaderTest.

Verdict: APPROVE (2 LOW).

1. LOW: unused `Truth8` import in TypeRegistryTest.
2. LOW: @Disabled reasons use the placeholder `BUG-TBD-duplicate-ids`
   instead of policy4j-the.4.26.
(The reviewer's line numbers were patch offsets; the source lines are 32,
148 and 156.)

Reviewer statements, conductor-checked where marked:
- All 19 a8a90d46 data types are covered (conductor check: 19 enum constants
  in `a8a90d46:.../XacmlTypes.java`), with short names and the 4 aliases
  (two WD-xquery-operators duration URNs, two 2.0 xpathExpression URNs).
- 16 scalar types round-trip; xpathExpression and jsonPathExpression through
  their expression strings; entity resolves by id only.
- 11 rule and 12 policy combining algorithm URNs (1.0, 1.1 ordered, 3.0);
  on-permit-apply-second disabled pending task 4.6.
- Combining tests depend only on algorithm ids. TypeRegistryTest depends on
  XacmlTypes.getType, AnyURI and TypeFactory.BaseTypeFactory, which task 5.3
  (and the AnyURI rename in 5.2) will replace; the tests must be ported then.
- ServiceLoader gap: combining provider (bug policy4j-the.4.27, test belongs
  in xacml-core once registered); marshalling providers (test belongs in
  xacml-xml).

Resolution: the conductor applied both LOW fixes in the lane and reran the
three classes on JDK 21: 87 tests, 0 failures, 3 skipped (the 3 intended
@Disabled tests). The adversarial review is queued for the Grok lane.
