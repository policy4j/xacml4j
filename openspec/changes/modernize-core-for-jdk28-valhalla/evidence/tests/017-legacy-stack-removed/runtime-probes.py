from pathlib import Path
import json
import subprocess
import xml.etree.ElementTree as ET

ROOT = Path.cwd()
EV = Path(__file__).resolve().parent
MODULES = ("xacml-core", "xacml-pdp", "xacml-xml", "xacml-json", "xacml-test", "xacml-conformance-tests")
jdk = subprocess.check_output(["rtk", "proxy", "jenv", "prefix", "21"], text=True).strip()
java = str(Path(jdk) / "bin/java")
receipts = []
for module in MODULES:
    report = next((ROOT / module / "target/surefire-reports").glob("TEST-*.xml"))
    properties = {node.get("name"): node.get("value", "") for node in ET.parse(report).getroot().find("properties")}
    classpath = properties["surefire.test.class.path"]
    legacy = [Path(entry).name for entry in classpath.split(":")
              if any(fragment in Path(entry).name for fragment in ("junit-4", "junit-vintage", "easymock", "objenesis"))]
    assert not legacy, (module, legacy)
    command = ["rtk", "proxy", java, "--class-path", classpath, str(EV / "TestEngineInventory.java"), module]
    result = subprocess.run(command, text=True, capture_output=True)
    assert result.returncode == 0, (module, result.stderr)
    assert "TestEngine IDs: [junit-jupiter]" in result.stdout, result.stdout
    receipts.append({"module": module, "classpath_source": report.relative_to(ROOT).as_posix(),
                     "java_version": properties.get("java.version"),
                     "legacy_classpath_entries": legacy, "engine_ids": ["junit-jupiter"],
                     "stdout": result.stdout, "exit_code": result.returncode})
    print(result.stdout.strip())
    if module == "xacml-core":
        probe = subprocess.run(["rtk", "proxy", java, "--class-path", classpath,
                                str(EV / "TruthWithoutJunit.java")], text=True, capture_output=True)
        assert probe.returncode == 0, probe.stderr
        (EV / "11-truth-oracle.txt").write_text(probe.stdout)
        print(probe.stdout.strip())
(EV / "11-engine-inventory.json").write_text(json.dumps(receipts, indent=2) + "\n")
