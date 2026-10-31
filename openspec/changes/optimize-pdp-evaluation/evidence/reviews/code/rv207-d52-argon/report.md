# Independent Adversarial Code Review: Corrected D52 Candidate (`rv207-d52-argon`)

- **Change**: `optimize-pdp-evaluation`
- **Design**: D52 — Prepared signatures for higher-order call sites (`openspec/changes/optimize-pdp-evaluation/design.md:2059-2090`)
- **Task / Bead**: Task `42.19`, Bead `policy4j-cwtu.42.19.3`
- **Reviewer**: `agyx_1`, Agy Gemini 4 Argon High (independent adversarial code reviewer; plain review packet)
- **Author**: Codex GPT-6 Astra (`cod_3`, actor `ntm-astra-g1`)
- **Candidate Worktree**: `.ntm/worktrees/xacml4j--remaining-g1-20261029/cod_3` (base `759d90a6c75510d55127d8e22870f9edd985c991` on `feature_xacml_20`)
- **Prior Rejections Addressed**:
  - `openspec/changes/optimize-pdp-evaluation/evidence/reviews/code/rv201-resume-d52-astra/report.md` (`P2 / D52-ASTRA-1`)
  - `openspec/changes/optimize-pdp-evaluation/evidence/reviews/code/rv204-resume-d52-argon-recheck/report.md` (`P2-1`)
- **Verdict**: **APPROVE** (`0` P1, `0` P2, `0` P3). Source and test slice approved; full reactor/conformance after the unrelated `ISO8601DateTest` calendar fix and the controlled JMH/profile gate remain **UNRUN**.

---

## 1. Candidate SHA-256 Verification and Tool Health (`SUPPORTED`)

### 1.1 Candidate File SHA-256 Verification (`W/cod_3`)

All 9 candidate files in `.ntm/worktrees/xacml4j--remaining-g1-20261029/cod_3` were verified via `shasum -a 256` before review and match the expected hashes:

| Repository-Relative Path in `W/cod_3` | SHA-256 | Match |
| --- | --- | --- |
| `xacml-core/src/main/java/org/xacml4j/v30/policy/Apply.java` | `222342ac35a484e587360eb473290e1547c6001b46b554e0b14586d484fa074e` | `PASS` |
| `xacml-core/src/main/java/org/xacml4j/v30/policy/HigherOrderCall.java` | `c2c7ef8049ebf6d4974b2e6722fe27e453109a8dc74e10f2190004c7ca7464a6` | `PASS` |
| `xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java` | `bda8302758388ee43f6746c04b3b33dbd80174e11f11a0f7fe852c3921b04a12` | `PASS` |
| `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java` | `cadc93aa1855de78607c7e8351137fe2715276caba33dd1a43c1ea0855a993e2` | `PASS` |
| `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/HigherOrderFunctions.java` | `238dafcd13ce7b8ad502b39e29e93695f2e565d8435a596687e407b2bff84169` | `PASS` |
| `xacml-core/src/test/java/org/xacml4j/v30/policy/ApplyArgumentValidationTest.java` | `c56f68aced2414252fe4a5f4068fd3a9738057f2e16b140a8f1f2df4311a2ddc` | `PASS` |
| `xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/LaterDecidingOperandTest.java` | `d5ea6a7f12a77ff95325b42bfc7872aa421a815b44f0a636859621090a2fbf09` | `PASS` |
| `xacml-core/src/test/java/org/xacml4j/v30/policy/function/HigherOrderMetadataCompatibilityTest.java` | `cece1eef058621f738dac19b36e991589b66a0eeae50598a52743e2bc1aaaf39` | `PASS` |
| `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/SharedConditionValuesPdpTest.java` | `d3272b0cb31f964e1e67ebc5c1a460ec41a91e43811061682d43523df70fb2dc` | `PASS` |

### 1.2 Tool Health and Coverage Labels

- `ast-grep`: `PASS` (`SUPPORTED`; `ast-grep scan` on the 5 production files in `W/cod_3` returned 0 findings).
- `ripwire`: `PASS` (`SUPPORTED` when confirmed in opened source; `ripwire W/cod_3 --exclude=/evidence/ --edit-check=...` reported `incompatible="0"` across all D52 methods).
- `serena`: `UNAVAILABLE` for `W/cod_3` (`scripts/agent-doctor.sh` passed on `W/agyx_1`, but Serena serves the reviewer checkout `W/agyx_1`, not `W/cod_3`; compensated by direct source reading in `W/cod_3`).
- `tgrep`: `UNAVAILABLE` (no trigram index in this worktree; compensated by `rtk rg` and direct `read_file`).

