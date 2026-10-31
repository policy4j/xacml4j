# Plan review rv007: swarm lane Codex Sol

Task 5.1 (`policy4j-971.5.1`), 2026-10-09. One of six lanes of the swarm
review. It reviewed the second version of the browser plan in the
repository, read-only, and read the driver's source on GitHub.
Reviewer: NTM persona `policy4j_arch_review_codex_sol`, Codex, GPT-6.1 Sol,
effort max as configured (not verified by the run).
Focus: security of the driver and the bridge.
JDK, Maven, XACML conformance and JMH are not applicable.

## Result

REQUEST_CHANGES: 3 HIGH, 3 MEDIUM. All accepted. This review changed the
driver of the design.

| # | Severity | Finding | Disposition |
| --- | --- | --- | --- |
| 1 | HIGH | The MCP server records console messages and saves downloads by itself | ACCEPTED. Confirmed in the installed package (run 018). D10: the driver is a script on the Playwright library, which writes nothing unless asked; downloads are denied except for one armed step |
| 2 | HIGH | The lock does not cover launch configuration: the server reads environment variables, one of which loads a file as code | ACCEPTED. Confirmed (run 018). D10 launch contract: fixed environment, verified Node binary, private working directory, options only in the request; scenario for a hostile environment |
| 3 | HIGH | The atomic guard omits the approved plan, sources, model and account | ACCEPTED. D12 preconditions per action |
| 4 | MEDIUM | Element references survive a new snapshot | ACCEPTED. D12: targets are resolved again for every action, with an observation number |
| 5 | MEDIUM | The evaluate tool has no argument channel, so a "fixed snippet" cannot take the expected hash | ACCEPTED. Confirmed (run 018). The page-script guard is gone; the library passes arguments to evaluated functions |
| 6 | MEDIUM | Characterization needs regions that only characterization produces | ACCEPTED. D15: probes from a reviewed vocabulary, seed regions reviewed by the operator, `structural` and `profile` fixtures |

The reviewer's request for a decision record on the driver's execution
boundary is passed to the maintainer in task 5.2 (D15).

## Files

- `packet.md`, `report.md`, `command.txt`: as in rv006.
