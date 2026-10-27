# 018: cycle H tests, red and green (tasks 12.1, 12.2)

Date: 2026-10-27. Oracle GraalVM 25.0.4, Maven 3.9.16 offline, release 25.

`xacml-core/src/test/java/org/xacml4j/v30/policy/EmptyBagCharacterizationTest.java`, 4 tests:
the empty bag of seven types keeps its value type, bag type, equality, hash, and serialized form
(round trip), also through `bagType().emptyBag()`, and types differ; `DynamicType` still throws on
every call; `AttributeDesignator.evaluate` returns the resolved bag itself, an empty bag of the type
when the attribute is absent and not required, and fails when it is required; one empty bag is
shared per type (the optimization).

Before D14: 3 of 4 pass (characterization); `oneEmptyBagIsSharedPerType` fails (red, as intended).
After D14: 4 of 4, and `BagOfValuesCharacterizationTest` 6, `BagOfValuesTest` 11, `AttributeDesignatorTest` 9,
`AttributeSelectorTest` 15, `MatchTest` 5. Reactor: `019-reactor-cycleH`.

Output: `output.txt`.
