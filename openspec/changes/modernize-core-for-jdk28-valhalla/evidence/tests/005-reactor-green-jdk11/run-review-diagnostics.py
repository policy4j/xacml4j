from pathlib import Path
import os
import subprocess
import sys
import xml.etree.ElementTree as ET

repo = Path.cwd()
ev = Path(__file__).parent
report = repo / "xacml-conformance-tests/target/surefire-reports/TEST-org.xacml4j.v20.Xacml30ConformanceTest.xml"
properties = ET.parse(report).getroot().find("properties")
classpath = next(item.get("value") for item in properties if item.get("name") == "java.class.path")
source, log = sys.argv[1:3]
result = subprocess.run([str(Path(os.environ["JAVA_HOME"]) / "bin/java"), "--source", "11", "-cp", classpath,
                         str(ev / source)], text=True, capture_output=True)
output = (result.stdout + result.stderr).replace(str(repo), "<repo>")
(ev / log).write_text(output)
print(f"Diagnostic {source}; log {log}; exit {result.returncode}")
for line in output.splitlines():
    if line.startswith(("CASE", "GAP", "OBSERVATION", "CHECK")):
        print(line)
if not any(line.startswith("CASE") for line in output.splitlines()) and result.returncode:
    print("\n".join(output.splitlines()[-15:]))
raise SystemExit(result.returncode)
