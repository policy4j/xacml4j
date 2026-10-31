FINAL REPORT (RVC2 code reviewer, Codex GPT-6)

**Verdict:** Reject cycles D and E pending fixes. Cycle A is approved for this review; cycles B and F need changes described below. These verdicts do not close the outstanding project gates.

**P2 — Cycle D: the new overload bypasses existing function overrides.** [FunctionSpec.java:155](xacml-core/src/main/java/org/xacml4j/v30/policy/FunctionSpec.java:155) calls `invokeWithList` directly. An existing custom `FunctionSpec` can override the earlier variadic `invoke` method; after the library is rebuilt, [Match.java:121](xacml-core/src/main/java/org/xacml4j/v30/policy/Match.java:121) selects the new overload and bypasses that override. A predicate can therefore return a different result or status.

- **Fix:** Have the default two-argument overload delegate to the variadic overload using an explicit `Expression[]`; retain the optimized override for the built-in implementation.
- **Test:** Use a custom `FunctionSpec` whose variadic override returns a distinguishable result from `invokeWithList`; exercise it through `Match`.
- **Checked:** Interface, dispatch call site, builder and characterization tests. **Not checked:** Executable reproduction.

**P2 — Cycle D: the optimized invocation receives a fixed-size list.** [FunctionSpecBuilder.java:382](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java:382) creates an `Arrays.asList` list and passes it, after in-place evaluation, to the public `FunctionInvocation` SPI at line 417. Before the change, eager evaluation supplied a structurally modifiable `ArrayList`. A custom invocation that removes or adds a parameter now throws `UnsupportedOperationException`, which is wrapped as a function failure and can make a match Indeterminate.

- **Fix:** Give the optimized invocation a structurally modifiable list, or retain the old path for custom invocations.
- **Test:** Register a two-argument eager `FunctionInvocation` that removes an argument before returning; compare the result and status before and after.
- **Checked:** Current path, exception wrapping, SPI and pre-change diff. **Not checked:** Executable reproduction or external providers.

**P2 — Cycle E: cached hashes violate equality for extensible identifiers.** [SemanticIdentifier.java:53](xacml-core/src/main/java/org/xacml4j/v30/SemanticIdentifier.java:53) exposes mutable `id` and `abbreviatedId` fields to subclasses, while [hashCode():104](xacml-core/src/main/java/org/xacml4j/v30/SemanticIdentifier.java:104) now caches their first hash. If a subclass hashes an instance, changes an identifier field, and compares it with a newly constructed equal instance, `equals` can return true while their hashes differ. Hash-based lookup can then miss an equal identifier.

- **Fix:** Recompute the hash while those protected fields remain mutable.
- **Test:** Use a subclass that changes each protected field after the first hash; assert equality and equal hashes against a fresh instance, including a hash-map lookup.
- **Checked:** Field visibility, equality and old/new hash implementations. **Not checked:** External subclasses or a runtime reproduction.

**P2 — Cycle E: empty fast paths replace live public views with permanent empty collections.** Before the change, the resolved-designator and selector maps were unmodifiable views of their backing caches. [DefaultEvaluationContextHandler.java:152](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java:152) now returns a detached empty map before the first PIP result. Likewise, [RootEvaluationContext.java:436](xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java:436) returns detached empty advice and obligation lists before the first response. An extension that retains one of these collections before resolution or staging no longer sees later entries. The empty [evaluated-policy list](xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java:407) also changes from a new modifiable list to `Collections.emptyList()`.

- **Fix:** Preserve the backed views, initializing their maps on read if necessary, and return a fresh modifiable evaluated-policy list in the empty case.
- **Test:** Capture each view before its first write, add the corresponding value, and assert the retained view observes it; also characterize mutation of an empty evaluated-policy result.
- **Checked:** Public `EvaluationContext` delegation and old/new implementations. **Not checked:** External consumers or an executable reproduction.

**P3 — Cycles B and E: null rejection now depends on whether data exists.** In [AttributeIndex.java:122](xacml-core/src/main/java/org/xacml4j/v30/AttributeIndex.java:122), `find` and `findFirst` no longer reject a null predicate when the index is empty; the previous stream `filter` rejected it immediately. `findValue` similarly skips a null filter when an existing matching attribute has no values of the requested type. [RootEvaluationContext.java:199](xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java:199) returns empty for a null rule before the status map exists, but throws after it exists.

- **Fix:** Restore null checks at the points where the previous implementations performed them.
- **Test:** Compare empty and populated index cases, an attribute with no values of the requested type, and status lookup before and after a first write.
- **Checked:** Pre-change diff and current branches. **Not checked:** Executable reproduction.

**P3 — Cycle F: the checked test evidence does not exercise a repository change during resolution.** [Task 9.1](openspec/changes/optimize-pdp-evaluation/tasks.md:53) marks that scenario done. The indexed [cycle F test](xacml-pdp/src/test/java/org/xacml4j/v30/spi/repository/DefaultPolicyReferenceResolverCacheTest.java:73) performs listener invalidation after resolution; its concurrent test returns a fixed repository value. It does not cover an update between repository lookup and [cache insertion](xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/DefaultPolicyReferenceResolver.java:87). The old implementation also had that lookup–invalidation–put race; a stale cached policy is therefore a **pre-existing risk**, not a proven cycle F regression.

- **Fix:** Add the missing before/after characterization and record the stale-cache race separately if reproduced.
- **Test:** Block a repository lookup after it captures the old policy, deliver a removal or replacement notification, release the lookup, then inspect the subsequent resolution and decision.
- **Checked:** Indexed test, resolver and pre-change resolver. **Not checked:** The interleaving at runtime or all existing conformance cases.

**P3 — Cycle E: the concurrent first-write test does not assert status writes.** [RootEvaluationContextCharacterizationTest.java:156](xacml-core/src/test/java/org/xacml4j/v30/policy/RootEvaluationContextCharacterizationTest.java:156) writes a rule status from each thread but only checks policy and advice counts. The [run summary](openspec/changes/optimize-pdp-evaluation/evidence/tests/009-cycleE-characterization-green/summary.md:9) describes the concurrent writes as losing nothing, which the assertions do not establish for statuses.

- **Fix:** Retain the rule and status for each worker and assert each mapping after all workers finish.
- **Test:** Repeat the barrier-controlled first-write case with distinct rule/status pairs and verify every lookup.
- **Checked:** Test body and recorded summary. **Not checked:** Runtime behavior under contention.

**Cycle verdicts**

- **A — APPROVE:** The request memo is decision-scoped and uses `putIfAbsent` for concurrent first publication. The separately documented PIP-absence semantics gap remains outside this cycle. [Handler](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java:82)
- **B — APPROVE WITH CHANGES:** Restore the null-filter behavior above; the reviewed ordinary lookup ordering and collection tests cover the intended path.
- **D — REJECT:** Both function-extension compatibility defects can change match behavior.
- **E — REJECT:** The cached identifier hash and detached public views change observable behavior; the concurrency proof also needs the missing assertion.
- **F — APPROVE WITH CHANGES:** Add the required overlapping repository-change characterization; do not treat the pre-existing invalidation race as a new regression.

**Verification and residual risk:** This was a read-only source, diff, test and recorded-evidence review. No files were written and no builds, tests, benchmarks or profilers were run. The recorded [reactor run](openspec/changes/optimize-pdp-evaluation/evidence/tests/011-reactor-cycles-DEF/summary.md:1) reports success on JDK 25; it is historical evidence, not a fresh run by this lane. Runtime reproductions, external extension usage, benchmark isolation and the remaining gate reviews are **UNVERIFIED**.
