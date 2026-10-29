=== BEGIN REPORT P-FIXED-opus ===
FINAL REPORT (policy4j_implementer, Claude Opus 5.5)

## Summary

This packet is analysis only. No production code changed.

- **`narrow` costs 4,936 B/op** (unprofiled). The profiled fork measured 4,880 B/op, and every byte of it is accounted for below.
- **IncludeInResult (IIR) dominates.**
  - 61% (2,967 B) is spent turning the request's 4 IIR attributes into 4 new `Category`/`Entity`/`AttributeIndex` copies and scanning for them.
  - The `Result` constructor's eager structural hash takes another 14% (676 B). It is never read during a decision.
  - Measured by CPU, these two take 29% and 19% of the decision.
- **`wide` costs 21.8 B per unread attribute** (+872 B/op for 40). All of it is one 24-byte Guava singleton iterator per attribute key that the IIR scan visits (`AttributeIndex.find`).
  - Indexing, copying, evaluation, and the hash pay nothing per unread attribute.
  - A category without IIR attributes would cost 0 B per unread attribute, because D32 skips it.
- **rsa2008 costs 3,052 B/op.** It has no IIR. Its fixed cost is in:
  - per-composite contexts and staging (36%);
  - the per-decision request-bag memo (11%);
  - the PIP session built for attributes that no resolver supplies (10%);
  - the `Result` obligation maps (10%).
- **The fixes outside the other lanes' files** would bring `narrow`/`wide` from about 4.9/5.8 KB to about 0.4 KB per decision in the benchmark.
  - The real saving is smaller (about 1.7 KB left on `narrow`) when each request is decided only once.
  - rsa2008 would drop from about 3.0 to 2.1 KB (−31%).
- **Ranked list.** Two fixes are each worth at least 0.4 KB on `narrow` or `wide`, with no XACML risk:
  1. Make `Result.hashCode` lazy.
  2. Record the IIR attributes when the index is built.

## Method

1. Built the reactor (`mvn -o -q -Dlicense.skipUpdateLicense=true -DskipTests package`, JDK 25, Maven 3.9.16).
   Settings for every run: `PdpDecideBenchmark.decide -p store=in-memory -p targetIndex=false -f 1 -wi 5 -i 5 -jvmArgs -XX:-UseJVMCICompiler`, with `-Djmh.ignoreLock=true` on the host JVM as instructed.
2. Allocation profiling:
   - Settings: async-profiler 4.5, `-prof 'async:libPath=/opt/homebrew/lib/libasyncProfiler.dylib;event=alloc;alloc=64k;output=jfr;dir=…'`, plus `-prof gc`.
   - The JFR was converted with `jfrconv --alloc --total --lines -o collapsed`. That gives byte-weighted stacks with line numbers.
   - JMH 1.37's own collapsed output counts samples, not bytes (pr13, §"Profile").
   - The B/op of a stack is (its bytes ÷ all bytes in the measured window) × the run's `gc.alloc.rate.norm`. 99.99% of the window's bytes are under `decide`.
   - Each stack matches an object of the expected size. For example, four 80-byte `HashMap` tables are 320 B, and the profile shows 322.0. So the per-site error is within ±3%.
3. CPU profiling: `event=cpu;interval=100000;output=collapsed`, with inclusive shares per phase.
4. Unprofiled B/op on this build, `-prof gc` only:

   | Workload | B/op | µs/op |
   | --- | ---: | ---: |
   | `narrow` | 4,936.1 ± 1.1 | 1.588 |
   | `wide` | 5,808.1 ± 1.2 | 1.702 |
   | rsa2008 `all` | 3,051.8 ± 0.4 | 2.652 |

   - The profiled forks measured 4,880.2, 5,744.2, and 3,015.6 B/op. The difference of about 60 B between forks comes from which iterators and builders C2 scalar-replaces, for example a 40-byte `Result.Builder` that is present in one fork and gone in the other.
   - Times are indicative only: other lanes ran timed smokes at the same time, with load average 6 to 8. B/op is the evidence.
