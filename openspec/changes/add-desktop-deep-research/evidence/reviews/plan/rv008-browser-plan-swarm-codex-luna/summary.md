# Plan review rv008: swarm lane Codex Luna

Task 5.1 (`policy4j-971.5.1`), 2026-10-09. One of six lanes of the swarm
review. It reviewed the second version of the browser plan in the
repository, read-only.
Reviewer: NTM persona `policy4j_arch_review_codex_luna`, Codex, GPT-6 Luna,
effort high as configured (not verified by the run).
Focus: specification and tasks.
JDK, Maven, XACML conformance and JMH are not applicable.

## Result

REQUEST_CHANGES: 2 MEDIUM, 1 LOW. All accepted.

| # | Severity | Finding | Disposition |
| --- | --- | --- | --- |
| 1 | MEDIUM | No scenario for two commands that want the same browser profile | ACCEPTED. Scenario "Two commands want the same profile", test `test_profile_lock_serializes_processes_without_waiting` |
| 2 | MEDIUM | Tasks for collection and documentation did not state that they wait for tasks 3.2 and 3.3 | ACCEPTED. The head of task group 5 lists the waits; the tracker has the dependencies |
| 3 | LOW | Task 5.6 bundled the bridge, authentication, key storage, sign-in, characterization and fixtures | ACCEPTED. Now three tasks: driver script and bridge (5.6), acknowledgement and sign-in preparation (5.7), probes (5.8), before the gate (5.9) |

The reviewer's remark that 52 test names show a mapping and not verified
behavior is taken into the test strategy: a synthetic test shows a decision
of the orchestrator, not the driver's or a provider's behavior.

## Files

- `packet.md`, `report.md`, `command.txt`: as in rv006.
