# JUnit 4 and EasyMock inventory

## Scope and method

Source snapshot: branch `integration/modernize-core-jdk28-valhalla`, `HEAD` `befdd116bd11385ac8d007732fa8e4d347af3143`, with the live working-tree edits visible during this inventory. No build or tests were run. Source annotation counts are lexical `@Test` matches in Java sources, cross-checked structurally with ast-grep in the XML and conformance modules. Surefire's existing `**/*TestCase.java` exclusion means the conformance count includes one performance test class/method that Surefire excludes. The tests under `xacml-test/src/test/java` and the main sources are reported separately.

The working tree was changing during this read-only inventory. Three untracked tests were visible at the final recount: `xacml-test/src/test/java/org/xacml4j/v30/ExpectedAttributeResolverBuilderTest.java`, `xacml-test/src/test/java/org/xacml4j/v20/Xacml30TestUtilityTest.java`, and `xacml-conformance-tests/src/test/java/org/xacml4j/v20/Xacml20ConformanceAttributeResolverTest.java`. They are included in the final counts. Existing PDP and XML tests and the conformance attribute resolver were also modified in the shared tree. Re-run the searches after concurrent work settles before using these counts as a frozen migration baseline.

## Per-module counts

Counts below are source counts, not executed test counts. Lifecycle is the total of `@Before`, `@After`, `@BeforeClass`, and `@AfterClass`; all observed lifecycle annotations are `@Before` or `@BeforeClass`.

| Module/source set | `@Test` files | `@Test` methods | Lifecycle | `@Ignore` | `@Test(expected=...)` | `@Rule` / `@ClassRule` | Static `org.junit.Assert` files | EasyMock files | Google Truth files | Hamcrest files |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| `xacml-core` tests | 95 | 492 | 54 | 4 | 48 | 0 / 0 | 89 | 37 | 11 | 5 |
| `xacml-pdp` tests | 19 | 72 | 18 | 5 | 7 | 1 / 0 | 16 | 15 | 1 | 0 |
| `xacml-xml` tests | 7 | 29 | 4 | 0 | 0 | 0 / 0 | 6 | 0 | 0 | 1 |
| `xacml-json` tests | 4 | 5 | 3 | 1 | 0 | 0 / 0 | 4 | 0 | 0 | 3 |
| `xacml-test` main | 0 | 0 | 1 | 0 | 0 | 0 / 0 | 4 | 0 | 0 | 0 |
| `xacml-test` tests | 5 | 20 | 2 | 0 | 9 | 0 / 0 | 2 | 1 | 0 | 0 |
| `xacml-conformance-tests` tests | 6 | 42 | 6 | 0 | 0 | 0 / 0 | 4 | 0 | 0 | 0 |
| **Test-source total** | **136** | **660** | **87** | **10** | **64** | **1 / 0** | **121** | **53** | **12** | **9** |

JUnit 4 special cases: no `@After`, `@AfterClass`, `@ClassRule`, `@RunWith`, or `Parameterized` usage was found. `ExpectedException` occurs in `xacml-pdp` and `xacml-xml`; the PDP field is `@Rule`, while the XML field is not annotated with `@Rule`. No `TemporaryFolder` use was found. Static `org.junit.Assert` counts are files with static imports, not assertion-call counts.

`xacml-test/src/main/java` has four production-source files importing JUnit 4 (`Xacml20TestUtility`, `Xacml30TestUtility`, `XacmlPolicyTestSupport`, `AttributeResolverTestSupport`). Its current `xacml-test/pom.xml` declares JUnit 4.13.2 with `compile` scope. These support APIs and the compile-scope dependency must move with the test-framework migration.

## EasyMock operation counts

These are lexical call/reference counts. `expect(...)` excludes the two JUnit `ExpectedException.expect(...)` calls in XML. Across EasyMock-bearing modules, chained outcomes are `andThrow` 27, `andAnswer` 5, and `andStubReturn` 0.

| Module/source set | `createMock` | `createStrictMock` | `createNiceMock` | `createStrictControl` | `IMocksControl` references | `Capture<>` | EasyMock `expect(...)` | `replay(...)` | `verify(...)` |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| `xacml-core` tests | 157 | 77 | 0 | 13 | 52 | 67 | 475 | 249 | 249 |
| `xacml-pdp` tests | 33 | 4 | 0 | 3 | 24 | 69 | 113 | 60 | 60 |
| `xacml-test` tests | 0 | 1 | 0 | 0 | 0 | 0 | 1 | 1 | 1 |
| `xacml-xml`, `xacml-json`, conformance tests | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| **Total** | **190** | **82** | **0** | **16** | **76** | **136** | **589** | **310** | **310** |

