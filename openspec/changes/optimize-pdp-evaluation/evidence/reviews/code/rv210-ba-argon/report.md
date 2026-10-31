# Code Review Report: `rv210-ba-argon` — Cycle BA (`D49`, `D50`, `D51`, Task `42.18`)

- **Change**: `openspec/changes/optimize-pdp-evaluation`
- **Task**: `42.18` (Cycle BA: `D49` long-string hash rejection, `D50` small-request category scan, `D51` built-in bag cardinality)
- **Reviewer**: `agyx_1` (Agy Gemini 4 Argon High, independent adversarial reviewer)
- **Author**: `cod_3` (Codex Astra)
- **Candidate Worktree**: `${HOME}/Projects/Personal/xacml4j/.ntm/worktrees/xacml4j--batch2/cod_3` (base `8b494a3629601dbca4cdbe668902dc229216e6fc`)
- **Reviewer Baseline**: `${HOME}/Projects/Personal/xacml4j/.ntm/worktrees/xacml4j--remaining-g1-20261029/agyx_1` (base `759d90a6c75510d55127d8e22870f9edd985c991`; zero diff between `8b494a36` and `759d90a6` across all 7 candidate files)
- **Item Verdicts**:
  - **D49 (`StringVal.java`, `StringValTest.java`)**: **APPROVE** (`0` P1, `0` P2, `0` P3)
  - **D50 (`RequestContext.java`, `RequestFixedCostCharacterizationTest.java`)**: **APPROVE** (`0` P1, `0` P2, `0` P3)
  - **D51 (`BagOfValues.java`, `BagStorages.java`, `BagStorageCompatibilityTest.java`)**: **REJECT** (`0` P1, `1` P2, `0` P3)

---

## 1. Candidate File Digest Verification (`SUPPORTED`)

All 7 candidate files in `${HOME}/Projects/Personal/xacml4j/.ntm/worktrees/xacml4j--batch2/cod_3` were verified with `rtk proxy shasum -a 256` and match the required hashes byte-for-byte:

| File | Baseline (`8b494a36` / `759d90a6`) SHA-256 | Candidate (`cod_3`) SHA-256 | Match |
| --- | --- | --- | --- |
| `xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java` | `b61dd02978b338f456a026d4bfd5427d9a1a7a874b13fa79d23fb2a8320ff5c5` | `fd37281c99b37e5f44a2e619d5ae9d1c2e12aeb60ae46fec826c2cc1b8f8e4f7` | PASS |
| `xacml-core/src/main/java/org/xacml4j/v30/BagStorages.java` | `48fe7add0ada8c0be739ac225db347b652339f9ecd01d9f831c333ca28e871b4` | `f1cb60a127114b15dcede1c6c26d1733d4f59abddbfee1f77b65a9a0cfa530f2` | PASS |
| `xacml-core/src/main/java/org/xacml4j/v30/RequestContext.java` | `28ba15e28f1f7af6232cb870164576cdbb5911551de9bb5cc2a8f42514ef9f1d` | `a5d8d64da447f1e8b5c53d20afaeba26a54b9519bc62260414c744475c970800` | PASS |
| `xacml-core/src/main/java/org/xacml4j/v30/types/StringVal.java` | `cf647acd85d425954929cc6cb3326bffbb821406f194d092ddc70d469613168f` | `6d415e5aced3aa47cf27e1c7d1a5231dd40c0c9cb10abc64b0039ffe4612bc9d` | PASS |
| `xacml-core/src/test/java/org/xacml4j/v30/BagStorageCompatibilityTest.java` | `4d518c2e48e767b5db84eb045bd50d608e9a93dc6225929a2eb1f3b2191c91dc` | `f659f3d2fa4be69f8439166c0da6c518a3d60b20fe71aa81b0066c681b4d8e3a` | PASS |
| `xacml-core/src/test/java/org/xacml4j/v30/RequestFixedCostCharacterizationTest.java` | `15cb5c2459a75f05ff4114c22e35de6426be9ba45c9560df7bf0ec441de439bf` | `b9a91186f5fcf7b18d95a35efe807e49b0ba73165202235e9213dbc4c69dbb36` | PASS |
| `xacml-core/src/test/java/org/xacml4j/v30/types/StringValTest.java` | `6e004f8b75e1c3f8ab7b55cc7e2ab8a4a6e3e948c6fa1be1a0eb6c9979e5e5d8` | `3ce7939b4879d37093ba9c2d2903a9ec17922486b81612e3992993cca60dbd89` | PASS |

