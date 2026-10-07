"""Collect the full reactor gate without counting harness-control receipts as suite cases."""
from pathlib import Path
import csv
import hashlib
import json
import re
import xml.etree.ElementTree as ET

repo = Path.cwd().resolve()
evidence = Path(__file__).resolve().parent
modules = ["xacml-core", "xacml-xml-jaxb", "xacml-xml", "xacml-json", "xacml-pdp", "xacml-test", "xacml-conformance-tests"]
metrics = ["tests", "failures", "errors", "skipped"]
counts = {}
for module in modules:
    classes = []
    for path in sorted((repo / module / "target/surefire-reports").glob("TEST-*.xml")):
        root = ET.parse(path).getroot()
        classes.append({"class": root.get("name"), **{key: int(root.get(key, "0")) for key in metrics}})
    counts[module] = {"counts": {key: sum(row[key] for row in classes) for key in metrics}, "classes": classes}
total = {key: sum(module["counts"][key] for module in counts.values()) for key in metrics}
raw = (evidence / "05-full-reactor-clean-verify.log").read_text()
assert "[INFO] BUILD SUCCESS" in raw
assert total["failures"] == total["errors"] == 0, total
manifest = list(csv.DictReader((repo / "xacml-conformance-tests/src/test/manifest/conformance-manifest.csv").open()))
results = []
suite_classes = {"Xacml20ConformanceTest": "oasis20", "Xacml30ConformanceTest": "oasis30",
                 "RSA2008InteropTest": "rsa2008", "XacmlExampleTest": "example"}
for class_name, suite in suite_classes.items():
    report = repo / ("xacml-conformance-tests/target/surefire-reports/TEST-org.xacml4j.v20." + class_name + ".xml")
    root = ET.parse(report).getroot()
    output = "\n".join(element.text or "" for element in root.iter("system-out"))
    matches = re.findall(r"CONFORMANCE (PASS|SKIP|FAIL)\s+([^/\s]+)/([^\s]+)", output)
    assert all(found_suite == suite for state, found_suite, case_id in matches)
    suite_rows = [row for row in manifest if row["suite"] == suite]
    enabled = {(row["suite"], row["case_id"]) for row in suite_rows if row["enabled"] == "true"}
    found = {(found_suite, case_id) for state, found_suite, case_id in matches}
    assert enabled == found, (suite, enabled ^ found)
    assert len(matches) == len(found)
    assert all(state == "PASS" for state, found_suite, case_id in matches)
    results.extend({"status": state, "suite": found_suite, "case_id": case_id} for state, found_suite, case_id in matches)
    skipped = [row for row in suite_rows if row["enabled"] == "false"]
    assert len(skipped) == int(root.get("skipped")), suite
    results.extend({"status": "SKIP", "suite": suite, "case_id": row["case_id"]} for row in skipped)
assert len(results) == len(manifest) == 880
conformance = {}
for suite in sorted(suite_classes.values()):
    selected = [row for row in results if row["suite"] == suite]
    conformance[suite] = {state: sum(row["status"] == state for row in selected) for state in ["PASS", "SKIP", "FAIL"]}
    conformance[suite]["dispositions"] = {
        disposition: sum(row["suite"] == suite and row["disposition"] == disposition for row in manifest)
        for disposition in ["executed", "special-harness", "skipped"]
    }
before = json.loads((evidence / "05-reactor-scope-before.json").read_text())
files = sorted({*repo.glob("*/pom.xml"), repo / "pom.xml", *repo.glob("*/src/**/*.java"),
                repo / "xacml-conformance-tests/src/test/manifest/conformance-manifest.csv"})
after = {str(path.relative_to(repo)): hashlib.sha256(path.read_bytes()).hexdigest() for path in files if path.is_file()}
changed = [path for path in sorted(set(before) | set(after)) if before.get(path) != after.get(path)]
(evidence / "05-reactor-scope-after.json").write_text(json.dumps(after, indent=2) + "\n")
(evidence / "05-reactor-input-stability.json").write_text(json.dumps({"files_checked": len(before), "changed_during_gate": changed}, indent=2) + "\n")
payload = {"result": "BUILD SUCCESS", "jdk": json.loads((evidence / "jdk.json").read_text()),
           "module_results": counts, "totals": total, "conformance": conformance,
           "input_changes_during_gate": changed}
(evidence / "05-reactor-results.json").write_text(json.dumps(payload, indent=2) + "\n")
with (evidence / "conformance-case-results.csv").open("w") as output:
    writer = csv.DictWriter(output, fieldnames=["suite", "case_id", "status"])
    writer.writeheader()
    writer.writerows(sorted(results, key=lambda row: (row["suite"], row["case_id"])))
print(json.dumps({"module_counts": {module: result["counts"] for module, result in counts.items()},
                  "total": total, "conformance": conformance, "conformance_rows": len(results),
                  "build_inputs_checked": len(before), "input_changes": changed}))