Strict mocks/controls enforce interaction order. 33 test files use `createStrictMock` or `createStrictControl`; see the exact file list below. These need explicit order assertions when converted, as D4 notes. The exact invocation types and class/interface review follow.

## D4 model/value mock inventory

The 66 `createMock`/`createStrictMock`/`createNiceMock` calls against the D4 target types break down as follows. The ten concrete model classes account for 62 mocks; `Value` accounts for four mocks and is an interface in this branch. `AttributeDesignator` is a class in this branch (the D4 record replacement refers to the donor design).

| Mocked type | Calls | Source classification |
| --- | ---: | --- |
| `AttributeAssignmentExpression` | 1 | class |
| `AttributeDesignator` | 1 | class |
| `Condition` | 3 | class |
| `Match` | 17 | class |
| `MatchAllOf` | 15 | class |
| `MatchAnyOf` | 16 | class |
| `Policy` | 2 | class |
| `PolicySet` | 2 | class |
| `Rule` | 2 | class |
| `Target` | 3 | class |
| `Value` | 4 | interface |

Exact test sites are in `PolicyIDReferenceTest`, `ConditionTest`, `RuleTest`, `PolicySetIDReferenceTest`, `TargetTest`, `ObligationExpressionTest`, `CombinerParameterTest`, `MatchAnyOfTest`, `PolicyTest`, `MatchTest`, and `MatchAllOfTest` under `xacml-core/src/test/java`. The `Value` mocks are in `ConditionTest` and `CombinerParameterTest`. No other mocked `.class` target found in the test sources is a concrete class; the remaining mocked targets are interfaces.

## Proposed disjoint migration lanes

| Lane | Exclusive source file list | Current load |
| --- | --- | --- |
| Core | `xacml-core/src/test/java/**/*.java` | 95 annotated files; largest lane; strict mocks and D4 model/value conversions |
| PDP | `xacml-pdp/src/test/java/**/*.java` | 19 annotated files; 15 EasyMock files; strict-order cases |
| XML | `xacml-xml/src/test/java/**/*.java` | 7 annotated files; JUnit ExpectedException and Hamcrest |
| JSON | `xacml-json/src/test/java/**/*.java` | 4 annotated files; Hamcrest |
| Test support | `xacml-test/src/main/java/**/*.java` and `xacml-test/src/test/java/**/*.java` | JUnit is a main-code compile dependency; 5 test classes, 20 methods, one EasyMock file |
| Conformance | `xacml-conformance-tests/src/test/java/**/*.java` | 6 annotated files; one test method is in the excluded performance `*TestCase` |

These module roots are disjoint. The root POM/JUnit dependency transition should be owned by the conductor as a shared setup task; each lane can own only its module sources and module POM. The full `@Test` file inventory and framework-specific file inventories follow so the split can be narrowed further if desired.

## `@Test` source file list

Repository-relative paths grouped by module prefix. The test-source counts and migration lane boundaries are in the tables above.

