<!-- Lane PF1 of the first profiling swarm (task 2.1): Claude Opus 5.5, read-only; report taken from the Claude session record, paths made repository-relative. -->

FINAL REPORT (xacml4j JMH benchmark engineer, lane PF1, Claude Opus 5.5)

## Summary

- **Cost per non-matching policy:** 2,112 B and 0.473 µs. Arithmetic from `add-pdp-jmh-benchmarks/.../002-baseline-jdk25/tables.md:31,36`: (2,127,619 − 15,816) B / 1000 and (479.1 − 6.503) µs / 1000.
- **Where it shows in the profile:** in `004.../alloc-synthetic_1000-all`, 99.65% of the sampled bytes sit under per-policy frames: `PolicyIDReference.createContext` 4.98% and `PolicyIDReference.evaluate` 94.67%.
- **Inventory total:** the inventory below sums to about 2,120 B per policy. Scale: 1% of samples ≈ 21.3 B per policy (11,729,781 B per 7-request cycle / 5,496 non-matching policies per cycle).
  - The scale matches object layouts: HashMap 48 B vs 48.6 B, `PolicyDescendantEvaluationContext` 32 B vs 32.9 B, `HashMap$Node[16]` 80 B vs 80.8 B, `Expression[2]` 24 B vs 24.8 B.
- **Main finding:** 76% (≈1,620 B) is the same request attribute resolved again for every policy. Each time the PDP runs an entity lookup stream, two filter streams, and a Guava multiset bag build.
  - XACML 3.0 §7.3.5 says every later test of an attribute "shall use the same bag of values that was initially tested".
  - So a per-decision memo (H1) is the specified model.
  - H1 alone: about −1,710 B (−81%) and −0.26 to −0.28 µs (−55 to −59%) per non-matching policy.
- **H1 to H7 together** leave about one context object (≈32 B) per non-matching policy (−98%).
- **H10 (target index)** removes the rest, but it needs research first.
- **Two existing spec deviations** found while reading (see open question 7). They are report-only; no hypothesis changes them.
- **Suggested cycles, each with its own JMH pair:** H1, then H2+H5+H6, then H3, then H4. H8 and H9 are for the fixed cost.

## Allocation inventory per non-matching policy (alloc-synthetic_1000-all, this lane's parse)

CPU shares come from `cpu-synthetic_1000-all`. Scale: 1% ≈ 4.81 ns per policy (472.6 ns ↔ 98.29% of samples under `DenyOverrides.doCombine`).

| # | Site (file:line) | Objects (sampled share) | B/policy | CPU |
| --- | --- | --- | --- | --- |
| 1a | `DefaultEvaluationContextHandler.java:109-110` | `()->doDesignatorResolve` lambda (1.13%) | 24 | — |
| 1b | `RequestContext.java:220-224,248-252` | stream Head, FindSink, `CollectSpliterators$1`, `Collections$2`, Optional; `Object[3]` from `SemanticIdentifier.hashCode` in the CategoryId map get (8.31%) | 177 | 9.1% |
| 1c | `AttributeIndex.java:105-109` findByIssuer | Head, `Object[10]`, ArrayList, filter op and sink, collector, reduce op and sink, spliterator, lambda (16.69%) | 356 | 15.2% |
| 1d | `Attribute.java:114-122` (call at `AttributeIndex.java:169`) | `ValueType[]` varargs, `Arrays$ArrayList`, filter/map/collect stream, lambda (22.40%) | 478 | 15.9% |
| 1e | `AttributeIndex.java:167-171` | ImmutableList builder `Object[]`, SingletonImmutableList (2.20%) | 47 | — |
| 1f | `Entity.java:214` bag build | `BagOfValues$Builder`, `ImmutableMultiset$Builder`, LinkedHashMultiset, LinkedHashMap, `HashMap$Node[16]`, `LinkedHashMap$Entry`, Count, 2× `Object[2]` (`Value.java:129`), `ImmutableEntry[]`, ImmutableEntry, RegularImmutableMultiset, entry set and iterator, BagOfValues (24.59%) | 524 | 14.1% |
| 1g | `Entity.java:214` | `Optional.of` (0.74%) | 16 | — |
| 2 | `AttributeReference.java:116-118` → `ValueType.java:125-127` | eager empty bag: LinkedHashMap, LinkedHashMultiset, EntrySet, BagOfValues (6.33%) | 135 | 1.8% |
| 3 | `Match.java:121` → `FunctionSpec.java:145`, `FunctionSpecBuilder.java:507,603`, `DefaultFunctionInvocation.java:96` | `Expression[2]`, 2× `LinkedList$Node`, ArrayList + `Object[2]`, `Object[2]` (6.81%) | 145 | 8.3% |
| 4 | `Match.java:120` bag iteration | `ImmutableMultiset$EntrySet`, ElementSet, `Arrays$ArrayList`, `IndexedImmutableSet$1` (4.14%) | 88 | 14.6% (of which `ImmutableList$1.get` self 10.7%) |
| 5 | `PolicyIDReference.java:80`, `Policy.java:167,252` | PolicyIDReferenceEvaluationContext 24 B, PolicyDescendantEvaluationContext 32 B, HashMap 48 B (4.98%) | 106 | 6.9% (3.65% is a Guava LocalCache lookup with no allocation) |
| 6 | `Target.java:72` | `Iterators$9` singleton iterator (1.15%) | 25 | <0.4% |