5. The benchmark decides pre-built requests. Parsing and building the request's attribute index are outside every number here. Bags kept in the request's index (cycle AF) are built once and reused.

## Accounting: every byte of `narrow` (profiled fork, 4,880 B/op)

| Phase | Site | Objects per decision | B/op |
| --- | --- | --- | ---: |
| **A Request handling and response** | `MultipleResourcesBudgetHandler.java:83` | `Budget` | 24.5 |
| | `AbstractRequestContextHandler.java:79` | `SingletonImmutableList` of the one result | 16.0 |
| | `PolicyDecisionPointContextFactory.java:154` | per-call PDP context (`…$1`) | 33.4 |
| | `DefaultPolicyDecisionPoint.java:114` | `NumberedDecisions` | 21.8 |
| | `DefaultPolicyDecisionPoint.java:141` | `ResponseContext` | 16.7 |
| | | **subtotal** | **112.4** |
| **B Request parsing and attribute index** | outside the benchmark (pre-built request; index and bag memo built once) | — | **0** |
| **C1 Per-decision contexts** | `PolicyDecisionPointContextFactory.java:231`, `RootEvaluationContext.java:195` | `RootEvaluationContext` 87.8, `Optional` 17.1 | 104.9 |
| | `PolicyDecisionPointContextFactory.java:226` | `DefaultEvaluationContextHandler` | 48.2 |
| | `PolicySet.java:188`, `Policy.java:192`, `Rule.java:69` | 3 descendant contexts | 94.4 |
| | | **subtotal** | **247.5** |
| **C2 Resolution snapshot** (request-bag memo) | `DefaultEvaluationContextHandler.java:172` and `:321` | `ConcurrentHashMap` 63.5, table[16] 79.5, 2 nodes 66.1 | **209.1** |
| **D Target evaluation** (no conditions in `narrow`) | `TargetBeforeContext.java:176-178` → `RequestContext.java:258` and `AttributeIndex.java:290` | 6 `Optional` (94.6) and 6 `AttributeIndex$Lookup` (145.9): one each per child pre-check | 240.5 |
| | `DefaultEvaluationContextHandler.java:318` → `AttributeIndex.java:290` | 2 `Lookup` | 47.3 |
| | | **subtotal** | **287.8** |
| **E1 IIR scan** | `RequestContext.java:353` → `Category.java:107` → `AttributeIndex.java:152-161` | per category: `ArrayList(0)` and `Object[1]` (186.9); one Guava iterator per attribute key and per key list (239.2) | **426.1** |
| **E2 IIR copies** (617 B per category ×4) | `RequestContext.java:353-365` → `Entity$Builder` → `AttributeIndex$Builder` | multimap builder: `AttributeIndex.Builder`, `ImmutableListMultimap.Builder`, `LinkedHashMap`, `HashMap` table[16], entry, `ArrayList`, `Object[10]` | 1,249.3 |
| | `AttributeIndex.java:80` (`build()`) | `AttributeIndex`, `ImmutableListMultimap`, `SingletonImmutableBiMap`, entry set, `Object[1]`, `SingletonImmutableList`, `Map.Entry[1]`, `ImmutableMapEntry` | 961.1 |
| | `Entity$Builder.build`, `Category$Builder.build:262` | `Entity`, `Category` | 258.3 |
| | `RequestContext.java:356, :365` | list builder, `Object[4]`, list | 71.9 |
| | | **subtotal** | **2,540.6** |
| **F1 `Result` eager hash** | `Result.java:70-79` → `Category.hashCode:201`, `Entity.hashCode:238`, `AttributeIndex.hashCode:414`, `Attribute.hashCode:136` | varargs `Object[]` of the three `Objects.hashCode` calls (351.3); Guava entry sets, entries, and iterators of the map hashes (324.9) | **676.2** |
| **F2 `Result` and IIR map** | `DefaultPolicyDecisionPoint.java:305-317`, `Result.java:311-316`, `:64-65` | `Result` 48.1, `Result.Builder` 39.0, `ImmutableMap.Builder` 32.9, `Entry[4]` 33.4, 4 `ImmutableMapEntry` 96.9, iterator 33.0, `RegularImmutableMap` 40.8, entry array 31.4, chained entry 24.5 | **380.0** |
| **Total** | | | **4,879.7** (profile 4,879.9) |

