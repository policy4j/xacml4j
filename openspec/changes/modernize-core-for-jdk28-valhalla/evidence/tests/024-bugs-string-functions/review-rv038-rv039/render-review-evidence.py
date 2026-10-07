"""Derive review evidence from completed Maven logs, SHA-256 snapshots and Surefire reports."""
import csv
import difflib
import hashlib
import json
import re
from pathlib import Path

RUN = Path(__file__).resolve().parent
REPO = next(parent for parent in RUN.parents if (parent / "pom.xml").is_file())
URI_ROOT = RUN.parent
HARNESS_ROOT = URI_ROOT.parent / "030-harness-comparators"
HARNESS = HARNESS_ROOT / "review-rv042-rv044"
COUNTS = ("tests", "failures", "errors", "skipped")
JAVA = (
    "xacml-core/src/main/java/org/xacml4j/v30/types/AnyURI.java",
    "xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/PermitOverrides.java",
    "xacml-core/src/test/java/org/xacml4j/v30/types/AnyURILexicalTest.java",
    "xacml-xml/src/test/java/org/xacml4j/v30/xml/AnyURIRequestLexicalTest.java",
    "xacml-json/src/test/java/org/xacml4j/v30/marshal/json/AnyURIRequestLexicalTest.java",
    "xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java",
    "xacml-test/src/main/java/org/xacml4j/v30/XacmlPolicyTestSupport.java",
    "xacml-test/src/test/java/org/xacml4j/v20/Xacml30TestUtilityTest.java",
    "xacml-test/src/test/java/org/xacml4j/v30/XacmlPolicyTestSupportTest.java",
)
MANIFEST = "xacml-conformance-tests/src/test/manifest/conformance-manifest.csv"
OWNED = {*JAVA, MANIFEST}


def read_json(name):
    return json.loads((RUN / (name + ".json")).read_text())


def write_json(path, value):
    path.write_text(json.dumps(value, indent=2) + "\n")


def normalize(text):
    return text.replace(str(REPO), "<repo>").replace(str(Path.home()), "<home>")


def replace_section(path, heading, addition):
    text = path.read_text()
    marker = "\n" + heading + "\n"
    if marker in text:
        text = text.split(marker, 1)[0]
    path.write_text(text.rstrip() + marker + "\n" + addition.rstrip() + "\n")


def table(headers, rows):
    return "\n".join(["| " + " | ".join(headers) + " |",
                      "| " + " | ".join("---" for _ in headers) + " |",
                      *["| " + " | ".join(str(value) for value in row) + " |" for row in rows]]) + "\n"


def counts(result):
    return {key: sum(row["counts"][key] for row in result.values()) for key in COUNTS}


# Completed review logs only; do not rewrite historical numbered receipts.
for folder in (RUN, HARNESS):
    for path in folder.rglob("*"):
        if path.is_file():
            original = path.read_text()
            redacted = normalize(original)
            if redacted != original:
                path.write_text(redacted)

initial = read_json("00-source-hashes")
red = read_json("03-red-source-hashes")
merged = read_json("08-after-conductor-merge")
before = read_json("11-before-full-gate")
after = read_json("11-after-full-gate")
assert before == after, "Sources changed during the final clean gate"
source_paths = [relative for relative in after if "/evidence/" not in relative]
live = {relative: hashlib.sha256((REPO / relative).read_bytes()).hexdigest() for relative in source_paths}
assert all(live[relative] == after[relative] for relative in source_paths), "Live source no longer matches the final gate"
changed = sorted(path for path in set(initial) | set(after) if initial.get(path) != after.get(path))
external = [path for path in changed if path not in OWNED]
assert all(merged.get(path) == after.get(path) for path in external), "Unexplained change after conductor merge"
assert all(initial[path] == red[path] for path in JAVA if "/src/main/" in path), "Production changed before complete red receipt"

