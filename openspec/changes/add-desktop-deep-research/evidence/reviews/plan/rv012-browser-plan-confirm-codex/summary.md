# Plan review rv012: confirming review of the third version

Task 5.1 (`policy4j-971.5.1`), 2026-10-10. One reviewer read the third
version of the browser plan, which had been written for the findings of the
six swarm reviews rv006 to rv011, together with the dispositions of all
earlier reviews.
Reviewer: Codex CLI 0.162.1, one-shot, read-only sandbox, the CLI's
configured default model (not recorded by the run). It saw only the packet.
JDK, Maven, XACML conformance and JMH are not applicable.

## Result

REQUEST_CHANGES: 3 HIGH, 4 MEDIUM, 1 LOW. All accepted; the plan is now in
its fourth version, which no reviewer has read. The findings are about the
exactness of guarantees. The reviewer found the driver choice, the
reservation rule, the request tuple, the child-job exceptions, the two cuts
and the stated limit of the acknowledgement sound in principle.

| # | Severity | Finding | Disposition |
| --- | --- | --- | --- |
| 1 | HIGH | A fill can trigger a page handler that submits with a delay; "cannot submit" is not achievable, and a send after it could submit twice | ACCEPTED. D12: the entry contract is what the driver can see: no submit key, a settle time from characterization, read-back, no new user turn and no generation; a provider that can submit unseen within that time is a no-go for automatic send; fixture for delayed self-submission |
| 2 | HIGH | The persistent browser profile keeps cache, history and site data of pages, also before a job is bound; "nothing stored" claimed more than the controls give | ACCEPTED. D11 "What the profile holds" and D13: the storage rule is about the workflow's own files; the browser's storage is named, never read by the workflow, and under the operator's control. Requirement and one scenario reworded and added |
| 3 | HIGH | `answer` did not read model and sources again, and `begin` not the model | ACCEPTED. D12: every action that submits content or starts research reads model and sources again, including memory; scenario "Model or sources change after preparation" |
| 4 | MEDIUM | The spec promised more than the admitted gap between the last check and the input event | ACCEPTED. D12 "Navigation": a navigation or loss of the target seen before the input event cancels; after it the job is uncertain. Requirement and scenario reworded |
| 5 | MEDIUM | Headless needed a headless characterization that could not be made | ACCEPTED. D11 and task 5.10: a separate observation without input after the headed characterization records headless as allowed; scenario |
| 6 | MEDIUM | A scenario said that sign-in uses no network | ACCEPTED. The no-network statement covers listing, audit and the local readiness check only |
| 7 | MEDIUM | That a headed window does not take the foreground is unproven, and the fixtures were headless | ACCEPTED. Task 5.3 and the test strategy: one headed check with another application in front, including a restart; the headed default depends on it |
| 8 | LOW | Migration said the desktop surface is unaffected, while the reservation rule changes the shared orchestrator | ACCEPTED. Migration names both shared changes and their regression checks |

## Limits

- One reviewer on a packet; it did not read the repository.
- This is the third review round. No round has ended with an approval.

## Files

- `prompt-header.md`: the instructions that preceded the plan files.
- `packet.txt`: content, size and hash of the packet.
- `report.md`: the reviewer's report, complete.
- `command.txt`: the invocation.
