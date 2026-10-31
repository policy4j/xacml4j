"""Summarize completed gates and append the authorized rv026 verification."""
import csv
import json
import re
import xml.etree.ElementTree as ET
from collections import Counter
from pathlib import Path

RUN = Path(__file__).resolve().parent
REPO = RUN.parents[5]
CHANGE = "openspec/changes/modernize-core-for-jdk28-valhalla"
MODULES = ["xacml-core", "xacml-xml-jaxb", "xacml-xml", "xacml-json", "xacml-pdp", "xacml-test", "xacml-conformance-tests"]
SUITES = ["oasis20", "oasis30", "rsa2008", "example"]
ANSI = re.compile(r"\x1b\[[0-?]*[ -/]*[@-~]")


def clean(text):
    return text.replace(str(REPO), "<repo>").replace(str(Path.home()), "<home>")


def module_counts():
    counts = {"root (pom)": dict.fromkeys(("tests", "failures", "errors", "skipped"), 0)}
    for module in MODULES:
        roots = [ET.parse(path).getroot() for path in (REPO / module / "target/surefire-reports").glob("TEST-*.xml")]
        counts[module] = {key: sum(int(root.attrib[key]) for root in roots) for key in ("tests", "failures", "errors", "skipped")}
    current = None
    logged = {}
    for line in (RUN / "15-jdk21-final-clean-verify.log").read_text().splitlines():
        module = re.search(r"< org\.xacml4j:([^ >]+) >", line)
        if module:
            current = module.group(1)
        result = re.search(r"Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)", line)
        if result:
            logged[current] = dict(zip(("tests", "failures", "errors", "skipped"), map(int, result.groups())))
    assert set(logged) == set(MODULES) - {"xacml-xml-jaxb"}, logged
    assert all(counts[module] == result for module, result in logged.items()), (counts, logged)
    return counts


def dependency_proof():
    expected = {"xacml", *MODULES}
    proof = {}
    for name, filename in (("objenesis", "12-objenesis-tree-retry.log"), ("legacy", "13-legacy-tree-retry.log")):
        lines = ANSI.sub("", (RUN / filename).read_text()).splitlines()
        assert any("BUILD SUCCESS" in line for line in lines), filename
        projects = {}
        current = None
        for line in lines:
            match = re.search(r"--- dependency:.* @ (\S+) ---", line)
            if match:
                current = match.group(1)
                projects[current] = []
            if current and re.search(r"org\.mockito:|org\.objenesis:|junit:junit:|org\.junit\.vintage:|org\.easymock:", line):
                projects[current].append(line)
        assert set(projects) == expected, projects
        if name == "legacy":
            assert all(not lines for lines in projects.values()), projects
        else:
            for project, lines in projects.items():
                assert len(lines) == 2, (project, lines)
                assert "org.mockito:mockito-core:jar:5.24.0:test" in lines[0], lines
                assert "org.objenesis:objenesis:jar:3.3:test" in lines[1], lines
        proof[name] = {"projects_checked": len(projects), "project_results": projects,
                       "result": "only beneath mockito-core" if name == "objenesis" else "empty in every project"}
    (RUN / "dependency-checks.json").write_text(json.dumps(proof, indent=2) + "\n")
    return proof


def surefire_summary():
    output = []
    for filename in ("01-guard-import-red.log", "02-guard-import-green.log", "03-all-cases-probe.log",
                     "04-all-cases-probe.log", "05-dependencies-guard-red.log", "06-all-cases-after-harness-fixes.log",
                     "07-nested-inventory-guard-red.log", "08-jdk21-conformance-gate.log",
                     "09-jdk21-clean-verify.log", "15-jdk21-final-clean-verify.log"):
        output += [filename, ""]
        current = "reactor"
        for line in ANSI.sub("", (RUN / filename).read_text()).splitlines():
            module = re.search(r"< org\.xacml4j:([^ >]+) >", line)
            if module:
                current = module.group(1)
            if any(token in line for token in ("Results:", "Tests run:", "BUILD SUCCESS", "BUILD FAILURE", "COMPILATION ERROR")):
                output.append(f"[{current}] {line}")
        output.append("")
    (RUN / "surefire-summary.txt").write_text(clean("\n".join(output)) + "\n")


