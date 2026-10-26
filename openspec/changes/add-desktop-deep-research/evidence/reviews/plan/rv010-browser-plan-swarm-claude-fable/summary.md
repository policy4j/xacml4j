# Plan review rv010: swarm lane Claude Fable

Task 5.1 (`policy4j-971.5.1`), 2026-10-09. One of six lanes of the swarm
review. It reviewed the second version of the browser plan in the
repository, read-only, and fetched the three terms pages again.
Reviewer: NTM persona `policy4j_arch_review_claude_fable`, Claude Fable 5.1,
effort max.
Focus: provider terms and the acknowledgement.
JDK, Maven, XACML conformance and JMH are not applicable.

## Result

REQUEST_CHANGES: 2 HIGH, 3 MEDIUM, 2 LOW. Four accepted in full, three in
part (one point of finding 6 was not accepted). The reviewer confirmed the quotes of research Q8 against the pages.

| # | Severity | Finding | Disposition |
| --- | --- | --- | --- |
| 1 | HIGH | A key that the system releases only after authentication needs entitlements that a locally compiled command-line helper cannot carry; the signed receipt is infeasible as planned (Apple developer forum answers, not tested) | ACCEPTED. D14: no signed receipt. A plain receipt is written after biometric authentication, and the limit of that is stated. A signed helper application is follow-up issue `policy4j-69t3` |
| 2 | HIGH | "One rule for both surfaces" was a sentence in D14 with no requirement in the desktop spec and no task; desktop live runs already happened without the terms question | ACCEPTED IN PART. The plan no longer claims a rule for the desktop surface and does not change the desktop specs. D14 states that the earlier approval came before the question was raised, asks the maintainer in task 5.2, and tasks 4.1 and 4.2 wait for the answer |
| 3 | MEDIUM | The receipt is not bound to the account, the operating-system user or the terms variant | ACCEPTED IN PART. The receipt records the user and is bound to the account on first use; another account needs a new acknowledgement (scenario). The variant is recorded only through the address of the terms; D14 says the texts read are those served in the United States |
| 4 | MEDIUM | "Current" meant "exists"; fetching the terms can fail or be partial; a "local" check needed the network | ACCEPTED. Thirty-day period and withdrawal; no fetch and no hash comparison; `doctor --local` uses no network |
| 5 | MEDIUM | Authentication proves that someone who can unlock the computer answered, not that anyone read the text | ACCEPTED. D14 "What an acknowledgement is"; the command prints the address first; the dialog names provider and date |
| 6 | LOW | Details of the gated list | ACCEPTED for the batch (checked per entry) and the scenario name. NOT ACCEPTED for `inspect --job`: in the existing code it observes the page, so it stays gated |
| 7 | LOW | D14 left out two quoted clauses | ACCEPTED. Anthropic's clause on harvesting and OpenAI's on protective measures are in D14 |

Not verified by the conductor: the effective dates that the reviewer read
on the Anthropic and Google pages (2025-10-08 and 2026-07-30).

## Files

- `packet.md`, `report.md`, `command.txt`: as in rv006.
