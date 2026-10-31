# rv008: re-check of the review fixes (Codex GPT-6.1 Terra)

Date: 2026-10-27. Read-only NTM lane. Packet `packet.md`; report `report.md`.

Verdicts: A APPROVE; B APPROVE; D APPROVE; E REJECT; F APPROVE WITH CHANGES. RESOLVED: variadic override,
modifiable pair list, stale cached hash, null predicates and null rule, concurrent status assertions,
`Attribute` serialVersionUID.

## Disposition (conductor, 2026-10-27)

| Finding | Disposition | Proof |
| --- | --- | --- |
| P2 E: a kept `entrySet()` of the resolved map was detached before the cache existed; `AbstractMap` edge behavior differed | fixed: asking for the view creates the cache and returns the unmodifiable wrapper of the real concurrent map, exactly as before cycle E (one map per decision when the view is read) | `DefaultEvaluationContextHandlerTest.resolvedMapsKeptBeforeTheFirstBagSeeLaterBags` now keeps the entry set and checks `remove` and `containsValue(null)`; passes before E and after the fix |
| P3 E: the array-free hash threw for a null field set by a subclass | fixed: `Objects.hashCode` per field, the same value as `Objects.hash` | `identifierHashFollowsChangedFields` checks a null `abbreviatedId` against `Objects.hash`; passes on the tree before cycle A and after |
| P3 F: the race test ignored its latch and did not model the repository change | fixed: both waits are asserted; the repository's content is removed before the notification and asserted afterwards | `removalDuringALookupLeavesTheLookedUpPolicyCached`; passes before F and after |
| run 012 red count | corrected: 9 tests (7 failures, 2 errors) | `tests/012-review-fixes-red-green/summary.md` |

Runs: `tests/014-recheck-fixes`, `tests/015-reactor-recheck-fixes`.