def commands():
    prefix = 'JAVA_HOME="$(jenv prefix 21)" rtk mvn -Dlicense.skipUpdateLicense=true '
    base = CHANGE + "/evidence/tests/023-conformance-manifest/"
    all_cases = "Xacml20ConformanceTest,Xacml30ConformanceTest,RSA2008InteropTest,XacmlExampleTest,ConformanceManifestTest,ConformancePolicyImportTest,VendoredFixtureIntegrityTest"
    lines = ["Commands executed from <repo>; all Maven invocations were plain commands without pipes or shell separators.", "",
             "Read AGENTS.md; docs/ai/TESTING.md; docs/ai/OPENSPEC.md Apply loop; proposal/design/tasks; conformance-coverage spec; Luna's 007 draft; the relevant fixture Special.txt and repository instructions.",
             "Memory quick pass: scoped MEMORY.md search; no workspace facts were accepted without local checks.", "",
             "rtk python3 " + base + "audit.py snapshot"]
    builds = [
        ("01-guard-import-red.log", "ConformanceManifestTest,ConformancePolicyImportTest"),
        ("02-guard-import-green.log", "ConformanceManifestTest,ConformancePolicyImportTest"),
        ("03-all-cases-probe.log", all_cases), ("04-all-cases-probe.log", all_cases),
        ("05-dependencies-guard-red.log", "ConformanceManifestTest"),
        ("06-all-cases-after-harness-fixes.log", all_cases),
        ("07-nested-inventory-guard-red.log", "ConformanceManifestTest"),
    ]
    for filename, tests in builds:
        if filename == "02-guard-import-green.log":
            lines.append("rtk python3 " + base + "audit.py generate")
        if filename == "06-all-cases-after-harness-fixes.log":
            lines.append("rtk python3 " + base + "audit.py generate")
        lines.append(prefix + "-pl xacml-conformance-tests -am -Dtest=" + tests
                     + " -Dsurefire.failIfNoSpecifiedTests=false -l " + base + filename + " test")
        if filename.startswith(("04", "06")):
            lines.append("rtk python3 " + base + "results.py " + filename[:2])
    lines += ["rtk python3 " + base + "classify.py",
              prefix + "-pl xacml-conformance-tests -am -l " + base + "08-jdk21-conformance-gate.log test",
              "rtk python3 " + base + "results.py 08",
              prefix + "-l " + base + "09-jdk21-clean-verify.log clean verify",
              "rtk python3 " + base + "results.py 09"]
    for number, includes, suffix in (("10", "org.objenesis:objenesis", "objenesis-test-tree"),
                                     ("11", '"junit:junit,org.junit.vintage:*,org.easymock:*"', "legacy-test-tree"),
                                     ("12", "org.objenesis:objenesis", "objenesis-tree-retry"),
                                     ("13", '"junit:junit,org.junit.vintage:*,org.easymock:*"', "legacy-tree-retry")):
        if includes.startswith('"'):
            flag = '"-Dincludes=' + includes[1:]
        else:
            flag = "-Dincludes=" + includes
        lines.append(prefix + "-Dscope=test " + flag + " -l " + base + number + "-" + suffix + ".log dependency:tree")
    lines += ["# Tree attempts 10/11 hit sandbox metadata-cache write denial; 12/13 reran with native escalation and both passed.",
              prefix + "-version", "rtk python3 " + base + "classify.py",
              "rtk python3 " + base + "audit.py final",
              "# The first final audit detected eight concurrent outside-lane source edits. They were preserved; fixtures/POMs stayed unchanged.",
              "rtk python3 " + base + "audit.py gate-start",
              prefix + "-l " + base + "15-jdk21-final-clean-verify.log clean verify",
              "rtk python3 " + base + "audit.py gate-end", "rtk python3 " + base + "results.py 15",
              "ast-grep run --lang java --pattern 'ConformanceManifest.cases($SUITE)' --json "
              + " ".join("xacml-conformance-tests/src/test/java/org/xacml4j/v20/" + name + ".java" for name in
                         ("Xacml20ConformanceTest", "Xacml30ConformanceTest", "RSA2008InteropTest", "XacmlExampleTest")),
              "rtk rg -n 'i < testCount|skipTests\\.add|addAllPolicies|LOG\\.info\\(\"Could not load' "
              + " ".join("xacml-conformance-tests/src/test/java/org/xacml4j/v20/" + name + ".java" for name in ("Xacml20ConformanceTest", "Xacml30ConformanceTest")),
              "rtk git diff --check -- xacml-conformance-tests/src/test/java",
              "rtk python3 " + base + "audit.py final",
              "rtk python3 " + base + "write-evidence.py",
              "rtk git status --short", "rtk python3 " + base + "audit.py gate-end", "",
              "External specification check: native Tavily extract of https://www.w3.org/TR/xmlschema-2/#dateTime-timezones (dateTime timezone range).", ""]
    (RUN / "command.txt").write_text("\n".join(lines))


