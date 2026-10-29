# Review Report: `R-C2AQ-argon` (Codex Terra's `FIX-CYCLE2` and Codex Astra's `AQ`)

- **Reviewer**: Lane `agyx_1` (Gemini Argon)
- **Date**: 2026-10-28
- **Saved Report**: [REPORT-R-C2AQ-argon.md](`.ntm/worktrees/xacml4j--batch2/agyx_1/REPORT-R-C2AQ-argon.md`)
- **Reviewed Artifacts**:
  - `FIX-CYCLE2`: [PATCH-CYCLE2.diff](`.ntm/worktrees/xacml4j--batch2/agyx_1/PATCH-CYCLE2.diff`), [REPORT-FIX-CYCLE2-terra.md](`.ntm/worktrees/xacml4j--batch2/agyx_1/REPORT-FIX-CYCLE2-terra.md`)
  - `AQ`: [PATCH-AQ.diff](`.ntm/worktrees/xacml4j--batch2/agyx_1/PATCH-AQ.diff`), [REPORT-I-AQ-astra.md](`.ntm/worktrees/xacml4j--batch2/agyx_1/REPORT-I-AQ-astra.md`)

---

## Part 1: `FIX-CYCLE2` (Codex Terra)

### Verdict: **APPROVE WITH CHANGES** (`1 P1`, `0 P2`, `0 P3`)

### Findings

#### `P1-1`: `isEvaluating`, `setEvaluating`, `isPolicyCyclic`, and `isPolicySetCyclic` walk `DescendantEvaluationContext` via `getParentContext()` instead of `getParent()`, skipping contexts wrapped by `StagedResponses` and failing all 8 cases of `aPolicyReferenceReenteredInsideItsPolicyReturnsAProcessingError` on the merged main tree

- **Location**:
  - [`PolicyIDReference.java:106-122`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/PolicyIDReference.java`) (`isEvaluating`, `setEvaluating`) and [`PolicyIDReference.java:170-181`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/PolicyIDReference.java`) (`isPolicyCyclic`)
  - [`PolicySetIDReference.java:106-122`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySetIDReference.java`) (`isEvaluating`, `setEvaluating`) and [`PolicySetIDReference.java:155-166`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySetIDReference.java`) (`isPolicySetCyclic`)
  - Interacts with [`BaseCompositeDecisionRule.StagedResponses.getParentContext`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java`) and [`BaseCompositeDecisionRule.combine`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java`)
