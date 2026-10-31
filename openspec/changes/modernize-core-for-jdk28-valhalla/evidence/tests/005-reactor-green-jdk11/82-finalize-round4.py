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
snapshot = json.loads((ev / "74-round4-before.json").read_text())
files = snapshot["allowed_existing"]
protected = {name: digest for name, digest in snapshot["hashes"].items() if name not in files}
assert len(protected) == 3542
for name, digest in protected.items():
    assert (repo / name).is_file() and hashlib.sha256((repo / name).read_bytes()).hexdigest() == digest, name
current = set()
for module in repo.glob("xacml-*"):
    if module.is_dir():
        current.update(str(path.relative_to(repo)) for path in (module / "src").rglob("*") if path.is_file())
assert not current - set(snapshot["hashes"]), current - set(snapshot["hashes"])
header = re.compile(r"/\*.*?#L%.*?\*/", re.DOTALL)
patch = []
for name in files:
    before = (ev / ("74-before-" + Path(name).name + ".txt")).read_text()
    after = (repo / name).read_text()
    assert before != after, name
    assert header.search(before).group() == header.search(after).group(), name
    delta = list(difflib.unified_diff(before.splitlines(keepends=True), after.splitlines(keepends=True),
                                    fromfile="a/" + name, tofile="b/" + name))
    assert all(line[1:].rstrip("\n") == line[1:].rstrip()
               for line in delta if line.startswith("+") and not line.startswith("+++")), name
    patch.extend(delta)
before_main = (ev / "74-before-HigherOrderFunctions.java.txt").read_text()
after_main = (repo / files[0]).read_text()
old_guard = '\t\t\tPreconditions.checkArgument(!ref.getEvaluatesTo().isBag(), "The referenced function must return a primitive value");'
new_guard = '\t\t\tif(ref.getEvaluatesTo().isBag()){\n\t\t\t\tspec.validateParametersAndThrow(arguments);\n\t\t\t}'
assert before_main.count(old_guard) == 1
assert after_main == before_main.replace(old_guard, new_guard, 1)
before_test = (ev / "74-before-HigherOrderVariadicFunctionsTest.java.txt").read_text()
after_test = (repo / files[1]).read_text()
for match in re.finditer(r"public void (\w+)\([^)]*\)(?: throws Exception)?\s*\{", before_test):
    if match.group(1) == "mapRejectsBagReturningFunctionAtValidation":
        continue
    start, depth, end = match.start(), 1, match.end()
    while depth:
        if before_test[end] == "{":
            depth += 1
        elif before_test[end] == "}":
            depth -= 1
        end += 1
    assert before_test[start:end] in after_test, match.group(1)
assert 'assertTrue("Indeterminate must carry an evaluation status", status.hasCaptured());' in after_test
(ev / "82-round4-fixes.patch").write_text("".join(patch))
(ev / "82-round4-file-hashes.json").write_text(json.dumps(
    {name: hashlib.sha256((repo / name).read_bytes()).hexdigest() for name in files}, indent=2) + "\n")

def totals(receipt):
    return [sum(result["counts"][i] for result in receipt["results"].values()) for i in range(4)]
red = json.loads((ev / "75-round4-red-tests.json").read_text())
assert red["production_matches_before_snapshot"] and totals(red) == [63,2,0,0]
failures = [test for result in red["results"].values() for test in result["tests"] if test["problems"]]
assert len(failures) == 2 and all(test["name"].startswith("mapRejectsBagReturningFunctionDuringApplyTypeResolution[") for test in failures)
assert all("IllegalArgumentException" in json.dumps(test) for test in failures)
for prefix in ("77-round4-green", "78-reactor-verify-round4", "80-reactor-verify-round4-retry"):
    green = json.loads((ev / (prefix + "-tests.json")).read_text())
    assert totals(green) == [63,0,0,0]
    assert green["production_hash"] == hashlib.sha256((repo / files[0]).read_bytes()).hexdigest()
diagnostics = json.loads((ev / "compile-diagnostics.json").read_text())
for name in ("75-round4-red.txt", "77-round4-green.txt", "78-reactor-verify-round4.txt", "80-reactor-verify-round4-retry.txt"):
    assert not diagnostics[name]

