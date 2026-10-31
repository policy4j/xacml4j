# PDP Mockito oracle repair

## Result

Status: **ready to apply** after the conductor's correction to repair item 7.

The restored-source JDK 21 reactor run reports xacml-pdp at 73 tests, 0 failures, 0 errors, and 3 skips (`pdp-run-repair.log`, lines 11921-11933). The source tree has 75 `@Test` annotations and 5 `@Disabled` annotations. The other reactor module, xacml-core, reports 762 tests and the expected 96 EasyMock class-mock errors (line 11176); those are outside this lane. Maven reports Java 21.0.6 (`command.txt`).

The conductor corrected repair item 7: the baseline oracle for these three tests is only that `resolve` throws `PathEvaluationException`. Each migrated test now wraps only `handler.resolve(...)` in `assertThrows`, includes a comment that the baseline status expectation was unreachable after the throw, and verifies the callback's `getEntity` call after the exception. The baseline's PIP expectation is also unreachable: entity content resolution throws before the PIP fallback (`DefaultEvaluationContextHandler.doSelectorResolve`/`resolveSelector`). The PIP stub was removed. `repair-path-exceptions.log` shows all 18 handler tests pass. No production code changed.

## Per-method oracle audit

| Test method(s) | Baseline oracle | Migrated oracle |
|---|---|---|
| `DefaultPolicyReferenceResolverTest.testResolvePolicyIDReference` | Repository listener registered by the resolver; repository `getPolicy` called twice across first resolution and cache eviction/refetch; `getPolicySet` called once. | Removed test-body listener call; verify listener registration once, `getPolicy` exactly twice, `getPolicySet` exactly once, and no further repository interactions. |
| `DefaultPolicyReferenceResolverTest.testResolvePolicySetIDReference` | The expected policy-set repository lookup and no unexpected repository traffic. | Explicit expected `getPolicySet` verification plus no-more-interactions. |
| `DefaultEvaluationContextHandlerTest.testSelectorFallsBackToEntityValueFromPipAndCachesResult` | Callback/PIP interactions across two resolutions prove cache behavior. | Verify callback entity lookup exactly twice and the selector/designator PIP resolutions once each; reject additional interactions. |
| `DefaultEvaluationContextHandlerTest.testSelectorResolveContentIsNotInRequestPIPCallsHandlerToResolveTheSameAttribute` | One callback/PIP path with a repeated cached selector resolution. | Verify callback and PIP call cardinalities; the re-entrant case verifies `pip.resolve(context, ref)` exactly once. |
| `DefaultEvaluationContextHandlerTest.testDesignatorResolveAttributeIsNotInRequestPIPCallsHandlerToResolveTheSameAttribute` | The PIP's recursive call into the handler terminates after the expected single PIP entry. | `verify(pip, times(1)).resolve(context, ref)`; wrong-count mutation fails with TooFewActualInvocations. |
| `DefaultEvaluationContextHandlerTest.testSelectorResolveContentIsInRequestXPathReturnsUnsupportedNodeType`, `...ReturnsNonEmptySetAndNodeFailsConvertionToDate`, `...ReturnsNonEmptySetAndCorrectNodeTypeAndNodeFailsConvertionToDate` | Baseline oracle: the call throws `PathEvaluationException`. The status expectation/control verification and PIP fallback expectation followed the throwing call and were unreachable. | `assertThrows` wraps only `handler.resolve(...)`; after it, verify the one callback `getEntity` call that occurs before the throw. No status or PIP verification is claimed. |
| `DefaultPolicyInformationPointTest.testMatchingResolverFoundAndResultIsCacheable` | Strict-order cache/context calls: cache lookup, context resolve, clock read, cache write. | `InOrder` verifies those calls in sequence and verifies no further cache/context interactions. |
| `DefaultPolicyInformationPointTest.testFound2MatchingResolversWithDifferentIssuersFirstResolverResolvesToEmptySet` (disabled) | Only the two recorded cache `getAttributes` calls; the baseline's commented-out expectations are not part of the oracle. | Verify exactly two cache reads, and no unexpected cache/context interactions. |
| `DefaultPolicyInformationPointTest.testAttributeResolutionWhenMatchingAttributeResolverFoundResolverResultsIsNotCachable` (disabled) | Recorded cache/context path and order. | Verify only the baseline-recorded cache read, context resolve, and cache write in order; no further calls. |
| `AnnotatedResolverFactoryTest.testParseAttributeResolverWithKeys` | Two context resolutions, one clock read, no other calls. | Verify both expected keys, clock once, then `verifyNoMoreInteractions(context)`. |
| `AnnotatedResolverFactoryTest.testParseAttributeResolverWithoutKeysWithContext` | No context expectations. | `verifyNoInteractions(context)`. |
| `DefaultResolverRegistryTest` methods (5) | Strict mock control was verified without recorded context expectations. | `verifyNoInteractions(context)` after each method's assertions. |
| `ResolverRegistryBuilderTest` | Control verification with no expected resolver interactions. | `verifyNoInteractions(r)`. |
| `RequestContextHandlerChainTest.testCreateCompositeHandlerWith1Handler` | One handler link caused by the SUT's `h.setNext(m1)`. | No direct mock setup call remains; verify `m0.setNext(m1)` once and no further interaction. |
| `RequestContextHandlerChainTest.testCreateCompositeHandlerWith4Handlers` | Constructor links `m0→m1→m2→m3`, followed by two SUT calls that link `m3→m4`. | `InOrder` verifies each constructor link once and `m3.setNext(m4)` twice; no more interactions on `m0..m3` and no interactions on `m4`. |
| `MultipleResourcesViaXacmlXPathHandlerTest` request tests | One recorded PDP request per tested path. | Removed duplicate consecutive verifies; one verify remains with the baseline count. |
| `DefaultPolicyDecisionPointTest` methods (2) | Strict-control verification over PDP collaborators and evaluation context. | Explicit collaborator verifications plus `verifyNoMoreInteractions` for collaborators and `verifyNoInteractions` for the evaluation context. |
| `InMemoryPolicyRepositoryTest` listener methods (4) | Listener calls verified after the latch barrier, with no extra listener interactions. | Retain the latch waits and callback assertions, then `verifyNoMoreInteractions(listener)`. |

