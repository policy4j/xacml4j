# Run 030 conformance blockers

Two new failures were exposed by checking inherited assignment metadata.
The comparator remains strict; no fixture, expected result, or engine file was
changed. The packet authorizes disabling newly failing cases with a reason and
a BUG-TBD placeholder until the conductor files the issue.

| Case | Classification | Observed failure | Cause | Manifest status |
| --- | --- | --- | --- | --- |
| oasis30/IIIA030 | Engine: XML response output | Obligation assignment2 has the expected XPath expression but lacks XPathCategory resource metadata | Xacml30RequestContextFromJaxbToObjectModelMapper.create(AttributeAssignment) copies the typed value's data type/content but drops its extension attributes | skipped, policy4j-the.4.24 |
| oasis30/IIIA330 | Engine: XML response output | Advice assignment2 has the expected XPath expression but lacks XPathCategory resource metadata | Same mapper method as IIIA030 | skipped, policy4j-the.4.24 |

Evidence: [09-conformance-gate-cases.json](09-conformance-gate-cases.json) has the
failing Surefire receipts. [09-assignment-metadata-probe.json](09-assignment-metadata-probe.json)
reproduces each PDP decision and inspects its marshalled response: expected
`{XPathCategory=urn:oasis:names:tc:xacml:3.0:attribute-category:resource}` versus
actual `{}`. Probe exit 0 means the missing-metadata observation was reproduced;
it does not mean either conformance case passed. Its first two launcher attempts
failed before execution; the corrected probe is retained with its source.

Read-only cause: `xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30RequestContextFromJaxbToObjectModelMapper.java:424-433`.
`TypeToXacml30.java:466` supplies XPathCategory in the value's extension map,
which the assignment mapper does not carry forward. The proposed repair is to
copy `av.getOtherAttributes()` into the assignment and add focused obligation
and advice marshalling tests. XML writes are outside this packet, so this is a
proposal only. These are output defects, not changes to expected fixtures.

IIIE302 now passes and is executed with its bug field cleared. The other parts
of policy4j-the.3.7 remain for their owners.

IIA022, IIA024 and IIIA340 were not re-enabled or re-probed. They retain their
policy4j-the.4.16 engine serialization reasons: IntegerVal debug text for the
first two; Infinity versus XML Schema INF for the third. Typed-value and absent
Content controls now pass in the harness, but the complete cases remain pending
Luna's lane. All other inherited exclusions are unchanged.

# Separate engine parser observation

`xacml-core/src/main/java/org/xacml4j/v30/types/BooleanVal.java:49` delegates
string input to Java Boolean.parseBoolean. That cannot implement the XML Schema
boolean lexical forms: `1` is true and `0` is false, while invalid forms must be
rejected. See [XML Schema Part 2, section 3.2.2](https://www.w3.org/TR/xmlschema-2/#boolean).

The typed-comparison tests exposed three failures before the harness explicitly
recognized true/false/1/0 and passed Boolean values to the type factory:
true/1 equality, false/1 inequality, and rejection of an invalid lexical pair.
Receipts: [03-boolean-parser-red.log](03-boolean-parser-red.log) and the green
[08-comparators-green-results.json](08-comparators-green-results.json).

Core was not changed. A core lexical-parser repair is a separate, unassigned
proposal for the conductor. Broader engine/conformance impact is UNVERIFIED;
no new failing conformance case is attributed to this observation.
## rv042/rv044 verification and 4.24 resolution

oasis30/IIIA030 and IIIA330 now execute and pass in run 10 and the final clean run 11. The conductor-reviewed 4.24 mapper merge retains XPathCategory on assignments. Their prior red receipts above remain historical. The manifest rows are executed and their bug ids are cleared; this lane did not close tracking issues.
