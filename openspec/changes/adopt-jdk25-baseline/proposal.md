## Why

The maintainer set the project baseline to JDK 25 on 2026-10-27 ("set base
line to jdk 25"; asked which scope, chose "Project and JMH"). JDK 25 is the
current long-term-support release, Oracle GraalVM 25.0.3 is installed, and the
policy compiler of `improve-xacml-parsing` is built as a GraalVM 25 native
executable. The reactor compiles with `<release>21</release>` today
(`pom.xml` compiler configuration), and the JMH baseline of
`add-pdp-jmh-benchmarks` ran on GraalVM 21.

## What Changes

- **BREAKING**: every module compiles with `<release>25</release>`; users
  need Java 25 or later. No preview features (the JDK 28 preview step of
  `modernize-core-for-jdk28-valhalla` stays separate).
- The enforcer requires Java 25 or later to build.
- Builds, tests, conformance, and JMH run on Oracle GraalVM 25.0.3; JMH
  forks use the C2 JIT (`-XX:-UseJVMCICompiler`) so numbers compare with
  OpenJDK builds.
- A JMH baseline on JDK 25 replaces the JDK 21 baseline as the reference for
  every later before/after pair (the JDK 21 runs stay as evidence).
- Docs and agent instructions say JDK 25 (`AGENTS.md`, `docs/ai/TESTING.md`,
  `docs/ai/BENCHMARKS.md`, `xacml-jmh/README.md`); the CI workflow builds with
  JDK 25 (today it uses JDK 8 and fails); the gates of open changes that say
  JDK 21 run on JDK 25 from here on.

## Capabilities

### New Capabilities

- `jdk-baseline`: the JDK the build targets, requires, and is measured on.

### Modified Capabilities

None.

## Research Questions

- Q1. JDK 22 to 25 changes that affect this build: annotation processing
  defaults of `javac` (processors run only when requested), removal of the
  Security Manager, warnings for `sun.misc.Unsafe` memory access and dynamic
  agent loading (Mockito), and deprecations that the sources use.
- Q2. Oracle GraalVM 25.0.3: JIT selection flags, and `native-image` support
  for the libraries on the parse path (JAXB RI 2.3, Guava, Logback).
- Q3. GitHub Actions setup for JDK 25 (distribution and cache).

## Verification

- Full reactor `clean test` on GraalVM 25 at release 21 first
  (characterization), then at release 25: test counts equal the JDK 21 gate
  (8,202 tests, 0 failures, 18 skipped at run 016 of `add-pdp-jmh-benchmarks`),
  conformance 937 run, 12 skipped.
- `javap` shows class file version 69 for every module's classes.
- AutoService still writes the `META-INF/services` files.
- JMH baseline on JDK 25 (Benchmark Plan of `add-pdp-jmh-benchmarks`, both
  commands), complete by the check script.

## Impact

- `pom.xml` (compiler release, enforcer rule), CI workflow, docs. No source
  change expected; Q1 lists any that the compiler or tests require.
- Users: **BREAKING** Java 25 required.
- Changes in flight: `modernize-core-for-jdk28-valhalla` (gates that say JDK
  21), `add-pdp-jmh-benchmarks` (baseline task 4.2 re-recorded on JDK 25),
  `optimize-pdp-evaluation`, `improve-xacml-parsing`, `upgrade-dependencies`.

## Approval

Approved by the maintainer on 2026-10-27 (scope chosen: project and JMH).

## Tracking

Beads epic `policy4j-cvw4`; task X.Y maps to issue `<epic>.X.Y`.
