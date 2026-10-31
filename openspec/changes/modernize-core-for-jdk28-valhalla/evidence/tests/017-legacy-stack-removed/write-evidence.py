from pathlib import Path
import collections
import csv
import difflib
import json
import re
import xml.etree.ElementTree as ET

ROOT = Path.cwd()
EV = Path(__file__).resolve().parent
REL = EV.relative_to(ROOT).as_posix()
audit = json.loads((EV / "final-audit.json").read_text())
before = json.loads((EV / "before-state.json").read_text())
static = json.loads((EV / "08-static-checks.json").read_text())
modules = ["xacml-core", "xacml-xml-jaxb", "xacml-xml", "xacml-json", "xacml-pdp", "xacml-test", "xacml-conformance-tests"]
reactor = ["xacml", *modules]
legacy = re.compile(r"junit:junit:|junit-vintage-engine:|org\.easymock:|org\.objenesis:")
dependency_receipts = []
for filename in ("12-final-test-tree.log", "13-final-junit-tree.log"):
    text = (EV / filename).read_text()
    seen = re.findall(r"--- dependency:3\.11\.0:tree \(default-cli\) @ ([^ ]+) ---", text)
    assert seen == reactor, (filename, seen)
    assert "BUILD SUCCESS" in text
    matches = legacy.findall(text)
    assert not matches, (filename, matches)
    dependency_receipts.append({"file": filename, "reactor_projects": seen, "legacy_artifact_matches": matches})
(EV / "14-dependency-checks.json").write_text(json.dumps(dependency_receipts, indent=2) + "\n")

conformance = {}
for name in ("Xacml20ConformanceTest", "Xacml30ConformanceTest", "RSA2008InteropTest", "XacmlExampleTest", "VendoredFixtureIntegrityTest"):
    path = ROOT / "xacml-conformance-tests/target/surefire-reports" / ("TEST-org.xacml4j.v20." + name + ".xml")
    suite = ET.parse(path).getroot()
    output = "\n".join(node.text or "" for node in suite.iter("system-out"))
    cases = re.findall(r'Executing test="([^"]+)"', output)
    counts = collections.Counter(cases)
    conformance[name] = {"surefire_tests": int(suite.get("tests")), "errors": int(suite.get("errors")),
                         "failures": int(suite.get("failures")), "execution_receipts": len(cases),
                         "unique_case_ids": len(counts), "duplicates": {key: count for key, count in counts.items() if count > 1},
                         "case_ids": cases, "source": path.relative_to(ROOT).as_posix()}
assert conformance["Xacml20ConformanceTest"]["execution_receipts"] == 347
assert conformance["Xacml30ConformanceTest"]["execution_receipts"] == 347
assert conformance["RSA2008InteropTest"]["surefire_tests"] == 17
assert conformance["XacmlExampleTest"]["surefire_tests"] == 2
(EV / "14-conformance-case-counts.json").write_text(json.dumps(conformance, indent=2) + "\n")

with (EV / "14-per-class-counts.csv").open("w", newline="") as file:
    writer = csv.writer(file)
    writer.writerow(["module", "class", "task_start_report_tests", "final_tests", "final_failures", "final_errors", "final_skipped"])
    for module in modules:
        old = {suite["class"]: suite["counts"][0] for suite in before["reports"][module]["suites"]}
        new = {suite["class"]: suite for suite in audit["reports"][module]["suites"]}
        for name in sorted(old.keys() | new.keys()):
            counts = new[name]["counts"] if name in new else [0, 0, 0, 0]
            writer.writerow([module, name, old.get(name, 0), *counts])

with (EV / "14-source-method-counts.csv").open("w", newline="") as file:
    writer = csv.writer(file)
    writer.writerow(["module", "file", "before_test_method_annotations", "after_test_method_annotations", "byte_identical"])
    source_counts = json.loads((EV / "08-source-method-counts.json").read_text())
    for module, files in source_counts.items():
        for name, annotations in files.items():
            assert name not in audit["source_changes"]
            total = sum(annotations.values())
            writer.writerow([module, name, total, total, True])

patch = []
for name, copy in (("pom.xml", "before-root-pom.xml"), ("xacml-core/pom.xml", "before-core-pom.xml")):
    patch.extend(difflib.unified_diff((EV / copy).read_text().splitlines(True), (ROOT / name).read_text().splitlines(True),
                                    fromfile="a/" + name, tofile="b/" + name))
(EV / "task-2.7.patch").write_text("".join(patch))