headers, assertions, implementation = {}, {}, []
for relative in JAVA:
    current = (REPO / relative).read_text()
    old_file = RUN / ("00-before-" + Path(relative).name)
    if old_file.exists():
        old = old_file.read_text()
        headers[relative] = re.search(r"/\*[\s\S]*?#L%\n \*/", old).group() == re.search(r"/\*[\s\S]*?#L%\n \*/", current).group()
        if "/src/test/" in relative:
            methods = set(re.findall(r"public void (\w+)\(", old))
            now = set(re.findall(r"public void (\w+)\(", current))
            assert methods <= now, "Existing method removed: " + relative
            deleted_assertions = [line for line in difflib.unified_diff(old.splitlines(), current.splitlines())
                                  if line.startswith("-") and not line.startswith("---") and "assert" in line]
            assert not deleted_assertions
            assertions[relative] = {"original_methods": sorted(methods), "all_original_methods_retained": True,
                                     "deleted_assertions": deleted_assertions}
    else:
        old = ""
        template = "xacml-json/src/test/java/org/xacml4j/v30/marshal/json/JsonEntityMarshallingTest.java" if relative.startswith("xacml-json/") else "xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/StringFunctionSemanticsTest.java"
        headers[relative] = re.search(r"/\*[\s\S]*?#L%\n \*/", current).group() == re.search(r"/\*[\s\S]*?#L%\n \*/", (REPO / template).read_text()).group()
    implementation.extend(difflib.unified_diff(old.splitlines(True), current.splitlines(True),
                          fromfile="a/" + relative, tofile="b/" + relative))
assert all(headers.values())
implementation.extend(difflib.unified_diff((RUN / "00-before-conformance-manifest.csv").read_text().splitlines(True),
                      (REPO / MANIFEST).read_text().splitlines(True), fromfile="a/" + MANIFEST, tofile="b/" + MANIFEST))
(RUN / "implementation.patch").write_text("".join(implementation))
protected = [path for path in initial if path.endswith("pom.xml") or
             path.startswith("xacml-conformance-tests/src/test/resources/") or
             path.startswith("xacml-xml-jaxb/src/main/resources/xsd/")]
assert all(initial[path] == after[path] for path in protected)
write_json(RUN / "source-audit.json", {"lane_changed_files": sorted(set(changed) & OWNED),
    "conductor_merge_files": external, "existing_headers_unchanged": headers,
    "original_test_assertions": assertions, "protected_files_unchanged": len(protected),
    "final_gate_source_stable": True, "live_source_matches_gate": True, "production_unchanged_before_run_03": True})

baseline = read_json("00-existing-case-results")
final_cases = read_json("11-final-case-results")
previous = {(row["suite"], row["case_id"]): row for row in baseline["cases"]}
deltas = [{"suite": row["suite"], "case_id": row["case_id"], "before": previous[(row["suite"], row["case_id"])]["outcome"],
           "after": row["outcome"], "cause": "Conductor-reviewed 4.24 XML assignment XPathCategory fix"}
          for row in final_cases["cases"] if row["outcome"] != previous[(row["suite"], row["case_id"])]["outcome"]]
assert {(row["suite"], row["case_id"]) for row in deltas} == {("oasis30", "IIIA030"), ("oasis30", "IIIA330")}
assert all(row["outcome"] in {"passed", "skipped"} for row in final_cases["cases"])
original_rows = list(csv.DictReader((RUN / "00-before-conformance-manifest.csv").open()))
current_rows = list(csv.DictReader((REPO / MANIFEST).open()))
assert len(original_rows) == len(current_rows) == 880
manifest_changes = [old["suite"] + "/" + old["case_id"] for old, new in zip(original_rows, current_rows) if old != new]
assert manifest_changes == ["oasis30/IIIA030", "oasis30/IIIA330"]
write_json(HARNESS / "conformance-case-delta.json", deltas)
with (HARNESS / "conformance-case-delta.csv").open("w", newline="") as out:
    writer = csv.DictWriter(out, fieldnames=list(deltas[0]))
    writer.writeheader()
    writer.writerows(deltas)

modules = read_json("11-full-reactor-results")
totals = counts(modules)
assert totals["failures"] == totals["errors"] == 0
module_table = table(["Module", "Tests run", "Failures", "Errors", "Skipped"],
                     [[module, *[row["counts"][key] for key in COUNTS]] for module, row in modules.items()]
                     + [["Total", *[totals[key] for key in COUNTS]]])
suite_rows = []
for suite in ("oasis20", "oasis30", "rsa2008", "example"):
    rows = [row for row in final_cases["cases"] if row["suite"] == suite]
    disposition = final_cases["dispositions"][suite]
    outcome = final_cases["outcomes"][suite]
    specials = [row for row in rows if row["disposition"] == "special-harness"]
    suite_rows.append([suite, len(rows), disposition.get("executed", 0),
                      str(sum(row["outcome"] == "passed" for row in specials)) + " / " + str(sum(row["outcome"] == "skipped" for row in specials)),
                      disposition.get("skipped", 0), outcome.get("passed", 0), outcome.get("skipped", 0)])
suite_table = table(["Suite", "Rows", "Executed disposition", "Special (passed / skipped)", "Skipped disposition", "Cases passed", "Cases skipped"], suite_rows)

