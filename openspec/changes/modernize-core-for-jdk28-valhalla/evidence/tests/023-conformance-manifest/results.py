"""Capture per-case outcomes without retaining Surefire XML trees or home paths."""
import csv
import json
import re
import sys
import xml.etree.ElementTree as ET
from collections import Counter
from pathlib import Path

RUN = Path(__file__).resolve().parent
REPO = RUN.parents[5]
MODULE = REPO / "xacml-conformance-tests"
CLASSES = {
    "Xacml20ConformanceTest": "oasis20",
    "Xacml30ConformanceTest": "oasis30",
    "RSA2008InteropTest": "rsa2008",
    "XacmlExampleTest": "example",
}


def normalize(text):
    return text.replace(str(REPO), "<repo>").replace(str(Path.home()), "<home>")


def capture(label):
    manifest = list(csv.DictReader((MODULE / "src/test/manifest/conformance-manifest.csv").open()))
    rows = []
    totals = {}
    for class_name, suite in CLASSES.items():
        root = ET.parse(MODULE / f"target/surefire-reports/TEST-org.xacml4j.v20.{class_name}.xml").getroot()
        totals[suite] = {key: int(root.attrib[key]) for key in ("tests", "failures", "errors", "skipped")}
        cases = [row for row in manifest if row["suite"] == suite]
        seen = set()
        for test in root.findall("testcase"):
            match = re.search(r"\[(\d+)\]$", test.attrib["name"])
            if not match:
                continue  # The two preserved testName assertions are not fixture cases.
            index = int(match.group(1)) - 1
            case = cases[index]
            assert case["case_id"] not in seen
            seen.add(case["case_id"])
            problem = next((child for child in test if child.tag in {"failure", "error", "skipped"}), None)
            outcome = "passed" if problem is None else {"failure": "failed", "error": "error", "skipped": "skipped"}[problem.tag]
            detail = "" if problem is None else normalize(problem.text or "")
            stdout = "\n".join(child.text or "" for child in test if child.tag == "system-out")
            if outcome != "skipped":
                assert "CONFORMANCE START " + suite + "/" + case["case_id"] in stdout, (class_name, test.attrib["name"], stdout)
            rows.append({**case, "outcome": outcome,
                         "exception": "" if problem is None else problem.attrib.get("type", ""),
                         "message": "" if problem is None else normalize(problem.attrib.get("message", "")),
                         "detail": detail})
        assert seen == {row["case_id"] for row in cases}, (suite, len(seen), len(cases))
    (RUN / f"{label}-case-results.json").write_text(json.dumps({"surefire": totals, "cases": rows}, indent=2) + "\n")
    print(json.dumps({"surefire": totals, "outcomes": {suite: dict(Counter(row["outcome"] for row in rows if row["suite"] == suite)) for suite in CLASSES.values()}}, indent=2))
    for row in rows:
        if row["outcome"] in {"failed", "error"}:
            print(f"{row['suite']}/{row['case_id']}: {row['exception']}: {row['message'][:300]}")


if __name__ == "__main__":
    capture(sys.argv[1])
