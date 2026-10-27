# Evidence

Layout: `tests/`, `reviews/<lane>/`, `benchmarks/jmh/`, `builds/`, `final/`.
One directory per run: `<NNN>-<scope>` (reviews: `rvNNN-<scope>-<lane>`).
Each run holds `summary.md`, `command.txt`, and its raw output. Every JMH run
is also a row of `benchmarks/jmh/LEDGER.md`. See docs/ai/OPENSPEC.md
"Evidence".

## Requirements to proof

| Capability / requirement / scenario | Test or conformance case | Run |
| --- | --- | --- |
| pdp-decision-correlation / Correlation ids without random numbers / Sequence of one PDP | `CorrelationIdTest` | `tests/002-correlation-green`; 7 tests after review: add-pdp-jmh-benchmarks `tests/016-gate-reactor` |
| pdp-decision-correlation / Correlation ids without random numbers / Two PDPs number alike | `CorrelationIdTest` | `tests/002-correlation-green`; 7 tests after review: add-pdp-jmh-benchmarks `tests/016-gate-reactor` |
| pdp-decision-correlation / Correlation ids without random numbers / No random source in the PDP package | rule `java-no-random-on-decide-path`, `ast-grep test`, `ast-grep scan` | `tests/002-correlation-green` (`ast-grep test`, `scan`); rule extended after rv001 A1: 13 cases, scan clean (add-pdp-jmh-benchmarks `tests/016-gate-reactor`) |
| pdp-decision-correlation / Request ids of individual decisions / One request | `CorrelationIdTest` | `tests/002-correlation-green`; 7 tests after review: add-pdp-jmh-benchmarks `tests/016-gate-reactor` |
| pdp-decision-correlation / Request ids of individual decisions / Several individual decisions | `CorrelationIdTest` | `tests/002-correlation-green`; 7 tests after review: add-pdp-jmh-benchmarks `tests/016-gate-reactor` |

## Gate tasks to proof

| Task | Run(s) | Status |
| --- | --- | --- |
| 1.3 Group 1 gate | reviews `code/rv001-group1-code`, `rv002-group1-code`, `rv003-group1-fixes`, `rv004-group1-recheck`; `tests/003-reactor-after-group1`; `benchmarks/jmh/` | reviews done (final APPROVE, rv004); reactor done; JMH pair `benchmarks/jmh/001-group1-before`, `002-group1-after`: no measurable change (intervals overlap) | 
| 2.3 First profiling round gate | `benchmarks/jmh/003-profile-decide-jdk25`, `004-profile-decide-jdk25-logback`; `reviews/plan/rv005-plan-cycles-terra` | profiles done; plan review rv005 (REJECT) dispositions applied; re-check of the dispositions pending |
| 1.4 Group 1 with `-t 4` | `benchmarks/jmh/007-group1-t4-before`, `008-group1-t4-after` | done: no claim (intervals overlap), bytes unchanged |
| 4.3 Cycle A gate | see tasks.md 4.3 | done: JMH pair, reactor and conformance (`tests/017`), reviews rv006 to rv012 (final APPROVE or APPROVE WITH CHANGES, applied); final-tree pair `017`/`018`: no claim |
| 5.3 Cycle B gate | see tasks.md 5.3 | done: JMH pair, reactor and conformance (`tests/017`), reviews rv006 to rv012 (final APPROVE or APPROVE WITH CHANGES, applied); final-tree pair `017`/`018`: no claim |
| 6.3 Cycle C gate | `tests/007-cycleC-characterization` | waiting for the maintainer's serialization decision (design Open Question 1) |
| 7.3 Cycle D gate | see tasks.md 7.3 | done: JMH pair, reactor and conformance (`tests/017`), reviews rv006 to rv012 (final APPROVE or APPROVE WITH CHANGES, applied); final-tree pair `017`/`018`: no claim |
| 8.3 Cycle E gate | see tasks.md 8.3 | done: JMH pair, reactor and conformance (`tests/017`), reviews rv006 to rv012 (final APPROVE or APPROVE WITH CHANGES, applied); final-tree pair `017`/`018`: no claim |
| 9.3 Cycle F gate | see tasks.md 9.3 | done: JMH pair, reactor and conformance (`tests/017`), reviews rv006 to rv012 (final APPROVE or APPROVE WITH CHANGES, applied); final-tree pair `017`/`018`: no claim |
| 12.3 Cycle H gate | `tests/018-cycleH-red-green`, `019-reactor-cycleH`; `benchmarks/jmh/019-cycleH-before`, `020-cycleH-after` | JMH done (full scan -46% bytes, -10% time); reactor done; review pending |

## Runs

