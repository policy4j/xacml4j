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
gate_name = "53-reactor-verify-review-fixes.txt"
gate = (ev / gate_name).read_text()
assert "BUILD SUCCESS" in gate and "BUILD FAILURE" not in gate

def normalize(value):
    return re.sub(r"\x1b\[[0-9;]*m", "", value.replace(str(repo), "<repo>"))

protected = json.loads((ev / "48-protected-before-review.json").read_text())
changed = [name for name, digest in protected.items()
           if not (repo / name).is_file()
           or hashlib.sha256((repo / name).read_bytes()).hexdigest() != digest]
assert not changed, changed
before = json.loads((ev / "48-review-source-before.json").read_text())
new_tests = ["xacml-core/src/test/java/org/xacml4j/v30/types/ISO8601FractionalDateTimeArithmeticTest.java",
             "xacml-core/src/test/java/org/xacml4j/v30/types/ISO8601DurationReviewTest.java"]
review_files = list(before) + new_tests
all_current_java = set()
for pattern in ("xacml-core/src/**/*.java", "xacml-pdp/src/**/*.java", "xacml-test/src/**/*.java",
                "xacml-xml/src/**/*.java", "xacml-conformance-tests/src/test/java/**/*.java"):
    all_current_java.update(str(path.relative_to(repo)) for path in repo.glob(pattern))
assert all_current_java - set(protected) - set(before) == set(new_tests)

patch = []
for name in review_files:
    original = before.get(name, "")
    current = (repo / name).read_text()
    assert original != current, name
    patch.extend(difflib.unified_diff(original.splitlines(keepends=True), current.splitlines(keepends=True),
                 fromfile="a/" + name if name in before else "/dev/null", tofile="b/" + name))
(ev / "53-review-fixes.patch").write_text("".join(patch))
(ev / "53-review-file-hashes.json").write_text(json.dumps({name: hashlib.sha256((repo / name).read_bytes()).hexdigest()
                                                         for name in review_files}, indent=2) + "\n")

counts = {}
module = "unknown"
for line in gate.splitlines():
    match = re.search(r"--- .* @ ([^ ]+) ---", line)
    if match:
        module = match.group(1)
    count = re.search(r"Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)", line)
    if count and " - in " not in line:
        counts[module] = [int(value) for value in count.groups()]
assert counts == {"xacml-core": [638,0,0,4], "xacml-xml": [34,0,0,0], "xacml-json": [5,0,0,1],
                  "xacml-pdp": [74,0,0,4], "xacml-test": [20,0,0,0], "xacml-conformance-tests": [41,0,0,0]}
assert [sum(row[index] for row in counts.values()) for index in range(4)] == [812,0,0,9]
all_counts = {"xacml": [0,0,0,0], "xacml-core": counts["xacml-core"], "xacml-xml-jaxb": [0,0,0,0],
              **{name: row for name, row in counts.items() if name != "xacml-core"}}
(ev / "53-module-counts.json").write_text(json.dumps(all_counts, indent=2) + "\n")

report_counts = {}
jdk_by_module = {}
for module, expected in counts.items():
    accumulated = [0,0,0,0]
    reports = {}
    for path in (repo / module / "target/surefire-reports").glob("TEST-*.xml"):
        root = ET.parse(path).getroot()
        row = [int(root.get(key)) for key in ("tests", "failures", "errors", "skipped")]
        reports[root.get("name")] = row
        accumulated = [a + b for a, b in zip(accumulated, row)]
        properties = {item.get("name"): item.get("value") for item in root.find("properties")}
        jdk = {key: properties[key] for key in ("java.version", "java.runtime.version", "java.vm.name", "java.vendor")}
        assert jdk["java.version"] == "11.0.32.1", (module, path.name, jdk)
        jdk_by_module[module] = jdk
    assert accumulated == expected, (module, accumulated, expected)
    report_counts[module] = reports
(ev / "53-surefire-report-counts.json").write_text(json.dumps(report_counts, indent=2) + "\n")
(ev / "53-jdk-properties.json").write_text(json.dumps(jdk_by_module, indent=2) + "\n")

