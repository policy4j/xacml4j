<!-- Lane PF4 of the first profiling swarm (task 2.1): Codex GPT-6 Astra, read-only; report taken from the Codex session record, paths made repository-relative. -->

FINAL REPORT (PF4 JMH benchmark engineer, Codex GPT-6 Astra)

The largest collection opportunity is avoiding repeated construction of request attribute bags. The skip-list is a smaller target: its insertion path does invoke `ThreadLocalRandom`, but it records applicable policies rather than every scanned target. Twelve hypotheses follow, ranked by addressable work.

No files changed. No Maven, tests, benchmarks, profilers, or Git/beads writes ran. All proposed gains are **UNVERIFIED**.

Evidence notation:

- `C/` = `xacml-core/src/main/java/org/xacml4j/v30/`
- `P/` = `xacml-pdp/src/main/java/org/xacml4j/v30/`
- `J/` = `xacml-jmh/src/main/java/org/xacml4j/jmh/`
- `B/` = `openspec/changes/add-pdp-jmh-benchmarks/evidence/benchmarks/jmh/002-baseline-jdk25/`
- `E/` = `openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/`
- R = `rsa2008-all`; S = `synthetic_1000-all`; F = `synthetic_1000-first-deny`.
- `CPU(S)` means `E/cpu-synthetic_1000-all/collapsed-cpu.csv.gz`; `ALLOC(S)` means the corresponding allocation file. R/F follow the same convention. Collapsed-file line numbers mean decompressed lines.

Recorded results used for sizing:

| Workload | Time, µs/decide | Allocation, B/decide |
|---|---:|---:|
| R | 18.969 ± 0.428 | 45,115 ± 838 |
| F | 6.503 ± 0.205 | 15,816 ± 294 |
| synthetic:1000, unknown-notapplicable | 479.056 ± 15.955 | 2,127,619 ± 11,403 |

Source: `B/results-default.json`, `B/results-scaling.json`; rendered in `B/tables.md:3` and `B/tables.md:32`.

For S arithmetic, the equal-weight mean of the seven separately measured requests is **383.012 µs and 1,675,683 B**. This is a derived sizing scale, not a measured mixed-workload score or confidence interval. The seven generated requests are established by `J/corpus/SyntheticCorpus.java:76`.

Recorded environment: Apple M2 Pro; Oracle GraalVM `25.0.4+7-LTS-jvmci-b01`, C2, `-Xms2g -Xmx2g -XX:+UseG1GC`; one thread. Baseline: three forks, five warmup and five measurement iterations, `-prof gc`. Profiles: one fork, CPU interval 1 ms. Sources: `B/summary.md:3`, `E/env.txt:123`, `E/env.txt:138`, `E/command.txt:2`.

**Estimation limits:** baseline 002 used Log4j; profile 004 used Logback (`B/summary.md:6`, `E/summary.md:4`). Multiplying profile shares by baseline scores therefore gives an approximate *addressable cost*, not a measured saving. Allocation percentages are collapsed sample-weight shares; converting them to bytes is also approximate. No recording establishes the replacement cost or recovery fraction. Inclusive stack totals count each matching stack once. **Do not add overlapping hypotheses, especially H1–H3.**

**H1. Memoize successful request-supplied designator bags within each individual decision**

- **Evidence:** S stacks under `doDesignatorResolve` containing `Entity.resolve` or `RequestContext.getEntity` account for **5,711/10,091 CPU samples = 56.59%**, and **60,014/79,945 allocation weight = 75.07%**. Representative stacks: `CPU(S):387`, `ALLOC(S):22`. Request values are rebuilt at `C/types/Entity.java:207`; only PIP results populate the existing cache at `P/pdp/DefaultEvaluationContextHandler.java:169`.
- **Change:** Add a separate, lazily allocated request-value memo in `DefaultEvaluationContextHandler`, keyed by the complete designator key. Start with a JDK map or a small array-backed map. Cache successful request lookups; retain existing PIP resolution behavior.
- **Expected gain:** UNVERIFIED. S addressable cost: `383.012 × 56.59% ≈ 216.8 µs`; `1,675,683 × 75.07% ≈ 1,257,920 B/decide`. First lookups and memo overhead remain. R’s corresponding budget is approximately **5.66 µs/19,151 B**, but its reuse rate was not measured.
- **Risk:** Keep this memo separate from `getResolvedDesignators()`: those entries become returned resolved attributes (`P/pdp/DefaultPolicyDecisionPoint.java:230`). Preserve issuer/type/category matching and MustBePresent behavior, XACML 3.0 §7.3.5. Keep state per individual decision, including multi-request expansion. No public signature change. Test repeated equivalent keys, differing issuers/types/categories, PIP call counts, returned attributes, and concurrent decisions.
- **Checked / not checked:** Checked cache insertion, response consumption, and per-request construction (`P/pdp/PolicyDecisionPointContextFactory.java:176`). Cache cardinalities, hit rates, and net gains remain unmeasured.

