# Historical finalizer for the pre-approval run 13. Latest evidence uses
# finalize-approved-continuation.py; do not rerun this obsolete finalizer.
from pathlib import Path
import hashlib
import json
import re
import subprocess

repo = Path.cwd()
evidence = Path(__file__).parent

def normalize(output):
    return re.sub(r"\x1b\[[0-9;]*m", "", output.replace(str(repo), "<repo>")
                  .replace(str(Path.home()), "<user-home>"))

def run(command):
    result = subprocess.run(command, capture_output=True, text=True)
    return result.returncode, normalize(result.stdout + result.stderr)

for log in evidence.glob("[0-9][0-9]-*.txt"):
    log.write_text(normalize(log.read_text()))

code, output = run(["rtk", "proxy", "bash", "-c", 'exec "$JAVA_HOME/bin/java" -version'])
assert code == 0
(evidence / "00-jdk-version.txt").write_text(output)

protected = json.loads((evidence / "00-protected-files.json").read_text())
changed_protected = [name for name, digest in protected.items()
                     if not (repo / name).is_file()
                     or hashlib.sha256((repo / name).read_bytes()).hexdigest() != digest]
owned = [
    "xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java",
    "xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java",
    "xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionLegacyHandler.java",
    "xacml-pdp/src/test/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandlerTest.java",
    "xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXacmlXPathHandlerTest.java",
]
changed_headers = []
for name in owned:
    code, before = run(["rtk", "proxy", "git", "show", "HEAD:" + name])
    assert code == 0
    after = (repo / name).read_text()
    license_header = re.compile(r"/\*.*?#L%.*?\*/", re.DOTALL)
    if license_header.search(before).group() != license_header.search(after).group():
        changed_headers.append(name)

scope = [f"Protected files compared: {len(protected)}",
         f"Protected files changed since continuation start: {len(changed_protected)}",
         *changed_protected,
         f"Owned Java files checked: {len(owned)}",
         f"License headers changed: {len(changed_headers)}",
         *changed_headers]
for name in changed_protected:
    scope.extend([f"Concurrent protected change: {name}; author UNVERIFIED; not edited by this lane",
                  f"Starting SHA256: {protected[name]}",
                  f"Current SHA256: {hashlib.sha256((repo / name).read_bytes()).hexdigest()}"])
checks = [
    ["rtk", "git", "diff", "--quiet", "a8a90d46", "--", "xacml-core"],
    ["rtk", "git", "diff", "--check", "--", "xacml-pdp/src"],
    ["rtk", "git", "apply", "--check", str(evidence / "required-pom-fix.patch")],
    ["rtk", "git", "status", "--short"],
    ["rtk", "proxy", "git", "diff", "--name-only", "--", "xacml-pdp/src",
     "xacml-test/src", "xacml-conformance-tests/src/test/java"],
]
failed_checks = []
for command in checks:
    code, output = run(command)
    scope.extend(["", normalize(" ".join(command)), f"Exit code: {code}", output.rstrip()])
    if code != 0:
        failed_checks.append(command)
(evidence / "scope-check.txt").write_text("\n".join(scope) + "\n")

surefire = []
for name in ["06-pdp-tests.txt", "07-selector-characterization-before.txt",
             "08-selector-characterization-after.txt", "10-reactor-verify.txt",
             "13-reactor-verify-current-tree.txt"]:
    lines = (evidence / name).read_text().splitlines()
    surefire.extend(["", f"Run: {name}"])
    module = "unknown"
    in_results = False
    for line in lines:
        match = re.search(r"--- .* @ ([^ ]+) ---", line)
        if match:
            module = match.group(1)
        if re.search(r"\[INFO\] Results:", line):
            in_results = True
            surefire.extend([f"Module: {module}", line])
            continue
        if in_results:
            surefire.append(line)
            if "Tests run:" in line:
                in_results = False
        elif "Tests run:" in line and " - in " not in line:
            surefire.extend([f"Module: {module}", line])
        elif "Reactor Summary" in line or "BUILD SUCCESS" in line or "BUILD FAILURE" in line:
            surefire.append(line)
        elif re.search(r"\.\.\..*(SUCCESS|FAILURE|SKIPPED)", line):
            surefire.append(line)
    surefire.append("")
(evidence / "surefire-summary.txt").write_text("\n".join(surefire).lstrip() + "\n")

diagnostics = json.loads((evidence / "compile-diagnostics.json").read_text())
fixed = diagnostics["01-pdp-baseline-compile.txt"] + diagnostics["03-pdp-compile-after-path-import.txt"]
fixed += [d for d in diagnostics["05-pdp-test-compile.txt"]
          if "method single" in "\n".join(d["context"].splitlines()[:3])]
