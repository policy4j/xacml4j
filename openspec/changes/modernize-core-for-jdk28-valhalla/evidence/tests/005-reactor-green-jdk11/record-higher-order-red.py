from pathlib import Path
import json
import xml.etree.ElementTree as ET

repo = Path.cwd()
ev = Path(__file__).parent
report = repo / "xacml-core/target/surefire-reports/TEST-org.xacml4j.v30.policy.function.impl.HigherOrderFunctionIdentifierTest.xml"
root = ET.parse(report).getroot()
assert [int(root.get(key)) for key in ("tests", "failures", "errors", "skipped")] == [11, 8, 0, 0]
before = json.loads((ev / "43-higher-order-source-before.json").read_text())
assert all((repo / name).read_text() == value for name, value in before.items())
root.remove(root.find("properties"))
(ev / "44-higher-order-identifier-red.xml").write_text(ET.tostring(root, encoding="unicode") + "\n")
results = []
for test in root.findall("testcase"):
    failure = test.find("failure")
    results.append({"test": test.get("name"), "failure": None if failure is None else failure.get("message")})
(ev / "44-higher-order-identifier-red.json").write_text(json.dumps(results, indent=2) + "\n")
assert sum(row["failure"] is not None and "Missing required function" in row["failure"] for row in results) == 4
print("Red receipt: 8 failures for four missing 3.0 IDs/legacy metadata; 3 unchanged standard names pass; production unchanged")
