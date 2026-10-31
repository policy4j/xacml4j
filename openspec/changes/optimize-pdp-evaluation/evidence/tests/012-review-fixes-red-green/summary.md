# 012: review fixes, red and green (reviews rv006, rv007)

Date: 2026-10-27. Oracle GraalVM 25.0.4, Maven 3.9.16 offline, release 25.

The tests added for the findings of rv006 (Sol) and rv007 (Terra), run on four kinds of tree:

- Red, the reviewed tree before the fixes: 9 of the new tests fail (7 failures, 2 errors; corrected after re-check rv007, which counted the output), one or more per finding
  (`nullPredicatesAreRejectedAsBefore`, `identifierHashFollowsChangedFields`,
  `attributeKeepsItsSerialVersionUid`, `responseViewsKeptBeforeTheFirstWriteSeeLaterWrites`,
  `nullRuleStatusLookupIsRejected`, `emptyPolicyListIsModifiable`,
  `eagerPairInvocationMayChangeItsArgumentList`, `twoArgumentCallKeepsAnOverriddenVariadicInvoke`,
  `resolvedMapsKeptBeforeTheFirstBagSeeLaterBags`; the strengthened concurrent test and the
  cycle F race characterization pass there, as they test unchanged behavior).
- The tree before each cycle (before B: the tree before cycle A; before D, E, F: the cycle
  snapshots): every test passes, so the tests pin the old behavior.
- Green, the working tree with the fixes: every test passes.

Output: `output.txt`. The reactor and conformance run with the fixes is `013-reactor-review-fixes`.