expected_counts = {"xacml-core": [762,0,0,4], "xacml-xml": [34,0,0,0],
                   "xacml-json": [5,0,0,1], "xacml-pdp": [74,1,0,4]}
for prefix, gate_name, pdp_failures in (("78", "78-reactor-verify-round4.txt", 2), ("80", "80-reactor-verify-round4-retry.txt", 1)):
    gate = (ev / gate_name).read_text()
    assert "BUILD FAILURE" in gate and "BUILD SUCCESS" not in gate
    assert re.search(r"Policy Unit Test Support .* SKIPPED", gate)
    assert re.search(r"Conformance Tests .* SKIPPED", gate)
    counts, module = {}, "unknown"
    for line in gate.splitlines():
        match = re.search(r"--- .* @ ([^ ]+) ---", line)
        if match:
            module = match.group(1)
        count = re.search(r"Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)", line)
        if count and " - in " not in line:
            counts[module] = [int(value) for value in count.groups()]
    expected = {name: row[:] for name, row in expected_counts.items()}
    expected["xacml-pdp"][1] = pdp_failures
    assert counts == expected, (prefix, counts)
    assert [sum(row[i] for row in counts.values()) for i in range(4)] == [875,pdp_failures,0,9]
    all_counts = {"xacml": [0,0,0,0], "xacml-core": counts["xacml-core"], "xacml-xml-jaxb": [0,0,0,0],
                  **{name: row for name, row in counts.items() if name != "xacml-core"},
                  "xacml-test": None, "xacml-conformance-tests": None}
    (ev / (prefix + "-module-counts.json")).write_text(json.dumps(all_counts, indent=2) + "\n")
    (ev / (prefix + "-unrun-modules.json")).write_text(json.dumps(
        {"xacml-test": "NOT RUN; reactor SKIPPED after PDP failure",
         "xacml-conformance-tests": "NOT RUN; reactor SKIPPED after PDP failure",
         "conformance_case_totals": "NOT RUN; prior reports are stale and excluded"}, indent=2) + "\n")
report_counts, jdk_by_module = {}, {}
for module, expected in expected_counts.items():
    total = [0,0,0,0]
    report_counts[module] = {}
    for path in (repo / module / "target/surefire-reports").glob("TEST-*.xml"):
        root = ET.parse(path).getroot()
        row = [int(root.get(key)) for key in ("tests", "failures", "errors", "skipped")]
        total = [a + b for a, b in zip(total, row)]
        report_counts[module][root.get("name")] = row
        props = {item.get("name"): item.get("value") for item in root.find("properties")}
        jdk = {key: props[key] for key in ("java.version", "java.runtime.version", "java.vm.name", "java.vendor")}
        assert jdk["java.version"] == "11.0.32.1", (module, path, jdk)
        jdk_by_module[module] = jdk
    assert total == expected, (module, total, expected)
(ev / "80-jdk-properties.json").write_text(json.dumps(jdk_by_module, indent=2) + "\n")
(ev / "80-surefire-report-counts.json").write_text(json.dumps(report_counts, indent=2) + "\n")

exclusions = {}
for name in ("Xacml20ConformanceTest", "Xacml30ConformanceTest"):
    source = (repo / "xacml-conformance-tests/src/test/java/org/xacml4j/v20" / (name + ".java")).read_text()
    by_group = {}
    for method in re.findall(r"public void \w+\(\) throws Exception\s*\{(.*?)\n\t\}", source, re.DOTALL):
        group = re.search(r'executeXacmlConformanceTestCase\(skipTests, "([^"]+)"', method)
        if group:
            by_group[group.group(1)] = [group.group(1) + number.zfill(3) for number in re.findall(r"skipTests.add\((\d+)\)", method)]
    exclusions[name] = by_group
    assert sum(len(values) for values in by_group.values()) == 30
