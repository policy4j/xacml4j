# Adversarial code review, round 2: one limit for the multiple-decision expansion (xacml4j)

You are an independent reviewer. Do not use tools. Do not edit files. Answer
in one message. Review only the diff below. It is the complete diff of this
change after the fixes of round 1. The full reactor passes on it:
7,887 tests, 0 failures, 0 errors, 20 skipped. Conformance unchanged: 937 tests, 12 skipped. The reactor ran
with a second, unrelated change in the tree (a fix of the XML content
comparison in another module); the two changes share no file.

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
the repeated categories without a bound, and above `Integer.MAX_VALUE`
Guava's `Sets.cartesianProduct` threw out of the handler. The
request-references handler evaluated one individual request per
`<RequestReference>` without a bound. The steps multiply: references x
repeats x content selectors.

## Earlier change that this one follows (ruling D16, already committed)

The XPath handler (`multiple:content-selector`) has limits: at most 100
node paths per category and at most 100 individual requests per request,
both configurable through
`MultipleResourcesHandler(int maxPathsPerCategory, int maxExpandedRequests)`.
Beyond a limit: one result, Indeterminate, status processing-error, fixed
message `Multiple decision request expansion limit exceeded`, with the
IncludeInResult attributes of the request; no individual request is
evaluated. A selector that selects no node gives an empty result list.
The maintainer ruled only on the XPath expansion. Applying the limit to
the other steps and to the chain as a whole is the author's
recommendation, carried out on the maintainer's standing instruction to
implement the recommendations.

## Change (final)

The chain is now: budget, request references, repeated categories,
checkpoint, legacy XPath, XPath.

1. Repeated categories: the running product of the repeats is checked
   after every category, before the cross product is built.
2. Request references: more references than `maxExpandedRequests` gives
   the single Indeterminate result, nothing evaluated.
3. `MultipleResourcesBudgetHandler` (first): passes a wrapper of the PDP
   context down the chain. The wrapper has two counters, each with the
   limit: the individual requests that reach the content-selector step
   (counted by the `Checkpoint` handler that stands after the
   repeated-categories handler), and the calls of `requestDecision`. At
   count limit + 1 it throws a private unchecked exception; the budget
   handler catches it and returns the single Indeterminate result. After
   a normal return the handler rejects a response with more results than
   the limit.
4. `AbstractRequestContextHandler` gets a package-private constructor
   without a feature id, for the two handlers of point 3.

Intended invariants for one request through `MultipleResourcesHandler`,
with N = `maxExpandedRequests`: at most N request references are
resolved; at most N individual requests reach the selector step (so at
most N runs of the legacy and XPath handlers); at most N calls of the
PDP's `requestDecision`; at most N results in the response.

Known and accepted: when a limit is reached in the middle, the individual
requests before it were evaluated in full (audit record, decision-cache
entry, metrics); their results are discarded.

## Round 1 result (Kimi: REQUEST_CHANGES) and what was done

- MEDIUM-1, limit x limit selector steps with empty selections and no
  evaluation: fixed by the second counter and the `Checkpoint` handler.
  New test `individualRequestsThatSelectNothingAreCounted`; red before the
  fix (`expected: <1> but was: <0>`). Spec: an individual request whose
  selectors select no node counts as an individual request; two new
  scenarios.
- LOW-1, the in-loop count of the results in the references handler was
  redundant with the count of the results in the budget handler, and the
  reported M3 kill predated that count: the in-loop count is removed. The
  mutation analysis was run again on the final code, see below.
- LOW-2, missing tests: added
  `referenceThatDoesNotResolveCountsAsAResult`, `equalRepeatsCountOnce`,
  `handlerAppendedToTheChainIsInsideTheLimit`,
  `combinedDecisionBeyondTheLimitIsTheOneIndeterminate`.
- Remark on `getRequestHandlers()`: a comment on the override.

Facts you cannot see in the diff:

- `AbstractRequestContextHandler.handleNext(request, context)`:
  `next == null ? Collections.singleton(context.requestDecision(request)) : next.handle(request, context)`.
  Every handler passes on the `context` object that it received.
- `RequestContextHandlerChain.handle` is final:
  `postProcessResults(req, handlers.get(0).handle(req, context))`. Its
  `setNext(handler)` sets the handler as next of the last handler of the
  chain.
- `MultipleResourcesHandler.postProcessResults` returns the results
  unchanged unless the request asks for a combined decision; a single
  result is always returned unchanged.
- `PolicyDecisionPointContext` has exactly the ten methods that the
  wrapper overrides.
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
  calling `handleNext`.
- Legacy XPath handler (unchanged): it rewrites the request and calls
  `handleNext` once; with repeated categories it returns one Indeterminate
  syntax-error result.
- A category set is built with `ImmutableSet.copyOf`, so equal categories
  count once, as before this change.
- Mutation analysis of the final code (test class
  `MultipleResourcesRepeatedCategoriesLimitTest`, 27 tests). M1, the
  wrapper does not count the calls of `requestDecision`: 3 failures
  (`repeatsTimesSelectorsHaveOneLimit`,
  `handlerAppendedToTheChainIsInsideTheLimit`,
  `combinedDecisionBeyondTheLimitIsTheOneIndeterminate`). M2, the number
  of references is not checked first: 1 failure
  (`moreRequestReferencesThanTheLimitAreIndeterminate`). M3, the wrapper
  does not count the selector steps: 1 failure
  (`individualRequestsThatSelectNothingAreCounted`, `expected: <1> but
  was: <0>`). M4, the results of the response are not counted: 2 failures
  (`referenceThatDoesNotResolveCountsAsAResult` `<1> but was <5>`,
  `responseHasNoMoreResultsThanTheLimit` `<1> but was <7>`).

## Questions

Report findings ordered by severity (HIGH, MEDIUM, LOW), each with file,
line from the diff, a concrete failing scenario, and a suggested fix. Report
only defects you can show from the diff and the facts above.

1. Is MEDIUM-1 closed? Construct the worst case of work that is not an
   evaluation for one request now, in handler runs and selector
   evaluations, as a function of N.
2. Are LOW-1 and LOW-2 closed as described?
3. Does the `Checkpoint` reject or change a request that is inside the
   limit, or count a request twice?
4. Can the private exception now escape or be caught elsewhere (it is
   thrown from the checkpoint too)?
5. Any other defect in the diff. Does the spec text say what the code
   does?

End with a verdict line: `VERDICT: APPROVE` or `VERDICT: REQUEST_CHANGES`.

## Diff

(the complete diff of the change followed here)
