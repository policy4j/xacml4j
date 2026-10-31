The round-2 resolver-message and status-selection fixes hold for resolver functions and for XPath evaluation errors on the decision path. One fail-open path remains for content selectors, and a syntax-error status message is still caller-controlled.

## Findings

### HIGH — empty content selection drops a resolver failure

`xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/DefaultPolicyInformationPoint.java`, the `return r` in the content-resolver loop (hunk starting at new line 119; the return is line 156).

`emptyUnlessFailed` throws the first resolver failure when no resolver supplies a bag. The content loop never reaches it once any resolver returns a `ContentRef`. The XPath result is returned immediately, including `Optional.empty()`.

Failing scenario:

1. Content resolver A throws `IllegalStateException` (backend down). `failure` is set and the loop continues.
2. Content resolver B returns a document whose XPath selects nothing (`selectorWithoutAMatchOnSuppliedContentReportsNoValue` shows that this is `Optional.empty()`).
3. The method returns that empty optional and discards `failure`.

With `MustBePresent=false` the selector becomes an empty bag, so a Deny rule goes NotApplicable. The same shape with the resolvers reversed never calls B: the first non-matching document is returned before B can throw.

That breaks the new requirement: no value from any resolver, and at least one failure, is Indeterminate `processing-error`. The attribute loop implements that (`resolverFailureWithAnEmptyResolverIsStillAFailure`). The content loop does not.

Fix: treat an empty selection as “this resolver supplied no bag” and keep scanning. Return only a present bag. After the loop, call `emptyUnlessFailed(failure)`. Leave a path exception from `Content.resolve` as a throw: a syntax error or an evaluation error of the selector is global and stays fail-closed.

### MEDIUM — a PIP can still choose `syntax-error` and its response text

`DefaultEvaluationContextHandler.java` line 212 (`throw e` in the `PathEvaluationException` catch) and `AttributeReference.java` line 135 (`isSyntaxError()` rethrows the same exception).

Resolver functions in the default PIP are mapped to `forResolverFailure` before this catch. A `PathEvaluationException` thrown by `PolicyInformationPoint.resolve` is not. Missing-attribute is rewritten. Every other path status is rethrown, and `AttributeReference.resolveImpl` rethrows a syntax error unchanged (`assertSame` in `testSelectorPipXPathSyntaxErrorKeepsSyntaxErrorStatus`).

Failing scenario: `pip.resolve` throws `PathEvaluationException.invalidXpath(path, Status.syntaxError().message("backend query: secret").build(), null)`. The handler returns that object. `resolveImpl` rethrows it. The marshalled status is `syntax-error` and the message is `backend query: secret`.

D18 requires keeping the syntax-error code. It does not require keeping the caller’s text. The same exception is how a real XPath syntax error leaves the PIP, because path evaluation sits outside the resolver `try`.

Fix: keep the syntax-error code, replace the status message with fixed text, and chain the original exception. Do this in the handler so every caller of `resolve` sees the sanitized status, not only `AttributeReference.resolveImpl`.

## Answers

1. Round 2 is fixed for resolver functions and for non-syntax XPath errors on the decision path. `forResolverFailure` is idempotent only when `isResolverFailure()` is true, and that flag is set only by its private constructor, which uses the 3-argument super constructor so the status message stays `Attribute resolution failed`. The default PIP catches every `RuntimeException` from context creation, cache, and the resolver function, including a `PathEvaluationException` with a chosen status. `AttributeResolverDescriptor` has already wrapped other resolver failures as `EvaluationException`, and the tests pass, so that type is caught. An XPath evaluation error (processing-error) is rethrown by the handler with the engine text still on it, then `resolveImpl` wraps it; the selector test checks the response message. What remains is the syntax-error passthrough in finding 2, and the empty-selection path in finding 1, which never builds a failure status at all.

2. A genuine miss is still `Optional.empty()` and still follows `MustBePresent` (`emptySelectionStillFollowsMustBePresent`, `resolversWithoutAValueAndWithoutAFailureReportNoValue`). A designator or single-resolver failure is `processing-error` for both `MustBePresent` values, including a Deny rule (`INDETERMINATE_D`). The remaining fail-open is finding 1: a content resolver failure plus a document that selects nothing becomes an empty bag or `missing-attribute`. No path in this diff turns a clean miss into Indeterminate.

3. `canonicalRdns` matches the canonical form you described. Join separators are raw commas with no spaces, so splitting on an unescaped comma is the inverse of that join. `\,` does not split: the backslash sets `escaped`, the comma is copied, and `escaped` clears. `\\` then a comma does split, which is an escaped backslash followed by a real separator. A trailing backslash is appended and does not drop a character or add an empty RDN; a valid canonical name does not end with an odd backslash. An empty canonical string returns an empty list, not `[""]`, so two empty names compare equal and an empty first name is the empty terminal sequence (the new test expects `true`, which is what D9 says). A multi-valued RDN stays one fragment because `+` is not a separator, and `#hex` contains no raw comma. Fragments are compared with the escapes still in place, which is the same comparison `x500Name-equal` makes on the joined canonical string.

4. No assertion was weakened past the rulings. Fail-open expectations were replaced with `processing-error`. Previously disabled `round`, `double-to-integer`, `x500Name-match`, and `ipAddress-*` cases now assert D5, D6, D8, and D9. Manifest rows for IIIC002/003, IIIG001–005, IIA023, IID029, and IIIG300 change only the reason text; disposition and the executed flag stay as they were.

VERDICT: REQUEST_CHANGES
grok exit=0