assert exclusions == json.loads((ev / "71-existing-case-exclusions.json").read_text())
(ev / "80-existing-case-exclusions.json").write_text(json.dumps(exclusions, indent=2) + "\n")
old_scope = (ev / "74-before-round4-scope-check.txt").read_text()
inventory = old_scope.split("Full continued lane project-file inventory (32):\n",1)[1].split("\n\n",1)[0].splitlines()
assert len(inventory) == len(set(inventory)) == 32 and all(name in inventory for name in files)
scope = [f"Round 4 protected files compared: {len(protected)}; changed: 0",
         "All 2906 vendored conformance resources and 14 run003 evidence files unchanged",
         "Only one bag-return guard changes in HigherOrderFunctions.resolve; every loop and other method byte-identical",
         "Function invocation/parameter machinery, LogicalFunctions and deferred first-error semantics unchanged",
         "PDP source/tests, all other core/XML/date-time/POM/fixture sources unchanged",
         "Two edited license headers unchanged; old success/control test bodies preserved; error assertions strengthened",
         "Conformance sources, bounds and 30 exclusions per suite unchanged; conformance NOT RUN in gates 78/80",
         "Only two authorized project files changed in round 4:", *files,
         "Full continued lane project-file inventory (32):", *inventory]
checks = [["rtk", "git", "diff", "--check", "--", *files],
          ["rtk", "git", "branch", "--show-current"], ["rtk", "git", "rev-parse", "HEAD"],
          ["rtk", "git", "status", "--short"], ["rtk", "git", "diff", "--cached", "--name-only"]]
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
scope += ["", "Prior global diff warning at ExpressionTypeBuilder.java:96 is unchanged other-lane whitespace.",
          "Scoped round 4 diff --check passes; proposed repository patch is unapplied."]
(ev / "scope-check.txt").write_text("\n".join(scope) + "\n")

sections = []
for name in ("75-round4-red.txt", "77-round4-green.txt", "78-reactor-verify-round4.txt", "80-reactor-verify-round4-retry.txt"):
    sections += ["", "Run: " + name]
    module, in_results = "unknown", False
    for line in (ev / name).read_text().splitlines():
        match = re.search(r"--- .* @ ([^ ]+) ---", line)
        if match:
            module = match.group(1)
        count = re.search(r"Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)", line)
        if "[INFO] Results:" in line:
            in_results = True
            sections += ["Module: " + module, line]
        elif in_results:
            sections.append(line)
            if count:
                in_results = False
        elif count and " - in " not in line:
            sections += ["Module: " + module, line]
        elif "Reactor Summary" in line or "BUILD SUCCESS" in line or "BUILD FAILURE" in line or re.search(r"\.\.\..*(SUCCESS|FAILURE|SKIPPED)", line):
            sections.append(line)
    if name.startswith(("78-", "80-")):
        sections.append("xacml-test and conformance NOT RUN; reactor SKIPPED after PDP failure; stale reports excluded.")
(ev / "surefire-summary.txt").write_text((ev / "74-before-round4-surefire-summary.txt").read_text().rstrip()
    + "\n\n# Round 4 review fixes (after 1.3i)\n" + "\n".join(sections) + "\n")
commands = (ev / "74-before-round4-command.txt").read_text().rstrip()
commands += "\n\n# Round 4 review fixes (after 1.3i): exact build/test/evidence commands\n"
commands += (ev / "round4-commands.txt").read_text().rstrip() + "\n"
commands += "\n".join(" ".join(command) for command in checks) + "\n"
commands += "rtk proxy git show a8a90d46:xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/AbstractPolicyRepository.java\n"
commands += "rtk proxy git show a8a90d46:xacml-pdp/src/test/java/org/xacml4j/v30/spi/repository/InMemoryPolicyRepositoryTest.java\n"
commands += "rtk python3 " + relative + "/83-audit-round4.py\n"
(ev / "command.txt").write_text(commands.replace(str(repo), "<repo>"))

