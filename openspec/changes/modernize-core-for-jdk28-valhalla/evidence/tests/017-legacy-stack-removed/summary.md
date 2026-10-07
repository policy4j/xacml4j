# Task 2.7: legacy test stack removed

The full JDK 21 reactor passes: **934 tests, 0 failures, 0 errors, 6 skips**.
Only root `pom.xml` and `xacml-core/pom.xml` were edited by this lane outside
this evidence directory. All module source files and all 2,911 vendored
fixture/schema files remain byte-identical to the task-start snapshot.

Java version: **21.0.6**, vendor: Oracle Corporation, GraalVM JDK 21; Maven
**3.9.16**. Branch: `integration/modernize-core-jdk28-valhalla`; HEAD at task start:
`befdd116bd11385ac8d007732fa8e4d347af3143`. Every Maven run used the license-header skip property.

## POM changes and plan corrections

- Removed root and core Vintage declarations, root Vintage/EasyMock/Objenesis
  management, inherited EasyMock/Objenesis dependencies, and the obsolete
  `easymock.version` and `objenesis.version` properties. There was no direct
  `junit:junit` declaration to remove.
- Luna's claim that Truth 0.45 has no JUnit dependency was false. The verbose
  filtered tree lists Truth -> JUnit 4.12 in **all eight** reactor projects
  (01-before-junit-tree.log); after applying Luna's patch alone, JUnit 4.12
  remained in all eight (02-luna-patch-junit-tree.log).
