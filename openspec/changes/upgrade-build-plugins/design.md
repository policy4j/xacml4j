## Context

- Plugins declared with versions: `pom.xml:258-321` (versions 2.15.0,
  compiler 3.11.0, dependency 3.11.0, surefire 3.0.0, license 1.7);
  `xacml-xml-jaxb/pom.xml:24-27` (`maven-jaxb2-plugin` 0.15.1). No
  `<pluginManagement>` exists. `xacml-core/pom.xml:11` configures the compiler
  without a version.
- The parent is `org.sonatype.oss:oss-parent:7`.
- Homebrew Maven 3.10.0 binds `maven-resources-plugin` 3.5.0; IntelliJ's
  bundled Maven 3.9.16 binds older default versions that are in the local
  repository (memory of 2026-10-24, checked again 2026-10-26).

## Goals / Non-Goals

**Goals:** pinned, current plugin versions; the same build with Maven 3.9 and
3.10; no source change.

**Non-Goals:** Maven 4 plugins; the Jakarta JAXB plugin; dependency upgrades
(libraries); the CI workflow (still JDK 8).

## Research Basis

`research/README.md` Q1 gives the versions. Q2 and Q3 are answered by the
checks of task 1.1 before the POM change is kept.

## Decisions

### D1. One `<pluginManagement>` in the root POM

All versions in one place, as properties-free literals next to each
`<plugin>` (the existing style of the root POM). The `<build><plugins>`
entries keep their configuration and lose their `<version>`. Alternative:
versions in properties (one more indirection for nine plugins; no reuse).

### D2. Latest stable 3.x lines, not Maven 4 betas

The `<release>` of the Apache plugins is a `4.0.0-beta`; those target Maven 4.
The build uses the highest unqualified version.

### D3. `maven-jaxb2-plugin` stays on its artifact

0.15.3 is a patch of the same artifact. The successor generates Jakarta code
and changes the generated API, which is a migration for its own change.

### D4. Checks before keeping the change

The license and JAXB checks (spec) ran first (evidence `tests/001`). The JAXB
plugin 0.15.3 generates the same 110 files; only the two XJC header comment
lines (generator version and generation time) differ (run 004, after review
rv010 made the comparison check file lists and failed diffs).

### D5. License header updates are opt-in

`update-file-header` with the current 1.7 already rewrites 1,741 files of a
clean tree, 1,693 of them vendored conformance fixtures; 2.7.1 differs from
1.7 only in trailing spaces of 160 headers. The build is safe only because
every documented command passes `-Dlicense.skipUpdateLicense=true`. The root
POM sets `license.skipUpdateLicense=true`, so a plain build never rewrites a
file; `-Dlicense.skipUpdateLicense=false` runs the update on purpose. The
documented commands keep working. Alternative: keep 1.7 (does not remove the
hazard) or add excludes for every fixture directory (long, and a new fixture
directory would be rewritten).

### D6. The enforcer and site plugins

`oss-parent:7` declares `maven-enforcer-plugin` 1.0 with an explicit version,
which `pluginManagement` does not override; the root POM declares 3.6.3 in
`<build><plugins>` so the inherited rule (a Maven version range) runs on the
current plugin. `maven-site-plugin` (3.12.1 against 3.22.0 before) is pinned
at 3.22.0 in `pluginManagement`.

## Test Strategy

No unit test changes. Checks: the effective POM with both Maven binaries;
full reactor `clean test` offline with both; `license:check-file-header`; a
scratch-copy `update-file-header` with a recursive diff; a recursive diff of
`xacml-xml-jaxb/target/generated-sources` from both plugin versions.

## Benchmark Plan

NOT APPLICABLE: build plugins do not change the produced code; the compiler
keeps `<release>21</release>`. The first JMH baseline of
`add-pdp-jmh-benchmarks` is built with the new plugins.

## Code Intelligence Plan

`rtk rg -n '<version>' pom.xml */pom.xml` before and after; `git diff --stat`
shows only the two POMs.

## Execution Plan

The conductor (Claude Opus 5.5) does the change; a Codex lane reviews the POM
diff and the check evidence in the next review gate of the running swarm.

## Risks / Trade-offs

- [Surefire 3.6 changes test discovery or reports] → the test counts must
  match; a difference stops the change.
- [Compiler 3.16 changes annotation processing] → AutoService and the JMH
  processor run in the reactor build and its tests.
- [Downloads need the corporate mirror] → one online resolution while the VPN
  is up; then offline.

## Migration and Rollback

Revert the two POMs.

## Open Questions

None.
