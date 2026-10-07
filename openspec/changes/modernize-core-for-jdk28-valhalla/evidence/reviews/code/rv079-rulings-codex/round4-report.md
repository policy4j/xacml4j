1. **HIGH — Diagnostic message extraction can bypass failure normalization.**

   Locations: `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java:177,239`; `xacml-core/src/main/java/org/xacml4j/v30/AttributeReferenceEvaluationException.java:111`.

   A custom PIP can throw a `RuntimeException` whose overridden `getMessage()` throws:
   ```java
   new EvaluationException(
       Status.missingAttribute(ref).build(), "backend detail");
   ```
   The eager `e.getMessage()` at handler line 177 throws before `forResolverFailure` runs. That secondary exception escapes the catch. `AttributeReference.resolveImpl`, at `AttributeReference.java:128`, accepts its missing-attribute status: `MustBePresent=false` returns absence, allowing NotApplicable; `true` preserves the exception and its response-visible backend text.

   The selector bypass is also visible: make `getMessage()` throw a `PathEvaluationException` carrying syntax-error. Line 239 throws it during logging; `doPipResolve` rethrows it at line 217; `doSelectorResolve` treats it as an engine syntax error at line 135. The PIP therefore chooses syntax-error instead of processing-error.

   **Fix:** Construct the normalized exception without calling overridable methods on its cause; retain the cause with fixed diagnostic text. Ensure logging cannot replace the normalized failure. Add tests with these throwing `getMessage()` implementations. The factories’ own `t.getMessage()` calls need the same correction.

The private-origin change fixes the original subclass-mark bypass. Ordinary PIP exceptions and engine path exceptions are mapped as described, but the diagnostic bypass leaves round-3’s protections incomplete.

Both visible loops now continue after an absent bag and preserve an earlier failure until a present bag succeeds. Ordinary absence remains absence. Content caching happens before testing the selection; the attribute cache-write statements are outside the displayed hunks, so exact cache parity cannot be verified from this diff.

No assertion is visibly weakened beyond the rulings. The reactor results are supplied evidence; this review used only the diff and stated facts.

VERDICT: REQUEST_CHANGES
codex exit=0
