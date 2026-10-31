# Combined reactor and conformance gate

Proves that the group 42 candidates pass together: D47 correction, P-CPU2, D52 correction, cycle BA
with the D51 fix, D55 with the RV209 alias bound, and tasks 5.7 and 5.8 of change
modernize-core-for-jdk28-valhalla.

Run 2026-10-31 on Oracle JDK 25.0.4 and Maven 3.10.0, `-o -Dlicense.skipUpdateLicense=true clean
test`, no failure-ignore flag, first in a clean tree at `759d90a6` with the 34 reviewed files, then
on the main checkout with the same bytes. Both: BUILD SUCCESS.

| Module | Tests | Failures | Errors | Skipped |
| --- | ---: | ---: | ---: | ---: |
| xacml-core | 7,553 | 0 | 0 | 2 |
| xacml-xml | 254 | 0 | 0 | 0 |
| xacml-json | 24 | 0 | 0 | 1 |
| xacml-pdp | 863 | 0 | 0 | 3 |
| xacml-test | 99 | 0 | 0 | 0 |
| xacml-conformance-tests | 1,818 | 0 | 0 | 30 |
| xacml-jmh | 225 | 0 | 0 | 0 |
| Total | 10,836 | 0 | 0 | 36 |

## Tests that pin each candidate

Under `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/`:

- D47: `RulesInPolicyContextPdpTest.java#runtimeValuesEvaluatedByPredicatesSeeTheRuleContext`,
  `#attributeDesignatorFunctionValuesSeeTheRuleContext`,
  `#targetPrecheckAndFallbackKeepTheirResponses`, `#matchedTargetKeepsTheFailingRulesStatus`
- P-CPU2: `NestedResponseStagingPdpTest.java#customDefinitionAfterSharedVariableDagKeepsResponsesWithItsDecision`,
  `#customAlgorithmsKeepAllChildrenWhenTargetIndexIsEnabled`

Under `xacml-core/src/test/java/org/xacml4j/v30/`:

- D52: `policy/function/HigherOrderMetadataCompatibilityTest.java#metadataChangedDuringEvaluationIsStillASyntaxError`,
  `#canonicalTypesAndAnyValueParametersKeepTheirPreparedCall`
- D51: `BagStorageCompatibilityTest.java#defaultCardinalityStillUsesStorage`
- RV209: `AttributeIndexTest.java#designatorAliasesStayBoundedAcrossCategories`

Conformance, under `xacml-conformance-tests/src/test/java/org/xacml4j/v20/`:
`Xacml30ConformanceTest.java`, `Xacml20ConformanceTest.java`, `RSA2008InteropTest.java`,
`ObservedConformanceTest.java`.

## Limits

This proves the combination passes the tests. It makes no performance claim; D49, D50, D51, D52, and
D55 have no controlled pair yet.
