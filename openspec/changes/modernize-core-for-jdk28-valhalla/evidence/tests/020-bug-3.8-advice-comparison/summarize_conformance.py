from pathlib import Path
import re
import xml.etree.ElementTree as ET


log = Path(
    "openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/"
    "020-bug-3.8-advice-comparison/conformance-rv028.log"
).read_text()
markers = (
    ("RSA 2008", "Running org.xacml4j.v20.RSA2008InteropTest"),
    ("OASIS 2.0", "Running org.xacml4j.v20.Xacml20ConformanceTest"),
    ("OASIS 3.0", "Running org.xacml4j.v20.Xacml30ConformanceTest"),
    ("Examples", "Running org.xacml4j.v20.XacmlExampleTest"),
)
positions = [log.index(marker) for _, marker in markers] + [len(log)]
for index, (name, _) in enumerate(markers):
    section = log[positions[index]:positions[index + 1]]
    if name.startswith("OASIS"):
        print(f"{name}: {section.count('Executing test=')} cases")
    else:
        report_name = {
            "RSA 2008": "TEST-org.xacml4j.v20.RSA2008InteropTest.xml",
            "Examples": "TEST-org.xacml4j.v20.XacmlExampleTest.xml",
        }[name]
        report = ET.parse(Path("xacml-conformance-tests/target/surefire-reports", report_name)).getroot()
        print(
            f"{name}: tests={report.attrib['tests']} failures={report.attrib['failures']} "
            f"errors={report.attrib['errors']} skipped={report.attrib['skipped']}"
        )

for module in ("xacml-test", "xacml-conformance-tests"):
    totals = {key: 0 for key in ("tests", "failures", "errors", "skipped")}
    reports = Path(module, "target/surefire-reports")
    for report in reports.glob("TEST-*.xml"):
        root = ET.parse(report).getroot()
        for key in totals:
            totals[key] += int(root.attrib.get(key, 0))
    print(module, " ".join(f"{key}={value}" for key, value in totals.items()))

baseline = Path.cwd().with_name(Path.cwd().name + ".base")
header_pattern = re.compile(rb"/\*\s*\n\s*\* #%L.*?\n\s*\* #L%\n\s*\*/", re.DOTALL)
for name in (
    "xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java",
    "xacml-test/src/test/java/org/xacml4j/v20/Xacml30TestUtilityTest.java",
):
    current_header = header_pattern.search(Path(name).read_bytes())
    base_header = header_pattern.search((baseline / name).read_bytes())
    if current_header is None or base_header is None or current_header.group() != base_header.group():
        raise SystemExit(f"license header differs from baseline: {name}")
    print("header identical:", name)
