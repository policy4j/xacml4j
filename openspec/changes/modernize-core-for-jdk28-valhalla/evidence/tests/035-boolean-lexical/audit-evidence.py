"""Check packet scope, preserve headers, and summarize completed gate receipts."""
import csv
import difflib
import hashlib
import json
import re
from pathlib import Path

RUN = Path(__file__).resolve().parent
REPO = RUN.parents[5]
MANIFEST = "xacml-conformance-tests/src/test/manifest/conformance-manifest.csv"
BLOCKERS = "openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/030-harness-comparators/conformance-blockers.md"
MODIFIED = (
    "xacml-core/src/main/java/org/xacml4j/v30/types/BooleanVal.java",
    "xacml-core/src/test/java/org/xacml4j/v30/types/BooleanValTest.java",
    "xacml-json/src/main/java/org/xacml4j/v30/marshal/json/GsonUtil.java",
    "xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java",
    MANIFEST, BLOCKERS,
)
NEW = {
    "xacml-core/src/test/java/org/xacml4j/v30/types/BooleanLexicalTest.java":
        "xacml-core/src/test/java/org/xacml4j/v30/types/BooleanValTest.java",
    "xacml-xml/src/test/java/org/xacml4j/v30/xml/BooleanRequestLexicalTest.java":
        "xacml-xml/src/test/java/org/xacml4j/v30/xml/Xacml30RequestContentTest.java",
    "xacml-json/src/test/java/org/xacml4j/v30/marshal/json/BooleanRequestLexicalTest.java":
        "xacml-json/src/test/java/org/xacml4j/v30/marshal/json/JsonRequestUnmarshallerTest.java",
}


def load(name):
    return json.loads((RUN / name).read_text())


def normalize(value):
    return value.replace(str(REPO), "<repo>").replace(str(Path.home()), "<home>")


def write_json(name, value):
    (RUN / name).write_text(json.dumps(value, indent=2) + "\n")


def header(value):
    return value[value.index("/*"):value.index("*/") + 2]


before = load("00-source-hashes.json")
after = load("10-post-full-gate-source-hashes.json")
external_changes = load("08-conductor-xml-merge-changes.json")
external_paths = {row["file"] for row in external_changes}
changed = {path for path in set(before) | set(after) if before.get(path) != after.get(path)}
assert changed == set(MODIFIED) | set(NEW) | external_paths, sorted(changed)
assert load("05-pre-conformance-source-hashes.json") == load("05-post-conformance-source-hashes.json") == load("06-post-full-gate-source-hashes.json")
assert load("09-pre-xml-conformance-source-hashes.json") == load("09-post-xml-conformance-source-hashes.json") == after
merged = load("08-merged-xml-source-hashes.json")
assert all(after.get(path) == merged.get(path) for path in external_paths)
for path, expected in after.items():
    assert hashlib.sha256((REPO / path).read_bytes()).hexdigest() == expected, path

patch, headers = [], []
for relative in sorted(set(MODIFIED) | set(NEW)):
    current = (REPO / relative).read_text()
    previous = "" if relative in NEW else (RUN / ("00-before-" + Path(relative).name)).read_text()
    patch.extend(difflib.unified_diff(previous.splitlines(keepends=True), current.splitlines(keepends=True),
                                     fromfile="/dev/null" if relative in NEW else "a/" + relative,
                                     tofile="b/" + relative))
    if relative.endswith(".java"):
        reference = (REPO / NEW[relative]).read_text() if relative in NEW else previous
        assert header(reference) == header(current), relative
        headers.append({"file": relative, "unchanged_header": True,
                        "reference": NEW.get(relative, "task-start source")})
(RUN / "task-only.patch").write_text("".join(patch))

old_test = (RUN / "00-before-BooleanValTest.java").read_text()
new_test = (REPO / "xacml-core/src/test/java/org/xacml4j/v30/types/BooleanValTest.java").read_text()
method_pattern = r"@Test\s+public void (\w+)\("
assert re.findall(method_pattern, old_test) == re.findall(method_pattern, new_test)

old_blockers = (RUN / "00-before-conformance-blockers.md").read_text()
assert (REPO / BLOCKERS).read_text() == old_blockers.replace("BUG-TBD-IIIA030", "policy4j-the.4.24").replace("BUG-TBD-IIIA330", "policy4j-the.4.24")

old_rows = list(csv.DictReader((RUN / "00-before-conformance-manifest.csv").open()))
new_rows = list(csv.DictReader((REPO / MANIFEST).open()))
assert len(old_rows) == len(new_rows) == 880
assert [(r["suite"], r["case_id"]) for r in old_rows] == [(r["suite"], r["case_id"]) for r in new_rows]
manifest_changes = []
for old, new in zip(old_rows, new_rows):
    fields = [key for key in old if old[key] != new[key]]
    assert not set(fields) - {"disposition", "enabled", "reason", "bug_id"}
    if fields:
        manifest_changes.append({"suite": new["suite"], "case_id": new["case_id"],
                                 "fields": {key: {"before": old[key], "after": new[key]} for key in fields}})