def case_tables():
    final = json.loads((RUN / "15-case-results.json").read_text())["cases"]
    first = {(row["suite"], row["case_id"]): row for row in json.loads((RUN / "04-case-results.json").read_text())["cases"]}
    second = {(row["suite"], row["case_id"]): row for row in json.loads((RUN / "06-case-results.json").read_text())["cases"]}
    blockers = {(row["suite"], row["case_id"]): row for row in json.loads((RUN / "conformance-blockers.json").read_text())}
    baseline = list(csv.DictReader((REPO / CHANGE / "evidence/builds/007-conformance-manifest-draft/manifest-draft.csv").open()))
    legacy = {(row["suite"], row["case_id"]): row for row in baseline}
    fields = ["suite", "case_id", "disposition", "enabled", "final_result", "probe_04", "probe_06",
              "previously_executed", "classification", "reason", "bug_id", "request_file", "policy_files", "expected_response_file"]
    rows = []
    for row in final:
        key = row["suite"], row["case_id"]
        assert row["outcome"] == ("passed" if row["enabled"] == "true" else "skipped"), row
        rows.append({"suite": row["suite"], "case_id": row["case_id"], "disposition": row["disposition"],
                     "enabled": row["enabled"], "final_result": row["outcome"], "probe_04": first[key]["outcome"],
                     "probe_06": second[key]["outcome"], "previously_executed": legacy[key]["executed_latest_green_run"],
                     "classification": blockers.get(key, {}).get("classification", ""), "reason": row["reason"],
                     "bug_id": row["bug_id"], "request_file": row["request_file"], "policy_files": row["policy_files"],
                     "expected_response_file": row["expected_response_file"]})
    with (RUN / "per-case-results.csv").open("w", newline="") as stream:
        writer = csv.DictWriter(stream, fieldnames=fields, lineterminator="\n")
        writer.writeheader()
        writer.writerows(rows)
    previous = {key for key, row in legacy.items() if row["executed_latest_green_run"] == "yes"}
    passed = {(row["suite"], row["case_id"]) for row in final if row["outcome"] == "passed"}
    assert previous <= passed, previous - passed
    inherited = [row for row in rows if legacy[(row["suite"], row["case_id"])] ["disposition"] == "skipped-exclusion"
                 or (row["suite"].startswith("oasis") and row["case_id"] == "IID029")]
    assert Counter(row["suite"] for row in inherited) == {"oasis20": 21, "oasis30": 21}
    with (RUN / "inherited-skip-dispositions.csv").open("w", newline="") as stream:
        writer = csv.DictWriter(stream, fieldnames=fields, lineterminator="\n")
        writer.writeheader()
        writer.writerows(inherited)
    totals = {}
    for suite in SUITES:
        selected = [row for row in final if row["suite"] == suite]
        totals[suite] = {"cases": len(selected), "executed": sum(row["disposition"] == "executed" for row in selected),
                         "special_harness": sum(row["disposition"] == "special-harness" for row in selected),
                         "special_passed": sum(row["disposition"] == "special-harness" and row["outcome"] == "passed" for row in selected),
                         "skipped": sum(row["disposition"] == "skipped" for row in selected),
                         "passed": sum(row["outcome"] == "passed" for row in selected)}
    (RUN / "manifest-baseline.json").write_text(json.dumps({"suites": totals, "total_cases": len(final),
                                                          "passed_cases": len(passed), "previous_unique_cases": len(previous),
                                                          "new_passing_cases": len(passed - previous), "removed_previous_cases": []}, indent=2) + "\n")
    return totals


