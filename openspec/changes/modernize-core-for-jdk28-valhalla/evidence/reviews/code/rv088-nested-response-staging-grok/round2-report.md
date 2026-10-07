The five round-1 findings are closed. No new finding meets the bar for a change.

**1. Round 1 is closed.** Scenario 1 now locks staging of the extended-Indeterminate path. The mutation result is consistent with the test and with this engine.

The alone evaluation is a real outermost policy: target failure is a processing-error whose message is `target failed`, the decision is Indeterminate, and `indeterminate-rule-obligation` is in the root Permit list. That is the positive control that the Permit rule ran and created the obligation (XACML 3.0 §7.12: an Indeterminate target still evaluates rules to compute the extended Indeterminate). The enclosed permit-overrides run then expects only `sibling-obligation` and `sibling-advice`. M1 is the bug that control is aimed at: the enclosed extended-Indeterminate rule is not staged, and the Permit list becomes `[indeterminate-rule-obligation, sibling-obligation]`. Nothing in that result is wrong. Round 1 treated the obligation as never created; that description does not match this engine, and the new pair of assertions is what makes the scenario fail closed under M1.

The other four items are in the diff. Scenario 3 asserts an empty Deny advice list and `assertOrigins` on every Permit obligation and advice. The PDP test asserts one result and an origin on every obligation and advice; the PIP mock is required by `PolicyDecisionPointBuilder`. Scenario 4's first evaluation expects the outermost Permit list to be exactly `mixed-permit-rule-obligation`. Scenario 5 is `level1`–`level4` around `leaf`, with sorted id lists (duplicates preserved) and origin values.

**2. The new Permit-list assertions match the stated design.** The outermost composite is not staged, so children publish into the root context by effect. `getMatchingObligations` reads that publication. The PDP result, which keeps only the final decision, is a different observation and is what `NestedResponseStagingPdpTest` checks.

- Scenario 1 alone: the policy is outermost and Indeterminate, and its Permit rule still publishes `indeterminate-rule-obligation` by effect. The policy-level Permit obligation and advice stay out because the policy decision is Indeterminate. The enclosed run shows that same obligation is staged and not published.
- Scenario 4, first evaluation: `mixed` is enclosed, so it publishes only its own Permit obligation; `enclosing` is outermost and its decision is Deny, so that Permit obligation remains in the root Permit list while the Deny list is only the sibling. The empty Permit list is asserted on `outer`, where `enclosing` is staged and may publish only its Deny decision.

The assertion the design does not promise was an empty Permit list on the outermost set. That assertion is gone.

**3. Each scenario's assertions cover what that scenario claims.** Scenarios 3 and 5 still pass if staging is removed, because every response effect matches the final decision. They lock single resolution, cardinality, loss, duplication, and assignment values. M1 and M2 are locked by scenarios 1, 2, and 4: obligation-failure publication, extended-Indeterminate publication, and a rule effect that differs from the enclosing policy's decision, including advice (`failing-rule-advice`, `mixed-deny-rule-advice`).

No finding.

VERDICT: APPROVE
grok exit=0
