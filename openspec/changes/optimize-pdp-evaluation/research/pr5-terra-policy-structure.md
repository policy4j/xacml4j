<!-- Lane PR5 of the second profiling swarm (after cycle H), Codex GPT-6.1 Terra, read-only; focus: per-policy evaluation structure; report from the NTM pane, paths made repository-relative. -->

The largest opportunity is to avoid evaluating provably non-matching policies, but that needs a design for observable resolver behavior and repository changes. The most direct implementation candidates are eliminating the target-list iterator and shortening attribute-resolution forwarding while preserving the public context chain.

Estimates below are **opportunity budgets, not measured gains**. I applied run 021 profile shares to the run 020 JMH results: synthetic:1000/all **100.4 µs and 135,900 B/decide**, first-deny **1.084 µs and 4,078 B**, and rsa2008/all **8.93 µs and 18,770 B**. The profile has one fork; run 020 has three. Allocation-sample weighting has not been established, so the byte calculations are approximate.

`all` rotates through seven requests; it is a mixed workload, not a full scan on every invocation. The generator and cursor are at [SyntheticCorpus.java](xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/SyntheticCorpus.java:71) and [PdpDecideBenchmark.java](xacml-jmh/src/main/java/org/xacml4j/jmh/pdp/PdpDecideBenchmark.java:145).

Profile references below use:

- **S-A / S-C:** run 021 synthetic:1000/all [allocation](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/021-profile-decide-cycle-H/alloc-synthetic_1000-all/top-alloc-synthetic_1000-all.md:77) and [CPU](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/021-profile-decide-cycle-H/cpu-synthetic_1000-all/top-cpu-synthetic_1000-all.md:77).
- **R-A:** run 021 rsa2008/all [allocation](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/021-profile-decide-cycle-H/alloc-rsa2008-all/top-alloc-rsa2008-all.md:77).
- **F-A / F-C:** run 021 synthetic first-deny [allocation](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/021-profile-decide-cycle-H/alloc-synthetic_1000-first-deny/top-alloc-synthetic_1000-first-deny.md:83) and [CPU](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/021-profile-decide-cycle-H/cpu-synthetic_1000-first-deny/top-cpu-synthetic_1000-first-deny.md:77).

XACML section references come from the retained project research and review. I did not fetch the standard in this lane.

**H1. Index a restricted class of policy targets**

- **Evidence:** Synthetic policies have one resource string-equality target and are referenced in order under deny-overrides ([SyntheticCorpus.java:111](xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/SyntheticCorpus.java:111)). In the run 021 synthetic collapsed CPU stacks, `Target.match` is inclusive in **7,360/10,124 = 72.7%** of samples. S-A:77–81 attributes **18.6%** to policy contexts, **14.2%** to reference contexts, and **13.8%** to the target iterator.
- **Change:** Research an index for an explicitly eligible built-in policy shape, with candidates and fallback children visited in their original order. Restrict it to cases where a skipped child is provably non-matching and reference resolution, attribute retrieval, function dispatch, and their observable effects remain equivalent. Rebuild or invalidate the index with repository changes; the current reference cache has repository-listener invalidation ([DefaultPolicyReferenceResolver.java:50](xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/DefaultPolicyReferenceResolver.java:50)).
- **Expected gain:** The three disjoint sampled allocation sites represent `135,900 × (18.6% + 14.2% + 13.8%) ≈ 63,300 B/decide` of gross mixed-workload opportunity. The inclusive target stack represents `100.4 × 72.7% ≈ 73 µs/decide` of sampled time, **not a removable-time estimate**: successful targets, required lookups, index work, and other work remain. The index overlaps H2–H5.
- **Risk:** High. `MustBePresent=false` alone does not prove No-match: resolution or a function can fail. Preserve XACML §§7.3.5, 7.6–7.7 and §7.18, as well as the engine’s currently characterized target-error behavior. Preserve status, resolved attributes, PIP and resolver calls, policy IDs, obligations, and advice. If skipped evaluations change an observable extension contract, mark that behavior **BREAKING** or retain the fallback. Shared indexes need safe publication and repository-version handling. Test indexed versus ordinary evaluation with missing and multi-valued attributes, resolver failures, custom providers, duplicate or changed references, ordered algorithms, and concurrent decisions; compare complete responses and callback traces.
- **Checked / not checked:** Generator, evaluation loop, profile stacks, and reference-cache invalidation checked. Eligibility, index design, callback equivalence, and gain are unverified.

