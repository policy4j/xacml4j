"""Capture packet snapshots, fresh Surefire counts, and manifest case outcomes."""
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
MANIFEST = "xacml-conformance-tests/src/test/manifest/conformance-manifest.csv"
BLOCKERS = "openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/030-harness-comparators/conformance-blockers.md"
CLASSES = {
    "org.xacml4j.v30.types.BooleanValTest",
    "org.xacml4j.v30.types.BooleanLexicalTest",
    "org.xacml4j.v30.xml.BooleanRequestLexicalTest",
    "org.xacml4j.v30.marshal.json.BooleanRequestLexicalTest",
    "org.xacml4j.v20.Xacml20TestUtilityTest",
    "org.xacml4j.v20.Xacml30TestUtilityTest",
    "org.xacml4j.v30.XacmlPolicyTestSupportTest",
    "org.xacml4j.v20.ConformanceManifestTest",
}


def normalize(value):
    return value.replace(str(REPO), "<repo>").replace(str(Path.home()), "<home>")


def write_json(label, value):
    (RUN / (label + ".json")).write_text(json.dumps(value, indent=2) + "\n")


def snapshot(label):
    paths = [path for path in REPO.glob("xacml-*/src/**/*") if path.is_file()]
    paths += [REPO / "pom.xml", *REPO.glob("xacml-*/pom.xml"), REPO / BLOCKERS]
    hashes = {str(path.relative_to(REPO)): hashlib.sha256(path.read_bytes()).hexdigest()
              for path in sorted(paths)}
    write_json(label, hashes)
    print(json.dumps({"snapshot": label, "files": len(hashes)}))


def initial():
    assert not (RUN / "00-source-hashes.json").exists()
    snapshot("00-source-hashes")
    for relative in ("xacml-core/src/main/java/org/xacml4j/v30/types/BooleanVal.java",
                     "xacml-core/src/test/java/org/xacml4j/v30/types/BooleanValTest.java",
                     "xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java",
                     MANIFEST, BLOCKERS):
        (RUN / ("00-before-" + Path(relative).name)).write_bytes((REPO / relative).read_bytes())
    prior = REPO / "openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/030-harness-comparators"
    for name, target in (("11-full-reactor-results.json", "00-prior-module-counts.json"),
                         ("11-final-case-results.json", "00-prior-case-results.json")):
        (RUN / target).write_bytes((prior / name).read_bytes())


def modules(label, focused=None, selected_modules=None):
    result = {}
    for module in MODULES:
        if selected_modules is not None and module not in selected_modules:
            continue
        roots = [ET.parse(path).getroot() for path in sorted((REPO / module / "target/surefire-reports").glob("TEST-*.xml"))]
        if focused:
            selected = {name for name in CLASSES if
                        (focused == "lexical" and (name.endswith("BooleanValTest") or "BooleanLexicalTest" in name or "BooleanRequestLexicalTest" in name))
                        or (focused == "comparator" and ("TestUtilityTest" in name or name.endswith("XacmlPolicyTestSupportTest")))
                        or (focused == "manifest" and name.endswith("ConformanceManifestTest"))}
            roots = [root for root in roots if root.attrib["name"] in selected]
        if not roots:
            continue
        classes = {root.attrib["name"]: {key: int(root.attrib[key]) for key in COUNTS} for root in roots}
        failures = [{"class": root.attrib["name"], "test": case.attrib["name"], "kind": child.tag,
                     "message": normalize(child.attrib.get("message", ""))}
                    for root in roots for case in root.findall("testcase") for child in case
                    if child.tag in {"failure", "error"}]
        disabled = [{"class": root.attrib["name"], "test": case.attrib["name"],
                     "reason": normalize(case.find("skipped").attrib.get("message", ""))}
                    for root in roots for case in root.findall("testcase") if case.find("skipped") is not None]
        result[module] = {"counts": {key: sum(c[key] for c in classes.values()) for key in COUNTS},
                          "classes": classes, "failures": failures, "disabled": disabled}
    write_json(label, result)
    print(json.dumps({key: value["counts"] for key, value in result.items()}, indent=2))


def cases(label):
    manifest = list(csv.DictReader((REPO / MANIFEST).open()))
    rows, totals = [], {}
    for class_name, suite in (("Xacml20ConformanceTest", "oasis20"), ("Xacml30ConformanceTest", "oasis30"),
                              ("RSA2008InteropTest", "rsa2008"), ("XacmlExampleTest", "example")):
        root = ET.parse(REPO / f"xacml-conformance-tests/target/surefire-reports/TEST-org.xacml4j.v20.{class_name}.xml").getroot()
        totals[suite] = {key: int(root.attrib[key]) for key in COUNTS}
        entries = [row for row in manifest if row["suite"] == suite]
        seen = set()
        for case in root.findall("testcase"):
            match = re.search(r"\[(\d+)\]$", case.attrib["name"])
            if not match:
                continue
            entry = entries[int(match.group(1)) - 1]
            assert entry["case_id"] not in seen
            seen.add(entry["case_id"])
            problem = next((child for child in case if child.tag in {"failure", "error", "skipped"}), None)
            outcome = "passed" if problem is None else {"failure": "failed", "error": "error", "skipped": "skipped"}[problem.tag]
            stdout = "\n".join(child.text or "" for child in case if child.tag == "system-out")
            if outcome != "skipped":
                assert "CONFORMANCE START " + suite + "/" + entry["case_id"] in stdout
            rows.append({**entry, "outcome": outcome, "exception": "" if problem is None else problem.attrib.get("type", ""),
                         "message": "" if problem is None else normalize(problem.attrib.get("message", ""))})
        assert seen == {entry["case_id"] for entry in entries}
    outcomes = {suite: dict(Counter(row["outcome"] for row in rows if row["suite"] == suite)) for suite in totals}
    dispositions = {suite: dict(Counter(row["disposition"] for row in rows if row["suite"] == suite)) for suite in totals}
    write_json(label, {"surefire": totals, "outcomes": outcomes, "dispositions": dispositions, "cases": rows})
    with (RUN / (label + ".csv")).open("w", newline="") as output:
        writer = csv.DictWriter(output, fieldnames=list(rows[0]))
        writer.writeheader()
        writer.writerows(rows)
    print(json.dumps({"outcomes": outcomes, "dispositions": dispositions}, indent=2))


if __name__ == "__main__":
    action, *args = sys.argv[1:]
    if action == "initial":
        initial()
    elif action == "snapshot":
        snapshot(args[0])
    elif action == "modules":
        modules(args[0], selected_modules=args[1].split(",") if len(args) > 1 else None)
    elif action == "focused":
        modules(args[0], args[1] if len(args) > 1 else "lexical")
    elif action == "cases":
        cases(args[0])
    else:
        raise ValueError(action)
