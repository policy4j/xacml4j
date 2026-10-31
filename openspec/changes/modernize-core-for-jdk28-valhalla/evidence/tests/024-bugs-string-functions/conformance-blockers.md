# Retained conformance blockers

These 28 cases were disabled in the final manifest and were not re-probed. Reasons and bug ids below are retained from the prior case classification, not fresh failures in this gate. All 16 cases targeted by bugs 4.14, 4.15 and 4.21 passed and are absent from this list.

| Suite | Case | Bug | Retained reason |
| --- | --- | --- | --- |
| oasis20 | IIA004 | policy4j-the.3.9 | Fixture/spec issue: Deliberately invalid policy omits AttributeId; importer rejects it; IIA004Special.txt permits validation-time rejection but the response-only path cannot evaluate it |
| oasis20 | IIA005 | policy4j-the.4.17 | Engine defect: Request omits AttributeId; XML mapping throws IllegalArgumentException before the PDP can return the expected syntax-error Indeterminate |
| oasis20 | IIC012 | policy4j-the.3.9 | Fixture/spec issue: Fixture declares a non-boolean Condition; static policy validation rejects its integer result before the supplied runtime Indeterminate response can be produced |
| oasis20 | IIC020 | policy4j-the.4.6 | Engine defect: integer-divide returns DoubleVal; integer-greater-than-or-equal then fails invocation type matching and returns Indeterminate instead of Permit |
| oasis20 | IIIC001 | policy4j-the.4.19 | Engine defect: 2.0 hierarchy response loses ResourceId urn:root; the XML mapper reads only included resource attributes but this single-resource request does not include them |
| oasis20 | IIIC002 | policy4j-the.4.18 | Engine defect: Configured default multiple-resource chain does not expand hierarchical Children/Descendants scope; expected 3 results but received one; IIICSpecial.txt describes the required resource tree |
| oasis20 | IIIC003 | policy4j-the.4.18 | Engine defect: Configured default multiple-resource chain does not expand hierarchical Children/Descendants scope; expected 7 results but received one; IIICSpecial.txt describes the required resource tree |
| oasis20 | IIIG001 | policy4j-the.4.20 | Engine defect: Legacy XPath over full request Attribute/Subject nodes is transformed into category Content lookup; it finds no request nodes or fails invocation instead of the vendored expected decision |
| oasis20 | IIIG002 | policy4j-the.4.20 | Engine defect: Legacy XPath over full request Attribute/Subject nodes is transformed into category Content lookup; it finds no request nodes or fails invocation instead of the vendored expected decision |
| oasis20 | IIIG003 | policy4j-the.4.20 | Engine defect: Legacy XPath over full request Attribute/Subject nodes is transformed into category Content lookup; it finds no request nodes or fails invocation instead of the vendored expected decision |
| oasis20 | IIIG004 | policy4j-the.4.20 | Engine defect: Legacy XPath over full request Attribute/Subject nodes is transformed into category Content lookup; it finds no request nodes or fails invocation instead of the vendored expected decision |
| oasis20 | IIIG005 | policy4j-the.4.20 | Engine defect: Legacy XPath over full request Attribute/Subject nodes is transformed into category Content lookup; it finds no request nodes or fails invocation instead of the vendored expected decision |
| oasis30 | IIA004 | policy4j-the.3.9 | Fixture/spec issue: Deliberately invalid policy omits AttributeId; importer rejects it; IIA004Special.txt permits validation-time rejection but the response-only path cannot evaluate it |
| oasis30 | IIA005 | policy4j-the.4.17 | Engine defect: Request omits AttributeId; XML mapping throws IllegalArgumentException before the PDP can return the expected syntax-error Indeterminate |
| oasis30 | IIA022 | policy4j-the.4.16 | Engine defect: Integer response value 56 is serialized as the IntegerVal debug string instead of the XML Schema integer lexical value 56 |
| oasis30 | IIA023 | policy4j-the.3.9 | Fixture/spec issue: Fixture expects Permit but request dateTime 1056-11-05T19:08:12-14:30 uses a timezone beyond the XML Schema maximum -14:00; JAXP rejects -870 minutes |
| oasis30 | IIA024 | policy4j-the.4.16 | Engine defect: Integer response value 56 is serialized as the IntegerVal debug string instead of the XML Schema integer lexical value 56 |
| oasis30 | IIC012 | policy4j-the.3.9 | Fixture/spec issue: Fixture declares a non-boolean Condition; static policy validation rejects its integer result before the supplied runtime Indeterminate response can be produced |
| oasis30 | IIC020 | policy4j-the.4.6 | Engine defect: integer-divide returns DoubleVal; integer-greater-than-or-equal then fails invocation type matching and returns Indeterminate instead of Permit |
| oasis30 | IID029 | policy4j-the.3.9 | Fixture/spec issue: Policy1 requires action-id in access-subject while the request supplies action-id in the action category; initial Target matching becomes Indeterminate instead of the expected Permit |
| oasis30 | IIIA340 | policy4j-the.4.16 | Engine defect: Positive infinity is serialized as Infinity instead of the XML Schema double lexical form INF in included attributes |
| oasis30 | IIIC002 | policy4j-the.4.18 | Engine defect: Configured default multiple-resource chain does not expand hierarchical Children/Descendants scope; expected 3 results but received one; IIICSpecial.txt describes the required resource tree |
| oasis30 | IIIC003 | policy4j-the.4.18 | Engine defect: Configured default multiple-resource chain does not expand hierarchical Children/Descendants scope; expected 7 results but received one; IIICSpecial.txt describes the required resource tree |
| oasis30 | IIIE301 | policy4j-the.3.7 | Fixture/spec issue: Draft fixture uses urn:oasis:names:tc:xacml:3.0:multiple:content-selector; engine recognizes the final profile:multiple:content-selector identifier; expected two results but received one |
| oasis30 | IIIE302 | policy4j-the.3.7 | Harness defect: xacml-test compares included category lists by position; actual subject-first and expected resource-first categories fail despite order-independent XACML category meaning; comparison is outside this lane |
| oasis30 | IIIE303 | policy4j-the.3.7 | Engine defect: RequestReference JAXB IDREF resolves to AttributesType; XML request mapper casts it to String and throws ClassCastException before evaluation |
| oasis30 | IIIG300 | policy4j-the.4.22 | Engine defect: Returned PolicyIdentifierList contains 5 policy references instead of the expected 2; non-applicable/evaluated reference tracking includes extra policies |
| oasis30 | IIIG301 | policy4j-the.4.22 | Engine defect: Returned PolicyIdentifierList contains 4 policy references instead of the expected 3; non-applicable/evaluated reference tracking includes extra policies |