CPU share for `narrow` (1.86 µs/op under profiling):

| Phase | CPU share |
| --- | ---: |
| IIR | 28.7% (scan 6.7%, copies 22.0%) |
| Target pre-check | 23.0% |
| `Result` constructor | 19.3% (hash alone 16.5%) |
| Policy evaluation | 13.5% |
| Handlers, numbering, and response | 6.7% |
| Request attribute resolution | 5.3% |
| `createResult` (rest) | 3.0% |

The pre-check's CPU is mostly the target match itself (`FunctionSpecImpl.scan`, `BagStorage.equal`), moved before context creation. It is not overhead.

**Change since my I-CORPUS baseline.** `narrow` went from 4,736 to 4,936 B/op on the current tree (+200). The new `TargetBeforeContext.cannotMatch` pre-check accounts for 240 B/op of `Optional` and `Lookup` objects. It is likely the cause, but that is UNVERIFIED: the old tree was not profiled. Fixes 4a and 4b remove these objects without touching `TargetBeforeContext`.

## `wide`: the cost of an unread attribute

- **Size.** (5,808.1 − 4,936.1) / 40 = **21.8 B per unread attribute per decision**. Time is +0.11 µs for 40, about 2.8 ns each (indicative).
- **Where it is paid.** The IIR scan alone grows by +872.8 B/op: 426.0 → 1,298.8.
  - The path is `RequestContext.getIncludeInResultAttributes()` (`RequestContext.java:353`) → `Category.getIncludeInResultAttributes()` (`Category.java:107`) → `Entity.find` → `AttributeIndex.find` (`AttributeIndex.java:155`).
  - `AttributeIndex.find` iterates `ImmutableListMultimap.values()`. Each key's value list is iterated by a new `Iterators.singletonIterator`: +963.7 B for 40 keys, 24.1 B each.
  - Small differences in other iterators (−96 B) are escape-analysis variance between forks.
  - The scan visits every attribute of a category that has at least one IIR attribute. `wide` puts 10 unread attributes in each of the 4 IIR categories.
- **Not paid** in parsing or indexing (outside the benchmark), in the IIR copies (only IIR attributes are copied), in target evaluation, in designator resolution, or in the hash.
- **When it would be zero.** A category without IIR attributes is skipped through `Entity.hasIncludeInResultAttributes()` (D32), so its unread attributes cost 0 B per decision.

## rsa2008: where its 3,016 B/op go (profiled fork)

| Group | B/op | Share | Contents |
| --- | ---: | ---: | --- |
| Contexts and staging | 1,079.2 | 36% | `StagedResponses` 333.6; PolicySet contexts 280.3; `PolicySetIDReference` contexts 127.6; Rule contexts 116.7; Policy contexts 70.5; root, handler, and `Optional` 150.6 |
| Obligations and advice | 420.5 | 14% | obligation builders' `LinkedHashMultimap`s, staging lists, the root context's maps |
| Functions | 413.6 | 14% | `FunctionSpecImpl.evaluate` lists and iterators, variadic arrays, bag iterators |
| Request-bag memo | 323.2 | 11% | about 5.6 request designators per decision |
| PIP session for attributes missing from the request | 305.7 | 10% | flights map, per-decision PIP, `DecisionSets`, `CompletableFuture`, `Frame`, `ScopedValue` carrier and snapshot, lambdas, resolver list and iterator |
| `Result` and obligation maps | 297.5 | 10% | |
| Handlers and numbering | 89.5 | 3% | |
| Other evaluation | 40.8 | 1% | |
| Eager `Result` hash | 29.1 | 1% | |
| `ResponseContext` | 16.3 | 1% | |