- **Group 1 (1a to 1g)** is the request attribute resolution: 76.1%, about 1,620 B per policy, 58.1% of CPU.

## Hypotheses (ranked by expected gain on the per-policy path; gains stack in order)

### H1. Memoize request-attribute bags per individual decision

- **Evidence:**
  - Group 1 is 76.1% of bytes. CPU for `DefaultEvaluationContextHandler.resolve` is 58.09%.
  - In `top-alloc-synthetic_1000-all.md` rows 77-106, the BagOfValues builder and stream rows are 1.4% to 6.1% each.
  - `designatorCache` holds PIP results only (`DefaultEvaluationContextHandler.java:109-110,169`). Request lookups are never cached (`:152-155`).
  - Group 4 views are created once per bag instance (frames `RegularImmutableMultiset.elementSet`, `ImmutableSet$CachingAsList.asList`). So a reused bag also removes group 4.
- **Change:**
  - In `xacml-pdp/.../DefaultEvaluationContextHandler.java` only, add a per-handler map `AttributeDesignatorKey → Optional<BagOfValues>` for request results, present and absent.
  - Fill it on the first lookup. Read it before the PIP fallback.
  - Use plain if/else instead of `Optional.or(lambda)`.
  - Keep `designatorCache` for PIP results only. `getResolvedAttributes` turns its entries into Result resolved attributes (`DefaultPolicyDecisionPoint.java:157,224-250`).
  - Use a HashMap if the single-thread rule (`design.md:14-16`) covers the handler; otherwise a ConcurrentHashMap, like `designatorCache`.
- **Expected gain:**
  - Bytes: −(76.1% + 4.14%) × 21.3 B ≈ −1,710 B per policy (−81%).
  - Time: −(58.09% + 3.54%) × 4.81 ns ≈ −296 ns, plus a memo hit of about 10 to 20 ns (UNVERIFIED). Net −0.26 to −0.28 µs (−55 to −59%).
  - synthetic:1000 unknown-notapplicable: 2.13 MB → ≈0.42 MB per op; 479 µs → ≈200-220 µs.
  - first-deny: 3 lookups of 2 keys (`SyntheticCorpus.java:124-137`). One memo hit saves 37.6%/3 × 15,816 ≈ 1.98 KB. The map costs about 0.2 KB. Net ≈ −1.8 KB (−11%).
  - rsa2008: upper bound 43.1% of bytes (resolve share in `alloc-rsa2008-all`). The real gain depends on how often a designator repeats in one decision (UNVERIFIED).
- **Risk:**
  - Semantics: XACML 3.0 §7.3.5 (quoted above) asks for this behavior. MustBePresent (§5.29) is still applied per designator after the lookup (`AttributeDesignator.java:91-100`).
  - Key equality covers category, data type, id, and issuer (`AttributeDesignatorKey.java:97-110`). Bags are immutable (`BagOfValues.java:63-75`).
  - Thread safety: there is one handler per individual decision (`PolicyDecisionPointContextFactory.java:176-184`).
  - The memo must not live on Entity or RequestContext. The benchmark reuses parsed requests (`PdpDecideBenchmark.java:118-120,156-161`).
  - No API change.
  - Test first: a handler test with a counting RequestContextCallback and a counting PIP.
    - Two resolves of one key → one `getEntity` call and the same bag instance.
    - An absent key still calls the PIP as before.
    - `getResolvedDesignators()` is unchanged.
  - Then the reactor and conformance counts (`design.md:92`), and a JMH pair with `-prof gc`.