- **Failure**:
  - `FIX-CYCLE2` was tested in `cod_1`, which did not yet contain `FIX-AN` (`review R-AN`). On the merged main tree with `FIX-AN`, a `Policy` or `PolicySet` enclosed in a `PolicySet` that uses a custom combining algorithm (`!hasBuiltInCombiningAlgorithm()`) wraps its `PolicyDescendantEvaluationContext` / `ReferencedPolicyContext` (or `PolicySet` equivalent) in `new StagedResponses(context)` before invoking `combiningAlgorithm.combine(staged, ...)`.
  - [`StagedResponses.getParentContext()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java`) returns `getParent().getParentContext()`, intentionally skipping `StagedResponses.getParent()` (the wrapped `PolicyDescendantEvaluationContext` / `ReferencedPolicyContext` / `PolicySetDescendantEvaluationContext` / `ReferencedPolicySetContext`).
  - Meanwhile, [`DescendantEvaluationContext.getCurrentPolicy()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/DescendantEvaluationContext.java`), [`getCurrentPolicySet()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/DescendantEvaluationContext.java`), [`getCurrentPolicyIDReference()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/DescendantEvaluationContext.java`), and [`getCurrentPolicySetIDReference()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/DescendantEvaluationContext.java`) walk `((DescendantEvaluationContext) c).parent` (`getParent()`), so they DO see the wrapped context.
  - Because `isEvaluating`, `setEvaluating`, `isPolicyCyclic`, and `isPolicySetCyclic` walk `c = c.getParentContext()` instead of `((DescendantEvaluationContext) c).getParent()`:
    1. **Unobserved decisions (`observed = false`)**: `staged` wraps a single merged `ReferencedPolicyContext` (or `ReferencedPolicySetContext`). `staged.getParentContext()` skips `ReferencedPolicyContext` and jumps to `ctxRoot`. Consequently, `isEvaluating(staged)` returns `false` while `staged.getCurrentPolicyIDReference() == this` returns `true`, so `isReferenceContext(staged)` returns `true` and `reference.createContext(staged)` returns `staged` unchanged!
       - When `matchFirst = false`, `reference.evaluate(staged)` recurses infinitely into `policy.evaluate(staged)` and throws `StackOverflowError`.
       - When `matchFirst = true`, `reference.isMatch(staged)` returns `MatchResult.MATCH` instead of `MatchResult.INDETERMINATE`, yielding `Decision.PERMIT` instead of `Decision.INDETERMINATE`.
    2. **Observed decisions (`observed = true`)**: `staged` wraps `PolicyDescendantEvaluationContext`, whose parent is `PolicyIDReferenceEvaluationContext`. `staged.getParentContext()` skips `PolicyDescendantEvaluationContext` and lands on `PolicyIDReferenceEvaluationContext`. `isEvaluating(staged)` sees `PolicyIDReferenceEvaluationContext.evaluating == true`, but `isPolicyCyclic(target, staged, true)` never sees `PolicyDescendantEvaluationContext` and returns `false`, so `createContext` returns `invalidContext(staged)` (`StatusCodeId.SYNTAX_ERROR`) instead of `cyclicContext(staged)` (`StatusCodeId.STATUS_PROCESSING_ERROR`).
  - Running `ReferenceContextPdpTest` on the merged main tree fails all 8 parameterized invocations of [`ReferenceContextPdpTest.aPolicyReferenceReenteredInsideItsPolicyReturnsAProcessingError`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/test/java/org/xacml4j/v30/pdp/ReferenceContextPdpTest.java`) (`[1]` and `[3]` throw `StackOverflowError`; `[2]`, `[4]`, `[6]`, `[8]` fail with `syntax-error` instead of `processing-error`; `[5]` and `[7]` fail with `PERMIT` instead of `INDETERMINATE`).
- **Fix**:
  - In `PolicyIDReference` (`isEvaluating`, `setEvaluating`, and the first loop of `isPolicyCyclic`) and `PolicySetIDReference` (`isEvaluating`, `setEvaluating`, and the first loop of `isPolicySetCyclic`), step across `DescendantEvaluationContext` nodes with `d.getParent()` / `((DescendantEvaluationContext) c).getParent()` instead of `c.getParentContext()`, matching [`DescendantEvaluationContext`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/DescendantEvaluationContext.java`) and [`RootEvaluationContext.closeStaging`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`):
    ```java
    // In PolicyIDReference:
    private boolean isEvaluating(EvaluationContext context){
    	for(EvaluationContext c = context; c instanceof DescendantEvaluationContext d; c = d.getParent()){
    		if(c instanceof PolicyIDReferenceEvaluationContext r && r.reference() == this) return r.evaluating;
    		if(c instanceof ReferencedPolicyContext r && r.reference() == this) return true;
    	}
    	return false;
    }

    void setEvaluating(EvaluationContext context, boolean evaluating){
    	for(EvaluationContext c = context; c instanceof DescendantEvaluationContext d; c = d.getParent()){
    		if(c instanceof PolicyIDReferenceEvaluationContext r && r.reference() == this){
    			r.evaluating = evaluating;
    			return;
    		}
    		if(c instanceof ReferencedPolicyContext r && r.reference() == this) return;
    	}
    }

    private static boolean isPolicyCyclic(CompositeDecisionRule target, EvaluationContext context,
    		boolean includeCurrent){
    	EvaluationContext top = DescendantEvaluationContext.topOf(context);
    	for(EvaluationContext c = context; c != top; c = ((DescendantEvaluationContext)c).getParent()){
    		if(c instanceof Policy.PolicyDescendantEvaluationContext p && p.policy() == target) return true;
    	}
    	EvaluationContext first = includeCurrent || context != top ? top : top.getParentContext();
    	for(EvaluationContext c = first; c != null; c = c.getParentContext()){
    		if(c.getCurrentPolicy() == target) return true;
    	}
    	return false;
    }
    ```
    (And the symmetric change in `PolicySetIDReference.isEvaluating`, `setEvaluating`, and `isPolicySetCyclic`; also check [`BaseCompositeDecisionRule.isEnclosed`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java`), which likewise walks `while (c instanceof DescendantEvaluationContext d) { c = d.getParent(); ... }` so a `StagedResponses` on a root `PolicySet` with a custom combining algorithm cannot hide its `PolicySetDescendantEvaluationContext` from a direct child.)

