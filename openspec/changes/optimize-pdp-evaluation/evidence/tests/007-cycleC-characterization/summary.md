# 007: cycle C characterization (task 6.1)

Date: 2026-10-27. Oracle GraalVM 25.0.4, Maven 3.9.16 offline. All on today's code, before any
change of `BagOfValues`.

- `xacml-core/src/test/java/org/xacml4j/v30/BagOfValuesCharacterizationTest.java`, 6 tests, all pass:
  iteration groups equal values in first-occurrence order (`[a, b, a, c, b]` iterates
  `[a, a, b, b, c]`; `value()` and `single()` give `a`); `values()` is an unmodifiable `Multiset`
  with counts and multiset equality; equality and hash ignore order and count duplicates; union and
  intersection drop duplicates, `containsAll` ignores counts; double bags keep today's NaN and
  signed-zero results (asserted against `Value.equals`); a Java serialization round trip keeps
  equality and order.
- `xacml-core/src/test/java/org/xacml4j/v30/policy/MatchTest.java`
  `failureOnAGroupedDuplicateComesBeforeALaterMatchingValue`: bag `[2, 3, 2]`, predicate false on
  the first 2 and failing on the second: Indeterminate, and 3 is never tested (an
  insertion-order bag would give Match). MatchTest 5 tests pass.
- Already covered by existing tests: a target whose first `AnyOf` is Indeterminate returns
  Indeterminate without evaluating the next (`TargetTest.testFirstIsIndeterminate`, bug
  `policy4j-yeu3`); the empty bag of an absent attribute with `MustBePresent=false`
  (`AttributeDesignatorTest`).

The serialization test fixes today's round trip; a new field layout breaks reading old streams,
which is the maintainer question of design Open Question 1 (task 6.2 waits).
