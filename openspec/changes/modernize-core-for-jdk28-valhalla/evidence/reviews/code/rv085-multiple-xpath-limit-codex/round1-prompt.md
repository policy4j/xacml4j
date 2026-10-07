# Adversarial code review: limits on multiple-decision XPath expansion (xacml4j)

You are an independent reviewer. Do not use tools. Do not edit files. Answer
in one message. Review only the diff below. It is the complete diff of this
change. The full reactor passes on it: 7,755 tests, 0 failures, 0 errors, 22 skipped; the conformance
suites (which include the multiple-decision cases) are unchanged.

## Project

xacml4j is a Java 21 implementation of OASIS XACML 2.0/3.0. A PDP has a
chain of request handlers. `MultipleResourcesViaXPathExpressionHandler`
implements section 3.2 of the XACML v3.0 Multiple Decision Profile: a
category with a `multiple:content-selector` attribute (an XPath over the
`Content` of that category) stands for one individual request per selected
node; several such categories give the cross product.

## Defect (security sweep, request-driven denial of service)

The handler built `Sets.cartesianProduct` over the node paths that
request-supplied XPaths select, with no limit on the paths per category or
on the product. A request with `//*` in three or four categories expanded
to about 10^6 individual decisions. Above `Integer.MAX_VALUE` Guava threw
`IllegalArgumentException("Cartesian product too large")` out of the
handler.

## Maintainer ruling this change implements (2026-10-07, D16)

A configurable bound with a safe default (100 expanded requests, 100 paths
per category); exceeding it is Indeterminate.

## What the profile says (XACML v3.0 Multiple Decision Profile Version 1.0, CS02, fetched 2026-10-07)

The profile defines no limit. Its statements on errors: "If a
<RequestReference> contains an invalid reference, then the corresponding
<Result> MUST contain an Indeterminate decision with status code
urn:oasis:names:tc:xacml:1.0:status:syntax-error"; for the XPath form: "If
there are any Indeterminate results, include them in the final result".
The status for an implementation limit is the engine's choice; this change
uses processing-error (the request is well-formed; the PDP declines to
process it).

## Change

- Two limits in the handler, defaults 100 and 100, checked before the cross
  product is built: paths per category after the XPath is evaluated; the
  running product after every category.
- Beyond a limit: one result, Indeterminate, processing-error, fixed
  message `Multiple decision request expansion limit exceeded`, with the
  IncludeInResult attributes of the request; no individual request is
  evaluated; the detail goes to the log at WARN.
- `MultipleResourcesHandler(int, int)` (public chain) passes the limits;
  `PolicyDecisionPointBuilder.requestHandler(...)` takes such a chain.

Facts you cannot see in the diff:

- `content.evaluateToNodePathList(xpath)` evaluates the XPath and returns
  one path string per selected node. It runs before the limit can be
  checked; its cost is bounded by the size of the request content.
- `Sets.cartesianProduct` is a lazy view; its size is the product as an
  `int`.
- `getCategories` returns the category itself (a set of one) when it has no
  selector, and an empty set when the XPath selects nothing.
- A `PathEvaluationException` in `getCategories` leaves the category
  unexpanded (existing behaviour, not part of this change).
- `MultipleResourcesViaRepeatingAttributesHandler` (repeated categories,
  section 3.3 of the profile) also builds a cross product. It is not
  changed here.
- `Result.indeterminate(Status)` gives Decision `INDETERMINATE`.

## Questions

Report findings ordered by severity (HIGH, MEDIUM, LOW), each with file,
line from the diff, a concrete failing scenario, and a suggested fix. Report
only defects you can show from the diff and the facts above.

1. Can a request still make the handler build or iterate more than
   `maxExpandedRequests` individual requests, or more than
   `maxPathsPerCategory` categories for one selector? Check the arithmetic
   (overflow, zero, the order of the checks).
2. Is any work that grows with the expansion done before a limit is checked?
3. Does the Indeterminate result leak request data, or drop something the
   syntax-error path keeps?
4. Do the tests pin the limits (at the limit, one above, product, overflow,
   configuration, zero PDP calls)? Name a missing test.
5. Is leaving the repeated-categories handler unbounded a defect of this
   change or a separate one?

End with a verdict line: `VERDICT: APPROVE` or `VERDICT: REQUEST_CHANGES`.

## Diff

```diff
