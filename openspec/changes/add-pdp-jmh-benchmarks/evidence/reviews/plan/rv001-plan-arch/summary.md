# rv001: plan review, architecture (task 1.1)

Date: 2026-10-26. Lane: `policy4j_arch_review_claude_fable`, Claude Code,
Claude Fable 5.1, read-only, in NTM session `xacml4j--pdp-jmh-plan`. Same
provider family as the plan's author; rv002 and rv003 are the lanes from other
families. Report: `report.md` (paths rewritten to repository-relative form).

Verdict: APPROVE WITH CHANGES.

| # | Severity | Finding | Resolution in the artifacts |
| --- | --- | --- | --- |
| F1 | major | No release hook for stores that hold resources; load benchmark leaks a repository per operation | D3, D4 `StoreSession`; spec "Store sessions are released"; D7 lifecycle operation |
| F2 | major | An exception from `decide` (in-memory null defect, 1.0 combining algorithm without catch) would escape the check | D5 records exceptions; spec scenario "A store loses a referenced policy" (empty and throwing) |
| F3 | major | Default shade replaces the thin jar and writes `dependency-reduced-pom.xml`; external stores cannot compile against the harness | D2 `outputFile`; D12; `.gitignore` line (task 2.2) |
| F4 | major | The test lane needs a fixed surface | D13 |
| F5 | minor | Snapshot `get` must be a direct lookup; reference cache is unbounded | D10; Context; beads note (task 1.2) |
| F6 | minor | Review-lane wording; no Grok or Kimi semantics lane | task 1.1 wording; waiver in the Execution Plan and gate 3.7 |
| F7 | minor | Duplicate names across jars | D3, D4; spec scenario "Duplicate store name" |
| F8 | nit | Checked exceptions, `PolicySource` equality, `MediaType` type, missing evidence row, multi-release manifest, manifest key escaping, shared `Random` | D4, D2, D9, evidence README, Context |

Verified by the conductor before acting: F2 (the `PolicySetIDReference` and
legacy deny-overrides paths), F3 (shade `outputFile` semantics as cited), F5
(`DefaultPolicyReferenceResolver.java:60-77`).
