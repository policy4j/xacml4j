# Run 077: limits on multiple-decision XPath expansion (bead the.4.50)

Date: 2026-10-07. Ruling: run 061 D16, option (a)
(`builds/070-maintainer-rulings`). Source of the bead: run 063 (security
sweep). JDK: Oracle GraalVM 21.0.6+8.1
(`java version "21.0.6" 2025-01-21 LTS`). Profile text: XACML v3.0
Multiple Decision Profile Version 1.0, Committee Specification 02 (18 May
2014), fetched from OASIS on 2026-10-07, SHA-256 of the HTML file
`939aea16e33a11544f34996f2ef3af3cd2aefb3be911e27c9c107a536c9f0d77`.
Reviews: `reviews/code/rv085-multiple-xpath-limit-codex`,
`reviews/code/rv086-multiple-xpath-limit-grok`.

## Defect

`MultipleResourcesViaXPathExpressionHandler.handle` built
`Sets.cartesianProduct` over the node paths selected by the
`multiple:content-selector` XPaths of the request, with no limit.

| Request | Before | After (default limits) |
| --- | --- | --- |
| one selector, 100 nodes | 100 individual requests | unchanged |
| one selector, 101 nodes | 101 individual requests | one Indeterminate, processing-error, no individual request |
| two selectors, 10 x 10 nodes | 100 individual requests | unchanged |
| two selectors, 11 x 11 nodes | 121 individual requests | one Indeterminate, processing-error, no individual request |
| five selectors, 100 nodes each | `IllegalArgumentException("Cartesian product too large")` thrown out of the handler | one Indeterminate, processing-error |
| a selector that selects no node, with any other selectors | no individual request (or the Guava exception when the other sets are large) | no individual request; not a limit violation |

## Change

| File | Change |
| --- | --- |
| `xacml-pdp/.../pdp/request/MultipleResourcesViaXPathExpressionHandler.java` | `DEFAULT_MAX_PATHS_PER_CATEGORY = 100`, `DEFAULT_MAX_EXPANDED_REQUESTS = 100`; constructor `(int maxPathsPerCategory, int maxExpandedRequests)`, both must be positive. `getCategories` checks the number of node paths right after the XPath is evaluated. `handle` collects the per-category sets, returns no result when one of them is empty, and otherwise multiplies the sizes in a `long` and checks after every factor, before `Sets.cartesianProduct`. Beyond a limit: `Result.indeterminate(Status.processingError().message(LIMIT_EXCEEDED_MESSAGE))` with the IncludeInResult attributes of the request; the detail is logged at WARN. |
| `xacml-pdp/.../pdp/request/MultipleResourcesHandler.java` | Public constructor `MultipleResourcesHandler(int maxPathsPerCategory, int maxExpandedRequests)`; the no-argument constructor uses the defaults. Configure with `PolicyDecisionPointBuilder.requestHandler(new MultipleResourcesHandler(a, b))`. |

Status code: the profile defines no limit. It uses syntax-error for an
invalid `<RequestReference>` and processing-error where a combined decision
cannot be made. The request here is well-formed, so the result is
processing-error. This is an engine choice, not a requirement of the
profile.

## Tests

- New `MultipleResourcesXPathExpansionLimitTest` (13 tests): defaults; at
  the limit and one above for paths and for the product; five categories
  of 100 (no overflow, no Guava exception); an empty selection in either
  order and with 100^5 other combinations (no result, no rejection); the
  path limit still applies next to an empty selection; configured limits;
  limits must be positive; the public chain passes both limits and its
  no-argument form has the defaults; the
  Indeterminate result keeps the IncludeInResult attributes; zero PDP
  calls when a limit is exceeded; the fixed status message.
- Red first: `red-surefire-summary.txt` (limits stored but not enforced:
  10 tests, 6 failures, 1 error). The two empty-selection tests were added
  with the fix of review round 1; they were not run against the round-1
  code.
- Gate: `surefire-summary.txt`, gate 2. Full reactor on JDK 21: 7,774
  tests, 0 failures, 0 errors, 22 skipped. Conformance 937 tests with 12 skipped,
  unchanged; the multiple-decision conformance cases stay below the
  limits.
- The gate tree also held runs 078 (bead the.4.51) and 079 (bead
  the.4.54), not yet committed.

## Spec

New capability `request-limits` (`specs/request-limits/spec.md`,
requirement "Multiple-decision XPath expansion is bounded", four
scenarios), listed in `proposal.md`.
`openspec validate modernize-core-for-jdk28-valhalla --strict` passes.

## Reviews

| Lane | Rounds | Final verdict | Record |
| --- | --- | --- | --- |
| Codex | 2 | APPROVE (round 2) | `reviews/code/rv085-multiple-xpath-limit-codex` |
| Grok grok-4.7 | 1 complete (round 2 packet) | APPROVE | `reviews/code/rv086-multiple-xpath-limit-grok` |

Round 1 of Codex found that an empty selection after large ones was
rejected; fixed before round 2. Each reviewer asked for one more test of
the public chain; both were added after the reviews.

## JMH

NOT APPLICABLE: one comparison per category on a path that runs once per
multiple-decision request.

## Limits

- `content.evaluateToNodePathList(xpath)` runs before the path limit can
  be checked. Its cost grows with the size of the request content, which
  this run does not limit.
- The repeated-categories form of the profile (section 3.3,
  `MultipleResourcesViaRepeatingAttributesHandler`) has the same unbounded
  cross product and runs before this handler in the chain, so the total
  number of individual requests of one request is still not bounded.
  Ruling D16 names the XPath expansion only; tracked as bug
  policy4j-the.4.57 (P1) for a maintainer decision.
- A request that exceeds a limit is logged at WARN once per request,
  with the category id of the request. A client that repeats it can fill
  the log; no rate limit here.
- A `PathEvaluationException` while a selector is evaluated still leaves
  the category unexpanded (unchanged).
