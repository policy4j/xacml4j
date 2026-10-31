"""Capture task-scoped source hashes, Surefire totals, and manifest outcomes."""
import csv
import hashlib
import json
import re
import sys
import xml.etree.ElementTree as ET
from collections import Counter
from pathlib import Path

RUN = Path(__file__).resolve().parent
REPO = next(parent for parent in RUN.parents if (parent / "pom.xml").is_file())
MODULES = ("xacml-core", "xacml-xml", "xacml-json", "xacml-pdp", "xacml-test", "xacml-conformance-tests")
COUNTS = ("tests", "failures", "errors", "skipped")
MANIFEST = "xacml-conformance-tests/src/test/manifest/conformance-manifest.csv"


def normalize(value):
    return value.replace(str(REPO), "<repo>").replace(str(Path.home()), "<home>")


def write_json(label, value):
    (RUN / (label + ".json")).write_text(json.dumps(value, indent=2) + "\n")


def snapshot(label):
    paths = [path for path in REPO.glob("xacml-*/src/**/*") if path.is_file()]
    paths += [REPO / "pom.xml", *REPO.glob("xacml-*/pom.xml")]
    write_json(label, {str(path.relative_to(REPO)): hashlib.sha256(path.read_bytes()).hexdigest()
                       for path in sorted(paths)})
    print(json.dumps({"snapshot": label, "files": len(paths)}))


def modules(label, names=None):
    result = {}
    for module in MODULES:
        roots = [ET.parse(path).getroot() for path in sorted((REPO / module / "target/surefire-reports").glob("TEST-*.xml"))]
        if names is not None:
            roots = [root for root in roots if root.attrib["name"].rsplit(".", 1)[-1] in names]
        if not roots:
            continue
        classes = {root.attrib["name"]: {key: int(root.attrib[key]) for key in COUNTS} for root in roots}
        failures = [{"class": root.attrib["name"], "test": case.attrib["name"], "kind": child.tag,
                     "message": normalize(child.attrib.get("message", "")), "detail": normalize(child.text or "")}
                    for root in roots for case in root.findall("testcase") for child in case
                    if child.tag in {"failure", "error"}]
        result[module] = {"counts": {key: sum(values[key] for values in classes.values()) for key in COUNTS},
                          "classes": classes, "failures": failures}
    write_json(label, result)
    print(json.dumps({module: value["counts"] for module, value in result.items()}, indent=2))


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
                         "message": "" if problem is None else normalize(problem.attrib.get("message", "")),
                         "static_rejection": " | ".join(line for line in stdout.splitlines() if "CONFORMANCE REJECTION " in line)})
        assert seen == {entry["case_id"] for entry in entries}
    outcomes = {suite: dict(Counter(row["outcome"] for row in rows if row["suite"] == suite)) for suite in totals}
    dispositions = {suite: dict(Counter(row["disposition"] for row in rows if row["suite"] == suite)) for suite in totals}
    write_json(label, {"surefire": totals, "outcomes": outcomes, "dispositions": dispositions, "cases": rows})
    with (RUN / (label + ".csv")).open("w", newline="") as output:
        writer = csv.DictWriter(output, fieldnames=list(rows[0]))
        writer.writeheader()
        writer.writerows(rows)
    print(json.dumps({"outcomes": outcomes, "dispositions": dispositions}, indent=2))


def initial():
    assert not (RUN / "00-source-hashes.json").exists()
    snapshot("00-source-hashes")
    for relative in (MANIFEST,
                     "xacml-conformance-tests/src/test/java/org/xacml4j/v20/ConformanceCaseRunner.java",
                     "xacml-xml/src/main/java/org/xacml4j/v30/xml/BaseJAXBUnmarshaller.java",
                     "xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml20PolicyFromJaxbToObjectModelMapper.java",
                     "xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30PolicyFromJaxbToObjectModelMapper.java"):
        (RUN / ("00-before-" + Path(relative).name)).write_bytes((REPO / relative).read_bytes())
    modules("00-existing-reports")
    cases("00-existing-case-results")