---

## 2. Findings (Severity First)

### [P2-1] D51 (`BagOfValues.java:74, 83-87, 180-192, 326-339`, `BagStorages.java:65-73`): Default `0` of `transient volatile int cardinality` Collides with Empty-Bag Cardinality `0`, Returning `size() == 0` / `isEmpty() == true` on a Non-Empty Bag Under Unsafe Publication

- **Severity**: P2 (Java memory model / publication-safety regression on `BagOfValues` relative to `storage.size()`)
- **Location**:
  - `xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java:74, 83-87, 180-192, 326-339`
  - `xacml-core/src/main/java/org/xacml4j/v30/BagStorages.java:65-73`
- **Evidence (`SUPPORTED`, source inspection)**:
  - `BagOfValues` declares:
    ```java
    private transient volatile BagOfValuesType type;
    private transient volatile BagStorage storage;
    private transient volatile Multiset<Value<?>> values;
    private transient volatile int cardinality;
    ```
    (`BagOfValues.java:71-74`), initializes `cardinality` in the constructor (`BagOfValues.java:83-87`):
    ```java
    private BagOfValues(BagOfValuesType type, BagStorage storage){
        this.type = type;
        this.storage = storage;
        this.cardinality = BagStorages.cardinality(storage);
    }
    ```
    and in `readObject` (`BagOfValues.java:335-338`), and reads it in `size()` (`BagOfValues.java:180-183`):
    ```java
    public int size() {
        int size = cardinality;
        return size >= 0 ? size : storage.size();
    }
    ```
  - Because `type`, `storage`, and `cardinality` are `transient volatile` rather than `final` (so `readObject` can assign them directly while keeping `serialPersistentFields`), JLS §17.5 `final`-field constructor-freeze guarantees do **not** apply to `BagOfValues`.
  - Under the Java Memory Model (JLS §17.4.4–17.4.5), a `volatile` store in a constructor has release semantics only for *preceding* program-order operations, not for a *subsequent* plain write that publishes the `BagOfValues` reference (`sharedBag = bag`). Moreover, in both the constructor (`:85-86`) and `readObject` (`:336-337`), `this.storage = storage` is written **before** `BagStorages.cardinality(storage)` is evaluated and written to `this.cardinality`.
  - **Old behavior**: `size()` executed `return storage.size();` (`BagOfValues.java:179` at `8b494a36`). If a non-empty `BagOfValues` was observed via unsafe publication:
    - Before `this.storage` was visible (`storage == null`), `bag.size()` threw `NullPointerException`.
    - Once `this.storage` was visible (`storage != null`), `storage.size()` read the engine `BagStorage`'s `final` fields (`Grouped.size` at `BagStorages.java:208, 223`, `Booleans.trueCount`/`falseCount` at `BagStorages.java:342-343, 364`, or constant `1` in `Single.size()` at `BagStorages.java:190`), which **do** carry JLS §17.5 `final`-field freeze guarantees. Thus the old `bag.size()` **never** returned `0` for a non-empty engine bag.
  - **Candidate behavior / failure scenario**: Because `BagStorages.cardinality(storage)` encodes `Empty` as `0` and custom storage as `-1` (`BagStorages.java:66, 72`), and `BagOfValues.size()` treats `size >= 0` as a valid cached cardinality (`BagOfValues.java:182`), the JVM's zero-initialized default value `cardinality == 0` is indistinguishable from an initialized empty bag. When a thread publishes a newly built or deserialized non-empty `BagOfValues` via a non-volatile field/array element and another thread calls `bag.size()`, `bag.isEmpty()`, `bag.single()`, or `bag.value()` before `this.cardinality` becomes visible (including when `this.storage` is already visible!), `bag.size()` reads `cardinality == 0` and silently returns `0` (`isEmpty() == true`, `single() == Optional.empty()`, `value()` throws `NoSuchElementException`, `equals()` returns `false`) instead of reading `storage.size()`.
