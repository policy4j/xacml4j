# JDK 11 and JDK 21 test baseline

## Stale build correction

The first JDK 21 run was incremental. As the conductor identified, Maven
reported existing class files as up to date after the source/target-to-release
change, so that run did not establish that every module compiled for release
21. Its logs and counts have been replaced by clean-build results. The JDK 11
before-change baseline below was also an ordinary `verify`, not `clean verify`;
it was not a clean build and is retained as requested. The current POMs cannot
recreate that before-change clean state.

## JDK 11 before the release change

OpenJDK 11.0.32.1 ran the full reactor `verify` with the original compiler
settings. The reactor reported 936 tests, no failures or errors, and 9 skips.
Because the command did not include `clean`, these are the observed test counts
from that incremental run, not a clean-build compilation proof.

| Module | Tests | Failures | Errors | Skips |
| --- | ---: | ---: | ---: | ---: |
| xacml-core | 762 | 0 | 0 | 4 |
| xacml-xml-jaxb | 0 | 0 | 0 | 0 |
| xacml-xml | 34 | 0 | 0 | 0 |
| xacml-json | 5 | 0 | 0 | 1 |
| xacml-pdp | 74 | 0 | 0 | 4 |
| xacml-test | 20 | 0 | 0 | 0 |
| xacml-conformance-tests | 41 | 0 | 0 | 0 |
| **Total** | **936** | **0** | **0** | **9** |

## Clean post-change builds

The root and `xacml-core` compiler configurations use `<release>21</release>`;
source and target settings were removed. Neither configuration enables preview
features. A JDK 11 `clean verify` stopped during `xacml-core` compilation with
`release version 21 not supported`, as expected. The full raw log is
`jdk11-after-release21.log`.

Oracle GraalVM OpenJDK 21.0.6 ran `clean verify` with
`maven.test.failure.ignore=true`. Maven compiled sources using `javac [debug
release 21]` after cleaning the reactor. The reactor completed successfully
with these Surefire counts:

| Module | Tests | Failures | Errors | Skips |
| --- | ---: | ---: | ---: | ---: |
| xacml-core | 762 | 0 | 96 | 4 |
| xacml-xml-jaxb | 0 | 0 | 0 | 0 |
| xacml-xml | 34 | 0 | 0 | 0 |
| xacml-json | 5 | 0 | 0 | 1 |
| xacml-pdp | 74 | 0 | 0 | 4 |
| xacml-test | 20 | 0 | 0 | 0 |
| xacml-conformance-tests | 41 | 0 | 0 | 0 |
| **Total** | **936** | **0** | **96** | **9** |

### Bytecode version check

After the clean JDK 21 build, the first non-metadata `.class` file in each
available output directory was read as a class-file header. All sampled files
have magic `CAFEBABE` and major version 65 (Java 21).

| Module | Main class sample | Major | Test class sample | Major |
| --- | --- | ---: | --- | ---: |
| xacml-core | `target/classes/org/xacml4j/util/Base64.class` | 65 | `target/test-classes/org/xacml4j/util/DOMUtilTest.class` | 65 |
| xacml-xml-jaxb | `target/classes/org/oasis/xacml/v20/jaxb/context/ActionType.class` | 65 | No test classes | — |
| xacml-xml | `target/classes/org/xacml4j/v30/xml/BaseJAXBMarshaller.class` | 65 | `target/test-classes/org/xacml4j/v30/xml/ExpressionBuilderTest.class` | 65 |
| xacml-json | `target/classes/org/xacml4j/v30/marshal/json/AttributeAssignmentDeserializer.class` | 65 | `target/test-classes/org/xacml4j/v30/marshal/json/JsonEntityMarshallingTest.class` | 65 |
| xacml-pdp | `target/classes/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.class` | 65 | `target/test-classes/org/xacml4j/util/DefaultFunctionFunctionInvocationFactoryTest.class` | 65 |
| xacml-test | `target/classes/org/xacml4j/v20/Xacml20TestUtility$1.class` | 65 | `target/test-classes/org/xacml4j/v20/Xacml20SamplePolicyTest.class` | 65 |
| xacml-conformance-tests | No main classes | — | `target/test-classes/org/xacml4j/v20/RSA2008InteropTest.class` | 65 |

The verification command and class-file header checks are recorded in
`command.txt`. `java-versions.txt` records the exact runtimes.

### JDK 21 error classification

The clean JDK 21 run still has 96 errors, exactly matching the planning probe.
The count difference from 96 is zero. The complete class-and-method inventory
below is regenerated from the clean JDK 21 Surefire XML reports.
All 96 are EasyMock class-proxy creation errors: each has
`IllegalArgumentException: Could not create type` and its trace includes
`org.easymock.internal.ClassProxyFactory.createProxy` and
`MocksControl.createMock`. A follow-up scan found 96/96 such stacks and zero
module-access errors. There are no test failures and no other error category.
The complete list of each test class and failing method is in
`surefire-summary.txt`.

| Test class | Errors |
| --- | ---: |
| ConditionTest | 6 |
| MatchAllOfTest | 6 |
| MatchAnyOfTest | 5 |
| MatchTest | 2 |
| ObligationExpressionTest | 1 |
| PolicyIDReferenceTest | 12 |
| PolicySetIDReferenceTest | 7 |
| PolicySetTest | 14 |
| PolicyTest | 14 |
| RuleTest | 23 |
| TargetTest | 6 |
| **Total** | **96** |

The conformance module completed: 347 OASIS 2.0 cases, 347 OASIS 3.0 cases,
17 RSA 2008 cases, and 2 examples. There are 694 `Executing test=` entries in
the clean JDK 21 log across the two OASIS suites; RSA and examples are counted
by their Surefire test classes.

Group 2 has a verified release 21 compile and class-file output. Its migration
baseline is 96 EasyMock class-mock errors in core; all other modules, including
conformance, passed on JDK 21.
