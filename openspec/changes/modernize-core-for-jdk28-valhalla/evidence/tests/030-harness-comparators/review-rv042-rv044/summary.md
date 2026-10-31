# rv042/rv044 fixes

The comparator repair is complete. Matches are consumed once, obligation FulfillOn is checked, and omitted 3.0 response effects follow the enclosing Permit/Deny result.
Double response values use numeric equality plus a NaN/NaN oracle match. Engine DoubleVal/equality semantics were not edited by this lane.

| Review | Change | Test / proof |
| --- | --- | --- |
| rv044 finding 1 | Core lexical URI equality flows to typed comparator | differentUriSpellingsRemainRejected: 2 pairs, both directions |
| rv044 finding 3 (supersedes rv042 LOW) | Consume matching obligations; compare FulfillOn | duplicateObligationsCannotReuseOneActualMatch, obligationMultiplicityAndPermutationArePreserved, differentFulfillOnIsRejected |
| Null-field regression found in first gate | Supply the response decision for omitted FulfillOn | omittedFulfillOnUsesTheResponseDecision: Permit and Deny, both directions; both old Xacml30SamplePolicyTest responses pass |
| rv044 finding 4, conductor correction | Signed zeros equal; NaN pairs equal in oracle only | doubleOracleAcceptsSignedZeroAndNan: 5; doubleOracleStillRejectsDifferentValues: 4 |
| Conductor 02:06 follow-up | Re-enable oasis30/IIIA030 and IIIA330 | Passed in run 10 conformance and run 11 clean verify |

The pre-fix complete run 03 comparator subset is 73 tests / 6 failures / 0 errors; URI/round-trip failures are in the sibling receipt.
Run 06 is 14 / 4 / 0 / 0 red; run 07 is green after the omitted-effect adjustment. Result merges duplicate ids, so the duplicate collection test calls the production comparator reflectively; real model objects and public-response controls remain.
Run 09's six errors were a setup mistake (missing reasons), before enumeration. No fixture failed in that run. Reasons were restored and the full suite rerun.
Read comparator-review.md and run-notes.md for the rationale and the missing first run 05/06 log receipts.

Final JDK 21 clean verify: all modules pass. Java 21.0.6 (Oracle GraalVM), Maven 3.9.16.
Raw final gate: evidence/tests/030-harness-comparators/review-rv042-rv044/11-full-reactor-clean-verify.log.
The complete conformance gates are run 08 before the merge (857 passed / 23 skipped), run 10 after the merge and manifest repair, and run 11 clean (859 / 21).
No conformance case failed or regressed. IIIA030 and IIIA330 are the only outcome changes: skipped -> passed, after the conductor's 4.24 merge.
All other case rows are byte-identical; fixtures and expected results are unchanged.

| Module | Tests run | Failures | Errors | Skipped |
| --- | --- | --- | --- | --- |
| xacml-core | 4502 | 0 | 0 | 5 |
| xacml-xml | 57 | 0 | 0 | 0 |
| xacml-json | 24 | 0 | 0 | 1 |
| xacml-pdp | 77 | 0 | 0 | 3 |
| xacml-test | 99 | 0 | 0 | 0 |
| xacml-conformance-tests | 901 | 0 | 0 | 21 |
| Total | 5660 | 0 | 0 | 30 |

| Suite | Rows | Executed disposition | Special (passed / skipped) | Skipped disposition | Cases passed | Cases skipped |
| --- | --- | --- | --- | --- | --- | --- |
| oasis20 | 374 | 360 | 2 / 0 | 12 | 362 | 12 |
| oasis30 | 487 | 477 | 1 / 1 | 8 | 478 | 9 |
| rsa2008 | 17 | 17 | 0 / 0 | 0 | 17 | 0 |
| example | 2 | 2 | 0 / 0 | 0 | 2 | 0 |

Surefire invocation totals include disabled and parameterized tests. The conformance module's 901 invocations contain 880 manifest cases and 21 non-case checks. xacml-xml-jaxb has no tests.
The final 115 focused review-test invocations pass. Original test methods and assertions remain; the nine owned Java license blocks are unchanged/template-matched.
source-audit.json distinguishes ten lane source/manifest changes from the conductor merge and proves final gate source stability. Poms and 2,911 vendored fixture/schema files are unchanged.
The 21 inherited case skips remain a coverage limit. Canonical string-from-anyURI lookup still needs a separate identifier fix (StringFunctions.java:276 omits :function:); conversion semantics were verified through its existing binding.
No commit, br, task tick, issue closure, install, fixture edit or expected-result edit was performed.