- **Minimal Fix**:
  - Reserve `0` (the JVM default value of `int cardinality`) as the "uninitialized or custom storage — delegate to `storage.size()`" sentinel, and use `-1` for `Empty`:
    - In `BagStorages.cardinality(BagStorage storage)` (`BagStorages.java:65-73`):
      - Return `-1` for `Empty` (`if (storage instanceof Empty) return -1;`).
      - Return `1` for `Single`, `values.size` for `Values`, `integers.size` for `Integers`, `doubles.size` for `Doubles`, and `booleans.trueCount + booleans.falseCount` for `Booleans` (all strictly positive `>= 1`, because `BagStorages.Builder.build()` at `BagStorages.java:113-152` builds `Empty` for `0`, `Single` for `1`, and `Values`/`Integers`/`Doubles`/`Booleans` only for `size >= 2`).
      - Return `0` for custom storage (`return 0;`).
    - In `BagOfValues.size()` (`BagOfValues.java:180-183`):
      ```java
      public int size() {
          int size = cardinality;
          if (size != 0) {
              return size > 0 ? size : 0;
          }
          return storage.size();
      }
      ```
      (and write `this.cardinality = BagStorages.cardinality(storage);` before `this.storage = storage;` in the constructor and `readObject`).
    - With this encoding, default `0` always falls back to `storage.size()`, non-empty engine bags (`size > 0`) return on the first branch without interface dispatch, empty engine bags (`size == -1`) return `0` without interface dispatch, and custom storages (`size == 0`) delegate to `storage.size()`.
- **Regression Test**:
  - In `BagStorageCompatibilityTest`: verify that a non-empty `BagOfValues` whose `cardinality` field is at its default `0` (e.g., reset to `0` via reflection in a white-box unit test simulating an uninitialized read of `cardinality` after `storage` is set, or testing `BagStorages.cardinality` sentinel values directly) still returns `storage.size()` (`> 0`) and `isEmpty() == false` rather than `0` / `true`, while `Empty` returns `0` / `true` and `CustomType.UNKNOWN_SIZE` still defers `size()` until called.

---

## 3. Hypothesis-by-Hypothesis Verification

### Question 1: D51 (`BagOfValues.java`, `BagStorages.java`)

1. **Is every way to obtain a `BagOfValues` covered: each constructor, `readObject`, `readResolve`, `clone`, and any reflective or unsafe path? (`SUPPORTED`, confirmed)**
   - `BagOfValues` is `public final class BagOfValues` (`BagOfValues.java:61`), so no subclass exists.
   - **Constructors**: There is a single constructor, `private BagOfValues(BagOfValuesType type, BagStorage storage)` (`BagOfValues.java:83-87`), invoked only by `BagOfValues.Builder.build()` (`BagOfValues.java:438`). It sets `this.cardinality = BagStorages.cardinality(storage)` at `:86`.
   - **`readObject` / `readResolve` / `readObjectNoData`**: `readObject(ObjectInputStream)` (`BagOfValues.java:326-339`) rebuilds `this.storage` and sets `this.cardinality = BagStorages.cardinality(storage)` at `:337`. Neither `readResolve` nor `writeReplace` exists. Because `BagOfValues` is `final` and its only superclass is `Object` (not `Serializable`), `readObjectNoData` can never be invoked on a `BagOfValues` instance (Java Object Serialization Specification §3.5).
   - **`clone`**: `BagOfValues` does not implement `Cloneable` (`BagOfValues.java:61-62`) and does not override `Object.clone()`.
   - **Reflective or unsafe paths**: `rtk rg` across `xacml-core`, `xacml-pdp`, `xacml-xml`, and `xacml-json` confirms no reflective constructor calls, `MethodHandles`, `Unsafe.allocateInstance`, or Gson/JAXB direct field deserialization into `BagOfValues`.
2. **Can any reader see the default `0` on a non-empty bag, including by unsafe publication, where the old code would have read the storage? (`SUPPORTED`, confirmed defect — see `[P2-1]`)**
   - Under safe publication or on the constructing/deserializing thread, no reader can see `0` on a non-empty bag.
   - Under **unsafe publication**, a reader can observe the default `0` of `transient volatile int cardinality` (because `volatile` stores in a constructor lack JLS §17.5 constructor-exit freeze barriers against a subsequent plain reference publish, and `this.storage` is assigned at `:85`/`:336` before `this.cardinality` at `:86`/`:337`). Because `size()` (`:181-182`) checks `size >= 0 ? size : storage.size()`, seeing default `0` causes `size()` to return `0` and `isEmpty()` to return `true` on a non-empty bag, whereas the old code (`return storage.size();`) read `storage` (whose size is stored in `final` fields) and never returned `0` on a non-empty bag. See `[P2-1]` above.
