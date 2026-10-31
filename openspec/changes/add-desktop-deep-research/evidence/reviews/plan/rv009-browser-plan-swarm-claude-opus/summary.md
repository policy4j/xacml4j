# Plan review rv009: swarm lane Claude Opus

Task 5.1 (`policy4j-971.5.1`), 2026-10-09. One of six lanes of the swarm
review. It reviewed the second version of the browser plan in the
repository, read-only.
Reviewer: NTM persona `policy4j_arch_review_claude_opus`, Claude Opus 5.5,
effort max. The plan's author is the same model, so this lane is not an
independent family; the maintainer asked for it by name.
Focus: privacy and data flow.
JDK, Maven, XACML conformance and JMH are not applicable.

## Result

REQUEST_CHANGES: 3 HIGH, 3 MEDIUM. All accepted, one by cutting a feature.

| # | Severity | Finding | Disposition |
| --- | --- | --- | --- |
| 1 | HIGH | Account memory and search over past chats are not a "source" in the plan; a follow-up such as "summarize my other conversations" collects them through sanctioned commands | ACCEPTED. D13 and the execution requirement: memory, earlier-chat reference, past-chat search and connected applications are private sources and must be shown off, or the job blocks; two scenarios |
| 2 | HIGH | The driver's environment, working directory and error output are not controlled | ACCEPTED. D10 launch contract (see rv007) |
| 3 | HIGH | The redacted outline is circular: regions come from the profile that characterization produces, and list entries are controls whose names are titles | ACCEPTED. D15: no outline; probes return counts, uniqueness, ancestor roles and enabled state; the operator reads each first result; fixtures before characterization are labeled `structural` |
| 4 | MEDIUM | Playwright starts Chrome with a mock keychain, so a sign-in made without the driver is unreadable to the driven browser; password saving, sync and backups | ACCEPTED. The two switches are confirmed in the candidate source (run 018). D11: the driver removes them, `login` writes profile settings first, the profile is excluded from backups; task 5.3 proves it on a fixture profile |
| 5 | MEDIUM | For plan approval the "control outside the model" is the calling agent reading provider text, which a context report can address | ACCEPTED by cutting the context job (D13; follow-up issue `policy4j-6p67`); the plan start now reads the source set again in the driver (D12) |
| 6 | MEDIUM | Two sentences still said snapshots stay in private files; private files are not hidden from a caller with the operator's rights; a salted hash of an account label can be confirmed; export names come from the provider | ACCEPTED. Sentences removed; D13 "What this boundary is"; commands print `account_unchanged` and no account value; saved files get generated names |

Also accepted: the claim about two capability switches that the candidate
version does not list (dropped with the driver change); the composer hash
for a rich-text composer (D12: a normal form per provider, or no-go); a
dedicated Google account as a precondition for Gemini (D11, asked in 5.2).

## Files

- `packet.md`, `report.md`, `command.txt`: as in rv006.
