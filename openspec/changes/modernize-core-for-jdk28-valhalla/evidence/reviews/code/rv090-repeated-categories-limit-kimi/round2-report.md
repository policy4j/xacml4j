## Findings

**HIGH** — none.

**MEDIUM** — none.

**LOW-1 (spec text, not code): the dedup case contradicts a scenario as written.**
File: `openspec/changes/modernize-core-for-jdk28-valhalla/specs/request-limits/spec.md`, scenario "More repeats of one category than the limit" (new line ~48): "WHEN a request has 101 `<Attributes>` elements of one category … THEN … Indeterminate, and no individual request is evaluated." The code dedups each category with `ImmutableSet.copyOf` *before* counting (`MultipleResourcesViaRepeatingAttributesHandler.java`, diff lines ~86–90), and your own test `equalRepeatsCountOnce` proves the divergent case: 101 *identical* `<Attributes>` elements expand to 1 request and are evaluated (Permit), not rejected. Concrete failing scenario for the spec as written: send 101 byte-identical `<Attributes>` of category `RESOURCE`; the literal reading of the scenario demands Indeterminate, the engine evaluates once. The security invariant is unaffected (dedup only shrinks the product), and the requirement sentence itself ("the number of individual requests that the repeated categories expand to") stays true. Suggested fix: write "101 *different* `<Attributes>` elements" in that scenario, or add a scenario stating that equal elements count once, mirroring `equalRepeatsCountOnce`. Non-blocking.

## Answers

**1. MEDIUM-1 — closed.** Worst case for one request now, N = `maxExpandedRequests`:
- Handler runs: budget 1; references 1 (resolves ≤ N references, checked pre-loop); repeats ≤ N runs (one per reference), each emitting a running product ≤ N, with at most N+1 individual requests constructed in total — the N+1th is what trips `Checkpoint.countSelectorStep()` (`MultipleResourcesBudgetHandler.java` ~line 158); Checkpoint ≤ N+1 runs; legacy XPath ≤ N; XPath ≤ N.
- Selector steps (requests reaching legacy/XPath handlers): ≤ N, including empty-selection requests, which was the round-1 hole.
- XPath evaluations: ≤ N × C, where C = selector-carrying categories per individual request (a request-size parameter, orthogonal to N and unchanged from D16); each selection is still capped at `maxPathsPerCategory` nodes by the unchanged XPath handler.
- Evaluations: ≤ N (`Budget.requestDecision` throws at N+1, before delegating).
- Results before the post-check: ≤ 2N (≤ N evaluated + ≤ N unevaluated Indeterminates from unresolved references and per-request XPath-limit rejections); the post-check collapses anything > N to the single Indeterminate, so the response carries ≤ N.
The old refs × repeats × selectors multiplication (N² selector steps with zero results, zero evaluations) is now O(N) in every dimension. The N+1 construction overshoot in the repeats handler is the only off-by-one, and it is bounded work, not a bypass.

**2. LOW-1 and LOW-2 — closed as described.** The references handler diff shows only the pre-loop `references.size() > maxExpandedRequests` check; the redundant in-loop count is gone, and the mutation matrix on the final code is coherent: M1 killed by 3 tests, M2 by `moreRequestReferencesThanTheLimitAreIndeterminate`, M3 by `individualRequestsThatSelectNothingAreCounted`, M4 by the two result-count tests. All four LOW-2 tests are present in the diff and their expectations match the implementation's behavior (boundary-inclusive at N, throw at N+1).

**3. Checkpoint is clean.** It is pass-through (no request mutation), counts exactly once per passage (each individual request traverses the linear chain once; XPath's per-node `handleNext` is downstream of it), and throws only when `selectorStepsLeft == 0` — i.e., on the N+1th passage. Requests at exactly N pass (proven by `requestReferencesUpToTheLimitAreEvaluated`, `singleRequestPassesTheSmallestLimit`, `individualRequestsThatSelectNothingAreCounted`'s at-limit cases). Without a `Budget` in the context it is a no-op.

**4. The private exception cannot escape.** It is thrown only from `Checkpoint.countSelectorStep()` (only when the context is a `Budget`, which exists only downstream of the budget handler) and from `Budget.requestDecision()` — both strictly inside the budget handler's `try`. Intervening catch blocks are typed to the three checked exceptions only; the chain entry point is the budget handler itself; everything runs on the calling thread. Re-entry through `Budget.getRequestHandlers()` re-enters the budget handler, which starts a fresh budget and catches its own throw. No path delivers it to PDP-level code.

**5. Other defects / spec fidelity.** Beyond LOW-1 above, the spec matches the code: every new scenario traces to a passing test with the stated counts (`repeatsTimesSelectorsHaveOneLimit`, `responseHasNoMoreResultsThanTheLimit`, `referenceThatDoesNotResolveCountsAsAResult`, etc.), the fixed message and IncludeInResult behavior are reused identically from D16, the overflow comment in the repeats handler is arithmetically correct (running product ≤ N × 2³¹ < 2⁶²), and the known-discard behavior is stated in both the spec and the class Javadoc. The re-entry caveat on `getRequestHandlers()` is documented on the override and no production code exercises it.

VERDICT: APPROVE
