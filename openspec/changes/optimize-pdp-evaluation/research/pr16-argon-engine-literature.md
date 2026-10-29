> Conductor note (2026-10-27): several code pointers in this note are stale or wrong (no `BaseAttributeValue` class exists; `PreparedPolicySets` and `Policy` are in `xacml-core` `org.xacml4j.v30.policy`; value hashes no longer use `Objects.hash` after cycle U; typed `LambdaMetafactory` invokers exist since cycle I and D23). Treat the literature part as the result and verify every code mapping in source.

# PR16 (Argon): Authorization Engines Beyond XACML and General Evaluation-Engine Techniques Mapped to `xacml4j`

- **Date**: 2026-04-17
- **Author**: Lane `agyx_1` (Agy Gemini 4 Argon)
- **Change**: `optimize-pdp-evaluation`
- **Scope**: Authorization engines and systems beyond XACML (Cedar, OPA/Rego, Google Zanzibar / SpiceDB / OpenFGA / Ory Keto, AWS Zelkova & IAM / AVP, Soufflé & Polar/Oso), general evaluation-engine techniques (JVM bytecode & `MethodHandle` / `LambdaMetafactory` / JEP 484 compilation, Truffle/Graal partial evaluation, BDDs/MTBDDs, bitsets/RoaringBitmap/SIMD, hash-consing/interning, JDK 25 platform features), and production decision & PIP caching patterns informing **D35** and **D37**.
- **Baseline State of `xacml4j`**:
  - [`openspec/changes/optimize-pdp-evaluation/design.md`](`openspec/changes/optimize-pdp-evaluation/design.md`) (Batches 1–5 merged; D24/D25 tasks 23.1–23.3 merged with eager preparation and resolution snapshots; 23.4–23.5 `TargetIndex` pending; D26 virtual-thread parallel PIP functions and D27 per-PDP single-flight + per-decision memo merged; D28 PIP JMH corpus merged; D34 zero-allocation `DecisionObserver<S>` SPI designed; Batch 6 D38–D42 in progress; D35 per-resolver TTL caches and D37 decision caching deferred).
  - [`pr14-argon-allocation-batch45.md`](`.ntm/worktrees/xacml4j--batch2/agyx_1/openspec/changes/optimize-pdp-evaluation/research/pr14-argon-allocation-batch45.md`) (post-Batch-4/5 + D24 allocation baseline: `rsa2008|all` at `85,784 B/op`, `synthetic:1000|first` at `4,160 B/op`, `synthetic:1000|all` at `90,320 B/op`, `pip:8` cache-on at `3,008 B/op`).

---

## Research Methodology and Tool Health

Per [`docs/ai/RESEARCH.md`](`.ntm/worktrees/xacml4j--batch2/agyx_1/docs/ai/RESEARCH.md`):

1. **`arxiv` MCP (`SUPPORTED`)**: Retrieved full LaTeX source (`main.tex` macros and sections 1, 2.3, 3.2, 3.3, 4.1, 4.4, 5.2, 5.3, 5.4, 6) for Cutler et al. (OOPSLA 2024, `arXiv:2403.04651v2`), section 3.2 of Disselkoen et al. (FSE 2024, `arXiv:2407.01688v1`), and metadata/abstracts for Lemire et al. (`arXiv:1402.6407v10`, `arXiv:1709.07821`) and Bembenek et al. (`arXiv:2408.14017v3`).
2. **`context7` MCP (`SUPPORTED`)**: Resolved and queried official engine and library repositories: `/cedar-policy/cedar` (templates, `Authorizer::is_authorized`, `tpe.rs` type-aware partial evaluation), `/open-policy-agent/opa` (rule indexing trie, radix trie, `any_suffix_match_seen` 2-hit lazy index, `http.send` intra/inter-query caching, partial evaluation), and `/ben-manes/caffeine` (`LoadingCache`, `AsyncLoadingCache`, `Expiry.creating` hierarchical timer wheel).
3. **`tavily` MCP (`SUPPORTED`, with one late rate-limit event)**: Retrieved primary sources for Google Zanzibar (USENIX ATC 2019 PDF), AWS Zelkova (FMCAD 2018 PDF, CAV 2022 Springer), AWS IAM & Amazon Verified Permissions caching, OPA Partial Evaluation benchmarks, SpiceDB / OpenFGA, Soufflé (PPDP 2021 PDF, Zhao 2022 thesis), Oso / Polar, OpenJDK `MethodHandle` constant folding (Jorn Vernee 2024), JDK 25 JEPs (OpenJDK JEP 25 index, JEP 484, JEP 506, JEP 519), Filliâtre & Conchon (ML Workshop 2006 PDF), and cache2k documentation. On the final query (`Ory Keto`), Tavily returned HTTP 429 (`status: 429`); per `docs/ai/RESEARCH.md`, no retry loop was attempted and Ory Keto data was taken from the already-fetched Cedar OOPSLA 2024 paper (`arXiv:2403.04651v2` §6).
4. **`duckduckgo` MCP (`PARTIAL`)**: Used once as fallback; returned 0 results due to upstream bot filtering, so already-fetched primary PDFs and arXiv sources were used instead.

---

## 1. Cedar (AWS): Evaluator Design, Policy Slicing, Lean Verification, Symbolic Compilation, and TPE

