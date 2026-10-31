# D-AIDX: Response Entity `AttributeIndex` Without Guava Multimap Builder Staging

- **Author**: Lane `agyx_1` (Gemini Argon)
- **Date**: 2026-10-29
- **Change**: `optimize-pdp-evaluation` (Task 42.16)
- **Mode**: Design proposal and options analysis (read-only)

---

## 1. Evidence & Exact Histogram Breakdown

### 1.1 Evidence Labels

- `SUPPORTED`:
  - Guava 31.1 bytecode (`rtk proxy javap -c -p` on `guava-31.1-jre.jar`: `ImmutableListMultimap`, `ImmutableListMultimap$Builder`, `ImmutableMultimap`, `ImmutableMultimap$Builder`, `AbstractMultimap`, `ImmutableList`).
  - Exact class histograms for `pip:8 independent` in [pr23-astra-fixed3-profile.md](`.ntm/worktrees/xacml4j--batch2/agyx_1/openspec/changes/optimize-pdp-evaluation/research/pr23-astra-fixed3-profile.md`) (5,864 B/decision before AY; 5,624 B/decision after AY) and cycle AZ's post-change histogram in [histo-after-pip8-independent.txt](`.ntm/worktrees/xacml4j--batch2/cc_1/target/i-az/histo-after-pip8-independent.txt`) / [REPORT-I-AZ-opus.md](`.ntm/worktrees/xacml4j--batch2/cc_1/REPORT-I-AZ-opus.md`) (5,648 B/decision on AZ's pre-AY base).
  - Source inspection of [`AttributeIndex`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/AttributeIndex.java`), [`Entity`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/types/Entity.java`), [`Category`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/Category.java`), [`Result`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/Result.java`), [`DefaultPolicyDecisionPoint`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java`), XML marshallers ([`Xacml30RequestContextFromJaxbToObjectModelMapper`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30RequestContextFromJaxbToObjectModelMapper.java`)), JSON marshallers ([`ResultAdapter`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-json/src/main/java/org/xacml4j/v30/marshal/json/ResultAdapter.java`), [`CategoryAdapter`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-json/src/main/java/org/xacml4j/v30/marshal/json/CategoryAdapter.java`)), and tests ([`AttributeIndexTest`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/test/java/org/xacml4j/v30/AttributeIndexTest.java`), [`ResolvedAttributesPdpTest`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/test/java/org/xacml4j/v30/pdp/ResolvedAttributesPdpTest.java`)).
  - OASIS XACML 3.0 Core Specification (`xacml-3.0-core-spec-os-en.html`), §5.29 `<AttributeDesignator>`, §5.46 `<Attribute>`, §5.48 `<Result>`, and §7.3.5 Attribute Retrieval, fetched via `tavily_extract`.

### 1.2 Guava 31.1 Bytecode Facts (`SUPPORTED`)

1. **`ImmutableListMultimap.of(K, V, ...)` (1 to 5 key-value pairs)**:
   - Every overload from 1 to 5 pairs in `com.google.common.collect.ImmutableListMultimap` calls `ImmutableListMultimap.builder()`, calls `builder.put(k, v)` for each pair, and calls `builder.build()`.
   - Only the 0-arg `ImmutableListMultimap.of()` avoids `Builder` (returning `EmptyImmutableListMultimap.INSTANCE`).
2. **`ImmutableListMultimap.Builder` / `ImmutableMultimap.Builder`**:
   - There is no `builderWithExpectedSize(int)` on `ImmutableListMultimap` or `ImmutableMultimap`. The sole constructor `ImmutableMultimap$Builder()` initializes `builderMap = Platform.preservesInsertionOrderOnPutsMap()` (`new LinkedHashMap<>()`, 64 B + `HashMap$Node[16]` 80 B on first put).
   - For each distinct key, `put` calls `newMutableValueCollection()`, which returns `new ArrayList<>()` (24 B + `Object[10]` 56 B on first `add`).
   - `build()` invokes package-private `ImmutableListMultimap.fromMapEntries(builderMap.entrySet(), null)`, which allocates `new ImmutableMap.Builder<>(entrySet.size())` (32 B + `Map.Entry[size]` 24 B), iterates `builderMap.entrySet()` (`LinkedEntrySet` 24 B + `LinkedEntryIterator` 32 B), and for each key calls `ImmutableList.copyOf(Collection)` on the `ArrayList` — which invokes `ArrayList.toArray()` (`Arrays.copyOf`, allocating `Object[1]` = 24 B per 1-element key) before wrapping in `SingletonImmutableList` (16 B).
   - Both `ImmutableListMultimap(ImmutableMap<K, ImmutableList<V>>, int)` and `ImmutableListMultimap.fromMapEntries(...)` are package-private in `com.google.common.collect`.

