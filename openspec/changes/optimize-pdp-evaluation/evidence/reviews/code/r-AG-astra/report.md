FINAL REPORT (cod_3, Codex Astra)

**Verdict: REJECT — one P1, two P2s.**

1. **P1 — Higher-order validation bypasses custom parameter contracts.** [HigherOrderFunctions.java:281](`.ntm/worktrees/xacml4j--batch2/cod_3/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/HigherOrderFunctions.java:281`) assumes matching arity and `!isVariadic()` make `validate(iterator, true)` equivalent to `isValidParamType`. The public SPI makes no such guarantee. A custom parameter can accept the type but reject the sequence; the shortcut now accepts it and can return `true` instead of the previous higher-order syntax error.
   **Fix:** restrict this shortcut to proven engine parameter implementations; retain the original validation for custom metadata. Pin the rejected invocation’s status.

2. **P2 — Standalone validation loses its detached argument list.** [FunctionSpecBuilder.java:655](`.ntm/worktrees/xacml4j--batch2/cod_3/xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java:655`) enables reuse for `validateParameters`, too. An additional validator retaining an `Arrays.asList` input now observes subsequent caller-array changes. A validator modifying its formerly mutable copy can now modify caller data or fail on immutable input.
   **Fix:** make `normalize(List)` delegate with `false`; let invocation paths opt into reuse explicitly. Runtime validation already takes the copied path.

3. **P2 — Eager evaluation can observe changes to later caller operands.** [FunctionSpecBuilder.java:668](`.ntm/worktrees/xacml4j--batch2/cod_3/xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java:668`) reuses caller-backed array lists, which `evaluate` subsequently reads live. Static counterexample: invoke `string-substring` with `[expressionReturning("abcd"), 0, 2]`, where the first expression changes the same array’s third entry to `4`. Previously normalization captured all operands first, producing `"ab"`; now evaluation produces `"abcd"`. No concurrent thread is needed.
   **Fix:** snapshot mutable caller-backed lists before evaluating operands; retain reuse for immutable Apply lists. Pin the returned value.

No additional finding in fixed-arity argument placement, logical short-circuits, lazy custom-invocation copying, or D34 failure-reporting boundaries. `readsArgumentLists` is a reasonable narrow bridge across packages: its exact implementation check avoids calling custom specs, whose Apply dispatch remains varargs. The new bag-first test usefully pins noncommutative operand order.

Verification: read the nine-file patch, implementation, and relevant tests. No edits, builds, tests, or benchmarks run. The counterexamples above are source-derived; Opus’s green gates and allocation smoke were not independently rerun.
