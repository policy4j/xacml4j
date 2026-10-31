## ADDED Requirements

### Requirement: Build targets JDK 28 with preview
The reactor SHALL compile with `--release 28 --enable-preview` and SHALL run
all tests with `--enable-preview` on the pinned JDK 28 early-access build.
Preview flags SHALL be configured in the POMs for compilation and for test
execution, not only on the command line.

#### Scenario: Plain Maven invocation
- **WHEN** `mvn -Dlicense.skipUpdateLicense=true verify` runs with `JAVA_HOME` set to the pinned JDK 28 build
- **THEN** compilation and every test module run with preview enabled and pass

### Requirement: JDK 28 bump precedes value classes
The move to JDK 28 SHALL first land without any `value class` or
`value record` declarations, with the full reactor and conformance suites
green. Value classes SHALL be introduced only in later, separate changes.

#### Scenario: First JDK 28 commit
- **WHEN** the build first switches to JDK 28
- **THEN** no source file declares a value class or value record, and all tests pass

### Requirement: Value-class conversion requires an identity audit
A type SHALL be converted to a value class or value record only after an
audit shows that no code relies on its identity: reference `==` or `!=`
comparisons, `synchronized` blocks, `System.identityHashCode`,
`IdentityHashMap`, `WeakReference` or `WeakHashMap` keys, interning caches,
or reflective field mutation (for example by Gson or JAXB). Aggregates and
builders (for example `Entity`, `Record`, `BagOf`, registries, and evaluation
contexts) SHALL remain identity classes unless an audit proves them
eligible.

#### Scenario: Audit precedes conversion
- **WHEN** a type is converted to a value class
- **THEN** the change records the audit results for that type and adds equality and serialization tests

#### Scenario: Synchronization on a candidate
- **WHEN** the audit finds `synchronized` on instances of a candidate type
- **THEN** the type is not converted until that code is changed

### Requirement: Value classes convert one type family at a time
Value-class conversion SHALL proceed one type family per change (for
example `BooleanVal` and `Int64` first), and each change SHALL keep the
reactor and conformance suites green and SHALL report JMH results for the
converted family's hot paths.

#### Scenario: Family conversion gate
- **WHEN** a type family is converted
- **THEN** all tests pass on JDK 28 with preview and the change includes before/after JMH results

### Requirement: CI uses the pinned JDK 28 build
Continuous integration SHALL build and test on the same JDK 28
early-access build that developers use, identified by its build string or
checksum, with preview enabled.

#### Scenario: CI JDK check
- **WHEN** the CI job starts
- **THEN** it verifies the JDK build identity before building and fails on a mismatch
