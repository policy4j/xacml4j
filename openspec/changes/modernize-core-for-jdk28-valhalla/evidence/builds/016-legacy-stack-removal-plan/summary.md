# Task 2.7 legacy test-stack removal plan

## Current snapshot

Read-only inventory on 2026-10-01. `dependency:tree` succeeded for all eight
reactor projects with JDK 21 / Maven 3.9.16; the output is retained in
`deptree.log`. The first sandboxed invocation failed when Maven tried to write
local repository resolver metadata (`Operation not permitted`). The same
authorized command was rerun with elevated execution and completed with
`BUILD SUCCESS`. No compile, test, or verify goal was run.

The task packet's expectation that PDP is still JUnit 4 is stale: at scan time
its test files were dirty and use Jupiter, and the scoped JUnit 4 / EasyMock
source searches returned no matches there. `xacml-core/src/test/java` is also
actively changing: status showed many modified tests and nine untracked
test/helper files (including `ModelFixtures.java`). Treat these source scan
results as a snapshot, not as a settled baseline; repeat them after Sol's edits
land.

## POM declarations and management

| POM line(s) | Declaration | Task 2.7 disposition |
| --- | --- | --- |
| `pom.xml:31` | `junit.version=5.14.4`; also used by imported JUnit BOM at `pom.xml:151-156` | Keep for Jupiter dependency management. |
| `pom.xml:86-88` | Parent dependency on `org.junit.vintage:junit-vintage-engine` | Remove after all modules are Jupiter-only. |
| `pom.xml:158-162` | Managed Vintage version/scope | Remove with the Vintage dependency. |
| `xacml-core/pom.xml:37-39` | Additional direct Vintage dependency | Remove; it is redundant after migration and otherwise keeps Vintage on core's test classpath. |
| `pom.xml:36` | `easymock.version=5.1.0` | Remove. |
| `pom.xml:123-125` | Parent dependency on `org.easymock:easymock` | Remove. |
| `pom.xml:251-255` | Managed EasyMock dependency/version/scope | Remove. |
| `pom.xml:37` | `objenesis.version=3.3` | Remove. |
| `pom.xml:127-129` | Parent dependency on `org.objenesis:objenesis` | Remove. |
| `pom.xml:257-261` | Managed Objenesis dependency/version/scope | Remove. |
| `pom.xml:41`, `pom.xml:135-140`, `pom.xml:269-279` | Truth version, parent test dependencies, and management | Keep; tests still import Truth. |
| `pom.xml:47`, `pom.xml:143-145`, `pom.xml:263-266` | Hamcrest version, parent test dependency, and management | Keep; tests still import Hamcrest. |

No POM declares or manages `junit:junit` directly. It enters through
Vintage; the JUnit BOM manages the JUnit version family. POM search found
these declarations only in root `pom.xml` and `xacml-core/pom.xml`.

## Dependency tree paths and smallest fixes

The inherited root test dependencies appear in the test-scoped tree of all
eight projects: `xacml`, `xacml-core`, `xacml-xml-jaxb`, `xacml-xml`,
`xacml-json`, `xacml-pdp`, `xacml-test`, and `xacml-conformance-tests`.

| Test classpath path (each listed reactor project) | Smallest fix | Risk |
| --- | --- | --- |
| Parent direct `org.junit.vintage:junit-vintage-engine:5.14.4` -> transitive `junit:junit:4.13.2` -> transitive `org.hamcrest:hamcrest-core:1.3` | Remove the parent Vintage dependency and its management entry; remove core's extra Vintage declaration. | Vintage discovery disappears. Current source scans find no JUnit 4 API use; run the final full JDK 21 gate to catch indirect/dynamic discovery needs. JUnit 4 and `hamcrest-core` should then disappear from the tree. |
| Parent direct `org.easymock:easymock:5.1.0` | Remove the dependency and management entry, then remove `easymock.version`. | None indicated by current source scan; tests must already have completed migration. |
| Parent direct `org.objenesis:objenesis:3.3` | Remove dependency and management entry, then remove `objenesis.version`. | No source reference found; current Mockito tree does not bring Objenesis transitively. Confirm after removal with dependency tree. |
| Parent direct `com.google.truth:truth:0.45` and `truth-java8-extension:0.45` | Keep; no JUnit 4 path is present below Truth 0.45 in this tree. | Updating/removing Truth could require rewriting live assertions; not needed for legacy removal. |
| Parent direct `org.hamcrest:hamcrest-all:1.3` | Keep; source still imports `org.hamcrest`. It is distinct from `hamcrest-core` reached through Vintage. | Removing it would break remaining Hamcrest-based tests. |

