No confirmed defects in the supplied diff.

1. **Both expansion limits hold.** Before each multiplication, the running product is at most `Integer.MAX_VALUE`; the next factor is also at most `Integer.MAX_VALUE`. Their product fits in `long`. Any product above the configured limit is rejected before constructing or iterating the Cartesian product. Empty selections return before multiplication, after all applicable path-limit checks. Nonpositive limits are rejected.

2. **Some work precedes the checks.** XPath evaluation creates the complete path list before checking its size. All per-category sets are collected before checking the product. That work is bounded by request content and the per-category cap under the supplied facts; no Cartesian-product construction or individual-request evaluation precedes the product check.

3. **The rejection result preserves the shown error-path behavior.** Its status message is fixed, and it copies `IncludeInResult` attributes exactly as the syntax-error path does. Those attributes intentionally contain request data. The WARN detail includes the request-supplied category ID; it does not appear in the status message.

4. **The tests cover the principal boundaries:** 100 accepted, 101 paths rejected, product 100 accepted, excessive product rejected, integer overflow, configurable limits, empty selections, attribute preservation, and zero PDP interactions on rejection. One missing test is public-chain propagation of `maxExpandedRequests`: `new MultipleResourcesHandler(100, 2)` with three selected nodes should return the limit result with zero PDP calls. The existing public-chain test exercises only the path limit.

5. **The repeated-categories limit is a separate outstanding defect.** This change explicitly bounds XPath expansion. It does not establish a bound across every multiple-decision mechanism.

Verification was limited to the supplied diff and facts. The reported 7,757-test reactor pass was not independently executed.

VERDICT: APPROVE
codex exit=0
