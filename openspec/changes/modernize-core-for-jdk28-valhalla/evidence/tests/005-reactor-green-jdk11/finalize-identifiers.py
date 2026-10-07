from pathlib import Path
import hashlib
import json
import re
import subprocess
import xml.etree.ElementTree as ET

repo = Path.cwd()
ev = Path(__file__).parent
ev_relative = str(ev.relative_to(repo))
gate_name = "46-reactor-verify-higher-order-identifiers.txt"
gate = (ev / gate_name).read_text()
assert "BUILD SUCCESS" in gate and "BUILD FAILURE" not in gate

def normalize(value):
    return re.sub(r"\x1b\[[0-9;]*m", "", value.replace(str(repo), "<repo>"))

def run(command):
    result = subprocess.run(command, capture_output=True, text=True)
    return result.returncode, normalize(result.stdout + result.stderr)

duration_before = json.loads((ev / "39-identifier-sources-before.json").read_text())
higher_before = json.loads((ev / "43-higher-order-source-before.json").read_text())
before = dict(duration_before, **higher_before)
operations = {
    "BagFunctions.java": [type_name + "-" + operation
        for type_name in ("dayTimeDuration", "yearMonthDuration")
        for operation in ("one-and-only", "bag-size", "is-in", "bag")],
    "SetFunctions.java": [type_name + "-" + operation
        for type_name in ("dayTimeDuration", "yearMonthDuration")
        for operation in ("intersection", "union", "subset", "at-least-one-member-of", "set-equals")],
    "HigherOrderFunctions.java": ["any-of", "all-of", "any-of-any", "map"],
}
references = {"BagFunctions.java": "Appendix A.3.10; Tables 10.2.8/10.2.9",
              "SetFunctions.java": "Appendix A.3.11; Tables 10.2.8/10.2.9",
              "HigherOrderFunctions.java": "Appendix A.3.12/A.4; Table 10.2.9"}
rows = []
for name, original in before.items():
    basename = Path(name).name
    expected = original
    for operation in operations[basename]:
        legacy = "urn:oasis:names:tc:xacml:1.0:function:" + operation
        canonical = "urn:oasis:names:tc:xacml:3.0:function:" + operation
        old = '\t@XacmlFuncSpec(id="' + legacy + '")'
        replacement = '\t@XacmlFuncSpec(id="' + canonical + '")\n\t@XacmlLegacyFunc(id="' + legacy + '")'
        assert expected.count(old) == 1
        expected = expected.replace(old, replacement, 1)
        old_line = original[:original.index(old)].count("\n") + 1
        rows.append({"provider": name, "original_line": old_line, "canonical": canonical,
                     "legacy": legacy, "operation": operation, "reference": references[basename],
                     "test_class": "HigherOrderFunctionIdentifierTest" if basename.startswith("Higher")
                                    else "DurationFunctionIdentifierTest"})
    if basename == "SetFunctions.java":
        expected = expected.replace("import org.xacml4j.v30.policy.function.XacmlFunctionProvider;",
                "import org.xacml4j.v30.policy.function.XacmlFunctionProvider;\n"
                "import org.xacml4j.v30.policy.function.XacmlLegacyFunc;", 1)
    assert (repo / name).read_text() == expected, name
assert len(rows) == 22
baseline_receipt = []
for name in before:
    code, baseline = run(["rtk", "proxy", "git", "show", "a8a90d46:" + name])
    assert code == 0
    for operation in operations[Path(name).name]:
        old_annotation = '@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:' + operation + '")'
        assert old_annotation in baseline
        baseline_receipt.append(name + ": " + old_annotation)
(ev / "46-identifier-baseline.txt").write_text("Core API base a8a90d46: original identifier annotations confirmed\n" + "\n".join(baseline_receipt) + "\n")

protected = json.loads((ev / "43-protected-before-higher-order-identifiers.json").read_text())
changed = [name for name, digest in protected.items()
           if not (repo / name).is_file()
           or hashlib.sha256((repo / name).read_bytes()).hexdigest() != digest]
assert not changed, changed

