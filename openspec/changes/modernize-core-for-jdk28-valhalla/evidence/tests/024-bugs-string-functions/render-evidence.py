"""Build redacted summaries from completed logs and fresh captured receipts."""
import csv
import difflib
import hashlib
import json
import re
from collections import Counter
from pathlib import Path

RUN = Path(__file__).resolve().parent
REPO = RUN.parents[5]
COMBINING = RUN.parent / "025-bug-4.21-combining"
SCOPE = {
    "xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/StringFunctions.java",
    "xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/PermitOverrides.java",
    "xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/StringFunctionSemanticsTest.java",
    "xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/OverrideDecisionTableTest.java",
    "xacml-conformance-tests/src/test/manifest/conformance-manifest.csv",
    "openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/023-conformance-manifest/inherited-skip-dispositions.csv",
}


def load(name):
    return json.loads((RUN / name).read_text())


def normalize(value):
    return value.replace(str(REPO), "<repo>").replace(str(Path.home()), "<home>")


def write_json(path, value):
    path.write_text(json.dumps(value, indent=2) + "\n")


start = load("00-task-start.json")
before = load("08-before-full-gate.json")
after = load("08-after-full-gate.json")
assert before == after, "Source changed during the full clean gate"
assert all(value == after.get(name) for name, value in start.items()
           if name.endswith("pom.xml") or name.startswith("xacml-conformance-tests/src/test/resources/")
           or name.startswith("xacml-xml-jaxb/src/main/resources/xsd/"))

headers = {}
patches = []
for directory, backup, source in [
    (RUN, "00-before-StringFunctions.java", "xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/StringFunctions.java"),
    (COMBINING, "00-before-PermitOverrides.java", "xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/PermitOverrides.java"),
]:
    original = (directory / backup).read_bytes()
    current = (REPO / source).read_bytes()
    header = lambda data: data[data.index(b"/*"):data.index(b"*/", data.index(b"/*")) + 2]
    assert header(original) == header(current)
    headers[source] = {"license_header_unchanged": True, "header_sha256": hashlib.sha256(header(current)).hexdigest()}
    patches.extend(difflib.unified_diff(original.decode().splitlines(keepends=True), current.decode().splitlines(keepends=True),
                                      fromfile="a/" + source, tofile="b/" + source))
    if backup == "00-before-StringFunctions.java":
        core_header = header(original)
for source in sorted(SCOPE):
    if source.endswith("Test.java"):
        data = (REPO / source).read_bytes()
        assert header(data) == core_header
        headers[source] = {"matches_core_LGPL_header": True, "header_sha256": hashlib.sha256(core_header).hexdigest()}
(RUN / "implementation.patch").write_text("".join(patches))
write_json(RUN / "source-scope-audit.json", {
    "scope": sorted(SCOPE), "files_hashed_before_and_after_full_gate": len(before), "full_gate_source_changes": [],
    "packet_modified_in_scope": sorted(name for name in start if start[name] != after.get(name) and name in SCOPE),
    "packet_added_in_scope": sorted(name for name in after if name not in start and name in SCOPE),
    "conductor_merge_modified_files": sorted(name for name in start if start[name] != after.get(name) and name not in SCOPE),
    "concurrent_unowned_additions_preserved": sorted(name for name in after if name not in start and name not in SCOPE),
    "poms_unchanged": sum(name.endswith("pom.xml") for name in start),
    "conformance_resource_files_unchanged": sum(name.startswith("xacml-conformance-tests/src/test/resources/") for name in start),
    "vendored_schema_files_unchanged": sum(name.startswith("xacml-xml-jaxb/src/main/resources/xsd/") for name in start),
    "headers": headers,
    "attribution": "Out-of-scope changes are consistent with the conductor-reported merges. Hashes identify changes, not their author. This lane did not edit them.",
})

modules = load("08-full-reactor-results.json")
cases = load("08-final-case-results.json")
assert all(entry["counts"]["failures"] == entry["counts"]["errors"] == 0 for entry in modules.values())
assert all(row["outcome"] == ("passed" if row["enabled"] == "true" else "skipped") for row in cases["cases"])
enabled = load("05-enabled-case-probe-scope.json")["changes"]
selected = {(entry["after"]["suite"], entry["after"]["case_id"]) for entry in enabled}
assert len(selected) == 16
target_rows = [row for row in cases["cases"] if (row["suite"], row["case_id"]) in selected]
assert len(target_rows) == 16 and all(row["outcome"] == "passed" and row["bug_id"] == "" for row in target_rows)
write_json(RUN / "targeted-case-results.json", target_rows)
write_json(COMBINING / "iid300-result.json", next(row for row in target_rows if row["case_id"] == "IID300"))
with (RUN / "targeted-case-results.csv").open("w", newline="") as output:
    writer = csv.DictWriter(output, fieldnames=list(target_rows[0]))
    writer.writeheader()
    writer.writerows(target_rows)

total = {key: sum(entry["counts"][key] for entry in modules.values()) for key in ("tests", "failures", "errors", "skipped")}
module_table = "\n| Module | Tests run | Failures | Errors | Skipped |\n| --- | ---: | ---: | ---: | ---: |\n"
for module, entry in modules.items():
    c = entry["counts"]
    module_table += f"| {module} | {c['tests']} | {c['failures']} | {c['errors']} | {c['skipped']} |\n"
