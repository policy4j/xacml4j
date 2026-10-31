# Independent Adversarial Code Review (Recheck): D52 (`rv204-resume-d52-argon-recheck` / `R-D52-argon`)

- **Change**: `optimize-pdp-evaluation`
- **Design**: D52 — Prepared signatures for higher-order call sites (`openspec/changes/optimize-pdp-evaluation/design.md:2059-2090`)
- **Task / Beads**: Task `42.19` (`openspec/changes/optimize-pdp-evaluation/tasks.md:330-331`), Beads `policy4j-cwtu.42.19.2`
- **Reviewer**: `agyx_1` (actor `ntm-argon-g1`), Agy Gemini 4 Argon High launched with the plain review packet (no separate `policy4j_reviewer_argon` CAO profile selected)
- **Author Report Inspected**: `REPORT-I-D52-terra.md` (`I-D52-terra`; exact model version not stated in the report)
- **Verdict**: **REJECT** (`0` P1, `1` P2, `0` P3)

---

## 1. Scope and Source Hash Verification (`SUPPORTED`)

### 1.1 Candidate Manifest vs Frozen Snapshot vs Baseline Comparison

All 8 files in the D52 candidate slice were compared by SHA-256 across:
1. Packet manifest (`d52-manifest.json`)
2. Frozen packet snapshot (`d52-candidate/`)
3. Live candidate worktree (`cod_1`, base `cd443805c3044e7412c25aae71dd02127994d418`)
4. Reviewer worktree current-main baseline (`759d90a6c75510d55127d8e22870f9edd985c991`)

| Repository-Relative Path | Base `cd443805` SHA-256 | Baseline `759d90a6` SHA-256 | Drift (`cd443805` vs `759d90a6`) | Candidate D52 SHA-256 (`manifest` = `d52-candidate` = `cod_1`) |
| --- | --- | --- | --- | --- |
| `xacml-core/src/main/java/org/xacml4j/v30/policy/Apply.java` | `845eb274e69c2b04ba0c06651dab264e17df75d31ebfdc1ff9a373775cb9da4c` | `845eb274e69c2b04ba0c06651dab264e17df75d31ebfdc1ff9a373775cb9da4c` | **NONE** | `222342ac35a484e587360eb473290e1547c6001b46b554e0b14586d484fa074e` |
| `xacml-core/src/main/java/org/xacml4j/v30/policy/HigherOrderCall.java` | *(new file)* | *(new file)* | **NONE** | `ae7b46cb690ddf289adaf3a47398102334626032ff1be037c3aad0d286aa8955` |
| `xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java` | `876a815b44a29a18c2b70017a787c9f5315cad4e2bfd411e2716a63daaea3574` | `876a815b44a29a18c2b70017a787c9f5315cad4e2bfd411e2716a63daaea3574` | **NONE** | `647f64d3e648ed538fb15ea6f5514ed0142a5c7a325e4a85343f7b460ebb271d` |
| `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java` | `daabc778ccc4be598b3d6c971e89eb26cac31148ef2d8db0e0dbf6de60e5b719` | `daabc778ccc4be598b3d6c971e89eb26cac31148ef2d8db0e0dbf6de60e5b719` | **NONE** | `cadc93aa1855de78607c7e8351137fe2715276caba33dd1a43c1ea0855a993e2` |
| `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/HigherOrderFunctions.java` | `750caf8ba87de90aec815b9187ef503acc006a1dbd7db8d56f98e38aca14ba50` | `750caf8ba87de90aec815b9187ef503acc006a1dbd7db8d56f98e38aca14ba50` | **NONE** | `238dafcd13ce7b8ad502b39e29e93695f2e565d8435a596687e407b2bff84169` |
| `xacml-core/src/test/java/org/xacml4j/v30/policy/ApplyArgumentValidationTest.java` | `5c847a27e1f5b8b29036e8bd652bc5dd67fa9488e40da9106f78e7e36bdc7330` | `5c847a27e1f5b8b29036e8bd652bc5dd67fa9488e40da9106f78e7e36bdc7330` | **NONE** | `c56f68aced2414252fe4a5f4068fd3a9738057f2e16b140a8f1f2df4311a2ddc` |
| `xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/LaterDecidingOperandTest.java` | `be5df106863a34b72c4e221b8faf893fb091e4fc0739cef83d7d1d59ec189ff0` | `be5df106863a34b72c4e221b8faf893fb091e4fc0739cef83d7d1d59ec189ff0` | **NONE** | `d5ea6a7f12a77ff95325b42bfc7872aa421a815b44f0a636859621090a2fbf09` |
| `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/SharedConditionValuesPdpTest.java` | `fe7c7b4dbfd520614f5ee8a17035865182e7a777c3e244d1b3fc7e3bedce8092` | `fe7c7b4dbfd520614f5ee8a17035865182e7a777c3e244d1b3fc7e3bedce8092` | **NONE** | `12d0fa547f66662d48e4314e41c7f9b731a9c3045cc889664b5a261bb16df689` |

