# Plan review rv011: swarm lane Grok

Task 5.1 (`policy4j-971.5.1`), 2026-10-09. One of six lanes of the swarm
review. It reviewed the second version of the browser plan in the
repository, read-only, with no fetches.
Reviewer: Grok CLI in an NTM pane, Grok 4.7, effort xhigh as requested at
spawn; no persona, the architecture-review role prompt was sent with the
packet.
Focus: adversarial lane; concrete attacks on the plan as written.
JDK, Maven, XACML conformance and JMH are not applicable.

## Result

REQUEST_CHANGES: 5 HIGH, 2 MEDIUM. All accepted, two by cutting features.

| # | Severity | Finding | Disposition |
| --- | --- | --- | --- |
| 1 | HIGH | The send guard was a script in the page's own realm; a page can wrap the click function and act between check and activation | ACCEPTED. D12: no guard and no counter runs as page script; the driver reads through locator queries and activates through the browser's input path; scenario for a page that replaces built-in functions |
| 2 | HIGH | Entry by typing can submit on Enter, and a later send submits again | ACCEPTED. D12: entry is one operation that cannot submit; a user turn that appears during entry is a possible send and is never followed by a second send; scenario |
| 3 | HIGH | Restored tabs: the action can land in an existing conversation | ACCEPTED. D11 page ownership; D12: a first send needs a conversation without a user turn; two scenarios |
| 4 | HIGH | The acknowledgement can be passed without the operator: replaceable key, password dialog that automation can drive, changed terms never block | ACCEPTED. D14: biometric authentication only, no key, a thirty-day period, and the stated limit that a process with the operator's rights can write the file. The limit is accepted, not removed |
| 5 | HIGH | The plan start did not read the sources again; a follow-up could run beside another active job of the provider | ACCEPTED. D12 precondition of `begin`; D13 follow-up precondition and the reservation rule of D9 |
| 6 | MEDIUM | A challenge drawn as an overlay keeps origin, landmarks and account, and headless would click Send | ACCEPTED. D11 positive challenge predicate; scenario "A challenge covers the page" |
| 7 | MEDIUM | "The file that this action produced" is ambiguous when the page starts a second download | ACCEPTED. D12: one armed download, any other new file fails the attempt; scenario |

Cuts the reviewer asked for, all taken: the in-page guard and counter; the
context job and the research-mode follow-up (follow-up issues
`policy4j-6p67` and `policy4j-j5ft`); the desktop-wide gate, which is now a
question for the maintainer and not part of this plan.

## Files

- `packet.md`, `report.md`, `command.txt`: as in rv006.
