# rv037: bugs policy4j-the.4.16, 4.17, 4.22 review (Agy)

Date: 2026-10-01. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity,
Gemini 3.8 Flash (High). Session `policy4j--g1`, pane 3. Read-only.

Scope: bugs3 lane patch (core, xml, json; 432 lines). The lane's conformance
runner changes are not merged (the lane predates the group 3 runner).

Verdict: REQUEST_CHANGES.

Findings:
1. MEDIUM, `DoubleVal.java:71-73`: toStringExp() still returns
   StringVal.of(get().toString()), so string-from-double (StringFunctions.java:210)
   returns "Infinity"/"-Infinity" instead of "INF"/"-INF". Fix: delegate to
   toXacmlString(), like IntegerVal.
2. LOW, IIIG300Response.xml:24-27: the fixture expects 2 ids; XACML 3.0
   Plus Errata 01 section 5.48 lists all fully applicable policies whatever
   their effect (policy2 Permit, policy4 Deny, policyset) = 3. Keep the
   spec-aligned engine; track IIIG300 as an adjudicated divergence.
   Resolution: added to policy4j-the.3.9.
3. LOW, `TypeToStringTest.java:183`: one tab instead of two.

Checked (reviewer): integer and x500Name now use toXacmlString (debug
strings gone); Java's Double.toString output ("1.0", "1.0E10", "-0.0") is a
valid xs:double lexical form and XACML 3.0 (double-to-string) does not require
canonical form; 27.50 vs 27.5 in IIA022/IIA024 is a harness string
comparison (bug policy4j-the.3.11). 4.17: both XML mappers
(`Xacml20RequestContextUnmarshaller.java:198`,
`Xacml30RequestContextFromJaxbToObjectModelMapper.java:215`) and the JSON
deserializer (`AttributeDeserializer.java:333`, unwrapped at
`JsonRequestContextUnmarshaller.java:368`) throw SyntaxException with status
syntax-error; producing an Indeterminate response needs a PDP/transport error
path (recorded gap). 4.22: the old predicate
`!isIndeterminate() || decision != NOT_APPLICABLE` was a tautology; the new one
records only Permit/Deny; all Indeterminate variants excluded; evaluated
policies are keyed by id, so references appear once.
Resolution: repair packet to the implementer lane (bugs3).
