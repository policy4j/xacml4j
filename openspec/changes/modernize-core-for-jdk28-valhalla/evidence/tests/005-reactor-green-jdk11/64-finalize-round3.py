from pathlib import Path
import difflib
import hashlib
import json
import re
import subprocess
import xml.etree.ElementTree as ET

repo = Path.cwd()
ev = Path(__file__).parent
relative = str(ev.relative_to(repo))
snapshot = json.loads((ev / "57-round3-before.json").read_text())
existing = snapshot["allowed_existing"] + snapshot["conditional_existing_not_yet_authorized_by_red"]
new = ["xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/HigherOrderVariadicFunctionsTest.java",
       "xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/HigherOrderVariadicInvocationTest.java"]
files = existing + new
protected = {name: digest for name, digest in snapshot["hashes"].items() if name not in existing}
changed = [name for name, digest in protected.items()
           if not (repo / name).is_file() or hashlib.sha256((repo / name).read_bytes()).hexdigest() != digest]
assert not changed, changed
current = set()
for module in repo.glob("xacml-*"):
    if module.is_dir():
        current.update(str(path.relative_to(repo)) for path in (module / "src").rglob("*") if path.is_file())
assert current - set(snapshot["hashes"]) == set(new), current - set(snapshot["hashes"]) - set(new)
header = re.compile(r"/\*.*?#L%.*?\*/", re.DOTALL)
patch = []
for name in files:
    before = (ev / ("57-before-" + Path(name).name + ".txt")).read_text() if name in existing else ""
    after = (repo / name).read_text()
    assert before != after, name
    template = before if before else (ev / "57-before-HigherOrderFunctions.java.txt").read_text()
    assert header.search(template).group() == header.search(after).group(), name
    delta = list(difflib.unified_diff(before.splitlines(keepends=True), after.splitlines(keepends=True),
        fromfile="a/" + name if before else "/dev/null", tofile="b/" + name))
    if before:
        assert all(line[1:].rstrip("\n") == line[1:].rstrip()
                   for line in delta if line.startswith("+") and not line.startswith("+++")), name
    else:
        assert all(line == line.rstrip() for line in after[header.search(after).end():].splitlines()), name
    patch.extend(delta)
(ev / "64-round3-fixes.patch").write_text("".join(patch))
(ev / "64-round3-file-hashes.json").write_text(json.dumps({name: hashlib.sha256((repo / name).read_bytes()).hexdigest()
                                                       for name in files}, indent=2) + "\n")
before_higher = (ev / "57-before-HigherOrderFunctions.java.txt").read_text()
after_higher = (repo / existing[0]).read_text()
for operation in ("all-of-any", "any-of-all", "all-of-all"):
    expression = re.compile(r'\t@XacmlFuncSpec\(id="urn:oasis:names:tc:xacml:1.0:function:' + operation + r'"\).*?(?=\n\t@XacmlFuncSpec)', re.DOTALL)
    assert expression.search(before_higher).group() == expression.search(after_higher).group(), operation
red = json.loads((ev / "59-round3-red-tests.json").read_text())
assert red["production_matches_before_snapshot"]
assert sum(result["failures"] for result in red["results"]) == 4
assert sum(result["errors"] for result in red["results"]) == 24
packing_red = json.loads((ev / "61-round3-packing-red-tests.json").read_text())
assert len(packing_red["results"]) == 1
assert packing_red["results"][0]["tests"] == 3 and packing_red["results"][0]["errors"] == 1
assert "ArrayStoreException" in json.dumps(packing_red)

gate_name = "63-reactor-verify-round3.txt"
gate = (ev / gate_name).read_text()
assert "BUILD SUCCESS" in gate and "BUILD FAILURE" not in gate
counts = {}
module = "unknown"
for line in gate.splitlines():
    match = re.search(r"--- .* @ ([^ ]+) ---", line)
    if match:
        module = match.group(1)
    count = re.search(r"Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)", line)
    if count and " - in " not in line:
        counts[module] = [int(value) for value in count.groups()]
assert counts == {"xacml-core": [740,0,0,4], "xacml-xml": [34,0,0,0], "xacml-json": [5,0,0,1],
                  "xacml-pdp": [74,0,0,4], "xacml-test": [20,0,0,0], "xacml-conformance-tests": [41,0,0,0]}
assert [sum(row[index] for row in counts.values()) for index in range(4)] == [914,0,0,9]
all_counts = {"xacml": [0,0,0,0], "xacml-core": counts["xacml-core"], "xacml-xml-jaxb": [0,0,0,0],
              **{name: row for name, row in counts.items() if name != "xacml-core"}}
