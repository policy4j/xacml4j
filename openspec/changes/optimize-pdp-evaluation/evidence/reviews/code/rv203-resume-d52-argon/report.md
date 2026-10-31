# Independent Adversarial Code Review: D52 (`rv203-resume-d52-argon` / `R-D52-argon`)

- **Change**: `optimize-pdp-evaluation`
- **Design**: D52 — Prepared signatures for higher-order call sites
- **Task / Beads**: `42.19.2` (`policy4j-cwtu.42.19.2`)
- **Reviewer**: `agyx_1` (actor `ntm-argon-g1`), Agy Gemini 4 Argon High (`policy4j_reviewer_argon`)
- **Author Lane Reviewed**: `I-D52-terra` (OpenAI GPT-5.6 Terra)
- **Verdict**: **APPROVE**

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
  - `tgrep`: `FAIL` (`UNAVAILABLE`, no trigram index in this worktree; compensated via direct `read_file` inspection and `rtk rg`)

---

## 2. Adversarial Verification Against Production Code (`SUPPORTED`)

Each failure mode from `openspec/changes/optimize-pdp-evaluation/design.md` (D52, lines 2116–2158), plan review `openspec/changes/optimize-pdp-evaluation/evidence/reviews/plan/pr-D52-argon/report.md`, and the review brief was tested against the candidate implementation and surrounding production classes.

### 2.1 Invalid Static Proof & Extension Callbacks

- **Operator trust boundary**:
  - `Apply` (`xacml-core/src/main/java/org/xacml4j/v30/policy/Apply.java:106-107`) only attempts `HigherOrderCall.prepare(spec, arguments)` when `bound == null`, `typeError.isEmpty()`, and `invocationArguments == arguments` (i.e., no `DynamicType` operand was wrapped in a `TypeAssertion`).
  - `FunctionSpecBuilder.canPrepareHigherOrder` (`xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java:321-335`) requires:
    1. `operator instanceof FunctionSpecImpl op`
    2. `op.fast != null && op.fastShape(dataOperands + 1)`
    3. `op.fast.isPreparedHigherOrderOperator(dataOperands)` (`xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java:196-200`), which holds only when `callShape == CallShape.ANY_OF || callShape == CallShape.ALL_OF` and `dataOperands` is `1` or `2`.
    4. In `DefaultFunctionInvocation.callShape` (`DefaultFunctionInvocation.java:168-183`), `ANY_OF` and `ALL_OF` are assigned only when `builtIn == true`, which in `DefaultFunctionInvocation.create` (`DefaultFunctionInvocation.java:357-364`) requires `factory == FunctionInvocationFactory.defaultFactory()`, `isBindable(m, instance)`, and `XacmlDefaultFunctions.isBuiltIn(m.getDeclaringClass())` for `HigherOrderFunctions.anyOf` or `HigherOrderFunctions.allOf`.
  - A custom `FunctionSpec` implementation, a custom `FunctionInvocationFactory` (e.g., `SharedConditionValuesPdpTest.java:256-269`), or a custom function with the `any-of`/`all-of` XACML URI therefore cannot receive a `HigherOrderCall`.
- **Predicate (`FunctionReference`) trust boundary**:
  - `HigherOrderCall.prepare` (`xacml-core/src/main/java/org/xacml4j/v30/policy/HigherOrderCall.java:62-67`) requires `arguments.get(0) instanceof FunctionReference ref` (`FunctionReference` is `final`, and its `evaluate(context)` returns `this` and its `returnType` is cached in a `final` field at construction time in `xacml-core/src/main/java/org/xacml4j/v30/policy/FunctionReference.java:50-64, 80-83, 160-163`) and `XacmlTypes.BOOLEAN.equals(ref.getEvaluatesTo())`.
  - Because `FunctionReference` caches `returnType` once in its constructor via `spec.resolveReturnType(ImmutableList.of())`, predicates with either `StaticFunctionReturnTypeResolver` or a custom `FunctionReturnTypeResolver` returning `XacmlTypes.BOOLEAN` have an immutable `ref.getEvaluatesTo()` equal to `XacmlTypes.BOOLEAN` (`LaterDecidingOperandTest.java:168-188`).
  - `FunctionSpecBuilder.canPrepareHigherOrder` (`FunctionSpecBuilder.java:324-334`) requires `predicate instanceof FunctionSpecImpl pred`, `!pred.isVariadic()`, `pred.getNumberOfParams() == dataOperands`, and every `FunctionParamSpec` in `pred.parameters` to be non-optional, non-variadic, and an instance of package-private final `FunctionParamValueTypeSpec` (with `!DynamicType.isDynamic(typed.getParamType())`) or package-private final `FunctionParamAnyValueSpec`. Custom `FunctionParamSpec` implementations and variadic/optional parameters are excluded.
