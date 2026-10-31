# Code Review `R-AODEC-argon`: Cycle AO (Temporal Bag Scans) & `I-DECLINE` (No-Op Index Guards)

- **Reviewer:** Gemini Argon (`agyx_1`)
- **Changes Reviewed:**
  1. **Cycle AO** (Codex Astra, `cod_3`): [`PATCH-AO.diff`](`.ntm/worktrees/xacml4j--batch2/agyx_1/PATCH-AO.diff`) (458 lines, 7 files), [`REPORT-I-AO-astra.md`](`.ntm/worktrees/xacml4j--batch2/agyx_1/REPORT-I-AO-astra.md`)
  2. **`I-DECLINE`** (Codex Terra, `cod_1`): [`PATCH-DECLINE.diff`](`.ntm/worktrees/xacml4j--batch2/agyx_1/PATCH-DECLINE.diff`) (149 lines, 4 files), [`REPORT-I-DECLINE-terra.md`](`.ntm/worktrees/xacml4j--batch2/agyx_1/REPORT-I-DECLINE-terra.md`)
- **Saved Report:** [`REPORT-R-AODEC-argon.md`](`.ntm/worktrees/xacml4j--batch2/agyx_1/REPORT-R-AODEC-argon.md`)
- **Verdicts:**
  - **Cycle AO:** **APPROVE** (0 P1, 0 P2, 0 P3)
  - **`I-DECLINE`:** **APPROVE** (0 P1, 0 P2, 0 P3)

---

## Part 1: Cycle AO — Bag Scans for Temporal Ordering Comparisons

### 1. `@XacmlFuncBagScan` on the Twelve Ordering Functions (`NonNumericComparisonFunctions`) & Per-Element Equivalence

Confirmed in [`NonNumericComparisonFunctions.java`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/NonNumericComparisonFunctions.java`) and [`BagStorages.java`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/BagStorages.java`) (`SUPPORTED`):