(ev / "63-module-counts.json").write_text(json.dumps(all_counts, indent=2) + "\n")
jdk_by_module = {}
report_counts = {}
for module, expected in counts.items():
    total = [0,0,0,0]
    report_counts[module] = {}
    for path in (repo / module / "target/surefire-reports").glob("TEST-*.xml"):
        root = ET.parse(path).getroot()
        row = [int(root.get(key)) for key in ("tests", "failures", "errors", "skipped")]
        total = [a + b for a, b in zip(total, row)]
        report_counts[module][root.get("name")] = row
        props = {item.get("name"): item.get("value") for item in root.find("properties")}
        jdk = {key: props[key] for key in ("java.version", "java.runtime.version", "java.vm.name", "java.vendor")}
        assert jdk["java.version"] == "11.0.32.1", (module, path, jdk)
        jdk_by_module[module] = jdk
    assert total == expected, (module, total, expected)
(ev / "63-jdk-properties.json").write_text(json.dumps(jdk_by_module, indent=2) + "\n")
(ev / "63-surefire-report-counts.json").write_text(json.dumps(report_counts, indent=2) + "\n")
green = json.loads((ev / "63-round3-green-tests.json").read_text())
assert sum(result["tests"] for result in green["results"]) == 155
assert all(not result["failures"] and not result["errors"] and not result["skipped"] for result in green["results"])

cases = json.loads((ev / "63-conformance-testcases.json").read_text())
previous_cases = json.loads((ev / "53-conformance-testcases.json").read_text())
for name, suite in cases.items():
    assert all(case["failure"] is None for case in suite["testcases"])
    assert [case["case_ids"] for case in suite["testcases"]] == [case["case_ids"] for case in previous_cases[name]["testcases"]]
for name in ("Xacml20ConformanceTest", "Xacml30ConformanceTest"):
    ids = [case_id for case in cases["org.xacml4j.v20." + name]["testcases"] for case_id in case["case_ids"]]
    assert len(ids) == 347 and ids.count("IIIG006") == 2
    assert not any(case_id.startswith("IIIE") for case_id in ids)
assert len(cases["org.xacml4j.v20.RSA2008InteropTest"]["testcases"]) == 17
assert len(cases["org.xacml4j.v20.XacmlExampleTest"]["testcases"]) == 2
exclusions = {}
for name in ("Xacml20ConformanceTest", "Xacml30ConformanceTest"):
    source = (repo / "xacml-conformance-tests/src/test/java/org/xacml4j/v20" / (name + ".java")).read_text()
    by_group = {}
    for method in re.findall(r"public void \w+\(\) throws Exception\s*\{(.*?)\n\t\}", source, re.DOTALL):
        group = re.search(r'executeXacmlConformanceTestCase\(skipTests, "([^"]+)"', method)
        if group:
            by_group[group.group(1)] = [group.group(1) + number.zfill(3) for number in re.findall(r"skipTests.add\((\d+)\)", method)]
    exclusions[name] = by_group
    assert sum(len(values) for values in by_group.values()) == 30
assert exclusions == json.loads((ev / "53-existing-case-exclusions.json").read_text())
assert exclusions == json.loads((ev / "36-existing-case-exclusions.json").read_text())
(ev / "63-existing-case-exclusions.json").write_text(json.dumps(exclusions, indent=2) + "\n")

old_scope = (ev / "57-before-round3-scope-check.txt").read_text()
inventory = old_scope.split("Full continued lane project-file inventory (26):\n",1)[1].split("\n\n",1)[0].splitlines()
inventory += [name for name in files if name not in inventory]
assert len(inventory) == len(set(inventory)) == 32
scope = [f"Round 3 protected files compared: {len(protected)}; changed: 0",
         "All 2906 vendored conformance resources and 14 run 003 evidence files unchanged this round",
         "Prior arithmetic/selector/XML repairs, POMs, fixtures, harness and other providers unchanged this round",
         "Existing edited source license headers checked: 7; unchanged; new test headers: 2; match template",
         "Three fixed-arity 1.0-only higher-order methods byte-identical to round 3 snapshot",
         "Conformance suite sources, loop bounds, exclusions (30 per suite) and case execution order unchanged",
         "Only nine authorized project files changed/added in round 3:", *files,
         "Full continued lane project-file inventory (32):", *inventory]
checks = [["rtk", "git", "diff", "--check", "--", *existing],
          ["rtk", "git", "branch", "--show-current"], ["rtk", "git", "rev-parse", "HEAD"],
          ["rtk", "git", "status", "--short"], ["rtk", "git", "diff", "--cached", "--name-only"]]
for command in checks:
    result = subprocess.run(command, capture_output=True, text=True)
    output = (result.stdout + result.stderr).replace(str(repo), "<repo>")
    assert result.returncode == 0, (command, output)
    if "--cached" in command:
        assert not output.strip()
    if "--show-current" in command:
        assert output.strip() == "integration/modernize-core-jdk28-valhalla"
    if "rev-parse" in command:
        assert output.strip() == "befdd116bd11385ac8d007732fa8e4d347af3143"
    scope += ["", " ".join(command), "Exit code: " + str(result.returncode), output.rstrip()]
