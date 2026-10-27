<!-- Code review RCK1 of cycle K, Codex GPT-6.1 Sol, read-only; report from the NTM pane, paths made repository-relative. -->

FINAL REPORT (RCK1 code reviewer, GPT-6 / Codex)

Reviewed cycle K against HEAD `21858796c85745679df07a91349d9c1bb834726b`.

**P2 — Finalizing inherited methods breaks existing binaries, beyond D17’s stated source incompatibility.**

[DescendantEvaluationContext.java:135](xacml-core/src/main/java/org/xacml4j/v30/policy/DescendantEvaluationContext.java:135) makes `getClock()` final; the same applies to the other walked queries. These methods are inherited by the public, extensible [RuleEvaluationContext at Rule.java:168](xacml-core/src/main/java/org/xacml4j/v30/policy/Rule.java:168).

An external subclass compiled against HEAD that overrides `getClock()` can no longer load successfully against this implementation. This is a binary compatibility break, not merely a recompilation failure. JLS §13.4.17 describes the resulting `IncompatibleClassChangeError`. [Java binary compatibility specification](https://docs.oracle.com/javase/specs/jls/se25/html/jls-13.html#jls-13.4.17)

Consequently, [D17’s “source-incompatible only” statement:359](openspec/changes/optimize-pdp-evaluation/design.md:359) understates the impact. An unchanged `EvaluationContext` interface does not prevent this break.

**Smallest fix:** preserve overridable methods in the public context hierarchy and treat user subclasses as virtual-dispatch boundaries; restrict bypassing to trusted internal contexts.

**Test:** compile an external `RuleEvaluationContext` subclass overriding `getClock()` against HEAD, then load and invoke it with the changed core. Also verify source compatibility. Local reproduction is **UNVERIFIED**.

**P2 — `isEnclosed` bypasses still-supported ancestry overrides and can change staging.**

[BaseCompositeDecisionRule.java:208](xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java:208) now calls `getParent()` instead of `getParentContext()`. The latter remains overridable at [DescendantEvaluationContext.java:327](xacml-core/src/main/java/org/xacml4j/v30/policy/DescendantEvaluationContext.java:327).

A public rule-context subclass can retain a policy-set context as its constructor parent while overriding `getParentContext()` to return the root. Place a child policy context below that subclass. HEAD’s ancestry walk reaches the root and selects no staging; the new walk follows the stored parent to the policy-set context and inserts `StagedResponses`. This subclass still compiles, so D17’s warning about overriding newly final methods does not cover it.

D17’s claim that staging wrappers are the only ancestry difference therefore applies only to the inspected internal implementations. [Design statement:365](openspec/changes/optimize-pdp-evaluation/design.md:365)

**Smallest fix:** restore the `getParentContext()` walk in `isEnclosed`.

**Test:** use the external subclass above and a capturing combining algorithm; require the same unstaged context and publication behavior as HEAD. Runtime reproduction is **UNVERIFIED**.

The inspected internal contexts—including anonymous extended-Indeterminate contexts and staging wrappers—did not reveal another query-answer change. The Optional memo retains positive request results, preserves the `putIfAbsent` winner, and leaves PIP results, absence, and failures on their existing paths: [DefaultEvaluationContextHandler.java:182](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java:182), [line 229](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java:229).

The four new context tests cover successful chains and foreign proxies, but do not exercise external rule-context subclasses, unresolved references, or extended-Indeterminate chains. [ContextChainQueryCharacterizationTest.java:128](xacml-core/src/test/java/org/xacml4j/v30/policy/ContextChainQueryCharacterizationTest.java:128)

Verification: source/HEAD comparisons, scoped `ast-grep scan`, and `git diff --check` passed. The interface diff was empty. Ripwire results were **PARTIAL**, with quality-delta exiting 2 for both modules. Retained evidence reports [6,582 core tests and 218 PDP tests passing](openspec/changes/optimize-pdp-evaluation/evidence/tests/034-cycleK-green/summary.md:11); these were not rerun.

Context and memo diffs remained stable. Unrelated switch/header edits appeared in `BaseCompositeDecisionRule` during review; its cycle K ancestry hunk remained unchanged.

No files were changed. No Maven, tests, benchmarks, Git/br writes, GitHub connector, or sign-in were used.

**Verdict: REJECT — preserve public context compatibility and ancestry dispatch.**
