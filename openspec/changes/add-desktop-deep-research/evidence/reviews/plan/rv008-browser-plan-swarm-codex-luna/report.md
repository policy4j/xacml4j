===REPORT-BEGIN codex-luna===
Profile and model: `policy4j_arch_review_codex_luna`; Codex, GPT-6 Luna, effort UNVERIFIED

VERDICT: REQUEST_CHANGES

Findings, most severe first.

1. **MEDIUM — `design.md` D11; `specs/browser-research-session/spec.md` “Apply the execution rules”; `tasks.md` 5.4–5.5.** D11 requires one driver per profile and a profile lock, but the browser spec has no scenario for concurrent commands contending on that lock; the desktop lock test does not prove browser-profile serialization. Add a production-entrypoint scenario for cross-process/profile contention and lock release after process exit, then map it to 5.4 and 5.5.

2. **MEDIUM — `tasks.md` 5.9–5.10.** These tasks rely on collection validation from 3.2 and documentation ownership from 3.3, but group 5 declares no dependency on either. A 5.9 implementation can therefore start before its validator exists, and 5.10 can overlap the task that owns the shared docs. Add explicit dependencies or sequence these tasks after 3.2 and 3.3.

3. **LOW — `tasks.md` 5.5–5.6.** Each implementation task combines several independently reviewable boundaries; 5.6 especially bundles the MCP client, DOM guards, authentication and key storage, login, characterization, and fixtures. Split the acknowledgement/key-storage work from bridge and characterization work, with a focused security check before the provider-page gate.

Unsupported or overstated claims: The 52-test mapping establishes one distinct test name per scenario, not that all behaviors are verified end to end. Synthetic-boundary tests can verify orchestrator decisions; bridge behavior depends on the separate fixture run, and provider-interface behavior remains UNVERIFIED until 5.8. The research README explicitly says no provider web interface was opened.

What should be cut or moved to its own change: Nothing established from this read-only review.

Checked and found sound: Explicit surface selection and cross-surface uncertainty; separate synthetic, fixture, and live evidence; fail-closed follow-up binding and context-job hashing; D14’s explicit statement that acknowledgement is not provider permission; no project MCP configuration change.

What you read and ran: Read `AGENTS.md`, `docs/ai/SWARMS.md`, the packet-named proposal, design D9–D15, browser and desktop specs, tasks, research README, prior `rv005` report, and `evidence/builds/017-browser-plan-revalidation/mapping.txt`. Ran focused `rtk proxy sed` and `rtk rg` reads/searches. Did not run validation, tests, a browser, or external research. Strict validation passing is supplied packet evidence, not re-run here.
===REPORT-END codex-luna===
