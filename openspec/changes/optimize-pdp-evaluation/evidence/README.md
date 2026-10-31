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
| 26.2, 31.2, 32.2, 38.2 review gates | `reviews/code/rv209-gate-audit-astra`, `tests/209-integration-gate` | done 2026-10-31: D29 reviewed; earlier findings checked closed in source; reactor and conformance green on the combined tree |
| 28.4, 36.4, 39.2, 41.5 review parts | `reviews/code/rv209-gate-audit-astra`, `rv214-ak-luna`, `tests/206-rv209-sol` | review findings closed; measurement open |

## Runs

Runs from 201 on follow the reference rule of docs/ai/OPENSPEC.md "Evidence": they cite tests and
benchmarks and keep no execution output, so a summary can name a log that is not in the repository.

| Run | Kind | Command | Result |
| --- | --- | --- | --- |
| `benchmarks/jmh/027-pair-d47` | controlled pair, D47 eligibility off against on, C2, `-prof gc`, 3 forks | `PdpDecideBenchmark.decide`, rsa2008 and rules:100, index off and on | allocation saving on all but multi-valued; time no significant difference; the remaining pairs are left to the maintainer |
| `tests/209-integration-gate` | combined reactor and conformance, conductor, detached tree at `759d90a6` with the 34 reviewed files | shared runner, `-o clean test`, no failure-ignore | PASS: 10,836 tests, 0 failures, 0 errors, 36 skipped; conformance module 1,818; no performance claim |
| `tests/208-d49-benchmark-terra` | D49 benchmark `StringValEqualsBenchmark` (Codex Terra) | `-pl xacml-jmh -am -DskipTests clean package` | packaged, listed in the jar metadata; not run; first-use mode exploratory |
| `tests/207-d51-fix-astra` | D51 default-zero fix and two D52 regression tests (Codex Astra) | focused red, green, and module runs; three mutants | PASS: D51 red then 154 green; modules core 7,504 (known date error only), PDP 810; mutants killed; 160 focused green |
| `tests/206-rv209-sol` | RV209-ASTRA-1, alias bound of 64 per index (Codex Sol) | focused red, green, module run | red 1,025 aliases, then green; modules core 7,383 (known date error only), PDP 816 |
| `tests/205-d47-correction-terra` | D47 correction, runtime values and target guard (Codex Terra) | focused red, green, module run; two mutants | red 4 of 44, green 56; modules core 7,357 (known date error only), PDP 851; mutants killed |
| `tests/204-d55-sol` | D55 lazy response index (Codex Sol) | characterization, focused, module run | 30 characterization tests, 8,473 module tests (known date error only); byte figures UNRUN |
| `tests/203-resume-d52-metadata-astra` | D52 metadata correction (Codex Astra) | focused red and green, suites | red 6 of 12 and 2 of 2, green 335; PDP 810; XML 254; JSON 24 |
| `tests/202-resume-variable-dag-sol` | P-CPU2 variable-DAG walk (Codex Sol) | red probe, focused, module gate | red 4,096 leaf reads, green 1; module 8,449 tests, 0 failures |
| `tests/201-resume-d47-terra` | first D47 runtime-value correction (Codex Terra) | focused and module runs | superseded by run 205, rejected by rv205 and rv206 |
| `reviews/code/rv215-d51-recheck-argon` | D51 fix re-review, Agy Gemini 4 Argon | source review | APPROVE, P2-1 closed |
| `reviews/plan/pr-D35-argon` | D35 draft plan review, task 37.2, Agy Gemini 4 Argon | source and official library documentation | APPROVE WITH CHANGES: 2 P1, 5 P2, 2 P3; folded into the draft; 37.3 waits for the maintainer |
| `reviews/code/rv214-ak-luna` | cycle AK review, Codex GPT-6 Luna | source review | APPROVE; two optional coverage notes |
| `reviews/code/rv211-d55-opus` | D55 and the RV209 bound, Claude Opus 5.5 | source review of the `cod_2` bytes | APPROVE, no findings; main's `AttributeIndex.java` differs by the maintainer's enhanced `for` |
| `reviews/code/rv213-d47-recheck-argon` | corrected D47 re-review, Agy Gemini 4 Argon | source review, author results supplied | APPROVE, no findings; selector path traced by source |
| `reviews/code/rv212-d47-recheck-opus` | corrected D47 adversarial re-review, Claude Opus 5.5 | source review of all eleven algorithm routes | APPROVE, no findings; allocation gate may still fail |
| `reviews/code/rv210-ba-argon` | cycle BA review, Agy Gemini 4 Argon | source review | D49 APPROVE, D50 APPROVE, D51 REJECT (P2-1 default zero); fixed in run 207 |
| `reviews/code/rv209-gate-audit-astra` | D29 review and gate audit, Codex GPT-6 Astra | source review of 28.4, 31.2, 32.2, 36.4, 38.2, 39.2, 41.5 | D29 APPROVE; those review gates CLOSED except 41.5 (RV209-ASTRA-1, fixed in run 206; AK, reviewed in rv214) |
| `reviews/code/rv208-d52-opus` | corrected D52 and task 5.8 review, Claude Opus 5.5 | source review | APPROVE both; two P3 test gaps, closed in run 207 |
| `reviews/code/rv207-d52-argon` | corrected D52 review, Agy Gemini 4 Argon | source review | APPROVE, no findings |
| `reviews/code/rv206-d47-opus` | D47 and P-CPU2 review, Claude Opus 5.5 | source review | Sol APPROVE; Terra REJECT (`attribute-designator` P2, one P3); estimates D47's allocation risk |
| `reviews/code/rv205-d47-argon` | D47 and P-CPU2 review, Agy Gemini 4 Argon | source review | Sol APPROVE; Terra REJECT (P2-1, P2-2); author models, one hash, and test counts in it are wrong, the source findings stand |
| `builds/202-worktree-audit` | live Git/Beads/OpenSpec inventory, conductor | twelve attached worktrees; tracked-file byte comparisons; tracker and checklist counts | old candidates preserved, current five lanes isolated; remaining gates and tracker discrepancies recorded; no implementation or performance acceptance |
| `builds/201-ntm-resume-preflight` | fresh NTM launch, model and packet checks for group 42 | scoped launch/recovery and five Beads packets; actual CLI model headers | five isolated lanes working; initial fish-pane launch failure retained; no implementation acceptance, tests or JMH claimed |
| `reviews/tests/rv202-resume-d47-luna` | terminal baseline coverage review, Codex GPT-6 Luna high, validated by the conductor | source review at `759d90a6`; no builds/tests | request/PIP runtime-value gap confirmed; bounded review packet complete; D47 candidate and parent task 42.14 remain unaccepted |
| `reviews/code/rv203-resume-d52-argon` | terminal D52 source review, Agy Gemini 4 Argon High | frozen candidate source review; no builds/tests | APPROVE reported, HELD by conductor: supplied-test counts, source citations and benchmark references require correction; fresh re-review requested |
| `reviews/code/rv201-resume-d52-astra` | terminal independent D52 source review, Codex GPT-6 Astra max | frozen candidate hashes and source trace | REJECT: D52-ASTRA-1, mutable custom parameter metadata can invalidate the cached signature proof; counterexample/runtime tests UNRUN; correction required before integration |
| `reviews/code/rv204-resume-d52-argon-recheck` | corrected terminal D52 source review, Agy Gemini 4 Argon High | frozen source review; no builds/tests | REJECT: custom/mutable metadata invalidates the static proof; supersedes rv203's held approval; agrees with Astra's blocking boundary; corrected candidate and performance remain unaccepted |
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
| `tests/024-cycleI-red` | cycle I tests before D15 (13.1) | Maven `-Dtest=DirectPairCallTest`, offline | 9 run, 1 failure as intended: 72.0 bytes per call |
| `tests/025-cycleI-green` | cycle I after D15 (13.2) | Maven focused and clean test, offline | focused green; reactor 8,291 tests, 18 skipped; conformance 938/12 |
| `reviews/code/rvi1-cycleI-sol` | cycle I review, Codex GPT-6.1 Sol | NTM pane, read-only packet | REJECT: 2 P2 fixed, 1 P3 superseded by the D18 decision |
| `reviews/plan/rvc7-cycleC-agy` | plan review of D7 as decided 2026-10-27 (6.2), Agy Gemini 4 Argon | NTM pane, read-only packet | APPROVE WITH CHANGES: 3 P1, 2 P2, all applied to D7 and tasks 6.3 to 6.6 |
| `tests/026-rvi1-fixes` | RVI1 fixes (13.3) | Maven `-Dtest=...` and clean test, offline | red 3 of 12 before, green after; reactor 8,294 tests, 18 skipped; conformance 938/12 |
| `reviews/code/rvi2-cycleI-recheck-sol` | cycle I re-check, Codex GPT-6.1 Sol | NTM pane, read-only packet | REJECT: RVI1 resolved; 1 new P2 (inaccessible parameter types), fixed |
| `tests/027-rvi2-fix` | the RVI2 fix (13.3) | Maven `-Dtest=...` and clean test, offline | red 1 of 13, then green; 241 focused tests; reactor 8,295 tests, 18 skipped; conformance 938/12 |
| `reviews/code/rvi3-cycleI-recheck-sol` | cycle I third check, Codex GPT-6.1 Sol | NTM pane, read-only packet | APPROVE WITH CHANGES: 1 P2 (hidden provider classes), fixed |
| `tests/028-rvi3-fix` | the RVI3 fix and gate (13.3) | Maven focused and clean test, offline | red 1 of 2, then green; 243 function tests; reactor 8,297 tests, 18 skipped; conformance 938/12 |
| `tests/029-cycleJ-characterization` | cycle J characterization (14.1) | Maven `-Dtest=...`, offline | 33 tests pass before D16 |
| `tests/030-cycleJ-green` | cycle J after D16 (14.2) | Maven focused and clean test, offline | 587 focused; reactor 8,301 tests, 18 skipped; conformance 938/12 |
| `reviews/code/rcj1-cycleJ-sol` | cycle J review, Codex GPT-6.1 Sol | NTM pane, read-only packet | REJECT: 2 P2 (combiner list semantics), fixed by restoring the combiners |
| `tests/031-rcj1-fix` | the RCJ1 fix (14.3) | Maven focused and clean test, offline | red 2 of 5, then green; 589 focused; reactor 8,303 tests, 18 skipped; conformance 938/12 |
| `reviews/code/rcj2-cycleJ-recheck-sol` | cycle J re-check, Codex GPT-6.1 Sol | NTM pane, read-only packet | REJECT: RCJ1 resolved; 1 new P2 (normalization snapshot), fixed |
| `tests/032-rcj2-fix` | the RCJ2 fix (14.3) | Maven focused and clean test, offline | red 1 of 13, then green; 592 focused; reactor 8,304 tests, 18 skipped; conformance 938/12 |
| `reviews/code/rcj3-cycleJ-recheck-sol` | cycle J third check (14.3), Codex GPT-6.1 Sol | NTM pane, read-only packet | APPROVE |
| `tests/033-cycleK-red` | cycle K tests (15.1) | Maven focused, offline | characterization 4 green before; handler test red 1 of 40 |
| `tests/034-cycleK-green` | cycle K (15.2) | Maven focused and clean test, offline | core 6,582, PDP 218; reactor 8,309 tests, 18 skipped; conformance 938/12 |
| `reviews/code/rck1-cycleK-sol` | cycle K review (15.3), Codex GPT-6.1 Sol | NTM pane, read-only packet | REJECT: 2 P2 (binary compatibility, staging walk), fixed |
| `tests/035-rck1-fix` | the RCK1 fix (15.3) | Maven focused and clean test, offline | focused green; reactor 8,313 tests, 18 skipped; conformance 938/12 |
| `reviews/code/rck2-cycleK-recheck-sol` | cycle K re-check (15.3), Codex GPT-6.1 Sol | NTM worktree lane, read-only | APPROVE WITH CHANGES: RCK1 resolved; 1 P3 (duplicate memo test), folded into the existing test |
| `reviews/code/rvl1-cycleL-sol` | cycle L (16.3), Codex GPT-6.1 Sol | NTM worktree lane, read-only | REJECT on the lane split only (the two files came with M and N); other checks pass; the logging test was later deleted (maintainer) |
| `reviews/code/rvm1-cycleM-astra` | cycle M (17.3), Codex Astra | NTM worktree lane, read-only | APPROVE WITH CHANGES: 1 P3 (list capacity), applied |
| `reviews/code/rvn1-cycleN-opus` | cycle N (19.3), Claude Opus 5.5 | NTM worktree lane, read-only | APPROVE WITH CHANGES: tests cut to status and error chain, `withoutStackTrace` and the no-op `BagFunctions` hunk dropped, applied |
| `tests/036-batch2-core-local` | `xacml-core` of the merged batch 2 and D23 | `javac` and JUnit launcher on local Gradle-cache jars (Maven blocked) | 6,595 found, 0 failed, 1 skipped; 9 Truth8 classes, PDP, conformance not run |
| `tests/037-reactor-batch2-D21-D23` | full reactor on the main tree: batch 2, D21, D23 | Maven 3.9.16 `clean test`, online (mirror) | BUILD SUCCESS: 8,362 tests, 0 failures, 18 skipped; conformance 938/12 |
| `tests/038-reactor-batch3-ABCD` | full reactor: batch 2 and batch 3 (D20 (d), D21 and SPI, D22, D23) with review fixes | Maven 3.9.16 `clean test`, offline | BUILD SUCCESS: 8,372 tests, 0 failures, 18 skipped; conformance 938/12 |
| `reviews/code/rd21f-D21-fable` | D21 (20.3), Claude Fable 5.1 | NTM worktree lane, read-only | APPROVE WITH CHANGES: the monitor on the decide path (moot after the Guava interner), one comment |
| `reviews/code/rd22t-D22-terra` | D22 and D20 (d) (21.3, 19.5), Codex Terra | NTM worktree lane, read-only | APPROVE WITH CHANGES: 2 P3 (superclass hash first; status equality in a test), applied |
| `reviews/code/rd23s-D23-sol` | D23 (22.4), Codex GPT-6.1 Sol | NTM worktree lane, read-only | REJECT: 1 P2 (`Apply` pair call bypasses a custom spec's varargs override); fixed by keeping `Apply`'s varargs call |
| `reviews/code/rso-D21-spi-opus` | D21 interner SPI, Claude Opus 5.5 | NTM worktree lane, read-only | APPROVE WITH CHANGES: P1 (category-less keys with a non-default interner) and P2s fixed by Opus's verified simpler shape; core 6,646 and PDP 217 green |
| `reviews/plan/rpa-D24-astra` | plan review of D24, Codex Astra | NTM worktree lane, read-only | REJECT: 3 P1 (PIP prefetch, failed-reference retry, separate listeners); D24 revised |
| `reviews/plan/rpa2-D24-D25-astra` | re-review of D24 and D25, Codex Astra | NTM worktree lane, read-only | REJECT: 2 P1 (failed binding retried, rebinding under an immutable index); third design round (maintainer) |
| `reviews/plan/rpf-D25-fable` | plan review of D25, Claude Fable 5.1 | NTM worktree lane, read-only | APPROVE WITH CHANGES: one mechanism with D24 (prepared lists, `BoundReference`), race-fix recipe, gain scoped; applied to D25 and group 24 |
| `reviews/code/rcc1-batch2-clean-code-astra` | clean code of cycles I, J, K, Codex Astra | NTM worktree lane, read-only | APPROVE WITH CHANGES: mechanism tests removed (allocation, class scans, binding chosen), binding cache a plain map, style; the `policy4j-0972` characterization in `MatchTest` kept (D7 keeps that order) |
| `benchmarks/jmh/019-cycleH-before` | JMH, session 5 (committed tree) | mix and scan | before values of cycle H |
| `benchmarks/jmh/020-cycleH-after` | JMH, after 019 | mix and scan | full scan 316 KB to 172 KB (-46%), -10.4% time; synthetic:100 -37% bytes |
| `benchmarks/jmh/021-profile-decide-cycle-H` | profile, cycle H tree | cpu and alloc, as run 004 | contexts 33%, call arrays 42%, bag iteration 14% of full-scan bytes; input of the second analysis swarm |

## Not applicable

None yet.
