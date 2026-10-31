# rv059: bug 3.9 static-rejection harness and bug 4.47 strict schema booleans review (Agy)

Date: 2026-10-02. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity;
pane shows Gemini 3.8 Flash (High) (self-label unreliable). Session
`policy4j--g1`, pane 3, 44 tool calls, 24 files. Read-only: main worktree
and evidence run 051 (author Sol/Codex).

Verdict: APPROVE (1 LOW).

- 3.9: IIA004 and IIC012 (both suites) pass with exact cause, status and
  message assertions; 16 negative and control tests in
  ConformanceStaticRejectionTest; IIA023, IID029, IIIG300 keep their
  adjudicated skips.
- 4.47: a SAX filter (`BooleanAttributeValidationFilter`) in
  `BaseJAXBUnmarshaller` (`:84-103`) validates ReturnPolicyIdList,
  CombinedDecision, MustBePresent and IncludeInResult for every request,
  response and policy unmarshaller (2.0 and 3.0), nine input forms; per-call
  objects (no shared state); prebuilt JAXBElement inputs bypass the filter.
  Vendored XSDs and the xjb binding untouched. 85 assertions in
  BooleanAttributeLexicalTest. Full reactor 6,041 tests, 0 failures.

LOW, `BaseJAXBUnmarshaller.java:101`: `TransformerFactory.newInstance()` on
every unmarshal call (JAXP service lookup each time). Note for the fix:
TransformerFactory is not specified as thread-safe; a shared instance needs
care (ThreadLocal or synchronized creation).

Conductor follow-up: the new SAX/Transformer pipeline now carries every XML
input; it must keep the previous parser's protections against external
entities and DTDs (XXE). Routed to the adversarial review.
