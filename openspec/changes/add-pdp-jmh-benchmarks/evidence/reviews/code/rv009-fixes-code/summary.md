# rv009: re-review of the review-gate fixes (task 3.7)

Date: 2026-10-27. Lane: `policy4j_code_review_codex_sol`, Codex CLI, GPT-6.1 Sol, read-only, in the
rv006 pane of `xacml4j--pdp-jmh-review`. Scope: design D14, the changed spec requirements, and every
changed or new file of the fix batch. Report: `report.md` (the full report followed a first short reply
without markers; the conductor asked for it).

Verdict: APPROVE WITH CHANGES.

| Finding | Status | Disposition |
| --- | --- | --- |
| rv006 F1 wildcard oracle | RESOLVED | - |
| rv006 F2 concurrent lookups | RESOLVED | - |
| rv006 F3 ids across forks | RESOLVED | - |
| new (first reply): load preflight closes the fixture only on unchecked failures | RESOLVED in the full report | fixed before the full report: `catch(Throwable)` with precise rethrow; test `loadSetUpClosesTheFixtureWhenAPreflightSessionThrowsACheckedException`, red on the old catch (expected 0 open sessions, was 1), green after (`evidence/tests/018-review-fix-red-green` runs c, d) |
| new minor: tear-down loses the check failure when close also fails | - | fixed: `try(opened){ opened.recheck(); }` in the three benchmarks; test `tearDownKeepsTheCheckFailureWhenCloseAlsoFails`, red on the old `finally` (primary was the close failure), green after (run 018 e, f); re-check rv011 |
