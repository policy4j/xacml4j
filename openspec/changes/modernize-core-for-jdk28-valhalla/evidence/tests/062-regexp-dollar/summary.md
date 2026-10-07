# RegExp end-anchor semantics (bug 4.49)

## Change

RegExpFunctions.covertXacmlToJavaSyntax now translates each unescaped dollar sign outside character classes to Java's strict end-of-input anchor, backslash-z. Escaped dollar signs and dollars inside character classes remain literal. The translator leaves the caret unchanged; tests confirm Java's default start-of-input behavior matches the required XPath behavior.

## Test-first evidence

- red.log failed before the translator change: ^foo$ matched foo followed by a newline (expected false, actual true); 11 tests ran, one failed.
- RegularExpressionFunctionsTest now covers ^foo$ with and without a final newline, escaped dollar, dollar in a character class, caret at the start and after a leading newline, plus anchored calls through all five typed regexp-match functions.
- The focused rerun kept both FunctionSafetyNet43Test and RegularExpressionFunctionsTest green: 120 tests, 0 failures, 0 errors, 12 skips.

## Verification

- JDK 21 xacml-core clean test: BUILD SUCCESS; 4,889 tests, 0 failures, 0 errors, 18 skips.
- JDK 21 full reactor verify: BUILD SUCCESS. Conformance module: 917 tests, 0 failures, 0 errors, 17 skips. OASIS 2.0: 375 tests/10 skips; OASIS 3.0: 488/7; RSA 2008: 17/0; examples: 2/0. No failures or errors.
- Every Maven command passed the license skip property.

Raw logs: red.log, focused-green.log, core-clean-test.log, and full-reactor-verify.log.
