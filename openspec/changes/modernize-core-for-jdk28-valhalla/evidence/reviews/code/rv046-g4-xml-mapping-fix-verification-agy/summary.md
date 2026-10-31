# rv046: rv043/rv040 fix verification, XML mapping lane (Agy)

Date: 2026-10-02. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity,
Gemini 3.8 Flash (High). Session `policy4j--g1`, pane 3. Read-only, on the
merged main worktree and the lane patch.

Verdict: APPROVE. No new issues.

- rv043 finding 1 (duplicate xml:id): FIXED. `RequestContext.java:75-84`
  throws SyntaxException on a repeated id;
  `Xacml30RequestReferenceMappingTest.java:76-88` checks syntax-error through
  the 3.0 unmarshaller.
- rv043 finding 2 (equals self-comparison): FIXED. `RequestContext.java:389`
  compares `r.entities`; `RequestContextEqualityTest.java:34-72` covers equal
  hash codes, xml:id and entity differences, and Category equality.
- rv043 finding 3 / rv040 LOW 1 (2.0 scope URN): FIXED.
  `Xacml20RequestContextUnmarshaller.java:203-206` accepts the 1.0 and 2.0
  scope URNs; the test marshals a response and finds ResourceId="urn:root".
- rv040 LOW 2 (AttributesType reference tests): FIXED,
  `Xacml30RequestReferenceMappingTest.java:42-74` (string reference, xml:id
  reference, missing xml:id, unsupported type).
- Conductor hand merge of `Xacml20RequestContextUnmarshallerTest`: both the
  4.17 test (`:68-77`) and the lane tests (`:122-124`, `:273-300`) intact.
