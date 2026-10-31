READ-ONLY RE-CHECK (a new packet in this pane; same rules: no file writes, no Maven, tests, benchmarks,
git or br writes). Role: the reviewer of RVH1. Lane tag RVH2.

Re-check cycle H round 2 against your RVH1 findings (stored at
openspec/changes/optimize-pdp-evaluation/evidence/reviews/code/rvh1-cycleH-sol/report.md). Round 2:
- xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java and types/ValueType.java are back to HEAD
  (no static map; the ValueType default emptyBag() is bagBuilder().build()).
- xacml-core/src/main/java/org/xacml4j/v30/types/XacmlTypes.java: each enum constant caches its own empty
  bag in an instance field, built with bagBuilder() on first use (a benign race: two threads can build two
  equal bags; one wins).
- xacml-core/src/main/java/org/xacml4j/v30/BagOfValuesType.java: emptyBag() returns the cached bag for a
  built-in XacmlTypes constant and builder().build() for any other type.
- AttributeReference.evaluate and AttributeDesignatorFunctions: v.isPresent() ? v.get() : ...emptyBag().
- tools/ast-grep/rules/java-no-eager-empty-bag.yml with its test.
- Tests: xacml-core/src/test/java/org/xacml4j/v30/policy/EmptyBagCharacterizationTest.java, now with
  customTypeKeepsItsBagBuilderAndIsNotShared and presentAttributeDoesNotBuildAnEmptyBag.
Evidence: evidence/tests/020-cycleH-round2 (red on HEAD 2 of 6, green 126) and 021-reactor-H-harness.

Answer per RVH1 finding: resolved, partly resolved, or not resolved, with file:line. Then look for new
defects in round 2: the unsynchronized field in an enum (publication of a BagOfValues through a data race:
is every field final, so that a racy read sees a fully built bag?), serialization of an enum-cached bag,
a remaining eager fallback on the decide path, and tests that pass for the wrong reason.

Report: findings most severe first (P1 to P3) with file:line, the smallest fix, and the test; then a
verdict: APPROVE, APPROVE WITH CHANGES, or REJECT.

Write your report between these two lines, each on its own line, exactly as written:
=== BEGIN RVH2 REPORT ===
=== END RVH2 REPORT ===
