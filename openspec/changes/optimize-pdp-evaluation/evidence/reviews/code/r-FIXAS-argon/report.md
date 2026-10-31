# Review R-FIXAS-argon: Re-Review of Codex Terra's FIX-AS

- **Reviewer**: Gemini Argon (`agyx_1`)
- **Change Under Review**: `FIX-AS` ([PATCH-FIXAS.diff](`.ntm/worktrees/xacml4j--batch2/agyx_1/PATCH-FIXAS.diff`), [REPORT-FIX-AS-terra.md](`.ntm/worktrees/xacml4j--batch2/agyx_1/REPORT-FIX-AS-terra.md`))
- **Prior Review**: [REPORT-R-C3AS-argon.md](`.ntm/worktrees/xacml4j--batch2/agyx_1/REPORT-R-C3AS-argon.md`), Part 2
- **Verdict**: **APPROVE WITH CHANGES** (`0 P1`, `1 P2`, `0 P3`)

---

## 1. Point-by-Point Verification (`SUPPORTED`)

1. **Closure of `R-C3AS P2-1` and `P3-1` (custom specs keep variadic dispatch everywhere they did before AS)**:
   - [`FunctionReference.invoke(EvaluationContext, Expression, Expression)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/FunctionReference.java`) now routes by [`FunctionSpecBuilder.usesBuiltInInvocation(spec)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java`), calling `spec.invoke(context, arg0, arg1)` for trusted built-ins and `spec.invoke(context, new Expression[]{arg0, arg1})` for custom or non-built-in specs.
   - [`FunctionReference.invoke(EvaluationContext, Expression)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/FunctionReference.java`) delegates to [`FunctionSpecBuilder.invokeVariadicSingle`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java`), and the redundant `invokeVariadicPair`, `invokeVariadicSingle`, and `usesBuiltInInvocation` methods on `FunctionReference` are removed.
   - [`HigherOrderFunctions`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/HigherOrderFunctions.java`) and [`DecidingOperands.firstDeciding`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DecidingOperands.java`) use the unary and pair `FunctionReference.invoke` overloads uniformly across `any-of`, `all-of`, `any-of-any`, `all-of-any`, `any-of-all`, `all-of-all`, and `map`, without duplicate `!ref.usesBuiltInInvocation()` fallbacks.
   - [`DefaultFunctionInvocation.invokeFast`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java`) guards `count > i && !fits(a_i, i)` and [`DefaultFunctionInvocation.fits(Object, int)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java`) checks `index < parameterTypes.length` (see `P2-1` below for the interaction with `invokeFixed`).
2. **Direct dispatch on built-in paths**:
   - Built-in 1- and 2-argument `Unary` / `Binary` direct bindings (`invokeDirect`) and `invokeFast` (`CallShape.FIXED`, `AND`, `OR`, `ANY_OF`, `ALL_OF`, `ANY_OF_ANY`, `MAP`) keep their direct dispatch.
   - **Regression found (`P2-1` below)**: [`DefaultFunctionInvocation.invokeFixed`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java`) still calls `!fits(a0, 0) || !fits(a1, 1) || !fits(a2, 2) || !fits(a3, 3)` without `count > i` guards. Because `fits(Object, int)` (`:580`) now returns `false` whenever `index >= parameterTypes.length` (even when `argument == null`), `!fits(a3, 3)` is always `true` whenever `parameterTypes.length < 4`. As a result, every `fixed` `MethodHandle` with `0`, `1`, `2`, or `3` Java parameters (including all variadic built-in functions on the non-fast path such as `*-bag`, `string-concatenate`, `n-of`, and 3+-operand higher-order calls, as well as `FunctionSpec.invoke(context, Expression...)` and runtime-validated calls on `fixed` handles) loses its `fixed.invokeExact` dispatch (`case 0..3` at `:552-555` are dead code) and falls back to `invokeFactory(spec, parameters(spec, context, arguments))` (`Method.invoke`), allocating `varArgs` a second time and allocating `new Object[count]`.
3. **Single provenance query for D45 (`usesBuiltInInvocation`)**:
   - [`FunctionSpecBuilder.usesBuiltInInvocation(FunctionSpec)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java`) is the single provenance query verifying `FunctionSpecImpl`, `DefaultFunctionInvocation`, default `FunctionInvocationFactory`, static bindable method in the engine classloader, and declaring class in `XacmlDefaultFunctions.PROVIDERS`. D45's `isShareableBuiltin(FunctionSpec)` can build directly on `usesBuiltInInvocation(spec)` plus checking `built.validator == null` and the 3-function allow-list.
4. **Tests pin outcomes**:
   - [`HigherOrderVariadicFunctionsTest.customPredicateKeepsItsVariadicInvocation`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/HigherOrderVariadicFunctionsTest.java`) pins `Apply.evaluate` outcomes (with `runtimeValidation` both `false` and `true`) for custom binary and unary `FunctionSpec` instances across `any-of` (both bag positions), `all-of` (both bag positions), `any-of-any`, `all-of-any`, `any-of-all`, `all-of-all`, and `map`. (Optionally also add `assertValue(..., "map", predicate, strings("a", "b"), string("c"))` at `:251` to cover the `bagFirst` branch of `mapPair`.)

