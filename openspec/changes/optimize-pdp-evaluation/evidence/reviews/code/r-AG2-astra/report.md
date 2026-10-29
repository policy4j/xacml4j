FINAL REPORT (cod_3, Codex Astra)

**Verdict: APPROVE WITH CHANGES — all three prior findings closed; one P3 remains.**

**P3 — Narrow the new public helper’s contract.** [FunctionSpecBuilder.java:293](`xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java:293`) returns `true` for `FunctionParamFuncReferenceSpec`, although that class’s `validate` accepts a `FunctionReference` while `isValidParamType` always returns `false`. [Source: FunctionParamFuncReferenceSpec.java:38](`xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionParamFuncReferenceSpec.java:38`).

The current higher-order caller is safe because it excludes nulls and function references first. **Fix:** document those preconditions on `validatesOneArgumentByType`; its present Javadoc promises general equivalence that does not hold.

The original findings are closed:

- **P1:** exact engine parameter classes receive the shortcut; custom parameters use `validate(iterator, true)`. The [regression](`xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/HigherOrderVariadicFunctionsTest.java:345`) checks rejection and `SYNTAX_ERROR`.
- **P2-1:** standalone normalization now delegates with `false`. The [validator regression](`xacml-core/src/test/java/org/xacml4j/v30/policy/function/FunctionInvocationCharacterizationTest.java:244`) checks immutable input acceptance and caller-array isolation.
- **P2-2:** reuse requires an immutable list; mutable caller-backed lists are copied before evaluation. The [substring regression](`xacml-core/src/test/java/org/xacml4j/v30/policy/function/FunctionInvocationCharacterizationTest.java:362`) pins `"ab"`. Immutable Apply lists retain reuse.

**API assessment:** keeping both narrow public bridges is acceptable. Their consumers span `policy` and `policy.function.impl`, while the inspected implementation classes live in `policy.function`. Simply moving both methods into a package-private helper would make them inaccessible; avoiding public access requires broader restructuring.

**Verification:** inspected the merged source, patch, tests, and raw logs. The [pre-fix run](<scratchpad>/fix-ag/before-fix.txt:34) confirms all three regressions failed: two assertions and one `UnsupportedOperationException`. The [fixed gate](<scratchpad>/fix-ag/gate.txt:405) passes both classes and records core **6,750/2 skipped**, PDP **464/3**, conformance **1,818/30**, with zero failures/errors.

Read-only review: no edits or fresh test runs.
