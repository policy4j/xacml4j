# Run 067: merged main worktree check after 4.5, 4.47 and 4.52 (conductor)

Date: 2026-10-02. Check lane chk8, copied from the main worktree after the
g452b merge (4.52 review fixes) and Sol's 4.5 and 4.47 work. JDK 21.

Command (lane root):
`JAVA_HOME="$(jenv prefix 21)" mvn -q -Dlicense.skipUpdateLicense=true clean verify`

Result: PASS (exit 0, no `[ERROR]` or `BUILD FAILURE` lines).

| Module | Tests | Failures | Errors | Skipped |
| --- | ---: | ---: | ---: | ---: |
| xacml-core | 5,755 | 0 | 0 | 17 |
| xacml-xml | 244 | 0 | 0 | 0 |
| xacml-json | 24 | 0 | 0 | 1 |
| xacml-pdp | 98 | 0 | 0 | 5 |
| xacml-test | 99 | 0 | 0 | 0 |
| xacml-conformance-tests | 917 | 0 | 0 | 16 |
| Total | 7,137 | 0 | 0 | 39 |

Totals are the conductor's sum of the surefire XML reports. One test more
than run 058 (7,136): `XmlContentTest.testXPathFactoryEnablesSecureProcessing`.
The log has no `CONFORMANCE FAIL` or `SKIP` lines. This is not the 4.7 gate:
maintainer decisions D1-D18 (`builds/061-pending-decisions`) are still open.
