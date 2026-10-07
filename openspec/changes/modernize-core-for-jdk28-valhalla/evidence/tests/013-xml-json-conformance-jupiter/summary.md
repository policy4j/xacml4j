# Task 2.6 — XML, JSON, and conformance JUnit 5 migration

## Result

Migrated the XML, JSON, and conformance Java tests to Jupiter. Source `@Test` counts are unchanged in each module. Conformance test loop bounds and exclusion sets are unchanged: the runner diffs for the OASIS 2.0 and 3.0 classes show only JUnit imports and lifecycle annotations changed. No XML, JSON, or conformance POM was changed.

## Test counts

| Module | Source @Test count before | Source @Test count after | JDK 21 module Surefire result |
| --- | ---: | ---: | --- |
| xacml-xml | 34 | 34 | 34 run, 0 failures, 0 errors, 0 skipped |
| xacml-json | 5 | 5 | 5 run, 0 failures, 0 errors, 1 skipped |
| xacml-conformance-tests | 42 | 42 | 41 run, 0 failures, 0 errors, 0 skipped |

The one source test omitted by Surefire is `RSA2008PerformanceTestCase.testRSA2008Performance`; the existing root Surefire exclusion `**/*TestCase.java` remains in force. `JsonEntityMarshallingTest.testEntityMarshall` remains disabled as before.

### Per-class source counts (before → after)

| Module | Test class | @Test methods |
| --- | --- | ---: |
| xacml-xml | ExpressionBuilderTest | 1 → 1 |
| xacml-xml | TypeToXacml30Test | 3 → 3 |
| xacml-xml | Xacml20RequestContextUnmarshallerTest | 6 → 6 |
| xacml-xml | Xacml20ResponseContextMarshallerTest | 1 → 1 |
| xacml-xml | Xacml20ResponseContextUnmarshallerTest | 1 → 1 |
| xacml-xml | Xacml30RequestContentTest | 5 → 5 |
| xacml-xml | Xacml30ResponseContextUnmarshallerTest | 1 → 1 |
| xacml-xml | XacmlPolicyUnmarshallerTest | 16 → 16 |
| xacml-json | JsonEntityMarshallingTest | 2 → 2 |
| xacml-json | JsonRequestUnmarshallerTest | 1 → 1 |
| xacml-json | JsonResponseContextMarshallerTest | 1 → 1 |
| xacml-json | JsonResponseContextUnmarshallerTest | 1 → 1 |
| conformance | RSA2008InteropTest | 17 → 17 |
| conformance | RSA2008PerformanceTestCase (excluded) | 1 → 1 |
| conformance | Xacml20ConformanceAttributeResolverTest | 2 → 2 |
| conformance | Xacml20ConformanceTest | 10 → 10 |
| conformance | Xacml30ConformanceTest | 10 → 10 |
| conformance | XacmlExampleTest | 2 → 2 |

## Conformance case totals

The JDK 21 log contains 347 `Executing test=` records under `Xacml20ConformanceTest`, 347 under `Xacml30ConformanceTest`, and Surefire reports 17 RSA interoperability tests and 2 example tests, all passing. These match the latest green JDK 11 receipt in `evidence/tests/005-reactor-green-jdk11/summary.md` and its conformance testcase receipts. The OASIS test-runner Surefire methods remain 10 per version; the methods iterate the same case loops as before.

## Assertions, imports, and headers

The `XacmlPolicyUnmarshallerTest` `assertThrows` lambdas contain only the policy parsing call; there are no verifications inside throwing lambdas. There are no EasyMock sites in the XML, JSON, or conformance tests. The only Mockito verification in the owned modules is the support test documented in `012-test-support-jupiter/summary.md`; no captors, strict mocks, duplicate verifies, or unverified RECORD-phase mock calls were found.

Scoped search for non-Jupiter `org.junit` imports, EasyMock imports/usages, JUnit 4 lifecycle annotations, `@Ignore`, and `@RunWith` returned no matches in the owned source trees. Compared all LGPL header blocks in the owned Java source trees with `.base`: 29 blocks checked, zero mismatches, zero duplicate blocks. Headers removed by the recipes were restored byte-for-byte from `.base` after the package declaration.

`TypeToXacml30Test` needed one compile-only disambiguation for a Jupiter `assertEquals` overload: both operands are explicitly passed as `Object`, preserving the original equality assertion. The XML gate passed after this fix.

## Verification

JDK 21 module runs used `-Dmaven.test.failure.ignore=true` on `-am` reactors so the unconverted upstream xacml-core failures do not prevent downstream modules from running. The three owned modules all reported zero failures and zero errors. Raw logs: `xml-jdk21-test.log`, `json-jdk21-test.log`, and `conformance-jdk21-test.log`. The conformance case lines and Surefire result lines are in the latter log.
