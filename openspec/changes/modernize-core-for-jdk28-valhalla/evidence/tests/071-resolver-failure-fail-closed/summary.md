# Run 071: resolver and path failures fail closed (beads the.4.34, the.11)

Date: 2026-10-07. Rulings: run 061 D3 and D18
(`builds/070-maintainer-rulings`). JDK: Oracle GraalVM 21.0.6+8.1
(`java version "21.0.6" 2025-01-21 LTS`). Reviews:
`reviews/code/rv079-rulings-codex`, `reviews/code/rv080-rulings-grok`.

## Behaviour

| Case | Before | After |
| --- | --- | --- |
| Designator, PIP or resolver throws, `MustBePresent=false` | empty bag; the rule can be NotApplicable | Indeterminate `processing-error` |
| Designator, PIP or resolver throws, `MustBePresent=true` | Indeterminate `missing-attribute` | Indeterminate `processing-error` |
| Selector, content resolver throws | empty bag or `missing-attribute` | Indeterminate `processing-error` |
| Selector, XPath evaluation error that is not a syntax error | empty bag or `missing-attribute` | Indeterminate `processing-error` for both `MustBePresent` values |
| Selector, `ContextSelectorId` does not select one node | `syntax-error` | `syntax-error` (unchanged code) |
| Lookup or selection succeeds and is empty | follows `MustBePresent` | unchanged |
| Several resolvers, one throws, another yields a value | the value | the value |
| Several resolvers, one throws, none yields a value | empty bag | Indeterminate `processing-error` |
| A resolver returns attributes without the requested one, or content in which nothing is selected | the search ends with "no value" | the next matching resolver is asked |
| Status message of these Indeterminate results | the exception text of the resolver or of the XPath engine | fixed text: `Attribute resolution failed` (`processing-error`), `Attribute reference is invalid` (`syntax-error`) |

## Code

| File | Change |
| --- | --- |
| `xacml-core/.../v30/AttributeReferenceEvaluationException.java` | Factories `forResolverFailure(ref, cause)` and `forSyntaxError(ref, cause)`; constants `RESOLUTION_FAILED_MESSAGE`, `INVALID_REFERENCE_MESSAGE`; private `origin` field set only by the factories; `isResolverFailure()` is `final`. A private constructor uses `CoreException(Status, String, Throwable)`, because `CoreException(Status, String)` copies the exception message into the status message. The message of the mapped failure names the class of the cause (`causeName`); `getMessage()` of the cause is never called, so a cause whose `getMessage()` throws cannot replace the mapped failure. |
| `xacml-core/.../v30/policy/AttributeReference.java` | `resolveImpl`: `missing-attribute` follows `MustBePresent`; `syntax-error` becomes `forSyntaxError`; every other `EvaluationException` and every `RuntimeException` becomes `forResolverFailure`. |
| `xacml-pdp/.../v30/pdp/DefaultEvaluationContextHandler.java` | Designator PIP call: every exception becomes `forResolverFailure` (was `Optional.empty()`). Selector: `resolveViaPip` maps every exception of `pip.resolve`; `doSelectorResolve` maps the engine's own `PathEvaluationException` (`syntax-error` keeps its code). Cycle detection is unchanged. |
| `xacml-pdp/.../v30/spi/pip/DefaultPolicyInformationPoint.java` | Both `resolve` overloads keep the first failure, go on to the next resolver, return only a present bag, and call `emptyUnlessFailed(failure)` at the end. For a selector the path is evaluated on the supplied content outside the resolver `try`. |
| All four classes above | The mapped failure is built before anything is logged, and the log line is the message of the mapped failure. The foreign exception is not given to the logger. |
| `xacml-pdp/.../v30/spi/pip/PolicyInformationPoint.java` | Javadoc only: an empty result is "no value"; every exception is reported as `processing-error` with a fixed message. |

## Behaviour changes to know about

