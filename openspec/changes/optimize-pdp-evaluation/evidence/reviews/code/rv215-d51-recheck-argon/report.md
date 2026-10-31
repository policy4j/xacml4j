# Code Review Report: `rv215-d51-recheck-argon` — Recheck of D51 (`rv210-ba-argon` P2-1, Task `42.18`)

- **Change**: `openspec/changes/optimize-pdp-evaluation`
- **Task**: `42.18` (Design `D51` built-in bag cardinality recheck)
- **Reviewer**: `agyx_1` (`policy4j_reviewer_argon`, Agy Gemini 4 Argon High)
- **Author**: `cod_3` (Codex Astra)
- **Candidate Worktree**: `${HOME}/Projects/Personal/xacml4j/.ntm/worktrees/xacml4j--remaining-g1-20261029/cod_3`
- **Author Evidence**: `openspec/changes/optimize-pdp-evaluation/evidence/tests/207-d51-fix-astra/summary.md` and `correction.patch`
- **Verdict for D51**: **APPROVE** (`0` P0, `0` P1, `0` P2, `0` P3)

---

## 1. Candidate SHA-256 Digest Verification (`SUPPORTED`)

All three D51 files in `${HOME}/Projects/Personal/xacml4j/.ntm/worktrees/xacml4j--remaining-g1-20261029/cod_3` were verified with `rtk shasum -a 256` and match the expected digests byte-for-byte:

| File | Expected & Actual SHA-256 (`cod_3`) | Status |
| --- | --- | --- |
| `xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java` | `ccded3b213e699cde8de2aa73bddbf38dd664c73353988c679f58ce966169534` | PASS |
| `xacml-core/src/main/java/org/xacml4j/v30/BagStorages.java` | `c1b94844320e905680244e9b46112f76c91c0a7c49d5513c23ab67d21b7823db` | PASS |
| `xacml-core/src/test/java/org/xacml4j/v30/BagStorageCompatibilityTest.java` | `b1aaea7094fa48d5a1033353960f7e034b875bf5271b7833034cf1f2bf79930a` | PASS |

---

## 2. Findings

None (`0` P0, `0` P1, `0` P2, `0` P3). Prior finding `rv210-ba-argon` `[P2-1]` is **RESOLVED**.

---

## 3. Question-by-Question Verification

### Question 1: Is P2-1 closed under unsafe publication? A racing reader may see any mix of default and written fields; can `size()`, `isEmpty()`, `single()` or `value()` now give a silently wrong answer where the old code would have thrown or been right? (`SUPPORTED`, confirmed closed)

- **Write order (`BagOfValues.java:83-87, 336-340`)**:
  - Constructor (`BagOfValues.java:83-87`):
    ```java
    private BagOfValues(BagOfValuesType type, BagStorage storage){
        this.type = type;
        this.cardinality = BagStorages.cardinality(storage);
        this.storage = storage;
    }
    ```
  - Deserialization (`BagOfValues.java:336-340`):
    ```java
    BagStorage readStorage = builder.build();
    this.type = readType;
    this.cardinality = BagStorages.cardinality(readStorage);
    this.storage = readStorage;
    this.hashCode = fields.get("hashCode", 0);
    ```
  - Both `cardinality` (`BagOfValues.java:74`) and `storage` (`BagOfValues.java:72`) are `private transient volatile`.