3. **Do the storage classes have a fixed size, and does `cardinality(...)` match `storage.size()` for every engine class, including `Booleans` with duplicates and a custom `BagStorage` returning `-1`? (`SUPPORTED`, hypothesis of mismatch refuted)**
   - All 6 engine storage classes in `BagStorages` (`BagStorages.java:161-390`) are `private static final` and immutable after construction:
     - `Empty` (`:161-177`): `size()` returns `0` (`:169`); `cardinality(storage)` returns `0` (`:66`).
     - `Single` (`:185-205`): `size()` returns `1` (`:190`); `cardinality(storage)` returns `1` (`:67`).
     - `Values` (`:236-253`), `Integers` (`:255-289`), `Doubles` (`:291-339`): extend `Grouped` (`:207-234`), where `final int size` (`:208`) is returned by `Grouped.size()` (`:223`); `cardinality(storage)` returns `values.size` / `integers.size` / `doubles.size` (`:68-70`).
     - `Booleans` (`:341-390`): `BagStorages.Builder.build()` (`:122-143`) groups equal `BooleanVal` instances into `distinct` (`distinctSize <= 2`) and accumulates all duplicate occurrences into `counts[i]` (via `counts[index]++` at `:130` for `size <= 8` or `grouped.merge(values[i], 1, Math::addExact)` at `:134` for `size > 8`). `Booleans` stores `final int trueCount` and `final int falseCount` (`:342-343, 355-360`) and `Booleans.size()` returns `trueCount + falseCount` (`:364`), which matches `booleans.trueCount + booleans.falseCount` in `BagStorages.cardinality(storage)` (`:71`).
     - Custom `BagStorage` returning `-1` from `storage.size()`: `BagStorages.cardinality(storage)` returns `-1` (`:72`) without invoking `storage.size()`. In `BagOfValues.size()` (`:181-182`), `size >= 0` is `false` for `-1`, so `BagOfValues.size()` delegates to `storage.size()`, which returns `-1`.
4. **Does the new field change the serialized form? (`SUPPORTED`, hypothesis refuted)**
   - `cardinality` is `private transient volatile int` (`BagOfValues.java:74`), `serialVersionUID = -8197446176793438616L` (`:64`) is unchanged, and `serialPersistentFields` (`:65-69`) explicitly restricts the stream fields to `hashCode`, `type`, and `values`. `writeObject` (`:317-323`) and `readObject` (`:326-339`) read and write only those three fields.

---

### Question 2: D49 (`StringVal.java`, `StringValTest.java`)

1. **Is the hash test sound for every equal pair? (`SUPPORTED`, hypothesis of unsoundness refuted)**
   - `StringVal.equals(Object other)` (`StringVal.java:74-84`):
     ```java
     if(other == this){
         return true;
     }
     if(other instanceof StringVal string && value.length() >= 32
             && value.length() == string.value.length()
             && value.hashCode() != string.value.hashCode()){
         return false;
     }
     return super.equals(other);
     ```
   - Both `this.value` and `string.value` are non-null `java.lang.String` instances (`Value.OtherValue` constructor enforces `Objects.requireNonNull(attrValue)` at `Value.java:113`, and `StringVal` is `final` at `StringVal.java:36`).
   - For any two `java.lang.String` objects, `s1.equals(s2)` implies `s1.hashCode() == s2.hashCode()`. Therefore `value.hashCode() != string.value.hashCode()` implies `!value.equals(string.value)`, so `super.equals(other)` (`Value.OtherValue.equals`, `Value.java:133-143`) would also return `false`. Equal hashes never return `true` early; they fall through to `super.equals(other)`.
2. **Does `equals` stay symmetric and consistent with `hashCode` against every other `OtherValue` and any subclass, and does the type check run where it ran before? (`SUPPORTED`, hypothesis of asymmetry/missing type check refuted)**
   - `StringVal` is `final` (`StringVal.java:36`), so it has no subclasses.
   - When `other` is any `Value.OtherValue<?>` that is not a `StringVal` (e.g., an external `new Value.OtherValue<String>(XacmlTypes.STRING, ...)` or `new Value.OtherValue<String>(XacmlTypes.ANYURI, ...)` or any other `OtherValue` subclass), `other instanceof StringVal string` is `false`, and `StringVal.equals(other)` delegates directly to `super.equals(other)` (`Value.OtherValue.equals`, `Value.java:133-143`), which runs `getEvaluatesTo().equals(e.getEvaluatesTo()) && value.equals(e.get())`.
   - When `other` is another `StringVal`, both `this.getEvaluatesTo()` and `string.getEvaluatesTo()` return `XacmlTypes.STRING` (`StringVal.java:41`, `Value.java:153, 157-159`), where `XacmlTypes.STRING.equals(XacmlTypes.STRING)` is `true` without side effects.
   - `StringVal` inherits `OtherValue.hashCode()` (`Value.java:128-130`: `31 * (31 + Objects.hashCode(value)) + Objects.hashCode(getEvaluatesTo())`), which remains completely consistent with `equals`.
