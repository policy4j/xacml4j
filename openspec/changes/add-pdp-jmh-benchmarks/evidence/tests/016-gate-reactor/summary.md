# 016: reactor gate after the review fixes (task 3.7)

Date: 2026-10-27. Oracle GraalVM 21.0.6 (JDK 21), Maven 3.9.16 (IntelliJ bundle), offline.
Tree: the uncommitted work of the three changes, with the fixes of reviews rv004 to rv011.
The raw log stays in the scratch directory (Maven logs carry local account data).

Result: BUILD SUCCESS.

| Module | Tests | Failures | Errors | Skipped |
| --- | --- | --- | --- | --- |
| xacml-core | 6,501 | 0 | 0 | 2 |
| xacml-xml | 253 | 0 | 0 | 0 |
| xacml-json | 24 | 0 | 0 | 1 |
| xacml-pdp | 202 | 0 | 0 | 3 |
| xacml-test | 99 | 0 | 0 | 0 |
| xacml-conformance-tests | 937 | 0 | 0 | 12 |
| xacml-jmh | 186 | 0 | 0 | 0 |
| total | 8,202 | 0 | 0 | 18 |

Against run 012 (8,178): +21 in `xacml-jmh` (165 to 186: `ReviewGateFixesTest` 18,
`DirectoryCorpusTest` +3) and +3 in `xacml-pdp` (199 to 202: the three `CorrelationIdTest` cases
added after review rv001 of `optimize-pdp-evaluation`); other modules unchanged. Conformance unchanged
(937 run, 12 skipped; no file under `xacml-conformance-tests` changed). `git status` after the build
shows only the files of the changes (no license header or other build rewrite).

`ast-grep scan` of `xacml-jmh/src/main` and `xacml-pdp/src/main/java`: no finding (exit 0).
