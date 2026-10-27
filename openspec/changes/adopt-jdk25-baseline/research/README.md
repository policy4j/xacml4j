# Research

Status: REQUIRED. JDK 22 to 25 changed `javac` and runtime defaults that can
affect this build.

## Questions and answers

### Q1. JDK 22 to 25 changes that affect the build

- Answer: (a) As of JDK 23, `javac` runs annotation processing only with
  explicit configuration; the build configures AutoService and the JMH
  generator through `annotationProcessorPaths` (explicit `-processorpath`),
  so they keep running (task 2.3 checks the generated service files).
  (b) No main or test source uses `SecurityManager`, `AccessController`,
  `doPrivileged`, `sun.misc.Unsafe`, `Thread.stop/suspend/resume`,
  finalizers, or boxed-primitive constructors (`rtk rg`, 2026-10-27, outside
  `xacml-proto/`). Libraries may print JDK 24+ warnings for `sun.misc.Unsafe`
  memory access; the build records any such warning in the gate log.
- Sources: JDK 23 release notes, `javac` section
  (https://www.oracle.com/java/technologies/javase/23-relnote-issues.html,
  retrieved 2026-10-27); local search.
- Confidence: high for (a) and the local search; medium for library warnings until the gate log is read.
- Disagreement / drift risk: none known.

### Q2. Oracle GraalVM 25 JIT flags and native-image support

- Answer: the installed runtime and `native-image` are Oracle GraalVM
  25.0.4+7.1 (`native-image --version`: 25.0.4 2026-07-21; the `jenv` alias
  `25.0.3` lags). `UseJVMCICompiler` defaults to true (Graal JIT);
  `-XX:-UseJVMCICompiler` sets it false (C2), as on GraalVM 21.
  `UseCompactObjectHeaders` exists as a product flag, default false (an
  allocation hypothesis for the profiling swarm, not a build setting).
  The JDK 25 release notes list 25.0.4.1 and a critical patch update due
  2026-10-20; updating the installed GraalVM is the maintainer's decision.
  native-image support for JAXB RI 2.3 and Logback: pending
  (`improve-xacml-parsing` group 9).
- Sources: local `java -XX:+PrintFlagsFinal -version` and `native-image --version`
  (2026-10-27); Consolidated JDK 25 release notes
  (https://www.oracle.com/java/technologies/javase/25all-relnotes.html,
  retrieved 2026-10-27).
- Confidence: high for the flags; pending for native-image library support.
- Disagreement / drift risk: Oracle GraalVM and GraalVM Community differ in JIT defaults.

### Q3. GitHub Actions setup for JDK 25

- Answer: `actions/setup-java` v6.1.0 (2026-10-14) and `actions/checkout`
  v7.0.1 (2026-07-20) are the latest releases; the workflow uses
  `actions/setup-java@v6` with `distribution: temurin`, `java-version: '25'`,
  and `cache: maven` (the old `adopt` distribution is replaced). CI runs the
  JVM build only; the GraalVM native build of `improve-xacml-parsing` adds
  its own job later.
- Sources: GitHub releases API (`gh api repos/actions/setup-java/releases/latest`,
  `repos/actions/checkout/releases/latest`, 2026-10-27).
- Confidence: high for the versions; the workflow itself runs only after a push (the maintainer's decision).
- Disagreement / drift risk: none known.

## Lanes

| Lane | Profile / model | Report | Status |
| --- | --- | --- | --- |
| conductor | Claude Code, Claude Opus 5.5 | this file | started 2026-10-27 |

## Synthesis

n/a
