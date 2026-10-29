# Design Draft D-RESP-argon: Cheaper Representation of Evaluated Obligation and Advice Assignments

- **Author**: Lane `agyx_1` (Gemini Argon)
- **Change**: `optimize-pdp-evaluation`
- **Topic**: Design decision **D-RESP** (to be numbered by the conductor after D47) — eliminating per-decision `ImmutableSetMultimap.Builder` allocation on the evaluated obligation/advice path while preserving all XACML 3.0 §7.18, public API, `protected`-field, and marshalling contracts.
- **Mode**: Read-only design draft (no repository files modified).

---

## 1. Weighing the Options

### Exact Baseline Cost in `rules:100 responses` (`SUPPORTED`)

In [`pr23-astra-fixed3-profile.md`](`.ntm/worktrees/xacml4j--batch2/agyx_1/openspec/changes/optimize-pdp-evaluation/research/pr23-astra-fixed3-profile.md`) (`rules:100 responses, index on`, `72` objects, `2,336 B/decision`), only **one** response is evaluated dynamically per decision: `Obligation("urn:xacml4j:jmh:rules:obligation:audit", Effect.PERMIT)` ([`RulesCorpus.java:193-196, 336-345`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/RulesCorpus.java`)). Its two `<AttributeAssignmentExpression>`s evaluate to a 3-element `ImmutableList<AttributeAssignment>` in [`BaseDecisionRuleResponseExpression.evaluateAttributeAssignments`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/BaseDecisionRuleResponseExpression.java`):
1. `owner = "alice"` (`urn:xacml4j:jmh:rules:assignment:owner`)
2. `owner = "bob"` (`urn:xacml4j:jmh:rules:assignment:owner`)
3. `level = "info"` (`urn:xacml4j:jmh:rules:assignment:level`)

Passing that 3-element `ImmutableList<AttributeAssignment>` to [`Obligation.of`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/Obligation.java`) $\to$ [`DecisionRuleResponse(String, Effect, Collection<AttributeAssignment>)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/DecisionRuleResponse.java`) currently allocates:

| Category | Histogram Class (`pr23-astra-fixed3-profile.md:111-147`) | Count | Bytes | Source / Guava `31.1-jre` Bytecode Site |
| --- | --- | ---: | ---: | --- |
| **Builder staging (dedicated rows)** | `com.google.common.collect.ImmutableSetMultimap$Builder` | 1 | 24 | [`DecisionRuleResponse.java:79`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/DecisionRuleResponse.java`) |
| | `java.util.LinkedHashMap` | 3 | 192 | 1 `builderMap` (`Platform.preservesInsertionOrderOnPutsMap`) + 2 backing maps for the `owner` and `level` `LinkedHashSet`s (`Sets.newLinkedHashSetWithExpectedSize(4)`) |
| | `[Ljava.util.HashMap$Node;` | 3 | 240 | 1 table for `builderMap` + 2 capacity-8 tables (`80 B` each) for the `owner` and `level` sets |
| | `java.util.LinkedHashMap$Entry` | 5 | 200 | 2 key entries in `builderMap` (`owner`, `level`) + 3 value entries across the two sets (`alice`, `bob`, `info`) |
| | `java.util.LinkedHashSet` | 2 | 32 | Mutable value sets for `owner` and `level` (`ImmutableSetMultimap$Builder.newMutableValueCollection`) |
| | `java.util.LinkedHashMap$LinkedEntrySet` | 1 | 24 | `builderMap.entrySet()` in `ImmutableSetMultimap$Builder.build()` |
| | `[Ljava.util.Map$Entry;` | 1 | 24 | `new ImmutableMap.Builder(2)` internal entry array in `ImmutableSetMultimap.fromMapEntries` |
| | `com.google.common.collect.ImmutableSet$RegularSetBuilderImpl` | 1 | 40 | `ImmutableSet.copyOf(Collection)` $\to$ `construct(2, 2, ...)` for the 2-element `owner` set |
| **Retained multimap + eager hash (dedicated rows)** | `com.google.common.collect.ImmutableSetMultimap` | 1 | 56 | [`DecisionRuleResponse.java:84`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/DecisionRuleResponse.java`) |
| | `com.google.common.collect.RegularImmutableMap` | 1 | 40 | Backing key map of `ImmutableSetMultimap` |
| | `com.google.common.collect.ImmutableMapEntry` | 2 | 48 | Entries for `owner` and `level` in `RegularImmutableMap` |
| | `[Lcom.google.common.collect.ImmutableMapEntry;` | 1 | 24 | Hash table of `RegularImmutableMap` |
| | `com.google.common.collect.RegularImmutableSet` | 1 | 32 | Value set for `owner` |
| | `com.google.common.collect.SingletonImmutableSet` | 1 | 16 | Value set for `level` |
| | `com.google.common.collect.ImmutableMapEntrySet$RegularEntrySet` | 1 | 24 | Allocated by eager `attributes.hashCode()` at [`DecisionRuleResponse.java:65`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/DecisionRuleResponse.java`) |
| **Dedicated class subtotal** | **15 classes** | **25** | **1,016** | Exact sum of dedicated histogram rows |
| **Shared pool rows (`PARTIAL` per-site split, confirmed in `javap`)** | `[Ljava.lang.Object;` (out of 11 / 304 B total) | 6 | 144 | `LinkedHashSet.toArray()` for `owner` (`Object[2]`, 24 B) and `level` (`Object[1]`, 16 B); `RegularSetBuilderImpl.dedupedElements` (`Object[2]`, 24 B) and `hashTable` (`Object[4]`, 32 B); retained `RegularImmutableSet.elements` (`Object[2]`, 24 B) and `table` (`Object[4]`, 24–32 B) |
| | `com.google.common.collect.RegularImmutableList` (out of 6 / 96 B total) | 1 | 16 | `RegularEntrySet` entry list wrapper allocated during `attributes.hashCode()` |
| **Grand total** | | **32** | **~1,176** | **50.3% of `rules:100 responses` (2,336 B/decision)** |

---

### Option Comparison Table

| Option | Mechanism & Guava `31.1-jre` Bytecode Reality | Expected Bytes Saved on `rules:100 responses` | Compatibility Risk | What an External Subclass Sees | Verdict |
| --- | --- | --- | --- | --- | --- |
| **Option 1 (Recommended): Normalized `ImmutableList<AttributeAssignment>` + lazy `ImmutableSetMultimap` materialization on factory instances (`Builder` stays eager)** | `DecisionRuleResponse(String, Effect, Collection)` verifies in a zero-allocation linear scan ($N \le 8$) that the evaluated `ImmutableList<AttributeAssignment>` is already grouped by `attributeId` with distinct values, and keeps it directly. `getAttributes()` returns that `ImmutableList`; `hashCode()` is lazy and computed directly over the contiguous `attributeId` runs; `equalsTo` fast-paths `assignmentList.equals` and falls back to `attributes().equals`. `this.attributes` is populated eagerly in `protected DecisionRuleResponse(Builder<?>)` and lazily in `attributes()` on factory-built `Obligation`/`Advice`. | **1,016 B** dedicated rows (25 objects) + **~160 B** shared `[Ljava.lang.Object;` / `RegularImmutableList` rows = **~1,176 B/decision** (`2,336 -> ~1,160 B/decision`, **-50.3%**). | **None.** `getAttributes()`, `getAttribute(id)`, `equals`, `hashCode`, `toString`, `merge`, `Builder.from`, XML, and JSON are identical. No custom `Multimap` class. | External subclasses of `DecisionRuleResponse` can only be constructed via `protected DecisionRuleResponse(Builder<?>)` (the factory constructor is package-private, `Advice` is `final`, `Obligation` has no `public`/`protected` constructors). By JLS §6.6.2.1 they can only access `.attributes` on their own subclass type, where `DecisionRuleResponse(Builder<?>)` eagerly sets `this.attributes = ImmutableSetMultimap.copyOf(b.attributes)`. | **RECOMMENDED** |
| **Option 2A: `ImmutableSetMultimap.of(...)` overloads for 1–5 assignments** | `javap -c` on Guava `31.1-jre` `ImmutableSetMultimap` (`lines 30-144`) shows that **every** `ImmutableSetMultimap.of(k1, v1, ...)` overload from 1 to 5 entries literally calls `ImmutableSetMultimap.builder().put(...).build()`. Even the existing 1-assignment branch at [`DecisionRuleResponse.java:75-78`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/DecisionRuleResponse.java`) allocates `Builder`, 2 `LinkedHashMap`s, 2 `HashMap$Node[]` tables, and 1 `LinkedHashSet` (~600 B)! | **0 B** (already uses the exact same `ImmutableSetMultimap.Builder` under the hood). | None, because it changes nothing. | `ImmutableSetMultimap` in `this.attributes`. | **Reject** (saves 0 B). |
| **Option 2B: `ImmutableListMultimap` when ids are distinct and no duplicates exist** | 1. **Not observationally equal**: `Multimap.equals` (`Multimaps.equalsImpl`) compares `asMap()`. `ImmutableSetMultimap.asMap()` values are `ImmutableSet` (`java.util.Set`), whereas `ImmutableListMultimap.asMap()` values are `ImmutableList` (`java.util.List`). Because `Set.equals(List)` is always `false`, an `Obligation.of(...)` backed by `ImmutableListMultimap` is **never `.equals()`** to `Obligation.builder(...).build()` (breaking [`ResponseFactoryTest:59`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/test/java/org/xacml4j/v30/ResponseFactoryTest.java`) and [`CorpusRequest.mismatch`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-jmh/src/main/java/org/xacml4j/jmh/harness/CorpusRequest.java`)). `hashCode()` also differs (`Set.of(x).hashCode() == x.hashCode()` vs `List.of(x).hashCode() == 31 + x.hashCode()`).<br>2. **Still allocates Builder**: `ImmutableListMultimap.of(...)` also calls `ImmutableListMultimap.builder()`, which allocates `LinkedHashMap` + `new ArrayList<>(10)` per key. Furthermore, in `rules:100 responses`, `owner` has **two** values (`alice`, `bob`), so ids are not distinct. | **0 B** in `rules:100 responses` (since `owner` has 2 values); even for distinct ids, allocates `LinkedHashMap` + `ArrayList` per key. | **High (P1)**: breaks `Obligation.equals`, `Advice.equals`, `Result.equals`, and `hashCode()` against builder-constructed instances. | `ImmutableListMultimap` instead of `SetMultimap`. | **Reject** (breaks `equals`/`hashCode` and does not apply to multi-valued attributes). |
| **Option 2C: `Multimaps.forMap(ImmutableMap.of(...))` when all `attributeId`s are distinct (1 value per key)** | `Multimaps.forMap(Map)` returns a `SetMultimap` (`Multimaps$MapMultimap`, 16 B) wrapping an `ImmutableMap`, so `asMap()` values are singleton `Set`s (`equals` and `hashCode` match `ImmutableSetMultimap`). However, in `rules:100 responses`, `owner` has **2 values** (`alice`, `bob`), so `Multimaps.forMap` does not apply! Moreover, `MapMultimap.get(key)` and `values().iterator()` allocate wrapper views on every call. | **0 B** in `rules:100 responses` (2 `owner` values); saves ~550 B only on dynamic 1-assignment responses. | Low, but allocates view objects on `getAttributes()` / `getAttribute(id)`. | `Multimaps$MapMultimap` on 1-value-per-key responses. | **Subsumed by Option 1** (Option 1 saves 100% of multimap cost on *both* 1-assignment and multi-assignment responses). |
| **Option 3: Keep eager `ImmutableSetMultimap` and cut only Guava's builder cost (`builder` pre-sizing or `copyOf(Multimap)`)** | Bytecode inspection of Guava `31.1-jre` `ImmutableSetMultimap` confirms:<br>1. There is **no** `builderWithExpectedSize` on `ImmutableSetMultimap`.<br>2. `fromMapEntries` and the constructor are package-private (`com.google.common.collect`).<br>3. Every public factory except `of()` (0 args) and `copyOf(Multimap)` goes through `ImmutableSetMultimap.Builder`, which unconditionally allocates `builderMap` (`LinkedHashMap`) + 1 `LinkedHashSet` (another `LinkedHashMap`) per key.<br>4. Calling `ImmutableSetMultimap.copyOf(Multimap)` requires an input `Multimap` whose `asMap().entrySet()` is iterated; without a custom `Multimap` (rejected by the maintainer), any Guava `Multimap` source (`LinkedHashMultimap`, `Multimaps.newSetMultimap`) allocates the same `Map` + `Set` nodes, and the retained `ImmutableSetMultimap` + `RegularImmutableMap` + `ImmutableMapEntry` + `RegularImmutableSet` + `SingletonImmutableSet` (~312 B) still remain. | **~0 to 40 B** without a custom `Multimap` (only lazy `hashCode` saves `RegularEntrySet` 24 B + `RegularImmutableList` 16 B = 40 B). | None. | `ImmutableSetMultimap` in `this.attributes`. | **Insufficient** alone (~40 B saved vs ~1,176 B with Option 1), though lazy `hashCode` is included in Option 1. |

---

## 2. Ready-to-Paste Design Text (`### D-RESP`)

```markdown
### D-RESP: Evaluated responses keep their normalized assignment list and materialize the multimap on demand

**Context.** Cycle AX ([D32](#d32-response-and-result-builders)) added `Obligation.of` and
`Advice.of` and removed `DecisionRuleResponse.Builder` and its `LinkedHashMultimap` from the
evaluation path, cutting `rules:100 responses` from 2,723 to 2,368 B/op (2,336 B in P-FIXED3's
exact class histogram, `research/pr23-astra-fixed3-profile.md`, the `rules:100 responses, index on`
table). Half of the remaining decision allocation in `rules:100 responses` — 1,016 B in 15
dedicated histogram rows (25 objects) plus about 160 B in shared `Object[]` and
`RegularImmutableList` rows, or about **1,176 B per decision** — is still spent inside
`DecisionRuleResponse(String, Effect, Collection<AttributeAssignment>)` building and hashing Guava's
`ImmutableSetMultimap` for the one dynamic `audit` obligation (`owner = "alice"`,
`owner = "bob"`, `level = "info"`):

- `ImmutableSetMultimap.Builder` staging: one `ImmutableSetMultimap$Builder` (24 B), three
  `LinkedHashMap`s (192 B: the builder map and one `LinkedHashSet` backing map per key), three
  `HashMap$Node[]` tables (240 B), five `LinkedHashMap$Entry` nodes (200 B), two `LinkedHashSet`s
  (32 B), one `LinkedEntrySet` (24 B), one `Map.Entry[]` (24 B), one
  `ImmutableSet$RegularSetBuilderImpl` (40 B), and about 88 B of temporary `Object[]` arrays from
  `LinkedHashSet.toArray()` and `RegularSetBuilderImpl` (**776 B** dedicated, **~864 B** with
  arrays);
- the retained `ImmutableSetMultimap` graph: one `ImmutableSetMultimap` (56 B), one
  `RegularImmutableMap` (40 B), two `ImmutableMapEntry` nodes (48 B), one `ImmutableMapEntry[]`
  table (24 B), one `RegularImmutableSet` (32 B) plus its two `Object[]` arrays (56 B), and one
  `SingletonImmutableSet` (16 B) (**216 B** dedicated, **272 B** with arrays);
- eager hashing in the constructor (`DecisionRuleResponse.java:65`): calling
  `attributes.hashCode()` forces `RegularImmutableMap.entrySet()` to allocate one
  `ImmutableMapEntrySet$RegularEntrySet` (24 B) and one `RegularImmutableList` (16 B) (**40 B**),
  even though PDP evaluation keys and merges obligations and advice by string identifier and never
  calls `DecisionRuleResponse.hashCode()`.

During PDP evaluation, `BaseDecisionRuleResponseExpression.evaluateAttributeAssignments` already
returns a detached `ImmutableList<AttributeAssignment>`. Nothing in PDP evaluation
(`BaseDecisionRule`, `RootEvaluationContext`, `Result.of`), XML marshalling
(`Xacml30RequestContextFromJaxbToObjectModelMapper`, `Xacml20ResponseContextMarshaller`), or JSON
marshalling (`ObligationOrAdviceAdapter`) ever calls `getAttribute(String)` or reads a `Multimap`;
each reads `getId()`, `getFulfillOn()`, and `getAttributes()`.

Bytecode inspection of Guava `31.1-jre` rules out two apparent shortcuts:
- Every `ImmutableSetMultimap.of(k1, v1, ...)` overload from one to five entries delegates to
  `ImmutableSetMultimap.builder().put(...).build()`, allocating `builderMap` (`LinkedHashMap`) and a
  `LinkedHashSet` per key. `ImmutableSetMultimap` has no pre-sized builder factory, and
  `fromMapEntries` is package-private.
- `ImmutableListMultimap` is not observationally equal to `ImmutableSetMultimap` even when keys and
  values are distinct: `Multimap.equals` compares `asMap()`, where `List` values never equal `Set`
  values (`Set.of(a).equals(List.of(a))` is `false`), and `List.hashCode()` (`31 + a.hashCode()`)
  differs from `Set.hashCode()` (`a.hashCode()`).

**Mechanism.**

1. **Normalized assignment list.** `DecisionRuleResponse` holds
   `private final ImmutableList<AttributeAssignment> assignmentList`, containing the distinct
   assignments grouped by first-seen `attributeId` in first-seen order — the exact sequence that
   `ImmutableSetMultimap.values()` produces.
2. **Zero-allocation fast check for already-normalized inputs.** In the package-private constructor
   `DecisionRuleResponse(String id, Effect fulfillOn, Collection<AttributeAssignment> assignments)`
   (used only by `Obligation.of` and `Advice.of`), `ImmutableList.copyOf(assignments)` is a no-op
   when `assignments` is already an `ImmutableList` (such as the return value of
   `evaluateAttributeAssignments`). A bounded scan (`isNormalized(list)`, for lists of up to eight
   assignments) checks in $O(N)$ without allocation whether equal `attributeId`s form contiguous
   runs and whether assignments within each run are pairwise distinct (`!a.equals(b)`):
   - Size 0 or 1: trivially normalized.
   - Size 2 (`a0`, `a1`): normalized unless `a0.equals(a1)` (when equal, the normalized list is
     `ImmutableList.of(a0)`).
   - Size $2 < N \le 8$: walks the list tracking contiguous `attributeId` runs; checks that each
     element differs from earlier elements in its run and that a new run's `attributeId` did not
     appear in an earlier run.
   - When `isNormalized(list)` holds (as in `rules:100 responses`, where `[owner=alice, owner=bob,
     level=info]` has two contiguous runs and distinct values), `this.assignmentList = list` is
     stored directly with **zero additional objects allocated**.
   - When `isNormalized(list)` is false (duplicate assignments, interleaved `attributeId` runs such
     as `[b=1, a=1, b=2]`, or $N > 8$), `DecisionRuleResponse` builds Guava's
     `ImmutableSetMultimap<String, AttributeAssignment>` via `buildMultimap(list)` as today, stores
     it in `this.attributes`, and sets `this.assignmentList = mm.values().asList()`. No custom
     multimap or grouping structure is introduced.
3. **Lazy multimap on factory instances; eager multimap on `Builder` instances.**
   - `protected Multimap<String, AttributeAssignment> attributes` keeps its name, `protected`
     visibility, and `Multimap<String, AttributeAssignment>` type.
   - `protected DecisionRuleResponse(Builder<?> b)` (the only constructor reachable by external
     subclasses of `DecisionRuleResponse`) eagerly initializes
     `this.attributes = ImmutableSetMultimap.copyOf(b.attributes)` and
     `this.assignmentList = ((ImmutableSetMultimap<String, AttributeAssignment>) this.attributes).values().asList()`.
     Any external subclass of `DecisionRuleResponse` therefore sees `this.attributes` non-null and
     populated before its own constructor body runs, exactly as before.
   - On `Obligation` and `Advice` instances created via `Obligation.of` and `Advice.of`, a private
     helper `attributes()` returns `this.attributes` if non-null, or builds and caches
     `ImmutableSetMultimap<String, AttributeAssignment>` from `this.assignmentList` on first demand.
4. **Accessors, equality, hash code, `toString`, `merge`, and `Builder.from`.**
   - `getAttributes()` returns `this.assignmentList` directly (an `ImmutableList<AttributeAssignment>`,
     avoiding `ImmutableMultimap$Values` view and iterator allocations).
   - `getAttribute(String attributeId)` returns `attributes().get(attributeId)` (an
     `ImmutableSet<AttributeAssignment>`, preserving unmodifiable set semantics and empty-set return
     for unknown ids).
   - `hashCode()` is computed lazily on first call (`private transient int hashCode`) directly over
     the contiguous `attributeId` runs of `this.assignmentList` without materializing
     `this.attributes` or `RegularEntrySet`: for each contiguous run of `key = a.getAttributeId()`,
     it sums `a.hashCode()` into `setHash` and adds `key.hashCode() ^ setHash` into `mapHash`, then
     returns `31 * (31 + Objects.hashCode(id)) + mapHash` — bit-for-bit identical to
     `31 * (31 + Objects.hashCode(id)) + attributes().hashCode()`.
   - `equalsTo(DecisionRuleResponse r)` checks `id.equals(r.id)` and
     `assignmentList.size() == r.assignmentList.size()`, returns `true` immediately when
     `assignmentList.equals(r.assignmentList)` (zero allocation when assignment order matches), and
     falls back to `attributes().equals(r.attributes())` when two responses have the same size but
     different key or value ordering.
   - `toString()` passes `attributes()` to `MoreObjects.toStringHelper`, preserving the exact text.
   - `Builder.from(DecisionRuleResponse r)` copies `r.getAttributes()` (`r.assignmentList`), and
     `Obligation.merge` / `Advice.merge` combine `this.getAttributes()` and `other.getAttributes()`
     without forcing multimap materialization on the merged input.

**Soundness.** Checked against [OASIS XACML 3.0 Plus Errata 01](https://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html)
(fetched 2026-10-28):

- **§5.32 `<Obligations>`, §5.33 `<AssociatedAdvice>`, §5.34 `<Obligation>`, §5.35 `<Advice>`,
  §5.36 `<AttributeAssignment>`**:
  > *"The `<Obligation>` element SHALL contain an identifier for the obligation and a set of
  > attributes that form arguments of the action defined by the obligation."* (§5.34)
  > *"The `<Advice>` element SHALL contain an identifier for the advice and a set of attributes that
  > form arguments of the supplemental information defined by the advice."* (§5.35)
- **§5.39 `<ObligationExpression>`, §5.40 `<AdviceExpression>`, §5.41
  `<AttributeAssignmentExpression>`**:
  > *"The `<AttributeAssignmentExpression>` element is used for including arguments in obligations
  > and advice. It SHALL contain an AttributeId and an expression which SHALL by evaluated into the
  > corresponding attribute value. ... The expression which evaluates to a constant attribute value
  > or a bag of zero or more attribute values."* (§5.41)
- **§7.18 Obligations and advice**:
  > *"A rule, policy, or policy set may contain one or more obligation or advice expressions. When
  > such a rule, policy, or policy set is evaluated, the obligation or advice expression SHALL be
  > evaluated to an obligation or advice respectively, which SHALL be passed up to the next level of
  > evaluation (the enclosing or referencing policy, policy set, or authorization decision) only if
  > the result of the rule, policy, or policy set being evaluated matches the value of the FulfillOn
  > attribute of the obligation or the AppliesTo attribute of the advice. If any of the attribute
  > assignment expressions in an obligation or advice expression with a matching FulfillOn or
  > AppliesTo attribute evaluates to Indeterminate, then the whole rule, policy, or policy set SHALL
  > be Indeterminate."*

Because `assignmentList` preserves the exact deduplicated, first-seen-`attributeId`-grouped
sequence of `ImmutableSetMultimap.values()`, and `equalsTo` falls back to
`attributes().equals(r.attributes())` whenever two equal-sized assignment lists differ in order,
every set-semantic, grouping, ordering, and equality property of `Obligation` and `Advice` is
unchanged.

**Compatibility.**

- **Public and protected API**: No public or protected class, constructor, method, or field is
  removed or narrowed. `DecisionRuleResponse.attributes` remains
  `protected Multimap<String, AttributeAssignment> attributes`, and
  `DecisionRuleResponse.Builder.attributes` remains
  `protected Multimap<String, AttributeAssignment> attributes = LinkedHashMultimap.create()`.
- **External subclasses (R-AN P2-2 and P2-3)**:
  - `Advice` is `final`, and `Obligation` has only a package-private `Obligation(Builder)` and a
    private constructor; neither can be subclassed outside `org.xacml4j.v30`.
  - Any external subclass of `DecisionRuleResponse` (such as `Receipt` in
    `ExternalResponseBuilderCompatibilityTest`) must call `protected DecisionRuleResponse(Builder<?>)`
    (the collection constructor is package-private). Under JLS §6.6.2.1, an external subclass can
    access the `protected` field `attributes` only on a reference of its own subclass type, and
    `DecisionRuleResponse(Builder<?>)` eagerly populates `this.attributes` with
    `ImmutableSetMultimap.copyOf(b.attributes)`.
  - External subclasses of `ObligationExpression` and `AdviceExpression`
    (`ExternalObligationExpressionCompatibilityTest`) continue to evaluate via `Obligation.of` /
    `Advice.of` or their overridden `evaluate` methods.
- **Detachment**: `DecisionRuleResponse(String, Effect, Collection<AttributeAssignment>)` still
  calls `ImmutableList.copyOf(assignments)`, detaching any mutable caller collection.
- **Thread safety**: `assignmentList` is an `ImmutableList<AttributeAssignment>`. Lazy
  initialization of `this.attributes` and `this.hashCode` computes deterministic values from
  immutable state (`ImmutableSetMultimap` has `final` fields and is safely published under JLS
  §17.5, matching `Result.hashCode` and Guava's own lazy views).
- **Marshalling**: XML (`Xacml30RequestContextFromJaxbToObjectModelMapper`,
  `Xacml20ResponseContextMarshaller`) and JSON (`ObligationOrAdviceAdapter`) iterate
  `getAttributes()`, which returns the same elements in the same order.

**Cost and gate.**

- **`rules:100 responses` (index on)**: removes the 25 dedicated `ImmutableSetMultimap.Builder`,
  `LinkedHashMap`, `LinkedHashSet`, `ImmutableSetMultimap`, `RegularImmutableMap`,
  `RegularImmutableSet`, `SingletonImmutableSet`, and `RegularEntrySet` allocations (**1,016 B**)
  plus **~160 B** of temporary/retained `Object[]` and `RegularImmutableList` allocations =
  **about −1,176 B/op** (`2,336 -> ~1,160 B/op`, **−50%**).
- **`rsa2008`, `narrow`, `multi-valued`, `synthetic:100`**: **0 B/op** change (`rsa2008` responses
  are constant and cached at policy build time by cycle AN; the other three emit no obligations or
  advice). `DecisionRuleResponse` instance size increases by at most 0–8 B (`assignmentList` 4 B
  reference; `DecisionRuleResponse` with `id` 4 B, `attributes` 4 B, `assignmentList` 4 B,
  `fulfillOn` 4 B, `hashCode` 4 B + 12 B header = **32 B**, unchanged from 32 B in `pr23:134`!).
- **Gate**: a three-fork controlled JMH pair (`PdpDecideBenchmark.decide`, `-prof gc`) on
  `rules:100 responses` (index on) and `rsa2008` (index off) before and after, plus full reactor
  unit and conformance suites. Keep the change if `rules:100 responses` drops by ~1.1–1.2 KB/op
  with no regression on `rsa2008`.

**Files.**

- `xacml-core/src/main/java/org/xacml4j/v30/DecisionRuleResponse.java`
- `xacml-core/src/main/java/org/xacml4j/v30/Obligation.java` (optional: `merge` can avoid
  `Builder` / `LinkedHashMultimap` when combining two responses)
- `xacml-core/src/main/java/org/xacml4j/v30/Advice.java` (same for `merge`)
- `xacml-core/src/test/java/org/xacml4j/v30/ResponseFactoryTest.java`

**Test plan (outcomes only).**

- `Obligation.of` and `Advice.of` with:
  - zero, one, two, and three+ assignments with distinct and repeated `attributeId`s (already
    grouped vs interleaved `[b=1, a=1, b=2]`, and with duplicate `(attributeId, category, issuer,
    value)` entries) produce the exact same `getAttributes()` sequence, `getAttribute(id)` sets,
    `equals`, `hashCode`, and `toString` as `Obligation.builder(...)` / `Advice.builder(...)`;
  - two responses with the same assignments added in different key or value orders compare `equals`
    in both directions (`of.equals(built)` and `built.equals(of)`) and have equal `hashCode()`;
  - `getAttribute(unknownId)` returns an empty unmodifiable collection, and mutating
    `getAttributes()` or `getAttribute(id)` throws `UnsupportedOperationException`;
  - `Obligation.merge`, `Advice.merge`, and `Builder.from` preserve first-seen `attributeId`
    grouping and deduplication across factory-built and builder-built responses;
  - `ExternalResponseBuilderCompatibilityTest` and `ExternalObligationExpressionCompatibilityTest`
    pass unchanged.
```

---

## 3. Open Questions and Recommendations

1. **Should `protected DecisionRuleResponse(Builder<?> b)` stay eager for `this.attributes`, while `DecisionRuleResponse(String, Effect, Collection<AttributeAssignment>)` initializes `this.attributes` lazily on demand?**
   - **Recommendation: Yes.** Only `DecisionRuleResponse(Builder<?> b)` is `protected` and reachable by external subclasses (`DecisionRuleResponse(String, Effect, Collection)` is package-private, `Advice` is `final`, and `Obligation` has no `public` or `protected` constructors). Keeping `this.attributes = ImmutableSetMultimap.copyOf(b.attributes)` eager in `DecisionRuleResponse(Builder<?> b)` guarantees that any external subclass reading `this.attributes` directly in its constructor or methods sees the exact `ImmutableSetMultimap` instance without calling a helper, while `Obligation.of` and `Advice.of` pay 0 B for multimap construction during PDP evaluation.
2. **Should `DecisionRuleResponse` object size stay at `32 B`?**
   - **Recommendation: Yes (verified: `32 B` is preserved automatically).** Under 64-bit HotSpot with compressed references and 8-byte alignment, `DecisionRuleResponse` currently has a `12 B` header + `4 B` `id` + `4 B` `attributes` + `4 B` `fulfillOn` + `4 B` `hashCode` = `28 B`, padded to **`32 B`** (confirmed in [`pr23-astra-fixed3-profile.md:134`](`.ntm/worktrees/xacml4j--batch2/agyx_1/openspec/changes/optimize-pdp-evaluation/research/pr23-astra-fixed3-profile.md`): `1 Obligation = 32 B`). Adding `private final ImmutableList<AttributeAssignment> assignmentList` (`4 B` compressed oop) fills the existing `4 B` alignment gap (`28 + 4 = 32 B`), so `Obligation` and `Advice` remain **`32 B`** (`0 B` object-size increase!).
3. **Should `Obligation.merge` / `Advice.merge` and P-FIXED3 R4 (mixed-literal preparation in `BaseDecisionRuleResponseExpression`) be bundled with D-RESP or kept separate?**
   - **Recommendation:**
     - **Keep P-FIXED3 R4 (`BaseDecisionRuleResponseExpression` mixed-literal preparation, ~32–112 B) separate** so D-RESP touches only `DecisionRuleResponse` (and optionally `Obligation`/`Advice` in `org.xacml4j.v30`) and its JMH pair isolates the ~1,176 B multimap saving cleanly.
     - **`Obligation.merge` and `Advice.merge`**: optionally update `Obligation.from` / `Advice.from` or `merge` only if desired; `rules:100 responses` has distinct obligation IDs (`audit`, `notify`, `record`), so `merge` is not on its hot path. Leaving `merge` as-is (since `from(this)` calls `this.getAttributes()`, which now returns `this.assignmentList` directly without materializing `this.attributes`) already avoids materializing `this.attributes` on the source obligations.
