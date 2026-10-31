# Browser plan validation

Task 5.1 (`policy4j-971.5.1`), 2026-10-09. Structural checks of the extended
plan: proposal, research, design D9 to D15, the new capability
`browser-research-session`, task group 5 and the evidence index. Planning
structure only; no test of the browser surface exists yet.
JDK, Maven, XACML conformance and JMH are not applicable.

## Result

PASS.

- `openspec validate add-desktop-deep-research --strict`: "Change
  'add-desktop-deep-research' is valid", exit 0, OpenSpec 1.6.0
  (`validate.txt`).
- `browser-research-session`: 11 requirements, 37 scenarios, 37 distinct
  named tests, none shared with the two existing specs. Every requirement
  has a SHALL statement, every scenario has GIVEN, WHEN, THEN and a Test
  line, and every requirement has one row in `evidence/README.md` that lists
  all of its test and live-check names (`mapping.txt`, "mapping_issues=none").
- Tasks 5.1 to 5.9 and decisions D1 to D15 are present.

The three specs together now hold 27 requirements and 82 scenarios.

## Not covered

- Whether the plan is sound: that is the plan review in
  `evidence/reviews/plan/`.
- The 37 tests are names in a spec. None is written or run.
