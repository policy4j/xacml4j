# rv074: bug 4.52 adversarial review, XPath secure processing (Grok)

Date: 2026-10-02. Lane: adversarial code reviewer, Grok Build grok-4.7
(xhigh), role `.ntm/roles/policy4j_code_review.md`, session cleared first.
Session `policy4j--g1`, pane 0, about 46 minutes (the conductor stopped the
JDK-internals probing and asked for the verdict). Read-only: merged main
worktree, evidence tests/064, Agy sweep builds/063, rv070; `javap` of JDK 21
`XPathFactoryImpl`, `FuncExtFunction`, `JAXPExtensionsProvider`, and the
JDK 28-ea `XPathFactoryImpl` constructor.

Verdict: APPROVE.

- `FEATURE_SECURE_PROCESSING` is set on the only production `XPathFactory`
  (`xacml-core/.../content/XPathProvider.java:207-211`); pdp, xml and json
  create none. No function or variable resolver is installed.
- Fail-closed: a provider that refuses the feature raises
  `IllegalStateException` on that thread's first XPath use and no factory is
  kept; `XmlContent` maps it to processing-error. Not a startup failure;
  does not happen on the JDK provider. Thread-local factory, nothing shared.

Findings:

1. LOW, the tests do not lock the control: `XmlContentTest.testXPathExtensionFunctionIsRejected`
   (`:233-241`) and `MatchTest.testXPathExtensionFunctionFailureMakesMatchIndeterminate`
   (`:174-197`) stay green if `setFeature` is deleted (evidence 064 shows
   them green before the change): with no resolver installed, the JDK
   rejects the `java:` call either way. They do prove the processing-error and
   Indeterminate mapping. Fix: assert
   `getFeature(XMLConstants.FEATURE_SECURE_PROCESSING)` on the engine's
   factory (the JDK 21 and JDK 28-ea default is off). Agy rv070 asked for the same.
2. LOW, `MatchTest:181,192` pins `missing-attribute` for an XPath failure
   with MustBePresent=true (via `AttributeReference.java:132-141`). XACML 3.0
   §5.30 reserves missing-attribute for an empty selection; an XPath error is
   processing-error. Fix in this patch: keep the INDETERMINATE assertion and
   drop the status pin; the `AttributeReference` mapping is a separate bug.

Residual: operator and time limits (Agy finding 3) are still absent;
MustBePresent=false turns a non-syntax XPath failure into an empty bag
(`AttributeReference.java:142`); JDK 28 behavior beyond the constructor not
reviewed.
