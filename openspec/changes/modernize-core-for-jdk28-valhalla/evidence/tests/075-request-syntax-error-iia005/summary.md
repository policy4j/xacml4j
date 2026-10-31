# Run 075: request syntax error of IIA005 in the harness (bead the.4.17)

Date: 2026-10-07. Ruling: run 061 D12, option (a)
(`builds/070-maintainer-rulings`). JDK: Oracle GraalVM 21.0.6+8.1
(`java version "21.0.6" 2025-01-21 LTS`). Review:
`reviews/code/rv083-request-syntax-error-codex`.

## Case

| | OASIS 2.0 `IIA005` | OASIS 3.0 `IIA005` |
| --- | --- | --- |
| Request | `Attribute` without `AttributeId` | the same |
| Expected response | Indeterminate, `status:syntax-error` | the same |
| Engine before | `SyntaxException("Request Attribute is missing AttributeId")` from the request unmarshaller; no response | the same |
| Manifest before | `skipped,false`, bug policy4j-the.4.17 | the same |
| Manifest after | `special-harness,true` | the same |

## Change

The ruling keeps `PolicyDecisionPoint.decide(RequestContext)` and puts the
mapping in the adapter in front of the PDP. For the conformance suite the
adapter is the harness.

| File | Change |
| --- | --- |
| `xacml-conformance-tests/src/test/java/org/xacml4j/v20/ConformanceCaseRunner.java` | `verifyOasis` catches a `SyntaxException` from unmarshalling the request only; an exception from `decide` is not mapped. `expectedRequestSyntaxError` accepts it only for a `special-harness` case with id `IIA005`, status syntax-error, and the message `Request Attribute is missing AttributeId`. The harness then builds `Result.indeterminate(Status.syntaxError())` and compares it with the vendored response like any other response. Every other `SyntaxException` is thrown again. |
| `xacml-conformance-tests/src/test/manifest/conformance-manifest.csv` | The two `IIA005` rows: `skipped,false,...,policy4j-the.4.17` becomes `special-harness,true,<reason>,` (empty bug id). No other row changes. |
| `xacml-conformance-tests/src/test/java/org/xacml4j/v20/ConformanceRequestSyntaxErrorTest.java` (new) | Five tests for both suites, see below. |

No production code and no vendored fixture changes.

## Tests

| Test | Case built from | Expected |
| --- | --- | --- |
| `mapsTheRequestSyntaxErrorOfIIA005ToTheExpectedResponse` | IIA005, `special-harness` | passes |
| `theMappedResponseMustStillMatchTheExpectedResponse` | IIA005 with the response fixture of IIA001 | comparison fails: expected PERMIT, actual INDETERMINATE |
| `doesNotMapTheSyntaxErrorForAnotherCase` | case id IIA001 with the IIA005 request | `SyntaxException` |
| `doesNotMapTheSyntaxErrorForAnExecutedCase` | IIA005, `executed` | `SyntaxException` |
| `acceptsOnlyTheMessageOfTheMissingAttributeId` | the guard called directly | true for the named message; false for another message, for `executed`, and for another case id |

- Red first: `red-surefire-summary.txt` (8 tests, 4 failures: the two
  mapping tests for both suites; the two negative controls already pass).
- Gate: `surefire-summary.txt`, gate 2. Full reactor on JDK 21: 7,735
  tests, 0 failures, 0 errors, 22 skipped. Conformance module: 937 tests,
  12 skipped (before this run: 927 and 14). The output has
  `CONFORMANCE REQUEST REJECTION` and `CONFORMANCE PASS` for
  `oasis20/IIA005` and `oasis30/IIA005`.
- The gate tree also held runs 074 (bead the.4.12) and 076 (bead
  the.4.53).

## Review

Codex, one round: APPROVE, no defect
(`reviews/code/rv083-request-syntax-error-codex`). It named two
improvements, both applied after the review: map only the request parsing
(not `decide`), and test the guard with another message
(`acceptsOnlyTheMessageOfTheMissingAttributeId`). Those two edits were not
sent back for review; gate 2 ran on them.

## JMH

NOT APPLICABLE: test harness and manifest only.

## Limits

- The engine itself still has no API that takes raw request bytes and
  returns a response for a request that cannot be parsed. A caller of the
  library has to catch `SyntaxException` from the unmarshaller, as the
  harness does. That is the ruling (option a), not a gap of this run.
- The mapping matches the exception message text. If the unmarshaller
  message changes, the two cases fail loudly instead of passing by another
  path.