---

## 2. Findings

None (`0` P1, `0` P2, `0` P3).

---

## 3. Verification of Focus Hypotheses (`SUPPORTED`)

### 3.1 Question 1 — Completeness of the Immutability Proof and Branch Ordering in `HigherOrderCall`

**Hypothesis tested**: Can a descriptor that the cached proof in `HigherOrderCall` depends on still be subclassed, retain a caller-owned object, or change what `isValidParamType` answers, or can the branch order in `HigherOrderCall.prepare` mishandle the first bag, first scalar, a second bag, or a bag with a custom element type?

**Result**: **Refuted by source** (`SUPPORTED`).

1. **Every descriptor on which the cached proof depends is final and closed over `XacmlTypes` enum constants**:
   - **Operator**: `FunctionSpecBuilder.canPrepareHigherOrder` (`xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java:320-324`) and `DefaultFunctionInvocation.isPreparedHigherOrderOperator` (`xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java:196-200`) require `operator` to be an instance of `static final class FunctionSpecImpl` (`FunctionSpecBuilder.java:392`) whose `fast` invocation is a `DefaultFunctionInvocation` with `callShape == CallShape.ANY_OF || callShape == CallShape.ALL_OF` and `dataOperands` in `{1, 2}`. `DefaultFunctionInvocation.callShape` (`DefaultFunctionInvocation.java:168-183`) and `create` (`DefaultFunctionInvocation.java:357-365`) assign `ANY_OF`/`ALL_OF` only when `builtIn == true` (`factory == FunctionInvocationFactory.defaultFactory()`, `isBindable(m, instance)`, and `HigherOrderFunctions.anyOf` or `allOf`).
   - **Predicate `FunctionReference` and return type**: `HigherOrderCall.prepare` (`xacml-core/src/main/java/org/xacml4j/v30/policy/HigherOrderCall.java:61, 65`) requires `arguments.get(0) instanceof FunctionReference ref` and `XacmlTypes.BOOLEAN.equals(ref.getEvaluatesTo())`. `FunctionReference` (`xacml-core/src/main/java/org/xacml4j/v30/policy/FunctionReference.java:46-64, 80-83, 160-163`) is `public final`, resolves `this.returnType` once in its constructor into a `private final ValueExpTypeInfo returnType` field, returns `returnType` from `getEvaluatesTo()`, and returns `this` from `evaluate(context)`. Because `XacmlTypes` is an `enum` (`xacml-core/src/main/java/org/xacml4j/v30/types/XacmlTypes.java:43`), `XacmlTypes.BOOLEAN.equals(ref.getEvaluatesTo())` invokes `final` `java.lang.Enum.equals` (`XacmlTypes.BOOLEAN == ref.getEvaluatesTo()`), proving `ref.getEvaluatesTo()` is permanently the enum constant `XacmlTypes.BOOLEAN` (`isBag() == false`).
   - **Predicate parameter specs**: `FunctionSpecBuilder.canPrepareHigherOrder` (`FunctionSpecBuilder.java:323-334`) requires `predicate instanceof FunctionSpecImpl pred`, `!pred.isVariadic()`, and `pred.getNumberOfParams() == dataOperands`. `pred.parameters` is a private list copied at construction (`FunctionSpecBuilder.java:398, 451`) and never mutated or exposed. Each parameter must be non-optional, non-variadic (`:326`), and an instance of either:
     - `final class FunctionParamValueTypeSpec` (`xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionParamValueTypeSpec.java:39, 43, 51, 100-102`) with `typed.getParamType() instanceof XacmlTypes` (`FunctionSpecBuilder.java:327-329`). Its `private ValueExpTypeInfo type` field has no setter or mutator, and its `isValidParamType(actual)` calls `DynamicType.isConsistent(this.type, actual)` (`xacml-core/src/main/java/org/xacml4j/v30/types/DynamicType.java:101-105`), which on an `XacmlTypes` `this.type` (`isBag() == false`, `isDynamic(this.type) == false`) and an `XacmlTypes` `actual` (`isDynamic(actual) == false`) reduces to `Enum.equals` (`this.type == actual`).
     - `final class FunctionParamAnyValueSpec` (`xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionParamAnyValueSpec.java:37-45`), which holds no descriptor field and whose `isValidParamType(actual)` returns `actual instanceof ValueType` (always `true` for any `XacmlTypes`).