**Result**: Zero source hash drift exists between `cd443805` and `759d90a6` on any file touched by D52 (`SUPPORTED`).

### 1.2 Tool Health (`SUPPORTED` / `UNAVAILABLE`)

- `scripts/agent-doctor.sh`:
  - `ast-grep`: `PASS` (`SUPPORTED`)
  - `ripwire`: `PASS` (`SUPPORTED`)
  - `serena`: `PASS` (`SUPPORTED`, serving this checkout)
  - `tgrep`: `FAIL` (`UNAVAILABLE`, no trigram index in this worktree; compensated via direct `read_file` inspection)

---

## 2. Actionable Defect (`REJECT`)

### `P2-1` — `canPrepareHigherOrder` and `HigherOrderCall.prepare` admit custom/mutable `ValueExpTypeInfo` and `ValueType` descriptors, allowing post-preparation signature drift and asymmetric `equals` divergence from checked evaluation

- **Location**:
  - `xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java:321-335` (`canPrepareHigherOrder`)
  - `xacml-core/src/main/java/org/xacml4j/v30/policy/HigherOrderCall.java:59-91` (`prepare`)
  - `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java:203-232` (`invokeHigherOrderSingle`, `invokeHigherOrderPair`)
  - Supporting contracts:
    - `xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java:130-175` (`param(ValueExpTypeInfo, ...)`)
    - `xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionParamValueTypeSpec.java:43-60, 100-102` (`type`, `isValidParamType`)
    - `xacml-core/src/main/java/org/xacml4j/v30/ValueExpTypeInfo.java:35-58`
    - `xacml-core/src/main/java/org/xacml4j/v30/types/ValueType.java:40-109`
    - `xacml-core/src/main/java/org/xacml4j/v30/BagOfValuesType.java:36-93`
    - `xacml-core/src/main/java/org/xacml4j/v30/types/DynamicType.java:87-105`
    - `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/HigherOrderFunctions.java:446-467, 520-529` (`validateArguments`)
- **Evidence**: `SUPPORTED` (direct source inspection of all files above).
- **Problem**:
  D52 (`openspec/changes/optimize-pdp-evaluation/design.md:2060-2062`) requires "standard immutable parameter metadata, and statically known operand types" so that skipping `HigherOrderFunctions.validateArguments` at evaluation time is sound.
  While `FunctionReference` (`FunctionReference.java:46`), `FunctionSpecImpl` (`FunctionSpecBuilder.java:392`), `FunctionParamValueTypeSpec` (`FunctionParamValueTypeSpec.java:39`), `FunctionParamAnyValueSpec` (`FunctionParamAnyValueSpec.java:37`), and `BagOfValuesType` (`BagOfValuesType.java:36`) are `final` classes, they retain `ValueExpTypeInfo` and `ValueType` instances supplied through unsealed public extension APIs:
  1. `FunctionSpecBuilder.param(ValueExpTypeInfo)` (`FunctionSpecBuilder.java:130, 142`) accepts any `ValueExpTypeInfo` implementation and stores it in `FunctionParamValueTypeSpec.type` (`FunctionParamValueTypeSpec.java:43, 51`). In `FunctionSpecBuilder.canPrepareHigherOrder` (`FunctionSpecBuilder.java:328-329`):
     ```java
     if (parameter instanceof FunctionParamValueTypeSpec typed) {
         if (DynamicType.isDynamic(typed.getParamType())) return false;
     }
     ```
     `DynamicType.isDynamic` (`DynamicType.java:87-89`) only tests `type != null && type.getValueType() == DynamicType.INSTANCE`. Any custom or mutable `ValueExpTypeInfo` / `ValueType` whose `getValueType()` is not `DynamicType.INSTANCE` at preparation time is admitted.
  2. `HigherOrderCall.prepare` (`HigherOrderCall.java:74-85`) checks:
     ```java
     ValueExpTypeInfo type = argument.getEvaluatesTo();
     if(type == null || type.getValueType() == null || DynamicType.isDynamic(type)
             || !ref.getParamSpecAt(i).isValidParamType(type.getValueType())) return Optional.empty();
     if(type instanceof BagOfValuesType bagType && bag == null){
         bag = bagType;
         bagFirst = i == 0;
     }else if(type instanceof ValueType valueType && scalar == null){
         scalar = valueType;
     }else{
         return Optional.empty();
     }
     ```
     This admits any custom/mutable `ValueType` in `bagType.getValueType()` (constructed via `new BagOfValuesType(customValueType)`) or in `scalar` (`type instanceof ValueType`).
  3. At evaluation time, `DefaultFunctionInvocation.invokeHigherOrderSingle` (`DefaultFunctionInvocation.java:207-209`) and `invokeHigherOrderPair` (`DefaultFunctionInvocation.java:224-227`) only check:
     ```java
     if(bagValue instanceof BagOfValues bag && expectedBagType.equals(bag.getEvaluatesTo())
             && scalarValue instanceof Value<?> scalar && expectedScalarType.equals(scalar.getEvaluatesTo()))
     ```
     They never re-invoke `ref.getParamSpecAt(i).isValidParamType(...)`, `type.getValueType() != null`, or `type.isBag()`.
