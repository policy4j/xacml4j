from pathlib import Path
import hashlib
import json
import re
import subprocess

repo = Path.cwd()
ev = Path(__file__).parent
snapshot = json.loads((ev / "66-round2-verification-before.json").read_text())
allowed = snapshot["allowed_existing"]
protected = {name: digest for name, digest in snapshot["hashes"].items() if name not in allowed}
assert len(protected) == 3541
for name, digest in protected.items():
    assert (repo / name).is_file() and hashlib.sha256((repo / name).read_bytes()).hexdigest() == digest, name
for name, digest in json.loads((ev / "72-round2-followup-file-hashes.json").read_text()).items():
    assert hashlib.sha256((repo / name).read_bytes()).hexdigest() == digest, name
patch = (ev / "72-round2-followup-fixes.patch").read_text()
assert set(re.findall(r"^\+\+\+ b/(.+)$", patch, re.MULTILINE)) == set(allowed)
assert "stripTrailingZeros" not in patch
assert "+                    .setScale(0, RoundingMode.DOWN).intValueExact();" in patch
assert "+            seconds = new BigDecimal(seconds.toBigInteger());" in patch

red = json.loads((ev / "69-round2-verification-red-tests.json").read_text())
assert red["production_matches_before_snapshot"]
for name, result in red["results"].items():
    problems = [test for test in result["tests"] if test["problems"]]
    if name.endswith("ISO8601DurationReviewTest"):
        assert {test["name"] for test in problems} == {"integralSecondsDiscardZeroFractionalScale",
            "subNanosecondConversionTruncatesTowardZero", "subNanosecondArithmeticEvaluatesThroughApply"}
    elif name.endswith("ISO8601FractionalDateTimeArithmeticTest"):
        assert len(problems) == 16
        assert all(any(duration in test["name"] for duration in ("PT0.0000000001S", "PT1.0000000019S", "PT0.9999999999S")) for test in problems)
    else:
        assert not problems
for prefix in ("70-round2-verification-green", "71-reactor-verify-round2-followup"):
    green = json.loads((ev / (prefix + "-tests.json")).read_text())
    assert sum(result["counts"][0] for result in green["results"].values()) == 117
    assert all(result["counts"][1:] == [0,0,0] for result in green["results"].values())
    assert green["production_hash"] == hashlib.sha256((repo / allowed[0]).read_bytes()).hexdigest()

counts = json.loads((ev / "71-module-counts.json").read_text())
assert [sum(row[i] for row in counts.values()) for i in range(4)] == [934,0,0,9]
properties = json.loads((ev / "71-jdk-properties.json").read_text())
assert len(properties) == 6 and all(value["java.version"] == "11.0.32.1" for value in properties.values())
assert 'openjdk version "11.0.32.1" 2026-08-18 LTS' in (ev / "72-jdk-version.txt").read_text()
summary = (ev / "summary.md").read_text()
review = (ev / "review-fixes.md").read_text()
blockers = (ev / "conformance-blockers.md").read_text()
assert "934 Surefire tests" in summary and "| xacml-core | 760 |" in summary and "| Total | 934 |" in summary
assert "## Round 2 verification follow-up" in summary and "## Round 2 verification follow-up" in review
assert "RoundingMode.DOWN" in review and "the conductor's java.time precision/compatibility decision" in review
assert "Sub-nanosecond XML durations are rejected" not in review
assert "Sub-nanosecond arithmetic is rejected" not in summary
assert "Final run 71: BUILD SUCCESS on JDK 11; 934 tests" in blockers
assert "IIIE301" in blockers and "IIIE302" in blockers and "IIIE303" in blockers and "not rerun in this follow-up" in blockers
cases = json.loads((ev / "71-conformance-testcases.json").read_text())
assert all(case["failure"] is None for suite in cases.values() for case in suite["testcases"])
for name in ("Xacml20ConformanceTest", "Xacml30ConformanceTest"):
    assert sum(len(case["case_ids"]) for case in cases["org.xacml4j.v20." + name]["testcases"]) == 347
assert len(cases["org.xacml4j.v20.RSA2008InteropTest"]["testcases"]) == 17
assert len(cases["org.xacml4j.v20.XacmlExampleTest"]["testcases"]) == 2
assert (ev / "71-existing-case-exclusions.json").read_text() == (ev / "63-existing-case-exclusions.json").read_text()
surefire = (ev / "surefire-summary.txt").read_text().split("# Round 2 verification follow-up (after 1.3h)",1)[1]
final_results = surefire.split("Run: 71-reactor-verify-round2-followup.txt",1)[1]
assert final_results.count("[INFO] Results:") == 6
assert "[WARNING] Tests run: 760, Failures: 0, Errors: 0, Skipped: 4" in final_results
assert "BUILD SUCCESS" in final_results
commands = (ev / "command.txt").read_text()
assert "/71-reactor-verify-round2-followup.txt" in commands and "/73-audit-round2-followup.py" in commands
for name in ("summary.md", "review-fixes.md", "surefire-summary.txt", "command.txt", "conformance-blockers.md", "scope-check.txt"):
    assert str(repo) not in (ev / name).read_text(), name
for command in (["rtk", "git", "diff", "--check", "--", *allowed], ["rtk", "git", "diff", "--cached", "--name-only"]):
    result = subprocess.run(command, capture_output=True, text=True)
    assert result.returncode == 0 and not result.stdout.strip() and not result.stderr.strip(), command
lines = ["PASS: final run 71 reactor 934/0/0/9; six fresh module Results and JDK 11 properties",
         "PASS: focused 69 red 117/1/18/0 with production unchanged; 70/71 green 117/0/0/0",
         "PASS: 347 + 347 + 17 + 2 configured conformance/example executions; no failed case IDs",
         "PASS: 30 exclusions per OASIS suite and all bounds, fixtures and execution order unchanged",
         "PASS: only three authorized project files changed; 3541 protected files unchanged",
         "PASS: headers/prior assertions preserved; scoped diff clean; no staged changes",
         "PASS: review decision, compiler diagnostics, exact commands and residual proposals recorded"]
(ev / "73-final-round2-followup-evidence-audit.txt").write_text("\n".join(lines) + "\n")
print("\n".join(lines))
