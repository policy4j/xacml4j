# Adversarial code review: one limit for the multiple-decision expansion (xacml4j)

You are an independent reviewer. Do not use tools. Do not edit files. Answer
in one message. Review only the diff below. It is the complete diff of this
change. The full reactor passes on it: 7,876 tests, 0 failures, 0 errors, 20 skipped. Conformance unchanged:
937 tests, 12 skipped. The reactor ran with a second, unrelated change in
the tree (a fix of the XML content comparison in another module); the two
changes share no file.

## Project

xacml4j is a Java 21 implementation of OASIS XACML 2.0/3.0. The PDP
expands a multiple-decision request through a chain of handlers
(`MultipleResourcesHandler`). The trust boundary is the request: it comes
from the caller of the PDP and is not trusted.

## Normative text (XACML v3.0 Multiple Decision Profile Version 1.0, section 3.3.3)

"For each combination of repeated <Attributes> elements, one Individual
Decision Request SHALL be created. This Individual Request SHALL be
identical to the original request context with one exception: only one
<Attributes> element of each repeated category SHALL be present."

The profile sets no limit. A limit is an engine rule.

## Defect (security sweep, bug policy4j-the.4.57)

`MultipleResourcesViaRepeatingAttributesHandler` built the cross product of
the repeated categories without a bound: 4 categories with 100
`<Attributes>` elements each expand to 10^8 individual requests, and above
`Integer.MAX_VALUE` Guava's `Sets.cartesianProduct` throws
`IllegalArgumentException` out of the handler. The request-references
handler evaluated one individual request per `<RequestReference>` without
a bound. The steps multiply: references x repeats x content selectors.

## Earlier change that this one follows (ruling D16, already committed)

The XPath handler (`multiple:content-selector`) has limits since an
earlier change: at most 100 node paths per category and at most 100
individual requests per request, both configurable through
`MultipleResourcesHandler(int maxPathsPerCategory, int maxExpandedRequests)`.
Beyond a limit: one result, Indeterminate, status processing-error, fixed
message `Multiple decision request expansion limit exceeded`, with the
IncludeInResult attributes of the request; no individual request is
evaluated. The maintainer ruled only on the XPath expansion. Applying the
limit to the other steps and to the chain as a whole is the author's
recommendation, carried out on the maintainer's standing instruction to
implement the recommendations.

## Change

The chain is now: budget, request references, repeated categories, legacy
XPath, XPath.

1. Repeated categories: the running product of the repeats is checked
   after every category, before the cross product is built. Beyond
   `maxExpandedRequests`: the single Indeterminate result, nothing
   evaluated.
2. Request references: more references than `maxExpandedRequests` gives
   the single Indeterminate result, nothing evaluated. In the loop over
   the references the results are counted together; above the limit the
   loop stops with the single Indeterminate result.
3. New first handler `MultipleResourcesBudgetHandler`: it passes a wrapper
   of the PDP context down the chain. The wrapper counts the calls of
   `requestDecision` (one per evaluated individual request) and throws a
   private unchecked exception at call number limit + 1. The handler
   catches it and returns the single Indeterminate result. After a normal
   return it also rejects a response with more results than the limit.
4. `AbstractRequestContextHandler` gets a package-private constructor
   without a feature id, for the budget handler.

Intended invariants for one request through `MultipleResourcesHandler`:
at most `maxExpandedRequests` calls of the PDP's `requestDecision`; at
most `maxExpandedRequests` results in the response.

Known and accepted: when the limit is reached in the middle, the
individual requests before it were evaluated in full (audit record,
decision-cache entry, metrics); their results are discarded.

Facts you cannot see in the diff:

- `AbstractRequestContextHandler.handleNext(request, context)`:
  `next == null ? Collections.singleton(context.requestDecision(request)) : next.handle(request, context)`.
  Every handler passes on the `context` object that it received.
