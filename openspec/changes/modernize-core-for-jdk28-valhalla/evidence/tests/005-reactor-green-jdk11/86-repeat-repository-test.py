from pathlib import Path
import hashlib
import json
import os
import re
import shlex
import subprocess
import sys
import xml.etree.ElementTree as ET

repo = Path.cwd()
ev = Path(__file__).parent
phase = sys.argv[1]
assert phase in ("before", "after")
run_dir = ev / ("85-repository-before" if phase == "before" else "87-repository-after")
assert not run_dir.exists(), "Preserve previous loop evidence; choose a new run directory"
run_dir.mkdir()
source = repo / "xacml-pdp/src/test/java/org/xacml4j/v30/spi/repository/InMemoryPolicyRepositoryTest.java"
source_hash = hashlib.sha256(source.read_bytes()).hexdigest()
baseline = json.loads((ev / "84-round4-gate-before.json").read_text())
assert (source_hash == baseline["hashes"][str(source.relative_to(repo))]) == (phase == "before")
assert os.environ.get("JAVA_HOME"), "Select JDK 11 with the wrapper command"
version = subprocess.run(["rtk", "proxy", str(Path(os.environ["JAVA_HOME"]) / "bin/java"), "-version"], capture_output=True, text=True)
assert version.returncode == 0
version_text = version.stdout + version.stderr
assert 'version "11.' in version_text, version_text
(run_dir / "jdk-version.txt").write_text(version_text.replace(str(repo), "<repo>"))
report = repo / "xacml-pdp/target/surefire-reports/TEST-org.xacml4j.v30.spi.repository.InMemoryPolicyRepositoryTest.xml"
ansi = re.compile(r"\x1b\[[0-9;]*[A-Za-z]")
results = []
commands = [f'JAVA_HOME="$(jenv prefix 11)" rtk python3 {ev}/86-repeat-repository-test.py {phase}', "# Each child below inherits that JAVA_HOME; no Maven install or stale installed dependency is used."]
for iteration in range(1, 31):
    assert hashlib.sha256(source.read_bytes()).hexdigest() == source_hash, "Source changed during stability loop"
    log = run_dir / f"{iteration:02d}.txt"
    previous_mtime = report.stat().st_mtime_ns if report.exists() else None
    command = ["rtk", "mvn", "-Dlicense.skipUpdateLicense=true", "-pl", "xacml-pdp", "-am", "-Dtest=InMemoryPolicyRepositoryTest", "-Dsurefire.failIfNoSpecifiedTests=false", "test", "-l", str(log)]
    commands.append(shlex.join(command))
    completed = subprocess.run(command, capture_output=True, text=True)
    raw = log.read_text() if log.exists() else ""
    raw = ansi.sub("", raw).replace(str(repo), "<repo>")
    log.write_text(raw)
    (run_dir / f"{iteration:02d}-console.txt").write_text(ansi.sub("", completed.stdout + completed.stderr).replace(str(repo), "<repo>"))
    item = {"iteration": iteration, "command": shlex.join(command), "source_sha256": source_hash, "exit_code": completed.returncode, "fresh_report": False, "counts": None, "failures": [], "java_version": None}
    if report.exists() and report.stat().st_mtime_ns != previous_mtime:
        item["fresh_report"] = True
        root = ET.parse(report).getroot()
        item["counts"] = {key: int(root.get(key, "0")) for key in ("tests", "failures", "errors", "skipped")}
        item["java_version"] = root.find("./properties/property[@name='java.version']").get("value")
        assert item["java_version"].startswith("11."), item["java_version"]
        for case in root.findall("testcase"):
            for tag in ("failure", "error"):
                for failure in case.findall(tag):
                    item["failures"].append({"test": case.get("name"), "kind": tag, "message": failure.get("message"), "text": (failure.text or "").replace(str(repo), "<repo>")})
        for property_node in root.findall("properties"):
            root.remove(property_node)
        for parent in root.iter():
            for child in list(parent):
                if child.tag in ("system-out", "system-err"):
                    parent.remove(child)
        xml = ET.tostring(root, encoding="unicode")
        (run_dir / f"{iteration:02d}-InMemoryPolicyRepositoryTest.xml").write_text(xml.replace(str(repo), "<repo>") + "\n")
    item["passed"] = completed.returncode == 0 and item["fresh_report"] and item["counts"]["failures"] == 0 and item["counts"]["errors"] == 0 and "BUILD SUCCESS" in raw
    item["compile_diagnostics"] = [line for line in raw.splitlines() if "[ERROR]" in line and (".java:[" in line or "COMPILATION ERROR" in line)]
    status = subprocess.run(["rtk", "git", "status", "--short"], capture_output=True, text=True)
    assert status.returncode == 0
    commands.append("rtk git status --short")
    (run_dir / f"{iteration:02d}-git-status.txt").write_text(status.stdout.replace(str(repo), "<repo>"))
    results.append(item)
    (run_dir / "iterations.json").write_text(json.dumps(results, indent=2).replace(str(repo), "<repo>") + "\n")
    passed = sum(result["passed"] for result in results)
    print(f"{phase} {iteration:02d}/30: {'PASS' if item['passed'] else 'FAIL'}; cumulative {passed} pass / {len(results) - passed} fail; class {item['counts']}", flush=True)
assert hashlib.sha256(source.read_bytes()).hexdigest() == source_hash
totals = {key: sum(result["counts"][key] for result in results if result["counts"] is not None) for key in ("tests", "failures", "errors", "skipped")}
failure_frequency = {}
for result in results:
    for failure in result["failures"]:
        key = failure["test"]
        failure_frequency[key] = failure_frequency.get(key, 0) + 1
passed = sum(result["passed"] for result in results)
summary = {"phase": phase, "runs": len(results), "passed": passed, "failed": len(results) - passed, "fresh_reports": sum(result["fresh_report"] for result in results), "reported_totals": totals, "executed_test_methods": totals["tests"] - totals["skipped"], "failure_frequency": failure_frequency, "source_sha256": source_hash, "java_versions": sorted({result["java_version"] for result in results if result["java_version"] is not None}), "compile_diagnostics": [line for result in results for line in result["compile_diagnostics"]]}
(run_dir / "summary.json").write_text(json.dumps(summary, indent=2) + "\n")
(run_dir / "command.txt").write_text("\n".join(commands).replace(str(repo), "<repo>") + "\n")
print(json.dumps(summary, indent=2), flush=True)
sys.exit(0 if passed == 30 else 1)
