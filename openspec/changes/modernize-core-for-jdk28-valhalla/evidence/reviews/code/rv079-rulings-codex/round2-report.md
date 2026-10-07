1. **HIGH — Selector resolver failures can still choose the status and expose backend text.**  
   File: `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java:198–203`.

   A content resolver can throw a `PathEvaluationException` containing `syntax-error` and backend details. `DefaultPolicyInformationPoint` retains that exception, and this catch rethrows it unchanged—even though the resolver failed before the selector’s XPath ran. That violates D3 and bypasses the fixed status message.

   A resolver-supplied `PathEvaluationException` carrying `missing-attribute` also reaches `AttributeReference.java:129–136`, where `MustBePresent=false` suppresses it into an empty result.

   **Fix:** Normalize exceptions at the content-resolver invocation boundary. Preserve syntax errors only from the engine’s actual selector XPath evaluation. Add tests using a real content-resolver callback that throws these exception variants, with both `MustBePresent` values. The new mocked PIP syntax-error test does not establish the exception’s origin.

2. **HIGH — The “already mapped” shortcut does not establish that the message was sanitized.**  
   File: `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java:223–226`.

   A PIP throwing an `AttributeReferenceEvaluationException` with `processing-error` and a status message containing backend details passes through unchanged. The shortcut checks the exception class and status code; neither proves that `forResolverFailure` created it. The constructor comment explicitly distinguishes other constructors that copy exception text into the status.

   **Fix:** Always normalize resolver-origin exceptions through `forResolverFailure`, or use explicit provenance identifying exceptions created by that factory. Add a regression test for a resolver-supplied processing-error `AttributeReferenceEvaluationException`, asserting the exact fixed response message.

3. **MEDIUM — RFC2253 reconstruction can change `x500Name-equal` semantics.**  
   File: `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/SpecialMatchFunctions.java:78` and `:90`.

   Consider these valid names:

   ```text
   first:  CN=#1e020041
   second: CN=A
   ```

   The first contains a DER BMPString encoding of `A`. `X500Principal.equals` distinguishes that encoding from the ordinary PrintableString name through its canonical representation. However, RFC2253 renders both as `CN=A`; parsing and rebuilding the RDNs therefore makes the new match return `true`. D9 requires the RDN comparison to retain the equality distinction.

   **Fix:** Split canonical representations while retaining hexadecimal values, or preserve each original RDN’s DER encoding. Add this case and assert consistency with `x500Name-equal`.

The round-1 order, content-failure, and designator stack-cleanup changes address finding 3. The new factory fixes its own message handling, and ordinary designator exceptions are normalized, but round-1 findings 1 and 2 remain incomplete through the bypasses above.

Ordinary `Optional.empty()` and empty-map misses remain legitimate absence results. RDN direction, whole multi-valued RDN comparison, and empty-name loop behavior are correct. No concrete parsing rejection is established here; the demonstrated X500 defect is encoding loss. `Math.rint` and the new nonfinite conversion rejection satisfy D5/D6.

No existing assertion was weakened beyond the rulings. Additional useful cases include escaped RDN values, reordered multi-valued RDNs, empty names, selector stack cleanup, signed-zero rounding, and finite doubles beyond the long range.

Static review of the supplied diff only. No tools, edits, or tests were run.

VERDICT: REQUEST_CHANGES
codex exit=0
