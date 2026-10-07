# rv040: bugs 4.19 and 3.7 (IIIE301, IIIE303) review (Agy)

Date: 2026-10-02. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity,
Gemini 3.8 Flash (High). Session `policy4j--g1`, pane 3. Read-only.

Scope: xmlbugs lane patch (340 lines): RequestContext, Xacml20RequestContextUnmarshaller,
Xacml30RequestContextFromJaxbToObjectModelMapper, MultipleResourcesViaXPathExpressionHandler
and tests.

Verdict: APPROVE (2 LOW).

Findings:
1. LOW, `Xacml20RequestContextUnmarshaller.java:204-205`: the scope check
   matches only urn:oasis:names:tc:xacml:1.0:resource:scope; the 2.0
   Hierarchical Resource Profile identifier
   urn:oasis:names:tc:xacml:2.0:resource:scope (used by fixtures such as
   001A-Request.xml:53) is missed, so ResourceId would be omitted.
2. LOW, `Xacml30RequestReferenceMappingTest.java:31`: tests cover String and
   unsupported Object references but not the AttributesType path (with an
   xml:id, and the null-id syntax error).

Reviewer statements: RequestContext adds no field; equals/hashCode/builder
unchanged; the category reference id is now kept for included attributes
(RequestContextIncludeInResultTest, red then green). Result/@ResourceId is
emitted for multiple-resource or scoped 2.0 requests only (2.0 section 6.10);
single-resource requests keep it absent (testRequestIIA13Mapping); IIIC001
passes. IDREF to AttributesType maps through xml:id; a missing id or an
unsupported object is a SyntaxException; duplicate ids are rejected by
ImmutableMap.Builder. The draft multiple:content-selector is accepted on
input, normalized, and never appears in output; result order kept
(LinkedHashSet). Vendored files unchanged. IIIE301/IIIE303 reach the
comparison and hit the known harness NPE (bug 3.11).
Resolution: LOW items to the implementer lane with the next packet.
