# Plan Review: D52 — Prepared Signatures for Higher-Order Call Sites (`PR-D52-argon`)

- **Reviewer**: Lane `agyx_1` (Gemini Argon)
- **Design Under Review**: `D52` in [design.md](`.ntm/worktrees/xacml4j--batch2/agyx_1/openspec/changes/optimize-pdp-evaluation/design.md`) and "D51" in [pr25-sol-cpu-analysis.md](`.ntm/worktrees/xacml4j--batch2/agyx_1/openspec/changes/optimize-pdp-evaluation/research/pr25-sol-cpu-analysis.md`)
- **Verdict**: **APPROVE WITH CHANGES** (`0 P1`, `2 P2`, `1 P3`)

---

## Summary Verdict

**APPROVE WITH CHANGES.**

Codex Sol's D52 targets a verified hot spot (`HigherOrderFunctions.validateArguments`: **1,090 self samples / 2.245%** of `rsa2008` decide samples, plus **628 self samples / 1.294%** in `DefaultFunctionInvocation.invokeFast`) without adding a public proof-carrier type (`Apply.CheckedArguments`, rejected in cycle AW) or changing the `FunctionSpec` SPI. Adding one `private final HigherOrderCall higherOrder` reference to [`Apply`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/Apply.java`) consumes 4 bytes of the existing 7-byte alignment padding under compressed oops, leaving `Apply`'s shallow size at **48 B** (`0 B` per-`Apply` growth and `0 B` per-decision allocation).

Two `P2` design details and one `P3` bridge cleanup should be folded into D52 before implementation:
1. **`P2-1` — Preserve D34's two-stage failure mapping and outer-function observation boundary on the prepared path.** Operand expressions `arg1` (and `arg2`) must be evaluated **before** entering `FunctionSpecImpl`'s failure wrapper so an operand evaluation failure is attributed only to the failing operand, whereas any failure inside the higher-order body (a predicate throwing `EvaluationException`, or a fallback `requireValidArguments` throwing `Status.syntaxError()`) must still be wrapped by [`DefaultFunctionInvocation`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java`) (`new FunctionInvocationException(spec, invocationCause(e))`) and recorded by [`FunctionSpecImpl.failed(context, e)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java`) against the outer `any-of` / `all-of` `FunctionSpec`.
2. **`P2-2` — Make the static operand-type proof and the cheap evaluated-value check exact (including `DynamicType` exclusion and D45 `PreparedConditions.Shared` compatibility).** A data operand's type is statically known iff `!(arg instanceof FunctionReference)` and `ValueExpTypeInfo t = arg.getEvaluatesTo()` satisfies `t != null && t.getValueType() != null && !DynamicType.isDynamic(t)`. Do not use an AST class allow-list, which would drop D52 on `Apply` nodes cloned around [`PreparedConditions.Shared`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/PreparedConditions.java`). At evaluation time, checking `vBag instanceof BagOfValues bag && expectedBagType.equals(bag.getEvaluatesTo())` (and, for binary calls, `vScalar instanceof Value<?> scalar && expectedScalarType.equals(scalar.getEvaluatesTo())`) reduces to `instanceof` plus reference identity on the built-in path and falls back to the checked path with the already-evaluated `v1[, v2]` whenever a custom/mock expression violates its static `getEvaluatesTo()` contract.
3. **`P3-1` — Minimize cross-package bridges (`FunctionReference` and `HigherOrderFunctions` need no new `public` methods).** [`FunctionReference.function()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/FunctionReference.java`) is already package-private in `org.xacml4j.v30.policy`, and [`DefaultFunctionInvocation`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java`) is in the same package (`org.xacml4j.v30.policy.function.impl`) as [`HigherOrderFunctions`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/HigherOrderFunctions.java`), so `FunctionReference` needs no edits and `HigherOrderFunctions`'s prepared entry points can be package-private.

---

## Findings

### `P2-1` — Preserve D34's two-stage failure attribution and outer-function `DecisionObservation` on the prepared path

