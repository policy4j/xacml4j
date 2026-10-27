# rv003: re-review of the group 1 fixes (gate task 1.3)

Date: 2026-10-27. Lane rv010, Codex CLI, GPT-6 Astra, read-only. Report: `report.md` (shared with
`upgrade-build-plugins` rv002; part A is this change).

Verdict A: APPROVE WITH CHANGES.

| Finding | Status | Disposition |
| --- | --- | --- |
| A1 rule misses qualified and receiver-less calls | RESOLVED | - |
| residual: tests miss hex past 9 and exception cleanup | RESOLVED | - |
| N2 (minor): the concurrent test shares an unsynchronized audit list | new | fixed: `Collections.synchronizedList`, 2,000 audit records asserted, all distinct; `CorrelationIdTest` 7 of 7 (add-pdp-jmh-benchmarks `evidence/tests/018-review-fix-red-green` run e, f; reactor run 016); re-check rv004 |
