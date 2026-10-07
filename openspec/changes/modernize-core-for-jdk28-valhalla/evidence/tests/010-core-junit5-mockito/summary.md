# Core JUnit 5 / Mockito migration — tasks 2.2 and 2.3

Result: the final JDK 21 core test run **54** is GREEN: **760 tests, 0 failures, 0 errors, 2 skipped**, exactly the rv019 follow-up gate. Two empty Vintage helper entries disappear from reporting; see [gate-count-note.md](gate-count-note.md). **No declared test or assertion was removed.** The three repaired catch oracles reject the scratch catch-deletion mutant; see [rv019 fixes](review-artifacts.md#rv019-fixes).

Toolchain: Java 21.0.6, runtime 21.0.6+8-LTS-jvmci-23.1-b55, Oracle Corporation, Java HotSpot(TM) 64-Bit Server VM. Surefire 3.0.0 uses JUnitPlatformProvider. The task-2.1 root configuration (Jupiter/Vintage 5.14.4, Mockito 5.24.0 startup agent) is unchanged.

Scope: 105 of the 106 existing core Java test sources changed, plus the new ModelFixtures.java helper. This lane's changes are under xacml-core/src/test/java and this run directory. SHA-256 audit checks all 3,451 protected paths. Root/core POMs, core main sources, JAXB/vendored inputs and prior runs 008/009 still match task start. 30 other protected paths changed in the shared tree; authorship is UNATTRIBUTED, and this lane did not write or restore those paths. See [shared-tree-changes.json](shared-tree-changes.json). Existing license blocks are byte-for-byte unchanged; sources already lacking a block retain their original state. This lane performed no commit, staging, branch change, br command, task tick or issue closure.

## Recipe dry run and application

- Plugin: `org.openrewrite.maven:rewrite-maven-plugin:6.46.1`.
- Recipe artifact: `org.openrewrite.recipe:rewrite-testing-frameworks:3.44.0`.
- Recipes: `org.openrewrite.java.testing.junit5.JUnit4to5Migration` and `org.openrewrite.java.testing.easymock.EasyMockToMockito`.
- Versions were checked against official published Maven metadata (2026-08-12 update) and successfully resolved/executed. The plugin reference shows a newer example version; the published resolvable version was pinned for this run. Context7 was unavailable in the exposed tools, so native DuckDuckGo fallback located the official OpenRewrite docs.
- Dry run 03b succeeded without modifying any source or protected file: **105 files, 2,225 added lines, 2,382 removed lines**, all in core test Java sources. Its diff removed **210 verification lines** and added 43; that loss was explicitly rejected during manual completion. [Dry-run diff](03b-rewrite-dry-run.patch), [machine summary](03b-dry-run-summary.json).
- The recipes leave unsupported controls, invalid captures/factories, ambiguous Jupiter overloads, license movement/duplication and incomplete exception scopes. They also initialize a parameterized context before assigning its runtime mode. The manual completion repairs these rather than accepting the generated code as finished.
- Run 04b applies the exact pinned recipes. Exclusions cover every POM, src/main and test resources; runPerSubmodule limits execution to the selected core module, skipMavenParsing avoids POM migration, and the plugin is invoked only by command-line coordinates. No plugin or dependency was added to a POM. Environmental cache write denials in 03 and 04 were retried with approved cache access in 03b and 04b; neither failed attempt edited source.

Primary documentation: [plugin reference](https://docs.openrewrite.org/reference/rewrite-maven-plugin), [JUnit4to5Migration](https://docs.openrewrite.org/recipes/java/testing/junit5/junit4to5migration), [EasyMockToMockito](https://docs.openrewrite.org/recipes/java/testing/easymock/easymocktomockito), [plugin metadata](https://repo.maven.apache.org/maven2/org/openrewrite/maven/rewrite-maven-plugin/maven-metadata.xml), [recipe metadata](https://repo.maven.apache.org/maven2/org/openrewrite/recipe/rewrite-testing-frameworks/maven-metadata.xml).

## Manual completion / test intent

- All 37 inventory-006 files and five newer review tests are mapped below. The task-start snapshot contains 480 expect sites, 249 group-verify sites, 68 capture declarations and 90 strict-mock/control creation sites. [Expectation ledger](expectation-ledger.md) accounts for every expect site, including real-model replacements and the formerly unreachable variadic invocation expectation.
- Ordered interactions use InOrder and no-more-interactions checks. Mockito mocks are recreated in BeforeEach; inline per-mode mocks remain fresh as before. No reset or clearInvocations call remains. Empty controls disappear when all their values are real immutable objects; all original object/value assertions remain.
- All 66 listed model/value mock sites become real models and values. The concrete reference fixture delegates to an expression interface, retaining per-child order/capture assertions through real Match/MatchAllOf/MatchAnyOf/Target composition. The rv019 repair adds a narrowly permitted Target spy in three exception-injection methods; other mocks remain interfaces. The audit resolves all 11 target types to their main-source declarations.
- Policy/PolicySet references now prove actual context creation and identity, id/version metadata and real model evaluation via a verified combiner, rather than forcing model methods to return canned results. Five original context captors in these two files had no captured-value assertion; the replacement uses real context identity assertions and combiner context captors on the evaluation paths.
- The rv019 Rule target-failure repair throws directly from Target.match without setting any context status in the stub. The original Indeterminate decision and both full status/message assertions now require BaseDecisionRule.isMatch's catch. Policy and PolicySet likewise inject directly at Target.match for their exception tests. Match's resolution-failure test uses a real mustBePresent designator, so the same missing-attribute exception reaches the real Match.
- All 48 expected-exception methods use assertThrows with the original type; existing catch/status/message assertions stay. The optional/variadic rejection test verifies the old invocation expectation with never(), because parameter validation throws before invocation and the original verify was unreachable. The unannotated builder helper remains unannotated and retains its original isVariadic assertion.
- Same-category XPath content calls use sequential Mockito answers and times(2), retaining both recorded contents. Five parameterized classes use MethodSource with unchanged invocation counts. The variadic context's default answer reads the runtime mode after assignment, and a fresh mock/captor is created in BeforeEach.
- All four bare Ignore annotations become bare Disabled annotations. The two real skipped methods remain; the two empty provider-class entries disappear from Jupiter reporting, as proved by the full baseline replay.

## Red / green evidence

| Run | Outcome | Cause / disposition |
| --- | --- | --- |
| 02 baseline | 762 / 0 failures / 96 errors / 4 skipped | Original EasyMock class/value proxy failures in 11 classes; recorded before recipe edits. |
| 03b dryRun / 04b run | SUCCESS | Both pinned recipes executed; all paths in scope. |
| 05 after recipes | testCompile RED, 100 reported diagnostics (compiler cap) | Unsupported controls/captures and remaining migration errors; no tests executed. |
| 09 manual migration | testCompile RED, 12 diagnostics | Missing helper imports/builder operation, split andThrow and generic Jupiter assertEquals overloads; fixed without changing asserted values. |
| 11 | testCompile RED, 1 diagnostic | FunctionInvocation has two abstract overloads, so a lambda is invalid; use the real default boolean-equal provider. |
| 12 first behavior run | 760 / 10 failures / 1 error / 2 skipped | Constructor interactions versus empty InOrder checks (five cases); required designator behavior; real target status propagation; two unnecessary parent-flag expectations; builder exception scope; unreachable variadic invocation expectation. |
| 14 and 18 | 760 / 0 / 0 / 2, SUCCESS | All above repaired; explicit runtime flag checks and lifecycle added. |
| 15 / 16 / 19 | count characterization | Temporary exact task-start source replay; migrated bytes restored afterward. Run 19 reproduces 762 / 0 / 96 / 4 and identifies both unnamed helper skips. |
| 22 clean test | 760 / 0 / 0 / 2, SUCCESS | Core-only clean rebuild on JDK 21. |
| 24 interface boundary completion | testCompile RED, 1 diagnostic | Concrete reference fixture requires accept(ExpressionVisitor); added a fail-fast visitor guard for this evaluation-only fixture. |
| 25 and 28 | 760 / 0 / 0 / 2, SUCCESS | Interface-only mocks, no resets; run 28 is the pre-review migration checkpoint; the later final gate is recorded below. |

Counts are tests / failures / errors / skipped. Receipts record every compiler diagnostic and failure trace. The compiler-error repairs affect tests/helper code only. The underlying core API, function semantics and all conformance fixtures are unchanged.

## Scope and preservation checks

`26-source-audit.json`: **550 declared methods before/after, identical names/order per file; 760 named invocations before/after with identical per-method parameterized cardinality and skip state; 1,596 assertion calls before, 1,714 after**, with no per-file decrease in any original assertion-call kind. These counts support the manual source review; counts alone do not prove semantic equivalence. The original captured-value assertions are retained in the mapped sources.

SUPPORTED syntactic proof: rtk rg finds zero legacy imports (exit 1 = no match), while the ast-grep import-declaration rule matches 544 imports in the task-start positive control and zero in current core tests. Every current Mockito target is an interface by its opened main-source declaration; no model/value mock, reset, clearInvocations or unaccounted @Mock field remains. Git diff --check passes after fixing only newly added whitespace lines, outside license blocks. [Source audit](26-source-audit.json), [exception audit](10-exception-audit.json), [commands](command.txt), [Surefire output](surefire-summary.txt).

## EasyMock-to-Mockito / real-model mapping

| Core file (relative to src/test/java/org/xacml4j/) | Inventory | Old expect / verify / capture / strict sites | Current verify / InOrder / no-more sites | Adaptation |
| --- | --- | ---: | ---: | --- |
| `v30/policy/AdviceExpressionTest.java` | 006 | 1 / 2 / 0 / 1 | 2 / 1 / 3 | Fresh Mockito interface mocks; every recorded call verified explicitly; InOrder and no-more-interactions retain the recording order and absence checks. |
| `v30/policy/ApplyTest.java` | 006 | 2 / 2 / 0 / 2 | 4 / 2 / 4 | Fresh Mockito interface mocks; every recorded call verified explicitly; InOrder and no-more-interactions retain the recording order and absence checks. |
| `v30/policy/AttributeDesignatorTest.java` | 006 | 9 / 9 / 11 / 1 | 22 / 9 / 18 | Fresh Mockito interface mocks; every recorded call verified explicitly; InOrder and no-more-interactions retain the recording order and absence checks. |
| `v30/policy/AttributeSelectorTest.java` | 006 | 9 / 9 / 10 / 1 | 20 / 9 / 18 | Fresh Mockito interface mocks; every recorded call verified explicitly; InOrder and no-more-interactions retain the recording order and absence checks. |
| `v30/policy/BagOfValuesTest.java` | 006 | 0 / 1 / 0 / 1 | 0 / 0 / 1 | Fresh Mockito interface mocks; every recorded call verified explicitly; InOrder and no-more-interactions retain the recording order and absence checks. |
| `v30/policy/CombinerParameterTest.java` | 006 | 0 / 1 / 0 / 1 | 0 / 0 / 0 | Two real BooleanVal values; all object-method assertions retained; the obsolete empty control has no mocks to verify. |
| `v30/policy/ConditionTest.java` | 006 | 12 / 6 / 3 / 1 | 24 / 5 / 11 | Real Rule and BooleanVal values; expression/context interfaces retain ordered checks and status captors. |
| `v30/policy/MatchAllOfTest.java` | 006 | 12 / 6 / 0 / 1 | 24 / 5 / 11 | Real Match children, real boolean-equal function and concrete reference fixtures; expression evaluations verified in order, including short circuit. |
| `v30/policy/MatchAnyOfTest.java` | 006 | 10 / 5 / 0 / 1 | 20 / 4 / 9 | Real MatchAllOf children; expression evaluations verified in order and unused children checked with no-more-interactions. |
| `v30/policy/MatchTest.java` | 006 | 9 / 2 / 2 / 0 | 15 / 2 / 4 | Real AttributeDesignator and Rule; resolve() and invocation interfaces are verified, including failure status/message capture. |
| `v30/policy/ObligationExpressionTest.java` | 006 | 2 / 3 / 0 / 0 | 2 / 1 / 4 | Real AttributeAssignmentExpression; id assertion replaces its getter expectation; expression failure still asserted and verified. |
| `v30/policy/PolicyIDReferenceTest.java` | 006 | 50 / 18 / 3 / 1 | 105 / 8 / 19 | Real Policy, real descendant contexts and immutable metadata assertions; resolver/combiner checks replace synthetic model calls. No reset. |
| `v30/policy/PolicySetIDReferenceTest.java` | 006 | 36 / 8 / 2 / 1 | 50 / 5 / 9 | Real PolicySet values and contexts; metadata/result/identity assertions and resolver/combiner checks. No reset. |
| `v30/policy/PolicySetTest.java` | 006 | 29 / 13 / 14 / 1 | 58 / 13 / 26 | Real Target/Condition; expression and combiner interfaces retain order, captured context metadata and obligations/advice assertions. |
| `v30/policy/PolicyTest.java` | 006 | 29 / 13 / 14 / 1 | 58 / 13 / 26 | Real Target/Condition; expression and combiner interfaces retain order, captured context metadata and obligations/advice assertions. |
| `v30/policy/RootEvaluationContextTest.java` | 006 | 1 / 3 / 0 / 0 | 2 / 1 / 4 | Fresh Mockito interface mocks; every recorded call verified explicitly; InOrder and no-more-interactions retain the recording order and absence checks. |
| `v30/policy/RuleTest.java` | 006 | 38 / 21 / 0 / 1 | 76 / 19 / 40 | Real Target/Condition with expression boundaries; real contexts, advice/obligation values and every original status/result assertion retained. |
| `v30/policy/TargetTest.java` | 006 | 8 / 7 / 8 / 1 | 16 / 5 / 11 | Real MatchAnyOf children; expression captors and ordered evaluations retain original composition outcomes. |
| `v30/policy/VariableDefinitionTest.java` | 006 | 3 / 2 / 0 / 2 | 8 / 2 / 4 | Fresh Mockito interface mocks; every recorded call verified explicitly; InOrder and no-more-interactions retain the recording order and absence checks. |
| `v30/policy/VariableReferenceTest.java` | 006 | 3 / 2 / 0 / 2 | 8 / 2 / 4 | Fresh Mockito interface mocks; every recorded call verified explicitly; InOrder and no-more-interactions retain the recording order and absence checks. |
| `v30/policy/combine/impl/DenyOverridesTest.java` | 006 | 32 / 10 / 0 / 0 | 64 / 10 / 20 | Fresh Mockito interface mocks; every recorded call verified explicitly; InOrder and no-more-interactions retain the recording order and absence checks. |
| `v30/policy/combine/impl/DenyUnlessPermitTest.java` | 006 | 16 / 7 / 0 / 9 | 32 / 7 / 14 | Fresh Mockito interface mocks; every recorded call verified explicitly; InOrder and no-more-interactions retain the recording order and absence checks. |
| `v30/policy/combine/impl/FirstApplicableTest.java` | 006 | 16 / 7 / 0 / 15 | 32 / 7 / 14 | Fresh Mockito interface mocks; every recorded call verified explicitly; InOrder and no-more-interactions retain the recording order and absence checks. |
| `v30/policy/combine/impl/OnPermitApplySecondPolicyTest.java` | 006 | 12 / 4 / 0 / 0 | 26 / 4 / 8 | Fresh Mockito interface mocks; every recorded call verified explicitly; InOrder and no-more-interactions retain the recording order and absence checks. |
| `v30/policy/combine/impl/OnlyOneApplicablePolicyCombiningAlgorithmTest.java` | 006 | 25 / 6 / 0 / 25 | 50 / 6 / 12 | Fresh Mockito interface mocks; every recorded call verified explicitly; InOrder and no-more-interactions retain the recording order and absence checks. |
| `v30/policy/combine/impl/PermitOverridesTest.java` | 006 | 30 / 9 / 0 / 17 | 60 / 9 / 18 | Fresh Mockito interface mocks; every recorded call verified explicitly; InOrder and no-more-interactions retain the recording order and absence checks. |
| `v30/policy/combine/impl/PermitUnlessDenyTest.java` | 006 | 10 / 5 / 0 / 1 | 20 / 4 / 9 | Fresh Mockito interface mocks; every recorded call verified explicitly; InOrder and no-more-interactions retain the recording order and absence checks. |
| `v30/policy/function/FunctionSpecBuilderTest.java` | 006 | 0 / 4 / 0 / 0 | 0 / 0 / 4 | Invocation interface and no-more-interactions retained; expected exception covers setup and call where setup can throw; unannotated helper restored. |
| `v30/policy/function/impl/AnnotationBasedFunctionFactoryTest.java` | 006 | 4 / 4 / 0 / 1 | 7 / 4 / 8 | Fresh Mockito interface mocks; every recorded call verified explicitly; InOrder and no-more-interactions retain the recording order and absence checks. |
| `v30/policy/function/impl/AttributeDesignatorFunctionTest.java` | 006 | 1 / 2 / 0 / 0 | 2 / 1 / 3 | Fresh Mockito interface mocks; every recorded call verified explicitly; InOrder and no-more-interactions retain the recording order and absence checks. |
| `v30/policy/function/impl/DefaultFunctionFunctionInvocationTest.java` | 006 | 18 / 6 / 0 / 0 | 36 / 6 / 12 | Fresh Mockito interface mocks; every recorded call verified explicitly; InOrder and no-more-interactions retain the recording order and absence checks. |
| `v30/policy/function/impl/DefaultFunctionSpecTest.java` | 006 | 20 / 10 / 0 / 0 | 39 / 10 / 20 | Invocation/context interfaces verified even after assertThrows. Invalid variadic input rejects before invocation; the formerly unreachable expectation is verified with never(). |
| `v30/policy/function/impl/DurationFunctionIdentifierTest.java` | newer review test | 1 / 0 / 0 / 0 | 1 / 0 / 1 | MethodSource preserves 72 cases; fresh context per mode; every alias/result assertion plus runtime-flag verification and no-more-interactions. |
| `v30/policy/function/impl/HigherOrderFunctionIdentifierTest.java` | newer review test | 1 / 0 / 0 / 0 | 1 / 0 / 1 | MethodSource preserves 22 cases; fresh context per mode, all lookup/evaluation assertions and explicit runtime-flag verification. |
| `v30/policy/function/impl/HigherOrderFunctionsTest.java` | 006 | 6 / 6 / 0 / 0 | 12 / 6 / 12 | Fresh Mockito interface mocks; every recorded call verified explicitly; InOrder and no-more-interactions retain the recording order and absence checks. |
| `v30/policy/function/impl/HigherOrderVariadicFunctionsTest.java` | newer review test | 1 / 0 / 1 / 0 | 4 / 0 / 1 | MethodSource preserves 54 cases; fresh BeforeEach mock with mode read after parameter assignment, ArgumentCaptor statuses, explicit checks and AfterEach no-more-interactions. |
| `v30/policy/function/impl/HigherOrderVariadicInvocationTest.java` | newer review test | 1 / 0 / 0 / 0 | 0 / 0 / 0 | Real RootEvaluationContext for both runtime modes; boolean mode assertion replaces the optional flag stub; all three tests retained. |
| `v30/policy/function/impl/LogicalFunctionsTest.java` | 006 | 11 / 21 / 0 / 0 | 22 / 2 / 27 | All six test methods and the checks from 21 legacy group-verify sites retained; expressions/context verified explicitly; five obsolete resets removed without clearing verified history. |
| `v30/policy/function/impl/NonNumericComparisionFunctionTest.java` | 006 | 0 / 3 / 0 / 1 | 0 / 0 / 3 | Fresh Mockito interface mocks; every recorded call verified explicitly; InOrder and no-more-interactions retain the recording order and absence checks. |
| `v30/policy/function/impl/RegularExpressionFunctionsTest.java` | 006 | 0 / 7 / 0 / 0 | 0 / 0 / 7 | Fresh Mockito interface mocks; every recorded call verified explicitly; InOrder and no-more-interactions retain the recording order and absence checks. |
| `v30/policy/function/impl/XPathFunctionsTest.java` | 006 | 11 / 5 / 0 / 1 | 18 / 5 / 10 | Context calls remain ordered; two same-category content resolutions use sequential answers and times(2), preserving both contents. |
| `v30/types/ISO8601DurationReviewTest.java` | newer review test | 1 / 0 / 0 / 0 | 1 / 0 / 1 | Fresh context per mode; all fraction/round-trip assertions retained, plus exact verification of 16 Apply flag calls per mode. |

## Per-class method and case counts

Cases below use the actual XML testcase classname. The two empty provider skips are shown under their own classes; Vintage's textual Maven reports attributed them to BagFunctionsTest and DefaultFunctionSpecTest. [Standalone counts](per-class-counts.md) and raw Results/per-report counts in surefire-summary.txt retain that distinction.

| Core class | Declared methods before / after | XML cases before / after | Skips before / after |
| --- | ---: | ---: | ---: |
| `util.DOMUtilTest` | 9 / 9 | 9 / 9 | 0 / 0 |
| `util.ISO8601DurationTest` | 2 / 2 | 2 / 2 | 0 / 0 |
| `util.JSONUtilTest` | 1 / 1 | 1 / 1 | 0 / 0 |
| `v30.AttributeIndexTest` | 1 / 1 | 1 / 1 | 0 / 0 |
| `v30.AttributeTest` | 5 / 5 | 5 / 5 | 0 / 0 |
| `v30.CategoryIdTest` | 4 / 4 | 4 / 4 | 0 / 0 |
| `v30.ContentTest` | 4 / 4 | 4 / 4 | 0 / 0 |
| `v30.EntityTest` | 5 / 5 | 5 / 5 | 0 / 0 |
| `v30.ObligationTest` | 4 / 4 | 4 / 4 | 0 / 0 |
| `v30.WellKnownAttributeIdsTest` | 1 / 1 | 1 / 1 | 0 / 0 |
| `v30.content.ContentTest` | 4 / 4 | 4 / 4 | 1 / 1 |
| `v30.content.JsonContentTest` | 7 / 7 | 7 / 7 | 0 / 0 |
| `v30.content.XmlContentTest` | 14 / 14 | 14 / 14 | 0 / 0 |
| `v30.policy.AdviceExpressionTest` | 2 / 2 | 2 / 2 | 0 / 0 |
| `v30.policy.AdviceTest` | 7 / 7 | 7 / 7 | 0 / 0 |
| `v30.policy.ApplyTest` | 2 / 2 | 2 / 2 | 0 / 0 |
| `v30.policy.AttributeAssignmentTest` | 1 / 1 | 1 / 1 | 0 / 0 |
| `v30.policy.AttributeDesignatorTest` | 9 / 9 | 9 / 9 | 0 / 0 |
| `v30.policy.AttributeSelectorTest` | 9 / 9 | 9 / 9 | 0 / 0 |
| `v30.policy.AttributesTest` | 7 / 7 | 7 / 7 | 0 / 0 |
| `v30.policy.BagOfValuesTest` | 11 / 11 | 11 / 11 | 0 / 0 |
| `v30.policy.CombinerParameterTest` | 1 / 1 | 1 / 1 | 0 / 0 |
| `v30.policy.ConditionTest` | 6 / 6 | 6 / 6 | 0 / 0 |
| `v30.policy.DecisionTest` | 1 / 1 | 1 / 1 | 0 / 0 |
| `v30.policy.MatchAllOfTest` | 6 / 6 | 6 / 6 | 0 / 0 |
| `v30.policy.MatchAnyOfTest` | 5 / 5 | 5 / 5 | 0 / 0 |
| `v30.policy.MatchTest` | 2 / 2 | 2 / 2 | 0 / 0 |
| `v30.policy.ObligationExpressionTest` | 3 / 3 | 3 / 3 | 0 / 0 |
| `v30.policy.PolicyDefaultsTest` | 2 / 2 | 2 / 2 | 0 / 0 |
| `v30.policy.PolicyIDReferenceTest` | 12 / 12 | 12 / 12 | 0 / 0 |
| `v30.policy.PolicySetIDReferenceTest` | 7 / 7 | 7 / 7 | 0 / 0 |
| `v30.policy.PolicySetTest` | 14 / 14 | 14 / 14 | 0 / 0 |
| `v30.policy.PolicyTest` | 14 / 14 | 14 / 14 | 0 / 0 |
| `v30.policy.RequestTest` | 8 / 8 | 8 / 8 | 0 / 0 |
| `v30.policy.ResultTest` | 1 / 1 | 1 / 1 | 0 / 0 |
| `v30.policy.RootEvaluationContextTest` | 3 / 3 | 3 / 3 | 0 / 0 |
| `v30.policy.RuleTest` | 23 / 23 | 23 / 23 | 0 / 0 |
| `v30.policy.StatusCodeIdTest` | 1 / 1 | 1 / 1 | 0 / 0 |
| `v30.policy.TargetTest` | 7 / 7 | 7 / 7 | 0 / 0 |
| `v30.policy.VariableDefinitionTest` | 2 / 2 | 2 / 2 | 0 / 0 |
| `v30.policy.VariableReferenceTest` | 2 / 2 | 2 / 2 | 0 / 0 |
| `v30.policy.VersionMatchTest` | 5 / 5 | 5 / 5 | 0 / 0 |
| `v30.policy.VersionTest` | 6 / 6 | 6 / 6 | 0 / 0 |
| `v30.policy.XPathTest` | 1 / 1 | 1 / 1 | 0 / 0 |
| `v30.policy.combine.AnnotatedDecisionCombiningAlgorithmProviderFactoryTest` | 5 / 5 | 5 / 5 | 0 / 0 |
| `v30.policy.combine.impl.DenyOverridesTest` | 11 / 11 | 11 / 11 | 0 / 0 |
| `v30.policy.combine.impl.DenyUnlessPermitTest` | 8 / 8 | 8 / 8 | 0 / 0 |
| `v30.policy.combine.impl.FirstApplicableTest` | 8 / 8 | 8 / 8 | 0 / 0 |
| `v30.policy.combine.impl.OnPermitApplySecondPolicyTest` | 4 / 4 | 4 / 4 | 0 / 0 |
| `v30.policy.combine.impl.OnlyOneApplicablePolicyCombiningAlgorithmTest` | 6 / 6 | 6 / 6 | 0 / 0 |
| `v30.policy.combine.impl.PermitOverridesTest` | 10 / 10 | 10 / 10 | 0 / 0 |
| `v30.policy.combine.impl.PermitUnlessDenyTest` | 5 / 5 | 5 / 5 | 0 / 0 |
| `v30.policy.function.FunctionSpecBuilderTest` | 11 / 11 | 11 / 11 | 0 / 0 |
| `v30.policy.function.ParamSingleTypeSpecTest` | 2 / 2 | 2 / 2 | 0 / 0 |
| `v30.policy.function.ParamTypeSequenceTest` | 3 / 3 | 3 / 3 | 0 / 0 |
| `v30.policy.function.impl.AnnotationBasedFunctionFactoryTest` | 5 / 5 | 5 / 5 | 0 / 0 |
| `v30.policy.function.impl.ArtimeticFunctionsTest` | 3 / 3 | 3 / 3 | 0 / 0 |
| `v30.policy.function.impl.AttributeDesignatorFunctionTest` | 2 / 2 | 2 / 2 | 0 / 0 |
| `v30.policy.function.impl.BagFunctionsTest` | 6 / 6 | 6 / 6 | 0 / 0 |
| `v30.policy.function.impl.DateTimeArithmeticBoundaryTest` | 1 / 1 | 50 / 50 | 0 / 0 |
| `v30.policy.function.impl.DateTimeValueArithmeticFunctionTest` | 3 / 3 | 3 / 3 | 0 / 0 |
| `v30.policy.function.impl.DefaultFunctionFunctionInvocationTest` | 6 / 6 | 6 / 6 | 0 / 0 |
| `v30.policy.function.impl.DefaultFunctionSpecTest` | 10 / 10 | 10 / 10 | 0 / 0 |
| `v30.policy.function.impl.DurationFunctionIdentifierTest` | 2 / 2 | 72 / 72 | 0 / 0 |
| `v30.policy.function.impl.HigherOrderFunctionIdentifierTest` | 2 / 2 | 22 / 22 | 0 / 0 |
| `v30.policy.function.impl.HigherOrderFunctionsTest` | 6 / 6 | 6 / 6 | 0 / 0 |
| `v30.policy.function.impl.HigherOrderVariadicFunctionsTest` | 27 / 27 | 54 / 54 | 0 / 0 |
| `v30.policy.function.impl.HigherOrderVariadicInvocationTest` | 3 / 3 | 3 / 3 | 0 / 0 |
| `v30.policy.function.impl.JavaMethodToFunctionSpecTest` | 7 / 7 | 7 / 7 | 0 / 0 |
| `v30.policy.function.impl.JavaMethodToFunctionSpecTestFunctions` | 0 / 0 | 0 / 0 | 0 / 0 |
| `v30.policy.function.impl.LogicalFunctionsTest` | 6 / 6 | 6 / 6 | 0 / 0 |
| `v30.policy.function.impl.NonNumericComparisionFunctionTest` | 6 / 6 | 6 / 6 | 0 / 0 |
| `v30.policy.function.impl.NumericComparisonFunctionTest` | 4 / 4 | 4 / 4 | 0 / 0 |
| `v30.policy.function.impl.RegularExpressionFunctionsTest` | 7 / 7 | 7 / 7 | 0 / 0 |
| `v30.policy.function.impl.SetFunctionTest` | 5 / 5 | 5 / 5 | 0 / 0 |
| `v30.policy.function.impl.SpecialMatchFunctionsTest` | 3 / 3 | 3 / 3 | 0 / 0 |
| `v30.policy.function.impl.TestFunctions` | 0 / 0 | 1 / 0 | 1 / 0 |
| `v30.policy.function.impl.TestInstanceFunctions` | 0 / 0 | 1 / 0 | 1 / 0 |
| `v30.policy.function.impl.XPathFunctionsTest` | 6 / 6 | 6 / 6 | 0 / 0 |
| `v30.types.AnyURITest` | 3 / 3 | 3 / 3 | 0 / 0 |
| `v30.types.Base64BinaryTest` | 3 / 3 | 3 / 3 | 0 / 0 |
| `v30.types.BinaryTest` | 4 / 4 | 4 / 4 | 0 / 0 |
| `v30.types.BooleanValTest` | 5 / 5 | 5 / 5 | 0 / 0 |
| `v30.types.DNSNameTest` | 4 / 4 | 4 / 4 | 0 / 0 |
| `v30.types.DoubleValTest` | 2 / 2 | 2 / 2 | 0 / 0 |
| `v30.types.EUI48Test` | 11 / 11 | 11 / 11 | 0 / 0 |
| `v30.types.HexTypeTest` | 2 / 2 | 2 / 2 | 0 / 0 |
| `v30.types.IPAddressTest` | 6 / 6 | 6 / 6 | 0 / 0 |
| `v30.types.ISO8601DateTest` | 4 / 4 | 4 / 4 | 0 / 0 |
| `v30.types.ISO8601DateTimeArithmeticTest` | 12 / 12 | 12 / 12 | 0 / 0 |
| `v30.types.ISO8601DateTimeTest` | 8 / 8 | 8 / 8 | 0 / 0 |
| `v30.types.ISO8601DayTimeDurationTest` | 9 / 9 | 9 / 9 | 0 / 0 |
| `v30.types.ISO8601DurationReviewTest` | 9 / 9 | 9 / 9 | 0 / 0 |
| `v30.types.ISO8601FractionalDateTimeArithmeticTest` | 2 / 2 | 46 / 46 | 0 / 0 |
| `v30.types.ISO8601TimeTest` | 4 / 4 | 4 / 4 | 0 / 0 |
| `v30.types.ISO8601YearMonthDurationTest` | 3 / 3 | 3 / 3 | 0 / 0 |
| `v30.types.IntegerTypeTest` | 3 / 3 | 3 / 3 | 0 / 0 |
| `v30.types.PathTest` | 2 / 2 | 2 / 2 | 0 / 0 |
| `v30.types.PortRangeTest` | 7 / 7 | 7 / 7 | 0 / 0 |
| `v30.types.RFC822NameTest` | 4 / 4 | 4 / 4 | 0 / 0 |
| `v30.types.StringValTest` | 1 / 1 | 1 / 1 | 0 / 0 |
| `v30.types.TypeCapabilityTest` | 1 / 1 | 1 / 1 | 0 / 0 |
| `v30.types.TypeToStringCapabilityTest` | 1 / 1 | 1 / 1 | 0 / 0 |
| `v30.types.TypeToStringTest` | 1 / 1 | 1 / 1 | 0 / 0 |
| `v30.types.Xacml20XPathTo30TransformerTest` | 3 / 3 | 3 / 3 | 1 / 1 |
| `v30.types.XacmlTypesTest` | 2 / 2 | 2 / 2 | 0 / 0 |
| **Total** | **550 / 550** | **762 / 760** | **4 / 2** |

## Conductor review follow-up: recipe artifacts / oracle strength

Historical result: run **47** on JDK 21 is GREEN: **760 / 0 failures / 0 errors / 2 skipped**. Run 45 also passes with the same counts. At that point, literal 762/four-skips acceptance remained pending. The rv019 follow-up packet specifies the 760/zero/zero/two gate; run 54 now passes it with the catch proof in review-artifacts.md.

| Conductor item | Evidence / disposition |
| --- | --- |
| 1. Plain RECORD-phase calls on Mockito mocks | Java statement and method-invocation AST scans find zero bare mock calls outside stubbing in current sources. The task-start positive control contains ten void recording sites; all ten map to explicit post-stimulus checks (the higher-order context setup maps to assertInvalid plus AfterEach). |
| 2. Verification inside assertThrows | Zero verify calls inside an assertThrows invocation. AttributeDesignatorTest's pre-existing try/catch that could fall through was strengthened to assertThrows; every original status/reference assertion remains after it. |
| 3. Dropped strict / ordered checks | Every expectation remains in the existing ledger; all ten recorded void calls have checks. InOrder sequences remain. Cache mutation initially survived (run 41) because strict-stub handling marked the extra stubbed call verified. Added 470 explicit cumulative total checks at 202 boundaries in 32 files, alongside ordered checks, to restore exact frequency enforcement. Totals come from expected calls, never actual invocation history. |
| 4. Duplicate consecutive verification | Zero identical consecutive verification statements. Ordered checks establish sequence; companion global times(N) checks establish total frequency, including interleaved extra stubbed calls. These have distinct purposes. |
| 5. Unasserted captors | Initial review confirmed **40** unasserted captors (pre-existing), across seven files / 34 methods. Added key, lazy status, Rule identity, rule-list and context checks. All 65 current captor declarations now have asserted value reads. Initial run 34: 61 / 2 / 0 / 0, due to two new assertions incorrectly assuming a direct context; corrected those new assertions to check extended mode and policy metadata on the existing Indeterminate wrapper. Run 35: **61 / 0 / 0 / 0**. No original assertion was removed. |
| 6. Lost times(N) / once checks | All 17 explicit count sites are accounted for: 15 remain exact mock-count checks; two Condition object-method sites now use real BooleanVal values with explicit type assertions. Implicit once calls keep explicit checks. Companion cumulative counts also enforce earlier expectations during later cached/scenario calls. |
| 7. control.verify-only or empty controls | All 24 methods found without an assertion call in the old body retain explicit interaction and/or no-more checks; expected-exception annotations are now explicit assertThrows. The empty-control mutation fails at verifyNoMoreInteractions. Real-value-only controls disappear with their model/value assertions preserved. |

SUPPORTED: ast-grep represents Java syntax. Receiver identity inference and count correspondence were also reviewed in source; this is not an IDE reference inventory. [Before review](32-review-artifacts-before.json), [final artifact audit](32-review-artifacts-audit.json), [new capture checks](33-captor-oracle-fixes.json), [exact-count checks](44-exact-count-checks.json). No Mockito strictness was relaxed, no assertion/count was adjusted to absorb a test's own normal invocation, and no reset/clearInvocations was added.

### Temporary mutation proof

Mutations changed owned test stimuli/collaborator answers only, to simulate wrong order, extra or missing calls, and wrong captured data. This proves the test oracles reject those faults; it is **not** a mutation-coverage claim for production code. None was an assertion or verification mutation. Every red below executed its test and failed at the listed oracle, not during compilation. The cache check was repeated after repair; the other original oracle checks were retained while the total checks were added.

| Oracle / test | Temporary mutation | Outcome | Restoration |
| --- | --- | --- | --- |
| `TargetTest.testAllMatch` | Reverse two matching children; the result remains MATCH but the original strict order must fail. | InOrder rejects reversed matching children; **1 / 1 / 0 / 0** ([receipt](37-mutant-order-red-receipt.json)) | Exact bytes restored |
| `HigherOrderFunctionsTest.testMapWithValidArguments` | Add a fourth input without removing any expected member; times(4) must reject the fifth flag call. | Exact count rejects five calls where four were recorded; **1 / 1 / 0 / 0** ([receipt](38-mutant-count-red-receipt.json)) | Exact bytes restored |
| `AttributeDesignatorTest.testMustBePresentTrueAttributeDoesNotExistAndContextHandlerReturnsEmptyBag` | Throw the expected exception without invoking the designator; assertThrows succeeds and the following resolve verification must fail. | assertThrows succeeds, then verification rejects missing resolve; **1 / 1 / 0 / 0** ([receipt](39-mutant-exception-verify-red-receipt.json)) | Exact bytes restored |
| `ConditionTest.testExpressionThrowsRuntimeException` | Return a different current Rule; result/status/count checks remain satisfied but the captured Rule identity assertion must fail. | New captured Rule identity assertion rejects a different Rule; **1 / 1 / 0 / 0** ([receipt](40-mutant-captured-rule-red-receipt.json)) | Exact bytes restored |
| `FunctionSpecBuilderTest.testParameterVarArgMinAndMaxEquals` | Insert an unexpected invocation before the expected builder exception; the replacement for the empty control verification must reject it. | No-more-interactions rejects an unexpected invocation with no expectations; **1 / 1 / 0 / 0** ([receipt](42-mutant-empty-control-red-receipt.json)) | Exact bytes restored |
| `PolicyIDReferenceTest.testEvaluateFailedToResolveReferenceAndReturnNull` | Simulate an unnecessary second resolver call after cached evaluation; the result remains Indeterminate but the one-call resolver budget must reject it. | Explicit total rejects a second resolver call after cached evaluation; **1 / 1 / 0 / 0** ([receipt](46-mutant-cache-exact-count-red-receipt.json)) | Exact bytes restored |

Run 41's initially surviving cache mutation is retained as [receipt](41-mutant-cache-red-receipt.json) and [initial patch](41-cache-mutation.patch). Run 46 repeats it against the strengthened source and fails with TooManyActualInvocations: wanted one resolver call, observed two. The code returns to exact pre-mutation bytes, verified by SHA-256 restoration receipts; run 47 then executes every core case successfully.

### Follow-up scope / gate

This follow-up changes seven files for capture/exception checks and adds total-count checks in 32 existing core test files (overlapping sets); the full migration still changes 105 existing sources plus ModelFixtures.java. All 550 original declared test methods and all 760 named invocations remain, including the same two actual disabled tests. License blocks remain byte-identical. Root/core POMs, core main code, vendored inputs and runs 008/009 match task-start hashes.

The shared-tree audit now observes 30 changed paths outside this lane (other module migration sources, one module POM and conductor tracking/evidence files). Authorship is UNATTRIBUTED; this lane's write commands/patches were restricted to core test sources and this run. Those external changes were preserved, not restored. The global preservation audit's original strict mode correctly rejected those differences; its explicit --allow-shared-changes mode records them while still rejecting any change to the protected core/POM/vendored/prior-run paths. [Shared-tree differences](shared-tree-changes.json).

Full reactor/conformance and JDK 28 remain UNRUN in this lane. No commit, br command, task tick, issue closure or out-of-scope fix was performed here.

## Remaining acceptance and risk

Task 2.2 is implemented and evidenced. Task 2.3's test run is green and declared-test/named-invocation parity is proved; the literal 762/four-skip numeric condition remains PENDING conductor decision. No decision is inferred from silence. The task/evidence index and beads status are left to the conductor.

Full reactor/conformance and JDK 28 are UNRUN in this lane, per the packet's core-only scope. Real-fixture substitutions require review for the documented proof changes above. The main sources and dependency/plugin configuration are unchanged.