3. **Does a `StringVal` built through another path hold the same `value` form? (`SUPPORTED`, hypothesis of divergent representation refuted)**
   - Every factory and method on `StringVal` (`of`, `ofAny`, `concat`, `trim`, `toLowerCase`, `toUpperCase` at `StringVal.java:52-123`) constructs `new StringVal(String)` with a standard `java.lang.String`. In Java (`java.lang.String`), `length()`, `hashCode()`, and `equals()` are invariant across internal coder representations (`LATIN1` vs `UTF16`) and construction paths (`new String(char[])`, substring, concatenation, deserialization).

---

### Question 3: D50 (`RequestContext.java`, `RequestFixedCostCharacterizationTest.java`)

1. **Does the scan keep the old result: first category selection, repeated categories, equal non-identical identifiers, a category with no entity, and a request of exactly 4 and exactly 5 categories? (`SUPPORTED`, hypothesis of behavior drift refuted)**
   - In `RequestContext(Builder b)` (`RequestContext.java:82-83, 526-538`), `this.entities = b.attrBuilder.build()` (`ImmutableListMultimap<CategoryId, Category>`) stores each `Category` `a` under key `a.getCategoryId()`, and `this.categories = ImmutableList.copyOf(entities.values())`.
   - In `ImmutableListMultimap`, `entities.get(category)` contains the `Category` entries whose `getCategoryId()` equals `category` in the exact order they were added to `attrBuilder`, and `entities.values()` preserves that same relative insertion order for every `CategoryId`.
   - In `RequestContext.getEntity(CategoryId category)` (`RequestContext.java:284-302`):
     ```java
     Preconditions.checkNotNull(category);
     if(categories.size() <= 4){
         for(int i = 0; i < categories.size(); i++){
             Category found = categories.get(i);
             CategoryId id = found.getCategoryId();
             if(id == category || category.equals(id)){
                 return found.optionalEntity();
             }
         }
         return Optional.empty();
     }
     ```
   - `CategoryId` is `final` (`CategoryId.java:41`) and inherits `SemanticIdentifier.equals` (`SemanticIdentifier.java:80-91`), which is reflexive and symmetric. Thus `id == category || category.equals(id)` matches iff `entities.get(category)` contains `found`, including for equal non-identical `CategoryId` instances (`CategoryId.parse(new String(...))`).
   - Because `categories` preserves the insertion order of `Category` entries for any given `CategoryId`, the first matching `Category` in `categories` is always identical to `((List<Category>) entities.get(category)).get(0)`, both when `categories.size() <= 4` (including `0, 1, 2, 3, 4` total categories, with or without repeated categories) and when `categories.size() >= 5` (including `5`).
   - A `Category` cannot have a `null` `Entity`: `Category(Builder)` enforces `Objects.requireNonNull(b.entity)` (`Category.java:70-71`), and `RequestContext(Builder)` calls `attr.getEntity().hasValues(...)` on every category (`RequestContext.java:95-96`). An empty category holds a non-null empty `Entity` and `found.optionalEntity()` returns `Optional.of(entity)` on both paths; an absent `CategoryId` returns `Optional.empty()` on both paths (`:294, 298`).
2. **Is the null check's failure the same exception as before? (`SUPPORTED`, confirmed)**
   - Previously, `getEntity(null)` called `getCategory(null)` (`RequestContext.java:251-254`), whose first statement is `Preconditions.checkNotNull(categoryId);` (throwing `NullPointerException` with `null` message).
   - Now, `getEntity(null)` executes `Preconditions.checkNotNull(category);` on its first line (`RequestContext.java:285`), throwing the identical `NullPointerException` with `null` message.
3. **Is `categories` always a random-access list at this point? (`SUPPORTED`, confirmed)**
   - `categories` is declared `private final ImmutableList<Category> categories;` (`RequestContext.java:64`) and assigned `ImmutableList.copyOf(entities.values())` in `RequestContext(Builder)` (`:83`). Guava's `ImmutableList` implements `java.util.RandomAccess` with $O(1)$ indexed `get(i)` and `size()`.

---

### Question 4: Test Coverage Assessment (`SUPPORTED`)

