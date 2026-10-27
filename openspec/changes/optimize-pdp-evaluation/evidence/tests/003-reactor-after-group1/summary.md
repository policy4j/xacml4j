# 003: full reactor after group 1 (part of gate 1.3)

Date: 2026-10-26. JDK: Oracle GraalVM 21.0.6+8.1. Maven 3.9.16, offline.
Sources: as run 002.

Result: BUILD SUCCESS in 30.2 s; all 8 modules SUCCESS.

| Module | Tests | Failures | Errors | Skipped |
| --- | --- | --- | --- | --- |
| Core Engine | 6,501 | 0 | 0 | 2 |
| XML marshallers | 253 | 0 | 0 | 0 |
| JSON | 24 | 0 | 0 | 1 |
| PDP | 199 | 0 | 0 | 3 |
| Policy Unit Test Support | 99 | 0 | 0 | 0 |
| Conformance Tests | 937 | 0 | 0 | 12 |
| **Total** | **8,013** | **0** | **0** | **18** |

Against the landing gate (8,009 tests, 18 skipped; conformance 937, 12
skipped), the only difference is the 4 tests of `CorrelationIdTest` in the PDP
module (195 to 199). Conformance is unchanged.

Raw lines: `surefire-summary.txt`.
