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
snapshot = json.loads((ev / "84-round4-gate-before.json").read_text())
files = snapshot["allowed_existing"]
assert len(files) == 1
source = repo / files[0]
before = (ev / "84-before-InMemoryPolicyRepositoryTest.java.txt").read_text()
after = source.read_text()
digest = hashlib.sha256(source.read_bytes()).hexdigest()
protected = {name: value for name, value in snapshot["hashes"].items() if name not in files}
assert len(protected) == 3543
for name, value in protected.items():
    assert (repo / name).is_file() and hashlib.sha256((repo / name).read_bytes()).hexdigest() == value, name
current = set()
for module in repo.glob("xacml-*"):
    if module.is_dir():
        current.update(str(path.relative_to(repo)) for path in (module / "src").rglob("*") if path.is_file())
assert not current - set(snapshot["hashes"]), current - set(snapshot["hashes"])
header = re.compile(r"/\*.*?#L%.*?\*/", re.DOTALL)
assert header.search(before).group() == header.search(after).group()
patch = list(difflib.unified_diff(before.splitlines(keepends=True), after.splitlines(keepends=True), fromfile="a/" + files[0], tofile="b/" + files[0]))
assert all(line[1:].rstrip("\n") == line[1:].rstrip() for line in patch if line.startswith("+") and not line.startswith("+++"))
(ev / "90-round4-gate-fixes.patch").write_text("".join(patch))
(ev / "90-round4-gate-file-hashes.json").write_text(json.dumps({files[0]: {"before": snapshot["hashes"][files[0]], "after": digest}}, indent=2) + "\n")

def methods(text):
    result = {}
    for match in re.finditer(r"\tpublic void (test\w+)\([^)]*\)(?: throws Exception)?\s*\{", text):
        depth, end = 1, match.end()
        while depth:
            depth += (text[end] == "{") - (text[end] == "}")
            end += 1
        result[match.group(1)] = (match.start(), text[match.start():end])
    return result

old_methods, new_methods = methods(before), methods(after)
expected_callbacks = {"testOnePolicyDifferentVersion": 4, "testAddRemove": 2, "testAddPolicyWithTheSameIdAndSameVersion": 1, "testFindAllPoliciesWithTheSameId": 4}
assert set(old_methods) == set(new_methods) == set(expected_callbacks)
barriers = []
for name, expected in expected_callbacks.items():
    old_start, old_body = old_methods[name]
    new_start, new_body = new_methods[name]
    assert new_body.count(f"CountDownLatch callbacks = listenerCallbacks({expected});") == 1
    assert len(re.findall(r'assertTrue\("Timed out waiting for [^"\n]+", callbacks.await\(5, TimeUnit.SECONDS\)\);\n\t\tc.verify\(\);', new_body)) == 1
    original_body = re.sub(r'\t\tCountDownLatch callbacks = listenerCallbacks\(\d\);\n', '', new_body)
    original_body = re.sub(r'\t\tassertTrue\("Timed out waiting for [^"\n]+", callbacks.await\(5, TimeUnit.SECONDS\)\);\n', '', original_body)
    if name == "testAddPolicyWithTheSameIdAndSameVersion":
        original_body = original_body.replace("() throws Exception", "()", 1)
    assert original_body == old_body, name
    barriers.append({"test": name, "expected_callbacks": expected, "old_verify_line": before[:old_start + old_body.index("c.verify()")].count("\n") + 1, "await_line": after[:new_start + new_body.index('assertTrue("Timed out')].count("\n") + 1, "new_verify_line": after[:new_start + new_body.index("c.verify()")].count("\n") + 1, "original_body_preserved_except_barrier_and_checked_exception": True})
assert before.count("c.verify();") == after.count("c.verify();") == 4
assert after.count("private CountDownLatch listenerCallbacks(int expected)") == 1
for callback in ("policyAdded", "policyRemoved", "policySetAdded", "policySetRemoved"):
    assert f"l.{callback}(p);\n\t\t\t\tcallbacks.countDown();" in after
(ev / "90-listener-barrier-inventory.json").write_text(json.dumps(barriers, indent=2) + "\n")