review = """## Round 4 review fixes

Started after packet 1.3i: run 71 full gate and run 73 final audit were complete.
Reviews: rv008 Agy APPROVE; rv009 Grok REQUEST_CHANGES. The conductor defers
the pre-existing engine-wide first-error behavior under policy4j-the.4.12;
this packet repairs only the LOW resolver escape and strengthens status tests.

| Finding / original file:line -> change | Tests first / observed result |
| --- | --- |
| LOW HigherOrderFunctions.java:359; map type resolution leaked IllegalArgumentException for a bag-returning reference. -> replace that precondition with a bag-only call to spec.validateParametersAndThrow(arguments), final :359-361. | mapRejectsBagReturningFunctionDuringApplyTypeResolution constructs Apply(map, string-bag, bag) with both 3.0/1.0 IDs, in runtime-checks off/on rows. getEvaluatesTo must produce the same exception class, message and syntax-error status as explicit validation; evaluate must retain processing-error. Run 75: both rows fail on IllegalArgumentException. Runs 77/78/80: both pass. |
| Strengthen status-only variadic assertions; HigherOrderVariadicFunctionsTest.java:317 before review. -> retain hasCaptured and add exact StatusCodeId comparisons; reset capture before each Condition evaluation. | assertInvalid covers no bag, two bags, missing operands, referenced arity/types/return type and the packet's malformed binary predicate. Explicit validation asserts syntax-error; Apply invocation and captured INDETERMINATE Condition status assert processing-error. mapRejectsBagReturningFunctionAtValidation also asserts syntax-error. All stronger assertions already pass in run 75 and again in runs 77/78/80. |
| MEDIUM first predicate error stops or/and/higher-order loops. | DEFERRED by conductor as policy4j-the.4.12. Every loop, helper and logical/invocation class is byte-identical to the round 4 snapshot; no behavior change or claim of error-order conformance. |

Smallest-change choice: return-type resolution is a separate entrypoint from
validation. Only a reference whose declared return type is already a bag is
delegated to the same validation path; its existing validator returns false
and FunctionSpecBuilder.java:435-437 raises PolicySyntaxException (syntax-error).
Valid map calls keep the existing type-resolution path and return type.
No new global validation, invocation behavior or exception mapping is added.
The engine's syntax exception is a structured EvaluationException; this fix
removes the raw IllegalArgumentException escape rather than changing that
exception hierarchy. Reflection invocation still maps failures to
FunctionInvocationException processing-error at DefaultFunctionInvocation:136-138.

Tests first: 75-round4-red.txt is 63/2/0/0 with the production hash equal to
the pre-test snapshot. Only the two new map-resolution rows fail. Run 77
repeats the focused command: 63/0/0/0. The 54 variadic rows, three invocation
checks and six legacy controls also pass inside both reactor attempts.
No compiler diagnostics in runs 75/77/78/80. No assertion was removed/weakened.

Full gate is BLOCKED outside this LOW-only scope. Run 78 reaches 875 tests /
2 failures / 0 errors / 9 skips; testOnePolicyDifferentVersion:143 and
testFindAllPoliciesWithTheSameId:219 fail listener verification in xacml-pdp.
With no intervening source edits, run 80 reaches 875/1/0/9; only the second
test fails. Core passes 762/0/0/4 in both; XML 34/0/0/0, JSON 5/0/0/1.
Both test-support and conformance are reactor SKIPPED, hence NOT RUN.
Prior conformance reports are stale for this round and excluded.

The repository and test match a8a90d46 byte for byte. Async notifications
run on an executor; immediate mock verification has no completion barrier.
The race cause is INFERRED from this path and differing failure counts.
No map call occurs in these test policies. Source proof and failed reports:
79-repository-race-proof.json, 80-repository-race-proof.json,
78-InMemoryPolicyRepositoryTest.xml and 80-InMemoryPolicyRepositoryTest.xml.
Concrete proposal: round4-gate-blocker-proposal.md and the unapplied,
UNVERIFIED required-repository-listener-barrier-fix.patch. No PDP edit made.

Scope: two authorized source/test files, two headers preserved, 3542 protected
files unchanged. Incremental delta: 82-round4-fixes.patch. Conformance fixture
bytes, loop bounds and thirty exclusions per suite remain unchanged; run 80
has zero case executions because conformance was not reached, not a conformance
pass. Last complete gate was run 71 (934/0/0/9; cases 347+347+17+2).
No commit, br, task tick, issue closure or evidence-index edit.
"""
(ev / "round4-review-fixes.md").write_text(review)
review_document = (ev / "74-before-round4-review-fixes.md").read_text()
(ev / "review-fixes.md").write_text(review_document.rstrip() + "\n\n" + review)
summary = (ev / "74-before-round4-summary.md").read_text()
old_intro = summary[:summary.index("JDK:")]
new_intro = """# Task 1.3 continued: Round 4 LOW fixed; full gate BLOCKED

Result: **LOW fix and exact-status tests PASS; full JDK 11 gate BLOCKED at PDP**.
Tests first: run 75 63/2/0/0 red -> run 77 63/0/0/0 green. Full attempt 78:
875 tests / 2 failures / 0 errors / 9 skips. Unchanged retry 80: 875/1/0/9.
Core/XML/JSON pass; a pre-existing repository listener-test race blocks PDP.
Test-support and all conformance suites are **NOT RUN** in both attempts.
No conformance result from prior target reports is counted as fresh evidence.
Only HigherOrderFunctions and HigherOrderVariadicFunctionsTest changed.
The first-error semantics remain deferred under policy4j-the.4.12.

"""
summary = new_intro + summary[len(old_intro):]
summary = summary.replace("Fresh raw version line: 72-jdk-version.txt.", "Fresh raw version line: 81-jdk-version.txt.", 1)
summary = summary.replace("Fresh final-gate Surefire JVM properties: 71-jdk-properties.json; direct version: 72-jdk-version.txt.",
    "Fresh retry JVM properties for four tested modules: 80-jdk-properties.json; direct version: 81-jdk-version.txt.", 1)
