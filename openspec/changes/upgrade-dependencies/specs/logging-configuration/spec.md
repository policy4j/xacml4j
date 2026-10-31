## ADDED Requirements

### Requirement: Modular Logback configuration
Test logging SHALL use Logback through shared configuration fragments kept
in one directory (`build-support/logback/`): appenders, async wrapping, and
default levels. Each module's `src/test/resources/logback-test.xml` SHALL
include the shared fragments and add only its own overrides; the
`xacml-jmh` benchmark jar SHALL carry a `logback.xml` built from the same
fragments. No library module SHALL ship a Logback configuration in its main
artifact. Levels SHALL be overridable by system properties without editing a
file.

#### Scenario: Shared defaults
- **GIVEN** `xacml-core` with only the shared configuration and its file name
- **WHEN** its tests run
- **THEN** root is INFO, `org.xacml4j` is DEBUG, the file is `xacml-core-test.log`, and the async file appender has caller data off, discarding threshold 0, and never-block off (`LoggingConfigurationTest` in `xacml-core`)

#### Scenario: Module override
- **GIVEN** `xacml-conformance-tests`, whose `logback-test.xml` (in `src/test/logback`, outside the vendored tree) sets `org.xacml4j` to INFO
- **WHEN** its tests run
- **THEN** `org.xacml4j` logs at INFO and root keeps the shared default (`LoggingConfigurationTest` in `xacml-conformance-tests`)

#### Scenario: Property override
- **GIVEN** a JVM started with `-Dxacml4j.log.level=TRACE` on the `xacml-core` test class path
- **WHEN** it reads the shared configuration
- **THEN** the root level is TRACE (`LoggingConfigurationTest` in `xacml-core`, child JVM)

#### Scenario: Library jars
- **GIVEN** the main jars of `xacml-core`, `xacml-pdp`, `xacml-xml`, `xacml-json`, and `xacml-test`
- **WHEN** their entries are listed
- **THEN** none holds `logback.xml`, `logback-test.xml`, or a `log4j2*.xml` (task 2.4 evidence)

### Requirement: Asynchronous appenders
Console and file output SHALL go through Logback `AsyncAppender`s without
caller data, and every queued event SHALL be written before the JVM exits
(shutdown hook, with no flush time limit). The benchmark configuration SHALL
never block the logging thread: events past a full queue are dropped (Logback's
`AsyncAppender` does not count them).

#### Scenario: Flush at exit
- **GIVEN** a JVM that logs 10,000 INFO messages and returns from `main` at once
- **WHEN** the file appender's file is read after the JVM ends
- **THEN** it holds all 10,000 messages, the last one last (`LoggingConfigurationTest` in `xacml-core`, child JVM)

#### Scenario: Benchmark appender never blocks
- **GIVEN** the `xacml-jmh` `logback.xml`
- **WHEN** it is parsed by Logback
- **THEN** its async appender has never-block on and caller data off (`LoggingConfigurationTest` in `xacml-jmh`)