- **Data operand static proof**:
  - `HigherOrderCall.prepare` (`HigherOrderCall.java:68-87`) verifies for each data operand `i` (`0 .. count - 1`):
    - `argument != null && !(argument instanceof FunctionReference)`
    - `type = argument.getEvaluatesTo()` is non-null, `type.getValueType() != null`, `!DynamicType.isDynamic(type)`
    - `ref.getParamSpecAt(i).isValidParamType(type.getValueType())`
    - Exactly one operand has `type instanceof BagOfValuesType` (`bag`), and for binary calls (`count == 2`) the other operand has `type instanceof ValueType` (`scalar`), recording `bag`, `scalar`, and `bagFirst`.
  - Any exception thrown by an extension `Expression.getEvaluatesTo()` during `HigherOrderCall.prepare` is caught (`catch (RuntimeException e) { return Optional.empty(); }`, lines 88–90).

### 2.2 Operand Evaluation Order & Failure Attribution

- **Left-to-right operand evaluation before body invocation**:
  - In `HigherOrderCall.evaluate` (`HigherOrderCall.java:93-102`):
    ```java
    Expression first = arguments.get(1).evaluate(context);
    if(arguments.size() == 2){
        return FunctionSpecBuilder.invokeHigherOrderSingle(operator, context, arguments, predicate, first, bagType);
    }
    Expression second = arguments.get(2).evaluate(context);
    return FunctionSpecBuilder.invokeHigherOrderPair(operator, context, arguments, predicate,
            first, second, scalarType, bagType, bagFirst);
    ```
  - This matches the exact evaluation order of `FunctionSpecImpl.invokeFast` (`FunctionSpecBuilder.java:654-661`), where `arguments.get(0)` (`FunctionReference`, which evaluates to `this`), `arguments.get(1)`, and `arguments.get(2)` are all evaluated **before** entering `HigherOrderFunctions`.
  - Specifically, when `bagFirst == false` and `first` (`arguments.get(1)`) evaluates to a value of the wrong runtime type while `second` (`arguments.get(2)`) throws an `EvaluationException` (e.g., `missing-attribute`), `second.evaluate(context)` still runs before the runtime type check on `first`, so `second`'s `EvaluationException` propagates rather than a premature `syntax-error` (`ApplyArgumentValidationTest.java:356-372`).
  - Conversely, if `first.evaluate(context)` throws an `EvaluationException`, `second.evaluate(context)` is not called.
- **Function / observer failure attribution (Design D34)**:
  - Because `first = arguments.get(1).evaluate(context)` and `second = arguments.get(2).evaluate(context)` execute inside `HigherOrderCall.evaluate(context)` **before** `FunctionSpecBuilder.invokeHigherOrderSingle` / `invokeHigherOrderPair` enters its `try { ... } catch (Exception e) { throw built.failed(context, e); }` block (`FunctionSpecBuilder.java:341-345, 352-358`), an operand evaluation failure is NOT wrapped in the outer `any-of`/`all-of` `FunctionSpec` and does NOT trigger `DecisionObservation.functionFailed` for `any-of`/`all-of`.
  - Once operand evaluation succeeds and `invokeHigherOrderSingle` / `invokeHigherOrderPair` is entered, any failure in the predicate or fallback validation is caught by `DefaultFunctionInvocation` (`throw new FunctionInvocationException(spec, invocationCause(e))`, `DefaultFunctionInvocation.java:210-212, 228-230`) and mapped by `built.failed(context, e)` (`FunctionSpecBuilder.java:343-345, 356-358`, calling `RootEvaluationContext.observationOf(context).functionFailed(context, this, failure)`).
  - Verified by `LaterDecidingOperandTest.higherOrderKeepsOperandAndBodyFailureAttribution` (`LaterDecidingOperandTest.java:129-164`).

### 2.3 Unchecked Fallback, Wrong Runtime Type, and Null Bags

- In `DefaultFunctionInvocation.invokeHigherOrderSingle` (`DefaultFunctionInvocation.java:203-214`):
  - The fast path `HigherOrderFunctions.invokeSingle(context, ref, bag, callShape == CallShape.ANY_OF)` runs **only** if `value instanceof BagOfValues bag && expectedBagType.equals(bag.getEvaluatesTo())`.
  - If `value` is `null`, not a `BagOfValues`, or a `BagOfValues` whose runtime `getEvaluatesTo()` differs from `expectedBagType`, execution falls through to `(BooleanVal)invokeFast(spec, context, arguments, ref, value, null, null)` using the **already-evaluated** `value` without re-evaluating `arguments.get(1)`.
