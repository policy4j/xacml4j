# 011: reactor and conformance with cycles A, B, D, E, F (gates 7.3, 8.3, 9.3)

Date: 2026-10-27 08:12 to 08:13 UTC. Oracle GraalVM 25.0.4, Maven 3.9.16 offline, release 25, `clean test`.

| Module | Run | Failures | Errors | Skipped |
| --- | --- | --- | --- | --- |
| xacml-core | 6,539 | 0 | 0 | 2 |
| xacml-xml | 253 | 0 | 0 | 0 |
| xacml-json | 24 | 0 | 0 | 1 |
| xacml-pdp | 215 | 0 | 0 | 3 |
| xacml-test | 99 | 0 | 0 | 0 |
| xacml-conformance-tests | 938 | 0 | 0 | 12 |
| xacml-jmh | 196 | 0 | 0 | 0 |

Total 8,264 tests, 0 failures, 0 errors, 18 skipped; BUILD SUCCESS. Conformance: 938 run, 12 skipped, as before the cycles: run 003 of group 1 had 937; the one more is the conformance module's `LoggingConfigurationTest` (change upgrade-dependencies). The OASIS and RSA case results are unchanged.

Output: `output.txt` (surefire module lines and totals).
