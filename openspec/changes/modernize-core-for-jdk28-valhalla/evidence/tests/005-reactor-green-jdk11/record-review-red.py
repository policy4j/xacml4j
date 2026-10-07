from pathlib import Path
import json
import xml.etree.ElementTree as ET

repo = Path.cwd()
ev = Path(__file__).parent
before = json.loads((ev / "48-review-source-before.json").read_text())
assert all((repo / name).read_text() == source for name, source in before.items() if "/main/" in name)
reports = {}
for module, cls, expected in (
    ("xacml-core", "org.xacml4j.v30.types.ISO8601FractionalDateTimeArithmeticTest", [30,30,0,0]),
    ("xacml-core", "org.xacml4j.v30.types.ISO8601DurationReviewTest", [5,5,0,0]),
    ("xacml-pdp", "org.xacml4j.v30.pdp.DefaultEvaluationContextHandlerTest", None)):
    report = repo / module / "target/surefire-reports" / ("TEST-" + cls + ".xml")
    root = ET.parse(report).getroot()
    counts = [int(root.get(key)) for key in ("tests", "failures", "errors", "skipped")]
    if expected is not None:
        assert counts == expected, (cls, counts)
    else:
        assert counts[1:] == [1,0,0]
    results = []
    for test in root.findall("testcase"):
        failure = test.find("failure")
        results.append({"test": test.get("name"), "result": "PASS" if failure is None else "FAIL",
                        "failure": None if failure is None else failure.get("message")})
    root.remove(root.find("properties"))
    (ev / ("49-red-" + cls.split(".")[-1] + ".xml")).write_text(ET.tostring(root, encoding="unicode") + "\n")
    reports[cls] = {"counts": counts, "tests": results}
(ev / "49-review-red-tests.json").write_text(json.dumps(reports, indent=2) + "\n")
print("Red receipt: 35 core failures and one no-ContextSelectorId failure; all production sources unchanged")
