# Review: one limit for the multiple-decision expansion

## Findings

### MEDIUM-1 — Step checks are per-invocation, not cumulative: a request can force Θ(limit²) individual-request builds and XPath selections with zero evaluations

**Files/lines:** `MultipleResourcesViaRequestReferencesHandler.java:73` (upfront check counts only references), `MultipleResourcesViaRepeatingAttributesHandler.java:83` (product check restarts from 1 on every invocation), `MultipleResourcesBudgetHandler.java:137-143` (budget counts only `requestDecision`).

**Scenario (default limit 100):** A request with 100 `<RequestReference>` elements, each referencing 100 `<Attributes>` elements of the resource category, each element carrying a `multiple:content-selector` whose XPath matches **no node** (e.g. `/record/absent`).

1. References handler: 100 ≤ 100, passes (`:73`).
2. Per reference, the repeats handler runs fresh: product 100 ≤ 100, passes (`:83`), builds 100 individual requests.
3. Per built request, the XPath handler performs the selection (real work over attacker-supplied XML), selects 0 nodes, returns an empty result list — no `handleNext`, no `requestDecision`, no result.
4. Nothing ever trips: budget untouched (0 evaluations), references in-loop check sees `results.size()` stay 0 (`:99`), post-check sees 0 (`MultipleResourcesBudgetHandler.java:80`).

Total: **10,000 RequestContext builds + 10,000 XPath selections**, 0 evaluations, empty response — 100× the apparent intent of "one request has one limit", entirely inside the stated invariants. Non-empty selections don't do this (each consumes budget and the exception unwinds the loops, ≤ limit+1 selections); over-limit selectors trip the in-loop check at ~2·limit results. Empty selections are the unique worst case. The same Θ(limit²) bound covers the legacy-XPath rewrite checks and the per-reference `ImmutableSet.copyOf` work. Operator-raised limits square accordingly.

**Fix:** Count *built* individual requests in the same per-request budget, not just evaluated ones — e.g. have `Budget` implement a package-private counting interface that the references/repeats handlers invoke once per individual request before `handleNext`, throwing `BudgetExceededException` at limit+1. Then builds ≤ limit+1 and selections ≤ limit+1 per request. Alternative if the project accepts the bound: document the quadratic residual explicitly (like the accepted discarded-evaluations note) and add an operator note. Either way, pin it with a test of the empty-selector multiplier case; the spec should also state what an empty selection counts as (currently unspecified — see Q8).

### LOW-1 — The references in-loop check is observationally redundant with the budget post-check; the reported M3 mutation kill is not reproducible from the shown code

**Files/lines:** `MultipleResourcesViaRequestReferencesHandler.java:99` vs `MultipleResourcesBudgetHandler.java:80`.

**Scenario:** `resultsOfRequestReferencesHaveOneLimit` under mutation M3 ("results of the references are not counted together"). Without cumulative in-loop counting, the loop over 3 references × 2 over-limit-selector repeats simply runs to completion, accumulating 6 Indeterminate results; the budget post-check (`:80`) then produces the identical single Indeterminate. Same response, same evaluation count (0), `verifyNoInteractions` still passes — the test should be **masked**, yet the mutation run reports it as killed. Either the mutation run predates the post-check, or the test pins something not visible in the diff. The in-loop check's only real effects today: skipping processing of the remaining references (work), a different warn line, and keeping the transient results list at ~2·limit instead of limit².

**Fix:** Re-run the mutation analysis on the final code. If M3 survives, either pin what the check actually guarantees (e.g. verify via a mock that references after the trip point are never resolved) or remove the check as redundant.

### LOW-2 — Missing tests

**File:** `MultipleResourcesRepeatedCategoriesLimitTest.java`

1. **Unresolved-reference counting.** Spec: "a request reference that does not resolve … SHALL count as one result". Every test reference resolves; the catch path's contribution to the count is unpinned. Add: limit 4; 2 references each expanding to 2 evaluated requests + 1 unresolvable reference → 5 results → single Indeterminate.
2. **Dedup non-rejection.** 101 *equal* `<Attributes>` of one category collapse to a set of 1 and must still expand (not be rejected). A mutation counting `attributes.size()` instead of `repeats.size()` at `MultipleResourcesViaRepeatingAttributesHandler.java:82-83` survives the current suite and would introduce exactly that regression.
3. **Appended handler inside the budget.** A handler appended via `setNext` after `MultipleResourcesHandler` receives the `Budget` wrapper (per the stated chain semantics); nothing pins that its `requestDecision` calls are counted.
4. **Combined-decision interaction.** A `combinedDecision` request over the limit must return the single Indeterminate unchanged through `postProcessResults`; unpinned in this class.

---

## Answers