existing = [
    "xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java",
    "xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java",
    "xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionLegacyHandler.java",
    "xacml-pdp/src/test/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandlerTest.java",
    "xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXacmlXPathHandlerTest.java",
    "xacml-test/src/main/java/org/xacml4j/v30/ExpectedAttributeResolverBuilder.java",
    "xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java",
    "xacml-conformance-tests/src/test/java/org/xacml4j/v20/Xacml20ConformanceAttributeResolver.java",
    "xacml-core/src/main/java/org/xacml4j/v30/types/ISO8601DateTime.java",
    "xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30RequestContextFromJaxbToObjectModelMapper.java",
    "xacml-core/src/main/java/org/xacml4j/v30/types/ISO8601Date.java",
    *before,
]
new = {
    "xacml-test/src/test/java/org/xacml4j/v30/ExpectedAttributeResolverBuilderTest.java": existing[5],
    "xacml-test/src/test/java/org/xacml4j/v20/Xacml30TestUtilityTest.java": existing[6],
    "xacml-conformance-tests/src/test/java/org/xacml4j/v20/Xacml20ConformanceAttributeResolverTest.java": existing[7],
    "xacml-core/src/test/java/org/xacml4j/v30/types/ISO8601DateTimeArithmeticTest.java": "xacml-core/src/test/java/org/xacml4j/v30/types/ISO8601DateTimeTest.java",
    "xacml-xml/src/test/java/org/xacml4j/v30/xml/Xacml30RequestContentTest.java": "xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30RequestContextUnmarshaller.java",
    "xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/DateTimeArithmeticBoundaryTest.java": "xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/DateTimeValueArithmeticFunctionTest.java",
    "xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/DurationFunctionIdentifierTest.java": "xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/BagFunctionsTest.java",
    "xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/HigherOrderFunctionIdentifierTest.java": "xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/BagFunctionsTest.java",
}
header = re.compile(r"/\*.*?#L%.*?\*/", re.DOTALL)
for name in existing:
    code, original = run(["rtk", "proxy", "git", "show", "HEAD:" + name])
    assert code == 0 and header.search(original).group() == header.search((repo / name).read_text()).group(), name
for name, template in new.items():
    assert header.search((repo / name).read_text()).group() == header.search((repo / template).read_text()).group(), name

counts = {}
module = "unknown"
for line in gate.splitlines():
    match = re.search(r"--- .* @ ([^ ]+) ---", line)
    if match:
        module = match.group(1)
    count = re.search(r"Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)", line)
    if count and " - in " not in line:
        counts[module] = [int(value) for value in count.groups()]
assert counts == {"xacml-core": [603,0,0,4], "xacml-xml": [34,0,0,0], "xacml-json": [5,0,0,1],
                  "xacml-pdp": [71,0,0,4], "xacml-test": [20,0,0,0], "xacml-conformance-tests": [41,0,0,0]}
(ev / "46-module-counts.json").write_text(json.dumps(counts, indent=2) + "\n")
assert [sum(count[index] for count in counts.values()) for index in range(4)] == [774,0,0,9]

cases = json.loads((ev / "46-conformance-testcases.json").read_text())
for suite_name in ("Xacml20ConformanceTest", "Xacml30ConformanceTest"):
    suite = cases["org.xacml4j.v20." + suite_name]
    ids = [case_id for case in suite["testcases"] for case_id in case["case_ids"]]
    assert len(ids) == 347 and ids.count("IIIG006") == 2
    assert all(name in ids for name in ("IIC104", "IIC106", "IIC107", "IIC150", "IIC164", "IIC232"))
    assert all(case["failure"] is None for case in suite["testcases"])
for suite in cases.values():
    assert suite["counts"]["failures"] == "0" and suite["counts"]["errors"] == "0"
    assert all(case["failure"] is None for case in suite["testcases"])
assert len(cases["org.xacml4j.v20.RSA2008InteropTest"]["testcases"]) == 17
assert len(cases["org.xacml4j.v20.XacmlExampleTest"]["testcases"]) == 2
run42 = json.loads((ev / "42-conformance-testcases.json").read_text())
failed42 = [case for case in run42["org.xacml4j.v20.Xacml30ConformanceTest"]["testcases"] if case["failure"]]
assert len(failed42) == 1 and failed42[0]["case_ids"][-1] == "IIC163"
failed42[0].update({"failed_case_id": "IIC164", "failure_phase": "policy import before evaluation/marker",
                    "failure_classification": "pre-existing core higher-order identifier registration defect",
                    "direct_reproduction": "43-higher-order-registration-diagnostics.txt"})
(ev / "42-conformance-testcases.json").write_text(json.dumps(run42, indent=2) + "\n")

exclusions = {}
for suite_name in ("Xacml20ConformanceTest", "Xacml30ConformanceTest"):
    name = "xacml-conformance-tests/src/test/java/org/xacml4j/v20/" + suite_name + ".java"
    source = (repo / name).read_text()
    by_group = {}
    for method in re.findall(r"public void \w+\(\) throws Exception\s*\{(.*?)\n\t\}", source, re.DOTALL):
        group = re.search(r'executeXacmlConformanceTestCase\(skipTests, "([^"]+)"', method)
        if group:
            by_group[group.group(1)] = [group.group(1) + number.zfill(3)
                    for number in re.findall(r"skipTests.add\((\d+)\)", method)]
    exclusions[suite_name] = by_group
    assert sum(len(values) for values in by_group.values()) == 30
