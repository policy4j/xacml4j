===REPORT-BEGIN codex-astra===
FINAL REPORT (policy4j_arch_review_codex_astra, GPT-6)
Profile and model: Codex; configured `gpt-6-astra`, effort `max` (`.ntm/personas.toml:465`). Actual serving variant/effort UNVERIFIED.
VERDICT: REQUEST_CHANGES

Static review: the revised plan still has gaps in reservation lifetime, action guards, and child-turn ownership. Attack sequences below were not executed.

Plan paths are relative to `openspec/changes/add-desktop-deep-research/`. `R` means `.agents/skills/desktop-deep-research/scripts/desktop_research.py`. `P` means the installed candidate’s `@playwright/mcp/node_modules/playwright-core/lib/coreBundle.js`.

Findings, most severe first:

1. **HIGH — D9/D11: a challenge can release an unresolved provider reservation.** D11 assigns `BLOCKED` to challenges (`design.md:487`), but the existing orchestrator classifies `BLOCKED` as terminal and excludes it from competing-job admission checks (`R:33`, `R:549`). Sequence: Send → challenge → `BLOCKED` → new request on the other surface, while the original outcome remains unknown. **Change:** separate observation availability from reservation lifetime; release the reservation only through positive reconciliation. Test possibly-sent `BLOCKED`/`FAILED` jobs across both surfaces.

2. **HIGH — D12: the atomic guard does not protect the approved interaction.** The prescribed snippet checks origin, address, control, composer hash and input counter, but not the approved plan hash (`design.md:529`). Sequence: approve plan A → asynchronous replacement with plan B → Begin; no operator input or composer change is needed. **Change:** define action-specific atomic preconditions: approved-plan hash for `begin`, pending-question hash for `answer`, plus current account/model/source evidence. Test changes without keyboard or pointer events.

3. **HIGH — D10/D13: filtering driver responses cannot prevent driver-side persistence.** The candidate source registers console listeners and routes messages into `appendFile` (`P:57912`, `P:58030`, `P:57459`), before bridge filtering. A fixture emitting unrelated text through `console.error` before marker binding therefore challenges the “nothing stored before binding” guarantee (`design.md:639`); restricting the output directory does not solve it. **Change:** specify verified driver logging suppression before navigation and test all resulting files, including console errors and page errors, against the locked package.

4. **MEDIUM — D13: exception four excludes a research child’s legitimate final report.** Collection stops at the next user turn (`design.md:601`), but inherited clarification handling sends another user turn (`R:705`). A research child that asks a question, receives `respond`, then finishes has its report outside the permitted collection interval. **Change:** persist ownership of continuation turns and bind completion/collection to the final result of that job; test clarified research children and unchanged parent collection.

5. **MEDIUM — D9/D13: request identity is underspecified for children.** Existing request and batch fingerprints omit surface, parent and mode (`R:428`, `R:780`); D13 adds only the context-report hash explicitly (`design.md:610`). Reusing identical request text/id for another parent or mode can resolve to the earlier job under inherited allocation rules. **Change:** define the immutable request tuple, including surface, parent/conversation, mode and context identity; reject conflicting retries before page access.

6. **MEDIUM — D11: retained driver sessions lack a compatible lock protocol.** `wait` retains the profile’s driver while polling (`design.md:491`), whereas existing polls reacquire the ledger through `Store.job` (`R:262`, `R:648`). If another command holds that ledger while waiting for the profile, the next poll reverses lock acquisition; holding the ledger throughout the wait instead blocks unrelated jobs. **Change:** specify session ownership and nonblocking profile acquisition, with no profile wait under the ledger; test concurrent wait/status and cross-surface commands.

7. **MEDIUM — D12: snapshot-reference expiry is not a driver guarantee.** The installed candidate preserves a node’s reference while its role/name remain unchanged and resolves it through the current snapshot (`P:14514`, `P:58205`). Taking another snapshot therefore does not necessarily reject the earlier reference, contrary to `design.md:526`. **Change:** enforce observation generations in the bridge and test a surviving reference whose surrounding conversation or application region changes.

Unsupported or overstated claims:

- “Without `--surface browser` nothing changes” (`design.md:896`) conflicts with D14’s new desktop acknowledgement gate.
- “Snapshots stay in private files” (`design.md:878`) contradicts D13’s revised storage restriction.
- Runtime safety and locked-package behavior remain UNVERIFIED.

What should be cut or moved to its own change: research-mode follow-ups until continuation-turn ownership is defined. An ADR is warranted for provider-reservation lifetime across surfaces.

Checked and found sound: explicit surface selection (D9); dedicated operator-signed-in profiles (D11); explicit context references and collision-resistant framing (D13); separate synthetic, fixture and live acceptance (Test Strategy).

What you read and ran: all packet files, AGENTS.md and procedure documents; orchestrator and relevant execution tests; installed candidate package metadata and source. Ran focused `rtk rg`, `rtk proxy sed`, `which`, `readlink`, `shasum`, and scoped `rtk git diff --stat`. Context7 fetched official [Playwright MCP target documentation](https://github.com/microsoft/playwright-mcp/blob/main/_autodocs/api-reference/tools-overview.md). No files changed, scripts/tests executed, browsers started, or provider pages opened. Strict validation was supplied evidence, not rerun. Memory supplied orientation only.

Residual risk: these are static findings; the globally installed candidate’s integrity was not established. Reviewed design SHA-256: `4635840a1b6da14bfadfb23ef3cfbbd1ec7456015e9defb688e726ab5798e03d`.
===REPORT-END codex-astra===