- **Exhaustive state analysis of `(cardinality, storage)` observed by a racing reader in `size()` (`BagOfValues.java:180-184`), `isEmpty()` (`:191-193`), `single()` (`:169-171`), and `value()` (`:162-167`)**:
  1. **Reader observes `cardinality == 0` (either the JVM default `0` before the volatile store to `cardinality` is visible, or a custom `BagStorage`)**:
     - `size()` (`BagOfValues.java:181-182`) executes `if (size == 0) return storage.size();`, which is identical to the pre-D51 implementation (`return storage.size();`).
     - If `storage` is still `null`, `storage.size()` throws `NullPointerException` (matching the pre-D51 code).
     - If `storage` is non-null, `storage.size()` reads the engine `BagStorage`'s `final` size fields (`Empty.size() == 0` at `BagStorages.java:172`, `Single.size() == 1` at `:193`, `Grouped.size` at `:211, 226`, `Booleans.trueCount + falseCount` at `:345-346, 367`) or delegates to the custom `BagStorage.size()`.
     - In `single()` (`BagOfValues.java:169-171`) and `value()` (`BagOfValues.java:162-167`), if `isEmpty()` returned `false` via `size == 0 -> storage.size() != 0`, this thread's volatile read of `this.storage` has already observed the non-null `BagStorage` reference; because `this.storage` is `volatile` and written only once (`null -> non-null`), the subsequent volatile read of `this.storage` in `valueAt(0)` (`BagOfValues.java:120-122`) is later in program order and synchronization order (JLS §17.4.4–17.4.5) and is guaranteed to observe the same non-null `BagStorage` (whose element fields are `final`).
  2. **Reader observes `cardinality == -1` (written only for engine `Empty`, `BagStorages.java:69`)**:
     - `size()` (`BagOfValues.java:181-183`) sees `size != 0` and `size > 0 == false`, returning `0` (the exact cardinality of the empty bag).
     - `isEmpty()` (`BagOfValues.java:191-193`) returns `true`.
     - `single()` (`BagOfValues.java:169-171`) returns `Optional.empty()`.
     - `value()` (`BagOfValues.java:162-167`) throws `NoSuchElementException`.
     - None of these can ever be wrong because `cardinality == -1` is written only when the bag is an engine `Empty` of actual size `0`.
  3. **Reader observes `cardinality > 0` (written only for non-empty engine `BagStorage` of actual size `cardinality >= 1`, `BagStorages.java:70-74`)**:
     - `size()` (`BagOfValues.java:181-183`) returns `size` (`> 0`), the exact cardinality of the bag.
     - `isEmpty()` (`BagOfValues.java:191-193`) returns `false`.
     - `single()` (`BagOfValues.java:169-171`) and `value()` (`BagOfValues.java:162-167`) see `isEmpty() == false` and call `valueAt(0)` (`BagOfValues.java:120-122`: `return (T) storage.valueAt(0);`). If `storage` is not yet visible (`null`), `storage.valueAt(0)` throws `NullPointerException` (matching the pre-D51 failure mode); if `storage` is visible (`non-null`), `storage.valueAt(0)` reads the `final` fields of `Single`/`Values`/`Integers`/`Doubles`/`Booleans` and returns the exact first `Value<?>`.

---

### Question 2: Can a storage of size 0 ever take a positive value, or a non-empty storage take -1? Check every engine class and the builder paths. (`SUPPORTED`, refuted)

- **Engine storage classes (`BagStorages.java:68-76, 164-393`)**:
  - All 6 engine storage classes (`Empty`, `Single`, `Values`, `Integers`, `Doubles`, `Booleans`) are `private static final` inside `final class BagStorages` (`BagStorages.java:40, 164, 188, 239, 258, 294, 344`) and are constructed exclusively by `BagStorages.Builder.build()` (`BagStorages.java:115-156`).
  - `BagStorages.Builder` (`BagStorages.java:83-157`):
    - `size` starts at `0` (`:90`) and is incremented only in `Builder.add(Value<?> value)` via `int nextSize = Math.addExact(size, 1); ... size = nextSize;` (`:97, 109`). `Math.addExact` throws `ArithmeticException` on integer overflow, so `Builder.size` is always in `[0, Integer.MAX_VALUE]`.
    - When `size == 0` (`BagStorages.java:116-118`), `build()` returns `Empty.of(kind)`. `BagStorages.cardinality(storage)` (`:69`) returns `-1`. No other path in `BagStorages` returns `Empty`, and `Empty.size()` is `0` (`:172`).
    - When `size == 1` (`BagStorages.java:119-121`), `build()` returns `new Single(first)`. `BagStorages.cardinality(storage)` (`:70`) returns `1` (`> 0`), matching `Single.size() == 1` (`:193`).
    - When `size >= 2` (`BagStorages.java:122-155`), `size` is in `[2, Integer.MAX_VALUE]`:
      - `Values` (`:148, 154, 239-256`), `Integers` (`:151, 258-292`), and `Doubles` (`:152, 294-342`) pass `size` to `Grouped(distinct, counts, distinctSize, size)` (`:215-216`), setting `final int size = size >= 2`. `BagStorages.cardinality(storage)` (`:71-73`) returns `values.size` / `integers.size` / `doubles.size`, which is strictly in `[2, Integer.MAX_VALUE]` (`> 0`, never `0` or `-1`).
      - `Booleans` (`:153, 344-393`) is built only when `specialized == true` (`:147`), meaning every added element satisfied `value instanceof BooleanVal` (`:52, 110`). Both grouping branches (`size <= 8` at `:125-135` and `size > 8` at `:136-146`) partition the `size` elements across at most 2 distinct `BooleanVal` keys (`TRUE` and `FALSE`) such that $\sum_{i=0}^{\text{distinctSize}-1} \text{counts}[i] = \text{size} \in [2, \text{Integer.MAX\_VALUE}]$. In `Booleans` (`:357-363`), `trueCount` and `falseCount` receive the disjoint counts for `true` and `false`, so `booleans.trueCount + booleans.falseCount == size >= 2` without overflow (`> 0`, never `0` or `-1`), matching `Booleans.size()` (`:367`).
  - **Custom `BagStorage` implementations**:
    - Cannot be `instanceof` any of the 6 `private static final` classes in `BagStorages`, so `BagStorages.cardinality(storage)` falls through to `return 0;` (`BagStorages.java:75`) regardless of whether the custom storage is empty or non-empty.
  - **`BagOfValues.Builder` (`BagOfValues.java:381-442`) and `BagOfValues.readObject` (`BagOfValues.java:327-341`)**:
    - Both build `BagStorage` exclusively via `type.getValueType().bagStorageBuilder()` (`BagOfValues.java:334-336, 393, 439-441`) and pass that `BagStorage` to `BagStorages.cardinality(...)` (`:85, 338`).
  - Consequently, no storage of size `0` can ever take a positive `cardinality`, and no non-empty storage can ever take `-1`.

