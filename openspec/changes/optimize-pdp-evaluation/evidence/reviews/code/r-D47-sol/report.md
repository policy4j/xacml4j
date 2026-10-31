FINAL REPORT (Codex Sol, GPT-6)

**Verdict: REJECT — one new P2.** No P1 findings.

1. **P2 — Runtime attribute values escape the eligibility check.** [Rule.java:304](`.ntm/worktrees/xacml4j--batch2/cod_2/xacml-core/src/main/java/org/xacml4j/v30/policy/Rule.java:304`) accepts designators and selectors without proving their returned values are engine values. Higher-order functions subsequently evaluate those values with the borrowed context (`DecidingOperands.java:204` → `FunctionReference.java:121` → `FunctionSpecBuilder.java:662`).

   Concrete failure: put the existing test’s `ParentPolicyValue` in a request attribute instead of a literal bag. A single Permit rule with `any-of(string-equal, "policy", designator)` returns Permit with observation enabled, but NotApplicable when borrowing: the value’s `evaluate(context)` sees the policy context’s parent rather than the rule context’s parent. A PIP-returned value has the same problem. This is extension compatibility involving trusted code, without hostile behavior.

   **Fix:** conservatively exclude higher-order expression paths that can evaluate unchecked runtime values. Retain a real rule context unless those values’ provenance is proven; preserve lazy resolution. Recheck allocation savings after tightening eligibility.

   **Test:** extend `RulesInPolicyContextPdpTest` with request-supplied and PIP-supplied `ParentPolicyValue` cases, index off/on, asserting Permit and observed/unobserved response equality. The current literal-bag case does not cover these paths (`RulesInPolicyContextPdpTest.java:130`, `:220`). Opus acknowledges this gap in his report’s residual risk 4.

**Other checks**

- **Slot/status:** entry gates are correct for the admitted expression paths (`Policy.java:397`). Local status starts empty; rule-keyed status precedes policy propagation; the pass guard preserves the originating status (`Policy.java:419–446`, `DescendantEvaluationContext.java:123`). Cleanup runs in `finally`, including exceptional exits (`TargetBeforeContext.java:86`).
- **Context answers:** descendants find the nearest active rule (`DescendantEvaluationContext.java:243`). Current-policy lookup remains unchanged, so D45 still finds its Policy. Resolution delegates to the root (`:352`, `RootEvaluationContext.java:510`); D26’s engine resolution path does not pass the borrowed context to workers.
- **Index:** unchanged. The parent revision already used the same eleven-class set, and `Prepared.children` still checks it (`Policy.java:72`, `:235`). The design’s seven-class `canIndexRules()` amendment is stale.
- **Tests:** the new cases pin decisions, statuses, responses, variables, worker fallback, references, and extension behavior. Observed evaluation provides a useful comparison against ordinary rule contexts. The missing coverage is the runtime-value case above.

**Confirmed existing risk, separate from D47:** [ResponseWriters.java:84](`.ntm/worktrees/xacml4j--batch2/cod_2/xacml-core/src/main/java/org/xacml4j/v30/policy/ResponseWriters.java:84`) revisits every variable reference without remembering definitions. A diamond `v(n)=and(v(n−1),v(n−1))` doubles the scan at each level. This is a real policy-loading cost for a small, valid DAG; `BaseDecisionRule.java:80` runs it before D47’s linear scan. Use an identity-based per-scan memo. Verify with a deep DAG that loads and returns the expected decision. Opus’s reported timings were not independently reproduced.

**Verification:** reviewed `PATCH-D47.diff`, design/amendments, production paths, and both test classes at `cd443805c3044e7412c25aae71dd02127994d418`. Scoped `ast-grep scan` and `git diff --check` passed. Ripwire health passed 7/7; its name-based impact results were confirmed in source. No files changed or builds/tests/JVMs run. The reported reactor result—10,567 tests, zero failures/errors, 36 skipped—and measurements remain supplied evidence.
