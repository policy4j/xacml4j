<!-- Re-check RVI2 of cycle I, Codex GPT-6.1 Sol, read-only; report from the NTM pane. -->

FINAL REPORT (RVI2 code reviewer, GPT-6 / Codex)

Reviewed against HEAD `21858796c85745679df07a91349d9c1bb834726b`. The scoped production diff and both test files remained unchanged during review.

RVI1 dispositions:

1. **Resolved — operand narrowing.** Evaluated operands remain `Expression` (`xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java:414`), as do the direct invoker’s arguments (`impl/DefaultFunctionInvocation.java:206`). The new tests cover valid function references and wrong-type references, including evaluation of the second operand (`DirectPairCallTest.java:404`, `:419`).

2. **Resolved — non-public provider access.** Eligibility now requires public methods, declaring classes and enclosing classes (`DefaultFunctionInvocation.java:150`). The regression fixture is package-private (`impl/PackagePrivateFunctions.java:38`), and its parity test is at `DirectPairCallTest.java:438`.

3. **Resolved by the maintainer’s decision — DEBUG output.** The success line is removed. D18 explicitly authorizes removing per-element diagnostics (`openspec/changes/optimize-pdp-evaluation/design.md:346`); completing the remaining logging changes belongs to cycle L.

**P2 — Parameter-type accessibility remains unchecked before generating the lambda.**

`xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java:150` checks the provider’s visibility, but its parameter-type checks only exclude primitives. At `:178`, the actual method signature becomes the lambda’s specialized signature.

A public provider can expose a public binary method whose parameters are a private nested `ValueExp` implementation. A public factory can supply those operands through the `Expression` interface. Reflection can invoke that public method with matching operands, but the generated lambda must cast its `Object` arguments to the inaccessible parameter class. Those casts can throw `IllegalAccessError`. The binding already exists and `isInstance()` succeeds, so the fallback at `:209` does not run; `:217` instead wraps the error.

This is a source-derived counterexample; local runtime reproduction is **UNVERIFIED**. OpenJDK’s adapter explicitly generates casts to specialized parameter types. [OpenJDK argument-conversion source](https://github.com/openjdk/jdk25u/blob/master/src/java.base/share/classes/java/lang/invoke/TypeConvertingMethodAdapter.java)

**Smallest fix:** check each parameter type with the binding lookup’s `accessClass()` before spinning the lambda, and use the existing reflective fallback when access fails. This also checks array element access and module exports. [Lookup.accessClass documentation](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/invoke/MethodHandles.Lookup.html#accessClass(java.lang.Class))

**Test:** register a public provider with two private nested value parameters, obtain operands through a public factory, and assert that pair and list calls both return the expected value. Add the corresponding named-module case with an inaccessible internal parameter type.

No further bridge, synthetic-method or interface-specific defect was established from source. Declaring-class access failures reach the binding fallback; ordinary instance bridge/default methods fail the static-method eligibility check. Runtime module and interface cases remain **UNVERIFIED**.

Verification performed: source comparison, JDK adapter/access-check source review, scoped `ast-grep scan` and `git diff --check`; both checks passed. Ripwire quality-delta exited 2 with heuristic findings.

Retained evidence records three intended failures (`026-rvi1-fixes/red.txt:49`), then 12 passing direct-pair tests (`green.txt:56`) and 240 passing focused tests (`green.txt:123`). These validate the original fixes, not the inaccessible-parameter case.

No files were changed. No Maven, tests or benchmarks were run.

**Verdict: REJECT — check parameter-type accessibility before accepting the binding.**