- CPU (3.28 µs/op under profiling): policy evaluation 67%, target pre-check 12%, handlers 6%, request resolution 6%, PIP session 3%, `Result` constructor 3%.

## Ranked fixes

How to read the figures:

- "Benchmark" is what the JMH pair will show.
- "Real" is the saving when each request is decided once, which is the production case.
- Byte figures are B/op from the profiles. µs figures are indicative and come from the CPU shares applied to the unprofiled times.
- "Independent" means the fix touches none of the files of Codex Terra (`TargetIndex`, the index) or Claude Fable (`BaseDecisionRule`, `TargetBeforeContext`, `DecisionCombiningAlgorithms`, the combine implementations, `FunctionSpec`, `FunctionSpecImpl`, and probably `RootEvaluationContext`).

### 1. Lazy `Result.hashCode`

| | `narrow` | `wide` | rsa2008 |
| --- | ---: | ---: | ---: |
| Saved, benchmark and real | −676 B, ≈ −0.26 µs | −684 B, ≈ −0.26 µs | −29 B, ≈ −0.04 µs |

- **Site and mechanism.**
  - `Result.java:70-79` computes the D22 hash fold in the constructor.
  - The fold hashes every IIR `Category` deeply (`Entity`, `AttributeIndex`, its `ImmutableListMultimap`, each `Attribute`).
  - It allocates varargs arrays and Guava entry sets and iterators.
  - Nothing in a decision reads it. The only caller in the engine is `ResponseContext.hashCode`.
- **Files:** `Result.java`. **Independent.**
- **XACML risk:** none (hash codes are not normative). D22 asks for the same hash values, and the same fold computed later gives them.
- **Sketch** (the idiom of `Status.hashCode`, `Status.java:197-206`):

  ```java
  private int hashCode;	// computed on first use; the fold of design D22

  @Override
  public int hashCode(){
  	int h = hashCode;
  	if(h == 0){
  		h = 1;
  		h = 31 * h + Objects.hashCode(decision);
  		h = 31 * h + Objects.hashCode(status);
  		h = 31 * h + Objects.hashCode(associatedAdvice);
  		h = 31 * h + Objects.hashCode(obligations);
  		h = 31 * h + Objects.hashCode(includeInResultAttributes);
  		h = 31 * h + Objects.hashCode(policyReferences);
  		h = 31 * h + Objects.hashCode(resolvedAttributes);
  		hashCode = h;
  	}
  	return h;
  }
  ```

### 2. Record the IIR attributes when the attribute index is built

| | `narrow` | `wide` | rsa2008 |
| --- | ---: | ---: | ---: |
| Benchmark | −426 B, ≈ −0.11 µs | −1,299 B, ≈ −0.2 µs | 0 |
| Real (one small list per IIR entity at build, about 70 B each) | ≈ −170 B | ≈ −1,040 B | 0 |

