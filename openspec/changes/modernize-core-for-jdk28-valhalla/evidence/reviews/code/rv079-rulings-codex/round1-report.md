1. **HIGH — Resolver diagnostics become response-visible.**  
   File: `xacml-core/src/main/java/org/xacml4j/v30/AttributeReferenceEvaluationException.java:86`.

   **Scenario:** A backend exception includes credentials, query literals, or private attribute data. With no successful fallback, `Status.processingError(t)` exposes that message through the evaluation status. Previously, the shown PIP/handler catches logged the failure and returned absence; this adds a response exposure.

   **Fix:** Use a fixed public processing-error message. Retain detailed causes in controlled diagnostics. Also sanitize the statuses preserved by `resolverFailure`; changing this factory alone does not cover that path.

2. **MEDIUM — Non-missing resolver exceptions can violate D3’s required status.**  
   File: `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java:216`.

   **Scenario:** A designator’s PIP throws an `EvaluationException` carrying `syntax-error`, perhaps from parsing a backend expression. Lines 216–217 preserve it, and `AttributeReference.resolveImpl` rethrows it. Evaluation remains Indeterminate, but its status is syntax-error instead of D3’s processing-error. D18’s selector syntax exception does not apply to this designator failure.

   **Fix:** Normalize resolver-originated failures to processing-error. Preserve syntax-error specifically for the selector’s own XPath syntax failure. Test a designator PIP throwing a non-processing, non-missing `EvaluationException` under both `MustBePresent` settings.

3. **LOW — The fallback-value test does not prove failure-before-success execution.**  
   File: `xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/DefaultPolicyInformationPointTest.java:319`.

   **Scenario:** If matching resolvers visit `"working"` first, the test passes without invoking `"failing"`. An implementation that incorrectly aborts on the first failure could therefore pass this test.

   **Fix:** Control matching order and assert that the failing resolver executes before the successful resolver. Add equivalent content-resolver coverage.

The first-failure/later-value rule matches the supplied specification. Wrapping a missing-attribute exception **thrown by the PIP** is also consistent with D3: successful absence remains `Optional.empty()`. A nested key lookup returning absence should therefore remain legitimate absence; an exception escaping resolver execution is a failure.

Throwing the original runtime exception is adequately contained by the shown PDP handler, subject to finding 2. Direct PIP callers receive that original type. Consequently, the new direct-PIP tests expecting `EvaluationException` require the descriptor to wrap their `IllegalStateException` before capture; that implementation is outside this diff.

The changed existing assertions follow the ruling without unrelated weakening. The shown `finally` blocks still execute during exception propagation. No confirmed new conversion of a processing-error into absence appears in the shown standard handler path.

Important gaps remain: real XPath evaluation, nested key resolution and final result status, `ContextSelectorId`, cache behavior, stack reuse after failure, and failure followed by a present empty bag. In particular, nested evaluation should test whether an earlier `setEvaluationStatusIfAbsent` leaves missing-attribute recorded after the outer failure becomes processing-error.

This was a diff-only review with no tools, edits, or test execution. The omitted branches and supporting implementations prevent a complete assurance about those paths.

VERDICT: REQUEST_CHANGES
codex exit=0
