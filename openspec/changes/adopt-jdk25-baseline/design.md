## Context

- `pom.xml` sets `maven-compiler-plugin` `<release>21</release>` with an
  explicit AutoService annotation processor path.
- Installed: Oracle GraalVM 21.0.6 and 25.0.3 (`jenv` aliases `21`, `25`);
  `native-image` is on the path through the `jenv` shim.
- CI (`.github/workflows/main.yml`) builds with JDK 8 and fails at release 21.

## Goals / Non-Goals

**Goals:** release 25 everywhere, JDK 25 builds, tests, and JMH, docs and
CI on JDK 25.

**Non-Goals:** preview features and value classes (JDK 28 change); source
rewrites to new language features (separate, measured changes).

## Research Basis

`research/README.md` Q1 to Q3.

## Decisions

### D1. Characterize before switching

Build and test on GraalVM 25 at release 21 first; any failure there is a JDK
runtime difference, not a release difference. Then switch to release 25.

### D2. Release 25, no preview, enforcer `[25,)`

One `<release>` in the root compiler configuration; the enforcer
`requireJavaVersion` rule in the root build. Annotation processing stays
explicit through `annotationProcessorPaths` (Q1 confirms it still runs).

### D3. JMH on GraalVM 25 with C2

The benchmark plan's flags stay (`-XX:-UseJVMCICompiler`, heap and G1 from
the annotations); `env.txt` records the final flags. The JDK 25 baseline is
`add-pdp-jmh-benchmarks` `evidence/benchmarks/jmh/002-baseline-jdk25`; the
JDK 21 baseline stays as `001-baseline`.

### D4. CI on JDK 25

The workflow sets up JDK 25 (distribution per Q3) and runs the gate command
offline-safe (`-B`, license skip). Pushing it is the maintainer's decision.

## Test Strategy

No new tests: the reactor, conformance, class-file versions, and service
files are the checks (spec `jdk-baseline`).

## Benchmark Plan

JMH applies to the baseline only (task 3.1): the JDK 25 baseline is a
recorded run, not a before/after claim. Later pairs use it.

## Code Intelligence Plan

`rtk rg` over POMs, docs, workflows, and agent files for `release>21`,
`jenv prefix 21`, `JDK 21`, and `21.0.6` to find every reference; each hit
is changed or listed with a reason (history and evidence keep their dates).

## Execution Plan

The conductor alone (POM, docs, CI); review by one lane from another family.

## Risks / Trade-offs

- [A library misbehaves on JDK 25] → the characterization run (D1) shows it
  before the release changes.
- [Users on Java 21] → **BREAKING** note; the maintainer chose it.

## Migration and Rollback

Revert the release and enforcer lines to return to 21.

## Open Questions

None.