def audit():
    before = json.loads((RUN / "00-source-hashes.json").read_text())
    packet_state = json.loads((RUN / "05-after-full-gate.json").read_text())
    latest_gate = "07" if (RUN / "07-after-full-gate.json").exists() else "05"
    final_snapshot = RUN / ("08-final-source.json" if latest_gate == "07" else "06-final-source.json")
    after = json.loads((final_snapshot if final_snapshot.exists() else RUN / (latest_gate + "-after-full-gate.json")).read_text())
    build_before = json.loads((RUN / (latest_gate + "-before-full-gate.json")).read_text())
    changed = [path for path in sorted(set(before) | set(packet_state)) if before.get(path) != packet_state.get(path)]
    concurrent_changes = [path for path in sorted(set(packet_state) | set(after)) if packet_state.get(path) != after.get(path)]
    build_changes = [path for path in sorted(set(build_before) | set(after)) if build_before.get(path) != after.get(path)]
    expected = {
        MANIFEST,
        "xacml-conformance-tests/src/test/java/org/xacml4j/v20/ConformanceCaseRunner.java",
        "xacml-conformance-tests/src/test/java/org/xacml4j/v20/ConformanceStaticRejectionTest.java",
        "xacml-xml/src/main/java/org/xacml4j/v30/xml/BaseJAXBUnmarshaller.java",
        "xacml-xml/src/main/java/org/xacml4j/v30/xml/BooleanAttributeValidationFilter.java",
        "xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml20PolicyFromJaxbToObjectModelMapper.java",
        "xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30PolicyFromJaxbToObjectModelMapper.java",
        "xacml-xml/src/test/java/org/xacml4j/v30/xml/BooleanAttributeLexicalTest.java",
    }
    assert set(changed) == expected, changed
    assert not set(concurrent_changes).intersection(expected), concurrent_changes
    assert not build_changes, build_changes
    headers = {}
    for path in changed:
        if not path.endswith(".java"):
            continue
        current = (REPO / path).read_bytes()
        assert b"#%L" in current and b"#L%" in current
        original = RUN / ("00-before-" + Path(path).name)
        if original.exists():
            prior = original.read_bytes()
            assert prior[prior.index(b"/*"):prior.index(b"*/") + 2] == current[current.index(b"/*"):current.index(b"*/") + 2]
            headers[path] = "existing header byte-identical"
        else:
            template_name = "ConformanceCaseRunner.java" if path.startswith("xacml-conformance-tests/") else "BaseJAXBUnmarshaller.java"
            template = (RUN / ("00-before-" + template_name)).read_bytes()
            assert template[template.index(b"/*"):template.index(b"*/") + 2] == current[current.index(b"/*"):current.index(b"*/") + 2]
            headers[path] = "new file uses module LGPL header"
    base = json.loads((RUN / "00-existing-reports.json").read_text())
    final = json.loads((RUN / (latest_gate + "-full-gate.json")).read_text())
    count_changes = {}
    for module in MODULES:
        changes = {name: {"existing_report": base[module]["classes"].get(name), "fresh_gate": counts}
                   for name, counts in final[module]["classes"].items()
                   if counts != base[module]["classes"].get(name)}
        if changes:
            count_changes[module] = changes
    base_cases = {row["suite"] + "/" + row["case_id"]: row for row in json.loads((RUN / "00-existing-case-results.json").read_text())["cases"]}
    final_cases = json.loads((RUN / (latest_gate + "-case-results.json")).read_text())["cases"]
    outcomes = [{"case": row["suite"] + "/" + row["case_id"],
                 "before": base_cases[row["suite"] + "/" + row["case_id"]]["outcome"], "after": row["outcome"],
                 "rejection": row["static_rejection"]}
                for row in final_cases if base_cases[row["suite"] + "/" + row["case_id"]]["outcome"] != row["outcome"]]
    write_json("scope-and-result-audit", {"source_files_changed": changed, "concurrent_source_changes": concurrent_changes,
                                         "final_gate": latest_gate, "build_source_changes": build_changes,
                                         "headers": headers, "per_class_count_changes": count_changes,
                                         "case_outcome_changes": outcomes})
    print(json.dumps({"source_files_changed": changed, "concurrent_source_changes": concurrent_changes,
                      "final_gate": latest_gate, "build_source_changes": build_changes,
                      "per_class_count_changes": count_changes, "case_outcome_changes": outcomes}, indent=2))


if __name__ == "__main__":
    action, *args = sys.argv[1:]
    if action == "initial":
        initial()
    elif action == "snapshot":
        snapshot(args[0])
    elif action == "modules":
        modules(args[0], set(args[1].split(",")) if len(args) > 1 else None)
    elif action == "cases":
        cases(args[0])
    elif action == "sanitize":
        for path in RUN.glob("*.log"):
            path.write_text(normalize(path.read_text()))
    elif action == "audit":
        audit()
    else:
        raise ValueError(action)