scope += ["", "Global git diff --check still reports the pre-existing whitespace in ExpressionTypeBuilder.java:96",
          "That other-lane file matches the snapshot. Scoped round 3 diff --check passes."]
(ev / "scope-check.txt").write_text("\n".join(scope) + "\n")

sections = []
for name in ("58-round3-before.txt", "59-round3-red.txt", "61-round3-packing-red.txt", "62-round3-focused.txt", gate_name):
    sections += ["", "Run: " + name]
    module, in_results = "unknown", False
    for line in (ev / name).read_text().splitlines():
        match = re.search(r"--- .* @ ([^ ]+) ---", line)
        if match:
            module = match.group(1)
        count = re.search(r"Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)", line)
        if "[INFO] Results:" in line:
            in_results = True
            sections += ["Module: " + module, line]
        elif in_results:
            sections.append(line)
            if count:
                in_results = False
        elif count and " - in " not in line:
            sections += ["Module: " + module, line]
        elif "Reactor Summary" in line or "BUILD SUCCESS" in line or "BUILD FAILURE" in line or re.search(r"\.\.\..*(SUCCESS|FAILURE|SKIPPED)", line):
            sections.append(line)
    if name.startswith("58-"):
        sections.append("Test compile only: nine bag-construction API diagnostics; zero tests ran; not the behavior-red receipt.")
(ev / "surefire-summary.txt").write_text((ev / "57-before-round3-surefire-summary.txt").read_text().rstrip()
                                       + "\n\n# Round 3 review fixes\n" + "\n".join(sections) + "\n")
commands = (ev / "57-before-round3-command.txt").read_text().rstrip() + "\n\n# Round 3 review fixes (after 1.3g): exact build/test/evidence commands\n"
commands += (ev / "round3-commands.txt").read_text().rstrip() + "\n"
commands += "\n".join(" ".join(command) for command in checks) + "\n"
commands += "rtk python3 " + relative + "/65-audit-round3.py\n"
(ev / "command.txt").write_text(commands.replace(str(repo), "<repo>"))

summary = (ev / "57-before-round3-summary.md").read_text()
summary = summary.replace("812 Surefire tests", "914 Surefire tests", 1)
summary = summary.replace("Both MEDIUM review findings are fixed\nwith red/green tests.", "Round 3's HIGH and MEDIUM findings and the prior round 2 fixes pass\nwith recorded red/green tests.", 1)
summary = summary.replace("Additional IIIE301–303 checks outside the\nunchanged suite bounds fail identically before/after; proposals are recorded.",
    "Prior round 2 direct IIIE301–303 checks outside the unchanged bounds failed\nidentically before/after; proposals remain open. They were not rerun in round 3.", 1)
summary = summary.replace("Raw version line: 38-jdk-version.txt.", "Fresh raw version line: 64-jdk-version.txt.", 1)
summary = summary.replace("Fresh final-gate Surefire JVM properties: 53-jdk-properties.json.", "Fresh final-gate Surefire JVM properties: 63-jdk-properties.json; direct version: 64-jdk-version.txt.", 1)
summary = summary.replace("/53-reactor-verify-review-fixes.txt", "/63-reactor-verify-round3.txt", 1)
summary = summary.replace("| xacml-core | 638 |", "| xacml-core | 740 |", 1)
summary = summary.replace("| Total | 812 |", "| Total | 914 |", 1)
summary = summary.replace("These are fresh run 53 Maven Results, retained in surefire-summary.txt and\n53-module-counts.json.", "These are fresh run 63 Maven Results, retained in surefire-summary.txt and\n63-module-counts.json.", 1)
review = (ev / "round3-review-fixes.md").read_text()
summary = summary.replace("## Review fixes\n", review + "\n\n## Review fixes (round 2, retained)\n", 1)
summary = summary.replace("Production method bodies and type/parameter annotations match the captured\npre-write source exactly.", "During the prior identifier-only continuation, method bodies and type/parameter\nannotations matched the captured pre-write source. Round 3's authorized higher-order\nsemantic and parameter adaptations are recorded above; BagFunctions/SetFunctions\nbodies remain unchanged.", 1)
summary = summary.replace("53-conformance-testcases.json records every method", "63-conformance-testcases.json records every method", 1)
summary = summary.replace("53-existing-case-exclusions.json. Suite and fixture hashes are unchanged.", "63-existing-case-exclusions.json, compared to runs 36/53. Suite and fixture hashes are unchanged.", 1)
summary = summary.replace("The continued task 1.3 lane changed **26 project files** plus run 005 evidence.\nThis review changes six: DefaultEvaluationContextHandler, its existing test,\nISO8601DateTime, DateTimeDurationUtils, and two new core tests. Accepted XML\nrepairs, identifier aliases, the approved POM and original run 003 are preserved.",
    "The continued task 1.3 lane changed **32 project files** plus run 005 evidence.\nRound 3 changes nine core source/test files, listed in round3-review-fixes.md and\nscope-check.txt. The prior 26-file inventory below remains part of this lane;\nthe four parameter/invocation source files and two new tests add six files.\nAccepted XML repairs, the approved POM and original run 003 are preserved.", 1)
