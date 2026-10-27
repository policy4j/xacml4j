# 037: full reactor on the main tree (batch 2, D21, D23)

Date: 2026-10-27, after the VPN came back (one online build refilled the local Maven
repository). `rtk mvn -B -Dlicense.skipUpdateLicense=true clean test`, Maven 3.9.16,
Oracle GraalVM 25.0.4. `reactor.txt` holds the scrubbed per-module counts.

BUILD SUCCESS, every module:

| Module | Tests | Skipped |
| --- | --- | --- |
| xacml-core | 6,636 | 2 |
| xacml-xml-jaxb | 253 | 0 |
| xacml-xml | 24 | 1 |
| xacml-pdp | 217 | 3 |
| xacml-json | 99 | 0 |
| xacml-conformance-tests | 938 | 12 |
| xacml-jmh | 195 | 0 |

8,362 tests, 0 failures, 0 errors, 18 skipped. Closes the reactor and conformance part
of the batch-2 gates (6.6, 15.3 to 19.3) and of D21 and D23 (20.3, 22.4 still need
their reviews).