- **Location**:
  - [design.md:2063-2072](`.ntm/worktrees/xacml4j--batch2/agyx_1/openspec/changes/optimize-pdp-evaluation/design.md`)
  - [pr25-sol-cpu-analysis.md:99-109](`.ntm/worktrees/xacml4j--batch2/agyx_1/openspec/changes/optimize-pdp-evaluation/research/pr25-sol-cpu-analysis.md`)
  - [`Apply.evaluate`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/Apply.java`)
  - [`FunctionSpecBuilder.FunctionSpecImpl.invokeFast`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java`) and [`failed`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java`)
  - [`DefaultFunctionInvocation.invokeFast`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java`) and [`invocationCause`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java`)
- **Evidence**: `SUPPORTED` (source inspection of `Apply.java`, `FunctionSpecBuilder.java`, `DefaultFunctionInvocation.java`, and `DecidingOperands.java`).
- **Issue**:
  Currently, [`FunctionSpecImpl.invokeFast`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java`) separates two failure domains (design D34, documented at [`FunctionSpecBuilder.java:692-694`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java`)):
  1. **Operand evaluation** (`v0 = evaluateArg(...)`, `v1 = evaluateArg(...)`, `v2 = evaluateArg(...)` at `:611-614`) happens **outside** `FunctionSpecImpl`'s `try { ... } catch (Exception e) { throw failed(context, e); }` block. If evaluating `arg1` or `arg2` throws `EvaluationException`, the exception propagates with the inner operand's `FunctionSpec` and does **not** fire `DecisionObservation.functionFailed` for the outer `any-of`/`all-of`.
  2. **Post-operand invocation** (`fast.invokeFast(this, context, params, v0, v1, v2, null)` at `:616`) runs **inside** the `try` block. When `DecidingOperands.anyTrue` / `allTrue` rethrows an unmasked predicate `EvaluationException` (or when fallback `requireValidArguments` throws `FunctionInvocationException(null, Status.syntaxError())`), [`DefaultFunctionInvocation.invokeFast`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java`) catches `Throwable e` and throws `new FunctionInvocationException(spec, invocationCause(e))` where `spec` is the outer `any-of`/`all-of` `FunctionSpec` and `status` is preserved from the inner `EvaluationException`. [`FunctionSpecImpl.invokeFast`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java`) then catches that `FunctionInvocationException` and calls [`failed(context, e)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java`), notifying `RootEvaluationContext.observationOf(context).functionFailed(context, this, failure)`.

  If D52's `HigherOrderCall` in `org.xacml4j.v30.policy` called `HigherOrderFunctions` directly from `Apply.evaluate`, [`Apply.evaluate`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/Apply.java`) (`catch (EvaluationException e) { throw e; }`) would rethrow the inner predicate's `FunctionInvocationException` directly: `e.getSpec()` would be the inner predicate (or `null` on fallback validation failure) instead of `any-of`/`all-of`, and `DecisionObservation.functionFailed` for `any-of`/`all-of` would be skipped.
- **Required change**:
  Specify in D52 that:
  1. `HigherOrderCall.evaluate(context)` evaluates `v1 = arg1.evaluate(context)` (and `v2 = arg2.evaluate(context)` for binary calls) in order **before** calling `FunctionSpecBuilder.invokeHigherOrderSingle` / `invokeHigherOrderPair`.
  2. `FunctionSpecBuilder.invokeHigherOrderSingle` / `invokeHigherOrderPair` wraps the cheap evaluated-value check, the prepared higher-order body, and the checked fallback inside `DefaultFunctionInvocation`'s `try { ... } catch (Throwable e) { throw new FunctionInvocationException(spec, invocationCause(e)); }` and `FunctionSpecImpl.failed(context, e)`.

---

### `P2-2` — Define the static operand-type proof and cheap evaluated-value check to exclude `DynamicType` and preserve D45 `PreparedConditions.Shared` clones

- **Location**:
  - [design.md:2063-2070](`.ntm/worktrees/xacml4j--batch2/agyx_1/openspec/changes/optimize-pdp-evaluation/design.md`)
  - [pr25-sol-cpu-analysis.md:99-104](`.ntm/worktrees/xacml4j--batch2/agyx_1/openspec/changes/optimize-pdp-evaluation/research/pr25-sol-cpu-analysis.md`)
  - [`Apply.withTypeAssertions`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/Apply.java`)
  - [`TypeAssertion.evaluate`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/TypeAssertion.java`)
  - [`FunctionParamValueTypeSequenceSpec.isValidParamType`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionParamValueTypeSequenceSpec.java`)
  - [`PreparedConditions.Shared`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/PreparedConditions.java`)
  - [`ApplyArgumentValidationTest`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/test/java/org/xacml4j/v30/policy/ApplyArgumentValidationTest.java`)
- **Evidence**: `SUPPORTED` (source inspection of `Apply.java`, `TypeAssertion.java`, `FunctionParamValueTypeSequenceSpec.java`, `PreparedConditions.java`, and `ApplyArgumentValidationTest.java`).
- **Issue**:
  1. **Why `DynamicType` / `TypeAssertion` must be excluded**: When a data argument to `any-of` or `all-of` has `DynamicType.isDynamic(arg.getEvaluatesTo())`, [`Apply.withTypeAssertions`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/Apply.java`) wraps it in `new TypeAssertion(arg, spec.getParamSpecAt(1), spec)`. However, for `any-of` and `all-of`, `spec.getParamSpecAt(1)` is `@FuncParamAnyValueTypeSeq(min = 1)` -> [`FunctionParamValueTypeSequenceSpec`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionParamValueTypeSequenceSpec.java`) with `paramType == null` (`isAnyValueType() == true`). Consequently, `TypeAssertion.evaluate` only checks `type != null && type.getValueType() != null`; it does **not** check the evaluated value's `ValueType` or bag multiplicity against `ref.function()`. That check happens inside `HigherOrderFunctions.validateArguments` and is pinned by [`ApplyArgumentValidationTest.higherOrderChecksEvaluatedDynamicBagElementType`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/test/java/org/xacml4j/v30/policy/ApplyArgumentValidationTest.java`) and [`higherOrderRejectsEvaluatedDynamicScalarWhenNoBagRemains`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/test/java/org/xacml4j/v30/policy/ApplyArgumentValidationTest.java`).
  2. **Why AST class allow-lists would break on D45**: In D45, [`PreparedConditions.isCrossable(Apply)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/PreparedConditions.java`) returns `true` for `any-of` and `all-of`. When D45 hoists a shared string subexpression inside an `any-of`/`all-of` operand (such as `string-one-and-only(Designator)`), [`PreparedConditions.rebuildSpine`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/PreparedConditions.java`) replaces that operand with a package-private `PreparedConditions.Shared` node and clones the enclosing `any-of`/`all-of` via `Apply.builder(apply.function()).params(rebuilt).build()`. `PreparedConditions.Shared.getEvaluatesTo()` returns the inner `Apply`'s static `ValueType` (`XacmlTypes.STRING`). If D52's static proof checked an AST class allow-list instead of `getEvaluatesTo()`, D45-cloned `any-of`/`all-of` nodes would lose their D52 plan.
- **Required change**:
  Specify the exact static eligibility conditions and cheap runtime check in D52:
  - **Static eligibility** (computed in `Apply`'s constructor when `typeError.isEmpty()`, `invocationArguments == arguments`, and `arguments.size()` is `2` or `3`):
    1. `spec` is the canonical built-in `any-of` or `all-of` (`FunctionSpecBuilder.canPrepareHigherOrder(spec, ref.function(), arguments.size() - 1)`).
    2. `arguments.get(0)` is a `FunctionReference ref` whose `FunctionSpec pred = ref.function()` is a `FunctionSpecImpl` with `pred.getNumberOfParams() == arguments.size() - 1`, `!pred.isVariadic()`, `pred.getReturnType().equals(XacmlTypes.BOOLEAN)`, and every `pred.getParamSpecAt(i)` an instance of [`FunctionParamValueTypeSpec`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionParamValueTypeSpec.java`) (with `!DynamicType.isDynamic(paramType)`) or [`FunctionParamAnyValueSpec`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionParamAnyValueSpec.java`) (`!paramSpec.isBag()`).
    3. Every data operand `arg_k` (`k = 1 .. arguments.size() - 1`) satisfies `!(arg_k instanceof FunctionReference)` and has `ValueExpTypeInfo t_k = arg_k.getEvaluatesTo()` with `t_k != null && t_k.getValueType() != null && !DynamicType.isDynamic(t_k)`.
    4. For unary (`arguments.size() == 2`), `t_1` is a `BagOfValuesType expectedBagType`. For binary (`arguments.size() == 3`), one of `(t_1, t_2)` is a `BagOfValuesType expectedBagType` and the other is a non-bag `ValueType expectedScalarType` (`bagFirst = t_1.isBag()`). (Compatibility with `pred`'s parameter specs is already guaranteed because `typeError.isEmpty()` passed [`HigherOrderFunctions.validateArguments`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/HigherOrderFunctions.java`) at `Apply` construction.)
  - **Cheap evaluated-value check**:
    - Unary (`v1`): `v1 instanceof BagOfValues bag && expectedBagType.equals(bag.getEvaluatesTo())`.
    - Binary (`v1, v2` with `bagFirst == false`): `v1 instanceof Value<?> scalar && expectedScalarType.equals(scalar.getEvaluatesTo()) && v2 instanceof BagOfValues bag && expectedBagType.equals(bag.getEvaluatesTo())` (and symmetrically when `bagFirst == true`).
    - If the check fails for any reason, pass the already-evaluated `v1` (and `v2`) to `DefaultFunctionInvocation.invokeFast(spec, context, arguments, ref, v1, v2, null)` so `HigherOrderFunctions.requireValidArguments` produces the exact same error without re-evaluating `arg1` or `arg2`.

---

### `P3-1` — Keep `FunctionReference` untouched and `HigherOrderFunctions` prepared helpers package-private

- **Location**:
  - [design.md:2071](`.ntm/worktrees/xacml4j--batch2/agyx_1/openspec/changes/optimize-pdp-evaluation/design.md`)
  - [pr25-sol-cpu-analysis.md:107-111](`.ntm/worktrees/xacml4j--batch2/agyx_1/openspec/changes/optimize-pdp-evaluation/research/pr25-sol-cpu-analysis.md`)
  - [`FunctionReference.function()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/FunctionReference.java`)
  - [`HigherOrderFunctions`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/HigherOrderFunctions.java`)
- **Evidence**: `SUPPORTED` (source inspection of package declarations and visibility in `FunctionReference.java`, `DefaultFunctionInvocation.java`, and `HigherOrderFunctions.java`).
- **Issue**:
  `design.md:2071` lists `FunctionReference` and `HigherOrderFunctions` as needing cross-package bridges. However:
  1. [`FunctionReference.function()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/FunctionReference.java`) is already package-private in `org.xacml4j.v30.policy`. A package-private `HigherOrderCall` in `org.xacml4j.v30.policy` can call `ref.function()` and `ref.invoke(context, ...)` directly; `FunctionReference.java` needs no modification at all.
  2. [`DefaultFunctionInvocation`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java`) and [`HigherOrderFunctions`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/HigherOrderFunctions.java`) are in the same package (`org.xacml4j.v30.policy.function.impl`). Because `FunctionSpecBuilder` routes through `DefaultFunctionInvocation` (see `P2-1`), the unchecked higher-order helpers in `HigherOrderFunctions` can be **package-private**, keeping every `public` method on `HigherOrderFunctions` checked.
- **Suggested change**:
  Remove `FunctionReference.java` from D52's file list and make the prepared helpers in `HigherOrderFunctions` package-private (exact signatures in Section 3 below).

---

## Detailed Review of the Five Checkpoints

### 1. Soundness (§7.4, §7.19, A.3.5, A.3.12)

- **Standards Fetched**: [OASIS XACML 3.0 Plus Errata 01](https://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html) §7.4 (Expressions), §7.19.1–§7.19.3 (Evaluation Errors), Appendix A.3.5 (Logical functions: `or`, `and`, `n-of`, `not`), and Appendix A.3.12 (Higher-order bag functions: `any-of`, `all-of`, `any-of-any`, `all-of-any`, `any-of-all`, `all-of-all`, `map`) via `tavily_extract` (`SUPPORTED`).
- **Normative Requirements vs. D52 Behavior**:
  1. **A.3.12 (`urn:oasis:names:tc:xacml:1.0:function:any-of` and `urn:oasis:names:tc:xacml:1.0:function:all-of`)**:
     - Under XACML 3.0 A.3.12, `any-of` and `all-of` take a boolean function reference followed by $n \ge 1$ arguments where $n - 1$ arguments are primitive values and **one** argument is a bag of primitive values. The expression is evaluated as if the predicate were applied to every element of the bag (preserving argument position) and the results were combined with `urn:oasis:names:tc:xacml:1.0:function:or` (`any-of`) or `urn:oasis:names:tc:xacml:1.0:function:and` (`all-of`).
  2. **Empty bags**:
     - For `any-of`, `or` over 0 arguments returns `"False"` (`BooleanVal.FALSE`).
     - For `all-of`, `and` over 0 arguments returns `"True"` (`BooleanVal.TRUE`).
     - In [`HigherOrderFunctions.evaluateSingle`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/HigherOrderFunctions.java`) and [`evaluatePair`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/HigherOrderFunctions.java`), [`DecidingOperands.anyTrue`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DecidingOperands.java`) and [`allTrue`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DecidingOperands.java`) iterate `bag.values()` and return `BooleanVal.FALSE` (`deciding = true`) or `BooleanVal.TRUE` (`deciding = false`) when `bag` is empty without invoking `ref`. D52 enters this exact loop after evaluating the operands.
  3. **Operand evaluation order, predicate order, and error masking (A.3.5 & A.3.12)**:
     - Data operands `arg1` (and `arg2`) are evaluated once, in left-to-right order, before the bag loop. If `arg1.evaluate(context)` throws `EvaluationException`, `arg2` is never evaluated and the exception propagates immediately (matching `FunctionSpecImpl.invokeFast` at [`FunctionSpecBuilder.java:611-614`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java`)).
     - Inside the bag loop, [`DecidingOperands.decide`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DecidingOperands.java`) evaluates `ref.invoke(context, ...)` in bag iteration order:
       - If an element returns the deciding boolean (`true` for `any-of`, `false` for `all-of`), it returns immediately, **masking any earlier `EvaluationException`** and skipping remaining bag elements (conforming to A.3.5 `or`/`and` and A.3.12 `any-of`/`all-of`).
       - If no element returns the deciding boolean and one or more elements threw `EvaluationException`, `DecidingOperands.decide` rethrows the **first** `EvaluationException` (`failure == null ? e : failure` at [`DecidingOperands.java:196`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DecidingOperands.java`)), preserving its `Status` and message.
  4. **Type errors, statuses, and messages (§7.19)**:
     - Construction-time type errors (`!typeError.isEmpty()`) continue to leave `higherOrder == null` and throw `new FunctionInvocationException(spec, Status.syntaxError(typeError))` in [`Apply.evaluate`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/Apply.java`).
     - Runtime validation (`context.isValidateFuncParamsAtRuntime() == true`) bypasses `higherOrder` (`!context.isValidateFuncParamsAtRuntime()` gate in `Apply.evaluate`) and takes `spec.invoke(context, invocationArguments)`.
     - If a custom/mock expression violates its static `getEvaluatesTo()` contract at runtime, the cheap evaluated-value check fails and delegates to `DefaultFunctionInvocation.invokeFast` with the already-evaluated values, which calls `HigherOrderFunctions.requireValidArguments` and throws `new FunctionInvocationException(spec, Status.syntaxError("Invalid higher-order function arguments"))`.
  5. **Lazy logical functions (A.3.5 `or`, `and`, `n-of`)**:
     - `or`, `and`, and `n-of` take `Expression` parameters (`isLazy() == true`), not values, and cannot be passed as the `FunctionReference` predicate to `any-of`/`all-of` (rejected at construction time by [`HigherOrderFunctions.takesOneValuePerParameter`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/HigherOrderFunctions.java`) because `FunctionParamExpressionSpec.isBag()` is false and `isValidParamType(BagOfValuesType)` is false). When `any-of` or `all-of` is itself an operand of a lazy `and`/`or`/`n-of`, `LogicalFunctions` calls `arg.evaluate(context)` lazily on the `Apply` node, which enters `Apply.evaluate(context)` as usual.