def report(counts, totals):
    module_rows = [f"| {name} | {value['tests']} | {value['failures']} | {value['errors']} | {value['skipped']} |" for name, value in counts.items()]
    overall = {key: sum(value[key] for value in counts.values()) for key in ("tests", "failures", "errors", "skipped")}
    expected_minimums = {"xacml-core": (760, 2), "xacml-xml": (34, 0), "xacml-json": (5, 1),
                         "xacml-pdp": (73, 3), "xacml-test": (20, 0), "xacml-conformance-tests": (901, 44)}
    for name, (minimum, skipped) in expected_minimums.items():
        assert counts[name]["tests"] >= minimum and counts[name]["skipped"] == skipped, (name, counts[name])
    assert overall["failures"] == 0 and overall["errors"] == 0 and overall["skipped"] == 50, overall
    (RUN / "final-module-counts.json").write_text(json.dumps({"modules": counts, "total": overall}, indent=2) + "\n")
    baseline_rows = [f"| {suite} | {value['cases']} | {value['executed']} | {value['special_harness']} ({value['special_passed']} passed) | {value['skipped']} | {value['passed']} |" for suite, value in totals.items()]
    audit = json.loads((RUN / "final-scope-audit.json").read_text())
    own = [path for path in audit["changed_source_or_manifest_files"] if path.startswith("xacml-conformance-tests/")]
    summary = ["# Conformance manifest and group 3 gate", "", "Tasks 3.1–3.4 are implemented; task 3.6's current-JDK gate passes. Tracking remains with the conductor.",
               "", "JDK: Java version 21.0.6, vendor Oracle Corporation (GraalVM); Maven 3.9.16; macOS aarch64. See jdk-version.txt.",
               "", "## Result and manifest baseline", "",
               "The manifest has 880 unique cases. All 836 enabled cases pass: 833 executed entries and three working special-harness entries. Forty-three entries are skipped and the remaining special-harness entry is blocked, for 44 disabled cases.",
               "", "| Suite | Request cases | Executed disposition | Special-harness | Skipped disposition | Passed cases |",
               "| --- | ---: | ---: | ---: | ---: | ---: |", *baseline_rows,
               "", "Every one of run 005/007's 711 distinct passing cases still executes. The old 347+347 OASIS events contained IIIG006 twice in each suite; the manifest runs each ID once. Coverage grows by 125 unique passing cases; no prior distinct case was dropped.",
               "The 72 superseded requests in xacml3.0-deprecated are outside the packet's 487 active 3.0 cases. The guard inventories all other active request directories recursively, rejects duplicate IDs, validates both directions and checks every referenced policy/response file.",
               "", "## Changes", "", *["- " + path for path in own],
               "", "The four runners use Jupiter parameterized tests with one independent result per manifest row. Numeric bounds and the duplicate IIIG006 invocation are gone. The two existing testName assertions remain. RSA and examples keep XacmlPolicyTestSupport's existing response assertions; OASIS keeps the existing 2.0/3.0 utility assertions.",
               "Policy loading is isolated per case and imports exactly the manifest's policies; missing or broken imports fail immediately with the case, file and original error. No shared import exception is logged and discarded. XACML 3.0 roots and dependencies use the 3.0 media type. Default multiple-decision handlers are enabled.",
               "IID029/IID030 import both roots and use the engine's only-one-applicable PolicySet to consider Target matching. Both 2.0 cases and 3.0 IID030 pass. 3.0 IID029's Policy1 uses the wrong action category; it remains special-harness with enabled=false and BUG-TBD-IID029.",
               "IIE001/IIE002's missing 3.0 dependency paths were corrected from the draft, including the actual lowercase-i IIE001Policyid1.xml spelling. IIE003 uses its Special.txt method 1: explicitly assert PolicyId2's static type mismatch, retain PolicyId1, and compare the unchanged response. This is the documented alternative, not an ignored import failure.",
               "", "## Red/green and gate receipts", "",
               "- 01: test-first compile red because the new manifest/runner APIs did not exist; 02: all 12 initial guard/import tests pass. The compile-red receipt is not presented as a semantic failure.",
               "- 03: extra closing brace in the RSA replacement caused a compile error; corrected before enumerating cases.",
               "- 04: all 880 cases attempted, with 48 failures/errors. 05: dependency-inventory and path-escape tests red (two failures). 06: both 3.0 reference cases and both IIE003 cases now pass; 44 out-of-scope failures remain.",
               "- 07: nested-request and duplicate-request guards red (two failures). Final guards pass all 13 tests. Import/media-type tests pass all three tests.",
               "- 08: requested conformance -am test gate passes; 1,788 tests, zero failures/errors, 49 skips. The JSON module is not in this dependency slice.",
               "- 09: full JDK 21 clean verify passes with 1,793 tests, zero failures/errors and 50 skips. 15: the final full clean gate passes with 1,801 tests, zero failures/errors and 50 skips. Concurrent outside-lane tests raised core 760 to 765, XML 34 to 35, JSON 5 to 6 and PDP 73 to 74. Test-support and conformance counts are unchanged. The final run's 3,546 recorded project-file hashes are identical before/after (15-final-gate-stability.json).",
               "", "| Module | Tests run | Failures | Errors | Skipped |", "| --- | ---: | ---: | ---: | ---: |", *module_rows,
               f"| **Total** | **{overall['tests']}** | **0** | **0** | **50** |",
               "", "Conformance Surefire's 901 tests are 880 fixture rows plus 21 helper/name/resolver/integrity tests. The 44 conformance skips are explicit manifest blockers; the other six skips retain the prior core/JSON/PDP reasons. Every IIC340–349 duration-set case passes. Per-case outcomes and every inherited skip are in per-case-results.csv and inherited-skip-dispositions.csv.",
               "", "## Failure classification and residual risk", "",
               "All 44 blocked cases have fresh failing receipts, a concrete reason and a bug reference: 36 engine defects, seven fixture/spec issues, one out-of-scope comparison-harness defect. See conformance-blockers.md/JSON for every case ID and cause. IIIE301–303 use existing policy4j-the.3.7; the other 41 suite/case rows use BUG-TBD-<case> pending conductor triage. The manifest gate is green with these exclusions; it is not complete conformance acceptance.",
               "The 21 inherited fixture-backed exclusions in each suite are reconciled individually. Twelve now pass in 2.0 and sixteen in 3.0; nine and five respectively remain blocked. The nine historical skip numbers without request fixtures are not phantom manifest rows.",
               "No fixture, expected response, POM or production file was edited by this lane. Original four runner license headers are byte-identical. The starting snapshot detected eight later outside-lane source edits; those were preserved and included in the final stable clean gate. final-scope-audit.json lists them and marks the wider scope audit PARTIAL. No author is inferred.",
               "The retained response utilities have prior oracle gaps (2.0 obligation self-comparison; 3.0 lacks AssociatedAdvice comparison). They were not changed or weakened; passing cases prove the existing assertions. Hierarchy and policy-reference tracking root-cause details still need maintainer review.",
               "", "## rv026 correction verification", "",
               "Trees 10/11 encountered sandbox metadata-cache write denial; native escalation retries 12/13 completed across all eight projects. Objenesis 3.3 appears only below Mockito core 5.24.0. The JUnit 4/Vintage/EasyMock filter is empty in every project. The approved verification addendum is appended to run 017 summary.md; no POM was edited.",
               "", "No commit, stage, install, br command, task tick or issue close was performed.", ""]
    (RUN / "summary.md").write_text("\n".join(summary))
    append_rv026(module_rows, overall)


