# rv078 task 4.6 numeric and harness review

Persona: grok. Session model label: Grok 4.7. Exact serving variant: UNVERIFIED.
Change: modernize-core-for-jdk28-valhalla. Task 4.6. Beads: policy4j-the.4.6.
Verdict: **APPROVE** the hashed numeric and harness diff. This does not close task 4.6 or gate 4.7.

## Hash recheck

`rtk proxy shasum -a 256` on 2026-10-04, after the source read, matches `numeric-harness-hashes.json` for all 11 scoped paths. The same digests matched the opening check. Preservation versus `evidence/tests/068-resume-jdk21-baseline/source-hashes-before.json`: `IntegerVal.java` `813a56a0…`, `DoubleVal.java` `84d2f22d…`, `Value.java` `213c477d…`, `BagOfValues.java` `fedcbc4d…`, `XPathFunctions.java` `94fe7db5…`, and vendored `IIC350Response.xml` / `IIC358Response.xml` `82069222…` are unchanged. Combining sources were not reviewed.

## Findings

No high or medium defect in the hashed production paths.

1. **Low.** `FunctionSafetyNet43Test.java:604` `row43IntegerModByZeroIsIndeterminate` is `@Disabled("C6")` and is one of the seven green skips. That skip is the integer-mod zero case, so calling all seven skips unrelated overstates the gap. `IntegerVal.mod` (`IntegerVal.java:114-116`) uses Java `%` and throws `ArithmeticException` on a zero divisor. `FunctionSpecBuilder.java:396-401` maps that `Exception` to `FunctionInvocationException`. `ArtimeticFunctionsTest.java:159-162` asserts `processing-error`, and green Surefire cases `zeroDivisorsMapToProcessingError` [3] and [4] have no failure element. Smallest fix: re-enable row43, or name C6 as duplicated rather than unrelated. Test: row43, or the existing arithmetic rows.

2. **Low.** `NaNFixtureAdjudicationTest.java:84-86` and `:100-102` assert only `Decision.NOT_APPLICABLE`. A NotApplicable result with a non-ok status or dropped obligations would pass. `ConformanceCaseRunner.java:163` still calls `Xacml30TestUtility.assertResponse` on `expectedResponse30`. Harness GREEN has not run, so that full comparison is unproven. Smallest fix: compare the marshalled PDP response to `expectedResponse30` in those two tests. Test: `iic350PolicyAndPdpDecidesNotApplicable` and `iic358PolicyAndPdpDecidesNotApplicable`.

## What holds

`EqualityPredicates.doubleEquals` (`EqualityPredicates.java:194-196`) is primitive `==`: NaN is unequal to itself, and `0.0 == -0.0`. `BagFunctions.doubleContains` (`BagFunctions.java:96-102`) and the double intersection, union, subset, set-equals, and at-least-one-member-of scans (`SetFunctions.java:186-264`) use that predicate. Union keeps every NaN because NaN never matches; signed zeros collapse as one member. `assertDoubleBag` (`DoubleBagSetEqualityTest.java:360-372`) checks type, size, NaN count, zero count, and non-zero finites without calling bag equality or the function under test. `javaEqualityAndHashCollectionsRemainReflexive` (`DoubleBagSetEqualityTest.java:254-272`) still requires Java `equals`/`hashCode`: NaN equals NaN, and `+0` does not equal `-0`.

`integer-divide` (`ArithmeticFunctions.java:160-168`) returns `IntegerVal` from Java long division. Toward-zero is the project convention: `7 / -3` is `-2` (`FunctionSafetyNet43Test.java:564-569`). The delta spec records that A.3.2 requires the integer type and does not state the truncation rule (`spec.md:3-7`). `IntegerVal.divide` still returns `DoubleVal` (`IntegerVal.java:109-111`) and its 068 hash is unchanged. Zero divisors for divide and mod are `processing-error` through the registry. Values above 2^53 and `Long.MIN_VALUE / 1` are in `ArtimeticFunctionsTest.java:124-128`. `Long.MIN_VALUE / -1` remains task 5.4.

`expectedResponse30` (`ConformanceCaseRunner.java:172-190`) rewrites only `oasis30` plus `special-harness` plus `IIC350` or `IIC358`, and only a single Permit result, to NotApplicable. `Xacml30TestUtility.getResponse` (`Xacml30TestUtility.java:312-318`) unmarshals a new object each call. `Xacml30ConformanceTest.java:46-47` runs every enabled case, so special-harness is not skipped. CSV rows for both IIC020 cases are `executed,true`; IIC350 and IIC358 are `special-harness,true` (`conformance-manifest.csv:95,474,706,714`). Those four rows match `manifest-comparison.json` `changed_rows`. That file reports 880 rows, 864 prior enabled identities kept, and added enabled keys only `oasis20/IIC020` and `oasis30/IIC020`. This lane spot-checked the four rows and did not recompute the full set.

XPath: `spec.md:94-116` matches the rv077 adjudication. Missing content is count `0` and equal/match `false`. Invalid evaluation stays Indeterminate. `XPathFunctions.java` matches its 068 hash, so task 4.6 did not change that production file. `FunctionSafetyNet43Test.java:332-433` locks the split, including a missing side short-circuiting a failing side (`:384-399`). Those rows are in the green 109/0 result. Whether A.3.15 expressly orders that mixed case ahead of evaluation failure was not re-fetched. The local decision record states the missing-content versus failure split. UNVERIFIED against a fresh OASIS fetch.

## Prior dispositions

IEEE primitive equality, unchanged Java `Value` equality, policy equality on double bag and set functions, integer-divide as `xs:integer` with project truncation, and an unchanged `IntegerVal.divide`: accepted. Source and 068 hashes agree. IIC350 and IIC358 stay enabled with an in-memory NotApplicable expectation and untouched vendored Permit bytes. Both IIC020 cases are enabled again. The rejected “missing XPath content is an error” proposal stays rejected. on-permit-apply-second was not reviewed.

## Verification and limits

No Maven, tests, network, or git writes in this lane. Opening `shasum` and closing `rtk proxy shasum -a 256` both match the 11-file manifest. Green Surefire headers: arithmetic 33/0, double bag 108/0, safety net 109/0 with 7 skips. Sum 250. The combined receipt exit code is 1 because combining suites failed (252, 8, and 4 failures). Those files were not reviewed. Red counts 252/63/2/7 and harness 5/3 match the receipts. Harness GREEN is absent. Overflow, n-ary union, and whole-engine acceptance are out of scope. This lane does not claim the conformance suite passed.

DONE_REVIEW_GROK_20261004
