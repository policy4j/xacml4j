FINAL REPORT (Codex Sol, GPT-6.1)

Draft for `openspec/changes/optimize-pdp-evaluation/research/pr15-sol-xacml-literature.md`

**XACML and ABAC optimization literature — 2026-10-27**

The strongest next opportunities are to skip irrelevant policy children, avoid repeated remote reads, and compile a conservative dependency plan for D37. Published whole-engine speedups justify investigating these techniques; they do not predict equivalent gains over today’s xacml4j.

Sources below are primary papers, author repositories, standards, and engine documentation, accessed on 2026-10-27. Expected xacml4j effects are estimates, not measurements.

**Recorded wins and the remaining cost**

The retained comparison before AF–AJ shows:

| Workload | Batch 1 | Later recorded allocation | Change |
|---|---:|---:|---:|
| rsa2008, all | 15,431 B/decision | 6,193.6 B/decision | −59.9% |
| synthetic:1000, first-deny | 14,768 B/decision | 1,480.2 B/decision | −90.0% |
| synthetic:1000, all | 135,965 B/decision | 51,686 B/decision | −62.0% |

Bags, lazy response collections, function argument evaluation, combining loops, and prepared references account for these reductions. The broad synthetic case nevertheless increased 10.7% against batch 3: the added context reference crossed an object-alignment boundary. Child contexts represent approximately 97.22%, or 50.2 KB/decision, of that recorded allocation. This makes avoiding irrelevant children more promising than shaving the remaining fixed overhead. These are retained results, not a new measurement of AF–AJ. [Local allocation report, tables and analysis](`openspec/changes/optimize-pdp-evaluation/research/pr14-argon-allocation-batch45.md:17`).

The index remains a planned improvement in the inspected tree: `PreparedPolicySets.create(true)` still rejects its use. [PreparedPolicySets.java:42](`xacml-core/src/main/java/org/xacml4j/v30/policy/PreparedPolicySets.java:42`).

**Semantic conditions for adopting a technique**

