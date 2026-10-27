# RCK1: code review of cycle K

Reviewer: Codex GPT-6.1 Sol, NTM pane, read-only (author: Claude). Verdict: REJECT (two P2).

| Finding | Disposition |
| --- | --- |
| P2 final walked methods break compiled subclasses of the public `Rule.RuleEvaluationContext` (binary, JLS 13.4.17), beyond D17's "source-incompatible" note | Fixed: HEAD's final set restored; walks pass only through the package's own contexts and stop at any other context or external rule-context subclass; `ExternalRuleContextCompatibilityTest`, `DescendantContextInventoryTest` (`tests/035-rck1-fix`) |
| P2 `isEnclosed` on parent fields bypasses an overridden `getParentContext()` and can change staging | Fixed: HEAD's `getParentContext()` walk restored; test `stagingFollowsTheParentContextOfAnExternalRuleContext` |

Confirmed by the reviewer: the internal contexts, extended Indeterminate and staging contexts included, give the
same answers; the `Optional` memo keeps the first-bag-wins rule and leaves information point results, absence,
and failures unchanged. Re-check: RCK2.