---

## 2. Findings

### `P2-1` — `DefaultFunctionInvocation.invokeFixed` loses `MethodHandle.invokeExact` dispatch for all methods with `< 4` Java parameters (`case 0..3` dead code, double `varArgs` allocation, and reflective fallback)

- **Location**: [`DefaultFunctionInvocation.java:547`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java`), [`:551-557`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java`), [`:578-581`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java`)
- **Failure**:
  - In `PATCH-FIXAS.diff`, [`fits(Object argument, int index)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java`) was changed from `return argument == null || parameterTypes[index].isInstance(argument);` to `return index < parameterTypes.length && (argument == null || parameterTypes[index].isInstance(argument));`, and `invokeFast` (`:206-207`) was updated to guard `count > i && !fits(a_i, i)`.
  - However, [`invokeFixed`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java`) was not updated and still evaluates:
    ```java
    if(count != parameterTypes.length || !fits(a0, 0) || !fits(a1, 1) || !fits(a2, 2) || !fits(a3, 3)){
        return invokeFactory(spec, parameters(spec, context, arguments));
    }
    ```
  - Whenever `parameterTypes.length < 4` (i.e., `0`, `1`, `2`, or `3` Java parameters), `3 < parameterTypes.length` is `false`, so `fits(a3, 3)` returns `false` and `!fits(a3, 3)` is always `true`.
  - Consequently, `case 0`, `case 1`, `case 2`, and `case 3` of `switch(count)` in `invokeFixed` (`:552-555`) are unreachable dead code. Every call through `invokeFixed` with `< 4` Java parameters — including all variadic built-ins without a `CallShape` fast path (`*-bag`, `string-concatenate`, `n-of`, and 3+-operand `any-of`/`all-of`/`any-of-any`/`map`), all `FunctionSpec.invoke(context, Expression...)` calls on `fixed` handles, and all runtime-validated calls on `fixed` handles — now allocates `varArgs` once on line 539, fails `!fits(a3, 3)` on line 547, allocates `new Object[count]` and `varArgs` a second time in `parameters(spec, context, arguments)` on line 548, and falls back to reflective `Method.invoke`.
- **Fix**:
  - Guard `fits` by `count > i` in [`DefaultFunctionInvocation.invokeFixed`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java`) just as in `invokeFast` (`:206-207`):
    ```java
    if(count != parameterTypes.length || count > 0 && !fits(a0, 0) || count > 1 && !fits(a1, 1)
            || count > 2 && !fits(a2, 2) || count > 3 && !fits(a3, 3)){
        return invokeFactory(spec, parameters(spec, context, arguments));
    }
    ```

---

## 3. Verification Summary

- `JAVA_HOME="$(jenv prefix 25)" rtk mvn -o -q -Dlicense.skipUpdateLicense=true -pl xacml-core -am -Dtest=HigherOrderVariadicFunctionsTest,LaterDecidingOperandTest,DirectPairCallTest,FunctionInvocationCharacterizationTest -Dsurefire.failIfNoSpecifiedTests=false test` — **PASS** (`SUPPORTED`).
- `ast-grep scan` on the 4 modified production Java files — **PASS** (`0` diagnostics, `SUPPORTED`).
