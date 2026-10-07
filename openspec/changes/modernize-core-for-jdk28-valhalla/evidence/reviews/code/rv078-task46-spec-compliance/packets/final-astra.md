Change: modernize-core-for-jdk28-valhalla. Task 4.6, Beads policy4j-the.4.6.
READ-ONLY final bounded follow-up review. Files outside listed scope are context only. No edits, builds, tests, external research or Git writes. Persona read-only takes precedence over report-file writing. Print complete final report in terminal; conductor will persist it. Maximum 1200 words. Reuse inspected numeric context rather than re-inventorying engine.
First and last verify 18 hashes in openspec/changes/modernize-core-for-jdk28-valhalla/evidence/reviews/code/rv078-task46-spec-compliance/final-scope-hashes.json; hash evidence is exact bytes. Approved oracle: official Core OS A.3.1/A.3.10/A.3.11 (NaN unequal, zeros equal; Java equality unchanged); integer quotient is exact integer and recorded truncation convention with 7.5 ambiguity; CS01 2.1 on-permit-apply-second accepts only 2/3 children (arity error I_DP no evaluation); first P => second, first N/D/I_D => N for 2 or third for 3; first I_P/I_DP/plain I => I_DP, preserve status. Core 7.18 obligations/advice flow only through an unbroken matching Permit/Deny ancestry.
New combining author Fable5.1; conductor applied private staging after writer handoff and adjusted parent-context forwarding. Review actual sources, especially BaseCompositeDecisionRule.StagedResponses: nesting, context forwarding, evaluation status, extended-indeterminate, references, expression failure, discarded children; reachable findings with test/fix required. RootEvaluationContext, DescendantEvaluationContext and public interface are unchanged this task; inspect as supporting context only if necessary.
Focused GREEN r1: 721 tests,0 failures/errors,7 skips includes policy/ref compatibility and table/reproducer. New tests add discarded Deny sibling and nested parent's own responses. Full JDK21 clean verify is running; inspect result only if complete, do not rerun.
Prior numerical review followups applied: row43 modzero enabled (remaining6skips in F43); type guards now tested with validation OFF both full/EMPTY wrong bag; NaN PDP tests compare full responses; suite/disposition/ID independently negative-controlled; expected fixture path bound, wrong-Permit-path rejected; XPath spec requires Content present for failure and explicit missing-content precedence. Outside 4.6: other16 zeroarg type-bag functions, integeroverflow (5.4), naryunion and real malformedXPath/performance (4.7). No need add disabled overflow row; existing tracking covers it.
Read decision-record.md and reports/receipts as needed, no wholeengine inventory. Author-owned target scope:
xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/ArithmeticFunctions.java
xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/EqualityPredicates.java
xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/BagFunctions.java
xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/SetFunctions.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/ArtimeticFunctionsTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/FunctionSafetyNet43Test.java
xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/DoubleBagSetEqualityTest.java
xacml-conformance-tests/src/test/java/org/xacml4j/v20/ConformanceCaseRunner.java
xacml-conformance-tests/src/test/java/org/xacml4j/v20/NaNFixtureAdjudicationTest.java
xacml-conformance-tests/src/test/manifest/conformance-manifest.csv
openspec/changes/modernize-core-for-jdk28-valhalla/specs/function-semantics/spec.md
xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java
xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DefaultXacml30DecisionCombiningAlgorithms.java
xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/OnPermitApplySecondPolicyCombiningAlgorithm.java
xacml-core/src/test/java/org/xacml4j/v30/policy/combine/DecisionCombiningAlgorithmRegistryTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/OnPermitApplySecondDecisionTableTest.java
xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/OnPermitApplySecondPolicyTest.java
openspec/changes/modernize-core-for-jdk28-valhalla/specs/combining-algorithm-registry/spec.md
Output result, reachable numbered findings/severity/artifact:line, prior dispositions, verification and remaining scope. State model/config pin versus UNVERIFIED serving variant. No terminal marker until review really complete.

You are primary cross-provider combining code review. Cover all combining and followup changes. Final marker DONE_FINAL_ASTRA_20261004.

