# Run 047: donor pre-audit for task 6.3, combining algorithms (Agy)

Date: 2026-10-02. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity;
pane shows Gemini 3.8 Flash (High) (report self-label unreliable). Session
`policy4j--g1`, pane 3, 47 tool calls. Read-only: donor `b9f114a3`. Raw
report: [agy-report.txt](agy-report.txt) (A 10 defects, B worktree fixes to
keep, C tests).

Reported top defects: donor on-permit-apply-second never returns Deny for a
first-policy Deny and always evaluates the second policy (CS01 section 2.1);
donor PermitOverrides reverts the 4.21 fix (Deny before Indeterminate{P});
BaseCompositeDecisionRule evaluates obligations for NotApplicable and
Indeterminate (7.18); only-one-applicable returns plain Indeterminate without
processing-error; legacy deny-overrides returns Deny for an Indeterminate
child.

## Conductor adjudication (2026-10-02)

- Rejected: defect 5. XACML 1.0/2.0 policy-combining deny-overrides returns
  Deny when a child policy is Indeterminate (Appendix C pseudo-code:
  `if (decision == Indeterminate) return Deny;`). The donor
  (`b9f114a3:.../combine/impl/LegacyDenyOverridesPolicyCombineAlgorithm.java:56-75`)
  matches the spec; the worktree must keep that behavior.
- Consistent with the worktree history: defect 2 (donor PermitOverrides
  predates the 4.21 fix; run 040 B2).
- Unverified: defects 1, 3, 4 and the rest; the 6.3 packet's behavioral
  fingerprint tests (rv048 conductor decision) and the 3,048-case Appendix C
  matrix (run 025) decide them. Only-one-applicable's extended Indeterminate
  form and status code need the spec text quoted before a change.