---

### Verification of Other `FIX-CYCLE2` Points (`SUPPORTED` — source inspection + Maven test run)

1. **`R-CYCBENCH` findings**:
   - **`P1-1` (root, inline, and versioned `PolicySet` back edges)**: All 16 cases of [`ReferenceContextPdpTest.aReferenceToAnActivePolicySetDoesNotReevaluateIt`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/test/java/org/xacml4j/v30/pdp/ReferenceContextPdpTest.java`) (`"inline permit"`, `"inline deny"`, `"versions"`, `"equal instances"`, across `pinned={false, true}` and `observed={false, true}`) and all 4 cases of [`ReferenceContextPdpTest.aRootSelfReferenceFailsBeforeMatchingItsSibling`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/test/java/org/xacml4j/v30/pdp/ReferenceContextPdpTest.java`) pass on the merged main tree.
   - **`P2-1` (context size)**: `private boolean evaluating` is removed from both [`ReferencedPolicySetContext`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySetIDReference.java`) and [`ReferencedPolicyContext`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/PolicyIDReference.java`), restoring `ReferencedPolicySetContext` to **32 B** under 12-byte object headers (and `ReferencedPolicyContext` to **32 B** under `-XX:+UseCompactObjectHeaders`).
2. **Identity walk cost**:
   - Walking `((DescendantEvaluationContext) c).getParent()` to `top` (`DescendantEvaluationContext.topOf(context)`) and comparing `p.policySet() == target` / `p.policy() == target` uses only direct field reads and reference equality (`==`), with zero allocations.

---

## Part 2: `AQ` — Fixed Objects of the Request Path (Codex Astra)

### Verdict: **APPROVE** (`0 P1`, `0 P2`, `0 P3`)

### Verification Summary (`SUPPORTED` — source inspection + Maven test run)