baseline = json.loads((ev / "85-repository-before/summary.json").read_text())
green = json.loads((ev / "87-repository-after/summary.json").read_text())
assert baseline["runs"] == baseline["fresh_reports"] == green["runs"] == green["fresh_reports"] == 30
assert (baseline["passed"], baseline["failed"]) == (26,4)
assert baseline["source_sha256"] == snapshot["hashes"][files[0]]
assert green["source_sha256"] == digest and (green["passed"], green["failed"]) == (30,0)
assert baseline["reported_totals"] == {"tests":120,"failures":4,"errors":0,"skipped":0}
assert green["reported_totals"] == {"tests":120,"failures":0,"errors":0,"skipped":0}
assert baseline["failure_frequency"] == {"testAddRemove":1,"testFindAllPoliciesWithTheSameId":3}
assert baseline["java_versions"] == green["java_versions"] == ["11.0.32.1"]
for directory, summary in (("85-repository-before", baseline), ("87-repository-after", green)):
    iterations = json.loads((ev / directory / "iterations.json").read_text())
    assert len(iterations) == 30 and not summary["compile_diagnostics"]
    for item in iterations:
        assert item["fresh_report"] and item["source_sha256"] == summary["source_sha256"]
        log = (ev / directory / f"{item['iteration']:02d}.txt").read_text()
        assert "BUILD SUCCESS" in log if item["passed"] else "BUILD FAILURE" in log
        assert item["counts"]["tests"] == 4 and item["counts"]["errors"] == item["counts"]["skipped"] == 0
        for failure in item["failures"]:
            assert "Expectation failure on verify" in failure["message"] and "PolicyRepositoryListener.policy" in failure["message"]
            assert "MocksControl.verify" in failure["text"]

expected_counts = {"xacml":[0,0,0,0],"xacml-core":[762,0,0,4],"xacml-xml-jaxb":[0,0,0,0],"xacml-xml":[34,0,0,0],"xacml-json":[5,0,0,1],"xacml-pdp":[74,0,0,4],"xacml-test":[20,0,0,0],"xacml-conformance-tests":[41,0,0,0]}
diagnostics = json.loads((ev / "compile-diagnostics.json").read_text())
previous_cases = json.loads((ev / "71-conformance-testcases.json").read_text())
case_summary = {}
for prefix, log_name in (("88", "88-reactor-verify-round4-gate.txt"), ("89", "89-reactor-verify-round4-gate-repeat.txt")):
    receipt = json.loads((ev / (prefix + "-gate-receipt.json")).read_text())
    assert receipt["build_success"] and receipt["totals"] == [936,0,0,9] and receipt["module_counts"] == expected_counts
    assert receipt["source_sha256"] == digest and not receipt["conformance_not_run"]
    assert not diagnostics[log_name]
    jdk = json.loads((ev / (prefix + "-jdk-properties.json")).read_text())
    assert len(jdk) == 6 and all(value["java.version"] == "11.0.32.1" for value in jdk.values())
    cases = json.loads((ev / (prefix + "-conformance-testcases.json")).read_text())
    assert set(cases) == set(previous_cases)
    counts = {}
    for name, suite in cases.items():
        assert suite["counts"]["failures"] == suite["counts"]["errors"] == suite["counts"]["skipped"] == "0"
        emitted = [case_id for test in suite["testcases"] for case_id in test["case_ids"]]
        old_emitted = [case_id for test in previous_cases[name]["testcases"] for case_id in test["case_ids"]]
        assert emitted == old_emitted and all(test["failure"] is None for test in suite["testcases"])
        if name.endswith(("Xacml20ConformanceTest", "Xacml30ConformanceTest")):
            assert len(emitted) == 347 and emitted.count("IIIG006") == 2
            counts[name.split(".")[-1]] = {"executed": len(emitted), "failed": 0, "failed_case_ids": [], "IIIG006_executions": 2}
        else:
            counts[name.split(".")[-1]] = {"executed_test_methods": int(suite["counts"]["tests"]), "failed": 0, "failed_case_ids": []}
    assert counts["RSA2008InteropTest"]["executed_test_methods"] == 17
    assert counts["XacmlExampleTest"]["executed_test_methods"] == 2
    assert json.loads((ev / (prefix + "-existing-case-exclusions.json")).read_text()) == json.loads((ev / "71-existing-case-exclusions.json").read_text())
    focused = json.loads((ev / (log_name[:-4] + "-tests.json")).read_text())
    assert [sum(result["counts"][i] for result in focused["results"].values()) for i in range(4)] == [63,0,0,0]
    case_summary[prefix] = counts
(ev / "90-conformance-case-totals.json").write_text(json.dumps(case_summary, indent=2) + "\n")