1. `DefaultEvaluationContextHandler.resolve` now throws
   `AttributeReferenceEvaluationException` for a path error (the
   `PathEvaluationException` is the cause). The status code is the same as
   before. Four handler tests that pinned the old type now pin the status,
   the fixed message, and the cause type.
2. A custom `PolicyInformationPoint` can no longer report `syntax-error`
   for a selector. Only the engine's own path evaluation does. In this code
   base the engine reports `syntax-error` in one place (`XmlContent`, the
   `ContextSelectorId` cardinality check), which is not on the PIP path.
3. With several matching resolvers, a later resolver is now asked when an
   earlier one returned attributes or content without a value. The default
   registry keeps one content resolver per category, so for content this
   needs a custom `ResolverRegistry`.

## Tests

- Red first: `red-surefire-summary.txt`.
- `AttributeResolutionSafetyNetTest` (PDP level): the task 4.34 tests are
  enabled; the matrix rows expect `processing-error`; Deny rule gives
  Indeterminate{D}; a resolver or an information point cannot choose the
  status or the response message (designator and selector).
- `DefaultEvaluationContextHandlerTest`: PIP failure is `processing-error`,
  is not cached, and does not look like a cycle; a subclass of the mapped
  failure with `missing-attribute` or `syntax-error` is still mapped; a PIP
  `syntax-error` with back-end text is mapped; path errors on request
  content have the fixed message; the engine's syntax error has the fixed
  message.
- `DefaultPolicyInformationPointTest`: failure, empty, and later-value
  order; resolver-chosen exception types; failed resolver plus a resolver
  without the attribute; failed content resolver plus content that selects
  nothing, in both orders; a resolver without a value followed by one with
  a value (attributes and content); path error on supplied content.
- `AttributeDesignatorTest`, `AttributeSelectorTest` (core): the same rules
  at the reference level with a mocked context.
- A failure whose `getMessage()` throws an exception with its own status
  (designator PIP, selector PIP, resolver, content resolver, and
  `AttributeSelector` with a mocked context), also as a checked
  exception. Red runs: `round4-red-surefire-summary.txt`,
  `round5-red-surefire-summary.txt`.
- Gate: `surefire-summary.txt`, gate 6. Full reactor on JDK 21: 7,695
  tests, 0 failures, 0 errors, 24 skipped. That run also held the
  uncommitted change of bead the.4.12 (run 074); gate 5 (7,638 tests) is
  the last run without it. Conformance: 927 tests with 14
  skipped, the same as run 069.

## Spec

`specs/attribute-resolution/spec.md`, requirement "Resolver failures are
processing errors": resolver order, what "no value" means, the fixed status
message, and who can report `syntax-error`; ten scenarios.
`openspec validate modernize-core-for-jdk28-valhalla --strict` passes.

## Reviews

| Lane | Rounds | Final verdict | Record |
| --- | --- | --- | --- |
| Codex | 6 | APPROVE (round 6) | `reviews/code/rv079-rulings-codex` |
| Grok grok-4.7 | 5 (round 1 stopped without a report) | APPROVE (rounds 4 and 5) | `reviews/code/rv080-rulings-grok` |

Findings that changed the code after the first green gate: the status
message of the response (round 1), the content resolver boundary and the
origin mark (round 2), the overridable accessor, the PIP's `syntax-error`,
and the dropped failure in the PIP loops (round 3), and the `getMessage()`
calls on the failure (rounds 4 and 5). Grok did not review the round-5
fix; Codex verified it.

## JMH

NOT APPLICABLE: the changed code runs only when a resolver or a path
evaluation fails, or when several resolvers match one reference. The
success path of a designator or selector has the same calls as before,
plus one `try` block.

## Limits

- The `attribute-selector` function with an entity argument evaluates the
  path on that entity (`Entity.resolve`), outside the context handler. A
  path error there keeps the status and message of the
  `PathEvaluationException`, like any other function error. Not changed in
  this run.
- A cyclic reference between resolvers still resolves to "no value"
  (`Optional.empty()`), as before.
- XACML 3.0 section 7.3.5 was not fetched again in this run; the expected
  values come from the rulings.
