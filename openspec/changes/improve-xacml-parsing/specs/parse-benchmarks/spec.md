## ADDED Requirements

### Requirement: Parse benchmarks
`xacml-jmh` SHALL provide JMH benchmarks of policy parsing and request parsing:
`PolicyParseBenchmark` (each policy of a corpus, by corpus name as in
`PdpDecideBenchmark`) and `RequestParseBenchmark` (each request of a corpus,
written as XACML 3.0 XML and as JSON by the project's marshallers; XACML 2.0 requests
are not written, the project has no 2.0 request marshaller),
with parameters for XSD validation (`none`, `xsd`) and the JSON provider. Each
benchmark SHALL check in setup that every input parses to a model equal to the
corpus model (same decisions for the corpus requests), so a broken parser
cannot produce numbers.

#### Scenario: Every input checked
- **GIVEN** each built-in corpus and each parameter set
- **WHEN** a benchmark is set up
- **THEN** setup parses every input once and compares the results with the corpus; a mismatch fails setup (`ParseBenchmarkTest`)

#### Scenario: Smoke run
- **GIVEN** the shaded benchmarks jar
- **WHEN** both parse benchmarks run with one fork, no warmup, one short iteration, and `-foe true`
- **THEN** every method and parameter set produces a score (`ParseBenchmarkTest`, in-process short run)

### Requirement: JMH pair for every optimization cycle
Every optimization cycle of this change, and every group that changes the
parse path, SHALL record a JMH run before and a JMH run after the change, back
to back on the same machine, JDK, JIT (C2), and flags, with `-prof gc`, three
forks, and five measurement iterations; both runs SHALL be rows of
`evidence/benchmarks/jmh/LEDGER.md`. A speed or allocation claim SHALL be made
only when the 99.9% error intervals of the pair do not overlap.

#### Scenario: Ledger audit
- **GIVEN** the change's tasks marked done
- **WHEN** the close-out audit runs
- **THEN** every optimization and parse-path group names a before run and an after run in the ledger (`evidence/README.md` audit, task 10.2)