2. **Neither `XacmlTypes` nor `BagOfValuesType` can be subclassed, hold a caller-owned object, or change `isValidParamType`**:
   - `XacmlTypes` (`XacmlTypes.java:43-281`) is a Java `enum`. Its 19 enum constants have synthetic `final` anonymous subclasses defined inside `XacmlTypes.java` that override only `ofAny(Object, Object...)`; external subclassing of an `enum` is prohibited by the language/JVM. Its fields (`typeId`, `shortTypeId`, `aliases`, `bagType`, `emptyBag`) hold only engine-created `String`s, `ImmutableSet<String>`, `new BagOfValuesType(this)`, and an empty `BagOfValues` — never a caller-owned object. `XacmlTypes.getValueType()` (`:261-263`) returns `this`, `isBag()` inherits `ValueExpTypeInfo.isBag()` (`false`), and `equals`/`hashCode` are `final` in `java.lang.Enum`.
   - `BagOfValuesType` (`xacml-core/src/main/java/org/xacml4j/v30/BagOfValuesType.java:36-106`) is `public final class BagOfValuesType implements ValueExpTypeInfo` with a single `private final ValueType type` field (`:40, 48-51`). When `HigherOrderCall.prepare` (`HigherOrderCall.java:76-77`) verifies `type instanceof BagOfValuesType bagType && bag == null && bagType.getValueType() instanceof XacmlTypes`, `bagType.type` is guaranteed to be an `XacmlTypes` enum constant (never a caller-owned `ValueType`). `BagOfValuesType.equals(Object o)` (`BagOfValuesType.java:82-93`) checks `o instanceof BagOfValuesType bt` and `type.equals(bt.type)` (`Enum.equals` when `this.type` is `XacmlTypes`).
   - Consequently, at evaluation time in `DefaultFunctionInvocation.invokeHigherOrderSingle` (`DefaultFunctionInvocation.java:207`) and `invokeHigherOrderPair` (`:224-225`), `expectedBagType.equals(bag.getEvaluatesTo())` and `expectedScalarType.equals(scalar.getEvaluatesTo())` hold **if and only if** the runtime bag's descriptor is a `BagOfValuesType` with the identical `XacmlTypes` element constant (`==`) and the runtime scalar's descriptor is the identical `XacmlTypes` constant (`==`). Under that identity, `ref.getParamSpecAt(i).isValidParamType(...)` is invariant and guaranteed `true`.
3. **Order of checks in `HigherOrderCall.prepare` (`HigherOrderCall.java:67-88`)**:
   ```java
   if(type instanceof BagOfValuesType bagType && bag == null
           && bagType.getValueType() instanceof XacmlTypes){
       bag = bagType;
       bagFirst = i == 0;
   }else if(type instanceof XacmlTypes valueType && scalar == null){
       scalar = valueType;
   }else{
       return Optional.empty();
   }
   if(!ref.getParamSpecAt(i).isValidParamType(type.getValueType())) return Optional.empty();
   ```
   - **Null `type`**: Fails both `instanceof` checks and returns `Optional.empty()` in `else` before `type.getValueType()` is called.
   - **First bag (`bag == null`, `type instanceof BagOfValuesType`, `bagType.getValueType() instanceof XacmlTypes`)**: Enters the first branch, records `bag = bagType` and `bagFirst = (i == 0)`, and validates `ref.getParamSpecAt(i).isValidParamType(type.getValueType())`.
   - **First scalar (`scalar == null`, `type instanceof XacmlTypes`)**: Fails `instanceof BagOfValuesType`, enters the `else if` branch, records `scalar = valueType`, and validates `ref.getParamSpecAt(i).isValidParamType(type.getValueType())`. If `count == 1`, `bag` remains `null` and line 87 (`if(bag == null || (count == 2 && scalar == null)) return Optional.empty();`) rejects the unary scalar call.
   - **Second bag (`count == 2`, `bag != null`, second operand is `BagOfValuesType`)**: Fails the `if` branch because `bag == null` is `false`; fails the `else if` branch because `BagOfValuesType` is not `XacmlTypes`; enters `else` and returns `Optional.empty()`.
   - **Bag operand whose element type is custom (`type instanceof BagOfValuesType`, `!(bagType.getValueType() instanceof XacmlTypes)`)**: Fails the `if` branch because `bagType.getValueType() instanceof XacmlTypes` is `false` (this also rejects `DynamicType.INSTANCE.bagType()`); fails the `else if` branch because `BagOfValuesType` is not `XacmlTypes`; enters `else` and returns `Optional.empty()` before calling `isValidParamType`.