```text
xacml-core/src/test/java/org/xacml4j/util/DOMUtilTest.java
xacml-core/src/test/java/org/xacml4j/util/ISO8601DurationTest.java
xacml-core/src/test/java/org/xacml4j/util/JSONUtilTest.java
xacml-core/src/test/java/org/xacml4j/v30/AttributeIndexTest.java
xacml-core/src/test/java/org/xacml4j/v30/AttributeTest.java
xacml-core/src/test/java/org/xacml4j/v30/CategoryIdTest.java
xacml-core/src/test/java/org/xacml4j/v30/ContentTest.java
xacml-core/src/test/java/org/xacml4j/v30/EntityTest.java
xacml-core/src/test/java/org/xacml4j/v30/ObligationTest.java
xacml-core/src/test/java/org/xacml4j/v30/WellKnownAttributeIdsTest.java
xacml-core/src/test/java/org/xacml4j/v30/content/ContentTest.java
xacml-core/src/test/java/org/xacml4j/v30/content/JsonContentTest.java
xacml-core/src/test/java/org/xacml4j/v30/content/XmlContentTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/AdviceExpressionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/AdviceTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/ApplyTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/AttributeAssignmentTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/AttributeDesignatorTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/AttributeSelectorTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/AttributesTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/BagOfValuesTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/CombinerParameterTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/ConditionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/DecisionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/MatchAllOfTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/MatchAnyOfTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/MatchTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/ObligationExpressionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/PolicyDefaultsTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/PolicyIDReferenceTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/PolicySetIDReferenceTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/PolicySetTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/PolicyTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/RequestTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/ResultTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/RootEvaluationContextTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/RuleTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/StatusCodeIdTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/TargetTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/VariableDefinitionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/VariableReferenceTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/VersionMatchTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/VersionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/XPathTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/combine/AnnotatedDecisionCombiningAlgorithmProviderFactoryTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/DenyOverridesTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/DenyUnlessPermitTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/FirstApplicableTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/OnPermitApplySecondPolicyTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/OnlyOneApplicablePolicyCombiningAlgorithmTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/PermitOverridesTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/PermitUnlessDenyTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/FunctionSpecBuilderTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/ParamSingleTypeSpecTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/ParamTypeSequenceTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/AnnotationBasedFunctionFactoryTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/ArtimeticFunctionsTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/AttributeDesignatorFunctionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/BagFunctionsTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/DateTimeValueArithmeticFunctionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionFunctionInvocationTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionSpecTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/HigherOrderFunctionsTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/JavaMethodToFunctionSpecTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/LogicalFunctionsTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/NonNumericComparisionFunctionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/NumericComparisonFunctionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/RegularExpressionFunctionsTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/SetFunctionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/SpecialMatchFunctionsTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/XPathFunctionsTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/AnyURITest.java
xacml-core/src/test/java/org/xacml4j/v30/types/Base64BinaryTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/BinaryTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/BooleanValTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/DNSNameTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/DoubleValTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/EUI48Test.java
xacml-core/src/test/java/org/xacml4j/v30/types/HexTypeTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/IPAddressTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/ISO8601DateTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/ISO8601DateTimeTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/ISO8601DayTimeDurationTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/ISO8601TimeTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/ISO8601YearMonthDurationTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/IntegerTypeTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/PathTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/PortRangeTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/RFC822NameTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/StringValTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/TypeCapabilityTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/TypeToStringCapabilityTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/TypeToStringTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/Xacml20XPathTo30TransformerTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/XacmlTypesTest.java
xacml-pdp/src/test/java/org/xacml4j/util/DefaultFunctionFunctionInvocationFactoryTest.java
xacml-pdp/src/test/java/org/xacml4j/util/FunctionInvocationBenchmark.java
xacml-pdp/src/test/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandlerTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPointTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesHandlerTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaRepeatingAttributesHandlerTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaRequestReferencesHandlerTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXacmlXPathHandlerTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/pdp/RequestContextHandlerChainTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/AnnotatedResolverFactoryTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/AttributeResolverDescriptorTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/AttributeSetTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/BaseAttributeResolverTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/ContentResolverDescriptorTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/DefaultPolicyInformationPointTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/DefaultResolverRegistryTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/ResolverRegistryBuilderTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/repository/DefaultPolicyReferenceResolverTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/repository/InMemoryPolicyRepositoryTest.java
xacml-xml/src/test/java/org/xacml4j/v30/xml/ExpressionBuilderTest.java
xacml-xml/src/test/java/org/xacml4j/v30/xml/TypeToXacml30Test.java
xacml-xml/src/test/java/org/xacml4j/v30/xml/Xacml20RequestContextUnmarshallerTest.java
xacml-xml/src/test/java/org/xacml4j/v30/xml/Xacml20ResponseContextMarshallerTest.java
xacml-xml/src/test/java/org/xacml4j/v30/xml/Xacml20ResponseContextUnmarshallerTest.java
xacml-xml/src/test/java/org/xacml4j/v30/xml/Xacml30ResponseContextUnmarshallerTest.java
xacml-xml/src/test/java/org/xacml4j/v30/xml/XacmlPolicyUnmarshallerTest.java
xacml-json/src/test/java/org/xacml4j/v30/marshal/json/JsonEntityMarshallingTest.java
xacml-json/src/test/java/org/xacml4j/v30/marshal/json/JsonRequestUnmarshallerTest.java
xacml-json/src/test/java/org/xacml4j/v30/marshal/json/JsonResponseContextMarshallerTest.java
xacml-json/src/test/java/org/xacml4j/v30/marshal/json/JsonResponseContextUnmarshallerTest.java
xacml-test/src/test/java/org/xacml4j/v20/Xacml20SamplePolicyTest.java
xacml-test/src/test/java/org/xacml4j/v20/Xacml30TestUtilityTest.java
xacml-test/src/test/java/org/xacml4j/v30/AttributeResolverTestSupportTest.java
xacml-test/src/test/java/org/xacml4j/v30/ExpectedAttributeResolverBuilderTest.java
xacml-test/src/test/java/org/xacml4j/v30/Xacml30SamplePolicyTest.java
xacml-conformance-tests/src/test/java/org/xacml4j/v20/RSA2008InteropTest.java
xacml-conformance-tests/src/test/java/org/xacml4j/v20/RSA2008PerformanceTestCase.java
xacml-conformance-tests/src/test/java/org/xacml4j/v20/Xacml20ConformanceAttributeResolverTest.java
xacml-conformance-tests/src/test/java/org/xacml4j/v20/Xacml20ConformanceTest.java
xacml-conformance-tests/src/test/java/org/xacml4j/v20/Xacml30ConformanceTest.java
xacml-conformance-tests/src/test/java/org/xacml4j/v20/XacmlExampleTest.java
```

