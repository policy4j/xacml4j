# Run 044: donor pre-audit for task 5.3, Primitives facade and registry (Agy)

Date: 2026-10-02. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity;
pane shows Gemini 3.8 Flash (High) (report self-label unreliable). Session
`policy4j--g1`, pane 3, 46 tool calls. Read-only: donor `b9f114a3` via
`git show`. Raw report: [agy-report.txt](agy-report.txt) (A 15 defects, B
carry-over fixes, C registry-test migration, D 30 tests).

Reported top defects: `Primitives` is an enum over an erased
`PrimitiveType<?, ?>` without the D5 lookup API; it lacks several a8a90d46
types; `IPAddressV2` does not compile and pulls a third-party dependency;
`DNSName` and `JsonPath` carry wrong ids; donor `BooleanVal` regresses 4.25.

## Conductor spot checks (2026-10-02)

- Confirmed: `b9f114a3:.../types/Primitives.java:34-132` has 16 constants
  (String, HexBinary, Base64Binary, Boolean, the five ISO 8601 types, Double,
  DnsName, X500Name, Int64, Int32, JsonPath, XmlPath) and none for anyURI,
  ipAddress, rfc822Name or entity. The report's claim that integer is missing
  is wrong (`Int64` covers xs:integer); `Int32` (UInt32) is not an a8a90d46
  type.
- Confirmed: `b9f114a3:.../types/DNSName.java:32` declares
  `urn:oasis:names:tc:xacml:1.0:data-type:dnsName`; the a8a90d46 and XACML 3.0
  id is `urn:oasis:names:tc:xacml:2.0:data-type:dnsName`.
- Other rows unverified; the 5.3 packet's registry tests (task 4.4 suite,
  ported) decide them.

Use: 5.3 builds the registry to design D5 from the a8a90d46 id list (the
4.4 tests are the oracle), taking donor code only where it passes them.