checks = [["rtk","git","diff","--check","--",files[0]], ["rtk","git","branch","--show-current"], ["rtk","git","rev-parse","HEAD"], ["rtk","git","status","--short"], ["rtk","git","diff","--cached","--name-only"]]
scope = ["Round 4 gate: 3543 protected source/POM/resource/run003 files compared; changed: 0", "Only the conductor-approved InMemoryPolicyRepositoryTest.java changed in this packet", "All four original test bodies, mock expectations, assertions and c.verify calls preserved; bounded completion barriers added", "One forwarding helper; all four listener methods forward before counting down; no sleep or production threading edit", "License header unchanged; tabs retained; scoped diff --check passes", "All 2906 vendored conformance resources and 14 run003 evidence files unchanged", "Conformance sources, loop bounds and 30 exclusions per suite unchanged", "Deferred first-error semantics (policy4j-the.4.12), core/XML/date arithmetic, accepted aliases and POM edits unchanged", "Source hash unchanged throughout all 30 after runs and both full gates", "Only one authorized project file changed in this packet:", *files]
old_scope = (ev / "84-before-round4-gate-scope-check.txt").read_text()
inventory = old_scope.split("Full continued lane project-file inventory (32):\n",1)[1].split("\n\n",1)[0].splitlines()
assert len(inventory) == 32 and files[0] not in inventory
inventory.append(files[0])
scope += ["Full continued lane project-file inventory (33):", *inventory]
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
scope += ["", "Prior global diff warning at ExpressionTypeBuilder.java:96 is unchanged other-lane whitespace.", "No commit, br, task tick, issue closure, or evidence-index edit."]
(ev / "scope-check.txt").write_text("\n".join(scope) + "\n")

def results_section(path):
    lines, module, in_results = [], "unknown", False
    for line in path.read_text().splitlines():
        match = re.search(r"--- .* @ ([^ ]+) ---", line)
        if match:
            module = match.group(1)
        count = "Tests run:" in line
        if "[INFO] Results:" in line:
            in_results = True
            lines += ["Module: " + module, line]
        elif in_results:
            lines.append(line)
            if count:
                in_results = False
        elif count:
            lines += ["Module: " + module, line]
        elif "Reactor Summary" in line or "BUILD SUCCESS" in line or "BUILD FAILURE" in line or re.search(r"\.\.\..*(SUCCESS|FAILURE|SKIPPED)", line):
            lines.append(line)
    return "\n".join(lines)

sections = ["# Round 4 gate: 30 before / 30 after and two full JDK 11 gates", "Before: 26 pass / 4 fail; 120/4/0/0. After: 30 pass / 0 fail; 120/0/0/0.", "Single-class runs execute four test methods; the full scan retains its existing blank Vintage skip (five reported, four executed)."]
for directory in ("85-repository-before", "87-repository-after"):
    for iteration in range(1,31):
        path = ev / directory / f"{iteration:02d}.txt"
        sections += ["", "Run: " + directory + "/" + path.name, results_section(path)]
for log_name in ("88-reactor-verify-round4-gate.txt", "89-reactor-verify-round4-gate-repeat.txt"):
    sections += ["", "Run: " + log_name, results_section(ev / log_name), "Total: 936 tests / 0 failures / 0 errors / 9 existing skips; all modules SUCCESS.", "Configured cases: OASIS 2.0 347/0; OASIS 3.0 347/0; RSA 17/0; examples 2/0. No failed case IDs."]
(ev / "surefire-summary.txt").write_text((ev / "84-before-round4-gate-surefire-summary.txt").read_text().rstrip() + "\n\n" + "\n".join(sections) + "\n")
commands = (ev / "84-before-round4-gate-command.txt").read_text().rstrip() + "\n\n# Round 4 gate: exact commands; child Maven runs inherit the wrapper's JDK 11 JAVA_HOME\n"
commands += (ev / "round4-gate-commands.txt").read_text().rstrip() + "\n"
for directory in ("85-repository-before", "87-repository-after"):
    commands += "\n# " + directory + " (30 sequential Maven runs, plus status after each)\n" + (ev / directory / "command.txt").read_text()
commands += "\n".join(" ".join(command) for command in checks) + "\n"
commands += "rtk python3 " + relative + "/91-audit-round4-gate.py\n"
(ev / "command.txt").write_text(commands.replace(str(repo), "<repo>"))