summary = summary.replace("Four standard 3.0 higher-order names; retain four 1.0 aliases; annotations only.",
    "Four standard 3.0 names and retained 1.0 aliases; round 3 adds variadic semantics and validation.", 1)
summary = summary.replace("36 parameterized standard/legacy registry checks; red before repairs.",
    "36 registry checks plus 36 evaluation rows comparing standard/legacy results with both runtime modes.", 1)
summary = summary.replace("Eight standard/legacy and three unchanged standard registry checks; red before repairs.",
    "11 registry checks plus 11 evaluation rows comparing standard/legacy results with both runtime modes.", 1)
summary = summary.replace("all six final run 53 Maven Results", "all six final run 63 Maven Results", 1)
old_scope_paragraph = "scope-check.txt verifies 3,434 protected hashes, including all 2,906 vendored\nconformance resources; zero changed. All 15 existing edited Java license\nheaders and 10 new test headers match. Only the six review files changed/appeared\nsince the pre-write snapshot. The full continued lane inventory is 26 files."
summary = summary.replace(old_scope_paragraph,
    "scope-check.txt verifies 3,535 protected hashes in round 3, including all 2,906\nvendored conformance resources; zero changed. Seven edited source headers remain\nunchanged; two new headers match the template. Exactly nine authorized core files\nchanged/appeared. The full continued lane inventory is 32 files; the prior scope\nproof is archived as 57-before-round3-scope-check.txt.", 1)
summary += "\nRound 3 scope proof: 3535 protected files unchanged; exactly nine authorized core\nsource/test files changed, bringing the continued lane inventory to 32 project files.\nSeven existing license headers are unchanged; two new headers match the template.\n64-round3-fixes.patch is the delta from the round 3 snapshot. No POM, fixture,\nexpected-result, other-provider, XML or tracking edits occurred in round 3.\nThe global diff check reports pre-existing ExpressionTypeBuilder.java:96 whitespace;\nthat file is unchanged and the scoped check passes.\n"
(ev / "summary.md").write_text(summary)
blockers = (ev / "57-before-round3-conformance-blockers.md").read_text()
blockers = blockers.replace("Final run 53: BUILD SUCCESS on JDK 11; 812 tests", "Final run 63: BUILD SUCCESS on JDK 11; 914 tests", 1)
blockers = blockers.replace("53-conformance-testcases.json", "63-conformance-testcases.json", 1)
blockers = blockers.replace("53-existing-case-exclusions.json equals runs 36/46", "63-existing-case-exclusions.json equals runs 36/46/53", 1)
blockers = blockers.replace("## Review fixes\n", "## Round 3 review fixes\n\nHIGH variadic higher-order binding (policy4j-the.4.8): RESOLVED; run 59 red,\nrun 61 mixed-array packing red, runs 62/63 green. MEDIUM identifier lookup-only\ncoverage: RESOLVED; 36 duration and 11 higher-order evaluation rows, each\ncomparing the canonical and retained alias with checks off/on. Full mapping:\nround3-review-fixes.md. No new configured conformance failures were observed.\nThe record-only time-in-range URN and duplicate-id merge issues remain unchanged.\nAdditional IIIE301-303 diagnostics below are retained round 2 observations;\nthey were not rerun or added to the configured round 3 gate.\n\n## Review fixes (round 2, retained)\n", 1)
blockers = blockers.replace("All earlier blockers remain resolved in run 53.", "All earlier configured blockers remain resolved in run 63.", 1)
blockers = blockers.replace("Identifier bodies and aliases, accepted XML\nrepairs", "Duration provider bodies/aliases, accepted XML\nrepairs", 1)
(ev / "conformance-blockers.md").write_text(blockers)
assert str(repo) not in (ev / "summary.md").read_text() + (ev / "command.txt").read_text() + (ev / "surefire-summary.txt").read_text()
print("PASS: 914/0/0/9; conformance 347 + 347 + 17 + 2 passed; exclusions unchanged")
print(f"PASS: {len(protected)} protected files; nine authorized changes; headers and fixed 1.0 methods unchanged")
print("Updated summary.md, surefire-summary.txt, command.txt, conformance-blockers.md and scope-check.txt")
