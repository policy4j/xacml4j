# rv054: rv049/rv050 fix verification, tasks 4.1 and 4.2 safety-net tests (Agy)

Date: 2026-10-02. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity;
pane shows Gemini 3.8 Flash (High) (report self-label unreliable). Session
`policy4j--g1`, pane 3. Read-only: lane g41 v2 patch (whole change) and
evidence run 038.

Verdict: APPROVE.

- rv050-1 FIXED: PIP-supplied value row (Permit/ok); `verify(pip)` on the
  resolver-failure rows; selector overload stubbed (success and failure).
- rv050-2 FIXED: attribute-designator function, existing category and
  entity: absent + false -> NotApplicable/ok with empty bag asserted; + true
  -> missing-attribute; absent-category rows disabled with policy4j-the.4.33.
- rv050-3 FIXED: issuer set + attribute issuer null + MustBePresent=true ->
  Indeterminate/missing-attribute; Deny-effect rule: absent and true
  conditions -> Deny/ok, indeterminate -> INDETERMINATE_D/processing-error.
- rv049-2 FIXED: StatusCodeId.OK asserted on issuer and function decisions.
- Also: wrong-category and wrong-data-type rows -> NotApplicable/ok; every
  disabled test carries its bead (4.32, 4.33, 4.34, 4.43); the conductor's
  4.43 relabel is correct (DefaultFunctionInvocation loses the status);
  tests only; LGPL headers present.

Merge (2026-10-02 02:25): lane g41 applied to the main worktree. The lane
predates the merged 4.32 fix, so the conductor re-ran the safety-net classes
in a check lane with the 4.32 test enabled (ConditionSafetyNetTest 4 tests,
0 failures, 0 skipped; AttributeResolutionSafetyNetTest 21 tests, 5 skipped)
and removed that `@Disabled` in the worktree.