review = """## Round 4 gate

The conductor approved required-repository-listener-barrier-fix.patch and
extended the scope to every listener-verifying test in this class. Status:
**APPROVED / APPLIED / VERIFIED**. The original two-test proposal is retained
as historical evidence; 90-round4-gate-fixes.patch is the extended applied delta.

InMemoryPolicyRepositoryTest.java:94 now has one helper that registers a
forwarding listener and returns a per-test CountDownLatch. Each callback
forwards to the existing EasyMock listener, then counts down after it returns.
Each test awaits completion for at most five seconds, with an explicit
callback timeout message, before its original c.verify(). No callback mock
expectation, repository assertion, verification, fixture or exclusion changed.

| Test | Callback count | Original verify -> final await / verify |
| --- | ---: | --- |
| testOnePolicyDifferentVersion | 4 added | :143 -> :174 / :175 |
| testAddRemove | 1 added + 1 removed | :166 -> :199 / :200 |
| testAddPolicyWithTheSameIdAndSameVersion | 1 added | :183 -> :218 / :219 |
| testFindAllPoliciesWithTheSameId | 4 added | :219 -> :256 / :257 |

Tests first: run 85 uses unchanged current source for thirty single-class
JDK 11 Maven runs. **26 pass / 4 fail**, 120 tests / 4 failures / 0 errors /
0 skips. Iterations 20, 21, 22 and 25 fail listener verification;
testAddRemove fails once and testFindAllPoliciesWithTheSameId three times.
Every failure is retained with its stack and original source hash. Earlier
full red gates 78/80 remain unchanged historical evidence. Run 87 repeats
the same thirty invocations after the fix: **30 pass / 0 fail**, 120/0/0/0.
All sixty reports are fresh and attest JDK 11.0.32.1; no compiler diagnostics.
The selected-class command runs four methods without skips. Full discovery
still reports the existing empty Vintage skip in this class; it is unchanged.

Runs 88 and 89 execute the full reactor with identical source, both
**936 tests / 0 failures / 0 errors / 9 existing skips, BUILD SUCCESS**.
Each gate executes OASIS 2.0 347 and OASIS 3.0 347 cases, RSA 17 and examples
2, all passed; the two resolver characterization methods also pass. No failed
configured case IDs. Case order, thirty exclusions per OASIS suite and loop
bounds match run 71. IIIG006 still executes twice in each OASIS suite.
Both gates repeat all 63 higher-order/status checks from the prior LOW fix.
Fresh receipts, per-module JDK properties and stripped repository reports
are retained separately for each gate before the repeat overwrites targets.

Scope proof: 3543 protected source/POM/resource/run003 files unchanged;
only this approved PDP test changed. All four original test bodies are
identical after removing the new barriers and checked-exception declaration.
All four c.verify() calls have barriers, the license header and tabs are kept,
and scoped git diff --check passes. No production threading change, sleep,
fixture/expected-result change, exclusion, commit, br, task tick or closure.

Residual limits: this finite repetition is not a proof against every possible
scheduler. First-error semantics remain DEFERRED as policy4j-the.4.12.
Configured conformance exclusions/bounds and previously recorded additional
IIIE301-303 proposals are unchanged; those additional cases were not rerun.
"""
(ev / "round4-gate.md").write_text(review)
(ev / "review-fixes.md").write_text((ev / "84-before-round4-gate-review-fixes.md").read_text().rstrip() + "\n\n" + review)
module_table = "| Module | Tests run | Failures | Errors | Skipped | Reactor result |\n| --- | ---: | ---: | ---: | ---: | --- |\n"
for name, row in expected_counts.items():
    module_table += "| " + name + " | " + " | ".join(map(str,row)) + " | SUCCESS" + ("; no tests" if row[0] == 0 else "") + " |\n"
