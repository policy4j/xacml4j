## Why

The build pins five plugins at old versions (compiler 3.11.0, surefire 3.0.0,
versions 2.15.0, license 1.7, `maven-jaxb2-plugin` 0.15.1) and leaves the
lifecycle plugins (clean, resources, jar, install, deploy) to the defaults of
the Maven binary. Homebrew Maven 3.10.0 binds `maven-resources-plugin` 3.5.0
by default, so the same project resolves different plugins with Maven 3.9 and
3.10, and an offline build with 3.10 fails before compiling. The maintainer
asked on 2026-10-26: "Upgrade plugins if needed to latest versions".

## What Changes

- The root `pom.xml` gets a `<pluginManagement>` section that pins every
  plugin of the build at its latest stable release on Maven Central
  (retrieved 2026-10-26): clean 3.5.0, resources 3.5.0, jar 3.5.1, install
  3.2.0, deploy 3.2.0, compiler 3.16.0, surefire 3.6.1, dependency 3.11.0
  (unchanged), shade 3.6.2 (for `xacml-jmh`), versions 2.23.0, license 2.7.1.
  The Maven 4 betas (`4.0.0-beta-*`) are not used: the build runs on Maven
  3.9 and 3.10.
- Also pinned: `maven-site-plugin` 3.22.0 and `maven-enforcer-plugin` 3.6.3
  (the parent `oss-parent:7` brings enforcer 1.0); the effective POM showed
  both.
- The root POM sets `license.skipUpdateLicense=true`: the header update
  rewrites 1,693 vendored conformance fixtures when it runs (evidence
  `tests/001`), so it runs only on request
  (`-Dlicense.skipUpdateLicense=false`).
- `xacml-xml-jaxb`: `maven-jaxb2-plugin` 0.15.1 to 0.15.3, the last release of
  that artifact. Its successor `org.jvnet.jaxb:jaxb-maven-plugin` 4.x
  generates Jakarta XML Binding code; moving to it is a migration of the
  generated API and stays out of this change.
- No code change. A default build changes no file. The generated JAXB
  sources keep the same files and differ only in the two XJC header comment
  lines (generator version and generation time). A license header update
  runs only on request and is outside this guarantee (spec requirement "The
  upgrade changes no source").

## Capabilities

### New Capabilities

- `build-plugin-versions`: the build resolves the same pinned plugin versions
  with every supported Maven binary.

### Modified Capabilities

None.

## Research Questions

- Q1. Which release is the latest stable one of each plugin?
- Q2. Does license-maven-plugin 2.7.1 rewrite the existing file headers that
  1.7 leaves alone?
- Q3. Does `maven-jaxb2-plugin` 0.15.3 generate the same sources as 0.15.1?

## Verification

- `rtk mvn help:effective-pom` lists the pinned versions for every plugin.
- Full reactor `clean test` on JDK 21 with Maven 3.9.16 and with Homebrew
  Maven 3.10.0, both offline after one online resolution: 8,013 tests,
  0 failures, 18 skipped; conformance 937, 12 skipped.
- A default reactor build leaves `git status` unchanged (run 002). A forced
  `update-file-header` in a scratch copy rewrites 1,809 files with 1.7 and
  1,704 with 2.7.1 (run 003): the update is opt-in with either version.
- The generated JAXB sources of 0.15.1 and 0.15.3 have the same 110 files and
  differ only in the two XJC header comment lines (`compare-generated.sh`,
  run 004).

## Impact

- Files: `pom.xml`, `xacml-xml-jaxb/pom.xml`.
- Downloads: the new plugin versions and their dependencies, once, through
  the configured Maven mirror (the maintainer's VPN); later builds run
  offline.
- `add-pdp-jmh-benchmarks` uses shade 3.6.2 from this change.
- Approval: the maintainer asked for the upgrade (2026-10-26).

## Tracking

Beads epic: see `tasks.md` header. Task X.Y maps to issue `<epic>.X.Y`.
