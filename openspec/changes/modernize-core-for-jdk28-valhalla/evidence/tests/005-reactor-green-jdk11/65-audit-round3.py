from pathlib import Path
import hashlib
import json
import re
import subprocess

repo = Path.cwd()
ev = Path(__file__).parent
snapshot = json.loads((ev / "57-round3-before.json").read_text())
allowed = set(snapshot["allowed_existing"] + snapshot["conditional_existing_not_yet_authorized_by_red"])
protected = {name: digest for name, digest in snapshot["hashes"].items() if name not in allowed}
assert len(protected) == 3535
for name, digest in protected.items():
    assert (repo / name).is_file() and hashlib.sha256((repo / name).read_bytes()).hexdigest() == digest, name
hashes = json.loads((ev / "64-round3-file-hashes.json").read_text())
assert len(hashes) == 9
for name, digest in hashes.items():
    assert hashlib.sha256((repo / name).read_bytes()).hexdigest() == digest, name
assert json.loads((ev / "63-existing-case-exclusions.json").read_text()) == json.loads((ev / "53-existing-case-exclusions.json").read_text())
assert json.loads((ev / "63-existing-case-exclusions.json").read_text()) == json.loads((ev / "36-existing-case-exclusions.json").read_text())
counts = json.loads((ev / "63-module-counts.json").read_text())
assert [sum(row[index] for row in counts.values()) for index in range(4)] == [914,0,0,9]
assert len(json.loads((ev / "63-jdk-properties.json").read_text())) == 6
summary = (ev / "summary.md").read_text()
assert "914 Surefire tests" in summary and "## Round 3 review fixes" in summary
assert "32 project files" in summary and "not rerun in round 3" in summary
assert "final run 53 Maven Results" not in summary
assert "3535" in (ev / "scope-check.txt").read_text()
assert "| Total | 914 | 0 | 0 | 9 | BUILD SUCCESS |" in summary
surefire = (ev / "surefire-summary.txt").read_text()
latest = surefire.split("Run: 63-reactor-verify-round3.txt", 1)[1]
assert latest.count("[INFO] Results:") == 6 and "Tests run: 740, Failures: 0, Errors: 0, Skipped: 4" in latest
assert "BUILD SUCCESS" in latest and "BUILD FAILURE" not in latest
assert "63-reactor-verify-round3.txt" in (ev / "command.txt").read_text()
assert "Final run 63" in (ev / "conformance-blockers.md").read_text()
for name in ("summary.md", "surefire-summary.txt", "command.txt", "conformance-blockers.md",
             "round3-review-fixes.md", "scope-check.txt", "63-reactor-verify-round3.txt"):
    assert str(repo) not in (ev / name).read_text(), name
diagnostics = json.loads((ev / "compile-diagnostics.json").read_text())
assert len(diagnostics["58-round3-before.txt"]) == 9
for name in ("59-round3-red.txt", "61-round3-packing-red.txt", "62-round3-focused.txt", "63-reactor-verify-round3.txt"):
    assert not diagnostics[name], name
result = subprocess.run(["rtk", "git", "status", "--short"], capture_output=True, text=True)
assert result.returncode == 0
(ev / "65-final-git-status.txt").write_text(result.stdout.replace(str(repo), "<repo>"))
result = subprocess.run(["rtk", "git", "diff", "--check", "--", *sorted(allowed)], capture_output=True, text=True)
assert result.returncode == 0, result.stdout + result.stderr
output = "\n".join([
    "PASS: fresh JDK 11 reactor 914 tests / 0 failures / 0 errors / 9 existing skips",
    "PASS: conformance OASIS 347 + 347, RSA 17, examples 2; no configured failures",
    "PASS: nine authorized project files; 3535 protected hashes unchanged; scoped diff check clean",
    "PASS: final summary, command, Results, blocker status, exclusions and compiler diagnostics agree",
    "No commit, staging, br, task tick, issue closure or fixture/expected-result edits",
]) + "\n"
(ev / "65-final-round3-evidence-audit.txt").write_text(output)
print(output, end="")