- In `DefaultFunctionInvocation.invokeHigherOrderPair` (`DefaultFunctionInvocation.java:217-232`):
  - The fast path `HigherOrderFunctions.invokePair(context, ref, scalar, bag, bagFirst, callShape == CallShape.ANY_OF)` runs **only** if `bagValue instanceof BagOfValues bag && expectedBagType.equals(bag.getEvaluatesTo()) && scalarValue instanceof Value<?> scalar && expectedScalarType.equals(scalar.getEvaluatesTo())`.
  - If either evaluated operand is `null`, not a `BagOfValues` / `Value<?>`, or has a runtime `getEvaluatesTo()` that does not equal `expectedBagType` / `expectedScalarType`, execution falls through to `(BooleanVal)invokeFast(spec, context, arguments, ref, first, second, null)` using the **already-evaluated** `first` and `second` without re-evaluating `arguments.get(1)` or `arguments.get(2)`.
  - In `invokeFast`, `HigherOrderFunctions.anyOfSingle` / `anyOfPair` / `allOfSingle` / `allOfPair` runs `requireValidArguments`, which throws `EvaluationException(Status.syntaxError().build(), "Invalid higher-order function arguments")`, wrapped and reported as a failure of the outer `any-of` / `all-of` `FunctionSpec`.
  - Verified by `ApplyArgumentValidationTest.higherOrderChecksTheEvaluatedBagType` and `higherOrderChecksTheEvaluatedScalarAndNullBag` (`ApplyArgumentValidationTest.java:296-352`).

### 2.4 Lazy Operands

- `LogicalFunctions.and`, `or`, and `nOf` are not prepared by `HigherOrderCall` (`CallShape.AND`, `CallShape.OR`, `CallShape.NONE` return `false` from `isPreparedHigherOrderOperator`).
- When a prepared `any-of` or `all-of` `Apply` is an operand of a lazy function (`and`, `or`, `n-of`), the lazy function only calls `operand.evaluate(context)` when that operand is reached.
- If the predicate `FunctionSpec` referenced by `FunctionReference` in `any-of`/`all-of` is itself declared with `lazyArgEval()` (`evaluateParameters == true`), `HigherOrderFunctions.invokeSingle` and `invokePair` invoke `ref.invoke(context, ...)` with `Value<?>` instances (`bag.valueAt(i)` and `scalar`), which are already-evaluated `ValueExp`s whose `evaluate(context)` returns `this`—identical to `HigherOrderFunctions.anyOfSingle` / `anyOfPair`.

### 2.5 Runtime Parameter Validation (`context.isValidateFuncParamsAtRuntime()`)

- In `Apply.evaluate` (`Apply.java:288-298`):
  ```java
  if(callSite != null){
      if(callSite instanceof BoundCall bound){
          return bound.invoke(context, invocationArguments.get(1));
      }
      if(!context.isValidateFuncParamsAtRuntime()){
          return ((HigherOrderCall)callSite).evaluate(context);
      }
  }
  return listInvocation
          ?spec.invokeWithList(context, invocationArguments)
          :spec.invoke(context, invocationArguments.toArray(new Expression[0]));
  ```
- When `context.isValidateFuncParamsAtRuntime()` is `true`, `HigherOrderCall.evaluate` is bypassed and execution goes through `spec.invokeWithList(context, invocationArguments)` -> `FunctionSpecImpl.invokeFast` -> `doInvoke(context, arguments, true)` (`FunctionSpecBuilder.java:651-653, 743-769`), which normalizes a copy of the argument list and invokes `doValidateNormalizedParameters(normalizedArgs)` (including `SingleBagFunctionParametersValidator`) before evaluating operands and invoking the function.

### 2.6 Interaction with D45 Shared Condition Clones (`PreparedConditions`)

- In `PreparedConditions.rebuild` (`xacml-core/src/main/java/org/xacml4j/v30/policy/PreparedConditions.java:395-408`), when a rule condition contains a shareable subexpression inside an `Apply` (such as `any-of(string-equal, normalize(email), bag)`), `PreparedConditions` replaces the shared subexpression with `PreparedConditions.Shared` and rebuilds the enclosing `Apply` via `Apply.builder(apply.function()).params(rebuilt).build()`.
- `PreparedConditions.Shared.getEvaluatesTo()` (`PreparedConditions.java:536-539`) returns `XacmlTypes.STRING`.
- Because `HigherOrderCall.prepare` checks `argument.getEvaluatesTo()` rather than restricting operand AST classes, the cloned `Apply` produced by `PreparedConditions.rebuild` also receives a prepared `HigherOrderCall`.
- Verified by `SharedConditionValuesPdpTest.aSharedOperandInsideAHigherOrderConditionKeepsItsDecision` (`SharedConditionValuesPdpTest.java:161-180`).
- Furthermore, in `Rule.EngineOnly.of` (D47, `xacml-core/src/main/java/org/xacml4j/v30/policy/Rule.java:460-480`), `any-of` and `all-of` have `FunctionSpecBuilder.usesBuiltInInvocation(spec) == true` and `writesNoResponses() == true`, and their first argument is `FunctionReference`, which `Rule.EngineOnly.isEngineOnlyExpression` checks recursively via `isEngineOnlyFunction(ref.function())`. Adding `HigherOrderCall` to `Apply.callSite` does not alter `Rule.EngineOnly` classification.