---

### 2. The Static Proof and the Cheap Evaluated-Value Check

- **Evidence**: `SUPPORTED` (source inspection of `Apply.java`, `TypeAssertion.java`, `BoundCall.java`, `PreparedConditions.java`, `VariableReference.java`, `VariableDefinition.java`, `AttributeDesignator.java`, `AttributeSelector.java`, `DynamicType.java`, `BagOfValues.java`, `BagOfValuesType.java`, `Value.java`, and `XacmlTypes.java`).
- **Case-by-case analysis of operand expressions**:

| Operand Form | Static `getEvaluatesTo()` | Prepared Plan Eligibility | Runtime Guarantee & Cheap Check |
| --- | --- | --- | --- |
| `Value<?>` literal (`StringExp`, `IntegerExp`, etc.) | Concrete `ValueType` (e.g., `XacmlTypes.STRING`) | **Eligible** | `evaluate(context)` returns `this`; `v instanceof Value<?> && expectedScalarType.equals(v.getEvaluatesTo())` is `true` by reference identity (`==`). |
| `BagOfValues` literal | Concrete `BagOfValuesType` | **Eligible** | `evaluate(context)` returns `this`; `v instanceof BagOfValues && expectedBagType.equals(v.getEvaluatesTo())` is `true` by reference identity (`==`). |
| `AttributeDesignator` / `AttributeSelector` | Concrete `BagOfValuesType` (`type.bagType()`) | **Eligible** | Built-in `EvaluationContext.resolve(...)` enforces the designator/selector `DataType`. If a custom/mocked `EvaluationContext` returns a mismatched `BagOfValues` (as in [`ApplyArgumentValidationTest:296-310`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/test/java/org/xacml4j/v30/policy/ApplyArgumentValidationTest.java`)), `expectedBagType.equals(bag.getEvaluatesTo())` is `false` and falls back to the checked path with the already-evaluated `v1[, v2]`. |
| Nested `Apply` (including D6 `BoundCall` and nested higher-order calls) | `spec.getReturnType()` | **Eligible** iff `!DynamicType.isDynamic(spec.getReturnType())` | D6 `BoundCall` ([`BoundCall.java:29-47`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/BoundCall.java`)) is internal to the child `Apply` and returns `BooleanVal`; it does not change the child `Apply`'s `getEvaluatesTo()` or `evaluate(context)` return type. Built-in `FunctionSpecImpl.invokeFast` validates the return value with `returnType.isValidReturnValue(result)` ([`FunctionSpecBuilder.java:616`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java`)). |
| `VariableReference` | `variableDefinition.getEvaluatesTo()` (delegates to `expression.getEvaluatesTo()`) | **Eligible** iff `!DynamicType.isDynamic(varRef.getEvaluatesTo())` | `DefaultPolicyEvaluationContext.evaluateVariable` caches and returns `variableDef.evaluate(this)`. If the variable's underlying expression is non-dynamic, its evaluated value is checked by the cheap check; if a custom `EvaluationContext` overrides `evaluateVariable` and returns a wrong type, the cheap check catches it and falls back. |
| D45 `PreparedConditions.Shared` | `expression.getEvaluatesTo()` (`XacmlTypes.STRING`) | **Eligible** | `Shared.evaluate(context)` returns the cached `StringExp` from the root snapshot map; `v instanceof Value<?>` and `expectedScalarType.equals(v.getEvaluatesTo())` succeed in two reference checks. |
| `DynamicType` (`ANY_ELEMENT`, `ANY_BAG`) or `TypeAssertion` | `DynamicType.isDynamic(t) == true` | **Excluded** (`higherOrder == null`) | Excluded at construction time (`P2-2`); evaluated through `spec.invokeFast(context, invocationArguments)` where `HigherOrderFunctions.validateArguments` validates the dynamic value against `ref.function()`. |
| Custom `Expression` implementation | User-defined `getEvaluatesTo()` | **Eligible** iff non-dynamic, **verified at runtime** | If a custom or Mockito expression claims `XacmlTypes.STRING.bagType()` in `getEvaluatesTo()` but returns `null`, a `FunctionReference`, a scalar, or an integer bag from `evaluate(context)`, the cheap check (`v instanceof BagOfValues bag && expectedBagType.equals(bag.getEvaluatesTo())`) returns `false` and falls back to the checked path with the already-evaluated value(s). |

