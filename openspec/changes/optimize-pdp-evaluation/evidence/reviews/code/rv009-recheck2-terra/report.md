FINAL REPORT (RVC2R2 code reviewer, Codex GPT-6)

**Verdict:** The reported re-check fixes are present. A newly identified function-validation aliasing defect remains in cycle D, and cycle E has two smaller compatibility and evidence issues.

**P2 — Cycle D mutates the list retained by a runtime validator.** [FunctionSpecBuilder.java:399](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java:399) passes the normalized list to a custom validator, then evaluates that same list in place and passes it to the invocation. Before cycle D, eager evaluation produced a separate list. A validator that retains its argument list can now see it evaluated or structurally changed by a custom invocation, potentially changing the function result. The [design](openspec/changes/optimize-pdp-evaluation/design.md:133) says runtime validation uses the generic path. This is a newly identified residual from cycle D, not a defect caused by the latest re-check fix.

- **Fix:** Use the generic path when runtime parameter validation is enabled, preserving the existing exception wrapping.
- **Test:** Have a validator retain its list and an invocation remove an argument; compare the retained list and function result for pair and generic calls with validation enabled.

**P3 — Cycle E’s retained advice and obligation views still differ on null.** An early [LiveValues](xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java:145) inherits `AbstractCollection.contains(null)` and returns false. The previous `ConcurrentHashMap.values()` view throws `NullPointerException`; the JDK 25 source confirms both behaviors. A view obtained after the first response uses the concurrent map directly, so the result also depends on when the caller obtained it.

- **Fix:** Preserve the concurrent values-view null behavior in `LiveValues`.
- **Test:** Retain advice and obligation views before the first write and compare `contains(null)` before and after a write with the pre-cycle implementation.

**P3 — Cycle E’s cache-allocation claim and performance evidence need updating.** [DefaultEvaluationContextHandler.java:156](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java:156) now creates the designator cache when its view is read. The normal uncached PDP result path reads it even when every attribute came from the request. That contradicts the nearby claim that such a decision creates no PIP cache; the [design](openspec/changes/optimize-pdp-evaluation/design.md:160) also still says `SemanticIdentifier` caches its hash. The indexed JMH pairs do not establish the performance of this post-review-fix tree.

- **Fix:** Either preserve lazy cache creation with a correct backed view, or update the design and comment to describe the accepted allocation. Refresh the cycle E JMH evidence for the final implementation.
- **Test:** Instrument cache creation during a request-only PDP decision; benchmark the final tree against its matched baseline. The performance impact is **UNVERIFIED**.

**Disposition of prior findings**

- **RESOLVED — Variadic override dispatch and mutable pair arguments:** The pair default delegates to the variadic overload, and the built-in pair path uses an `ArrayList`. The separate validation aliasing defect above remains. [FunctionSpec.java:158](xacml-core/src/main/java/org/xacml4j/v30/policy/FunctionSpec.java:158), [FunctionSpecBuilder.java:382](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java:382)
- **RESOLVED — Identifier hash, including null fields:** Both fields are hashed afresh with null-safe `Objects.hashCode`; the updated test covers a null abbreviated ID. [SemanticIdentifier.java:102](xacml-core/src/main/java/org/xacml4j/v30/SemanticIdentifier.java:102)
- **RESOLVED — Detached resolved-map entry set and wrapper operations:** Both getters return an unmodifiable wrapper of the actual cache; the revised test retains an entry set and checks removal and null lookup. The selector’s later-entry case is symmetric in source but was not separately exercised in the cited test. [DefaultEvaluationContextHandler.java:156](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java:156)
- **RESOLVED — Null predicates and null rule:** The checks remain at the characterized points. [AttributeIndex.java:123](xacml-core/src/main/java/org/xacml4j/v30/AttributeIndex.java:123), [RootEvaluationContext.java:233](xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java:233)
- **RESOLVED — Concurrent status assertions:** Each worker’s rule and status are now checked. [RootEvaluationContextCharacterizationTest.java:189](xacml-core/src/test/java/org/xacml4j/v30/policy/RootEvaluationContextCharacterizationTest.java:189)
- **RESOLVED — Race-test ordering:** The test checks both waits, removes the modeled repository value before notification, and checks that it remains absent. The stale-cache race itself is pre-existing and remains a separate risk. [DefaultPolicyReferenceResolverCacheTest.java:137](xacml-pdp/src/test/java/org/xacml4j/v30/spi/repository/DefaultPolicyReferenceResolverCacheTest.java:137)
- **RESOLVED — Red-run count:** The summary now states nine red tests: seven failures and two errors. [Run 012 summary](openspec/changes/optimize-pdp-evaluation/evidence/tests/012-review-fixes-red-green/summary.md:7)

The separate `Attribute` serial UID fix remains present and has a recorded characterization test. [RequestFixedCostCharacterizationTest.java:158](xacml-core/src/test/java/org/xacml4j/v30/RequestFixedCostCharacterizationTest.java:158)

**Cycle verdicts:** A **APPROVE**; B **APPROVE**; D **REJECT**; E **APPROVE WITH CHANGES**; F **APPROVE**.

**Verification:** Read-only source, test and evidence inspection; no files were written and no tests, builds or benchmarks were run. The recorded [run 014](openspec/changes/optimize-pdp-evaluation/evidence/tests/014-recheck-fixes/summary.md:1) reports its characterizations passing before and after, and [run 015](openspec/changes/optimize-pdp-evaluation/evidence/tests/015-reactor-recheck-fixes/summary.md:1) reports 8,274 tests with zero failures or errors and 18 skipped. The new scenarios and final-tree performance remain **UNVERIFIED**.
