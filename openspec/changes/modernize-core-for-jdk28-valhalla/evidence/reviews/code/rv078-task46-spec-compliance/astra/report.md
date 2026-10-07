# Astra numeric and harness review

Conductor copied the native final response because the reviewer persona forbids file creation.

FINAL REPORT (policy4j_code_review_codex_astra, exact serving variant UNVERIFIED)

**APPROVE — bounded source review.** No blocking defect established in the eleven-file packet for `modernize-core-for-jdk28-valhalla`, task 4.6, `policy4j-the.4.6`. Harness GREEN and task closure remain pending.

1. **P3, nonblocking — fixture guard tests do not isolate each condition.** [NaNFixtureAdjudicationTest.java:199](xacml-conformance-tests/src/test/java/org/xacml4j/v20/NaNFixtureAdjudicationTest.java:199): all negative controls use `executed`. A future regression that broadens adjudication to every `special-harness` case could pass these controls. The current whitelist is correctly implemented at [ConformanceCaseRunner.java:175](xacml-conformance-tests/src/test/java/org/xacml4j/v20/ConformanceCaseRunner.java:175). Smallest fix/test: add synthetic cases varying suite, disposition, and case ID independently while retaining a single-result Permit fixture; assert unchanged Permit for each excluded combination.

Prior-finding dispositions:

- **IEEE equality: resolved within scope.** [EqualityPredicates.java:194](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/EqualityPredicates.java:194) compares primitive doubles. [SetFunctions.java:188](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/SetFunctions.java:188) uses policy membership consistently: union retains NaN occurrences, intersection excludes them, and signed zeros deduplicate. Tests cover same-object NaN, distinct wrappers, infinities, empty bags, duplicates, wrong types and arity. The [result oracle:360](xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/DoubleBagSetEqualityTest.java:360) independently checks size, NaN/zero counts and finite values; it does not reuse policy equality or bag equality. Java equality/hash contracts receive separate assertions at line 254.

- **Integer division: resolved for the accepted bounded domain.** [ArithmeticFunctions.java:160](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/ArithmeticFunctions.java:160) declares and returns integer, divides longs directly and retains the zero guard. [ArtimeticFunctionsTest.java:108](xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/ArtimeticFunctionsTest.java:108) covers precision above 2^53 and sign combinations; subsequent tests exercise composed Apply with runtime validation, zero/status propagation and invalid arguments. Truncation is correctly described as the project convention, not an express §A.3.2 mandate.

- **Manifest preservation: verified statically.** Read-only CSV comparison found 880 unique identities, nine columns throughout, all 864 previously enabled identities retained, and exactly two additions to the enabled set: both IIC020 cases. Only their metadata and the IIC350/IIC358 adjudication metadata changed; resource paths remained identical. Reconstructing the baseline CSV from `069/manifest-before.json` reproduced the supplied 068 SHA-256, `19f9bfe8b0dda4bfbffd02fe22af6c4765a75f778580536f0a3b5e61794b67e1`.

- **NaN fixture adjudication: source correction accepted; runtime gate pending.** [ConformanceCaseRunner.java:172](xacml-conformance-tests/src/test/java/org/xacml4j/v20/ConformanceCaseRunner.java:172) loads the expected response, restricts adjudication by suite/disposition/ID, requires one Permit result and changes only its decision. The full-response comparison call remains at line 163. The out-of-scope loader’s freshness and vendored byte preservation were not independently verified; the fixture test at line 136 asserts Permit, not byte identity.

- **XPath missing Content: corrected requirement accepted.** [function-semantics/spec.md:94](openspec/changes/modernize-core-for-jdk28-valhalla/specs/function-semantics/spec.md:94) now distinguishes missing Content (`0`/`false`) from invalid evaluation (`Indeterminate`), consistent with the packet’s [Core §A.3.15](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html) adjudication. Scoped safety-net tests cover absence and injected evaluation failures; they do not establish complete XPath-engine compliance.

Verification: SHA-256 checks matched **11/11 files before and after**; packet-manifest hash remained `572a0349f17e41ca6f573ccd317bf30d3e0e3d8d32866830ba599ee66b1af840`. Scoped `ast-grep scan` exited 0 without diagnostics. Retained XML summaries corroborate numeric RED **252/63 failures/2 errors/7 skips**, revised numeric GREEN **250/0/0/7**, and harness RED **5/3 failures**. Numeric GREEN is a slice of an overall exit-1 run, not a successful reactor gate.

No builds, tests, network operations or edits were performed. Combining source was excluded. Overflow, three-or-more-bag union, remaining skips, broader caller compatibility, performance and whole-engine acceptance remain outside this approval. Legacy-helper preservation remains supplied evidence.

BLOCKER: The developer-level reviewer contract prohibits creating files anywhere. The requested `astra/report.md` was not written; this terminal report is the review record.

DONE_REVIEW_ASTRA_20261004