## Framework-specific file inventories

### EasyMock files: xacml-core

```text
xacml-core/src/test/java/org/xacml4j/v30/policy/AdviceExpressionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/ApplyTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/AttributeDesignatorTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/AttributeSelectorTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/BagOfValuesTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/CombinerParameterTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/ConditionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/MatchAllOfTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/MatchAnyOfTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/MatchTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/ObligationExpressionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/PolicyIDReferenceTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/PolicySetIDReferenceTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/PolicySetTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/PolicyTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/RootEvaluationContextTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/RuleTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/TargetTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/VariableDefinitionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/VariableReferenceTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/DenyOverridesTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/DenyUnlessPermitTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/FirstApplicableTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/OnPermitApplySecondPolicyTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/OnlyOneApplicablePolicyCombiningAlgorithmTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/PermitOverridesTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/PermitUnlessDenyTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/FunctionSpecBuilderTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/AnnotationBasedFunctionFactoryTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/AttributeDesignatorFunctionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionFunctionInvocationTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionSpecTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/HigherOrderFunctionsTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/LogicalFunctionsTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/NonNumericComparisionFunctionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/RegularExpressionFunctionsTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/XPathFunctionsTest.java
```

### EasyMock files: xacml-pdp

```text
xacml-pdp/src/test/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandlerTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPointTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesHandlerTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaRepeatingAttributesHandlerTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaRequestReferencesHandlerTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXacmlXPathHandlerTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/pdp/RequestContextHandlerChainTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/AnnotatedResolverFactoryTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/AttributeResolverDescriptorTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/BaseAttributeResolverTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/DefaultPolicyInformationPointTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/DefaultResolverRegistryTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/ResolverRegistryBuilderTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/repository/DefaultPolicyReferenceResolverTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/repository/InMemoryPolicyRepositoryTest.java
```

### EasyMock files: xacml-test tests

```text
xacml-test/src/test/java/org/xacml4j/v30/ExpectedAttributeResolverBuilderTest.java
```

### Strict-order files: xacml-core

```text
xacml-core/src/test/java/org/xacml4j/v30/policy/AdviceExpressionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/ApplyTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/AttributeDesignatorTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/AttributeSelectorTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/BagOfValuesTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/CombinerParameterTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/ConditionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/MatchAllOfTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/MatchAnyOfTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/PolicyIDReferenceTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/PolicySetIDReferenceTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/PolicySetTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/PolicyTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/RuleTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/TargetTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/VariableDefinitionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/VariableReferenceTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/DenyUnlessPermitTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/FirstApplicableTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/OnlyOneApplicablePolicyCombiningAlgorithmTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/PermitOverridesTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/PermitUnlessDenyTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/AnnotationBasedFunctionFactoryTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/NonNumericComparisionFunctionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/XPathFunctionsTest.java
```

### Strict-order files: xacml-pdp

