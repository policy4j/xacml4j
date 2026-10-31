# Group 3 blocker bug-id follow-up

Applied the conductor mapping to the manifest, conformance-blockers.md and per-case-results.csv. Verified all 44 suite/case mappings in each file. Replaced 41 case placeholders per file; the three IIIE301–303 references already matched policy4j-the.3.7.

Only bug-id values changed in the three existing files. Every other field and byte is preserved; counts, dispositions, reasons and case results are unchanged. See 16-bug-id-mapping-check.json for the mapping and file hashes. Other existing run 023 evidence is retained unchanged as historical receipts.

Exact plain Maven command from <repo>:

```sh
JAVA_HOME="$(jenv prefix 21)" rtk mvn -Dlicense.skipUpdateLicense=true -pl xacml-conformance-tests -am -Dtest=ConformanceManifestTest -Dsurefire.failIfNoSpecifiedTests=false -l openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/023-conformance-manifest/16-jdk21-manifest-guard.log test
```

JDK 21 guard result: 13 tests, 0 failures, 0 errors, 0 skips; BUILD SUCCESS, exit 0. ConformanceManifestTest was the only test class executed. No other tests or full gate were rerun.

Maven receipt:

```text
[INFO] Running org.xacml4j.v20.ConformanceManifestTest
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.18 s - in org.xacml4j.v20.ConformanceManifestTest
[INFO] Results:
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

The 880-case manifest still has 836 enabled cases and 44 blockers. This change records tracking ids; it does not resolve those blockers. No br command, commit, task tick or issue close was performed. Work stopped for read-only review.
