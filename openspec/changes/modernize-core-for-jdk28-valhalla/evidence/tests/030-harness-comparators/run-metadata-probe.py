"""Run the evidence-only Java probe with the failed Surefire run's classpath."""
import json
import os
import subprocess
import xml.etree.ElementTree as ET
from pathlib import Path

RUN = Path(__file__).resolve().parent
REPO = RUN.parents[5]
report = REPO / "xacml-conformance-tests/target/surefire-reports/TEST-org.xacml4j.v20.Xacml30ConformanceTest.xml"
root = ET.parse(report).getroot()
properties = {p.attrib["name"]: p.attrib["value"] for p in root.find("properties")}
result = subprocess.run([
    "rtk", "proxy", str(Path(os.environ["JAVA_HOME"]) / "bin/java"), "--enable-preview",
    "--source", "21", "--class-path", properties["surefire.test.class.path"], str(RUN / "AssignmentMetadataProbe.java"),
    str(REPO / "xacml-conformance-tests/src/test/manifest/conformance-manifest.csv"),
], cwd=REPO, capture_output=True, text=True, timeout=90)
normalize = lambda value: value.replace(str(REPO), "<repo>").replace(str(Path.home()), "<home>")
(RUN / "09-assignment-metadata-probe.log").write_text(normalize(result.stdout + result.stderr))
receipts = [line for line in result.stdout.splitlines() if line.startswith("METADATA ")]
(RUN / "09-assignment-metadata-probe.json").write_text(json.dumps({
    "exit_code": result.returncode, "receipts": receipts,
    "interpretation": "Diagnostic confirmation of an output defect; not a passing conformance case",
}, indent=2) + "\n")
print(json.dumps({"exit_code": result.returncode, "receipts": receipts}, indent=2))
assert result.returncode == 0 and len(receipts) == 2, "See 09-assignment-metadata-probe.log"