current_gate = """## Latest full-reactor attempt (run 80)

```sh
JAVA_HOME="$(jenv prefix 11)" rtk mvn -Dlicense.skipUpdateLicense=true verify -l RELATIVE/80-reactor-verify-round4-retry.txt
```

| Module | Tests run | Failures | Errors | Skipped | Reactor result |
| --- | ---: | ---: | ---: | ---: | --- |
| xacml parent | 0 | 0 | 0 | 0 | SUCCESS; no tests |
| xacml-core | 762 | 0 | 0 | 4 | SUCCESS |
| xacml-xml-jaxb | 0 | 0 | 0 | 0 | SUCCESS; no tests |
| xacml-xml | 34 | 0 | 0 | 0 | SUCCESS |
| xacml-json | 5 | 0 | 0 | 1 | SUCCESS |
| xacml-pdp | 74 | 1 | 0 | 4 | FAILURE |
| xacml-test | NOT RUN | - | - | - | reactor SKIPPED |
| xacml-conformance-tests | NOT RUN | - | - | - | reactor SKIPPED |
| Executed total | 875 | 1 | 0 | 9 | BUILD FAILURE |

Fresh counts: 80-module-counts.json and four Results sections in
surefire-summary.txt. Null downstream rows mean NOT RUN, not zero-test passes.
The first attempt's counts (PDP 74/2/0/4) are in 78-module-counts.json.
The below run 71 counts are the last successful gate before round 4.

""".replace("RELATIVE", relative)
summary = summary.replace("## Final full-reactor gate\n", current_gate + review + "\n\n## Last successful full-reactor gate (run 71; before round 4)\n", 1)
summary = summary.replace("## Conformance case totals and dispositions\n", "## Last successful conformance totals and dispositions (run 71; not run in round 4)\n", 1)
summary = summary.replace("This follow-up changes three existing core source/test files, listed above and\nin scope-check.txt.",
    "Round 4 changes two existing core source/test files, listed above and in\nscope-check.txt. Packet 1.3i's three-file proof is archived as\n74-before-round4-scope-check.txt.", 1)
summary = summary.replace("Round 3's nine-file proof is archived as\n66-before-round2-verification-scope-check.txt.",
    "Round 3's nine-file proof is archived as\n66-before-round2-verification-scope-check.txt. Current round 4 changes only the\nmap resolver guard and its existing variadic test; its proof is scope-check.txt.", 1)
summary = summary.replace("history, focused red/green Results, and all six final run 71 Maven Results.",
    "history, focused red/green Results, all six prior run 71 Results, and four\ntested-module Results for each of the failed full attempts 78/80.", 1)