| Run | Kind | Command | Result |
| --- | --- | --- | --- |
| `tests/001-correlation-red` | test (task 1.1) | `rtk proxy mvn -B -o -pl xacml-pdp -am -Dtest=CorrelationIdTest test`; `ast-grep test` | RED as intended: 2 failures, 2 errors (random suffix) |
| `tests/002-correlation-green` | test (task 1.2) | same | GREEN: 4 tests; `ast-grep test` and `scan` pass |
| `tests/003-reactor-after-group1` | reactor (task 1.3) | `rtk proxy mvn -B -o clean test` | PASS: 8,013 tests, 0 failures, 18 skipped; conformance 937/12 |
| `reviews/code/rv001-group1-code` | code review, Codex GPT-6 Astra | NTM pane, read-only packet | APPROVE WITH CHANGES: A1 (minor) |
| `reviews/code/rv002-group1-code` | code review, Agy, Gemini 4 Argon | NTM pane, read-only packet | APPROVE: 2 minor, 2 nits |
| `reviews/code/rv003-group1-fixes` | re-review, Codex GPT-6 Astra | NTM pane, read-only packet | APPROVE WITH CHANGES: A1 resolved; N2 (minor, test race) fixed |
| `reviews/code/rv004-group1-recheck` | re-check, Codex GPT-6 Astra | NTM pane, read-only packet | APPROVE |
| `benchmarks/jmh/001-group1-before` | JMH, JDK 21 C2 | `PdpDecideBenchmark` in-memory, rsa2008 and synthetic:100 | 19.63 ± 0.72 and 45.57 ± 2.1 us/op |
| `benchmarks/jmh/002-group1-after` | JMH, JDK 21 C2, back to back | same | 20.38 ± 1.6 and 43.87 ± 1.4 us/op: overlap, no claim |
| `benchmarks/jmh/004-profile-decide-jdk25-logback` | profile, same set, Logback | same | the Log4j stack walk per request is gone |
| `tests/006-cycleA-review-tests` | test (task 4.4) | barrier race, PIP callback, real PDP with two resources | 38 + 1 tests pass |
| `tests/011-reactor-cycles-DEF` | reactor (gates 7.3, 8.3, 9.3) | `mvn -B -o clean test` | PASS: 8,264 tests, 0 failures, 18 skipped; conformance 938/12 |
| `benchmarks/jmh/008-group1-t4-after` | JMH `-t 4`, back to back with 007 | mix | overlap: no claim |
| `benchmarks/jmh/009-cycleB-after` | JMH, after 006 in the same session | mix and scan | rsa2008 -15.2%; first-deny -28.8% time, -37% bytes; scans: no claim |
| `benchmarks/jmh/003-profile-decide-jdk25` | profile, async-profiler 4.5, JDK 25 C2 (Log4j) | cpu and alloc; rsa2008 all, synthetic:1000 all and first-deny | input of the discovery swarm PF1-PF6 |
| `reviews/plan/rv005-plan-cycles-terra` | plan review, Codex GPT-6.1 Terra | NTM pane, read-only packet | REJECT; every finding applied to the design (D7, D10, D12) |
| `tests/004-cycleA-red-green` | test (tasks 4.1, 4.2) | handler tests | RED before D5 (1 failure), GREEN after |
| `tests/005-cycleB-characterization-green` | test (tasks 5.1, 5.2) | `AttributeLookupCharacterizationTest` | 8 of 8 before and after D6 |
| `tests/007-cycleC-characterization` | test (task 6.1) | `BagOfValuesCharacterizationTest`, `MatchTest` | 6 + 5 pass on today's code |
| `tests/008-cycleD-characterization-green` | test (tasks 7.1, 7.2) | before-tree and tree | 8 of 8 before and after D8 |
| `tests/009-cycleE-characterization-green` | test (tasks 8.1, 8.2) | before-tree and tree | 11 of 11 before and after D9 |
| `tests/010-cycleF-characterization-green` | test (tasks 9.1, 9.2) | before-tree and tree | 5 of 5 before and after D10 |
| `benchmarks/jmh/005-cycleA-before` | JMH, JDK 25 C2, session 2 | mix and scan (D12) | before values of cycle A |
| `benchmarks/jmh/006-cycleA-after` | JMH, back to back with 005 | same | rsa2008 -9.1%; synthetic:1000 full scans -56% to -61% time, about -75% bytes |
| `benchmarks/jmh/007-group1-t4-before` | JMH `-t 4` (task 1.4) | mix | before values |
| `benchmarks/jmh/010-cycleD-before` | JMH, session 3, chain start (cycle B jar) | mix and scan | before values of cycle D; drift against 009: times within the intervals |
| `benchmarks/jmh/011-cycleD-after` | JMH, after 010 | mix and scan | synthetic:100 -14.9%; synthetic:1000 sets -7.8% to -18.6% time, about -28% bytes |
| `benchmarks/jmh/012-cycleE-after` | JMH, after 011 | mix and scan | rsa2008 -16.0%; first-deny -30.0% time, -34% bytes; scans: no claim |
| `benchmarks/jmh/013-cycleF-after` | JMH, after 012 | mix and scan | four scan sets -9.7% to -13.9% (disjoint), bytes -13% on every scan set |
| `benchmarks/jmh/014-cycleE-t4-before` | JMH `-t 4` (cycle D jar) | mix | before values |
| `benchmarks/jmh/015-cycleE-t4-after` | JMH `-t 4`, after 014 | mix | rsa2008 -16.0%; synthetic:100: no claim |
| `reviews/code/rv006-cycles-sol` | gate review A-F, Codex GPT-6.1 Sol | NTM pane, read-only packet | A APPROVE; B, D, E REJECT; F APPROVE; all findings fixed |
| `reviews/code/rv007-cycles-terra` | gate review A-F, Codex GPT-6.1 Terra | NTM pane, read-only packet | A APPROVE; B, F APPROVE WITH CHANGES; D, E REJECT; all findings fixed |
| `reviews/code/rv008-recheck-terra` | re-check, Terra | NTM pane | E REJECT (detached entry set, null hash, race test); fixed |
| `reviews/code/rv009-recheck2-terra` | re-check 2, Terra | NTM pane | D REJECT (validator list aliasing); E APPROVE WITH CHANGES; fixed |
| `reviews/code/rv010-recheck-sol` | re-check, Sol | NTM pane | E REJECT (removeIf through the iterator, contains(null)); fixed |
| `reviews/code/rv011-final-sol` | final re-check, Sol | NTM pane | A, B, D, F APPROVE; E APPROVE WITH CHANGES (latch assertion; fixed) |
| `reviews/code/rv012-final-terra` | final re-check, Terra | NTM pane | A, B, F APPROVE; D, E APPROVE WITH CHANGES (D8 wording and latch fixed; final JMH pair in session 4) |
| `tests/012-review-fixes-red-green` | test (review fixes) | red tree, before-trees, tree | 9 red (7 failures, 2 errors); green before and after |
| `tests/013-reactor-review-fixes` | reactor | Maven clean test, offline | PASS: 8,274 tests, 18 skipped; conformance 938/12 |
| `tests/014-recheck-fixes` | test (rv008 fixes) | before-trees and tree | green |
| `tests/015-reactor-recheck-fixes` | reactor | same | PASS: 8,274 tests |
| `tests/016-recheck2-fixes` | test (rv009, rv010 fixes) | red tree, before-trees, tree | 3 red on the round-1 tree; green before and after |
| `tests/017-reactor-recheck2-fixes` | reactor | same | PASS: 8,276 tests, 18 skipped; conformance 938/12 |
| `benchmarks/jmh/016-profile-decide-cycles-AF` | profile, async-profiler 4.5, final tree (task 11.1) | cpu and alloc, as run 004 | empty bags built on every attribute evaluation: 44.5% of full-scan bytes (new cycle H) |
| `benchmarks/jmh/017-review-fixes-before` | JMH, session 4 (cycle F jar) | mix and three scan sets | before values of the review fixes |
| `benchmarks/jmh/018-review-fixes-after` | JMH, after 017 | same | no claim on any set; first-deny +150 B |
| `tests/018-cycleH-red-green` | test (tasks 12.1, 12.2) | `EmptyBagCharacterizationTest` | 3 of 4 before D14 (sharing test red), 4 of 4 after |
| `tests/019-reactor-cycleH` | reactor (gate 12.3) | Maven clean test, offline | PASS: 8,280 tests, 18 skipped; conformance 938/12 |
| `tests/020-cycleH-round2` | red and green after RVH1 (12.2) | Maven `-Dtest=...`, offline; `ast-grep test` | red on HEAD 2 of 6, as intended; green 126; rule tests pass |
| `tests/021-reactor-H-harness` | reactor (gate 12.3) | Maven clean test, offline | PASS: 8,280 tests, 18 skipped; conformance 938/12 |
| `reviews/code/rvh1-cycleH-sol` | cycle H review, Codex GPT-6.1 Sol | NTM pane, read-only packet | APPROVE WITH CHANGES: 2 P2, 1 P3; all addressed in round 2 (`tests/020-cycleH-round2`) |
| `reviews/code/rvh2-cycleH-recheck-sol` | cycle H re-check, Codex GPT-6.1 Sol | NTM pane, read-only packet | APPROVE WITH CHANGES: RVH1 resolved; 1 new P2 (hash-cache double read), fixed |
| `tests/022-rvh2-hash-fix` | the RVH2 fix (12.3) | Maven `-Dtest=...`, offline | 24 tests pass; stress test only, not red first (a data race cannot be forced) |
| `tests/023-commit-tree-cycleH` | the exact tree of the cycle H commit (12.3) | Maven clean test on a clean export, offline | PASS: 8,283 tests, 18 skipped; conformance 938/12 |
| `benchmarks/jmh/019-cycleH-before` | JMH, session 5 (committed tree) | mix and scan | before values of cycle H |
| `benchmarks/jmh/020-cycleH-after` | JMH, after 019 | mix and scan | full scan 316 KB to 172 KB (-46%), -10.4% time; synthetic:100 -37% bytes |
| `benchmarks/jmh/021-profile-decide-cycle-H` | profile, cycle H tree | cpu and alloc, as run 004 | contexts 33%, call arrays 42%, bag iteration 14% of full-scan bytes; input of the second analysis swarm |

## Not applicable

None yet.
