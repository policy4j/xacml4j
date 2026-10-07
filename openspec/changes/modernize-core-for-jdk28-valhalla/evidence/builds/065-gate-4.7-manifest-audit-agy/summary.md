# Run 065: conformance manifest audit for the group 4 gate (task 4.7) (Agy)

Date: 2026-10-02. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity;
pane shows Gemini 3.8 Flash (High) (self-label unreliable). Session
`policy4j--g1`, pane 3, 29 tool calls. Read-only: current manifest vs the
run 015 baseline, `vendored-fixtures.sha1`, beads (read-only `br show`).

Result: improved, no regression.
- Enabled cases 836 -> 863 (+27: +2 OASIS 2.0, +25 OASIS 3.0); disabled
  44 -> 17.
- Request/policy/response paths: 0 differences across all 880 cases.
- Every skipped or disabled row has a bead id; no placeholders.
- Vendored fixtures: `git status` shows 6 modified files (Example_2
  Rule_1-3, oasis20 IIIF002Request, IIIF007Policy, IIIG006Policy); all match
  the integrity manifest (pre-existing state restored in task 1.5); this
  change touched none.

Needs attention: oasis20 IIIC001 is still `skipped,false` with bead
policy4j-the.4.19, which is fixed and closed (2.0 Result/@ResourceId);
oasis30 IIIC001 already passes. Action: try to enable it in the next
manifest update.

Remaining skips map to open beads/decisions: 4.6 (IIC020 integer-divide),
4.17 (IIA005), 4.18 (IIIC002, IIIC003 hierarchical), 3.9 (IIA023, IID029,
IIIG300), and the others listed in the report.
