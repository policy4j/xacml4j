# XML request and response mapping bugs

## Result

The rv040/rv043 review findings are fixed in this lane. Core, XML, PDP, and
JSON JDK 21 gates pass. The current conformance run passes with the committed
manifest state, but IIIE301 and IIIE303 remain excluded there. Their earlier
temporarily enabled run reaches the known test-support comparison NPE for
absent `Content`; that harness fix is outside this lane and is present in
the main worktree. No harness, manifest, fixture, or generated schema was
changed for this follow-up.

## Review fixes and evidence

| Finding | Test first (red) | Fix | Green evidence |
| --- | --- | --- | --- |
| rv043 M: duplicate request `xml:id` must raise syntax-error | `review-xml-red.log`: XML request with two `Attributes xml:id="dup"` threw Guava `IllegalArgumentException` | `RequestContext` now detects a repeated ID and throws `SyntaxException` | `review-xml-green.log`, then `review-xml-final.log` |
| rv043 M: `RequestContext.equals` compared entities to itself | `review-core-red.log`: unequal entities and unequal IDs incorrectly compared equal | Compare `entities` with `r.entities` | `review-core-green.log`, then `review-core-final.log` |
| rv043 M / rv040 L: XACML 2.0 resource scope URI | `review-xml-red.log`: XACML 2.0 scope left resource-id out of IncludeInResult | Recognize both 1.0 and 2.0 `resource:scope` URNs | `review-xml-green.log`, then `review-xml-final.log`; response marshalling asserts `ResourceId="urn:root"` |
| rv040 L: JAXB `AttributesType` request references | Happy-path and missing-ID cases added for `xml:id` | Existing mapper resolves the ID and reports missing ID as `SyntaxException`; no production change was needed | Both new cases pass in `review-xml-green.log` |

The equality tests also check equal requests have equal hash codes, requests
that differ only by ID are unequal, and requests with different entity values
are unequal. Category equality/hash behavior was audited: its equality checks
category, reference ID, entity, and the derived cached reference; its hash
uses category, reference ID, and entity. No analogous self-comparison defect
was found.

The duplicate-ID test calls the 3.0 XML request unmarshaller and asserts the
exception status is `StatusCodeId.SYNTAX_ERROR`. The two reference mapping
tests cover an `AttributesType` with an ID and without an ID.

## Earlier mapping changes retained

- OASIS 2.0 IIIC001: XACML 2.0 response mapping emits the request resource ID
  when the request is multi-resource or has a supported resource-scope
  attribute. The existing focused case test and prior temporary-enabled
  conformance evidence remain in `resourceid-green.log` and
  `conformance-final.log`.
- OASIS 3.0 IIIE301: accepts the draft multiple-content-selector URI as an
  input alias and retains XPath result order.
- OASIS 3.0 IIIE303: maps JAXB `AttributesType` IDREFs by their XML ID,
  preserves legacy String references, and rejects missing/unsupported
  references as syntax errors.

The earlier enabled-case conformance run reached the existing
`Xacml30TestUtility.assertAttributesType` absent-`Content` NPE for IIIE301
and IIIE303. This follow-up intentionally does not change test-support code.
The current restored manifest rows are still skipped, so a green full-suite
exit does not prove those two cases in this lane.

## Final JDK 21 module gates

| Module command target | Tests | Failures | Errors | Skips |
| --- | ---: | ---: | ---: | ---: |
| xacml-core | 4,328 | 0 | 0 | 2 |
| xacml-xml | 42 | 0 | 0 | 0 |
| xacml-pdp | 77 | 0 | 0 | 3 |
| xacml-json | 7 | 0 | 0 | 1 |
| xacml-conformance-tests | 901 | 0 | 0 | 28 |

Core/XML final logs are `review-core-final.log` and `review-xml-final.log`.
The PDP and JSON gates are recorded in `review-pdp-full.log` and
`review-json-full.log`. The conformance gate is `review-conformance.log`.
Its markers are 362 OASIS 2.0, 471 OASIS 3.0, 17 RSA 2008, and 2 example
passes. IIIE301 and IIIE303 are not among those markers because their
manifest dispositions remain skipped.

## Readiness

Ready to apply the in-scope changes. The four requested JUnit gates are green,
and the 2.0 marshaller now verifies the actual Result ResourceId. The
conformance harness NPE for temporarily enabled IIIE301/IIIE303 is documented
and must be rerun after the harness fix already merged in the main worktree.

