<!-- Lane PF5 of the first profiling swarm (task 2.1): Codex GPT-6.1 Terra, read-only; report taken from the Codex session record, paths made repository-relative. -->

FINAL REPORT (PF5, Codex GPT-6.1 Terra)

The largest opportunity is to avoid evaluating known non-matching targets. A request-local cache for successfully resolved request attributes is the strongest narrower candidate. Two existing target-evaluation defects need characterization before either optimization is judged against “unchanged decisions.”

All gains below are **unverified hypotheses**. The recorded JDK 25 baseline used Log4j; profile 004 used Logback. The profile percentages and baseline therefore do not form a matched before/after pair. Allocation percentages are sampled proportions, so multiplying them by B/op is an illustrative scale, not a measured allocation saving.

| Recorded JDK 25 baseline, in-memory store | Time | Allocation |
| --- | ---: | ---: |
| RSA 2008, all requests | 18.97 ± 0.43 µs/op | 45,115 ± 840 B/op |
| Synthetic 1,000, first deny | 6.503 ± 0.200 µs/op | 15,816 ± 290 B/op |
| Synthetic 1,000, unknown | 479.1 ± 16 µs/op | 2,127,619 ± 11,000 B/op |

Source: [baseline tables](openspec/changes/add-pdp-jmh-benchmarks/evidence/benchmarks/jmh/002-baseline-jdk25/tables.md:3) and [baseline build details](openspec/changes/add-pdp-jmh-benchmarks/evidence/benchmarks/jmh/002-baseline-jdk25/summary.md:3).

For the synthetic `all` profile, the seven recorded individual request means average to approximately **383.0 µs and 1,675,683 B/op**. This is an arithmetic reference, not a separately measured `all` baseline. The harness cycles through the selected requests ([PdpDecideBenchmark.java](xacml-jmh/src/main/java/org/xacml4j/jmh/pdp/PdpDecideBenchmark.java:143)). Inclusive profile stacks overlap; the opportunities below must not be added together.

Evidence abbreviations:

- **CS / AS:** [synthetic-all CPU](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/cpu-synthetic_1000-all/top-cpu-synthetic_1000-all.md:73) / [allocation](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/alloc-synthetic_1000-all/top-alloc-synthetic_1000-all.md:73).
- **CF / AF:** [first-deny CPU](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/cpu-synthetic_1000-first-deny/top-cpu-synthetic_1000-first-deny.md:73) / [allocation](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/alloc-synthetic_1000-first-deny/top-alloc-synthetic_1000-first-deny.md:73).
- **CR / AR:** [RSA CPU](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/cpu-rsa2008-all/top-cpu-rsa2008-all.md:73) / [allocation](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/alloc-rsa2008-all/top-alloc-rsa2008-all.md:73).

**HIGH — Target returns before a later No-match can dominate an Indeterminate.** [Target.match](xacml-core/src/main/java/org/xacml4j/v30/policy/Target.java:69) stops at the first Indeterminate. [XACML 3.0 §7.7](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html) requires No-match if *any* `AnyOf` is No-match. Thus, an error in the first `AnyOf` followed by a non-matching second one returns Indeterminate instead of No-match; [composite evaluation](xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java:120) can consequently produce a different decision. The existing [test](xacml-core/src/test/java/org/xacml4j/v30/policy/TargetTest.java:179) explicitly expects the second expression not to run. Add both operand orders with Indeterminate and No-match, then correct the target logic and the conflicting test in a separately authorized semantics change. **Runtime reproduction: UNVERIFIED.**

**HIGH — Match returns on the first failed comparison, even if a later bag value matches.** [Match.match](xacml-core/src/main/java/org/xacml4j/v30/policy/Match.java:111) catches an `EvaluationException` outside the value loop and returns Indeterminate immediately. [XACML 3.0 §7.6](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html) gives a later true comparison precedence over an earlier indeterminate comparison. A two-value bag and an admissible predicate that throws for the first value and returns true for the second would exercise the difference. It can affect authorization: a deny rule’s Indeterminate is not Deny, and [permit-unless-deny](xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/PermitUnlessDeny.java:47) returns Permit without a Deny. Add that test and its reverse order; retain Indeterminate when no comparison is true. **Runtime reproduction and the affected deployed policy population: UNVERIFIED.**

