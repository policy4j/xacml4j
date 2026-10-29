# Review R-AW-argon: Cycle AW — Function Call Shapes and Failure Statuses (Merged Part)

- **Reviewer**: Lane `agyx_1` (Gemini Argon)
- **Author**: Codex Terra (Cycle AW, merged subset)
- **Verdict**: **APPROVE** (`0 P1`, `0 P2`, `0 P3`)

---

## Findings

None (`0 P1`, `0 P2`, `0 P3`).

---

## Verification of Review Points

### 1. `StatusCode` Shared `PROCESSING_ERROR` Instance (`SUPPORTED`: source inspection + `rtk rg` across reactor)

- **Immutability and subclassing**:
  - [`StatusCode.PROCESSING_ERROR`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/StatusCode.java`) is initialized once at class initialization (`builder(StatusCodeId.STATUS_PROCESSING_ERROR).build()`, safely published under JLS §12.4.2) and returned by [`StatusCode.processingError()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/StatusCode.java`).
  - [`StatusCode`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/StatusCode.java`) has a `private` constructor and `private` fields `value` and `minorStatus` with no mutators; no external class can subclass `StatusCode` or mutate an existing instance.
  - Its [`StatusCodeId.STATUS_PROCESSING_ERROR`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/StatusCodeId.java`) identifier is a direct instance of [`StatusCodeId`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/StatusCodeId.java`) (extending [`SemanticIdentifier`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/SemanticIdentifier.java`)), which exposes no mutators.
- **Builders with `minorStatus`**:
  - [`StatusCode.Builder.minorStatus(StatusCode)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/StatusCode.java`) and [`StatusCode.Builder.minorStatus(Builder)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/StatusCode.java`) only store the child reference on the builder for the enclosing `StatusCode`; they never mutate the passed `StatusCode`.
- **Equality and Serialization**:
  - [`StatusCode.equals`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/StatusCode.java`) and [`StatusCode.hashCode`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/StatusCode.java`) are value-based over `(value, minorStatus)` (with a fast `o == this` check in `equals`).
  - `StatusCode` does not implement `Serializable`. XML (`Xacml30RequestContextFromJaxbToObjectModelMapper`, `Xacml20ResponseContextMarshaller`, `Xacml20ResponseContextUnmarshaller`) and JSON (`StatusCodeAdapter`) marshallers only read `getValue()` and `getMinorStatus()` when serializing and construct fresh `StatusCode` instances via `StatusCode.builder(...)` when deserializing.

### 2. `FunctionInvocationException` Status, Message, and Cause (`SUPPORTED`: source inspection + test verification)

- **Two-argument constructor [`FunctionInvocationException(FunctionSpec, Throwable)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/FunctionInvocationException.java`)**:
  - Calls `super(statusFor(cause, null, null), cause)` directly instead of chaining through `this(spec, cause, null)` (which previously allocated an empty `Object[0]` varargs array).
  - In [`statusFor(Throwable, String, Object...)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/FunctionInvocationException.java`):
    1. When `cause` (or any exception in its cause chain before a self-cause cycle) is an [`EvaluationException`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/EvaluationException.java`), the loop at `:61-68` returns `((EvaluationException) current).getStatus()` first, before inspecting `message` or `params`.
    2. When no `EvaluationException` is in the cause chain and `message == null` (`:69-71`), `statusFor` returns `Status.processingError().error(cause).build()`. Previously, `Optional.ofNullable(message).map(v -> String.format(v, params)).orElse(null)` evaluated to `null` and called [`Status.Builder.message(null)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/Status.java`) (allocating a second `Object[0]` varargs array), which set `Builder.message` to `Optional.empty()`. Skipping `.message(...)` when `message == null` leaves `Builder.message` at its initial `Optional.empty()` (`Status.java:212`), producing the exact same `Status` (`code = StatusCode.processingError()`, `message = Optional.empty()`, `detail = Optional.empty()`) and the exact same [`CoreException`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/CoreException.java`) state (`getMessage() == null`, `getCause() == cause`, `getSpec() == spec`).
- **Message and four-argument constructors ([`:34-39`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/FunctionInvocationException.java`), [`:52-58`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/FunctionInvocationException.java`))**:
  - When `message != null` and no `EvaluationException` is in `cause`, `:72-75` runs `Status.processingError().message(String.format(message, params)).error(cause).build()`, identical to before.
  - When `message == null` is passed to those constructors, `:69-71` produces `Optional.empty()` as `getStatus().getMessage()` and `null` as `getMessage()`, identical to before.

### 3. Fast Argument Range in `FunctionSpecBuilder.FunctionSpecImpl` (`SUPPORTED`: source inspection)

- In [`FunctionSpecImpl`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java`), `parameters`, `evaluateParameters`, and `fast` are fixed at construction, and [`fastShape(int size)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java`) is `private` and called only from [`invokeWithList`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java`) where `size = arguments.size() >= 0`:
  - **`FIXED`** (`:401-403`): When `!evaluateParameters && mandatoryParameters`, `fastMinArguments = fastMaxArguments = fast.fixedArgumentCount()` (which is $\ge 0$ because `evalContextRequired` is true only when `parameterTypes.length >= 1`). `size >= min && size <= max` holds iff `size == fast.fixedArgumentCount()`. When the guard is false, `min = max = -1`, which is false for all `size >= 0`.
  - **`AND`, `OR`** (`:404-409`): The old switch checked `evaluateParameters && parameters.size() == 1 && isVariadic()` without constraining `size`. When that guard holds, the new code sets `fastMinArguments = 0` and `fastMaxArguments = Integer.MAX_VALUE`. For every list size `size = arguments.size()` (`0 <= size <= Integer.MAX_VALUE`, including `0` for `and()` and `or()`), `size >= 0 && size <= Integer.MAX_VALUE` is `true`. When the guard is false, `min = max = -1`, which is false for all `size >= 0`.
  - **`ANY_OF`, `ALL_OF`, `ANY_OF_ANY`, `MAP`** (`:410-416`): When `!evaluateParameters && parameters.size() == 2 && isMandatory(parameters.get(0)) && isVariadic()`, `fastMinArguments = 2` and `fastMaxArguments = 3`. For integer `size`, `size >= 2 && size <= 3` holds iff `size == 2 || size == 3`. When the guard is false, `min = max = -1`, which is false for all `size >= 0`.
  - **`NONE` / `default`** (`:394-395, 417`): `fast` is `null` (or `min = max = -1`), so `fastShape` is never reached or returns `false` for all `size >= 0`.

### 4. Tests (`SUPPORTED`: source inspection + Maven test execution)

- [`ApplyArgumentValidationTest.higherOrderChecksTheEvaluatedBagType`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/test/java/org/xacml4j/v30/policy/ApplyArgumentValidationTest.java`) and [`higherOrderEvaluatesAllOperandsBeforeCheckingTheirActualTypes`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/test/java/org/xacml4j/v30/policy/ApplyArgumentValidationTest.java`) pin observable evaluation outcomes (`isTypeCorrect()`, `EvaluationException` status codes `SYNTAX_ERROR` and `MISSING_ATTRIBUTE`, and messages) with runtime parameter validation both off and on, and pass on the code before AW as well as after.
- [`FunctionInvocationStatusTest.oneAndOnlyFailureKeepsItsStatusAndCauseMessage`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/FunctionInvocationStatusTest.java`) pins the `STATUS_PROCESSING_ERROR` code, empty status message (`Optional.empty()`), and cause message (`"Bag is empty"`) when `string-one-and-only` fails on an empty bag via `FunctionInvocationException(FunctionSpec, Throwable)`, and passes both before and after AW.
- Executed `JAVA_HOME="$(jenv prefix 25)" rtk mvn -o -q -Dlicense.skipUpdateLicense=true -pl xacml-core -am -Dtest=ApplyArgumentValidationTest,FunctionInvocationStatusTest,HigherOrderVariadicFunctionsTest,DirectPairCallTest -Dsurefire.failIfNoSpecifiedTests=false test` — **PASS** (exit code `0`).
- Executed `ast-grep scan` on `StatusCode.java`, `FunctionInvocationException.java`, and `FunctionSpecBuilder.java` — **PASS** (`0` diagnostics).
