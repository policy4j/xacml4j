# rv038/rv039 fixes

The URI repair is complete. AnyURI stores URI.create(v) without normalization and overrides lexical equality/hash while keeping OtherValue and the URI getter.
Its repeated String branch now accepts StringVal, restoring the valid anyURI-from-string path in the same scoped class.
PermitOverrides' never-set atLeastOneIndeterminate flag and unreachable branch were removed; the 3,048-row override matrix passes in the final gate.
The conductor merged overridable Value equality/hash at 02:06; this lane did not edit Value.java or the merged engine/mapper files.

| Review | Change | Test / proof |
| --- | --- | --- |
| rv038 MEDIUM / rv044 finding 1 | Original URI spelling and codepoint equality/hash | AnyURILexicalTest: 34; XML and JSON AnyURIRequestLexicalTest: 3 each; Xacml30TestUtilityTest rejects both normalization/case pairs |
| rv039 LOW | Remove dead PermitOverrides flag | OverrideDecisionTableTest: 3,048 final invocations; oasis30/IID300 passes |

Run 03 is the complete pre-fix receipt: 113 tests, 29 failures, 4 errors. Run 04 is green at 113/0/0/0. Two later null-FulfillOn controls bring final focused coverage to 115/0/0/0.
Run 01's test setup/canonical-id errors and the stale comparator snapshot are explained in spec-and-caller-review.md; they are not used as fresh green evidence.

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