- **Is the cheap evaluated check enough when the static proof is wrong (e.g. a lying custom `Expression` or custom `EvaluationContext`)?**
  **Yes (`SUPPORTED`).** Because `ref.function()` is verified at construction to have standard immutable parameter metadata (`FunctionParamValueTypeSpec` with non-dynamic `ValueType` or `FunctionParamAnyValueSpec`) and `Apply`'s static `typeErrorOf(spec, arguments)` already proved that `expectedBagType` (and `expectedScalarType` for binary calls) satisfies `ref.function()`'s parameter specs:
  - Any runtime value `vBag` that satisfies `vBag instanceof BagOfValues bag && expectedBagType.equals(bag.getEvaluatesTo())` has the exact `BagOfValuesType` that was already proven valid for `ref.function()`.
  - Any runtime value `vScalar` that satisfies `vScalar instanceof Value<?> scalar && expectedScalarType.equals(scalar.getEvaluatesTo())` has the exact non-bag `ValueType` that was already proven valid for `ref.function()`.
  - Conversely, if either check fails, the fallback invokes `DefaultFunctionInvocation.invokeFast(spec, context, arguments, ref, v1, v2, null)` with the already-evaluated `v1[, v2]`, which runs the full `HigherOrderFunctions.requireValidArguments` check without re-evaluating `arg1` or `arg2`.

