# Testing

Tests are the proof behind every OpenSpec task. This file covers the
commands, the test stack, what to test for each kind of change, and how to
record results as evidence.

## Current state

- The `feature_xacml_20` working tree does not compile. Implementation work
  follows the change `modernize-core-for-jdk28-valhalla`, which first builds
  a green baseline on an integration branch (tasks 1.x). Until that exists,
  a test run proves nothing about the working tree.
- Stack today: the JUnit 4 API on the JUnit Platform through
  `junit-vintage-engine`, EasyMock, Google Truth, and Hamcrest. There is
  no Jupiter dependency yet.
- Target stack (change group 2): JUnit 5 (Jupiter) and Mockito; Truth and
  Hamcrest stay; JUnit 4, Vintage, EasyMock, and Objenesis go away.
- No coverage tool is configured. Adding JaCoCo is a build change and needs
  its own task.

## Commands

Select the JDK per run; the local `.java-version` says 11 and is ignored.
Always skip the license header rewrite. Run from the repository root.

| Goal | Command |
| --- | --- |
| Compile | `JAVA_HOME="$(jenv prefix 21)" rtk mvn -q -Dlicense.skipUpdateLicense=true compile` |
| One module (with what it needs) | `JAVA_HOME="$(jenv prefix 21)" rtk mvn -q -Dlicense.skipUpdateLicense=true -pl xacml-core -am test` |
| One class | `... -pl xacml-pdp -am -Dtest=DefaultPolicyDecisionPointTest -Dsurefire.failIfNoSpecifiedTests=false test` |
| One method (globs allowed) | `... -pl xacml-core -am -Dtest='RuleTest#<method>' -Dsurefire.failIfNoSpecifiedTests=false test` |
| Conformance suites | `... -pl xacml-conformance-tests -am -Dtest='Xacml20ConformanceTest,Xacml30ConformanceTest,RSA2008InteropTest,XacmlExampleTest' -Dsurefire.failIfNoSpecifiedTests=false test` |
| Full gate | `JAVA_HOME="$(jenv prefix 21)" rtk mvn -Dlicense.skipUpdateLicense=true verify` |

Replace `21` with `28-ea` from change group 8 on. After any Maven run,
`rtk git status` must show no license-header churn. Surefire skips
`**/*TestCase.java` (the RSA 2008 performance suite).

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
- Build real value and model objects. Mock only interfaces and SPI types
  (repositories, resolvers, evaluation contexts). Value types may become
  JEP 401 value classes, which cannot be mocked.
- No network, no wall-clock sleeps, no dependence on the default locale,
  time zone, or hash iteration order. Use `@TempDir` (JUnit 5) or
  `TemporaryFolder` (JUnit 4) for files.
- Never weaken, delete, or `@Ignore` an assertion to make a test pass. A
  skip needs a written reason and an issue.
- New fixtures go in the module's own `src/test/resources`. The OASIS and
  RSA fixtures (`xacml-conformance-tests/src/test/resources/**`) and the
  XSDs are read-only.

JUnit 5 and Mockito rules (after change task 2.1): `@Test`,
`@BeforeEach`/`@BeforeAll`, `@Nested`, `@ParameterizedTest` with
`@MethodSource` or `@CsvSource` for truth tables, `assertThrows`,
`@Disabled("reason")`; `@ExtendWith(MockitoExtension.class)` with strict
stubs, `InOrder` only where order matters, `ArgumentCaptor` for captures.
Until task 2.1 lands, write new tests with the JUnit 4 API.

## Test migration parity

While tests move to JUnit 5 and Mockito, each converted module keeps at
least its baseline test-method count, and conformance keeps at least its
baseline executed-case count. A lower number fails the gate until each
missing test is accounted for.

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
