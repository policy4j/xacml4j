Beads: epic `policy4j-uunt` (created with the proposal); task X.Y is issue `<epic>.X.Y`.
Gate (default for code tasks): `PATH="<maven-3.9.16>/bin:$PATH" JAVA_HOME="$(jenv prefix 25)" rtk mvn -o -Dlicense.skipUpdateLicense=true clean test`
JMH rule: every group that changes the parse path and every optimization cycle records a before/after pair (spec `parse-benchmarks`); lanes stay idle during JMH runs.

## 1. Plan

- [x] 1.1 Discovery swarm (design Execution Plan, six NTM lanes, read-only) answers research Q1 to Q7; reports in `research/`, `research/synthesis.md`; check: every lane indexed in `research/README.md`; done: six reports in `research/`, `research/synthesis.md`
- [ ] 1.2 Plan review by lanes from other families than the author; resolve findings in the artifacts; check: `openspec validate improve-xacml-parsing --strict`; evidence: `evidence/reviews/plan/`
- [ ] 1.3 Maintainer approval of the proposal, the **BREAKING** items, and the dependency decisions; record it in the proposal

## 2. Parse benchmarks and baseline

- [x] 2.1 Write `ParseBenchmarkTest` (setup checks every input; smoke cases) before the benchmarks; check: fails to compile or fails; done: `evidence/tests/001-parse-benchmarks-red`
- [ ] 2.2 Add `PolicyParseBenchmark` and `RequestParseBenchmark` (spec `parse-benchmarks`); prototype the three position-capture candidates of D3 (`-Xlocator`, SAX side table, JAXB over Woodstox) in a scratch copy and measure each against capture off; check: `ParseBenchmarkTest`, `BenchmarkSmokeTest` (benchmarks done: `evidence/tests/002-parse-benchmarks-green`; the `-Xlocator` prototype is open)
- [x] 2.3 Record the parse baseline (`evidence/benchmarks/jmh/001-parse-baseline`, ledger rows); check: completeness script; done: `evidence/benchmarks/jmh/001-parse-baseline` (6 results, completeness checked; ledger rows)
- [ ] 2.4 Gate: review, module tests, smoke run; JMH: the baseline is this group's run

## 3. Diagnostics core

- [ ] 3.1 Characterize current messages and exception types for the broken-document catalog (`evidence/tests/`)
- [ ] 3.2 Write failing tests: `SyntaxExceptionTest`, `DiagnosticFormatterTest`
- [ ] 3.3 Add the diagnostics model, collector, `ParseOptions`, `Unmarshaller` default method, and both formatters (D1, D2, D5); check: the tests of 3.2
- [ ] 3.4 Gate: review, reactor and conformance; JMH: not applicable (no parse-path change until group 4)

## 4. XML positions and several errors per parse

- [ ] 4.1 Write failing `PolicyDiagnosticsTest`, `RequestDiagnosticsTest`, `DiagnosticGoldenTest` with the XML catalog
- [ ] 4.2 Positions (D3 choice) and the path stack; unknown root as `SyntaxException`; check: root and position cases
- [ ] 4.3 Move the XACML 3.0 policy and request mappers to the collector (D4); check: catalog cases for 3.0
- [ ] 4.4 Move the XACML 2.0 policy and request mappers; check: catalog cases for 2.0
- [ ] 4.5 Gate: review, reactor and conformance; JMH before/after on valid input (`evidence/benchmarks/jmh/`); docs

## 5. XML parsing security and limits

- [ ] 5.1 Write failing `SecureXmlFactoriesTest`, `XmlAttackCorpusTest`, `ParseLimitsTest`, and the ast-grep rule `java-xml-factory-outside-securexml` with its rule test
- [ ] 5.2 Add `SecureXml`; move `BaseJAXBUnmarshaller`, `DOMUtil`, and every other factory use to it; fail-closed setup (D6); check: factory and attack tests, `ast-grep scan`
- [ ] 5.3 Add the limits filter on every path and the strict caller-parser option; check: `ParseLimitsTest`
- [ ] 5.4 Gate: security review by a lane from another family, reactor and conformance, JMH before/after (limits overhead), docs (**BREAKING** DOCTYPE note)

## 6. Optional XSD validation

- [ ] 6.1 Characterize the schema validity of the conformance fixtures (research Q6, read-only); write failing `XsdValidationTest`
- [ ] 6.2 Add schema compilation, class-path resolution, and the single-pass validating filter (D7); check: `XsdValidationTest`, `XsdConformanceCharacterizationTest`
- [ ] 6.3 Gate: review, reactor and conformance, JMH before/after with `validation=none` and `validation=xsd`, docs

## 7. JSON limits, positions, and provider

- [ ] 7.1 Write failing `JsonParseLimitsTest` and `JsonRequestDiagnosticsTest` against the current provider
- [ ] 7.2 JSON bake-off prototypes (D8) in a scratch module; JMH comparison run per candidate; decision recorded in the design
- [ ] 7.3 Implement the chosen provider (or harden Gson in place) with limits, duplicate detection, and positions; JSON conformance parity; check: the JSON tests, parity run
- [ ] 7.4 Gate: review, reactor and conformance, JMH before/after, dependency approval recorded, docs

## 8. Parse optimization cycles

- [ ] 8.1 Profile the parse benchmarks (async-profiler CPU and allocation; swarm analysis of the recorded profiles); hot spots into `research/`, one task group per hot spot into this file
- [ ] 8.2 Gate: plan review of the new groups; each new group ends with its own JMH before/after pair; repeat 8.1 until the stop rule of D9 holds

## 9. Checker CLI (only if Open Question 1 keeps it)

- [ ] 9.1 Write the failing CLI test (exit codes, text and SARIF output)
- [ ] 9.2 Add the CLI (D10); check: the CLI test
- [ ] 9.3 Gate: review, docs; JMH: not applicable (no parse-path change)

## 10. Close out

- [ ] 10.1 `openspec validate improve-xacml-parsing --strict`
- [ ] 10.2 Audit requirement → task → test → evidence in `evidence/README.md`, including the JMH pair of every parse-path group and cycle
- [ ] 10.3 Close the beads epic and run `br sync --flush-only`
