# Additional IIIE3xx checks: proposals, not applied

The configured run 53 gate passes. These three vendored cases are outside its
unchanged OASIS group bounds. DiagnoseXPathMultipleDecisions.java independently
loads each unchanged policy/request, uses the production PDP and default
multiple-decision request handlers, marshals the response, and calls all
unchanged Xacml30TestUtility.assertResponse assertions against the original
response fixture. Runs 50 (before any review production edit) and 54 (after
the full gate) each execute three cases and fail all three identically.
The utility catches each failure to let the other diagnostics run; it exits
nonzero if any fails. No fixture, expected result or assertion was changed.

These are additional coverage blockers; they do not contradict the green
configured gate. Base snippets are confirmed unchanged from a8a90d46 in
55-review-baseline-snippets.json. No adaptation regression is shown.

| Case | Observed before/after | Classification and source | Proposed repair and proof, pending scope |
| --- | --- | --- | --- |
| IIIE301 | One Indeterminate response instead of Permit + NotApplicable; expected result count 2, actual 1. | Fixture compatibility: IIIE301Request.xml:65 uses draft urn:oasis:names:tc:xacml:3.0:multiple:content-selector. MultipleResourcesViaXPathExpressionHandler.java:58 recognizes the profile:multiple form, so expansion is bypassed and explicit policy context selectors fail. Both identifiers predate these adaptations. | Decide whether to support the draft attribute URN as a compatibility alias. If approved, add a failing production expansion test first; recognize the alias in the handler's detection, category lookup and content-selector transformation while retaining the canonical URI. Re-run the exact unchanged-fixture diagnostic and all existing expansion tests. Do not change the fixture. |
| IIIE302 | Actual and expected decisions both Permit + NotApplicable, but category comparison expects resource at position 0 and gets access-subject. | Pre-existing harness: Xacml30TestUtility.assertAttributesType():84–93 compares categories by list position, while request/response category ordering differs. The comparator body matches a8a90d46. This first failure is observed; correctness of later assertions is UNVERIFIED. | Add a failing comparator test with reordered equivalent categories, then compare categories by category/id while retaining multiplicity and every metadata/value/content assertion. Characterize absent Content as well before any null-handling repair. Re-run IIIE302 without changing its expected response. |
| IIIE303 | Request unmarshalling throws ClassCastException: JAXB AttributesType cannot be cast to String. | Pre-existing XML defect: Xacml30RequestContextFromJaxbToObjectModelMapper.java:495 casts getReferenceId() to String; JAXB IDREF resolves this fixture's reference to an AttributesType. This cast matches a8a90d46 and is separate from approved Content selection. | Add a failing production request-unmarshaller test with two referenced Attributes elements. Map a JAXB AttributesType IDREF to its id, preserve supported string references, and reject unsupported objects explicitly. Check both resulting references and unchanged IIIE303 response assertions. |

The canonical selector attribute is defined by the
[OASIS XACML 3.0 Multiple Decision Profile, Committee Specification 02, 18 May 2014, section 2.1](https://docs.oasis-open.org/xacml/3.0/multiple/v1.0/cs02/xacml-3.0-multiple-v1.0-cs02.html).
It includes profile:multiple; the vendored v0.4 request does not. This is a
compatibility decision, not a proposal to replace the canonical constant.

Required authorization would cover the PDP attribute alias, the harness
comparison behavior, and JAXB IDREF mapping, respectively. The earlier
identifier grant permits core function/algorithm/type registration annotations
only; the Content grant does not cover IDREFs. These proposals were not applied.