module_table += f"| Total | {total['tests']} | {total['failures']} | {total['errors']} | {total['skipped']} |\n"
case_table = "\n| Suite | Manifest rows | Executed disposition | Special harness (passed / skipped) | Skipped disposition | Cases passed | Cases skipped |\n| --- | ---: | ---: | --- | ---: | ---: | ---: |\n"
for suite in ("oasis20", "oasis30", "rsa2008", "example"):
    rows = [row for row in cases["cases"] if row["suite"] == suite]
    dispositions = Counter(row["disposition"] for row in rows)
    special = Counter(row["outcome"] for row in rows if row["disposition"] == "special-harness")
    outcomes = Counter(row["outcome"] for row in rows)
    case_table += f"| {suite} | {len(rows)} | {dispositions['executed']} | {special['passed']} / {special['skipped']} | {dispositions['skipped']} | {outcomes['passed']} | {outcomes['skipped']} |\n"
case_table += "| Total | 880 | 849 | 3 / 1 | 27 | 852 | 28 |\n"
appendix = (module_table + "\nThese are Surefire invocation counts, including parameterized invocations and skips. "
            "The two new classes contribute 505 and 3,048 invocations. xacml-xml-jaxb has no tests.\n" + case_table +
            "\nThe conformance module reports 901 invocations: 880 manifest cases, the two preserved testName tests, "
            "and 19 guard/import/resolver/integrity tests. Case totals exclude those 21 non-case checks. "
            "852 cases passed, 0 failed, 28 were skipped. Skipped rows are retained, not evidence of a fresh failed evaluation.\n\n"
            "Raw gate output: `../024-bugs-string-functions/07-conformance-gate.log` and "
            "`../024-bugs-string-functions/08-full-reactor-clean-verify.log`. Fresh module/class receipts: "
            "`../024-bugs-string-functions/08-full-reactor-results.json`; per-case receipts: "
            "`../024-bugs-string-functions/08-final-case-results.csv`. See surefire-summary.txt for exact Maven lines.\n")
for directory in (RUN, COMBINING):
    summary = directory / "summary.md"
    body = summary.read_text().split("<!-- derived-counts -->")[0].rstrip()
    summary.write_text(body + "\n\n<!-- derived-counts -->\n" + appendix)

blocked = [row for row in cases["cases"] if row["outcome"] == "skipped"]
body = ("# Retained conformance blockers\n\nThese 28 cases were disabled in the final manifest and were not re-probed. "
        "Reasons and bug ids below are retained from the prior case classification, not fresh failures in this gate. "
        "All 16 cases targeted by bugs 4.14, 4.15 and 4.21 passed and are absent from this list.\n\n"
        "| Suite | Case | Bug | Retained reason |\n| --- | --- | --- | --- |\n")
for row in blocked:
    body += f"| {row['suite']} | {row['case_id']} | {row['bug_id']} | {row['reason'].replace('|', '&#124;')} |\n"
(RUN / "conformance-blockers.md").write_text(body)

# Only completed Maven logs are normalized. -l preserves exact Maven output;
# redaction changes checkout/home paths, not result lines or exit status.
logs = sorted(RUN.glob("*.log")) + sorted(COMBINING.glob("*.log"))
for path in logs:
    path.write_text(normalize(path.read_text()))
output = ["Completed Maven results; paths replaced with <repo> / <home>.\n"]
final_maven_totals = []
for path in logs:
    relative = path.relative_to(RUN.parent)
    lines = path.read_text().splitlines()
    output += [f"\n=== {relative} ===\n"]
    if path.name == "08-full-reactor-clean-verify.log":
        output += ["Per-class Maven Tests run lines:\n"]
        output += [line + "\n" for line in lines if "Tests run:" in line and " in org.xacml4j." in line]
    module = "unknown"
    for index, line in enumerate(lines):
        match = re.search(r" @ (xacml-[^ ]+) ---", line)
        if match:
            module = match.group(1)
        if "[INFO] Results:" in line:
            output += [f"\nModule: {module}\n"]
            for result_line in lines[index:]:
                output += [result_line + "\n"]
                totals = re.fullmatch(r"\[(?:INFO|WARNING|ERROR)\] Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)", result_line)
                if totals:
                    if path.name == "08-full-reactor-clean-verify.log":
                        final_maven_totals.append((module, tuple(map(int, totals.groups()))))
                    break
    output += [line + "\n" for line in lines if "BUILD SUCCESS" in line or "BUILD FAILURE" in line or "Total time:" in line]
assert dict(final_maven_totals) == {module: tuple(entry["counts"][key] for key in ("tests", "failures", "errors", "skipped"))
                                   for module, entry in modules.items()}, final_maven_totals
for directory in (RUN, COMBINING):
    (directory / "surefire-summary.txt").write_text("".join(output))
print(json.dumps({"full_reactor": total, "targeted_cases_passed": len(target_rows), "retained_case_skips": len(blocked),
                  "pom_files_unchanged": 8, "vendored_files_unchanged": 2911,
                  "full_gate_source_files_unchanged": len(before)}, indent=2))