selected = {
    "org.xacml4j.v30.types.ISO8601FractionalDateTimeArithmeticTest": ("xacml-core", 30),
    "org.xacml4j.v30.types.ISO8601DurationReviewTest": ("xacml-core", 5),
    "org.xacml4j.v30.types.ISO8601DateTimeArithmeticTest": ("xacml-core", 12),
    "org.xacml4j.v30.policy.function.impl.DateTimeArithmeticBoundaryTest": ("xacml-core", 50),
    "org.xacml4j.v30.pdp.DefaultEvaluationContextHandlerTest": ("xacml-pdp", 18),
    "org.xacml4j.v30.pdp.request.MultipleResourcesViaXacmlXPathHandlerTest": ("xacml-pdp", 6),
}
green = {}
for name, (module, expected) in selected.items():
    root = ET.parse(repo / module / "target/surefire-reports" / ("TEST-" + name + ".xml")).getroot()
    row = [int(root.get(key)) for key in ("tests", "failures", "errors", "skipped")]
    assert row == [expected,0,0,0]
    green[name] = {"counts": row, "tests": [{"test": test.get("name"), "result": "PASS"}
                                          for test in root.findall("testcase")]}
    root.remove(root.find("properties"))
    for element in root.iter():
        if element.text:
            element.text = normalize(element.text)
        if element.tail:
            element.tail = normalize(element.tail)
        for key, value in element.attrib.items():
            element.set(key, normalize(value))
    (ev / ("53-green-" + name.rsplit(".",1)[1] + ".xml")).write_text(ET.tostring(root, encoding="unicode") + "\n")
(ev / "53-review-green-tests.json").write_text(json.dumps(green, indent=2) + "\n")
red = json.loads((ev / "49-review-red-tests.json").read_text())
assert red["org.xacml4j.v30.types.ISO8601FractionalDateTimeArithmeticTest"]["counts"] == [30,30,0,0]
assert red["org.xacml4j.v30.types.ISO8601DurationReviewTest"]["counts"] == [5,5,0,0]
assert red["org.xacml4j.v30.pdp.DefaultEvaluationContextHandlerTest"]["counts"] == [18,1,0,0]

cases = json.loads((ev / "53-conformance-testcases.json").read_text())
previous_cases = json.loads((ev / "46-conformance-testcases.json").read_text())
for name, suite in cases.items():
    assert all(case["failure"] is None for case in suite["testcases"])
    assert suite["counts"]["failures"] == "0" and suite["counts"]["errors"] == "0"
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
assert exclusions == json.loads((ev / "46-existing-case-exclusions.json").read_text())
assert exclusions == json.loads((ev / "36-existing-case-exclusions.json").read_text())
(ev / "53-existing-case-exclusions.json").write_text(json.dumps(exclusions, indent=2) + "\n")
diagnostics = {}
for run in ("50-IIIE3xx-before.txt", "54-IIIE3xx-after.txt"):
    lines = normalize((ev / run).read_text()).splitlines()
    failed = [line for line in lines if re.match(r"CASE IIIE\d+ FAIL", line)]
    assert len(failed) == 3 and "CASES executed=3; failed=3; passed=0" in lines
    diagnostics[run] = failed
assert list(diagnostics.values())[0] == list(diagnostics.values())[1]
(ev / "54-IIIE3xx-results.json").write_text(json.dumps(diagnostics, indent=2) + "\n")

