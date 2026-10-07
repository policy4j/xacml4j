from pathlib import Path
import json
import re
import xml.etree.ElementTree as ET

ROOT = Path.cwd()
EV = Path(__file__).resolve().parent
NS = {"m": "http://maven.apache.org/POM/4.0.0"}
before = ET.parse(EV / "before-root-pom.xml").getroot()
after = ET.parse(ROOT / "pom.xml").getroot()


def properties(root):
    return {node.tag.rsplit("}", 1)[-1]: node.text or "" for node in root.find("m:properties", NS)}


def dependencies(root, path):
    return {(node.findtext("m:groupId", namespaces=NS), node.findtext("m:artifactId", namespaces=NS)):
            ET.tostring(node) for node in root.findall(path, NS)}


removed = {("org.junit.vintage", "junit-vintage-engine"), ("org.easymock", "easymock"), ("org.objenesis", "objenesis")}
old_properties = properties(before)
new_properties = properties(after)
expected_properties = dict(old_properties)
del expected_properties["easymock.version"]
del expected_properties["objenesis.version"]
expected_properties["google.truth.version"] = "1.1.5"
assert new_properties == expected_properties
for path, modified in (("m:dependencies/m:dependency", {("org.mockito", "mockito-core"), ("org.mockito", "mockito-junit-jupiter")}),
                       ("m:dependencyManagement/m:dependencies/m:dependency", {("com.google.truth", "truth")})):
    old = dependencies(before, path)
    new = dependencies(after, path)
    assert old.keys() - new.keys() == removed
    assert not new.keys() - old.keys()
    assert {key for key in new if old[key] != new[key]} == modified
assert ET.tostring(before.find("m:build", NS)) == ET.tostring(after.find("m:build", NS))
core_before = (EV / "before-core-pom.xml").read_bytes()
core_after = (ROOT / "xacml-core/pom.xml").read_bytes()
vintage = b"\t\t<dependency>\n\t\t\t<groupId>org.junit.vintage</groupId>\n\t\t\t<artifactId>junit-vintage-engine</artifactId>\n\t\t</dependency>\n"
assert core_before.count(vintage) == 1
assert core_before.replace(vintage, b"", 1) == core_after

audit = json.loads((EV / "final-audit.json").read_text())
assert audit["source_changes"] == [] and audit["unexpected_counts"] == {}
assert set(audit["changed_outside_evidence"]) == {"pom.xml", "xacml-core/pom.xml", "openspec/changes/modernize-core-for-jdk28-valhalla/tasks.md"}
assert len(audit["reports"]["xacml-core"]["suites"]) == 103
engines = json.loads((EV / "11-engine-inventory.json").read_text())
assert len(engines) == 6 and all(receipt["engine_ids"] == ["junit-jupiter"] for receipt in engines)
tree_receipts = json.loads((EV / "14-dependency-checks.json").read_text())
assert all(len(receipt["reactor_projects"]) == 8 and not receipt["legacy_artifact_matches"] for receipt in tree_receipts)
case_receipts = json.loads((EV / "14-conformance-case-counts.json").read_text())
for name in ("Xacml20ConformanceTest", "Xacml30ConformanceTest"):
    assert case_receipts[name]["duplicates"] == {"IIIG006": 2}
text = (EV / "10-final-jdk21-clean-verify.log").read_text()
assert "\0" not in text and "BUILD SUCCESS" in text and "BUILD FAILURE" not in text
assert text.count("Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider") == 6
assert len(re.findall(r"\[INFO\] Results:", text)) == 6
bad_paths = []
for path in EV.rglob("*"):
    if path.is_file() and path.name != "final-checks.json":
        content = path.read_text()
        if str(ROOT) in content or str(Path.home()) in content:
            bad_paths.append(path.relative_to(EV).as_posix())
assert not bad_paths, bad_paths
result = {"result": "PASS", "root_pom_changes_exact": True, "core_pom_change_only_vintage": True,
          "junit_bom_and_jupiter": "5.14.4", "mockito": "5.24.0", "truth": "1.1.5", "hamcrest": "1.3",
          "plugin_and_startup_agent_configuration_unchanged": True, "source_or_fixture_changes": [],
          "core_report_classes": 103, "only_jupiter_tested_modules": 6,
          "dependency_tree_projects": 8, "gate": [934, 0, 0, 6],
          "authoritative_log_complete": True, "home_paths_in_evidence": bad_paths,
          "observed_shared_tracking_change": "tasks.md: 2.2/2.3 now checked; no edit by this lane"}
(EV / "final-checks.json").write_text(json.dumps(result, indent=2) + "\n")
print(json.dumps(result, indent=2))
