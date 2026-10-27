READ-ONLY CODE REVIEW. Do not write or edit any file, do not run Maven, tests, benchmarks,
profilers, git writes, or br writes. Read source, tests, and recorded evidence only. Project rules:
AGENTS.md. Role: code reviewer from a model family other than the author's (the conductor,
Claude Opus 5.5, wrote these changes). Lane tag RVC2.

Change: openspec/changes/optimize-pdp-evaluation. Read proposal.md, design.md (D5 to D10, and the
review dispositions in evidence/reviews/), specs/pdp-attribute-resolution/spec.md, and tasks.md
groups 4 to 9 first. Then review five optimization cycles; each must keep every decision, status
code, response element, and public behavior unless the design names the change:

A. D5, per-decision memo of request-resolved designator bags (XACML 3.0 section 7.3.5):
   xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java (requestDesignators,
   doDesignatorResolve). Tests: xacml-pdp/src/test/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandlerTest.java
   (the tests added for cycle A and review RVP), RequestAttributeMemoPdpTest.java.
B. D6, loops instead of streams in attribute lookup: xacml-core/src/main/java/org/xacml4j/v30/AttributeIndex.java
   (findValues, hasValues, findValue, attributes, hasIssuer, isOfType, countOfType), Attribute.java
   (valueList), RequestContext.java (getEntity). Test: xacml-core/src/test/java/org/xacml4j/v30/AttributeLookupCharacterizationTest.java.
D. D8, two-argument predicate calls: xacml-core/src/main/java/org/xacml4j/v30/policy/FunctionSpec.java
   (new default invoke(context, arg0, arg1)), xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java
   (eagerPair, doInvoke, evaluateInPlace; LinkedList to ArrayList). Tests:
   xacml-core/src/test/java/org/xacml4j/v30/policy/function/FunctionInvocationCharacterizationTest.java, MatchTest.java.
E. D9, the fixed cost of a decision: SemanticIdentifier.java (cached hash), AttributeIndex.java (single,
   findByIssuer, find, findFirst as loops), RequestContext.java (containsRepeatingCategories,
   getIncludeInResultAttributes), xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java
   (maps created on first write with VarHandle compare-and-set; skip list replaced by a hash map sorted
   on read; static logger), DefaultEvaluationContextHandler.java (caches and cycle stacks created on
   first use), DefaultPolicyDecisionPoint.java (getResolvedAttributes early return),
   PolicyDecisionPointContextFactory.java (one Duration). Tests: RootEvaluationContextCharacterizationTest.java,
   RequestFixedCostCharacterizationTest.java, xacml-pdp/src/test/java/org/xacml4j/v30/pdp/DecisionFixedCostPdpTest.java.
F. D10, policy-level allocation: xacml-core/src/main/java/org/xacml4j/v30/policy/Policy.java (shared empty
   variable map for a policy without variables), xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/DefaultPolicyReferenceResolver.java
   (unbounded Guava caches replaced by ConcurrentHashMap; the size argument was already ignored). Tests:
   PolicyVariableContextCharacterizationTest.java, DefaultPolicyReferenceResolverCacheTest.java.

Look for: a decision, status code, or response element that changes; an iteration order that changes
where a caller can see it (the policy id list, resolved attributes, bag values, advice and obligation
merge); thread-safety holes (lost writes on concurrent first use, unsafe publication, a stack or cache
shared across decisions); exceptions that change type or status; an API change not marked BREAKING;
tests that pass for the wrong reason or do not run the new path; claims in comments that the code does
not keep. Evidence of time and allocation is in evidence/benchmarks/jmh/LEDGER.md when present.

Report: findings most severe first (P1 to P3) with file:line, the smallest fix, the test that proves
it, and "checked" / "not checked"; then one verdict per cycle (A, B, D, E, F): APPROVE, APPROVE WITH
CHANGES, or REJECT.

Write your report between these two lines, each on its own line, exactly as written:
=== BEGIN RVC2 REPORT ===
=== END RVC2 REPORT ===