**H2. Fuse attribute filtering and bag construction to remove intermediate collections**

- **Evidence:** `AttributeIndex.findValues` accounts inclusively for **33.53% CPU/41.44% allocation** in S: **3,383/10,091** and **33,129/79,945**. It creates an issuer-filtered list, per-attribute typed lists, and an immutable aggregate before `Entity.resolve` builds a bag. Sources: `C/AttributeIndex.java:105`, `C/AttributeIndex.java:163`, `C/Attribute.java:114`, `C/types/Entity.java:207`; allocation stacks include `ALLOC(S):71`.
- **Change:** Add an internal loop that filters issuer and type directly into the destination bag builder. Preserve existing public collection-returning methods and their ownership behavior.
- **Expected gain:** UNVERIFIED. S budget: `383.012 × 33.53% ≈ 128.4 µs`; `1,675,683 × 41.44% ≈ 694,403 B/decide`. R: approximately **3.11 µs/9,890 B**. Filtering and final bag storage remain.
- **Risk:** Preserve duplicates, absent-issuer wildcard behavior, type filtering, and missing-value behavior (§7.3.5; bag functions A.3.10). No signature change. Test multiple attributes sharing an ID, mixed issuers/types, duplicate values, and builder reuse without mutation of earlier results.
- **Checked / not checked:** Checked the complete intermediate-copy chain. Did not establish that all public callers accept immutable views; replacing their returned collections directly would require a separate compatibility review.

**H3. Give small bags a compact representation and direct iterator**

- **Evidence:** Bag construction plus multiset traversal, excluding H5’s empty fallback, accounts for approximately **28.76% CPU/28.77% allocation** in S. `Match → ImmutableList$1.get` alone is **10.7%** (`E/cpu-synthetic_1000-all/top-cpu-synthetic_1000-all.md:77`). `CPU(S):631` confirms this is the multiset iterator. Storage/build sites: `C/BagOfValues.java:64`, `:74`, `:280`.
- **Change:** Prototype empty/singleton/small-array bag storage with a direct immutable iterator. Retain multiplicities and order-independent bag equality. Compare larger-cardinality representations separately, including an Eclipse Collections **13.0.0** variant.
- **Expected gain:** UNVERIFIED. S budget: `383.012 × 28.76% ≈ 110.1 µs`; `1,675,683 × 28.77% ≈ 482,153 B/decide`. R: approximately **2.78 µs/9,064 B**. Required bag/value storage remains.
- **Risk:** Preserve A.3.10/A.3.11 behavior and the distinction between Java equality and XACML double equality. Characterize duplicates, NaN, signed zero, union/intersection, and immutable snapshots. Changing observable collection equality or serialized compatibility is **BREAKING** unless compatibility is retained.
- **Checked / not checked:** Confirmed the iterator attribution and existing multiset equality implementation. Read `DoubleBagSetEqualityTest.java:60`. No representation comparison, serialization audit, or Eclipse Collections measurement ran.

**H4. Replace the unbounded Guava policy-reference caches with simpler concurrent maps**

- **Evidence:** `DefaultPolicyReferenceResolver.resolve` accounts for **368/10,091 = 3.65% CPU** in S; `LocalCache$Segment.getEntry` alone is 2.0% (`E/cpu-synthetic_1000-all/top-cpu-synthetic_1000-all.md:82`). Construction specifies initial capacity, with no eviction or expiry configuration in the opened constructor (`P/spi/repository/DefaultPolicyReferenceResolver.java:68`).
- **Change:** Prototype JDK `ConcurrentHashMap` storage in that resolver. Preserve reference keys, repository lookups, and add/remove invalidation. These caches belong to the PDP factory, unlike H1’s request memo (`P/pdp/PolicyDecisionPointContextFactory.java:102`).
- **Expected gain:** UNVERIFIED. S CPU budget: `383.012 × 3.65% ≈ 13.97 µs/decide`. No positive B/decide saving is supported: the allocation recordings attributed no events to this resolver. Retained heap and cold-cache costs are separate questions.
- **Risk:** Preserve version-range resolution and invalidation under concurrent repository updates. Do not replace shared caches with ordinary `HashMap`. No signature change. Test warm hits, misses, replacement/removal, wildcard versions, and concurrent resolve/update.
- **Checked / not checked:** Checked constructor, lookup and invalidation paths, plus existing `ReferenceCacheCharacterizationTest`. Concurrent update behavior and replacement performance remain unverified.

