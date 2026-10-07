from pathlib import Path
import hashlib
import json
import sys
import xml.etree.ElementTree as ET

repo = Path.cwd()
ev = Path(__file__).parent
prefix = sys.argv[1]
classes = ("HigherOrderVariadicFunctionsTest", "HigherOrderVariadicInvocationTest",
           "DurationFunctionIdentifierTest", "HigherOrderFunctionIdentifierTest", "HigherOrderFunctionsTest")
if len(sys.argv) > 2:
    classes = tuple(sys.argv[2].split(","))
results = []
for name in classes:
    report = repo / "xacml-core/target/surefire-reports" / ("TEST-org.xacml4j.v30.policy.function.impl." + name + ".xml")
    root = ET.parse(report).getroot()
    item = {"class": name, **{key: int(root.attrib[key]) for key in ("tests", "failures", "errors", "skipped")}, "cases": []}
    retained = ET.Element("testsuite", {key: root.attrib[key] for key in ("name", "tests", "failures", "errors", "skipped")})
    for case in root.findall("testcase"):
        record = {"name": case.attrib["name"], "problems": []}
        saved = ET.SubElement(retained, "testcase", case.attrib)
        for kind in ("failure", "error", "skipped"):
            for problem in case.findall(kind):
                attrs = {key: value.replace(str(repo), "<repo>") for key, value in problem.attrib.items()}
                content = (problem.text or "").replace(str(repo), "<repo>")
                ET.SubElement(saved, kind, attrs).text = content
                record["problems"].append({"kind": kind, **attrs, "text": content})
        item["cases"].append(record)
    ET.ElementTree(retained).write(ev / (prefix + "-" + name + ".xml"), encoding="unicode")
    results.append(item)
    print(f"{name}: tests={item['tests']} failures={item['failures']} errors={item['errors']} skipped={item['skipped']}")
snapshot = json.loads((ev / "57-round3-before.json").read_text())
production = {relative: hashlib.sha256((repo / relative).read_bytes()).hexdigest()
              for relative in snapshot["allowed_existing"][:1] + snapshot["conditional_existing_not_yet_authorized_by_red"]}
unchanged = all(snapshot["hashes"][relative] == digest for relative, digest in production.items())
(ev / (prefix + "-tests.json")).write_text(json.dumps({"results": results,
    "production_hashes": production, "production_matches_before_snapshot": unchanged}, indent=2) + "\n")
print(f"Production unchanged from pre-test snapshot: {unchanged}")
