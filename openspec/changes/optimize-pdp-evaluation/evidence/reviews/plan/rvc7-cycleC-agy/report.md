<!-- Plan review RVC7 of design D7 (cycle C, as decided 2026-10-27), Agy Gemini 4 Argon, read-only; report taken from the Agy conversation database, paths made repository-relative. -->

# RVC7 Plan Review: Revised Design D7 (Cycle C — Type-Specialized Bag Storage, Primitive Fast Paths, and Serialization)

## Prioritized Findings (Most Severe First)

### [P1-1] `boolean` storage as "two counts" loses first-occurrence group order (`[false, true]` vs `[true, false]`)
- **Evidence (`SUPPORTED`)**:
  - [`design.md:144-146`](openspec/changes/optimize-pdp-evaluation/design.md#L140-L147) states: *"`boolean` as two counts, and the other types as value arrays. Every storage keeps today's iteration order (equal values grouped in the order of their first occurrence), size, counts, and set view."*
  - Two counts alone (`trueCount`, `falseCount`) cannot distinguish `[BooleanVal.FALSE, BooleanVal.TRUE, BooleanVal.FALSE]` from `[BooleanVal.TRUE, BooleanVal.FALSE, BooleanVal.FALSE]`. Both have `falseCount = 2` and `trueCount = 1`, yet under today's `ImmutableMultiset` first-occurrence grouping (pinned by [`BagOfValuesCharacterizationTest.java:69-78`](xacml-core/src/test/java/org/xacml4j/v30/BagOfValuesCharacterizationTest.java#L69-L78) and [`MatchTest.java:204-221`](xacml-core/src/test/java/org/xacml4j/v30/policy/MatchTest.java#L204-L221)), the first iterates as `[FALSE, FALSE, TRUE]` with `bag.value() == FALSE`, whereas the second iterates as `[TRUE, FALSE, FALSE]` with `bag.value() == TRUE`.
- **Recommendation**:
  - Store `(int trueCount, int falseCount, boolean trueFirst)` in the `boolean` storage (when both counts are non-zero, `trueFirst` records whether `TRUE` was added before `FALSE`).

### [P1-2] Hot-path callers in `HigherOrderFunctions`, `SetFunctions.double*`, `BagFunctions.doubleIsIn`, and `BagOfValues` still call `bag.values()`
- **Evidence (`SUPPORTED`)**:
  - D7 ([`design.md:122-125`](openspec/changes/optimize-pdp-evaluation/design.md#L122-L125)) makes [`BagOfValues.values()`](xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java#L78-L80) lazily build and cache a Guava `Multiset<Value<?>>`.
  - A repository search (`rtk rg '\.values\(\)'`) shows that [`HigherOrderFunctions.java:73,101,134,151,168,169,188,218`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/HigherOrderFunctions.java#L73-L221) (`any-of`, `all-of`, `any-of-any`, `all-of-any`, `any-of-all`, `all-of-all`, `map`), [`BagFunctions.doubleIsIn` (`BagFunctions.java:295`)](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/BagFunctions.java#L290-L296), [`SetFunctions.double*` (`SetFunctions.java:194-259`)](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/SetFunctions.java#L188-L264), [`BagOfValues.single()` (`BagOfValues.java:113`)](xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java#L112-L114), [`BaseDecisionRuleResponseExpression.java:133`](xacml-core/src/main/java/org/xacml4j/v30/policy/BaseDecisionRuleResponseExpression.java#L133), and `Quantified*.java` all call `bag.values()`.
  - In `rsa2008`, 4 of the 9 policy sets (`02a`, `02d`, `02e`, `02f`) call `urn:oasis:names:tc:xacml:1.0:function:any-of` on request bags. If `HigherOrderFunctions` continues to pass `bag.values()` to [`DecidingOperands`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DecidingOperands.java#L54-L93), every `any-of` evaluation in `rsa2008` will force lazy construction of the Guava `Multiset` (and box every element of a primitive storage), defeating Cycle C on those requests.
- **Recommendation**:
  - Update [`HigherOrderFunctions`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/HigherOrderFunctions.java), [`DecidingOperands`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DecidingOperands.java), [`BagFunctions.doubleIsIn`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/BagFunctions.java#L290-L296), [`SetFunctions.double*`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/SetFunctions.java#L188-L264), [`BagOfValues.single()`](xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java#L112-L114), [`BaseDecisionRuleResponseExpression`](xacml-core/src/main/java/org/xacml4j/v30/policy/BaseDecisionRuleResponseExpression.java#L133), and `Quantified*` in task 6.4 so none of them call `bag.values()`.

### [P1-3] Dual equality semantics on `double` bags: `DoubleVal.equals` (storage grouping & Java methods) vs IEEE `==` (XACML `double-*` functions)
- **Evidence (`SUPPORTED`)**:
  - [`DoubleBagSetEqualityTest.java`](xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/DoubleBagSetEqualityTest.java#L71-L285) and [`BagOfValuesCharacterizationTest.java:115-126`](xacml-core/src/test/java/org/xacml4j/v30/BagOfValuesCharacterizationTest.java#L115-L126) pin **two distinct double equality rules**:
    1. **Storage grouping & `BagOfValues` Java methods (`equals`, `hashCode`, `contains(Value)`, `containsAll`, `containsAtLeastOneOf`, `union`, `intersection`, `values()`)**: use `Double.doubleToLongBits(a) == Double.doubleToLongBits(b)` (matching `DoubleVal.equals` / `Double.equals`). Here, **all NaN bit patterns are equal** (`0x7ff8000000000000L` and `0x7ff8000000000001L` group together and produce equal bags; [`DoubleBagSetEqualityTest.java:256-285`](xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/DoubleBagSetEqualityTest.java#L256-L285)), and **`0.0d` is NOT equal to `-0.0d`** (`[0.0d, -0.0d, -0.0d]` has 1 `0.0d` and 2 `-0.0d`s; [`DoubleBagSetEqualityTest.java:236-251`](xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/DoubleBagSetEqualityTest.java#L236-L251)).
    2. **XACML 3.0 §A.3.1 / §A.3.10 / §A.3.11 `double-*` policy functions (`double-equal`, `double-is-in`, `double-subset`, `double-set-equals`, `double-at-least-one-member-of`, `double-union`, `double-intersection`)**: use IEEE 754 primitive `a == b` ([`EqualityPredicates.doubleEquals`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/EqualityPredicates.java#L194-L196)). Here, **`NaN != NaN`** (every NaN occurrence is retained in `double-union` and excluded in `double-intersection`, and `double-is-in(NaN, bag)` is `false`) and **`0.0d == -0.0d`** (`double-union` collapses `0.0d` and `-0.0d` to the first zero).
- **Recommendation**:
  - In `DoubleBagStorage`:
    - Group elements at build time by `Double.doubleToLongBits(a) == Double.doubleToLongBits(b)` (NOT `a == b` and NOT `doubleToRawLongBits`), while storing the first occurrence's `double` in `double[]` so `-0.0d` vs `+0.0d` bit signs are preserved.
    - Keep `BagOfValues.contains` / `containsAll` / `union` / `intersection` using `Double.doubleToLongBits` equality, and provide separate primitive IEEE `==` methods on `BagOfValues` / `DoubleBagStorage` for `BagFunctions.doubleIsIn` and `SetFunctions.double*`.

### [P2-1] Unnecessary unboxing-then-reboxing in `Entity.resolve` $\to$ `*-one-and-only` (`bag.value()`) for `integer` and `double` bags
- **Evidence (`SUPPORTED`)**:
  - Request attributes in [`Attribute`](xacml-core/src/main/java/org/xacml4j/v30/Attribute.java) already store `List<Value<?>>` (`IntegerVal`, `DoubleVal`, `BooleanVal` instances created when the request was parsed).
  - [`Entity.resolve`](xacml-core/src/main/java/org/xacml4j/v30/types/Entity.java#L207-L215) passes `Collection<Value<?>> v` to `type.bagType().builder().attributes(v).build()`.
  - If `IntegerBagStorage` / `DoubleBagStorage` unboxes `v` into `new long[n]` / `new double[n]` and discards the `IntegerVal` / `DoubleVal` reference(s), then every call to `integer-one-and-only` or `double-one-and-only` ([`BagFunctions.oneAndOnlyImpl`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/BagFunctions.java#L78-L83) $\to$ `bag.value()`) or `HigherOrderFunctions` must call [`IntegerVal.of(long)`](xacml-core/src/main/java/org/xacml4j/v30/types/IntegerVal.java#L63-L65) or [`DoubleVal.of(double)`](xacml-core/src/main/java/org/xacml4j/v30/types/DoubleVal.java#L57-L60), which allocates a new `IntegerVal` + `Long` (outside `-128..127`) or `DoubleVal` + `Double` on every `value()` call!
- **Recommendation**:
  - In `LongBagStorage` and `DoubleBagStorage`, keep a `private Value<?> firstValue` field (populated without allocation when built from `Value<?>` via `Builder.attribute` / `Builder.attributes`, or lazily cached on first `value()` call). Because `LongBagStorage` with `long[] values` is 16 B (12 B header + 4 B oop) padded to 24 B under 8-byte alignment, adding a 4 B `Value<?> firstValue` reference fits in the **exact same 24 B object size** (0 extra bytes!) and makes `bag.value()` / `single()` / `one-and-only` **zero-allocation**.
  - Additionally, add a small-integer cache `[-128..255]` in [`IntegerVal.of(long)`](xacml-core/src/main/java/org/xacml4j/v30/types/IntegerVal.java#L63-L65) (and avoid boxing `Long` before `IntegerVal(long)`).

### [P2-2] `BagOfValues.hashCode()` must compute the exact Guava `Multiset.hashCode()` formula without building the `Multiset`
- **Evidence (`SUPPORTED`)**:
  - [`BagOfValues.hashCode()`](xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java#L231-L239) returns `Objects.hash(values, type)` = `31 * (31 + values.hashCode()) + type.hashCode()`, and `hashCode` is part of the serialized stream of `BagOfValues` (line 65).
  - By [`Multiset.hashCode()`](xacml-core/src/test/java/org/xacml4j/v30/BagOfValuesCharacterizationTest.java#L92-L100), `values.hashCode()` is the order-independent sum over distinct elements $e$:
    $$\sum_{e \in \text{distinct}} \left(\text{e.hashCode()} \wedge \text{count}(e)\right)$$
    where for [`Value.OtherValue`](xacml-core/src/main/java/org/xacml4j/v30/types/Value.java#L128-L130) (`IntegerVal`, `DoubleVal`, `BooleanVal`, `StringVal`, etc.), `e.hashCode()` is `31 * (31 + rawValue.hashCode()) + valueType.hashCode()`.
- **Recommendation**:
  - Because every storage groups equal elements contiguously, each storage can compute the exact `Multiset.hashCode()` in one pass over its contiguous equal runs:
    - For `LongBagStorage`: for each run of value $v$ with length $c$, add `((31 * (31 + Long.hashCode(v)) + XacmlTypes.INTEGER.hashCode()) ^ c)`.
    - For `DoubleBagStorage`: for each run of value $v$ (grouped by `Double.doubleToLongBits(v)`) with length $c$, add `((31 * (31 + Double.hashCode(v)) + XacmlTypes.DOUBLE.hashCode()) ^ c)`.
    - For `BooleanBagStorage`: add `((31 * (31 + Boolean.hashCode(true)) + XacmlTypes.BOOLEAN.hashCode()) ^ trueCount)` (if `trueCount > 0`) and `((31 * (31 + Boolean.hashCode(false)) + XacmlTypes.BOOLEAN.hashCode()) ^ falseCount)` (if `falseCount > 0`).
  - Pin this in task 6.3: assert that for every storage type, `bag.hashCode()` without calling `values()` equals `31 * (31 + bag.values().hashCode()) + bag.getEvaluatesTo().hashCode()`.

---

## Detailed Answers to the 6 Review Questions

### 1. Storage SPI on [`ValueType`](xacml-core/src/main/java/org/xacml4j/v30/types/ValueType.java)
- **Is `ValueType` the right seam?**
  - **Yes**, with one refinement on visibility: [`ValueType`](xacml-core/src/main/java/org/xacml4j/v30/types/ValueType.java) is a `public` interface in `org.xacml4j.v30.types`, whereas [`BagOfValues`](xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java) is in `org.xacml4j.v30`. Therefore, any default method on `ValueType` (and the storage / storage-builder type it returns) must be `public` (e.g., `BagStorage` or `BagOfValues.Storage`).
- **What the SPI and storage interface should look like**:
  - Define `public interface BagStorage` (or `BagOfValues.Storage`) and `public interface BagStorageBuilder` (or `BagOfValues.StorageBuilder`), with an additive default method on [`ValueType`](xacml-core/src/main/java/org/xacml4j/v30/types/ValueType.java):
    ```java
    default BagOfValues.StorageBuilder bagStorageBuilder() {
        return BagOfValues.objectStorageBuilder(this);
    }
    ```
  - **StorageBuilder contract**:
    - `void add(Value<?> value)` (validates `value.getEvaluatesTo().equals(type)` as [`BagOfValues.assertExpressionType`](xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java#L271-L277) does today)
    - `default void addAll(Iterable<? extends Value<?>> values)`
    - `default void addRaw(Object value)` (calls `add(type.ofAny(value))` by default; `INTEGER`, `DOUBLE`, `BOOLEAN` override to parse/unbox `Long`/`Double`/`Boolean`/`String` directly into the primitive buffer without allocating a wrapper `Value<?>`)
    - `BagOfValues.Storage build()`
  - **Storage contract**:
    - `int size()`
    - `default boolean isEmpty() { return size() == 0; }`
    - `Value<?> valueAt(int index)` (returns the $i$-th element in grouped first-occurrence iteration order; for `index == 0` on `LongBagStorage` / `DoubleBagStorage`, returns cached `firstValue` if present)
    - `boolean contains(Value<?> value)`
    - `int count(Value<?> value)`
    - `int distinctCount()` and `Value<?> distinctValueAt(int distinctIndex)` (or `Set<Value<?>> elementSet()`, enabling allocation-light set views and `Multiset` construction)
    - `int multisetHashCode()` (computes $\sum (\text{hash}(e) \wedge \text{count}(e))$)
    - `boolean multisetEquals(BagOfValues.Storage other)` (order-independent multiplicity equality: compares `size()`, `distinctCount()`, and for each distinct element in `this`, checks `count(e) == other.count(e)`)
    - `Multiset<Value<?>> toMultiset()` (builds the unmodifiable `Multiset` view on demand when `BagOfValues.values()` or `writeObject` is called)
    - Package-private or internal predicate hook (see Question 2) so primitive arrays never escape into the public API.
- **How `BagOfValues.Builder` and custom `ValueType`s opt in or out**:
  - [`BagOfValues.Builder`](xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java#L279-L331) delegates `value(...)`, `values(...)`, `attribute(...)`, `attributes(...)` to `this.storageBuilder = bagType.getValueType().bagStorageBuilder()`.
  - Also add a fast path in `Entity.resolve`: when `v` is a 1-element `List<Value<?>>` (the 99% case in XACML requests), avoid resizing buffers or hash maps.
  - A custom `ValueType` that does nothing automatically inherits `ValueType.bagStorageBuilder()` (`ObjectArrayBagStorage`) and keeps any custom `bagBuilder()` override (preserving [`EmptyBagCharacterizationTest.customTypeKeepsItsBagBuilderAndIsNotShared`](xacml-core/src/test/java/org/xacml4j/v30/policy/EmptyBagCharacterizationTest.java#L125-L136)). A custom primitive-backed `ValueType` can override `bagStorageBuilder()` to supply its own `Storage`.
- **How iteration order, counts, equality, hash, and set view stay identical across storages**:
  - **Grouping algorithm at build time**:
    - For $n \le 1$: already grouped.
    - For $2 \le n \le 8$: in-place or small-buffer grouping by first occurrence ($O(n^2)$ comparisons — at most 28 primitive/object comparisons, zero hash table allocation!). When duplicates exist (`[a, b, a]`), compact equal elements adjacent to their first occurrence (`[a, a, b]`).
    - For $n > 8$: group using a temporary open-addressed table or `LinkedHashMap` (for objects) into the contiguous grouped array.
  - Because the grouped array stores `[e_0, ..., e_0, e_1, ..., e_1, ...]`, adjacent elements are equal, so:
    - **Iteration order**: `0 .. size - 1` is identical to `ImmutableMultiset` (`[a, a, b]`).
    - **Counts**: a linear scan (or binary/linear search for $e_k$ followed by counting the contiguous run) gives `count(e)`.
    - **Equality (`BagOfValues.equals`)**: checks `type.equals(other.type) && storage.size() == other.storage.size() && storage.multisetEquals(other.storage)`. Because equal values are contiguous runs in each storage, `multisetEquals` simply checks that the number of runs (`distinctCount`) is equal and for each run $(v, c)$ in `this`, `other.count(v) == c`!
    - **Set operations (`union`, `intersection`, `containsAll`, `containsAtLeastOneOf`)**:
      - `union(other)`: appends distinct values (one per contiguous run) of `this` and `other` into a new `Builder`, which deduplicates them in first-occurrence order — no `Sets.union` or `Multiset` needed!
      - `intersection(other)`: iterates each distinct run leader $v$ of `this`; if `other.contains(v)`, adds $v$ once to `Builder`.
      - `containsAll(other)`: for each distinct run leader $v$ of `other`, checks `this.contains(v)`.
      - `containsAtLeastOneOf(other)`: for each distinct run leader $v$ of `other`, checks `this.contains(v)`.

---

### 2. The Primitive Fast Path (`Match`, Bag Functions, Set Functions, Higher-Order Functions)
- **Which built-in predicates should `long[]` / `double[]` / `boolean` / object storage answer directly?**
  1. **`LongBagStorage` (`XacmlTypes.INTEGER`)**:
     - `urn:oasis:names:tc:xacml:1.0:function:integer-equal` (`lit == v`)
     - `urn:oasis:names:tc:xacml:1.0:function:integer-greater-than` (`lit > v` in `Match` where `lit` is arg0 and bag element `v` is arg1; or `v > lit` when bag is arg0 in `any-of`!)
     - `urn:oasis:names:tc:xacml:1.0:function:integer-less-than` (`lit < v`)
     - `urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal` (`lit >= v`)
     - `urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal` (`lit <= v`)
     - Plus in `BagFunctions` / `SetFunctions`: `integer-is-in`, `integer-subset`, `integer-set-equals`, `integer-at-least-one-member-of`, `integer-union`, `integer-intersection`.
  2. **`DoubleBagStorage` (`XacmlTypes.DOUBLE`)**:
     - `urn:oasis:names:tc:xacml:1.0:function:double-equal` (`lit == v`, IEEE 754 primitive `==`)
     - `urn:oasis:names:tc:xacml:1.0:function:double-greater-than` (`lit > v`)
     - `urn:oasis:names:tc:xacml:1.0:function:double-less-than` (`lit < v`)
     - `urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal` (`lit >= v`)
     - `urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal` (`lit <= v`)
     - Plus in `BagFunctions` / `SetFunctions`: `double-is-in`, `double-subset`, `double-set-equals`, `double-at-least-one-member-of`, `double-union`, `double-intersection` (all using IEEE `==`, matching [`DoubleBagSetEqualityTest`](xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/DoubleBagSetEqualityTest.java)).
  3. **`BooleanBagStorage` (`XacmlTypes.BOOLEAN`)**:
     - `urn:oasis:names:tc:xacml:1.0:function:boolean-equal` (`lit ? trueCount > 0 : falseCount > 0`, $O(1)$!).
     - Plus `boolean-is-in`, `boolean-subset`, `boolean-set-equals`, `boolean-at-least-one-member-of`, `boolean-union`, `boolean-intersection` in $O(1)$ using the counts and `trueFirst`!
  4. **`ObjectArrayBagStorage` (`STRING`, `ANYURI`, `DATE`, `DATETIME`, `TIME`, `DAYTIMEDURATION`, `YEARMONTHDURATION`, `X500NAME`, `RFC822NAME`, `HEXBINARY`, `BASE64BINARY`, `ENTITY`)**:
     - All 12 standard `*-equal` functions in [`EqualityPredicates`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/EqualityPredicates.java#L52-L191) (`_eq(a, b)` $\equiv$ `a.equals(b)`), plus `string-equal-ignore-case`.
- **How the fast path is selected (provenance, NOT function ID)**:
  - Extending Cycle I's mechanism in [`DefaultFunctionInvocation.create`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java#L132-L141):
    - When `factory == FunctionInvocationFactory.defaultFactory() && instance == null` and `m.getDeclaringClass()` is `EqualityPredicates.class` or `NumericComparisonFunctions.class`, tag the `DefaultFunctionInvocation` (and its owning `FunctionSpecImpl`) with an internal `KnownPredicate` enum constant (e.g., `INT_EQ`, `INT_GT`, `INT_LT`, `INT_GTE`, `INT_LTE`, `DBL_EQ`, `DBL_GT`, `DBL_LT`, `DBL_GTE`, `DBL_LTE`, `BOOL_EQ`, `VALUE_EQ`, `STRING_EQ_IGNORE_CASE`).
    - A custom `FunctionSpec` or custom `FunctionInvocationFactory` (such as the Mockito mock in [`MatchTest.java:68-73`](xacml-core/src/test/java/org/xacml4j/v30/policy/MatchTest.java#L68-L73)) has `knownPredicate == null` and always takes the generic element-by-element invocation path.
    - In [`Match.match(EvaluationContext)`](xacml-core/src/main/java/org/xacml4j/v30/policy/Match.java#L111-L139): only take the storage fast path when `knownPredicate != null && !context.isValidateFuncParamsAtRuntime()`.
- **How errors, NaN, signed zero, and `policy4j-0972` stay identical**:
  - None of the 11 primitive comparison/equality methods (`EqualityPredicates.eq`, `NumericComparisonFunctions.*`) or 12 `_eq` methods can ever throw an exception on valid typed values (`IntegerVal`, `DoubleVal`, `BooleanVal`, etc.). And `Match` validates `(value, attributeRef)` types in its constructor ([`Match.java:61-75`](xacml-core/src/main/java/org/xacml4j/v30/policy/Match.java#L61-L75)), while `BagOfValues` validates element types when built ([`BagOfValues.java:271-277`](xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java#L271-L277)).
  - Therefore, on a built-in `KnownPredicate`, no per-element error can occur, so scanning the distinct runs (or the primitive array) produces the **exact same `MatchResult.MATCH` or `MatchResult.NOMATCH`** as the generic loop, including for `NaN` (`NaN == x`, `NaN < x`, `NaN > x`, `NaN <= x`, `NaN >= x` are all `false` in IEEE 754) and signed zero (`0.0 == -0.0` is `true`, `0.0 < -0.0` is `false`).
  - For any **other** predicate (custom functions, or functions that can fail on an element like `regexp-string-match` or `xpath-node-match`), `Match.match` iterates `for (int i = 0, n = attributes.size(); i < n; i++)` over `attributes.valueAt(i)` in the grouped first-occurrence order (`[2, 2, 3]`), preserving `policy4j-0972` ([`MatchTest.failureOnAGroupedDuplicateComesBeforeALaterMatchingValue`](xacml-core/src/test/java/org/xacml4j/v30/policy/MatchTest.java#L204-L221)) identically.
- **Where a generic path still boxes values, and is that acceptable?**
  - Boxing (`IntegerVal.of(longs[i])` / `DoubleVal.of(doubles[i])`) only occurs when:
    1. A multi-element `integer` or `double` bag is passed to a custom/non-fast-path predicate in `Match` or `HigherOrderFunctions`, or
    2. A caller explicitly invokes `bag.values()`, `bag.stream()`, or `writeObject`.
  - If [P2-1] is applied (retaining `firstValue` on `LongBagStorage` / `DoubleBagStorage` when constructed from `Value<?>` and caching small `IntegerVal`s), even `integer-one-and-only` and `double-one-and-only` (`bag.value()`) do **zero boxing**. That makes residual boxing on the generic multi-element custom-predicate path completely acceptable.

---

### 3. Serialization Compatibility (`serialPersistentFields` + `writeObject` / `readObject`)
- **Is `serialPersistentFields` plus `writeObject` / `readObject` enough to keep the stream of `BagOfValues` identical in both directions?**
  - **Yes**, under Section 1.10, 2.1, and 3.1 of the Java Object Serialization Specification, **provided three exact invariants hold**:
    1. `serialVersionUID` remains `-8197446176793438616L` ([`BagOfValues.java:61`](xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java#L61)).
    2. `serialPersistentFields` declares the exact three field descriptors of today's class:
       ```java
       private static final ObjectStreamField[] serialPersistentFields = {
           new ObjectStreamField("hashCode", int.class),
           new ObjectStreamField("type", BagOfValuesType.class),
           new ObjectStreamField("values", Multiset.class)
       };
       ```
       And `writeObject(ObjectOutputStream out)` writes:
       ```java
       ObjectOutputStream.PutField fields = out.putFields();
       fields.put("hashCode", hashCode);
       fields.put("type", type);
       fields.put("values", values()); // MUST be a Guava ImmutableMultiset instance!
       out.writeFields();
       ```
       - **Subtlety on `SC_WRITE_METHOD` flag in the stream**:
         - When a class defines `private void writeObject(ObjectOutputStream)`, `ObjectStreamClass` sets the `ObjectStreamConstants.SC_WRITE_METHOD` (`0x01`) bit in the class descriptor flags and appends an `TC_ENDBLOCKDATA` (`0x78`) byte after the field data.
         - Does an **old** `BagOfValues` (which has no `writeObject`) read a stream written by the **new** `BagOfValues` (which has `writeObject` and `SC_WRITE_METHOD`)? **Yes!** Per Java Object Serialization Spec §3.1 / `ObjectInputStream.readSerialData`, when the reading class has no `readObject` method and the stream class descriptor has `SC_WRITE_METHOD`, `ObjectInputStream` calls `defaultReadFields` and then skips any optional data up to `TC_ENDBLOCKDATA`!
         - Does the **new** `BagOfValues` (which has `readObject`) read a stream written by the **old** `BagOfValues` (which has `SC_SERIALIZABLE` without `SC_WRITE_METHOD`)? **Yes!** `in.readFields()` reads the default-serialized fields (`hashCode`, `type`, `values`) regardless of whether `SC_WRITE_METHOD` was set in the stream.
       - **Subtlety on the runtime class of `values` in the stream**:
         - In the old class, `this.values` was always built by `ImmutableMultiset.Builder.build()` (`RegularImmutableMultiset` or `SingletonImmutableMultiset`, serialized via `ImmutableMultiset$SerializedForm`).
         - Therefore, `toMultiset()` / `values()` in the new class **must return an `ImmutableMultiset<Value<?>>`** (built via `ImmutableMultiset.Builder`), NOT a custom `AbstractMultiset` subclass that does not exist in the old jar! Otherwise, an old jar reading a stream written by the new jar would fail with `ClassNotFoundException` on the custom `Multiset` class!
    3. In `readObject(ObjectInputStream in)`:
       ```java
       ObjectInputStream.GetField fields = in.readFields();
       BagOfValuesType readType = (BagOfValuesType) fields.get("type", null);
       Multiset<Value<?>> readValues = (Multiset<Value<?>>) fields.get("values", null);
       int readHash = fields.get("hashCode", 0);
       if (readType == null || readValues == null) {
           throw new InvalidObjectException("BagOfValues stream is missing type or values");
       }
       // assign non-final fields or via reflection/Unsafe if final, then VarHandle.releaseFence()
       ```
       Note that `type` and `storage` cannot be assigned directly inside `readObject` if they are declared `final`; either make `type` and `storage` non-final `private` fields with a `VarHandle.releaseFence()` at the end of `readObject`, or use a `Field` setter.
- **What golden-stream tests prove it**:
  1. Before editing `BagOfValues`, capture the exact serialized bytes (hex/Base64 constants in the test) produced by `ObjectOutputStream` on the current class for:
     - Empty bags (`STRING`, `INTEGER`, `DOUBLE`, `BOOLEAN`)
     - Singleton bags (`STRING`, `INTEGER`, `DOUBLE`, `BOOLEAN`)
     - Multi-element bags with interleaved duplicates (`[A, B, A, C, B]` for `STRING`; `[2L, 3L, 2L]` for `INTEGER`; `[0.0d, -0.0d, Double.NaN, 0.0d]` for `DOUBLE`; `[false, true, false]` for `BOOLEAN`), both before and after calling `bag.hashCode()`.
  2. Assert that the new `BagOfValues` deserializes every golden byte array into an equal bag with identical iteration order, `hashCode()`, and `values()`.
  3. Assert that serializing those bags with the new `BagOfValues` and reading them back through an isolated `URLClassLoader` loading the **pre-change `BagOfValues.class`** (or checking the serialized stream descriptor and `ImmutableMultiset$SerializedForm` structure) succeeds and produces an equal old `BagOfValues`.

---

### 4. Eclipse Collections 13.0.0 Evaluation
- **Is the recorded recommendation in D7 right?**
  - **Yes (`SUPPORTED`).** The recommendation in [`design.md:164-177`](openspec/changes/optimize-pdp-evaluation/design.md#L164-L177) is sound on every technical point:
    1. Eclipse Collections' `LongHashBag` / `ImmutableLongHashBag` is backed by `LongIntHashMap` (open-addressed hash table), which does **not** preserve first-occurrence insertion order (violating XACML 3.0 §7.6 error ordering pinned by `policy4j-0972`) and allocates a hash table per bag.
    2. Typical XACML request bags have 1–4 elements; a flat grouped `long[]` / `double[]` / `(trueCount, falseCount, trueFirst)` has lower memory footprint, zero hashing overhead, and contiguous memory for SIMD scans.
    3. Adding a 12.5 MB dependency (`eclipse-collections-api` + `eclipse-collections`) to `xacml-core` for a 1–4 element array is unwarranted; reserving Eclipse Collections for measured primitive-keyed hash-table hotspots is the right boundary.
- **Would implementing Eclipse Collections' API interfaces (`ImmutableLongBag` or `LongIterable`) on our storage be worth its size and contract?**
  - **No.** `LongIterable` in `eclipse-collections-api` (1.9 MB JAR) pulls in dozens of transitive interface methods (`forEach`, `injectInto`, `count`, `anySatisfy`, `allSatisfy`, `noneSatisfy`, `detectIfNone`, `collect`, `sum`, `max`, `min`, `average`, `median`, `toArray`, `toSortedArray`, `toBag`, `toList`, `toSet`, `asLazy`, etc.) and couples `xacml-core`'s public/SPI surface to an external collection library. Keep `xacml-core` dependency-free here; if an external caller ever wants an adapter, it is a 5-line wrapper outside `BagStorage`.

---

### 5. Vector API (`jdk.incubator.vector`) Readiness
- **What in the storage design keeps a future SIMD scan internal?**
  1. **Encapsulation of backing arrays**: `LongBagStorage` and `DoubleBagStorage` are package-private (or private static) classes inside `org.xacml4j.v30` / `org.xacml4j.v30.types`; neither `BagOfValues` nor `BagStorage` exposes `long[]` or `double[]`.
  2. **Push-down predicate evaluation on `BagStorage`**: `Match` (and `BagFunctions` / `SetFunctions`) asks the storage to evaluate the predicate on the literal (`storage.match(knownPredicate, literalValue)` or `storage.containsLong(long)` / `storage.containsDoubleIeee(double)`), rather than pulling elements out of the storage in `Match`.
  3. **Contiguous primitive layout**: because `LongBagStorage` and `DoubleBagStorage` store elements in a flat `long[]` or `double[]` (or a deduplicated `long[] distinctValues` + `int[] counts` — note that for pure boolean predicates like `Match`, `contains`, `subset`, `at-least-one-member-of`, duplicate values need not even be tested twice when the predicate cannot throw!), a future JDK `LongVector.fromArray(SPECIES_PREFERRED, values, i)` / `DoubleVector.fromArray(...)` loop drops straight into `LongBagStorage.match` without changing a single signature in `BagOfValues`, `ValueType`, or `Match`.
  - **Design tip for Vector API**: in `LongBagStorage` and `DoubleBagStorage`, consider storing `long[] values` (all elements in grouped order, or `long[] distinct` + `int[] counts` when duplicates are present). Even simpler: when a bag has no duplicates (the 99% case), `counts` is `null`, and `values` has length $n$, which is already optimal for both indexed access (`valueAt(i)`) and SIMD vector lanes!

---

### 6. Risks, `BREAKING` Items, and Recommended Task Order
- **Risks & `BREAKING` Check**:
  - **No `BREAKING` changes** if:
    1. `BagOfValues` keeps `serialVersionUID = -8197446176793438616L`, `serialPersistentFields` (`hashCode`, `type`, `values`), and `writeObject` creates an `ImmutableMultiset` for the `"values"` stream field.
    2. `BagOfValues.values()` still returns an unmodifiable Guava `Multiset<T>`.
    3. `ValueType` gains only additive `default` methods.
    4. `XacmlTypes.emptyBag()` continues to return a shared singleton empty bag per built-in type, while `bagBuilder().build()` returns a new `BagOfValues` shell sharing the singleton empty `BagStorage` (preserving [`EmptyBagCharacterizationTest.java:135`](xacml-core/src/test/java/org/xacml4j/v30/policy/EmptyBagCharacterizationTest.java#L135)).
- **Recommended Task Order for Group 6 (`tasks.md`)**:
  1. **Task 6.2 (Plan update)**: Update [`design.md` D7](openspec/changes/optimize-pdp-evaluation/design.md#L140-L160) and [`tasks.md` group 6](openspec/changes/optimize-pdp-evaluation/tasks.md#L33-L40) to incorporate [P1-1] (`boolean` storage `trueFirst` flag), [P1-2] (`HigherOrderFunctions`, `SetFunctions.double*`, `BagFunctions.doubleIsIn`, `BagOfValues.single` off `bag.values()`), [P1-3] (dual `double` equality: `Double.doubleToLongBits` for storage/Java methods vs IEEE `==` for XACML `double-*` functions), [P2-1] (`firstValue` retention on primitive storages for 0-alloc `value()` / `one-and-only`), and [P2-2] (exact `Multiset.hashCode()` formula on `BagStorage`).
  2. **Task 6.3 (Red / Golden & Equivalence Tests First)**:
     - Capture golden serialized byte arrays from the current `BagOfValues` across `STRING`, `INTEGER`, `DOUBLE` (with `0.0d`, `-0.0d`, `NaN`), and `BOOLEAN` (`[false, true, false]` and `[true, false, false]`) before modifying `BagOfValues`.
     - Add storage equivalence tests checking `size()`, `isEmpty()`, `value()`, `single()`, iteration order (`[a, b, a, c, b]` $\to$ `[a, a, b, b, c]`), `count()`, `equals()`, `hashCode()` (verified equal to `Objects.hash(bag.values(), bag.getEvaluatesTo())` *before* calling `bag.values()`), `union()`, `intersection()`, `containsAll()`, `containsAtLeastOneOf()`.
     - Add fast-path vs generic-path tests in `MatchTest` covering all 11 numeric/boolean predicates + equality predicates, NaN, `+0.0d`/`-0.0d`, runtime validation on/off, custom `FunctionInvocation` with a standard function ID (must not take fast path), and `policy4j-0972` failing predicates.
  3. **Task 6.4a (Implement `BagStorage` SPI & Storages + `BagOfValues` Serialization)**:
     - Implement `BagOfValues.Storage`, `ObjectArrayBagStorage`, `LongBagStorage`, `DoubleBagStorage`, `BooleanBagStorage`, `ValueType` default method, `XacmlTypes` overrides, and `BagOfValues` methods (`serialPersistentFields`, `writeObject`, `readObject`, `single()`, `union()`, `intersection()`, `containsAll()`, `containsAtLeastOneOf()`, `equals()`, `hashCode()`).
     - Verify 6.1, 6.3, `EmptyBagCharacterizationTest`, `DoubleBagSetEqualityTest`, and `BagOfValuesTest`.
  4. **Task 6.4b (Wire `Match`, `BagFunctions`, `SetFunctions`, `HigherOrderFunctions`, `DecidingOperands`, `BaseDecisionRuleResponseExpression`, `Quantified*`)**:
     - Tag provenance-verified predicates in `DefaultFunctionInvocation` / `FunctionSpecImpl`.
     - Wire `Match.match` fast path and indexed fallback loop.
     - Switch `BagFunctions.doubleIsIn`, `SetFunctions.double*`, `HigherOrderFunctions`, `DecidingOperands`, `BaseDecisionRuleResponseExpression`, and `Quantified*` off `bag.values()` to indexed / primitive-storage methods.
  5. **Task 6.5 (Gate)**:
     - Full reactor `clean test` and conformance suites (`Xacml20ConformanceTest`, `Xacml30ConformanceTest`, `RSA2008InteropTest`), code review from another model family, and JMH measurement with batch 3.

---

## Verdict

**APPROVE WITH CHANGES** — Address [P1-1], [P1-2], [P1-3], [P2-1], and [P2-2] in D7 / tasks 6.3–6.4, then proceed to implementation.
