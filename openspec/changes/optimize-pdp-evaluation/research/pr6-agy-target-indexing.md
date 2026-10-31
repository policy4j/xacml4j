<!-- Lane PR6 of the second profiling swarm (after cycle H), Agy Gemini 4 Argon, read-only; focus: target indexing and literature; report taken from the Agy conversation database, paths made repository-relative. -->

# PR6 Report: Target Indexing, Decision Diagrams, and Combining Semantics After Cycle H

## Baseline Arithmetic & Literature/Engine Context (`SUPPORTED`)

### 1. Where Time and Allocation Go on `synthetic:1000` After Cycle H (Runs 019–021)
From [`LEDGER.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/LEDGER.md) (Run 020, after Cycle H):
- `synthetic:1000, request=first-deny` (1 policy evaluated, short-circuit on policy 0): **`1.084 ± 0.024 us/op`**, **`4,078 ± 124 B/op`**.
- `synthetic:1000` full-scan requests (1,000 child [`PolicyIDReference`](xacml-core/src/main/java/org/xacml4j/v30/policy/PolicyIDReference.java#L40-L195) entries evaluated under [`DenyOverrides.doCombine`](xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DenyOverrides.java#L69-L119)):
  - `first-permit`: `127.2 ± 4.04 us/op`, `172,000 ± 142 B/op`
  - `middle-permit`: `132.8 ± 4.29 us/op`, `172,100 ± 87 B/op`
  - `last-permit`: `128.7 ± 4.35 us/op`, `172,100 ± 83 B/op`
  - `last-deny`: `129.7 ± 4.47 us/op`, `172,100 ± 85 B/op`
  - `unknown-notapplicable` (0 matches out of 1,000): `127.7 ± 7.26 us/op`, `170,300 ± 143 B/op`
- `synthetic:1000, request=all` (7-request mix averaging $5,500 / 7 = 785.7$ policy evaluations/decide): **`100.4 ± 2.18 us/op`**, **`135,900 ± 69 B/op`**.
- `rsa2008, request=all` (9 policy sets, 17 requests): **`8.93 ± 0.29 us/op`**, **`18,770 ± 55 B/op`**.

Subtracting the 1-policy fixed cost (`1.084 us`, `4,078 B`) from the 1,000-policy scan (`127.7–129.7 us`, `170,300–172,100 B`):
- **Marginal cost per non-matching policy after Cycle H**: **`126.6–128.6 ns/policy`** and **`166.2–168.0 B/policy`** (`99.1%` of full-scan time and `97.6%` of full-scan allocation).
- In [`top-alloc-synthetic_1000-all.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/021-profile-decide-cycle-H/alloc-synthetic_1000-all/top-alloc-synthetic_1000-all.md) (`25,066` total samples; `24,781` = `98.9%` inside [`DenyOverrides.doCombine`](xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DenyOverrides.java#L69-L119)), the **`160 B`** allocated per non-matching policy is accounted for by 7 exact sites summing to **`97.7%`** of all sampled allocations:
  1. [`Policy.createContext`](xacml-core/src/main/java/org/xacml4j/v30/policy/Policy.java#L168-L170) $\to$ `new PolicyDescendantEvaluationContext` (`24–32 B`): `4,666` samples (**`18.6%`**)
  2. [`FunctionSpecBuilder$FunctionSpecImpl.invoke`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java#L416-L423) $\to$ `ArrayList` backing `Object[2]` (`24 B`): `3,607` samples (**`14.4%`**)
  3. [`PolicyIDReference.createContext`](xacml-core/src/main/java/org/xacml4j/v30/policy/PolicyIDReference.java#L80-L93) $\to$ `new PolicyIDReferenceEvaluationContext` (`24 B`): `3,551` samples (**`14.2%`**)
  4. [`DefaultFunctionInvocation.invoke`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java#L87-L98) $\to$ `params.toArray()` `Object[2]` (`24 B`): `3,468` samples (**`13.8%`**)
  5. [`Target.match`](xacml-core/src/main/java/org/xacml4j/v30/policy/Target.java#L69-L86) $\to$ `SingletonImmutableList.iterator()` `Iterators$9` (`24 B`): `3,453` samples (**`13.8%`**)
  6. [`FunctionSpecBuilder$FunctionSpecImpl.invoke`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java#L416-L423) $\to$ `new ArrayList<>(2)` (`24 B`): `3,404` samples (**`13.6%`**)
  7. [`DefaultEvaluationContextHandler.resolve`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java#L172-L180) $\to$ `Optional.of(bag)` (`16 B`): `2,330` samples (**`9.3%`**)

### 2. Survey of Literature and Engines on Target Indexing & Decision Diagrams
1. **XEngine (Liu, Chen, Hwang, Xie — SIGMETRICS 2008; IEEE/ACM ToN 2011)** and **Multi-Data-Type Interval Decision Diagrams / MIDD (Ngo, Demchenko, de Laat — *Computers & Security* 49, 2015)** and **Prezta (`arXiv:2607.11466v2`)**:
   - Convert XACML `<Target>` equality/interval clauses into an attribute-keyed decision diagram (FDD / MTBDD / MIDD) where each internal node resolves one `(Category, AttributeId, DataType)` attribute once and branches via a hash/interval edge to the subset of candidate child policies/rules in original policy order.
   - **Critical XACML 3.0 compliance gap in pure XEngine numericalization**: XEngine's original numericalization assumes 2-valued attributes present in the request and collapses XACML 3.0's 4-valued extended indeterminate lattice (`Indeterminate{D}`, `Indeterminate{P}`, `Indeterminate{DP}`, XACML 3.0 §7.12–7.15 & Appendix C). Ngo et al. (2015) and Turkmen et al. show that preserving XACML 3.0 semantics requires tracking both **candidate matching policies** and **candidate indeterminate policies** (when `MustBePresent="true"` or a PIP/function throws an evaluation error) in original child index order.
2. **SBA-XACML (Mourad & Jebbaoui, 2015)** and **Poliseek (2022)**:
   - Partition a composite rule's children at load time into an **inverted equality index** `(AttributeDesignatorKey -> Map<Value, BitSet/int[]>)` plus an **unindexed wildcard/residual bitset** for children with empty targets or non-equality predicates (`regexp-match`, `<`, `>`).
3. **Production Java Engines (OW2 AuthzForce Core PDP, AT&T XACML, WSO2 Balana)**:
   - **OW2 AuthzForce**: Resolves static `PolicyIdReference` / `PolicySetIdReference` once at policy-load time (`StaticRefPolicyProvider`) so runtime combining never queries a concurrent repository map or checks cyclic chains for static graphs; pre-binds fixed-arity `FirstOrderFunctionCall` evaluators; and in combining algorithms, groups/orders rules by effect (`Permit` vs `Deny`) where the combining algorithm is commutative (`deny-overrides`, `permit-overrides`, `deny-unless-permit`, `permit-unless-deny`), while preserving declaration order for `first-applicable` and `ordered-*`.
   - **WSO2 Balana & AT&T XACML**: Do not build an inverted target hash index inside `PolicySet`; they evaluate child targets sequentially via `MatchResult` before constructing child evaluation contexts or evaluating rules.

### 3. What Is Safe Under XACML 3.0 Combining Semantics (`SUPPORTED`)
Let a [`PolicySet`](xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySet.java#L54-L429) (or [`Policy`](xacml-core/src/main/java/org/xacml4j/v30/policy/Policy.java#L56-L404)) have children $C_0, C_1, \dots, C_{n-1}$ where each child $C_i$ has a `<Target>` containing at least one conjunctive equality [`Match`](xacml-core/src/main/java/org/xacml4j/v30/policy/Match.java#L45-L270) clause (`MatchAllOf` of `*-equal` on an [`AttributeDesignator`](xacml-core/src/main/java/org/xacml4j/v30/policy/AttributeDesignator.java#L43-L250) $A$ and literal [`Value`](xacml-core/src/main/java/org/xacml4j/v30/Value.java) $v_i$).
- **XACML 3.0 §7.6 (Target Evaluation) & §7.7 (Match Evaluation)**:
  - If attribute $A$ resolves in the [`EvaluationContext`](xacml-core/src/main/java/org/xacml4j/v30/EvaluationContext.java) without error to a non-empty [`BagOfAttributeExp`](xacml-core/src/main/java/org/xacml4j/v30/BagOfAttributeExp.java) $B = \{u_1, \dots, u_k\}$ (or an empty bag when `MustBePresent="false"`), and for child $C_i$ the literal $v_i \notin B$:
    - Standard equality functions (`string-equal`, `integer-equal`, `anyURI-equal`, etc., XACML 3.0 §A.3.1) are total and never return `Indeterminate` on valid typed values.
    - Therefore, `Match.match` is guaranteed to return `MatchResult.NOMATCH`, `Target.match` is guaranteed to return `MatchResult.NOMATCH` (for single-`AnyOf` conjunctive equality targets, or when no `AnyOf` branch matches $B$ and no preceding `AnyOf`/`AllOf` branch in $C_i$ references an attribute that errors or has `MustBePresent="true"` on an empty bag), and $C_i$ is guaranteed to evaluate to `Decision.NOT_APPLICABLE` (XACML 3.0 §7.12, §7.13).
- **Invariance Across All Standard Combining Algorithms (XACML 3.0 Appendix C)**:
  - In every standard XACML 3.0 and 2.0 combining algorithm — `deny-overrides` (§C.2), `ordered-deny-overrides` (§C.3), `permit-overrides` (§C.4), `ordered-permit-overrides` (§C.5), `deny-unless-permit` (§C.6), `permit-unless-deny` (§C.7), `first-applicable` (§C.8), `only-one-applicable` (§C.9), `on-permit-apply-second` — **`Decision.NOT_APPLICABLE` is the identity element**: skipping a child known to produce `NOT_APPLICABLE` without Side Effects does not change `atLeastOnePermit`, `atLeastOneErrorD/P/DP`, short-circuit order, staged obligations/advice (§7.18), or `ReturnPolicyIdList` (§5.42, since only applicable policies contribute to the evaluated policy ID list, or in `xacml4j` [`BaseCompositeDecisionRule.evaluate`](xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java#L194-L216) where `addEvaluationResult` is only called after `isMatch(context) == MatchResult.MATCH` at line 213!).
- **Crucial Verification in `xacml4j`**:
  - Look at [`BaseCompositeDecisionRule.evaluate`](xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java#L168-L215):
    - When `isMatch(context)` returns `NOMATCH` (lines 176–182), it **does not** call `context.addEvaluationResult(this, r)`, **does not** evaluate obligations or advice, and **does not** mutate any state on `RootEvaluationContext` (except `DefaultEvaluationContextHandler` caching the resolved request designator bag on the first policy, which an index lookup on attribute $A$ performs identically on the first lookup!).
  - Look at [`SyntheticCorpus.java`](xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/SyntheticCorpus.java#L127-L163):
    - The root `PolicySet` has 1,000 child `PolicyIDReference` elements under `deny-overrides`.
    - Every child policy $i \in [0, 999]$ has a `<Target>` with one `MatchAnyOf` $\to$ one `MatchAllOf` $\to$ one `Match` with `string-equal`, literal `"dept-" + i`, and `AttributeDesignator(Category=ACCESS_SUBJECT, AttributeId="urn:xacml4j:jmh:department", DataType=STRING, MustBePresent=false)`.
    - Every request has 1 `"urn:xacml4j:jmh:department"` value (`"dept-0"`, `"dept-500"`, `"dept-999"`, or `"dept-unknown"`).

---

## Ranked Hypotheses (Most Valuable First)

### H1. Equality Target Index (Decision-Diagram / Inverted Attribute Index) on `PolicySet` and `Policy`
- **Evidence (`SUPPORTED`)**:
  - In [`021-profile-decide-cycle-H/cpu-synthetic_1000-all/top-cpu-synthetic_1000-all.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/021-profile-decide-cycle-H/cpu-synthetic_1000-all/top-cpu-synthetic_1000-all.md) (`10,124` total samples) and collapsed stacks (`task-82`):
    - [`PolicySet.evaluate`](xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySet.java#L172-L176) $\to$ [`DenyOverrides.doCombine`](xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DenyOverrides.java#L69-L119) accounts for **`96.3%` CPU** (`9,754 / 10,124` samples) and **`98.9%` allocation** (`24,781 / 25,066` samples).
    - Inside `DenyOverrides.doCombine`, `999` of `1,000` iterations on `first-permit`, `middle-permit`, `last-permit`, `last-deny`, and `1,000` of `1,000` on `unknown-notapplicable` evaluate non-matching policies via [`PolicyIDReference.createContext`](xacml-core/src/main/java/org/xacml4j/v30/policy/PolicyIDReference.java#L80-L93) (**`15.5%` CPU**, **`32.8%` alloc**) and [`PolicyIDReference.evaluate`](xacml-core/src/main/java/org/xacml4j/v30/policy/PolicyIDReference.java#L101-L108) $\to$ [`Target.match`](xacml-core/src/main/java/org/xacml4j/v30/policy/Target.java#L69-L86) (**`72.7%` CPU**, **`65.7%` alloc**).
  - In [`LEDGER.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/LEDGER.md) Run 020:
    - Evaluating 1 policy (`first-deny`) costs **`1.084 us`** and **`4,078 B`**.
    - Evaluating 1,000 policies (`first-permit` / `middle-permit` / `last-permit` / `last-deny` / `unknown-notapplicable`) costs **`127.2–132.8 us`** and **`170,300–172,100 B`**.
- **Change**:
  - Introduce an immutable, load-time/lazy-initialized `TargetIndex` on [`PolicySet`](xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySet.java#L54-L165) (and optionally [`Policy`](xacml-core/src/main/java/org/xacml4j/v30/policy/Policy.java#L56-L155) for rules) inspired by XEngine / MIDD (Ngo et al. 2015) and SBA-XACML:
    1. **Index construction (built once per `(PolicySet, PolicyReferenceResolver)` or lazily on first evaluation once references are resolved)**:
       - Inspect each child $C_i$ ($0 \le i < n$) in declaration order. If $C_i$ is a [`PolicyIDReference`](xacml-core/src/main/java/org/xacml4j/v30/policy/PolicyIDReference.java#L40-L195) or [`PolicySetIDReference`](xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySetIDReference.java#L38-L185), resolve its target via `context.resolve(ref)` (see H2 for caching resolved references when the repository is immutable/version-matched).
       - A child $C_i$'s [`Target`](xacml-core/src/main/java/org/xacml4j/v30/policy/Target.java#L41-L130) is **equality-indexable on key $K = (\text{Category}, \text{AttributeId}, \text{DataType}, \text{Issuer})$** if its first `MatchAnyOf` has a single `MatchAllOf` whose first `Match` uses standard `*-equal` on an `AttributeDesignator` with key $K$ and literal `Value` $v_i$ (or if every `MatchAllOf` in `MatchAnyOf` shares key $K$).
       - Build an immutable `Map<Value, int[]>` mapping each literal `Value` $v$ to the sorted array of child indices $i$, plus an `int[] unindexedIndices` for children with `Target == null`, empty `AnyOf`, non-equality predicates (`regexp`, `<`, `>`), `AttributeSelector`, or different leading keys, and a `boolean hasMustBePresent` flag.
    2. **Candidate selection at evaluation time**:
       - Before the combining loop over `List<CompositeDecisionRule> rules`, if the `PolicySet` has a single-attribute equality `TargetIndex` on key $K$:
         - Resolve $K$ once against `context`: `BagOfAttributeExp bag = designator.evaluate(context)`.
         - **Case 1 (Normal fast path)**: `bag` resolves without `EvaluationException` and is non-empty:
           - If `bag.size() == 1` and `unindexedIndices.length == 0` (the common case, including `synthetic:100` and `synthetic:1000`): look up `int[] candidates = index.get(bag.valueAt(0))` (1 `HashMap.get`). Pass only those candidate children (0 or 1 child in `synthetic:1000`, in original declaration order) to the combining algorithm!
           - If `bag.size() > 1` or `unindexedIndices.length > 0`: merge the sorted `int[]` posting lists in declaration order (small $k$-way merge or bitset) so original order is strictly preserved.
         - **Case 2 (Empty bag)**:
           - If `bag.isEmpty()`, only children with `MustBePresent="true"` on $K$ (which evaluate to `Indeterminate`) and `unindexedIndices` can return anything other than `NOT_APPLICABLE`. If `hasMustBePresent == false` and `unindexedIndices.length == 0`, return `Decision.NOT_APPLICABLE` immediately!
         - **Case 3 (`EvaluationException` / `Indeterminate` on $K$)**:
           - Fall back to the full child list in declaration order so every child's `ExtendedIndeterminate` (`INDETERMINATE_D/P/DP`) is combined per XACML 3.0 §7.14.
- **Expected gain**:
  - **`synthetic:1000` full-scan requests (`first-permit`, `middle-permit`, `last-permit`, `last-deny`, `unknown-notapplicable`)**:
    - Replaces 1,000 policy context creations + 1,000 target evaluations with **1 attribute resolution + 1 `HashMap.get(Value)` + at most 1 candidate policy evaluation** (`0` candidates for `unknown-notapplicable`, `1` candidate for `first-permit`, `middle-permit`, `last-permit`, `last-deny`).
    - Time drops from **`127.2–132.8 us/op`** to **`~1.05–1.15 us/op`** on `first/middle/last-permit` and `last-deny` (matching `first-deny` at `1.084 us/op`), and to **`~0.45–0.60 us/op`** on `unknown-notapplicable`: a **~115x–200x speedup** (**`-126 to -131 us/op`**, **`-99.1%` time**).
    - Allocation drops from **`170,300–172,100 B/op`** to **`~4,078 B/op`** (1 match) and **`~1,600 B/op`** (`unknown-notapplicable`): **`-168 KB/op`** (**`-97.6%` to `-99.0%` bytes**).
  - **`synthetic:1000, request=all`**:
    - Time drops from **`100.4 us/op`** to **`~1.00–1.15 us/op`** (**`-99.3 us/op`**, **`-98.9%`**).
    - Allocation drops from **`135,900 B/op`** to **`~3,700–4,100 B/op`** (**`-131.8 KB/op`**, **`-97.0%`**).
  - **`synthetic:100, request=all`**:
    - Time drops from **`10.64 us/op`** to **`~1.00–1.15 us/op`** (**`-9.5 us/op`**, **`-89%`**); allocation from **`17,750 B/op`** to **`~3,700–4,100 B/op`** (**`-77%`**).
- **Risk**:
  - **XACML 3.0 semantics**:
    - Preserves declaration order (`ordered-deny-overrides` §C.3, `ordered-permit-overrides` §C.5, `first-applicable` §C.8) because candidate `int[]` arrays are stored and iterated in ascending child index order.
    - Preserves `OnlyOneApplicable` (§C.9) because all matching or indeterminate candidates are visited.
    - **Exception**: `on-permit-apply-second` (XACML 3.0 Profile for Hierarchical/Combiner Parameters, [`OnPermitApplySecondPolicyCombiningAlgorithm`](xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/OnPermitApplySecondPolicyCombiningAlgorithm.java)) depends on positional index (1st child vs 2nd child vs 3rd child); only enable `TargetIndex` for standard set/sequence combining algorithms where `NOT_APPLICABLE` is an identity element (`DenyOverrides`, `PermitOverrides`, `FirstApplicable`, `DenyUnlessPermit`, `PermitUnlessDeny`, `OnlyOneApplicable`, `LegacyDenyOverrides`, `LegacyPermitOverrides`), or pass the filtered candidate view via a method on `PolicySet` / `DecisionCombiningAlgorithm`.
    - Preserves `MustBePresent="true"` (§7.3.5) and `Indeterminate{D/P/DP}` (§7.14) via Cases 2 and 3 above.
    - Note on custom stateful `EvaluationContextHandler` implementations or custom `PolicyReferenceResolver`: if a reference cannot be resolved or throws `PolicyResolutionException` during evaluation, Case 3 or unindexed fallback preserves exact exception/indeterminate behavior.
  - **Thread safety**: `TargetIndex` is immutable (`final` fields, `Map.copyOf` / `int[]`), published safely via `volatile` lazy init or constructor initialization.
  - **API change**: Non-breaking if internal to [`PolicySet`](xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySet.java) / [`Policy`](xacml-core/src/main/java/org/xacml4j/v30/policy/Policy.java) and built-in [`DecisionCombiningAlgorithm`](xacml-core/src/main/java/org/xacml4j/v30/DecisionCombiningAlgorithm.java) implementations.
  - **Verification tests**: `Xacml30ConformanceTest`, `Xacml20ConformanceTest`, `RSA2008InteropTest`, `SyntheticCorpus` checks in [`PdpFixture.check`](xacml-jmh/src/main/java/org/xacml4j/jmh/harness/PdpFixture.java#L312-L325), plus targeted unit tests with `MustBePresent="true"`, multi-valued request bags, `Indeterminate` PIP errors, and `first-applicable` / `deny-overrides` ordering.
- **Checked / not checked**:
  - Checked [`PolicySet.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySet.java), [`Target.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/Target.java), [`Match.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/Match.java), [`DenyOverrides.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DenyOverrides.java), [`SyntheticCorpus.java`](xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/SyntheticCorpus.java), and all 7 standard combining algorithms.

---

### H2. Pre-Resolve or Cache `PolicyIDReference` / `PolicySetIDReference` Resolution and Eliminate Redundant `isReferenceTo` Re-Check
- **Evidence (`SUPPORTED`)**:
  - In [`021-profile-decide-cycle-H/cpu-synthetic_1000-all/top-cpu-synthetic_1000-all.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/021-profile-decide-cycle-H/cpu-synthetic_1000-all/top-cpu-synthetic_1000-all.md) and [`top-alloc-synthetic_1000-all.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/021-profile-decide-cycle-H/alloc-synthetic_1000-all/top-alloc-synthetic_1000-all.md):
    - [`PolicyIDReference.createContext`](xacml-core/src/main/java/org/xacml4j/v30/policy/PolicyIDReference.java#L80-L93) is **`15.5%` inclusive CPU** (`1,573 / 10,124` samples) and **`32.8%` inclusive allocation** (`8,217 / 25,066` samples = **`14.2%`** for `PolicyIDReferenceEvaluationContext` + **`18.6%`** for `PolicyDescendantEvaluationContext`).
    - Inside `PolicyIDReference.createContext`, [`DefaultPolicyReferenceResolver.resolve`](xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/DefaultPolicyReferenceResolver.java#L65-L92) $\to$ `ConcurrentHashMap.get` takes **`7.9%` inclusive CPU** (`795 / 10,124`, `6.6%` self in `ConcurrentHashMap.get`) plus context-chain delegation (`RootEvaluationContext.resolve` `2.1%` self CPU).
    - Immediately after `PolicyIDReference.createContext` resolves `Policy resolved` and wraps it in `PolicyDescendantEvaluationContext`, [`PolicyIDReference.evaluate`](xacml-core/src/main/java/org/xacml4j/v30/policy/PolicyIDReference.java#L101-L108) (line 106) calls `isReferenceTo(context.getCurrentPolicy())` $\to$ [`BaseCompositeDecisionRuleIDReference.matches`](xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRuleIDReference.java#L104-L108) (`this.id.equals(id) && version.match(v)`), which costs another **`3.1%` CPU** (`312 / 10,124` samples, including `1.7%` in `String.equals`) just to re-verify that the context created 2 lines earlier by `d.createContext(context)` belongs to `this`!
- **Change**:
  1. **Eliminate redundant `isReferenceTo` check when evaluating via `PolicyIDReference` / `PolicySetIDReference`**:
     - In [`PolicyIDReference.evaluate`](xacml-core/src/main/java/org/xacml4j/v30/policy/PolicyIDReference.java#L101-L108) and [`PolicyIDReference.isMatch`](xacml-core/src/main/java/org/xacml4j/v30/policy/PolicyIDReference.java#L96-L99) (and [`PolicySetIDReference`](xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySetIDReference.java#L94-L106)), check `context.getParentContext() instanceof PolicyIDReferenceEvaluationContext refCtx && refCtx.getReference() == this` (pointer equality `==` in 1 instruction!) before falling back to `isReferenceTo(p)` (which does string/version comparison).
  2. **Cache resolved `Policy` / `PolicySet` on `PolicyIDReference` / `PolicySetIDReference` (or in `DefaultPolicyReferenceResolver` by identity) and defer context allocation until target match**:
     - Following OW2 AuthzForce's `StaticRefPolicyProvider` design: [`DefaultPolicyReferenceResolver`](xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/DefaultPolicyReferenceResolver.java#L41-L93) already caches `ConcurrentMap<PolicyIDReference, Policy> resolvedPolicies` unconditionally for the lifetime of the PDP! If `PolicyIDReference` caches a single `@Stable` / `volatile` `(PolicyReferenceResolver/RootPolicy, Policy)` pair or `RootEvaluationContext` exposes the resolver directly without walking the `DescendantEvaluationContext` parent chain, lookup drops from `ConcurrentHashMap.get` + `PolicyIDReference.equalsTo` (`String.equals` + 3 `Objects.equal`) to a direct field read.
     - Furthermore, when evaluating a non-matching policy reference, why allocate **two** context objects (`PolicyIDReferenceEvaluationContext` + `PolicyDescendantEvaluationContext` = `48–56 B`, **`32.8%`** of all allocations in `021/alloc-synthetic_1000-all`) before [`Target.match`](xacml-core/src/main/java/org/xacml4j/v30/policy/Target.java#L69-L86) even runs?
     - Check what `Target.match(context)` actually uses on `context`:
       - `AttributeDesignator.evaluate(context)` $\to$ `context.resolve(AttributeDesignatorKey)` (delegates straight to `RootEvaluationContext`!).
       - `AttributeSelector.evaluate(context)` $\to$ `context.resolve(AttributeSelectorKey)` (reads `context.getCurrentPolicy().getVariableDefinitions()`? No — `XPath` namespace mappings come from `getCurrentPolicy().getNamespaceContext()` only when `AttributeSelector` is used!).
       - `FunctionSpecImpl.invoke(context, ...)` $\to$ `context.isValidateFuncParamsAtRuntime()` (delegates straight to `RootEvaluationContext`!).
       - Therefore, for any `Target` that contains only `AttributeDesignator`s (no `AttributeSelector`, which can be a `boolean hasAttributeSelector` flag on [`Target`](xacml-core/src/main/java/org/xacml4j/v30/policy/Target.java#L41-L67)), `Target.match(parentContext)` produces the **exact same `MatchResult`** as `Target.match(childContext)` without allocating `PolicyIDReferenceEvaluationContext` or `PolicyDescendantEvaluationContext` unless the target returns `MATCH`!
- **Expected gain (standalone, even without H1; and complementary with H1 when multiple candidates or unindexed targets are evaluated)**:
  - On `synthetic:1000` full scan (without H1):
    - Skipping `PolicyIDReferenceEvaluationContext` + `PolicyDescendantEvaluationContext` allocation on `NOMATCH` removes **`32.8%` of all allocations** (`48 B/policy` = **`48 KB/op`** on full scan, **`37.7 KB/op`** on `synthetic:1000-all`).
    - Skipping `ConcurrentHashMap.get` + `isReferenceCyclic` + `isReferenceTo` + 2 levels of `DescendantEvaluationContext` delegation saves **`15.5%` (`createContext`) + `3.1%` (`isReferenceTo`) + `~6%` (2 fewer context delegation hops in `Match.match`) = `~24.6%` CPU** (**`~31 us/op`** on full scan, **`~24.7 us/op`** on `synthetic:1000-all`).
- **Risk**:
  - **XACML 3.0 semantics**:
    - Cyclic reference detection (`isReferenceCyclic`, [`PolicyIDReference.java:149-163`](xacml-core/src/main/java/org/xacml4j/v30/policy/PolicyIDReference.java#L149-L163)): a `Policy` (unlike a `PolicySet`) cannot contain `PolicyIDReference` or `PolicySetIDReference` children in XACML 2.0/3.0! Thus `PolicyIDReference.isReferenceCyclic` can **never** be true (a `Policy` is always a leaf composite rule). Even for `PolicySetIDReference`, cyclic check and `context.resolve(ref)` still run if `Target` matches or if we resolve `Policy` first before allocating the two wrapper contexts.
    - If `Target` contains an `AttributeSelector` (XPath), it needs `context.getCurrentPolicy()` / `getCurrentPolicySet()` for XPath namespace context (`RootEvaluationContext.resolve(AttributeSelectorKey)`). Gating the context-free target check on `!target.hasAttributeSelector()` preserves XPath namespace resolution 100%.
  - **Thread safety**: Safe; `hasAttributeSelector` is an immutable `boolean` computed in `Target` constructor.
  - **API change**: Non-breaking (internal to `DecisionCombiningAlgorithms` / `DenyOverrides` / `PolicyIDReference` / `Policy`).
  - **Verification tests**: `PolicyIDReferenceTest`, `PolicySetTest`, `Xacml30ConformanceTest` (including `IIC*` / `IIA*` / `III*` XPath selector and policy reference tests).
- **Checked / not checked**:
  - Checked [`PolicyIDReference.java:80-163`](xacml-core/src/main/java/org/xacml4j/v30/policy/PolicyIDReference.java#L80-L163), [`PolicySetIDReference.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySetIDReference.java), [`DescendantEvaluationContext.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/DescendantEvaluationContext.java), [`RootEvaluationContext.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java).

---

### H3. Direct Binary Predicate Fast Path for [`Match.match`](xacml-core/src/main/java/org/xacml4j/v30/policy/Match.java#L115-L154) and 2-Arg [`FunctionSpec.invoke`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java#L415-L425) (Eliminating `ArrayList(2)` + Two `Object[2]` + Reflection on Target/Condition Calls)
- **Evidence (`SUPPORTED`)**:
  - In [`021-profile-decide-cycle-H/alloc-synthetic_1000-all/top-alloc-synthetic_1000-all.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/021-profile-decide-cycle-H/alloc-synthetic_1000-all/top-alloc-synthetic_1000-all.md):
    - Every [`Match.match`](xacml-core/src/main/java/org/xacml4j/v30/policy/Match.java#L121) call (`predicate.invoke(context, value, v)`) hits [`FunctionSpecBuilder$FunctionSpecImpl.invoke(EvaluationContext, Expression...)`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java#L415-L425), which allocates:
      1. `new ArrayList<Expression>(params.length)` (`24 B`, **`13.6%`** of alloc samples = `3,404 / 25,066`)
      2. `ArrayList` backing `Object[2]` (`24 B`, **`14.4%`** of alloc samples = `3,607 / 25,066`)
      3. Inside [`DefaultFunctionInvocation.invoke`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java#L96), `params.toArray()` allocates a second `Object[2]` (`24 B`, **`13.8%`** of alloc samples = `3,468 / 25,066`).
    - Together, [`FunctionSpecBuilder$FunctionSpecImpl.invoke`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java#L378-L425) accounts for **`41.8%` of all allocations** (`10,479 / 25,066`, `72 B/match`) and **`23.9%` of inclusive CPU** (`2,423 / 10,124`) in `synthetic:1000-all`!
  - In [`021-profile-decide-cycle-H/alloc-rsa2008-all/top-alloc-rsa2008-all.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/021-profile-decide-cycle-H/alloc-rsa2008-all/top-alloc-rsa2008-all.md) and [`top-cpu-rsa2008-all.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/021-profile-decide-cycle-H/cpu-rsa2008-all/top-cpu-rsa2008-all.md):
    - `FunctionSpecBuilder$FunctionSpecImpl.invoke` is **`52.9%` of all allocations** (`20,152 / 38,059`) and **`56.7%` of all CPU** (`5,766 / 10,176` samples)!
    - Note why Cycle D missed `Match.match`: Cycle D added `invoke(EvaluationContext, List<Expression>)` for `FunctionInvocation`, which allocates `new Object[params.size()]` in `evaluateParameters`, whereas `Match.match` calls `FunctionSpec.invoke(EvaluationContext, Expression, Expression)` (or varargs `Expression...` at [`FunctionSpecBuilder.java:415`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java#L415-L425)), which still builds `new ArrayList<>(2)` and calls `evaluateInPlace` + `DefaultFunctionInvocation.invoke`!
- **Change**:
  1. Add a 2-argument overload ` <T extends Value> T invoke(EvaluationContext context, Expression arg0, Expression arg1)` on [`FunctionSpec`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpec.java) (with a `default` method delegating to varargs for binary compatibility) and implement it in [`FunctionSpecBuilder$FunctionSpecImpl`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java#L378-L425) without allocating `ArrayList` or `Object[]` when the function is a standard binary predicate or binary function.
  2. Even better (and specific to XACML `<Match>` semantics, XACML 3.0 §7.7): in [`Match`](xacml-core/src/main/java/org/xacml4j/v30/policy/Match.java#L115-L154), both `value` (`AttributeExp`) and `v` (`Value` from `BagOfAttributeExp`) are **already evaluated `Value` instances** (`AttributeExp` is a leaf `Value`: `AttributeExp.evaluate(context)` simply returns `this`!).
     - Moreover, for the 14 standard XACML equality predicates (`*:string-equal`, `*:integer-equal`, `*:boolean-equal`, `*:anyURI-equal`, `*:date-equal`, `*:time-equal`, `*:dateTime-equal`, `*:dayTimeDuration-equal`, `*:yearMonthDuration-equal`, `*:rfc822Name-equal`, `*:x500Name-equal`, `*:hexBinary-equal`, `*:base64Binary-equal`), their implementation in `EqualFunctions.java` is literally `BooleanExp.valueOf(a.equals(b))`!
     - Pre-classify standard equality `FunctionSpec` instances at `Match` construction time (or bind a non-allocating `(EvaluationContext, AttributeExp, AttributeExp) -> BooleanExp` lambda/`BiPredicate` on `FunctionSpecImpl`):
       - In `Match.match`, if `predicate` is a standard equality function and parameter types were already validated at `Match` build time (see [`Match.java:68-82`](xacml-core/src/main/java/org/xacml4j/v30/policy/Match.java#L68-L82), which already validates `predicate.validateParametersAndReturnType` in the constructor!), `Match.match` can evaluate `value.equals(v)` directly — **zero allocations, zero `ArrayList`, zero `Object[]`, zero `Method.invoke`, zero `isValidateFuncParamsAtRuntime` context walks**!
- **Expected gain**:
  - **On `synthetic:1000` (per non-matching policy / without H1)**:
    - Eliminates `72 B/policy` (**`41.8%`** of all allocations in `021/alloc-synthetic_1000-all` = **`-72 KB/op`** on full scan, **`-56.8 KB/op`** on `synthetic:1000-all`).
    - Eliminates **`23.9%` CPU** (`FunctionSpecImpl.invoke`) = **`-30.4 us/op`** on full scan, **`-24.0 us/op`** on `synthetic:1000-all`.
  - **On `rsa2008, request=all`**:
    - `Target.match` is `10.7%` alloc / `11.8%` CPU, and `Condition.evaluate` is `53.8%` alloc / `56.5%` CPU (where 2-arg `FunctionInvocation` / `FunctionSpec.invoke` accounts for `52.9%` alloc and `56.7%` CPU).
    - Expected saving on `rsa2008-all`: **`~1.5–2.2 us/op`** (**`17%–25%` of `8.93 us`**) and **`~3.5–5.5 KB/op`** (**`19%–29%` of `18.77 KB`**).
- **Risk**:
  - **XACML 3.0 semantics**: [`Match`](xacml-core/src/main/java/org/xacml4j/v30/policy/Match.java#L68-L82) already validates `(attributeValue, attributeRef)` against `predicate` in its constructor. At runtime, `attributeRef.evaluate(context)` returns a `BagOfAttributeExp` whose elements are guaranteed to match `attributeRef.getEvaluatesTo()` (`DataType`). For standard total equality functions (`EqualFunctions`), `value.equals(v)` is 100% semantically identical to reflective invocation.
  - **Thread safety**: Stateless.
  - **API change**: Non-breaking (`default` method on `FunctionSpec` if added to the interface, or package-private/internal fast path on `Match` / `FunctionSpecImpl`).
  - **Verification tests**: `MatchTest`, `EqualFunctionsTest`, `Xacml30ConformanceTest`, `RSA2008InteropTest`.
- **Checked / not checked**:
  - Checked [`Match.java:60-154`](xacml-core/src/main/java/org/xacml4j/v30/policy/Match.java#L60-L154), [`FunctionSpecBuilder.java:378-425`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java#L378-L425), [`DefaultFunctionInvocation.java:87-98`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java#L87-L98).

---

### H4. Replace `ImmutableList<MatchAnyOf>` / `ImmutableList<MatchAllOf>` / `ImmutableList<Match>` in [`Target`](xacml-core/src/main/java/org/xacml4j/v30/policy/Target.java#L41-L86), [`MatchAnyOf`](xacml-core/src/main/java/org/xacml4j/v30/policy/MatchAnyOf.java#L42-L98), and [`MatchAllOf`](xacml-core/src/main/java/org/xacml4j/v30/policy/MatchAllOf.java#L45-L94) with Indexed `List.copyOf` Loops (Eliminating `Iterators$9`)
- **Evidence (`SUPPORTED`)**:
  - In [`021-profile-decide-cycle-H/alloc-synthetic_1000-all/top-alloc-synthetic_1000-all.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/021-profile-decide-cycle-H/alloc-synthetic_1000-all/top-alloc-synthetic_1000-all.md):
    - `com.google.common.collect.Iterators$9` (allocated by `SingletonImmutableList.iterator()` when a collection has size 1) is the **#5 allocation site overall**: `3,453 / 25,066` samples (**`13.8%`** of all allocations in `synthetic:1000-all`, `24 B` per policy!).
    - In [`021-profile-decide-cycle-H/alloc-synthetic_1000-first-deny/top-alloc-synthetic_1000-first-deny.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/021-profile-decide-cycle-H/alloc-synthetic_1000-first-deny/top-alloc-synthetic_1000-first-deny.md), `Iterators$9` is **`8.0%`** of all allocations (`5,297 / 66,379` samples).
    - In [`021-profile-decide-cycle-H/alloc-rsa2008-all/top-alloc-rsa2008-all.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/021-profile-decide-cycle-H/alloc-rsa2008-all/top-alloc-rsa2008-all.md), `Iterators$9` is **`6.4%`** of all allocations (`2,426 / 38,059` samples).
  - Why it still happens after Cycle E:
    - Cycle E changed [`MatchAnyOf.matches`](xacml-core/src/main/java/org/xacml4j/v30/policy/MatchAnyOf.java#L46-L77) and [`MatchAllOf.matches`](xacml-core/src/main/java/org/xacml4j/v30/policy/MatchAllOf.java#L47-L76) to indexed `for (int i = 0; i < matches.size(); i++)` loops, **but [`Target.java:44-86`](xacml-core/src/main/java/org/xacml4j/v30/policy/Target.java#L44-L86) still stores `private final Collection<MatchAnyOf> matches = ImmutableList.copyOf(b.anyOf)` and iterates `for (Matchable m : matches)`**! Every single-`AnyOf` `Target` is backed by Guava `SingletonImmutableList`, which allocates `new Iterators$9` on every enhanced-for loop!
- **Change**:
  - In [`Target.java:44-86`](xacml-core/src/main/java/org/xacml4j/v30/policy/Target.java#L44-L86):
    - Change `private final Collection<MatchAnyOf> matches` to `private final List<MatchAnyOf> matches = List.copyOf(b.anyOf);`.
    - In [`Target.match(EvaluationContext)`](xacml-core/src/main/java/org/xacml4j/v30/policy/Target.java#L69-L86), replace `for(Matchable m : matches)` with an indexed loop `for (int i = 0, n = matches.size(); i < n; i++) { MatchAnyOf m = matches.get(i); ... }`.
    - Also replace `ImmutableList.copyOf` in [`MatchAnyOf.java:50`](xacml-core/src/main/java/org/xacml4j/v30/policy/MatchAnyOf.java#L50) and [`MatchAllOf.java:51`](xacml-core/src/main/java/org/xacml4j/v30/policy/MatchAllOf.java#L51) with `List.copyOf(...)` so callers of `getAnyOf()` / `getAllOf()` / `getMatches()` do not trigger `SingletonImmutableList` iterator allocations either.
- **Expected gain**:
  - Eliminates **`13.8%`** of allocations on `synthetic:1000-all` (`24 B/policy` = **`-24 KB/op`** on full scan, **`-18.8 KB/op`** on `synthetic:1000-all`), **`8.0%`** of allocations on `synthetic:1000-first-deny` (**`-326 B/op`**), and **`6.4%`** of allocations on `rsa2008-all` (**`-1,200 B/op`**).
- **Risk**:
  - **XACML 3.0 semantics**: Zero semantic change (XACML 3.0 §7.6).
  - **Thread safety**: `List.copyOf` is deeply immutable and thread-safe.
  - **API change**: Non-breaking (`Target.getAnyOf()` returns `Collection<MatchAnyOf>`).
  - **Verification tests**: `TargetTest`, `Xacml30ConformanceTest`, `RSA2008InteropTest`.
- **Checked / not checked**:
  - Checked [`Target.java:44-86`](xacml-core/src/main/java/org/xacml4j/v30/policy/Target.java#L44-L86), [`MatchAnyOf.java:46-77`](xacml-core/src/main/java/org/xacml4j/v30/policy/MatchAnyOf.java#L46-L77), [`MatchAllOf.java:47-76`](xacml-core/src/main/java/org/xacml4j/v30/policy/MatchAllOf.java#L47-L76).

---

### H5. Flatten `DescendantEvaluationContext` Delegation Chain & Remove `Optional.of(bag)` Allocation in [`DefaultEvaluationContextHandler.resolve`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java#L172-L185)
- **Evidence (`SUPPORTED`)**:
  - In [`021-profile-decide-cycle-H/alloc-synthetic_1000-all/top-alloc-synthetic_1000-all.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/021-profile-decide-cycle-H/alloc-synthetic_1000-all/top-alloc-synthetic_1000-all.md):
    - [`DefaultEvaluationContextHandler.resolve`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java#L172-L180) allocates `Optional.of(bag)` (`16 B`) on every cached attribute lookup: `2,330 / 25,066` samples (**`9.3%`** of all allocations in `synthetic:1000-all`, `16 KB/op` on full scan) and **`11.5%` inclusive CPU** (`1,165 / 10,124` samples).
    - In [`021-profile-decide-cycle-H/alloc-rsa2008-all/top-alloc-rsa2008-all.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/021-profile-decide-cycle-H/alloc-rsa2008-all/top-alloc-rsa2008-all.md), `DefaultEvaluationContextHandler.resolve` is **`25.6%` inclusive allocation** (`9,753 / 38,059`, with `Optional.of` alone at `1,630` samples = **`4.3%`**) and **`20.1%` inclusive CPU** (`2,041 / 10,176`).
  - In [`021-profile-decide-cycle-H/cpu-synthetic_1000-all/top-cpu-synthetic_1000-all.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/021-profile-decide-cycle-H/cpu-synthetic_1000-all/top-cpu-synthetic_1000-all.md):
    - Every call to `context.resolve(AttributeDesignatorKey)`, `context.resolve(PolicyIDReference)`, and `context.isValidateFuncParamsAtRuntime()` walks 3–4 levels of [`DescendantEvaluationContext`](xacml-core/src/main/java/org/xacml4j/v30/policy/DescendantEvaluationContext.java#L45-L261) via megamorphic `EvaluationContext` interface dispatch (`itable stub` is **`3.9%` self CPU** = `396 / 10,124`; `DescendantEvaluationContext.resolve(AttributeDesignatorKey)` is **`4.2%` self CPU** = `424 / 10,124`; `DescendantEvaluationContext.resolve(PolicyIDReference)` is **`1.7%` self CPU** = `173 / 10,124`; `DescendantEvaluationContext.isValidateFuncParamsAtRuntime` is **`1.5%` self CPU** = `148 / 10,124` — totaling **`11.3%` self CPU** just in `DescendantEvaluationContext` pass-through methods!).
- **Change**:
  1. **Direct root delegation in [`DescendantEvaluationContext`](xacml-core/src/main/java/org/xacml4j/v30/policy/DescendantEvaluationContext.java#L45-L75)**:
     - Store `private final EvaluationContext root;` (resolved in the constructor: `this.root = (parent instanceof DescendantEvaluationContext d) ? d.root : parent;`) and `private final boolean validateFuncParamsAtRuntime = root.isValidateFuncParamsAtRuntime();`.
     - Delegate all root-owned methods (`resolve(AttributeDesignatorKey)`, `resolve(PolicyIDReference)`, `resolve(PolicySetIDReference)`, `isValidateFuncParamsAtRuntime()`, `getTicker()`, `getEffectiveTime()`, `getResolvedDesignators()`) directly to `root` (1 hop instead of $O(\text{depth})$ hops, and monomorphic when `root` is `RootEvaluationContext`).
  2. **Remove `Optional<BagOfAttributeExp>` wrapper in [`DefaultEvaluationContextHandler.resolve`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java#L60-L185)**:
     - `DefaultEvaluationContextHandler.resolve(EvaluationContext, AttributeDesignatorKey)` is a `private` helper method (line 172)! It wraps a non-null `BagOfAttributeExp` in `Optional.of(bag)` solely so line 102 can call `resolve(context, key).orElseGet(...)`.
     - Change `private Optional<BagOfAttributeExp> resolve(...)` to `private BagOfAttributeExp resolve(...)` returning `null` on cache miss (since `BagOfAttributeExp` in `requestDesignators` is never `null`; empty bags are `TypeToBag.empty()`).
- **Expected gain**:
  - Eliminates **`9.3%`** of all allocations on `synthetic:1000-all` (`16 B/policy` = **`-16 KB/op`** on full scan, **`-12.6 KB/op`** on `synthetic:1000-all`) and **`4.3%`** of all allocations on `rsa2008-all` (**`-800 B/op`**).
  - Flattening `DescendantEvaluationContext` to `root` eliminates ~2/3 of the **`11.3%` self CPU** in delegation + `itable stub` = **`~7.5%` CPU** (**`-9.5 us/op`** on full scan, **`-0.5 us/op`** on `rsa2008-all`).
- **Risk**:
  - **XACML 3.0 semantics**: Zero semantic change unless a test mocks an intermediate `EvaluationContext` that intercepts `resolve(AttributeDesignatorKey)` (if `parent instanceof DescendantEvaluationContext d`, `d` is the concrete class in `xacml-core` whose methods are `public` pass-throughs, so `d.root` is guaranteed to have identical behavior even if the outermost parent is a Mockito mock!).
  - **Thread safety**: `root` and `validateFuncParamsAtRuntime` are `final` fields.
  - **API change**: Non-breaking (`DefaultEvaluationContextHandler.resolve` is `private`; `DescendantEvaluationContext` public signatures unchanged).
  - **Verification tests**: `DefaultEvaluationContextHandlerTest`, `RootEvaluationContextTest`, `Xacml30ConformanceTest`.
- **Checked / not checked**:
  - Checked [`DescendantEvaluationContext.java:45-261`](xacml-core/src/main/java/org/xacml4j/v30/policy/DescendantEvaluationContext.java#L45-L261) (none of its subclasses — `PolicyDescendantEvaluationContext`, `PolicySetDescendantEvaluationContext`, `PolicyIDReferenceEvaluationContext`, `PolicySetIDReferenceEvaluationContext`, `RuleEvaluationContext` — override `resolve(AttributeDesignatorKey)`, `resolve(PolicyIDReference)`, or `isValidateFuncParamsAtRuntime()`!) and [`DefaultEvaluationContextHandler.java:99-185`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java#L99-L185).

---

### H6. Effect-Aware Rule & Policy Ordering / Partitioning Under Commutative Combining Algorithms (`deny-overrides`, `permit-overrides`, `deny-unless-permit`, `permit-unless-deny`)
- **Evidence (`SUPPORTED`)**:
  - Literature: XEngine (Liu et al. 2008/2011), SBA-XACML (Mourad & Jebbaoui 2015), and OW2 AuthzForce (`DenyOverridesAlg`, `PermitOverridesAlg`, `DenyUnlessPermitAlg`) partition child `Rule`s by `Effect` (`DENY` vs `PERMIT`).
  - Codebase evidence:
    - In [`DenyOverrides.doCombine`](xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DenyOverrides.java#L69-L119):
      - When a child evaluates to `Decision.DENY`, `DenyOverrides` **short-circuits immediately** at line 77 (`return Decision.DENY`).
      - When a child evaluates to `Decision.PERMIT`, `DenyOverrides` sets `atLeastOnePermit = true` (line 80) and **must continue evaluating all remaining children** to prove no other child evaluates to `DENY` or `INDETERMINATE_D`/`INDETERMINATE_DP`!
    - Look at [`LEDGER.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/LEDGER.md) Run 020:
      - `synthetic:1000, request=first-deny` takes **`1.084 us/op`** (short-circuits at index 0), whereas `synthetic:1000, request=first-permit` takes **`127.2 us/op`** (because policy 0 returns `PERMIT`, and `DenyOverrides` still scans policies 1..999 looking for a `DENY`!).
      - Similarly, inside a [`Policy`](xacml-core/src/main/java/org/xacml4j/v30/policy/Policy.java) using unordered `urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides` (or `1.0:rule-combining-algorithm:deny-overrides`), if `PERMIT` rules precede a matching `DENY` rule, the engine evaluates all the `PERMIT` rules' `<Target>` and `<Condition>` expressions only to discard them when a later `DENY` rule matches.
- **Change**:
  1. **For unordered rule-combining algorithms**:
     - XACML 3.0 §C.2 (`deny-overrides`) and §C.4 (`permit-overrides`) explicitly distinguish unordered `deny-overrides` / `permit-overrides` from `ordered-deny-overrides` (§C.3) and `ordered-permit-overrides` (§C.5):
       - In `xacml4j`, [`DenyOverridesRuleOrderedCombiningAlgorithm`](xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DenyOverridesRuleOrderedCombiningAlgorithm.java) subclasses [`DenyOverrides`](xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DenyOverrides.java) with the exact same `doCombine` loop.
       - Note that [`Rule`](xacml-core/src/main/java/org/xacml4j/v30/policy/Rule.java#L44-L280) in XACML 3.0 has a fixed static `Effect` (`Effect.DENY` or `Effect.PERMIT`) and cannot contain `<ObligationExpressions>` (only `<AdviceExpressions>` in 3.0, and even those are only staged on the winning decision or discarded on `DENY` override). Actually, in `xacml4j`, `Rule.evaluate` stages rule advice into `EvaluationContext` when `Rule` returns `PERMIT` or `DENY`. Wait: in [`BaseCompositeDecisionRule.evaluate`](xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java#L194-L214), when `combineDecision == Decision.DENY`, `Policy` only keeps advice/obligations matching `Effect.DENY`? Let's check: `Rule.evaluate` calls `context.addMatchingAdvices(Effect, ...)` on `PolicyDescendantEvaluationContext`! Wait — if a `PERMIT` rule is evaluated before a `DENY` rule, does `PolicyDescendantEvaluationContext` filter advice by the final `Decision`? Let's check [`BaseCompositeDecisionRule.java:194-214`](xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java#L194-L214): line 206 merges *all* child `matchingObligations` and `matchingAdvices` into parent without re-filtering by the parent's `combineDecision` if a sibling returned the opposite effect! Wait — in XACML 3.0 §7.18, "no obligations or advice SHALL be returned to the PEP if the evaluation of the rule, policy, or policy set does not match the Decision resulting from the combining algorithm." And if `DENY` rules are evaluated *first* under unordered `deny-overrides` (as OW2 AuthzForce does), a matching `DENY` rule short-circuits immediately before any `PERMIT` rule is evaluated!
  2. **Interaction with H1 (`TargetIndex`)**:
     - When H1 (`TargetIndex`) is active on `synthetic:1000`, `first-permit` already only has **1 candidate policy** (`policy-0`, since `policy-1..999` have `dept != "dept-0"`), so H1 alone already reduces `first-permit` from 1,000 policy evaluations to **1 policy evaluation** (`1.084 us`) without needing to reorder policies!
     - Where effect-partitioning adds value *beyond* H1 is when **multiple rules/policies match the same request** (e.g., within a `Policy` in `rsa2008` or multi-rule policies under `deny-overrides`, `permit-overrides`, `deny-unless-permit`, `permit-unless-deny`): partitioning `DENY`-effect rules before `PERMIT`-effect rules in unordered `DenyOverrides<Rule>` and `PermitUnlessDeny<Rule>` (and `PERMIT` before `DENY` in unordered `PermitOverrides<Rule>` and `DenyUnlessPermit<Rule>`) short-circuits on the overriding effect without evaluating rules of the overridden effect.
- **Expected gain**:
  - In policies where multiple rules share a target (such as `rsa2008`'s `XacmlPolicySet-03-N-RPS-med-rec-vrole.xml` and `04-N-PPS-PRD-004.xml` which evaluate multiple rules in `Policy.evaluate`, **`66.6%` inclusive CPU** on `rsa2008-all`), evaluating overriding-effect rules first saves **5%–15% of rule evaluation CPU** on overriding requests.
- **Risk**:
  - **XACML 3.0 semantics**:
    - XACML 3.0 §C.2 (`deny-overrides`) states: *"The order in which the collection of rules is evaluated is not specified"* (whereas §C.3 `ordered-deny-overrides` requires evaluation in declaration order so obligations/advice of earlier rules are deterministic).
    - Therefore, [`DenyOverridesRuleOrderedCombiningAlgorithm`](xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DenyOverridesRuleOrderedCombiningAlgorithm.java) and [`PermitOverridesRuleOrderedCombineAlgorithm`](xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/PermitOverridesRuleOrderedCombineAlgorithm.java) MUST NOT reorder rules, while unordered `DenyOverrides` / `DenyUnlessPermit` / `PermitUnlessDeny` may only reorder if no custom `AuditEvent` or test asserts non-winning rule evaluation order.
  - **Thread safety**: Safe if rule partition is precomputed on `Policy`.
  - **API change**: Non-breaking.
  - **Verification tests**: `Xacml30ConformanceTest`, `RSA2008InteropTest`, `DenyOverridesTest`.
- **Checked / not checked**:
  - Checked [`DenyOverrides.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DenyOverrides.java), [`DenyOverridesRuleOrderedCombiningAlgorithm.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DenyOverridesRuleOrderedCombiningAlgorithm.java), [`BaseCompositeDecisionRule.java:194-215`](xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java#L194-L215).

---

### H7. Precompute `AttributeDesignatorKey` on [`AttributeDesignator`](xacml-core/src/main/java/org/xacml4j/v30/policy/AttributeDesignator.java#L43-L250) and Specialize `RootEvaluationContext` Request Designator Lookup
- **Evidence (`SUPPORTED`)**:
  - In [`021-profile-decide-cycle-H/cpu-synthetic_1000-all/top-cpu-synthetic_1000-all.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/021-profile-decide-cycle-H/cpu-synthetic_1000-all/top-cpu-synthetic_1000-all.md):
    - Inside [`DefaultEvaluationContextHandler.resolve`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java#L172-L180) (`11.5%` inclusive CPU), `requestDesignators.get(key)` spends:
      - `ConcurrentHashMap.get`: **`1.2%` self CPU** (`120 / 10,124`)
      - `AttributeDesignatorKey.equals`: **`1.9%` self CPU** (`188 / 10,124`) + `String.equals` (**`3.8%` self CPU** = `388 / 10,124`, shared with policy ref lookup) + `Category.equals`: **`0.7%` self CPU** (`66 / 10,124`).
      - Why is `requestDesignators` in [`DefaultEvaluationContextHandler.java:60-72`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java#L60-L72) a `ConcurrentHashMap` at all? A `DefaultEvaluationContextHandler` instance is created **per request** inside `DefaultPolicyDecisionPoint.decide`! Furthermore, [`RootEvaluationContext.java:71`](xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java#L71) **also** maintains its own `private final Map<AttributeDesignatorKey, BagOfAttributeExp> resolvedDesignators = new HashMap<>()`, and calls `resolvedDesignators.put(key, v)` on every single `resolve(AttributeDesignatorKey)` call (line 194) **without checking `resolvedDesignators.get(key)` first**!
  - Read [`RootEvaluationContext.java:185-200`](xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java#L185-L200):
    ```java
    public BagOfAttributeExp resolve(AttributeDesignatorKey key) throws EvaluationException {
        Preconditions.checkNotNull(key);
        BagOfAttributeExp v = handler.resolve(this, key);
        ...
        resolvedDesignators.put(key, v);
        return v;
    }
    ```
    Every single attribute evaluation calls `handler.resolve(this, key)` AND then calls `resolvedDesignators.put(key, v)` (re-hashing `key` and writing into `RootEvaluationContext.resolvedDesignators` 1,000 times per decide for the exact same key!).
- **Change**:
  1. In [`RootEvaluationContext.resolve(AttributeDesignatorKey)`](xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java#L185-L200):
     - Check `BagOfAttributeExp v = resolvedDesignators.get(key); if (v != null) { return v; }` **before** calling `handler.resolve(this, key)` and `resolvedDesignators.put(key, v)`!
     - Because `handler.resolve` never returns `null` (it returns `key.getDataType().emptyBag()` when missing), `resolvedDesignators.get(key)` hits on the 2nd..1000th lookup without ever calling `DefaultEvaluationContextHandler.resolve`, without allocating `Optional.of(bag)`, and without calling `resolvedDesignators.put(key, v)` 999 redundant times!
  2. In [`DefaultEvaluationContextHandler.java:60-72`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java#L60-L72), replace `new ConcurrentHashMap<>()` with `new HashMap<>()` (per-request handler, single-threaded unless `evaluateAsync` is used; or keep `HashMap` in `RootEvaluationContext` as the primary cache).
- **Expected gain**:
  - Checking `resolvedDesignators.get(key)` in `RootEvaluationContext.resolve` before `handler.resolve` + `put`:
    - Immediately eliminates 999 out of 1,000 calls to `DefaultEvaluationContextHandler.resolve` and `Optional.of(bag)` on `synthetic:1000`!
    - Saves **`9.3%` of all allocations** (`16 KB/op` on full scan) and **`~7%–8%` of total CPU** (`DefaultEvaluationContextHandler.resolve` + redundant `HashMap.put` in `RootEvaluationContext`).
    - On `rsa2008-all`, where the same subject/resource/action designators are resolved repeatedly across 9 policy sets and dozens of rules/conditions (`DefaultEvaluationContextHandler.resolve` is **`20.1%` inclusive CPU** and **`25.6%` inclusive alloc**), short-circuiting on `resolvedDesignators.get(key)` saves **`~8%–12%` CPU (`0.7–1.1 us/op`)** and **`~4.3%` allocation (`~800 B/op`)**!
- **Risk**:
  - **XACML 3.0 semantics**: XACML 3.0 §7.3.2 & §7.3.5 state that an attribute designator's value in the evaluation context is fixed for the duration of a single authorization decision request. `DefaultEvaluationContextHandler` already caches the resolved bag in `requestDesignators` on the first lookup (`requestDesignators.put(key, v)` at line 127), so returning `resolvedDesignators.get(key)` from `RootEvaluationContext` returns the **exact same `BagOfAttributeExp` reference** that `handler.resolve` returns.
  - **Exception**: Verify whether any unit test mocks `EvaluationContextHandler` and expects `handler.resolve(context, key)` to be invoked multiple times for the same key within one `RootEvaluationContext`.
  - **Thread safety**: `RootEvaluationContext` is already single-request/single-threaded (`resolvedDesignators` is already a `HashMap`).
  - **API change**: Non-breaking.
  - **Verification tests**: `RootEvaluationContextTest`, `DefaultEvaluationContextHandlerTest`, `Xacml30ConformanceTest`, `RSA2008InteropTest`.
- **Checked / not checked**:
  - Checked [`RootEvaluationContext.java:71`](xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java#L71) and [`RootEvaluationContext.java:185-200`](xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java#L185-L200), [`DefaultEvaluationContextHandler.java:60-185`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java#L60-L185).

---

### H8. Multi-Attribute Decision Diagram (MIDD / DAG) for Multi-Clause Targets (`rsa2008` and Multi-Dimension Policies)
- **Evidence (`SUPPORTED`)**:
  - Literature: Ngo, Demchenko, de Laat (*Computers & Security* 49, 2015, "Multi-Data-Type Interval Decision Diagrams for XACML Policy Evaluation") and Liu et al. (XEngine, 2008/2011).
  - In `rsa2008-all` ([`021-profile-decide-cycle-H/cpu-rsa2008-all/top-cpu-rsa2008-all.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/021-profile-decide-cycle-H/cpu-rsa2008-all/top-cpu-rsa2008-all.md)), [`Target.match`](xacml-core/src/main/java/org/xacml4j/v30/policy/Target.java#L69-L86) is **`11.8%` inclusive CPU** (`1,198 / 10,176` samples) and **`10.7%` inclusive allocation** (`4,065 / 38,059` samples). Unlike `synthetic:1000` (where each child policy tests 1 attribute `department`), XACML 2.0/3.0 enterprise policies (like `rsa2008`) have `<Target>`s that test `(subject:role, resource:type, action:id)` or have multiple `<Rule>`s within a `<Policy>` testing distinct `(role, action)` combinations.
- **Change**:
  - Extend H1's `TargetIndex` from a 1-level map on a single `AttributeDesignatorKey` to a 2-to-3 level decision tree / trie (`AttributeDesignatorKey_0 -> Value -> AttributeDesignatorKey_1 -> Value -> int[] candidateIndices`), choosing at policy load time the `AttributeDesignatorKey` with the highest distinct literal cardinality (maximum selectivity) as the root level of the index (standard BDD variable-ordering heuristic).
- **Expected gain**:
  - On `synthetic:1000`: identical to H1 (**`-99%` time and allocation** on full scan).
  - On `rsa2008-all` (where `Target.match` is `11.8%` CPU and `10.7%` alloc across 9 policy sets and their rules): saves **~4%–7% CPU (`0.35–0.65 us/op`)** and **~4%–6% allocation (`0.7–1.1 KB/op`)**.
- **Risk**:
  - Higher implementation complexity than H1's 1-attribute equality index; since `rsa2008-all` spends `56.5%` CPU in `Condition.evaluate` and only `11.8%` in `Target.match`, build H1's 1-attribute most-selective equality index first and only add multi-level branching if multi-attribute target corpora require it.
- **Checked / not checked**:
  - Checked `rsa2008-all` profile shares in `021-profile-decide-cycle-H` and `Rsa2008Corpus.java`.

---

## Summary of Expected Cumulative Impact on `synthetic:1000` and `rsa2008`

| Hypothesis | Target Bottleneck | `synthetic:1000` Full Scan (`128.7 us`, `172.1 KB`) | `synthetic:1000-all` (`100.4 us`, `135.9 KB`) | `rsa2008-all` (`8.93 us`, `18.77 KB`) |
| --- | --- | --- | --- | --- |
| **H1** (Equality Target Index on `PolicySet`/`Policy`) | Skips 999/1,000 non-matching policies | **`~1.1 us` (`-99.1%`)**, **`~4.1 KB` (`-97.6%`)** | **`~1.1 us` (`-98.9%`)**, **`~3.9 KB` (`-97.1%`)** | `~0.2–0.4 us` (`-2% to -4%`) |
| **H2** (Defer child context alloc until Target match + fast `isReferenceTo` + ref cache) | `PolicyIDReference.createContext` (`32.8%` alloc, `15.5%` CPU) + `isReferenceTo` (`3.1%` CPU) | `-31 us` (`-24%`), `-48 KB` (`-28%`) standalone | `-24.7 us` (`-24.6%`), `-37.7 KB` (`-27.7%`) standalone | `-0.1 us`, `-0.2 KB` |
| **H3** (2-arg `FunctionSpec.invoke` & direct equality in `Match.match`) | `FunctionSpecImpl.invoke` (`41.8%` alloc, `23.9%` CPU on synth; `52.9%` alloc, `56.7%` CPU on rsa) | `-30.4 us` (`-24%`), `-72 KB` (`-42%`) standalone | `-24.0 us` (`-24%`), `-56.8 KB` (`-42%`) standalone | **`-1.5 to -2.2 us` (`-17% to -25%`)**, **`-3.5 to -5.5 KB` (`-19% to -29%`)** |
| **H4** (`Target.matches` `List.copyOf` + indexed loop) | `SingletonImmutableList` `Iterators$9` (`13.8%` alloc on synth, `6.4%` on rsa) | `-24 KB` (`-14%`) standalone | `-18.8 KB` (`-13.8%`) standalone | **`-1.2 KB` (`-6.4%`)** |
| **H5 + H7** (`RootEvaluationContext` `resolvedDesignators.get` + flat `root` pointer + drop `Optional.of`) | `DefaultEvaluationContextHandler.resolve` (`9.3%` alloc, `11.5%` CPU on synth; `25.6%` alloc, `20.1%` CPU on rsa) | `-20 us` (`-15.5%`), `-16 KB` (`-9.3%`) standalone | `-15.5 us` (`-15.5%`), `-12.6 KB` (`-9.3%`) standalone | **`-1.0 to -1.5 us` (`-11% to -17%`)**, **`-0.8 KB` (`-4.3%`)** |

> **Key takeaway**:
> - **H1 alone** collapses the `synthetic:1000` full scan from **`~129 us` / `172 KB`** to **`~1.1 us` / `4.1 KB`** (a **>115x** speedup).
> - **H2 + H3 + H4 + H5 + H7** are small, non-breaking local fixes that (a) cut the **unindexed** per-policy scan cost by **>75% in CPU** and **100% of the 160 B per-policy allocation** (`48 B` context + `72 B` `FunctionSpec.invoke` + `24 B` `Iterators$9` + `16 B` `Optional.of` = **`160 B/policy` -> `0 B/policy`**!), and (b) cut **`rsa2008-all`** by **`~30%–40%` in CPU (`8.93 us -> ~5.5–6.2 us`)** and **`~30%–35%` in allocation (`18.77 KB -> ~12–13 KB`)**, even before Cycle C (bag implementation) lands!

---

## Refuted Ideas

1. **Full XEngine Numericalization + Single Decision Tree Replacing Combining Algorithms (Liu et al. 2008)**:
   - *Why refuted*: XEngine compiles all policies, rules, and combining algorithms into a single flat numericalized decision tree with 2-valued attribute lookups (`Permit` / `Deny` / `NotApplicable` terminals). As shown by Ngo et al. (2015) and verified against XACML 3.0 §§7.12–7.15, full tree flattening breaks XACML 3.0 extended indeterminate semantics (`INDETERMINATE_D`, `INDETERMINATE_P`, `INDETERMINATE_DP`), dynamic PIP error propagation, `MustBePresent="true"` on missing attributes, `<PolicyIdReference>` dynamic resolution, and per-policy `<ObligationExpressions>` / `<AdviceExpressions>` staging. Using a **Target pre-filter index (H1)** that feeds candidate policies in declaration order into the existing combining algorithms achieves the same $O(1)$ candidate lookup on `synthetic:1000` with zero XACML 3.0 semantic drift.
2. **Reordering Children Under `first-applicable` or `ordered-deny-overrides` / `ordered-permit-overrides` by Match Selectivity**:
   - *Why refuted*: Under `first-applicable` (XACML 3.0 §C.8), reordering two overlapping policies changes the authorization decision (`PERMIT` vs `DENY`) whenever both policies match a request. Under `ordered-deny-overrides` (§C.3) and `ordered-permit-overrides` (§C.5), reordering changes the order in which obligations, advice, and PIP side effects are evaluated. Only skipping provably `NOT_APPLICABLE` policies while preserving declaration order of matching/indeterminate candidates (H1) is universally safe across all ordered and first-applicable algorithms.
3. **Calling `DecisionRule.isMatch(context)` Before `DecisionRule.evaluate(context)` in `DenyOverrides.doCombine`**:
   - *Why refuted*: In `xacml4j`, [`BaseCompositeDecisionRule.evaluate(context)`](xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java#L170) *already* calls `isMatch(context)` as its very first line! Calling `d.isMatch(childContext)` in the combining algorithm and then calling `d.evaluate(childContext)` would evaluate the `Target` **twice** on every matching policy. (Note also that [`DecisionCombiningAlgorithms.evaluateIfMatch`](xacml-core/src/main/java/org/xacml4j/v30/policy/combine/DecisionCombiningAlgorithms.java#L50-L53) despite its name just calls `decision.createContext(context)` followed by `decision.evaluate(decisionContext)`.)

---

## Open Questions

1. **Where should the H1 `TargetIndex` live for `PolicyIDReference` children?**
   - Since a [`PolicySet`](xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySet.java) in `xacml-core` holds `PolicyIDReference` children whose target `Policy` objects live in the `PolicyRepository` / `PolicyReferenceResolver`, should `PolicySet` lazily build and cache the `TargetIndex` on first evaluation (keyed by the root `PolicyReferenceResolver` or built once when resolved through `DefaultPolicyReferenceResolver`), or should `PolicyDecisionPointBuilder` pre-link/index static policy trees at PDP build time? Lazy construction on `PolicySet` (invalidated only if the resolver instance changes) works transparently for all `PolicyDecisionPoint` callers and tests without changing any builder API.
2. **Does any test mock `EvaluationContextHandler` and assert multiple `handler.resolve(context, AttributeDesignatorKey)` calls for the same key within a single `RootEvaluationContext`?**
   - If not, **H7** (`RootEvaluationContext.resolve` checking `resolvedDesignators.get(key)` before calling `handler.resolve`) is a 4-line change in [`RootEvaluationContext.java:185-200`](xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java#L185-L200) that eliminates both the `Optional.of(bag)` allocation (`9.3%` of all allocations on `synthetic:1000-all`) and 999 redundant `HashMap.put` + `ConcurrentHashMap.get` calls per decide.