### H1. Index eligible policy targets

- **Evidence:** Synthetic policies each have a string-equality resource target and are held under a deny-overrides root ([SyntheticCorpus.java](xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/SyntheticCorpus.java:111)). CS:77–85 shows work within each target. The unknown request costs 472.6 µs and 2,111,803 B more than first-deny in the recorded baseline.
- **Change:** Build an immutable candidate index for provably indexable target predicates around [PolicySet.combineDecisions](xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySet.java:177), retaining original child order and an evaluation fallback for every unindexable or uncertain target.
- **Expected gain:** If indexing removed 90% of that observed synthetic difference, the illustrative scale is **425 µs and 1.90 MB per decision**. This is not a bound or a measured result; first-deny also evaluates a matching rule, and index lookup has a cost. RSA gain is unknown.
- **Risk / test:** Missing values may be supplied by PIPs; errors, issuer, datatype, multiple values, selectors, references, obligations, advice, status, policy IDs, and ordered/first-applicable algorithms must remain observable. Invalidate safely on repository changes. Differential-test full responses, including missing and failing resolvers and policy updates, against the interpreter and an independent oracle.
- **Checked / not checked:** The corpus shape and linear combining loop were checked; indexability, update cost, concurrency, and resulting performance were not.

### H2. Compile a decision diagram or specialized policy plan