```text
xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesHandlerTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaRepeatingAttributesHandlerTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaRequestReferencesHandlerTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXacmlXPathHandlerTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/pdp/RequestContextHandlerChainTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/DefaultPolicyInformationPointTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/DefaultResolverRegistryTest.java
```

### Strict-order files: xacml-test

```text
xacml-test/src/test/java/org/xacml4j/v30/ExpectedAttributeResolverBuilderTest.java
```

### Google Truth files: core

```text
xacml-core/src/test/java/org/xacml4j/v30/AttributeIndexTest.java
xacml-core/src/test/java/org/xacml4j/v30/CategoryIdTest.java
xacml-core/src/test/java/org/xacml4j/v30/ContentTest.java
xacml-core/src/test/java/org/xacml4j/v30/EntityTest.java
xacml-core/src/test/java/org/xacml4j/v30/content/ContentTest.java
xacml-core/src/test/java/org/xacml4j/v30/content/JsonContentTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/AttributeDesignatorTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/AttributeSelectorTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/AttributeDesignatorFunctionTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/BinaryTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/TypeToStringCapabilityTest.java
```

### Google Truth files: pdp

```text
xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/AnnotatedResolverFactoryTest.java
```

### Hamcrest files: core

```text
xacml-core/src/test/java/org/xacml4j/v30/ObligationTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/AttributeDesignatorTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/RootEvaluationContextTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/VersionMatchTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/RegularExpressionFunctionsTest.java
```

### Hamcrest files: xml

```text
xacml-xml/src/test/java/org/xacml4j/v30/xml/XacmlPolicyUnmarshallerTest.java
```

### Hamcrest files: json

```text
xacml-json/src/test/java/org/xacml4j/v30/marshal/json/JsonRequestUnmarshallerTest.java
xacml-json/src/test/java/org/xacml4j/v30/marshal/json/JsonResponseContextMarshallerTest.java
xacml-json/src/test/java/org/xacml4j/v30/marshal/json/JsonResponseContextUnmarshallerTest.java
```

### JUnit Assert imports: core

```text
Warning: truncated output (original token count: 1689)
Total output lines: 89

xacml-core/src/test/java/org/xacml4j/util/DOMUtilTest.java
xacml-core/src/test/java/org/xacml4j/util/ISO8601DurationTest.java
xacml-core/src/test/java/org/xacml4j/util/JSONUtilTest.java
xacml-core/src/test/java/org/xacml4j/v30/AttributeTest.java
xacml-core/src/test/java/org/xacml4j/v30/CategoryIdTest.java
xacml-core/src/test/java/org/xacml4j/v30/EntityTest.java
xacml-core/src/test/java/org/xacml4j/v30/ObligationTest.java
xacml-core/src/test/java/org/xacml4j/v30/WellKnownAttributeIdsTest.java
xacml-core/src/test/java/org/xacml4j/v30/content/XmlContentTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/AdviceExpressionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/AdviceTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/AttributeAssignmentTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/AttributeDesignatorTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/AttributeSelectorTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/AttributesTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/BagOfValuesTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/CombinerParameterTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/ConditionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/DecisionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/MatchAllOfTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/MatchAnyOfTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/MatchTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/ObligationExpressionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/PolicyDefaultsTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/PolicyIDReferenceTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/PolicySetIDReferenceTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/PolicySetTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/PolicyTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/RequestTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/ResultTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/RootEvaluationContextTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/RuleTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/StatusCodeIdTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/TargetTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/VariableDefinitionTest.java
xacml-cor…489 tokens truncated…-core/src/test/java/org/xacml4j/v30/policy/function/impl/HigherOrderFunctionsTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/JavaMethodToFunctionSpecTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/LogicalFunctionsTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/NonNumericComparisionFunctionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/NumericComparisonFunctionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/RegularExpressionFunctionsTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/SetFunctionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/SpecialMatchFunctionsTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/XPathFunctionsTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/AnyURITest.java
xacml-core/src/test/java/org/xacml4j/v30/types/Base64BinaryTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/BinaryTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/BooleanValTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/DNSNameTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/DoubleValTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/EUI48Test.java
xacml-core/src/test/java/org/xacml4j/v30/types/HexTypeTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/IPAddressTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/ISO8601DateTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/ISO8601DateTimeTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/ISO8601DayTimeDurationTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/ISO8601TimeTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/ISO8601YearMonthDurationTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/IntegerTypeTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/PathTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/PortRangeTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/RFC822NameTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/StringValTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/TypeCapabilityTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/TypeToStringTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/Xacml20XPathTo30TransformerTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/XacmlTypesTest.java
```