Decision equality alone is insufficient. Preserve bag multiplicity (§7.3.2), designator matching (§7.3.4), the first resolved bag within a decision (§7.3.5), selector behavior (§7.3.7), obligation/advice evaluation and failures (§7.18), and missing attributes and errors (§7.19). Preserve extended Indeterminate outcomes and the applicable ordering rules in Annex C, plus lazy logical evaluation in Appendix A.3.5. Some combining algorithms permit order freedom; xacml4j additionally has observable PIP demand, response, and callback order that an optimization must preserve under its approved contract. [OASIS XACML 3.0, Errata 01, 2017](https://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html).

In-process extensions are trusted code. Unknown dependencies still prevent static optimization unless an extension supplies a suitable contract.

**1. Numericalization and normalization: XEngine**

Liu, Chen, Hwang and Xie’s XEngine replaces string-oriented matching with numerical domains, normalizes policies, and evaluates through forwarding tables or policy decision diagrams related to firewall decision diagrams. On seven real policies and 100,000 random requests, the paper reports average table/diagram speedups of 117×/75× over Sun’s PDP for single-valued requests, and 24×/18× for its multivalued experiment. These compare against a 2008 engine and include a restricted request decomposition. [SIGMETRICS 2008, author paper](https://taoxie.cs.illinois.edu/publications/sigmetrics08-xengine.pdf).

Its XACML 2.0 decision-model proof does not establish full XACML 3.0 response equivalence, including extended errors and obligation staging.

For xacml4j, use numerical policy-child IDs and postings for proven literal comparisons in `PolicySet.Prepared`; retain original children and evaluation order. Existing canonical keys and function bindings already remove part of the primitive overhead. The expected gain is the avoided child visits in synthetic:1000, bounded by the recorded context cost—not XEngine’s published multiplier. Whole-policy normalization is substantially riskier because it can flatten policy boundaries and duplicate or relocate response effects.

**2. Decision diagrams: MDD, MTBDD, FDD and MIDD**

These are related representations, not interchangeable semantics guarantees. MDDs branch over several domain values; MTBDDs have binary decisions and multiple terminal values; FDD-style structures partition attribute domains. XEngine’s PDD and MIDD add their own policy encodings.

Ngo, Demchenko and de Laat’s MIDD work explicitly addresses errors, critical attributes, obligations and advice; it should not be dismissed as a permit/deny-only model. Its author implementation separately reports about 200,000 requests/s on a random four-level, ten-attribute, 360-rule workload, while acknowledging incomplete specification coverage. That repository result is not a controlled comparison with xacml4j. [*Computers & Security* 49, 2015](https://www.sciencedirect.com/science/article/abs/pii/S0167404814001655), [author implementation](https://github.com/canhnt/sne-xacml).

Margrave uses MTBDDs for verification and change-impact analysis. Its Continue-application experiments report policy loading up to approximately 355 ms and property checks below 10 ms. These are analysis results, not PDP throughput. The paper explicitly excludes obligations from its representation. [Fisler et al., ICSE 2005](https://www.cs.cmu.edu/~mtschant/pubs/fkmt-verif-change-impact-xacml.pdf).

The practical xacml4j candidate is a bounded interval or multi-key **candidate filter**, followed by the existing combiner. Apply it around `PolicySet.Prepared`, preserving original positions and fallback evaluation. Existing equality-only synthetic policies offer little additional opportunity after 23.4; a range/overlap corpus is needed. Risks are diagram growth, rebuild cost, and incorrectly excluding children when values are missing, multivalued, or erroneous.

**3. Set-based evaluation: SBA-XACML**

Mourad and Jebbaoui express policy evaluation through set algebra. Their model includes conditions, obligations and Indeterminate; a blanket claim that it ignores these would be wrong. However, its displayed target/rule abstractions do not by themselves establish complete XACML 3.0 bag, selector and error equivalence.

For 100,000 synthetic requests and policies growing from 400 to 4,000 rules, the reported single-valued speedup falls from 3.2× to 2.4× against XEngine, and from 8× to 2.7× against Sun. The experiment places the denying rule last to force extensive evaluation. [*Expert Systems with Applications* 42, 2015](https://icaangroup.com/wp-content/uploads/2021/06/1-s2.0-S0957417414004370-main-1.pdf).

Use compact sets of **candidate child IDs**, not sets replacing XACML bags. This could cheaply intersect several target restrictions before creating contexts. Synthetic all/middle/last requests would expose the gain; first-deny already visits little. Risk is moderate for conservative filtering and high for replacing the evaluator or response assembly.

**4. Adaptive ordering and clustering**

Marouf, Shehab, Squicciarini and Sundareswaran combine subject-based clustering with usage-driven policy ordering. Their institutional abstract reports orders-of-magnitude improvements over Sun’s PDP. Detailed workload ratios and complete XACML 3.0 response/error preservation remain **UNVERIFIED** from the accessible primary material. [*IEEE Transactions on Services Computing* 4(4), 2011](https://pure.psu.edu/en/publications/adaptive-reordering-and-clustering-based-framework-for-efficient-/).

Do not reorder authored xacml4j children as a general optimization. First-applicable and ordered algorithms make this directly unsafe; even other algorithms can expose changed PIP demand and response order.

A safer adaptation is to use statistics to select among equivalent indexes, leaving execution order fixed. It needs skew and distribution-drift workloads. Expected additional gain on the current single-key synthetic corpus is small after indexing; order-preserving index selection is lower risk than adaptive policy execution.

**5. Target indexing, matching trees, clustering and splitting**

Pina Ros, Lischka and Gómez Mármol separate a Matching Tree from a Combining Tree, enabling binary-search-based matching while retaining policy combination structure. Their author description explicitly includes obligations and broad comparison support. It reports orders-of-magnitude improvements over Sun; detailed numerical ratios and complete error behavior are **UNVERIFIED** here. [SACMAT 2012, author publication](https://webs.um.es/felixgm/pubs/graph-based-xacml-evaluation/).

A later clustering engine, DPEngine, reports evaluation times of 2.23% of Sun, 3.47% of HPEngine, 3.67% of XEngine and 4.06% of SBA-XACML on 100,000 rules and 10,000 requests. Complete XACML 3.0 response equivalence is **UNVERIFIED**; those percentages are workload-specific. [Yu et al., *Scientific Reports* 12:5000, 2022](https://www.nature.com/articles/s41598-022-08637-8).

These support the existing D24/D25 architecture: prepare structure once, filter matching candidates, then use the original combiner. Finish 23.4 before attempting more elaborate clustering. Preserve source ordinals, reference identity and one evaluation per original child; splitting must not duplicate obligations or advice. Expected headroom is approximately 50 KB/decision on the recorded broad synthetic workload. Index construction and policy-churn costs must also be measured.

**6. Code generation, specialization and compiled combining**

Yu, Sivasubramanian and Xie translate XACML policies into Java for concolic test generation. Their reported benefit concerns test coverage, not production PDP throughput. It is evidence that translation is feasible, not evidence of a fast, fully compatible bytecode PDP. [CSIIRW 2009, author paper](https://taoxiease.github.io/publications/csiirw09-policy.pdf).

MyABDAC compiles attribute-based policy into MySQL access-control configuration, including GRANT/REVOKE operations. Its experiments include 50,000 users, 100 attributes and policies of 100–5,000 rules. This is a specialized enforcement and maintenance strategy, not a drop-in general XACML evaluator. [Jahid et al., CODASPY 2011](https://web.mit.edu/ha22286/www/papers/CODASPY11.pdf).

For xacml4j, start with successful, total, literal-only expressions in `Apply`, or a small prepared condition plan using existing function bindings. Preserve build-time checks, lazy failures, operand order and six-state combining behavior. `VariableDefinition` already memoizes named variables; that is not a new opportunity. [Apply.java:84](`xacml-core/src/main/java/org/xacml4j/v30/policy/Apply.java:84`), [VariableDefinition.java:115](`xacml-core/src/main/java/org/xacml4j/v30/policy/VariableDefinition.java:115`).

Expected gain is limited to the remaining repeated expression work; the current corpora do not size it well. A repeated-condition corpus is needed. Full JVM-bytecode speedup and compatibility claims remain **UNVERIFIED**.

**7. Exact decision caching and projected request keys**

JBoss XACML documented configurable exclusion of environment, subject, resource and action attributes from cache keys in PicketBox 2.0.5. Its example excludes an unread current-time attribute. A repeated seven-request experiment over four policies and one policy set improved from 36.176 s to 4.485 s with enhanced LRU caching—approximately 8.1×. This is an old repeated-request experiment, not a forecast for today’s microsecond evaluator. [Saldhana, JBoss engineering documentation, 2010](https://developer.jboss.org/docs/DOC-15767).

This is direct prior art for D37’s projection. Improve it through automatic conservative dependency analysis rather than manual exclusions. Garbage collection of weak keys is not policy-revocation correctness.

WSO2 documents separate policy, attribute, decision and PEP caches, with invalidation following policy, attribute and combining changes. [WSO2 Identity Server 5.9 documentation, undated](https://is.docs.wso2.com/en/5.9.0/learn/improving-xacml-pdp-performance-with-caching-techniques).

A specific primary publication establishing the requested “CPDP” acronym was **UNVERIFIED**. The verified cached-PDP implementations above support the architectural comparison without assigning claims to an unidentified paper.

In xacml4j, `PolicyDecisionCache` currently accepts a whole `RequestContext`; lookup precedes evaluation-context creation. D37 therefore needs a prepared projection and validity information available before evaluation, or a revised cache contract. Expected savings are proportional to the hit rate minus projection/cache cost; changing environment attributes make the present corpora unsuitable for estimating realistic hit rates. [PolicyDecisionCache.java:43](`xacml-pdp/src/main/java/org/xacml4j/v30/spi/pdp/PolicyDecisionCache.java:43`), [DefaultPolicyDecisionPoint.java:145](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java:145`).

**8. Secondary and approximate authorization recycling**

SAAM derives additional authorizations from cached decisions under a specified policy model. Its Bell–LaPadula experiment reports that caching 10% of authorizations lets the secondary evaluator answer over 30% more requests than conventional exact-only recycling, using 100 subjects, 1,000 objects and a fourteen-node lattice. This is sound inference within its model, not permission to guess generic XACML decisions. [Crampton, Leung and Beznosov, SACMAT 2006](https://citeseerx.ist.psu.edu/document?doi=691aea10d3b4b905990c746b2f82a25071c0d702&repid=rep1&type=pdf).

A workflow/RBAC caching study reports sub-millisecond cached authorization against approximately 27–38 ms without caching, while emphasizing cache-management costs. That workload includes stateful authorization concerns absent from the CPU-only corpora. [Kohler, Brucker and Schaad, MetriSec 2010](https://logicalhacking.com/publications/kohler.ea-caching-2010/kohler.ea-caching-2010.pdf).

General secondary inference is a poor next step for xacml4j: neither result proves full XACML 3.0 status, obligation or advice equivalence. Take the invalidation and dependency lessons into D37. Defer inference unless introduced as an explicitly restricted policy-model extension.

**9. Attribute dependency analysis, prefetch, batching and deadlines**

Brucker and Petritsch compute potentially required service attributes from policy structure, indexed by request context. PEP prefetch or a PDP request for the computed set can bound their protocol to two evaluation attempts. This is a protocol bound, not a measured multiplier for xacml4j. Their analysis assumes service attributes do not occur in core targets. [ESSoS 2010, LNCS 5965](https://brucker.ch/bibliography/download/2010/brucker.ea-efficient-2010.pdf).

NIST’s ABAC architecture also permits advance attribute retrieval and caching; it does not prove that eager retrieval preserves a particular engine’s effects. [NIST SP 800-162, 2014, updated 2019](https://nvlpubs.nist.gov/nistpubs/specialpublications/nist.sp.800-162.pdf).

The useful transfer is a static **MayRead** plan without fetching anything early. Use it for D37, resolver dependency inspection, and planning inside an already demanded resolver.

For remote work, use one bulk backend operation or D26’s parallel independent functions after preparing their inputs. Idealized latency changes from \\(nL\\) to \\(L\\) for one bulk operation, or roughly \\(\\lceil n/p\\rceil L\\) for parallel calls; these are unmeasured bounds excluding overhead. Keep deadlines, per-attribute failures, resolver fallback order and demanded-only publication. Relevant code is `AttributeResolverDescriptor` and `StructuredResolverRunner`. [StructuredResolverRunner.java:73](`xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/StructuredResolverRunner.java:73`).

Whole-policy eager prefetch can fetch attributes skipped by lazy evaluation, changing failures, calls and publication. The Multiple Decision Profile batches decisions but does not establish a shared attribute snapshot across them. [OASIS Multiple Decision Profile, Committee Specification 02, 2014](https://docs.oasis-open.org/xacml/3.0/multiple/v1.0/cs02/xacml-3.0-multiple-v1.0-cs02.html).

**10. Static redundancy, conflicts, unreachable rules and specialization**

Margrave demonstrates change-impact and policy-property analysis through decision diagrams. Later SMT-based work translates XACML constructs, combining algorithms and background theories for analysis. These establish useful analysis methods; a production-PDP speedup from deleting proven redundant code is **UNVERIFIED** here. [Fisler et al., ICSE 2005](https://www.cs.cmu.edu/~mtschant/pubs/fkmt-verif-change-impact-xacml.pdf), [Türk­men et al., *Computers & Security* 66, 2017](https://www.sciencedirect.com/science/article/pii/S0167404817300135).

For xacml4j, begin with offline reports and retain the authored, serialized policy. A proof supporting runtime removal must include missing values, multiplicity, extended Indeterminate outcomes and response effects. Two rules with equal decisions can still contribute different obligations; a conflict is not automatically redundancy.

Specialization to fixed attributes requires a binding guard and policy-generation invalidation. Put any optimized representation beside `PolicySet.Prepared`; do not rewrite the public policy model. Existing corpora contain little deliberate redundancy, so expected runtime gain is unknown. Automatic deletion is high risk; warnings and conservative preparation are lower risk.

**What the existing workloads can establish**

| Workload | Useful coverage | Important gap |
|---|---|---|
| rsa2008 | Nine XACML 2.0 policy sets, seventeen requests; mixed expression and policy behavior | Small legacy workload; JMH checks decisions, not every response field |
| synthetic | Large reference lists; equality targets; first/middle/last and unknown requests | Weak coverage of ranges, overlapping targets, multivalued bags, errors and response effects |
| `pip:N` | Ten cases, configurable latency/concurrency/cache; full-response oracle | Simulated backend; no production transport distribution or realistic update/hit-rate trace |
| Conformance suites | Separate semantic regression coverage | Not a throughput or realistic-cache workload |

These inventories come from `Rsa2008Corpus`, `SyntheticCorpus`, `PipCorpus` and the fixture’s distinct validation paths. [PdpFixture.java:430](`xacml-jmh/src/main/java/org/xacml4j/jmh/harness/PdpFixture.java:430`).

FIX-30 changes the interpretation of the current `pip` shared case: every resolver declares a context dependency, so `shared` with `pipCache=off` no longer exercises the whole-set memo. Add a legitimate context-key-free case before using that combination to measure task 30.

The next workload additions should cover overlapping ranges and duplicate bags; repeated conditions; wide requests with irrelevant environment and IncludeInResult attributes; policy/source churn; and remote latency distributions. Record construction cost, warm evaluation, hit rate, tail latency and saturation separately. Published Sun/XEngine ratios do not substitute for these comparisons.

**Eight next opportunities, ranked by expected gain and risk**

| Rank | Opportunity | Expected effect, unmeasured | Risk and useful workload |
|---:|---|---|---|
| 1 | Finish restricted target index, 23.4 | Avoid most irrelevant child visits; recorded broad synthetic allocation headroom is about 50 KB/decision | Moderate; synthetic all/middle/last, then error/payload cases |
| 2 | Compile a conservative MayRead plan per policy generation | Enables D37 projection over relevant attributes rather than the entire request; no eager PIP work | Moderate; wide requests and dependency/selector cases |
| 3 | D35 per-resolver positive TTL cache and cross-decision miss sharing, **Later** | Saves remote calls on hits and concurrent misses; little benefit at zero backend latency | Moderate; `pip`, controlled hits, expiry and source changes |
| 4 | D37 exact projected result cache, **Later** | Approximately hit-rate × avoided evaluation cost, minus key/cache overhead | High correctness risk; request reuse plus policy/source churn |
| 5 | Bulk backend reads inside demanded resolvers | Replace repeated remote round trips; existing D26 provides the parallel alternative | Low–moderate with an explicit backend contract; nonzero-latency `pip` |
| 6 | Successful literal folding and small prepared condition plans | Removes repeated expression work; bounded by the remaining dispatch/expression share | Moderate; new repeated-condition corpus |
| 7 | Multi-key/range candidate filters | Further reduce visits where equality indexing is insufficient | Moderate–high; new range/overlap corpus; little extra on today’s equality synthetic case |
| 8 | Payload-aware redundancy and unreachability analysis | Initially authoring feedback; runtime savings only after proof and profiling | High for automatic removal; new redundant-policy corpus |

D35 remains deferred. Its recorded packaging direction—separate Caffeine/cache2k modules and one JDK `ServiceLoader` locator—does not require changing for this research.

Adaptive execution reordering, generic secondary authorization inference and whole-engine diagram replacement rank below these opportunities because their semantic and integration risks exceed the currently demonstrated incremental gain.

**What D37 should take from the literature**

1. **Compute dependencies once.** Build an immutable MayRead plan covering targets, conditions, variables, obligation/advice assignments, referenced policies, selectors, `ContextSelectorId`, content and implicit context reads. Include resolver dependencies where declared. Unknown custom or dynamic reads require a declared contract or cache bypass. Actual reads from a previous evaluation alone are insufficient for a universal prelookup key.

2. **Project the request without resolving attributes.** Use a key containing PDP/domain identity, policy generation and plan identity, then ordered relevant slots with presence and typed bag values. Preserve issuer semantics and duplicate counts; retain equality checks after hashing. Do not change global `RequestContext.equals/hashCode`.

3. **Account for the whole cached response.** An unread attribute can still appear through IncludeInResult. Include echoed attributes and response options in the key, or separately approve rematerializing those response fields. Include ReturnPolicyIdList behavior, selector content/version dependencies and relevant environment values. Exclude current time only when the policy cannot read it.

4. **Use remaining lifetime.** A source with TTL 60 seconds whose cached value is already 55 seconds old contributes at most five remaining seconds. Bound the entry by the earliest participating positive-source expiry, policy/configuration limits and any provable time transition. A participating non-cacheable source prevents caching.

5. **Track unsuccessful dependencies too.** A remote absence, failure or timeout followed by a successful fallback can affect the decision. Under today’s no-negative-caching contract, such an evaluation should be uncacheable unless a separate validity contract is introduced. Taking TTL only from the winning resolver is insufficient.

6. **Fence invalidation and publication.** Repository events need a generation namespace and protection against late publication into a retired generation. D24/D25 do not provide an atomic repository snapshot across every previously unresolved reference; generation identity alone must not be treated as proof of a stable closure. Resolver/provider configuration changes also need versioning or invalidation.

7. **Keep correctness independent of optional observation.** Collect validity information internally, not through an optional D34 observer. Preserve audit, decision counters and cache-hit events. Apply keys to normalized individual decisions consistently with multiple-decision processing.

The literature supports projected keys and dependency analysis. It does not establish that “hash the attributes read last time and use the minimum nominal TTL” is exact for this engine.

Verification: read-only source and retained evidence inspection; primary-source retrieval through research tools and web fallback. Tavily rate limits, unhelpful DuckDuckGo results and unrelated arXiv metadata limited some searches; unsupported details are marked **UNVERIFIED**. No files changed, builds, tests, JVMs or measurements run.
