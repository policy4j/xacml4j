FINAL REPORT (RVC2R code reviewer, Codex GPT-6)

**Verdict:** The function fixes are resolved. Cycle E still changes a retained public map view, and the cycle F race test can pass without exercising the intended interleaving.

**P2 — A retained resolved-map entry set is still detached.** [DefaultEvaluationContextHandler.java:188](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java:188) returns `Collections.emptySet()` when `entrySet()` is called before the cache exists. If a caller retains that set, resolves a PIP attribute, and reads the set again, it remains empty although the retained map now contains the attribute. The previous unmodifiable map exposed a backed entry set. The same issue applies to selector entries.

The new `LiveView` also inherits `AbstractMap` behavior for operations such as `remove` of an absent key and `containsValue(null)`, which differs from the previous unmodifiable concurrent-map wrapper.

- **Fix:** Return a genuinely backed, unmodifiable map and entry set before the first write—for example, initialize the cache when its public view is requested.
- **Test:** Retain `entrySet()` before the first designator and selector resolutions, then assert that each set sees its later entry. Also characterize no-op mutation and null-value lookup.

**P3 — Identifier hashing still changes the null-field behavior.** [SemanticIdentifier.java:105](xacml-core/src/main/java/org/xacml4j/v30/SemanticIdentifier.java:105) now recalculates the hash, resolving the stale-cache defect for changed non-null fields. However, a subclass can set the protected `abbreviatedId` to null: `equals` handles that value at line 90, and the original `Objects.hash` did too, but the replacement calls `abbreviatedId.hashCode()` and throws. This regression was introduced by the optimization and remains after the fix.

- **Fix:** Use null-safe field hashes while retaining the array-free calculation.
- **Test:** Set `abbreviatedId` to null in the mutable subclass and compare equality and hash behavior with the pre-cycle implementation.

**P3 — The repository-race characterization can pass without the race.** [DefaultPolicyReferenceResolverCacheTest.java:151](xacml-pdp/src/test/java/org/xacml4j/v30/spi/repository/DefaultPolicyReferenceResolverCacheTest.java:151) ignores the result of `inLookup.await`. If that wait times out before the worker enters the lookup, the test can send the removal first, release the latch, and subsequently pass with a lookup that started after removal. The mock also continues to return the same policy rather than modeling its removal.

- **Fix:** Assert both latch waits succeed and model the repository’s state changing before the notification.
- **Test:** Block after capturing the old policy, remove it from the repository, notify the resolver, release the lookup, and independently assert the repository and subsequent resolver results. Keep the stale-cache behavior labeled as pre-existing.

**Disposition of the RVC2 findings**

- **RESOLVED — Variadic override dispatch:** The default pair overload delegates through the variadic method, with a `Match` characterization. [FunctionSpec.java:158](xacml-core/src/main/java/org/xacml4j/v30/policy/FunctionSpec.java:158)
- **RESOLVED — Fixed-size invocation arguments:** The pair path now creates a modifiable `ArrayList`; the added test removes and adds an argument. [FunctionSpecBuilder.java:382](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java:382)
- **RESOLVED — Stale cached hash after changing non-null identifier fields:** The hash is recalculated and the test checks equality and hash-set lookup. The separate null-field regression above remains. [SemanticIdentifier.java:102](xacml-core/src/main/java/org/xacml4j/v30/SemanticIdentifier.java:102)
- **NOT RESOLVED — Retained public views:** Retained map and response-collection reads are improved, and the empty policy list is modifiable, but a retained resolved-map entry set remains detached as described above. [Handler test](xacml-pdp/src/test/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandlerTest.java:965)
- **RESOLVED — Null predicates and null rule:** The checks now occur at the characterized points, including `findValue` after an attribute passes the issuer check. [AttributeIndex.java:123](xacml-core/src/main/java/org/xacml4j/v30/AttributeIndex.java:123), [RootEvaluationContext.java:233](xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java:233)
- **RESOLVED — Concurrent status assertions:** The test retains every rule and status and checks each result after the workers finish. [RootEvaluationContextCharacterizationTest.java:189](xacml-core/src/test/java/org/xacml4j/v30/policy/RootEvaluationContextCharacterizationTest.java:189)
- **NOT RESOLVED — Repository change during lookup:** A characterization was added, but its unchecked latch permits the false-positive schedule above.

The RVC1 `Attribute` serialization concern is also addressed by removing the added accessor and testing the recorded base UID. [AttributeIndex.java:254](xacml-core/src/main/java/org/xacml4j/v30/AttributeIndex.java:254), [UID test](xacml-core/src/test/java/org/xacml4j/v30/RequestFixedCostCharacterizationTest.java:158)

**Cycle verdicts:** A **APPROVE**; B **APPROVE**; D **APPROVE**; E **REJECT**; F **APPROVE WITH CHANGES**.

**Verification:** Read source, tests, dispositions and recorded runs only; no files, builds, tests or benchmarks were changed or run. The recorded reactor summary reports 8,274 tests, zero failures or errors, and 18 skipped. The red/green summary says ten tests failed on the pre-fix tree, while its retained output lists **seven failures and two errors** across the named classes. The red-test count needs correction; runtime behavior and the proposed additional tests remain **UNVERIFIED**. [Run 012 summary](openspec/changes/optimize-pdp-evaluation/evidence/tests/012-review-fixes-red-green/summary.md:7), [output](openspec/changes/optimize-pdp-evaluation/evidence/tests/012-review-fixes-red-green/output.txt:1), [reactor summary](openspec/changes/optimize-pdp-evaluation/evidence/tests/013-reactor-review-fixes/summary.md:1)
