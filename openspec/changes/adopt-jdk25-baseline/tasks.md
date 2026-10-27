Beads: epic `policy4j-cvw4`; task X.Y is issue `<epic>.X.Y`.
Gate: `PATH="<maven-3.9.16>/bin:$PATH" JAVA_HOME="$(jenv prefix 25)" rtk mvn -o -Dlicense.skipUpdateLicense=true clean test`

## 1. Plan

- [x] 1.1 Research Q1 to Q3 (`research/README.md`); done: Q1 to Q3 answered
- [x] 1.2 Characterize: reactor `clean test` on GraalVM 25 at release 21 (`evidence/tests/001-jdk25-at-release21`); done: 8,202 tests, 0 failures, 18 skipped

## 2. Release 25

- [x] 2.1 Set `<release>25</release>` and the enforcer `requireJavaVersion [25,)`; fix what Q1 and the build require; check: the gate; done: root `<release>25</release>`, enforcer `enforce-java-25`, `xacml-core` override removed (`evidence/tests/002-release25-gate`)
- [x] 2.2 Docs, agent instructions, and CI to JDK 25; gates of the open changes that say JDK 21 note JDK 25 from here on; check: reference search record; done: `AGENTS.md`, `docs/ai/TESTING.md`, `docs/ai/BENCHMARKS.md`, `xacml-jmh/README.md`, `openspec/config.yaml`, CI (`setup-java@v6`, Temurin 25), gate commands of the open changes, note in `modernize-core-for-jdk28-valhalla`
- [x] 2.3 Gate: reactor and conformance on JDK 25, class-file versions, service files, enforcer on JDK 21, review by another family (`evidence/tests/`, `evidence/reviews/code/`); done: `evidence/tests/003-release25-regate`, review `evidence/reviews/code/rv001-gates-astra` (APPROVE WITH CHANGES; changes applied)

## 3. JMH baseline on JDK 25

- [x] 3.1 Run the Benchmark Plan of `add-pdp-jmh-benchmarks` on GraalVM 25 with C2, no agent session alive (`add-pdp-jmh-benchmarks` `evidence/benchmarks/jmh/002-baseline-jdk25`); check: completeness script; done: add-pdp-jmh-benchmarks `evidence/benchmarks/jmh/002-baseline-jdk25` (34 of 34 sets)

## 4. Close out

- [x] 4.1 `openspec validate adopt-jdk25-baseline --strict`; done: valid (2026-10-27)
- [x] 4.2 Audit requirement → task → evidence in `evidence/README.md`; done: every requirement and scenario maps to a run in `evidence/README.md` (audit 2026-10-27)
- [x] 4.3 Close the beads epic and run `br sync --flush-only`; done: epic policy4j-cvw4 closed; `br sync --flush-only`
