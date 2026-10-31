# 004: spine gate, full reactor (task 2.4)

Date: 2026-10-26. JDK: Oracle GraalVM 21.0.6+8.1. Maven 3.9.16, offline. No
`-Dlicense.skipUpdateLicense` flag (the POM default skips the header update).

Result: BUILD SUCCESS in 35.8 s; all 9 modules SUCCESS, including
`Xacml4J JMH Benchmarks`.

| Module | Tests | Failures | Errors | Skipped |
| --- | --- | --- | --- | --- |
| Core Engine | 6,501 | 0 | 0 | 2 |
| XML marshallers | 253 | 0 | 0 | 0 |
| JSON | 24 | 0 | 0 | 1 |
| PDP | 199 | 0 | 0 | 3 |
| Policy Unit Test Support | 99 | 0 | 0 | 0 |
| Conformance Tests | 937 | 0 | 0 | 12 |
| JMH Benchmarks | 2 | 0 | 0 | 0 |
| **Total** | **8,015** | **0** | **0** | **18** |

Conformance unchanged. No file under `xacml-conformance-tests` changed
(`git status`). JMH: NOT APPLICABLE for this gate (no benchmark yet). Code
review of the spine: in gate 3.7.
