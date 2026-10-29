FINAL REPORT (Codex Sol, GPT-6)

**Verdict: REJECT. Two P2 findings; no P1 or P3 findings.** Original P2-2 and P2-3 are closed. The original P2-1 examples are fixed, but staging remains incorrect.

1. **P2 — Closing an Indeterminate pass can restore a scope that never opened.**  
   [RootEvaluationContext.java:610](`.ntm/worktrees/xacml4j--batch2/cod_2/xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java:610`); `BaseCompositeDecisionRule.java:221`.

   A policy with an Indeterminate target enters its extended pass without entering `combine`. Closing that pass nevertheless selects the original policy context because `wouldStage()` answers eligibility, not whether staging opened.

   Concrete case: an outer first-applicable set contains an enclosed `permit-unless-deny` set S. S contains a responding policy P whose target fails, and S declares Permit obligations/advice. After P’s pass, `staging` incorrectly points to P. S returns Permit, but `closeStaging(S)` does nothing. Publishing S’s frame stages it back into S, and the final `dropStaged(S)` discards it. Permit survives without S’s required responses.

   **Fix:** save and restore the actual previously open owner; do not reconstruct open state from context ancestry and `wouldStage()`.  
   **Test:** the nested shape above, using a missing required target attribute. Assert Permit with S’s complete obligations/advice and no responses from P. This exercises [XACML §§7.13, 7.14 and 7.18](https://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html).

2. **P2 — A joined worker bypasses staging when writing through an active enclosing context.**  
   [RootEvaluationContext.java:589](`.ntm/worktrees/xacml4j--batch2/cod_2/xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java:589`); `BaseCompositeDecisionRule.java:176`.

   For an enclosing owner, `isStaging()` calls `wouldStage()`, which rechecks `root.isDecidingThread()` on the **worker**. It therefore rejects an already-open scope. The immediate-owner identity branch does not have this problem.

   Concrete case: a Deny rule’s custom condition starts and joins a worker that adds Permit responses through its enclosing policy-set context. Both the policy and set use built-in algorithms. While the policy is innermost, the worker bypasses the set’s staging and reaches the root. A later Permit sibling exposes those responses. The former enclosing wrapper discarded them. Thread start/join establishes visibility; this is not an unjoined-write race.

   **Fix:** separate deciding-thread admission when opening a scope from membership in an already-open scope. Membership must reflect recorded open state independently of the writing thread.  
   **Test:** the case above under outer permit-overrides; assert Permit with none of the worker’s responses. This preserves [§7.18 filtering](https://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html).

Original-finding disposition:

- **P2-1:** custom-combiner worker writes now use the wrapper, and direct root writes bypass staging. The tests at `NestedResponseStagingPdpTest.java:170` and `:193` pin those outcomes. Findings above prevent closure overall.
- **P2-2 closed:** `DecisionRuleResponse.java:112` restores the protected mutable multimap; its diff against HEAD is empty. The external builder test exercises direct mutation and assignment order.
- **P2-3 closed:** `ObligationExpression.java:47` and `AdviceExpression.java:52` restrict caching to exact classes. Static factories use final fields during construction; subclass evaluation remains virtual. The external failure test pins Indeterminate and processing-error.

The ancestry assurance in `REPORT-FIX-AN-fable.md:144` is therefore unsound. Its joined-worker qualification is also too broad. The acknowledged closed-context forwarding remains a compatibility difference; it does not establish safety for the active-scope cases above.

Verification: read the full patch, current implementation, original review, and added tests. Scoped whitespace checks produced no diagnostics. Java ast-grep scan passed: 11 files, three rules. Ripwire quality comparison was **PARTIAL**: it compared the accumulated batch against HEAD and exited 2; its findings cannot be attributed to FIX-AN.

No files changed; no builds, tests, or measurements run. Both failure scenarios are source-derived and unexecuted. The green reactor and unchanged allocation figure remain author-supplied evidence. Trusted custom extensions are assumed.
