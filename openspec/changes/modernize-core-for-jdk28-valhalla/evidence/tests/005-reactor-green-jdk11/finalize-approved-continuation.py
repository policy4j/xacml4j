# Historical finalizer for run 26 before core/XML restoration.
# Latest evidence uses finalize-baseline-restoration.py.
from pathlib import Path
import hashlib
import json
import re
import subprocess

repo = Path.cwd()
ev = Path(__file__).parent

def normalize(output):
    return re.sub(r"\x1b\[[0-9;]*m", "", output.replace(str(repo), "<repo>"))

def run(command):
    result = subprocess.run(command, capture_output=True, text=True)
    return result.returncode, normalize(result.stdout + result.stderr)

protected = json.loads((ev / "14-protected-before-downstream.json").read_text())
changed = [name for name, digest in protected.items()
           if not (repo / name).is_file()
           or hashlib.sha256((repo / name).read_bytes()).hexdigest() != digest]
owned_existing = [
    "xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java",
    "xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java",
    "xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionLegacyHandler.java",
    "xacml-pdp/src/test/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandlerTest.java",
    "xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXacmlXPathHandlerTest.java",
    "xacml-test/src/main/java/org/xacml4j/v30/ExpectedAttributeResolverBuilder.java",
    "xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java",
    "xacml-conformance-tests/src/test/java/org/xacml4j/v20/Xacml20ConformanceAttributeResolver.java",
]
new_files = {
    "xacml-test/src/test/java/org/xacml4j/v30/ExpectedAttributeResolverBuilderTest.java": owned_existing[5],
    "xacml-test/src/test/java/org/xacml4j/v20/Xacml30TestUtilityTest.java": owned_existing[6],
    "xacml-conformance-tests/src/test/java/org/xacml4j/v20/Xacml20ConformanceAttributeResolverTest.java": owned_existing[7],
}
header = re.compile(r"/\*.*?#L%.*?\*/", re.DOTALL)
changed_headers = []
for name in owned_existing:
    code, before = run(["rtk", "proxy", "git", "show", "HEAD:" + name])
    assert code == 0
    after = (repo / name).read_text()
    if header.search(before).group() != header.search(after).group():
        changed_headers.append(name)
for name, template in new_files.items():
    if header.search((repo / name).read_text()).group() != header.search((repo / template).read_text()).group():
        changed_headers.append(name)
pom_matches = (repo / "xacml-test/pom.xml").read_bytes() == (ev / "14-approved-pom.txt").read_bytes()
scope = [f"Protected files compared with approved-continuation snapshot: {len(protected)}",
         f"Protected files changed: {len(changed)}", *changed,
         f"Existing owned Java license headers checked: {len(owned_existing)}",
         f"New Java headers compared with module template: {len(new_files)}",
         f"Changed license headers: {len(changed_headers)}", *changed_headers,
         f"Approved POM matches post-patch snapshot exactly: {pom_matches}",
         "Current lane files (plus this run directory):",
         *owned_existing, *new_files, "xacml-test/pom.xml",
         "Shared dirty XML/resource/JSON changes predate this approved continuation; no writes by this lane."]
checks = [
    ["rtk", "git", "diff", "--quiet", "a8a90d46", "--", "xacml-core"],
    ["rtk", "git", "diff", "--check", "--", "xacml-pdp/src", "xacml-test/pom.xml", "xacml-test/src", "xacml-conformance-tests/src/test/java"],
    ["rtk", "git", "apply", "--reverse", "--check", str(ev / "required-pom-fix.patch")],
    ["rtk", "git", "apply", "--check", str(ev / "required-core-fix.patch")],
    ["rtk", "git", "apply", "--check", str(ev / "required-xml-content-fix.patch")],
    ["rtk", "git", "status", "--short"],
    ["rtk", "proxy", "git", "diff", "--name-only", "--", "xacml-pdp/src", "xacml-test/pom.xml", "xacml-test/src", "xacml-conformance-tests/src/test/java"],
    ["rtk", "git", "diff", "--cached", "--name-only"],
]
failed_checks = []
for command in checks:
    code, output = run(command)
    scope.extend(["", " ".join(command), f"Exit code: {code}", output.rstrip()])
    if code:
        failed_checks.append(command)
(ev / "scope-check.txt").write_text(normalize("\n".join(scope)) + "\n")

logs = ["06-pdp-tests.txt", "07-selector-characterization-before.txt", "08-selector-characterization-after.txt",
        "10-reactor-verify.txt", "13-reactor-verify-current-tree.txt", "16-test-support-characterization-after.txt",
        "20-reactor-verify-approved-pom.txt", "21-conformance-resolver-after-cast.txt", "22-reactor-verify-with-conformance.txt",
        "24-response-comparison-before.txt", "25-response-comparison-after.txt", "26-reactor-verify-response-comparison.txt"]
surefire = []
gate_counts = {}
for name in logs:
    lines = (ev / name).read_text().splitlines()
    surefire.extend(["", "Run: " + name])
    module = "unknown"
    in_results = False
    for line in lines:
        match = re.search(r"--- .* @ ([^ ]+) ---", line)
        if match:
            module = match.group(1)
        counts = re.search(r"Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)", line)
        if name == logs[-1] and counts and " - in " not in line:
            gate_counts[module] = [int(v) for v in counts.groups()]
        if "[INFO] Results:" in line:
            in_results = True
            surefire.extend(["Module: " + module, line])
        elif in_results:
            surefire.append(line)
            if counts:
                in_results = False
        elif counts and " - in " not in line:
            surefire.extend(["Module: " + module, line])
        elif "Reactor Summary" in line or "BUILD SUCCESS" in line or "BUILD FAILURE" in line:
            surefire.append(line)
        elif re.search(r"\.\.\..*(SUCCESS|FAILURE|SKIPPED)", line):
            surefire.append(line)
