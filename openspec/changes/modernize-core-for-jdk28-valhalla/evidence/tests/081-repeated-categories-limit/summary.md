# Run 081: one limit for the multiple-decision expansion (bead the.4.57)

Dates: 2026-10-07 and 2026-10-08. Source of the bead: run 077 (found
while fixing the.4.50). JDK: Oracle GraalVM 21.0.6+8.1
(`java version "21.0.6" 2025-01-21 LTS`). Review:
`reviews/code/rv090-repeated-categories-limit-kimi`.

## Decision basis

Ruling D16 of run 061 names the XPath expansion only. Applying the same
limit to repeated categories, to request references and to the chain as
a whole is the recommendation of this session. It was carried out on the
maintainer's standing instruction to implement the recommendations
without waiting; no separate ruling exists for it. The maintainer can
change the default or the scope afterwards.

## Normative text

XACML v3.0 Multiple Decision Profile Version 1.0, section 3.3.3
(https://docs.oasis-open.org/xacml/3.0/multiple/v1.0/xacml-3.0-multiple-v1.0.html,
fetched in run 077): "For each combination of repeated <Attributes>
elements, one Individual Decision Request SHALL be created." The profile
sets no limit; the limit is an engine rule.

## Defect

- `xacml-pdp/.../request/MultipleResourcesViaRepeatingAttributesHandler`
  built the cross product of the repeated categories without a bound.
  Four categories with 100 `<Attributes>` elements each expand to 10^8
  individual requests. Above `Integer.MAX_VALUE` Guava's
  `Sets.cartesianProduct` throws `IllegalArgumentException` out of the
  handler (red run 1, test `productOfManyCategoriesDoesNotOverflow`).
- `MultipleResourcesViaRequestReferencesHandler` evaluated one individual
  request per `<RequestReference>` without a bound.
- The steps multiply: references x repeats x content selectors.

## Change

The chain of `MultipleResourcesHandler` is now: budget, request
references, repeated categories, checkpoint, legacy XPath, XPath. With
N = `maxExpandedRequests` (default 100), for one request:

| Bound | Where |
| --- | --- |
| At most N request references; more gives the one Indeterminate, nothing evaluated | `MultipleResourcesViaRequestReferencesHandler` |
| The product of the repeats of one (referenced) request is at most N; checked after every category, before the cross product is built | `MultipleResourcesViaRepeatingAttributesHandler` |
| At most N individual requests reach the selector step, also those that select no node | `MultipleResourcesBudgetHandler.Checkpoint` |
| At most N individual requests are evaluated | the context wrapper of `MultipleResourcesBudgetHandler` |
| At most N results in the response | `MultipleResourcesBudgetHandler` |

- Beyond a bound: one result, Indeterminate, status processing-error,
  the fixed message `Multiple decision request expansion limit exceeded`,
  with the IncludeInResult attributes of the request. A WARN line names
  the configured limit only.
- The budget handler passes a wrapper of the PDP context down the chain.
  The wrapper counts and throws a private unchecked exception at count
  N + 1; the budget handler catches it. A handler that an application
  appends to the chain gets the wrapper too.
- `AbstractRequestContextHandler` has a package-private constructor
  without a feature id for the two new handlers. They add no feature to
  `getFeatures()`.
- `MultipleResourcesHandler(int maxPathsPerCategory, int maxExpandedRequests)`
  passes the limit to all of them; `new MultipleResourcesHandler()` uses
  100.

## Tests

- New `MultipleResourcesRepeatedCategoriesLimitTest` (27 tests): repeats
  at and above the limit; products; a category that occurs once; 100^5
  does not overflow or throw; the limit is configurable and positive;
  repeats x selectors, references x repeats and references alone at and
  above the limit; a selector step beyond its own limit; requests that
  select nothing; a reference that does not resolve; 101 equal repeats
  count once; a handler appended to the chain; a combined decision;
  every request has the whole limit; a single request under a limit of
  1; the IncludeInResult attributes; the features of the chain.
- Red first:
  - `red-surefire-summary.txt` (limit declared, not enforced): 11 tests,
    7 failures and 1 error
    (`Cartesian product too large; must have size at most Integer.MAX_VALUE`).
  - `red-2-surefire-summary.txt` (results of the response not counted):
    `expected: <1> but was: <7>`.
  - `red-3-surefire-summary.txt` (selector steps not counted):
    `expected: <1> but was: <0>`.
  - The other tests of the chain limit were written with their code or
    after it. `mutation-check.txt` shows that the suite detects each of
    the four parts when it is switched off.
- Gate: `surefire-summary.txt`, five clean builds. The last one is the
  tree as committed: 7,889 tests, 0 failures, 0 errors, 20 skipped.
  Conformance 937 tests with 12 skipped, unchanged.
- The gates of 2026-10-08 ran with the fix of bead policy4j-the.4.60 in
  the tree too. The two beads share no file. This bead was not built
  without that fix after gate 1.

## Found while testing

Five repeated categories that differed only in `<Content>` expanded to
four individual requests in one test and to five in another. Cause: the
XML comparison ignored child nodes and whole documents. Filed and fixed
as bug policy4j-the.4.60 (run 082). The test helpers of this run give
every repeated category a different attribute value, so this run does not
depend on that fix.

## Spec

`specs/request-limits/spec.md`: new requirements "Repeated-categories
expansion is bounded" (4 scenarios) and "One request has one limit on
individual requests" (9 scenarios).
`openspec validate modernize-core-for-jdk28-valhalla --strict` passes.

## Review

`reviews/code/rv090-repeated-categories-limit-kimi`: Kimi, two rounds.
Round 1 REQUEST_CHANGES: one MEDIUM (limit x limit selector steps without
an evaluation when the selectors select no node), two LOW (a redundant
count and a mutation result that did not fit the code; four missing
tests). All applied. Round 2 APPROVE with one LOW on the spec text,
applied. An attempt on 2026-10-07 gave working notes and no report.

## JMH

Not measured. Per request: one wrapper object, one counter step per
individual request, one multiplication per category. No JMH module
exists yet (task 9.2).

## Limits

- Compatibility: a request that gives more than 100 individual requests
  or has more than 100 request references was evaluated before and is
  Indeterminate now, until the limit is raised with
  `MultipleResourcesHandler(int, int)`. The default of 100 is the
  proposal of run 077; the maintainer has not confirmed it.
- When a limit is reached in the middle of a request, up to N individual
  requests were already evaluated in full (audit record, decision-cache
  entry, metrics). Their results are not returned.
- Work that is not an evaluation is bounded by N steps per stage, each
  step in proportion to the size of the request. The request-size limit
  itself is outside this bead.
- Equal categories count once (`ImmutableSet`), as before.
- A selector that selects no node gives no result (run 077). A response
  without a result is then possible, as before.
