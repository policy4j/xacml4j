from pathlib import Path
import json
import re
import sys
import xml.etree.ElementTree as ET

repo = Path.cwd()
ev = Path(__file__).parent
prefix = sys.argv[1]
suites = {}
for report in (repo / "xacml-conformance-tests/target/surefire-reports").glob("TEST-*.xml"):
    root = ET.parse(report).getroot()
    cases = []
    for tc in root.findall("testcase"):
        output = tc.findtext("system-out", "")
        ids = re.findall(r'Executing test="([^"]+)"', output)
        failure = tc.find("failure")
        if failure is None:
            failure = tc.find("error")
        cases.append({"method": tc.attrib["name"], "case_ids": ids,
                      "failure": None if failure is None else {
                          "type": failure.attrib.get("type"), "message": failure.attrib.get("message"),
                          "trace": (failure.text or "").replace(str(repo), "<repo>")}})
        if failure is not None:
            (ev / (prefix + "-" + root.attrib["name"].split(".")[-1] + "-" + tc.attrib["name"] + "-tail.txt")).write_text(
                    output[-16000:].replace(str(repo), "<repo>"))
    suites[root.attrib["name"]] = {"counts": {k: root.attrib.get(k) for k in ["tests", "failures", "errors", "skipped"]},
                                  "testcases": cases}
(ev / (prefix + "-conformance-testcases.json")).write_text(json.dumps(suites, indent=2) + "\n")
for name, suite in suites.items():
    print(name, suite["counts"], "case executions", sum(len(c["case_ids"]) for c in suite["testcases"]))
    for case in suite["testcases"]:
        if case["failure"]:
            print(case["method"], "LAST CASE", case["case_ids"][-1:], case["failure"]["message"])
