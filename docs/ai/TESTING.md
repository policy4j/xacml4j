# Testing

Tests are the proof behind every OpenSpec task. This file covers the
commands, the test stack, what to test for each kind of change, and how to
record results as evidence.

## Current state

- Implementation follows `modernize-core-for-jdk28-valhalla` on
  `feature_xacml_20` (design D1).
- Stack: JUnit 5 (Jupiter, `junit-bom` 5.14.4) and Mockito 5.24.0.
  Google Truth (1.1.5) and Hamcrest stay.
- Mockito starts as a JVM agent. The `maven-dependency-plugin` `properties`
  goal supplies the JAR path; Surefire uses
  `@{argLine} -javaagent:${org.mockito:mockito-core:jar}`. See
  [run 009](../../openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/009-dual-engine/summary.md)
  for the setup proof.
- JUnit 4, Vintage, and EasyMock are removed, and Objenesis is no longer a
  direct dependency (task 2.7). Mockito still brings Objenesis in
  transitively; do not exclude it, because Mockito needs it to create class
  mocks.
- No coverage tool is configured. Adding JaCoCo is a build change and needs
  its own task.

## Commands

Select the JDK per run; the local `.java-version` says 11 and is ignored.
Always skip the license header rewrite. Run from the repository root.

| Goal | Command |
| --- | --- |
| Compile | `JAVA_HOME="$(jenv prefix 25)" rtk mvn -q -Dlicense.skipUpdateLicense=true compile` |
| One module (with what it needs) | `JAVA_HOME="$(jenv prefix 25)" rtk mvn -q -Dlicense.skipUpdateLicense=true -pl xacml-core -am test` |
| One class | `... -pl xacml-pdp -am -Dtest=DefaultPolicyDecisionPointTest -Dsurefire.failIfNoSpecifiedTests=false test` |
| One method (globs allowed) | `... -pl xacml-core -am -Dtest='RuleTest#<method>' -Dsurefire.failIfNoSpecifiedTests=false test` |
| Conformance suites | `... -pl xacml-conformance-tests -am -Dtest='Xacml20ConformanceTest,Xacml30ConformanceTest,RSA2008InteropTest,XacmlExampleTest' -Dsurefire.failIfNoSpecifiedTests=false test` |
| Full gate | `JAVA_HOME="$(jenv prefix 25)" rtk mvn -Dlicense.skipUpdateLicense=true verify` |

The baseline is JDK 25 (change `adopt-jdk25-baseline`); replace `25` with `28-ea` from group 8 of `modernize-core-for-jdk28-valhalla` on. After any Maven run,
`rtk git status` must show no license-header churn. Surefire skips
`**/*TestCase.java` (the RSA 2008 performance suite).

Maven: the root POM pins every plugin (`<pluginManagement>`, change
`upgrade-build-plugins`), so Maven 3.9.16 and 3.10.0 build the reactor alike
and the header update runs only with `-Dlicense.skipUpdateLicense=false`.
Offline (`-o`), each Maven needs the pinned plugins in the local repository
under an id it accepts: after Maven 3.10 downloads a plugin, run one online
build with Maven 3.9 before an offline 3.9 build. When the configured
repository mirror is unreachable, build offline with IntelliJ's bundled
Maven 3.9.16 first on `PATH`.

## Test layers

| Layer | Where | Use it for |
| --- | --- | --- |
| Unit | `*/src/test/java` next to the code | Value types, functions, combining algorithms, policy model, marshalling pieces |
| Policy tests | `xacml-test` support (`XacmlPolicyTestSupport`, `AttributeResolverTestSupport`) | Evaluating small policies against requests with stubbed attributes |
| PDP and SPI | `xacml-pdp` tests | Decision flow, policy repositories, attribute resolvers (PIP), multi-request handling |
| Marshalling | `xacml-xml`, `xacml-json` tests | XACML 2.0/3.0 XML and JSON round trips |
| Conformance | `xacml-conformance-tests` | OASIS 2.0/3.0 suites, RSA 2008 interop, examples: the behavior gate |

## What to test by change type