- **Concrete Reachable Counterexamples**:
  1. **Counterexample 1 — Mutable/custom `ValueExpTypeInfo` in `FunctionParamValueTypeSpec`**:
     - Build a predicate via `FunctionSpecBuilder.builder("urn:test:pred").param(mutableParamType).param(XacmlTypes.STRING).build(XacmlTypes.BOOLEAN, invocation)` where `mutableParamType` implements `ValueExpTypeInfo` and delegates `getValueType()` / `equals(Object)` to an `AtomicReference<ValueType>` initially set to `XacmlTypes.STRING`.
     - Build `Apply expr = Apply.builder(anyOf).param(new FunctionReference(pred), StringVal.of("a"), XacmlTypes.STRING.bagOf(StringVal.of("a"))).build()`.
     - At `Apply` construction time, `typeErrorOf` and `HigherOrderCall.prepare` both succeed (`expectedScalarType = XacmlTypes.STRING`, `expectedBagType = XacmlTypes.STRING.bagType()`).
     - Mutate the `AtomicReference` to `XacmlTypes.INTEGER` before calling `expr.evaluate(context)`.
     - **Prepared path (`isValidateFuncParamsAtRuntime() == false`)**: `expectedBagType.equals(bag.getEvaluatesTo())` and `expectedScalarType.equals(scalar.getEvaluatesTo())` compare `XacmlTypes.STRING` and `XacmlTypes.STRING.bagType()` without consulting `mutableParamType`, so `HigherOrderFunctions.invokePair` invokes `pred` and returns `BooleanVal` (`true`/`false`).
     - **Ordinary checked path (`anyOf.invokeWithList(context, expr.getArguments())` or `isValidateFuncParamsAtRuntime() == true`)**: `HigherOrderFunctions.validateArguments` (`HigherOrderFunctions.java:461-463`) calls `ref.getParamSpecAt(0).isValidParamType(XacmlTypes.STRING)` -> `DynamicType.isConsistent(mutableParamType, XacmlTypes.STRING)` -> `false`, throwing `FunctionInvocationException` (`StatusCodeId.SYNTAX_ERROR` `"Invalid higher-order function arguments"` on the unchecked invocation path, or `StatusCodeId.STATUS_PROCESSING_ERROR` under `doValidateNormalizedParameters`).
  2. **Counterexample 2 — Custom `ValueType` on operand (same-instance `equals` short-circuit or asymmetric `equals`)**:
     - If an operand uses a custom `ValueType` (as `scalar` or inside `new BagOfValuesType(customType)`) whose `getValueType()` becomes `null` or `isBag()` flips to `true` after `Apply` construction, `BagOfValuesType.equals` (`BagOfValuesType.java:83`, `if (o == this) return true;`) and `expectedScalarType.equals(scalar.getEvaluatesTo())` (`this == o`) short-circuit to `true` on the same descriptor instance and invoke the predicate, whereas `HigherOrderFunctions.validateArguments` (`HigherOrderFunctions.java:456-459`) rejects `type.getValueType() == null` or `bags != 1` with `Status.syntaxError()`.
     - Even without post-construction mutation, if a predicate parameter uses `FunctionParamValueTypeSpec(strictCustomType)` (where `strictCustomType.equals(broadCustomType)` is `true` and `strictCustomType.equals(XacmlTypes.INTEGER)` is `false`) and a scalar operand declares static `getEvaluatesTo() == broadCustomType` (where `broadCustomType.equals(XacmlTypes.INTEGER)` is `true`) but evaluates at runtime to `XacmlTypes.INTEGER.ofAny(1)`, `invokeHigherOrderPair` checks `expectedScalarType.equals(XacmlTypes.INTEGER)` (`broadCustomType.equals(XacmlTypes.INTEGER) == true`) and invokes the predicate, whereas `HigherOrderFunctions.validateArguments` checks `strictCustomType.equals(XacmlTypes.INTEGER)` (`false`) and throws `Status.syntaxError()`.