**H2. Reuse the first value of a singleton bag during repeated target matches**

- **Evidence:** S-C:77 attributes **1,469/10,124 = 14.5%** of CPU samples to `Match.match → ImmutableMultiset$1.next → AbstractIndexedListIterator.next → ImmutableList$1.get`. The generator supplies one resource value; [Match.java:120](xacml-core/src/main/java/org/xacml4j/v30/policy/Match.java:120) traverses the bag for every policy. F-C:77 has **441/10,151 = 4.3%** for the same project-attributed path.
- **Change:** For a bag with total size one, have `Match.match` use the existing `BagOfValues.value()` path, and investigate a private, transient singleton-value cache behind that method. Keep the Guava multiset as the serialized representation and retain the existing generic iteration for empty and multi-valued bags. This can be studied independently of Cycle C’s representation decision.
- **Expected gain:** The sampled CPU budgets are `100.4 × 14.5% ≈ 14.6 µs` for S/all and `1.084 × 4.3% ≈ 0.047 µs` for first-deny. The first lookup, cache access, and predicate call remain, so neither is a predicted saving. No allocation saving is established; a cache field could add bytes through object alignment.
- **Risk:** Preserve predicate invocation, status and first-error behavior under XACML §7.6, including duplicate values and order. `BagOfValues` has an explicit serial UID ([BagOfValues.java:63](xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java:63)); a transient cache must leave serialized fields and cross-version reads unchanged. A shared bag needs safe cache publication. Test empty, singleton, duplicate and multi-value bags; failing and successful predicates; concurrent access; and old/new serialization round trips.
- **Checked / not checked:** The sampled stack, singleton corpus shape, existing `value()` method, and serialized multiset field were checked. Cache layout, serialization compatibility, and the JIT effect were not tested.

**H3. Lazily materialize the reference-context node**

- **Evidence:** S-A:77 and :79 attribute **18.6%** of samples to `PolicyDescendantEvaluationContext` and **14.2%** to `PolicyIDReferenceEvaluationContext`. The two are constructed in [PolicyIDReference.java:74](xacml-core/src/main/java/org/xacml4j/v30/policy/PolicyIDReference.java:74) and [Policy.java:160](xacml-core/src/main/java/org/xacml4j/v30/policy/Policy.java:160). Before Cycle H, run 016 attributed **10.4% and 7.6%** respectively; the larger run 021 shares partly reflect the smaller allocation denominator.
- **Change:** Investigate an internal fast path that performs the existing reference validity and cycle checks at the same point, keeps the current reference visible from the policy context, and materializes the reference parent on first ancestry or state observation. It must preserve a stable parent object, reference-local status, and the normal path for overridden or custom policy behavior. Do not pool mutable contexts across evaluations.
- **Expected gain:** The gross reference-node budget is `135,900 × 14.2% ≈ 19,300 B/decide`; extra fields, materialized nodes, and synchronization reduce that amount. The combined context classes account for about `135,900 × 32.8% ≈ 44,600 B`, but that is **not** the saving from merging two objects: a replacement context still has a cost. CPU savings are not established.
- **Risk:** `getParentContext()` is public; cycle detection and response staging walk the chain ([EvaluationContext.java:203](xacml-core/src/main/java/org/xacml4j/v30/EvaluationContext.java:203), [PolicyIDReference.java:149](xacml-core/src/main/java/org/xacml4j/v30/policy/PolicyIDReference.java:149), [BaseCompositeDecisionRule.java:202](xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java:202)). Preserve §7.18 staging, variable scope, extended Indeterminate, and retained contexts. Safe publication matters if one context is accessed concurrently. A design that removes or changes the visible parent topology is **BREAKING**, as D10 states. Test parent identity and status before and after evaluation, cycle detection, nested references, custom extensions, failed obligations, and policy-ID reporting.
- **Checked / not checked:** Construction, public ancestry, cycle walk, and staging walk checked. No lazy-node design or external consumer compatibility was verified.

