## ADDED Requirements

### Requirement: Java 25 class files
Every module SHALL compile with `<release>25</release>` and without preview
features, and the build SHALL fail on a JDK older than 25.

#### Scenario: Class file version
- **GIVEN** the reactor built on GraalVM 25
- **WHEN** `javap -v` reads one class of each module
- **THEN** each shows major version 69 and no preview minor version (task 2.3 evidence)

#### Scenario: Older JDK
- **GIVEN** JDK 21
- **WHEN** the build validates
- **THEN** the enforcer `requireJavaVersion` rule fails it (task 2.3 evidence)

### Requirement: Same behavior on JDK 25
Moving to JDK 25 SHALL NOT change a test result, a conformance result, or a
generated service registration.

#### Scenario: Reactor gate
- **GIVEN** the reactor at release 25
- **WHEN** `clean test` runs on GraalVM 25
- **THEN** the test counts equal the JDK 21 gate and conformance is 937 run, 12 skipped (task 2.3 evidence)

#### Scenario: Service files
- **GIVEN** the `xacml-core`, `xacml-xml`, and `xacml-jmh` jars built at release 25
- **WHEN** their `META-INF/services` entries are listed
- **THEN** they equal those of the JDK 21 build (task 2.3 evidence)

### Requirement: Measured on JDK 25
The JMH baseline and every later before/after pair SHALL run on JDK 25 with
the C2 JIT, and each run record SHALL show the JDK build and the JIT flags.

#### Scenario: Baseline on JDK 25
- **GIVEN** the benchmarks jar built at release 25
- **WHEN** the Benchmark Plan of `add-pdp-jmh-benchmarks` runs on GraalVM 25 with `-XX:-UseJVMCICompiler`
- **THEN** the results are complete by the check script and `env.txt` shows Java 25 and `UseJVMCICompiler = false` (task 3.1 evidence)
