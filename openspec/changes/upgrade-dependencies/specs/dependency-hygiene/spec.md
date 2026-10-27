## ADDED Requirements

### Requirement: No vulnerable logging backend in the artifacts
No module SHALL declare or inherit `org.apache.logging.log4j:log4j-core` or a
Log4j SLF4J binding in any scope, and the build SHALL fail when one is added.
Library modules SHALL depend on `org.slf4j:slf4j-api` only for logging.

#### Scenario: Dependency tree
- **GIVEN** the reactor
- **WHEN** `dependency:tree` runs for every module
- **THEN** no `log4j-core`, `log4j-slf4j-impl`, or `log4j-slf4j2-impl` appears, and `logback-classic` appears only in test scope, except runtime scope in `xacml-jmh` (task 2.4 evidence)

#### Scenario: Ban rule
- **GIVEN** a scratch copy whose `xacml-core` POM declares `log4j-core`
- **WHEN** the build validates
- **THEN** the enforcer `bannedDependencies` rule fails the build (task 2.4 evidence)

### Requirement: Current and used dependencies
Every managed dependency SHALL be at the latest stable release that keeps the
project's tests and conformance green, or SHALL record in the design why it
stays older; a managed dependency that no module uses SHALL be removed, and
so SHALL a POM property that nothing references.

#### Scenario: Version audit
- **GIVEN** the root POM after the change
- **WHEN** each managed version is compared with Maven Central metadata of the gate day
- **THEN** each is the latest stable release or has a recorded reason (task 5.1 evidence)

#### Scenario: Unused entries
- **GIVEN** the root POM after the change
- **WHEN** `dependency:analyze` runs and the properties are searched in every POM
- **THEN** no managed dependency is unused by all modules and no property is unreferenced, apart from recorded reasons (task 5.1 evidence)

### Requirement: Behavior unchanged
The upgrades SHALL NOT change a decision, a parse result, or a marshalled
document.

#### Scenario: Gate
- **GIVEN** the upgraded reactor
- **WHEN** `clean test` runs offline with Maven 3.9.16 and 3.10.0 on JDK 25
- **THEN** all tests pass and conformance stays at 937 run, 12 skipped (task 5.1 evidence)

#### Scenario: Vulnerability scan
- **GIVEN** the resolved dependencies of every module
- **WHEN** the scan of research Q8 runs
- **THEN** no known critical or high vulnerability remains without a recorded reason (task 5.1 evidence)
