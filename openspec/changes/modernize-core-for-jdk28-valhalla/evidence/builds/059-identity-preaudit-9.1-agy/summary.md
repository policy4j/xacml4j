# Run 059: pre-audit for task 9.1, identity use of value-class candidates (Agy)

Date: 2026-10-02. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity;
pane shows Gemini 3.8 Flash (High). Session `policy4j--g1`, pane 3, about 50-130
tool calls (the report's own count differs from the pane count). Read-only,
against the current pre-refactor types; redo after groups 5-7. Raw report:
[agy-report.txt](agy-report.txt).

Result: 25 types audited. No fundamental identity blocker (no
`synchronized` on values, no identityHashCode, no identity or weak maps
keyed by values reported); 23 types need structural changes before they can
become value classes; 2 donor types reported eligible (not in the worktree).
37 sites: superclasses with instance fields (Value.AbstractValue,
AttributeReferenceKey, SemanticIdentifier, ISO8601Duration), non-final fields
(Value.OtherValue, Binary, PortRange, DNSName, CategoryId, designator and
selector keys), non-final classes (IPAddress, StatusCodeId), cached
hash/string fields (designator/selector keys, ISO8601Duration),
Externalizable in-place mutation (Binary, PortRange), mutable wrapped
objects (ByteBuffer in Binary, javax.xml Duration), and `assertSame`
identity assertions in StatusCodeIdTest and CategoryIdTest.

## Conductor spot check (2026-10-02)

Confirmed: `xacml-core/src/main/java/org/xacml4j/v30/types/Binary.java:44-48,
201` - `Binary implements Externalizable` with a non-final `ByteBuffer value`
that `readExternal` assigns; a value class cannot be deserialized by in-place
mutation (needs readResolve/serialization proxy or no Java serialization).

Use: the 9.x work starts from this list after groups 5-7 (types renamed and
reshaped); `==` on values compares state under JEP 401, so the
`assertSame` tests must change to equality.