(ev / "surefire-summary.txt").write_text(normalize("\n".join(surefire).lstrip()) + "\n")
assert gate_counts == {"xacml-core": [494,0,0,4], "xacml-xml": [29,0,0,0], "xacml-json": [5,0,0,1],
                       "xacml-pdp": [71,0,0,4], "xacml-test": [20,0,0,0], "xacml-conformance-tests": [41,5,0,0]}
(ev / "26-module-counts.json").write_text(json.dumps(gate_counts, indent=2) + "\n")

diagnostics = json.loads((ev / "compile-diagnostics.json").read_text())
pdp = diagnostics["01-pdp-baseline-compile.txt"] + diagnostics["03-pdp-compile-after-path-import.txt"]
pdp += [d for d in diagnostics["05-pdp-test-compile.txt"] if "method single" in "\n".join(d["context"].splitlines()[:3])]
dispositions = {
    ("MultipleResourcesViaXPathExpressionHandler.java",46): "Import and cast public Path instead of removed top-level PathValue.",
    ("MultipleResourcesViaXPathExpressionHandler.java",119): "Stream Entity.attributes().",
    ("MultipleResourcesViaXPathExpressionHandler.java",145): "Use path.getEvaluatesTo().getValueType().",
    ("MultipleResourcesViaXPathExpressionHandler.java",146): "Cascading inference error resolved by the transform value-type fix.",
    ("MultipleResourcesViaXPathExpressionHandler.java",163): "Iterate Entity.attributes().",
    ("MultipleResourcesViaXPathExpressionLegacyHandler.java",68): "Use Entity.findValues(id, type); preserve filtering/cardinality.",
    ("MultipleResourcesViaXPathExpressionLegacyHandler.java",90): "Iterate Entity.attributes().",
    ("DefaultEvaluationContextHandler.java",49): "Implement Optional<Entity> getEntity(CategoryId).",
    ("DefaultEvaluationContextHandler.java",131): "Resolve designators by attribute id, datatype and issuer.",
    ("DefaultEvaluationContextHandler.java",132): "Cascading Optional inference error resolved by the designator overload fix.",
    ("DefaultEvaluationContextHandler.java",180): "Cast the single bag value directly to Entity.",
    ("DefaultEvaluationContextHandler.java",181): "Remove EntityValue.get(); Entity is the value.",
    ("DefaultEvaluationContextHandler.java",196): "Delegate Optional<Entity> getEntity to the request callback.",
    ("MultipleResourcesViaXacmlXPathHandlerTest.java",184): "Use RequestContext.getAttribute(category, id); keep value/path assertions.",
}

def table(entries, disposition):
    rows = ["| Compiler file:line:column | Diagnostic | Fix |", "| --- | --- | --- |"]
    for d in entries:
        message = d["message"].replace("|", "\\|")
        location = f"{d['file']}:{d['line']}:{d['column']}"
        rows.append(f"| `{location}` | {message} | {disposition(d)} |")
    return "\n".join(rows)

compiler = ["# Compiler errors and fixes", "", "Locations are the compiler's original locations, including temporary test insertions.",
            "Repeated errors from reruns are retained in compile-diagnostics.json and raw logs.", "",
            "## PDP diagnostics (14)", "", table(pdp, lambda d: dispositions[(Path(d["file"]).name,d["line"])])]
compiler += ["", "## JUnit main-source diagnostics (24)", "", table(diagnostics["13-reactor-verify-current-tree.txt"],
            lambda d: "Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained.")]
downstream = diagnostics["14-reactor-compile-approved-pom.txt"] + diagnostics["17-reactor-conformance-compile.txt"] + diagnostics["19-conformance-resolver-after.txt"]
compiler += ["", "## Downstream API diagnostics (3)", "", table(downstream, lambda d:
             "Use value.getEvaluatesTo().getValueType()." if Path(d["file"]).name == "ExpectedAttributeResolverBuilder.java"
             else "Import StringVal instead of removed String." if d["line"] == 32
             else "Cast the annotation-constrained string BagOfValues.value() result to StringVal.")]
transient = diagnostics["04-pdp-test-compile.txt"] + [d for d in diagnostics["05-pdp-test-compile.txt"]
            if "method permit" in "\n".join(d["context"].splitlines()[:3])]
compiler += ["", "## Temporary edit errors, also corrected (4)", "", table(transient, lambda d:
             "Use path.getEvaluatesTo().getValueType(); resolves subsequent inference error." if Path(d["file"]).name == "MultipleResourcesViaXPathExpressionHandler.java"
             else "New test setup uses Result.ok(Decision.PERMIT), not nonexistent Result.permit().")]
(ev / "compiler-fixes.md").write_text("\n".join(compiler) + "\n")

continued = (ev / "continued-commands.txt").read_text()
commands = ev / "command.txt"
marker = "# Conductor-approved POM continuation (runs 14 onward)"
if marker not in commands.read_text():
    commands.write_text(commands.read_text().rstrip() + "\n\n" + marker + "\n" + continued)

print("Full reactor compiles; latest gate BUILD FAILURE at conformance.")
print("Full counts: Tests run 660 / Failures 5 / Errors 0 / Skipped 9")
print("Compiler fixes recorded: 14 PDP, 24 JUnit dependency, 3 downstream API; 4 temporary edit errors corrected.")
print(f"Protected files: {len(protected)} checked / {len(changed)} changed")
print(f"License headers: {len(owned_existing)} existing and {len(new_files)} new checked / {len(changed_headers)} changed")
print(f"Exact approved POM retained: {pom_matches}; failed scope checks: {len(failed_checks)}")
assert not changed and not changed_headers and pom_matches and not failed_checks
