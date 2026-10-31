<!-- Re-check RVI3 of cycle I, Codex GPT-6.1 Sol, read-only; report from the NTM pane, paths made repository-relative. -->

FINAL REPORT (RVI3 code reviewer, GPT-6 / Codex)

Reviewed against HEAD `21858796c85745679df07a91349d9c1bb834726b`. The scoped production diff and regression test remained unchanged during review.

**P2 — Binding a hidden provider can prevent its class from unloading.**

[DefaultFunctionInvocation.java:157](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java:157) accepts a public hidden provider when it shares xacml-core’s loader. Such a provider can enter through [FunctionProviderBuilder.fromClass:140](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionProviderBuilder.java:140).

For a hidden provider with accessible parameter and return types, binding succeeds. OpenJDK 25 stores the hidden static target’s `MethodHandle` in the generated lambda’s class data and defines that lambda with `STRONG_LOADER_LINK`. This creates a loader-rooted reference to the provider class. Dropping the provider and function specifications therefore cannot release it; the `ClassValue` cache does not break that external reference. Repeated registration of disposable hidden providers can retain classes and metaspace. [OpenJDK lambda implementation](https://github.com/openjdk/jdk25u/blob/master/src/java.base/share/classes/java/lang/invoke/InnerClassLambdaMetafactory.java)

Hidden classes defined without `STRONG` are intended to unload independently of their loader. The original reflective path does not introduce this strongly linked generated class. [JEP 371](https://openjdk.org/jeps/371)

**Smallest fix:** exclude `m.getDeclaringClass().isHidden()` in `isBindable`, preserving reflection for those providers.

**Test:** define a public provider with `defineHiddenClass` without `STRONG`, using the same loader and accessible binary signature. Assert that binding is declined and reflective invocation succeeds. A class-unloading test can additionally verify reclamation after dropping references.

This finding follows from source and JDK implementation evidence. Local GC reproduction is **UNVERIFIED**.

**RVI2 disposition: resolved.**

[DefaultFunctionInvocation.java:177](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java:177) checks every parameter, and line 179 checks the return type before generating the lambda. Access failures use the existing reflective fallback.

`Lookup.accessClass` covers array element accessibility, JVM nested-class/nest access, module readability, and package exports. Checking the return type is conservative and preserves behavior through fallback. [Lookup.accessClass documentation](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/invoke/MethodHandles.Lookup.html#accessClass(java.lang.Class))

The regression at [DirectPairCallTest.java:452](xacml-core/src/test/java/org/xacml4j/v30/policy/function/DirectPairCallTest.java:452) compares both paths and requires the successful `true` result at line 462. It cannot pass merely because both calls fail. Retained evidence records the intended `IllegalAccessError` before the fix and 13 passing direct-pair tests afterward: [red log:49](openspec/changes/optimize-pdp-evaluation/evidence/tests/027-rvi2-fix/cycleI-rvi2-red.log:49), [green log:56](openspec/changes/optimize-pdp-evaluation/evidence/tests/027-rvi2-fix/cycleI-rvi2-green.log:56).

Verification: source comparison, JDK access/lambda implementation review, `ast-grep scan` and scoped `git diff --check` passed. Ripwire quality-delta exited 2 with heuristic findings. No files were changed; no Maven, tests, or benchmarks were run. Module, array, return-type, and class-unloading runtime cases remain **UNVERIFIED**.

**Verdict: APPROVE WITH CHANGES — exclude hidden declaring classes from direct binding.**
