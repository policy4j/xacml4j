# Browser plan revalidation, third version

Task 5.1 (`policy4j-971.5.1`), 2026-10-09. Structural checks of the plan
after it was revised for the findings of the six swarm reviews rv006 to
rv011. Planning structure only.
JDK, Maven, XACML conformance and JMH are not applicable.

## Result

PASS.

- `openspec validate add-desktop-deep-research --strict`: "Change
  'add-desktop-deep-research' is valid", exit 0, OpenSpec 1.6.0
  (`validate.txt`).
- `browser-research-session`: 11 requirements, 60 scenarios, 60 distinct
  named tests, none shared with the two desktop specs. Every requirement has
  a SHALL statement and one row in `evidence/README.md` with all of its test
  and live-check names; every task number in a row exists in `tasks.md`
  (`mapping.txt`, "mapping_issues=none").
- Tasks 5.1 to 5.14 and decisions D1 to D15 are present; every task number
  that the design, the proposal and the spec refer to exists.

| Version | Reviewed in | Requirements | Scenarios | Tasks in group 5 |
| --- | --- | --- | --- | --- |
| First | rv005 | 11 | 37 | 9 |
| Second | rv006 to rv011 | 11 | 52 | 12 |
| Third | not yet reviewed | 11 | 60 | 14 |

The three specs together hold 27 requirements and 105 scenarios.

## What changed from the second version

- The driver: the Playwright library in a script that the bridge owns, with
  a launch contract, in place of the MCP server (design D10, run 018).
- Guards: preconditions per action in the driver, no script in the page
  (D12).
- A reservation rule for the provider and a fixed request tuple (D9).
- Memory and earlier chats count as private sources (D13).
- Cut: passing a report to another provider, and a second research run in
  one conversation (D13).
- The acknowledgement: biometric authentication and a plain receipt, with
  its limit stated; no signed receipt (D14).
- Characterization by probes that return counts, not by an outline (D15).

## Not covered

- Whether the third version is sound. Seven reviews asked for changes on
  the first two versions; no reviewer has read the third. Task 5.1 stays
  open for that.
- The 60 tests are names in a spec. None is written or run.