- **Site and mechanism.** `AttributeIndex.find` (`AttributeIndex.java:152-161`) scans every attribute with per-key iterators on every decision (E1). Instead, the builder loop (`AttributeIndex.java:449-456`) already tests `isIncludeInResult()` for the D32 flag. It can collect those attributes once.
- **Effect.** The per-unread-attribute cost goes to zero.
- **Files:** `AttributeIndex.java`, `Entity.java`, `Category.java:107` (delegate). **Independent** of this packet's lanes, but `AttributeIndex` was Fable's in batch 2; please confirm.
- **XACML risk:** none. It is the same attributes, so §5.46 and §5.48 hold ("A list of attributes that were part of the request").
- **Order.** The copy's own index regroups by id as today. Only a direct caller of `Category.getIncludeInResultAttributes()`, with the same id repeated non-adjacently in one category, would see request order instead of id-grouped order.
- **Serialization.** If `AttributeIndex` is `Serializable` (it has `transient` fields), keep the list `transient`, recompute it after deserialization, and pin `serialVersionUID` before adding methods.
- **Sketch:**

  ```java
  // AttributeIndex.Builder.attributes(...): collect while adding (the loop already tests the flag, D32)
  if(attr.isIncludeInResult()){
  	included().add(attr);	// an ImmutableList.Builder created by the first such attribute
  }

  // AttributeIndex
  /** The attributes marked to be included in the result (XACML 3.0 §5.46), known since the index was built */
  public Collection<Attribute> includeInResultAttributes(){
  	return included;	// ImmutableList.of() when the D32 flag is false
  }
  ```

### 3. Build the IIR category once per `Category`; reuse the category when all its attributes are IIR

| | `narrow` | `wide` | rsa2008 |
| --- | ---: | ---: | ---: |
| Benchmark | −2,541 B, ≈ −0.35 µs | −2,527 B, ≈ −0.35 µs | 0 |
| Real | ≈ −1,234 B (subject and action are wholly IIR, so no copy at all) | 0 (every category is mixed) | 0 |

- **Site and mechanism.** `RequestContext.getIncludeInResultAttributes()` (`RequestContext.java:344-365`) builds a new `Category`, `Entity`, and `AttributeIndex` on every decision, at 617 B each (E2).
- **Real savings in the multiple decision profile.** There, individual requests share categories, so the copy is made once per category instead of once per individual decision.
- **Files:** `Category.java`, `RequestContext.java`, plus an attribute count on `Entity`. **Independent.**
- **XACML risk:**
  - §5.48: the result holds the request's IIR attributes, so the content is unchanged.
  - §5.44: `<Content>` is not copied today, so reuse is allowed only when the entity has no content.
  - `Category` is `Serializable` with a default `serialVersionUID`. Pin it with `serialver` before adding a method.
- **Sketch:**

  ```java
  // Category
  private transient Category includedInResult;	// built on first use

  /** This category with its IncludeInResult attributes only, and without content (XACML 3.0 §5.46, §5.48) */
  public Category includeInResultCategory(){
  	Category c = includedInResult;
  	if(c == null){
  		Collection<Attribute> included = getIncludeInResultAttributes();
  		c = included.size() == entity.attributeCount() && entity.getContent().isEmpty() ? this
  				: Category.builder(categoryId).id(refId).entity(Entity.builder().attributes(included).build()).build();
  		includedInResult = c;
  	}
  	return c;
  }
  ```

- **A cheaper copy** would need Guava's `expectedValuesPerKey` and `builderWithExpectedKeys` (about −88 B per copied key). `javap` shows neither in Guava 31.1-jre, the project's version, so that needs an upgrade: a dependency change through OpenSpec.

### 4. Allocation-free request lookups, then no per-decision request-bag memo

| | `narrow` | `wide` | rsa2008 |
| --- | ---: | ---: | ---: |
| Saved, real | −497 B | −533 B | −323 B |

Time is about neutral (±0.03 µs): one hash lookup per repeated test instead of the map's creation and `putIfAbsent`.

- **4a. `Category` holds its `Optional<Entity>`.** It is created once, so `RequestContext.getEntity` (`RequestContext.java:251-259`) returns it instead of `Optional.of` per call. That is −16 B for each call that escapes: 6 per `narrow` decision, through `TargetBeforeContext.java:176`.
- **4b. `AttributeIndex.resolve(AttributeDesignatorKey)`** (`AttributeIndex.java:314`) keeps its bag under the designator key. That key caches its hash (`AttributeDesignatorKey.java:90-95`).
  - Today the method builds a `Lookup` record (24 B) and hashes it, through `Enum.hashCode`, on every call.
  - On a miss, it resolves through the `Lookup` path once and stores the same `Optional` under the designator key, so every path sees one instance (D17).
  - Saves −192 B (8 objects) on `narrow` and −216 B on `wide`. On rsa2008, C2 already scalar-replaces these objects.