---

### Question 3: Does anything else read `cardinality` or bypass `size()`? (`SUPPORTED`, confirmed none)

- **`rtk rg -n 'cardinality|BagStorages'` across `xacml-core/src`, `xacml-pdp/src`, `xacml-xml/src`, and `xacml-json/src` in `cod_3`**:
  - `BagOfValues.cardinality` (`BagOfValues.java:74`) is written only in `BagOfValues(BagOfValuesType, BagStorage)` (`:85`) and `BagOfValues.readObject(ObjectInputStream)` (`:338`), and read in production code only in `BagOfValues.size()` (`:181`). (In tests, `BagStorageCompatibilityTest.resetCardinality` accesses it reflectively at `BagStorageCompatibilityTest.java:179-183`.)
  - `BagStorages.cardinality(BagStorage)` (`BagStorages.java:68`) is called only at `BagOfValues.java:85` and `BagOfValues.java:338` (also confirmed by `ripwire .../cod_3 --exclude=/evidence/ --edit-check=xacml-core/src/main/java/org/xacml4j/v30/BagStorages.java:cardinality`).
  - Inside `BagOfValues.java`, `storage.size()` is called only at `BagOfValues.java:182` inside `size()`. Every other cardinality consumer in `BagOfValues` calls `size()` or `isEmpty()` (`spliterator()` at `:115`, `value()` at `:163`, `single()` at `:170`, `isEmpty()` at `:192`, `count(Value<?>)` at `:206`, `containsAll(BagOfValues)` at `:277`, `equals(Object)` at `:292`).

---

## 4. Verification Run & Residual Risk

### Verification Run by Reviewer
1. **SHA-256 Digest Check (`SUPPORTED`)**: Verified all 3 files in `cod_3` via `rtk shasum -a 256`.
2. **Static Analysis (`SUPPORTED`)**: `ast-grep scan` on `BagOfValues.java`, `BagStorages.java`, and `BagStorageCompatibilityTest.java` in `cod_3` returned `0` findings (`exit 0`).
3. **Call-Graph & Reference Search (`SUPPORTED` / `PARTIAL`)**:
   - `rtk rg -n 'cardinality|BagStorages'` across `xacml-core/src`, `xacml-pdp/src`, `xacml-xml/src`, and `xacml-json/src` in `cod_3`.
   - `ripwire .../cod_3 --exclude=/evidence/ --edit-check=xacml-core/src/main/java/org/xacml4j/v30/BagStorages.java:cardinality` and `--edit-check=xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java:size` (`status="unchanged"`, `incompatible="0"`).
4. **Supplied Test Gate (Not Rerun by Reviewer)**:
   - Author-supplied results in `openspec/changes/optimize-pdp-evaluation/evidence/tests/207-d51-fix-astra/summary.md`: `xacml-core` 7,504 tests (`0` failures, `1` error — the known `ISO8601DateTest` month defect), `xacml-pdp` 810, `xacml-xml` 254, `xacml-json` 24, `0` failures (`154` focused tests in `BagStorageCompatibilityTest` passing green after `57` expected failures on red).

### Residual Risk
- **JMH Gate (`UNRUN`)**: Controlled same-JDK JMH pairs for `D49`, `D50`, and `D51` remain to be executed by the benchmark lane to decide whether `D51`'s avoided interface dispatch outweighs its volatile read/branch on the hot path.