The dependency tree repeats the same paths under each module, because the
test-stack dependencies are declared in the parent POM. Truth 0.45's subtree
contains Guava, Checker Framework, diff-utils, and AutoValue annotations; it
does **not** contain `junit:junit`, Vintage, EasyMock, or Objenesis.

## Source scan

The scoped `rtk rg` searches and ast-grep import searches covered `src/main`
and `src/test` in core, PDP, XML/JAXB, XML, JSON, test support, and
conformance. At this snapshot they found:

- No JUnit 4 imports/usages (`org.junit.Test` / `Assert` / lifecycle / rules /
  runners / `Assume`, `junit.framework`, `@Test(expected=...)`,
  `@RunWith`, `@Ignore`, `ExpectedException`, or `TemporaryFolder`).
- No EasyMock imports, APIs, names in comments/strings, or `org.easymock`
  references; no `org.hamcrest.junit` reference.
- Jupiter imports remain, including in `xacml-test/src/main`; those are not
  legacy JUnit 4 usage. Keep the JUnit BOM/Jupiter dependencies needed by the
  migrated modules.
- Truth remains in core, PDP, XML, and JSON test sources; Hamcrest remains in
  core, XML, and JSON test sources. Keep both dependency families and their
  versions.

The Xacml-core test tree was being edited during this scan (many modified
files plus nine untracked test/helper files). PDP tests were also modified
and, contrary to the packet's earlier snapshot, now scan as Jupiter-only.
Rerun the source scan after the concurrent migration work settles.

## Proposed POM patch

`proposed-2.7-pom.patch` is an unapplied, `rtk git apply --check`-validated
patch. It removes only the root Vintage/EasyMock/Objenesis declarations and
management, their obsolete version properties, and core's extra Vintage
dependency. It retains the JUnit BOM/property for Jupiter and retains Truth
and Hamcrest versions/dependencies because source still uses them.

## Task 2.7 verification commands

After all migration edits settle and the proposed patch is applied:

1. Run `JAVA_HOME="$(jenv prefix 21)" rtk mvn -Dlicense.skipUpdateLicense=true clean verify -l openspec/changes/modernize-core-for-jdk28-valhalla/evidence/builds/016-legacy-stack-removal-plan/jdk21-clean-verify.log`.
2. Run the dependency tree command from `command.txt`; confirm no
   `junit:junit`, Vintage, EasyMock, or Objenesis paths in any reactor module.
3. Run the legacy source scans in `command.txt` across every module's `src`;
   require no matches. Jupiter imports are allowed.
4. Parse fresh `target/surefire-reports/TEST-*.xml` totals per module and
   compare test identities/counts to the accepted 010 / 011 / 012 / 013
   evidence. Current migrated receipts report core 760 Surefire tests (550
   declared methods), PDP 73 Surefire tests (75 source `@Test` annotations),
   XML 34, JSON 5, and test support 20. The 013 conformance receipt has 42
   source `@Test` methods and 41 Surefire tests; task 3.5 adds one integrity
   method, so the current conformance expectation is 43 source methods and
   42 Surefire tests (the performance TestCase remains excluded). PDP's
   migrated Vintage-to-Jupiter result is 73 tests / 3 skips versus the old
   74 / 4 because Vintage counted class-level ignored `TestObject`, which
   Jupiter does not discover as a test. Compare source methods and per-class
   identities as well as aggregate Surefire counts.

No full 2.7 gate was run in this prep task.