- **Checked / not checked:**
  - Checked: the lines cited above, and no repeating categories at evaluation (`PolicyDecisionPointContextFactory.java:170-175`).
  - Not checked: PIPs that call `context.resolve` from other threads; a selector memo (same §7.3.5 rule); Guava view caching (inferred from frame names only).

### H2. Remove the eager empty bag and cache one per bag type

- **Evidence:**
  - Group 2: 6.33% → 135 B per policy; CPU 1.80%. first-deny 3.35%, rsa2008 3.75%.
  - `v.orElse(getDataType().emptyBag())` builds its argument even when `v` is present (`AttributeReference.java:116-118`).
  - `emptyBag` builds a new bag on each call (`ValueType.java:125-127`; TODO at `BagOfValuesType.java:75`).
- **Change:**
  - `AttributeReference.java`: use `v.isPresent() ? v.get() : getDataType().emptyBag()`.
  - `BagOfValuesType.java`: add a lazily cached transient empty bag.
  - `ValueType.java`: the default `emptyBag()` delegates to `bagType().emptyBag()`. XacmlTypes already caches `bagType` (`XacmlTypes.java:167,173,211-214`).
- **Expected gain:** −135 B (−6.4%) and about −9 ns (−1.8%) per policy. first-deny ≈ −530 B. After H1 this is 33% of the remaining 411 B.
- **Risk:**
  - Semantics: §5.29 and §7.3.5 (empty bag when MustBePresent=false). A shared empty bag equals a new one (`BagOfValues.java:210-236`).
  - Do not build the bag in the BagOfValuesType constructor: the Builder rejects dynamic types (`BagOfValues.java:282-288`).
  - Empty bags lose distinct identity. No identity use was found in main code (rtk rg: IdentityHashMap appears only in `BaseFunctionProvider`).
  - Test: the reference returns the present bag unchanged; an absent reference gives an empty bag whose `equals` and `hashCode` match a freshly built one.
- **Checked / not checked:** the path's call sites are checked. Tests that might expect distinct empty-bag instances are not checked.

### H3. Fixed-arity fast path for the Match predicate call

- **Evidence:**
  - Group 3: 6.81% → 145 B per policy.
  - CPU `FunctionSpec.invoke` 8.26%: normalize 1.56%, copyInto 0.89%, evaluate 0.77%, `Method.invoke` 1.62%, the `isValidateFuncParamsAtRuntime` context walk 1.05%.
- **Change:**
  - (a) `FunctionSpecImpl` records "all parameters mandatory". When the argument count equals the parameter count, it uses the argument list as is. `normalize` gives the same elements in the same order in that case (`FunctionSpecBuilder.java:516,521-537`).
  - (b) Add a two-argument `invoke(context, a, b)` as default methods on FunctionSpec and FunctionInvocation; the default is the current list path. FunctionSpecImpl and DefaultFunctionInvocation override it: they evaluate both arguments into locals and fill the reflective `Object[]` directly. `Match.java:121` calls it.
  - (c) Later, optional: bind a fixed-arity MethodHandle in `FunctionInvocationFactory.java:59-73`, so no `Object[]` is needed.
  - Keep the try/catch and exception mapping (`:391-402`) and the runtime-validation branch (`:367-378`).
- **Expected gain:** (a) −47 B, (b) −76 B, (c) −23 B: about −145 B in total (−6.9%), and −15 to −25 ns (−3 to −5%) per policy. first-deny ceiling ≈ −680 B (4.30%).
- **Risk:**
  - No XACML rule changes: function semantics (Annex A.3) stay the same, and arguments are still evaluated in order.
  - Not BREAKING: only new default methods.
  - Test: string-equal, a function with optional parameters, and a variadic function (string-concatenate). Fast and general paths must give equal results and the same exception types, with runtime validation on and off. Then conformance.
