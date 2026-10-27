Beads: epic `policy4j-ba4s` (created in task 1.2); task X.Y is issue `<epic>.X.Y`.
Gate (default for code tasks): `PATH="<maven-3.9.16>/bin:$PATH" JAVA_HOME="$(jenv prefix 25)" rtk mvn -o -q -Dlicense.skipUpdateLicense=true -pl xacml-jmh -am test`
(after `upgrade-build-plugins`, Homebrew Maven 3.10 works too; the evidence names the Maven that ran it).

## 1. Plan

- [x] 1.1 Plan review: rv002 architecture (Agy, Gemini 4 Argon) and rv003 JMH method (Codex GPT-6.1 Terra) from other families than the author's, and rv001 architecture (Claude Fable 5.1) as an additional Claude lane; resolve the findings in the artifacts; check: `openspec validate add-pdp-jmh-benchmarks --strict`; evidence: `evidence/reviews/plan/rv001-plan-arch`, `rv002-plan-arch`, `rv003-plan-jmh`
- [x] 1.2 Create the beads epic and one issue per task; record the `InMemoryPolicyRepository` null defects (unknown id, wrong-type id, the escape from `decide` under the 1.0/2.0 combining algorithms) and the unused resolver cache `size` as one separate issue linked to the epic; check: `br ready` shows task 2.1; evidence: beads `policy4j-ba4s`, defects `policy4j-dofm`, `policy4j-d4as`, `policy4j-w842`

## 2. Module and harness spine

- [x] 2.1 Write the failing test `PdpFixtureTest` (store `in-memory`, corpus `rsa2008`: the fixture opens, all 17 requests have their expected decisions, all lookups return their rules); check: the test cannot compile or fails before 2.2 and 2.3; evidence: `evidence/tests/001-spine-red`
- [x] 2.2 Add the module: one `<module>` line in `pom.xml`, one `/xacml-jmh/target` line in `.gitignore`, and `xacml-jmh/pom.xml` (D1, D2; after `upgrade-build-plugins`). The new module and the JMH 1.37 and shade 3.6.2 dependencies need the maintainer's approval (given, proposal Approval); check: an offline `package -DskipTests` of `-pl xacml-jmh -am` writes `xacml-jmh/target/benchmarks.jar` and a thin `xacml-jmh-2.0.0-SNAPSHOT.jar`, and no `dependency-reduced-pom.xml`; evidence: `evidence/tests/003-spine-package`
- [x] 2.3 Add the harness API with sessions, lookups, and request selection, `PdpFixture` with both checks, `InMemoryPolicyStore`, `Rsa2008Corpus`, and `log4j2.xml` (D3 to D6, D9, D11, D13); check: `PdpFixtureTest` passes; evidence: `evidence/tests/002-spine-green`
- [x] 2.4 Gate: `xacml-jmh` tests and a full reactor `clean test` on JDK 21 (evidence: `evidence/tests/`); code review of the spine is part of gate 3.7; JMH: NOT APPLICABLE until group 3 adds the benchmarks; docs: in task 4.1; evidence: `evidence/tests/004-spine-gate-reactor` (8,015 tests)

## 3. Stores, corpora, benchmarks, and tests (parallel lanes)

- [x] 3.1 Write the tests of the Test Strategy for every requirement, including `ReferenceCacheCharacterizationTest`; they fail until 3.2 to 3.5 land (lane: Claude Opus 5.5, `policy4j_test_writer`); check: the gate command; evidence: `evidence/tests/` (`evidence/tests/009-lanes-tests-green`, 165 tests)
- [x] 3.2 Add the `snapshot` store (D10) (lane: Codex GPT-6 Astra); check: `SnapshotPolicyRepositoryTest` and the `snapshot` cases of `PdpFixtureTest`; evidence: `evidence/tests/006-snapshot-astra-characterization`, `evidence/builds/007-snapshot-astra-compile`
- [x] 3.3 Add the `synthetic` corpus (D9) (lane: Codex GPT-6.1 Sol); check: `SyntheticCorpusTest`; evidence: `evidence/builds/005-corpora-sol`
- [x] 3.4 Add the `dir` corpus with its manifest (D9) (lane: Codex GPT-6.1 Sol); check: `DirectoryCorpusTest`; evidence: `evidence/builds/005-corpora-sol`
- [x] 3.5 Add `PdpDecideBenchmark`, `PolicyStoreLookupBenchmark`, and `PolicyStoreLoadBenchmark` (D7, D8, D13) (lane: Codex GPT-6.1 Terra); check: `BenchmarkSmokeTest`, `PdpDecideBenchmarkTest`, and `PolicyStoreLoadBenchmarkTest`; evidence: `evidence/tests/008-fanin-functional-smoke`, `evidence/builds/011-lane-reports`
- [x] 3.6 Show that a store in another jar reaches the forked JVMs (research Q3): put the test store in its own jar and run `java -cp xacml-jmh/target/benchmarks.jar:<jar> org.openjdk.jmh.Main PdpDecideBenchmark -p store=<test store> -f 1 -wi 0 -i 1 -foe true`; evidence: `evidence/tests/` (`evidence/tests/010-external-store-fork`)
- [x] 3.7 Gate: code reviews rv004 (Claude Fable 5.1, Codex-authored files), rv006 (Codex GPT-6.1 Sol, Claude-authored files), rv007 (Agy, Gemini 4 Argon, all files, with the semantics focus and the recorded waiver for a Grok or Kimi lane, design Execution Plan) and JMH method review rv005 (Claude Fable 5.1) under `evidence/reviews/`; `xacml-jmh` tests and a full reactor `clean test` on JDK 21; conformance unchanged (no change under `xacml-conformance-tests`); a one-fork smoke run of `benchmarks.jar` with `-foe true`; `ast-grep scan` of `xacml-jmh/src/main`; docs and spec consistency; evidence: `evidence/README.md` row 3.7 (reviews rv004 to rv011, runs 012, 016, 017, 018)

## 4. Baseline and docs

- [x] 4.1 Write `xacml-jmh/README.md` and update `docs/ai/BENCHMARKS.md` and the module table of `AGENTS.md` (D12); check: task 4.2 runs the README commands as written; done: `xacml-jmh/README.md`, `docs/ai/BENCHMARKS.md`, `AGENTS.md`; run 001 used the README commands
- [x] 4.2 Record the baseline run of the Benchmark Plan on JDK 21 with `-prof gc` and `-foe true`, with no agent session alive, and check the results for completeness (D11); evidence: `evidence/benchmarks/jmh/`; done on JDK 21: `evidence/benchmarks/jmh/001-baseline`; the reference baseline moves to JDK 25 (change `adopt-jdk25-baseline` task 3.1)
- [ ] 4.3 Gate: performance and docs review rv008 (Agy, Gemini 4 Argon) of the method, the numbers, and the docs; evidence: `evidence/reviews/performance/`

## 5. Close out

- [ ] 5.1 `openspec validate add-pdp-jmh-benchmarks --strict`
- [ ] 5.2 Audit requirement → task → test → evidence in `evidence/README.md`
- [ ] 5.3 Close the beads epic and run `br sync --flush-only`