### 2.7 Interaction with Regex-Bound Calls (`BoundCall`) and `Apply` Memory Layout

- In `Apply` (`Apply.java:81-82, 103-107, 288-295`), the former `private final BoundCall bound` field is replaced by `private final Object callSite`.
- Mutual exclusion at construction time (`Apply.java:103-107`):
  - `BoundCall` requires `arguments.size() == 2 && arguments.get(0) instanceof Value<?> literal`.
  - `HigherOrderCall` requires `arguments.get(0) instanceof FunctionReference`.
  - Because `FunctionReference` (`final`) does not implement `Value<?>`, `bound != null` and `HigherOrderCall.prepare(...).isPresent()` are mutually exclusive.
- Sharing the single reference field `callSite` avoids adding a reference field to `Apply`, keeping `Apply` at 40 bytes on 64-bit JDK 25 with compressed oops (12B header + 4B `int hashCode` + 1B `boolean listInvocation` + 3B padding + 5 × 4B reference fields = 40B; a 6th reference field would have crossed the 8-byte alignment boundary to 48B).
- When `string-regexp-match` is passed as the `FunctionReference` predicate to `any-of` or `all-of` (e.g., `any-of(string-regexp-match, pattern, bag)`), `HigherOrderFunctions.invokePair` calls `DecidingOperands.anyTrue(context, ref, scalar, bag, bagFirst)` (`xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DecidingOperands.java:132-160`), which calls `ref.invoke(context, scalar, value)` -> `FunctionSpecImpl.invokeDirect` -> `RegExpFunctions.Matching.stringRegexpMatch`, using the `Matching` instance's pattern cache as before (`BoundCall` only applies when `string-regexp-match` is the direct `Apply.spec` with a literal first argument).

---

## 3. Defects Found

None (`0` P1, `0` P2, `0` P3).

---

## 4. Supplied Evidence vs Unrun Gates

### 4.1 Supplied Test Evidence (from `REPORT-I-D52-terra.md`, not re-run in this read-only review lane)

- Targeted tests reported passing by `I-D52-terra` on `cod_1`:
  - `ApplyArgumentValidationTest`: 60 tests, 0 failures
  - `LaterDecidingOperandTest`: 76 tests, 0 failures
  - `SharedConditionValuesPdpTest`: 31 tests, 0 failures
- Full module suites reported passing by `I-D52-terra` on `cod_1`:
  - `xacml-core`: 1,353 tests, 0 failures
  - `xacml-pdp`: 161 tests, 0 failures

### 4.2 Unrun Gates (`UNRUN`)

Per review scope and `openspec/changes/optimize-pdp-evaluation/tasks.md` (`42.20`–`42.21`), performance and allocation acceptance cannot be claimed from test counts or static inspection and remain explicitly **UNRUN**:
1. **Controlled C2 JMH timing benchmark (`42.20`)**: `UNRUN` (`PdpDecideBenchmark.decide`, `-Djmh.corpus=PolicyBench`, `policyCount=100`, `rulesPerPolicy=10`, `-XX:-UseJVMCICompiler`, paired against the pre-D52 baseline on an idle machine).
2. **GC allocation rate & object count profiling (`42.20`)**: `UNRUN` (`-prof gc` B/op verification on higher-order policy evaluation).
3. **Async CPU profile verification (`42.20` / `42.21`)**: `UNRUN` (confirming removal of `HigherOrderFunctions.validateArguments` from hot `anyOfPair` / `allOfPair` / `anyOfSingle` / `allOfSingle` frames and recording the keep/revert decision in `design.md` and `evidence/README.md`).

---

## 5. Final Verdict

**APPROVE** — The D52 implementation in `d52-candidate/` (and `cod_1`) is sound, preserves all XACML 3.0 evaluation order, Indeterminate status, `DecisionObserver` attribution, `DynamicType` runtime assertion, `isValidateFuncParamsAtRuntime()`, `PreparedConditions` (D45) cloning, and `BoundCall` invariants, and keeps `Apply` object size unchanged. Ready for integration and controlled JMH benchmarking (`42.20`–`42.21`).
