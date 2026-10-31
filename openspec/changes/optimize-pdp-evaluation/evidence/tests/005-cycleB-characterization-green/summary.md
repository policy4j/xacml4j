# 005: cycle B characterization and green (tasks 5.1, 5.2)

Date: 2026-10-27. Oracle GraalVM 25.0.4, Maven 3.9.16 offline, release 25.

`xacml-core/src/test/java/org/xacml4j/v30/AttributeLookupCharacterizationTest.java`, 8 tests:
`findValues` order and duplicates across issuers, issuer and type filtering, unmodifiable results
(also for the one-attribute and the empty case), `Entity.resolve` bags, `hasValues`, `findValue`
first match in attribute order and with a filter, `Entity.find(id, issuer)` still a modifiable list
(public behavior kept), and `RequestContext.getEntity` returning the first entity of a category.
Before D6: 8 of 8 pass (characterization of today's behavior).

Change (D6): `AttributeIndex.findValues`, `hasValues`, `findValue` use index loops over the stored
immutable lists (new package-private `Attribute.valueList()`), return the attribute's own
immutable value list when one attribute supplies every matching value, and build an
`ImmutableList` otherwise; `RequestContext.getEntity` reads the first stored category without a
stream over a transformed view. Public `findByIssuer` and `Attribute.getValuesByType` are unchanged.

After D6: reactor `test` BUILD SUCCESS, 8,228 tests, 0 failures, 18 skipped (`xacml-core` 6,511 =
6,503 plus 8); conformance 938 run, 12 skipped (unchanged).