# Keep exact Results blocks and per-class count lines from the authoritative gate.
parts = []
for filename in ("05-objenesis-exclusion-probe.log", "07-jdk21-clean-verify.log", "10-final-jdk21-clean-verify.log"):
    text = (EV / filename).read_text()
    assert "\0" not in text, filename
    lines = text.splitlines()
    parts.append("\n=== " + filename + " ===\n")
    parts.extend(line + "\n" for line in lines if "Tests run:" in line and " - in " in line)
    for index, line in enumerate(lines):
        if "[INFO] Results:" not in line:
            continue
        end = next((end for end in range(index + 1, len(lines)) if re.search(r"\[INFO\] ---", lines[end])), len(lines))
        parts.append("\n" + "\n".join(lines[index:end]) + "\n")
    parts.extend(line + "\n" for line in lines if "BUILD SUCCESS" in line or "BUILD FAILURE" in line)
parts.append("\nFinal per-module counts (tests / failures / errors / skipped), from fresh Surefire XML:\n")
for module in modules:
    parts.append(module + ": " + " / ".join(str(count) for count in audit["counts"][module]) + "\n")
parts.append("\nConformance: OASIS 2.0 347 execution receipts; OASIS 3.0 347; RSA 17; examples 2; integrity test 1.\n")
parts.append("Each OASIS suite has 346 unique IDs; the existing IIIG006 invocation appears twice. No harness or fixture was changed.\n")
parts.append("Fresh Surefire classpaths: only junit-jupiter TestEngine in all six tested modules (11-engine-inventory.json).\n")
(EV / "surefire-summary.txt").write_text("".join(parts))

maven = 'JAVA_HOME="$(jenv prefix 21)" rtk mvn -Dlicense.skipUpdateLicense=true '
commands = [
    "# Task 2.7. All commands from <repo>; JDK 21. No install, br or Git writes.",
    "# Read AGENTS.md, docs/ai/TESTING.md and OPENSPEC Apply loop; proposal, design D4, tasks 2.7 and test-framework spec.",
    "# Read Luna's 016 plan/patch/commands. Its patch applied cleanly in this snapshot.",
    "rtk git branch --show-current", "rtk git status --short",
    "rtk git apply --check openspec/changes/modernize-core-for-jdk28-valhalla/evidence/builds/016-legacy-stack-removal-plan/proposed-2.7-pom.patch",
    "rtk python3 " + REL + "/audit.py prepare",
    maven + "-version",
    "# First attempt: sandbox denied a resolver-status.properties cache write. Same command then rerun with escalation.",
    maven + "dependency:tree -Dscope=test -Dincludes=junit:junit -Dverbose -l " + REL + "/01-before-junit-tree.log",
    "rtk proxy mv " + REL + "/01-before-junit-tree.log " + REL + "/00-before-junit-tree-sandbox.log",
    maven + "dependency:tree -Dscope=test -Dincludes=junit:junit -Dverbose -l " + REL + "/01-before-junit-tree.log",
    "# Applied Luna's removal with native apply_patch, changing only root/core POMs.",
    maven + "dependency:tree -Dscope=test -Dincludes=junit:junit -Dverbose -l " + REL + "/02-luna-patch-junit-tree.log",
    "# Dependency gate RED: Truth 0.45 still brings junit:junit 4.12 into all eight projects.",
    "# Added junit:junit exclusion to managed Truth, with native apply_patch.",
    maven + "dependency:tree -Dscope=test -Dincludes=junit:junit -Dverbose -l " + REL + "/03-after-junit-tree.log",
    maven + "dependency:tree -Dscope=test -Dverbose -l " + REL + "/04-after-full-test-tree.log",
    "# Dependency gate RED: retained Mockito brings Objenesis 3.3 into all eight projects.",
    "# Added Objenesis exclusions to both inherited Mockito dependencies, with native apply_patch.",
    maven + "-pl xacml-core -Dtest=RuleTest#testPermitRuleEvaluateIfApplicableWithTargetEvaluationException test -l " + REL + "/05-objenesis-exclusion-probe.log",
    maven + "dependency:tree -Dscope=test -Dverbose -l " + REL + "/06-final-test-tree.log",
    maven + "clean verify -l " + REL + "/07-jdk21-clean-verify.log",
    "# Test gate RED: core 760 / 0 failures / 21 errors / 2 skips. Truth 0.45 eagerly links JUnit ComparisonFailure.",
    "rtk ./scripts/agent-doctor.sh ast-grep",
    "rtk python3 " + REL + "/scan.py",
    "# Pinned Truth and truth-java8-extension to 1.1.5 in the owned root POM; retained the JUnit exclusion.",
    maven + "clean verify -l " + REL + "/09-jdk21-clean-verify.log",
    "# Exit 0, but log normalized while Maven was writing; it contains NUL padding and is superseded by run 10.",
    maven + "clean verify -l " + REL + "/10-final-jdk21-clean-verify.log",
    "rtk python3 " + REL + "/audit.py finish",
    "rtk python3 " + REL + "/runtime-probes.py",
    maven + "dependency:tree -Dscope=test -Dverbose -l " + REL + "/12-final-test-tree.log",
    maven + "dependency:tree -Dscope=test -Dincludes=junit:junit -Dverbose -l " + REL + "/13-final-junit-tree.log",
    "# Logs normalized only after Maven exited, except the superseded run 09 noted above.",
    "rtk python3 " + REL + "/audit.py normalize",
    "rtk python3 " + REL + "/scan.py",
    "# scan.py records each exact rg/Java AST argv, output and status in 08-static-checks.json.",
    "# runtime-probes.py uses JDK 21 Java source-file execution, with each exact classpath read from its fresh Surefire XML.",
    "# Command form: rtk proxy <jdk21>/bin/java --class-path <Surefire classpath> <run>/TestEngineInventory.java <module>",
    "# Assertion probe form: rtk proxy <jdk21>/bin/java --class-path <core Surefire classpath> <run>/TruthWithoutJunit.java",
    "rtk git diff --check -- pom.xml xacml-core/pom.xml",
    "rtk git status --short -- pom.xml xacml-core/pom.xml xacml-core/src/test",
    "rtk python3 " + REL + "/write-evidence.py",
    "# First final-check helper compared XML whitespace tails; replaced it with an exact byte comparison of the core Vintage deletion.",
    "rtk python3 " + REL + "/final-checks.py",
    "rtk python3 " + REL + "/audit.py normalize",
    "rtk python3 " + REL + "/final-checks.py",
]
(EV / "command.txt").write_text("\n".join(commands) + "\n")

