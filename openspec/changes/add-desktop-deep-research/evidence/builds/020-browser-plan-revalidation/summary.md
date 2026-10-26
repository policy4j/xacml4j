# Browser plan revalidation, fourth version

Task 5.1 (`policy4j-971.5.1`), 2026-10-10. Structural checks of the plan
after it was revised for the eight findings of the confirming review rv012.
Planning structure only.
JDK, Maven, XACML conformance and JMH are not applicable.

## Result

PASS.

- `openspec validate add-desktop-deep-research --strict`: "Change
  'add-desktop-deep-research' is valid", exit 0, OpenSpec 1.6.0
  (`validate.txt`).
- `browser-research-session`: 11 requirements, 63 scenarios, 63 distinct
  named tests, none shared with the two desktop specs. Every requirement has
  a SHALL statement and one row in `evidence/README.md` with all of its test
  and live-check names; every task number in a row exists in `tasks.md`
  (`mapping.txt`, "mapping_issues=none").
- Tasks 5.1 to 5.14 and decisions D1 to D15 are present.

| Version | Reviewed in | Verdict | Scenarios | Tasks in group 5 |
| --- | --- | --- | --- | --- |
| First | rv005 | REQUEST_CHANGES | 37 | 9 |
| Second | rv006 to rv011 | six times REQUEST_CHANGES | 52 | 12 |
| Third | rv012 | REQUEST_CHANGES | 60 | 14 |
| Fourth | not reviewed | none | 63 | 14 |

The three specs together hold 27 requirements and 108 scenarios.

## Not covered

- Whether the fourth version is sound. No review has approved any version.
  Task 5.1 stays open.
- The 63 tests are names in a spec. None is written or run.