- **Minimal Fix**:
  Restrict both the predicate's `FunctionParamValueTypeSpec` parameter types and the operands' scalar/bag-element `ValueType`s to the built-in `XacmlTypes` enum (whose `equals` is `final` `Enum.equals` identity, `isBag()` is `false`, `getValueType()` is `this`, and which is never `DynamicType.INSTANCE`):
  1. In `xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java:328-330`:
     ```java
     if (parameter instanceof FunctionParamValueTypeSpec typed) {
         if (!(typed.getParamType() instanceof XacmlTypes)) return false;
     } else if (!(parameter instanceof FunctionParamAnyValueSpec)) {
         return false;
     }
     ```
  2. In `xacml-core/src/main/java/org/xacml4j/v30/policy/HigherOrderCall.java:74-84`:
     ```java
     ValueExpTypeInfo type = argument.getEvaluatesTo();
     if(type == null || !(type.getValueType() instanceof XacmlTypes)
             || !ref.getParamSpecAt(i).isValidParamType(type.getValueType())) return Optional.empty();
     if(type instanceof BagOfValuesType bagType && bag == null){
         bag = bagType;
         bagFirst = i == 0;
     }else if(type instanceof XacmlTypes valueType && scalar == null){
         scalar = valueType;
     }else{
         return Optional.empty();
     }
     ```
- **Required Regression Coverage**:
  Add a test in `xacml-core/src/test/java/org/xacml4j/v30/policy/ApplyArgumentValidationTest.java` (or `LaterDecidingOperandTest.java`) verifying that:
  1. A higher-order `any-of` / `all-of` call whose predicate has a custom/mutable `ValueExpTypeInfo` parameter descriptor in `FunctionSpecBuilder.param(...)` that flips validity after `Apply` construction is not prepared into `HigherOrderCall` and throws `EvaluationException` (`StatusCodeId.SYNTAX_ERROR` when `isValidateFuncParamsAtRuntime() == false`, `StatusCodeId.STATUS_PROCESSING_ERROR` when `true`).
  2. A higher-order `any-of` / `all-of` call whose scalar or bag operand uses a custom `ValueType` implementation is not prepared into `HigherOrderCall` and runs `HigherOrderFunctions.validateArguments` at evaluation time.

---

## 3. Verification of Other Review Checkpoints (`SUPPORTED`)

1. **Operator and `FunctionReference` return-type checks**:
   - `Apply` (`xacml-core/src/main/java/org/xacml4j/v30/policy/Apply.java:106-107`) attempts `HigherOrderCall.prepare(spec, arguments)` only when `bound == null`, `typeError.isEmpty()`, and `invocationArguments == arguments` (excluding any `DynamicType` operand wrapped in `TypeAssertion` by `withTypeAssertions` at lines 119–141).
   - `FunctionSpecBuilder.canPrepareHigherOrder` (`FunctionSpecBuilder.java:321-325`) and `DefaultFunctionInvocation.isPreparedHigherOrderOperator` (`DefaultFunctionInvocation.java:196-200`) require `operator` to be a `FunctionSpecImpl` with `callShape == CallShape.ANY_OF || callShape == CallShape.ALL_OF` and `dataOperands` equal to `1` or `2`. `DefaultFunctionInvocation.callShape` (`DefaultFunctionInvocation.java:168-183`) and `create` (`DefaultFunctionInvocation.java:357-365`) set `ANY_OF`/`ALL_OF` only when `builtIn == true` (`factory == FunctionInvocationFactory.defaultFactory()`, `isBindable(m, instance)`, and `HigherOrderFunctions.anyOf` or `allOf`).
   - `FunctionReference` (`xacml-core/src/main/java/org/xacml4j/v30/policy/FunctionReference.java:46-64, 80-83, 160-163`) is `final`, caches `returnType` in a `final` field in its constructor, and evaluates to `this`. `HigherOrderCall.prepare` (`HigherOrderCall.java:66`) checks `XacmlTypes.BOOLEAN.equals(ref.getEvaluatesTo())`, which invokes `Enum.equals` (`XacmlTypes.BOOLEAN == ref.getEvaluatesTo()`).
