from pathlib import Path
import json
import xml.etree.ElementTree as ET

repo = Path.cwd()
ev = Path(__file__).parent
report = repo / "xacml-core/target/surefire-reports/TEST-org.xacml4j.v30.policy.function.impl.DurationFunctionIdentifierTest.xml"
root = ET.parse(report).getroot()
assert [int(root.get(key)) for key in ("tests", "failures", "errors", "skipped")] == [36, 36, 0, 0]
before = json.loads((ev / "39-identifier-sources-before.json").read_text())
assert all((repo / name).read_text() == value for name, value in before.items())
root.remove(root.find("properties"))
(ev / "39-duration-identifier-red.xml").write_text(ET.tostring(root, encoding="unicode") + "\n")
results = []
for test in root.findall("testcase"):
    failure = test.find("failure")
    results.append({"test": test.get("name"), "failure": failure.get("message")})
(ev / "39-duration-identifier-red.json").write_text(json.dumps(results, indent=2) + "\n")
assert sum("Missing required function" in row["failure"] for row in results) == 18
print("Red receipt: 36 failures; 18 missing standard identifiers; 18 old-ID metadata assertions; production unchanged")
