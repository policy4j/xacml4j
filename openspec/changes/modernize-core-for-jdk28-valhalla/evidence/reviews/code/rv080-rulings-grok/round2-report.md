Round-1 items 2 and 3 are fixed. Item 1 is fixed only for failures that go through `forResolverFailure`. Two paths still put the original failure text on the response status, and two new PIP tests assert a type this code does not throw.

## Findings

### MEDIUM — selector `PathEvaluationException` is rethrown with its original status message

`DefaultEvaluationContextHandler.java` (selector `catch`, `throw e`, about line 203)

`catch (PathEvaluationException)` now rethrows the same instance. That `try` includes `pip.resolve`, which `testSelectorPipXPathSyntaxErrorKeepsSyntaxErrorStatus` stubs. `AttributeReference` then rethrows any `EvaluationException` whose status is not missing-attribute (`AttributeReference.java` about lines 129–130). `AttributeSelectorTest` uses `assertSame(failure, thrown)`, so the status message is whatever the exception already carried.

Failing scenario: a content resolver throws `PathEvaluationException.invalidXpath("/a", Status.processingError().message("ldap://admin:s3cret@internal/").build(), null)`, or the XPath engine builds that exception with `cause.getMessage()` in the status (the same `(Status, String)` constructor this change calls out as copying the exception message into the status). The PDP result status message is that string. Before this change this catch returned an empty bag, so the text never reached the response. A syntax-error `PathEvaluationException` from the PIP is also preserved, so a retrieval failure can be reported as `syntax-error`.

Suggested fix: from this catch, keep a syntax-error only when the failure comes from parsing the selector expression. For every other `PathEvaluationException`, and for any `PathEvaluationException` thrown by `pip.resolve` or the cache, throw a new exception whose status message is a fixed string. Map a PIP failure to `resolverFailure`.

### MEDIUM — already-wrapped `processing-error` skips sanitizing

`DefaultEvaluationContextHandler.java` `resolverFailure`, about lines 223–226

```java
if (e instanceof AttributeReferenceEvaluationException
        && e.getStatus().getStatusCode().isProcessingError()) {
    return (EvaluationException) e;
}
```

`forSelector` still feeds `t.getMessage()` into the exception (`AttributeReferenceEvaluationException.java`), and the `(Status, String)` constructor copies that message into the status. This shortcut returns that exception unchanged. Nested `forResolverFailure` is safe. Any other `AttributeReferenceEvaluationException` with `processing-error` is not.

Failing scenario: a resolver, or `forSelector(key, sqlException)`, throws `AttributeReferenceEvaluationException` with status `processing-error` and message `SQLSTATE 28000 password authentication failed for user pep`. `resolverFailure` returns it. `AttributeReference` rethrows it because the status is not missing-attribute. The response `StatusMessage` is the SQL text. `designatorResolverCannotChooseTheStatusOrTheResponseMessage` only throws a plain `EvaluationException`, so this path is untested.

Suggested fix: always return `forResolverFailure(key, e)`. The fixed status message replaces the inner one. The cause chain still has the detail for logs.

### MEDIUM — attribute PIP tests contradict `emptyUnlessFailed`

`DefaultPolicyInformationPoint.java` about line 108, and `DefaultPolicyInformationPointTest.resolverFailureWithoutAnotherValueIsThrownAsProcessingError` / `resolverFailureWithAnEmptyResolverIsStillAFailure`

`emptyUnlessFailed` throws the original `RuntimeException`. The resolver lambda throws `IllegalStateException("backend down")`. Those two tests expect `EvaluationException` with `processing-error`. The new content-resolver test expects `IllegalStateException` and the message `content backend down` from the same helper. The `processing-error` translation lives in `DefaultEvaluationContextHandler.resolverFailure`, which these two tests never call.

Failing scenario: run `DefaultPolicyInformationPointTest`. The two attribute tests fail on the exception type. As written they claim a contract the method does not implement.

Suggested fix: expect `IllegalStateException` at the PIP boundary, matching the content-resolver test. Keep the `processing-error` and fixed-message assertions on the handler and PDP tests.

