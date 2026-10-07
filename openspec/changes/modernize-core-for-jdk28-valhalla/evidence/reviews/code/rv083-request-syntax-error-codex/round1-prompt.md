# Adversarial code review: request syntax error in the conformance harness (xacml4j)

You are an independent reviewer. Do not use tools. Do not edit files. Answer
in one message. Review only the diff below. It is the complete diff of this
change. The full reactor passes on it: 7,709 tests, 0 failures, 0 errors,
22 skipped. The conformance module runs 935 tests with 12 skipped; before
the change it ran 927 with 14 skipped (8 new harness tests, and the two
IIA005 cases now run and pass).

## Project

xacml4j is a Java 21 implementation of OASIS XACML 2.0/3.0.
`xacml-conformance-tests` runs the OASIS conformance suites. A CSV manifest
lists every case with a disposition (`executed`, `special-harness`,
`skipped`), an enabled flag, a reason, and a bug id. The vendored OASIS
fixtures are read-only.

## Maintainer ruling this change implements (2026-10-07, D12)

Keep `PolicyDecisionPoint.decide(RequestContext)`. A request that cannot be
parsed is mapped to an Indeterminate response with status syntax-error by
the adapter or harness in front of the PDP, not by the PDP.

## Case

IIA005 (OASIS 2.0 and 3.0): the request has an `Attribute` without
`AttributeId`. The expected response is one result: Decision Indeterminate,
StatusCode `urn:oasis:names:tc:xacml:1.0:status:syntax-error`, no message.
Both request unmarshallers throw
`SyntaxException("Request Attribute is missing AttributeId")` with status
syntax-error. Before this change both rows were `skipped,false`.

Facts you cannot see in the diff:

- `SyntaxException` is an unchecked exception with a `getStatus()`.
- `Result.indeterminate(Status)` returns a builder for a result with
  Decision Indeterminate and that status.
- The response comparison checks the decision, the status code, and the
  other result parts; the expected IIA005 response has no status message.
- `ConformanceManifest.Case` is a record: suite, caseId, requestFile,
  policyFiles, expectedResponseFile, disposition, enabled, reason, bugId.
- The same file already maps two named static policy rejections (IIA004,
  IIC012) for `special-harness` cases in `expectedStaticRejection`.
- A reason must not hold a comma. Enabled `special-harness` rows have an
  empty bug id.

## Questions

Report findings ordered by severity (HIGH, MEDIUM, LOW), each with file,
line from the diff, a concrete failing scenario, and a suggested fix. Report
only defects you can show from the diff and the facts above.

1. Can the mapping hide a real defect: can a case other than IIA005, or a
   different syntax error in IIA005, pass because of it?
2. Does a `SyntaxException` from `pdp.decide` (not from parsing the request)
   reach the mapping, and is that a problem for IIA005?
3. Do the four tests pin the mapping (named case only, special-harness only,
   response still compared)? Name a missing test.
4. Is the manifest change consistent (disposition, enabled flag, reason, bug
   id)?

End with a verdict line: `VERDICT: APPROVE` or `VERDICT: REQUEST_CHANGES`.

## Diff

```diff
