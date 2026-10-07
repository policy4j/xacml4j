# rv003: group 1 round 2 review, downstream repair (Agy)

Date: 2026-10-01. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity,
Gemini 3.8 Flash (High). Session `policy4j--g1`, pane 3. Read-only.

Scope: task 1.3 downstream part: xacml-pdp/src, xacml-test (pom junit
compile scope, src incl. Xacml30TestUtility comparison), conformance test
Java, xacml-core date/time arithmetic fixes, xacml-xml mixed-content fix,
evidence run 005. Identifier-alias work in progress was out of scope.

Verdict: APPROVE. Findings: none.

Reviewer statements:
- PDP: MultipleResourcesViaXPath* repair keeps category ids, reference ids and
  attribute metadata; DefaultEvaluationContextHandler resolves a selector with
  no ContextSelectorId and no content-selector against the root Content.
- xacml-test: absent optional collections normalized to empty; the
  assertObligations self-comparison now reads the actual response, which
  strengthens the check (Xacml30TestUtilityTest).
- Conformance runners (Xacml20/30ConformanceTest, RSA2008InteropTest) have no
  diff; exclusion sets and loop bounds unchanged.
- Date/time: ISO8601Date uses toPeriod(); ISO8601DateTime applies one signed
  Period for year-month and fixes the day-time subtraction sign; 65 tests in
  DateTimeArithmeticBoundaryTest and ISO8601DateTimeArithmeticTest.
- XML: whitespace-only text skipped in mixed Content (Xacml30RequestContentTest).
- Hygiene: ast-grep scan 0; no System.out/err in src/main; no machine paths in
  run 005; Surefire 727 run, 0 failures, 1 error (IIC150), 9 skipped.

Residual risk stated: IIC150 (duration bag identifier) pending in the
identifier lane.
