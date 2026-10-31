# Conductor-copied native final report

FINAL REPORT (policy4j_code_review, claude-opus-5-5)

Persona name is inferred from the role prompt: UNVERIFIED. Model ID is the one the harness states; serving variant UNVERIFIED. `opus/report.md` was not written because my persona's read-only rule is binding. This block is the record for the conductor to copy.

## Verdict: APPROVE

Numeric and harness source match the oracle. I found no defect that breaks an oracle item. Runtime acceptance stays pending on the conductor's harness GREEN. Findings 1–6 are follow-ups, not blockers.

## Summary

- Hashes: 11/11 match `numeric-harness-hashes.json`, first and last.
- `double-equal` is primitive `==`: NaN false, zeros equal (`EqualityPredicates.java:194-196`).
- Double is-in, union, intersection, subset, set-equals and at-least-one-member-of scan with that predicate. Union keeps every NaN occurrence (`BagFunctions.java:96-103,290-296`; `SetFunctions.java:188-264`).
- `integer-divide` declares and returns `xs:integer` by long division (`ArithmeticFunctions.java:160-169`). The comment, tests and `spec.md:4-7` call truncation a project convention.
- `IntegerVal`, `DoubleVal`, `Value` and `BagOfValues` equal their 068 preflight hashes. Java equality and legacy `IntegerVal.divide` are unchanged.
- Manifest: 880 rows; enabled 864 to 866; none dropped; added `oasis20/IIC020` and `oasis30/IIC020`; exactly four rows differ (lines 95, 474, 706, 714).
- Adjudication is guarded by suite, disposition, case id and single-result Permit (`ConformanceCaseRunner.java:172-191`). `getResponse` unmarshals fresh per call (`Xacml30TestUtility.java:312-322`). Full-result comparison is retained (`:73-90`). IIC350/IIC358 fixtures equal their 068 hashes and are clean in `git status`.

## Findings

1. **MEDIUM** `BagFunctions.java:303-306`, pre-existing, partly fixed. The registry passes `null` varargs for zero arguments (`DefaultFunctionInvocation.java:128-129`). Only `doubleBag` guards it. The other sixteen `type-bag` functions (for example `:147-154`) reach `BagOfValues.java:312-318` and throw.
   - Scenario: `string-bag-size(string-bag())` is Indeterminate, not `0`, against the A.3.10 text quoted at `BagFunctions.java:53-57`. Desk-checked; runtime UNVERIFIED. Other variadic functions were not inspected.
   - Fix: pass an empty typed array at the registry.
   - Test: a zero-argument row for every `type-bag` URN. Outside the 4.6 oracle; track for gate 4.7.

2. **LOW** `SetFunctions.java:266-269`, `BagFunctions.java:294`: the new type guards have no covering test. `DoubleBagSetEqualityTest.java:316-326` runs with validation on (`:69`), so `FunctionSpecBuilder.java:367-378` rejects first. None of the RED failures is in those rows. The PDP default is validation off (`PolicyDecisionPointContextFactory.java:80,182`).
   - Scenario: delete the guards and every test still passes. With validation off, `double-union(doubles, emptyIntegerBag)` then returns a bag, against `spec.md:71-78`.
   - Fix and test: repeat the rows with `ModelFixtures.context(false)` and an empty integer bag.

3. **LOW** `FunctionSafetyNet43Test.java:604-611`: row43 (`integer-mod` by zero) stays `@Disabled("C6")`. `ArtimeticFunctionsTest.java:161-162` asserts the same result GREEN, and `spec.md:8-9` requires it. One of the seven "unrelated" skips is decided.
   - Fix: enable row43 and refresh the Javadoc list at `:69`.

4. **LOW** `ArtimeticFunctionsTest.java:108-129`: `Long.MIN_VALUE / -1` wraps silently at `ArithmeticFunctions.java:168`. The omission is tracked only in prose (`numeric/report.md:88-89`). Rows 35/36 cover abs and add.
   - Fix: add a `@Disabled("C3 task 5.4")` divide row.

5. **LOW** `ConformanceCaseRunner.java:175-185`: the guard does not bind the fixture.
   - Scenario: an IIC350 row pointed at any other single-result Permit response is rewritten to NotApplicable. `NaNFixtureAdjudicationTest.java:136-151` checks the decision, not bytes. `VendoredFixtureIntegrityTest` was not inspected.
   - Fix: also require `expectedResponseFile` to be `xacml3.0-ct-v.0.4/<caseId>Response.xml`.
   - Tests: wrong path, multi-result fixture, and `executed` IIC350 left unadjudicated.

6. **LOW** `spec.md:102-104`: the invalid-XPath scenario omits "Content present". Code and tests give missing Content precedence (`XPathFunctions.java:61-66,86-92`; `FunctionSafetyNet43Test.java:384-400`).
   - Fix: qualify the scenario and add a precedence scenario.
   - The Indeterminate path is tested only through a throwing mock (`:332-353`).

## Prior finding dispositions (rv077)

1. IEEE equality with reflexive Java equality: implemented, source-verified.
2. `integer-divide` type and convention wording: implemented.
3. on-permit-apply-second: not reviewed (combining files excluded).
4. Conformance identities, IIC020, IIC350/358: implemented in source; runtime pending.
5. XPath missing Content: delta corrected (`spec.md:94-116`), consistent with rv077 item 5. Invalid evaluation stays Indeterminate. A real malformed-XPath test remains gate 4.7 work.
6. PIP and other semantics: out of scope.

Short-ID alias rows were removed (35 to 33 tests). The count matches the current file.

## Evidence and proof limits

- Receipts agree: RED 252/63/2/7; GREEN 250/0/0/7; harness RED 5/3.
- GREEN covers three classes, inside a run that exited 1 on combining RED. No full `xacml-core` or reactor run followed.
- GREEN receipts carry no source hashes. Scoped files' mtimes (latest 12:55:43) precede the receipt (12:59:05): consistent, not proof.
- Three `expectedResponse30*` tests post-date harness RED (12:41:58 against 12:37:54) and have never run. Desk-checked only.
- IIC020 in both suites and the IIC350/358 rows are not executed yet.
- IIC351–355 and IIC359 desk-checked: IEEE equality does not change their expected decisions.
- Standards text was not re-fetched. I relied on the decision record and rv077.

## Verification run

`rtk proxy shasum -a 256` twice; scoped `rtk git status` and `rtk git diff HEAD`; `rtk rg`; `rtk proxy jq` comparing `manifest-before.json` with the CSV; file reads. No builds, tests, network or writes.

## Residual risk

- The `integer-divide` return-type change rejects policies that fed its result to double functions.
- Overflow, union of three or more bags, and O(n·m) double scans are untested.
- Whole-engine compliance is not claimed.

DONE_REVIEW_OPUS_20261004