| Change | Minimum tests |
| --- | --- |
| Function (`@PolicyFuncProvider`) | Parameterized truth table citing the XACML Appendix A entry; wrong arity and wrong types; empty bag, one-and-only with 0 and 2 values; Indeterminate on error |
| Combining algorithm | Full decision table (Permit, Deny, NotApplicable, Indeterminate{D,P,DP}); obligations and advice propagation; lookup by URN |
| Data type | Lexical parsing including invalid input and boundaries; canonical `equals`/`hashCode`; round trip through string, XML, and JSON |
| Attribute designator or selector | Result is a bag; MustBePresent true/false with a missing attribute and with a resolver failure (status codes); issuer matching |
| Condition or target | Absent, true, false, Indeterminate, empty and multi-valued results |
| Marshalling | Round trip of a representative fixture; malformed input fails with a clear error |
| PDP, cache, registry | Concurrency-sensitive state (shared caches, registries) under parallel use; ServiceLoader discovery |
| Value class conversion (JEP 401) | Equality and hashing, JSON and XML serialization, use in bags; the identity audit from the change; JMH before and after (docs/ai/BENCHMARKS.md) |

Every behavioral task starts with a failing test (new behavior or bug) or
a characterization test that pins current behavior (refactors). Record it
before you change production code.

## Writing tests

- Match the file's indentation and keep the license header.
- Name tests after behavior: `integerDivideTruncatesTowardZero`, not
  `test1`.
- Cite the XACML section in a comment when the expected value comes from
  the standard. Never copy an expected value from the code under test.
- Use real policy model and value objects. Mock interfaces and collaborators
  such as repositories, resolvers, and evaluation contexts. Value types may
  become JEP 401 value classes, which cannot be mocked.
- No network, no wall-clock sleeps, no dependence on the default locale,
  time zone, or hash iteration order. Use `@TempDir` for files.
- Never weaken or delete an assertion, or disable a test to make it pass.
  `@Disabled` needs a written reason and an issue.
- New fixtures go in the module's own `src/test/resources`. The OASIS and
  RSA fixtures (`xacml-conformance-tests/src/test/resources/**`) and the
  XSDs are read-only. `VendoredFixtureIntegrityTest` fails on any change to
  them; after a deliberate upstream update, refresh the manifest with
  `GenerateVendoredFixtureManifest --write` (change evidence run 015).

Use Jupiter's `@Test`, `@BeforeEach`/`@BeforeAll`, `@Nested`, and
`@ParameterizedTest` with `@MethodSource` or `@CsvSource` for truth tables.
Use `assertThrows` for expected exceptions. Wrap only the throwing call;
put verifications after `assertThrows`.

Mockito rules:

- Prefer `@ExtendWith(MockitoExtension.class)` with strict stubs. Any
  `lenient()` stub needs a comment explaining why.
- Create fresh mocks in `@BeforeEach` instead of resetting them.
- Verify every interaction the test depends on with the exact count,
  including stubbed calls. Keep total count checks alongside `InOrder`:
  strict stubs can mark extra stubbed calls as verified.
- Use `InOrder` and `verifyNoMoreInteractions` where order or strictness
  matters.
- Use `verifyNoInteractions` for collaborators that must not be touched.
- Assert the values captured by every `ArgumentCaptor`.
- Do not call a collaborator mock to set up an interaction that a later
  `verify(...)` would count. Stub return values with `when(...)` and put
  verifications after the code under test.

## Test migration parity

While tests move to JUnit 5 and Mockito, each converted module keeps at
least its baseline test-method count, and conformance keeps at least its
baseline executed-case count. A lower number fails the gate until each
missing test is accounted for.

Jupiter does not report a `@Disabled` class with no test methods in
Surefire; Vintage did. This accounts for the core count changing from
762 to 760 and the PDP count from 74 to 73. Record this difference while
keeping the declared test methods and real skips unchanged.

## Recording results

Each run is a directory under the change's `evidence/tests/`
(docs/ai/OPENSPEC.md "Evidence"):

- `command.txt`: the exact command, including the JDK selector.
- `summary.md`: JDK build (`java -version`), modules, tests run, failures,
  errors, skipped, and for conformance the executed, skipped, and failed
  case counts.
- `surefire-summary.txt`: the Maven `Results:` section and the per-module
  `Tests run:` lines. Replace the checkout path with `<repo>`.

Do not commit `target/`, full Maven logs, or per-suite report trees.
