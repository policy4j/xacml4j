<!-- Review RVI1 of cycle I (D15), Codex GPT-6.1 Sol, read-only; report from the NTM pane. -->

FINAL REPORT (RVI1 code reviewer, GPT-6 / Codex)

Reviewed cycle I against HEAD `21858796c85745679df07a91349d9c1bb834726b`. The scoped production diff and test source remained unchanged during review.

1. **P2 — Evaluated operands are narrowed to `ValueExp` before the fallback can preserve behavior.**  
   `xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java:413` assigns each evaluated operand to `ValueExp`. However, `FunctionReference` implements only `Expression` and evaluates to itself (`policy/FunctionReference.java:45`, `:124`).

   A public binary provider accepting an annotated function-reference parameter is eligible for binding. Its list invocation can succeed, while its direct invocation throws a `FunctionInvocationException` caused by `ClassCastException`. Supplying a function reference as a wrong-type operand to `string-equal` also changes the old reflective failure and prevents evaluation of operand two.

   **Smallest fix:** keep evaluated operands typed as `Expression`, including the parameters of `DefaultFunctionInvocation.invokeDirect()`, and let the existing Java-parameter checks select the reflective fallback.

   **Test:** compare both paths for a valid function-reference parameter and for a function reference supplied to `string-equal`; assert result/failure details and operand evaluation order.

2. **P2 — Binding uses broader access than the reflective caller.**  
   `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java:159` creates its lookup in the implementation package. The reflective caller resides in the parent function package (`function/FunctionInvocationFactory.java:73`).

   A package-private provider in `org.xacml4j.v30.policy.function.impl`, with a public static binary method, passes registration. The lambda lookup has package access, while the reflective caller does not. Consequently, the pair path can return a value where `invokeWithList()` fails with an `IllegalAccessException` cause. The current eligibility check at `DefaultFunctionInvocation.java:147` does not exclude this case. Access checks depend on the respective caller or lookup class. [Lookup.unreflect documentation](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/invoke/MethodHandles.Lookup.html#unreflect(java.lang.reflect.Method)), [Method.invoke documentation](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/reflect/Method.html)

   **Smallest fix:** require public declaring classes and public methods before binding; retain reflection otherwise.

   **Test:** register such a package-private provider and assert identical pair/list failures, including cause class.

3. **P3 — DEBUG calls lose the existing diagnostic sequence.**  
   `xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java:417` emits one line only after success. It omits the existing pre-invocation function and argument records; a throwing target never reaches this replacement line. D18 and Open Question 2 still preserve diagnostic lines pending a decision (`openspec/changes/optimize-pdp-evaluation/design.md:266`, `:371`).

   **Smallest fix:** use the existing generic path when DEBUG is enabled, retaining the direct path at INFO.

   **Test:** capture the DEBUG records for successful and throwing pair calls and compare them with HEAD. Keep the allocation assertion scoped to INFO.

Verification performed: source and HEAD comparison, registration-path tracing, official Java access documentation, scoped `ast-grep scan` and `git diff --check`. Both checks passed. Ripwire impact was **PARTIAL** because it missed visible callers; quality-delta exited 2 with heuristic findings.

Retained evidence records nine passing direct-pair tests (`evidence/tests/025-cycleI-green/focused.txt:56`) and reactor success (`025-cycleI-green/reactor.txt:20`). Those cases do not cover the two counterexamples above. Their runtime reproduction, foreign-loader unloading and fresh allocation measurements remain **UNVERIFIED**.

No files were changed. No Maven, tests or benchmarks were run.

**Verdict: REJECT — resolve the two behavioral differences before accepting cycle I.**
