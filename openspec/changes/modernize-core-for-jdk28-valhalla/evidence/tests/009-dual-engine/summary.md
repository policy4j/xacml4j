# Task 2.1: dual JUnit engines on JDK 21

Result: **dual-engine/Mockito proof PASS; clean-reactor baseline parity PASS**.
The full gate completed with failure-ignore enabled: **936 tests, 0 failures,
96 errors, 9 skipped**, exactly run 008. This establishes transition parity;
it is not an all-tests-green result. All 96 errors remain the existing
EasyMock class-proxy errors in xacml-core.

JDK: `java version "21.0.6" 2025-01-21 LTS`; Oracle GraalVM
`21.0.6+8.1`, VM build `21.0.6+8-LTS-jvmci-23.1-b55`.
Exact version output: `11-java-version.txt`. All 143 fresh full-gate
Surefire XML reports independently record Java 21.0.6.

## Root POM change

- JUnit 5 BOM/Jupiter/Vintage 5.14.4; Mockito core and Jupiter extension
  5.24.0, test scope. JUnit 4, Vintage, EasyMock 5.1.0 and Objenesis 3.3 remain.
- Keep Surefire 3.0.0: its JUnit Platform provider executed both engines,
  and independent engine-filter runs proved their identities.
- Use Mockito's documented JDK 21 startup agent. Dependency plugin 3.11.0
  `properties` resolves the Mockito artifact path; Surefire uses
  `@{argLine} -javaagent:${org.mockito:mockito-core:jar}`, with an empty
  default `argLine`. No machine path is embedded. After this fix the probe
  emitted no self-attachment/dynamic-agent warning. The ordinary CDS warning
  from bootstrap instrumentation is retained in the raw output.
- All module POMs are unchanged. No module Surefire override blocks Jupiter.
  The prior xacml-test JUnit 4 compile-scope dependency stays unchanged.

Incremental task delta, excluding pre-existing release-21 edits:
`08-root-pom-fixes.patch`. Version dates, official sources and reasoning:
[documentation.md](documentation.md). **Context7 UNAVAILABLE**: its native
MCP initialization failed with HTTP 500/upstream DNS failure, so the requested
resolve/query tools could not be called. Native DuckDuckGo MCP retrieved
official publication metadata/documentation as the fallback. This lookup
gap remains explicitly recorded; no MCP configuration was changed.

## Test-first proof and temporary test removal

The throwaway JSON probe uses Jupiter, MockitoExtension and a final
collaborator class; it checks stubbing, interaction verification and the
Mockito startup-agent JVM argument. It remains only as evidence text in
`08-probe-source.java.txt`.

| Run | Test result / reason |
| --- | --- |
| 02, before POM edits | RED: 16 compiler diagnostics, all in the throwaway probe: missing Jupiter/Mockito packages, static imports and annotations. No tests executed. Each diagnostic is in the receipt. |
| 03 | Environmental abort: Maven dependency cache outside the sandbox was unwritable; no tests executed. |
| 04, dependencies added | RED: 6 / 1 / 0 / 1. Mock creation/stubbing passed; the startup-agent assertion failed and self-attachment warnings were emitted. Cache access was approved for the retry. |
| 05, documented agent added | GREEN: both engines, 6 / 0 / 0 / 1 (Jupiter probe 1; Vintage 5 including one existing skip). |
| 06, junit-jupiter filter | GREEN: 1 / 0 / 0 / 0, only the probe executed. |
| 07, junit-vintage filter | GREEN: 5 / 0 / 0 / 1, only four existing JUnit 4 JSON classes executed. |
| 08 | Probe source deleted; protected-file and POM hash checks passed. |
| 09, probe absent, clean reactor | Baseline parity PASS: 936 / 0 / 96 / 9; Maven BUILD SUCCESS with test failure-ignore. |
| 10 / 10b, dependency tree | Initial unquoted pattern failed in the shell; quoted direct-goal run then hit cache permissions. Approved retry with test-compile plus dependency:tree succeeded, resolving reactor artifacts without running tests. |

Count order above: Tests / Failures / Errors / Skipped.
The final clean build removed the probe's compiled classes and report.
No lasting test source or engine filter was added.

## Full clean JDK 21 gate

Command (exact invocation also in `command.txt`):

```sh
JAVA_HOME="$(jenv prefix 21)" rtk mvn -Dlicense.skipUpdateLicense=true clean verify -Dmaven.test.failure.ignore=true -l openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/009-dual-engine/09-full-reactor-clean-verify-jdk21.log
```

| Module | Tests run | Failures | Errors | Skipped |
| --- | ---: | ---: | ---: | ---: |
| Root aggregator | 0 | 0 | 0 | 0 |
| xacml-core | 762 | 0 | 96 | 4 |
| xacml-xml-jaxb | 0 | 0 | 0 | 0 |
| xacml-xml | 34 | 0 | 0 | 0 |
| xacml-json | 5 | 0 | 0 | 1 |
| xacml-pdp | 74 | 0 | 0 | 4 |
| xacml-test | 20 | 0 | 0 | 0 |
| xacml-conformance-tests | 41 | 0 | 0 | 0 |
| **Total** | **936** | **0** | **96** | **9** |

No compiler errors in the final gate. Every error is
`java.lang.IllegalArgumentException: Could not create type`, with
`ClassProxyFactory.createProxy` and `MocksControl.createMock` in its trace.
The 96 class/method/type/message identities match the run-008 retained
reports exactly; no new failure/error category or module-access error.
Full classification: `easymock-error-parity.json`, final gate receipt and
`surefire-summary.txt`. The retained baseline receipt is labeled historical;
the gate counts and traces come from the fresh clean execution.

Conformance: OASIS 2.0 **347 executions**, OASIS 3.0 **347 executions**,
RSA 2008 **17 cases**, **2 examples**, plus **2 resolver characterization
methods**; all passed. Each OASIS suite has 346 distinct IDs because
IIIG006 executes twice, as in run 008. Failed case IDs: **none**.
Both OASIS case sequences match run 008 in order. Fixtures, expected
results, exclusions and the existing skips are unchanged. Details:
`conformance-parity.json`.

## Scope and remaining work

Only root `pom.xml` has a persistent project-file change from this task,
plus this evidence directory. `scope-check.json` confirms all 4,199
protected files (including module POMs, existing source/test files,
fixtures, tasks, evidence index and prior runs 003/005/008) have the same
bytes as at task start. No license-header churn; root diff check passed.
No new source files or temporary probe classes remain. Branch/HEAD and
empty staging area were verified; shared prior dirty work is preserved.

Residual risk: the 96 baseline EasyMock errors await the planned test
migration. Context7-specific release lookup could not be completed;
official primary-source verification and local execution are recorded.
No commit, br command, task tick or issue closure was performed.