**H5. Stop eagerly constructing the unused empty fallback bag**

- **Evidence:** `C/policy/AttributeReference.java:116` uses `orElse(getDataType().emptyBag())`; `C/types/ValueType.java:125` builds a new bag. S stacks containing `ValueType.emptyBag` contribute **182/10,091 = 1.80% CPU** and **5,072/79,945 = 6.34% allocation**. `ALLOC(S):140` contains 2,448 samples ending in `LinkedHashMap`.
- **Change:** Evaluate the fallback only when the optional is empty. Consider canonical typed empty bags afterward, with separate compatibility checks.
- **Expected gain:** UNVERIFIED. S addressable cost: `383.012 × 1.80% ≈ 6.91 µs`; `1,675,683 × 6.34% ≈ 106,312 B/decide`. F: approximately **0.102 µs/530 B**. Synthetic requests contain the resource/action attributes, making eager successful-lookup fallbacks the relevant case (`J/corpus/SyntheticCorpus.java:156`).
- **Risk:** Preserve missing versus failed resolution and MustBePresent (§7.3.5). No signature change. Test present, missing, resolver-failure and selector-failure cases with both MustBePresent values.
- **Checked / not checked:** Confirmed eager evaluation and the allocation stacks. Did not measure the replacement or canonical-empty compatibility.

**H6. Replace request-time normalization linked lists with compact lists**

- **Evidence:** `normalize*` accounts for **1.58% CPU/2.20% allocation** in S and **4.35% CPU/6.57% allocation** in R. The S allocation includes **1,759 LinkedList-node samples** (`E/alloc-synthetic_1000-all/top-alloc-synthetic_1000-all.md:92`). Allocation occurs at `C/policy/function/FunctionSpecBuilder.java:507`.
- **Change:** First replace the normalized argument `LinkedList` with a suitably sized `ArrayList`. Consider an internal fixed-arity representation only after that pair.
- **Expected gain:** UNVERIFIED. S budget: **6.04 µs/36,865 B**; R: `18.969 × 4.35% ≈ 0.826 µs`, `45,115 × 6.57% ≈ 2,964 B`. Replacement arrays still consume space.
- **Risk:** Preserve optional null slots, variadic handling, argument order, lazy evaluation and validation. XACML §7.4/§7.19.2 requirements are retained in `modernize-core-for-jdk28-valhalla/evidence/tests/083-apply-argument-check/oasis-xacml30-errata01-7.4-7.19.2.txt:8`. No signature change. Test optional/variadic and invalid-type cases with runtime validation enabled and disabled.
- **Checked / not checked:** Checked normalization and evaluation. No fixed-arity bypass was validated.

**H7. Allocate policy-local variable maps only on first cached result**

- **Evidence:** Every `PolicyDescendantEvaluationContext` constructs a `HashMap` (`C/policy/Policy.java:252`). This is **1,820/79,945 = 2.28% allocation** in S (`E/alloc-synthetic_1000-all/top-alloc-synthetic_1000-all.md:89`), despite the generated policies containing targets and rules without variables (`J/corpus/SyntheticCorpus.java:122`).
- **Change:** Keep the map absent until `setVariableEvaluationResult`; return the existing cache-miss result while absent. Start with lazy allocation, without introducing indexed variable slots.
- **Expected gain:** UNVERIFIED. S map-allocation budget: `1,675,683 × 2.28% ≈ 38,148 B/decide`. Constructor CPU attribution is **37/10,091**, approximately **1.40 µs** on the S scale; map-specific timing is not separately resolved.
- **Risk:** Preserve policy-local scope and evaluate-once behavior (`C/policy/VariableDefinition.java:115`). No signature change. Test repeated references, identical variable names in different policies, failed evaluation, and concurrent requests.
- **Checked / not checked:** Checked allocation, getter/setter and variable evaluation. No concurrency or allocation test ran.

**H8. Precompute whether an immutable request contains repeating categories**

