from pathlib import Path
import hashlib
import json
import subprocess
import sys
import xml.etree.ElementTree as ET

repo = Path.cwd()
ev = Path(__file__).parent
prefix = sys.argv[1] if len(sys.argv) > 1 else "78"
main = "xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/AbstractPolicyRepository.java"
test = "xacml-pdp/src/test/java/org/xacml4j/v30/spi/repository/InMemoryPolicyRepositoryTest.java"
proof = {}
for name in (main, test):
    command = ["rtk", "proxy", "git", "show", "a8a90d46:" + name]
    result = subprocess.run(command, capture_output=True)
    assert result.returncode == 0, result.stderr.decode()
    current = (repo / name).read_bytes()
    assert current == result.stdout, name
    proof[name] = {"current_sha256": hashlib.sha256(current).hexdigest(),
                  "base_sha256": hashlib.sha256(result.stdout).hexdigest(),
                  "command": " ".join(command), "matches_api_base": True}
report = repo / "xacml-pdp/target/surefire-reports/TEST-org.xacml4j.v30.spi.repository.InMemoryPolicyRepositoryTest.xml"
root = ET.parse(report).getroot()
saved = ET.Element("testsuite", {key: root.get(key) for key in ("name", "tests", "failures", "errors", "skipped")})
results = []
for case in root.findall("testcase"):
    target = ET.SubElement(saved, "testcase", case.attrib)
    entry = {"name": case.get("name"), "problems": []}
    for tag in ("failure", "error", "skipped"):
        for item in case.findall(tag):
            attrs = {key: value.replace(str(repo), "<repo>") for key, value in item.attrib.items()}
            content = (item.text or "").replace(str(repo), "<repo>")
            ET.SubElement(target, tag, attrs).text = content
            entry["problems"].append({"kind": tag, **attrs, "trace": content})
    results.append(entry)
ET.ElementTree(saved).write(ev / (prefix + "-InMemoryPolicyRepositoryTest.xml"), encoding="unicode")
proof["failed_run"] = {"counts": [int(root.get(key)) for key in ("tests", "failures", "errors", "skipped")], "tests": results}
assert proof["failed_run"]["counts"] == {"78": [5,2,0,1], "80": [5,1,0,1]}[prefix]
proof["inference"] = "Pre-existing asynchronous listener/test race: default single-thread executor enqueues notifications; tests verify EasyMock immediately, with no completion barrier. No map function is used by these policies."
(ev / ("79-repository-race-proof.json" if prefix == "78" else prefix + "-repository-race-proof.json")).write_text(json.dumps(proof, indent=2) + "\n")
print("Repository source and tests byte-identical to a8a90d46; captured listener-verification failures from run", prefix)