- **Evidence:** The same repeated scan supports this alternative to H1. Ngo, Demchenko and de Laat’s [2015 XACML decision-diagram paper](https://dare.uva.nl/id/e4c1d1d0-d3c5-4fb9-9048-e7e3c870ae77) identifies both the opportunity and the risk of losing Indeterminate and missing-attribute semantics.
- **Change:** Experiment with a compiled plan for a declared subset at policy-load time; interpret everything else. Keep the plan tied to the policy/reference version and invalidation lifecycle.
- **Expected gain:** If a plan removed half of H1’s observed full-scan difference, the scale would be **236 µs and 1.06 MB/op**. This overlaps H1 completely.
- **Risk / test:** Do not collapse decisions to a Boolean. The 2026 arXiv [Prezta policy compiler](https://arxiv.org/html/2607.11466v2) is useful prior work, but its documented subset omits selectors, policy references, obligations and advice; its binary-output design is not a replacement for this PDP. Differential-test all extended decisions, outputs and update races; measure compile time and retained memory.
- **Checked / not checked:** The papers and current policy hierarchy were inspected; no diagram, equivalence proof, or benchmark exists for this code.

### H3. Cache successful request-attribute lookups per decision

- **Evidence:** Summing weights of collapsed stacks containing `DefaultEvaluationContextHandler.doDesignatorResolve` gives 5,806/10,091 CPU samples and 60,014/79,945 allocation samples in synthetic-all. The handler caches PIP results but resolves request values again ([handler](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java:148), [Entity.resolve](xacml-core/src/main/java/org/xacml4j/v30/types/Entity.java:207)).
- **Change:** Cache only successful request-origin bags in a separate, decision-scoped structure. Keep the existing PIP-result map separate: the PDP uses it to build resolved attributes in the response ([DefaultPolicyDecisionPoint.java](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java:230)).
- **Expected gain:** The inclusive stacks scale to 57.5% × 383 ≈ **220 µs** and 75.1% × 1,675,683 ≈ **1.26 MB/op**. If half were avoided, the illustrative scale is **110 µs and 629 KB/op**. Some work remains necessary. RSA’s corresponding inclusive scales are about **6.0 µs and 19 KB/op**.
- **Risk / test:** Do not cache PIP failures or absence indiscriminately, cross multi-request contexts, or change response attributes. Test repeating categories, issuer and datatype variants, missing/failing PIPs, callbacks, full response equality, and concurrent decisions.
- **Checked / not checked:** The request-versus-PIP paths and response use were checked. Request immutability and cache safety across every custom callback remain unverified.

### H4. Specialize empty and singleton bags

- **Evidence:** CS:77 attributes 10.7% of CPU samples to `ImmutableList$1.get` beneath `Match`; the collapsed stack places it in `ImmutableMultiset` iteration. AS:77–79 attributes 12.8% of allocation samples to three bag-builder sites. [BagOfValues](xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java:63) stores a Guava multiset.
- **Change:** Measure a compact empty/singleton representation or a specialized iteration path in `BagOfValues` and `Match`, with a general multivalue fallback.
- **Expected gain:** The leaf CPU scale is 10.7% × 383 ≈ **41 µs/op**; the three sampled allocation sites scale to 12.8% × 1.676 MB ≈ **214 KB/op**. Neither amount is established as removable.
- **Risk / test:** Preserve duplicate counts, equality, hash, iteration and set operations. A changed serialized form is potentially **BREAKING**: the class is `Serializable`. Test empty, singleton, duplicates, multivalue bags, serialization compatibility, and match outcomes.
- **Checked / not checked:** The hot stack and public `values()` declaration were checked; external casts to `Multiset` and serialized-data compatibility were not.

### H5. Reduce reference-context and cache work

- **Evidence:** CS:81–82 has 2.0% self CPU each in descendant resolution and Guava reference-cache lookup. AS:89,98 and the adjacent context rows identify policy-context maps and objects. [PolicyIDReference](xacml-core/src/main/java/org/xacml4j/v30/policy/PolicyIDReference.java:74) resolves references and creates contexts; [Policy](xacml-core/src/main/java/org/xacml4j/v30/policy/Policy.java:250) eagerly creates a variable map.
- **Change:** Measure lazy variable maps and a smaller reference/context path for resolved references. Preserve the resolver’s [repository-listener invalidation](xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/DefaultPolicyReferenceResolver.java:140).
- **Expected gain:** The two CPU rows scale to about **15.3 µs/op**. The 2.3% map, 1.5% policy context and 1.2% reference context rows scale to about **84 KB/op**. These are opportunities, not established savings.
- **Risk / test:** Preserve policy scope, variables, reference versions and failures, advice staging, tracing, and concurrent invalidation. Test additions/removals and same-ID version changes during decisions.
- **Checked / not checked:** Context construction and listener code were checked; safe flattening and stale-reference behavior were not tested.

### H6. Fuse attribute lookup loops

- **Evidence:** [AttributeIndex.findByIssuer](xacml-core/src/main/java/org/xacml4j/v30/AttributeIndex.java:105) and [Attribute.getValuesByType](xacml-core/src/main/java/org/xacml4j/v30/Attribute.java:114) build stream pipelines and lists. CS:78,85 attributes 2.6% and 1.1% CPU to pipeline construction; AS:80–87 lists five pipeline allocations totaling 13.3%.
- **Change:** Fuse issuer and type filtering into direct loops, retaining the public collection behavior.
- **Expected gain:** Those CPU rows scale to **14.2 µs/op**; the five allocation rows scale to **223 KB/op**. This overlaps H3 and only the eliminated pipeline work is eligible.
- **Risk / test:** Preserve issuer-null wildcard behavior, type filtering, duplicates, order and returned-collection mutability. Test empty, multiple issuer/type, and duplicate cases.
- **Checked / not checked:** The loops and allocations were checked; the cost of the replacement and all callers’ mutability expectations were not.

### H7. Specialize fixed-arity function invocation

- **Evidence:** CS:83 attributes 1.8% CPU to `Arrays$ArrayList` beneath `Match`; `invoke` converts varargs to a list ([FunctionSpec.java](xacml-core/src/main/java/org/xacml4j/v30/policy/FunctionSpec.java:143)). AS:92 attributes 2.2% of allocation samples to normalization nodes; [FunctionSpecBuilder](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java:360) normalizes on invocation.
- **Change:** Prebind the validated fixed-arity path for eligible predicates; retain generic normalization and dispatch for extensions, optional and variadic functions.
- **Expected gain:** The array-list leaf plus the 1.58% inclusive normalization stack scale to about **12.9 µs/op**; the linked-node row scales to **37 KB/op**. This overlaps H1 and H2.
- **Risk / test:** Preserve argument evaluation order, validation mode, extension dispatch, and errors. Test fixed, optional and variadic functions, custom providers, and both sides of the Match error case above.
- **Checked / not checked:** Invocation and normalization were checked; specialization eligibility and JIT behavior were not.

### H8. Avoid eager empty-bag construction

- **Evidence:** [AttributeReference.evaluate](xacml-core/src/main/java/org/xacml4j/v30/policy/AttributeReference.java:104) passes `emptyBag()` to `Optional.orElse`, so Java evaluates it even when a value is present. Summed collapsed stacks containing both methods account for 182/10,091 CPU and 5,072/79,945 allocation samples in synthetic-all.
- **Change:** Construct the typed empty bag only on the absent path; separately measure whether immutable empty bags can safely be reused.
- **Expected gain:** The inclusive scales are **6.9 µs and 106 KB/op**. The avoidable fraction depends on successful resolutions and escape analysis.
- **Risk / test:** Preserve MustBePresent and resolver-failure behavior. `ValueType.emptyBag()` is overridable, so test custom type behavior before changing invocation timing; establish safe publication before sharing bags.
- **Checked / not checked:** Eager evaluation and the recorded stack were checked. No candidate or escape-analysis measurement was run.

### H9. Remove hash varargs arrays

- **Evidence:** AS:100 attributes 1.5% of allocation samples to `SemanticIdentifier.hashCode`’s `Object[]`; CS:95 attributes 0.8% self CPU. [SemanticIdentifier.hashCode](xacml-core/src/main/java/org/xacml4j/v30/SemanticIdentifier.java:101) uses `Objects.hash`.
- **Change:** Measure an equivalent direct hash calculation without the varargs array. Do not memoize blindly: the identifier fields are protected and mutable.
- **Expected gain:** The sampled rows scale to **3.1 µs and 25 KB/op**; AF attributes 3.3% of first-deny allocation samples to the same array, about **522 B/op** under the baseline-proportion assumption.
- **Risk / test:** Keep the exact hash contribution of both IDs and runtime class. Test equality/hash agreement and subclass field mutation.
- **Checked / not checked:** Field visibility and hash implementation were checked; allocation eliminated by C2 was not measured independently.

### H10. Measure compact object headers

- **Evidence:** AS:77–104 shows many small allocation sites. [JEP 519](https://openjdk.org/jeps/519) delivered compact headers as a JDK 25 product feature; the [JDK 25 launcher documentation](https://github.com/openjdk/jdk25u-dev/blob/master/src/java.base/share/man/java.md) says `-XX:+UseCompactObjectHeaders` is disabled by default.
- **Change:** Run a matched JMH pair with the flag off/on, retaining C2, G1, heap, JDK build and workload; record actual fork flags and `-prof gc` output.
- **Expected gain:** **Time and B/op UNVERIFIED.** The documentation’s approximate four bytes per object would imply roughly `4 × N` B/op for `N` allocated objects, but this profile does not establish `N` or the aligned size changes.
- **Risk / test:** Check decision equality and concurrent behavior, and measure both time and allocation. The baseline’s actual compact-header flag was not recorded in the cited environment excerpt.
- **Checked / not checked:** JEP and documentation were checked; neither flag configuration was run in this lane.

**Refuted as standalone shortcuts:** Deny-overrides already returns immediately on Deny; it cannot generally stop at Permit because a later Deny can override it ([DenyOverrides.java](xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DenyOverrides.java:67)). Blanket reordering is unsafe for first-applicable and for observable evaluation effects; the two correctness findings also make current target order significant. The faster isolated snapshot-store lookup does not establish a faster `decide`: the benchmark warms the reference cache ([JDK 21 baseline notes](openspec/changes/add-pdp-jmh-benchmarks/evidence/benchmarks/jmh/001-baseline/summary.md:50)).

**Open questions:** Which production policies and resolvers qualify for indexing or request-local caching? What fraction of the inclusive stacks remains after those changes? Can policy updates invalidate a compiled plan atomically? What are the matched Logback baseline and candidate results, including RSA, first-deny, all seven synthetic requests, and concurrent decisions?

**Verification and residual risk:** Read the named sources, recorded tables and profile summaries; decompressed and summed selected collapsed-stack weights with read-only `rtk proxy gzip -dc` and Python. Queried arXiv and primary sources. No files were changed, and no Maven build, test, benchmark, profiler, Git write, or beads write was run. The two defects are static findings requiring executable regression tests; every projected gain requires a quiet-window JMH pair on the same build and JDK.
