# Run 068: resumed JDK 21 baseline

Date: 2026-10-04. Change: `modernize-core-for-jdk28-valhalla`.
Actor: `codex-modernization-resume`; claimed issue `policy4j-the.4.6`.
Branch: `integration/modernize-core-jdk28-valhalla`, HEAD `befdd116bd11385ac8d007732fa8e4d347af3143` plus the existing dirty integration work.

## Verification

- Strict OpenSpec validation: PASS. Apply instructions report 25/57 tasks complete.
- JDK: Oracle GraalVM 21.0.6+8.1 (`java` 21.0.6).
- Fresh full-reactor `clean verify`: PASS, process exit 0.
- Surefire: 175 report files; 7,137 tests, zero failures, zero errors, 39 skipped.

| Module | Tests | Failures | Errors | Skipped |
| --- | ---: | ---: | ---: | ---: |
| xacml-core | 5,755 | 0 | 0 | 17 |
| xacml-xml | 244 | 0 | 0 | 0 |
| xacml-json | 24 | 0 | 0 | 1 |
| xacml-pdp | 98 | 0 | 0 | 5 |
| xacml-test | 99 | 0 | 0 | 0 |
| xacml-conformance-tests | 917 | 0 | 0 | 16 |
| Total | 7,137 | 0 | 0 | 39 |

## Preservation and coverage

SHA-256 snapshots of 3,753 tracked/untracked files outside the evidence tree are unchanged after the build. No production source, test, vendored fixture, schema, or existing user hunk was modified. License updates were disabled. No Git staging or history operations occurred.

`receipts.json` and `surefire-summary.txt` contain the extracted report counts. `source-hashes-before.json` records the preservation snapshot. `maven.log` is local build output, excluded by the global log ignore. The RTK output contains no Maven BUILD SUCCESS marker; acceptance rests on the command exit code and fresh Surefire reports, not a string match.

## Task 4.6 preflight

Source search is SUPPORTED for literal references, not a complete semantic caller inventory. `ArithmeticFunctions.divideInteger` still declares and returns `DoubleVal`; both conformance manifests disable IIC020 for this mismatch. The direct Java test caller is `ArtimeticFunctionsTest`; the safety-net tests for an integer result remain disabled pending C2.

The written `function-semantics` spec requires IEEE NaN inequality; run 061 decision D2 recommends the opposite as a retained deviation. The maintainer was asked to choose before product edits. Registration of `on-permit-apply-second` also awaits confirmation and profile-correct implementation.

This baseline does not complete task 4.6 or the 4.7 safety-net gate. All unresolved maintainer decisions and existing skips remain outstanding. No new review lane ran, and JDK 28 was not tested. JMH: NOT APPLICABLE to this baseline verification.