- **Checked / not checked:**
  - Checked: the default `ValueExp.evaluate` returns `this` (`ValueExp.java:41-44`), and no value class in xacml-core overrides it (rtk rg). The fast path still calls `evaluate`.
  - Not checked: third-party FunctionInvocation implementations. The default method keeps them working.

### H4. Bag values without Guava multiset views (array-backed bag, indexed loop in Match)

- **Evidence:**
  - CPU for bag iteration in `Match.match` is 14.62%: `ImmutableList$1.get` 11.08% with 10.7% self (`top-cpu-synthetic_1000-all.md:7,77`), plus 3.54% for the views.
  - Bytes: group 4 (88 B) and the bag build 1f (524 B). first-deny bag build is 12.9% (≈2.0 KB). rsa2008 `BagOfValues$Builder.<init>` 5.59% + `BagOfValues.<init>` 6.31%.
- **Change:**
  - `BagOfValues.java` stores a `Value<?>[]`. The Builder appends to an array, with no LinkedHashMultiset and no hashing.
  - `values()` returns a cached transient unmodifiable List view.
  - Add `size()` and `get(int)`, and use an indexed loop at `Match.java:120`.
  - `equals`, `hashCode`, `contains`, `containsAll`, `union`, and `intersection` keep multiset semantics (count-sensitive, order-free), with a hash index for large bags.
  - Only BagOfValues uses Multiset in main code (rtk rg).
- **Expected gain:**
  - After H1: about 0 B per policy, and −50 ns (−11%) if the `ImmutableList$1.get` self time is real (UNVERIFIED; it may be attribution skid).
  - Without H1: −612 B per policy (−29%). Fixed cost ≈ −1.9 KB.
- **Risk:**
  - Semantics: §7.3.2 bags are unordered and may hold duplicates. A different iteration order changes a Match result only when the predicate fails on some elements (open question 7).
  - `values()` is no longer an ImmutableMultiset. That is BREAKING only for callers that cast it to Multiset; none in main sources, tests not checked.
  - Bag functions (A.3.10, A.3.11) need the hash path to avoid O(n²) `contains`. `hashCode` must stay order-free and consistent with `equals`.
  - Test first: characterization tests for `equals`/`hashCode` with duplicates and different orders, union, intersection, containsAll, and bag functions. Then conformance.

### H5. Create the policy variable map only when the policy has variables

- **Evidence:**
  - HashMap 2.28% → 49 B per policy; first-deny 62 B.
  - `Maps.newHashMap()` runs for every policy context (`Policy.java:250-253`). Synthetic policies have no variables (`SyntheticCorpus.java:124-137`).
- **Change:** in `Policy.java`, create `varDefEvalResults` lazily in `setVariableEvaluationResult`. Getting a value returns null when the map is absent.
- **Expected gain:** −49 B (−2.3%) per policy. The CPU share is below the 0.4% display threshold.
- **Risk:**
  - Semantics: §7.8 is unchanged. The set path is guarded by `containsKey` (`Policy.java:285-288`), so a policy without definitions never stores.
  - Thread safety is the same as now.
  - Test: existing VariableReference tests.

### H6. Indexed loop over the target's AnyOf

- **Evidence:** `Iterators$9` 1.15% → 25 B (`Target.java:72`); first-deny 94 B. Escape analysis keeps this allocation, but removes the iterators in MatchAnyOf and MatchAllOf (they do not appear in the profile).
- **Change:** `Target.java` keeps the ImmutableList for `getAnyOf()`, adds an array for `match()`, and loops by index with the same loop body.
- **Expected gain:** −25 B (−1.2%) per policy.
- **Risk:** no semantic change if the body stays as is, including the current break on Indeterminate. Test: targets with 0, 1, and 2 AnyOf.

### H7. One context object per policy reference

- **Evidence:** 25 B + 33 B per policy (`PolicyIDReference.java:80,93`; `Policy.java:167`). CPU for `PolicyIDReferenceEvaluationContext.<init>` is 0.56%.
- **Change:**
  - In package `v30.policy`, add a Policy context variant that also returns the reference from `getCurrentPolicyIDReference()`.
  - `createContext` resolves the policy first, then creates only this one context.
  - Keep the cycle check (`:182-186`) and the extended-indeterminate variant (`Policy.java:256-268`).