- **4c. Drop `requestDesignators` for request bags** (`DefaultEvaluationContextHandler.java:169-173, :264-273, :313-327`).
  - The entity's memo (cycle AF) already returns one `Optional<BagOfValues>` per designator for the life of the request. That is stronger than the per-decision memo that §7.3.5 consistency needs. The engine reads §7.3.5 as "every later test of an attribute uses the bag first tested" (D17).
  - Saves C2: −209, −206, and −323 B.
- **Files:** `Category.java`, `RequestContext.java`, `AttributeIndex.java` (confirm, as in fix 2), `DefaultEvaluationContextHandler.java`. **Independent.** `TargetBeforeContext` and `RootEvaluationContext` stay unchanged.
- **XACML risk:** low. §7.3.5 holds through the entity memo. D34 observers would report `REQUEST` instead of `REQUEST_MEMO` for repeated tests. That is a maintainer decision, and it may change the observer tests.
- **Sketch:**

  ```java
  // AttributeIndex
  public Optional<BagOfValues> resolve(AttributeDesignatorKey key){
  	ConcurrentMap<Object, Optional<BagOfValues>> kept = bags;
  	Optional<BagOfValues> bag = kept != null ? kept.get(key) : null;
  	if(bag != null){
  		return bag;
  	}
  	bag = resolve(key.getAttributeId(), key.getDataType(), key.getIssuer());
  	if(bag.isEmpty()){
  		return bag;
  	}
  	Optional<BagOfValues> first = bags().putIfAbsent(key, bag);
  	return first != null ? first : bag;
  }

  // DefaultEvaluationContextHandler.doDesignatorResolve
  Optional<BagOfValues> found = entity.get().resolve(key);	// one Optional per attribute for the request (cycle AF)
  if(found.isPresent()){
  	o.resolvedFrom(AttributeSource.REQUEST);
  	return found;
  }
  ```

### 5. PIP fast path for keys that no resolver supplies

| | `narrow` | `wide` | rsa2008 |
| --- | ---: | ---: | ---: |
| Saved, real when the PIP has no resolver for the key | 0 | 0 | −306 B (10%), ≈ −0.08 µs |

- **Site and mechanism.** `DefaultEvaluationContextHandler.doPipResolve` (`:329-336`) builds the whole session machinery and only then learns that no resolver matches:
  - flights map;
  - `DefaultPolicyInformationPoint.forDecision` with its `DecisionSets`, `ConcurrentHashMap`, and `Optional`;
  - `CompletableFuture`, `Frame`, `ScopedValue` carrier and snapshot, and two lambdas;
  - `DefaultResolverRegistry.getMatchingAttributeResolver`'s `ArrayList`.
- **Fix.** Ask first, through an additive `PolicyInformationPoint.canResolve(AttributeReferenceKey)` that defaults to `true`. `DefaultPolicyInformationPoint` answers it from the registry, ideally from a set of keys precomputed at registration. When the answer is no, return the empty result that the attempt would have returned.
- **Files:** `DefaultEvaluationContextHandler.java`, `PolicyInformationPoint.java`, `DefaultPolicyInformationPoint.java`, `DefaultResolverRegistry.java`. Not listed for Terra or Fable, but the PIP classes were Fable's AL lane; please confirm.
- **XACML risk:** low. A missing attribute gives the same empty bag, and `MustBePresent` gives the same Indeterminate with `missing-attribute` (§7.3.5, §7.19). D27's single flight and task 30's session matter only when a resolver runs. The D34 events for a key with no resolver must stay as they are.

### 6. Keep the IIR categories as a list in `Result`

| | `narrow` | `wide` | rsa2008 |
| --- | ---: | ---: | ---: |
| Saved, real | −293 B | ≈ −260 B | 0 |

