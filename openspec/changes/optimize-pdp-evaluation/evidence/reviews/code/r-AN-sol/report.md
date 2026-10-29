FINAL REPORT (Codex Sol, GPT-6)

**Verdict: cycle AN — REJECT. FIX-AI2 — APPROVE.** Three P2 findings; no P1 or P3 findings.

1. **P2 — Root staging changes the scope of response writes.**  
   [RootEvaluationContext.java:391](`.ntm/worktrees/xacml4j--batch2/cod_2/xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java:391`), also `:414`; `BaseCompositeDecisionRule.java:144`.

   Thread and depth replace the old context boundary. A custom combiner can join a worker that adds Permit responses through its supplied context, then return Deny. Previously the wrapper discarded those responses. Now the worker bypasses staging, so a later Permit sibling can expose them, violating [XACML §7.18](https://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html).

   The opposite change affects synchronous PIP writes: `RootEvaluationContext.resolve` passes the root to the handler (`:437`), which reaches `ResolverContext.getEvaluationContext()`. Direct root writes are now staged and potentially discarded. D36 explicitly keeps those writes outside staging (`design.md:1493`).

   **Fix:** preserve context-scoped staging. Retaining the wrapper for paths involving custom code is the smallest safe fallback; deciding-thread identity cannot identify response scope.  
   **Tests:** extend the existing custom-combiner outcome case with a joined worker; add a synchronous resolver that writes directly to the root. Assert final responses, including order.

2. **P2 — Narrowing the builder field breaks public API compatibility.**  
   [DecisionRuleResponse.java:113](`.ntm/worktrees/xacml4j--batch2/cod_2/xacml-core/src/main/java/org/xacml4j/v30/DecisionRuleResponse.java:113`).

   External subclasses could access the protected `attributes` field. Making it private breaks compilation and causes `IllegalAccessError` for previously compiled accesses. Restoring only `protected` is insufficient: direct mutation of the new immutable empty multimap would fail.

   **Fix:** retain the protected, mutable field under the existing constructor contract. Confine lazy storage to a separate internal path if needed.  
   **Test:** an external-package builder subclass that directly populates the field and builds a response with the expected assignments.

3. **P2 — Constant obligations bypass subclass evaluation overrides.**  
   [ObligationExpression.java:44](`.ntm/worktrees/xacml4j--batch2/cod_2/xacml-core/src/main/java/org/xacml4j/v30/policy/ObligationExpression.java:44`), also `:48`.

   `ObligationExpression` has a public constructor and inherits the overridable protected `evaluateAttributeAssignments`. With empty or literal assignments, the cached response bypasses that override. An override that previously threw `EvaluationException` can therefore stop producing Indeterminate and allow Permit. Construction also now calls overridable `getId()` and `getEffect()` before subclass initialization finishes.

   **Fix:** cache the whole obligation only for the exact `ObligationExpression` class; preserve virtual evaluation for subclasses.  
   **Test:** an external-package subclass whose assignment evaluation fails; assert the resulting Indeterminate, status, and absence of obligations.

Other checks:

- **Sequential staging:** nested publication, lazy frame creation, order, and exceptional cleanup look correct. The extended-Indeterminate pass opens a separate unpublished level. The foreign-root wrapper retains filtering (`BaseCompositeDecisionRule.java:195`, `:327`; `RootEvaluationContext.java:592`, `:607`).
- **Request bags:** the entity/index returns the winning bag and Optional; absence remains unstored. Observed decisions retain `REQUEST_MEMO` (`DefaultEvaluationContextHandler.java:308`; `AttributeIndex.java:319`, `:351`). The three revised tests still assert bag identity, reentrant/concurrent consistency, and exclusion from resolved attributes.
- **No-resolver shortcut:** category/id lookup deliberately ignores issuer; registration updates the queried index without negative memoization. Defaults preserve custom implementations and doubles. `missing(o)` preserves the normal absent source and MustBePresent handling (`DefaultResolverRegistry.java:108`; `DefaultEvaluationContextHandler.java:329`, `:349`, `:390`). Concurrent attribute registration retains the existing unsynchronized multimap race.
- **Constants/tests/style:** exact engine expressions retain assignment content and order, including bag expansion. New tests pin useful outcomes. No added `return null` statements were found.
- **FIX-AI2:** precheck access is confined to the final deciding thread; evaluation and NOMATCH exits clear the slot in `finally`; MATCH retains context validation. The pending MATCH used by the match-before-evaluate protocol remains intentional (`RootEvaluationContext.java:529`, `:543`, `:562`; `BaseDecisionRule.java:192`).

Verification: inspected all 21 patch files and current source. Scoped `git diff --check` and Java `ast-grep scan` passed. Ripwire quality comparison was **PARTIAL**: it compared the entire accumulated batch against HEAD and exited 2; its totals cannot be attributed to AN. No files edited, builds, or tests run. The supplied green reactor remains author evidence; the failure scenarios above are source-derived and unexecuted.

Trust boundary: in-process custom contexts, combiners, resolvers, and subclasses are trusted extension code.