| Check | Verification in Source | Status |
| --- | --- | --- |
| **Twelve functions annotated** | `time-{greater-than,greater-than-or-equal,less-than,less-than-or-equal}` ([`NonNumericComparisonFunctions.java:76-116`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/NonNumericComparisonFunctions.java`)), `date-{greater-than,greater-than-or-equal,less-than,less-than-or-equal}` ([`NonNumericComparisonFunctions.java:142-182`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/NonNumericComparisonFunctions.java`)), and `dateTime-{greater-than,greater-than-or-equal,less-than,less-than-or-equal}` ([`NonNumericComparisonFunctions.java:184-224`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/NonNumericComparisonFunctions.java`)) carry the matching `@XacmlFuncBagScan(Comparison.{GREATER_THAN,GREATER_OR_EQUAL,LESS_THAN,LESS_OR_EQUAL})`. String comparisons (`:37-74`) and `timeInRange` (`:118-140`) are unannotated. | `SUPPORTED` |
| **Literal-first argument order** | In XACML `<Match>` evaluation, the policy literal is argument `a` and the bag element is argument `b`, evaluating `a.compareTo(b) > 0`, `>= 0`, `< 0`, or `<= 0`. [`BagStorages.compareTemporal`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/BagStorages.java`) evaluates `compare(comparison, literal.compareTo(type.cast(bag.valueAt(i))), 0)`, preserving `literal` as the left-hand receiver of `compareTo`. | `SUPPORTED` |
| **`date` and `dateTime` fixed UTC default** | [`ISO8601Date`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/types/ISO8601Date.java`) and [`ISO8601DateTime`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/types/ISO8601DateTime.java`) assign `ZoneOffset.UTC` when parsed without an explicit zone and compare via `compareTo` (`instant().compareTo(o.instant())` and `get().toInstant().compareTo(o.get().toInstant())`). Because `BagStorages.compareTemporal` calls `literal.compareTo(...)` directly, it uses the exact same comparison and fixed UTC default as the twelve function bodies. | `SUPPORTED` |
| **`time` requires a zone on both sides, else `NONE`** | [`DefaultFunctionInvocation.canScan`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java`) requires `literal instanceof ISO8601Time time && time.isTimeZoneDefined()` (so an unzoned literal does not even build a precheck plan), and [`BagStorages.compareTemporal`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/BagStorages.java`) checks both `time.isTimeZoneDefined()` on `literal` (`:392`) and `!(value instanceof ISO8601Time time && !time.isTimeZoneDefined())` across **all** elements of `bag` in a first pass (`:405-410`) before performing any comparison in the second pass (`:411-413`). A mixed bag whose first element has a zone and matches the literal but whose second element lacks a zone still returns `Scan.NONE`. | `SUPPORTED` |
| **Offsets, nanoseconds, duplicates** | `ISO8601Date.compareTo`, `ISO8601DateTime.compareTo`, and `ISO8601Time.compareTo` normalize offsets to UTC instants or UTC reference nanoseconds at full nanosecond precision. Duplicate values in `bag` are visited or short-circuited with identical boolean semantics. | `SUPPORTED` |
| **Unexpected value class or failure gives `NONE`** | The first pass in [`BagStorages.compareTemporal`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/BagStorages.java`) checks `if (value.getClass() != type ...) return BagStorage.Scan.NONE;` for every element before comparing, and both [`BagStorages.compareTemporal`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/BagStorages.java`) and [`DefaultFunctionInvocation.scan`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java`) catch `Throwable` and return `BagStorage.Scan.NONE`. | `SUPPORTED` |

### 2. `BagOfValues.scanTemporal`, `DefaultFunctionInvocation`, `BagStorage`, `BagStorages`, and `time-in-range`

- **[`BagOfValues.scanTemporal(BagStorage.Comparison, Value<?>)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java`):** Adds the single conductor-approved public forwarding method `return storage.compareTemporal(comparison, literal);`. `BagOfValues` has an explicit `serialVersionUID` (`-3544429211640153263L`), and no fields or serialization paths are altered.
- **[`BagStorage.compareTemporal`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/BagStorage.java`) & [`BagStorages.compareTemporal`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/BagStorages.java`):** Default interface method delegating to `BagStorages.compareTemporal`.
- **[`DefaultFunctionInvocation.canScan` & `scan`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java`):** Preserves `FunctionInvocationException` wrapping on the existing `scanIeee` / `EQUAL` / `IntegerVal` / `DoubleVal` try block (`:178-188`), and dispatches `ISO8601Date`, `ISO8601DateTime`, and `ISO8601Time` in a separate block (`:189-195`) that catches `Throwable` and returns `BagStorage.Scan.NONE`.
- **`time-in-range`:** [`NonNumericComparisonFunctions.timeInRange`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/NonNumericComparisonFunctions.java`) is untouched (and [`DefaultFunctionInvocation.bagScan`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java`) only accepts 2-parameter methods with `!evalContextRequired`).

### 3. `TemporalComparisonTargetPdpTest` (108 Cases)

[`TemporalComparisonTargetPdpTest.java`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/test/java/org/xacml4j/v30/pdp/TemporalComparisonTargetPdpTest.java`) covers 108 parameterized cases (verified green in `xacml-pdp`):
- **96 boundary cases (`boundaries`, `:76-114`):** 3 temporal types (`date`, `dateTime`, `time`) $\times$ 4 ordering functions $\times$ 8 bag shapes (singletons, pairs, and duplicates of `before`, `equal`, `after`, including absent zones on `date`/`dateTime`, `+02:00` offsets, and $\pm 1\text{ ns}$ boundaries around `12:00:00Z`). Each case pins the decision against an independent truth table (`matches[comparison][bag]`), asserts `result.getStatus().isSuccess()`, and asserts full `Result` + status error-chain equality across `ordinary` (delegating mock forcing `scan -> Scan.NONE`), `observed`, `unobserved`, and `indexed`.
- **12 timezone-mismatch cases (`timeZoneMismatches`, `:116-131`):** 4 `time-*` functions $\times$ 3 zone combinations (zoned literal vs. unzoned bag, unzoned literal vs. zoned bag, and zoned literal vs. `[11:00:00Z, 13:00:00]` mixed bag), asserting that `observed`, `unobserved`, and `indexed` match `ordinary`.

### 4. Request Lookup Allocations on the Merged Tree (with Cycle AM)

Confirmed in the merged worktree source (`SUPPORTED`):
In [`TargetBeforeContext.precheck`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/TargetBeforeContext.java`):
```java
Optional<Entity> entity = context.resolveEntity(key.getCategory());
if(entity.isEmpty()) return UNKNOWN;
Optional<BagOfValues> bag = entity.get().resolve(key);
```
1. **`context.resolveEntity(key.getCategory())`** calls [`RequestContext.getEntity(CategoryId)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/RequestContext.java`), which indexes directly into the `ImmutableList<Category>` (`((List<Category>)found).get(0)`) and returns `Category.optionalEntity()`, the pre-allocated `Optional<Entity>` cached on `Category` by Cycle AM. That eliminates the 37.53% `Optional` allocation from Astra's pre-AM profile.
2. **`entity.get().resolve(key)`** calls [`Entity.resolve(AttributeDesignatorKey)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/types/Entity.java`) $\to$ [`AttributeIndex.resolve(AttributeDesignatorKey)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/AttributeIndex.java`):
   - When the attribute is present on the request `Entity` (as in `rules:100 / date-ranges`), the first resolution stores `(key, bag)` in `AttributeIndex.bags`, and every subsequent precheck lookup returns `kept.get(key)` directly (`:353-356`) without allocating an `AttributeIndex.Lookup` record or a new `Optional`.
   - When the attribute id is absent from the request `Entity`, [`AttributeIndex.resolve(String, ValueType, String)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/AttributeIndex.java`) returns `Optional.empty()` on `!attributes.containsKey(attributeId)` before constructing `Lookup`.
   - That eliminates the 56.79% `AttributeIndex.Lookup` allocation from Astra's pre-AM profile.