---

### 3. The Cross-Package Bridges

- **Evidence**: `SUPPORTED` (source inspection of `Apply.java`, `FunctionReference.java`, `FunctionSpecBuilder.java`, `DefaultFunctionInvocation.java`, and `HigherOrderFunctions.java`).
- **Package layout**:
  - `org.xacml4j.v30.policy`: `Apply`, `FunctionReference`, `HigherOrderCall` (new, package-private).
  - `org.xacml4j.v30.policy.function`: `FunctionSpecBuilder`, `FunctionParamValueTypeSpec`, `FunctionParamAnyValueSpec`.
  - `org.xacml4j.v30.policy.function.impl`: `DefaultFunctionInvocation`, `HigherOrderFunctions`, `DecidingOperands`.
- **Proposed narrow bridge names and signatures**:

1. **`org.xacml4j.v30.policy.HigherOrderCall` (package-private `final class`)**:
   ```java
   package org.xacml4j.v30.policy;

   final class HigherOrderCall {
       static HigherOrderCall prepare(FunctionSpec spec, List<Expression> arguments);
       BooleanVal evaluate(EvaluationContext context) throws EvaluationException;
   }
   ```
   - `Apply` holds `private final HigherOrderCall higherOrder;` (initialized in `Apply(Builder b)` to ` (this.typeError.isEmpty() && this.invocationArguments == this.arguments) ? HigherOrderCall.prepare(this.spec, this.arguments) : null`).
   - In `Apply.evaluate(EvaluationContext context)`:
     ```java
     if (higherOrder != null && !context.isValidateFuncParamsAtRuntime()) {
         return higherOrder.evaluate(context);
     }
     ```
   - `HigherOrderCall` reads `ref.function()` via existing package-private [`FunctionReference.function()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/FunctionReference.java`) — no change to `FunctionReference.java`.