- **Evidence:** `RequestContext.containsRepeatingCategories` scans categories and performs repeated lookups (`C/RequestContext.java:274`). Its inclusive F shares are **825/10,099 = 8.17% CPU**, **3,771/83,571 = 4.51% allocation**. The iterator allocation appears at `E/alloc-synthetic_1000-first-deny/top-alloc-synthetic_1000-first-deny.md:90`.
- **Change:** Compute a final boolean when the immutable category multimap is built. Keep the current method and request-expansion behavior.
- **Expected gain:** UNVERIFIED. F budget: `6.503 × 8.17% ≈ 0.531 µs`; `15,816 × 4.51% ≈ 714 B/decide`. R: approximately **0.640 µs/1,150 B**. Construction cost moves outside the pre-parsed-request benchmark and must also be assessed on request creation.
- **Risk:** Preserve repeated categories, not merely duplicate category objects. No signature change; immutable state can be shared. Test empty, singleton, distinct and repeated categories through the real request-handler chain, including expansion limits.
- **Checked / not checked:** Checked immutable construction and the current predicate. End-to-end parse-plus-decide impact remains unmeasured.

**H9. Replace skip-list policy bookkeeping with a deterministic small structure**

- **Evidence:** R `addEvaluationResult` accounts for **373/10,106 = 3.69% CPU**, **488/53,229 = 0.92% allocation**. String comparisons and `doPut` appear at `E/cpu-rsa2008-all/top-cpu-rsa2008-all.md:79` and `:86`. `CPU(R):2207` shows `ConcurrentSkipListMap.doPut → ThreadLocalRandom.nextSecondarySeed`. Allocation/insertion sites: `C/policy/RootEvaluationContext.java:115`, `:203`.
- **Change:** Compare a compact sorted ID/value structure for small cardinalities, with a deterministic larger fallback. Preserve ID replacement and sorted retrieval. A synchronized `TreeMap` is a simpler comparison candidate where existing concurrent access must remain supported.
- **Expected gain:** UNVERIFIED. R budget: `18.969 × 3.69% ≈ 0.700 µs`; `45,115 × 0.92% ≈ 414 B`. F: approximately **0.079 µs/198 B**. Random-number frames themselves are only **8/10,106 = 0.079% CPU** in R.
- **Risk:** Preserve fully applicable policy reporting, including effects different from the final decision: XACML §5.48, retained in `modernize-core-for-jdk28-valhalla/evidence/tests/029-bug-4.22-policy-id-list/summary.md:11`. Test ordering, duplicate IDs, all decision states, and ReturnPolicyIdList. Disabling collection solely because the response flag is false could change the public context getter’s behavior.
- **Checked / not checked:** Confirmed filtering and random calls. Did not establish thread confinement for third-party combiners or justify removing synchronization.

**H10. Add an empty fast path for resolved-attribute response construction**

- **Evidence:** F `getResolvedAttributes` accounts for **423/10,099 = 4.19% CPU**, **1,688/83,571 = 2.02% allocation**. Iterator `hasNext` alone is 3.1% (`E/cpu-synthetic_1000-first-deny/top-cpu-synthetic_1000-first-deny.md:78`). The method always constructs a `HashMultimap` and `LinkedList` (`P/pdp/DefaultPolicyDecisionPoint.java:230`).
- **Change:** Return an empty collection immediately when the resolved map is empty. For populated results, iterate entries once and compare a JDK map of appropriately deduplicated collections.
- **Expected gain:** UNVERIFIED. F method budget: `6.503 × 4.19% ≈ 0.272 µs`; `15,816 × 2.02% ≈ 319 B`. The initial map access/view and emptiness check remain.
- **Risk:** Preserve issuer, category, duplicate suppression and returned resolved attributes. No signature change. Test request-only attributes separately from PIP-added attributes and compare complete results. Preserve immutable result ownership (`C/Result.java:54`, `:286`).
- **Checked / not checked:** Checked producer, consumer and result copying. Non-empty result cardinalities and replacement performance remain unmeasured.

**H11. Lazily allocate optional root-context maps**

- **Evidence:** The root constructor creates five `ConcurrentHashMap` objects for advice, obligations and statuses (`C/policy/RootEvaluationContext.java:107`). These contribute **2,223/83,571 = 2.66% allocation** in F (`E/alloc-synthetic_1000-first-deny/top-alloc-synthetic_1000-first-deny.md:81`).
- **Change:** Allocate optional storage on first write, retaining safe initialization and current merge behavior. Start with the status map; expand to response maps only after reviewing returned collection-view behavior.
- **Expected gain:** UNVERIFIED. The five-map allocation budget is `15,816 × 2.66% ≈ 421 B/F decide`; eliminating only the status map captures a fraction. The entire root constructor accounts for approximately **0.025 µs** on F; map-only CPU cost is unresolved.
- **Risk:** Per-request construction does not prove that extension code never accesses one context concurrently. Preserve initialization safety, same-ID merging and obligation/advice propagation (§7.18). Changing live views or concurrent behavior can be **BREAKING**. Test empty/populated states, concurrent first writes where supported, and retained views.
- **Checked / not checked:** Checked construction, merging and getters. Third-party context usage was not audited.