old_scope = (ev / "46-before-review-scope-check.txt").read_text()
inventory = old_scope.split("Full continued lane project-file inventory (23):\n",1)[1].split("\n\n",1)[0].splitlines()
assert len(inventory) == 23
inventory += ["xacml-core/src/main/java/org/xacml4j/util/DateTimeDurationUtils.java", *new_tests]
assert len(set(inventory)) == 26
templates = {
    "ExpectedAttributeResolverBuilderTest.java": "xacml-test/src/main/java/org/xacml4j/v30/ExpectedAttributeResolverBuilder.java",
    "Xacml30TestUtilityTest.java": "xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java",
    "Xacml20ConformanceAttributeResolverTest.java": "xacml-conformance-tests/src/test/java/org/xacml4j/v20/Xacml20ConformanceAttributeResolver.java",
    "Xacml30RequestContentTest.java": "xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30RequestContextUnmarshaller.java",
    "DateTimeArithmeticBoundaryTest.java": "xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/DateTimeValueArithmeticFunctionTest.java",
    "DurationFunctionIdentifierTest.java": "xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/BagFunctionsTest.java",
    "HigherOrderFunctionIdentifierTest.java": "xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/BagFunctionsTest.java",
}
header = re.compile(r"/\*.*?#L%.*?\*/", re.DOTALL)
existing, new = [], []
for name in inventory:
    if not name.endswith(".java"):
        continue
    result = subprocess.run(["rtk", "proxy", "git", "show", "HEAD:" + name], capture_output=True, text=True)
    current = (repo / name).read_text()
    if result.returncode == 0:
        assert header.search(result.stdout).group() == header.search(current).group(), name
        existing.append(name)
    else:
        assert "does not exist" in result.stderr or "exists on disk, but not in" in result.stderr, result.stderr
        template = templates.get(Path(name).name, "xacml-core/src/test/java/org/xacml4j/v30/types/ISO8601DateTimeTest.java")
        assert header.search(current).group() == header.search((repo / template).read_text()).group(), name
        assert all(line == line.rstrip() for line in current[header.search(current).end():].splitlines()), name
        new.append(name)
assert len(existing) == 15 and len(new) == 10

scope = [f"Protected files compared: {len(protected)}", "Protected files changed: 0",
         "Protected vendored conformance resources: 2906; unchanged: True",
         "Protected run 003 evidence files: 14; unchanged: True",
         "Previous identifier annotation repairs, XML mapper and approved POM hashes unchanged: True",
         "XmlContent.java, XPathProvider and ISO8601YearMonthDuration.java hashes unchanged: True",
         "Only six authorized project files changed/added during review follow-up:", *review_files,
         f"Existing edited Java license headers checked: {len(existing)}; unchanged: True",
         f"New Java headers matched to templates: {len(new)}; all match: True",
         "Conformance exclusion sets unchanged: True (30 per OASIS suite)",
         "Conformance suite sources/group bounds and all protected fixtures unchanged: True",
         "Full continued lane project-file inventory (26):", *inventory]
checks = [["rtk", "git", "diff", "--check", "--", *existing, "xacml-test/pom.xml"],
          *[["rtk", "git", "apply", "--reverse", "--check", relative + "/" + name] for name in (
              "required-duration-bag-alias-fix.patch", "required-pom-fix.patch", "required-xml-content-fix.patch", "required-date-period-fix.patch")],
          ["rtk", "git", "diff", "--quiet", "a8a90d46", "--", "xacml-conformance-tests/src/test/java/org/xacml4j/v20/Xacml30ConformanceTest.java",
           "xacml-conformance-tests/src/test/java/org/xacml4j/v20/Xacml20ConformanceTest.java",
           *["xacml-conformance-tests/src/test/resources/xacml3.0-ct-v.0.4/" + case + suffix + ".xml"
             for case in ("IIIE301", "IIIE302", "IIIE303") for suffix in ("Policy", "Request", "Response")]],
          ["rtk", "git", "branch", "--show-current"], ["rtk", "git", "rev-parse", "HEAD"],
          ["rtk", "git", "status", "--short"], ["rtk", "git", "diff", "--cached", "--name-only"]]
for command in checks:
    result = subprocess.run(command, capture_output=True, text=True)
    output = normalize(result.stdout + result.stderr)
    assert result.returncode == 0, (command, output)
    if "--cached" in command:
        assert not output.strip()
    if "--show-current" in command:
        assert output.strip() == "integration/modernize-core-jdk28-valhalla"
    if "rev-parse" in command:
        assert output.strip() == "befdd116bd11385ac8d007732fa8e4d347af3143"
    scope += ["", " ".join(command), "Exit code: " + str(result.returncode), output.rstrip()]
