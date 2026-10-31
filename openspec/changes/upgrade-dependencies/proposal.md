## Why

The root POM puts `log4j-core` 2.14.0 at compile scope on every module (root
`<dependencies>`), so every published artifact pulls it to its users. Apache
lists 2.14.0 as affected by CVE-2021-44228 (CVSS 10.0, fixed in 2.15.0) and
CVE-2021-44832 (fixed in 2.17.0) (https://logging.apache.org/security.html,
retrieved 2026-10-27). The same block forces a logging backend and the 2013
Yammer metrics library on every module. Several other dependencies are years
old, three managed dependencies are used by no module, and two POM
properties are unused, one with an invalid value (`jaxb.api.version` is
`>2.3.3`).

## What Changes

- **Logging**: Log4j 2 is removed. Modules compile against `slf4j-api`
  2.0.20 only; Logback 1.6.6 (`logback-classic`) is the backend for tests
  (test scope) and for the `xacml-jmh` benchmark jar (runtime). Logging uses
  `AsyncAppender` around the console and file appenders. The Logback
  configuration is modular: shared fragments (appenders, async wrapping,
  default levels) in one directory that every module's test resources
  include, and a short `logback-test.xml` per module with its overrides.
  The maven-enforcer `bannedDependencies` rule bans `log4j-core` and the
  Log4j SLF4J bindings so they cannot return. **BREAKING**: the artifacts no
  longer bring a logging backend; users choose one (as SLF4J intends).
- **Runtime libraries**: Guava 31.1-jre to 33.7.2-jre; Gson 2.8.9 to 2.15.0
  (change `improve-xacml-parsing` may later replace it); json-path 2.8.0 to
  the latest release that keeps its API for `JsonContent` (3.0.0 or the last
  2.x, research Q4); Yammer `com.codahale.metrics:metrics-core` 3.0.2 replaced
  by Dropwizard `io.dropwizard.metrics:metrics-core` 4.2.40 (same package
  `com.codahale.metrics`), declared by `xacml-pdp` only; auto-service 1.0.1
  to 1.1.1.
- **Removed (deprecated, unused)**: managed `protobuf-java` and
  `protobuf-java-util` 3.22.3, `ipaddress` 5.4.0, and `eui4j-core` 0.0.3
  (no module declares them; no import outside the non-module `xacml-proto/`);
  the properties `jaxb.api.version`, `jaxb.api.impl.version`,
  `json.smart.version`, `yammer.metrics.version`, `log4j.version`, and
  `protobuf.version`, `ipaddress.version`, `euij4j.version` once unused.
- **Test libraries**: `hamcrest-all` 1.3 to `org.hamcrest:hamcrest` 3.0;
  Truth 1.1.5 to 1.4.5 (with the Java 8 extension only if 1.4.5 still needs
  it); JUnit 5.14.4 to 6.1.3 if research Q6 finds the migration small,
  otherwise the latest 5.x (Mockito 5.24.0 is current).
- **JAXB**: stays on the `javax.xml.bind` 2.3 line at its latest patch in
  this change; the Jakarta XML Binding 4 migration (namespace change of the
  generated code) is decided together with change `improve-xacml-parsing`,
  which may replace JAXB on the parse path. The 2.3 line is marked
  deprecated in the docs.
- Root `<dependencies>` keeps only what every module needs (`slf4j-api`
  and test libraries).

## Capabilities

### New Capabilities

- `dependency-hygiene`: current, used, and non-vulnerable dependencies,
  enforced by the build.
- `logging-configuration`: SLF4J API only in library code; Logback with
  async appenders and modular configuration for tests and benchmarks.

### Modified Capabilities

None.

## Research Questions

- Q1. Latest releases (answered for the versions above from Maven Central
  metadata, retrieved 2026-10-27; stable versions only).
- Q2. Logback 1.6.6: Java baseline, `AsyncAppender` options (queue size,
  discarding threshold, never block, caller data), shutdown hook syntax,
  `<include>` of class-path fragments, and what replaced Janino conditions.
- Q3. SLF4J 2.0 provider lookup with Logback 1.6; behavior with no provider.
- Q4. json-path 3.0.0 changes against the API `JsonContent` uses
  (`Configuration`, `DocumentContext`, `JsonPath`, `Option`, `TypeRef`), and
  its JSON provider default.
- Q5. Guava 32 to 33 removals and behavior changes for the used APIs; Gson
  2.9 to 2.15 behavior changes (strictness, number handling) that affect
  `xacml-json`.
- Q6. JUnit 6 migration steps and Mockito and Truth compatibility; Truth 1.4
  and its Java 8 extension; Hamcrest 3.0 artifact names.
- Q7. Latest patch of `jakarta.xml.bind-api` 2.3.x and `jaxb-impl` 2.3.x.
- Q8. A vulnerability scan that runs offline or with the available tools
  (OSV-Scanner, OWASP dependency-check) for the gate.

## Verification

- Full reactor `clean test` on JDK 25 (change `adopt-jdk25-baseline`) with Maven 3.9.16 and 3.10.0; OASIS
  XACML 2.0/3.0 and RSA 2008 conformance unchanged (937 run, 12 skipped).
- `dependency:tree` for every module shows no `log4j-core`, no Log4j SLF4J
  binding, and no Yammer metrics; `dependency:analyze` lists no used
  undeclared and no unused declared dependency without a recorded reason.
- The enforcer rule fails a test POM that adds `log4j-core`.
- Test logs are written through Logback (a test checks that the async file
  appender received a message).
- A vulnerability scan of the resolved dependencies shows no known critical
  or high vulnerability, or each is recorded with a reason.
- JMH: NOT APPLICABLE (maintainer decision 2026-10-27: dependency upgrades
  do not need JMH runs).

## Impact

- POMs: root (properties, `<dependencies>`, `<dependencyManagement>`,
  enforcer rules), every module POM that declares a changed dependency.
- Config: five `log4j2*.xml` files replaced; one of them is
  `xacml-conformance-tests/src/test/resources/log4j2-test.xml`, inside the
  vendored fixture tree: the maintainer decides whether it may be deleted
  (it is inert once Log4j is gone); the new conformance configuration lives
  outside that tree.
- Code: none expected beyond API changes found by research (Guava, Truth,
  JUnit, json-path); each found change is listed in its task.
- Users: **BREAKING** no logging backend shipped; metrics artifact
  coordinates change (same packages).
- Dependency approval needed: every version change and every removal above.

## Approval

Approved by the maintainer on 2026-10-27: all version changes and all deletions,
including `xacml-conformance-tests/src/test/resources/log4j2-test.xml`
(Open Question 1 of the design: yes, delete it). Test log levels (Open
Question 2) were not answered; the proposed default applies (console WARN,
file INFO, `org.xacml4j` DEBUG to the file). Builds and tests run on JDK 25
(change `adopt-jdk25-baseline`). JMH: not applicable (maintainer, 2026-10-27).

## Tracking

Beads epic `policy4j-yllw` (created with this proposal at the maintainer's
request); task X.Y maps to issue `<epic>.X.Y`. Implementation starts after
the maintainer's approval; the logging group can be approved and done first.