- **D49 (`StringValTest.java:46-85`)**: Pins 40-character hash collisions (`"Aa".repeat(20)` vs `"BB".repeat(20)`), equal distinct `String` instances at lengths `31`, `32`, `64`, non-ASCII Latin-1 (`\u00e9`), BMP CJK (`\u4e2d`), and supplementary surrogate pairs (`\ud83d\ude00`), first/last/length inequalities, and symmetric equality/inequality against external `Value.OtherValue<String>` instances (`XacmlTypes.STRING` and `XacmlTypes.ANYURI`). No concrete test gap.
- **D50 (`RequestFixedCostCharacterizationTest.java:70-100`)**: Pins `count = 0..6` categories (spanning both sides of the `4`/`5` boundary), equal non-identical `CategoryId` instances (`assertThat(equal).isNotSameInstanceAs(entry.getCategoryId())`), identical `CategoryId` instances, absent `CategoryId`, `getEntity(null)` (`NullPointerException`), and duplicate categories selecting the first `Category`'s `Entity`. No concrete test gap.
- **D51 (`BagStorageCompatibilityTest.java:141-148`)**: Pins lazy `size()` evaluation on `CustomType.UNKNOWN_SIZE` (`customStorageCardinalityIsRequestedOnlyWhenUsed`) alongside existing parameterized cardinality/hash/serialization compatibility tests (`:90-139, 150-260`). Needs a regression test accompanying the `[P2-1]` fix for the default `0` sentinel.

---

## 4. Verification Run & Unverified Gates

### Verification Run by Reviewer
1. **Candidate SHA-256 Digest Check (`SUPPORTED`)**: `rtk proxy shasum -a 256` on all 7 files in `cod_3`, plus `rtk git -C .../cod_3 diff 8b494a36 759d90a6` confirming zero baseline drift.
2. **`ast-grep scan` (`SUPPORTED`)**: Ran `ast-grep scan` on all 7 candidate files in `cod_3`; `0` findings (`exit 0`).
3. **`ripwire` Callers / Contract / Quality Checks (`PARTIAL` — name-based call graph)**:
   - `ripwire .../cod_3 --exclude=/evidence/ --edit-check=xacml-core/src/main/java/org/xacml4j/v30/BagStorages.java:cardinality`: `status="new-symbol"`, 2 real callers in `BagOfValues.java` (`:83`, `:326`); the 1 flagged `incompatible="1"` row (`TargetIndex.java:148`) is a name-based false positive calling `java.util.BitSet.cardinality()`.
   - `ripwire .../cod_3 --exclude=/evidence/ --edit-check=xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java:size`: `status="unchanged"`, `incompatible="0"`.
   - `ripwire .../cod_3 --exclude=/evidence/ --edit-check=xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java:isEmpty`: `status="unchanged"`, `incompatible="0"`.
   - `ripwire .../cod_3 --exclude=/evidence/ --edit-check=xacml-core/src/main/java/org/xacml4j/v30/RequestContext.java:getEntity`: `status="unchanged"`; the 5 flagged `incompatible="1"` rows inside `RequestContext.java` (`:95, 259, 335, 371, 395`) are name-based false positives calling 0-arg `Category.getEntity()`.
   - `ripwire .../cod_3 --exclude=/evidence/ --edit-check=xacml-core/src/main/java/org/xacml4j/v30/types/StringVal.java:equals`: `status="new-symbol"`, `incompatible="0"`.
   - `ripwire .../cod_3 --exclude=/evidence/ --quality-delta`: only `short-horizon-churn`, minor `verbosity` on touched files, and `dead-code` false positives on the 5 new JUnit `@Test` methods; no complexity, nesting, parameter, or duplication regressions.

### Unverified / Unrun Gates
- **No builds or tests were executed by this reviewer** (per read-only review rules; author-recorded test logs in `cod_3/openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/ba-astra/summary.md` report 110 characterization tests, 133 focused tests, and 10,518 reactor tests with 0 failures/errors and 36 skipped).
- **Controlled JMH pairs for D49, D50, and D51 are UNRUN** (`ba-astra/summary.md:4, 61-65`). Per maintainer decision (`design.md:1994`), each of D49, D50, and D51 is kept only if its independent controlled same-JDK JMH pair wins.
- **Serena (`UNAVAILABLE` for `cod_3`)**: Serena is bound to `agyx_1` (`port 9261`), not `cod_3`; all type and reference checks for `cod_3` were confirmed directly in source and with `rtk rg` + `ripwire`.