(ev / "scope-check.txt").write_text("\n".join(scope) + "\n")

summary_lines = []
for name in ("48-review-arithmetic-before.txt", "49-review-context-before.txt", "51-review-arithmetic-after.txt",
             "52-review-context-after.txt", gate_name):
    summary_lines += ["", "Run: " + name]
    module, in_results = "unknown", False
    for line in (ev / name).read_text().splitlines():
        match = re.search(r"--- .* @ ([^ ]+) ---", line)
        if match:
            module = match.group(1)
        count = re.search(r"Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)", line)
        if "[INFO] Results:" in line:
            in_results = True
            summary_lines += ["Module: " + module, line]
        elif in_results:
            summary_lines.append(line)
            if count:
                in_results = False
        elif count and " - in " not in line:
            summary_lines += ["Module: " + module, line]
        elif "Reactor Summary" in line or "BUILD SUCCESS" in line or "BUILD FAILURE" in line or re.search(r"\.\.\..*(SUCCESS|FAILURE|SKIPPED)", line):
            summary_lines.append(line)
summary_lines += ["", "Additional direct checks (outside unchanged OASIS loops):",
                  "Runs 50 and 54: IIIE301, IIIE302, IIIE303; executed=3, failed=3, passed=0 in each run."]
(ev / "surefire-summary.txt").write_text(normalize((ev / "46-before-review-surefire-summary.txt").read_text().rstrip()
                                                 + "\n" + "\n".join(summary_lines)) + "\n")
commands = (ev / "46-before-review-command.txt").read_text().rstrip() + "\n\n# Review round 2 fixes: tests first (runs 48 onward)\n"
commands += (ev / "review-commands.txt").read_text().rstrip() + "\n\n# Final evidence and scope checks\n"
commands += "rtk python3 " + relative + "/finalize-review-fixes.py\n" + "\n".join(" ".join(command) for command in checks) + "\n"
(ev / "command.txt").write_text(normalize(commands))

summary = (ev / "46-before-review-summary.md").read_text()
old_lead = summary[summary.index("Result:"):summary.index("JDK:")]
summary = summary.replace(old_lead, "Result: **PASS for the configured full reactor** on JDK 11: **812 Surefire tests,\n0 failures, 0 errors, 9 existing skips**. Both MEDIUM review findings are fixed\nwith red/green tests. OASIS 2.0 and 3.0 each pass 347 case executions; all 17\nRSA cases and both examples pass. Additional IIIE301–303 checks outside the\nunchanged suite bounds fail identically before/after; proposals are recorded.\n\n",1)
for original, replacement in (("46-jdk-properties.json", "53-jdk-properties.json"),
    ("46-reactor-verify-higher-order-identifiers.txt", gate_name),
    ("| xacml-core | 603 |", "| xacml-core | 638 |"), ("| xacml-pdp | 71 |", "| xacml-pdp | 74 |"),
    ("| Total | 774 |", "| Total | 812 |"), ("fresh run 46 Maven Results", "fresh run 53 Maven Results"),
    ("46-module-counts.json", "53-module-counts.json"), ("46-conformance-testcases.json", "53-conformance-testcases.json"),
    ("46-existing-case-exclusions.json", "53-existing-case-exclusions.json")):
    assert original in summary, original
    summary = summary.replace(original, replacement)
review_section = (ev / "review-fixes.md").read_text().split("\n",1)[1].split("## Every arithmetic operation checked",1)[0]
summary = summary.replace("## Approved identifier repairs, tests first", "## Review fixes\n" + review_section
    + "Full operation inventory and retained review gaps: review-fixes.md.\nAdditional case proposals: review-proposals.md.\n\n## Approved identifier repairs, tests first",1)
