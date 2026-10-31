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
snapshot = json.loads((ev / "66-round2-verification-before.json").read_text())
files = snapshot["allowed_existing"]
protected = {name: digest for name, digest in snapshot["hashes"].items() if name not in files}
changed = [name for name, digest in protected.items()
           if not (repo / name).is_file() or hashlib.sha256((repo / name).read_bytes()).hexdigest() != digest]
assert not changed, changed
current = set()
for module in repo.glob("xacml-*"):
    if module.is_dir():
        current.update(str(path.relative_to(repo)) for path in (module / "src").rglob("*") if path.is_file())
assert not current - set(snapshot["hashes"]), current - set(snapshot["hashes"])
assert len(protected) == 3541
header = re.compile(r"/\*.*?#L%.*?\*/", re.DOTALL)
patch = []
for name in files:
    before = (ev / ("66-before-" + Path(name).name + ".txt")).read_text()
    after = (repo / name).read_text()
    assert before != after, name
    assert header.search(before).group() == header.search(after).group(), name
    delta = list(difflib.unified_diff(before.splitlines(keepends=True), after.splitlines(keepends=True),
                                    fromfile="a/" + name, tofile="b/" + name))
    assert all(line[1:].rstrip("\n") == line[1:].rstrip()
               for line in delta if line.startswith("+") and not line.startswith("+++")), name
    patch.extend(delta)
    if "/src/test/" in name:
        # All existing test method bodies must remain byte-identical.
        for match in re.finditer(r"public void (\w+)\([^)]*\)(?: throws Exception)?\s*\{", before):
            start = match.start()
            depth, end = 1, match.end()
            while depth:
                if before[end] == "{":
                    depth += 1
                elif before[end] == "}":
                    depth -= 1
                end += 1
            assert before[start:end] in after, (name, match.group(1))
(ev / "72-round2-followup-fixes.patch").write_text("".join(patch))
(ev / "72-round2-followup-file-hashes.json").write_text(json.dumps(
    {name: hashlib.sha256((repo / name).read_bytes()).hexdigest() for name in files}, indent=2) + "\n")

red = json.loads((ev / "69-round2-verification-red-tests.json").read_text())
assert red["production_matches_before_snapshot"]
def totals(receipt):
    return [sum(result["counts"][index] for result in receipt["results"].values()) for index in range(4)]
assert totals(red) == [117, 1, 18, 0]
for prefix in ("70-round2-verification-green", "71-reactor-verify-round2-followup"):
    assert totals(json.loads((ev / (prefix + "-tests.json")).read_text())) == [117, 0, 0, 0]
setup = json.loads((ev / "67-round2-verification-red-tests.json").read_text())
assert not setup["tests_executed"] and not setup["results"]
diagnostics = json.loads((ev / "compile-diagnostics.json").read_text())
assert len(diagnostics["67-round2-verification-red.txt"]) == 2
for name in ("69-round2-verification-red.txt", "70-round2-verification-green.txt", "71-reactor-verify-round2-followup.txt"):
    assert not diagnostics[name]

gate_name = "71-reactor-verify-round2-followup.txt"
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
assert counts == {"xacml-core": [760,0,0,4], "xacml-xml": [34,0,0,0], "xacml-json": [5,0,0,1],
                  "xacml-pdp": [74,0,0,4], "xacml-test": [20,0,0,0], "xacml-conformance-tests": [41,0,0,0]}
assert [sum(row[index] for row in counts.values()) for index in range(4)] == [934,0,0,9]
all_counts = {"xacml": [0,0,0,0], "xacml-core": counts["xacml-core"], "xacml-xml-jaxb": [0,0,0,0],
              **{name: row for name, row in counts.items() if name != "xacml-core"}}
(ev / "71-module-counts.json").write_text(json.dumps(all_counts, indent=2) + "\n")
jdk_by_module, report_counts = {}, {}
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
(ev / "71-jdk-properties.json").write_text(json.dumps(jdk_by_module, indent=2) + "\n")
(ev / "71-surefire-report-counts.json").write_text(json.dumps(report_counts, indent=2) + "\n")