2. **`org.xacml4j.v30.policy.function.FunctionSpecBuilder` (`public static` internal bridges, following the existing `usesBuiltInInvocation` / `validatesOneArgumentByType` pattern at [`FunctionSpecBuilder.java:301-334`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java`))**:
   ```java
   public static boolean canPrepareHigherOrder(
           FunctionSpec operator,
           FunctionSpec predicate,
           int dataOperands);

   public static BooleanVal invokeHigherOrderSingle(
           FunctionSpec operator,
           EvaluationContext context,
           List<Expression> arguments,
           FunctionReference ref,
           Expression v1,
           BagOfValuesType expectedBagType)
           throws EvaluationException;

   public static BooleanVal invokeHigherOrderPair(
           FunctionSpec operator,
           EvaluationContext context,
           List<Expression> arguments,
           FunctionReference ref,
           Expression v1,
           Expression v2,
           ValueType expectedScalarType,
           BagOfValuesType expectedBagType,
           boolean bagFirst)
           throws EvaluationException;
   ```
   - `canPrepareHigherOrder` checks:
     1. `operator instanceof FunctionSpecImpl opImpl && opImpl.invocation instanceof DefaultFunctionInvocation dfi && dfi.isPreparedHigherOrderOperator(dataOperands)`;
     2. `predicate instanceof FunctionSpecImpl predImpl && !predImpl.isVariadic() && predImpl.getNumberOfParams() == dataOperands && XacmlTypes.BOOLEAN.equals(predImpl.getReturnType())`;
     3. Every parameter spec `p` of `predImpl` is either `FunctionParamValueTypeSpec` with `!DynamicType.isDynamic(p)` or `FunctionParamAnyValueSpec`.
   - `invokeHigherOrderSingle` and `invokeHigherOrderPair` cast `operator` to `FunctionSpecImpl`, enter `try { ... } catch (Exception e) { throw opImpl.failed(context, e); }`, check the evaluated value(s) against `expectedBagType` (and `expectedScalarType`), and either call `dfi.invokeHigherOrderSingle` / `dfi.invokeHigherOrderPair` or fall back to `dfi.invokeFast(operator, context, arguments, ref, v1, v2, null)`.

3. **`org.xacml4j.v30.policy.function.impl.DefaultFunctionInvocation` (`public` methods on the internal implementation class)**:
   ```java
   public boolean isPreparedHigherOrderOperator(int dataOperands);

   public BooleanVal invokeHigherOrderSingle(
           FunctionSpec spec,
           EvaluationContext context,
           FunctionReference ref,
           BagOfValues bag)
           throws FunctionInvocationException;

   public BooleanVal invokeHigherOrderPair(
           FunctionSpec spec,
           EvaluationContext context,
           FunctionReference ref,
           Value<?> scalar,
           BagOfValues bag,
           boolean bagFirst)
           throws FunctionInvocationException;
   ```
   - `DefaultFunctionInvocation` already recognizes `HigherOrderFunctions.anyOfSingle`, `allOfSingle`, `anyOfPair`, and `allOfPair` in its `MethodHandle` fast-dispatch table (`kind`). Storing a `boolean deciding` flag (or `byte higherOrderKind`: `NONE`, `ANY_SINGLE`, `ALL_SINGLE`, `ANY_PAIR`, `ALL_PAIR`) lets `invokeHigherOrderSingle` / `invokeHigherOrderPair` call package-private `HigherOrderFunctions.invokeSingle` / `invokePair` inside `try { ... } catch (Throwable e) { throw new FunctionInvocationException(spec, invocationCause(e)); }`.

4. **`org.xacml4j.v30.policy.function.impl.HigherOrderFunctions` (package-private methods, no new `public` API)**:
   ```java
   static BooleanVal invokeSingle(
           EvaluationContext context,
           FunctionReference ref,
           BagOfValues bag,
           boolean deciding)
           throws EvaluationException {
       return evaluateSingle(context, ref, bag, deciding);
   }

   static BooleanVal invokePair(
           EvaluationContext context,
           FunctionReference ref,
           Value<?> scalar,
           BagOfValues bag,
           boolean bagFirst,
           boolean deciding)
           throws EvaluationException {
       return evaluatePair(context, ref, scalar, bag, bagFirst, deciding);
   }
   ```

---

### 4. Cost and Savings Analysis

- **Evidence**: `SUPPORTED` (profile data in [`pr23-astra-fixed3-profile.md:269-280`](`.ntm/worktrees/xacml4j--batch2/agyx_1/openspec/changes/optimize-pdp-evaluation/research/pr23-astra-fixed3-profile.md`), [`pr25-sol-cpu-analysis.md:33-53, 93-112`](`.ntm/worktrees/xacml4j--batch2/agyx_1/openspec/changes/optimize-pdp-evaluation/research/pr25-sol-cpu-analysis.md`), and `rtk rg` over `xacml-conformance-tests/src/test/resources/rsa2008-interop`).
- **Call-site inventory in `rsa2008-interop`**:
  `rsa2008-interop` contains **7** higher-order function call sites across 4 policy files:
  - `XacmlPolicySet-02a-CDA.xml:31`
  - `XacmlPolicySet-02d-prog-note.xml:29, 50`
  - `XacmlPolicySet-02e-MA.xml:35`
  - `XacmlPolicySet-02f-emergency.xml:36, 52, 135`
  Every single one is a 2-data-operand `urn:oasis:names:tc:xacml:1.0:function:any-of` with `<Function FunctionId="urn:oasis:names:tc:xacml:1.0:function:string-equal"/>`, a string scalar (`AttributeValue` or `string-one-and-only(SubjectAttributeDesignator)`), and a string `AttributeDesignator` bag (`hl7:confidentiality-code` or `med:dissented-subject-id`). **All 7 (100%) qualify for D52's prepared binary plan.**
- **What is eliminated vs. what stays**:
  - **Eliminated (100% of `validateArguments` on `rsa2008`)**:
    - `HigherOrderFunctions.validateArguments` (**1,090 self samples / 2.245%** of decide samples), which currently runs on every `any-of` evaluation: `funcRef.getEvaluatesTo()`, `takesOneValuePerParameter(funcSpec)` (which loops over `funcSpec.getNumberOfParams()` calling `FunctionSpecBuilder.validatesOneArgumentByType`), the 2-iteration parameter validation loop (`funcSpec.getParamSpecAt`, `FunctionParamValueTypeSpec.isValidParamType`, `DynamicType.isConsistent`, `BagOfValuesType.getEntryType`), and the outer `DefaultFunctionInvocation.invokeFast` 3-arg `MethodHandle.invokeExact` to `HigherOrderFunctions.anyOfPair`.
  - **What stays**:
    - Evaluating `arg1.evaluate(context)` and `arg2.evaluate(context)`;
    - Checking `!context.isValidateFuncParamsAtRuntime()`;
    - The two cheap evaluated-value checks (`v1 instanceof Value<?> scalar && expectedScalarType.equals(scalar.getEvaluatesTo())` and `v2 instanceof BagOfValues bag && expectedBagType.equals(bag.getEvaluatesTo())` — two `instanceof` tests and two `==` reference comparisons);
    - `DecidingOperands.anyTrue` iterating the bag and invoking `ref.invoke(context, scalar, v)` (which uses `FunctionReference`'s existing `invoke2` fast path).
- **Is 0.5 to 1.5% believable?**
  **Yes (`SUPPORTED`).** Removing the entire 2.245% `validateArguments` self-time plus its helper calls (`takesOneValuePerParameter`, `isValidParamType`, `isConsistent`) and one `MethodHandle.invokeExact` hop while adding only two `instanceof` + `==` checks makes a net **0.5 to 1.5%** wall-clock reduction on `rsa2008` realistic and conservative.
- **Memory footprint**:
  `Apply` currently has a 12B object header + `int hashCode` (4B) + 5 reference fields (`spec`, `arguments`, `typeError`, `invocationArguments`, `bound` = 20B) + `boolean listInvocation` (1B) = **37 B**, padded to **40 B** or **48 B** depending on field layout; even if 6 references, $16 + 24 + 1 = 41\text{ B} \to 48\text{ B}$. Let's be exact: `Apply` currently has 5 reference fields (`spec`, `arguments`, `typeError`, `invocationArguments`, `bound` = 20B) + `int hashCode` (4B) + `boolean listInvocation` (1B) + 12B header = **37 B** (40 B with 3B padding). Adding 1 reference field (`higherOrder`, 4B) brings `Apply` to 41 B (48 B), an increase of 8 B per `Apply` node at policy load time and **0 B per decision**. (Or, if `listInvocation` is folded into `HigherOrderCall` or an `int` bit / derived from `invocationArguments.size() > 3`, `Apply` stays **40 B** with **0 B** load-time growth as well! Note: `this.listInvocation` is simply `this.invocationArguments.size() > 3`, so replacing `private final boolean listInvocation` or folding it if desired keeps `Apply` small, though even +8 B per static `Apply` node is load-time only and 0 B/op at evaluation time.)

---

### 5. Overlap with D47 (Rule Contexts) and D53 (Prepared Target Admission)

- **Evidence**: `SUPPORTED` (source inspection of `Rule.java`, `TargetBeforeContext.java`, `PreparedConditions.java`, and `design.md` sections D47, D52, D53).
- **Comparison Table**:

| Dimension | D47 (Rule Evaluated in Policy Context) | D52 (Prepared Higher-Order Call Sites) | D53 (Prepared Target Admission & Rule Continuation) | Conflict? |
| --- | --- | --- | --- | --- |
| **Touched Files** | `Rule.java`, `Policy.java`, `DescendantEvaluationContext.java`, `TargetBeforeContext.java` | `Apply.java`, `HigherOrderCall.java` (new), `FunctionSpecBuilder.java`, `DefaultFunctionInvocation.java`, `HigherOrderFunctions.java` | `TargetBeforeContext.java`, `Rule.java`, `BaseDecisionRule.java` | **None** (`D52` is disjoint from both `D47` and `D53`). |
| **Construction-Time Eligibility** | `Rule` walks its `Condition` and response expressions checking `FunctionSpecBuilder.usesBuiltInInvocation(spec)` on every `Apply` and `FunctionReference`. | `Apply` prepares `HigherOrderCall` when `canPrepareHigherOrder(spec, ref.function(), count)` and operands have non-dynamic static types. | `TargetBeforeContext` precomputes trusted `<Match>` shape on `<Target>`. | **None** (`D52` does not alter `Apply.function()`, `Apply.getArguments()`, `FunctionReference.function()`, or `usesBuiltInInvocation`). |
| **Evaluation-Time Context** | Eligible rule borrows its `Policy`'s `EvaluationContext` (`activeRule`, `activeRuleStatus`). | `HigherOrderCall.evaluate(context)` forwards `context` unchanged to `arg1.evaluate(context)`, `arg2.evaluate(context)`, and `ref.invoke(context, ...)`. | Target admission runs before rule context creation; rule continuation enters `Rule` with target already matched. | **None** (`HigherOrderCall` never inspects `context.getCurrentRule()` and preserves status recording on `context`). |
| **Interaction with D45 (`PreparedConditions`)** | D47 gates rule borrowing on `PreparedConditions` compatibility. | When D45 clones an `any-of`/`all-of` `Apply` with a `PreparedConditions.Shared` operand, `Apply.builder(...).build()` runs `HigherOrderCall.prepare` and preserves D52 (with `P2-2`). | `Rule` evaluates its prepared `Condition` after target admission. | **None** once `P2-2` is applied. |
