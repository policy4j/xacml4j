# Task 4.17: missing request AttributeId

## Result

XACML 2.0 and 3.0 XML request mappers now convert an absent or empty `AttributeId` into `SyntaxException`; the JSON attribute deserializer does the same and the request unmarshaller unwraps the Gson parse wrapper. Tests assert that the exception status code is `urn:oasis:names:tc:xacml:1.0:status:syntax-error`. The previous behavior was `IllegalArgumentException` from `Attribute.Builder`.

Red evidence: the XACML 2.0 and 3.0 XML tests initially failed with `IllegalArgumentException` from `Attribute.Builder`. Reverting the JSON conversion temporarily also made its focused test fail with `IllegalArgumentException: Property 'AttributeId' is mandatory.` The final focused XML and JSON tests pass.

## Limitation / not ready

The packet asks for an `Indeterminate` `ResponseContext` with syntax-error status. The current architecture unmarshals to `RequestContext` before `PolicyDecisionPoint.decide(RequestContext)`; `RequestUnmarshaller` has no response-returning error channel. These changes correctly expose a syntax-error `SyntaxException`, but do not manufacture a PDP response. The conformance runner's IIA005 cases remain excluded, and the expected response is not yet verified end-to-end. A transport/harness integration decision is needed before this task can be called complete.

The full reactor remains green for all currently enabled cases; the 2.0 and 3.0 IIA005 fixture requests are still skipped by the unchanged runner exclusion sets.