cases = json.loads((ev / "71-conformance-testcases.json").read_text())
previous_cases = json.loads((ev / "63-conformance-testcases.json").read_text())
assert set(cases) == set(previous_cases)
for name, suite in cases.items():
    assert all(case["failure"] is None for case in suite["testcases"])
    assert [(case["method"], case["case_ids"]) for case in suite["testcases"]] == [
        (case["method"], case["case_ids"]) for case in previous_cases[name]["testcases"]]
exclusions = {}
for name in ("Xacml20ConformanceTest", "Xacml30ConformanceTest"):
    ids = [case_id for case in cases["org.xacml4j.v20." + name]["testcases"] for case_id in case["case_ids"]]
    assert len(ids) == 347 and ids.count("IIIG006") == 2
    assert not any(case_id.startswith("IIIE") for case_id in ids)
    source = (repo / "xacml-conformance-tests/src/test/java/org/xacml4j/v20" / (name + ".java")).read_text()
    by_group = {}
    for method in re.findall(r"public void \w+\(\) throws Exception\s*\{(.*?)\n\t\}", source, re.DOTALL):
        group = re.search(r'executeXacmlConformanceTestCase\(skipTests, "([^"]+)"', method)
        if group:
            by_group[group.group(1)] = [group.group(1) + number.zfill(3) for number in re.findall(r"skipTests.add\((\d+)\)", method)]
    exclusions[name] = by_group
    assert sum(len(values) for values in by_group.values()) == 30
assert len(cases["org.xacml4j.v20.RSA2008InteropTest"]["testcases"]) == 17
assert len(cases["org.xacml4j.v20.XacmlExampleTest"]["testcases"]) == 2
for prefix in ("36", "63"):
    assert exclusions == json.loads((ev / (prefix + "-existing-case-exclusions.json")).read_text())
(ev / "71-existing-case-exclusions.json").write_text(json.dumps(exclusions, indent=2) + "\n")

old_scope = (ev / "66-before-round2-verification-scope-check.txt").read_text()
inventory = old_scope.split("Full continued lane project-file inventory (32):\n",1)[1].split("\n\n",1)[0].splitlines()
assert len(inventory) == len(set(inventory)) == 32 and all(name in inventory for name in files)
scope = [f"Round 2 verification follow-up protected files compared: {len(protected)}; changed: 0",
         "All 2906 vendored conformance resources and 14 run 003 evidence files unchanged this follow-up",
         "Prior arithmetic/selector/XML/higher-order repairs, POMs, fixtures, harness and other providers unchanged",
         "Three edited Java license headers byte-identical; all existing test method bodies preserved",
         "Conformance sources, loop bounds, exclusions (30 per suite) and case execution order unchanged",
         "Only three authorized project files changed this follow-up:", *files,
         "Full continued lane project-file inventory (32):", *inventory]
