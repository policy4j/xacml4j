# Run 066: OASIS 2.0 IIIC001 re-enabled (manifest audit follow-up)

Result: oasis20/IIIC001 passes. Its manifest row is now executed and enabled, with the former policy4j-the.4.19 bug reference cleared. The other 879 manifest rows are unchanged.

JDK: 21.0.6, Oracle Corporation, runtime 21.0.6+8-LTS-jvmci-23.1-b55, as recorded in the preceding clean reactor security gate.

The full OASIS 2.0 runner reports 375 tests, 0 failures, 0 errors and 9 skips (374 manifest cases plus its name helper test). This is 365 passing cases and 9 skipped cases, an increase of one executed case. ConformanceManifestTest reports 13 tests, 0 failures, 0 errors and 0 skips. Combined selected gate: 388 tests, 0 failures, 0 errors, 9 skips.

The log records CONFORMANCE PASS oasis20/IIIC001. Surefire testConformance(Case)[359] also passes with no skip. results.json records the actual report and confirms that only this row differs from run 051/rv064-fix/09-case-results.csv.

Command: JAVA_HOME="$(jenv prefix 21)" rtk mvn -Dlicense.skipUpdateLicense=true -pl xacml-conformance-tests -am -Dtest=Xacml20ConformanceTest,ConformanceManifestTest -Dsurefire.failIfNoSpecifiedTests=false -l openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/064-manifest-audit/01-oasis20-and-manifest-guard.log test

No fixture, expected response, harness or engine code changed. The earlier security clean gate had 863 passing conformance cases and 17 skips; the new manifest has 864 enabled cases and 16 skips. The full reactor and all conformance suites will be checked again by the resumed task 4.5 gate in run 058. OASIS 3.0 IIIC001 remains executed.

No commit, br command, task tick or issue closure was performed.

Conductor note (2026-10-02): this folder was created as tests/064-manifest-audit, which collided with tests/064-xpath-secure-processing; renamed to run 066. Paths inside command.txt may still name 064.