expected_manifest = {("oasis20", "IIA005"), *(('oasis30', case) for case in
    ("IIA005", "IIA022", "IIA024", "IIIA030", "IIIA330", "IIIA340", "IIIG300", "IIIG301", "IIIE301", "IIIE303"))}
assert {(r["suite"], r["case_id"]) for r in manifest_changes} == expected_manifest
write_json("manifest-changes.json", manifest_changes)

old_cases = {(r["suite"], r["case_id"]): r for r in load("00-prior-case-results.json")["cases"]}
final_cases = load("10-final-case-results.json")["cases"]
changes = [{"suite": r["suite"], "case_id": r["case_id"],
            "before": old_cases[r["suite"], r["case_id"]]["outcome"], "after": r["outcome"]}
           for r in final_cases if old_cases[r["suite"], r["case_id"]]["outcome"] != r["outcome"]]
assert {(r["suite"], r["case_id"]) for r in changes} == {("oasis30", case) for case in ("IIA022", "IIA024", "IIIA340", "IIIG301", "IIIE301", "IIIE303")}
assert all(r["before"] == "skipped" and r["after"] == "passed" for r in changes)
assert all(r["outcome"] in {"passed", "skipped"} for r in final_cases)
assert load("05-conformance-case-results.json") == load("06-final-case-results.json")
assert load("09-merged-xml-case-results.json") == load("10-final-case-results.json")
write_json("case-result-changes.json", changes)

scope = {"result": "PASS", "task_start_existing_files": len(before), "task_end_files": len(after),
         "unchanged_existing_files": sum(after.get(path) == digest for path, digest in before.items()),
         "modified_existing_files": sorted(MODIFIED), "new_java_files": sorted(NEW),
         "external_conductor_changes_after_run06": external_changes,
         "source_bytes_stable_during_both_gates": True, "license_headers": headers,
         "BooleanValTest_methods_preserved": re.findall(method_pattern, old_test),
         "manifest_changed_rows": len(manifest_changes), "manifest_inventory": len(new_rows),
         "run030_change_limited_to_two_bug_ids": True}
write_json("scope-and-header-audit.json", scope)

results = load("10-full-reactor-results.json")
assert all(v["counts"]["failures"] == v["counts"]["errors"] == 0 for v in results.values())
for module, data in results.items():
    assert data["counts"]["tests"] >= load("00-prior-module-counts.json")[module]["counts"]["tests"]
totals = {key: sum(data["counts"][key] for data in results.values()) for key in ("tests", "failures", "errors", "skipped")}
write_json("final-reactor-totals.json", totals)

summary = ["Maven Surefire output from completed JDK 21 runs. Paths are normalized to <repo>/<home>.\n",
           "Run 02 is intentionally RED. maven.test.failure.ignore=true let XML and JSON regressions also run; Maven exit 0 is not a green result.\n",
           "Run 05 selects conformance and upstream modules; xacml-json is only part of runs 02, 03, and 06.\n"]
for path in sorted(RUN.glob("*.log")):
    content = normalize(path.read_text())
    path.write_text(content)
    summary.append("\n=== " + path.name + " ===\n")
    in_results = False
    in_reactor_summary = False
    for line in content.splitlines():
        if "Reactor Summary" in line:
            in_reactor_summary = True
        if re.match(r"\[INFO\] Results:\s*$", line):
            in_results = True
        if in_results or in_reactor_summary or "Tests run:" in line or "Running org.xacml4j" in line or re.match(r"\[INFO\] Building ", line):
            summary.append(line + "\n")
        if in_results and "Tests run:" in line:
            in_results = False
(RUN / "surefire-summary.txt").write_text("".join(summary))

blockers = ["# Remaining conformance dispositions\n\n",
            "Final merged XML gates (09 and 10) passed: 857 cases executed, 0 failed, 23 skipped.\n\n",
            "IIIA030 and IIIA330 now reference policy4j-the.4.24 in the manifest and run 030 blockers.\n",
            "IIA022, IIA024, IIIA340 and IIIG301 passed and are enabled with their bug fields cleared.\n",
            "IIIE301 and IIIE303 passed after the reviewed XML lane merge and are now enabled; IIIC001 was already enabled and passed again.\n",
            "IIIG300 stays skipped under the conductor's section 5.48 adjudication; both IIA005 cases stay skipped pending request-error response mapping.\n\n",
            "| Suite | Case | Disposition | Bug | Reason |\n",
            "| --- | --- | --- | --- | --- |\n"]
for row in final_cases:
    if row["outcome"] == "skipped":
        blockers.append("| " + " | ".join(row[key].replace("|", "\\|") for key in ("suite", "case_id", "disposition", "bug_id", "reason")) + " |\n")
(RUN / "conformance-blockers.md").write_text("".join(blockers))

print(json.dumps({"scope": scope["result"], "source_files_modified": len(MODIFIED), "new_tests": len(NEW),
                  "gate": totals, "case_outcome_changes": changes}, indent=2))