# Exact Surefire lines and Maven Results sections, preserving error receipts too.
logs = sorted([*RUN.glob("*.log"), *HARNESS.glob("*.log")])
summary = []
for log in logs:
    lines = log.read_text().splitlines()
    summary.extend(["", "=== " + str(log.relative_to(REPO)) + " ==="])
    in_results = False
    for line in lines:
        if "Results:" in line:
            in_results = True
        if in_results or "Tests run:" in line or "BUILD SUCCESS" in line or "BUILD FAILURE" in line or "Using auto detected provider" in line:
            summary.append(line)
        if in_results and ("Tests run:" in line or "Reactor Summary" in line):
            in_results = False
summary.extend(["", "Final full-gate derived module counts:", module_table,
                "Final manifest case counts (exclude 21 non-case invocations):", suite_table])
summary_text = "\n".join(summary).strip() + "\n"
(RUN / "surefire-summary.txt").write_text(summary_text)
(HARNESS / "surefire-summary.txt").write_text(summary_text)

counts_note = """Final JDK 21 clean verify: all modules pass. Java 21.0.6 (Oracle GraalVM), Maven 3.9.16.
Raw final gate: evidence/tests/030-harness-comparators/review-rv042-rv044/11-full-reactor-clean-verify.log.
The complete conformance gates are run 08 before the merge (857 passed / 23 skipped), run 10 after the merge and manifest repair, and run 11 clean (859 / 21).
No conformance case failed or regressed. IIIA030 and IIIA330 are the only outcome changes: skipped -> passed, after the conductor's 4.24 merge.
All other case rows are byte-identical; fixtures and expected results are unchanged.
"""
final_focus = counts(read_json("11-uri-final"))
final_focus_other = counts(read_json("11-comparator-final"))
assert final_focus["tests"] + final_focus_other["tests"] == 115
core_classes = modules["xacml-core"]["classes"]
assert core_classes["org.xacml4j.v30.policy.combine.impl.OverrideDecisionTableTest"]["tests"] == 3048
common = counts_note + "\n" + module_table + "\n" + suite_table + """
Surefire invocation totals include disabled and parameterized tests. The conformance module's 901 invocations contain 880 manifest cases and 21 non-case checks. xacml-xml-jaxb has no tests.
The final 115 focused review-test invocations pass. Original test methods and assertions remain; the nine owned Java license blocks are unchanged/template-matched.
source-audit.json distinguishes ten lane source/manifest changes from the conductor merge and proves final gate source stability. Poms and 2,911 vendored fixture/schema files are unchanged.
The 21 inherited case skips remain a coverage limit. Canonical string-from-anyURI lookup still needs a separate identifier fix (StringFunctions.java:276 omits :function:); conversion semantics were verified through its existing binding.
No commit, br, task tick, issue closure, install, fixture edit or expected-result edit was performed.
"""
uri_section = """The URI repair is complete. AnyURI stores URI.create(v) without normalization and overrides lexical equality/hash while keeping OtherValue and the URI getter.
Its repeated String branch now accepts StringVal, restoring the valid anyURI-from-string path in the same scoped class.
PermitOverrides' never-set atLeastOneIndeterminate flag and unreachable branch were removed; the 3,048-row override matrix passes in the final gate.
The conductor merged overridable Value equality/hash at 02:06; this lane did not edit Value.java or the merged engine/mapper files.

| Review | Change | Test / proof |
| --- | --- | --- |
| rv038 MEDIUM / rv044 finding 1 | Original URI spelling and codepoint equality/hash | AnyURILexicalTest: 34; XML and JSON AnyURIRequestLexicalTest: 3 each; Xacml30TestUtilityTest rejects both normalization/case pairs |
| rv039 LOW | Remove dead PermitOverrides flag | OverrideDecisionTableTest: 3,048 final invocations; oasis30/IID300 passes |

Run 03 is the complete pre-fix receipt: 113 tests, 29 failures, 4 errors. Run 04 is green at 113/0/0/0. Two later null-FulfillOn controls bring final focused coverage to 115/0/0/0.
Run 01's test setup/canonical-id errors and the stale comparator snapshot are explained in spec-and-caller-review.md; they are not used as fresh green evidence.

""" + common
harness_section = """The comparator repair is complete. Matches are consumed once, obligation FulfillOn is checked, and omitted 3.0 response effects follow the enclosing Permit/Deny result.
Double response values use numeric equality plus a NaN/NaN oracle match. Engine DoubleVal/equality semantics were not edited by this lane.

| Review | Change | Test / proof |
| --- | --- | --- |
| rv044 finding 1 | Core lexical URI equality flows to typed comparator | differentUriSpellingsRemainRejected: 2 pairs, both directions |
| rv044 finding 3 (supersedes rv042 LOW) | Consume matching obligations; compare FulfillOn | duplicateObligationsCannotReuseOneActualMatch, obligationMultiplicityAndPermutationArePreserved, differentFulfillOnIsRejected |
| Null-field regression found in first gate | Supply the response decision for omitted FulfillOn | omittedFulfillOnUsesTheResponseDecision: Permit and Deny, both directions; both old Xacml30SamplePolicyTest responses pass |
| rv044 finding 4, conductor correction | Signed zeros equal; NaN pairs equal in oracle only | doubleOracleAcceptsSignedZeroAndNan: 5; doubleOracleStillRejectsDifferentValues: 4 |
| Conductor 02:06 follow-up | Re-enable oasis30/IIIA030 and IIIA330 | Passed in run 10 conformance and run 11 clean verify |

The pre-fix complete run 03 comparator subset is 73 tests / 6 failures / 0 errors; URI/round-trip failures are in the sibling receipt.
Run 06 is 14 / 4 / 0 / 0 red; run 07 is green after the omitted-effect adjustment. Result merges duplicate ids, so the duplicate collection test calls the production comparator reflectively; real model objects and public-response controls remain.
Run 09's six errors were a setup mistake (missing reasons), before enumeration. No fixture failed in that run. Reasons were restored and the full suite rerun.
Read comparator-review.md and run-notes.md for the rationale and the missing first run 05/06 log receipts.

""" + common
(RUN / "summary.md").write_text("# rv038/rv039 fixes\n\n" + uri_section)
(HARNESS / "summary.md").write_text("# rv042/rv044 fixes\n\n" + harness_section)
replace_section(URI_ROOT / "summary.md", "## rv038/rv039 fixes", uri_section.replace("source-audit.json", "review-rv038-rv039/source-audit.json")
                + "\nDetailed review receipts: [review-rv038-rv039/summary.md](review-rv038-rv039/summary.md).\n")