2. **Operand Evaluation Order & Failure/Observer Attribution (Design D34)**:
   - `HigherOrderCall.evaluate` (`HigherOrderCall.java:93-102`) evaluates `first = arguments.get(1).evaluate(context)` and (for binary calls) `second = arguments.get(2).evaluate(context)` in left-to-right order **before** calling `FunctionSpecBuilder.invokeHigherOrderSingle` / `invokeHigherOrderPair`.
   - If `first.evaluate(context)` throws `EvaluationException`, `second` is not evaluated, and the exception propagates with the inner operand's `FunctionSpec` without calling `DecisionObservation.functionFailed` for `any-of`/`all-of`.
   - If `first` evaluates to a runtime value of the wrong type and `second.evaluate(context)` throws `EvaluationException`, `second` is still evaluated before the runtime type check on `first`, so `second`'s `EvaluationException` propagates (`ApplyArgumentValidationTest.java:356-372`).
   - Inside `FunctionSpecBuilder.invokeHigherOrderSingle` (`FunctionSpecBuilder.java:338-346`) and `invokeHigherOrderPair` (`FunctionSpecBuilder.java:349-359`), the call to `built.fast.invokeHigherOrderSingle` / `invokeHigherOrderPair` is wrapped in `try { ... } catch (Exception e) { throw built.failed(context, e); }`, and inside `DefaultFunctionInvocation` (`DefaultFunctionInvocation.java:206-212, 221-230`) any body failure is wrapped in `new FunctionInvocationException(spec, invocationCause(e))`. Predicate failures and fallback validation failures are therefore attributed to the outer `any-of`/`all-of` `FunctionSpec` and reported to `RootEvaluationContext.observationOf(context).functionFailed(context, this, failure)` (`FunctionSpecBuilder.java:775-780`; tested in `LaterDecidingOperandTest.java:129-164`).
3. **Unchecked Fallback, Wrong Runtime Type, and Null Bags**:
   - When `value` in `invokeHigherOrderSingle` (`DefaultFunctionInvocation.java:207-213`) is `null`, not a `BagOfValues`, or has a `BagOfValuesType` not equal to `expectedBagType`, or when either operand in `invokeHigherOrderPair` (`DefaultFunctionInvocation.java:224-231`) is `null` or does not match `BagOfValues`/`Value<?>` and the expected type, execution delegates to `invokeFast(spec, context, arguments, ref, first, second, null)` with the **already-evaluated** operand values (never re-evaluating `arguments.get(1)` or `arguments.get(2)`), where `HigherOrderFunctions.requireValidArguments` throws `EvaluationException(Status.syntaxError().build(), "Invalid higher-order function arguments")` (`ApplyArgumentValidationTest.java:296-352`).
4. **Runtime Parameter Validation (`context.isValidateFuncParamsAtRuntime()`)**:
   - `Apply.evaluate` (`Apply.java:292-294`) gates `((HigherOrderCall)callSite).evaluate(context)` on `!context.isValidateFuncParamsAtRuntime()`. When `true`, evaluation takes `spec.invokeWithList(context, invocationArguments)` -> `FunctionSpecImpl.invokeFast` -> `doInvoke(context, arguments, true)` (`FunctionSpecBuilder.java:651-653, 743-769`).