1. **Equivalence with the handler chain for every request shape**:
   - **Multiple Decision Profile (v3.0 CS02 §§2–6) and v2.0 Multiple Resource Profile (§§2.2.2–2.2.3)**:
     - [`RequestContext`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/RequestContext.java`) computes `multipleContentSelector`, `legacyContentSelector`, and `requiresExpansion` during construction in the existing `categories` loop:
       - `multipleContentSelector`: `entity.hasValues("urn:oasis:names:tc:xacml:3.0:profile:multiple:content-selector", XacmlTypes.XPATH, null) || entity.hasValues("urn:oasis:names:tc:xacml:3.0:multiple:content-selector", XacmlTypes.XPATH, null)`, matching [`MultipleResourcesViaXPathExpressionHandler`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java`) (CS02 §3.2).
       - `legacyContentSelector`: `attr.getCategoryId().equals(CategoryId.RESOURCE) && entity.hasValues("urn:oasis:names:tc:xacml:1.0:resource:resource-id", XacmlTypes.XPATH, null)`, matching [`MultipleResourcesViaXPathExpressionLegacyHandler`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionLegacyHandler.java`) when `!request.containsRepeatingCategories()` (v2.0 Multiple Resource Profile §2.2).
       - `requiresExpansion`: `!requestReferences.isEmpty() || entities.size() != entities.keySet().size() || multipleContentSelector || legacyContentSelector`, covering request references (CS02 §3.4), repeated categories (CS02 §3.3), and both XPath selector profiles.
     - [`DefaultPolicyDecisionPointContextFactory.canHandleDirectly`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PolicyDecisionPointContextFactory.java`) checks `request != null && request.getClass() == RequestContext.class && !request.requiresExpansion() && !request.isCombinedDecision() && requestHandlers != null && requestHandlers.canHandleSingleRequestDirectly()`.
       - Checking `request.getClass() == RequestContext.class` prevents bypassing custom `RequestContext` subclasses.
       - Checking `!request.isCombinedDecision()` keeps combined-decision requests (CS02 §4) on [`MultipleResourcesHandler.postProcessResults`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesHandler.java`).
       - [`RequestContextHandlerChain.canHandleSingleRequestDirectly`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/main/java/org/xacml4j/v30/spi/pdp/RequestContextHandlerChain.java`) checks exact classes (`getClass() == RequestContextHandlerChain.class`, `getClass() == MultipleResourcesHandler.class`) and checks `!((AbstractRequestContextHandler) handlers.get(handlers.size() - 1)).hasNext()` on every call so any custom handler or handler appended after PDP construction via `setNext` stays on the handler-chain path.
       - [`MultipleResourcesBudgetHandler`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesBudgetHandler.java`) enforces `maxExpandedRequests > 0` at construction, so a single unexpanded request never exceeds the budget.
   - **`IncludeInResult`, decision cache, audit, and decision counters**:
     - Both the direct path and the handler-chain path invoke [`DefaultPolicyDecisionPoint.decide(context, request, number, observation, correlation)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java`), which performs identical cache lookup/store, `decisionAuditor.audit(this, r, request)`, `incrementDecisionCounters(...)`, and `createResult(...)` with `request.getIncludeInResultAttributes()`.
2. **Shared configuration context (`singleRequestContext`)**:
   - Created once per PDP in [`DefaultPolicyDecisionPoint`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java`):
     `factory.createContext((context, request) -> decide(context, request, 1, DecisionObservation.none(), factory.nextCorrelation()), 0)`.
   - **Thread safety**: `singleRequestContext` holds no mutable per-request state; all getters delegate to immutable fields of `DefaultPolicyDecisionPointContextFactory`, and `createEvaluationContext` creates a fresh per-call `DefaultEvaluationContextHandler` and `RootEvaluationContext`.
   - **Per-call correlation IDs**: Each direct call acquires its own `factory.nextCorrelation()` (`correlationSequence.incrementAndGet()`) on the call stack and passes it to `decide(..., correlation)`. [`getCorrelationId(context, correlation)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java`) formats `factory.getCorrelationId(correlation)` only when `LOG.isDebugEnabled()` is enabled. `singleRequestContext` never escapes `DefaultPolicyDecisionPoint`.
   - **Per-call state formerly held by wrappers**: `NumberedDecisions` (`number` is always `1` for a single request) and `Budget` (`maxExpandedRequests >= 1` for 1 request) are not needed on the direct path.
3. **`ResponseContext.of(Result)` sharing and immutability**:
   - [`ResponseContext.results`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/ResponseContext.java`) is now `private final ImmutableList<Result> results;`. Neither `ResponseContext`, `Result`, nor `Status` exposes any mutator or modifiable collection.
   - [`ResponseContext.single`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/ResponseContext.java`) shares `PERMIT`, `DENY`, or `NOT_APPLICABLE` only when `result.getStatus() == plain.results.get(0).getStatus()` (the shared `Result.OK` instance, excluding any `Status` with a message, detail, or error) and all five collection/map properties (`includeInResultAttributes`, `obligations`, `associatedAdvice`, `policyIdentifiers`, `resolvedAttributes`) are empty.
4. **New public API**:
   - [`RequestContext.requiresExpansion()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/RequestContext.java`), [`RequestContext.containsMultipleContentSelector()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/RequestContext.java`), [`RequestContext.containsLegacyContentSelector()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/RequestContext.java`), [`ResponseContext.of(Result)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/ResponseContext.java`), and [`RequestContextHandlerChain.canHandleSingleRequestDirectly()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/main/java/org/xacml4j/v30/spi/pdp/RequestContextHandlerChain.java`) have clear, fluent names and concise Javadoc contracts.
5. **Tests**:
   - [`SingleRequestPathPdpTest`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/test/java/org/xacml4j/v30/pdp/SingleRequestPathPdpTest.java`) (10 test executions) and [`ResponseContextTest`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/test/java/org/xacml4j/v30/ResponseContextTest.java`) (3 tests, 10 single-result shapes) all pass and pin the outcomes.