### 1.3 Per-Decision Bytes in `pip:8 independent` (`SUPPORTED`)

In `pip:8 independent`, [`DefaultPolicyDecisionPoint.getResolvedAttributes`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java`) resolves two attributes (`urn:xacml4j:jmh:pip:case-id` and `urn:xacml4j:jmh:pip:attr-1`, both with `includeInResult = false`) in a single category (`SUBJECT_ACCESS`) and calls [`DefaultPolicyDecisionPoint.category`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java`):

```java
private static Category category(CategoryId c, Collection<Attribute> attributes) {
    return Category.builder(c)
            .entity(Entity.builder().attributes(attributes).build())
            .build();
}
```

From [pr23-astra-fixed3-profile.md](`.ntm/worktrees/xacml4j--batch2/agyx_1/openspec/changes/optimize-pdp-evaluation/research/pr23-astra-fixed3-profile.md`) and [histo-after-pip8-independent.txt](`.ntm/worktrees/xacml4j--batch2/cc_1/target/i-az/histo-after-pip8-independent.txt`), constructing that single response `Category` allocates:

| Phase | Class | Count | Bytes | Notes |
| --- | --- | ---: | ---: | --- |
| `Set<Attribute>` iteration | `java.util.HashMap$KeySet` | 1 | 16 | `attributes.iterator()` in `AttributeIndex.Builder.attributes` |
| `Set<Attribute>` iteration | `java.util.HashMap$KeyIterator` | 1 | 32 | `attributes.iterator()` |
| Builder wrappers | `Category$Builder`, `AttributeIndex$Builder` | 0–2 | 0–48 | 48 B in `pr23`; scalar-replaced in `histo-after` (`Entity$Builder` is always scalar-replaced) |
| Multimap builder staging | `com.google.common.collect.ImmutableListMultimap$Builder` | 1 | 24 | `AttributeIndex.Builder.attrBuilder` |
| Multimap builder staging | `java.util.LinkedHashMap` | 1 | 64 | `ImmutableMultimap.Builder.builderMap` |
| Multimap builder staging | `java.util.HashMap$Node[]` (16-slot) | 1 | 80 | `builderMap.table` |
| Multimap builder staging | `java.util.LinkedHashMap$Entry` | 2 | 80 | 2 attribute ids |
| Multimap builder staging | `java.util.LinkedHashMap$LinkedEntrySet` | 1 | 24 | `builderMap.entrySet()` |
| Multimap builder staging | `java.util.LinkedHashMap$LinkedEntryIterator` | 0–1 | 0–32 | 32 B in `pr23`; scalar-replaced in `histo-after` |
| Multimap builder staging | `java.util.ArrayList` | 2 | 48 | `newMutableValueCollection()` per id |
| Multimap builder staging | `java.lang.Object[]` (two `Object[10]` + two `Object[1]`) | 4 | 160 | 2 × 56 B (`ArrayList` backing) + 2 × 24 B (`ArrayList.toArray()` inside `ImmutableList.copyOf`) |
| Multimap builder staging | `com.google.common.collect.ImmutableMap$Builder` | 1 | 32 | `ImmutableListMultimap.fromMapEntries` |
| Multimap builder staging | `java.util.Map$Entry[]` (2-slot) | 1 | 24 | `ImmutableMap.Builder.entries` |
| **Staging subtotal** | | **14–17** | **536–616** | **Transient staging garbage per decision** |
| Retained multimap | `com.google.common.collect.ImmutableListMultimap` | 1 | 48 | `AttributeIndex.attributes` |
| Retained multimap | `com.google.common.collect.RegularImmutableMap` | 1 | 40 | `ImmutableMultimap.map` |
| Retained multimap | `com.google.common.collect.ImmutableMapEntry[]` | 1 | 24 | 2-slot hash table |
| Retained multimap | `com.google.common.collect.ImmutableMapEntry` | 2 | 48 | 2 map entries |
| Retained multimap | `com.google.common.collect.ImmutableMapEntry$NonTerminalImmutableMapEntry` | 1 | 24 | Bucket collision between the two attribute ids |
| Retained multimap | `com.google.common.collect.SingletonImmutableList` | 2 | 32 | Per-id value lists |
| **Retained multimap subtotal** | | **8** | **216** | **Built eagerly even when never queried by id** |
| **Total multimap + staging** | | **22–25** | **752–832** | Plus 48 B `HashMap$KeySet`/`KeyIterator` |