- **Expected gain:** −25 B (−1.2%) and about −0.5% CPU per policy.
- **Risk:**
  - Medium. `getParentContext()` chains get one level shorter.
  - The cycle walk (`PolicyIDReference.java:149-163`) and the §7.18 staging walk (`BaseCompositeDecisionRule.java:202-212`) read these chains. Extensions see a different chain, but no API break.
  - Test: cyclic-reference, §7.18 staging, and §7.14 tests, then conformance.

### H8. Hash codes without varargs arrays

- **Evidence:**
  - `Value.java:128-130`: 2× `Object[2]` per bag build, 49 B per policy.
  - `SemanticIdentifier.java:102-104`: `Object[3]` per CategoryId lookup, 32 B per policy.
  - CPU: `SemanticIdentifier.hashCode` 3.02%, `Value$OtherValue.hashCode` 4.2%.
  - first-deny: 1.18% + 3.31% ≈ 710 B (4.5%).
- **Change:**
  - Compute the same value without an array: `31*(31+value.hashCode())+type.hashCode()`, and `31*(31*(31+id.hashCode())+abbreviatedId.hashCode())+getClass().hashCode()`.
  - Optionally cache it in a transient field that uses 0 for "not computed". The class is Serializable, and its fields are written only in the constructor (`:57-58`; no subclass writes them, rtk rg).
- **Expected gain:** −80 B per policy (−3.8%) and −2 to −4% CPU on its own. After H1, only the fixed cost gains (≈ −0.7 KB).
- **Risk:** none, because the hash values stay the same. Test: old vs new hash for StringVal and CategoryId.

### H9. Loops and zero-copy returns for the first attribute lookup

- **Evidence:**
  - Groups 1b to 1e: 1,058 B per policy without H1.
  - first-deny: `findValues` 28.1% + `getEntity` 3.8% ≈ 5.0 KB per op.
  - rsa2008: 21.9% + 4.4% ≈ 11.9 KB per op.
- **Change:**
  - Use loops instead of streams (standing rule).
  - Add a private zero-copy path for `Entity.resolve`:
    - with no issuer, use `attributes.get(id)` directly;
    - for one attribute whose values all have the requested type (checked by an index loop), return its immutable values list;
    - for `getEntity`, loop over `entities.get(category)`.
  - Keep the public `findByIssuer` and `getValuesByType` results as they are (`Collectors.toList`), or mark BREAKING.
- **Expected gain:** after H1, this applies only to the first lookup per designator per decision. It is the largest fixed-cost item on this path: first-deny about −4 to −5 KB (−25 to −30%).
- **Risk:** none for XACML: same values in the same order (§7.3.4). Test: issuer null and non-null, mixed types, several attributes with one id.

### H10. Skip policies whose target cannot match (target index)

