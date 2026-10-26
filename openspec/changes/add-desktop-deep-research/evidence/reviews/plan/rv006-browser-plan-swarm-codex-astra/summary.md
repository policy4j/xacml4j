# Plan review rv006: swarm lane Codex Astra

Task 5.1 (`policy4j-971.5.1`), 2026-10-09. One of six lanes of the swarm
review that the maintainer asked for. It reviewed the second version of the
browser plan (after rv005) in the repository, read-only.
Reviewer: NTM persona `policy4j_arch_review_codex_astra`, Codex, GPT-6
Astra, effort max as configured (the serving variant is not verified).
Focus: architecture and state machine.
JDK, Maven, XACML conformance and JMH are not applicable.

## Result

REQUEST_CHANGES: 3 HIGH, 4 MEDIUM. All accepted; the plan is now in its
third version, which this reviewer has not seen.

| # | Severity | Finding | Disposition |
| --- | --- | --- | --- |
| 1 | HIGH | A challenge after a send makes the job `BLOCKED`, which the orchestrator treats as terminal, so the provider is free again while the outcome is unknown | ACCEPTED. Design D9 reservation rule; new requirement "Keep the provider reserved until the outcome is known" with three scenarios. The same defect in the approved desktop code is issue `policy4j-971.2.5` |
| 2 | HIGH | The send guard did not cover the approved plan, the pending question, the model or the sources | ACCEPTED. D12 table of preconditions per action; scenario for a plan that changes after approval |
| 3 | HIGH | The driver writes console messages to a file before the bridge can filter | ACCEPTED. Confirmed in the installed package (run 018). The driver is now a script on the Playwright library (D10); scenario "The page logs text and starts a download by itself" |
| 4 | MEDIUM | A research child that asks a question has its report outside the collection interval | ACCEPTED by cutting the second research run in a conversation (D13); follow-up issue `policy4j-j5ft` |
| 5 | MEDIUM | Request identity for child jobs is not defined | ACCEPTED. D9 request tuple; scenario for a reused request id |
| 6 | MEDIUM | A driver session kept by `wait` has no lock protocol | ACCEPTED. No driver session outlives a command; the profile lock is taken without waiting (D11); scenario for two commands on one profile |
| 7 | MEDIUM | The driver does not reject an old element reference | ACCEPTED. No reference outlives an observation; actions carry an observation number (D12) |

Also accepted: the sentence "Without `--surface browser` nothing changes"
and the risk line about snapshots in private files were wrong and are
rewritten. The reviewer's request for a decision record on the reservation
rule is passed to the maintainer in task 5.2 (D15).

## Files

- `packet.md`: the packet sent to the pane, after the role prompt.
- `report.md`: the lane's report, complete, taken from the CLI's session
  record because a terminal pane keeps only the last screen.
- `command.txt`: how the swarm was run.
