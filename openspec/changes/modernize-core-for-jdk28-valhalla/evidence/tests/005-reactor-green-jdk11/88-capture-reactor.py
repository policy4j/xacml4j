from pathlib import Path
import hashlib
import json
import re
import sys
import xml.etree.ElementTree as ET

repo = Path.cwd()
ev = Path(__file__).parent
prefix = sys.argv[1]
assert prefix in ("88", "89")
log_name = prefix + ("-reactor-verify-round4-gate.txt" if prefix == "88" else "-reactor-verify-round4-gate-repeat.txt")
log = (ev / log_name).read_text()
start = json.loads((ev / (prefix + "-gate-start.json")).read_text())
counts, module = {}, "unknown"
for line in log.splitlines():
    match = re.search(r"--- .* @ ([^ ]+) ---", line)
    if match:
        module = match.group(1)
    count = re.search(r"Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)", line)
    if count and " - in " not in line:
        counts[module] = [int(value) for value in count.groups()]
all_modules = ("xacml", "xacml-core", "xacml-xml-jaxb", "xacml-xml", "xacml-json", "xacml-pdp", "xacml-test", "xacml-conformance-tests")
all_counts = {name: counts.get(name, [0,0,0,0] if name in ("xacml", "xacml-xml-jaxb") else None) for name in all_modules}
(ev / (prefix + "-module-counts.json")).write_text(json.dumps(all_counts, indent=2) + "\n")
reports, jdk_by_module = {}, {}
for module, expected in counts.items():
    total = [0,0,0,0]
    reports[module] = {}
    for path in (repo / module / "target/surefire-reports").glob("TEST-*.xml"):
        assert path.stat().st_mtime_ns > start["start_ns"], ("Stale Surefire report", path)
        root = ET.parse(path).getroot()
        row = [int(root.get(key)) for key in ("tests", "failures", "errors", "skipped")]
        total = [a + b for a, b in zip(total, row)]
        reports[module][root.get("name")] = row
        properties = {node.get("name"): node.get("value") for node in root.find("properties")}
        jdk = {key: properties[key] for key in ("java.version", "java.runtime.version", "java.vm.name", "java.vendor")}
        assert jdk["java.version"].startswith("11."), (module, jdk)
        jdk_by_module[module] = jdk
        if root.get("name") == "org.xacml4j.v30.spi.repository.InMemoryPolicyRepositoryTest":
            for properties_node in root.findall("properties"):
                root.remove(properties_node)
            for parent in root.iter():
                for child in list(parent):
                    if child.tag in ("system-out", "system-err"):
                        parent.remove(child)
            (ev / (prefix + "-InMemoryPolicyRepositoryTest.xml")).write_text(ET.tostring(root, encoding="unicode").replace(str(repo), "<repo>") + "\n")
    assert total == expected, (module, total, expected)
(ev / (prefix + "-surefire-report-counts.json")).write_text(json.dumps(reports, indent=2) + "\n")
(ev / (prefix + "-jdk-properties.json")).write_text(json.dumps(jdk_by_module, indent=2) + "\n")
exclusions = {}
for name in ("Xacml20ConformanceTest", "Xacml30ConformanceTest"):
    source = (repo / "xacml-conformance-tests/src/test/java/org/xacml4j/v20" / (name + ".java")).read_text()
    by_group = {}
    for method in re.findall(r"public void \w+\(\) throws Exception\s*\{(.*?)\n\t\}", source, re.DOTALL):
        group = re.search(r'executeXacmlConformanceTestCase\(skipTests, "([^"]+)"', method)
        if group:
            by_group[group.group(1)] = [group.group(1) + number.zfill(3) for number in re.findall(r"skipTests.add\((\d+)\)", method)]
    exclusions[name] = by_group
    assert sum(len(values) for values in by_group.values()) == 30
assert exclusions == json.loads((ev / "71-existing-case-exclusions.json").read_text())
(ev / (prefix + "-existing-case-exclusions.json")).write_text(json.dumps(exclusions, indent=2) + "\n")
source = repo / "xacml-pdp/src/test/java/org/xacml4j/v30/spi/repository/InMemoryPolicyRepositoryTest.java"
digest = hashlib.sha256(source.read_bytes()).hexdigest()
assert digest == start["source_sha256"]
receipt = {"log": log_name, "build_success": "BUILD SUCCESS" in log and "BUILD FAILURE" not in log, "totals": [sum(row[i] for row in counts.values()) for i in range(4)], "module_counts": all_counts, "source_sha256": digest, "fresh_report_modules": sorted(reports), "conformance_not_run": "xacml-conformance-tests" not in counts}
(ev / (prefix + "-gate-receipt.json")).write_text(json.dumps(receipt, indent=2) + "\n")
print(json.dumps(receipt, indent=2))
