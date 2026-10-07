# Harness comparators — bugs 3.10, 3.11 and IIIE302 (3.7)

Result: the final JDK 21 full reactor `clean verify` passed. 5426 tests,
0 failures, 0 errors and 35 skips.
Conformance: 851 executed fixture cases passed; 29 remain skipped. The stricter
assignment comparison exposed two XML output defects; both are recorded below.

JDK: `Java version: 21.0.6, vendor: Oracle Corporation, runtime: /Library/Java/JavaVirtualMachines/graalvm-21.jdk/Contents/Home`.
Maven: Apache Maven 3.9.16. Each Maven run selected `jenv prefix 21` and passed
`-Dlicense.skipUpdateLicense=true`. See [command.txt](command.txt).

## Changes and test proof

| Issue | Change | Tests and receipts |
| --- | --- | --- |
| 3.10, XACML 2.0 obligations | Use the actual obligation list. Compare multisets without collapsing duplicate IDs or reusing a match. Check obligation ID and FulfillOn, assignment ID, data type, parsed value and extension attributes. Normalize absent/empty obligations. | `Xacml20TestUtilityTest`: all-field negative controls, duplicate IDs/counts, permutations, missing/extra obligations, typed values. Original source: 16 tests, 14 failures and 1 error in [01-comparators-red-results.json](01-comparators-red-results.json). Final: 18/0/0. |
| 3.10, XACML 2.0 status | Check nullable status and the full nested StatusCode chain; compare StatusDetail DOM fields rather than object identity. Keep decision and ResourceId checks. | `nestedStatusCodesMustMatch`, `statusDetailsCompareDomFieldsRatherThanNodeIdentity`, `resourceIdAndDecisionStillMustMatch`; red in run 01, green in run 08. |
| 3.10, RSA/examples | Match obligations by ID plus the full multiset of assignment values and metadata. Retain the final object equality assertion. | `XacmlPolicyTestSupportTest`: six characterization invocations already passed before the edit and still pass. The original ID matcher was followed by object equality, which already rejected ordinary value/type/issuer/category/ID differences. This is an explicit matcher change, not a claim that these six controls were previously false positives. |
| 3.11, typed values | Parse registered data types on both sides and compare value objects. Concatenate XML text chunks before parsing; strings retain case/whitespace. Unknown extension types retain exact lexical comparison; invalid registered lexical forms fail. | `lexicalFormsCompareByDataType`, `differentValuesOfTheSameTypeRemainRejected`, `stringsKeepWhitespaceAndTextChunkBoundariesDoNotMatter`, `obligationAndAdviceAssignmentsAlsoUseTypedValues`; 27.50/27.5 and integer 1/01 positive controls, 27.5/27.6 and abc/ABC negative controls. Red in run 01, final green in run 08. |
| 3.11, missing Content | Both absent Content values match; a one-sided absent value fails in either direction. Compare present Content type and DOM structure. | `absentContentIsSafeAndPresenceIsComparedSymmetrically`, `contentComparesTypeAndDomStructure`: original NPE/mismatch in run 01, green in run 08. |
| IIIE302, category order | Match included category lists as full multisets by Category, XML ID, attributes and Content. Attribute/value order is ignored; duplicate counts and all fields remain checked. | `includedCategoriesAttributesAndValuesAreMultisets`, `categoryMatchingKeepsAllAttributeFields`, `valueBagsAreUnorderedButKeepDuplicatesAndMetadata`: red in run 01, green in run 08. IIIE302 re-enabled and passed in runs 05, 09, 10 and 11. |
| Inherited assignment fields | Check `getOtherAttributes()` in both the public assignment assertion and the aggregate multiset matcher, for 2.0 and 3.0. | `assignmentExtensionAttributesMustMatch`: direct/response paths for 2.0, direct/obligation/advice paths for 3.0. All five failed before the fix in [07-assignment-metadata-red-results.json](07-assignment-metadata-red-results.json). Same values pass; changed values, missing fields and changed namespaces fail after the fix. |

Source entry points: `Xacml20TestUtility.java:76` (status), `:98` (obligations),
`:113` (assignments); `Xacml30TestUtility.java:90` (categories), `:133` (typed
content), `:152` (boolean lexical parsing), `:259` (assignments);
`XacmlPolicyTestSupport.java:201` (model obligation matcher).
The complete packet diff is [task.patch](task.patch).

