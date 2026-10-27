## Context

- Root `pom.xml` `<dependencies>` gives every module `slf4j-api`,
  `log4j-api`, `log4j-core` (compile), `log4j-slf4j-impl` (runtime),
  `com.codahale.metrics:metrics-core` 3.0.2 (compile), and the test
  libraries. No Java file imports `org.apache.logging.log4j` (checked
  2026-10-27); code logs through SLF4J.
- Metrics are used only in `xacml-pdp` (`DefaultPolicyDecisionPoint`,
  `MetricsSupport`, `PolicyDecisionPointBuilder`, package
  `com.codahale.metrics`); `io.dropwizard.metrics:metrics-core` 4.1.17 is
  managed but declared by no module.
- json-path is used only by `xacml-core` `JsonContent`.
- Managed but declared by no module: `protobuf-java`, `protobuf-java-util`,
  `ipaddress`, `eui4j-core` (imports exist only in `xacml-proto/`, which is
  not a module).
- Logging configs: `log4j2-test.xml` in `xacml-core`, `xacml-pdp`,
  `xacml-json`, and `xacml-conformance-tests` test resources (root level
  DEBUG to console and a rolling file, with caller data `%M`, `%F:%L`), and
  `log4j2.xml` in `xacml-jmh` main resources (WARN).

## Goals / Non-Goals

**Goals:** no vulnerable or unused dependency; SLF4J API only in libraries;
Logback with async appenders and modular configuration; every dependency at
its latest stable release or with a recorded reason; a build rule that keeps
Log4j out.

**Non-Goals:** the JSON framework choice and any JAXB replacement (change
`improve-xacml-parsing`); a Jakarta XML Binding migration; JMH runs
(maintainer decision 2026-10-27).

## Research Basis

`research/README.md`: versions from Maven Central metadata (2026-10-27) and
the Log4j advisory; Q2 to Q8 pending.

## Decisions

### D1. Logging backend: Logback 1.6.6 via SLF4J 2.0.20

Library modules compile against `slf4j-api` only. `logback-classic` is test
scope in the root `<dependencies>` and runtime scope in `xacml-jmh`.
Alternatives: upgrade Log4j to 2.26.1 (fixes the CVEs but keeps a backend in
the artifacts); `slf4j-simple` for tests (no async appender, no includes).
The maintainer asked for Logback (2026-10-27).

### D2. Modular configuration through shared class-path fragments

Shared files in `build-support/logback/` (new, project-owned):
`xacml4j-appenders.xml` (console and rolling-file appenders, each wrapped in
an `AsyncAppender`), `xacml4j-levels.xml` (default levels, overridable by
`${xacml4j.log.level:-INFO}` and per-package properties), and
`xacml4j-base.xml` (includes both). The root POM adds that directory to every
module's test resources (`<testResources>` with the module's own
`src/test/resources` first), so each module's `logback-test.xml` is:
`<configuration><include resource="xacml4j-base.xml"/>` plus its overrides.
`xacml-jmh` adds the directory to its main resources and ships a
`logback.xml` that includes the fragments with WARN and never-block.
Alternatives: a copy per module (drifts); a test-support module that ships
the fragments (one more module to depend on from every module, and
`xacml-core` would need it in test scope only); rejected for now. Q2 confirms
`<include resource=...>` and property-default syntax for 1.6.

### D3. Async appenders

`AsyncAppender` queue 8,192, discarding threshold 0 in tests (keep every
level), caller data off (the current `%M`, `%F:%L` patterns force caller
data; the new patterns use the logger name instead), shutdown hook so queued
events reach the file before exit. Benchmarks: never-block on, so a full
queue drops events instead of stalling a measured thread. Default levels:
console WARN, file INFO, `org.xacml4j` DEBUG to the file only (today: root
DEBUG to both, which slows and floods test output).

### D4. Ban rule

maven-enforcer `bannedDependencies` in the root build: excludes
`org.apache.logging.log4j:log4j-core`, `:log4j-slf4j-impl`,
`:log4j-slf4j2-impl`, and `com.codahale.metrics:metrics-core` (the Yammer
coordinates); `searchTransitive` true.

### D5. Runtime libraries

Guava 33.7.2-jre, Gson 2.15.0, auto-service 1.1.1, Dropwizard metrics
4.2.40 in `xacml-pdp` only. json-path: 3.0.0 if Q4 finds the used API
intact, else the latest 2.x, with the reason recorded. Each upgrade lands
with the module's tests; API changes found by research become listed edits.

### D6. Removals

Delete the managed entries and properties of protobuf, ipaddress, eui4j,
Yammer metrics, Log4j, and the invalid `jaxb.api.*` properties. Absence is
supported by two lanes: no module POM declares them (POM search), and no
import or class-path resource refers to them outside `xacml-proto/` (text
search including resources and configuration).

### D7. Test libraries

`org.hamcrest:hamcrest` 3.0 replaces `hamcrest-all` 1.3; Truth 1.4.5
(the Java 8 extension kept only if 1.4.5 requires it); JUnit 6.1.3 when Q6
finds the migration limited to the BOM and known removed APIs, else the
latest 5.x with the reason recorded. Mockito stays at 5.24.0 (latest).

### D8. JAXB stays on 2.3 for now

Latest 2.3.x patches (Q7). The Jakarta 4 migration changes the generated
packages, the XJC plugin, and every mapper import; it waits for the parse-path
decision of `improve-xacml-parsing` (a direct StAX parser would make JAXB
unnecessary on that path). The docs mark the 2.3 line deprecated.

## Test Strategy

- Characterization first: `dependency:tree` of every module before the
  change (shows `log4j-core` 2.14.0 in each), and the current test output.
- `LoggingConfigurationTest` (one per module that has tests, sharing a
  helper in its own test sources) for the spec scenarios; `xacml-jmh`
  checks its `logback.xml`.
- Module tests and the full reactor after each group; conformance counts.

## Benchmark Plan

NOT APPLICABLE: the maintainer decided on 2026-10-27 that dependency
upgrades need no JMH runs. The benchmark jar's logging configuration is
checked by `LoggingConfigurationTest` instead.

## Code Intelligence Plan

- Removals: POM search over every `pom.xml`, `rtk rg` over sources,
  resources, and configuration for the group ids and package names; Serena
  references for any class found.
- API changes: compile errors and test failures after each version step;
  `rtk rg` for the removed APIs named by research.

## Execution Plan

One writer (the conductor) for the POMs and configs, so no two lanes edit
`pom.xml` at once. Review by a lane from another family per gate (Codex Sol
or Astra; Agy Argon for the logging configuration).

## Risks / Trade-offs

- [Users relied on the shipped Log4j backend] → **BREAKING** note with the
  one-line dependency to add (any SLF4J provider).
- [json-path 3.0 or JUnit 6 API breaks] → fall back to the latest
  compatible line with the reason recorded.
- [Async appender loses events at exit] → shutdown hook and the flush test.
- [A logging file inside the vendored conformance tree] → the maintainer
  approved deleting `log4j2-test.xml` (2026-10-27); the new conformance
  config sits in `xacml-conformance-tests/src/test/logback/`, added as a
  test resource directory, so the vendored tree gets no new file.

## Migration and Rollback

Each group changes POMs and configs only; reverting a group restores its
POMs. Users add an SLF4J provider of their choice.

## Open Questions

1. Resolved 2026-10-27: the maintainer approved deleting
   `xacml-conformance-tests/src/test/resources/log4j2-test.xml`.
2. Not answered on 2026-10-27; the proposed default applies (console WARN,
   file INFO, `org.xacml4j` DEBUG to the file).
