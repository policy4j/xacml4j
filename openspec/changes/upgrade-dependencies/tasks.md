Beads: epic `policy4j-yllw` (created with the proposal); task X.Y is issue `<epic>.X.Y`.
Gate (default): `PATH="<maven>/bin:$PATH" JAVA_HOME="$(jenv prefix 25)" rtk mvn -o -Dlicense.skipUpdateLicense=true clean test`, with Maven 3.9.16 and with Maven 3.10.0 in the final gate.
JMH: NOT APPLICABLE for every group (maintainer decision 2026-10-27).

## 1. Plan

- [ ] 1.1 Research Q2 to Q8 (`research/README.md`), from official docs and release notes
- [ ] 1.2 Plan review by a lane from another family; resolve findings; check: `openspec validate upgrade-dependencies --strict`; evidence: `evidence/reviews/plan/`
- [x] 1.3 Maintainer approval of the versions, the removals, the **BREAKING** logging change, and Open Questions 1 and 2 (approved 2026-10-27, recorded in proposal.md, Approval)

## 2. Logging: Log4j out, Logback in

- [x] 2.1 Characterize: `dependency:tree` of every module and the current test logging (`evidence/tests/001-characterization`); done (static, from the HEAD POM): `evidence/tests/001-characterization`
- [x] 2.2 Write the failing `LoggingConfigurationTest` cases (module override, property override, flush at exit, benchmark appender); done: `LoggingConfigurationTest` in `xacml-core`, `xacml-conformance-tests`, `xacml-jmh` (written before the first Logback build)
- [x] 2.3 Add `build-support/logback/` fragments, per-module `logback-test.xml`, the `xacml-jmh` `logback.xml`, the test-resource wiring, SLF4J 2.0.20 and Logback 1.6.6; remove Log4j and its configs outside the vendored tree (D1 to D3); check: `LoggingConfigurationTest`, module tests; done with a recorded stopgap: slf4j-api 2.0.17 and Logback 1.3.16 from the local repository (mirror unreachable); raise to 2.0.20 and 1.6.6 with one online `-U` build (`evidence/tests/002-logging-gate`)
- [x] 2.4 Add the enforcer ban rule (D4); prove it fails on a scratch POM; gate: review, reactor and conformance, `dependency:tree` and jar listings (`evidence/tests/`); done: `evidence/tests/004-logging-regate`, review `evidence/reviews/code/rv001-gates-astra` (REJECT; all five findings fixed, see its summary)

## 3. Runtime libraries and removals

- [ ] 3.1 Guava 33.7.2-jre, Gson 2.15.0, auto-service 1.1.1; check: reactor tests (auto-service 1.1.1 done offline, run 003; Guava and Gson wait for the mirror)
- [ ] 3.2 Dropwizard metrics 4.2.40 in `xacml-pdp` only; json-path per Q4; check: `xacml-pdp` and `xacml-core` tests
- [x] 3.3 Remove the unused managed entries and properties after the two-lane absence check (D6); check: reactor build, POM search record; done: `evidence/tests/003-offline-upgrades`
- [ ] 3.4 Gate: review, reactor and conformance

## 4. Test libraries and JAXB patches

- [ ] 4.1 Hamcrest 3.0 and Truth 1.4.5; check: reactor tests (Hamcrest 3.0 done offline, run 003; Truth waits for the mirror)
- [ ] 4.2 JUnit 6.1.3 or the latest 5.x per Q6; check: reactor tests
- [ ] 4.3 Latest JAXB 2.3.x patches (Q7) and the deprecation note in the docs; check: reactor tests and conformance
- [ ] 4.4 Gate: review, reactor and conformance

## 5. Final gate and docs

- [ ] 5.1 Reactor offline with Maven 3.9.16 and 3.10.0; conformance; `dependency:analyze`; version audit against Maven Central; vulnerability scan (Q8); evidence: `evidence/tests/`
- [ ] 5.2 Docs: `AGENTS.md` (logging, dependencies), `docs/ai/TESTING.md` (log levels and properties), release notes for the **BREAKING** items

## 6. Close out

- [ ] 6.1 `openspec validate upgrade-dependencies --strict`
- [ ] 6.2 Audit requirement → task → test → evidence in `evidence/README.md`
- [ ] 6.3 Close the beads epic and run `br sync --flush-only`