**H12. Allocate staged obligation/advice lists only when populated**

- **Evidence:** `StagedResponses` creates four empty `ArrayList`s (`C/policy/BaseCompositeDecisionRule.java:267`). R attributes **1,269/53,229 = 2.38% allocation** to this constructor (`E/alloc-rsa2008-all/top-alloc-rsa2008-all.md:82`).
- **Change:** Keep staging and publication intact; initialize each private list when its first response arrives.
- **Expected gain:** UNVERIFIED. R allocation budget: `45,115 × 2.38% ≈ 1,076 B/decide`; F: approximately **122 B**. R constructor CPU attribution is only **12/10,106**, approximately **0.023 µs**. This is chiefly an allocation hypothesis.
- **Risk:** Staging is required to prevent responses from failed or differently decided children escaping (§7.18; `C/policy/BaseCompositeDecisionRule.java:141`). No public signature change. Preserve nested response tests, particularly failed obligations, different effects and exactly-once publication (`xacml-core/src/test/java/org/xacml4j/v30/policy/NestedResponseStagingTest.java:129`, `:251`).
- **Checked / not checked:** Checked staging, publication and existing test scenarios. The proportion of lists that remain empty is unmeasured.

**Refuted ideas and misleading priorities**

- **Treating the skip-list as shared PDP contention:** the factory constructs a new root context per individual decision (`P/pdp/PolicyDecisionPointContextFactory.java:185`). These one-thread recordings establish neither cross-request contention nor concurrent scaling.
- **Charging skip-list inserts to every non-matching target:** `addEvaluationResult` excludes NotApplicable and Indeterminate (`C/policy/RootEvaluationContext.java:201`). Attribute reconstruction is the stronger explanation for synthetic scan growth.
- **Attributing the prominent `Arrays$ArrayList` frame to function varargs:** `CPU(S):369` contains **183 samples** through `RegularImmutableMultiset.elementSet → Arrays.asList`, versus 186 in that reported deepest-project row. A varargs-only change misses nearly all of that particular attribution.
- **Replacing bags with sets:** this drops multiplicity; existing bag storage and functions require duplicate-sensitive behavior (`C/BagOfValues.java:47`, `:128`; A.3.10).
- **Optimizing the backing policy store to accelerate warm decide:** fixture checks warm reference caches, and the retained characterization test asserts subsequent decisions perform no store lookups (`xacml-jmh/src/test/java/org/xacml4j/jmh/harness/ReferenceCacheCharacterizationTest.java:38`). Store lookup benchmarks do not establish decide gains.
- **Pooling whole evaluation contexts using `clear()`:** the opened implementation clears evaluated policies and TTL but does not reset all response/status state (`C/policy/RootEvaluationContext.java:455`). Pooling requires a separate lifecycle design.
- **Blanket Guava replacement:** `RequestContext.getCategoriesByCategoryId()` publicly returns Guava `Multimap` (`C/RequestContext.java:163`). Changing that signature is **BREAKING**. Profile-supported internal substitutions should precede broad dependency removal.

These are source/profile refutations of mechanisms or priorities, not measured candidate regressions.

**Open questions and verification**

1. Measure positive memo hit rates and bag cardinalities on both corpora. H1 could regress requests with mostly unique keys; H3 needs empty, singleton, duplicate-heavy and larger-bag cases.
2. Establish the supported concurrency contract for evaluation contexts before replacing concurrent structures. Keep policy-reference caches per PDP and request-derived state per individual decision.
3. Compare JDK, custom and Eclipse Collections 13.0.0 candidates in separate pairs. No evidence reviewed here selects Eclipse Collections as the winner.
4. Reprofile after each accepted change. H1 consumes much of H2/H3’s opportunity, and H3’s accounting explicitly excludes H5.
5. Require fresh same-build Logback baseline/candidate JMH pairs, at least three forks and `-prof gc`, plus complete decision/status/response comparisons and conformance. Several small fixed-cost estimates are below the old baseline error bars.

Verification performed: scoped `rtk rg` and `rtk proxy sed` reads; `rtk proxy gzip -dc`; read-only in-memory Python aggregation of all six collapsed profiles and both baseline JSON files; `rtk git status --short` before and after. The status listings matched. Standard-section references came from source and retained evidence; no fresh standards fetch occurred.

Residual risk: sampling attribution, allocation-weight-to-byte conversion, mixed-workload derivation, backend differences, and unknown replacement costs prevent any net-speedup claim. This report supplies ranked experiments; implementation and performance acceptance remain unverified.
