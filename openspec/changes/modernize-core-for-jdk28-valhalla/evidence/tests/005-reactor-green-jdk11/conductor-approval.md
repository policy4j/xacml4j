# Conductor approval: xacml-test compile-scope JUnit dependency

The conductor explicitly approved applying exactly `required-pom-fix.patch`
to `xacml-test/pom.xml`: `junit:junit:4.13.2`, compile scope. No other POM edits
are authorized. The approved patch was applied with `rtk git apply`; no
staging or commit was performed. The earlier blocked gate runs remain in
this directory as historical diagnostics. Subsequent runs continue task 1.3.

Writes remain scoped to PDP/test-support sources, conformance Java, the
approved POM patch and this run directory. XML, core, generated JAXB and
vendored conformance resources remain outside this continuation's writes.
No fixture or expected-result changes, assertion weakening, br command,
task tick or issue closure are authorized.