sources = [name for name in before["files"] if "/src/" in name]
fixture_count = sum(name.startswith("xacml-conformance-tests/src/test/resources/")
                    or name.startswith("xacml-xml-jaxb/src/main/resources/xsd/") for name in before["files"])
assert fixture_count == 2911
total = [sum(audit["counts"][module][index] for module in modules) for index in range(4)]
assert total == [934, 0, 0, 6]
table = "\n".join("| " + module + " | " + " | ".join(str(count) for count in audit["counts"][module]) + " |" for module in modules)
method_table = "\n".join("| " + module + " | " + str(count) + " | " + str(count) + " |"
                         for module, count in static["source_test_method_annotations"].items())
summary = f"""# Task 2.7: legacy test stack removed

The full JDK 21 reactor passes: **934 tests, 0 failures, 0 errors, 6 skips**.
Only root `pom.xml` and `xacml-core/pom.xml` were edited by this lane outside
this evidence directory. All module source files and all 2,911 vendored
fixture/schema files remain byte-identical to the task-start snapshot.

Java version: **21.0.6**, vendor: Oracle Corporation, GraalVM JDK 21; Maven
**3.9.16**. Branch: `{before['branch']}`; HEAD at task start:
`{before['head']}`. Every Maven run used the license-header skip property.

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

Authoritative command: `JAVA_HOME=\"$(jenv prefix 21)\" rtk mvn -Dlicense.skipUpdateLicense=true clean verify -l {REL}/10-final-jdk21-clean-verify.log`.
Result: exit **0**, `BUILD SUCCESS`, all eight reactor projects SUCCESS.

| Module | Tests run | Failures | Errors | Skipped |
| --- | ---: | ---: | ---: | ---: |
{table}
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
  stdout/stderr files. {static['java_files']} Java source files are inventoried.
- The retained Truth and Truth8 assertion probes pass positive assertions
  and reject deliberately wrong values with AssertionError and expected
  message facts, while legacy classes are absent (11-truth-oracle.txt).
- Before/after source test-method annotation counts are identical, confirmed
  by byte-identical source files and the Java AST inventory:

| Module | Before | After |
| --- | ---: | ---: |
{method_table}

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
"""
(EV / "summary.md").write_text(summary)
print(json.dumps({"gate": "PASS", "totals": total, "dependency_projects": reactor,
                  "OASIS_execution_counts": [347, 347], "source_changes": audit["source_changes"],
                  "fixture_count": fixture_count}, indent=2))
