# 003: removals and the upgrades possible offline (tasks 3.1 part, 3.3, 4.1 part)

Date: 2026-10-27. Oracle GraalVM 25.0.4, Maven 3.9.16 offline (the repository mirror is
unreachable; only versions already in the local repository can be used).

Removed (unused):
- managed `protobuf-java`, `protobuf-java-util` (3.22.3), `ipaddress` (5.4.0), `eui4j-core`
  (0.0.3), and the properties `protobuf.version`, `ipaddress.version`, `euij4j.version`,
  `jaxb.api.version` (value `>2.3.3`), `jaxb.api.impl.version`, `json.smart.version`.
- Absence, two lanes: (1) no module POM declares any of them in `<dependencies>` (only the
  root's `<dependencyManagement>` listed them); (2) text search over sources, resources, and
  configuration outside `xacml-proto/` finds only the removed POM entries and a Javadoc link in
  `xacml-core/src/main/java/org/xacml4j/v30/EUI48.java` that credits eui4j's algorithm (no
  import). With no module declaring them, no class can compile against them; the reactor
  compiles.

Upgraded (cached locally): auto-service 1.0.1 to 1.1.1 (service files unchanged: core 2,
xml 1, jmh 2 interfaces); `org.hamcrest:hamcrest-all` 1.3 to `org.hamcrest:hamcrest` 3.0
(the tests use only `MatcherAssert` and `CoreMatchers`).

Result: BUILD SUCCESS, 8,206 tests, 0 failures, 18 skipped (per module as run 002).

Blocked on the mirror (not in the local repository): Guava 33.7.2-jre (newest cached 31.1),
Gson 2.15.0 (2.10.1), json-path 3.0.0 or 2.11.0 (2.9.0), Dropwizard metrics 4.2.40 (4.2.25),
Truth 1.4.5 (1.1.5), JUnit 6.1.3, jaxb-impl 2.3.9 (only its POM), slf4j-api 2.0.20 and
Logback 1.6.6 (stopgap 2.0.17 and 1.3.16).
