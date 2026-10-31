# RV214 — Cycle AK / D43 independent review

**Verdict: APPROVE (static code review).** Reviewed the unchanged main checkout at `759d90a6c75510d55127d8e22870f9edd985c991`; the review report is the only file written in this worktree. No build or tests were run.

## Hypotheses checked

1. **`Category.resolve` preserves lookup outcomes — supported by source.** At `xacml-core/src/main/java/org/xacml4j/v30/Category.java:83-88`, a key whose category is unequal returns `Optional.empty()`; a matching key delegates to `entity.resolve(key)`. `AttributeIndex.resolve(AttributeDesignatorKey)` at `xacml-core/src/main/java/org/xacml4j/v30/AttributeIndex.java:351-369` resolves the same attribute id, type, and issuer and returns the optional result, retaining only present bags. Thus matching categories return the entity's bag, mismatched categories return empty, and a matching category with an absent attribute returns empty. The check uses `equals`, so equal but non-identical `CategoryId`s match. The D43 diff changes only the Entity overload from scalar arguments to the key overload; it does not change the category check.

2. **Category adds no memo owner or retention path — hypothesis refuted.** `Category` owns only its `Entity` reference (`Category.java:52-59`); the bag memo is the transient map on the entity's `AttributeIndex` (`AttributeIndex.java:75`, `351-369`). Category delegates directly at line 88. It adds no category-scoped map or second alias path beyond the AttributeIndex behavior covered by RV209-ASTRA-1.

3. **D43 serialization shape is unchanged; round-trip behavior is not established.** The pre-AK `Category` source in the first-parent version of commit `a2da1658` has four instance fields: `refId`, `cachedRef`, `categoryId`, and `entity`. The current class retains those fields; the two later cache fields are `transient` (`Category.java:52-59`). Its explicit `serialVersionUID` at line 48 records the pre-AM default value. D43 itself adds no serialized state. I did not run serialization. A deserialized Category's resolution behavior therefore is not runtime-verified; static inspection also shows that `Entity` contains an `AttributeIndex`, whose class declaration does not implement `Serializable` (`xacml-core/src/main/java/org/xacml4j/v30/AttributeIndex.java:51`). Ordinary Category round-trip serialization appears unsupported before D43, so this is not a regression attributable to AK.

4. **BENCHMARKS.md's compact-header guidance agrees with D12 — supported.** D12 (`design.md:2118-2137`) keeps each code-cycle pair on the same build inputs and flags; the later batch rule compares before/after batch snapshots. `docs/ai/BENCHMARKS.md:88-99` says those pairs retain default headers and measures compact headers as a separate option pair, which keeps cycle history comparable. The documented one-fork smoke is not evidence of the separate controlled pair; the gate still needs its recorded batch pair.

## Existing coverage and remaining gaps

`xacml-core/src/test/java/org/xacml4j/v30/AttributeLookupCharacterizationTest.java:128-141`, `categoryResolvesADesignatorOfItsCategoryThroughItsEntity`, pins a found bag for the matching category and empty for a mismatch. The adjacent entity test (`:101-126`) covers absent values and memo behavior at the Entity level.

Two narrow wrapper-level cases are not directly pinned:

- **Equal, non-identical category ids:** `CategoryIdTest.java:43-48` proves two parsed custom ids compare equal, but does not pass one to `Category.resolve` while the Category owns the other. A future identity comparison at `Category.java:84` would make a valid custom-category designator return empty. Minimal test: construct Category and key with separate `CategoryId.parse("urn:test:category").orElseThrow()` results and assert the found bag.
- **Absent attribute through Category:** the entity-level empty lookup is covered, but `Category.resolve` has no direct matching-category/missing-id assertion. A wrapper regression that returned an unrelated empty/available result would not be caught there. Minimal test: resolve a missing id through a matching Category and assert `Optional.empty()`.

These are coverage additions, not current defects: the inspected source returns the expected outcomes in both cases. No Category serialization round-trip test exists; ordinary serialization's pre-existing `AttributeIndex` limitation above prevents treating that as a D43-specific coverage failure.

## Not verified

- No tests, builds, JMH, or serialization round trip were executed; this is source and test inspection only.
- The documented compact-header smoke and D12 batch pair were not independently rerun or audited beyond the cited source text.
- No integrated reactor or conformance gate was run.
