"""Capture scoped hashes, fresh Surefire totals, and manifest case receipts."""
import csv
import hashlib
import json
import re
import sys
import xml.etree.ElementTree as ET
from collections import Counter
from pathlib import Path

RUN = Path(__file__).resolve().parent
REPO = RUN.parents[5]
MODULES = ("xacml-core", "xacml-xml", "xacml-json", "xacml-pdp", "xacml-test", "xacml-conformance-tests")
COUNTS = ("tests", "failures", "errors", "skipped")
CASE_CLASSES = {
    "Xacml20ConformanceTest": "oasis20",
    "Xacml30ConformanceTest": "oasis30",
    "RSA2008InteropTest": "rsa2008",
    "XacmlExampleTest": "example",
}


def normalize(value):
    return value.replace(str(REPO), "<repo>").replace(str(Path.home()), "<home>")


def write_json(label, value):
    (RUN / (label + ".json")).write_text(json.dumps(value, indent=2) + "\n")


def snapshot(label):
    paths = [path for path in REPO.glob("xacml-*/src/**/*") if path.is_file()]
    paths += [REPO / "pom.xml", *REPO.glob("xacml-*/pom.xml")]
    paths.append(REPO / "openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/023-conformance-manifest/inherited-skip-dispositions.csv")
    hashes = {str(path.relative_to(REPO)): hashlib.sha256(path.read_bytes()).hexdigest()
              for path in sorted(paths)}
    write_json(label, hashes)
    print(json.dumps({"snapshot": label, "files": len(hashes)}))


def reports(module):
    return [ET.parse(path).getroot() for path in sorted((REPO / module / "target/surefire-reports").glob("TEST-*.xml"))]


def capture_cases(label, only_suite=None):
    manifest = list(csv.DictReader((REPO / "xacml-conformance-tests/src/test/manifest/conformance-manifest.csv").open()))
    rows = []
    counts = {}
    for class_name, suite in CASE_CLASSES.items():
        if only_suite and only_suite != suite:
            continue
        root = ET.parse(REPO / f"xacml-conformance-tests/target/surefire-reports/TEST-org.xacml4j.v20.{class_name}.xml").getroot()
        counts[suite] = {key: int(root.attrib[key]) for key in COUNTS}
        cases = [row for row in manifest if row["suite"] == suite]
        seen = set()
        for test in root.findall("testcase"):
            match = re.search(r"\[(\d+)\]$", test.attrib["name"])
            if not match:
                continue  # Preserve the testName tests separately from fixture counts.
            case = cases[int(match.group(1)) - 1]
            assert case["case_id"] not in seen
            seen.add(case["case_id"])
            problem = next((child for child in test if child.tag in {"failure", "error", "skipped"}), None)
            outcome = "passed" if problem is None else {"failure": "failed", "error": "error", "skipped": "skipped"}[problem.tag]
            stdout = "\n".join(child.text or "" for child in test if child.tag == "system-out")
            if outcome != "skipped":
                assert "CONFORMANCE START " + suite + "/" + case["case_id"] in stdout, (suite, case["case_id"])
            rows.append({**case, "outcome": outcome,
                         "exception": "" if problem is None else problem.attrib.get("type", ""),
                         "message": "" if problem is None else normalize(problem.attrib.get("message", ""))})
        assert seen == {row["case_id"] for row in cases}, (suite, len(seen), len(cases))
    outcomes = {suite: dict(Counter(row["outcome"] for row in rows if row["suite"] == suite))
                for suite in counts}
    dispositions = {suite: dict(Counter(row["disposition"] for row in rows if row["suite"] == suite))
                    for suite in counts}
    write_json(label, {"surefire": counts, "outcomes": outcomes, "dispositions": dispositions, "cases": rows})
    with (RUN / (label + ".csv")).open("w", newline="") as output:
        writer = csv.DictWriter(output, fieldnames=list(rows[0]))
        writer.writeheader()
        writer.writerows(rows)
    print(json.dumps({"label": label, "surefire": counts, "outcomes": outcomes, "dispositions": dispositions}, indent=2))


def capture_modules(label, only_module=None):
    data = {}
    for module in MODULES:
        if only_module and only_module != module:
            continue
        roots = reports(module)
        assert roots, module
        classes = {root.attrib["name"]: {key: int(root.attrib[key]) for key in COUNTS} for root in roots}
        disabled = [{"class": root.attrib["name"], "test": test.attrib["name"],
                     "reason": normalize(test.find("skipped").attrib.get("message", ""))}
                    for root in roots for test in root.findall("testcase") if test.find("skipped") is not None]
        failures = [{"class": root.attrib["name"], "test": test.attrib["name"],
                     "kind": problem.tag, "message": normalize(problem.attrib.get("message", ""))}
                    for root in roots for test in root.findall("testcase")
                    for problem in test if problem.tag in {"failure", "error"}]
        data[module] = {"counts": {key: sum(row[key] for row in classes.values()) for key in COUNTS},
                        "classes": classes, "disabled": disabled, "failures": failures}
    write_json(label, data)
    print(json.dumps({module: entry["counts"] for module, entry in data.items()}, indent=2))


if __name__ == "__main__":
    action, label, *scope = sys.argv[1:]
    if action == "snapshot":
        snapshot(label)
    elif action == "cases":
        capture_cases(label, scope[0] if scope else None)
    elif action == "modules":
        capture_modules(label, scope[0] if scope else None)
    else:
        raise ValueError(action)