module_table += "| Total | 936 | 0 | 0 | 9 | BUILD SUCCESS (both runs) |\n"
intro = """# Task 1.3 continued: Round 4 gate GREEN twice

Result: **full JDK 11 reactor verify PASS in runs 88 and 89**, each
936 tests / 0 failures / 0 errors / 9 existing skips. Configured OASIS 2.0
347 cases and OASIS 3.0 347 cases, RSA 17, examples 2: all passed in each.
The approved test-only listener barrier covers every listener-verifying test.
Single-class stability: before 26/30 passes (4 failures); after 30/30 passes.
Only InMemoryPolicyRepositoryTest.java changed in this gate follow-up;
all prior accepted core/XML/POM/identifier/variadic edits are preserved.

JDK: `openjdk version "11.0.32.1" 2026-08-18 LTS`;
Zulu11.90+205-CA, build 11.0.32.1+1-LTS. Direct version: 90-jdk-version.txt;
all six tested modules attest JDK 11 in 88-/89-jdk-properties.json.
Branch: integration/modernize-core-jdk28-valhalla; HEAD
befdd116bd11385ac8d007732fa8e4d347af3143; core API base a8a90d46.

## Latest full-reactor gates (runs 88 and 89)

"""
for name in ("88-reactor-verify-round4-gate.txt", "89-reactor-verify-round4-gate-repeat.txt"):
    intro += '```sh\nJAVA_HOME="$(jenv prefix 11)" rtk mvn -Dlicense.skipUpdateLicense=true verify -l ' + relative + '/' + name + '\n```\n\n'
intro += module_table + "\nCounts are fresh Maven Results, cross-checked against every fresh Surefire\nreport. Per-module receipts: 88-/89-module-counts.json; Results and every\nper-class count are retained in surefire-summary.txt. Case evidence:\n88-/89-conformance-testcases.json and 90-conformance-case-totals.json.\n\n"
old_summary = (ev / "84-before-round4-gate-summary.md").read_text()
history = old_summary[old_summary.index("## Round 4 review fixes"):]
history = history.replace("## Round 4 review fixes\n", "## Round 4 review fixes (historical gates 78/80; blocker now resolved above)\n",1)
(ev / "summary.md").write_text(intro + review + "\n" + history)
old_blockers = (ev / "84-before-round4-gate-conformance-blockers.md").read_text()
blocker_intro = """# Round 4 gate GREEN twice; repository listener-test blocker RESOLVED

Conductor approval applied only to InMemoryPolicyRepositoryTest.java, extended
to all four listener-verifying tests. Thirty current-source runs before:
26 pass / 4 fail; thirty after: 30 pass / 0 fail. Original red gates 78/80
are retained. Proposal status APPROVED/APPLIED/VERIFIED; applied extended
delta: 90-round4-gate-fixes.patch, mapping: round4-gate.md.

Runs 88/89: each 936/0/0/9, BUILD SUCCESS on JDK 11. Configured OASIS 2.0
347/0, OASIS 3.0 347/0, RSA 17/0, examples 2/0. No failed case IDs.
All conformance bounds, thirty exclusions per OASIS suite, case order and
fixture bytes unchanged. All earlier configured blockers remain resolved.
First-error semantics remain DEFERRED as policy4j-the.4.12. Additional
IIIE301-303 proposals below remain OPEN and were not rerun.

"""
(ev / "conformance-blockers.md").write_text(blocker_intro + old_blockers[old_blockers.index("## Previous gate/conformance status"):])
proposal = """# Round 4 gate proposal: repository listener completion

Status: **APPROVED / APPLIED / VERIFIED** by the conductor's Round 4 gate
blocker decision. Scope is this test only, extended to all four tests that
verify listener callbacks after asynchronous changes. The retained
required-repository-listener-barrier-fix.patch is the initial two-test proposal;
90-round4-gate-fixes.patch is the approved extended implementation.

One helper forwards all listener methods to the unchanged EasyMock listener
and counts down after completion. Counts are 4, 2, 1 and 4; every c.verify()
is preceded by a five-second await and clear timeout assertion. Original
callback expectations and all repository assertions are preserved. Production
threading, fixtures, expected results, sleeps and exclusions were not changed.

Proof: before source unchanged, 30 runs -> 26 pass / 4 fail (testAddRemove
once, testFindAllPoliciesWithTheSameId three times); after -> 30 pass / 0 fail.
85-/87-repository-*/summary.json retain counts/hashes/JDK and each report/log.
Full reactor gates 88/89 both pass 936/0/0/9, including configured conformance
347+347 OASIS cases, 17 RSA and 2 examples, all passed. No source change
between stability-after and either full gate. 3543 protected files unchanged.

Original red/source analysis is retained below; its PROPOSED/UNVERIFIED
status describes the earlier unapproved packet, and is superseded above.

## Original proposal (historical)

"""
(ev / "round4-gate-blocker-proposal.md").write_text(proposal + (ev / "84-before-round4-gate-round4-gate-blocker-proposal.md").read_text())
print("Round 4 gate evidence finalized: before 26/4, after 30/0; full gates 936/0/0/9 twice; conformance 347+347+17+2 per gate; 3543 protected files unchanged")