**H4. Use indexed traversal for immutable target children**

- **Evidence:** S-A:81 attributes **3,453/25,066 = 13.8%** of allocation samples to `Target.match → Iterators$9`; the collapsed stack identifies Guava’s singleton-list iterator. [Target.java:41](xacml-core/src/main/java/org/xacml4j/v30/policy/Target.java:41) stores the builder’s immutable list as a `Collection` and iterates it at line 72. Similar lists exist in `MatchAnyOf` and `MatchAllOf`.
- **Change:** Retain the public collection getters and encounter order, but use indexed access over the known immutable lists in target evaluation. Measure the nested loops separately; their iterators were not attributed as allocations in S/all.
- **Expected gain:** The target iterator budget is `135,900 × 13.8% ≈ 18,800 B/decide`. S-C and its collapsed stacks show **88** `Target.match` self samples and **34** iterator-`next` samples: `100.4 × 122/10,124 ≈ 1.2 µs` of touched sampled code, not a speedup estimate. Allocation and GC effects may change that result.
- **Risk:** Preserve iteration order, short-circuit points, exceptions, and status. XACML §7.7 and the project’s existing characterization make target error order significant: [TargetTest.java:180](xacml-core/src/test/java/org/xacml4j/v30/policy/TargetTest.java:180) expects evaluation to stop at the first Indeterminate. Keep the current behavior in this optimization. Immutable policy lists can be shared; per-decision contexts must remain separate. Test empty, singleton, and multi-child targets, both error orders, and unchanged callback order.
- **Checked / not checked:** Field construction, loop, collapsed allocation stack, and existing order test checked. Candidate code and JMH effects are unverified.

**H5. Shorten final attribute-resolution forwarding**

- **Evidence:** S-C:79 reports **425/10,124 = 4.2%** CPU self samples in `DescendantEvaluationContext.resolve`; collapsed stacks attribute **398/10,124 = 3.93%** to the attribute-designator path. The attribute-reference overload is `final` and forwards to its parent at [DescendantEvaluationContext.java:223](xacml-core/src/main/java/org/xacml4j/v30/policy/DescendantEvaluationContext.java:223).
- **Change:** Inside that final overload, traverse consecutive `DescendantEvaluationContext` parents directly and dispatch at the first non-descendant context. Keep the public parent links intact and stop at custom context implementations. Do not apply the same shortcut blindly to overridable methods such as runtime-validation accessors or policy-reference resolution.
- **Expected gain:** `100.4 × 398/10,124 ≈ 3.95 µs/decide` is the sampled forwarding-self budget; a loop and final dispatch still cost time. This change has no identified allocation site to remove, so expected allocation is **0 B saved** unless its implementation adds storage.
- **Risk:** Preserve the same final receiver, resolution order, exceptions, and status effects, including custom contexts and XACML §7.3.5 retrieval behavior. Keep existing parent objects and mutable validation settings; no shared cache is needed. Test nested policy and reference contexts, custom non-descendant parents, missing attributes, resolver failures, staging, and concurrent separate decisions.
- **Checked / not checked:** The final attribute overload, parent field, and collapsed stack were checked. Inlining, receiver distribution, and loop performance were not.

**H6. Allocate staged response lists only when needed**