---

## 2. Weighing the Options

### Option 1: Narrow internal factory building an eager index directly (`ImmutableListMultimap.of(...)` or `ImmutableMap<String, ImmutableList<Attribute>>`)

- **Variant 1a (`ImmutableListMultimap.of(...)` overloads)**:
  - **Bytecode check**: As shown in §1.2, all 1-to-5-pair `ImmutableListMultimap.of(...)` overloads in Guava 31.1 delegate to `ImmutableListMultimap.builder().put(...).build()`.
  - **Expected savings**: **0 B**.
- **Variant 1b (Replace `Multimap<String, Attribute>` field in `AttributeIndex` with `ImmutableMap<String, ImmutableList<Attribute>>` + `ImmutableList<Attribute>`)**:
  - Because `ImmutableListMultimap(ImmutableMap, int)` is package-private in Guava, avoiding `ImmutableListMultimap.Builder` while staying eager requires changing `AttributeIndex`'s private index representation from `Multimap<String, Attribute>` to `ImmutableMap<String, ImmutableList<Attribute>>` (plus `ImmutableList<Attribute>` for `attributes()`).
  - For distinct `attributeId`s, an internal factory can build `ImmutableMap<String, ImmutableList<Attribute>>` via `ImmutableMap.of(id1, ImmutableList.of(a1), id2, ImmutableList.of(a2))` without `LinkedHashMap` or `ArrayList`.
  - **Expected savings**: Removes the 536 B `ImmutableListMultimap.Builder` staging and the 48 B `ImmutableListMultimap` wrapper, but **still allocates** `RegularImmutableMap` (40 B), `ImmutableMapEntry[2]` (24 B), 2 `ImmutableMapEntry` + 1 `NonTerminalImmutableMapEntry` (72 B), 2 `SingletonImmutableList` (32 B), and the flat `RegularImmutableList` + `Object[2]` (40 B) = **208 B retained per decision** (net savings **~544 B/decision**), even though nothing in the engine or marshallers ever looks up a response entity by attribute id.
  - Furthermore, two designators in the same category can share the same `attributeId` when their `issuer` or `dataType` differs (e.g., [`ResolvedAttributesPdpTest:75-91`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/test/java/org/xacml4j/v30/pdp/ResolvedAttributesPdpTest.java`)), so the factory still needs a grouping check or fallback for duplicate `attributeId`s.

### Option 2 (Recommended): Lazy `ImmutableListMultimap` over the normalized `ImmutableList<Attribute>` (`AttributeIndex.of`, `Entity.of`, `Category.of`) — same pattern as D48

- **How it works**:
  - [`AttributeIndex`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/AttributeIndex.java`), [`Entity`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/types/Entity.java`), and [`Category`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/Category.java`) are all `public final` with exclusively `private` fields, and **no public or package-private method on any of them exposes a `Multimap` or `Map`**.
  - Every consumer of a response `Category` — XML marshalling ([`Xacml30RequestContextFromJaxbToObjectModelMapper:210,253`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30RequestContextFromJaxbToObjectModelMapper.java`)), JSON marshalling ([`ResultAdapter:147`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-json/src/main/java/org/xacml4j/v30/marshal/json/ResultAdapter.java`), [`CategoryAdapter:86`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-json/src/main/java/org/xacml4j/v30/marshal/json/CategoryAdapter.java`)), JMH harnesses (`PipCorpus`, `CorpusRequest`), and PDP tests — calls `category.getEntity().attributes()` (the flat `Collection<Attribute>`).
  - Following D48 (`DecisionRuleResponse`), `AttributeIndex` stores `private final ImmutableList<Attribute> attributeList` (in `attributeId`-grouped order) and `private volatile Multimap<String, Attribute> attributes`.
  - When built via `AttributeIndex.of(Collection<Attribute>)`, if the attributes are already grouped by `attributeId` (checked by a bounded allocation-free scan up to 8 attributes; trivially true for $\le 2$ attributes or whenever all `attributeId`s are distinct), `attributeList` is stored directly (`ImmutableList.copyOf(attributes)` = `RegularImmutableList` 16 B + `Object[2]` 24 B = **40 B**) and `attributes` stays `null` until a caller invokes an id-keyed lookup (`get(id)`, `attributes(id)`, `resolve(...)`, `containsAttributeId(id)`, or `toString()`).
  - `AttributeIndex(Builder)` (used by request construction before `decide`) stays eager so request entities used during evaluation already have their `ImmutableListMultimap` initialized.
  - `AttributeIndex.equals` and `AttributeIndex.hashCode` operate directly on `attributeList` without materializing the multimap, producing bit-for-bit identical results to `ImmutableListMultimap`.
- **Expected savings**:
  - Eliminates **both** the 536–616 B `ImmutableListMultimap.Builder` staging **and** the 216 B retained `ImmutableListMultimap` graph, plus 16 B (`HashMap$KeySet`, because `ImmutableList.copyOf(Collection)` calls `HashSet.toArray()`, which allocates only `KeyIterator` 32 B + `Object[2]` 24 B), replacing 768–848 B with `RegularImmutableList` (16 B) + `Object[2]` (24 B) = **40 B**: net **−728 B per decision** (or **−808 B** when `AttributeIndex$Builder` and `Category$Builder` are not scalar-replaced), bringing `pip:8 independent` from **5,624 B to ~4,896 B per decision (−12.9%)**.
  - Also benefits [`Category.includedInResult()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/Category.java`) on single-use requests with mixed `IncludeInResult` attributes, where `getIncludeInResultAttributes()` is already an id-grouped `ImmutableList<Attribute>`.

### Option 3: Keep the eager `ImmutableListMultimap` representation and pre-size the builder

- **Bytecode check**: `ImmutableListMultimap` and `ImmutableMultimap` have no `builderWithExpectedSize(int)` in Guava 31.1, and `ImmutableListMultimap(ImmutableMap, int)` is package-private. Even if `AttributeIndex.Builder` pre-grouped attributes into a `Maps.newLinkedHashMapWithExpectedSize(n)` of `ImmutableList<Attribute>` and called `ImmutableListMultimap.Builder.putAll(K, Iterable<V>)`, `ImmutableMultimap.Builder` still allocates an un-sized `LinkedHashMap` (144 B) and an `ArrayList` + `Object[10]` (80 B) per key.
- **Expected savings**: **0 B** without a custom `Multimap` implementation (which is disallowed).

### Summary Comparison

| Criterion | Option 1a: `ImmutableListMultimap.of(...)` | Option 1b: Eager `ImmutableMap<String, ImmutableList<Attribute>>` | Option 2 (Recommended): Lazy `ImmutableListMultimap` over normalized `ImmutableList<Attribute>` (D48 style) | Option 3: Pre-size `ImmutableListMultimap.Builder` |
| --- | --- | --- | --- | --- |
| **Savings in `pip:8 independent`** | **0 B** (calls `Builder` internally) | **~544 B/decision** (drops staging, keeps 208 B map + entry lists) | **~728 B/decision** (`5,624 → ~4,896 B`, drops 536 B staging + 216 B retained multimap + 16 B `KeySet`, adds 40 B `RegularImmutableList`) | **0 B** (no `builderWithExpectedSize` in Guava 31.1) |
| **Hand-rolled map/multimap?** | No | No | No (uses `ImmutableList` and lazy `ImmutableListMultimap`) | No |
| **What public API callers see** | Identical | Identical | Identical (`attributes()` returns the `ImmutableList<Attribute>` directly; `get(id)` / `resolve(...)` lazily builds `ImmutableListMultimap`) | Identical |
| **Compatibility risk** | None | Low | **None** (`AttributeIndex`, `Entity`, `Category` are `final` with all-`private` fields; equality, hash, order, and grouping are bit-identical) | None |

---

## 3. Ready-to-Paste Design Text (`design.md`)

### D-AIDX: Response Entity `AttributeIndex` Builds Its Multimap on Demand

#### Context

When a decision resolves attributes through the PIP, [`DefaultPolicyDecisionPoint.getResolvedAttributes`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java`) packages each category's resolved [`Attribute`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/Attribute.java`) set into a response [`Category`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/Category.java`) via `Category.builder(c).entity(Entity.builder().attributes(attributes).build()).build()`. [`Entity.Builder`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/types/Entity.java`) delegates to [`AttributeIndex.Builder`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/AttributeIndex.java`), which stages every attribute through Guava's `ImmutableListMultimap.Builder`. In Guava 31.1, `ImmutableListMultimap.Builder` (and every `ImmutableListMultimap.of(K, V, ...)` overload from 1 to 5 pairs) allocates an un-sized `LinkedHashMap` (`64 B` + `HashMap$Node[16]` `80 B` + `2 × LinkedHashMap$Entry` `80 B` + `LinkedEntrySet` `24 B`), a `new ArrayList<>()` per distinct `attributeId` (`2 × 24 B` + `2 × Object[10]` `112 B` + `2 × Object[1]` `48 B` from `ArrayList.toArray()` inside `ImmutableList.copyOf`), and an `ImmutableMap.Builder` (`32 B` + `Map$Entry[2]` `24 B`) before producing the retained `ImmutableListMultimap` graph (`ImmutableListMultimap` `48 B`, `RegularImmutableMap` `40 B`, `ImmutableMapEntry[2]` `24 B`, `2 × ImmutableMapEntry` + `1 × NonTerminalImmutableMapEntry` `72 B`, and `2 × SingletonImmutableList` `32 B`).

In `pip:8 independent` (5,624 B per decision after AY), that single response `AttributeIndex` costs **536 B** of builder staging (616 B when `LinkedEntryIterator`, `AttributeIndex$Builder`, and `Category$Builder` are not scalar-replaced) plus **216 B** for the retained `ImmutableListMultimap` graph and **48 B** for `HashSet` iteration (`HashMap$KeySet` + `HashMap$KeyIterator`) — **800 B** to wrap two already-deduplicated [`Attribute`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/Attribute.java`) instances whose response `Entity` is only iterated in order (`entity.attributes()`) or marshalled, never queried by `attributeId`.

Similarly, on single-use requests where a category contains a mix of `IncludeInResult="true"` and `IncludeInResult="false"` attributes, [`Category.includedInResult()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/Category.java`) rebuilds a filtered response `Category` from `entity.getIncludeInResultAttributes()` (which is already an id-grouped `ImmutableList<Attribute>`) by passing it back through `Entity.builder().attributes(attributes).build()`.

#### Mechanism

Apply the same lazy-multimap pattern as D48 (`DecisionRuleResponse`) to [`AttributeIndex`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/AttributeIndex.java`):

1. **Normalized attribute list and lazy multimap in `AttributeIndex`**:
   - Store `private final ImmutableList<Attribute> attributeList` (the attributes in `attributeId`-grouped order) alongside `private volatile Multimap<String, Attribute> attributes`.
   - For `AttributeIndex.EMPTY`, initialize `attributeList = ImmutableList.of()` and `attributes = ImmutableListMultimap.of()`.
   - Keep `AttributeIndex(Builder b)` eager (`this.attributes = b.attrBuilder.build(); this.attributeList = ((ImmutableListMultimap<String, Attribute>) this.attributes).values().asList();`) so request entities constructed via `Entity.Builder` / `AttributeIndex.Builder` have their lookup multimap ready before `decide`.
   - Add `public static AttributeIndex of(Collection<Attribute> attributes)`:
     - Return `EMPTY` when `attributes == null || attributes.isEmpty()`.
     - Snapshot `ImmutableList<Attribute> list = ImmutableList.copyOf(attributes)` (which is a zero-copy reference return when `attributes` is already an `ImmutableList<Attribute>`, and uses `Collection.toArray()` without `HashMap$KeySet` when `attributes` is a `HashSet<Attribute>`).
     - Check whether `list` is already grouped by `attributeId` via a bounded allocation-free scan `isGroupedById(list)` for `list.size() <= 8`: equal `attributeId`s must form a single contiguous run (`for (int i = 1; i < n; i++)` if `!list.get(i).getAttributeId().equals(list.get(i - 1).getAttributeId())`, verify no `0 <= j < i - 1` has `list.get(j).getAttributeId().equals(list.get(i).getAttributeId())`). Note that any list of size $\le 2$ is always grouped by `attributeId`.
     - When `isGroupedById(list)` holds, construct `new AttributeIndex(list)` with `this.attributeList = list` and `this.attributes = null`.
     - When `isGroupedById(list)` does not hold (`list.size() > 8` or interleaved equal `attributeId`s with different issuers/types), fall back to `AttributeIndex.builder().attributes(list).build()`.
   - Add a private `multimap()` helper using double-checked locking on `this.attributes`:
     ```java
     private Multimap<String, Attribute> multimap() {
         Multimap<String, Attribute> m = this.attributes;
         if (m == null) {
             synchronized (this) {
                 m = this.attributes;
                 if (m == null) {
                     ImmutableListMultimap.Builder<String, Attribute> b = ImmutableListMultimap.builder();
                     for (int i = 0, n = attributeList.size(); i < n; i++) {
                         Attribute a = attributeList.get(i);
                         b.put(a.getAttributeId(), a);
                     }
                     m = this.attributes = b.build();
                 }
             }
         }
         return m;
     }
     ```
2. **Collection-backed operations vs. multimap-backed operations**:
   - Operations that do not need key indexing read `attributeList` directly without materializing `attributes`:
     - `attributes()` returns `attributeList`.
     - `isEmpty()` returns `attributeList.isEmpty()`.
     - `size()` returns `attributeList.size()`.
     - `contains(Attribute a)` returns `attributeList.contains(a)`.
     - `getIncludeInResultAttributes()` scans `attributeList` (preserving the existing lazy `includedInResult` field and returning `attributeList` directly when all attributes have `isIncludeInResult() == true`).
     - `equals(Object o)` compares `attributeList.containsAll(other.attributeList) && other.attributeList.containsAll(attributeList)` (identical to `attributes.values().containsAll(...)`).
     - `hashCode()`: when `this.attributes != null`, returns `this.attributes.hashCode()`; otherwise computes `ImmutableListMultimap.hashCode()` (`asMap().hashCode()`) directly over the contiguous `attributeId` runs of `attributeList` in an allocation-free loop:
       ```java
       int mapHash = 0;
       int n = attributeList.size();
       int i = 0;
       while (i < n) {
           Attribute first = attributeList.get(i);
           String id = first.getAttributeId();
           int listHash = 31 + first.hashCode();
           i++;
           while (i < n && id.equals(attributeList.get(i).getAttributeId())) {
               listHash = 31 * listHash + attributeList.get(i).hashCode();
               i++;
           }
           mapHash += id.hashCode() ^ listHash;
       }
       return mapHash;
       ```
   - Keyed operations (`get(String)`, `attributes(String)`, `containsAttributeId(String)`, `getAttributesIds()`, `resolve(...)`, `toString()`, and `Builder.index(AttributeIndex)`) call `multimap()` (or for `Builder.index(AttributeIndex)`, iterate `index.attributeList` to add each attribute).
3. **Direct factories on `Entity` and `Category` and engine call sites**:
   - [`Entity`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/types/Entity.java`): add `public static Entity of(AttributeIndex index)` and `public static Entity of(Collection<Attribute> attributes)` (returning `EMPTY` when empty, or `new Entity(null, AttributeIndex.of(attributes))`).
   - [`Category`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/Category.java`): add `public static Category of(CategoryId categoryId, String refId, Entity entity)` and `public static Category of(CategoryId categoryId, Entity entity)`.
   - In [`DefaultPolicyDecisionPoint.category(CategoryId, Collection<Attribute>)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java`), replace `Category.builder(c).entity(Entity.builder().attributes(attributes).build()).build()` with `Category.of(c, Entity.of(attributes))`.
   - In [`Category.includedInResult()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/Category.java`), replace `Category.builder(categoryId).id(refId).entity(Entity.builder().attributes(attributes).build()).build()` with `Category.of(categoryId, refId, Entity.of(attributes))`.

#### Soundness

1. **OASIS XACML 3.0 §5.46 (`<Attribute>`), §5.48 (`<Result>`), and §7.3.5 (Attribute Retrieval)**:
   - **§5.46 `<Attribute>`**: Defines `<Attribute>` with `AttributeId` [Required], `Issuer` [Optional], `IncludeInResult` [Required, default `false`] (*"Whether to include this attribute in the result. This is useful to correlate requests with their responses in case of multiple decisions"*), and one or more `<AttributeValue>` elements.
   - **§5.48 `<Result>`**: Defines `<Attributes>` [Optional] inside `<Result>` as *"A list of attributes that were part of the request. The choice of which attributes are included here is made with the IncludeInResult attribute of the `<Attribute>` elements of the request. See section 5.46."*
   - **§7.3.5 Attribute Retrieval**: Specifies that the PDP requests attribute values from the context handler, which may retrieve them from PIPs, and that each retrieved bag is treated as immutable during policy evaluation.
   - **Engine representation**: `xacml4j` exposes both `Result.getIncludeInResultAttributes()` (the normative §5.48 `<Attributes>` collection marshalled to XML and JSON responses) and `Result.getResolvedAttributes()` (an engine extension returning the PIP-resolved designator attributes per category). Both carry `Collection<Category>` where each `Category` wraps an `Entity` and its `AttributeIndex`.
2. **Order, grouping, and lookup invariance**:
   - When `isGroupedById(list)` holds, `list` is already in the exact iteration order that `ImmutableListMultimap.Builder.build().values()` produces, because `ImmutableListMultimap.Builder` preserves first-seen key order and per-key insertion order.
   - When `isGroupedById(list)` does not hold (for example, 3 or more attributes in the same category where two share an `attributeId` with different `issuer`s and another `attributeId` sits between them), `AttributeIndex.of` falls back to `AttributeIndex.Builder`, which groups the equal `attributeId`s contiguously just as before.
   - If a caller ever invokes a keyed lookup (`get(id)`, `attributes(id)`, `resolve(...)`, `getAttributesIds()`, `containsAttributeId(id)`) on a lazy `AttributeIndex`, `multimap()` builds the exact same `ImmutableListMultimap<String, Attribute>` from `attributeList`.
3. **Hash and equality invariance**:
   - In Guava 31.1, `ImmutableListMultimap.hashCode()` inherits `AbstractMultimap.hashCode()`, which returns `asMap().hashCode()`. In `RegularImmutableMap<String, ImmutableList<Attribute>>`, the map hash is the sum over distinct keys of `key.hashCode() ^ valueList.hashCode()`, where `valueList.hashCode()` is standard `java.util.List.hashCode()`. Because `attributeList` has contiguous `attributeId` runs, the run-wise fold in `AttributeIndex.hashCode()` computes the exact same 32-bit `int` for every input, including multiple `Attribute`s sharing an `attributeId`.

#### Compatibility

- **Public API**: [`AttributeIndex`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/AttributeIndex.java`), [`Entity`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/types/Entity.java`), and [`Category`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/Category.java`) are `public final` with only `private` fields. All existing public builders (`AttributeIndex.Builder`, `Entity.Builder`, `Category.Builder`) and methods remain unchanged; `AttributeIndex.of`, `Entity.of`, and `Category.of` are additive static factories.
- **XML and JSON marshalling**: Both XML ([`Xacml30RequestContextFromJaxbToObjectModelMapper`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30RequestContextFromJaxbToObjectModelMapper.java`)) and JSON ([`ResultAdapter`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-json/src/main/java/org/xacml4j/v30/marshal/json/ResultAdapter.java`), [`CategoryAdapter`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-json/src/main/java/org/xacml4j/v30/marshal/json/CategoryAdapter.java`)) iterate `category.getEntity().attributes()` and produce identical output without triggering multimap construction.
- **Serialization**: `AttributeIndex` does not implement `Serializable`. `Entity` (`serialVersionUID = 6188174758603655643L`) and `Category` (`serialVersionUID = 1764671001452130122L`) retain their exact field layouts and `serialVersionUID` values.
- **Thread safety**: `attributeList` is `final` and `ImmutableList`; `attributes` and `includedInResult` are safely published (`volatile` / immutable value).

#### Cost and Gate

- **Expected allocation reduction**:
  - In `pip:8 independent`: removes `ImmutableListMultimap$Builder` (24 B), `LinkedHashMap` (64 B), `HashMap$Node[16]` (80 B), `2 × LinkedHashMap$Entry` (80 B), `LinkedEntrySet` (24 B), `2 × ArrayList` (48 B), `2 × Object[10]` + `2 × Object[1]` (160 B), `ImmutableMap$Builder` (32 B), `Map$Entry[2]` (24 B), `ImmutableListMultimap` (48 B), `RegularImmutableMap` (40 B), `ImmutableMapEntry[2]` (24 B), `2 × ImmutableMapEntry` + `1 × NonTerminalImmutableMapEntry` (72 B), `2 × SingletonImmutableList` (32 B), and `HashMap$KeySet` (16 B), replacing **768 B** (or **848 B** when builder objects are not scalar-replaced) with `RegularImmutableList` (16 B) + `Object[2]` (24 B) = **40 B**: net **−728 B per decision** (`5,624 → ~4,896 B/decision`, **−12.9%**).
  - In `rsa2008` and `responses`: **0 B** change on decisions without resolved PIP attributes and without mixed `IncludeInResult` single-use categories.
- **Verification gate**:
  1. Exact single-threaded class histograms (`DecisionFixedCostPdpTest`) on `pip:8 independent`, `responses`, and `multi-valued`, verifying the removal of `ImmutableListMultimap*`, `LinkedHashMap*`, `RegularImmutableMap`, `ImmutableMapEntry*`, and `SingletonImmutableList` from the `pip:8 independent` per-decision delta.
  2. Controlled back-to-back JMH pair (`-f 2 -wi 3 -i 5 -prof gc`) on `PipDecideBenchmark` (`pip:8 independent`) and `PdpDecideBenchmark` (`rsa2008`), requiring `pip:8` `gc.alloc.rate.norm` to drop by ~700 B/op with no throughput regression on `pip:8` or `rsa2008`.
  3. Full unit and conformance suites (`xacml-core`, `xacml-pdp`, `xacml-xml`, `xacml-json`, `xacml-conformance-tests`).

#### Files

- [`xacml-core/src/main/java/org/xacml4j/v30/AttributeIndex.java`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/AttributeIndex.java`)
- [`xacml-core/src/main/java/org/xacml4j/v30/types/Entity.java`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/types/Entity.java`)
- [`xacml-core/src/main/java/org/xacml4j/v30/Category.java`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/Category.java`)
- [`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java`)
- [`xacml-core/src/test/java/org/xacml4j/v30/AttributeIndexTest.java`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/test/java/org/xacml4j/v30/AttributeIndexTest.java`)
- [`xacml-pdp/src/test/java/org/xacml4j/v30/pdp/ResolvedAttributesPdpTest.java`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/test/java/org/xacml4j/v30/pdp/ResolvedAttributesPdpTest.java`)
- [`xacml-pdp/src/test/java/org/xacml4j/v30/pdp/DecisionFixedCostPdpTest.java`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/test/java/org/xacml4j/v30/pdp/DecisionFixedCostPdpTest.java`)

#### Test Plan (Outcomes Only)

1. **Equality and hash parity**: An `AttributeIndex`, `Entity`, and `Category` created via `of(...)` and via `builder()...build()` with the same attributes (1 attribute, 2 distinct attributes, multiple attributes sharing an `attributeId` with different issuers/types, and $> 8$ attributes) are mutually equal (`a.equals(b)` and `b.equals(a)`) and produce identical `hashCode()` and `toString()` before and after lazy multimap materialization.
2. **Order and grouping invariance**: When `AttributeIndex.of(...)` receives interleaved attributes sharing an `attributeId` (e.g., `[idA(issuer1), idB, idA(issuer2)]`), `attributes()` groups the `idA` attributes contiguously in first-seen `attributeId` order (`[idA(issuer1), idA(issuer2), idB]`), matching `AttributeIndex.Builder`.
3. **On-demand lookup parity**: Calling `get(id)`, `attributes(id)`, `containsAttributeId(id)`, `getAttributesIds()`, `resolve(...)`, or `Builder.index(AttributeIndex)` on a lazy `AttributeIndex` returns the exact same collections and values as a builder-constructed `AttributeIndex`.
4. **PIP resolved-attribute order and content**: `Result.getResolvedAttributes()` and `Result.getIncludeInResultAttributes()` preserve the exact category and attribute iteration order pinned in `ResolvedAttributesPdpTest` across 1 to 7 attributes and multiple issuers.
5. **Per-decision allocation gate**: `DecisionFixedCostPdpTest` confirms that `pip:8 independent` allocates zero `ImmutableListMultimap*` and zero `LinkedHashMap*` instances per decision and drops by at least 700 B per decision.

#### Open Questions

1. **Visibility of the direct factories (`AttributeIndex.of`, `Entity.of`, `Category.of`)**:
   - *Question*: Should `AttributeIndex.of(Collection<Attribute>)`, `Entity.of(...)`, and `Category.of(...)` be `public static` on the `xacml-core` model types (matching `DecisionRuleResponse` and `Result` factories), or should `Entity` stay builder-only outside `org.xacml4j.v30`?
   - *Recommendation*: Make `AttributeIndex.of(Collection<Attribute>)`, `Entity.of(AttributeIndex)` / `Entity.of(Collection<Attribute>)`, and `Category.of(CategoryId, String, Entity)` / `Category.of(CategoryId, Entity)` `public static`. `Category`, `AttributeIndex`, and `DefaultPolicyDecisionPoint` span three packages (`org.xacml4j.v30`, `org.xacml4j.v30.types`, and `org.xacml4j.v30.pdp`), and static `of(...)` factories on immutable model value objects are clean, idiomatic, and preserve the existing `Builder` classes unchanged.
2. **Whether `AttributeIndex(Builder)` should stay eager or also build its multimap lazily**:
   - *Question*: Should `AttributeIndex(Builder)` keep building `ImmutableListMultimap` eagerly, or should it also defer multimap construction?
   - *Recommendation*: Keep `AttributeIndex(Builder)` eager. Callers of `AttributeIndex.Builder` already paid for `ImmutableListMultimap.Builder` staging when populating the builder, and request entities built via `RequestContext.Builder` / `Entity.Builder` are immediately queried by `attributeId` during `decide`. Only `AttributeIndex.of(Collection<Attribute>)` skips the builder staging, so making `of(...)` lazy and `Builder.build()` eager avoids any lazy-initialization synchronization on shared request entities while eliminating all multimap staging and retention on response entities.
