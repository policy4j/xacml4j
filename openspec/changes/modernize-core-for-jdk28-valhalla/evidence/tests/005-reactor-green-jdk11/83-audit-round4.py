from pathlib import Path
import hashlib
import json
import re
import subprocess

repo = Path.cwd()
ev = Path(__file__).parent
snapshot = json.loads((ev / "74-round4-before.json").read_text())
allowed = snapshot["allowed_existing"]
protected = {name: digest for name, digest in snapshot["hashes"].items() if name not in allowed}
assert len(protected) == 3542
for name, digest in protected.items():
    assert (repo / name).is_file() and hashlib.sha256((repo / name).read_bytes()).hexdigest() == digest, name
for name, digest in json.loads((ev / "82-round4-file-hashes.json").read_text()).items():
    assert hashlib.sha256((repo / name).read_bytes()).hexdigest() == digest, name
patch = (ev / "82-round4-fixes.patch").read_text()
assert set(re.findall(r"^\+\+\+ b/(.+)$", patch, re.MULTILINE)) == set(allowed)
before = (ev / "74-before-HigherOrderFunctions.java.txt").read_text()
after = (repo / allowed[0]).read_text()
old_guard = '\t\t\tPreconditions.checkArgument(!ref.getEvaluatesTo().isBag(), "The referenced function must return a primitive value");'
new_guard = '\t\t\tif(ref.getEvaluatesTo().isBag()){\n\t\t\t\tspec.validateParametersAndThrow(arguments);\n\t\t\t}'
assert after == before.replace(old_guard, new_guard, 1)
tests = (repo / allowed[1]).read_text()
assert "mapRejectsBagReturningFunctionDuringApplyTypeResolution" in tests
assert "assertEquals(validationError.getClass(), resolutionError.getClass());" in tests
assert "assertEquals(validationError.getMessage(), resolutionError.getMessage());" in tests
assert "assertEquals(StatusCodeId.SYNTAX_ERROR, resolutionError.getStatus().getStatusCode().getValue());" in tests
assert "assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, invocationError.getStatus().getStatusCode().getValue());" in tests
assert "assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, status.getValue().getStatusCode().getValue());" in tests
assert "status.reset();" in tests and "status.hasCaptured()" in tests

red = json.loads((ev / "75-round4-red-tests.json").read_text())
assert red["production_matches_before_snapshot"]
assert [sum(result["counts"][i] for result in red["results"].values()) for i in range(4)] == [63,2,0,0]
for prefix in ("77-round4-green", "78-reactor-verify-round4", "80-reactor-verify-round4-retry"):
    green = json.loads((ev / (prefix + "-tests.json")).read_text())
    assert [sum(result["counts"][i] for result in green["results"].values()) for i in range(4)] == [63,0,0,0]
    assert green["production_hash"] == hashlib.sha256((repo / allowed[0]).read_bytes()).hexdigest()
for prefix, failures in (("78",2), ("80",1)):
    counts = json.loads((ev / (prefix + "-module-counts.json")).read_text())
    assert counts["xacml-test"] is None and counts["xacml-conformance-tests"] is None
    assert [sum(row[i] for row in counts.values() if row is not None) for i in range(4)] == [875,failures,0,9]
    assert counts["xacml-core"] == [762,0,0,4] and counts["xacml-pdp"] == [74,failures,0,4]
    assert "NOT RUN" in (ev / (prefix + "-unrun-modules.json")).read_text()
properties = json.loads((ev / "80-jdk-properties.json").read_text())
assert len(properties) == 4 and all(value["java.version"] == "11.0.32.1" for value in properties.values())
assert 'openjdk version "11.0.32.1" 2026-08-18 LTS' in (ev / "81-jdk-version.txt").read_text()
for name, expected_failures in (("79-repository-race-proof.json",2), ("80-repository-race-proof.json",1)):
    proof = json.loads((ev / name).read_text())
    sources = {key: value for key, value in proof.items() if key.endswith(".java")}
    assert len(sources) == 2 and all(item["matches_api_base"] and item["current_sha256"] == item["base_sha256"] for item in sources.values())
    assert proof["failed_run"]["counts"] == [5,expected_failures,0,1]
