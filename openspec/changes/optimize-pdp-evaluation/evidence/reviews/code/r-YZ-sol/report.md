
Reviewed the merged main tree at HEAD `87133237` and both patches, excluding the unrelated `JsonContent` hunks.

**Cycle Y: REJECT — two P2 findings.**

1. **P2 — Previously live response views become permanently empty.**  
   [RootEvaluationContext.java:463](`xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java:463`), also `:468`.

   A combiner can retain the matching-obligations/advice collection before evaluating children. Previously, that collection saw subsequent additions and allowed their removal. It now remains empty. Moving the characterization tests’ reads after the first write hides this compatibility change (`RootEvaluationContextCharacterizationTest.java:212,259`).

   This is **not acceptable as a compatibility-preserving optimization**. The approved Y2 change concerns evaluated policies, separately.

   **Fix:** restore the live map-values behavior and the original before-first-write assertions. Alternatively, an intentional public-contract change needs an explicit maintainer decision and interface documentation.

2. **P2 — The new presence query bypasses an external context’s existing override.**  
   [DescendantEvaluationContext.java:307](`xacml-core/src/main/java/org/xacml4j/v30/policy/DescendantEvaluationContext.java:307`).

   An external `Rule.RuleEvaluationContext` subclass can override `getResolvedDesignators()` with a nonempty map. Its inherited `hasResolvedDesignators()` still queries its `top`, returning false when the root is empty. Descendants below that external context inherit the inconsistency.

   **Fix:** retain direct forwarding for known engine contexts; for an external subclass, derive the answer from its virtual `getResolvedDesignators()`. For example, branch on `isOwn(this)`.

   **Test:** extend the existing external-context compatibility test with that override; assert consistent presence answers on the custom context and below it.

Other Y checks:

- Y2 uses the correct `DefaultPolicyDecisionPoint` logger and approved request/debug condition (`PolicyDecisionPointContextFactory.java:65,186`). Policy identifiers retain their sorted order (`RootEvaluationContext.java:426`).
- The category list copies the existing multimap iteration order. Include-in-result filtering preserves category and attribute order; both attribute-add paths update the flag, and `noAttributes()` resets it (`RequestContext.java:77,344`; `AttributeIndex.java:353`).
- `Result` retains duplicate-category rejection at build, merge-by-id for obligations/advice, and list ordering/duplicates for policy references. `ResponseContext` preserves result order, duplicates, equality, and immutable snapshots (`Result.java:273`; `ResponseContext.java:77`).
- The added outcome tests are pragmatic. The revised retained-view tests require correction with finding 1.

**Cycle Z: APPROVE — no P1–P3 findings.**

- Z1’s paths supply fresh lists from `normalize` or `pairList` (`FunctionSpecBuilder.java:427,487,615`). Invocation receives them after evaluation finishes; retaining an invocation’s final list introduces no later engine mutation. Validated calls still copy, preserving rv009 (`:502,665`; `FunctionInvocationCharacterizationTest.java:208`). Lazy functions still receive unevaluated expressions.
- Z2 preserves rejection of null references, null/empty operand arrays, null operands, and invalid signatures. The runtime boundary retains `syntax-error` and `"Invalid higher-order function arguments"` (`HigherOrderFunctions.java:227,248`). The build-time list overload remains (`:236`).
- The two added message assertions pin public error behavior without mechanism tests.

**Verification:** read-only source/diff review; no edits, builds, tests, or JVMs. Supplied reactor results: core 6,660; PDP 233; conformance 938/12 skipped; JMH 201, green. Runtime reproduction of the findings remains unrun.
