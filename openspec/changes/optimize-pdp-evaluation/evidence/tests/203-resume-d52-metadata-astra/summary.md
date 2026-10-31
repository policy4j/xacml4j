D52-ASTRA-1 is corrected and the candidate is ready for independent Sol and Agy Argon review. The full affected core gate is BLOCKED by the existing calendar-conversion error described below. This is an author handoff, with no review verdict.

Change `optimize-pdp-evaluation`, task 42.19, bead `policy4j-cwtu.42.19.3`; author `cod_3` / `ntm-astra-g1`, GPT-6 Astra, max effort. Worktree HEAD remains `759d90a6c75510d55127d8e22870f9edd985c991`.

The correction changes two production paths relative to the frozen candidate:

- `xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java:329` admits typed predicate parameters only when the descriptor itself is a canonical `XacmlTypes` instance. The standard any-value parameter remains eligible.
- `xacml-core/src/main/java/org/xacml4j/v30/policy/HigherOrderCall.java:74` admits canonical scalar descriptors and final `BagOfValuesType` descriptors containing canonical element types. Custom metadata keeps the existing checked invocation. The exact unary/binary shapes, both bag positions, and custom-factory fallback remain intact.

The new `HigherOrderMetadataCompatibilityTest.java` has 80 cases. Twelve outcome cases use the public builder, pass construction-time checks, and mutate a caller-owned descriptor during operand evaluation. They cover both operators, all three shapes, runtime validation off/on, exact syntax-error status/message, outer function identity, one observer failure with that same identity/exception, operand order and single evaluation, and empty bags with zero predicate invocations. Controls cover canonical typed and any-value preparation, legitimate custom bags and scalars on the checked path, and a custom invocation factory.

`SharedConditionValuesPdpTest.java:213` adds the two real-PDP rule outcomes. Its existing higher-order D45 case now also checks all six prepared clones retain `PreparedConditions$Shared` and a `HigherOrderCall`.

| Executed evidence | Result |
| --- | --- |
| [red-core.log](red-core.log), frozen production, first run with `clean` | 12 cases: six expected failures with validation off, six validation-on controls passed. Failures were missing exceptions. |
| [red-pdp.log](red-pdp.log), frozen production | Two expected failures: any-of yielded NOT_APPLICABLE and all-of yielded PERMIT instead of INDETERMINATE_P. |
| [green-focused.log](green-focused.log), corrected source | 335 tests, zero failures/errors/skips: core 287 and PDP 48. Includes all 80 new cases, D45, regex binding, argument validation, and later-deciding operands. |
| [green-core-pdp-suites.log](green-core-pdp-suites.log), ordinary affected run | Core 7,443 tests, zero assertion failures, one error, two skipped; downstream modules skipped because core failed. The filename was chosen before execution and does not indicate a passing gate. |
| [affected-suites-continued.log](affected-suites-continued.log) | Core repeats the same one error; PDP 810 tests, zero failures/errors, three skipped; XML 254 clean; JSON 24, zero failures/errors, one skipped. |

All Maven commands used the shared serialized runner, `--maven /opt/homebrew/bin/mvn`, JDK 25, `-o`, and `-Dlicense.skipUpdateLicense=true`. The first attempt was denied access to the shared lock before Maven started; it was rerun with the required sandbox approval. The last run used `-Dmaven.test.failure.ignore=true` solely to reach downstream suites. Its Maven exit 0 is **not** a passing core or reactor gate. [test-results.json](test-results.json) records every run and count.

The remaining core error is `ISO8601DateTest.testCreateFromCalendar:59`: `DateTimeException: Invalid date 'SEPTEMBER 31'` on October 31. `DateTimeDurationUtils.java:151` passes zero-based `Calendar.MONTH` to one-based `LocalDate.of`; the protected test also compares against the zero-based month. Both files are byte-identical to this worktree's base. The conductor must route that separate correction and rerun the ordinary gate; neither protected file was changed here.

All eight overlay hashes matched the manifest before edits. All five production hashes still matched before both meaningful red runs. [red-proof.json](red-proof.json) and [red-sources/](red-sources/) preserve that evidence. The original batch2/cod_1 files and frozen packet files still match all eight manifest entries. The protected 7,228-file hash set is unchanged through the correction and suite runs. `Apply.java` remains byte-identical to the original candidate, preserving its field reuse; no new object-layout or performance measurement was made.

[correction.patch](correction.patch) contains only the correction against the original candidate. [candidate-ready.json](candidate-ready.json) records all nine owned source hashes. No public API, repository settings, metadata, version, Beads, task checkbox, branch, or commit was changed.

Independent Sol/Agy Argon review, the conductor's combined reactor/conformance gate, and the controlled quiet-host JMH pair remain pending. Performance: **UNRUN**.

BLOCKER cod_3 policy4j-cwtu.42.19.3: the full affected core gate fails in protected ISO8601DateTest.testCreateFromCalendar because the existing calendar conversion constructs SEPTEMBER 31.