---

### 3.2 Question 2 — Rejected Custom Descriptor Routing, Status, Message, Observer Attribution, and Operand Evaluation Count

**Hypothesis tested**: Does a rejected custom descriptor diverge from pre-D52 behavior in status code, status message, `FunctionSpec` / `DecisionObserver` attribution, or operand evaluation order/count?

**Result**: **Refuted by source** (`SUPPORTED`).

1. When `canPrepareHigherOrder` (`FunctionSpecBuilder.java:320-335`) returns `false` or `HigherOrderCall.prepare` (`HigherOrderCall.java:58-92`) returns `Optional.empty()`, `Apply` (`xacml-core/src/main/java/org/xacml4j/v30/policy/Apply.java:103-107`) sets `this.callSite = null`.
2. In `Apply.evaluate(context)` (`Apply.java:277-304`):
   - If `typeError.isPresent()`, lines 280–286 throw `FunctionInvocationException(spec, NOT_TYPE_CORRECT, typeError.get())` (`StatusCodeId.STATUS_PROCESSING_ERROR`), notify `RootEvaluationContext.observationOf(context).functionFailed(context, spec, failure)`, and evaluate `0` operands — unchanged from pre-D52.
   - If `typeError.isEmpty()`, `callSite == null` bypasses lines 288–295 and executes lines 296–298 (`spec.invokeWithList(context, invocationArguments)`), which is the exact pre-D52 invocation path:
     - **With `context.isValidateFuncParamsAtRuntime() == false`**: `FunctionSpecImpl.invokeWithList` (`FunctionSpecBuilder.java:639-640`) calls `invokeFast(context, arguments)` (`:650-667`), which evaluates `arguments.get(0)`, `arguments.get(1)` (and `arguments.get(2)` if binary) **once each in left-to-right order** outside the `try / catch` failure wrapper (so an operand evaluation failure propagates with the operand's own `FunctionSpec` and no outer observer failure). It then calls `DefaultFunctionInvocation.invokeFast` (`DefaultFunctionInvocation.java:274-277`) -> `HigherOrderFunctions.anyOfSingle` / `anyOfPair` / `allOfSingle` / `allOfPair` (`xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/HigherOrderFunctions.java:83-102, 156-175`), which run `requireValidArguments(ref, ...)` (`:422-480`). On a post-construction metadata mismatch, `requireValidArguments` throws `new EvaluationException(Status.syntaxError().build(), "Invalid higher-order function arguments")`, wrapped by `DefaultFunctionInvocation.invokeFast` (`:284-286`) in `new FunctionInvocationException(spec, invocationCause(e))` (preserving `StatusCodeId.SYNTAX_ERROR`, message `"Invalid higher-order function arguments"`, and outer `spec`), and reported **once** to `RootEvaluationContext.observationOf(context).functionFailed(context, this, failure)` in `FunctionSpecImpl.failed` (`FunctionSpecBuilder.java:775-780`).
     - **With `context.isValidateFuncParamsAtRuntime() == true`**: `FunctionSpecImpl.invokeFast` (`FunctionSpecBuilder.java:651-653`) calls `doInvoke(context, arguments, true)` (`:743-769`), which runs `doValidateNormalizedParameters` before evaluating operands (throwing `StatusCodeId.STATUS_PROCESSING_ERROR` with `0` operand evaluations if already invalid before evaluation) and then evaluates each operand **once in order** and invokes `HigherOrderFunctions.anyOf` / `allOf` (`:68, 141`), where `requireValidArguments` catches any mutation that occurred during operand evaluation with the same `StatusCodeId.SYNTAX_ERROR`, `"Invalid higher-order function arguments"`, outer `FunctionSpec`, and single observer notification.

---

### 3.3 Question 3 — Closure of `rv201` and `rv204` Defects and Absence of Regressions in `correction.patch`

**Hypothesis tested**: Do any of the frozen-candidate defects in `rv201` (`D52-ASTRA-1`) or `rv204` (`P2-1`) remain open, or did `correction.patch` introduce a new defect or disable D52 for canonical callers?

**Result**: **All prior defects close; no new defect introduced** (`SUPPORTED`).

1. **`rv201` (`P2 / D52-ASTRA-1`) and `rv204` (`P2-1`)**:
   - Closed by `FunctionSpecBuilder.java:327-330` (`if (!(typed.getParamType() instanceof XacmlTypes)) return false;`) and `HigherOrderCall.java:76-84` (requiring `type instanceof BagOfValuesType bagType && bag == null && bagType.getValueType() instanceof XacmlTypes` for the bag operand and `type instanceof XacmlTypes valueType && scalar == null` for the scalar operand).
2. **Preservation of D52 and D45 (`PreparedConditions`) for canonical types**:
   - Canonical `XacmlTypes` typed parameters and `FunctionParamAnyValueSpec` (`anyAttribute()`) parameters remain eligible (`HigherOrderMetadataCompatibilityTest.java:342-364`).
   - D45 `PreparedConditions.Shared` clones delegate `getEvaluatesTo()` to the underlying expression (`XacmlTypes.STRING` or `XacmlTypes.STRING.bagType()`), so rebuilt higher-order `Apply` nodes in `PreparedConditions` retain both `PreparedConditions$Shared` arguments and a non-null `HigherOrderCall` in `Apply.callSite` (`SharedConditionValuesPdpTest.java:191-208`).
   - `Apply.java` is byte-identical (`222342ac35a484e587360eb473290e1547c6001b46b554e0b14586d484fa074e`) to the original candidate, preserving the `Object callSite` field reuse.

---

## 4. Supplied Evidence vs Unverified / Unrun Gates

- **Reviewer execution**: Read-only review; no builds, tests, or benchmarks were executed by this reviewer (`UNRUN`).
- **Supplied author test runs (`W/cod_3/openspec/changes/optimize-pdp-evaluation/evidence/tests/203-resume-d52-metadata-astra/summary.md` and `test-results.json`)**:
  - Focused red runs on frozen production: `red-core.log` (`12` tests, `6` expected failures with runtime validation off) and `red-pdp.log` (`2` tests, `2` expected failures).
  - Focused green run on corrected candidate (`green-focused.log`): `335` tests (`287` in `xacml-core`, `48` in `xacml-pdp`), `0` failures, `0` errors, `0` skipped.
  - Module suites on 2026-10-31 (`green-core-pdp-suites.log`, `affected-suites-continued.log`):
    - `xacml-core`: `7,443` tests, `0` failures, `1` error (`ISO8601DateTest.testCreateFromCalendar` at `xacml-core/src/test/java/org/xacml4j/v30/types/ISO8601DateTest.java:59` / `xacml-core/src/main/java/org/xacml4j/util/DateTimeDurationUtils.java:150-152`, which passes zero-based `Calendar.MONTH` (`9` in October) to one-based `LocalDate.of`, failing on October 31 with `DateTimeException: Invalid date 'SEPTEMBER 31'`; verified in baseline `759d90a6` as pre-existing and unrelated to D52), `2` skipped.
    - `xacml-pdp`: `810` tests, `0` failures, `0` errors, `3` skipped.
    - `xacml-xml`: `254` tests, `0` failures, `0` errors, `0` skipped.
    - `xacml-json`: `24` tests, `0` failures, `0` errors, `1` skipped.
- **Unrun gates (`UNRUN`)**:
  - Full reactor and conformance suite (`xacml-conformance-tests`) without `-Dmaven.test.failure.ignore=true` after the unrelated `ISO8601DateTest` / `DateTimeDurationUtils` calendar-month defect is resolved.
  - Controlled same-JDK C2 JMH pair (`PdpDecideBenchmark.decide` with `-jvmArgs -XX:-UseJVMCICompiler` and `-prof gc`) and async CPU profile gate for D52 / batch acceptance.