5. **D45 Shared Condition Clones (`PreparedConditions`) and D47 Rule Context Eligibility (`Rule.EngineOnly`)**:
   - In `PreparedConditions` (`xacml-core/src/main/java/org/xacml4j/v30/policy/PreparedConditions.java`), `isCrossable(Apply)` (lines 219–221) admits type-correct built-in `Apply` calls (`FunctionSpecBuilder.usesBuiltInInvocation(apply.function())`), and `Preparation.rebuild` (lines 380–408) replaces selected repeated string subexpressions with `PreparedConditions.Shared` (lines 464–504) and clones enclosing `Apply` nodes via `Apply.builder(apply.function()).params(rebuilt).build()` (line 406).
   - `PreparedConditions.Shared.getEvaluatesTo()` (`PreparedConditions.java:475-477`) delegates to `expression.getEvaluatesTo()`, which for D45 eligible expressions (`PreparedConditions.java:187-202, 223-239, 303-311`) is always `XacmlTypes.STRING` or `XacmlTypes.STRING.bagType()`. Thus D45-cloned `any-of`/`all-of` `Apply` nodes remain eligible for `HigherOrderCall` even with the `P2-1` `XacmlTypes` fix (`SharedConditionValuesPdpTest.java:161-180`).
   - In `Rule` (`xacml-core/src/main/java/org/xacml4j/v30/policy/Rule.java`), `this.inPolicyContext = new EngineOnly().rule(this)` (line 63) walks the rule via `Rule.EngineOnly.rule(Rule)` (lines 252–256), `condition(Condition)` (lines 284–286), and `expression(Expression)` (lines 302–348), checking `FunctionReference.class` at lines 320–323 (`FunctionSpecBuilder.usesBuiltInInvocation(((FunctionReference)e).function())`) and `Apply.class` at lines 324–335 (`FunctionSpecBuilder.usesBuiltInInvocation(apply.function())` and `apply.getArguments()`). `Apply.callSite` does not alter `Apply.function()`, `Apply.getArguments()`, or `Rule.EngineOnly.rule(this)`.
6. **Regex-Bound Call (`BoundCall`) Interaction and `Apply` Object Size**:
   - `Apply` (`Apply.java:81-82, 103-107, 288-295`) shares `private final Object callSite` between `BoundCall` (which requires `arguments.size() == 2 && arguments.get(0) instanceof Value<?> literal`) and `HigherOrderCall` (which requires `arguments.get(0) instanceof FunctionReference`). Because `FunctionReference` (`final`) does not implement `Value<?>`, the two plans are mutually exclusive, and `Apply` stays at 40 bytes on JDK 25 with compressed references (`REPORT-I-D52-terra.md:23`).

---

## 4. Supplied Evidence vs Unrun Gates

### 4.1 Test Evidence (`UNRUN` in this review lane; supplied in `REPORT-I-D52-terra.md`)

- **Reviewer execution**: No builds or tests were run in this read-only review lane (`UNRUN`).
- **Supplied author report (`REPORT-I-D52-terra.md:19-21`)**:
  - Baseline JMH build (`rtk mvn -o -q -Dlicense.skipUpdateLicense=true -pl xacml-jmh -am -DskipTests clean package`): `PASS`.
  - Focused `xacml-core` test run (`-pl xacml-core -Dtest=ApplyArgumentValidationTest,LaterDecidingOperandTest,HigherOrderVariadicFunctionsTest -Dsurefire.failIfNoSpecifiedTests=false test`): `PASS` (per-class test counts are **not recorded** in `REPORT-I-D52-terra.md`).
  - Full reactor `package` (`rtk mvn -o -q -Dlicense.skipUpdateLicense=true package` with `-Djmh.ignoreLock=true`): `PASS` — **10,591 tests, 36 skipped, 0 failures/errors** (`xacml-core` **7,363** / 2 skipped, `xacml-pdp` **808** / 3 skipped, `xacml-conformance-tests` **1,818** / 30 skipped, `xacml-jmh` **225**, `xacml-xml` **254**, `xacml-json` **24** / 1 skipped, `xacml-test` **99**).

### 4.2 Unrun Benchmark and Profile Gates for Task `42.19` (`UNRUN`)

Per `openspec/changes/optimize-pdp-evaluation/tasks.md:330-331`, `openspec/changes/optimize-pdp-evaluation/design.md:2022-2024, 2120-2128`, `xacml-jmh/README.md:20-58`, `xacml-jmh/src/main/java/org/xacml4j/jmh/pdp/PdpDecideBenchmark.java:50-81`, and `REPORT-I-D52-terra.md:24-25`:
- **Controlled same-JDK C2 JMH pair (`PdpDecideBenchmark.decide`)**: **UNRUN** (`NOT RUN` in `REPORT-I-D52-terra.md:25` due to host load 13.13–37.90; baseline jar `021abbf8bc621da2d7809341856f98f87f9ec6ce4b15f9cbe411e309184c5eb0` and candidate jar `d1bc5f1b147e302bb5b211e98e9bd529f879115cace77360458d0d1d002f975b` were built for back-to-back C2 `-jvmArgs -XX:-UseJVMCICompiler` `-prof gc` comparison on `rsa2008` and `synthetic:100`, etc., after `P2-1` is fixed and re-packaged).
- **Async CPU profile gate**: **UNRUN** (`NOT RUN` in `REPORT-I-D52-terra.md:25`).