Time ≈ −0.02 µs.

- **Site and mechanism.** `Result.Builder.includeInResultAttr` (`Result.java:311-316`) re-keys the request's `ImmutableList<Category>` into an `ImmutableMap.Builder`, and `Result.java:64-65` builds the map.
- **Fix.** Keep the list.
  - `getIncludeInResultByCategory` becomes a linear scan over 4 or fewer categories, keeping its existing nullable contract.
  - `equals` compares by category id, order-insensitive as today.
  - The hash term is Σ(id.hashCode() ^ category.hashCode()), the map's hash, so D22's values hold.
  - A repeated category id is still rejected, as `ImmutableMap.Builder.build()` rejects it today.
- **Files:** `Result.java`. **Independent.**
- **XACML risk:** none (§5.48 order is not specified; request order is kept).

### 7. Constant obligations and advice

| | rsa2008 |
| --- | ---: |
| Saved, real | ≈ −126 B, ≈ −0.03 µs |

- **Site and mechanism.** `BaseDecisionRuleResponseExpression.evaluateAttributeAssignments` (`:117-147`) and `Obligation.builder` → `DecisionRuleResponse.Builder.<init>:112` build a `LinkedHashMultimap` and a new `Obligation` on every evaluation.
- **Fix.** An expression without assignment expressions, or with only literal `AttributeValue`s, always gives the same immutable response. Build that response once, at construction. In addition, `DecisionRuleResponse.Builder` should create its multimap on the first attribute.
- **Files:** `ObligationExpression.java`, `AdviceExpression.java`, `BaseDecisionRuleResponseExpression.java`, `DecisionRuleResponse.java`. **Independent**, though they are in the `policy` package.
- **XACML risk:** none. The id, fulfill-on, and assignments are the same, so §7.18 holds.

### 8. `Result` obligations without the staging `LinkedHashMap`

| | rsa2008 |
| --- | ---: |
| Saved, real | ≈ −110 B |

- **Site and mechanism.** `createResult` (`DefaultPolicyDecisionPoint.java:305-313`) passes the root context's values, which already have one obligation per id. `Result.Builder.addObligation` (`Result.java:388-404`) then stages them in a `LinkedHashMap`, and `ImmutableMap.copyOf` copies them again (`Result.java:61`).
- **Fix.** When the builder is empty, copy into `ImmutableMap.builderWithExpectedSize` (available in Guava 31.1). A repeated id takes today's merge path.
- **Files:** `Result.java`. **Independent.**
- **XACML risk:** none. The order is the root map's order, as today, and §7.18 merging is unchanged.

### 9. Multiple-decision checks from build-time flags

| | `narrow` | `wide` | rsa2008 |
| --- | ---: | ---: | ---: |
| Saved, real | ≈ −0.05 µs | ≈ −0.05 µs | ≈ −0.1 µs |
| With a single-request fast path that also skips `Budget` and the list | −40 B, ≈ −0.1 µs | −40 B, ≈ −0.1 µs | −36 B, ≈ −0.15 µs |

- **Site and mechanism.** `MultipleResourcesViaXPathExpressionHandler.java:186-187` calls `RequestContext.containsAttributeValues` (`:288-298`) twice per request. Each call walks every category through `Entity.hasValues`. That is 2.4 to 2.7% of CPU in `AttributeIndex.attributes`, plus 0.5 to 0.9% in the walk.
- **Fix.** D9 already proposed precomputed attribute-value flags; only `containsRepeatingCategories` (`:282`) became O(1). Record during the request build whether an attribute uses either content-selector id.
- **Files:** `RequestContext.java`, `MultipleResourcesViaXPathExpressionHandler.java`, `DefaultPolicyDecisionPoint.java:133-142`. **Independent.**
- **XACML risk:** medium for the fast path. Every Multiple Decision Profile trigger must be in the flag: request references, repeated categories, and content selectors. The profile's section numbers are UNVERIFIED (not fetched). A wrong flag would skip an expansion silently, so add one test per handler.