### JUnit Assert imports: pdp

```text
xacml-pdp/src/test/java/org/xacml4j/util/DefaultFunctionFunctionInvocationFactoryTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandlerTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPointTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesHandlerTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaRepeatingAttributesHandlerTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaRequestReferencesHandlerTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXacmlXPathHandlerTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/AnnotatedResolverFactoryTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/AttributeResolverDescriptorTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/AttributeSetTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/ContentResolverDescriptorTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/DefaultPolicyInformationPointTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/DefaultResolverRegistryTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/ResolverRegistryBuilderTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/repository/DefaultPolicyReferenceResolverTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/repository/InMemoryPolicyRepositoryTest.java
```

### JUnit Assert imports: xml

```text
xacml-xml/src/test/java/org/xacml4j/v30/xml/ExpressionBuilderTest.java
xacml-xml/src/test/java/org/xacml4j/v30/xml/TypeToXacml30Test.java
xacml-xml/src/test/java/org/xacml4j/v30/xml/Xacml20RequestContextUnmarshallerTest.java
xacml-xml/src/test/java/org/xacml4j/v30/xml/Xacml20ResponseContextUnmarshallerTest.java
xacml-xml/src/test/java/org/xacml4j/v30/xml/Xacml30ResponseContextUnmarshallerTest.java
xacml-xml/src/test/java/org/xacml4j/v30/xml/XacmlPolicyUnmarshallerTest.java
```

### JUnit Assert imports: json

```text
xacml-json/src/test/java/org/xacml4j/v30/marshal/json/JsonEntityMarshallingTest.java
xacml-json/src/test/java/org/xacml4j/v30/marshal/json/JsonRequestUnmarshallerTest.java
xacml-json/src/test/java/org/xacml4j/v30/marshal/json/JsonResponseContextMarshallerTest.java
xacml-json/src/test/java/org/xacml4j/v30/marshal/json/JsonResponseContextUnmarshallerTest.java
```

### JUnit Assert imports: xacml-test

```text
xacml-test/src/test/java/org/xacml4j/v30/AttributeResolverTestSupportTest.java
xacml-test/src/test/java/org/xacml4j/v30/ExpectedAttributeResolverBuilderTest.java
```

### JUnit Assert imports: conformance

```text
xacml-conformance-tests/src/test/java/org/xacml4j/v20/RSA2008PerformanceTestCase.java
xacml-conformance-tests/src/test/java/org/xacml4j/v20/Xacml20ConformanceAttributeResolverTest.java
xacml-conformance-tests/src/test/java/org/xacml4j/v20/Xacml20ConformanceTest.java
xacml-conformance-tests/src/test/java/org/xacml4j/v20/Xacml30ConformanceTest.java
```

### Special JUnit APIs: core

```text
Warning: truncated output (original token count: 1259)
Total output lines: 64

xacml-core/src/test/java/org/xacml4j/util/DOMUtilTest.java
xacml-core/src/test/java/org/xacml4j/v30/AttributeTest.java
xacml-core/src/test/java/org/xacml4j/v30/EntityTest.java
xacml-core/src/test/java/org/xacml4j/v30/content/ContentTest.java
xacml-core/src/test/java/org/xacml4j/v30/content/JsonContentTest.java
xacml-core/src/test/java/org/xacml4j/v30/content/XmlContentTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/AdviceExpressionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/AdviceTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/ApplyTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/AttributeDesignatorTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/AttributeSelectorTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/AttributesTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/BagOfValuesTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/ConditionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/MatchAllOfTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/MatchAnyOfTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/MatchTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/ObligationExpressionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/PolicyIDReferenceTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/PolicySetIDReferenceTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/PolicySetTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/PolicyTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/RequestTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/ResultTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/RootEvaluationContextTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/RuleTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/TargetTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/VariableDefinitionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/VariableReferenceTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/VersionMatchTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/VersionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/XPathTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/combine/AnnotatedDecisionCombiningAlgorithmProviderFactoryTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/DenyOverridesTest.java
xacml-core/src/tes…59 tokens truncated…ndPolicyTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/OnlyOneApplicablePolicyCombiningAlgorithmTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/PermitOverridesTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/PermitUnlessDenyTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/FunctionSpecBuilderTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/ParamTypeSequenceTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/AnnotationBasedFunctionFactoryTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/AttributeDesignatorFunctionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/DateTimeValueArithmeticFunctionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionFunctionInvocationTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionSpecTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/HigherOrderFunctionsTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/JavaMethodToFunctionSpecTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/LogicalFunctionsTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/NonNumericComparisionFunctionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/RegularExpressionFunctionsTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/SetFunctionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/SpecialMatchFunctionsTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/TestFunctions.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/TestInstanceFunctions.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/XPathFunctionsTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/Base64BinaryTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/ISO8601DateTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/ISO8601DateTimeTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/ISO8601DayTimeDurationTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/ISO8601YearMonthDurationTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/RFC822NameTest.java
xacml-core/src/test/java/org/xacml4j/v30/types/Xacml20XPathTo30TransformerTest.java
```

