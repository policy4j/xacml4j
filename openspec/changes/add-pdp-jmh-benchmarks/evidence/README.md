# Evidence

Layout: `tests/`, `reviews/<lane>/`, `benchmarks/jmh/`, `builds/`, `final/`.
One directory per run: `<NNN>-<scope>` (reviews: `rvNNN-<scope>-<lane>`).
Each run holds `summary.md` and `command.txt`
and cites tests, benchmarks, and specifications by reference, with no execution
output; runs before 2026-10-31 keep their files. See
docs/ai/OPENSPEC.md "Evidence".

## Requirements to proof

| Capability / requirement / scenario | Test or conformance case | Run |
| --- | --- | --- |
| Policy store selection / Built-in stores are found | `PolicyStoresTest` | `tests/009-lanes-tests-green`, `tests/016-gate-reactor` |
| Policy store selection / Unknown store name | `PolicyStoresTest` | `tests/009-lanes-tests-green`, `tests/016-gate-reactor` |
| Policy store selection / Store argument | `PolicyStoresTest` | `tests/009-lanes-tests-green`, `tests/016-gate-reactor` |
| Policy store selection / Duplicate store name | `PolicyStoresTest` | `tests/009-lanes-tests-green`, `tests/016-gate-reactor` |
| Policy store selection / Store from another jar | `PdpFixtureTest`; task 3.6 run | `tests/010-external-store-fork`, `tests/016-gate-reactor` |
| Store sessions are released / Ids across processes | `ReviewGateFixesTest` | `tests/018-review-fix-red-green`, `tests/016-gate-reactor` |
| Store sessions are released / Fixture lifecycle | `PdpFixtureTest` | `tests/009-lanes-tests-green`, `tests/016-gate-reactor` |
| Store sessions are released / Load operations | `PolicyStoreLoadBenchmarkTest` | `tests/009-lanes-tests-green`, `tests/016-gate-reactor` |
| Policy corpus selection / Built-in corpora are found | `PolicyCorporaTest` | `tests/009-lanes-tests-green`, `tests/016-gate-reactor` |
| Policy corpus selection / Unknown corpus name | `PolicyCorporaTest` | `tests/009-lanes-tests-green`, `tests/016-gate-reactor` |
| Decisions and lookups are checked / Wildcard version reference | `ReviewGateFixesTest` | `tests/018-review-fix-red-green`, `tests/016-gate-reactor` |
| Decisions and lookups are checked / A store that changes during a trial | `ReviewGateFixesTest` | `tests/018-review-fix-red-green` (e, f), `tests/016-gate-reactor` |
| Decisions and lookups are checked / Every built-in store and corpus passes | `PdpFixtureTest` | `tests/009-lanes-tests-green`, `tests/016-gate-reactor` |
| Decisions and lookups are checked / A store loses a referenced policy | `PdpFixtureTest` | `tests/009-lanes-tests-green`, `tests/016-gate-reactor` |
| Decisions and lookups are checked / A store loses an unreferenced policy or misreports a version | `PdpFixtureTest` | `tests/009-lanes-tests-green`, `tests/016-gate-reactor` |
| Decisions and lookups are checked / A store loses the root policy | `PdpFixtureTest` | `tests/009-lanes-tests-green`, `tests/016-gate-reactor` |
| Decisions and lookups are checked / A wrong expectation | `PdpFixtureTest` | `tests/009-lanes-tests-green`, `tests/016-gate-reactor` |
| Snapshot store answers like the in-memory store / Same answers for every corpus identifier | `SnapshotPolicyRepositoryTest` | `tests/009-lanes-tests-green`, `tests/016-gate-reactor` |
| Snapshot store answers like the in-memory store / Versions in order and duplicates kept once | `SnapshotPolicyRepositoryTest` | `tests/009-lanes-tests-green`, `tests/016-gate-reactor` |
| Snapshot store answers like the in-memory store / Unknown identifier | `SnapshotPolicyRepositoryTest` | `tests/009-lanes-tests-green`, `tests/016-gate-reactor` |
| RSA 2008 corpus / Corpus contents | `Rsa2008CorpusTest` | `tests/009-lanes-tests-green`, `tests/016-gate-reactor` |
| Synthetic corpus / Deterministic output | `SyntheticCorpusTest` | `tests/009-lanes-tests-green`, `tests/016-gate-reactor` |
| Synthetic corpus / Slots and outcomes | `SyntheticCorpusTest` | `tests/009-lanes-tests-green`, `tests/016-gate-reactor` |
| Synthetic corpus / Count out of range | `SyntheticCorpusTest` | `tests/009-lanes-tests-green`, `tests/016-gate-reactor` |
| Directory corpus / Valid directory | `DirectoryCorpusTest` | `tests/009-lanes-tests-green`, `tests/016-gate-reactor` |
| Directory corpus / Manifest errors | `DirectoryCorpusTest` | `tests/009-lanes-tests-green`, `tests/016-gate-reactor` |
| Benchmark set / Concurrent lookups are checked | `ReviewGateFixesTest` | `tests/018-review-fix-red-green`, `tests/016-gate-reactor` |
| Benchmark set / Import through a store that refuses adds | `ReviewGateFixesTest` | `tests/018-review-fix-red-green`, `tests/016-gate-reactor` |
| Benchmark set / One import tool per operation | `ReviewGateFixesTest` | `tests/018-review-fix-red-green`, `tests/016-gate-reactor` |
| Benchmark set / Smoke run of every benchmark | `BenchmarkSmokeTest` | `tests/016-gate-reactor`; jar: `tests/008-fanin-functional-smoke`, `tests/017-gate-smoke` |
| Benchmark set / Allocation metric | `BenchmarkSmokeTest`; task 4.2 run | `tests/016-gate-reactor`; `benchmarks/jmh/001-baseline` (gc.alloc.rate.norm in all 34 sets) |
| Benchmark set / Request selection | `PdpDecideBenchmarkTest` | `tests/009-lanes-tests-green`, `tests/016-gate-reactor` |
| Benchmark set / Several threads | `PdpDecideBenchmarkTest` | `tests/009-lanes-tests-green`, `tests/016-gate-reactor` |
| Design D7 premise (characterization) | `ReferenceCacheCharacterizationTest` | `tests/009-lanes-tests-green`, `tests/016-gate-reactor` |