assert (ev / "80-existing-case-exclusions.json").read_text() == (ev / "71-existing-case-exclusions.json").read_text()
summary = (ev / "summary.md").read_text()
review = (ev / "round4-review-fixes.md").read_text()
blockers = (ev / "conformance-blockers.md").read_text()
assert summary.startswith("# Task 1.3 continued: Round 4 LOW fixed; full gate BLOCKED")
assert "## Round 4 review fixes" in summary and "## Round 4 review fixes" in (ev / "review-fixes.md").read_text()
assert "| xacml-pdp | 74 | 1 | 0 | 4 | FAILURE |" in summary
assert "| xacml-conformance-tests | NOT RUN |" in summary and "| Executed total | 875 | 1 | 0 | 9 | BUILD FAILURE |" in summary
assert "policy4j-the.4.12" in review and "DEFERRED" in review and "INFERRED" in review
assert "NOT RUN" in blockers and "Proposal OPEN" in blockers and "UNVERIFIED, unapplied" in blockers
proposal = (ev / "round4-gate-blocker-proposal.md").read_text()
assert "PROPOSED ONLY" in proposal and "UNVERIFIED" in proposal and "no PDP source or" in proposal
proposed_patch = (ev / "required-repository-listener-barrier-fix.patch").read_text()
assert re.findall(r"^\+\+\+ b/(.+)$", proposed_patch, re.MULTILINE) == ["xacml-pdp/src/test/java/org/xacml4j/v30/spi/repository/InMemoryPolicyRepositoryTest.java"]
assert "addedPolicies.await(5, TimeUnit.SECONDS)" in proposed_patch and "Thread.sleep" not in proposed_patch
surefire = (ev / "surefire-summary.txt").read_text().split("# Round 4 review fixes (after 1.3i)",1)[1]
final_results = surefire.split("Run: 80-reactor-verify-round4-retry.txt",1)[1]
assert final_results.count("[INFO] Results:") == 4
assert "[ERROR] Tests run: 74, Failures: 1, Errors: 0, Skipped: 4" in final_results
assert "BUILD FAILURE" in final_results and "conformance NOT RUN" in final_results
assert "/83-audit-round4.py" in (ev / "command.txt").read_text()
diagnostics = json.loads((ev / "compile-diagnostics.json").read_text())
for name in ("75-round4-red.txt", "77-round4-green.txt", "78-reactor-verify-round4.txt", "80-reactor-verify-round4-retry.txt"):
    assert not diagnostics[name]
for name in ("summary.md", "review-fixes.md", "surefire-summary.txt", "command.txt", "conformance-blockers.md", "scope-check.txt"):
    assert str(repo) not in (ev / name).read_text(), name
for command in (["rtk", "git", "diff", "--check", "--", *allowed], ["rtk", "git", "diff", "--cached", "--name-only"]):
    result = subprocess.run(command, capture_output=True, text=True)
    assert result.returncode == 0 and not result.stdout.strip() and not result.stderr.strip(), command
lines = ["PASS: LOW map resolution red 63/2/0/0 -> green 63/0/0/0; both aliases/runtime modes",
         "PASS: exact syntax-error validation and processing-error invocation/Condition assertions",
         "BLOCKER: full gates 78 875/2/0/9 and 80 875/1/0/9 fail at PDP listener verification",
         "NOT RUN: test-support/conformance skipped; stale downstream reports excluded",
         "PASS: repository source/test match a8a90d46; race inference/proposal recorded; patch unapplied",
         "PASS: 3542 protected files unchanged; two authorized changes; headers and all loops preserved",
         "PASS: policy4j-the.4.12 deferred unchanged; scope diff clean; no staged changes",
         "PASS: JDK 11 receipts, raw failures, exact commands and normalized summaries recorded"]
(ev / "83-final-round4-evidence-audit.txt").write_text("\n".join(lines) + "\n")
print("\n".join(lines))
