# rv051: bug 4.25 adversarial review, BooleanVal lexical forms and manifest re-enablements (Grok)

Date: 2026-10-02. Lane: adversarial code reviewer, Grok Build grok-4.7
(xhigh), role `.ntm/roles/policy4j_code_review.md`, session cleared first.
Session `policy4j--g1`, pane 0. Read-only (run 035 evidence, sources, XML
Schema Part 2 3.2.2/4.3.6, JSON Profile of XACML 3.0 v1.1 3.3.1/4.2.1/4.2.4,
XACML 3.0 Plus Errata 01 A.3.9, JAXB 2.3.8 sources). Maven not re-run.

Verdict: APPROVE (no change required in this packet).

- `BooleanVal.fromLexical` (`:61-80`) accepts exactly true/false/1/0 after
  trimming XML S; rejects empty, `+1`, `01`, case variants, `yes`, internal
  whitespace, NBSP, U+2003, U+000B, form feed. Trim-only equals full
  collapse for this lexical space.
- No XACML value path still uses `Boolean.parseBoolean`/`valueOf` (remaining
  uses read JVM properties or the manifest CSV). boolean-from-string, XML
  AttributeValue and JSON attribute text all reach the same parser.
- JSON profile: xs:boolean maps to a JSON boolean; with an explicit boolean
  DataType a JSON string goes through the XML Schema lexical space ("1" true,
  "TRUE" syntax-error); native booleans unchanged. Request flags are JSON
  Booleans in the profile; string flags now parse strictly.
- No conformance fixture uses True/TRUE as a boolean literal (RSA hits are
  xs:string).
- Manifest: IIA022, IIA024, IIIA340, IIIE301, IIIE303, IIIG301 executed and
  passed; IIIA030/IIIA330 skipped with policy4j-the.4.24; paths unchanged.

Non-blocking: `boolean-from-string("TRUE")` surfaces processing-error instead
of syntax-error (Errata 01 A.3.9) because of the invocation wrapper; that is
bead policy4j-the.4.43; no test pins the wrong status.

New finding filed: schema boolean attributes (ReturnPolicyIdList,
CombinedDecision, MustBePresent, IncludeInResult) are parsed by JAXB's
converter, which maps invalid tokens to false and treats "10" as true
(policy4j-the.4.47). Test fixture `xacml-xml` `MarshallerTestPolicy.xml:64-66`
has boolean text "assignment3" (currently never unmarshalled).
