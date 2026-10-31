# rv017: tasks 2.5 and 2.6 review, downstream JUnit 5 lane (Grok, adversarial)

Date: 2026-10-01. Lane: adversarial code reviewer, Grok Build grok-4.7
(xhigh), role `.ntm/roles/policy4j_code_review.md`, session cleared first.
Session `policy4j--g1`, pane 0. Read-only (patch, both evidence summaries,
xacml-test and parent POMs, cited sources; rtk rg). Did not run Maven.

Scope: same 28-file patch as rv016.

Verdict: APPROVE. Findings: none.

Reviewer statements:
- No comparison dropped in Xacml30TestUtility; it had no assertion messages;
  two-argument assertEquals calls are expected-value literals, not JUnit 4
  message-first calls.
- xacml-test/pom.xml: junit-jupiter-api with BOM version 5.14.4 (parent
  imports junit-bom) and default compile scope. The only in-tree subclasses of
  XacmlPolicyTestSupport are migrated in this patch; only
  xacml-conformance-tests depends on xacml-test; no public signature exposes
  org.junit types; @Before -> @BeforeEach; every @BeforeAll is static.
- Conformance: both runners keep `for (int i = 1; i < testCount; i++)` and
  the same skip sets; counted from the bounds: IIA 19, IIIA 22, IIB 53,
  IIC 215, IID 28, IIE 2, IIIF 6, IIIG 1, plus explicit IIIG006 = 347 per
  version; RSA 17; examples 2; RSA2008PerformanceTestCase still excluded by
  the parent `**/*TestCase.java` rule.
- @Test counts: xacml-test 20, xml 34, json 5, conformance 42; the only skip
  is JsonEntityMarshallingTest.testEntityMarshall (@Ignore -> @Disabled, FIXME
  comment kept); no JUnit 4 or EasyMock imports; license headers outside every
  hunk.

Residual risk (reviewer):
- Out-of-repo JUnit 4 subclasses of XacmlPolicyTestSupport would no longer
  run setup() (@BeforeEach is ignored by the vintage engine). None exist in
  this reactor; record it as a test-support API change for release notes.
- One resolver test verifies getClock() without verifyNoMoreInteractions; the
  strict EasyMock control would have rejected an extra context call.
