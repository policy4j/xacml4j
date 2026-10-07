# Remaining conformance dispositions

Final merged XML gates (09 and 10) passed: 857 cases executed, 0 failed, 23 skipped.

IIIA030 and IIIA330 now reference policy4j-the.4.24 in the manifest and run 030 blockers.
IIA022, IIA024, IIIA340 and IIIG301 passed and are enabled with their bug fields cleared.
IIIE301 and IIIE303 passed after the reviewed XML lane merge and are now enabled; IIIC001 was already enabled and passed again.
IIIG300 stays skipped under the conductor's section 5.48 adjudication; both IIA005 cases stay skipped pending request-error response mapping.

| Suite | Case | Disposition | Bug | Reason |
| --- | --- | --- | --- | --- |
| oasis20 | IIA004 | skipped | policy4j-the.3.9 | Fixture/spec issue: Deliberately invalid policy omits AttributeId; importer rejects it; IIA004Special.txt permits validation-time rejection but the response-only path cannot evaluate it |
| oasis20 | IIA005 | skipped | policy4j-the.4.17 | Request syntax error: Missing AttributeId is rejected with SyntaxException; mapping request syntax errors to a response remains pending decision |
| oasis20 | IIC012 | skipped | policy4j-the.3.9 | Fixture/spec issue: Fixture declares a non-boolean Condition; static policy validation rejects its integer result before the supplied runtime Indeterminate response can be produced |
| oasis20 | IIC020 | skipped | policy4j-the.4.6 | Engine defect: integer-divide returns DoubleVal; integer-greater-than-or-equal then fails invocation type matching and returns Indeterminate instead of Permit |
| oasis20 | IIIC001 | skipped | policy4j-the.4.19 | Engine defect: 2.0 hierarchy response loses ResourceId urn:root; the XML mapper reads only included resource attributes but this single-resource request does not include them |
| oasis20 | IIIC002 | skipped | policy4j-the.4.18 | Engine defect: Configured default multiple-resource chain does not expand hierarchical Children/Descendants scope; expected 3 results but received one; IIICSpecial.txt describes the required resource tree |
| oasis20 | IIIC003 | skipped | policy4j-the.4.18 | Engine defect: Configured default multiple-resource chain does not expand hierarchical Children/Descendants scope; expected 7 results but received one; IIICSpecial.txt describes the required resource tree |
| oasis20 | IIIG001 | skipped | policy4j-the.4.20 | Engine defect: Legacy XPath over full request Attribute/Subject nodes is transformed into category Content lookup; it finds no request nodes or fails invocation instead of the vendored expected decision |
| oasis20 | IIIG002 | skipped | policy4j-the.4.20 | Engine defect: Legacy XPath over full request Attribute/Subject nodes is transformed into category Content lookup; it finds no request nodes or fails invocation instead of the vendored expected decision |
| oasis20 | IIIG003 | skipped | policy4j-the.4.20 | Engine defect: Legacy XPath over full request Attribute/Subject nodes is transformed into category Content lookup; it finds no request nodes or fails invocation instead of the vendored expected decision |
| oasis20 | IIIG004 | skipped | policy4j-the.4.20 | Engine defect: Legacy XPath over full request Attribute/Subject nodes is transformed into category Content lookup; it finds no request nodes or fails invocation instead of the vendored expected decision |
| oasis20 | IIIG005 | skipped | policy4j-the.4.20 | Engine defect: Legacy XPath over full request Attribute/Subject nodes is transformed into category Content lookup; it finds no request nodes or fails invocation instead of the vendored expected decision |
| oasis30 | IIA004 | skipped | policy4j-the.3.9 | Fixture/spec issue: Deliberately invalid policy omits AttributeId; importer rejects it; IIA004Special.txt permits validation-time rejection but the response-only path cannot evaluate it |
| oasis30 | IIA005 | skipped | policy4j-the.4.17 | Request syntax error: Missing AttributeId is rejected with SyntaxException; mapping request syntax errors to a response remains pending decision |
| oasis30 | IIA023 | skipped | policy4j-the.3.9 | Fixture/spec issue: Fixture expects Permit but request dateTime 1056-11-05T19:08:12-14:30 uses a timezone beyond the XML Schema maximum -14:00; JAXP rejects -870 minutes |
| oasis30 | IIC012 | skipped | policy4j-the.3.9 | Fixture/spec issue: Fixture declares a non-boolean Condition; static policy validation rejects its integer result before the supplied runtime Indeterminate response can be produced |
| oasis30 | IIC020 | skipped | policy4j-the.4.6 | Engine defect: integer-divide returns DoubleVal; integer-greater-than-or-equal then fails invocation type matching and returns Indeterminate instead of Permit |
| oasis30 | IID029 | special-harness | policy4j-the.3.9 | Fixture/spec issue: Policy1 requires action-id in access-subject while the request supplies action-id in the action category; initial Target matching becomes Indeterminate instead of the expected Permit |
| oasis30 | IIIA030 | skipped | policy4j-the.4.24 | Engine XML output defect: obligation assignment2 loses XPathCategory resource metadata in Xacml30RequestContextFromJaxbToObjectModelMapper.create(AttributeAssignment) |
| oasis30 | IIIA330 | skipped | policy4j-the.4.24 | Engine XML output defect: advice assignment2 loses XPathCategory resource metadata in Xacml30RequestContextFromJaxbToObjectModelMapper.create(AttributeAssignment) |
| oasis30 | IIIC002 | skipped | policy4j-the.4.18 | Engine defect: Configured default multiple-resource chain does not expand hierarchical Children/Descendants scope; expected 3 results but received one; IIICSpecial.txt describes the required resource tree |
| oasis30 | IIIC003 | skipped | policy4j-the.4.18 | Engine defect: Configured default multiple-resource chain does not expand hierarchical Children/Descendants scope; expected 7 results but received one; IIICSpecial.txt describes the required resource tree |
| oasis30 | IIIG300 | skipped | policy4j-the.3.9 | Fixture/spec adjudication: Fixture expects 2 policy ids; XACML 3.0 Plus Errata 01 section 5.48 lists all fully applicable policies so the engine returns 3 |
