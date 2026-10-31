from pathlib import Path
import re
import xml.etree.ElementTree as ET


evidence = Path(__file__).parent
modules = [
    "xacml-core",
    "xacml-xml-jaxb",
    "xacml-xml",
    "xacml-json",
    "xacml-pdp",
    "xacml-test",
    "xacml-conformance-tests",
]

before_log = (evidence / "jdk11-before.log").read_text(errors="replace")
before = {}
current_module = None
for line in before_log.splitlines():
    module_match = re.search(
        r"--- surefire:[^ ]+:[^ ]+ \([^)]*\) @ ([^ ]+) ---", line
    )
    if module_match:
        current_module = module_match.group(1)
    result_match = re.search(
        r"Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)$",
        line,
    )
    if current_module and result_match:
        before[current_module] = tuple(map(int, result_match.groups()))

after = {}
errors = {}
for module in modules:
    reports = sorted((Path(module) / "target/surefire-reports").glob("TEST-*.xml"))
    roots = [ET.parse(report).getroot() for report in reports]
    after[module] = tuple(
        sum(int(root.get(key, "0")) for root in roots)
        for key in ("tests", "failures", "errors", "skipped")
    )
    for root in roots:
        for case in root.findall("testcase"):
            for kind in ("failure", "error"):
                result = case.find(kind)
                if result is not None:
                    errors.setdefault(root.get("name"), []).append(
                        (
                            kind,
                            case.get("name"),
                            result.get("type"),
                            result.get("message"),
                        )
                    )

lines = [
    "JDK 11 before release change — extracted from jdk11-before.log",
    "module | tests | failures | errors | skipped",
    "---|---:|---:|---:|---:",
]
for module in modules:
    lines.append(module + " | " + " | ".join(map(str, before.get(module, (0, 0, 0, 0)))))

lines += [
    "",
    "JDK 21 after release change — aggregated from current module Surefire XML reports",
    "module | tests | failures | errors | skipped",
    "---|---:|---:|---:|---:",
]
for module in modules:
    lines.append(module + " | " + " | ".join(map(str, after[module])))

lines += ["", "JDK 21 failure/error method inventory (all reported methods)"]
for class_name, cases in sorted(errors.items()):
    lines.append("- " + class_name + " (" + str(len(cases)) + "):")
    for kind, method, exception_type, message in cases:
        lines.append(
            "  - "
            + kind
            + " "
            + method
            + " — "
            + str(exception_type)
            + ": "
            + str(message)
        )

(evidence / "surefire-summary.txt").write_text("\n".join(lines) + "\n")