- `RequestContextHandlerChain.handle` is final:
  `postProcessResults(req, handlers.get(0).handle(req, context))`. Its
  `setNext(handler)` sets the handler as next of the last handler of the
  chain, so a handler that an application appends after
  `MultipleResourcesHandler` receives the wrapper too.
- `MultipleResourcesHandler.postProcessResults` returns the results
  unchanged unless the request asks for a combined decision; a single
  result is always returned unchanged.
- `PolicyDecisionPointContext` has exactly these methods:
  `getCorrelationId`, `createEvaluationContext`, `getDomainPolicy`,
  `getDecisionCache`, `getDecisionAuditor`, `requestDecision`,
  `getRequestHandlers`, `isDecisionCacheEnabled`,
  `isDecisionAuditEnabled`, `isValidateFuncParamsAtRuntime`.
- `DefaultPolicyDecisionPoint.decide(request)` creates one context per
  request and calls `context.getRequestHandlers().handle(request, context)`
  once, on the calling thread. No production code calls
  `getRequestHandlers()` or `requestDecision` elsewhere; the handlers
  iterate sequentially.
- The catch blocks in the handler package catch `RequestSyntaxException`,
  `PathEvaluationException` and the XPath handler's private checked
  `ExpansionLimitExceededException` only.
- XPath handler (unchanged): passes the request on when it has repeated
  categories or no `multiple:content-selector`; otherwise checks its
  limits, then calls `handleNext` once per individual request and collects
  the results; beyond a limit it returns one Indeterminate result without
  calling `handleNext`. A selector that selects no node gives an empty
  result list.
- Legacy XPath handler (unchanged): it rewrites the request (resource-id
  of XPath type to a content selector) and calls `handleNext` once; with
  repeated categories it returns one Indeterminate syntax-error result.
- A category set is built with `ImmutableSet.copyOf`, so equal categories
  count once, as before this change.
- `RequestContext.getRequestReferences()` is the list of
  `<RequestReference>` elements; `getReferencedCategory(ref)` looks a
  category up by its `xml:id`.
- Red run of the first version of the test class (limit declared, not
  enforced): 11 tests, 7 failures and 1 error (`Cartesian product too
  large; must have size at most Integer.MAX_VALUE`).
- Red run of `responseHasNoMoreResultsThanTheLimit` before the result
  count was checked: `expected: <1> but was: <7>`.
- Mutation check of the final code (test class
  `MultipleResourcesRepeatedCategoriesLimitTest`, 21 tests at that time):
  M1, the wrapper does not count: 2 failures
  (`repeatsTimesSelectorsHaveOneLimit`,
  `requestReferencesTimesRepeatsHaveOneLimit`). M2, the number of
  references is not checked first: 1 failure
  (`moreRequestReferencesThanTheLimitAreIndeterminate`). M3, the results
  of the references are not counted together: 1 failure
  (`resultsOfRequestReferencesHaveOneLimit`).

## Questions

Report findings ordered by severity (HIGH, MEDIUM, LOW), each with file,
line from the diff, a concrete failing scenario, and a suggested fix. Report
only defects you can show from the diff and the facts above.

1. Is there a request for which `MultipleResourcesHandler` evaluates more
   than `maxExpandedRequests` individual requests, returns more than
   `maxExpandedRequests` results, overflows a count, or throws?
2. Can the private exception of the budget escape the chain, or be caught
   by another handler?
3. Is the work that is not an evaluation bounded too (handler steps,
   XPath selections), or can a request make the chain do a multiple of the
   limit of such steps? Give the worst case you can construct.
4. Is a request that the profile allows and that is inside the limit now
   rejected or answered differently than before?
5. Is the wrapper complete and safe (delegation, one per request, no
   sharing between threads)?
6. Does the Indeterminate result or a log line leak request data?
7. Do the tests pin the invariants? Name a missing test.
8. Does the spec text say what the code does?

End with a verdict line: `VERDICT: APPROVE` or `VERDICT: REQUEST_CHANGES`.

## Diff

(the complete diff of the change followed here)