- **Evidence:** after H1 to H7, each policy still costs the reference resolution (Guava LocalCache, 3.65% CPU), one context, and the target walk. Full-scan sets are 99% per-policy work.
- **Change:** a research item (`proposal.md:61-62`). At load time, index the children whose target is one equality Match on a designator with MustBePresent=false (so it cannot be Indeterminate), keyed by value. At decide time, evaluate only the candidates and the non-indexable children, in document order.
- **Expected gain:** ceiling of −2,112 B and −0.47 µs per skipped policy (today's numbers).
- **Risk:**
  - High. A skipped child must be provably No Match (§7.6, §7.7). Its NotApplicable must then have no effect under every combining algorithm.
  - It must also produce no status, no obligations or advice (§7.18), and no ReturnPolicyIdList entry (`RootEvaluationContext.java:200-205`).
  - Needs a research note, a design decision, and a property test: indexed vs full evaluation must give equal decisions, statuses, and obligations.

## Refuted ideas (checked)

- **BagOfValuesType per bag:** already cached (`XacmlTypes.java:167,173,211-214`). Not in the profile.
- **BooleanVal per predicate result:** constants (`BooleanVal.java:36-37,43`). Not in the profile.
- **AttributeDesignatorKey hash:** cached (`AttributeDesignatorKey.java:43,90-95`). `designatorCache.get` allocates nothing.
- **Not in the profile (scalar-replaced on C2):**
  - MatchAnyOf, MatchAllOf, and deny-overrides iterators;
  - the LinkedList object of `normalize`;
  - `Arrays$ArrayList` in `FunctionSpec.invoke`;
  - the `Collections2.transform` view;
  - the 4-argument debug varargs at `Entity.java:210`.
  - Removing them saves no measured bytes on C2. Graal JIT may differ (UNVERIFIED).
- **Memo on Entity or RequestContext, or a precomputed entity map:** this would share work across benchmark iterations that reuse parsed requests, so the JMH gain would be larger than the production gain. H1 avoids this bias.
- **Request bags in `designatorCache`:** this would change the Result resolved attributes (`DefaultPolicyDecisionPoint.java:157,224-250`).
- **Eclipse Collections for bags:** not needed on this path after H1. A small array-backed bag (H4) adds no dependency.

## Open questions

1. Is the 10.7% self time of `ImmutableList$1.get` for one-element bags real, or skid? The answer decides H4's time gain. It needs a perfasm or inlining view.
2. Why does escape analysis keep `Iterators$9` in `Target.match` but remove the iterators in MatchAnyOf and MatchAllOf? `-XX:+PrintInlining` on one fork would show it.
3. Is the evaluation handler confined to one thread, including PIPs? The answer decides HashMap vs ConcurrentHashMap for H1.
4. Run 004 has no `-prof gc`. All byte estimates scale the B/op of run 002 (Log4j build). The fixed cost of the Logback build may be lower.
5. The Guava 31.1 → 33.7.2 bump (`upgrade-dependencies/tasks.md:20`) can change the multiset allocations. Each JMH pair must use one Guava version.
6. On rsa2008, the `FunctionSpec.invoke` subtree is 47.8% of bytes, because nested Apply arguments include designator resolution. It needs its own breakdown.
7. **Production defects to report, not edit.** No hypothesis causes or fixes them; UNVERIFIED by test.
   - (a) `Target.java:72-86` stops at the first Indeterminate AnyOf. XACML 3.0 §7.7 Table 1: any No Match gives No Match.
   - (b) `Match.java:113-138` returns Indeterminate at the first predicate EvaluationException. §7.6: True wins if any application is True.
   - H3, H4, and H6 must keep the current behavior until a separate behavior change decides.
8. **Out of lane (CPU only):**
   - the Guava LocalCache in `DefaultPolicyReferenceResolver.resolve` (3.65%);
   - the 3-level context delegation for `resolve` and `isValidateFuncParamsAtRuntime` (about 4%);
   - logback turbo-filter checks (`CopyOnWriteArrayList.getArray` 0.9% self).

## Verification run (read-only)

- **Read:**
  - `optimize-pdp-evaluation` proposal, design, and tasks;
  - summaries and tables of runs 001 and 002;
  - run 004 summary, command, top tables, and jmh.log;
  - every source file cited above.
- **Parsed** the collapsed stacks with an inline reader (`rtk proxy python3 -I -c …`) over `collapsed-alloc.csv.gz` (79,945 samples), `collapsed-cpu.csv.gz` (10,091), and the rsa2008 and first-deny alloc profiles. Outputs: the per-policy subtree tree and the flat list of leaf sites.
- **Searched** with `rtk rg`: identity uses, Multiset users, subclass field writes, evaluate overrides.
- **Fetched** the XACML 3.0 core text (https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html): §5.29, §7.3.2 (TOC), §7.3.5, §7.6, §7.7, §7.14, §7.15.
- **Not run:** Maven, tests, JMH, profilers. No benchmark code added.
- **No files written.** The client kept two large command outputs in its session tool-results folder, outside the repository.

## Residual risk

- **Bytes:** estimates come from allocation sample shares scaled to run 002. The scale was cross-checked against object sizes within 3%.
- **Time:** estimates assume CPU shares scale linearly. An edit changes inlining and escape analysis, so each hypothesis needs its own pair:
  - `PdpDecideBenchmark`, store=in-memory;
  - synthetic:1000 with request first-deny, unknown-notapplicable, and all;
  - rsa2008;
  - 3 forks, `-prof gc`.
- **Not measured, inferred:** the memo-hit cost (10 to 20 ns) and Guava view caching.
