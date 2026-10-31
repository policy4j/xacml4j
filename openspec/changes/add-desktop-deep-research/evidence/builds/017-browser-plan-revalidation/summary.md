# Browser plan revalidation

Task 5.1 (`policy4j-971.5.1`), 2026-10-09. Structural checks of the plan
after it was revised for the nine findings of review rv005. This is the
version that went to the swarm review. Planning structure only.
JDK, Maven, XACML conformance and JMH are not applicable.

## Result

PASS.

- `openspec validate add-desktop-deep-research --strict`: "Change
  'add-desktop-deep-research' is valid", exit 0, OpenSpec 1.6.0
  (`validate.txt`).
- `browser-research-session`: 11 requirements, 52 scenarios, 52 distinct
  named tests, none shared with the two desktop specs. Every requirement has
  a SHALL statement and one row in `evidence/README.md` with all of its test
  and live-check names; every task number in a row exists in `tasks.md`
  (`mapping.txt`, "mapping_issues=none").
- Tasks 5.1 to 5.12 and decisions D1 to D15 are present; every task number
  that the design, the proposal and the spec refer to exists.

Compared with run 015: 37 scenarios became 52, and nine tasks became twelve.
The three specs together hold 27 requirements and 97 scenarios.

## Not covered

- Whether the revised plan is sound: see the reviews in
  `evidence/reviews/plan/`.
- The 52 tests are names in a spec. None is written or run.
