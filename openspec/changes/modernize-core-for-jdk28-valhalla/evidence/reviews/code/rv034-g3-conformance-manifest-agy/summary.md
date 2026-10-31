# rv034: group 3 review, conformance manifest and harness (Agy)

Date: 2026-10-01. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity,
Gemini 3.8 Flash (High). Session `policy4j--g1`, pane 3. Read-only.

Scope: tasks 3.1-3.4 and the 3.6 gate (conformance-manifest.csv,
ConformanceManifest, ConformanceCaseRunner, ConformanceManifestTest,
ConformancePolicyImportTest, the four parameterized runners), evidence run
`tests/023-conformance-manifest`.

Verdict: APPROVE. Findings: none.

Reviewer statements:
- Manifest: 880 cases (oasis20 374, oasis30 487, rsa2008 17, example 2); the
  72 deprecated 3.0 requests are excluded on purpose; no active 3.0 case is
  missing.
- 836 enabled cases pass on JDK 21 (833 executed + 3 special-harness); 44
  disabled with reasons and bug ids (36 engine defects, 7 fixture/spec, 1
  harness). All 711 previously passing distinct cases still run with the same
  response assertions; the duplicate IIIG006 run is gone.
- Import failures throw with case id, resource and cause
  (`ConformanceCaseRunner.java:83-94`); no logged-and-ignored path remains;
  3.0 policies use MediaType XACML30_XML.
- The manifest guard checks both directions, duplicates, referenced files,
  and cannot pass with an empty manifest.
- IIE003 follows Special.txt method 1 (asserts the static rejection of
  PolicyId2 with "is not compatible", imports PolicyId1, compares the
  response); IID029/IID030 wrap both roots in only-one-applicable as
  Special.txt requires; neither can pass for the wrong reason.
- Each case is an independent @ParameterizedTest named <suite>/<caseId> with
  its own PDP and repository.
- Spot-checked blockers (fixture and engine lines opened): IIA004
  (Special.txt allows static rejection), IIC012 (Condition.java:59 rejects a
  non-boolean condition at load), IIA023 (offset -14:30 outside XML Schema
  range), IID029 (action-id in access-subject), IIC300
  (StringFunctions.java:54-60 argument order), IID300
  (PermitOverrides.java:88-94 returns Deny before Indeterminate{P}),
  IIIG300 (RootEvaluationContext.java:201 records every evaluated policy),
  IIA005 (Attribute.java:163 throws).
- Gate: conformance 901 tests (880 rows + 21 helpers), 44 skipped; reactor
  1,801 tests, 0 failures, 0 errors, 50 skipped.