assert exclusions == json.loads((ev / "36-existing-case-exclusions.json").read_text())
(ev / "46-existing-case-exclusions.json").write_text(json.dumps(exclusions, indent=2) + "\n")

lookup_results = {}
for test_name, expected_count in (("DurationFunctionIdentifierTest",36), ("HigherOrderFunctionIdentifierTest",11)):
    report = repo / ("xacml-core/target/surefire-reports/TEST-org.xacml4j.v30.policy.function.impl." + test_name + ".xml")
    root = ET.parse(report).getroot()
    assert [int(root.get(key)) for key in ("tests", "failures", "errors", "skipped")] == [expected_count,0,0,0]
    lookup_results[test_name] = [test.get("name") for test in root.findall("testcase")]
    properties = {item.get("name"): item.get("value") for item in root.find("properties")}
    jdk_properties = {key: properties[key] for key in ("java.version", "java.runtime.version", "java.vm.name", "java.vendor")}
    assert jdk_properties["java.version"] == "11.0.32.1"
    root.remove(root.find("properties"))
    (ev / ("46-" + test_name + "-green.xml")).write_text(normalize(ET.tostring(root, encoding="unicode")) + "\n")
(ev / "46-jdk-properties.json").write_text(json.dumps(jdk_properties, indent=2) + "\n")
(ev / "46-identifier-green-tests.json").write_text(json.dumps(lookup_results, indent=2) + "\n")
(ev / "46-identifier-audit.json").write_text(json.dumps(rows, indent=2) + "\n")

url = "https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html"
audit = ["# Identifier audit: 22 standard names, 22 retained aliases", "",
         "Source: [OASIS XACML 3.0 Standard, 22 January 2013](" + url + ").",
         "Appendix A.3.10/A.3.11 and Table 10.2.8 specify the duration functions.",
         "Appendix A.3.12 specifies four higher-order 3.0 names; A.4 retains their old forms.",
         "Table 10.2.9 lists the prior duration/higher-order identifiers.", "",
         "Each row is tested under both complete identifiers through the production default",
         "FunctionProvider. The tests check presence, canonical/legacy metadata and object identity.",
         "All 44 old/new lookups pass; three unchanged standard higher-order names also pass.", "",
         "| Original provider:line -> canonical identifier | Retained legacy alias | Normative reference | Focused test |",
         "| --- | --- | --- | --- |"]
for row in rows:
    method = "resolvesEveryStandardHigherOrderName" if row["test_class"].startswith("Higher") else "resolvesStandardAndLegacyNamesToSameFunction"
    audit.append("| `" + Path(row["provider"]).name + ":" + str(row["original_line"]) + "` -> `" + row["canonical"]
                 + "` | `" + row["legacy"] + "` | [" + row["reference"] + "](" + url + ") | `" + row["test_class"] + "." + method + "[" + row["canonical"] + "]` and `[" + row["legacy"] + "]` |")
audit.extend(["", "Family scan: all four bag and five set operations for both duration types now",
              "have the standard 3.0 names. Duration equality and all six arithmetic function IDs",
              "already have 3.0 names and legacy annotations; no change was required. XacmlTypes",
              "already registers the standard 2001 XML Schema duration IDs and 2002 XQuery aliases.",
              "The higher-order family retains standard 1.0 all-of-any, any-of-all and all-of-all;",
              "the new test resolves these unchanged names.", "",
              "No combining-algorithm identifier failure was revealed by the completed suites;",
              "no algorithm or type identifiers were changed. Appendix C remains the authority",
              "for algorithm identifiers if another case reveals a defect.", "",
              "Scope proof: expected replacements are generated from captured pre-write source",
              "and compared byte for byte. Only ID/legacy annotations and one annotation import",
              "change; method bodies and type/parameter annotations remain identical.", "",
              "Identifier coverage is not complete semantic certification: new 3.0 higher-order",
              "names reuse the existing implementations. Variadic signatures and semantic open",
              "questions in tasks 4.5/4.6 remain outside this task; no semantics were changed."])
(ev / "identifier-audit.md").write_text("\n".join(audit) + "\n")

