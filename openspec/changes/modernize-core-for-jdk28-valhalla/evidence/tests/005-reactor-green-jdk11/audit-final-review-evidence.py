from pathlib import Path
import hashlib
import json
import re
import subprocess
import xml.etree.ElementTree as ET

repo = Path.cwd()
ev = Path(__file__).parent
summary = (ev / "summary.md").read_text()
commands = (ev / "command.txt").read_text()
surefire = (ev / "surefire-summary.txt").read_text()
blockers = (ev / "conformance-blockers.md").read_text()
scope = (ev / "scope-check.txt").read_text()
assert "Result: **PASS for the configured full reactor**" in summary
assert "## Review fixes" in summary and "programmaticFourteenMonthsAreCountedOnce" in summary
assert "| xacml-core | 638 | 0 | 0 | 4" in summary
assert "| xacml-pdp | 74 | 0 | 0 | 4" in summary
assert "| Total | 812 | 0 | 0 | 9" in summary
assert "junit:junit:4.13.2 compile-scope patch" in summary
assert "additional IIIE3xx proposals OPEN" in blockers
assert all(case in blockers and case in summary for case in ("IIIE301", "IIIE302", "IIIE303"))
assert "UNVERIFIED" in summary and "XmlContent" in summary
assert "Protected files changed: 0" in scope
assert "license headers checked: 15; unchanged: True" in scope
assert "New Java headers matched to templates: 10; all match: True" in scope

gate_section = surefire.split("Run: 53-reactor-verify-review-fixes.txt",1)[1]
assert gate_section.count("[INFO] Results:") == 6
assert "BUILD SUCCESS" in gate_section and "BUILD FAILURE" not in gate_section
assert len(re.findall(r"Tests run: \d+, Failures: 0, Errors: 0, Skipped: \d+", gate_section)) == 6
for name in ("48-review-arithmetic-before.txt", "49-review-context-before.txt", "51-review-arithmetic-after.txt",
             "52-review-context-after.txt", "53-reactor-verify-review-fixes.txt"):
    assert name in commands and name in surefire
    assert str(repo) not in (ev / name).read_text()
    maven = [line for line in commands.splitlines() if "rtk mvn" in line and name in line]
    assert len(maven) == 1 and 'JAVA_HOME="$(jenv prefix 11)"' in maven[0]
    assert "-Dlicense.skipUpdateLicense=true" in maven[0]
for name in ("50-IIIE3xx-before.txt", "54-IIIE3xx-after.txt", "55-fractional-duration-gaps.txt"):
    assert name in commands and str(repo) not in (ev / name).read_text()
for value in (summary, commands, surefire, blockers, scope):
    assert str(repo) not in value

green = json.loads((ev / "53-review-green-tests.json").read_text())
red = json.loads((ev / "49-review-red-tests.json").read_text())
for name, suite in red.items():
    assert suite["counts"][0] == green[name]["counts"][0]
    assert {test["test"] for test in suite["tests"]} == {test["test"] for test in green[name]["tests"]}
    assert green[name]["counts"][1:] == [0,0,0]
for path in ev.glob("53-green-*.xml"):
    root = ET.parse(path).getroot()
    assert root.find("properties") is None
    assert root.get("failures") == "0" and root.get("errors") == "0"
assert sum(count[0] for count in json.loads((ev / "53-module-counts.json").read_text()).values()) == 812
assert json.loads((ev / "53-existing-case-exclusions.json").read_text()) == json.loads((ev / "46-existing-case-exclusions.json").read_text())

protected = json.loads((ev / "48-protected-before-review.json").read_text())
assert len(protected) == 3434
assert all((repo / name).is_file() and hashlib.sha256((repo / name).read_bytes()).hexdigest() == digest
           for name, digest in protected.items())
review_hashes = json.loads((ev / "53-review-file-hashes.json").read_text())
assert len(review_hashes) == 6
assert all(hashlib.sha256((repo / name).read_bytes()).hexdigest() == digest for name, digest in review_hashes.items())
patch = (ev / "53-review-fixes.patch").read_text()
assert len(re.findall(r"^\+\+\+ b/", patch, re.MULTILINE)) == 6
for command, expected in ((["rtk", "git", "branch", "--show-current"], "integration/modernize-core-jdk28-valhalla"),
                          (["rtk", "git", "rev-parse", "HEAD"], "befdd116bd11385ac8d007732fa8e4d347af3143")):
    result = subprocess.run(command, capture_output=True, text=True)
    assert result.returncode == 0 and result.stdout.strip() == expected
    print(" ".join(command) + ": " + expected)
print("Final review evidence audit PASS: exact commands, six final Maven Results, 812/0/0/9, red/green tests, preserved hashes, open IIIE proposals")
