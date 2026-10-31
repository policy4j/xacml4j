# Tasks 4.1 and 4.2 safety-net tests

## Result

JDK 21 reactor gate passed after the rv049/rv050 follow-up:
`-pl xacml-core,xacml-pdp -am test`.
The two requested modules had **4,565 tests, 0 failures, 0 errors, 16 skips**.
Maven 3.9.16 ran on GraalVM Java 21.0.6.

| Module | Tests | Failures | Errors | Skips |
| --- | ---: | ---: | ---: | ---: |
| xacml-core | 4,467 | 0 | 0 | 8 |
| xacml-pdp | 98 | 0 | 0 | 8 |

## Added test coverage

| Test class | Surefire count | Coverage |
| --- | --- | --- |
| `xacml-core/.../ConditionSafetyNetTest` | 4, 0 failures, 0 errors, 1 disabled | rejects integer and string declared Condition expressions; scalar boolean Condition acceptance; policy4j-the.4.32 selector status expectation |
| `xacml-core/.../AttributeDesignatorFunctionTest` | 6, 0 failures, 0 errors, 2 disabled | optional missing attributes return empty bags for category and Entity forms; required missing attributes carry missing-attribute in direct function calls; absent-category 4.33 expectations |
| `xacml-pdp/.../AttributeResolutionSafetyNetTest` | 21, 0 failures, 0 errors, 5 disabled | matrix now verifies PIP calls; PIP-supplied designator and selector values; selector resolver failure; issuer null/mismatch statuses; function behavior; wrong category/data type; Permit and Deny condition outcomes |

The 4.1 matrix asserts both decision and status for all six current-behavior rows. It verifies the PIP's designator overload on every missing/resolver-failure row, and verifies no PIP call for request-present rows. A separate test proves the PIP can supply an otherwise missing value and cause Permit. Selector success and resolver-failure cases stub and verify the selector overload. Today a swallowed resolver exception yields NotApplicable/status ok when MustBePresent=false and Indeterminate/missing-attribute when true. The two fail-closed processing-error expectations are separate disabled tests, one per MustBePresent setting.

The category-URI and Entity forms of `attribute-designator` are evaluated with real request/model values. Optional missing attributes produce NotApplicable/ok through the PDP; the core function test also asserts both returned bags have size zero. Direct required-attribute function calls carry missing-attribute. The PDP-level required-category/entity case is retained as disabled under policy4j-the.4.33: the current Apply/PDP path converts the function's missing-attribute exception to processing-error. Mockito is used only for the PIP resolver. Rules, Conditions, values, attributes, entities, policies, requests and the PDP are real objects.

The 4.2 cardinality cases use a boolean-typed `boolean-one-and-only` Apply. Zero and two returned values each produce Indeterminate_P with processing-error at the PDP result. The Condition constructor tests pin the existing boolean declared-type requirement; bag-typed Conditions are not tested because the design adjudication rejected that requirement.

The 4.32 test uses a real `RootEvaluationContext` and a small `EvaluationContextHandler` fixture that returns null for selector resolution. That is needed to reproduce the selector's null-result edge case without mocking EvaluationContext; it is disabled because current code marks missing-attribute despite MustBePresent=false.

## Disabled tests added for this task

| Test | Reason |
| --- | --- |
| `ConditionSafetyNetTest.selectorAbsentResultMustNotSetMissingAttributeStatus` | policy4j-the.4.32: MustBePresent=false should return an empty bag without setting missing-attribute status |
| `AttributeResolutionSafetyNetTest.attributeDesignatorFunctionWithAbsentCategoryYieldsEmptyBag` | policy4j-the.4.33: absent category URI with MustBePresent=false should yield an empty bag |
| `AttributeResolutionSafetyNetTest.attributeDesignatorFunctionWithAbsentCategoryAndMustBePresentYieldsMissingAttribute` | policy4j-the.4.33: absent category URI with MustBePresent=true should yield missing-attribute |
| `AttributeResolutionSafetyNetTest.attributeDesignatorFunctionRequiredMissingAttributeIsIndeterminateForCategoryAndEntity` | policy4j-the.4.33: PDP evaluation should preserve missing-attribute from the category and Entity function forms |
| `AttributeDesignatorFunctionTest.testDesignatorFunctionWithAbsentCategoryReturnsEmptyBag` | policy4j-the.4.33: absent category URI with MustBePresent=false should return an empty bag |
| `AttributeDesignatorFunctionTest.testDesignatorFunctionWithAbsentCategoryFailsWhenRequired` | policy4j-the.4.33: absent category URI with MustBePresent=true should fail with missing-attribute |
| `AttributeResolutionSafetyNetTest.resolverFailureMustBeIndeterminateWithProcessingErrorWhenNotMustBePresent` | policy4j-the.4.34: resolver retrieval failure must fail closed as processing-error with MustBePresent=false |
| `AttributeResolutionSafetyNetTest.resolverFailureMustBeIndeterminateWithProcessingErrorWhenMustBePresent` | policy4j-the.4.34: resolver retrieval failure must fail closed as processing-error with MustBePresent=true |

There are eight total skips per module; the other skips are pre-existing, listed in `surefire-summary.txt`. The 4.33 direct absent-category tests were run enabled before being disabled: they failed as expected because absent category currently throws processing-error / `Entity is null` instead of returning an empty bag or missing-attribute. The retained baseline Maven output is `run-jdk21.log`; the follow-up red and green outputs are `run-rv050-red.log` and `run-rv050-final.log`. No production code was changed.
