from pathlib import Path
import hashlib
import json
import re
import subprocess
import xml.etree.ElementTree as ET

repo = Path.cwd()
ev = Path(__file__).parent
for name in ("39-duration-identifier-red.xml", "44-higher-order-identifier-red.xml"):
    root = ET.fromstring((ev / name).read_text().replace("<repo>", "&lt;repo&gt;"))
    properties = root.find("properties")
    if properties is not None:
        root.remove(properties)
        (ev / name).write_text(ET.tostring(root, encoding="unicode") + "\n")
summary = (ev / "summary.md").read_text()
commands = (ev / "command.txt").read_text()
surefire = (ev / "surefire-summary.txt").read_text()
assert "Result: **PASS**" in summary and "774" in summary
assert "RESOLVED, JDK 11 reactor green" in (ev / "conformance-blockers.md").read_text()
assert "HigherOrderFunctions.java:54/72/90/169" in summary
expected = ["xacml-core | 603 | 0 | 0 | 4", "xacml-xml | 34 | 0 | 0 | 0",
            "xacml-json | 5 | 0 | 0 | 1", "xacml-pdp | 71 | 0 | 0 | 4",
            "xacml-test | 20 | 0 | 0 | 0", "xacml-conformance-tests | 41 | 0 | 0 | 0"]
assert all(value in summary for value in expected)
gate_section = surefire.split("Run: 46-reactor-verify-higher-order-identifiers.txt", 1)[1]
assert gate_section.count("[INFO] Results:") == 6
assert "BUILD SUCCESS" in gate_section and "BUILD FAILURE" not in gate_section
assert len(re.findall(r"Tests run: \d+, Failures: 0, Errors: 0, Skipped: \d+", gate_section)) == 6
for name in ("39-duration-identifiers-before.txt", "40-duration-identifiers-after.txt",
             "41-duration-identifiers-after.txt", "42-reactor-verify-duration-identifiers.txt",
             "44-higher-order-identifiers-before.txt", "45-higher-order-identifiers-after.txt",
             "46-reactor-verify-higher-order-identifiers.txt"):
    assert name in commands and name in surefire
    assert str(repo) not in (ev / name).read_text()
    maven = [line for line in commands.splitlines() if "rtk mvn" in line and name in line]
    assert len(maven) == 1 and 'JAVA_HOME="$(jenv prefix 11)"' in maven[0]
    assert "-Dlicense.skipUpdateLicense=true" in maven[0]
for value in (summary, commands, surefire, (ev / "scope-check.txt").read_text()):
    assert str(repo) not in value
protected = json.loads((ev / "43-protected-before-higher-order-identifiers.json").read_text())
assert all((repo / name).is_file() and hashlib.sha256((repo / name).read_bytes()).hexdigest() == digest
           for name, digest in protected.items())
for command, expected in ((["rtk", "git", "branch", "--show-current"], "integration/modernize-core-jdk28-valhalla"),
                          (["rtk", "git", "rev-parse", "HEAD"], "befdd116bd11385ac8d007732fa8e4d347af3143")):
    result = subprocess.run(command, capture_output=True, text=True)
    assert result.returncode == 0 and result.stdout.strip() == expected
    print(" ".join(command) + ": " + expected)
print("Final evidence audit PASS: exact commands, six Maven Results, all counts/cases, path normalization, preserved source/input hashes")
