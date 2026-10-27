Beads: epic `policy4j-ixe7`; task X.Y is issue `<epic>.X.Y`.
Gate: `PATH="<maven>/bin:$PATH" JAVA_HOME="$(jenv prefix 21)" rtk mvn -o -Dlicense.skipUpdateLicense=true clean test`, once with Maven 3.9.16 and once with Maven 3.10.0.

## 1. Pin and upgrade the plugins

- [x] 1.1 Characterize before the change: record the effective plugin versions with both Maven binaries; run `license:update-file-header` with 1.7 and 2.7.1 in scratch copies and diff them against the tree; build `xacml-xml-jaxb` with 0.15.1 and 0.15.3 and diff the generated sources; evidence: `evidence/tests/001-characterization`
- [x] 1.2 Add `<pluginManagement>` to `pom.xml` with the versions of research Q1 and set `maven-jaxb2-plugin` 0.15.3 in `xacml-xml-jaxb/pom.xml` (D1 to D4); resolve the new plugins online once through the configured mirror; check: `help:effective-pom` with both Maven binaries
- [x] 1.3 Gate: full reactor `clean test` offline with Maven 3.9.16 and Maven 3.10.0 (counts unchanged: 8,013 tests, 18 skipped; conformance 937, 12 skipped); `git status` shows only the two POMs and the change; review of the POM diff by a Codex lane (`evidence/reviews/code/`); JMH: NOT APPLICABLE (design); docs: `docs/ai/TESTING.md` build note if Maven 3.10 now works

## 2. Close out

- [x] 2.1 `openspec validate upgrade-build-plugins --strict` (valid, 2026-10-27)
- [x] 2.2 Audit requirement → task → evidence in `evidence/README.md` (every scenario has a run; gate 1.3 done)
- [x] 2.3 Close the beads epic and run `br sync --flush-only`