XACML 2.0 `ResultType` carries Decision, Status, Obligations and ResourceId
(`xacml-xml-jaxb/target/generated-sources/xjc/org/oasis/xacml/v20/jaxb/context/ResultType.java:52-58`).
It has no included categories, AssociatedAdvice or policy identifier list to
compare. StatusMessage remains outside the oracle, matching the existing 3.0
utility (`Xacml30TestUtility.java:206` checks only the top-level status code).
The new 2.0 nested-code/StatusDetail checks are stronger than that 3.0 status
check. This packet does not widen 3.0 status comparison.

Typed lexical/value comparison follows [XML Schema Part 2, sections 2.2 and 2.3](https://www.w3.org/TR/xmlschema-2/#datatype),
with strict strings from [section 3.2.1](https://www.w3.org/TR/xmlschema-2/#string).
The four boolean lexical forms are defined by [section 3.2.2](https://www.w3.org/TR/xmlschema-2/#boolean).
Native Tavily extraction of the 1.1 page returned unrelated SVG material;
the official W3C 1.0 source was checked through the web fallback.

## Red and green runs

| Run | Fresh coverage | Tests | Failures | Errors | Skips | Result |
| --- | --- | ---: | ---: | ---: | ---: | --- |
| 01 | Three comparator classes, production unchanged | 70 | 25 | 2 | 0 | Expected red |
| 02 | Three comparator classes after initial repair | 70 | 1 | 0 | 0 | Boolean parser defect exposed |
| 03 | 3.0 comparator, before boolean oracle repair | 50 | 3 | 0 | 0 | Expected red |
| 04 | Three comparator classes | 72 | 0 | 0 | 0 | Green |
| 05 | Conformance, before assignment extension audit | 901 | 0 | 0 | 27 | Green; superseded by stronger oracle |
| 06 | Full clean reactor, before assignment extension audit | 5421 | 0 | 0 | 33 | Green; superseded |
| 07 | Three comparator classes, before extension-field fix | 77 | 5 | 0 | 0 | Expected red |
| 08 | Three comparator classes | 77 | 0 | 0 | 0 | Green |
| 09 | Full conformance with stronger assignment checks | 901 | 2 | 0 | 27 | IIIA030 and IIIA330 exposed |
| 10 | Conformance after recording the two blockers | 901 | 0 | 0 | 29 | Green |
| 11 | Final full clean reactor | 5426 | 0 | 0 | 35 | Green |

Counts above are Surefire invocations, including parameterized invocations and
skips. Focused receipts filter to the requested classes, so stale unrelated
target reports are not counted. Full run 11 follows `clean` and its XML totals
are checked against the Maven Results lines in [surefire-summary.txt](surefire-summary.txt).

## Final reactor counts

| Module | Tests run | Failures | Errors | Skipped |
| --- | ---: | ---: | ---: | ---: |
| xacml-core | 4323 | 0 | 0 | 2 |
| xacml-xml | 36 | 0 | 0 | 0 |
| xacml-json | 7 | 0 | 0 | 1 |
| xacml-pdp | 76 | 0 | 0 | 3 |
| xacml-test | 83 | 0 | 0 | 0 |
| xacml-conformance-tests | 901 | 0 | 0 | 29 |
| Total | 5426 | 0 | 0 | 35 |

Root and generated JAXB modules have no test classes. Their build phases pass.
Receipts: [11-full-reactor-results.json](11-full-reactor-results.json),
[11-full-reactor-clean-verify.log](11-full-reactor-clean-verify.log).

| xacml-test class | Historical run 024 | Final run 11 |
| --- | ---: | ---: |
| Xacml20SamplePolicyTest | 1 | 1 |
| Xacml20TestUtilityTest | 0 | 18 |
| Xacml30TestUtilityTest | 24 | 53 |
| AttributeResolverTestSupportTest | 1 | 1 |
| ExpectedAttributeResolverBuilderTest | 2 | 2 |
| Xacml30SamplePolicyTest | 2 | 2 |
| XacmlPolicyTestSupportTest | 0 | 6 |

No existing test invocations or assertions were removed. The module grew from
30 to 83 invocations: 18 new 2.0 controls, 29 added 3.0 invocations, six new
model-support controls. See [test-class-counts.csv](test-class-counts.csv).

## Conformance results and changed cases

| Suite | Manifest cases | Executed/pass | Actual skips | Executed disposition | Special harness | Skipped disposition |
| --- | ---: | ---: | ---: | ---: | --- | ---: |
| oasis20 | 374 | 362 | 12 | 360 | 2 pass / 0 skip | 12 |
| oasis30 | 487 | 470 | 17 | 469 | 1 pass / 1 skip | 16 |
| rsa2008 | 17 | 17 | 0 | 17 | 0 pass / 0 skip | 0 |
| example | 2 | 2 | 0 | 2 | 0 pass / 0 skip | 0 |
| Total | 880 | 851 | 29 | 848 | 3 pass / 1 skip | 28 |

The conformance module's 901 tests comprise 880 manifest case invocations and
21 harness/guard/helper tests. The two OASIS suite `testName` methods are
included in that helper count, not the 851 executed fixture cases.

| Case | Historical run 024 | Stronger-oracle run 09 | Final run 11 | Classification |
| --- | --- | --- | --- | --- |
| oasis30/IIIE302 | Skipped | Passed | Passed | Harness category-order defect fixed; executed with bug cleared |
| oasis30/IIIA030 | Passed | Failed | Skipped | Engine/XML output defect; obligation assignment2 loses XPathCategory; BUG-TBD-IIIA030 |
| oasis30/IIIA330 | Passed | Failed | Skipped | Engine/XML output defect; advice assignment2 loses XPathCategory; BUG-TBD-IIIA330 |

Those are the only three outcome/manifest changes; the other 877 are unchanged.
[per-case-delta.csv](per-case-delta.csv) records all 880 outcomes before, during
the strict probe and after. [11-final-case-results.csv](11-final-case-results.csv)
records every final disposition, reason and outcome.

No newly exposed OASIS 2.0 failure. All 362 enabled cases passed, including the
15 with nonempty expected obligations listed in [field-coverage.json](field-coverage.json).
Among enabled 3.0 fixtures, 22 carry expected obligations and one carries
included attributes (IIIE302). No enabled fixture in either suite supplies a
nonempty StatusDetail; status-detail proof is focused unit-test coverage only.

IIA022, IIA024 and IIIA340 remain disabled with policy4j-the.4.16, unchanged.
They were not executed or temporarily re-enabled. Typed-value and absent-Content
unit tests now cover the harness portions; their engine serialization fixes
remain in Luna's lane. The other inherited exclusions were also preserved.

## Blockers and residual risk

The two new case blockers are reproduced through the existing PDP harness and
actual XML response marshaller in [09-assignment-metadata-probe.json](09-assignment-metadata-probe.json).
Expected assignments retain `XPathCategory=urn:oasis:names:tc:xacml:3.0:attribute-category:resource`;
both actual assignments have an empty extension map. The read-only cause is
`Xacml30RequestContextFromJaxbToObjectModelMapper.java:424-433`: it copies the
typed value's data type/content, but discards `av.getOtherAttributes()`.
`TypeToXacml30.java:466` supplies XPathCategory to that typed-value map.
Fixing the mapper is outside this packet. No fixtures or expectations changed.
See [conformance-blockers.md](conformance-blockers.md) for the proposals.

A separate pre-existing core parser observation was found while building a
typed oracle: `BooleanVal.java:49` uses Java `Boolean.parseBoolean`, interpreting
XML Schema `1` and arbitrary invalid strings as false. Run 03 demonstrates the
three comparator failures before the harness normalizes the four legal boolean
forms and rejects invalid ones. Core was not edited. Broader engine/conformance
impact is UNVERIFIED; the conductor can file a separate bug if needed.

Mixed DOM content is compared structurally and in sequence, not by a semantic
XML canonicalizer. Registered-type equality uses the current core value types;
this packet does not repair their semantics. Remaining disabled cases are not
claimed as passing. No JMH run is needed for a test-oracle change.

## Scope and headers

[source-audit.json](source-audit.json) compares packet-start and final hashes:
only the six named xacml-test Java files and three manifest rows changed.
All other module source files, 8 poms, and 2911 vendored
fixture/XSD files retained their bytes. Existing LGPL headers are byte-identical;
new test headers copy the module template. Before/after run 11 snapshots are
identical; current source bytes match the final gate snapshot. The shared tree's
older dirty files were preserved. No commit, beads command, task tick or issue
closure was performed. Evidence uses `<repo>`/`<home>` in place of checkout/home
paths.
Final artifact checks: [final-checks.json](final-checks.json) records the scoped
Git status/diff check, zero deleted existing 3.0 comparator-test assertions,
valid local evidence links and no unredacted checkout/home paths.
## rv042/rv044 fixes

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
../024-bugs-string-functions/review-rv038-rv039/source-audit.json distinguishes ten lane source/manifest changes from the conductor merge and proves final gate source stability. Poms and 2,911 vendored fixture/schema files are unchanged.
The 21 inherited case skips remain a coverage limit. Canonical string-from-anyURI lookup still needs a separate identifier fix (StringFunctions.java:276 omits :function:); conversion semantics were verified through its existing binding.
No commit, br, task tick, issue closure, install, fixture edit or expected-result edit was performed.

Detailed review receipts: [review-rv042-rv044/summary.md](review-rv042-rv044/summary.md).