replace_section(HARNESS_ROOT / "summary.md", "## rv042/rv044 fixes", harness_section.replace("source-audit.json", "../024-bugs-string-functions/review-rv038-rv039/source-audit.json")
                + "\nDetailed review receipts: [review-rv042-rv044/summary.md](review-rv042-rv044/summary.md).\n")
executed = (RUN / "commands-executed.txt").read_text() + "\n" + (RUN / "11-commands.txt").read_text()
(HARNESS / "command.txt").write_text(executed)
for root, heading in ((URI_ROOT, "rv038/rv039"), (HARNESS_ROOT, "rv042/rv044")):
    replace_section(root / "command.txt", "## " + heading + " fixes", executed + "\nrtk python3 " + str((RUN / "render-review-evidence.py").relative_to(REPO)) + "\n")
    replace_section(root / "surefire-summary.txt", "## " + heading + " fixes", summary_text)

blocker_path = HARNESS_ROOT / "conformance-blockers.md"
replace_section(blocker_path, "## rv042/rv044 verification and 4.24 resolution",
                "oasis30/IIIA030 and IIIA330 now execute and pass in run 10 and the final clean run 11. The conductor-reviewed 4.24 mapper merge retains XPathCategory on assignments. Their prior red receipts above remain historical. The manifest rows are executed and their bug ids are cleared; this lane did not close tracking issues.\n")

# Validate reviewed docs' local links and redact all newly written evidence.
links, path_leaks = [], []
for folder in (RUN, HARNESS):
    for path in folder.rglob("*"):
        if not path.is_file():
            continue
        text = normalize(path.read_text())
        path.write_text(text)
        if str(REPO) in text or str(Path.home()) in text:
            path_leaks.append(str(path.relative_to(REPO)))
        if path.suffix == ".md":
            for target in re.findall(r"\]\(([^)]+)\)", text):
                if not target.startswith(("https://", "http://", "#")):
                    links.append({"document": str(path.relative_to(REPO)), "target": target, "exists": (path.parent / target).exists()})
assert not path_leaks
assert all(row["exists"] for row in links)
write_json(RUN / "final-evidence-check.json", {"module_totals": totals, "focused_tests": 115,
    "case_changes": deltas, "local_links": links, "path_leaks": path_leaks,
    "lane_source_files": len(set(changed) & OWNED), "conductor_source_files": len(external),
    "manifest_row_changes": manifest_changes, "all_other_manifest_rows_unchanged": True})
print(json.dumps({"full_gate": totals, "focused_tests": 115, "case_changes": deltas,
                  "lane_files": len(set(changed) & OWNED), "conductor_files": len(external)}, indent=2))
