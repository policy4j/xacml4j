# Evidence

Layout: `tests/`, `reviews/<lane>/`, `benchmarks/jmh/`, `builds/`, `final/`.
One directory per run: `<NNN>-<scope>` (reviews: `rvNNN-<scope>-<lane>`).
Each run holds `summary.md` and `command.txt`
and cites tests, benchmarks, and specifications by reference, with no execution
output; runs before 2026-10-31 keep their files. Every JMH run
is also a row of `benchmarks/jmh/LEDGER.md`. See docs/ai/OPENSPEC.md
"Evidence".

## Requirements to proof

| Capability / requirement / scenario | Test or conformance case | Run |
| --- | --- | --- |

## Gate tasks to proof

| Task | Run(s) | Status |
| --- | --- | --- |
| 1.1 Discovery swarm | `research/rx1-*.md` to `rx6-*.md`, `research/synthesis.md` | done |
| 1.2 Plan review | `reviews/plan/rv001-plan-astra`, `rv002-plan-agy`, `rv003-plan-recheck-astra`, `rv004-plan-recheck-agy` | done: REJECT x2, revised, then APPROVE WITH CHANGES (applied) and APPROVE |
| 1.3 Maintainer decisions | proposal, section "Maintainer Decisions (2026-10-28)" | done: D3 per provider and repository; legacy default; D5 adopted; defaults and D6 accepted; strict XF 1.0 |
| 2.2 Regex benchmarks | `implementation/rx-bench-opus.md`; review `reviews/code/r-RXBENCH-argon/report.md` (part 2) | done: `RegexFunctionBenchmark` and four `rules:<n>` regex requests; JMH 225; review APPROVE |
| 2.1, 3.1, 3.2 Characterization, SPI, `jdk` engine | `implementation/rx-spi-fable.md` | done: 116 characterization rows; reactor green on the main tree; review R-RXSPI-sol pending |

## Runs

| Run | Kind | Command | Result |
| --- | --- | --- | --- |

## Not applicable

None yet.
