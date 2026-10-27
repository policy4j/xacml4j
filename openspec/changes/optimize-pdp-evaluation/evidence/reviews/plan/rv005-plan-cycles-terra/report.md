<!-- Plan review RVP of tasks 2.2 (cycles A to G): Codex GPT-6.1 Terra, read-only, NTM session xacml4j--pdp-profile-1 pane 0, after one follow-up. -->

FINAL REPORT (RVP architecture reviewer, Codex GPT-6.1 Terra)

**Verdict: REJECT.** The cycle order is plausible: request-bag reuse should precede work on allocations it removes, and later cycles need fresh measurements. The plan nevertheless permits observable decision changes in cycle C, leaves the concurrency and reference-context contracts unresolved, and does not yet establish clean before/after evidence.

### HIGH — Cycle C changes bag iteration while the plan promises to retain current error behavior

[Design D7](openspec/changes/optimize-pdp-evaluation/design.md:102) specifies insertion-order arrays. The existing builder creates an `ImmutableMultiset` ([BagOfValues.java](xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java:276)); Guava 31.1 groups equal elements during iteration. Thus input `[a, b, a]` currently iterates as `[a, a, b]`, but the proposed array iterates as `[a, b, a]`. [Guava 31.1 documentation](https://guava.dev/releases/31.1-jre/api/docs/com/google/common/collect/ImmutableMultiset.html)

That matters to the existing [Match.match](xacml-core/src/main/java/org/xacml4j/v30/policy/Match.java:111): an extension predicate that returns false for the first `a`, throws for the second, and returns true for `b` yields Indeterminate in the current order and Match in the proposed order. The [synthesis](openspec/changes/optimize-pdp-evaluation/research/synthesis.md:57) explicitly requires the existing §7.6 and §7.7 defects to remain unchanged in optimization cycles. [XACML 3.0 §§7.6–7.7](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html)

**Required change:** Preserve the current effective iteration and representative-value behavior during cycle C, or explicitly separate and approve a behavior change. Add characterization tests for interleaved duplicates and error-before-match, and for target Indeterminate followed by No-match. Compare complete decisions and statuses, not only bag equality. Test NaN and signed-zero set behavior against the existing [double equality cases](xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/DoubleBagSetEqualityTest.java:81); [XACML A.3.10 and A.3.11](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html) distinguish bags and set operations.

**Checked:** D7, current bag builder and Match loop, Guava’s documented iteration, and the standard. **Not checked:** executable reproduction; no tests ran.

### HIGH — Cycle A promises one lookup without a same-context concurrency contract

The [new requirement](openspec/changes/optimize-pdp-evaluation/specs/pdp-attribute-resolution/spec.md:3) says the request is searched once and the callback is called once. The current shared tree already has a cache lookup followed by `getEntity`, then `putIfAbsent` ([DefaultEvaluationContextHandler.java](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java:116)). Two concurrent resolutions of the same key can both miss and call the callback before either inserts.

The factory creates a handler per individual decision, but [the PIP SPI](xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/PolicyInformationPoint.java:52) receives the `EvaluationContext`; per-decision ownership alone does not prove that a PIP never calls it back from another thread. The proposed tests in [task 4.1](openspec/changes/optimize-pdp-evaluation/tasks.md:22) do not settle that contract.

**Required change:** Define whether same-context concurrent callbacks are supported. If supported, specify reentrant-safe publication and the required lookup-count behavior; do not introduce a single-flight scheme that deadlocks on a PIP callback. Add a barrier-controlled same-key race, recursive PIP callbacks, and a real multiple-decision request through the factory. Keep request-origin bags separate from PIP response provenance. Express the XACML requirement in terms of equivalent bag values; the same Java instance and callback count are implementation checks, not requirements of §7.3.5.

**Checked:** Factory scope, public PIP signature, cache sequence, spec and current tests. **Not checked:** whether deployed PIPs use concurrent callbacks; runtime race reproduction.

### HIGH — Cycle F removes context boundaries without testing their semantics

[D10](openspec/changes/optimize-pdp-evaluation/design.md:131) calls for one context object per reference. Today [PolicyIDReference](xacml-core/src/main/java/org/xacml4j/v30/policy/PolicyIDReference.java:149) walks parent contexts to detect cycles; [BaseCompositeDecisionRule](xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java:202) walks the chain to decide whether to stage obligations and advice. `getParentContext()` is also part of the public [EvaluationContext](xacml-core/src/main/java/org/xacml4j/v30/EvaluationContext.java:198) interface. Task [9.1](openspec/changes/optimize-pdp-evaluation/tasks.md:52) names variables, versions and repository changes, but not those boundaries.

**Required change:** Specify how the logical parent chain, cycle detection and §7.18 response staging remain equivalent. Add tests for cyclic references, nested policy sets, extended Indeterminate, failed obligations, advice publication, policy-ID reporting, and a custom extension inspecting parent contexts. If the public parent topology changes, mark that compatibility change **BREAKING** and obtain a maintainer decision.

**Checked:** Reference construction, cycle walk, staging walk and public context interface. **Not checked:** an implementation of the proposed flattened context or external consumers.

### MEDIUM — PIP absence exposes an additional §7.3.5 discrepancy

[D5](openspec/changes/optimize-pdp-evaluation/design.md:83) deliberately leaves PIP absence uncached. The present [characterization test](xacml-pdp/src/test/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandlerTest.java:885) models an empty PIP result followed by a value. With `MustBePresent=false`, [AttributeReference](xacml-core/src/main/java/org/xacml4j/v30/policy/AttributeReference.java:104) turns the first absence into an empty bag; a later test can see the new value. XACML §7.3.5 requires evaluation to behave as though the initially tested bag remains immutable. [XACML 3.0 §7.3.5](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html)

**Required change:** Record this as a separate, currently unverified semantics defect and narrow any claim that cycle A implements all of §7.3.5. Preserve the existing behavior in this optimization until a separately approved semantics change decides how to handle missing values and failures.

**Checked:** Planned no-absence policy, handler test, reference conversion and standard. **Not checked:** an end-to-end PDP reproduction.

### MEDIUM — D7’s serialization and collection compatibility decision is conditional on the wrong evidence

[BagOfValues](xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java:58) is `Serializable` and currently stores a non-transient `Multiset`. D7 acknowledges a changed serialized form but proposes marking it **BREAKING** only if a review finds persistent use ([design.md](openspec/changes/optimize-pdp-evaluation/design.md:104)). Compatibility of an old stream is a property of the formats; finding a consumer determines impact, not whether the format changed. The returned `values()` collection also currently has multiset equality behavior, which a list does not. [Guava multiset documentation](https://github.com/google/guava/wiki/NewCollectionTypesExplained)

**Required change:** Either define and test backward-compatible serialization and collection behavior, or mark the change **BREAKING** now and obtain the maintainer’s decision. Add old-stream-to-new-reader tests, duplicate/order-sensitive `values()` tests, and builder snapshot tests to task 6.1.

**Checked:** Serializable declaration, fields, `values()` and D7. **Not checked:** external serialized-bag consumers or a candidate serialization format.

### MEDIUM — Cycle B’s zero-copy return conflicts with its compatibility claim

[D6](openspec/changes/optimize-pdp-evaluation/design.md:95) proposes returning a stored immutable list while claiming the same collection behavior. The existing public [findByIssuer](xacml-core/src/main/java/org/xacml4j/v30/AttributeIndex.java:105) and [getValuesByType](xacml-core/src/main/java/org/xacml4j/v30/Attribute.java:114) collect new lists. OpenJDK 25 currently uses `ArrayList` for `Collectors.toList()`, although the JDK API does not guarantee that implementation. [OpenJDK 25 source](https://raw.githubusercontent.com/openjdk/jdk25u/master/src/java.base/share/classes/java/util/stream/Collectors.java)

**Required change:** Keep public methods’ observed ownership and mutation behavior, using a private/package-local zero-copy path where appropriate, or explicitly classify the public behavior change. Specify the visibility of the single-type overload: a public overload also needs a source-compatibility check for calls such as `getValuesByType(null)`. Add mutation, null-array, empty and overload-resolution characterization to task 5.1.

**Checked:** Public methods, stored-list construction and JDK implementation. **Not checked:** external callers or compilation of a proposed overload.

### MEDIUM — Cycle D does not define eligibility for lazy fixed-arity extensions

[D8](openspec/changes/optimize-pdp-evaluation/design.md:113) retains generic handling for optional and variadic functions, but does not explicitly exclude fixed-arity functions requiring lazy arguments. The public [FunctionSpec SPI](xacml-core/src/main/java/org/xacml4j/v30/policy/FunctionSpec.java:100) exposes that property, and [FunctionSpecBuilder](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java:360) branches between lazy and eager evaluation.

**Required change:** Define a fast-path eligibility rule covering lazy evaluation, custom providers, runtime validation and exception/status propagation. Add a fixed-arity lazy custom function whose second operand throws but must remain unevaluated, plus status-preserving failure tests. Keep the current generic SPI fallback.

**Checked:** Function SPI, normalization and evaluation branch. **Not checked:** a proposed fast-path implementation.

### MEDIUM — Cycle E does not specify policy-result ordering or same-context state safety

[D9](openspec/changes/optimize-pdp-evaluation/design.md:119) removes the skip list and lazily creates mutable context structures. The current [root context](xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java:107) uses concurrent maps, a sorted policy map and an atomic TTL; its [public getter](xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java:363) exposes the resulting policy sequence. “Keep a concurrent type if shared” does not establish which structures or initialization paths are shared.

**Required change:** Specify policy-ID order, duplicate-ID handling, advice/obligation merge behavior, TTL updates and safe lazy publication. Extend task 8.1 with `ReturnPolicyIdList`, nested response staging, concurrent first writes and reentrant callbacks, in addition to concurrent *separate* decisions.

**Checked:** D9, root fields and getters. **Not checked:** the intended replacement or a supported same-context threading contract.

### MEDIUM — The planned JMH pairs do not yet isolate the changes

[D12](openspec/changes/optimize-pdp-evaluation/design.md:143) has useful fork and allocation requirements, but its synthetic-1,000 pair covers only first-deny and unknown, while profile 004 analyzed `request=all`. The retained baseline used Log4j; profile 004 used Logback ([baseline summary](openspec/changes/add-pdp-jmh-benchmarks/evidence/benchmarks/jmh/002-baseline-jdk25/summary.md:3), [profile summary](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/summary.md:1)). A pair containing all of D9’s edits measures the group, not the individual causes.

The shared tree already contains cycle-A code and tests ([handler](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java:64), [test](xacml-pdp/src/test/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandlerTest.java:853)), while tasks [4.1–4.2](openspec/changes/optimize-pdp-evaluation/tasks.md:22) remain unchecked. The profile-004 tracked-change inventory does not list that handler ([env.txt](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/env.txt:4)).

**Required change:** Record the pre-A test and source evidence, then build each pair from fingerprinted snapshots differing only in the intended cycle, with identical backend, dependencies, JDK and flags. Measure synthetic-1,000 `all` and the individual positions, plus low-reuse/PIP cases. For [task 1.4](openspec/changes/optimize-pdp-evaluation/tasks.md:10), run before overlapping later edits or reverse only group 1’s changes in the same snapshot. Attribute bundled edits only to the group unless separately measured. For D11, report both latency and allocation, including regressions, before recommending the flag.

**Checked:** Plan, recorded baseline/profile metadata and current source diff. **Not checked:** whether a prior red test or pre-A baseline has been recorded elsewhere; no benchmark ran.

### MEDIUM — The research handoff contains unresolved scope and evidence discrepancies

The [synthesis](openspec/changes/optimize-pdp-evaluation/research/synthesis.md:43) includes selector memoization in cycle A, but D5 defers it and no task names its characterization or measurement. The reference-cache comparison is included in F; prelinked/MethodHandle invocation remains a research item. PF4’s staged-response allocation hypothesis and the newly arrived PF3 forwarding hypothesis also need explicit disposition after reprofiling, rather than being lost in a broad F pair. [PF4](openspec/changes/optimize-pdp-evaluation/research/pf4-astra-collections.md:123), [PF3](openspec/changes/optimize-pdp-evaluation/research/pf3-sol-cpu.md:68)

PF6 also needs correction before it is counted as corroborating measurement: it labels 480 µs as synthetic-1,000 `all`, while the cited baseline identifies 480 µs as **last-permit**; it cites 85,484 allocation samples where the profile table says 79,945. [PF6](openspec/changes/optimize-pdp-evaluation/research/pf6-agy-engines.md:12), [baseline](openspec/changes/add-pdp-jmh-benchmarks/evidence/benchmarks/jmh/002-baseline-jdk25/summary.md:24), [profile](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/alloc-synthetic_1000-all/top-alloc-synthetic_1000-all.md:1). The other lanes independently support the main hypotheses.

**Required change:** Correct or qualify PF6’s unsupported figures; reconcile selector scope in synthesis, D5 and tasks; put the remaining unrefuted hypotheses on an explicit post-cycle profiling/research backlog. Update [research/README.md](openspec/changes/optimize-pdp-evaluation/research/README.md:1), which still says research is not required and lists no lanes, despite task 2.2 requiring that index.

**Checked:** All five requested lane reports, the newly arrived PF3, synthesis and the cited profile rows. **Not checked:** external-engine performance or the eventual post-cycle profiles.

The synthesis changed during this review when PF3 arrived; I reread the revised file. The last observed SHA-256 prefixes were synthesis `c9b2f5fd2365`, design `adca31f0a565`, tasks `2b5cd4acb1e5`, and attribute spec `43de118c0d04`. They identify the material reviewed, not a frozen workspace.

Verification was read-only: source and artifact reads, `rtk rg`, `rtk git diff`, hashes, and primary documentation. No files were written; no build, test, benchmark, profiler, Git write or beads write ran. Runtime behavior, strict OpenSpec validation and all candidate gains remain **UNVERIFIED**.
