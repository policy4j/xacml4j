Two defects remain.

1. **HIGH — A resolver can forge the factory provenance check.**  
   File: `xacml-core/src/main/java/org/xacml4j/v30/AttributeReferenceEvaluationException.java:97` and `:132`.

   The class and `isResolverFailure()` are overridable. A resolver can throw an anonymous subclass constructed with `missing-attribute` and backend text, overriding `isResolverFailure()` to return `true`. The factory returns it unchanged. It survives the PIP and handler mappings, then `AttributeReference.resolveImpl` converts it to an empty bag when `MustBePresent=false`, or rethrows missing-attribute with its message when true. Thus round-2 finding 2 remains exploitable.

   **Fix:** Check the private `resolverFailure` field directly instead of calling an overridable method, or make the accessor final. Add tests using subclasses with missing-attribute and syntax-error statuses.

2. **MEDIUM — A selector PIP can still pass through backend syntax status and text.**  
   File: `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java:212`; downstream passthrough: `xacml-core/src/main/java/org/xacml4j/v30/policy/AttributeReference.java:134–135`.

   With no request content, a custom `PolicyInformationPoint.resolve` can throw a `PathEvaluationException` carrying syntax-error and backend details. The handler rethrows it unchanged, and `AttributeReference` does likewise. The resolver therefore chooses syntax-error instead of D3’s processing-error, and its status message reaches the response. This requires no forged provenance marker. The default PIP’s resolver boundary is fixed, but this supported PIP path remains open.

   **Fix:** Distinguish engine selector-path errors from resolver failures at their origin. Normalize resolver failures; preserve genuine XPath syntax-error codes while replacing their response-visible messages with fixed text.

For the other questions:

- Ordinary absence returned as `Optional.empty()` remains supported. Finding 1 provides a concrete failure-to-empty-bag/NotApplicable path.
- `canonicalRdns` correctly tracks backslash parity: odd runs escape commas; even runs allow splitting. Empty names form an empty terminal sequence, and trailing backslashes are retained.
- No assertion was weakened beyond the rulings. Manifest execution flags remain unchanged.

This is a diff-only review. The 7,624-test pass is supplied evidence.

VERDICT: REQUEST_CHANGES
codex exit=0