---

## Part 2: `I-DECLINE` — No-Op Index Guards for Non-Narrowing Targets

### 1. Verification of the Guards

Confirmed in the merged worktree source (`SUPPORTED`):
1. **No-key and singleton constructor in [`TargetIndex.java:65-72`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/TargetIndex.java`):**
   - `private TargetIndex(List<T> children)` sets `this.children = children`, `this.key = Optional.empty()`, `this.anyChildren = children`, `this.positions = Map.of()`, and `this.singleValueCandidates = Map.of()`, avoiding `ConcurrentHashMap` and map-copy allocations when an index has no eligible key or fewer than two children.
   - Used by [`TargetIndex.create`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/TargetIndex.java`) when `children.size() < 2` and by [`TargetIndex.Builder.build`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/TargetIndex.java`) when `children.size() < 2 || key.isEmpty()`.
2. **`Policy` and `PolicySet` skip indexing below two children:**
   - [`Policy.Prepared.children`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/Policy.java`) guards `policy.rules.size() < 2` and returns `policy.rules` before accessing `generation.indexes`.
   - [`PolicySet.Prepared.children`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySet.java`) includes `set.decisionRules.size() > 1` in `indexable`. A 0- or 1-child `PolicySet` still builds and caches its `PreparedList` (so D24 `PinnedReference` pinning still takes place), skips `TargetIndex.Builder`, and returns `existing.children()` at `:254`.
3. **`candidates` guard `key.isEmpty()` ([`TargetIndex.java:87`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/TargetIndex.java`)):**
   - When a `Policy` or `PolicySet` has $\ge 2$ children but none has an indexable `string-equal` target (`key.isEmpty()`), `Builder.build` caches the lightweight `new TargetIndex<>(children)` in `generation.indexes` and `candidates(context)` returns `children` immediately on `if (key.isEmpty() || ...) return children;` without resolving any request entity or bag.
4. **Outcome equivalence (index on vs. off):**
   - For `key.isEmpty()`, `candidates(context)` returned `children` before and returns `children` now.
   - For `< 2` children, returning the singleton `[child]` instead of indexing it passes `child` to the combining algorithm, where `child`'s own target evaluation (or D41 `TargetBeforeContext`) produces `NotApplicable` whenever the `TargetIndex` would have filtered it out (since `TargetIndex` only indexes `mustBePresent = false`, built-in `string-equal` matches over request attributes, which cannot be `Indeterminate` when the request attribute is a `StringVal` and already returned `children` when the request attribute was absent).
5. **`TargetIndexPdpTest` additions ([`TargetIndexPdpTest.java:236-291`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/test/java/org/xacml4j/v30/pdp/TargetIndexPdpTest.java`)):**
   - `singlePolicyAndRuleKeepDecisionsAndResponses` (`:237-259`) and `nonIndexableRangeRulesKeepFirstApplicableResponses` (`:262-291`) pin exact decisions and obligations and verify full response equality (`assertResponse(expected, actual)`) between index-off and index-on (verified green in `xacml-pdp`).

---

## Findings (P1 to P3)

None.
