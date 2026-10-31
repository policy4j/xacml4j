# Task 4.4 registry tests

## Changes and coverage

Added tests only in `xacml-core/src/test/java`:

- `TypeRegistryTest`: 42 parameterized baseline identifier/alias cases resolve to the canonical type; 16 representative scalar lexical values parse, serialize with their value's XACML lexical serializer, and parse equal; XPath and JSONPath values round-trip through their path lexical form. The `entity` type is covered by identifier resolution; it has no scalar lexical sample.
- `DecisionCombiningAlgorithmRegistryTest`: all 23 listed XACML rule and policy combining algorithm URNs resolve and return the requested URN.
- `FunctionProviderServiceLoaderTest`: `ServiceLoader` discovers `XacmlDefaultFunctions` as the default `FunctionProvider`.

## Disabled cases

| Test | Reason |
| --- | --- |
| `TypeRegistryTest.duplicateTypeIdentifierFailsRegistration` | `BUG-TBD-duplicate-ids`: current `TypeFactory.BaseTypeFactory` keeps the first duplicate ID. The test asserts the required `IllegalArgumentException`. |
| `TypeRegistryTest.duplicateFunctionIdentifierFailsRegistration` | `BUG-TBD-duplicate-ids`: aggregating duplicate default function providers currently keeps the first duplicate ID. The test asserts the required `IllegalArgumentException`. |
| `DecisionCombiningAlgorithmRegistryTest.resolvesOnPermitApplySecondProfileIdentifier` | Task 4.6: this CS01 profile algorithm's profile semantics are not implemented. |

## ServiceLoader scope gap

The requested three-provider ServiceLoader coverage cannot be completed under the `-pl xacml-core` and test-files-only constraints. The core module's generated service file is only for `FunctionProvider`; the default combining algorithm provider is instantiated directly by `withDefaultAlgorithms()` and has no `@AutoService` registration. The `MarshallingProvider` implementations (`Xacm20MarshallingProvider` and `Xacm30MarshallingProvider`) live in `xacml-xml`, which depends on core, so core cannot load those provider classes without a downstream-module test or a dependency-cycle change. No tests were added that assert the absent service providers as expected behavior. Evidence: source search for `@AutoService`/provider implementations and `xacml-core/target/classes/META-INF/services`.

## Verification

JDK 21 command: `JAVA_HOME="$(jenv prefix 21)" rtk mvn -Dlicense.skipUpdateLicense=true -pl xacml-core test -l openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/034-registry-tests/core-registry-tests.log`

Result: 4,410 tests, 0 failures, 0 errors, 5 skips; `BUILD SUCCESS`. New test classes account for 87 tests: 84 passed and 3 intentionally disabled. Two additional existing disabled tests are `ContentTest.testXmlBase64` and `Xacml20XPathTo30TransformerTest.testXPathTransformationWithFunctions`.

The scalar round-trip deliberately calls each value type's lexical serializer. `TypeToString` has existing debug-string output for integer and X500 values in this lane, so it is not used as the serializer for this registry test. Those formatter defects are outside this tests-only task.

## Conductor note (2026-10-02)

After review rv045 the conductor removed an unused Truth8 import and replaced the
BUG-TBD-duplicate-ids placeholder with policy4j-the.4.26 in TypeRegistryTest, then
reran the three registry classes on JDK 21: 87 tests, 0 failures, 3 skipped. The
raw Surefire summary above predates that edit. The ServiceLoader gap for the
combining provider is bug policy4j-the.4.27.