old = "No adaptation regression or new semantic failure is left by the final gate.\nconformance-blockers.md marks every recorded blocker resolved; no proposal\nneeds further authorization to obtain this configured green baseline."
assert old in summary
summary = summary.replace(old, "No adaptation regression is shown by the configured gate. Additional IIIE301,\nIIIE302 and IIIE303 checks each fail before/after the review edits; they are\nclassified as fixture compatibility, pre-existing harness ordering, and a\npre-existing JAXB IDREF defect. They were not part of prior green-suite coverage.\nconformance-blockers.md and review-proposals.md retain these pending proposals.",1)
old = "The continued task 1.3 lane changed **23 project files** plus this run 005\ndirectory. Five project files changed/appeared in the latest identifier\ncontinuation; all earlier repairs and accepted xacml-xml review files stayed\nunchanged. The original xacml-xml adaptation evidence in run 003 is preserved."
assert old in summary
summary = summary.replace(old, "The continued task 1.3 lane changed **26 project files** plus run 005 evidence.\nThis review changes six: DefaultEvaluationContextHandler, its existing test,\nISO8601DateTime, DateTimeDurationUtils, and two new core tests. Accepted XML\nrepairs, identifier aliases, the approved POM and original run 003 are preserved.",1)
summary = summary.replace("Signed combined Period for year/month add/subtract; prior approved day-time subtraction sign condition retained.",
                          "Signed combined Period for year/month; signed Duration with nanos for day-time add/subtract.",1)
summary = summary.replace("Entity API, designator overload, Optional contract; content-only selectors without an explicit/generated context ID.",
                          "Entity API, designator overload, Optional contract; every no-ContextSelectorId path uses the Content document.",1)
summary = summary.replace("Four selector, issuer/duplicate/Entity and PIP/cache tests.",
                          "Selector, issuer/duplicate/Entity and PIP/cache tests; review document/explicit context and two retained cardinality-gap checks.",1)
anchor = "The xacml-test POM change is the exact conductor-approved"
position = summary.index(anchor)
summary = summary[:position].rstrip() + "\n| `xacml-core/src/main/java/org/xacml4j/util/DateTimeDurationUtils.java` | Decimal day-time parsing/conversion; normalize P14M once. |\n| `xacml-core/src/test/java/org/xacml4j/v30/types/ISO8601FractionalDateTimeArithmeticTest.java` | Thirty signed/fractional add/subtract checks through production functions. |\n| `xacml-core/src/test/java/org/xacml4j/v30/types/ISO8601DurationReviewTest.java` | Five decimal round-trip/equality/conversion and P14M checks. |\n\n" + summary[position:]
summary = summary.replace("The previous day-time subtraction sign fix remains unchanged.",
                          "The prior signed subtraction behavior is retained and repeated by the review tests.",1)
start = summary.index("## Evidence, scope and remaining risk")
end = summary.index("# Compiler errors and fixes", start)
summary = summary[:start] + """## Evidence, scope and remaining risk

command.txt retains exact commands; surefire-summary.txt includes the original
history, focused red/green Results, and all six final run 53 Maven Results.
Paths are normalized to <repo>. 46-before-review-* archives preserve the prior
green state. Original run 003's fourteen files match their protected hashes.

scope-check.txt verifies 3,434 protected hashes, including all 2,906 vendored
conformance resources; zero changed. All 15 existing edited Java license
headers and 10 new test headers match. Only the six review files changed/appeared
since the pre-write snapshot. The full continued lane inventory is 26 files.
No assertion was removed or weakened. The one relative-path expectation was
changed as explicitly requested; all fixture/expected-response bytes and
OASIS exclusions/bounds are unchanged. Git has no staged changes; other-lane
fixture restorations, XML work, evidence and the prior JSON deletion remain.

Residual risk: IIIE301–303 direct checks need the three concrete proposals in
review-proposals.md; they fail identically before/after. Zero/two-node context
selector syntax-error behavior is unsupported and recorded without changing
XmlContent. Other fractional truncation persists in the integer getSeconds /
isZero path and Java-to-XML duration factory; 55-fractional-duration-gaps.txt
records runtime examples. Sub-nanosecond arithmetic is rejected rather than
rounded. Advice comparison is a pre-existing harness gap; mixed Content that
starts with a comment/processing instruction is UNVERIFIED. Inherited coverage
limits and tasks 4.5/4.6 function semantic questions remain. Detailed review
tests, all checked operations and notes are in review-fixes.md.
No commit, br command, task tick, issue closure or evidence-index edit was made.

""" + summary[end:]
(ev / "summary.md").write_text(normalize(summary))

