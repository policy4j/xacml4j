# rv006: gate review of cycles A, B, D, E, F (Codex GPT-6.1 Sol)

Date: 2026-10-27. Read-only NTM lane, other model family than the author. Packet: `packet.md`; report: `report.md` (local paths rewritten).

Verdicts: A APPROVE; B REJECT; D REJECT; E REJECT; F APPROVE.

## Disposition (conductor, 2026-10-27)

| Finding | Disposition | Proof |
| --- | --- | --- |
| D: the default two-argument `invoke` bypassed a custom varargs override (RVC1 1, RVC2 P2) | fixed: the default calls `invoke(context, new Expression[]{arg0, arg1})`; only the built-in implementation overrides it | `FunctionInvocationCharacterizationTest.twoArgumentCallKeepsAnOverriddenVariadicInvoke` (through `Match`): fails on the pre-fix tree, passes before D and after the fix |
| D: the pair path handed `FunctionInvocation` a fixed-size list (RVC1 2, RVC2 P2) | fixed: a two-element `ArrayList` | `eagerPairInvocationMayChangeItsArgumentList`: error on the pre-fix tree, passes before D and after the fix |
| B: `Attribute.valueList()` changed the default `serialVersionUID` (RVC1 3) | fixed: accessor removed; `AttributeIndex` reads `values()` and uses the stored list when it is a `List`. Base UID 7950110929829202273 (computed from the class built before the change), pre-fix 2563522610257036810 | `RequestFixedCostCharacterizationTest.attributeKeepsItsSerialVersionUid` |
| E: cached identifier hash over protected mutable fields (RVC1 4, RVC2 P2) | fixed: the hash is computed each time, without the varargs array | `identifierHashFollowsChangedFields` (mutable subclass, `HashSet` lookup) |
| E: empty fast paths replaced live views (RVC1 5, RVC2 P2) | fixed: before the first write, advice and obligation getters return a view that reads the map once it exists (removal works through it as before); the handler's resolved maps return a read-only live view that rejects a null key as the concurrent cache did; the empty policy id list is a new modifiable list | `responseViewsKeptBeforeTheFirstWriteSeeLaterWrites`, `emptyPolicyListIsModifiable`, `DefaultEvaluationContextHandlerTest.resolvedMapsKeptBeforeTheFirstBagSeeLaterBags` |
| B and E: null predicates and a null rule accepted on some paths (RVC2 P3) | fixed: `find`, `findFirst`, `find(id, ...)` reject a null predicate first; `findValue` rejects a null filter once an attribute passes the issuer test; `getEvaluationStatus(null)` throws before and after the first write | `nullPredicatesAreRejectedAsBefore`, `nullRuleStatusLookupIsRejected` |
| E: the concurrent first-write test did not assert statuses (RVC1 6, RVC2 P3) | fixed: every rule's status is asserted after the workers finish | `concurrentFirstWritesAreAllKept` |
| F: no test of a repository change during a lookup (RVC2 P3) | added a characterization: a removal that arrives during the lookup leaves the looked-up policy cached, before and after cycle F (pre-existing race, filed as a bug) | `DefaultPolicyReferenceResolverCacheTest.removalDuringALookupLeavesTheLookedUpPolicyCached`: passes on the tree before F and after |

Run: `tests/012-review-fixes-red-green`. A re-check by the reviewers follows the fixes.
