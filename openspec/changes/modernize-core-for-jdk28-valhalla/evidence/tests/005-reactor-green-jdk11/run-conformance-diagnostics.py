from pathlib import Path
import os
import subprocess
import sys
import xml.etree.ElementTree as ET

repo = Path.cwd()
ev = Path(__file__).parent
report = repo / "xacml-conformance-tests/target/surefire-reports/TEST-org.xacml4j.v20.Xacml30ConformanceTest.xml"
properties = ET.parse(report).getroot().find("properties")
classpath = next(p.attrib["value"] for p in properties if p.attrib["name"] == "java.class.path")
source = sys.argv[1] if len(sys.argv) > 1 else "DiagnoseConformance.java"
log = sys.argv[2] if len(sys.argv) > 2 else "23-conformance-diagnostics.txt"
result = subprocess.run([str(Path(os.environ["JAVA_HOME"]) / "bin/java"), "--source", "11", "-cp", classpath,
                         str(ev / source)], text=True, capture_output=True)
output = (result.stdout + result.stderr).replace(str(repo), "<repo>")
(ev / log).write_text(output)
print(output)
raise SystemExit(result.returncode)