### Special JUnit APIs: pdp

```text
xacml-pdp/src/test/java/org/xacml4j/util/DefaultFunctionFunctionInvocationFactoryTest.java
xacml-pdp/src/test/java/org/xacml4j/util/FunctionInvocationBenchmark.java
xacml-pdp/src/test/java/org/xacml4j/util/TestObject.java
xacml-pdp/src/test/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandlerTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPointTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesHandlerTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaRepeatingAttributesHandlerTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaRequestReferencesHandlerTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXacmlXPathHandlerTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/pdp/RequestContextHandlerChainTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/AnnotatedResolverFactoryTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/AttributeResolverDescriptorTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/AttributeSetTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/BaseAttributeResolverTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/DefaultPolicyInformationPointTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/DefaultResolverRegistryTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/ResolverRegistryBuilderTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/repository/DefaultPolicyReferenceResolverTest.java
xacml-pdp/src/test/java/org/xacml4j/v30/spi/repository/InMemoryPolicyRepositoryTest.java
```

### Special JUnit APIs: xml

```text
xacml-xml/src/test/java/org/xacml4j/v30/xml/Xacml20RequestContextUnmarshallerTest.java
xacml-xml/src/test/java/org/xacml4j/v30/xml/Xacml20ResponseContextMarshallerTest.java
xacml-xml/src/test/java/org/xacml4j/v30/xml/Xacml20ResponseContextUnmarshallerTest.java
xacml-xml/src/test/java/org/xacml4j/v30/xml/XacmlPolicyUnmarshallerTest.java
```

### Special JUnit APIs: json

```text
xacml-json/src/test/java/org/xacml4j/v30/marshal/json/JsonEntityMarshallingTest.java
xacml-json/src/test/java/org/xacml4j/v30/marshal/json/JsonRequestUnmarshallerTest.java
xacml-json/src/test/java/org/xacml4j/v30/marshal/json/JsonResponseContextMarshallerTest.java
```

### Special JUnit APIs: xacml-test

```text
xacml-test/src/test/java/org/xacml4j/v20/Xacml20SamplePolicyTest.java
xacml-test/src/test/java/org/xacml4j/v20/Xacml30TestUtilityTest.java
xacml-test/src/test/java/org/xacml4j/v30/Xacml30SamplePolicyTest.java
```

### Special JUnit APIs: conformance

```text
xacml-conformance-tests/src/test/java/org/xacml4j/v20/RSA2008InteropTest.java
xacml-conformance-tests/src/test/java/org/xacml4j/v20/RSA2008PerformanceTestCase.java
xacml-conformance-tests/src/test/java/org/xacml4j/v20/Xacml20ConformanceTest.java
xacml-conformance-tests/src/test/java/org/xacml4j/v20/Xacml30ConformanceTest.java
```

### D4 model/value mock files

```text
xacml-core/src/test/java/org/xacml4j/v30/policy/CombinerParameterTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/ConditionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/MatchAllOfTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/MatchAnyOfTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/MatchTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/ObligationExpressionTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/PolicyIDReferenceTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/PolicySetIDReferenceTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/PolicySetTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/PolicyTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/RuleTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/TargetTest.java
```

### JUnit 4 in xacml-test main

```text
xacml-test/src/main/java/org/xacml4j/v20/Xacml20TestUtility.java
xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java
xacml-test/src/main/java/org/xacml4j/v30/AttributeResolverTestSupport.java
xacml-test/src/main/java/org/xacml4j/v30/XacmlPolicyTestSupport.java
```