blockers = (ev / "46-before-review-conformance-blockers.md").read_text()
end = blockers.index("The exact JUnit compile-scope POM patch")
historical_table = blockers[blockers.index("| Case / earlier blocker"):end]
blockers = """# Conformance blocker status: configured gate GREEN; additional IIIE3xx proposals OPEN

Final run 53: BUILD SUCCESS on JDK 11; 812 tests / 0 failures / 0 errors /
9 existing skips. Configured OASIS 2.0 347 pass, OASIS 3.0 347 pass, RSA 17
pass, examples 2 pass. Case evidence: 53-conformance-testcases.json. The
30 explicit exclusions per OASIS suite and all loop bounds are unchanged;
53-existing-case-exclusions.json equals runs 36/46. Fixtures are unchanged.

## Review fixes

Both MEDIUM findings: RESOLVED with tests first (runs 48/49 red, 51/52 green,
53 full gate green). The conditional P14M double-count defect is also fixed
after its red test showed 26 instead of 14 months. All six standard arithmetic
operations, signed fractions, round trips/equality/ordering, existing multiple
XPath expansion and the no-ID/explicit-ID selector cases pass. Mapping:
review-fixes.md; red/green test receipts: 49-review-red-tests.json and
53-review-green-tests.json.

## Additional targeted coverage, pending proposals

Runs 50 and 54: IIIE301–303 executed=3, failed=3, passed=0 in each. These
cases are outside the configured loops; the direct diagnostic invokes the
unchanged production comparator and exits nonzero on failures.

| Case | Cause / classification | Status |
| --- | --- | --- |
| IIIE301 | Draft fixture selector URN omits profile; the canonical handler ID matches OASIS CS02. Fixture compatibility, unchanged from a8a90d46. | OPEN proposal: decide draft attribute alias support; no fixture changes. |
| IIIE302 | Pre-existing harness compares Attributes categories by position; decisions match but category order differs. | OPEN proposal: preserve every assertion while matching categories by category/id and multiplicity. Later assertions UNVERIFIED. |
| IIIE303 | Pre-existing XML request IDREF cast assumes String, but JAXB supplies AttributesType. | OPEN proposal: unwrap IDREF target's id with a failing request-unmarshaller test first. |

All three observed failures are identical before/after; no adaptation
regression is shown. 55-review-baseline-snippets.json confirms the relevant
base source. review-proposals.md gives each file:line, exact observed failure,
proposed repair and test. These are outside current semantic/Content scope
and were not applied.

## Retained gaps and record-only notes

Zero-node context selection falls back to the document and two-node selection
uses the first node, instead of Indeterminate syntax-error. The requested
characterization tests pass before/after; XmlContent stays unchanged. Integer
getSeconds/isZero and Java-to-XML duration construction still truncate;
55-fractional-duration-gaps.txt records them. AssociatedAdvice comparison is
missing (pre-existing), and comment/PI-first mixed Content is UNVERIFIED.
No fixtures, expected results, other POMs, tracking or commits were changed.

## Earlier configured-suite blockers

""" + historical_table + "All earlier blockers remain resolved in run 53. Prior approval records and\nred/green receipts are retained. Identifier bodies and aliases, accepted XML\nrepairs and the exact conductor-approved JUnit compile-scope patch are unchanged.\n"
(ev / "conformance-blockers.md").write_text(blockers)
print("Final gate: 812 tests / 0 failures / 0 errors / 9 skips; BUILD SUCCESS")
print("Configured conformance: 347 + 347 + 17 = 711 passed; plus two examples")
print("Additional IIIE301-303: 3 executed / 3 failed identically before/after; proposals OPEN")
print(f"Scope: {len(protected)} protected hashes unchanged; 15 existing and 10 new Java headers match; six review files")