- Added a managed `junit:junit` exclusion to Truth. The first clean gate then
  exposed the runtime dependency: core **760 / 0 / 21 / 2**, rooted in
  `NoClassDefFoundError: org/junit/ComparisonFailure` (07-jdk21-clean-verify.log).
  Updated the existing shared `google.truth.version` from **0.45 to 1.1.5**
  for Truth and truth-java8-extension; kept every assertion and test source.
  Truth 1.1.5's [Platform implementation](https://github.com/google/truth/blob/v1.1.5/core/src/main/java/com/google/common/truth/Platform.java)
  falls back to AssertionErrorWithFacts when JUnit ComparisonFailure cannot
  load. The [1.4.2 release notes](https://github.com/google/truth/releases/tag/v1.4.2)
  document potential Truth/Truth8 overload ambiguity; 1.1.5 preserves the
  current assertion API without edits to the tests under review.
- Luna's claim that Mockito has no transitive Objenesis was also false:
  Mockito 5.24.0 -> Objenesis 3.3 appeared in every project (04-after-full-test-tree.log).
  Excluded Objenesis from both inherited Mockito dependencies. The focused
  existing Target-spy test passed **1 / 0 / 0 / 0** (05), followed by the full
  green gate. The actual default inline mocks used in this reactor work
  without Objenesis on this JDK.
- Kept JUnit BOM/Jupiter **5.14.4**, Mockito **5.24.0**, Hamcrest **1.3**,
  and both Truth artifacts. Dependency-plugin properties and the startup-agent
  Surefire `argLine` are unchanged. No other module POM or plugin configuration changed.
  [task-2.7.patch](task-2.7.patch) is the exact task-start-to-final POM diff.

## Full clean gate

Authoritative command: `JAVA_HOME="$(jenv prefix 21)" rtk mvn -Dlicense.skipUpdateLicense=true clean verify -l openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/017-legacy-stack-removed/10-final-jdk21-clean-verify.log`.
Result: exit **0**, `BUILD SUCCESS`, all eight reactor projects SUCCESS.

| Module | Tests run | Failures | Errors | Skipped |
| --- | ---: | ---: | ---: | ---: |
| xacml-core | 760 | 0 | 0 | 2 |
| xacml-xml-jaxb | 0 | 0 | 0 | 0 |
| xacml-xml | 34 | 0 | 0 | 0 |
| xacml-json | 5 | 0 | 0 | 1 |
| xacml-pdp | 73 | 0 | 0 | 3 |
| xacml-test | 20 | 0 | 0 | 0 |
| xacml-conformance-tests | 42 | 0 | 0 | 0 |
| Total | 934 | 0 | 0 | 6 |

Counts match the task packet exactly. The root and JAXB projects have no
tests. [surefire-summary.txt](surefire-summary.txt) preserves Maven's per-class
count lines and Results blocks. The 103 core classes still run **760** tests.
Core tests under rv019 review were not edited. Source hashes and per-class
counts are in final-audit.json and 14-per-class-counts.csv.

The first green clean gate (09) also exited 0. Its log was normalized while
Maven was writing and contains NUL padding, so it is superseded by the
complete, NUL-free run 10. No source/POM changed between the two green gates.
The initial sandbox dependency-tree attempt failed on a Maven cache metadata
write; the same command was rerun with escalation and succeeded (00/01).

## Conformance results

OASIS 2.0: **347** execution receipts, **0 failed**; OASIS 3.0: **347**,
**0 failed**; RSA 2008: **17**, **0 failed**; examples: **2**, **0 failed**.
VendoredFixtureIntegrityTest: **1**, **0 failed** against **2,911** blobs.
The conformance module has **42** Surefire tests: two 10-method loop runners,
17 RSA invocations, two examples, two resolver tests, and one integrity test.
Each OASIS runner emits **346 unique case IDs** because the existing
`IIIG006` execution is repeated. These counts describe the current harness;
this task did not alter loop bounds, exclusion sets, fixtures, expected
results or source code. See 14-conformance-case-counts.json for every emitted
case ID and duplicate. The task-start integrity failure report was from the
earlier scratch mutation probe; the fresh clean gate passes on the worktree.

## Dependency, engine and source proof

- Final full test-scoped verbose tree (12) and `-Dincludes=junit:junit`
  tree (13) completed for **all eight projects**. No selected or omitted
  JUnit 4/Vintage/EasyMock/Objenesis artifacts remain. The filtered JUnit
  result is empty in each project. Exact commands and raw logs are retained;
  14-dependency-checks.json checks each tree execution and empty result.
- Maven's Surefire output records JUnitPlatformProvider. ServiceLoader over
  each fresh Surefire test classpath discovers only **junit-jupiter** in all
  six modules with tests (11-engine-inventory.json). Every such classpath
  lacks legacy artifacts; root/JAXB show Jupiter only in their dependency
  trees. The Mockito startup-agent argLine is retained.
- Independent `rtk rg` scans return exit **1**, empty output, for legacy
  imports/usages and annotations across every module's main/test Java
  sources. Java ast-grep returns **[]**, exit **0**, for the same scope.
  The explicit Java rule is legacy-test-stack.yml; the synthetic positive
  control produces **48** diagnostics, confirming the detector works.
  Scan outputs and exact argv are in 08-static-checks.json and companion
  stdout/stderr files. 505 Java source files are inventoried.
- The retained Truth and Truth8 assertion probes pass positive assertions
  and reject deliberately wrong values with AssertionError and expected
  message facts, while legacy classes are absent (11-truth-oracle.txt).
- Before/after source test-method annotation counts are identical, confirmed
  by byte-identical source files and the Java AST inventory:

| Module | Before | After |
| --- | ---: | ---: |
| xacml-core | 550 | 550 |
| xacml-pdp | 75 | 75 |
| xacml-xml-jaxb | 0 | 0 |
| xacml-xml | 34 | 34 |
| xacml-json | 5 | 5 |
| xacml-test | 20 | 20 |
| xacml-conformance-tests | 43 | 43 |

Parameterized/inherited invocations differ from declared source-method
counts. Surefire's Jupiter counts remain at the accepted migrated baseline.
PDP's task-start target reports still included Vintage's empty disabled
TestObject class (74/4); the final Jupiter gate gives the required **73/3**.
See 14-source-method-counts.csv for the per-file inventory.

## Scope and remaining limits

No source, header, fixture, schema, test expectation or other module POM was
changed. `rtk git diff --check` passes. The snapshot also observed a concurrent
tasks.md tracking update checking 2.2/2.3; this lane did not edit tasks.md and
left that shared change intact. Task 2.7 remains unchecked.

No commit, stage, br, install, task tick or issue close was performed.
The conductor indexes this receipt and controls task tracking.
Alternative Mockito mock-maker/instantiator modes without Objenesis are
**UNVERIFIED**; the full existing default-inline suite and allowed spies are
green on JDK 21. JDK 28/Valhalla is outside this gate.

## Review correction (rv026)

Agy (rv026) found the Objenesis exclusion from mockito-core and mockito-junit-jupiter unsafe: Mockito 5.24.0 uses ObjenesisInstantiator as its default instantiator and as the inline mock maker fallback for class mocks whose constructor throws. The conductor deleted both exclusion blocks; Mockito keeps its transitive org.objenesis:objenesis 3.3. The direct Objenesis dependency, its management entry, and objenesis.version stay removed, which is what task 2.7 required (EasyMock needed Objenesis directly). Verification: the next full JDK 21 reactor gate and dependency:tree (recorded in the addendum below when run).

### Addendum: verification

Verified after the conductor restored Mockito's transitive Objenesis. JDK 21.0.6; Maven 3.9.16.
All eight reactor projects passed clean verify twice (run 023 logs 09 and 15). The final gate retained the same source/POM hashes before and after.
The first full gate (09) retained run 017's core/XML/JSON/PDP/test-support counts and expanded conformance from 42 Surefire tests to 901: 1,793 tests, zero failures/errors and 50 skips. Concurrent outside-lane tests raised core 760 to 765, XML 34 to 35, JSON 5 to 6 and PDP 73 to 74 before the final gate (15), which ran 1,801 tests. All module counts meet or exceed run 017; no prior distinct conformance case was removed. Final counts:

| Module | Tests run | Failures | Errors | Skipped |
| --- | ---: | ---: | ---: | ---: |
| root (pom) | 0 | 0 | 0 | 0 |
| xacml-core | 765 | 0 | 0 | 2 |
| xacml-xml-jaxb | 0 | 0 | 0 | 0 |
| xacml-xml | 35 | 0 | 0 | 0 |
| xacml-json | 6 | 0 | 0 | 1 |
| xacml-pdp | 74 | 0 | 0 | 3 |
| xacml-test | 20 | 0 | 0 | 0 |
| xacml-conformance-tests | 901 | 0 | 0 | 44 |
| **Total** | **1801** | **0** | **0** | **50** |

Conformance: 362 OASIS 2.0, 455 OASIS 3.0, 17 RSA and two examples passed; 44 fixture cases have documented blocking dispositions. The other six unit-test skips are unchanged.

Exact plain Maven commands:

```sh
JAVA_HOME="$(jenv prefix 21)" rtk mvn -Dlicense.skipUpdateLicense=true -l openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/023-conformance-manifest/09-jdk21-clean-verify.log clean verify
JAVA_HOME="$(jenv prefix 21)" rtk mvn -Dlicense.skipUpdateLicense=true -l openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/023-conformance-manifest/15-jdk21-final-clean-verify.log clean verify
JAVA_HOME="$(jenv prefix 21)" rtk mvn -Dlicense.skipUpdateLicense=true -Dscope=test -Dincludes=org.objenesis:objenesis -l openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/023-conformance-manifest/12-objenesis-tree-retry.log dependency:tree
JAVA_HOME="$(jenv prefix 21)" rtk mvn -Dlicense.skipUpdateLicense=true -Dscope=test "-Dincludes=junit:junit,org.junit.vintage:*,org.easymock:*" -l openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/023-conformance-manifest/13-legacy-tree-retry.log dependency:tree
```

Both dependency checks completed for every project. Objenesis is present only as org.mockito:mockito-core:5.24.0 -> org.objenesis:objenesis:3.3. JUnit 4, Vintage and EasyMock results are empty in all eight projects. Initial sandbox cache-write denials (10/11) are retained; approved native retries (12/13) provide the complete proof.
This supersedes the earlier run 017 claims that transitive Objenesis is absent; direct dependency/management/property removal is retained. This lane made no POM edits.

Evidence: [run 023 summary](../023-conformance-manifest/summary.md), [Surefire results](../023-conformance-manifest/surefire-summary.txt), [dependency proof](../023-conformance-manifest/dependency-checks.json), [per-case results](../023-conformance-manifest/per-case-results.csv).