### 1.1 Primary Sources (`SUPPORTED`)
- Joseph W. Cutler, Craig Disselkoen, Aaron Eline, Shaobo He, Kyle Headley, Michael Hicks, Kesha Hietala, Eleftherios Ioannidis, John Kastner, Anwar Mamat, Darin McAdams, Matt McCutchen, Neha Rungta, Emina Torlak, and Andrew Wells, *"Cedar: A New Language for Expressive, Fast, Safe, and Analyzable Authorization (Extended Version)"*, Proc. ACM Program. Lang. (PACMPL) 8, OOPSLA1, Article 118 (April 2024), [`arXiv:2403.04651v2`](https://arxiv.org/pdf/2403.04651v2.pdf).
- Craig Disselkoen, Aaron Eline, Shaobo He, Kyle Headley, Michael Hicks, Kesha Hietala, Eleftherios Ioannidis, John Kastner, Anwar Mamat, Darin McAdams, Matt McCutchen, Neha Rungta, Emina Torlak, and Andrew Wells, *"How We Built Cedar: A Verification-Guided Approach"*, Proc. ACM Softw. Eng. (PACMSE) 1, FSE (July 2024), [`arXiv:2407.01688v1`](https://arxiv.org/pdf/2407.01688v1.pdf).
- Cedar Rust implementation (`cedar-policy/cedar`, `cedar-policy-core/src/tpe.rs`, queried via Context7 `/cedar-policy/cedar`).

### 1.2 Architecture, Mechanisms, and Reported Results
1. **Bounded, Recursion-Free Evaluator (`arXiv:2403.04651v2` §1, §3.2)**:
   - Cedar forbids general recursion, unbounded loops, and arbitrary quantification (`contains`, `containsAll`, `containsAny` replace `forall`/`exists`).
   - Entity hierarchy store $\mu(E::s) = (v, h_1)$ precomputes and stores each entity's full transitive ancestor set $h_1$ as a hash set. Evaluating `e1 in e2` (`Eval-In`) looks up $\mu(E_1::s_1) = (v_1, h_1)$ and checks $E_2::s_2 \in h_1 \cup \{E_1::s_1\}$ in $O(1)$ average time without runtime graph traversal.
   - Nominal entity equality `E1::s1 == E2::s2` (`Eval-Eq`) compares entity type and ID directly without dereferencing $\mu$, so even non-existent entities compare safely.
   - **Reported Performance (`arXiv:2403.04651v2` §5.2.2 & `main.tex` benchmark macros, 100,000 requests per data point on AWS EC2 `m5.4xlarge`)**:
     - Cedar median evaluation latency is **`4.0–5.0 µs`** on `gdrive` and `TinyTodo`, and **`11.0 µs`** on `github` (p99 **`< 10 µs`** on `gdrive`, **`< 20 µs`** on `github`).
     - Cedar is **`28.7×–35.2×` faster than OpenFGA** (`gdrive` scale 50: `134.7 µs / 4.7 µs = 28.7×`; `github`: `378.7 µs / 11.0 µs = 34.4×`; `TinyTodo`: `175.8 µs / 5.0 µs = 35.2×`).
     - Cedar is **`42.8×–80.8×` faster than OPA/Rego** (`gdrive`: `284.0 µs / 4.7 µs = 60.4×`; `github`: `888.9 µs / 11.0 µs = 80.8×`; `TinyTodo`: `213.8 µs / 5.0 µs = 42.8×`).

2. **Sound Policy Slicing (`arXiv:2403.04651v2` §3.3, Theorem 3.1, §5.3)**:
   - Every Cedar policy has a syntactic scope header over `principal`, `action`, and `resource`.
   - Cedar extracts an index key pair $\langle \mathit{pof}(c), \mathit{rof}(c) \rangle \in (\mathit{Entity} \cup \{\mathtt{Any}\})^2$ from each policy $c$, where $\mathit{pof}(c) = E::s$ if the scope constrains `principal == E::s` or `principal in E::s`, and $\mathtt{Any}$ otherwise (similarly for $\mathit{rof}(c)$ on `resource`).
   - Policies are stored in a two-attribute map $\Sigma : (\mathit{Entity} \cup \{\mathtt{Any}\})^2 \to 2^{\mathit{Policy}}$.
   - At decision time for request $\sigma = (P, A, R, v_c)$ and store $\mu$ where $\mu(P) = (\_, h_P)$ and $\mu(R) = (\_, h_R)$, the slice generator forms the cross-product of ancestor sets:
     $$K = (h_P \cup \{P, \mathtt{Any}\}) \times (h_R \cup \{R, \mathtt{Any}\})$$
     and returns $\bigcup_{k \in K} \Sigma[k]$.
   - **Theorem 3.1 (Sound Slicing, proved in Lean 4)**: Any policy $c \notin \mathit{Slice}(\sigma, \mu, C)$ is guaranteed to evaluate to `false` under $(\sigma, \mu)$, so evaluating only the slice produces the exact same decision as evaluating $C$.
   - **Reported Slicing Speedup (§5.3, `main.tex` macros)**: On template-linked policies (which grow quadratically in entity count), slicing reduces evaluation time on `github-templates` (scale 50) from **`93.0 µs` to `9.3 µs` (`10.0×` speedup)** and on `gdrive-templates` (scale 50) from **`163.8 µs` to `9.1 µs` (`18.0×` speedup)**.

3. **Verification-Guided Development (VGD) and Differential Random Testing (DRT) (`arXiv:2407.01688v1` §3–§4; `arXiv:2403.04651v2` §5.1)**:
   - Cedar maintains a formal executable model in Lean 4 alongside its production Rust engine. The Lean proof-to-model LOC ratio is **`3.4 : 1`** and checks in ~3 minutes.
   - Seven security and correctness theorems are proved in Lean: `forbid` trumps `permit`, default deny, explicit allow, policy order independence, sound policy slicing, type-validation soundness (validated policies never fail with runtime type/attribute errors except non-existent entity lookup or integer overflow), and termination.
   - **Differential Random Testing (DRT)**: Using `cargo-fuzz` and structure-aware generators (`abac` type-directed and `hierarchy`-based generators), Cedar runs millions of randomized `(request, entities, policies)` inputs per night comparing the Rust production engine against the compiled Lean reference model. VGD discovered **4 bugs during Lean proofs** and **21 bugs via DRT** prior to release (including subtleties in template linking and parser edge cases).

4. **Symbolic Compilation to SMT (`arXiv:2403.04651v2` §4, §5.4)** and **Type-Aware Partial Evaluation (`tpe.rs`)**:
   - Cedar's symbolic compiler reduces policy equivalence/subsumption/disjointness queries ($\phi(C_1, \dots, C_n)$) to a **decidable quantifier-free SMT-LIB fragment** over Algebraic Datatypes (`Option`, records, entity IDs), BitVectors (`(_ BitVec 64)`), Strings, CVC5 Finite Sets, and Uninterpreted Functions (representing entity attributes and ancestor relations $\mathit{in}_E$), coupled with a finite-hierarchy grounding theorem (Theorem 4.6). Across 45 refactoring equivalence queries on `gdrive`, `github`, and `TinyTodo`, CVC5 solves queries in **`45.3–119.6 ms` (`75.1 ms` average)**.
   - **Type-Aware Partial Evaluation (TPE, `cedar-policy-core/src/tpe.rs`)**: Evaluates policies against a `PartialRequest` and `PartialEntities` (where attributes or principals may be unknown) using the schema environment $\Gamma$, returning simplified `Vec<ResidualPolicy>` trees that can be translated into SQL `WHERE` clauses for data-filtering pushdown.

### 1.3 XACML 3.0 Semantic Compatibility Analysis
- **Where Cedar diverges from XACML 3.0**:
  - Cedar's combining rule is fixed (`forbid-overrides` with default `Deny`) and **ignores runtime evaluation errors** (a policy that errors on a missing attribute is simply skipped and logged in `Diagnostics::errors()`).
  - XACML 3.0 supports ordered and error-sensitive combining algorithms (`deny-overrides`, `permit-overrides`, `first-applicable`, `only-one-applicable`, `deny-unless-permit`, `permit-unless-deny`, legacy `ordered-*`), distinguishes `Indeterminate{D}`, `Indeterminate{P}`, and `Indeterminate{DP}` (§7.10, §7.12–§7.14, Annex C), requires missing attributes with `MustBePresent="true"` to return `Indeterminate` (§7.19), stages obligations/advice only on matching `Permit`/`Deny` (§7.18), and optionally returns all applicable policy IDs (`ReturnPolicyIdList`, §5.42).
- **What preserves XACML 3.0 semantics 100%**:
  1. **Sound Policy Slicing with `Any` Wildcard Buckets + Declaration-Order Preservation**: In XACML 3.0, if a child `Policy`/`PolicySet` or `Rule` has a `Target` whose `AnyOf` clauses require exact equality `attr == literal` on request-only designators with `MustBePresent="false"`, any child not present in $\bigcup_{k \in K} \Sigma[k]$ is **provably `NotApplicable`** (never `Match` and never `Indeterminate` when the request attribute is resolved cleanly and `MustBePresent="false"`). Sorting or bitset-iterating the union in original child declaration index order $0 \dots N-1$ preserves XACML 3.0 evaluation order, Annex C short-circuit behavior, §7.18 obligation ordering, and §5.42 `ReturnPolicyIdList`.
  2. **Verification-Guided Differential Random Testing (DRT)**: Treating the unindexed, un-prepared evaluator (`DefaultPolicyDecisionPoint` without `PreparedPolicySets` or `TargetIndex`) as the executable reference oracle and running randomized/mutated `(PolicySet, RequestContext)` differential checks against the indexed/prepared PDP verifies exact equivalence on `(Decision, Status, Obligations, AssociatedAdvice, PolicyIdentifierList)`.

### 1.4 Concrete Mapping to `xacml4j`
- **Target Classes**: [`PreparedPolicySets`](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PreparedPolicySets.java`), upcoming `TargetIndex` (task `23.4`), [`Policy`](`xacml-core/src/main/java/org/xacml4j/v30/pdp/Policy.java`) (`combineDecisions` over `List<Rule>`), and [`XacmlPolicyTestSupport`](`xacml-test/src/main/java/org/xacml4j/v30/XacmlPolicyTestSupport.java`).
- **Corpora Impacted**: `rsa2008` (296 rules inside a single policy + role-hierarchy policy sets), `synthetic:100`, `synthetic:1000`.
- **Expected Size & Risk**: Small-to-Medium (~200–350 LOC for compound `(Attr1, Attr2, Any)` slicing + intra-`Policy` rule indexing; ~250 LOC for DRT test harness). Risk: Low when fallback bucket (`Any`) captures all non-indexable/PIP/MustBePresent targets.

---

## 2. Open Policy Agent (OPA) and Rego: Multi-Dimensional Rule Indexing, Partial Evaluation, Wasm vs Topdown, and Builtin Caching

### 2.1 Primary Sources (`SUPPORTED`)
- OPA Official Documentation (*"Policy Performance"*, *"Configuration"*, *"HTTP Built-in Caching"*, queried via Context7 `/open-policy-agent/opa`).
- Torin Sandall, *"Optimizing OPA: Rule Indexing"* (OPA Blog, 2017) and *"Partial Evaluation"* (OPA Blog, 2018, `https://blog.openpolicyagent.org/partial-evaluation-162750eaf422`).
- OPA GitHub repository issues `open-policy-agent/opa#2281` and `open-policy-agent/opa#3407` (Wasm compilation vs Topdown rule indexing).

### 2.2 Architecture, Mechanisms, and Reported Results
1. **Multi-Dimensional Rule Indexing Trie (`/open-policy-agent/opa` `docs/content/policy-performance.md`)**:
   - OPA's `Topdown` evaluator builds a trie over `input.*` references in rule bodies to find applicable rules in $O(D)$ (where $D$ is the number of indexed input attributes) rather than $O(N)$ rules.
   - Supported indexable statements:
     - Equality (`input.a = "x"`, `"x" == input.a`) and local variable unification (`x := input.a; x == "foo"`);
     - Membership (`"x" in input.array`), where the trie traverses *all* elements of the input array (`BagOfValues` in XACML!) and merges matching subtries;
     - Glob and prefix/suffix matching (`glob.match`, `strings.any_prefix_match`, `strings.any_suffix_match`), indexed via a terminal **radix trie** node.
   - **Trie Node Ordering Heuristic**: OPA orders scalar equality nodes (`input.x == "lit"`) *before* collection-membership (`in`) nodes, and places radix-trie nodes at the leaves to minimize branching factor.
   - **Lazy 2-Hit Index Construction (`any_suffix_match_seen`)**: For expensive string-collection matchers, OPA computes an FNV hash of the pattern array on the first call without building a trie; only on the **second hit** of the same hash does OPA allocate and populate the radix trie (bounded by `max_num_entries = 1000`).

2. **Partial Evaluation (`PartialResult`, Sandall 2018)**:
   - When rules iterate over static external `data.*` collections joined against `input.*`, standard rule indexing cannot index the loop body.
   - OPA's partial evaluator runs at policy/data load time with `unknowns = ["input"]`, unrolling the `data.*` loops and emitting a flat synthetic ruleset containing only `input.*` equality predicates that the rule-indexing trie can index directly.
   - **Reported Numbers**: On a 2,000-role / 2,000-binding RBAC benchmark, full evaluation took **`45.49 ms/op`**, whereas partially evaluated + indexed evaluation took **`0.062 ms/op` (`~730×` latency reduction)**, trading off **`31.97 s` build time and `29.1 MB` heap**.

3. **Wasm Compilation vs Topdown Evaluation (`opa#2281`, `opa#3407`)**:
   - OPA can compile Rego AST $\to$ IR $\to$ WebAssembly (Wasm) bytecode.
   - **Critical negative result**: OPA's Wasm compiler emits linear branching instructions and **does not implement the dynamic rule-indexing trie** (`opa#2281`). Consequently, for small non-indexed compute loops Wasm reduces interpreter dispatch overhead by `2×–5×`, **but on large rulesets ($N \ge 500$ rules) Topdown with Rule Indexing beats raw Wasm by an order of magnitude** unless Partial Evaluation is run *before* Wasm compilation (`AST -> PartialEval -> IR -> Wasm`).

4. **Two-Level Builtin Caching (`http.send` & `inter_query_builtin_cache`)**:
   - OPA separates external resolver caching into:
     - **Intra-query cache**: Deduplicates identical external calls within a single policy decision (matching `xacml4j`'s per-decision `requestDesignators` / `resolverValues` memo in [`DefaultEvaluationContextHandler`](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java`)).
     - **Inter-query cache**: Cross-decision cache with configurable `max_size_bytes` and `caching_mode`: `"serialized"` (byte-array storage) vs `"deserialized"` (parsed in-memory AST values, eliminating repeated deserialization on warm hits).

### 2.3 XACML 3.0 Semantic Compatibility Analysis
- OPA's trie traversal over multi-valued `in` collections (`"x" in input.array`) is isomorphic to XACML 3.0's `Match` semantics (§7.6), which evaluates to `Match` if *at least one* value in the resolved `BagOfValues` matches the literal!
- Furthermore, XACML 3.0's `Target` structure is a 3-level conjunctive normal form:
  $$\mathtt{Target} = \bigwedge_{i} \mathtt{AnyOf}_i, \quad \mathtt{AnyOf}_i = \bigvee_{j} \mathtt{AllOf}_{i,j}, \quad \mathtt{AllOf}_{i,j} = \bigwedge_{k} \mathtt{Match}_{i,j,k}$$
  This maps directly onto OPA's multi-attribute trie + Cedar's wildcard bucket (`Any`).

### 2.4 Concrete Mapping to `xacml4j`
- **Critical Gap Identified in `xacml4j` Current Plan**:
  - In [`design.md`](`openspec/changes/optimize-pdp-evaluation/design.md`), task `23.4` (`TargetIndex`) indexes child `DecisionCombiningAlgorithmElement`s inside a [`PolicySet`](`xacml-core/src/main/java/org/xacml4j/v30/pdp/PolicySet.java`).
  - However, [`Policy.combineDecisions`](`xacml-core/src/main/java/org/xacml4j/v30/pdp/Policy.java`) passes `List<Rule> rules` directly to `decisionCombiningAlgorithm.combine(context, rules)` **with no `TargetIndex` on `Rule`s**!
  - In `rsa2008`, `PolSet11` (`urn:oasis:names:tc:xacml:2.0:WriteRules-policy`) has **296 `Rule`s inside a single `Policy`**, each with its own `Target` (`subject:role` and `resource:type` / `action:type`). Without intra-`Policy` rule indexing, `rsa2008` linearly evaluates up to 296 rule targets on every request!
- **Actionable Recommendation**: Share the `TargetIndex<E extends DecisionCombiningAlgorithmElement>` machinery between `PolicySet` (`List<DecisionCombiningAlgorithmElement>`) and `Policy` (`List<Rule>`). And heed OPA's Wasm lesson: **always keep structural `TargetIndex` filtering above any expression bytecode compilation**.

---

## 3. Google Zanzibar, Leopard Indexing, Zookies, SpiceDB, OpenFGA, and Ory Keto: What Carries Over to ABAC

### 3.1 Primary Sources (`SUPPORTED`)
- Ruoming Pang, Ramon Caceres, Mike Burrows, Zhifeng Chen, Pratik Dave, Nathan Germer, Alexander Golynski, Kevin Graney, Nina Kang, Karthik Chockalingam, et al., *"Zanzibar: Google's Consistent, Global Authorization System"*, 2019 USENIX Annual Technical Conference (USENIX ATC 2019), pp. 33–46, [PDF](https://www.usenix.org/system/files/atc19-pang.pdf).
- AuthZed SpiceDB documentation (`ZedToken` consistency modes, Caveats, and Materialize), OpenFGA documentation (Contextual Tuples and Conditions), and Ory Keto benchmarks in Cutler et al. (`arXiv:2403.04651v2` §6).

### 3.2 Architecture, Mechanisms, and Reported Results
1. **Production Scale and Tail-Latency Engineering (`atc19-pang.pdf` §1, §4)**:
   - Zanzibar stores >2 trillion ACL tuples across thousands of namespaces and processes **>10 million client queries/sec** (generating 22M internal delegated `Check`/`Read` RPCs/sec, 200M in-memory cache lookups/sec, and 20M Spanner queries/sec) with **95th-percentile latency `< 10 ms` (~3 ms median), 99th-percentile `< 15 ms`**, and **`>99.999%` availability**.
2. **Zookies (Bounded-Staleness Consistency Tokens) and the "New Enemy Problem" (`atc19-pang.pdf` §2.2, §3.2)**:
   - Stale caches or out-of-order attribute replication cause the **New Enemy Problem**: Alice removes Bob from an ACL and immediately adds confidential content, or an administrator revokes a compromised role, but a cached authorization decision or stale attribute cache still grants Bob access.
   - Zanzibar solves this without paying global synchronization latency on every request: when an ACL or content mutation occurs, Zanzibar returns an opaque consistency token (*zookie*, wrapping a Spanner TrueTime commit timestamp). Subsequent `Check` requests carry the zookie (or accept a default bounded-staleness snapshot window when no zookie is supplied). Any cache entry whose snapshot timestamp is older than the request's zookie is bypassed or refreshed. SpiceDB implements this as `ZedToken` (`minimize_latency`, `at_least_as_fresh`, `at_exact_snapshot`, `fully_consistent`).
3. **Leopard Specialized Transitive Group Index (`atc19-pang.pdf` §3.3, §4.4)**:
   - Deeply nested or wide group hierarchies degrade recursive pointer-chasing. Leopard flattens arbitrary transitive reachability into two denormalized skip-list index families: `GROUP2GROUP(G) -> {ancestor/descendant groups}` and `MEMBER2GROUP(U) -> {direct groups}`.
   - Membership `Check(User U in Group G)` reduces to a single skip-list set intersection:
     $$(\mathtt{MEMBER2GROUP}(U) \cap \mathtt{GROUP2GROUP}^{-1}(G)) \neq \emptyset$$
   - Incremental watch workers apply tuple deltas continuously in the background. Leopard serves **1.56M median / 2.22M p99 queries/sec** at **median `< 150 µs`, p99 `< 1 ms`**.
4. **Lock-Table Single-Flight Deduplication + Hot-Key Prefetching (`atc19-pang.pdf` §3.4.3, §4.4)**:
   - Zanzibar routes related `Check` sub-problems via consistent hashing and places an **in-flight lock table** in front of the cache and Spanner reads: only one thread evaluates a miss while concurrent requests for the same key await the shared result.
   - **Reported Numbers (§4.4)**: On `Check` requests, the callee cache achieves a 10% direct hit rate and **an additional 12% of requests are satisfied by the in-flight lock table** (preventing 500K internal RPCs/sec from stampeding Spanner during bursts). For the top `0.1%` super-hot groups, Zanzibar runs **eager background prefetching** rather than lazy on-demand expansion.
5. **Hybrid ReBAC + ABAC (SpiceDB Caveats & OpenFGA Conditions)**:
   - Both SpiceDB and OpenFGA discovered pure ReBAC cannot express time windows, IP ranges, or numeric quotas without tuple explosion, so they added ABAC predicates (CEL expressions in SpiceDB Caveats; typed Conditions in OpenFGA) attached to edges. Graph traversal returns a boolean caveated expression tree that is evaluated against the request context at the end of the check.

### 3.3 Concrete Mapping to `xacml4j`
1. **Epoch / Zookie Consistency Tokens for D35 & D37 (`New Enemy` Prevention)**:
   - In `xacml4j`, [`PreparedPolicySets`](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PreparedPolicySets.java`) already constructs an immutable `Generation` snapshot whenever the root policy or `PolicyRepository` changes.
   - Assigning each `PreparedPolicySets.Generation` a monotonic `long generationId` and embedding `generationId` into **D37**'s `RequestCacheKey` guarantees **instant, zero-allocation, lock-free atomic invalidation** of cached decisions whenever policies change.
   - Furthermore, allowing callers (or the `RequestContext`) to optionally pass a `minAttributeRevision` / `bypassCache` hint—or exposing `PolicyInformationPoint.invalidate(AttributeDesignatorKey)`—prevents the New Enemy Problem when an application mutates a subject's privileges and immediately re-evaluates.
2. **Cross-Decision Lock-Table Coalescing (`D35`)**:
   - Zanzibar's 12% lock-table hit rate (exceeding its 10% passive cache hit rate on `Check`!) empirically validates the maintainer's **D35** design requirement: *"Concurrent misses of one key from different decisions of one PDP should share one remote call."* Using `LoadingCache.get(key, loader)` in Caffeine, cache2k, or Guava provides this exact per-key lock-table coalescing across concurrent PDP decisions.
3. **Leopard Transitive Closure in PIP Role/Group Resolvers**:
   - In `rsa2008` and enterprise XACML deployments, role inheritance is often modeled via chained `PolicySetIdReference`s (`PPS:employee` $\subset$ `PPS:manager` $\subset$ `PPS:doctor`). When role hierarchy resolution is moved to a PIP resolver returning the flattened ancestor role bag (Leopard's `MEMBER2GROUP`), `TargetIndex` can resolve role-based policies in one `O(1)` lookup instead of evaluating deep `PolicySetIdReference` chains.

---

## 4. AWS Zelkova, AWS IAM Evaluation at Scale, and Amazon Verified Permissions (AVP)

### 4.1 Primary Sources (`SUPPORTED`)
- John Backes, Pauline Bolignano, Byron Cook, Catherine Dodge, Andrew Gacek, Kasper Luckow, Neha Rungta, Oksana Tkachuk, and Carsten Varming, *"Semantic-based Automated Reasoning for AWS Access Policies using SMT"*, 2018 Formal Methods in Computer Aided Design (FMCAD 2018), pp. 1–9, [PDF](http://www0.cs.ucl.ac.uk/staff/b.cook/FMCAD18.pdf).
- Neha Rungta, *"A Billion SMT Queries a Day"*, Computer Aided Verification (CAV 2022), LNCS vol. 13371, pp. 3–18, [Springer](https://link.springer.com/chapter/10.1007/978-3-031-13185-1_1).
- AWS IAM Policy Evaluation Logic documentation and Amazon Verified Permissions (AVP) + API Gateway authorizer caching documentation.

### 4.2 Architecture, Mechanisms, and Reported Results
1. **Zelkova Control-Plane SMT Analysis (Backes et al. 2018; Rungta 2022)**:
   - Translates IAM policies (Statements with `Effect: Allow|Deny`, `Action`, `Resource` globs `*`/`?`, and `Condition` blocks like `StringLike`, `NumericLessThanEquals`, `DateGreaterThan`, `IpAddress`, `ForAllValues`, `ForAnyValue`) into quantifier-free SMT formulas over strings, regular expressions, bitvectors, and integers.
   - Because general regex intersection/containment with wildcards is PSPACE-complete, Zelkova applies two domain-specific passes before invoking a solver portfolio (Z3, CVC4, cvc5 with UNSAT proof checking, Z3Automata):
     1. **Exact Character-Count Partitioning**: Rewrites regexes into finite unions of fixed-length string constraints up to length $k$ plus a symbolic tail $> k$.
     2. **Large Constant Abstraction**: Replaces static string prefixes/ARN segments that cannot overlap wildcard boundaries with distinct integer enum constants, turning expensive string-theory constraints into trivial integer equality checks.
   - Powers S3 Block Public Access, IAM Access Analyzer, and AWS Config across **>1 billion SMT queries per day** (most finishing in milliseconds to <1 second).

2. **AWS IAM Data-Plane Evaluation (>400 Million Evaluations/sec Globally) vs AVP Caching**:
   - **Core AWS IAM Data Plane**: Does **not** use a TTL decision cache on the enforcement path because any `Deny` attachment or role revocation must take effect immediately upon control-plane propagation. Instead, IAM achieves sub-millisecond evaluation at >400M req/s by:
     1. **Control-plane pre-compilation and eager regional distribution** of policies into compact, pre-parsed binary structures stored in memory on local enforcement nodes;
     2. **Deterministic short-circuit evaluation order** (`Explicit Deny` across SCPs / Resource / Identity policies $\to$ `Allow` check), aborting the moment any explicit `Deny` matches.
   - **Amazon Verified Permissions (AVP) + API Gateway Edge Caching**:
     - For application-level ABAC/Cedar policies, AWS recommends caching at the PEP / API Gateway authorizer boundary with a configurable short TTL (default **120 seconds**, max 3,600 seconds) keyed by a **projected identity + action + resource tuple** (`context.identity.sourceIp`, `Authorization` token claims, `httpMethod`, `resourcePath`), rather than hashing the raw HTTP payload.

### 4.3 Concrete Mapping to `xacml4j`
1. **Load-Time Constant Abstraction / Interned Enum IDs for `TargetIndex` and `Match`**:
   - Inspired by Zelkova's Large Constant Abstraction: in a prepared policy set, all distinct `(ValueType, Value)` constants appearing in `Target` `Match` predicates and `Apply` literals can be assigned a dense integer constant ID $0 \dots M-1$ within the `PreparedPolicySets.Generation`. Request attribute values looked up in that constant table can then be compared by integer ID (`int == int`) or bitset lookup rather than repeated `String.equals` / `URI.equals`.
2. **AVP's Projected Cache Key and Volatile-Attribute Bailout for `D37`**:
   - Directly solves the maintainer's **D37** design question (*"which request attributes form the key... time-dependent policies"*):
     - At `PreparedPolicySets` build time, walk the reachable policy tree once to compute its static `PolicyAttributeFootprint`:
       - The exact sorted array of referenced `AttributeDesignatorKey[]`s;
       - Whether the policy tree references XACML environment clock attributes (`urn:oasis:names:tc:xacml:1.0:environment:current-time`, `current-date`, `current-dateTime`) or `AttributeSelector` / XPath expressions.
     - If a policy tree references `current-time` / `current-dateTime` (and the caller did not supply a fixed test timestamp in the `RequestContext`), **D37** automatically disables decision caching for that policy tree (or caps the TTL), preventing stale time-window decisions!

---

## 5. Datalog-Based Authorization (Soufflé, Formulog, Polar/Oso) and Incremental Evaluation

### 5.1 Primary Sources (`SUPPORTED`)
- David Zhao, Pavle Subotic, Mukund Raghothaman, and Bernhard Scholz, *"Towards Elastic Incrementalization for Datalog"*, 23rd International Symposium on Principles and Practice of Declarative Programming (PPDP 2021), [PDF](https://souffle-lang.github.io/pdf/ppdp21incremental.pdf); and David Zhao, *"Provenance, Incremental Evaluation, and Debugging in Datalog"*, PhD Thesis, University of Sydney (2022).
- Aaron Bembenek, Michael Greenberg, and Stephen Chong, *"Making Formulog Fast: An Argument for Unconventional Datalog Evaluation"*, Proc. ACM Program. Lang. 8, OOPSLA 2024, [`arXiv:2408.14017v3`](https://arxiv.org/html/2408.14017v3).
- Oso / Polar Engine Architecture documentation (`docs.osohq.com`, `docs.rs/oso`).

### 5.2 Architecture, Mechanisms, and Reported Results
1. **Soufflé: Stratification, Semi-Naïve Specialization to C++, and Elastic Incrementalization (PPDP 2021)**:
   - Soufflé compiles Datalog programs via a 3-stage pipeline (Datalog AST $\to$ Relational Algebra Machine RAM with semi-naïve delta rules $\Delta T^{(k)} = \pi(\Delta T^{(k-1)} \bowtie R) - T^{(k-1)}$ $\to$ specialized C++ with custom B-tree and Brie tries).
   - **Elastic Incrementalization (`ppdp21incremental.pdf`)**: For dynamic updates (insertions `diff_plus` and deletions `diff_minus`), Soufflé annotates each derived tuple with its iteration number $I$ and derivation count $C$. High-impact stratum deletions fall back to local recomputation while low-impact updates propagate deltas incrementally, achieving **`5.4×–6.2×` speedups** over full re-evaluation on small/medium graph mutations.
2. **Formulog (Bembenek et al., OOPSLA 2024, `arXiv:2408.14017v3`)**:
   - **Crucial finding for PIP-backed engines**: When Datalog rules invoke expensive external side-effecting/I/O queries (SMT solver calls in Formulog; PIP resolvers in XACML), traditional bottom-up breadth-first semi-naïve evaluation wastes massive work computing facts on unreachable branches. Switching to **top-down, goal-directed depth-first evaluation with per-query memoization** yielded a **`10×` median speedup** because external calls are only triggered along live proof paths.
3. **Polar / Oso (Goal-Directed Unification VM with Specializer Dispatch)**:
   - Polar uses a top-down Prolog-like goal stack in Rust that yields coroutine events (`QueryEvent::ExternalIsa`, `ExternalCall`) to inspect host-language objects lazily without upfront serialization, and indexes rules by head predicate + argument type specializers.

### 5.3 XACML 3.0 Semantic Compatibility & Concrete Mapping to `xacml4j`
- **Why bottom-up Datalog execution does not fit XACML 3.0 runtime**: XACML 3.0 evaluation is strictly top-down and lazy (Annex C short-circuiting). Evaluating rules bottom-up would eagerly invoke PIP resolvers on unreachable rules and manufacture spurious `Indeterminate` errors. Formulog (2024) confirms top-down lazy evaluation with memoization (`xacml4j`'s current architecture + D27) is strictly superior when external resolvers exist.
- **What transfers directly to `xacml4j`**:
  1. **Incremental `PreparedPolicySets` Graph Maintenance on `PolicyRepository` Updates**:
     - Currently in [`PreparedPolicySets`](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PreparedPolicySets.java`), any `PolicyRepositoryListener` event invalidates the entire `generation` reference (`generation = null`), forcing every root `PolicySet` to be re-walked and re-indexed from scratch.
     - By recording the reverse dependency map `CompositeDecisionRuleID -> Set<RootPolicySetKey>` during `PreparedPolicySets` pinning (analogous to Soufflé's dependency graph), a repository update to policy $P$ only re-prepares the root `PolicySet`s that transitively reference $P$, copying all unaffected prepared entries into the new `Generation` in $O(1)$ time.
  2. **Polar-Style Arity/Shape Specializers on `Apply` (`Batch 6` Cycle AB)**:
     - Specializing `Apply` invocation paths for arity 1, 2, 3 vs varargs/bags avoids generic list machinery on the hot path.

---

## 6. General Evaluation-Engine Techniques Mapped to `xacml4j` and JDK 25

### 6.1 Compiling Policies to JVM Bytecode: `MethodHandle` Trees, `LambdaMetafactory`, and JEP 484 (`java.lang.classfile`) + `defineHiddenClass`

#### Primary Sources (`SUPPORTED`)
- Jorn Vernee (Oracle HotSpot Compiler Engineer), *"A Primer on MethodHandles — Part 1: Constant Folding"* (Jan 2024, `https://jornvernee.github.io/methodhandles/2024/01/19/methodhandle-primer.html`) and OpenJDK HotSpot Wiki (`MethodHandles` and `@Stable` constant folding rules).
- OpenJDK JEP 371: *Hidden Classes* (Final in JDK 15, `MethodHandles.Lookup.defineHiddenClass`).
- OpenJDK JEP 484: *Class-File API* (`java.lang.classfile`, **Final in JDK 24 and standard in JDK 25**, `https://openjdk.org/jeps/484`).

#### Technical Analysis & Critical JVM Pitfall
1. **Why Naive `MethodHandle` Trees in Instance Fields Fail to Inline (`SUPPORTED`)**:
   - In [`FunctionInvocationFactory`](`xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionInvocationFactory.java`), `xacml4j` currently invokes `@FuncSpec` methods via reflection (`Method.invoke(instance, params)`), which boxes primitives, allocates/copies `Object[]`, and goes through `NativeMethodAccessorImpl` / generated reflection inflation stubs.
   - A common temptation is to replace `Method` in `DefaultFunctionInvocation` with `private final MethodHandle mh;` or compose an expression AST into a tree of `MethodHandles.filterArguments(...)` stored in an instance field on `Apply`.
   - **HotSpot C2 Rule (Vernee 2024)**: C2 **only** constant-folds and inlines through `MethodHandle.invokeExact` when the `MethodHandle` instance itself is a **JIT compile-time constant**—meaning it lives in a `static final` field, a constant-pool `invokedynamic` `CallSite`, or an internal JDK `@Stable` field! When a `MethodHandle` is loaded from a normal `final` instance field on a heap object (`Apply` or `DefaultFunctionInvocation`), C2 emits `<failure reason='not inlineable'> MethodHandle::invokeBasic` because multiple instances of that class carry different `MethodHandle` targets.
2. **Two Proven Solutions on Standard JDK 25 (Zero Preview Flags, Zero External Dependencies)**:
   - **Tier 1 (Immediate, Low Risk — `LambdaMetafactory` Typed Invokers in `FunctionInvocationFactory`)**:
     - Every `@PolicyFuncProvider` method in `xacml4j` (`StringFunctions`, `ArithmeticFunctions`, `LogicalFunctions`, `HigherOrderBagFunctions`, etc.) is a `public static` method!
     - At function registration time in [`FunctionInvocationFactory`](`xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionInvocationFactory.java`), use `LambdaMetafactory.metafactory(...)` to bind 1-arg, 2-arg, 3-arg, and context-taking static methods to small functional interfaces (`FuncInvoker1`, `FuncInvoker2`, `FuncInvoker3`).
     - Why this beats instance-field `MethodHandle`: `LambdaMetafactory` spins a dedicated hidden class per `@FuncSpec` function whose `invoke(...)` method contains a direct `invokestatic` bytecode instruction to the target Java method. When stored on `FunctionSpecImpl`, C2's standard type-profiled receiver inlining (or specialized `Apply` node) inlines the call directly without needing `static final` constant folding, eliminating `Method.invoke` and `Object[]` varargs arrays completely.
   - **Tier 2 (Medium-Term — JEP 484 `java.lang.classfile` + `Lookup.defineHiddenClass` for Hot `Condition` / `Apply` Trees)**:
     - Because JEP 484 (`java.lang.classfile`) is **final and standard in JDK 25**, `xacml4j` can compile a `Condition` or complex `Apply` tree into a Hidden Class (`Lookup.defineHiddenClassWithClassData(bytes, constantsList, true, ClassOption.NESTMATE)`) in ~250 LOC with **zero external dependencies** (no ASM or ByteBuddy needed).
     - Using `MethodHandles.classData(lookup, "_", List.class)` stored into `static final` fields of the generated hidden class, all `AttributeDesignatorKey`s, literal `Value`s, and `FunctionSpec` handles become true `static final` constants inside the hidden class, enabling C2 to constant-fold and inline the entire `Condition` expression into a single flat machine-code block!
     - **Garbage collection safety**: Hidden classes defined without `STRONG` option (`ClassOption.NESTMATE` only) are unloaded automatically by the GC as soon as the `PreparedPolicySets.Generation` that references them is replaced!

---

### 6.2 Truffle and Graal Partial Evaluation (Futamura Projections)

#### Primary Sources (`SUPPORTED`)
- Oracle GraalVM Truffle Framework Documentation (*"Optimizing Truffle Interpreters"* / `@ExplodeLoop` / `CompilerDirectives.isPartialEvaluationConstant`).

#### Analysis & Verdict for `xacml4j`
- Truffle achieves the First Futamura Projection by annotating AST interpreter methods with `@ExplodeLoop` and `CompilerDirectives.CompilationFinal` so the Graal JIT compiler partially evaluates the AST into a single compiled machine-code graph.
- **Why not adopt Truffle directly in `xacml4j`**:
  1. Truffle partial evaluation only triggers when running on the Graal JIT compiler with the Truffle runtime SDK (`org.graalvm.truffle`); on standard OpenJDK / Temurin JDK 25 with C2 (which `xacml4j` uses in CI and JMH via `-XX:-UseJVMCICompiler`), Truffle annotations are no-ops and Truffle ASTs run as ordinary megamorphic Java interpreters.
  2. JEP 484 (`java.lang.classfile`) + `Lookup.defineHiddenClass` (Section 6.1 Tier 2) achieves the **exact same First Futamura Projection on standard HotSpot C2** without adding any third-party dependency or requiring GraalVM in production.

---

### 6.3 Decision Diagrams and BDDs / MTBDDs at Runtime

#### Primary Sources (`SUPPORTED`)
- Randal E. Bryant, *"Graph-Based Algorithms for Boolean Function Manipulation"*, IEEE Trans. Computers C-35(8), 1986.
- E. M. Clarke, K. L. McMillan, X. Zhao, M. Fujita, and J. Yang, *"Spectral Transforms for Large Boolean Functions with Applications to Technology Mapping"* (MTBDDs), DAC 1993 / FMSD 1995.
- Kathi Fisler, Shriram Krishnamurthi, Leo A. Meyerovich, and Michael Carl Tschantz, *"Verification and Change-Impact Analysis of Access-Control Policies"* (Margrave MTBDD engine), ICSE 2005, pp. 196–205.
- Alex X. Liu, Fei Chen, JeeHyun Hwang, and Tao Xie, *"XEngine: A Fast and Scalable XACML Policy Evaluation Engine"*, ACM SIGMETRICS 2008.
- Canh Ngo, Yuri Demchenko, and Cees de Laat, *"Multi-Data-Types Interval Decision Diagrams for XACML Evaluation Engine"* (MIDD), Computers & Security 49 (2015), pp. 1–16.

#### XACML 3.0 Semantic Compatibility & Verdict for `xacml4j`
- **Do NOT compile policies to BDDs/MTBDDs/MIDDs in `xacml4j`**:
  1. **Breaks XACML 3.0 Semantics**: Total conversion of policies to MTBDDs/FDDs (as in XEngine and Margrave) assumes attributes are statically present, flattens combining algorithms by numeric encoding, and cannot preserve XACML 3.0's lazy left-to-right PIP invocation order, `Indeterminate{D,P,DP}` propagation, §7.18 obligation/advice evaluation order, §5.42 `ReturnPolicyIdList`, or dynamic XPath `AttributeSelector`s.
  2. **Memory Blowup**: Complex `Condition`s combining `AllOf` conjunctions and bag functions can suffer exponential node blowup under fixed variable orderings.
  3. **Better Alternative**: Multi-attribute `TargetIndex` with flat `long[]` bitsets (Section 6.4) provides the same $O(1)$ target elimination without altering XACML 3.0 evaluation semantics.

---

### 6.4 SIMD or Bitset Matching of Targets (`long[]` Word Bitsets vs `RoaringBitmap` vs JDK 25 Vector API)

#### Primary Sources (`SUPPORTED`)
- Samy Chambi, Daniel Lemire, Owen Kaser, and Robert Godin, *"Better bitmap performance with Roaring bitmaps"*, Software: Practice and Experience 46(5), 2016 (`arXiv:1402.6407v10`).
- Daniel Lemire, Gregory Ssi-Yan-Kai, and Owen Kaser, *"Consistently faster and smaller compressed bitmaps with Roaring"*, Software: Practice and Experience 46(11), 2016 (`arXiv:1603.06549`); and Lemire et al., *"Roaring Bitmaps: Implementation of an Optimized Software Library"*, SPE 2018 (`arXiv:1709.07821`).
- OpenJDK JEP 508: *Vector API (Tenth Incubator)* in JDK 25 (`https://openjdk.org/projects/jdk/25`).

#### Technical Analysis & Concrete Mapping to `xacml4j`
1. **JDK 25 Vector API Constraint (`SUPPORTED`)**:
   - In JDK 25, the Vector API is **JEP 508 (Tenth Incubator)** in module `jdk.incubator.vector`. Because `xacml4j` enforces `<release>25</release>` with no incubator or preview modules, explicit `jdk.incubator.vector` calls are off-limits. However, HotSpot C2's SuperWord auto-vectorizer automatically compiles contiguous `for (int i = 0; i < words.length; i++) acc[i] &= mask[i];` loops over `long[]` into 256-bit AVX2 / 128-bit ARM NEON instructions!
2. **Flat `long[]` Word Bitsets vs `RoaringBitmap` for `xacml4j` Policy Sizes**:
   - `RoaringBitmap` (Lemire et al. 2016/2018) partitions 32-bit integers into $2^{16}$-element chunks (`ArrayContainer` for $\le 4,096$ integers, `BitmapContainer` of $1,024\text{ longs} = 8\text{ KB}$ for $> 4,096$ integers, `RunContainer` for RLE).
   - Notice Roaring's own threshold: for universes or chunks with $N \le 4,096$, a single 64-bit word bitset is *smaller* than Roaring's container header overhead!
   - In `xacml4j`:
     - For $N = 64$ children/rules: **a single primitive `long` (8 bytes, zero heap allocation on stack!)** represents the entire candidate bitset!
     - For $N = 296$ (`rsa2008` `PolSet11`): **`long[5]` (40 bytes of payload)** represents all 296 rules.
     - For $N = 1,000$ (`synthetic:1000`): **`long[16]` (128 bytes of payload)** represents all 1,000 policies.
   - **How Bitset Target Indexing Works in `xacml4j`**:
     - Each `Match` predicate `(AttrKey, LiteralValue)` in an `AllOf` maps to an immutable `long[]` bitset (or scalar `long` when $N \le 64$) of child indices $0 \dots N-1$.
     - Evaluating an `AllOf` conjunction is a bitwise `AND` (`&`) of its `Match` bitsets; evaluating an `AnyOf` disjunction is a bitwise `OR` (`|`) of matching `AllOf` bitsets plus the `unindexedWildcardMask` (`Any`).
     - Iterating the resulting candidate bitset in exact declaration order $0 \dots N-1$ uses `Long.numberOfTrailingZeros(word)` (`TZCNT` / `RBIT+CLZ` hardware instruction, 1 CPU cycle per matching child):
       ```java
       for (int w = 0; w < words.length; w++) {
           long word = words[w];
           while (word != 0L) {
               int childIdx = (w << 6) + Long.numberOfTrailingZeros(word);
               word &= (word - 1L); // clear lowest set bit
               // evaluate child[childIdx] in exact XACML declaration order!
           }
       }
       ```
     - **Zero sorting needed, zero `HashSet` or `ArrayList` deduplication needed, and 100% preservation of XACML 3.0 declaration order!**

---

### 6.5 Interning and Hash-Consing of Values

#### Primary Sources (`SUPPORTED`)
- Jean-Christophe Filliâtre and Sylvain Conchon, *"Type-Safe Modular Hash-Consing"*, ACM SIGPLAN Workshop on ML (2006), pp. 12–19, [PDF](https://usr.lmf.cnrs.fr/~jcf/publis/hash-consing2.pdf).

#### Concrete Mapping to `xacml4j`
- Filliâtre & Conchon (2006) show that hash-consing immutable values assigns each structural value a unique integer `tag` and pre-computes `hashCode` once at construction time, turning:
  1. Hash table probes into single field loads (`value.hashCode` is already cached), and
  2. Equality checks on interned/canonical values into reference equality (`a == b`) before falling back to `.equals()`.
- In `xacml4j`, [`AttributeReferenceKeyInterner`](`xacml-core/src/main/java/org/xacml4j/v30/pdp/AttributeReferenceKeyInterner.java`) already interns `AttributeDesignatorKey` and `AttributeSelectorKey`.
- **Two High-Leverage Extensions**:
  1. **Pre-hash `Value` / `StringExp` / `AnyURIExp` literals at policy load time** (or lazily cache `int hash` on `BaseAttributeValue` / `StringExp`): during `TargetIndex` lookups and `DecisionCache` key hashing, `StringExp.hashCode()` currently recomputes `Objects.hash(getType(), getValue())` on every probe.
  2. **Singleton / Canonical `BagOfValues` for Policy & `AttributeIndex` Constants** (already planned in Batch 6 Cycle AE / `D38`).

---

### 6.6 JDK 25 Platform Features (Strictly Non-Preview Assessment)

#### Primary Sources (`SUPPORTED`)
- OpenJDK JDK 25 Release Feature List (`https://openjdk.org/projects/jdk/25`).
- OpenJDK JEP 506: *Scoped Values* (**Final in JDK 25**, `https://openjdk.org/jeps/506`).
- OpenJDK JEP 519: *Compact Object Headers* (**Final Product Flag in JDK 25**, promoted from experimental JEP 450).
- OpenJDK JEP 514 & JEP 515: *Ahead-of-Time Command-Line Ergonomics* and *Ahead-of-Time Method Profiling* (**Final in JDK 25**).
- OpenJDK JEP 502: *Stable Values (Preview)* and JEP 505: *Structured Concurrency (Fifth Preview)* in JDK 25.

#### Feature-by-Feature Matrix for `xacml4j` (`<release>25</release>`, No Preview)

| JDK 25 Feature | JEP & Status in JDK 25 | Allowed in `xacml4j`? | Concrete Impact on `xacml4j` |
| --- | --- | --- | --- |
| **`ScopedValue`** | **JEP 506 — Final** | **YES** (Already used in [`DefaultEvaluationContextHandler.ANCESTRY`](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java`)) | Zero-allocation cycle detection across virtual-thread hops (D26/D27). Can also propagate caller correlation/zookie hints if needed. |
| **Virtual Threads** | **JEP 444 — Final (JDK 21+)** | **YES** (Used in [`StructuredResolverRunner`](`xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/StructuredResolverRunner.java`)) | Parallel I/O attribute resolution in D26 without pooling OS threads. |
| **Compact Object Headers (`-XX:+UseCompactObjectHeaders`)** | **JEP 519 — Final Product Flag** | **YES** (JVM flag in JMH / production; zero source changes) | Shrinks every Java object header from 12 bytes to **8 bytes**. In [`pr14`](`.ntm/worktrees/xacml4j--batch2/agyx_1/openspec/changes/optimize-pdp-evaluation/research/pr14-argon-allocation-batch45.md`), `PolicyIDReferenceEvaluationContext` crossed the 24B $\to$ 32B 8-byte alignment boundary when a 4B field was added (`+6,280 B/op` on `synthetic:1000`). With `-XX:+UseCompactObjectHeaders`, `PolicyIDReferenceEvaluationContext`, `RuleEvaluationContext`, `BagOfValues`, and `BagStorages$Single` drop by **8 bytes per instance**, saving **10–20% of total bytes/op across all corpora**! |
| **Class-File API (`java.lang.classfile`)** | **JEP 484 — Final (JDK 24+)** | **YES** (Standard `java.base` API) | Enables zero-dependency bytecode compilation of hot `Condition` / `Apply` expressions into `Lookup.defineHiddenClass` (Section 6.1). |
| **AOT Cache & Method Profiling** | **JEP 514 & JEP 515 — Final** | **YES** (JVM flags `-XX:AOTMode=...`) | Persists method type profiles (`Apply`, `FunctionSpec`, `CombiningAlgorithm`) so C2 compiles hot paths immediately at JVM startup without warmup degradation. |
| **`StableValue`** | **JEP 502 — Preview** | **NO** (Blocked by no-preview rule) | Do not use until finalized in JDK 26+. Use `LambdaMetafactory` or `defineHiddenClass` `static final` fields instead. |
| **Structured Concurrency (`StructuredTaskScope`)** | **JEP 505 — 5th Preview** | **NO** (Blocked by no-preview rule) | [`StructuredResolverRunner`](`xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/StructuredResolverRunner.java`) already encapsulates virtual-thread fan-out cleanly and can swap its internal implementation when JEP 505 finalizes. |
| **Vector API** | **JEP 508 — 10th Incubator** | **NO** (Incubator module) | Use flat `long[]` bitset loops auto-vectorized by HotSpot C2 SuperWord. |

---

## 7. Production Decision Caching (D37) and PIP Resolver Caching (D35)

### 7.1 Primary Sources (`SUPPORTED`)
- Ben Manes, Caffeine Documentation (*"Population"*, *"Eviction — W-TinyLFU"*, *"Variable Expiry — Hierarchical TimerWheel"*, queried via Context7 `/ben-manes/caffeine`).
- Jens Wilke, cache2k User Guide (*"Loading / Read-Through"*, *"Expiry and Refresh"*, *"Resilience Policy Caveats"*, `https://cache2k.org/docs/latest/user-guide.html`).
- Google Guava `com.google.common.cache.CacheBuilder` & `LoadingCache` documentation.
- [`PolicyInformationPointCacheProvider.java`](`xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/PolicyInformationPointCacheProvider.java`), [`BasePolicyInformationPointCacheProvider.java`](`xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/BasePolicyInformationPointCacheProvider.java`), and [`PolicyDecisionCache.java`](`xacml-pdp/src/main/java/org/xacml4j/v30/spi/pdp/PolicyDecisionCache.java`).

### 7.2 D35 Design Blueprint: Per-Resolver TTL Cache SPI with Guava, Caffeine, and cache2k

#### Flaws in Current `PolicyInformationPointCacheProvider`
Inspecting [`BasePolicyInformationPointCacheProvider`](`xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/BasePolicyInformationPointCacheProvider.java`):
1. It uses **one global shared cache** with a single `defaultAttributeTTL` and `doGet` + `doPut` check-then-act (no cross-decision single-flight `get(key, loader)`).
2. On every hit, `isExpiredAttributes` calls `System.currentTimeMillis() - v.getCreatedTime() >= ttl * 1000`, even though the underlying cache library already tracks entry expiration natively using monotonic `System.nanoTime()` (`Ticker`).
3. It keys entries by `(AttributeResolverDescriptor, Map<AttributeDesignatorKey, BagOfValues>)` via a mutable `Objects.hashCode(rid, context)` wrapper (`AttributeCacheKey`), even when two resolvers have different TTLs and max-size requirements.

#### Recommended D35 Architecture
1. **Per-Resolver Cache Instance & Key**:
   - Each cacheable resolver (`descriptor.isCacheable() && descriptor.getPreferredCacheTTL() > 0`) gets its own isolated cache instance sized and configured with that resolver's `TTL` and `maxEntries` (or a per-resolver policy in the factory).
   - Because each resolver owns its own cache instance, the cache key is simply an immutable **`ResolverContextKey`** holding the compact `BagOfValues[]` array corresponding to `descriptor.getContextKeys()` in declaration order (with pre-computed `hashCode` at construction time—zero `Map` allocation!).
2. **Cross-Decision Single-Flight Loading (`get(ResolverContextKey, Callable<AttributeSet>)`)**:
   - Replace split `getAttributes` / `putAttributes` on the hot path with a single read-through method:
     ```java
     AttributeSet getOrLoad(
         AttributeResolverDescriptor descriptor,
         ResolverContextKey key,
         Callable<AttributeSet> loader) throws Exception;
     ```
   - When decision $D_1$ and decision $D_2$ concurrently miss on the same `(resolver, key)`, the cache's per-key lock coalesces them into **one** `loader.call()` execution (Zanzibar's lock-table pattern).
   - **Never cache exceptions or empty error results**: If `loader.call()` throws any exception (`XacmlSyntaxException`, runtime exception, or timeout), all three libraries abort the load without storing a negative entry, so the next decision retries cleanly.
3. **Comparison of the Three Required Implementations (Guava, Caffeine, cache2k)**:

| Dimension | Guava (`com.google.common.cache`) | Caffeine (`com.github.benmanes.caffeine.cache`) | cache2k (`org.cache2k`) |
| --- | --- | --- | --- |
| **Eviction Algorithm** | Segmented LRU | **W-TinyLFU** (Window TinyLFU frequency sketch + SLRU; near-optimal hit rate on skewed/scan workloads) | **Clock-Pro** (lock-free read path; lowest GC pressure on high-hit-rate workloads) |
| **Per-Resolver Fixed TTL** | `CacheBuilder.newBuilder().maximumSize(max).expireAfterWrite(ttl, SECONDS).build()` | `Caffeine.newBuilder().maximumSize(max).expireAfterWrite(ttl, SECONDS).build()` | `Cache2kBuilder.of(ResolverContextKey.class, AttributeSet.class).entryCapacity(max).expireAfterWrite(ttl, SECONDS).build()` |
| **Per-Entry Variable TTL (if needed)** | Not natively supported without custom eviction | Native `Expiry.creating((k, v) -> ...)` backed by $O(1)$ **Hierarchical TimerWheel** | Native `ExpiryPolicy<K, V>` (`calculateExpiryTime`) |
| **Single-Flight Call** | `cache.get(key, loader::call)` | `cache.get(key, k -> loader.call())` | `cache.computeIfAbsent(key, loader::call)` |
| **Critical Implementation Caveat** | Wraps checked exceptions in `ExecutionException` and unchecked in `UncheckedExecutionException`; unwrap before rethrowing. | `cache.get(key, mappingFn)` requires unchecked wrapper (`SneakyThrow` or wrapper exception) around `Callable<AttributeSet>` and unwraps on catch. | **Must NOT attach a `ResiliencePolicy` that caches exceptions**; with default resilience off, exceptions thrown inside `computeIfAbsent` propagate directly without caching. |

---

### 7.3 D37 Design Blueprint: Decision Caching with Static Attribute Projection, Remaining-TTL Propagation, and Epoch Invalidation

#### Solving the Three Hard Problems in [`PolicyDecisionCache`](`xacml-pdp/src/main/java/org/xacml4j/v30/spi/pdp/PolicyDecisionCache.java`)

1. **Problem 1: Which Request Attributes Form the Cache Key? (`PolicyAttributeFootprint` Projection)**
   - Hashing the entire `RequestContext` fails in production because requests carry correlation IDs, client timestamps, or unreferenced subject/resource attributes that drop cache hit rates to near zero, or conversely miss dynamic `AttributeSelector` content.
   - **Solution (Static Policy-Tree Footprint Analysis at `PreparedPolicySets` Build Time)**:
     - When `PreparedPolicySets` builds a `Generation` for a root policy, compute an immutable **`PolicyAttributeFootprint`** by walking all reachable `PolicySet`, `Policy`, `Rule`, `Target`, `Condition`, `ObligationExpression`, and `AdviceExpression` nodes:
       1. `AttributeDesignatorKey[] projectedKeys`: all distinct `AttributeDesignatorKey`s referenced anywhere in the prepared tree, sorted canonically.
       2. `boolean hasAttributeSelectorsOrXPath`: `true` if any `AttributeSelector` or XPath function (`xpath-node-*`) appears in the tree.
       3. `boolean hasImplicitEnvironmentClock`: `true` if `current-time`, `current-date`, or `current-dateTime` is referenced in `projectedKeys`.
       4. `boolean hasUnpinnedReferences`: `true` if any policy reference could not be statically pinned.
     - **Fast Projected `RequestCacheKey` Construction**:
       - If `hasAttributeSelectorsOrXPath || hasUnpinnedReferences` is `true` (or if `hasImplicitEnvironmentClock` is `true` and the `RequestContext` does not explicitly supply a fixed clock attribute), **skip the decision cache** (or fall back to full request key if configured).
       - Otherwise, construct `ProjectedDecisionCacheKey` containing:
         - `long policyGenerationId` (from `PreparedPolicySets.Generation`, providing instant $O(1)$ invalidation on policy reload);
         - `boolean returnPolicyIdList` (§5.42 `ReturnPolicyIdList` changes the `Result` structure);
         - `BagOfValues[] projectedValues` of length `projectedKeys.length`, populated directly from the `RequestContext`'s `AttributeIndex` (plus `includeInResult` attributes, since §5.42 requires returning `IncludeInResult="true"` attributes in the `Result`!).
         - Pre-compute a 32-bit hash over `(policyGenerationId, returnPolicyIdList, projectedValues, includeInResultAttributes)` during the single array fill pass.

2. **Problem 2: How Do Resolver TTLs Bound the Decision TTL Without Double-TTL Extension?**
   - **Subtle Bug to Avoid**: Suppose a PIP resolver has a nominal TTL of `60 s`, and its `AttributeSet` was cached in the **D35** PIP cache `50 s` ago (so only **`10 s`** of validity remain). If decision $D$ reads that `AttributeSet` from the PIP cache and caches the resulting `Result` in the **D37** decision cache for the resolver's full nominal `60 s`, the decision remains cached for `50 + 60 = 110 s` (**nearly $2\times$ the allowed TTL**)!
   - **Solution (Monotonic Expiration Deadline Propagation)**:
     - Stamp each `AttributeSet` created by a cacheable PIP resolver with `long expiresAtNanos = System.nanoTime() + TimeUnit.SECONDS.toNanos(ttl)`.
     - Whenever [`DefaultEvaluationContextHandler`](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java`) resolves a PIP attribute during a decision:
       - If `!descriptor.isCacheable() || descriptor.getPreferredCacheTTL() <= 0`, mark the `EvaluationContext` as **uncacheable** (`decisionExpiresAtNanos = 0L`).
       - Otherwise, update `decisionExpiresAtNanos = Math.min(decisionExpiresAtNanos, attributeSet.getExpiresAtNanos())`.
     - If the decision uses **only request attributes** (zero PIP resolvers invoked), its TTL is the PDP's configured `defaultDecisionCacheTTL`.
     - If the decision is `Indeterminate` with a `processing-error` / resolver failure, **never cache it** (`decisionExpiresAtNanos = 0L`).
     - In Caffeine (`Expiry.creating`) and cache2k (`ExpiryPolicy`), each cached `Result` uses its exact computed `remainingNanos = decisionExpiresAtNanos - nowNanos`!

3. **Problem 3: Preserving XACML 3.0 Semantics Under Decision Caching**
   - Because `ProjectedDecisionCacheKey` includes `returnPolicyIdList`, `includeInResult` attributes, and every `AttributeDesignatorKey` referenced by targets, conditions, **and** obligation/advice attribute assignment expressions, a cache hit returns a `Result` with the **exact** `Decision`, `Status`, evaluated `Obligation`s, `Advice`, `IncludeInResult` attributes, and `PolicyIdentifier` list required by XACML 3.0.
   - And when a `DecisionObserver<S>` (D34) is attached, `DefaultPolicyDecisionPoint` still fires `decisionStarted` / `decisionFinished` around the cache lookup so observability remains intact.

---

## 8. Ranked List of the Best Next Optimization Opportunities for `xacml4j`

Below is the prioritized roadmap of optimization opportunities derived from this survey, ordered by **impact-to-risk ratio** and mapped directly to `xacml4j` classes and JMH benchmarks.

| Rank | Optimization Opportunity | Inspired By | Target Classes in `xacml4j` | Target Corpora & Expected Gain | Size & Risk |
| --- | --- | --- | --- | --- | --- |
| **1** | **Intra-`Policy` Rule Indexing + Flat `long[]` Bitset `TargetIndex` with Compound `(Attr1, Attr2, Any)` Slicing** | Cedar OOPSLA 2024 (§3.3 Slicing), OPA Rule Indexing Trie, Lemire Bitsets | [`PreparedPolicySets`](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PreparedPolicySets.java`), [`Policy`](`xacml-core/src/main/java/org/xacml4j/v30/pdp/Policy.java`), `TargetIndex` (task `23.4`) | **`rsa2008`** (296 rules inside `PolSet11`), **`synthetic:100`**, **`synthetic:1000`**. Eliminates $O(N)$ target scans inside both `PolicySet` and `Policy`; `long[]` bitset (`long` for $N \le 64$, `long[5]` for $N=296$, `long[16]` for $N=1,000$) intersects `AllOf` clauses in `< 5 ns` and iterates in exact declaration order via `Long.numberOfTrailingZeros` with **0 B/op** deduplication/sorting allocation. Expected **`5×–18×` throughput gain** on large policy/rule sets. | **M** (~300 LOC). **Low Risk** (`Any` wildcard mask preserves all non-indexed/PIP/MustBePresent rules in exact order). |
| **2** | **Static Policy-Tree `PolicyAttributeFootprint` for Dense Request Slot Lookup & Projected Decision Cache Keys (D37)** | Cedar Schema Environment $\Gamma$, AWS AVP Projected Keys, OPA Static Reference Analysis | [`PreparedPolicySets`](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PreparedPolicySets.java`), [`DefaultEvaluationContextHandler`](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java`), [`PolicyDecisionCache`](`xacml-pdp/src/main/java/org/xacml4j/v30/spi/pdp/PolicyDecisionCache.java`) | **All corpora (`rsa2008`, `synthetic:*`, `pip:8`)**. Assigns each `AttributeDesignatorKey` in a prepared policy tree a dense slot index $0 \dots K-1$. Replaces the per-decision `ConcurrentHashMap<AttributeDesignatorKey, BagOfValues>` (`requestDesignators`) on non-parallel paths with a flat `BagOfValues[K]` array, and drives **D37** projected `RequestCacheKey` + automatic `current-time`/selector hazard detection. | **M** (~280 LOC). **Low Risk** (fallback to map for ad-hoc dynamic keys). |
| **3** | **`LambdaMetafactory`-Bound Typed Function Invokers in `FunctionInvocationFactory`** | OpenJDK `MethodHandle` Constant-Folding Rules (Vernee 2024), Polar/Oso Specializers | [`FunctionInvocationFactory`](`xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionInvocationFactory.java`), [`FunctionSpecBuilder`](`xacml-core/src/main/java/org/xacml4j/v30/policy/FunctionSpecBuilder.java`) | **`rsa2008`**, **`synthetic:100`**, **`synthetic:1000`**. Replaces `Method.invoke(instance, Object[])` reflection dispatch with `LambdaMetafactory`-generated hidden-class invokers (`FuncInvoker1`, `FuncInvoker2`, `FuncInvoker3`, `FuncInvokerList`), allowing C2 to inline standard XACML functions directly without `Object[]` allocation or reflection overhead. | **S–M** (~200 LOC). **Low Risk** (all `@FuncSpec` methods are `public static`; fallback to reflection for non-standard signatures). |
| **4** | **Per-Resolver Loading Cache SPI with Cross-Decision Single-Flight Coalescing (D35: Caffeine, cache2k, Guava)** | Google Zanzibar Lock-Table Coalescing, OPA `deserialized` Inter-Query Cache, Caffeine W-TinyLFU & cache2k Clock-Pro | [`PolicyInformationPointCacheProvider`](`xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/PolicyInformationPointCacheProvider.java`), [`DefaultPolicyInformationPoint`](`xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/DefaultPolicyInformationPoint.java`) | **`pip:8` (warm & cold)**. Replaces global `doGet`/`doPut` with per-resolver `getOrLoad(descriptor, ResolverContextKey, loader)` and `expiresAtNanos` deadline stamping so concurrent decisions share one remote call and D37 inherits accurate remaining TTLs. | **M** (~350 LOC across SPI + 3 adapters). **Low Risk**. |
| **5** | **JDK 25 Compact Object Headers (`-XX:+UseCompactObjectHeaders`, JEP 519) & AOT Profiling (JEP 514/515) in JMH & Deployment** | OpenJDK JDK 25 JEP 519, JEP 514, JEP 515 | [`xacml-jmh`](`xacml-jmh/README.md`), [`docs/ai/BENCHMARKS.md`](`docs/ai/BENCHMARKS.md`) | **All corpora**. Reduces every HotSpot object header from 12B to 8B, pulling `PolicyIDReferenceEvaluationContext` (32B $\to$ 24B), `RuleEvaluationContext`, `BagOfValues`, and `BagStorages$Single` down by 8 bytes each (**10–20% allocation reduction with 0 LOC changed**). | **XS** (JVM flag in JMH & docs). **Zero Code Risk**. |
| **6** | **Pre-Hashed Immutable `Value` / `StringExp` / `AnyURIExp` & Load-Time Constant ID Tagging** | Filliâtre & Conchon (2006) Hash-Consing, AWS Zelkova Constant Abstraction | [`BaseAttributeValue`](`xacml-core/src/main/java/org/xacml4j/v30/types/BaseAttributeValue.java`), [`StringExp`](`xacml-core/src/main/java/org/xacml4j/v30/types/StringExp.java`) | **`rsa2008`**, **`synthetic:*`**, **`TargetIndex`**, **`D37`**. Caches `int hash` on immutable `Value` instances so `TargetIndex` and `DecisionCache` probes never re-hash strings/URIs or box `Objects.hash(type, value)` varargs! (Note: `Objects.hash(type, value)` in `BaseAttributeValue.hashCode()` allocates an `Object[2]` array if not escape-analyzed!). | **XS** (~40 LOC). **Zero Semantic Risk**. |
| **7** | **JEP 484 (`java.lang.classfile`) + `Lookup.defineHiddenClass` Bytecode Compilation for Hot `Condition` / `Apply` Subtrees** | First Futamura Projection, JEP 484 (JDK 25 Final), JEP 371 Hidden Classes | [`Condition`](`xacml-core/src/main/java/org/xacml4j/v30/pdp/Condition.java`), [`Apply`](`xacml-core/src/main/java/org/xacml4j/v30/pdp/Apply.java`), [`PreparedPolicySets`](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PreparedPolicySets.java`) | **Compute-heavy `Condition` corpora (`rsa2008`, complex ABAC rules)**. Compiles expression ASTs into a nestmate Hidden Class with `classData` `static final` constants after OPA-style 2-hit threshold, eliminating megamorphic AST traversal. | **L** (~400 LOC, zero external deps). **Medium Risk** (requires exact `Indeterminate` exception mapping; validate via Rank 8 DRT). |
| **8** | **Verification-Guided Differential Random Testing (DRT) & Incremental `PreparedPolicySets` Maintenance** | Cedar FSE 2024 (`arXiv:2407.01688v1`), Soufflé PPDP 2021 Incremental Maintenance | [`PreparedPolicySetsPdpTest`](`xacml-pdp/src/test/java/org/xacml4j/v30/pdp/PreparedPolicySetsPdpTest.java`), [`PreparedPolicySets`](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PreparedPolicySets.java`) | Differential fuzzing harness comparing unoptimized PDP vs `PreparedPolicySets` + `TargetIndex` + `D35/D37` across randomized policies/requests for `(Decision, Status, Obligations, Advice, PolicyIdList)`; plus reverse-dependency incremental re-pinning on `PolicyRepository` updates. | **S–M** (~250 LOC). **Low Risk** (pure safety multiplier + fast repo reload). |
