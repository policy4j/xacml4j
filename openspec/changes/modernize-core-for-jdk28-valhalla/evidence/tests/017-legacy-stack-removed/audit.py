from pathlib import Path
import hashlib
import json
import re
import subprocess
import sys
import xml.etree.ElementTree as ET

ROOT = Path.cwd()
EV = Path(__file__).resolve().parent
MODULES = (
    "xacml-core", "xacml-pdp", "xacml-json", "xacml-xml",
    "xacml-xml-jaxb", "xacml-test", "xacml-conformance-tests",
)
EXPECTED = {
    "xacml-core": [760, 0, 0, 2],
    "xacml-pdp": [73, 0, 0, 3],
    "xacml-json": [5, 0, 0, 1],
    "xacml-xml": [34, 0, 0, 0],
    "xacml-xml-jaxb": [0, 0, 0, 0],
    "xacml-test": [20, 0, 0, 0],
    "xacml-conformance-tests": [42, 0, 0, 0],
}


def git(*args):
    return subprocess.check_output(["rtk", "proxy", "git", *args])


def snapshot():
    names = git("ls-files", "-z", "--cached", "--others", "--exclude-standard")
    files = {}
    for raw in sorted(set(names.split(b"\0")) - {b""}):
        name = raw.decode()
        if name.startswith("openspec/changes/modernize-core-for-jdk28-valhalla/evidence/"):
            continue
        path = ROOT / name
        if path.is_file():
            files[name] = hashlib.sha256(path.read_bytes()).hexdigest()
    return files


def reports():
    result = {}
    for module in MODULES:
        suites = []
        totals = [0, 0, 0, 0]
        for path in sorted((ROOT / module / "target/surefire-reports").glob("TEST-*.xml")):
            suite = ET.parse(path).getroot()
            counts = [int(suite.get(key, "0")) for key in ("tests", "failures", "errors", "skipped")]
            totals = [a + b for a, b in zip(totals, counts)]
            suites.append({
                "class": suite.get("name"), "counts": counts,
                "tests": [
                    {"class": test.get("classname"), "name": test.get("name"),
                     "skipped": test.find("skipped") is not None}
                    for test in suite.findall("testcase")
                ],
            })
        result[module] = {"counts": totals, "classes": len(suites), "suites": suites}
    return result


def normalize_logs():
    replacements = [(str(ROOT), "<repo>"), (str(Path.home()), "<home>")]
    for path in EV.rglob("*"):
        if not path.is_file():
            continue
        content = path.read_text()
        for old, new in replacements:
            content = content.replace(old, new)
        path.write_text(content)


if sys.argv[1] == "prepare":
    before = {"files": snapshot(), "reports": reports(),
              "branch": git("branch", "--show-current").decode().strip(),
              "head": git("rev-parse", "HEAD").decode().strip()}
    (EV / "before-state.json").write_text(json.dumps(before, indent=2) + "\n")
    for name, copy in (("pom.xml", "before-root-pom.xml"), ("xacml-core/pom.xml", "before-core-pom.xml")):
        (EV / copy).write_bytes((ROOT / name).read_bytes())
    print(json.dumps({"branch": before["branch"], "head": before["head"],
                      "files": len(before["files"]),
                      "existing_report_totals": {key: value["counts"] for key, value in before["reports"].items()}}, indent=2))
elif sys.argv[1] == "normalize":
    normalize_logs()
    print("Replaced checkout and home paths in evidence artifacts after command completion.")
elif sys.argv[1] == "finish":
    normalize_logs()
    before = json.loads((EV / "before-state.json").read_text())
    after = snapshot()
    changed = [name for name in sorted(before["files"].keys() | after.keys())
               if before["files"].get(name) != after.get(name)]
    results = reports()
    unexpected_counts = {module: value["counts"] for module, value in results.items()
                         if value["counts"] != EXPECTED[module]}
    source_changes = [name for name in changed if "/src/" in name]
    artifact = {"changed_outside_evidence": changed, "source_changes": source_changes,
                "counts": {key: value["counts"] for key, value in results.items()},
                "unexpected_counts": unexpected_counts,
                "reports": results}
    (EV / "final-audit.json").write_text(json.dumps(artifact, indent=2) + "\n")
    print(json.dumps({key: value for key, value in artifact.items() if key != "reports"}, indent=2))
    if unexpected_counts or source_changes:
        sys.exit(1)
else:
    raise SystemExit("Use prepare, normalize, or finish.")
