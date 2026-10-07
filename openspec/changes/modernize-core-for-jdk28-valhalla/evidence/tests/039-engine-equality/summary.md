# Engine equality and mapping fixes

## Changes and red/green evidence

| Bug | Test first: red result | Fix and green result |
| --- | --- | --- |
| 4.24 XPathCategory in obligation/advice assignments | `bug-4.24-red.log`: `Xacml30ResponseContextUnmarshallerTest`, 2 tests, 1 failure; serialized response omitted `XPathCategory`. | Copy the `AttributeValueType` extension attributes into `AttributeAssignmentType`. `bug-4.24-green.log`: 2 tests, 0 failures/errors. XML output test checks both obligation and advice assignments. |
| 4.28 temporal equality | `bug-4.28-red.log`: 3 tests, 2 failures. Date zone was discarded (the two same-calendar-date offsets compared equal); equal-instant dateTimes compared unequal. | Date retains zone for lexical output and compares by UTC instant; dateTime compares/hashcodes by instant; time compares/hashcodes by UTC-normalized time of day, with unzoned values interpreted as UTC. `bug-4.28-green-final.log`: 4 tests, 0 failures/errors, including date/dateTime/time less-than functions. |
| 4.30 string-equal-ignore-case | `bug-4.30-red.log`: 3 tests, 2 failures (ASCII and `ÄB`/`äb`); the negative case passed. | Lowercase both inputs with `Locale.ROOT`, then use string-equal. `bug-4.30-green.log`: 3 tests, 0 failures/errors. `Straße` and `STRASSE` remain unequal under lowercase. |
| 4.31 yearMonthDuration lexical form | `bug-4.31-red.log`: 4 tests, 1 failure; `P30D` was accepted as zero. | Validate the signed `P[nY][nM]` lexical form before conversion. `bug-4.31-green.log`: 4 tests, 0 failures/errors. |
| 4.32 selector status precedence | `bug-4.32-red.log`: 10 tests, 1 failure; a null resolution with `MustBePresent=false` set missing-attribute status. | Group the missing-value predicate with `MustBePresent`. The selector returns an empty date bag without setting status. `bug-4.32-green.log`: 10 tests, 0 failures/errors. |

## Regression gates

- JDK 21 full reactor `clean verify`: `reactor-clean-verify-final.log` ends in `BUILD SUCCESS`; all eight reactor modules succeeded. The conformance test module reports 901 tests, 0 failures, 0 errors, 25 skips.
- Core temporal regression selection for the 4.10/4.11 fixes: `temporal-regressions-4.10-4.11.log`, 47 tests, 0 failures/errors (date/time arithmetic, time formatting, time-in-range, and fractional duration tests).
- Separate conformance reactor run: `conformance-tests.log`, `BUILD SUCCESS`. Surefire reports 375 OASIS 2.0 tests (12 skips), 488 OASIS 3.0 tests (13 skips), 17 RSA tests, and 2 examples; all have 0 failures and 0 errors. Case markers show 362 OASIS 2.0, 474 OASIS 3.0, 17 RSA, and 2 example cases passing.
- The active manifest in this lane (also byte-identical to the read-only `.base` copy) enables 362 OASIS 2.0 and 474 OASIS 3.0 cases. This differs from the historical green-gate totals of 347 per OASIS suite; the current run had no failing cases. I made no persistent changes to the manifest.
- The regular conformance run omits IIIA030 and IIIA330 because both rows are excluded for bug 4.24. I temporarily enabled only those two rows in the lane manifest and ran `Xacml30ConformanceTest`: both cases passed (`conformance-4.24-targets.log`; 488 test invocations, 0 failures/errors, 11 skips). I then restored the manifest from a byte copy and `cmp -s` returned 0.
- No fixture was changed. The one failed attempt to invoke the filtered test without `-Dsurefire.failIfNoSpecifiedTests=false` stopped in the `xacml-core` dependency before the conformance class ran; the corrected invocation passed.

## Changed conformance outcomes

No case failed in the regular run. IIIA030 and IIIA330 were disabled in the normal manifest run and both passed when explicitly enabled after the 4.24 fix. The other conformance cases remained passing. This is outcome evidence for the lane run; no baseline behavior run was performed in the read-only `.base` copy.