dispositions = {
    ("MultipleResourcesViaXPathExpressionHandler.java", 46): "Import public Path instead of removed top-level PathValue; cast the selected value to Path.",
    ("MultipleResourcesViaXPathExpressionHandler.java", 119): "Stream Entity.attributes() instead of Entity.stream().",
    ("MultipleResourcesViaXPathExpressionHandler.java", 145): "Pass path.getEvaluatesTo().getValueType() to transform().",
    ("MultipleResourcesViaXPathExpressionHandler.java", 146): "Cascading stream inference error resolved by the transform() value-type fix.",
    ("MultipleResourcesViaXPathExpressionHandler.java", 163): "Iterate Entity.attributes() instead of removed zero-argument find().",
    ("MultipleResourcesViaXPathExpressionLegacyHandler.java", 68): "Use Entity.findValues(id, type), retaining the XPath filter and value cardinality checks.",
    ("MultipleResourcesViaXPathExpressionLegacyHandler.java", 90): "Iterate Entity.attributes() instead of zero-argument find().",
    ("DefaultEvaluationContextHandler.java", 49): "Implement EvaluationContextHandler.getEntity() with Optional<Entity>.",
    ("DefaultEvaluationContextHandler.java", 131): "Resolve the designator with attribute id, data type and issuer.",
    ("DefaultEvaluationContextHandler.java", 132): "Cascading Optional inference error resolved by the designator-overload fix.",
    ("DefaultEvaluationContextHandler.java", 180): "Cast the single PIP bag value directly to Entity.",
    ("DefaultEvaluationContextHandler.java", 181): "Remove EntityValue.get(); Entity is itself the value.",
    ("DefaultEvaluationContextHandler.java", 196): "Replace the generic Optional<Content> method with Optional<Entity>, delegating to the request callback.",
    ("MultipleResourcesViaXacmlXPathHandlerTest.java", 184): "Use RequestContext.getAttribute(category, id), preserving all value/path assertions.",
}

def table(entries, disposition):
    rows = ["| Compiler file:line:column | Diagnostic | Disposition |",
            "| --- | --- | --- |"]
    for d in entries:
        location = f"{d['file']}:{d['line']}:{d['column']}"
        message = d["message"].replace("|", "\\|")
        rows.append(f"| `{location}` | {message} | {disposition(d)} |")
    return "\n".join(rows)

generated = ["\n<!-- generated compiler dispositions -->",
             f"### Fixed pre-existing PDP diagnostics ({len(fixed)})\n",
             table(fixed, lambda d: dispositions[(Path(d["file"]).name, d["line"])])]
transient = diagnostics["04-pdp-test-compile.txt"] + [
    d for d in diagnostics["05-pdp-test-compile.txt"]
    if "method permit" in "\n".join(d["context"].splitlines()[:3])]
generated.extend(["\n### Temporary errors introduced and corrected during this run\n",
                  table(transient, lambda d: "Use getEvaluatesTo().getValueType(); its stream inference error also disappears."
                        if Path(d["file"]).name == "MultipleResourcesViaXPathExpressionHandler.java"
                        else "New test setup uses Result.ok(Decision.PERMIT), not nonexistent Result.permit().")])
blocked = diagnostics["13-reactor-verify-current-tree.txt"]
generated.extend([f"\n### Unfixed xacml-test diagnostics ({len(blocked)})\n",
                  table(blocked, lambda d: "BLOCKED: JUnit is test scope; add the compile-scope dependency proposed in required-pom-fix.patch. POM is outside this packet.")])
summary = evidence / "summary.md"
base = summary.read_text().split("\n<!-- generated compiler dispositions -->")[0]
summary.write_text(base + "\n".join(generated) + "\n")

print("Gate: BUILD FAILURE at xacml-test (JUnit compile dependency)")
print("Full gate totals: 599 test methods, 0 failures, 0 errors, 9 skipped; conformance unrun")
print(f"Compiler dispositions: {len(fixed)} pre-existing PDP diagnostics fixed; {len(blocked)} JUnit diagnostics blocked")
print(f"Protected files: {len(protected)} checked, {len(changed_protected)} changed; license headers changed: {len(changed_headers)}")
print(f"Read-only checks failed: {len(failed_checks)}")
assert set(changed_protected).issubset({
    "xacml-xml/src/main/java/org/xacml4j/v30/xml/ExpressionTypeBuilder.java"})
assert not changed_headers and not failed_checks
