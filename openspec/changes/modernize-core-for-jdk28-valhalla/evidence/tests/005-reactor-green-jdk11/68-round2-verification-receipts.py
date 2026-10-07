from pathlib import Path
import hashlib
import json
import sys
import xml.etree.ElementTree as ET

repo = Path.cwd()
ev = Path(__file__).parent
prefix = sys.argv[1]
classes = [
    "org.xacml4j.v30.types.ISO8601DurationReviewTest",
    "org.xacml4j.v30.types.ISO8601FractionalDateTimeArithmeticTest",
    "org.xacml4j.v30.types.ISO8601DateTimeArithmeticTest",
    "org.xacml4j.v30.policy.function.impl.DateTimeArithmeticBoundaryTest",
]
snapshot = json.loads((ev / "66-round2-verification-before.json").read_text())
main = snapshot["allowed_existing"][0]
digest = hashlib.sha256((repo / main).read_bytes()).hexdigest()
if "Tests run:" not in (ev / (prefix + ".txt")).read_text():
    (ev / (prefix + "-tests.json")).write_text(json.dumps({"tests_executed": False,
        "reason": "Compilation failed before tests; previous Surefire reports are stale and excluded",
        "results": {}, "production_hash": digest,
        "production_matches_before_snapshot": snapshot["hashes"][main] == digest}, indent=2) + "\n")
    print("No tests executed: compilation failed before Surefire; stale reports excluded")
    sys.exit(0)
results = {}
for name in classes:
    path = repo / "xacml-core/target/surefire-reports" / ("TEST-" + name + ".xml")
    root = ET.parse(path).getroot()
    result = {"counts": [int(root.get(key)) for key in ("tests", "failures", "errors", "skipped")], "tests": []}
    saved = ET.Element("testsuite", {key: root.get(key) for key in ("name", "tests", "failures", "errors", "skipped")})
    for case in root.findall("testcase"):
        entry = {"name": case.get("name"), "problems": []}
        target = ET.SubElement(saved, "testcase", case.attrib)
        for tag in ("failure", "error", "skipped"):
            for problem in case.findall(tag):
                attrs = {key: value.replace(str(repo), "<repo>") for key, value in problem.attrib.items()}
                content = (problem.text or "").replace(str(repo), "<repo>")
                ET.SubElement(target, tag, attrs).text = content
                entry["problems"].append({"kind": tag, **attrs, "trace": content})
        result["tests"].append(entry)
    ET.ElementTree(saved).write(ev / (prefix + "-" + name.rsplit(".",1)[1] + ".xml"), encoding="unicode")
    results[name] = result
    print(name.rsplit(".",1)[1], result["counts"])
(ev / (prefix + "-tests.json")).write_text(json.dumps({"results": results,
    "production_hash": digest, "production_matches_before_snapshot": snapshot["hashes"][main] == digest}, indent=2) + "\n")
print("Production unchanged from pre-test snapshot:", snapshot["hashes"][main] == digest)
