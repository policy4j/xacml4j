# rvm1-cycleM-astra (Codex Astra, NTM worktree lane, read-only)

FINAL REPORT (M-review-astra, Codex Astra)

Verdict: **APPROVE WITH CHANGES**. One P3 finding; no P1/P2 defects found by inspection. Changed-file line numbers refer to the patch postimage.

**P3 — `BaseDecisionRule.java:204,245`: capacity includes inapplicable expressions.** One matching Permit expression alongside 999 Deny expressions allocates 1,000 backing slots for one result. This introduces avoidable allocation on mixed-effect policies. Keep creation lazy, but use a small or bounded initial capacity for both result lists. [Patch locations](<scratchpad/merge-M.diff:230>).

The requested staging checks passed source review:

- **Ownership:** `StagedResponses.staged()` replaces the immutable empty list before adding. Parent staging copies elements; the root merges elements without retaining the incoming list. Built-in paths do not mutate a published child list afterward (`BaseCompositeDecisionRule.java:261–313`; `RootEvaluationContext.java:275–319`).
- **Publication and order:** matching Permit/Deny responses retain encounter order, child responses precede enclosing responses, and advice forwarding precedes obligations. Publication still follows successful enclosing response evaluation. References preserve these boundaries; extended-Indeterminate staging is discarded (`BaseCompositeDecisionRule.java:127–166`; `BaseDecisionRule.java:153–181`). This preserves the decision-path filtering required by [XACML 3.0 §7.18](https://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html).
- **Empty calls:** the root’s merge work occurs inside element loops; audit consumes the completed `Result`. Skipping empty calls therefore preserves built-in behavior. The custom-context change is explicitly accepted by D19 (`RootEvaluationContext.java:275–319`; `DefaultPolicyDecisionPoint.java:151–161`).
- **Threads:** staging is created per evaluation and built-in combiners invoke children synchronously. Concurrent requests do not share these buffers. Custom parallel combiners were already unsupported by the previous unsynchronized staging lists (`BaseCompositeDecisionRule.java:127,166`; `DecisionCombiningAlgorithms.java:50`).

Existing characterizations cover effect separation, nested/reference publication, assignment values, and Indeterminate suppression. Their sorted ID assertions do **not** verify publication order; that conclusion rests on source inspection (`NestedResponseStagingTest.java:339–345`).

Verification: all six patch base/postimage blob IDs matched; inputs remained unchanged. No edits, builds, or tests performed. Compilation and runtime gates remain **UNVERIFIED** under the reported Maven blocker.