### Combined

| Workload | Benchmark | Real (each request decided once) |
| --- | --- | --- |
| `narrow` | 4,880 → about 410 B/op | about 1,700 B/op |
| `wide` | 5,744 → about 400 B/op | — |
| rsa2008 | 3,016 → about 2,090 B/op (−31%) | — |

- Time, indicative: `narrow` −0.8 µs (about −50%), rsa2008 −0.3 µs (about −11%).

## In other lanes' files (not proposed for this lane)

- **Contexts and staging** (rsa2008 1,079 B, 36%; `narrow` 248 B).
  - Sites: `Policy/PolicySet/Rule/PolicySetIDReference/PinnedReference.createContext`, `BaseCompositeDecisionRule.StagedResponses` (next to Fable's `BaseDecisionRule`), and `RootEvaluationContext` (probably Fable's).
  - Ideas, all UNVERIFIED for semantics:
    - a rule without variable definitions evaluated in its policy's context (rsa2008 −117 B);
    - one context for a reference and its target (−128 B);
    - the staging object created on the first staged response (−334 B).
- **`TargetBeforeContext`** (Fable). Fixes 4a and 4b remove its 240 B/op of `Optional` and `Lookup` with no change to its file. Its CPU is the target match itself.
- **`FunctionSpecImpl.evaluate`** (Fable). Use an index loop instead of for-each over the immutable argument list (rsa2008 −94 B of `Iterators$ArrayItr`). This is my FIX-AG residual 3.
- **`RootEvaluationContext`** (probably Fable).
  - The root obligation and advice `ConcurrentHashMap`s cost rsa2008 about 103 B.
  - The `Optional` at `RootEvaluationContext.java:195` costs 16 B on every decision.

## Residual risk

1. **Production versus benchmark.** The benchmark reuses pre-built requests. Fixes 2 and 3 are shown with both figures. Per-request caches only move work when each request is decided once, as D32 Y5 already notes ("overstated by request reuse").
2. **Sampling and escape analysis.** Per-site figures are within about ±3%. The total differs by about 60 B between forks because scalar replacement differs. Re-measure with a D12 pair, not one fork.
3. **Times.** All are indicative: `-Djmh.ignoreLock=true` with other lanes' smokes running, load average 6 to 8, one fork.
4. **Ownership.** `AttributeIndex` and the PIP classes are not in this packet's lists, but earlier batches gave them to Fable. Please confirm before assigning fixes 2, 4, and 5.
5. **Serialization.** Fixes 2 to 4 add members to serializable classes that use default `serialVersionUID`s (`Category`, probably `AttributeIndex`). Pin each value first.

## Raw profiles

All are kept in the worktree under `target/p-fixed/` (gitignored, not evidence):

- `alloc-rules/…request-{narrow,wide}…/jfr-alloc.jfr`, `alloc-rsa/…/jfr-alloc.jfr`: JFR allocation recordings.
- `alloc-{narrow,wide,rsa}.collapsed`: byte-weighted stacks.
- `{narrow,wide,rsa}-stacks.txt`, `*-view.txt`, `rsa-phases.txt`: per-stack B/op.
- `cpu-{narrow,rsa}/…/collapsed-cpu.csv`: CPU stacks.
- `gc-rules.json`, `gc-rsa.json`: unprofiled B/op.
- `alloc-*.json`, `cpu-*.json`: JMH logs of the profiled runs.

The analysis scripts (`sites.py`, `phases.py`, `cpu_phases.py`, `view.py`) are in `<scratchpad>/p-fixed/`.

XACML citations are from the OASIS XACML 3.0 Plus Errata 01 complete HTML, read 2026-10-28: §5.29, §5.30, §5.44, §5.46, §5.48, §7.3.5.

=== END REPORT P-FIXED-opus ===