## Answers

1. Round 1, item 2 is fixed: a designator PIP `EvaluationException` with `syntax-error` is wrapped by `forResolverFailure`, and the PDP test checks that `secret` is absent from the status message. Item 3 is fixed: call order is recorded, a content-resolver failure test exists, and the designator stack is clean on the next call. Item 1 is fixed for `forResolverFailure`: the status message is `Attribute resolution failed`, and the handler and PDP tests lock that in for ordinary designator failures. The cause text remains in the exception message and, by the new constructor’s own comment, in the status error object. It still reaches a status message on the two paths above. The selector PDP test never checks the message, so `selector resolver failed` could be published and the test would stay green.

2. A lookup that returns no value stays a miss. `resolversWithoutAValueAndWithoutAFailureReportNoValue` still gets an empty `Optional`, and the safety-net row `missing-must-be-present-false` stays `NotApplicable`. A thrown failure becomes `processing-error` for both `MustBePresent` values, including a Deny rule (`Indeterminate{D}`). A later resolver value is kept and the earlier failure is dropped, which matches the spec scenario. A resolver that throws to mean “no such attribute” is now `Indeterminate`. That is what D3 requires. One latent hole: `AttributeReference` (about line 136) still turns a missing-attribute `EvaluationException` into an empty bag when `MustBePresent` is false, and the selector handler does not remap `PathEvaluationException`. A `PathEvaluationException` built with missing-attribute becomes an empty bag on that flag.

3. `LdapName.getRdns()` is root-first (index 0 is the rightmost RDN). A terminal sequence is a prefix of that list, and the loop implements that. `O=Medico Corp,C=US` against `cn=John Smith,o=Medico Corp,c=US` matches. A single AVA does not match a multi-valued RDN, because `sameRdn` compares the whole RDN. Case folding goes through `X500Principal.equals`, which is the same comparison `x500Name-equal` uses for names. An empty first name has size 0, the loop does not run, and the result is `true` for every second name, including empty. `new LdapName(principal.getName(RFC2253))` and `new X500Principal(rdn.toString())` are not total on every name `X500Principal` accepts (trailing escaped space, some hex AVAs, some multi-valued `Rdn.toString()` forms). `rdns` turns `InvalidNameException` into `IllegalArgumentException` (about line 81), and `sameRdn` (about line 91) lets `IllegalArgumentException` leave the function. A name the engine already accepted can abort evaluation instead of returning a boolean. Reversed AVA order, a comma inside a value, and an OID-versus-keyword RDN are untested. Those should match. A string suffix such as `C=US` versus `DC=US` is correctly rejected.

4. `Math.rint` matches D5: `NaN` stays `NaN`, infinities stay, `0.5 → 0.0`, `1.5 → 2.0`, `2.5 → 2.0`, `-2.5 → -2.0`. `rint(-0.5)` is `-0.0` and `rint(-1.5)` is `-2.0`. Both are ties-to-even. Neither is tested. `floor` is unchanged in this diff. The new infinity tests match `Math.floor`. `double-to-integer` of `NaN` and either infinity throws `processing-error` before the range check. The `2^63` bound still rejects the overflow double and still allows the exact double `-2^63`. No value covered by D5 or D6 is wrong.

5. The old empty-bag and missing-attribute assertions for resolver failure were replaced with `processing-error`. That follows D3. It is tighter. The removed `fail()` placeholders were undecided decisions, and the new assertions match D5, D6, D8, and D9. Manifest rows stay `skipped` or `special-harness`. Only the reason text changes. IIIG001–IIIG005 of the 2.0 suite are labeled “run 061 D13”, the same id as the hierarchical rows. IIIG300 is labeled D14. Nothing else in the execution columns moved. Missing coverage: selector response message, the early-return exception type, content-resolver “failure then value”, `round(-1.5)` and `round(-0.5)`, empty X.500 names, escaped RDN values, reversed multi-valued AVAs.

VERDICT: REQUEST_CHANGES
grok exit=0