## Mutation checks

All temporary mutations were reverted before the final gate. The test-level mutation runs use `-Dmaven.test.failure.ignore=true`, so Maven's reactor can report success while the targeted Surefire test correctly reports one failure.

| Oracle under test | Temporary mutation | Result |
|---|---|---|
| Policy-reference lookup identity / cache refetch | Made the second `getPolicy` stub return a fresh equivalent policy object. | `testResolvePolicyIDReference` failed at identity assertion (1 test, 1 failure), `mutation-policy-reference.log`. |
| Selector cache callback/PIP counts | Temporarily bypassed selector-cache lookup in the handler. | `testSelectorFallsBackToEntityValueFromPipAndCachesResult` and `testSelectorResolveContentIsNotInRequestPIPCallsHandlerToResolveTheSameAttribute` failed on excess callback invocations (18 tests, 2 failures), `mutation-selector-cache.log`. The source was restored to `Optional.ofNullable(selectorCache.get(ref))`. |
| Re-entrant designator PIP call count | Changed the expected PIP call count from one to two. | The test failed with TooFewActualInvocations (1 test, 1 failure), `mutation-reentrant-pip-count.log`. The expected count was restored to one. |
| Annotated resolver expected-key verification | Mutated the expected second key to the first key. | `testParseAttributeResolverWithKeys` failed because the actual second-key interaction remained unverified (`verifyNoMoreInteractions`), `mutation-annotated-keys.log`. Both key verifications were restored. |
| Annotated resolver no-interaction oracle | Temporarily added a clock call in `resolve2`. | `testParseAttributeResolverWithoutKeysWithContext` failed at `verifyNoInteractions(context)` (1 test, 1 failure), `mutation-annotated-no-interactions.log`. The fixture was restored. |
| Dead baseline status expectation | Temporarily added `verify(context).setEvaluationStatus(any(Status.class))` after the throwing call. | Mockito reported zero context interactions (`mutation-path-status.log`). The final tests preserve the exception-only oracle and verify the callback that is reached. |

## Count and skip reconciliation

Before/after per-class test counts are preserved in `surefire-summary.txt`. Its baseline InMemoryPolicyRepositoryTest line reports 5 runs / 1 skip, while both baseline and lane source contain four `@Test` methods and no skip annotation in that class. This is a retained Surefire/source discrepancy, not a deleted test. The aggregate baseline 74/4 versus current 73/3 is explained by `TestObject`: it has a class-level `@Ignore`/`@Disabled` and no test methods; Vintage counted the ignored helper as a skipped test, while Jupiter discovers no test for the disabled helper. Current skips are `BaseAttributeResolverTest.testMBeanInvocationStats` and the two disabled `DefaultPolicyInformationPointTest` methods named above.

## Header and import checks

Compared the LGPL block after the package line against `.base` for every baseline PDP test Java file with a header: 20 checked, 0 mismatches. The six previously lost headers were restored byte-for-byte. All `xacml-pdp/src/main` files compare byte-for-byte with `.base`, confirming the temporary selector-cache mutation was reverted. Source scan found 75 `@Test` methods/annotations. The legacy JUnit 4 and EasyMock import/API search returned no matches (exit 1 means no matches). Exact commands are recorded in `command.txt`.