- **Evidence:** R-A:77 attributes **1,837/38,059 = 4.8%** of allocation samples to the four `ArrayList`s constructed by `StagedResponses`; F-A:83 attributes **2.5%**. The lists are created at [BaseCompositeDecisionRule.java:265](xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java:265).
- **Change:** Lazily create each private effect-specific list on the first staged element. Preserve the staging wrapper and materialize the selected mutable lists before `publish`, including empty lists, so a parent that retains the iterable sees the same object and later mutations.
- **Expected gain:** The gross four-list budgets are `18,770 × 4.8% ≈ 901 B/rsa decide` and `4,078 × 2.5% ≈ 102 B/first-deny decide`. The selected lists and populated lists may still be needed; the actual saving is smaller. RSA constructor self time is about `8.93 × 0.22% ≈ 0.020 µs`, and the constructor remains.
- **Risk:** XACML §7.18 requires response staging by decision. Preserve order, duplicate and merge behavior, publication timing, empty callbacks, failure handling, and retained iterable identity. Avoid introducing races beyond the existing context contract. Run [NestedResponseStagingTest.java:129](xacml-core/src/test/java/org/xacml4j/v30/policy/NestedResponseStagingTest.java:129), its nested-reference and exactly-once cases, plus tests for empty retained lists and concurrent access where supported.
- **Checked / not checked:** List construction, add and publish paths, and existing tests checked. The fraction of lists that remain empty and compatibility with retained extension references are unverified.

**H7. Avoid the combining-list iterator for known random-access lists**

- **Evidence:** S-A:86 attributes **18/25,066 = 0.07%** of allocation samples to `DenyOverrides.doCombine → Iterators$ArrayItr`; F-A:102 attributes **1,073/66,379 = 1.62%**. The synthetic CPU collapsed stacks have **100/10,124 = 0.99%** in iterator `hasNext`, `get`, and `next` beneath the combining loop. [DenyOverrides.java:73](xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DenyOverrides.java:73) already stops on Deny.
- **Change:** Compare indexed traversal for known engine-owned immutable random-access lists, with the existing iterator path for arbitrary `List` implementations. Consider legacy combining loops separately where their profiles justify it.
- **Expected gain:** S/all allocation proxy: `135,900 × 18/25,066 ≈ 98 B/decide`; first-deny: `4,078 × 1,073/66,379 ≈ 66 B/decide`. The S/all iterator CPU budget is `100.4 × 0.99% ≈ 0.99 µs`; loop work remains. Sampling uncertainty is material at these sizes.
- **Risk:** Keep document order, custom-list behavior, and all decision-table outcomes. Under XACML Annex C.2, Permit cannot terminate deny-overrides because a later Deny wins. No mutable state should be shared. Test every decision-table combination, first and last Deny, a custom list, and complete response contents.
- **Checked / not checked:** Current loop, short-circuit, and profile stacks checked. The engine-owned-list guard and performance have not been tested.

**Refuted or deferred ideas**

- Removing both context nodes outright would change public ancestry; fusing them without an equivalent visible chain is **BREAKING**. H3 is a compatibility-preserving research proposal, not proof that this can be done.
- Stopping deny-overrides at Permit or reordering policies is unsafe. The current loop already stops at Deny.
- Replacing target-child iteration does not directly remove the **14.5%** `ImmutableList$1.get` CPU frame; the collapsed stack places it under `Match.match`’s **bag** iteration.
- `MustBePresent=false` is insufficient by itself to prove that a target cannot be Indeterminate.
- The no-variable policy map is already shared under D10. Cycle C’s replacement of the serialized multiset remains deferred pending the maintainer’s decision.

**Open questions**

- Which extensions retain or inspect evaluation contexts, resolution callbacks, mutable empty response lists, and policy IDs? These determine whether H1 and H3 can preserve behavior.
- Can an index follow the repository’s actual version and invalidation semantics without changing reference-resolution timing or PIP observations?
- Are allocation-profile shares byte-weighted for these recordings, and how do candidate object layouts and JIT decisions affect net bytes?
- Every candidate needs its own same-environment JMH before/after pair and semantic tests. Per the proposal, a round gaining less than 5% triggers another research swarm.

This lane read source and retained evidence only. It wrote no files and ran no tests, Maven builds, benchmarks, or profilers.