def append_rv026(module_rows, overall):
    target = REPO / CHANGE / "evidence/tests/017-legacy-stack-removed/summary.md"
    original = target.read_text()
    assert "## Review correction (rv026)" in original
    marker = "### Addendum: verification"
    if marker in original:
        return
    base = CHANGE + "/evidence/tests/023-conformance-manifest/"
    lines = ["", marker, "", "Verified after the conductor restored Mockito's transitive Objenesis. JDK 21.0.6; Maven 3.9.16.",
             "All eight reactor projects passed clean verify twice (run 023 logs 09 and 15). The final gate retained the same source/POM hashes before and after.",
             "The first full gate (09) retained run 017's core/XML/JSON/PDP/test-support counts and expanded conformance from 42 Surefire tests to 901: 1,793 tests, zero failures/errors and 50 skips. Concurrent outside-lane tests raised core 760 to 765, XML 34 to 35, JSON 5 to 6 and PDP 73 to 74 before the final gate (15), which ran 1,801 tests. All module counts meet or exceed run 017; no prior distinct conformance case was removed. Final counts:",
             "", "| Module | Tests run | Failures | Errors | Skipped |", "| --- | ---: | ---: | ---: | ---: |", *module_rows,
             f"| **Total** | **{overall['tests']}** | **0** | **0** | **50** |", "",
             "Conformance: 362 OASIS 2.0, 455 OASIS 3.0, 17 RSA and two examples passed; 44 fixture cases have documented blocking dispositions. The other six unit-test skips are unchanged.",
             "", "Exact plain Maven commands:", "", "```sh",
             'JAVA_HOME="$(jenv prefix 21)" rtk mvn -Dlicense.skipUpdateLicense=true -l ' + base + "09-jdk21-clean-verify.log clean verify",
             'JAVA_HOME="$(jenv prefix 21)" rtk mvn -Dlicense.skipUpdateLicense=true -l ' + base + "15-jdk21-final-clean-verify.log clean verify",
             'JAVA_HOME="$(jenv prefix 21)" rtk mvn -Dlicense.skipUpdateLicense=true -Dscope=test -Dincludes=org.objenesis:objenesis -l ' + base + "12-objenesis-tree-retry.log dependency:tree",
             'JAVA_HOME="$(jenv prefix 21)" rtk mvn -Dlicense.skipUpdateLicense=true -Dscope=test "-Dincludes=junit:junit,org.junit.vintage:*,org.easymock:*" -l ' + base + "13-legacy-tree-retry.log dependency:tree", "```", "",
             "Both dependency checks completed for every project. Objenesis is present only as org.mockito:mockito-core:5.24.0 -> org.objenesis:objenesis:3.3. JUnit 4, Vintage and EasyMock results are empty in all eight projects. Initial sandbox cache-write denials (10/11) are retained; approved native retries (12/13) provide the complete proof.",
             "This supersedes the earlier run 017 claims that transitive Objenesis is absent; direct dependency/management/property removal is retained. This lane made no POM edits.",
             "", "Evidence: [run 023 summary](../023-conformance-manifest/summary.md), [Surefire results](../023-conformance-manifest/surefire-summary.txt), [dependency proof](../023-conformance-manifest/dependency-checks.json), [per-case results](../023-conformance-manifest/per-case-results.csv).", ""]
    target.write_text(original + "\n".join(lines))


def main():
    # Every selected log belongs to a completed process; never normalize an active Maven log.
    for log in RUN.glob("*.log"):
        original = log.read_text()
        assert "\x00" not in original, log
        log.write_text(clean(original))
    for filename in ("09-jdk21-clean-verify.log", "15-jdk21-final-clean-verify.log"):
        assert "BUILD SUCCESS" in (RUN / filename).read_text(), filename
    dependency_proof()
    surefire_summary()
    commands()
    counts = module_counts()
    totals = case_tables()
    report(counts, totals)
    print(json.dumps({"module_totals": {key: sum(value[key] for value in counts.values()) for key in ("tests", "failures", "errors", "skipped")},
                      "manifest_baseline": totals, "rv026_addendum": "appended"}, indent=2))


if __name__ == "__main__":
    main()
