from pathlib import Path
import hashlib
import json
import re
import subprocess
import xml.etree.ElementTree as ET

repo = Path.cwd()
ev = Path(__file__).parent
snapshot = json.loads((ev / "84-round4-gate-before.json").read_text())
allowed = snapshot["allowed_existing"][0]
digest = hashlib.sha256((repo / allowed).read_bytes()).hexdigest()
protected = {name: value for name, value in snapshot["hashes"].items() if name != allowed}
assert len(protected) == 3543
for name, value in protected.items():
    assert (repo / name).is_file() and hashlib.sha256((repo / name).read_bytes()).hexdigest() == value, name
inventory = json.loads((ev / "90-listener-barrier-inventory.json").read_text())
assert len(inventory) == 4
test_names = {item["test"] for item in inventory}
assert [item["expected_callbacks"] for item in inventory] == [4,2,1,4]
for document in ("summary.md", "review-fixes.md", "round4-gate.md"):
    text = (ev / document).read_text()
    assert "## Round 4 gate" in text
    for item in inventory:
        assert f":{item['old_verify_line']} -> :{item['await_line']} / :{item['new_verify_line']}" in text, (document,item)
    assert "26 pass / 4 fail" in text and "30 pass / 0 fail" in text
    assert "936 tests / 0 failures / 0 errors / 9 existing skips" in text
    assert "policy4j-the.4.12" in text
command_text = (ev / "command.txt").read_text()
summary_text = (ev / "surefire-summary.txt").read_text()
phase_counts = {}
for directory in ("85-repository-before", "87-repository-after"):
    summary = json.loads((ev / directory / "summary.json").read_text())
    iterations = json.loads((ev / directory / "iterations.json").read_text())
    assert summary["runs"] == summary["fresh_reports"] == len(iterations) == 30
    if directory.startswith("85"):
        assert summary["source_sha256"] == snapshot["hashes"][allowed]
        assert (summary["passed"],summary["failed"]) == (26,4)
    else:
        assert summary["source_sha256"] == digest and (summary["passed"],summary["failed"]) == (30,0)
    phase_counts[directory] = {key: summary[key] for key in ("passed","failed","reported_totals")}
    for item in iterations:
        assert item["fresh_report"] and item["java_version"] == "11.0.32.1" and not item["compile_diagnostics"]
        prefix = f"{item['iteration']:02d}"
        report = ET.parse(ev / directory / (prefix + "-InMemoryPolicyRepositoryTest.xml")).getroot()
        assert {case.get("name") for case in report.findall("testcase")} == test_names
        assert [int(report.get(key)) for key in ("tests","failures","errors","skipped")] == [item["counts"][key] for key in ("tests","failures","errors","skipped")]
        assert f"Run: {directory}/{prefix}.txt" in summary_text
        assert f"/{directory}/{prefix}.txt" in command_text
    assert len(list((ev / directory).glob("*-InMemoryPolicyRepositoryTest.xml"))) == 30
case_totals = json.loads((ev / "90-conformance-case-totals.json").read_text())
for prefix in ("88","89"):
    receipt = json.loads((ev / (prefix + "-gate-receipt.json")).read_text())
    assert receipt["source_sha256"] == digest and receipt["build_success"] and receipt["totals"] == [936,0,0,9]
    assert len(receipt["fresh_report_modules"]) == 6
    assert f"Run: {receipt['log']}" in summary_text and receipt["log"] in command_text
    totals = case_totals[prefix]
    assert totals["Xacml20ConformanceTest"]["executed"] == totals["Xacml30ConformanceTest"]["executed"] == 347
    assert totals["RSA2008InteropTest"]["executed_test_methods"] == 17
    assert totals["XacmlExampleTest"]["executed_test_methods"] == 2
    assert all(value["failed"] == 0 and not value["failed_case_ids"] for value in totals.values())
    report = ET.parse(ev / (prefix + "-InMemoryPolicyRepositoryTest.xml")).getroot()
    assert [int(report.get(key)) for key in ("tests","failures","errors","skipped")] == [5,0,0,1]
    assert {case.get("name") for case in report.findall("testcase") if case.find("skipped") is None} == test_names
assert case_totals["88"] == case_totals["89"]
assert "APPROVED / APPLIED / VERIFIED" in (ev / "round4-gate-blocker-proposal.md").read_text()
assert "GREEN twice" in (ev / "conformance-blockers.md").read_text()
assert (ev / "surefire-summary.txt").read_text().startswith((ev / "84-before-round4-gate-surefire-summary.txt").read_text().rstrip())
assert (ev / "command.txt").read_text().startswith((ev / "84-before-round4-gate-command.txt").read_text().rstrip())
assert 'version "11.0.32.1"' in (ev / "90-jdk-version.txt").read_text()
current_evidence = [ev / name for name in ("summary.md","review-fixes.md","round4-gate.md","surefire-summary.txt","command.txt","scope-check.txt","conformance-blockers.md","round4-gate-blocker-proposal.md")]
for prefix in ("85-repository-before", "87-repository-after"):
    current_evidence += [path for path in (ev / prefix).rglob("*") if path.is_file()]
current_evidence += [path for pattern in ("88-*.txt","88-*.json","88-*.xml","89-*.txt","89-*.json","89-*.xml","90-*.txt","90-*.json","90-*.patch") for path in ev.glob(pattern)]
for path in current_evidence:
    assert str(repo) not in path.read_text(), ("Checkout path not normalized",path)
result = subprocess.run(["rtk","git","diff","--check","--",allowed],capture_output=True,text=True)
assert result.returncode == 0, result.stdout + result.stderr
cached = subprocess.run(["rtk","git","diff","--cached","--name-only"],capture_output=True,text=True)
assert cached.returncode == 0 and not cached.stdout.strip()
receipt = {"result":"PASS", "protected_files_unchanged":len(protected), "only_project_file_changed":allowed, "source_sha256":digest, "listener_tests_with_barriers":len(inventory), "stability":phase_counts, "full_gate_totals":{"88":[936,0,0,9],"89":[936,0,0,9]}, "conformance_failed_case_ids":[], "existing_exclusions_per_oasis_suite":30, "evidence_files_checked":len(current_evidence), "scoped_diff_check":"PASS", "staged_files":0}
(ev / "91-final-round4-gate-evidence-audit.json").write_text(json.dumps(receipt,indent=2) + "\n")
print(json.dumps(receipt,indent=2))
