## ADDED Requirements

### Requirement: Correlation ids without random numbers
The PDP SHALL give each `decide` call a correlation id of the form
`<prefix>-<sequence>`, where the prefix is fixed when the PDP is built and the
sequence is a per-PDP counter in hexadecimal that starts at 1 and grows by one
per `decide` call. The PDP SHALL NOT use a random number generator to name a
`decide` call or an individual decision.

#### Scenario: Sequence of one PDP
- **GIVEN** a PDP that has not decided a request yet
- **WHEN** it decides three requests
- **THEN** the three correlation ids have the same prefix and the sequences `1`, `2`, and `3` (`CorrelationIdTest`)

#### Scenario: Two PDPs number alike
- **GIVEN** two PDPs built in the same way
- **WHEN** each decides two requests
- **THEN** both give the sequences `1` and `2` (`CorrelationIdTest`)

#### Scenario: No random source in the PDP package
- **GIVEN** the main sources of `xacml-pdp` package `org.xacml4j.v30.pdp`
- **WHEN** the project ast-grep scan runs
- **THEN** it finds no `java.util.Random`, `ThreadLocalRandom`, `SecureRandom`, `Math.random()`, or `UUID.randomUUID()` (rule `java-no-random-on-decide-path` and its rule test)

### Requirement: Request ids of individual decisions
While the PDP evaluates an individual decision of a `decide` call, the
logging context key `XacmlRequestId` SHALL hold `<correlation id>-<n>`, where
`n` is 1 for the first individual decision of that call and grows by one for
each further individual decision of the same call. The PDP SHALL remove the
key when the individual decision ends.

#### Scenario: One request
- **GIVEN** a request that yields one individual decision
- **WHEN** the PDP decides it
- **THEN** the auditor sees `XacmlRequestId` equal to `<correlation id>-1`, and after `decide` returns the key is absent (`CorrelationIdTest`)

#### Scenario: Several individual decisions
- **GIVEN** a request handler that asks the PDP for two individual decisions in one `decide` call
- **WHEN** the PDP decides the request
- **THEN** the auditor sees `<correlation id>-1` and then `<correlation id>-2` (`CorrelationIdTest`)
