from pathlib import Path
import csv
import difflib
import hashlib
import json
import re
import sys
import xml.etree.ElementTree as ET

repo = Path.cwd()
evidence = repo / "openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/051-static-rejection-jaxb-boolean/rv064-fix/"
prefix = sys.argv[1] if len(sys.argv) > 1 else "06"
counts = ("tests", "failures", "errors", "skipped")
modules = ("xacml-core", "xacml-xml-jaxb", "xacml-xml", "xacml-json", "xacml-pdp", "xacml-test", "xacml-conformance-tests")
suites = {module: [ET.parse(path).getroot() for path in (repo / module / "target/surefire-reports").glob("TEST-*.xml")] for module in modules}
module_counts = {module: {name: sum(int(suite.get(name, "0")) for suite in values) for name in counts} for module, values in suites.items()}
jdk = {item.get("name"): item.get("value") for values in suites.values() for suite in values for item in suite.findall("properties/property") if item.get("name") in ("java.version", "java.runtime.version", "java.vendor")}
manifest = list(csv.DictReader((repo / "xacml-conformance-tests/src/test/manifest/conformance-manifest.csv").open()))
classes = {"oasis20": "Xacml20ConformanceTest", "oasis30": "Xacml30ConformanceTest", "rsa2008": "RSA2008InteropTest", "example": "XacmlExampleTest"}
case_results = []
for group, name in classes.items():
    rows = [row for row in manifest if row["suite"] == group]
    suite = next(suite for suite in suites["xacml-conformance-tests"] if suite.get("name").endswith("." + name))
    methods = {int(match.group(1)): method for method in suite.findall("testcase") if (match := re.search(r"\[(\d+)\]$", method.get("name", "")))}
    assert len(methods) == len(rows), (group, len(methods), len(rows))
    for index, row in enumerate(rows, 1):
        method = methods[index]
        result = "failed" if method.find("failure") is not None or method.find("error") is not None else "skipped" if method.find("skipped") is not None else "passed"
        case_results.append({**row, "outcome": result})
previous = {(row["suite"], row["case_id"]): row["outcome"] for row in csv.DictReader((evidence.parent / "07-case-results.csv").open())}
changes = [row for row in case_results if previous[(row["suite"], row["case_id"])] != row["outcome"]]
with (evidence / (prefix + "-case-results.csv")).open("w", newline="") as output:
    writer = csv.DictWriter(output, fieldnames=list(case_results[0]))
    writer.writeheader()
    writer.writerows(case_results)
totals = {group: {"manifest": sum(row["suite"] == group for row in case_results), **{result: sum(row["suite"] == group and row["outcome"] == result for row in case_results) for result in ("passed", "skipped", "failed")}} for group in classes}
result = {"jdk": jdk, "modules": module_counts, "total": {name: sum(module[name] for module in module_counts.values()) for name in counts}, "conformance": totals, "changed_cases": changes}
(evidence / (prefix + "-gate-results.json")).write_text(json.dumps(result, indent=2) + "\n")
before_hashes = json.loads((evidence / ("pre-gate-source-hashes.json" if prefix == "06" else prefix + "-pre-gate-source-hashes.json")).read_text())
changed_sources = [name for name, digest in before_hashes.items() if hashlib.sha256((repo / name).read_bytes()).hexdigest() != digest]
original = (evidence / "before-BaseJAXBUnmarshaller.java.txt").read_text()
current = (repo / "xacml-xml/src/main/java/org/xacml4j/v30/xml/BaseJAXBUnmarshaller.java").read_text()
header = lambda text: text[text.index("/*"):text.index("*/") + 2]
test_path = "xacml-xml/src/test/java/org/xacml4j/v30/xml/BaseJAXBUnmarshallerSecurityTest.java"
test = (repo / test_path).read_text()
audit = {"source_hashes_checked_during_gate": len(before_hashes), "source_files_changed_during_gate": changed_sources, "existing_header_unchanged": header(original) == header(current), "new_test_header_matches": header(current) == header(test), "production_files_changed_by_this_fix": ["xacml-xml/src/main/java/org/xacml4j/v30/xml/BaseJAXBUnmarshaller.java"], "new_test_files": [test_path], "manifest_sha256": hashlib.sha256((repo / "xacml-conformance-tests/src/test/manifest/conformance-manifest.csv").read_bytes()).hexdigest()}
(evidence / "scope-audit.json").write_text(json.dumps(audit, indent=2) + "\n")
(evidence / (prefix + "-scope-audit.json")).write_text(json.dumps(audit, indent=2) + "\n")
patch = "".join(difflib.unified_diff(original.splitlines(True), current.splitlines(True), fromfile="a/xacml-xml/src/main/java/org/xacml4j/v30/xml/BaseJAXBUnmarshaller.java", tofile="b/xacml-xml/src/main/java/org/xacml4j/v30/xml/BaseJAXBUnmarshaller.java"))
patch += "".join(difflib.unified_diff([], test.splitlines(True), fromfile="/dev/null", tofile="b/" + test_path))
(evidence / "changes.patch").write_text(patch)
for path in evidence.glob("*.log"):
    text = path.read_text()
    text = text.replace(str(repo), "<repo>")
    text = re.sub(r"/Users/[^/\s]+", "<home>", text)
    path.write_text(text)
lines = []
for path in sorted(evidence.glob("*.log")):
    lines.append("COMMAND LOG: " + path.name)
    text = path.read_text().splitlines()
    for index, line in enumerate(text):
        if "Tests run:" in line or "BUILD SUCCESS" in line or "BUILD FAILURE" in line:
            lines.append(line)
        elif "[INFO] Results:" in line:
            lines.extend(text[index:min(index + 4, len(text))])
    lines.append("")
(evidence / "surefire-summary.txt").write_text("\n".join(lines) + "\n")
print(json.dumps(result, indent=2))
print(json.dumps(audit, indent=2))
