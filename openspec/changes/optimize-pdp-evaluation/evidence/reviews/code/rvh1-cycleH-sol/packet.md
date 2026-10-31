READ-ONLY CODE REVIEW (second packet in this pane; same rules: no file writes, no Maven, tests, benchmarks,
git or br writes). Role: code reviewer from a model family other than the author's (the conductor, Claude
Opus 5.5, wrote the change). Lane tag RVH1.

Review cycle H of openspec/changes/optimize-pdp-evaluation: design.md D14, tasks.md group 12, and the
changed files (compare with the last commit, HEAD):
- xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java (static `empty(ValueType)` with a ConcurrentHashMap)
- xacml-core/src/main/java/org/xacml4j/v30/BagOfValuesType.java (`emptyBag()`)
- xacml-core/src/main/java/org/xacml4j/v30/types/ValueType.java (default `emptyBag()`)
- xacml-core/src/main/java/org/xacml4j/v30/policy/AttributeReference.java (`evaluate`: no eager orElse)
- xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/AttributeDesignatorFunctions.java (two sites)
Tests: xacml-core/src/test/java/org/xacml4j/v30/policy/EmptyBagCharacterizationTest.java; evidence
evidence/tests/018-cycleH-red-green, 019-reactor-cycleH, and the JMH pair 019/020 when present.

Look for: a caller that can observe the shared instance (identity, mutation through `values()`, the lazy
hash field, serialization and deserialization of a shared bag, a bag type subclass); a type for which caching
is wrong (custom `ValueType` implementations, types with equal ids, class loaders); memory growth of the
static map; a changed exception or status for a type without bags; a remaining eager fallback on the decide
path; tests that pass for the wrong reason.

Report: findings most severe first (P1 to P3) with file:line, the smallest fix, and the test; then a
verdict: APPROVE, APPROVE WITH CHANGES, or REJECT.

Write your report between these two lines, each on its own line, exactly as written:
=== BEGIN RVH1 REPORT ===
=== END RVH1 REPORT ===
