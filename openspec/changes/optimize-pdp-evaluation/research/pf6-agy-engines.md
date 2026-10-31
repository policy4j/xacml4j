<!-- Lane PF6 of the first profiling swarm (task 2.1): Agy, Gemini 4 Argon (high), read-only; report taken from the Agy conversation database (longest block between the markers), local links made repository-relative. -->

FINAL REPORT (policy4j_bench_agy_argon, Agy Gemini 4 Argon)

### Executive Summary

1. **Goal & Scope**: Read-only cross-engine and profile-guided optimization analysis (`LANE PF6`) for [`PolicyDecisionPoint.decide`](xacml-core/src/main/java/org/xacml4j/v30/pdp/PolicyDecisionPoint.java#L44-L53) under [`openspec/changes/optimize-pdp-evaluation`](openspec/changes/optimize-pdp-evaluation/proposal.md), comparing `xacml4j` against **OW2 AuthzForce** (`authzforce/core`, `authzforce/core-pdp-api`), **AT&T XACML 3.0** (`att/xacml-3.0`), **WSO2 Balana** (`wso2/balana`), and academic/industrial high-throughput engines (**XEngine**, **SBA-XACML**, **Poliseek**).
2. **Baseline Reference ([`002-baseline-jdk25/tables.md`](openspec/changes/add-pdp-jmh-benchmarks/evidence/benchmarks/jmh/002-baseline-jdk25/tables.md), Oracle GraalVM 25.0.4 with C2 `-XX:-UseJVMCICompiler`, `-Xms2g -Xmx2g` G1, 1 thread, `store=in-memory`)**:
   - `rsa2008, request=all`: **18.97 ± 0.43 us/op**, **45,115 ± 840 B/op** (`gc.alloc.rate.norm`).
   - `synthetic:100, request=all`: **43.87 ± 0.90 us/op**, **176,210 ± 1,400 B/op**.
   - `synthetic:1000, request=first-deny` (fixed cost per decision + 1 matching target): **6.503 ± 0.20 us/op**, **15,816 ± 290 B/op**.
   - `synthetic:1000, request=all`: **480 ± 16 us/op** (`last-permit`: **480 ± 29 us/op**, `2,121,700 ± 18,000 B/op`; `unknown-notapplicable`: **479.1 ± 16 us/op**, **2,127,600 ± 11,000 B/op**).
   - **Derived marginal cost per non-matching policy target on JDK 25**: `(479.1 - 6.503) / 1000 = 0.4726 us` (**~473 ns**) and `(2,127,600 - 15,816) / 1000 = 2,111.8 B` (**~2.11 KB**) per policy target (`SUPPORTED`: [`002-baseline-jdk25/tables.md`](openspec/changes/add-pdp-jmh-benchmarks/evidence/benchmarks/jmh/002-baseline-jdk25/tables.md#L15-L53) and [`001-baseline/summary.md`](openspec/changes/add-pdp-jmh-benchmarks/evidence/benchmarks/jmh/001-baseline/summary.md#L38-L55)).
3. **Core Finding**: Other XACML engines achieve near-zero allocation per target evaluation through four structural choices that directly transfer to `xacml4j`:
   - **Request-context attribute memoization / pre-indexed bags**: `xacml4j` only caches PIP-resolved attributes (`designatorCache.putIfAbsent` in [`DefaultEvaluationContextHandler.doPipResolve`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java#L169)), **never caching attributes resolved from `RequestContext`** ([`doDesignatorResolve`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java#L152-L156)). In `synthetic:1000`, `xacml4j` re-runs 3 nested Java stream pipelines and rebuilds the exact same `BagOfValues` (with `LinkedHashMap` + `LinkedHashMultiset` + `RegularImmutableMultiset`) **1,000 times per decision**, causing **~55.4% of all allocations (~1.18 MB/op)** and **~22% of CPU**. AuthzForce (`BaseEvaluationContext.java`) pre-populates `Map<AttributeFqn, AttributeBag<?>>` once per request.
   - **Direct typed `Match` evaluation**: `xacml4j` routes every [`Match.match`](xacml-core/src/main/java/org/xacml4j/v30/policy/Match.java#L120-L129) predicate call through varargs `Expression[]`, `new LinkedList<>()` parameter normalization ([`FunctionSpecBuilder.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java#L500-L519)), `new ArrayList<>(2)` argument evaluation, `new Object[]` copying, and `Method.invoke` reflection (**~17% CPU** in `synthetic:1000-all`). AuthzForce (`MatchEvaluator.java`, `EqualTypeMatchFunction.java`) pre-binds `Match` at policy construction and invokes a direct typed `Matcher<T>.match(arg0, arg1)` interface.
   - **Target indexing across `PolicySet` children**: `xacml4j` linearly scans all 1,000 child policies in [`DenyOverrides.doCombine`](xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DenyOverrides.java#L73-L99). XEngine, SBA-XACML, and Poliseek index target equality predicates by `(Category, AttributeId, DataType) -> ConstantValue -> childBitSet`, reducing `O(N)` target scans to `O(1)` lookup while preserving combining order.
   - **Single-pass request flags & lightweight `EvaluationContext`**: `xacml4j` spends **26.9% of CPU** and **24.0% of allocations** on `first-deny` in `MultipleResources*Handler` before `requestDecision` starts, and allocates **7 `ConcurrentHashMap`s + 1 `ConcurrentSkipListMap` + 2 `ConcurrentLinkedDeque`s** per single-threaded request context.

---

### Cross-Engine Technique Comparison Matrix

| Subsystem | `xacml4j` (Current) | OW2 AuthzForce (`authzforce/core` & `core-pdp-api`) | WSO2 Balana (`wso2/balana`) | AT&T XACML (`att/xacml-3.0`) & Literature (XEngine / SBA-XACML / Poliseek) |
| --- | --- | --- | --- | --- |
| **Request Attribute Lookup & Caching** | Uncached `RequestContext` lookup in [`DefaultEvaluationContextHandler.doDesignatorResolve`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java#L152-L156); 3 nested Streams per lookup; rebuilds `BagOfValues` every time (`designatorCache` only caches PIP results). | Pre-populates `namedAttributes: Map<AttributeFqn, AttributeBag<?>>` once at request context creation (`BaseEvaluationContext.java:53-112`); `O(1)` map lookup in `GenericAttributeProviderBasedAttributeDesignatorExpression.java:132`. | Single pass `setupAttributes` into `mapAttributes` at context creation (`XACML3EvaluationCtx.java:155-204`); builds `BagAttribute(type, List)` (`:108-126`). | AT&T wraps request in `RequestFinder` / `RequestEngine` (`StdEvaluationContext.java:81-90`). SBA-XACML / Poliseek pre-index request attributes by `(Category, Id, Type)` array/hash slots. |
| **Bag Representation** | [`BagOfValues`](xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java#L54-L90) allocates `LinkedHashMap` + `LinkedHashMultiset` in builder and copies to Guava `RegularImmutableMultiset` (hashing every element via `Objects.hash`). | `Bags.java:43-175` specializes `Empty`, `Singleton` (direct `private final AV singleVal` field), and `Multi` (`ImmutableMultiset` only when `size > 1`). | `BagAttribute.java` wraps a plain `List<AttributeValue>` (or singleton/empty list) with zero multiset hashing. | `StdAttributeValue` list in AT&T; flat primitive/value arrays in XEngine. |
| **Target `<Match>` Dispatch** | [`Match.match`](xacml-core/src/main/java/org/xacml4j/v30/policy/Match.java#L120-L129) calls `FunctionSpec.invoke(context, value, v)` -> varargs `Expression[]` -> `LinkedList` normalize -> `ArrayList` eval -> `Object[]` copy -> `Method.invoke`. | `MatchEvaluator.java:63-99` pre-binds `Match` at load time into `FirstOrderFunctionCall<BooleanValue>`; `EqualTypeMatchFunction.java:65-150` calls typed `Matcher<T>.match(arg0, arg1)` (`arg0.equals(arg1)`). | `TargetMatch.java:172-238` calls `function.evaluate(inputs, context)` directly over a 2-element list without `LinkedList` normalization or reflection. | XEngine / Poliseek compile equality `<Match>` into direct hash-table lookups against the request attribute index. |
| **Multi-Policy Target Indexing** | Linear `O(N)` scan in [`DenyOverrides`](xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DenyOverrides.java#L73-L99) and other combining algorithms; resolves `PolicyIDReference` via Guava `LocalCache` on every policy. | `StaticRefPolicyProvider` pre-resolves static policy references at load time (`PolicyEvaluators.java`); linear scan over child evaluators unless custom combining alg used. | Linear scan in `PolicyFinder` / combining algorithms. | **XEngine** (decision diagrams), **SBA-XACML** (set-based compilation), and **Poliseek** (two-stage hash/bitset index) index `<Target>` attributes to skip non-matching policies in `O(1)`. |
| **Multiple Decision Profile (MDP) Detection** | 4 handlers in `RequestContextHandlerChain` repeatedly scan `RequestContext.entities.values()` via Streams on every single-decision request (**26.9% CPU** on `first-deny`). | Separate configured `RequestFilter` (`default-lax` vs `multiple:repeated-attribute-categories-lax`, `server/CHANGELOG.md`); zero MDP scan when single-request filter is active or no multi-attributes exist. | Computes `multipleAttributes`, `multipleContentSelectors`, and `resourceScope` in one pass during `XACML3EvaluationCtx.setupAttributes` (`:155-204`); skips MDP when false (`PDP.java:196`). | AT&T `MultiDecisionRequest` splits only when multi-decision attributes are detected during initial request inspection. |
| **Regex Functions (`*-regexp-match`)** | [`RegExpFunctions`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java#L178-L216) has `CACHE_REGXP = false` by default; converts XSD regex to Java regex and runs `Pattern.compile` on every call. | `RegexpMatchFunctionHelper.java:89-165` pre-compiles constant regex arguments at policy load time into `CompiledRegexMatchFunctionCall` (using Saxon `XP20` regex engine). | `RegExpFunction.java` delegates without pre-compilation. | AT&T `FunctionDefinitionRegexpMatch.java:96` calls `elementValueString.matches(regexpValue)` per call (uncached and wrongly anchored). |

---

### Ranked Optimization Hypotheses (Most Valuable First)

#### H1. Memoize `RequestContext` attribute designator resolution in `DefaultEvaluationContextHandler` and replace `RequestContext`/`AttributeIndex`/`Attribute` stream pipelines with indexed lookups

- **Evidence**:
  - **Source**: In [`DefaultEvaluationContextHandler.resolve(EvaluationContext, AttributeReferenceKey)`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java#L105-L110) and [`doDesignatorResolve`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java#L148-L157), `designatorCache.get(ref)` is checked at line 108, and on a miss it calls `requestCallback.getEntity(ref.getCategory()).flatMap(e -> e.resolve(designatorKey.getAttributeId(), designatorKey.getDataType(), designatorKey.getIssuer()))`. However, **`designatorCache.putIfAbsent(key, v)` is only called inside [`doPipResolve` (line 169)](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java#L169) when the attribute is missing from the request**! When an attribute *is* present in `RequestContext`, its resolved `BagOfValues` is **never cached** in `designatorCache`.
  - Furthermore, each uncached `RequestContext` resolution executes three nested Java Stream pipelines and rebuilds a `BagOfValues`:
    1. [`RequestContext.getEntity(CategoryId)`](xacml-core/src/main/java/org/xacml4j/v30/RequestContext.java#L186-L194): `entities.get(category).stream().map(Category::getEntity).findFirst()`.
    2. [`Entity.resolve`](xacml-core/src/main/java/org/xacml4j/v30/types/Entity.java#L163-L165) -> [`AttributeIndex.findValues`](xacml-core/src/main/java/org/xacml4j/v30/AttributeIndex.java#L118-L128) -> [`AttributeIndex.findByIssuer`](xacml-core/src/main/java/org/xacml4j/v30/AttributeIndex.java#L75-L84): `index.get(attributeId).stream().filter(a -> issuer == null || issuer.equals(a.getIssuer())).collect(Collectors.toList())`.
    3. [`Attribute.getValuesByType`](xacml-core/src/main/java/org/xacml4j/v30/Attribute.java#L120-L125): `values.stream().filter(v -> v.getType().equals(type)).collect(Collectors.toList())`.
  - **Profile shares ([`004-profile-decide-jdk25-logback`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/summary.md))**:
    - In [`top-alloc-synthetic_1000-all.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/alloc-synthetic_1000-all/top-alloc-synthetic_1000-all.md#L10-L49) (85,484 samples):
      - Stream allocations in `Attribute.getValuesByType`: `ReferencePipeline$Head` (2.7%) + `ReferencePipeline$2` (2.6%) + `Spliterators$IteratorSpliterator` (2.6%) + `ArrayList` (2.6%) + `ReferencePipeline$2$1` (1.5%) + `ReduceOps$3` (1.4%) + `ReduceOps$3ReducingSink` (1.4%) = **14.8% of total allocations**.
      - Stream allocations in `AttributeIndex.findByIssuer`: `ReferencePipeline$2` (2.8%) + `ReferencePipeline$Head` (2.6%) + `ArrayList` (2.6%) + `AbstractList$SubList$1` (1.6%) + `Spliterators$IteratorSpliterator` (1.6%) + `ReferencePipeline$2$1` (1.5%) = **12.7% of total allocations**.
      - Stream allocations in `RequestContext.getEntity` (2.6%) + `AttributeIndex.findValues` `Iterators$5` (1.4%) = **4.0% of total allocations**.
      - Repeated `BagOfValues` construction per policy target (`BagOfValues$Builder.<init>` 6.1% + 2.9%, `BagOfValues$Builder.attributes` 3.8% + 1.8%, `BagOfValues.<init>` 2.3% + 2.3% + 1.8% + 1.5% + 1.4%) = **23.9% of total allocations**.
      - **Combined allocation share in `synthetic:1000-all`**: `14.8% + 12.7% + 4.0% + 23.9% = 55.4%` of all allocations!
    - In [`top-cpu-synthetic_1000-all.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/cpu-synthetic_1000-all/top-cpu-synthetic_1000-all.md#L56-L78): `DefaultEvaluationContextHandler.resolve` accounts for `99.9% - 76.2% = 23.7%` of total CPU samples (`doDesignatorResolve` 20.1% total CPU; `Entity.resolve` 17.7% total CPU; `AttributeIndex.findValues` 16.0% total CPU).
    - In [`top-alloc-rsa2008-all.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/alloc-rsa2008-all/top-alloc-rsa2008-all.md#L10-L49): Stream and `BagOfValues` allocations from `Entity.resolve` account for **~26% of all allocations**.
  - **External Engine Citation**: OW2 AuthzForce (`authzforce/core-pdp-api`, `src/main/java/org/ow2/authzforce/core/pdp/api/BaseEvaluationContext.java:53-112` and `src/main/java/org/ow2/authzforce/core/pdp/api/expression/GenericAttributeProviderBasedAttributeDesignatorExpression.java:127-137`) pre-populates `namedAttributes: Map<AttributeFqn, AttributeBag<?>>` once per request and memoizes all designator lookups in that map (`context.getNamedAttributeValue(attributeFqn, datatype)`).
- **Change**:
  1. In [`DefaultEvaluationContextHandler.doDesignatorResolve`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java#L148-L157), cache the `BagOfValues` resolved from `RequestContext` in `designatorCache` (`designatorCache.put(designatorKey, bag)`), so any `AttributeDesignatorKey` is resolved from `RequestContext` at most once per individual decision request.
  2. Replace the stream pipelines in [`RequestContext.getEntity`](xacml-core/src/main/java/org/xacml4j/v30/RequestContext.java#L186-L194), [`AttributeIndex.findByIssuer`](xacml-core/src/main/java/org/xacml4j/v30/AttributeIndex.java#L75-L84), [`AttributeIndex.findValues`](xacml-core/src/main/java/org/xacml4j/v30/AttributeIndex.java#L118-L128), [`AttributeIndex.containsValues`](xacml-core/src/main/java/org/xacml4j/v30/AttributeIndex.java#L94-L103), and [`Attribute.getValuesByType`](xacml-core/src/main/java/org/xacml4j/v30/Attribute.java#L120-L125) with direct indexed `for` loops (per the standing rule in [`optimize-pdp-evaluation/proposal.md`](openspec/changes/optimize-pdp-evaluation/proposal.md#L25-L31): *"Replace stream pipelines and functional combinators on hot paths with indexed or enhanced-for loops"*).
- **Expected gain**:
  - **`synthetic:1000, request=all`**: Eliminates 999 of 1,000 request attribute resolutions and stream pipelines per decision. Saves `0.554 * 2,121,700 B ≈ 1,175,000 B/op` (**~1.18 MB/op**, reducing allocation by **~55%**) and `0.235 * 480 us ≈ 113 us/op` (**~23.5% of total decision latency**).
  - **`rsa2008, request=all`**: Eliminates all stream allocations in `Entity.resolve` and repeated resolutions across policies/rules, saving `~0.22 * 45,115 B ≈ 9,900 B/op` (**~9.9 KB/op**) and `~0.12 * 18.97 us ≈ 2.3 us/op`.
  - **`synthetic:1000, request=first-deny`**: Replacing streams in `RequestContext.getEntity`, `AttributeIndex`, and `Attribute` saves `~1.8 KB/op` and `~0.4 us/op`.
- **Risk**:
  - **XACML 3.0 semantics**: XACML 3.0 §7.3.5 (Attribute Retrieval) requires that within a single decision request context, an `<AttributeDesignator>` with the same `(Category, AttributeId, DataType, Issuer)` always resolves to the same bag of values. Note that [`AttributeDesignatorKey`](xacml-core/src/main/java/org/xacml4j/v30/AttributeDesignatorKey.java#L122-L133) `equals`/`hashCode` includes `(category, attributeId, dataType, issuer)` and intentionally excludes `mustBePresent`, while [`AttributeDesignator.evaluate`](xacml-core/src/main/java/org/xacml4j/v30/policy/AttributeDesignator.java#L118-L129) enforces `if (v.isEmpty() && isMustBePresent()) throw new MissingAttributeException(...)` *after* `context.resolve(this)` returns. Therefore, caching the `BagOfValues` by `AttributeDesignatorKey` in `designatorCache` preserves exact `MustBePresent="true"` / `"false"` behavior even when two designators share `(Category, AttributeId, DataType, Issuer)` with different `MustBePresent` flags!
  - **Thread safety**: `DefaultEvaluationContextHandler` is instantiated per individual decision request (`PolicyDecisionPointContextFactory.java:165`); it is not shared across concurrent requests.
  - **API change**: Non-breaking (internal to `DefaultEvaluationContextHandler`, `RequestContext`, `AttributeIndex`, `Attribute`).
  - **Verification**: Full unit test suite + `Xacml30ConformanceTest` and `RSA2008InteropTest`, plus a unit test with two `<AttributeDesignator>`s having identical `(Category, AttributeId, DataType, Issuer)` and opposite `MustBePresent` flags.
- **Checked / not checked**:
  - Checked [`DefaultEvaluationContextHandler.java`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java#L105-L175), [`AttributeDesignatorKey.java`](xacml-core/src/main/java/org/xacml4j/v30/AttributeDesignatorKey.java#L90-L134), [`AttributeDesignator.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/AttributeDesignator.java#L115-L136), [`RequestContext.java`](xacml-core/src/main/java/org/xacml4j/v30/RequestContext.java#L186-L194), [`Entity.java`](xacml-core/src/main/java/org/xacml4j/v30/types/Entity.java#L163-L165), [`AttributeIndex.java`](xacml-core/src/main/java/org/xacml4j/v30/AttributeIndex.java#L75-L129), and [`Attribute.java`](xacml-core/src/main/java/org/xacml4j/v30/Attribute.java#L120-L125) (`SUPPORTED`).

---

#### H2. Replace `BagOfValues` Guava `LinkedHashMultiset`/`ImmutableMultiset` and `LinkedHashMap` builder with specialized Empty, Singleton, and List-backed bags

- **Evidence**:
  - **Source**: Every [`BagOfValues.Builder`](xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java#L218-L226) eagerly allocates both `new LinkedHashMap<>()` (`attributes`) and `LinkedHashMultiset.create()` (`values`, which internally allocates another `LinkedHashMap`), and [`BagOfValues.<init>`](xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java#L60-L63) copies them into `ImmutableMap.copyOf(b.attributes)` and `ImmutableMultiset.copyOf(b.values)` (which allocates `Multisets$ImmutableEntry[]`, `RegularImmutableMultiset`, `LinkedEntryIterator`, `AbstractMultiset$EntrySet`, and hashes every `Value` via `Value$OtherValue.hashCode`). Moreover, [`TypeToBagOfValues.empty()`](xacml-core/src/main/java/org/xacml4j/v30/types/TypeToBagOfValues.java) / `type.bagOf()` goes through `BagOfValues.Builder` even for empty or 1-element bags!
  - **Profile shares**:
    - [`top-alloc-synthetic_1000-all.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/alloc-synthetic_1000-all/top-alloc-synthetic_1000-all.md#L10-L47): `BagOfValues$Builder.<init>` (`LinkedHashMap` 6.1% + `LinkedHashMultiset` 2.9%) + `BagOfValues$Builder.attributes` (`HashMap$Node[]` 3.8% + `LinkedHashMap$Entry` 1.8%) + `BagOfValues.<init>` (`BagOfValues` 2.3% + `Multisets$ImmutableEntry[]` 2.3% + `RegularImmutableMultiset` 1.8% + `LinkedEntryIterator` 1.5% + `AbstractMultiset$EntrySet` 1.4%) = **23.9% of total allocations**.
    - [`top-alloc-rsa2008-all.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/alloc-rsa2008-all/top-alloc-rsa2008-all.md#L11-L39): `BagOfValues$Builder` and `BagOfValues.<init>` rows sum to `4.2% + 2.5% + 1.9% + 1.5% + 1.1% + 1.1% + 1.1% + 0.9% = 14.3%` of total allocations (**~6.45 KB/op**).
    - [`top-alloc-synthetic_1000-first-deny.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/alloc-synthetic_1000-first-deny/top-alloc-synthetic_1000-first-deny.md#L13-L45): `BagOfValues$Builder` and `BagOfValues.<init>` rows sum to `3.2% + 1.9% + 1.5% + 1.2% + 1.1% + 1.1% + 1.1% = 11.1%` of total allocations (**~1.76 KB/op**).
  - **External Engine Citation**:
    - OW2 AuthzForce (`authzforce/core-pdp-api`, `src/main/java/org/ow2/authzforce/core/pdp/api/value/Bags.java:43-175`) defines specialized `Empty`, `EmptyAttributeBag`, `Singleton`, and `SingletonAttributeBag` classes (storing `private final AV singleVal` directly for `getSingleElement()`), only allocating a multi-element collection when `values.size() > 1`.
    - WSO2 Balana (`wso2/balana`, `modules/balana-core/src/main/java/org/wso2/balana/ctx/xacml3/XACML3EvaluationCtx.java:108-126`) backs `BagAttribute` with a flat `ArrayList<AttributeValue>` without any `Multiset` or `Map` overhead.
- **Change**:
  1. Cache a canonical empty `BagOfValues<V>` instance per [`TypeToBagOfValues<V>`](xacml-core/src/main/java/org/xacml4j/v30/types/TypeToBagOfValues.java) so `bagType.empty()` returns a pre-allocated constant with zero allocation.
  2. In [`BagOfValues`](xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java#L54-L280) and [`BagOfValues.Builder`](xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java#L214-L280):
     - Replace `LinkedHashMap` and `LinkedHashMultiset` in `Builder` with a small `ArrayList<V>` (or single-element fast path) and `List<Attribute>` (usually 0 or 1 `Attribute` in XACML requests).
     - Store values in `BagOfValues` as an immutable `List<V>` (e.g., `List.of()` for 0, `List.of(v)` for 1, `List.copyOf(values)` for `>1`), avoiding `ImmutableMultiset` construction and `Value.hashCode()` on every bag creation. If `BagOfValues.equals(Object)` requires multiset (order-independent multiplicity) comparison, compute it lazily in `equals`/`hashCode` or only when comparing two bags of size `> 1`.
- **Expected gain**:
  - **Independent of H1** (or for unique attributes in `rsa2008` and `first-deny`):
    - **`rsa2008, request=all`**: Saves ~80% of the 14.3% `BagOfValues` allocation share = `~5.1 KB/op` (**~11.4% of total allocations**) and `~1.0 us/op`.
    - **`synthetic:1000, request=first-deny`**: Saves ~80% of the 11.1% `BagOfValues` allocation share = `~1.4 KB/op` (**~9% of total allocations**) and `~0.35 us/op`.
    - **Combined with H1 on `synthetic:1000, request=all`**: Eliminates the remaining first-lookup `BagOfValues` overhead (`~1.4 KB/op`).
- **Risk**:
  - **XACML 3.0 semantics**: XACML 3.0 §7.3.2 (Attribute bags) defines bags as unordered collections that may contain duplicate values. Standard XACML bag functions (`*-bag-size`, `*-is-in`, `*-one-and-only`, `*-bag`, `*-intersection`, `*-union`, `*-subset`, `*-at-least-one-member-of`, `*-set-equals` in [`BagFunctions.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/BagFunctions.java)) iterate `bag.values()` or check `bag.size()` / `bag.contains(v)`. Preserving `BagOfValues.equals`/`hashCode` multiset semantics for tests while storing `List<V>` internally keeps 100% behavioral compatibility.
  - **Thread safety**: `BagOfValues` remains deeply immutable (`List.of` / `List.copyOf`).
  - **API change**: [`BagOfValues.values()`](xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java#L88-L90) currently returns `Collection<V>` (`public Collection<V> values()`), **not** `Multiset<V>`! So replacing the internal `Multiset<V>` field with `List<V>` is **non-breaking** to the public API!
  - **Verification**: `BagFunctionsTest`, `BagOfValuesTest`, `Xacml30ConformanceTest`, `RSA2008InteropTest`.
- **Checked / not checked**:
  - Checked [`BagOfValues.java`](xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java#L54-L280) and verified that `values()` returns `Collection<V>` (`line 88`) and `Multiset` does not leak into any public signature of `BagOfValues` (`SUPPORTED`).

---

#### H3. Pre-bind `Match` predicate calls and add a fast binary `Value` invocation path on `FunctionSpec` (bypassing `LinkedList` normalization, `ArrayList` evaluation, `Object[]` copying, and `Method.invoke`)

- **Evidence**:
  - **Source**: In [`Match.match(EvaluationContext)`](xacml-core/src/main/java/org/xacml4j/v30/policy/Match.java#L120-L129), for every `Value<?> v` in the resolved attribute bag:
    1. Line 122 calls `predicate.invoke(context, value, v)`, which invokes the varargs method [`FunctionSpec.invoke(EvaluationContext, Expression...)`](xacml-core/src/main/java/org/xacml4j/v30/policy/FunctionSpec.java#L143-L146), allocating `Expression[2]` + `Arrays.asList(arguments)` (`Arrays$ArrayList`).
    2. [`FunctionSpecBuilder$FunctionSpecImpl.invokeWithList`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java#L363-L370) calls `normalize(arguments)` ([`lines 500-519`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java#L500-L519)), which allocates a `new LinkedList<Expression>()`, iterates `paramSpec.listIterator()` (`ImmutableList$1.get`) and `arguments.listIterator()`, and appends two `LinkedList$Node`s.
    3. Line 366 calls `evaluate(context, normalizedArgs)` ([`lines 601-608`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java#L601-L608)), which allocates `new ArrayList<Expression>(normalizedArgs.size())` and calls `exp.evaluate(context)` on two `Value` objects that are *already evaluated* (`Value.evaluate` returns `this`).
    4. [`DefaultFunctionInvocation.invoke`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java#L96-L100) allocates `new Object[numOfParms + ...]` (`Object[2]`), copies `context` and parameters into it, and calls [`FunctionInvocationFactory$Default.doInvoke`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionInvocationFactory.java#L73) -> `Method.invoke(instance, params)`.
  - **Profile shares**:
    - In [`top-cpu-synthetic_1000-all.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/cpu-synthetic_1000-all/top-cpu-synthetic_1000-all.md#L10-L75):
      - `ImmutableList$1.get` (inside `FunctionSpecImpl.normalize`): **10.7% of total CPU (self)**!
      - `Arrays.asList` (inside `FunctionSpec.invoke`): **1.8% of total CPU (self)**.
      - `FunctionSpecBuilder$FunctionSpecImpl.invokeWithList`: **17.4% of total CPU** (from 19.2% in `FunctionSpec.invoke` down to 1.8% in `EqualityPredicates.equals`). Notice that `EqualityPredicates.equals` (`StringVal.equals`) itself is only **1.8% of CPU**, while the `FunctionSpec` wrapper around it takes **17.4% of CPU** (~9.6x the cost of the actual comparison!).
    - In [`top-alloc-synthetic_1000-all.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/alloc-synthetic_1000-all/top-alloc-synthetic_1000-all.md#L17-L28): `Match.match -> FunctionSpec.invoke -> Expression[]` (2.3%) + `FunctionSpecImpl.normalize -> LinkedList$Node` (2.2%) + `Arrays$ArrayList` + `ArrayList` + `Object[]` = **~6.8% of total allocations (~144 KB/op)**.
    - In [`top-alloc-rsa2008-all.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/alloc-rsa2008-all/top-alloc-rsa2008-all.md#L10-L43): `FunctionSpecImpl.normalize -> LinkedList$Node` is the **#1 single allocation site in the entire RSA 2008 benchmark at 4.6%** (plus `FunctionSpecImpl.evaluate -> ArrayList` 1.1% + `DefaultFunctionInvocation.invoke -> Object[]` 1.1% + `Apply.evaluate -> ImmutableList.copyOf` 2.0% = **8.8% of total allocations**).
  - **External Engine Citation**:
    - OW2 AuthzForce (`authzforce/core`, `pdp-engine/src/main/java/org/ow2/authzforce/core/pdp/impl/MatchEvaluator.java:63-99`; `authzforce/core-pdp-api`, `src/main/java/org/ow2/authzforce/core/pdp/api/func/EqualTypeMatchFunction.java:65-150`) pre-validates the 2-argument signature at construction time and dispatches equality matches via a direct typed `Matcher<PARAM_T>.match(arg0, arg1)` (`arg0.equals(arg1)`), with zero list allocations and zero reflection.
- **Change**:
  1. Add a 2-argument `Value` overload on [`FunctionSpec`](xacml-core/src/main/java/org/xacml4j/v30/policy/FunctionSpec.java#L140-L157): ` <T extends ValueExp> T invoke(EvaluationContext context, Expression arg0, Expression arg1)` (or specifically for already-evaluated `Value`s in [`Match.match`](xacml-core/src/main/java/org/xacml4j/v30/policy/Match.java#L120-L129): `BooleanVal invokeMatch(EvaluationContext context, Value<?> arg0, Value<?> arg1)`).
  2. In [`FunctionSpecBuilder.FunctionSpecImpl`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java#L337-L610), pre-compute a boolean flag `hasSimpleFixedArity` at build time (true when no parameter is `Optional` or `VarArg`). When `hasSimpleFixedArity` is true:
     - Skip `normalize(arguments)` completely (no `LinkedList`, no `ListIterator`!).
     - For the 2-arg `invoke(context, arg0, arg1)` fast path, validate the two parameter specs directly by index (`paramSpec.get(0)`, `paramSpec.get(1)`) and invoke a binary invoker interface (`BiFunctionInvocation` / `MethodHandle.invokeExact`) without allocating `Expression[]`, `Arrays$ArrayList`, `LinkedList`, `ArrayList`, or `Object[]`.
     - For standard equality predicates (`string-equal`, `integer-equal`, `boolean-equal`, `anyURI-equal`, etc. in [`EqualityPredicates`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/EqualityPredicates.java)), specialize the binary invoker to `BooleanVal.of(arg0.equals(arg1))` after type validation.
  3. Also replace `new LinkedList<>()` and `new ArrayList<>()` in [`Apply.evaluate`](xacml-core/src/main/java/org/xacml4j/v30/policy/Apply.java#L118) and `FunctionSpecImpl.invokeWithList` with a pre-sized `Expression[]` or direct indexed loop for general `Apply` expressions.
- **Expected gain**:
  - **`synthetic:1000, request=all`**: Saves ~16% of total CPU (`0.16 * 480 us ≈ 76.8 us/op`) and ~6.8% of total allocations (`0.068 * 2,121,700 B ≈ 144,300 B/op`).
  - **`rsa2008, request=all`**: Saves ~11% of total CPU (`0.11 * 18.97 us ≈ 2.1 us/op`) and ~8.8% of total allocations (`0.088 * 45,115 B ≈ 3,970 B/op`).
  - **`synthetic:1000, request=first-deny`**: Saves ~8.4% of total CPU (`ImmutableList$1.get` is 5.7% CPU in `first-deny` -> `~0.55 us/op`) and ~4.5% of allocations (`~710 B/op`).
- **Risk**:
  - **XACML 3.0 semantics**: XACML 3.0 §7.7 (Target evaluation) and §A.3.1 (Equality predicates). Type-checking on `arg0` and `arg1` against `paramSpec.get(0)` and `paramSpec.get(1)` must still occur before invoking the function so invalid parameter types still raise `FunctionParametersException` -> `Indeterminate`.
  - **Thread safety**: `FunctionSpecImpl` is immutable and stateless across requests.
  - **API change**: Non-breaking (adds a default/concrete overload on `FunctionSpec` while keeping existing `invoke(EvaluationContext, List<Expression>)` and `invoke(EvaluationContext, Expression...)`).
  - **Verification**: `FunctionInvocationTest`, `MatchTest`, `ApplyTest`, `Xacml30ConformanceTest`, `RSA2008InteropTest`.
- **Checked / not checked**:
  - Checked [`Match.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/Match.java#L115-L143), [`Apply.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/Apply.java#L110-L132), [`FunctionSpec.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/FunctionSpec.java#L140-L157), [`FunctionSpecBuilder.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java#L337-L610), [`DefaultFunctionInvocation.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java#L75-L120), and [`FunctionInvocationFactory.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionInvocationFactory.java#L60-L81) (`SUPPORTED`).

---

#### H4. Compile an optional Target Equality Index in `PolicySet` / Combining Algorithms for `O(1)` candidate policy selection

- **Evidence**:
  - **Source**: In [`SyntheticCorpus.java`](xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/SyntheticCorpus.java#L120-L176), a root `PolicySet` combines `N` (`100` or `1,000`) child `PolicyIDReference`s under `deny-overrides`, where each child policy has a `<Target>` matching `string-equal("resource-<i>", AttributeDesignator(resource, resource-id, string))`. [`DenyOverrides.doCombine`](xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DenyOverrides.java#L73-L99) iterates `for (D r : rules)` over all 1,000 children sequentially. For `last-permit` and `unknown-notapplicable`, all 1,000 child targets are evaluated (`480 us/op`, `2.12 MB/op`), whereas `first-deny` takes only `6.503 us/op` (`15.8 KB/op`) ([`002-baseline-jdk25/tables.md`](openspec/changes/add-pdp-jmh-benchmarks/evidence/benchmarks/jmh/002-baseline-jdk25/tables.md#L33-L53)).
  - Even if H1–H3 reduce per-target evaluation cost by 5x (from `473 ns` to `~95 ns`), linearly scanning 1,000 policies still costs `~95 us/op` (`O(N)`).
  - **External Engine Citation**:
    - **XEngine** (Alex X. Liu et al., *"XEngine: A Fast and Scalable XACML Policy Evaluation Engine"*, SIGMETRICS / IEEE ToN), **SBA-XACML** (Mourad et al.), and **Poliseek** (*"Poliseek: A Fast and Accurate Framework for XACML Policy Evaluation"*, MDPI Applied Sciences 2022) index policy targets by extracting equality `<Match>` clauses (`*-equal` with a constant `Value` and an `AttributeDesignator` with `MustBePresent="false"`) into an inverted hash index: `AttributeDesignatorKey -> Map<Value<?>, int[]/BitSet of child indices>`, plus a `fallbackIndices` set for children with empty targets, regex/comparison predicates, `MustBePresent="true"`, or unindexed `PolicyIDReference`s.
- **Change**:
  - Introduce a package-private or internal `TargetIndex<D extends DecisionRule>` helper in `org.xacml4j.v30.policy` (or `org.xacml4j.v30.policy.combine.impl`) that inspects the list of child `DecisionRule`s of a `PolicySet` or `Policy` (resolving static in-memory `PolicyIDReference` targets when available, see H10):
    - If a child's `Target` has at least one `<AnyOf>` clause where every `<AllOf>` contains at least one standard equality `<Match>` (`FunctionSpec` is `*-equal` and `designator.isMustBePresent() == false`), index that child index under `(AttributeDesignatorKey, constantValue)`.
    - Otherwise, record the child index in an `alwaysEvaluate` bitset.
    - At evaluation time, resolve the indexed `AttributeDesignatorKey`(s) once from `EvaluationContext`:
      - If resolution throws `EvaluationException` (`Indeterminate`), fall back to sequential evaluation (preserving XACML 3.0 §7.7 / §7.14 `Indeterminate{DP/P/D}` combining semantics!).
      - Otherwise, union the matching child index sets for each value in the resolved `BagOfValues` with `alwaysEvaluate`, and iterate only the candidate children in **strictly increasing declaration index order**.
- **Expected gain**:
  - **`synthetic:1000, request=all` / `last-permit` / `unknown-notapplicable`**: Reduces child target evaluations from `500–1,000` per decision down to `0–1` per decision! Combined with H1–H3, reduces `synthetic:1000` (`last-permit` and `unknown-notapplicable`) from **480 us/op (2,122 KB/op)** down to **~7–9 us/op (~16–18 KB/op)** — a **>50x latency reduction** and **>100x allocation reduction** on 1,000-policy sets, and **~5x** on `synthetic:100` (`43.87 us/op` -> `~7–8 us/op`).
- **Risk**:
  - **XACML 3.0 semantics**:
    1. **Declaration order**: `FirstApplicable` (§C.6), `OrderedDenyOverrides` (§C.3), and `OrderedPermitOverrides` (§C.5) require evaluating applicable children in exact declaration order, and `Obligations`/`Advice` collection order can be observed by tests. Iterating candidate indices in ascending bitset/array order preserves exact declaration order.
    2. **`MustBePresent="true"` and `Indeterminate` (§7.7, §7.12–7.14)**: A `<Match>` whose `AttributeDesignator` has `MustBePresent="true"` or whose attribute resolution fails in PIP returns `Indeterminate`, which under `DenyOverrides` (§C.2) converts into `Indeterminate{D}`, `Indeterminate{P}`, or `Indeterminate{DP}` and can change `NotApplicable` or `Permit` into `Indeterminate`! By indexing *only* `MustBePresent="false"` equality matches and falling back to sequential evaluation whenever `context.resolve(key)` throws `EvaluationException`, `NotApplicable` is proven for all skipped children without ever masking an `Indeterminate` result.
    3. **`isReturnPolicyIdList()` (§5.42)**: Non-applicable policies (`MatchResult.NOMATCH`) are never added to `PolicyIdentifierList` (only evaluated applicable policies are; Note: check [`BaseCompositeDecisionRule.evaluate`](xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java#L140-L152) where `context.addEvaluationResult(this, Decision.NOT_APPLICABLE)` is called even on `NOMATCH`, while [`RootEvaluationContext.addEvaluationResult`](xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java#L276-L282) adds the policy ONLY `if (!d.isNotApplicable())`). Wait — what about `EvaluationContextHandler.addEvaluationTime`? If a listener/MBean tracks per-policy `NOMATCH` evaluation time, skipping non-matching policies changes the `NOMATCH` counter. Confirm whether any test asserts `NOMATCH` counts on skipped policies.
  - **Thread safety**: The compiled `TargetIndex` is immutable once built and safe for concurrent readers.
  - **API change**: Non-breaking.
  - **Verification**: All conformance tests (`Xacml30ConformanceTest`, `RSA2008InteropTest`) + property test comparing indexed vs unindexed evaluation across `SyntheticCorpus` and mixed `Indeterminate` / `MustBePresent="true"` policies.
- **Checked / not checked**:
  - Checked [`DenyOverrides.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DenyOverrides.java#L73-L99), [`BaseCompositeDecisionRule.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java#L130-L185), [`RootEvaluationContext.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java#L276-L282), and [`SyntheticCorpus.java`](xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/SyntheticCorpus.java#L120-L176) (`SUPPORTED`).

---

#### H5. Pre-compute Multiple Decision Profile (MDP) flags once in `RequestContext` to short-circuit `RequestContextHandlerChain` in `O(1)`

- **Evidence**:
  - **Source**: Every call to [`DefaultPolicyDecisionPoint.decide(RequestContext)`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java#L148-L178) passes the request through [`RequestContextHandlerChain`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PolicyDecisionPointBuilder.java#L123-L133):
    1. `MultiRequestsAttributeReferenceHandler` (checks `!req.getRequestReferences().isEmpty()`).
    2. `RepeatingAttributesHandler` (calls `req.containsRepeatingCategories()`).
    3. `MultipleResourcesViaXPathExpressionLegacyHandler` (calls `req.containsRepeatingCategories()` again, then `req.containsAttributeValues(CONTENT_SELECTOR, XPATH)`).
    4. [`MultipleResourcesViaXPathExpressionHandler`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java#L96-L106) (calls `req.containsRepeatingCategories()` a 3rd time, then [`containsMultipleContentSelector(request)`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java#L184-L188) which calls `request.containsAttributeValues(MULTIPLE_CONTENT_SELECTOR, XacmlTypes.XPATH)` **twice** at lines 186 and 187!).
    5. `ScopeAttributeHandler` (checks `req.getOnlyEntity(Categories.RESOURCE)` and looks up `RESOURCE_SCOPE`).
    6. Finally, [`PolicyDecisionPointContextFactory.createEvaluationContext`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PolicyDecisionPointContextFactory.java#L171) calls `req.containsRepeatingCategories()` a **4th time**!
    - Each `request.containsRepeatingCategories()` ([`RequestContext.java:274-280`](xacml-core/src/main/java/org/xacml4j/v30/RequestContext.java#L274-L280)) iterates `entities.asMap().values()`, and each `request.containsAttributeValues(...)` ([`RequestContext.java:258-269`](xacml-core/src/main/java/org/xacml4j/v30/RequestContext.java#L258-L269)) creates a Guava transformed iterator (`Iterators$9`) over `entities.values()` and runs two nested stream pipelines (`AttributeIndex.findByIssuer` and `Attribute.getValuesByType`) on every `Category` in the request!
  - **Profile shares ([`004-profile-decide-jdk25-logback`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/summary.md))**:
    - In [`top-cpu-synthetic_1000-first-deny.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/cpu-synthetic_1000-first-deny/top-cpu-synthetic_1000-first-deny.md#L56-L65): `DefaultPolicyDecisionPoint.decide` is 100.0% of CPU samples, but `DefaultPolicyDecisionPoint.requestDecision` is only **73.1%**! The handler chain consumes **26.9% of total CPU** on `first-deny` (`MultipleResourcesViaXPathExpressionHandler.handle` alone consumes `85.0% - 73.2% = 11.8%` CPU; `ScopeAttributeHandler.handle` consumes `73.2% - 73.1% + ...`; `MultipleResourcesViaXPathExpressionLegacyHandler` consumes `90.6% - 85.0% = 5.6%` CPU; `RepeatingAttributesHandler` consumes `94.7% - 90.6% = 4.1%` CPU).
    - In [`top-alloc-synthetic_1000-first-deny.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/alloc-synthetic_1000-first-deny/top-alloc-synthetic_1000-first-deny.md#L10-L65): `DefaultPolicyDecisionPoint.requestDecision` is only **76.0%** of total allocations! The handler chain consumes **24.0% of total allocations** (`MultipleResourcesViaXPathExpressionHandler` consumes `88.6% - 76.0% = 12.6%` of total allocations; `RequestContext.getEntities -> Iterators$9` is **4.2% of total allocations** at row 1).
    - In [`top-cpu-rsa2008-all.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/cpu-rsa2008-all/top-cpu-rsa2008-all.md#L56-L63) and [`top-alloc-rsa2008-all.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/alloc-rsa2008-all/top-alloc-rsa2008-all.md#L56-L63): `requestDecision` is **88.9% of CPU** and **91.8% of allocations** (the MDP handler chain wastes **11.1% of CPU** and **8.2% of allocations** on `rsa2008-all`).
  - **External Engine Citation**: WSO2 Balana (`wso2/balana`, `modules/balana-core/src/main/java/org/wso2/balana/ctx/xacml3/XACML3EvaluationCtx.java:155-204`) computes `multipleAttributes` (boolean), `multipleContentSelectors` (nullable Set), and `resourceScopeAttribute` in a single pass when the request/context is constructed.
- **Change**:
  1. In [`RequestContext.<init>`](xacml-core/src/main/java/org/xacml4j/v30/RequestContext.java#L96-L115), pre-compute and store boolean fields (or an `int` bitmask) on the immutable `RequestContext`:
     - `private final boolean repeatingCategories;`
     - `private final boolean hasMultipleContentSelector;`
     - `private final boolean hasLegacyContentSelector;`
     - `private final boolean hasResourceScope;`
  2. Have `RequestContext.containsRepeatingCategories()` return `this.repeatingCategories` in `O(1)`.
  3. Have `MultipleResourcesViaXPathExpressionHandler`, `MultipleResourcesViaXPathExpressionLegacyHandler`, and `ScopeAttributeHandler` check the pre-computed `RequestContext` boolean flags (and fix the duplicate `containsAttributeValues` call in [`MultipleResourcesViaXPathExpressionHandler.java:186-187`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java#L184-L188)), immediately delegating to `next.handle(request, contextFactory)` in `O(1)` with zero allocations when false.
- **Expected gain**:
  - **`synthetic:1000, request=first-deny`**: Eliminates almost the entire 26.9% CPU and 24.0% allocation overhead of the MDP handler chain on single-decision requests: saves `~0.25 * 6.503 us ≈ 1.63 us/op` (**~25% of fixed decision latency**) and `~0.23 * 15,816 B ≈ 3,640 B/op` (**~23% of fixed decision allocation**).
  - **`rsa2008, request=all`**: Saves `~0.10 * 18.97 us ≈ 1.90 us/op` (**~10% of total CPU**) and `~0.08 * 45,115 B ≈ 3,610 B/op` (**~8% of total allocations**).
- **Risk**:
  - **XACML 3.0 Multiple Decision Profile v1.0 (§2.1–2.4)**: Since `RequestContext` and its `Category`/`Entity`/`Attribute` children are deeply immutable once built (`RequestContext.Builder.build()`), pre-computing whether repeating categories, `multiple:content-selector`, `content-selector`, or `resource:scope` are present at `RequestContext` construction time is 100% semantically identical to scanning for them on every handler invocation.
  - **Thread safety**: `private final boolean` fields on immutable `RequestContext` are safely published.
  - **API change**: Non-breaking (package-private or public boolean getters on `RequestContext`).
  - **Verification**: `MultipleResourcesViaXPathExpressionHandlerTest`, `ScopeAttributeHandlerTest`, `RepeatingAttributesHandlerTest`, `Xacml30ConformanceTest` (Section III.F / III.G Multiple Decision tests), `RSA2008InteropTest`.
- **Checked / not checked**:
  - Checked [`RequestContext.java`](xacml-core/src/main/java/org/xacml4j/v30/RequestContext.java#L96-L285), [`MultipleResourcesViaXPathExpressionHandler.java`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java#L96-L190), and [`PolicyDecisionPointContextFactory.java`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PolicyDecisionPointContextFactory.java#L162-L176) (`SUPPORTED`).

---

#### H6. Make per-request collections in `RootEvaluationContext`, `DefaultEvaluationContextHandler`, `Policy`, and `DefaultPolicyDecisionPoint` non-concurrent and lazy

- **Evidence**:
  - **Source**:
    1. [`RootEvaluationContext.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java#L66-L127):
       - Line 66 declares `protected final Logger LOG = LoggerFactory.getLogger(RootEvaluationContext.class);` as an **instance field** (called on every `new RootEvaluationContext(...)`!) instead of `private static final Logger`.
       - Lines 107–116 eagerly allocate **5 `ConcurrentHashMap`s** (`denyAdvices`, `denyObligations`, `permitAdvices`, `permitObligations`, `evaluationStatusMap`), **1 `ConcurrentSkipListMap`** (`evaluatedPolicies`), and **1 `AtomicReference`** (`extendedIndeterminate`).
       - Lines 276–282: `addEvaluationResult(CompositeDecisionRule, Decision)` inserts into `evaluatedPolicies` (`ConcurrentSkipListMap.doPut` -> `String.compareTo`) on every matching policy/policyset **even when `request.isReturnPolicyIdList()` is `false`**, and [`DefaultPolicyDecisionPoint.requestDecision`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java#L207) simply discards `context.getEvaluatedPolicies()` when `!req.isReturnPolicyIdList()`!
    2. [`DefaultEvaluationContextHandler.java`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java#L77-L90): Eagerly allocates **2 `ConcurrentHashMap`s** (`designatorCache`, `selectorCache`) and **2 `ConcurrentLinkedDeque`s** (`attrRefEvalStack`, `policyRedResolvStack`) per request.
    3. [`Policy.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/Policy.java#L244-L253): `Policy.createEvaluationContext` allocates `new PolicyDescendantEvaluationContext(context)` which eagerly allocates `new HashMap<String, ValueExp>()` (`varDefEvalResults` at line 252) on every policy evaluation, even when `variableDefinitions.isEmpty()`!
    4. [`DefaultPolicyDecisionPoint.getResolvedAttributes`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java#L230-L250): Eagerly allocates `HashMultimap.create()` and `new LinkedList<Category>()` on every decision even when `context.getResolvedDesignators().isEmpty()`!
  - **Profile shares**:
    - In [`top-alloc-synthetic_1000-all.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/alloc-synthetic_1000-all/top-alloc-synthetic_1000-all.md#L21-L34): `Policy.createEvaluationContext -> PolicyDescendantEvaluationContext` (2.3%) + `HashMap` (1.5%) + `PolicyIDReference.createEvaluationContext` (1.2%) = **5.0% of all allocations (~106 KB/op)** across the 1,000 policies.
    - In [`top-cpu-rsa2008-all.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/cpu-rsa2008-all/top-cpu-rsa2008-all.md#L17-L29): `String.compareTo` (1.8%) + `ConcurrentSkipListMap.doPut` (1.1%) from `RootEvaluationContext.addEvaluationResult` = **2.9% of total CPU**!
    - In [`top-alloc-rsa2008-all.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/alloc-rsa2008-all/top-alloc-rsa2008-all.md#L12-L44): `Policy.createEvaluationContext` (`PolicyDescendantEvaluationContext` 3.7% + `HashMap` 2.4%) + `RootEvaluationContext.<init> -> ConcurrentHashMap` (1.3%) + `RootEvaluationContext.addEvaluationResult -> ConcurrentSkipListMap$Node` (1.1%) = **8.5% of total allocations (~3.83 KB/op)**.
    - In [`top-alloc-synthetic_1000-first-deny.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/alloc-synthetic_1000-first-deny/top-alloc-synthetic_1000-first-deny.md#L14-L53) and [`top-cpu-synthetic_1000-first-deny.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/cpu-synthetic_1000-first-deny/top-cpu-synthetic_1000-first-deny.md#L16-L32): `RootEvaluationContext.<init> -> ConcurrentHashMap` is **2.7% alloc**; `DefaultPolicyDecisionPoint.getResolvedAttributes` (`RegularImmutableMap` / `HashMultimap`) is **3.9% CPU** and **~3.5% alloc**; `DefaultEvaluationContextHandler.<init>` is **~2.0% alloc**.
  - **External Engine Citation**:
    - OW2 AuthzForce (`authzforce/core-pdp-api`, `src/main/java/org/ow2/authzforce/core/pdp/api/BaseEvaluationContext.java:49-85`; `authzforce/core`, `pdp-engine/src/main/java/org/ow2/authzforce/core/pdp/impl/policy/PolicyEvaluators.java:115-160`) uses a `private static final Logger`, plain non-concurrent maps (`HashCollections.newMutableMap()`), and collects `applicablePolicies` only when `isApplicablePolicyIdListRequested()` is `true`.
    - WSO2 Balana (`wso2/balana`, `modules/balana-core/src/main/java/org/wso2/balana/PDP.java:244-248`) only collects policy references inside `if (requestCtx.isReturnPolicyIdList())`.
- **Change**:
  1. In [`RootEvaluationContext`](xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java#L66-L282):
     - Change `LOG` to `private static final Logger LOG = LoggerFactory.getLogger(RootEvaluationContext.class);`.
     - Replace `ConcurrentHashMap`, `ConcurrentSkipListMap`, and `AtomicReference` with plain non-concurrent fields (`Map` / `TreeMap` or `LinkedHashMap`, and a plain `Decision extendedIndeterminate = Decision.INDETERMINATE` reference), initialized lazily on first write (since the majority of rules/policies have no obligations or advices!).
     - Pass `boolean trackEvaluatedPolicies` (or check whether `evaluatedPolicies` is needed; note that `DefaultPolicyDecisionPoint.getEvaluatedPolicies` also logs at debug level if `log.isDebugEnabled()`, so track `evaluatedPolicies` only when `returnPolicyIdList || log.isDebugEnabled()` or use a lightweight `ArrayList<CompositeDecisionRule>` and sort/deduplicate only when `getEvaluatedPolicies()` is called!).
  2. In [`DefaultEvaluationContextHandler`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java#L77-L90): Replace the 2 `ConcurrentHashMap`s and 2 `ConcurrentLinkedDeque`s with plain `HashMap` (for `designatorCache`), lazy `HashMap` (for `selectorCache`, since XPath selectors are rare), and `ArrayDeque` (lazy or small initial capacity 4).
  3. In [`Policy.PolicyDescendantEvaluationContext`](xacml-core/src/main/java/org/xacml4j/v30/policy/Policy.java#L247-L290): Initialize `varDefEvalResults` lazily on the first `getVariableEvaluationResult`/`setVariableEvaluationResult` call (or `Collections.emptyMap()` when `variableDefinitions.isEmpty()`).
  4. In [`DefaultPolicyDecisionPoint.getResolvedAttributes`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java#L230-L250): Add `if (desig.isEmpty()) { return Collections.emptyList(); }` at line 232 before allocating `LinkedList` and `HashMultimap.create()`.
- **Expected gain**:
  - **`synthetic:1000, request=first-deny`**: Saves `~0.14 * 6.503 us ≈ 0.91 us/op` and `~0.16 * 15,816 B ≈ 2,530 B/op`.
  - **`rsa2008, request=all`**: Saves `~0.06 * 18.97 us ≈ 1.14 us/op` and `~0.085 * 45,115 B ≈ 3,830 B/op`.
  - **`synthetic:1000, request=all`**: Lazily initializing `varDefEvalResults` in `PolicyDescendantEvaluationContext` saves `1.5% * 2,121,700 B ≈ 31,800 B/op` (**1,000 `HashMap` allocations**).
- **Risk**:
  - **XACML 3.0 semantics**:
    - §5.42 (`ReturnPolicyIdList`) and §7.18 (`Obligations` and `Advice`). Note: check if any test directly calls `context.getEvaluatedPolicies()` on a `RootEvaluationContext` without going through `DefaultPolicyDecisionPoint` — if so, replacing `ConcurrentSkipListMap` with a lazy `ArrayList<CompositeDecisionRule>` (and only sorting/deduplicating in `getEvaluatedPolicies()`) preserves `context.getEvaluatedPolicies()` unconditionally while still eliminating `ConcurrentSkipListMap.doPut` and `String.compareTo` on the hot path!
  - **Thread safety**: Each `RootEvaluationContext` and `DefaultEvaluationContextHandler` is created per individual decision request and evaluated on the calling thread (`PolicyDecisionPointContextFactory.createEvaluationContext`).
  - **API change**: Non-breaking.
  - **Verification**: `RootEvaluationContextTest`, `DefaultPolicyDecisionPointTest`, `Xacml30ConformanceTest`, `RSA2008InteropTest`.
- **Checked / not checked**:
  - Checked [`RootEvaluationContext.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java#L66-L285), [`DefaultEvaluationContextHandler.java`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java#L77-L95), [`Policy.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/Policy.java#L244-L290), and [`DefaultPolicyDecisionPoint.java`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java#L200-L250) (`SUPPORTED`).

---

#### H7. Pre-compute `hashCode` and eliminate `Objects.hash(...)` varargs array allocations in `SemanticIdentifier`, `Value.OtherValue`, `BagOfValues`, and `AttributeDesignatorKey`

- **Evidence**:
  - **Source**:
    1. [`SemanticIdentifier.hashCode()`](xacml-core/src/main/java/org/xacml4j/v30/SemanticIdentifier.java#L101-L104) (the base class of `CategoryId`, `FunctionId`, and `DataTypeId`):
       ```java
       @Override
       public final int hashCode() {
           return Objects.hash(id, abbreviatedId, getClass());
       }
       ```
       Every time a `CategoryId` is looked up in `RequestContext.entities` (`Multimap<CategoryId, Category>`), `Objects.hash` allocates an `Object[3]` varargs array (and C2 does not scalar-replace it across megamorphic/virtual `hashCode()` dispatch, hence `Arrays.copyOf` in the allocation profile) and hashes two strings + a `Class`!
    2. [`Value.OtherValue.hashCode()`](xacml-core/src/main/java/org/xacml4j/v30/types/Value.java#L127-L130) (the base class of `StringVal`, `IntegerVal`, `AnyURIVal`, etc.):
       ```java
       @Override
       public int hashCode() {
           return Objects.hash(value, getEvaluatesTo());
       }
       ```
       Allocates `Object[2]` on every `hashCode()` call!
    3. [`BagOfValues.hashCode()`](xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java#L193-L196): `Objects.hash(type, values)` allocates `Object[2]`.
    4. [`AttributeDesignatorKey.hashCode()`](xacml-core/src/main/java/org/xacml4j/v30/AttributeDesignatorKey.java#L90-L96): Calls `Objects.hash(attributeId, issuer)` (and `super.hashCode()` in `AttributeReferenceKey` calls `Objects.hash(category, dataType)`).
  - **Profile shares**:
    - In [`top-alloc-synthetic_1000-first-deny.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/alloc-synthetic_1000-first-deny/top-alloc-synthetic_1000-first-deny.md#L12-L38): `SemanticIdentifier.hashCode -> Arrays.copyOf` is **3.3% of total allocations** (row 3!) and `Value$OtherValue.hashCode -> Arrays.copyOf` is **1.2% of total allocations** = **4.5% of total allocations**.
    - In [`top-cpu-synthetic_1000-first-deny.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/cpu-synthetic_1000-first-deny/top-cpu-synthetic_1000-first-deny.md#L18): `SemanticIdentifier.hashCode` is **3.4% of total CPU (self)**!
    - In [`top-alloc-rsa2008-all.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/alloc-rsa2008-all/top-alloc-rsa2008-all.md#L13-L17): `Value$OtherValue.hashCode -> Arrays.copyOf` is **3.1% of total allocations** (row 4!) and `SemanticIdentifier.hashCode -> Arrays.copyOf` is **2.4% of total allocations** (row 8!) = **5.5% of total allocations (~2.48 KB/op)**.
    - In [`top-alloc-synthetic_1000-all.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/alloc-synthetic_1000-all/top-alloc-synthetic_1000-all.md#L19-L41): `Value$OtherValue.hashCode` (2.3%) + `SemanticIdentifier.hashCode` (1.5%) = **3.8% of total allocations (~80.6 KB/op)**.
  - **External Engine Citation**: OW2 AuthzForce (`authzforce/core-pdp-api`, `GenericAttributeProviderBasedAttributeDesignatorExpression.java:64, 152-157`) caches `hashCode` in an `int` field and avoids `Objects.hash` varargs on hot value and identifier types.
- **Change**:
  1. In [`SemanticIdentifier`](xacml-core/src/main/java/org/xacml4j/v30/SemanticIdentifier.java#L41-L105), pre-compute `private final int hashCode` in the constructor using primitive arithmetic:
     `this.hashCode = 31 * (31 * (31 + id.hashCode()) + Objects.hashCode(abbreviatedId)) + getClass().hashCode();` and return `this.hashCode` from `hashCode()`. Also add a fast path in `equals(Object)`: `if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; SemanticIdentifier<?> that = (SemanticIdentifier<?>) o; return this.hashCode == that.hashCode && Objects.equals(id, that.id) && Objects.equals(abbreviatedId, that.abbreviatedId);`.
  2. In [`Value.OtherValue`](xacml-core/src/main/java/org/xacml4j/v30/types/Value.java#L103-L131), replace `Objects.hash(value, getEvaluatesTo())` with `31 * (31 + value.hashCode()) + getEvaluatesTo().hashCode()` (or cache it in an `int` field).
  3. In [`BagOfValues.hashCode()`](xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java#L193-L196), [`AttributeReferenceKey.hashCode()`](xacml-core/src/main/java/org/xacml4j/v30/AttributeReferenceKey.java), and [`AttributeDesignatorKey.hashCode()`](xacml-core/src/main/java/org/xacml4j/v30/AttributeDesignatorKey.java#L90-L96), replace `Objects.hash(...)` with `31 * a + b` arithmetic and pre-compute `hashCode` in `AttributeDesignatorKey.<init>`.
- **Expected gain**:
  - **`synthetic:1000, request=first-deny`**: Saves `0.045 * 15,816 B ≈ 712 B/op` (**4.5% of total allocations**) and `0.034 * 6.503 us ≈ 0.22 us/op` (**3.4% of total CPU**).
  - **`rsa2008, request=all`**: Saves `0.055 * 45,115 B ≈ 2,480 B/op` (**5.5% of total allocations**) and `~0.015 * 18.97 us ≈ 0.28 us/op`.
  - **`synthetic:1000, request=all`** (standalone): Saves `0.038 * 2,121,700 B ≈ 80,600 B/op`.
- **Risk**:
  - **XACML 3.0 semantics**: None (`31 * (31 * (31 + a) + b) + c` is the exact mathematical definition of `Arrays.hashCode(new Object[]{a, b, c})` used by `Objects.hash(a, b, c)`!).
  - **Thread safety**: Immutable `final int hashCode` on immutable classes. Note: compatible with future JDK 28 JEP 401 value classes as well (`final` instance field initialized in constructor).
  - **API change**: Non-breaking (produces the exact same `int` hash code value as `Objects.hash`).
  - **Verification**: Existing unit tests for `SemanticIdentifier`, `Value`, `AttributeDesignatorKey`, and `BagOfValues`.
- **Checked / not checked**:
  - Checked [`SemanticIdentifier.java`](xacml-core/src/main/java/org/xacml4j/v30/SemanticIdentifier.java#L41-L105), [`Value.java`](xacml-core/src/main/java/org/xacml4j/v30/types/Value.java#L103-L131), [`BagOfValues.java`](xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java#L193-L196), and [`AttributeDesignatorKey.java`](xacml-core/src/main/java/org/xacml4j/v30/AttributeDesignatorKey.java#L40-L96) (`SUPPORTED`).

---

#### H8. Pre-compile constant regular expressions at policy/expression construction and enable a bounded cache in `RegExpFunctions`

- **Evidence**:
  - **Source**: In [`RegExpFunctions.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java#L176-L216) and [`line 396`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java#L396):
    - Line 178: `private final static boolean CACHE_REGXP = Boolean.parseBoolean(System.getProperty(CACHE_REGEXP_PROP_NAME));` — **regex caching is `false` by default**!
    - On every single `string-regexp-match`, `anyURI-regexp-match`, `ipAddress-regexp-match`, `dnsName-regexp-match`, `rfc822Name-regexp-match`, or `x500Name-regexp-match` invocation, [`regexpMatch(StringVal, Value)`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java#L390-L406) calls [`compilePattern(String)`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java#L194-L216), which runs:
      1. `covertXacmlToJavaSyntax(regExpStr)` (`lines 180-192`): runs `XACML_PATTERNS.matcher(xacmlRegExp)`, allocates a `new StringBuffer()`, and allocates a `new StringBuilder().append(".*").append(...).append(".*")` (Note: wrapping with `.*...*` also fails when the regex contains top-level alternation `a|b` or multiline newlines unless parenthesized, and triggers catastrophic backtracking on non-matching strings compared to `matcher.find()`!).
      2. `withoutQuoting(jdk)` and `new RepetitionScanner(withoutQuoting).scan()`.
      3. `Pattern.compile(jdk)`.
  - **External Engine Citation**:
    - OW2 AuthzForce (`authzforce/core-pdp-api`, `src/main/java/org/ow2/authzforce/core/pdp/api/func/RegexpMatchFunctionHelper.java:89-165`) checks `argExpressions.get(0).getValue()` when constructing a function call (`Match` or `Apply`); when the regex argument is a constant `StringValue`, `getCompiledRegexMatchCall` compiles the regex **once at policy construction time** into `CompiledRegexMatchFunctionCall` and reuses `compiledRegex.containsMatch(...)` on every evaluation with zero runtime compilation!
    - Also aligns with the sibling change [`openspec/changes/add-regex-engine-spi/proposal.md`](openspec/changes/add-regex-engine-spi/proposal.md).
- **Change**:
  1. Enable a bounded concurrent cache by default in [`RegExpFunctions`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java#L176-L216) (e.g., Guava `CacheBuilder.newBuilder().maximumSize(1024).build()` or a small lock-free ring/ConcurrentHashMap bounded at 1,024 entries instead of `CACHE_REGXP = false` or an unbounded `ConcurrentHashMap`).
  2. When [`Match`](xacml-core/src/main/java/org/xacml4j/v30/policy/Match.java#L65-L90) or [`Apply`](xacml-core/src/main/java/org/xacml4j/v30/policy/Apply.java#L62-L85) is constructed with a `*-regexp-match` predicate and a constant `StringVal` argument (`Match.value` is always a constant `Value` in XACML `<Match>`!), pre-compile (or warm the cache for) the pattern at construction time.
  3. Replace the `new StringBuilder().append(".*").append(result.toString()).append(".*")` substring wrapper + `matcher.matches()` in [`RegExpFunctions.java:191, 405`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java#L191) with `matcher.find()` (after verifying that all existing regex tests pass; see Risk below).
- **Expected gain**:
  - In policies using `*-regexp-match` in `<Target>` or `<Condition>` (such as URI/resource regex policies and conformance tests), eliminates `Pattern.compile` + `StringBuffer` + `RepetitionScanner` (`~1.5–4.0 us` and `~2–5 KB` per regex match invocation), yielding a **10x–30x speedup** on regex match evaluation. (In `rsa2008-all` and `synthetic:1000`, `*-regexp-match` is not used in the corpus policies, so the gain is targeted to regex-bearing policies and `FunctionInvocationBenchmark`).
- **Risk**:
  - **XACML 3.0 semantics**: XACML 3.0 §A.3.13 (`string-regexp-match` and other `*-regexp-match` functions) specifies W3C XML Schema Part 2 / XQuery 1.0 & XPath 2.0 Functions and Operators (`xf:matches`) regular expression semantics (unanchored substring match unless anchored with `^`/`$`). Note: if an invalid regex is in an `<Apply>` branch that is never evaluated at runtime (e.g., short-circuited `and`/`or`), eager throwing at `<Apply>` construction time would turn a runtime-unreachable `Indeterminate` into a policy parse error. Therefore, if pre-compiling at construction time fails with `PatternSyntaxException` / `IllegalArgumentException`, defer the exception to evaluation time (`FunctionParametersException` -> `Indeterminate`), which preserves exact XACML 3.0 lazy expression evaluation semantics!
  - **Thread safety**: `java.util.regex.Pattern` is immutable and thread-safe (`Matcher` is created per call via `pattern.matcher(input)`).
  - **API change**: Non-breaking.
  - **Verification**: `RegExpFunctionsTest`, `Xacml30ConformanceTest` (`IIA`, `IIB`, `IIC` regex cases).
- **Checked / not checked**:
  - Checked [`RegExpFunctions.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java#L176-L408) and AuthzForce `RegexpMatchFunctionHelper.java` (`SUPPORTED`).

---

#### H9. Replace `Method.invoke` in `FunctionInvocationFactory` with `MethodHandle` / `LambdaMetafactory` and eliminate exception stack-trace churn on expected XACML `Indeterminate` / missing-attribute paths

- **Evidence**:
  - **Source**:
    1. [`FunctionInvocationFactory.java:60-78`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionInvocationFactory.java#L60-L78):
       ```java
       protected Object doInvoke(Object... params) {
           try {
               return m.invoke(instance, params);
           } catch (Exception t) {
               throw new RuntimeException(t);
           }
       }
       ```
       And [`DefaultFunctionInvocation.java:96-114`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java#L96-L114) catches `Exception e`, unwraps `Throwable cause = e.getCause().getCause()`, and rethrows `(FunctionParametersException) cause`!
       Every time a XACML function such as [`BagFunctions.oneAndOnlyImpl`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/BagFunctions.java#L75-L84) encounters an empty bag (which happens routinely in `rsa2008` when a rule's `<Condition>` checks an optional subject/resource attribute via `string-one-and-only`), **three separate exceptions with full stack traces** are allocated and filled in per evaluation:
       - `new FunctionParametersException(...)` (in `BagFunctions.oneAndOnlyImpl`),
       - `new InvocationTargetException(target)` (inside `Method.invoke`),
       - `new RuntimeException(t)` (inside `FunctionInvocationFactory$Default.doInvoke`),
       followed by `LOG.debug(e.getMessage(), e)` and `new ExpressionInvocationException(spec, e)` in [`FunctionSpecBuilder.java:371-385`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java#L371-L385) (**a 4th stack trace!**)!
  - **Profile shares**:
    - In [`top-cpu-rsa2008-all.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/cpu-rsa2008-all/top-cpu-rsa2008-all.md#L11-L21):
      - `java_lang_Throwable::fill_in_stack_trace(Handle, int, TRAPS)` is the **#2 self-CPU hotspot in the entire RSA 2008 benchmark at 2.7% of total CPU** (321 samples)!
      - `java.security.AccessController.getStackAccessControlContext()` (triggered by reflective class/stack inspection) is **#4 self-CPU at 2.0% of total CPU** (240 samples)!
    - In [`top-alloc-rsa2008-all.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/alloc-rsa2008-all/top-alloc-rsa2008-all.md#L30): `BagFunctions.oneAndOnlyImpl -> String.format -> Formatter` is **1.3% of total allocations** (plus the 4 exception objects and their stack trace arrays).
  - **External Engine Citation**:
    - OW2 AuthzForce (`authzforce/core-pdp-api`, `GenericAttributeProviderBasedAttributeDesignatorExpression.java:58-61`, `RegexpMatchFunctionHelper.java:147-149`) pre-creates `ImmutableXacmlStatus` / `IndeterminateEvaluationException` or avoids reflective `Method.invoke` wrapper exceptions completely.
- **Change**:
  1. In [`FunctionInvocationFactory`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionInvocationFactory.java#L41-L80) and [`DefaultFunctionInvocation`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java#L75-L116):
     - Replace `Method.invoke` with a `MethodHandle` (`MethodHandles.lookup().unreflect(m)` / `asSpreader`), or throw `EvaluationException` directly from `doInvoke` without wrapping in `InvocationTargetException` and `new RuntimeException(t)`!
  2. In [`EvaluationException`](xacml-core/src/main/java/org/xacml4j/v30/EvaluationException.java) subclasses used for control-flow `Indeterminate` results (`FunctionParametersException`, `MissingAttributeException`, `ExpressionInvocationException`), add a lightweight constructor calling `super(message, cause, false, false)` (`writableStackTrace = false`) when `LOG.isDebugEnabled()` is `false`, and avoid formatting `String.format("Found \"%d\" values in the bag, expected only one", bag.size())` in `BagFunctions.oneAndOnlyImpl` unless needed (e.g., pass static message `"Bag size is not 1"` when `bag.isEmpty()`).
- **Expected gain**:
  - **`rsa2008, request=all`**: Eliminates the 4x stack-trace fill-in (`2.7%` CPU) + `String.format` (`1.3%` alloc) + `InvocationTargetException` + `RuntimeException` allocations on every `one-and-only` empty-bag evaluation: saves `~0.045 * 18.97 us ≈ 0.85 us/op` (**~4.5% of total CPU**) and `~0.035 * 45,115 B ≈ 1,580 B/op` (**~3.5% of total allocations**).
- **Risk**:
  - **XACML 3.0 semantics**: XACML 3.0 §7.9 / §7.15 (`Status` and `Indeterminate` handling). In `xacml4j`, [`Rule.evaluate`](xacml-core/src/main/java/org/xacml4j/v30/policy/Rule.java#L155-L176) and [`BaseDecisionRule.evaluate`](xacml-core/src/main/java/org/xacml4j/v30/policy/BaseDecisionRule.java#L110-L135) catch `EvaluationException` and extract `e.getStatus()` (not the Java stack trace) to build the XACML `Status` (`urn:oasis:names:tc:xacml:1.0:status:processing-error` or `missing-attribute`). Keeping `writableStackTrace = LOG.isDebugEnabled()` preserves full stack traces during debugging while eliminating `Throwable.fillInStackTrace` in production/benchmark runs.
  - **Thread safety**: `MethodHandle` is immutable and thread-safe.
  - **API change**: Non-breaking.
  - **Verification**: `BagFunctionsTest`, `RuleTest`, `Xacml30ConformanceTest`, `RSA2008InteropTest`.
- **Checked / not checked**:
  - Checked [`FunctionInvocationFactory.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionInvocationFactory.java#L41-L80), [`DefaultFunctionInvocation.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java#L75-L116), [`FunctionSpecBuilder.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java#L363-L387), and [`BagFunctions.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/BagFunctions.java#L75-L84) (`SUPPORTED`).

---

#### H10. Cache or pre-resolve `PolicyIDReference` / `PolicySetIDReference` to avoid Guava `LocalCache` and `Pair` lookups on every evaluation

- **Evidence**:
  - **Source**: Every time a [`PolicyIDReference`](xacml-core/src/main/java/org/xacml4j/v30/policy/PolicyIDReference.java#L80-L93) (or [`PolicySetIDReference`](xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySetIDReference.java#L80-L93)) is evaluated (both in `createEvaluationContext` at line 91 and in `evaluate`), it calls `context.resolve(this)` -> [`DefaultEvaluationContextHandler.resolve(PolicyIDReference)`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java#L204-L234) -> [`DefaultPolicyReferenceResolver.resolve(PolicyIDReference)`](xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/DefaultPolicyReferenceResolver.java#L105-L120).
  - Look at what happens on each child `PolicyIDReference` in [`BaseCompositeDecisionRule.CombineAlgorithmContext`](xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java#L316-L320):
    1. `r.createEvaluationContext(context)` calls [`PolicyIDReference.createEvaluationContext`](xacml-core/src/main/java/org/xacml4j/v30/policy/PolicyIDReference.java#L87-L93), which calls `context.resolve(this)` (**lookup #1** in `DefaultPolicyReferenceResolver`), pushes/pops `policyRedResolvStack`, allocates `new PolicyIDReferenceEvaluationContext(context, ref)` (**1.2% alloc** in `synthetic:1000-all`), and then calls `ref.createEvaluationContext(...)` which allocates `new PolicyDescendantEvaluationContext(...)` (**2.3% alloc**)!
    2. Next, `r.evaluate(combineContext)` calls [`PolicyIDReference.evaluate`](xacml-core/src/main/java/org/xacml4j/v30/policy/PolicyIDReference.java#L80-L85), which calls `context.resolve(this)` a **second time** (**lookup #2** in `DefaultPolicyReferenceResolver`)!
    3. Inside [`DefaultPolicyReferenceResolver.resolve`](xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/DefaultPolicyReferenceResolver.java#L105-L120), when `ref.getVersion()` is present (as in `SyntheticCorpus`), it allocates `Pair.of(ref.getId(), ref.getVersion())` and calls `resolvedPolicyDirectRefs.getIfPresent(key)` (`LocalCache$Segment.getEntry`). When `ref.getVersion()` is null (as in `rsa2008` `<PolicyIdReference>urn:oasis:names:tc:xacml:2.0:interop:rsa8:policy:...</PolicyIdReference>`), `isDirectPolicyReference(ref)` returns **`false`** (`line 161`: `ref.getId() != null && ref.getVersion() != null`), so **`rsa2008` misses the direct cache completely** and hits `resolvedPolicyRefs` (`Pair<String, VersionMatchConstraints>`) or the repository on every call!
  - **Profile shares**:
    - In [`top-cpu-synthetic_1000-all.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/cpu-synthetic_1000-all/top-cpu-synthetic_1000-all.md#L16-L31): `LocalCache$Segment.getEntry` is **2.0% of total CPU (self)** + `DefaultPolicyReferenceResolver.resolve` (0.9% self) + `DefaultEvaluationContextHandler.resolve` (0.9% self) = **3.8% of total CPU**.
    - In [`top-cpu-rsa2008-all.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/cpu-rsa2008-all/top-cpu-rsa2008-all.md#L33-L35): `LocalCache$Segment.getEntry` is **0.9% of total CPU** + `DefaultEvaluationContextHandler.resolve` is **0.9% of total CPU**.
    - In [`top-alloc-synthetic_1000-all.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/alloc-synthetic_1000-all/top-alloc-synthetic_1000-all.md#L34): `PolicyIDReference.createEvaluationContext -> PolicyIDReferenceEvaluationContext` is **1.2% of total allocations**.
  - **External Engine Citation**: OW2 AuthzForce (`authzforce/core`, `PolicyEvaluators.java`, `StaticRefPolicyProvider`) resolves static policy references once when the policy tree is wired, avoiding repeated cache lookups per request.
- **Change**:
  1. In [`PolicyIDReference.evaluate`](xacml-core/src/main/java/org/xacml4j/v30/policy/PolicyIDReference.java#L80-L93) and [`PolicySetIDReference.evaluate`](xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySetIDReference.java#L80-L93): `PolicyIDReferenceEvaluationContext` (created in `createEvaluationContext` right before `evaluate`!) *already* resolved `Policy ref = context.resolve(this)` and passed `ref` to `new PolicyIDReferenceEvaluationContext(context, ref)`! Simply store `resolvedPolicy` on `PolicyIDReferenceEvaluationContext` (or resolve inside `evaluate` instead of resolving twice in both `createEvaluationContext` and `evaluate`!), immediately cutting `context.resolve(this)` calls in **half** (from 2 per reference to 1)!
  2. In [`DefaultPolicyReferenceResolver`](xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/DefaultPolicyReferenceResolver.java#L60-L165), key the cache directly by `PolicyIDReference` / `PolicySetIDReference` (which already implements `equals` and `hashCode` in [`BasePolicyReference.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/BasePolicyReference.java)) instead of allocating a `Pair.of(ref.getId(), ...)` on every lookup!
- **Expected gain**:
  - **`synthetic:1000, request=all`**: Saves `~0.03 * 480 us ≈ 14.4 us/op` and `~0.015 * 2,121,700 B ≈ 31,800 B/op`.
  - **`rsa2008, request=all`**: Saves `~0.02 * 18.97 us ≈ 0.38 us/op` and `~500 B/op`.
- **Risk**:
  - **XACML 3.0 semantics**: Circular policy reference detection (`policyRedResolvStack.contains(ref)` in [`DefaultEvaluationContextHandler.java:209, 241`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java#L209)) must still run when entering a policy reference. Performing the resolution and cycle check once when creating the reference context (and storing the resolved `Policy`/`PolicySet` in that context) preserves exact circular-reference detection!
  - **Thread safety**: Safe for concurrent requests.
  - **API change**: Non-breaking.
  - **Verification**: `PolicyIDReferenceTest`, `PolicySetIDReferenceTest`, `DefaultPolicyReferenceResolverTest`, `Xacml30ConformanceTest`, `RSA2008InteropTest`.
- **Checked / not checked**:
  - Checked [`PolicyIDReference.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/PolicyIDReference.java#L80-L93), [`BaseCompositeDecisionRule.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java#L316-L320), [`DefaultEvaluationContextHandler.java`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java#L204-L265), and [`DefaultPolicyReferenceResolver.java`](xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/DefaultPolicyReferenceResolver.java#L105-L165) (`SUPPORTED`).

---

#### H11. Eliminate intermediate `String` and `StringVal` allocations in `EqualityPredicates.equalsIgnoreCase`

- **Evidence**:
  - **Source**: [`EqualityPredicates.equalsIgnoreCase(StringVal, StringVal)`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/EqualityPredicates.java#L108-L114):
    ```java
    public static BooleanVal equalsIgnoreCase(
            @FuncParam @XacmlFuncParam StringVal a,
            @FuncParam @XacmlFuncParam StringVal b) {
        return _eq(
                StringVal.of(a.get().toLowerCase(Locale.ROOT)),
                StringVal.of(b.get().toLowerCase(Locale.ROOT)));
    }
    ```
    Every `string-equal-ignore-case` (XACML 3.0) or `string-equal-ignore-case` / `rfc822Name-equal` call allocates two lower-cased `String` objects and two `StringVal` wrappers (even when `a.get().length() != b.get().length()` or `a.get().equals(b.get())`!).
  - **External Engine Citation**: WSO2 Balana (`EqualFunction.java`) and AT&T XACML (`FunctionDefinitionEquality.java`) compare strings directly without wrapping lower-cased copies in new `AttributeValue` objects.
- **Change**:
  - In [`EqualityPredicates.equalsIgnoreCase`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/EqualityPredicates.java#L108-L114), check `String s1 = a.get(), s2 = b.get(); if (s1.equals(s2)) return BooleanVal.TRUE;` and compare via `s1.equalsIgnoreCase(s2)` (or `s1.toLowerCase(Locale.ROOT).equals(s2.toLowerCase(Locale.ROOT))` only when non-ASCII characters are present, avoiding all `String` and `StringVal` allocations for ASCII strings).
- **Expected gain**:
  - Saves 4 object allocations (`2 String` + `2 StringVal`, ~128–256 B) and `~40–80 ns` per `string-equal-ignore-case` call.
- **Risk**:
  - **XACML 3.0 semantics**: XACML 3.0 §A.3.1 (`urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case`) references `xf:compare` / Unicode case folding (`Locale.ROOT`). Fast-pathing ASCII with `equalsIgnoreCase` (or `s1.length() != s2.length()` for ASCII) and falling back to `s1.toLowerCase(Locale.ROOT).equals(s2.toLowerCase(Locale.ROOT))` without `StringVal.of(...)` wrapping preserves 100% of Unicode case-folding edge cases (such as `ß`) while eliminating both `StringVal` allocations unconditionally and `String` allocations on ASCII inputs.
  - **Thread safety**: Stateless.
  - **API change**: Non-breaking.
  - **Verification**: `EqualityPredicatesTest`, `Xacml30ConformanceTest`.
- **Checked / not checked**:
  - Checked [`EqualityPredicates.java:108-114`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/EqualityPredicates.java#L108-L114) (`SUPPORTED`).

---

#### H12. Replace Stream pipelines and `UnmodifiableIterator` allocations in `BaseCompositeDecisionRule.evalAdvices`/`evalObligations`, `Target`, `MatchAnyOf`, and `MatchAllOf` with indexed loops

- **Evidence**:
  - **Source**:
    1. [`BaseCompositeDecisionRule.evalObligations`](xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java#L212-L220) and [`evalAdvices`](xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java#L222-L230):
       ```java
       return obligations.stream()
               .filter(o -> o.isFulfillOn(d))
               .map(o -> o.evaluate(context))
               .collect(Collectors.toList());
       ```
       Runs two stream pipelines on **every** matching `Policy` and `PolicySet` decision (Permit or Deny), even when `obligations.isEmpty()` and `advices.isEmpty()`!
    2. [`Target.matchAnyOf`](xacml-core/src/main/java/org/xacml4j/v30/policy/Target.java#L65-L80), [`MatchAnyOf.match`](xacml-core/src/main/java/org/xacml4j/v30/policy/MatchAnyOf.java#L63-L88), and [`MatchAllOf.match`](xacml-core/src/main/java/org/xacml4j/v30/policy/MatchAllOf.java#L68-L89) use enhanced-for `for (Match m : allOf)` over `List` (`ImmutableList`), which allocates/calls `ImmutableList$Itr` (`AbstractIndexedListIterator.next`).
  - **Profile shares**:
    - In [`top-cpu-synthetic_1000-first-deny.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/cpu-synthetic_1000-first-deny/top-cpu-synthetic_1000-first-deny.md#L27-L85): `BaseCompositeDecisionRule.evalAdvices` is **1.6% of total CPU (self)** and `2.5%` total (`64.6% - 59.1% = 5.5%` of total CPU is spent in `evalObligations` + `evalAdvices` on a policy with **zero** obligations and **zero** advices!).
    - In [`top-alloc-synthetic_1000-first-deny.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/alloc-synthetic_1000-first-deny/top-alloc-synthetic_1000-first-deny.md#L78-L82): `evalObligations` + `evalAdvices` account for `69.0% - 62.6% = 6.4%` of total allocations on `first-deny` (**~1.01 KB/op** for empty obligations/advices!).
    - In [`top-cpu-synthetic_1000-all.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/cpu-synthetic_1000-all/top-cpu-synthetic_1000-all.md#L27-L33): `Target.matchAnyOf` (1.1% self) + `AbstractIndexedListIterator.next` (0.9% self) = **2.0% of total CPU**.
  - **External Engine Citation**: OW2 AuthzForce (`authzforce/core`, `pdp-engine/src/main/java/org/ow2/authzforce/core/pdp/impl/policy/PolicyEvaluators.java:105-113`) uses `DP_WITHOUT_EXTRA_PEP_ACTION_RESULT_FACTORY` when a `Policy` or `PolicySet` has no obligations/advice, bypassing obligation/advice evaluation completely when empty!
- **Change**:
  1. In [`BaseDecisionRule.evaluate`](xacml-core/src/main/java/org/xacml4j/v30/policy/BaseDecisionRule.java#L118-L125) and [`BaseCompositeDecisionRule`](xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java#L168-L233): Guard `if (!obligations.isEmpty())` and `if (!advices.isEmpty())` before calling `evalObligations`/`evalAdvices`, and replace the stream pipeline in `evalObligations` and `evalAdvices` with an indexed `for (int i = 0, n = obligations.size(); i < n; i++)` loop.
  2. Replace enhanced-for loops in [`Target.matchAnyOf`](xacml-core/src/main/java/org/xacml4j/v30/policy/Target.java#L65-L80), [`MatchAnyOf.match`](xacml-core/src/main/java/org/xacml4j/v30/policy/MatchAnyOf.java#L63-L88), and [`MatchAllOf.match`](xacml-core/src/main/java/org/xacml4j/v30/policy/MatchAllOf.java#L68-L89) with indexed `for (int i = 0, n = list.size(); i < n; i++)` loops.
- **Expected gain**:
  - **`synthetic:1000, request=first-deny`**: Saves `~0.055 * 6.503 us ≈ 0.36 us/op` (**5.5% of total CPU**) and `~0.064 * 15,816 B ≈ 1,010 B/op` (**6.4% of total allocations**).
  - **`synthetic:1000, request=all`**: Saves `~0.02 * 480 us ≈ 9.6 us/op` (**2.0% of total CPU**).
- **Risk**:
  - **XACML 3.0 semantics**: XACML 3.0 §7.18 (Obligations and Advice expressions). Guarding empty lists and using indexed loops preserves exact evaluation order and exception propagation.
  - **Thread safety**: Stateless iteration over `ImmutableList`.
  - **API change**: Non-breaking.
  - **Verification**: `ObligationTest`, `AdviceTest`, `Xacml30ConformanceTest`, `RSA2008InteropTest`.
- **Checked / not checked**:
  - Checked [`BaseCompositeDecisionRule.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java#L168-L233), [`BaseDecisionRule.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/BaseDecisionRule.java#L118-L125), [`Target.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/Target.java#L65-L80), [`MatchAnyOf.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/MatchAnyOf.java#L63-L88), and [`MatchAllOf.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/MatchAllOf.java#L68-L89) (`SUPPORTED`).

---

### Summary of Expected Cumulative Gains

| Benchmark Scenario | Baseline (`002-baseline-jdk25`) | After H1–H3, H5–H7, H9–H12 (No Target Index) | After H1–H12 (With H4 Target Index) |
| --- | --- | --- | --- |
| `synthetic:1000, request=first-deny` | `6.503 us/op`, `15,816 B/op` | `~2.4–2.9 us/op`, `~5.0–6.5 KB/op` (**~2.4x faster, ~65% less alloc**) | `~2.4–2.9 us/op`, `~5.0–6.5 KB/op` |
| `synthetic:1000, request=all` / `last-permit` | `480 us/op`, `2,121,700 B/op` | `~140–175 us/op`, `~250–350 KB/op` (**~3x faster, ~85% less alloc**) | `~3.5–5.5 us/op`, `~6.5–9.0 KB/op` (**~90x faster, >99% less alloc**) |
| `rsa2008, request=all` | `18.97 us/op`, `45,115 B/op` | `~9.5–11.5 us/op`, `~16–20 KB/op` (**~1.8x faster, ~58% less alloc**) | `~8.5–10.5 us/op`, `~15–18 KB/op` |

---

### Refuted Ideas (Checked and Rejected)

1. **Cross-request decision caching (`Map<RequestContext, ResponseContext>` or `Map<RequestContext, Result>` in `DefaultPolicyDecisionPoint`)**:
   - **Why refuted**: Checked [`DefaultPolicyDecisionPoint.java`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java#L148-L216) and [`DefaultEvaluationContextHandler.java`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java#L159-L175). In XACML 3.0, policies may depend on environment attributes (`current-time`, `current-date`, `current-dateTime`, or custom PIP resolvers with per-attribute TTLs), and in production workloads `RequestContext` instances vary per request (e.g., request IDs, timestamps, or subject tokens). Worse, hashing an entire `RequestContext` on every call adds overhead on cache misses and benchmarks artificially. Per-request memoization of `AttributeDesignatorKey` (H1) is both spec-compliant (XACML 3.0 §7.3.5) and zero-risk.
2. **Short-circuiting `DenyOverrides` / `PermitOverrides` by skipping `Indeterminate` child targets (`MustBePresent="true"`) via a naive hash index**:
   - **Why refuted**: Checked XACML 3.0 §7.7 (Target evaluation), §7.14 (PolicySet evaluation), and Appendix C.2 (`deny-overrides`) / [`DenyOverrides.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DenyOverrides.java#L73-L135). In XACML 3.0, if Policy A returns `Permit` and Policy B's `<Target>` evaluates to `Indeterminate` (for example, because an `<AttributeDesignator MustBePresent="true"/>` is missing from the request), and Policy B contains a `Deny` rule (`Indeterminate{D}` or `Indeterminate{DP}`), `DenyOverrides` MUST return `Indeterminate{DP}`, **not** `Permit`! A naive target index that skips Policy B because the attribute is missing from the request would silently return `Permit`, violating XACML 3.0 §C.2 and failing conformance tests. That is why H4 strictly restricts indexing to `MustBePresent="false"` equality matches and falls back on any resolution error.
3. **Replacing `String.matches(regex)` directly as AT&T XACML does (`FunctionDefinitionRegexpMatch.java:96`)**:
   - **Why refuted**: Checked AT&T XACML (`att/xacml-3.0`, `xacml-pdp/src/main/java/com/att/research/xacmlatt/pdp/std/functions/FunctionDefinitionRegexpMatch.java:96`), which calls `elementValueString.matches(regexpValue)`. Under XACML 3.0 §A.3.13 and XQuery/XPath 2.0 `xf:matches`, regular expressions are **unanchored** (matching any substring unless `^`/`$` are used) and use XML Schema character class syntax (`[\i-[:]]`). Using `String.matches` (which implicitly anchors `^...$` in Java) violates XACML 3.0 §A.3.13 and breaks conformance tests.
4. **Replacing `AtomicLong` counters in `DefaultPolicyDecisionPoint` (`noOfDecisions`, `totalDecisionTime`)**:
   - **Why refuted**: Checked [`top-cpu-synthetic_1000-first-deny.md`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/cpu-synthetic_1000-first-deny/top-cpu-synthetic_1000-first-deny.md) and [`DefaultPolicyDecisionPoint.java:96-99, 155-177`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java#L155-L177). On single-threaded JMH benchmarks, uncontended `AtomicLong.incrementAndGet()` does not appear in any top-50 CPU table (`<0.1%` CPU).

---

### Open Questions for the Maintainer / Conductor

1. **Target Index (`H4`) and `EvaluationContextHandler.addEvaluationTime` metrics**:
   - In [`BaseCompositeDecisionRule.evaluate`](xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java#L140-L185), the `finally` block calls `context.addEvaluationTime(this, matchResult, d, duration)` even when `matchResult == MatchResult.NOMATCH` (though [`DefaultEvaluationContextHandler.addEvaluationTime`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java#L276-L282) is currently a complete no-op `{}`!). Can `PolicySet` target indexing skip calling `createEvaluationContext` / `evaluate` on statically proven `NOMATCH` child policies without notifying `addEvaluationTime`?
2. **Regex Engine SPI Alignment (`H8`)**:
   - Should constant-regex pre-compilation on `Match` / `Apply` (`H8`) be implemented directly against `java.util.regex.Pattern` in `optimize-pdp-evaluation`, or wired through the SPI proposed in [`openspec/changes/add-regex-engine-spi/proposal.md`](openspec/changes/add-regex-engine-spi/proposal.md)?
3. **`BagOfValues.equals(Object)` Semantics (`H2`)**:
   - Does any caller outside unit tests rely on order-independent `BagOfValues.equals(Object)` (e.g., `bagOf("a", "b").equals(bagOf("b", "a"))`), or should `BagOfValues` store a flat `List<V>` and perform order-independent comparison lazily inside `equals`/`hashCode`?

### Verification & Residual Risk

- **Verification performed**:
  - Read-only inspection of all 6 profile tables in [`004-profile-decide-jdk25-logback`](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback/summary.md), baseline tables in [`002-baseline-jdk25/tables.md`](openspec/changes/add-pdp-jmh-benchmarks/evidence/benchmarks/jmh/002-baseline-jdk25/tables.md) and [`001-baseline/summary.md`](openspec/changes/add-pdp-jmh-benchmarks/evidence/benchmarks/jmh/001-baseline/summary.md), and 25+ `xacml4j` production classes (`SUPPORTED`).
  - Cross-checked external engine implementations by fetching primary source files from `authzforce/core`, `authzforce/core-pdp-api`, `wso2/balana`, and `att/xacml-3.0` (`SUPPORTED`).
  - Confirmed zero files modified and zero build/benchmark processes executed in compliance with read-only lane rules.
- **Residual risk**:
  - Profile shares overlap slightly between callers and callees when multiple hypotheses touch the same call chain (e.g., H1 and H2 both reduce `BagOfValues` allocations on `synthetic:1000-all`; arithmetic above separates standalone vs combined savings). Every hypothesis should be validated individually with a JMH baseline/candidate pair per [`docs/ai/BENCHMARKS.md`](docs/ai/BENCHMARKS.md).
