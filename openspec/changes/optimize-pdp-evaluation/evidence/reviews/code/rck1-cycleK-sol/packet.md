READ-ONLY CODE REVIEW (a new packet in this pane; same rules: no file writes, no Maven, tests, benchmarks,
git or br writes; no GitHub connector, no sign-in). Lane tag RCK1. Author of the change: Claude (another
model family).

Change: openspec/changes/optimize-pdp-evaluation, cycle K, design D17 ("Cycle K: context queries in one
loop, one Optional per attribute", including the "As built" list) in design.md. Review the uncommitted diff
of these files against HEAD (git diff HEAD -- <file>; the other uncommitted files are cycles I and J,
already reviewed):

- xacml-core/src/main/java/org/xacml4j/v30/policy/DescendantEvaluationContext.java (top() loop, final
  forwarders, answered queries by a type test per level)
- xacml-core/src/main/java/org/xacml4j/v30/policy/Rule.java, Policy.java, PolicySet.java,
  PolicyIDReference.java, PolicySetIDReference.java (overrides replaced by final accessors)
- xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java (isEnclosed walks parent
  fields)
- xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java (request memo keeps the
  Optional)
- Tests: xacml-core/src/test/java/org/xacml4j/v30/policy/ContextChainQueryCharacterizationTest.java (new),
  xacml-pdp/src/test/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandlerTest.java
  (repeatedRequestDesignatorGivesTheSameOptional). Evidence: evidence/tests/033-cycleK-red,
  034-cycleK-green.

Check in particular:
1. Every method made final and walked: was it a pure forwarder at HEAD for every subclass, including the
   anonymous extended-Indeterminate contexts (Rule, Policy, PolicySet) and StagedResponses? Does any level
   answer one of them itself (for example status, staging, TTL, clock)?
2. The answered queries: does the nearest instance of the answering class give exactly what hop-by-hop
   dispatch gave, for every chain the engine builds (references that fail to resolve, extended
   Indeterminate contexts, staging contexts, a foreign context above or inside the chain)?
3. isEnclosed: is the parent-field walk equivalent to the getParentContext() walk in every chain?
4. The Optional memo: same results, same first-bag-wins rule under concurrency, no change for information
   point results, absence, or failures.
5. API: the EvaluationContext interface is unchanged; Rule.RuleEvaluationContext is public and non-final,
   and the walked methods are now final. Is the note in D17 accurate and sufficient?
6. Gaps in the tests.

Report: findings most severe first (P1 to P3) with file:line, the smallest fix, and the test; then a
verdict: APPROVE, APPROVE WITH CHANGES, or REJECT.

Write your report between these two lines, each on its own line, exactly as written:
=== BEGIN RCK1 REPORT ===
=== END RCK1 REPORT ===