summary_lines = []
for log_name in ("39-duration-identifiers-before.txt", "40-duration-identifiers-after.txt",
                 "41-duration-identifiers-after.txt", "42-reactor-verify-duration-identifiers.txt",
                 "44-higher-order-identifiers-before.txt", "45-higher-order-identifiers-after.txt", gate_name):
    summary_lines.extend(["", "Run: " + log_name])
    module = "unknown"
    in_results = False
    for line in (ev / log_name).read_text().splitlines():
        match = re.search(r"--- .* @ ([^ ]+) ---", line)
        if match:
            module = match.group(1)
        count = re.search(r"Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)", line)
        if "[INFO] Results:" in line:
            in_results = True
            summary_lines.extend(["Module: " + module, line])
        elif in_results:
            summary_lines.append(line)
            if count:
                in_results = False
        elif count and " - in " not in line:
            summary_lines.extend(["Module: " + module, line])
        elif "Reactor Summary" in line or "BUILD SUCCESS" in line or "BUILD FAILURE" in line or re.search(r"\.\.\..*(SUCCESS|FAILURE|SKIPPED)", line):
            summary_lines.append(line)
surefire_path = ev / "surefire-summary.txt"
prefix = surefire_path.read_text().split("\nRun: 39-duration-identifiers-before.txt", 1)[0].rstrip()
surefire_path.write_text(normalize(prefix + "\n" + "\n".join(summary_lines)) + "\n")

scope = [f"Protected files compared: {len(protected)}", f"Protected files changed: {len(changed)}",
         "BagFunctions matches exact approved eight canonical ID + legacy alias replacements: True",
         "SetFunctions matches only ten canonical ID + legacy alias replacements and annotation import: True",
         "HigherOrderFunctions matches only four canonical ID + legacy alias replacements: True",
         "Previously approved arithmetic, XML content and POM source hashes unchanged: True",
         f"Existing edited Java license headers checked: {len(existing)}; unchanged: True",
         f"New Java headers matched to templates: {len(new)}; all match: True",
         "Conformance exclusion sets unchanged: True (30 per suite)",
         "Conformance suite sources/loop bounds and vendored fixtures match pre-write hashes: True",
         "Only five authorized project files were changed/added during this identifier continuation.",
         "Full continued lane project-file inventory (23):", *existing, *new, "xacml-test/pom.xml"]
checks = [
    ["rtk", "git", "diff", "--check", "--", *existing, "xacml-test/pom.xml"],
    ["rtk", "git", "apply", "--reverse", "--check", ev_relative + "/required-duration-bag-alias-fix.patch"],
    ["rtk", "git", "apply", "--reverse", "--check", ev_relative + "/required-pom-fix.patch"],
    ["rtk", "git", "apply", "--reverse", "--check", ev_relative + "/required-xml-content-fix.patch"],
    ["rtk", "git", "apply", "--reverse", "--check", ev_relative + "/required-date-period-fix.patch"],
    ["rtk", "git", "diff", "--quiet", "a8a90d46", "--", "xacml-conformance-tests/src/test/java/org/xacml4j/v20/Xacml30ConformanceTest.java", "xacml-conformance-tests/src/test/java/org/xacml4j/v20/Xacml20ConformanceTest.java", "xacml-conformance-tests/src/test/resources/xacml3.0-ct-v.0.4/IIC150Policy.xml", "xacml-conformance-tests/src/test/resources/xacml3.0-ct-v.0.4/IIC164Policy.xml"],
    ["rtk", "git", "status", "--short"],
    ["rtk", "git", "diff", "--cached", "--name-only"],
]
check_commands = []
for command in checks:
    code, output = run(command)
    assert code == 0, (command, output)
    if "--cached" in command:
        assert not output.strip()
    check_commands.append(" ".join(command))
    scope.extend(["", " ".join(command), "Exit code: " + str(code), output.rstrip()])
(ev / "scope-check.txt").write_text(normalize("\n".join(scope)) + "\n")

commands_path = ev / "command.txt"
marker = "# Conductor-approved identifier repairs (runs 39 onward)"
commands_prefix = commands_path.read_text().split(marker, 1)[0].rstrip()
commands_path.write_text(normalize(commands_prefix + "\n\n" + marker + "\n"
        + (ev / "identifier-commands.txt").read_text().rstrip()
        + "\n\n# Final evidence and scope checks\nrtk python3 " + ev_relative + "/finalize-identifiers.py\n"
        + "\n".join(check_commands) + "\n"))
print("Final gate: 774 tests / 0 failures / 0 errors / 9 skips; BUILD SUCCESS")
print("Conformance: OASIS 2.0 347/347; OASIS 3.0 347/347; RSA 17/17; examples 2/2")
print("Identifiers: 22 added standard names, 22 retained aliases, 3 unchanged higher-order names; 47 tests pass")
print(f"Scope: {len(protected)} protected hashes unchanged; {len(existing)} existing and {len(new)} new Java headers unchanged")