## Gate tasks to proof

| Task | Run(s) | Status |
| --- | --- | --- |
| 1.1 Plan review | `reviews/plan/rv001-plan-arch`, `rv002-plan-arch`, `rv003-plan-jmh` | done |
| 2.4 Spine gate | `tests/002-spine-green`, `tests/003-spine-package`, `tests/004-spine-gate-reactor` | done (code review in 3.7) |
| 3.7 Lanes gate | `reviews/code/rv004`, `rv006`, `rv007`; `reviews/performance/rv005`; `tests/` | `tests/009-lanes-tests-green`, `tests/016-gate-reactor` |
| 4.3 Baseline gate | `benchmarks/jmh/`; `reviews/performance/rv008` | `tests/009-lanes-tests-green`, `tests/016-gate-reactor` |

## Runs

| Run | Kind | Command | Result |
| --- | --- | --- | --- |
| `reviews/plan/rv001-plan-arch` | plan review, Claude Fable 5.1 | NTM pane, read-only packet | APPROVE WITH CHANGES: 4 major, 3 minor, 1 nit group |
| `reviews/plan/rv002-plan-arch` | plan review, Agy, Gemini 4 Argon | NTM pane, read-only packet | APPROVE WITH CHANGES: 4 major, 4 minor, 1 nit group |
| `reviews/plan/rv003-plan-jmh` | JMH method review, Codex GPT-6.1 Terra | NTM pane, read-only packet | REJECT (repairable): 1 blocker, 7 major, 3 minor |
| `tests/001-spine-red` | test (task 2.1) | `rtk proxy mvn -B -o -pl xacml-jmh -am -Dtest=PdpFixtureTest test` | RED: test sources do not compile (no `PdpFixture`) |
| `tests/002-spine-green` | test (task 2.3) | same | GREEN: 2 tests, 0 failures |
| `tests/003-spine-package` | build (task 2.2) | `rtk proxy mvn -B -o -pl xacml-jmh -am -DskipTests package` | PASS: `benchmarks.jar` and thin jar, no reduced POM, services merged |
| `tests/004-spine-gate-reactor` | reactor (task 2.4) | `rtk proxy mvn -B -o clean test` | PASS: 8,015 tests, 0 failures, 18 skipped; conformance 937/12 |
| `builds/005-corpora-sol` | lane build (tasks 3.3, 3.4), Codex GPT-6.1 Sol | lane report | PASS: synthetic and directory corpora |
| `tests/006-snapshot-astra-characterization` | characterization (task 3.2), Codex GPT-6 Astra | lane report | production repository behavior recorded |
| `builds/007-snapshot-astra-compile` | lane build (task 3.2), Codex GPT-6 Astra | lane report | PASS: snapshot store compiles in isolation |
| `tests/008-fanin-functional-smoke` | fan-in smoke of the shaded jar | `java -jar benchmarks.jar -f 1 -wi 0 -i 1 -r 200ms -foe true` | PASS: every method and parameter set; integration only |
| `tests/009-lanes-tests-green` | tests (task 3.1) | `rtk proxy mvn -B -o -pl xacml-jmh -am test` | GREEN: 165 tests |
| `tests/010-external-store-fork` | task 3.6, research Q3 | `java -cp benchmarks.jar:<jar> org.openjdk.jmh.Main ...` | PASS: an external store reaches the forks |
| `builds/011-lane-reports` | implementation lane reports | NTM panes | stored |
| `tests/012-lanes-gate-reactor` | reactor (part of 3.7) | `rtk proxy mvn -B -o clean test` | PASS: 8,178 tests, 0 failures, 18 skipped |
| `reviews/code/rv004-lanes-code` | code review, Claude Fable 5.1 | NTM pane, read-only packet | APPROVE WITH CHANGES: 1 major, 2 minor, 5 nits; dispositions in `summary.md` |
| `reviews/performance/rv005-benchmarks-jmh` | JMH method review, Claude Fable 5.1 | NTM pane, read-only packet | APPROVE WITH CHANGES: 2 major, 4 minor, 2 nits |
| `reviews/code/rv006-spine-code` | code review, Codex GPT-6.1 Sol | NTM pane, read-only packet | REJECT: 3 major; all resolved (rv009) |
| `reviews/code/rv007-all-semantics` | code and semantics review, Agy, Gemini 4 Argon | NTM pane, read-only packet | APPROVE: 3 minor, 2 nits; descriptive part inaccurate (see summary) |
| `reviews/code/rv009-fixes-code` | re-review of the fixes, Codex GPT-6.1 Sol | NTM pane, read-only packet | APPROVE WITH CHANGES: rv006 F1 to F3 resolved; 1 new defect fixed, 1 minor fixed |
| `reviews/code/rv011-teardown-recheck` | re-check, Codex GPT-6.1 Sol | NTM pane, read-only packet | APPROVE |
| `tests/018-review-fix-red-green` | red and green runs of the fixes | `rtk proxy mvn -B -o -pl xacml-jmh -am -Dtest=... test` | both new regressions red on the old code, green after; 186 tests |
| `tests/016-gate-reactor` | reactor (3.7) | `rtk proxy mvn -B -o clean test` | PASS: 8,202 tests, 0 failures, 18 skipped; conformance 937/12; ast-grep clean |
| `benchmarks/jmh/001-baseline` | JMH baseline (4.2), JDK 21 C2 | Benchmark Plan, both commands | 34 of 34 sets complete; see summary |
| `tests/017-gate-smoke` | one-fork smoke of the shaded jar (3.7) | `java -jar benchmarks.jar -f 1 -wi 0 -i 1 -r 200ms -foe true` | PASS: 20 of 20 sets; integration only |
| `tests/019-simplify-harness` | tests (task 6.1, D15) | `rtk mvn -o -pl xacml-jmh -am -Dtest='org.xacml4j.jmh.**' test`; `ast-grep scan` | GREEN: 194 tests; scan clean; 18 -> 8 public harness types |
| `tests/020-reactor-H-harness` | reactor (task 6.2) | `rtk proxy mvn -B -o clean test` | PASS: 8,280 tests, 18 skipped; conformance 938/12 |
| `reviews/code/rvj1-simplify-sol` | review of D15 (6.2), Codex GPT-6.1 Sol | NTM pane, read-only packet | APPROVE WITH CHANGES: 2 P3, both fixed |
| `tests/021-rvj1-fixes` | RVJ1 fixes, with a mutation check | `rtk mvn -o -pl xacml-jmh -am -Dtest=... test` | GREEN: 195 tests; the mutation fails the new test |
| `tests/022-commit-tree-harness` | the exact tree of the D15 commit (6.2) | `rtk proxy mvn -B -o clean test` on a clean export | PASS: 8,282 tests, 18 skipped; conformance 938/12 |

## Not applicable

- 2.4 JMH: NOT APPLICABLE: the spine has no benchmark yet; group 3 adds them.