checks = [["rtk", "git", "diff", "--check", "--", *files],
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
scope += ["", "The prior global diff warning at ExpressionTypeBuilder.java:96 is unchanged other-lane whitespace.",
          "That file matches the follow-up snapshot. The scoped follow-up diff --check passes."]
(ev / "scope-check.txt").write_text("\n".join(scope) + "\n")

sections = []
for name in ("67-round2-verification-red.txt", "69-round2-verification-red.txt", "70-round2-verification-green.txt", gate_name):
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
    if name.startswith("67-"):
        sections.append("Test compile only: two ValueExp-to-ISO8601DateTime assignment errors; zero tests ran; stale reports excluded.")
(ev / "surefire-summary.txt").write_text((ev / "66-before-round2-verification-surefire-summary.txt").read_text().rstrip()
    + "\n\n# Round 2 verification follow-up (after 1.3h)\n" + "\n".join(sections) + "\n")
commands = (ev / "66-before-round2-verification-command.txt").read_text().rstrip()
commands += "\n\n# Round 2 verification follow-up (after 1.3h): exact build/test/evidence commands\n"
commands += (ev / "round2-followup-commands.txt").read_text().rstrip() + "\n"
commands += "\n".join(" ".join(command) for command in checks) + "\n"
commands += "rtk python3 " + relative + "/73-audit-round2-followup.py\n"
(ev / "command.txt").write_text(commands.replace(str(repo), "<repo>"))

review = """## Round 2 verification follow-up

Started after packet 1.3h: round 3 implementation, run 63 full gate and run 65
final audit were complete. Grok's rv007 approves both original MEDIUM fixes.
The conductor's packet 1.3i authorizes these two refinements and their tests.

Decision: truncate XML duration fractional seconds toward zero at nanosecond
precision when converting to java.time.Duration. Apply RoundingMode.DOWN to
the nanos magnitude, then retain the existing duration sign handling. This is
the conductor's java.time precision/compatibility decision, not a claim that
XML Schema requires truncation. The XML duration value retains all nonzero
decimal digits; the precision reduction occurs only at the java.time boundary.

| Finding / original file:line -> fix | Tests written first | Result |
| --- | --- | --- |
| LOW integral lexical scale; DateTimeDurationUtils.java:260-264 -> if the fractional remainder is zero, pass new BigDecimal(seconds.toBigInteger()) to newDuration; no stripTrailingZeros. Final lines 262-264. | ISO8601DurationReviewTest.integralSecondsDiscardZeroFractionalScale covers PT1.0S/PT1S, negative mirrors, 10 and zero; checks lexical form, SECONDS scale, equality/hash/ordering. nonzeroFractionalLexicalFormStaysDecimal retains +/-P0DT0H0M1.5S. | Run 69: expected P0DT0H0M1S, actual P0DT0H0M1.0S. Runs 70/71: PASS. |
| Valid sub-nanosecond duration throws ArithmeticException; DateTimeDurationUtils.java:227 -> setScale(0, RoundingMode.DOWN) before intValueExact; whole seconds and sign handling unchanged. Final lines 228-229. | subNanosecondConversionTruncatesTowardZero tests +/-PT0.0000000001S -> 0ns, +/-PT1.0000000019S -> +/-1s1ns and +/-PT0.9999999999S -> +/-999999999ns, parsed and raw XML conversions. subNanosecondArithmeticEvaluatesThroughApply runs add/subtract on the default provider, both aliases and validation modes. Fractional arithmetic test adds eight signed rows with both directions and whole/.250 bases. | Run 69: 18 ArithmeticException-related errors, including wrapped Apply invocation failure. Runs 70/71: PASS; no runtime exception or Indeterminate. |

Tests were written before production edits. Run 69 is the behavior-red receipt:
117 tests / 1 failure / 18 errors / 0 skips. Its recorded utility hash equals
the pre-test snapshot. Run 70 repeats the same focused command: 117/0/0/0.
Run 71 repeats all 117 in the full reactor: 934/0/0/9, BUILD SUCCESS on JDK 11.
The four added duration-review methods and sixteen arithmetic rows add twenty
tests; the existing 97 checks pass unchanged in red and green. Per-test names
and stripped XML are retained in 69-, 70- and 71-*-tests.json/XML receipts.

Run 67 stopped during new-test compilation: ISO8601DurationReviewTest.java:115:57
and :116:67 assigned Apply.evaluate's ValueExp directly to ISO8601DateTime.
Explicit ISO8601DateTime casts fix both setup diagnostics. No test ran; its
receipt excludes stale Surefire reports. Runs 69/70/71 have zero compiler
diagnostics. compile-diagnostics.json and the raw logs retain every diagnostic.

Operation inventory: DateTimeArithmeticBoundaryTest retains all six standard
functions (50 checks), and ISO8601DateTimeArithmeticTest retains twelve type
checks. ISO8601FractionalDateTimeArithmeticTest now runs 23 add and 23 subtract
checks through DateTimeArithmeticFunctions, including the sixteen new signed
precision rows. subNanosecondArithmeticEvaluatesThroughApply additionally
checks both day-time operations with four signed operands, both 3.0/1.0 aliases
and runtime validation off/on. Fractional round-trip/equality/hash/ordering,
programmatic P14M and all prior month-end boundaries pass again.

Scope: only DateTimeDurationUtils.java and the two existing focused test files
changed. Three license headers and all prior test method bodies are preserved;
3541 protected source/POM/resource/run003 files are unchanged. Delta:
72-round2-followup-fixes.patch. Neither fixtures nor expected responses changed.
Both OASIS suites still execute 347 cases, RSA 17 and examples 2, all passed.
The thirty exclusions per OASIS suite, loop bounds and emitted case order match
run 63. No new failed case ID. Earlier direct IIIE301-303 proposals and other
recorded selector/fraction/advice gaps remain outside this follow-up; those
additional cases were not rerun. No commit, br, task tick or issue closure.
"""
(ev / "round2-followup-review-fixes.md").write_text(review)
review_document = (ev / "66-before-round2-verification-review-fixes.md").read_text()
review_document = review_document.replace(
    "* Sub-nanosecond XML durations are rejected with ArithmeticException rather\n  than rounded; Java dateTime arithmetic has nanosecond precision.",
    "* The sub-nanosecond rejection observed in round 2 is RESOLVED by the\n  conductor-approved truncation toward zero, verified in runs 69/70/71 below.", 1)
assert "Sub-nanosecond XML durations are rejected" not in review_document
(ev / "review-fixes.md").write_text(review_document.rstrip() + "\n\n" + review)

summary = (ev / "66-before-round2-verification-summary.md").read_text()
summary = summary.replace("914 Surefire tests", "934 Surefire tests", 1)
summary = summary.replace("Round 3's HIGH and MEDIUM findings and the prior round 2 fixes pass\nwith recorded red/green tests.",
    "Both round 2 verification follow-up items pass with recorded red/green tests;\nround 3 and earlier fixes pass again.", 1)
summary = summary.replace("They were not rerun in round 3.", "They were not rerun in this follow-up.", 1)
summary = summary.replace("64-jdk-version.txt", "72-jdk-version.txt")
summary = summary.replace("Fresh final-gate Surefire JVM properties: 63-jdk-properties.json", "Fresh final-gate Surefire JVM properties: 71-jdk-properties.json", 1)
summary = summary.replace("/63-reactor-verify-round3.txt", "/71-reactor-verify-round2-followup.txt", 1)
summary = summary.replace("| xacml-core | 740 |", "| xacml-core | 760 |", 1)
summary = summary.replace("| Total | 914 |", "| Total | 934 |", 1)
summary = summary.replace("These are fresh run 63 Maven Results, retained in surefire-summary.txt and\n63-module-counts.json.",
    "These are fresh run 71 Maven Results, retained in surefire-summary.txt and\n71-module-counts.json.", 1)
summary = summary.replace("## Round 3 review fixes\n", review + "\n\n## Round 3 review fixes (retained)\n", 1)
summary = summary.replace("63-conformance-testcases.json records every method", "71-conformance-testcases.json records every method", 1)
summary = summary.replace("63-existing-case-exclusions.json, compared to runs 36/53", "71-existing-case-exclusions.json, compared to runs 36/63", 1)
summary = summary.replace("Round 3 changes nine core source/test files, listed in round3-review-fixes.md and\nscope-check.txt.",
    "This follow-up changes three existing core source/test files, listed above and\nin scope-check.txt. Round 3's nine-file proof is archived as\n66-before-round2-verification-scope-check.txt.", 1)
summary = summary.replace("Decimal day-time parsing/conversion; normalize P14M once.",
    "Decimal parsing/conversion; normalize P14M once; scale-zero integral seconds and nanos truncation.", 1)
summary = summary.replace("Thirty signed/fractional add/subtract checks through production functions.",
    "46 signed/fractional/precision add/subtract checks through production functions.", 1)
summary = summary.replace("Five decimal round-trip/equality/conversion and P14M checks.",
    "Nine lexical/decimal/conversion/P14M and signed precision checks, including Apply in both runtime modes.", 1)
summary = summary.replace("all six final run 63 Maven Results", "all six final run 71 Maven Results", 1)
summary = summary.replace(
    "scope-check.txt verifies 3,535 protected hashes in round 3, including all 2,906\nvendored conformance resources; zero changed. Seven edited source headers remain\nunchanged; two new headers match the template. Exactly nine authorized core files\nchanged/appeared. The full continued lane inventory is 32 files; the prior scope\nproof is archived as 57-before-round3-scope-check.txt.",
    "scope-check.txt verifies 3,541 protected hashes in this follow-up, including all\n2,906 vendored conformance resources; zero changed. Three edited headers and all\nprior test method bodies are unchanged. Exactly three authorized existing core\nfiles changed. The full continued lane inventory remains 32 files; prior round 3\nscope proof is archived as 66-before-round2-verification-scope-check.txt.", 1)
summary = summary.replace("Sub-nanosecond arithmetic is rejected rather than\nrounded.",
    "Sub-nanosecond arithmetic now truncates toward zero at the\njava.time boundary under the conductor's explicit precision decision.", 1)
summary += "\nFinal follow-up scope proof: 3541 protected files unchanged; three authorized\nsource/test files changed, all three license headers and existing test bodies\npreserved. 72-round2-followup-fixes.patch is the incremental delta. Latest gate:\n71-reactor-verify-round2-followup.txt; focused 69 red -> 70 green; final 934/0/0/9.\n"
(ev / "summary.md").write_text(summary)
blockers = (ev / "66-before-round2-verification-conformance-blockers.md").read_text()
blockers = blockers.replace("Final run 63: BUILD SUCCESS on JDK 11; 914 tests", "Final run 71: BUILD SUCCESS on JDK 11; 934 tests", 1)
blockers = blockers.replace("63-conformance-testcases.json", "71-conformance-testcases.json", 1)
blockers = blockers.replace("63-existing-case-exclusions.json equals runs 36/46/53", "71-existing-case-exclusions.json equals runs 36/46/53/63", 1)
blockers = blockers.replace("## Round 3 review fixes\n", "## Round 2 verification follow-up\n\nIntegral lexical scale and valid sub-nanosecond arithmetic: RESOLVED. Run 69\n117/1/18/0 red with production unchanged; run 70 117/0/0/0 green; run 71 full\n934/0/0/9 green. Scale-zero integral seconds restore the prior lexical form;\nRoundingMode.DOWN truncates nanos toward zero under the conductor's decision.\nTests and decision: review-fixes.md, Round 2 verification follow-up. No new\nconfigured failed case IDs; emitted case order/exclusions are unchanged.\nAdditional IIIE301-303 proposals below were not rerun in this follow-up.\n\n## Round 3 review fixes (retained)\n", 1)
blockers = blockers.replace("All earlier configured blockers remain resolved in run 63", "All earlier configured blockers remain resolved in run 71", 1)
(ev / "conformance-blockers.md").write_text(blockers)
for name in ("summary.md", "review-fixes.md", "surefire-summary.txt", "command.txt", "conformance-blockers.md", "scope-check.txt"):
    assert str(repo) not in (ev / name).read_text(), name
print("PASS: focused red 117/1/18/0 -> green 117/0/0/0; full reactor 934/0/0/9")
print("PASS: conformance 347 + 347 + 17 + 2; no failures; unchanged exclusions and execution order")
print("PASS: 3541 protected files; three authorized changes; headers and prior assertions preserved")
print("Updated summary, review-fixes, surefire-summary, command, blockers and scope evidence")
