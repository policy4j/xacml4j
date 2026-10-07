# Time reference-day, base64 lexical, and combining fingerprints

## Changes

- `ISO8601Time` now compares, tests equality, and hashes using the UTC nanosecond position relative to the XPath time reference date. `time-in-range` folds the first argument into the daily cycle beginning at the converted start bound before checking the inclusive interval. Unzoned values use UTC for equality and ordering; `time-in-range` continues to use its context timezone for an unzoned first argument.
- Added coverage for `08:00:00+09:00` versus `17:00:00-06:00`: they are unequal, and the first is earlier. Equivalent instants (`12:00:00Z` / `13:00:00+01:00`) and unzoned/UTC (`23:00:00` / `23:00:00Z`) remain equal with matching hashes. A range crossing the reference-day boundary is covered alongside the existing midnight and GMT-08 cases.
- `Base64Binary` now validates XML Schema base64 padding and alphabet before decoding, accepts XML whitespace, and reports invalid lexical values as `SyntaxException` (syntax-error). Updated the registry sample to padded `YQ==` and updated the existing malformed-input test to expect the syntax error.
- Added registry behavior vectors using real rules for legacy versus XACML 3.0 deny/permit-overrides. The vectors exercise `{D}` and `{P}` indeterminates, their `{DP}` combined results, and direct `{DP}` child results for XACML 3.0. A test comment records that fixed result lists cannot distinguish ordered from unordered visitation.

## rv062 follow-up

- The registry now evaluates all **23 registered URNs** on the same four requested vectors: `NotApplicable-only`, `Permit+Deny`, `Deny+Indeterminate{D}`, and `Permit+Indeterminate{D}`. Appendix C expectations are pinned for XACML 3.0 algorithms; legacy URNs use their Appendix C legacy behavior. Ordered and unordered variants share the decision table; fixed result lists do not pin visitation order.
- Enabled AnyURI lexical rows with the merged 4.15 behavior: `http://example.com/a/../b` and `http://example.com/a%20b` preserve their original spellings.
- `scalarLexicalValueRoundTrips` now takes an expected lexical literal for every row and checks the formatter against it before reparsing. The dayTimeDuration row pins the current output `P0DT0H0M1.5S` for input `PT1.5S`.
- Both missing-attribute branches in `AttributeDesignatorFunctions` now use the lexical AttributeId for the missing-attribute status and exception message; tests assert the message for category and entity lookups.
- `rv062-red.log` captures the initial test-first run. It exposed the status-message mismatch and an incorrect duration expectation; provisional combining expectations were then corrected against Appendix C before the passing run recorded in `rv062-green.log`.

## Test-first evidence

`focused-red.log` records the new tests failing before implementation: the two offset times compared equal, time ordering/range lost the reference day, and unpadded base64 was accepted. `focused-green.log` records the focused time/base64/registry suites green, including the existing malformed-input test updated to require syntax-error. `combining-followup.log` covers the final added direct `{DP}` child vectors.

## Verification

- JDK 21 focused tests before rv062: 122 tests, 0 failures, 0 errors, 4 skips. The rv062 focused run covers TypeRegistryTest, DecisionCombiningAlgorithmRegistryTest, and AttributeDesignatorFunctionTest: 200 tests, 0 failures, 0 errors, 3 skips.
- Final JDK 21 `clean verify`: reactor build success; conformance module reports 917 tests, 0 failures, 0 errors, 17 skips.
- Explicit final conformance run: OASIS 2.0: 375 test methods, 0 failures/errors, 10 skips; OASIS 3.0: 488, 0 failures/errors, 7 skips; RSA 2008: 17, 0 failures/errors/skips; examples: 2, 0 failures/errors/skips. Manifest and static-rejection harness tests also pass (13 and 16, respectively). No conformance cases failed.
- Maven was passed `-Dlicense.skipUpdateLicense=true`. The lane is a private copy without `.git`; no source or fixture outside the listed xacml-core files and this evidence directory was edited.

## rv065 positive-offset correction

- Added positive-offset regressions for an in-range value, both inclusive bounds, and a value one second before the lower bound. Added midnight-wrapping intervals with positive and negative offsets. Existing unzoned values with a non-UTC context timezone remain covered.
- Test-first evidence: `rv065-red.log` fails on `08:00:00+09:00` inside `07:00:00+09:00`–`09:00:00+09:00` (expected true, actual false) with the previous reference-day shift.
- Replaced the reference-day shift with `timeInCycle = start + Math.floorMod(time - start, day)` after extending the end by one day for a wrapping interval. This avoids moving bounds away from negative UTC-reference values and keeps both endpoints inclusive.
- JDK 21 focused time-in-range suite: 14 tests, 0 failures/errors/skips. `-pl xacml-core clean test`: BUILD SUCCESS. Full reactor `verify`: BUILD SUCCESS; conformance module 917 tests, 0 failures/errors, 17 skips. All four conformance suites retained their previous totals and had no failures.
- Raw rv065 output: `rv065-red.log`, `rv065-focused-green.log`, `rv065-core-clean-test.log`, and `rv065-full-reactor-verify.log`.

Raw output is in `focused-red.log`, `focused-green.log`, `combining-followup.log`, `full-reactor-clean-verify.log`, `conformance.log`, `rv062-red.log`, `rv062-green.log`, `rv062-full-reactor-clean-verify.log`, and `rv062-conformance.log`.
