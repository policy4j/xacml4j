from pathlib import Path
import csv
import hashlib
import json
import re
import xml.etree.ElementTree as ET
repo = Path.cwd()
evidence = repo / "openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/064-manifest-audit/"
manifest_path = repo / "xacml-conformance-tests/src/test/manifest/conformance-manifest.csv"
rows = list(csv.DictReader(manifest_path.open()))
previous = {(row["suite"], row["case_id"]): row for row in csv.DictReader((repo / "openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/051-static-rejection-jaxb-boolean/rv064-fix/09-case-results.csv").open())}
fields = list(rows[0])
changed = [row for row in rows if any(row[field] != previous[(row["suite"], row["case_id"])][field] for field in fields)]
assert [(row["suite"], row["case_id"]) for row in changed] == [("oasis20", "IIIC001")]
suites = {}
for name in ("Xacml20ConformanceTest", "ConformanceManifestTest"):
    suite = ET.parse(repo / ("xacml-conformance-tests/target/surefire-reports/TEST-org.xacml4j.v20." + name + ".xml")).getroot()
    suites[name] = {field: int(suite.get(field, "0")) for field in ("tests", "failures", "errors", "skipped")}
oasis = [row for row in rows if row["suite"] == "oasis20"]
index = next(index for index, row in enumerate(oasis, 1) if row["case_id"] == "IIIC001")
report = ET.parse(repo / "xacml-conformance-tests/target/surefire-reports/TEST-org.xacml4j.v20.Xacml20ConformanceTest.xml").getroot()
method = next(method for method in report.findall("testcase") if method.get("name") == "testConformance(Case)[" + str(index) + "]")
assert all(method.find(field) is None for field in ("failure", "error", "skipped"))
result = {"case": "oasis20/IIIC001", "outcome": "passed", "test_method": method.get("name"), "suites": suites, "manifest_rows_changed": changed, "manifest_sha256": hashlib.sha256(manifest_path.read_bytes()).hexdigest(), "oasis20_cases": {"total": len(oasis), "enabled": sum(row["enabled"] == "true" for row in oasis), "skipped": sum(row["enabled"] == "false" for row in oasis)}}
(evidence / "results.json").write_text(json.dumps(result, indent=2) + "\n")
log = evidence / "01-oasis20-and-manifest-guard.log"
text = re.sub(r"/Users/[^/\s]+", "<home>", log.read_text().replace(str(repo), "<repo>"))
log.write_text(text)
lines = text.splitlines()
summary = []
for index, line in enumerate(lines):
    if "Tests run:" in line or "CONFORMANCE PASS oasis20/IIIC001" in line or "BUILD SUCCESS" in line:
        summary.append(line)
    elif "[INFO] Results:" in line:
        summary.extend(lines[index:index + 4])
(evidence / "surefire-summary.txt").write_text("\n".join(summary) + "\n")
print(json.dumps(result, indent=2))