old_scope_text = "scope-check.txt verifies 3,541 protected hashes in this follow-up, including all\n2,906 vendored conformance resources; zero changed. Three edited headers and all\nprior test method bodies are unchanged. Exactly three authorized existing core\nfiles changed. The full continued lane inventory remains 32 files; prior round 3\nscope proof is archived as 66-before-round2-verification-scope-check.txt."
new_scope_text = "scope-check.txt verifies 3,542 protected hashes in round 4, including all 2,906\nvendored conformance resources; zero changed. Two edited headers are unchanged;\nexisting success/control test bodies remain byte-identical and error assertions\nare strengthened. Exactly two authorized existing core files changed. The lane\ninventory remains 32 files; prior scope proof is archived as\n74-before-round4-scope-check.txt."
assert old_scope_text in summary
summary = summary.replace(old_scope_text, new_scope_text, 1)
summary = summary.replace("Residual risk: IIIE301", "Current unfinished gate: the PDP listener-test race needs scope approval for\nthe concrete, unapplied barrier proposal. Full conformance must then run again.\nFirst-error loop semantics are unchanged and deferred as policy4j-the.4.12.\n\nRetained residual risk: IIIE301", 1)
summary = summary.replace("Final follow-up scope proof: 3541", "Previous packet 1.3i scope proof: 3541", 1)
summary += "\nCurrent round 4 scope proof: 3542 protected files unchanged; two authorized\nfiles changed. 82-round4-fixes.patch contains the delta. Latest full gate is\n80-reactor-verify-round4-retry.txt, BUILD FAILURE outside the LOW-only scope;\n875/1/0/9, conformance NOT RUN. Proposed PDP barrier patch is UNVERIFIED/unapplied.\n"
(ev / "summary.md").write_text(summary)
old_blockers = (ev / "74-before-round4-conformance-blockers.md").read_text()
old_blockers = old_blockers.replace("# Conformance blocker status: configured gate GREEN; additional IIIE3xx proposals OPEN", "## Previous gate/conformance status (run 71; retained)", 1)
old_blockers = old_blockers.replace("Final run 71:", "Last successful prior run 71:", 1)
blockers = """# Round 4 gate BLOCKED at PDP; LOW fix verified

Run 78: 875/2/0/9, BUILD FAILURE; run 80 unchanged retry: 875/1/0/9,
BUILD FAILURE. InMemoryPolicyRepositoryTest listener verification fails at
:143 and :219 in run 78; only :219 in run 80. Source/tests byte-identical to
a8a90d46. Async listener/test race is INFERRED from the source and varying
failure counts; production map is not called by these tests.

Core 762/0/0/4, XML 34/0/0/0 and JSON 5/0/0/1 pass in both. LOW map resolver
and exact-status tests pass (75 red -> 77/78/80 green). First-error semantics
remain DEFERRED as policy4j-the.4.12. Scope permits only this LOW fix.

xacml-test and conformance suites: NOT RUN; reactor SKIPPED after PDP failure.
No new conformance failure IDs, because no cases executed this round. Prior
target reports are stale and excluded. Thirty exclusions per OASIS suite and
all loop bounds/fixture hashes are unchanged; 80-existing-case-exclusions.json
matches run 71. Last complete gate/case results below are historical.

Proposal OPEN: round4-gate-blocker-proposal.md and
required-repository-listener-barrier-fix.patch (UNVERIFIED, unapplied).
Requested scope is the repository test only; preserve all expectations and
await completion before verification in the two four-addition tests.
No PDP edit, fixture/expected-result change, commit or tracking update made.

""" + old_blockers
(ev / "conformance-blockers.md").write_text(blockers)
for name in ("summary.md", "review-fixes.md", "surefire-summary.txt", "command.txt", "conformance-blockers.md", "scope-check.txt"):
    assert str(repo) not in (ev / name).read_text(), name
print("PASS: LOW red 63/2/0/0 -> green 63/0/0/0; exact status assertions pass")
print("BLOCKER: full gates 78 875/2/0/9 and 80 875/1/0/9; PDP listener tests fail; conformance NOT RUN")
print("PASS: 3542 protected files; two authorized changes; all loops and other production code unchanged")
print("Updated summary, review-fixes, surefire-summary, command, blockers and scope evidence")
