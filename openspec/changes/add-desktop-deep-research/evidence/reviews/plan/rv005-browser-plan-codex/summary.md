# Plan review rv005: browser surface, round 1

Task 5.1 (`policy4j-971.5.1`), 2026-10-09. Independent review of the first
version of the browser plan (design D9 to D15, capability
`browser-research-session` with 37 scenarios, task group 5 with nine tasks).
Reviewer: Codex CLI 0.162.1, one-shot, read-only sandbox, the CLI's
configured default model (not recorded by the run). The plan author is
another model family (Claude Opus 5.5). The reviewer saw only the packet.
JDK, Maven, XACML conformance and JMH are not applicable.

## Result

REQUEST_CHANGES: 3 HIGH, 6 MEDIUM. All nine were accepted and the plan was
revised. The revision was not seen by this reviewer; it went to the swarm
review that follows this run.

| # | Severity | Finding | Disposition |
| --- | --- | --- | --- |
| 1 | HIGH | Check and action are separate driver calls; a role and name do not show that a control belongs to the application | ACCEPTED. D12 "Guards at the moment of action": one operation per mutation, in-page guard for Send, region rule, input counter, hostile fixtures. Five new scenarios |
| 2 | HIGH | Full snapshots and page text in private files, and an agent at the page during characterization | ACCEPTED. D13: filter in memory, store only the bound job's content and control facts, nothing before binding. D15: characterization only through a reviewed harness with a redacted outline. Three new scenarios |
| 3 | HIGH | An interactive terminal does not prove the operator; only start and follow-up were gated; desktop coverage open | ACCEPTED. D14: device-owner authentication, signed receipt, a list of gated and ungated operations, one proposed rule for both surfaces, the remaining limit stated. Scenarios for a pseudo-terminal and a forged receipt |
| 4 | MEDIUM | Chat-mode child jobs conflict with the research-only rules | ACCEPTED. D13 and the follow-up requirement state four exceptions for child jobs; two new scenarios; the execution requirement lists its substitutions |
| 5 | MEDIUM | The three terms texts restrict different things; acknowledgement is not permission; "route without this restriction" | ACCEPTED. D14 and research Q8 give text and inference per provider; acknowledgement described as acceptance of possible non-compliance; API routes described as supported under their own terms |
| 6 | MEDIUM | A label does not make the receiving model treat a report as reference; no escaping | ACCEPTED. D13: boundary-token encoding, marker kept unique, controls outside the model, derived label, hostile-report scenario |
| 7 | MEDIUM | "Never reads or stores cookies" contradicts a persistent browser | ACCEPTED. D10 and D11 name the browser and the driver as trusted components and limit the rule to the workflow's own components; driver error text is not forwarded |
| 8 | MEDIUM | Research revision not mapped to the candidate version; installed hash is not provenance; API reachability; "confirms" | ACCEPTED. D10: lock file with dependency closure and registry integrity, stored tool list, global copy not used. Run 016 compares the options with the help output of 0.0.73. API use labeled UNVERIFIED. "documents" replaces "confirms" |
| 9 | MEDIUM | Characterization before the acknowledgement and the restricted bridge; implicit dependencies | ACCEPTED. Group 5 now has twelve tasks: bridge, acknowledgement and harness (5.6) and a gate (5.7) come before characterization (5.8); collection depends on 3.2, documentation on 3.3; 4.3 and 4.4 wait for 5.12 |

Found sound by the reviewer: explicit surface selection and the shared
uncertainty block; dedicated profiles, operator sign-in and no evasion of
challenges; separate synthetic, fixture and live evidence; explicit request
for exports that write to the provider account.

## Limits

- One reviewer, one round, on a packet. It did not read the repository and
  did not verify the validation result.
- Two sentences were added to design D10 and D14 after the packet was built
  and before the report came back (an alternative in D10, the note that the
  gate is a record and not a control). The reviewer did not see them.

## Files

- `prompt-header.md`: the instructions that preceded the plan files.
- `packet.txt`: size and hash of the full packet; the plan files of that
  time are superseded by the current ones.
- `report.md`: the reviewer's report, complete.
- `command.txt`: the invocation.