**1. Over-limit evaluations, results, overflow, or throws?** No path found. Evaluations ≤ limit: `Budget.requestDecision` (`MultipleResourcesBudgetHandler.java:137-143`) decrements before delegating and throws before the limit+1-st call reaches the PDP. Results ≤ limit: the post-check at `:80` sees the whole downstream chain's output, and `postProcessResults` only ever combines. Overflow: `expandedRequests` is `long`; entering each multiplication it is ≤ limit ≤ 2³¹−1, multiplied by a set size ≤ 2³¹−1, so the peak is (2³¹−1)² < 2⁶³ — the comment is accurate, and because the product is ≤ limit before `Sets.cartesianProduct` is called, the Guava `IllegalArgumentException` is unreachable. Boundaries are inclusive (`>` everywhere) and pinned (10×10 at limit 100; 100 references at limit 100). No request-triggered throw escapes `handle`.

**2. Can `BudgetExceededException` escape or be caught elsewhere?** No. It is a `private static final` nested class — no other file can name it in a `catch`; the package's catch blocks handle only `RequestSyntaxException`, `PathEvaluationException` and the XPath handler's checked `ExpansionLimitExceededException`, none a supertype. It is thrown only from `Budget.requestDecision`, reachable only inside the `try` at `MultipleResourcesBudgetHandler.java:75-85`. Even a misbehaving application-appended handler that caught `RuntimeException` could not exceed the limit: every subsequent `requestDecision` re-throws before delegating, so the real PDP still sees ≤ limit calls.

**3. Is non-evaluation work bounded?** Bounded, but at a *multiple* of the limit — this is MEDIUM-1. Worst case: Θ(limit²) individual-request builds + Θ(limit²) XPath selections with **0 evaluations** and an empty response (limit references × limit repeats × empty selectors; 10,000 of each at defaults). Secondary case: over-limit selectors cost 1 selection + 1 Indeterminate per built request and trip the in-loop check at ~2·limit. Non-empty selections are linear (≤ limit+1) because the budget exception unwinds all loops.

**4. Are in-limit, profile-legal requests rejected or answered differently?** No change found. The product check uses the same `ImmutableSet` copies that feed the cartesian product, so dedup semantics are preserved; category order and result assembly are unchanged; per-sub-request Indeterminate-and-continue semantics (D16) are preserved (pinned by `selectorStepBeyondTheLimitIsIndeterminateForItsRequest`); empty selections still yield 0 results. Over-limit requests now get the single Indeterminate where they previously expanded — that is the intended, spec'd change, including the "evaluated up to the limit, then Indeterminate" case the spec's own scenarios describe.

**5. Is the wrapper complete and safe?** Complete: all ten `PolicyDecisionPointContext` methods are overridden, and the compiler enforces this (an added interface method breaks the build; a default method would still dispatch `requestDecision` virtually to the counted override). One `Budget` per `handle` call; `left` is mutated only on the handling thread per the stated execution model; concurrent requests get independent budgets (pinned by `everyRequestHasTheWholeLimit`). One latent remark, not a defect today: `getRequestHandlers()` (`:170-173`) delegates to the real context, so any *future* code that re-enters the chain through it would start a fresh budget — no such caller exists per the stated facts; a comment on that override would help.

**6. Data leaks?** None. The result carries only the fixed `LIMIT_EXCEEDED_MESSAGE` and the request's own IncludeInResult attributes (echoed to the caller that supplied them, per the profile). All three warn lines log only the configured limit value — an integer from operator configuration, no request content. The empty stack trace on the control-flow exception is fine since it is never logged.

**7. Do the tests pin the invariants?** Mostly yes: boundary off-by-ones (at-limit expansion), no-evaluation-before-rejection (`verifyNoInteractions`), per-request budget reset, overflow safety, feature count, and the configurable limit are all pinned, and M1/M2 kills are credible. Gaps: LOW-2 (unresolved-reference counting, dedup non-rejection, appended-handler, combined-decision) and LOW-1 (the M3 kill contradicts the shown code — re-run the mutation analysis).

**8. Does the spec say what the code does?** Yes for the enforced invariants — every scenario traces to the code paths above, including the "6 evaluated, then Indeterminate" totals. Two gaps if MEDIUM-1 stands: the spec is silent on what an empty selector selection counts as (it is the zero-cost multiplier), and "One request has one limit on individual requests" reads as a whole-request bound while the code enforces it only for evaluations and final results, not for built requests or selections.

## Verdict

The core contract holds — evaluations and results are hard-capped, nothing escapes, nothing leaks — but this change exists to bound request-driven expansion work, and a crafted request still forces the square of the limit in XPath selections while evaluating nothing and returning empty (MEDIUM-1), with a cheap, local fix available; the mutation-evidence inconsistency (LOW-1) should also be resolved before merge.

VERDICT: REQUEST_CHANGES
