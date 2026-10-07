"""Run byte-identical historical arithmetic sources with current infrastructure jars.

This is a scoped arithmetic probe, not a full a8a90d46 reactor build.
Every copied source is checked against its archived bytes before compilation.
The historical classes precede all current classes and the Java probe checks
their code-source locations before it calculates a result.
"""
from pathlib import Path
import csv
import hashlib
import json
import os
import subprocess
import tempfile
import xml.etree.ElementTree as ET

evidence = Path(__file__).resolve().parent
repo = Path.cwd().resolve()
report = repo / "xacml-core/target/surefire-reports/TEST-org.xacml4j.v30.policy.function.impl.DateTimeArithmeticRegistryTest.xml"
classpath = ET.parse(report).getroot().find("./properties/property[@name='java.class.path']").get("value")
relative_sources = [
    "org/xacml4j/util/DateTimeDurationUtils.java",
    "org/xacml4j/v30/policy/function/impl/DateTimeArithmeticFunctions.java",
    "org/xacml4j/v30/types/ISO8601Date.java",
    "org/xacml4j/v30/types/ISO8601DateTime.java",
    "org/xacml4j/v30/types/ISO8601Duration.java",
    "org/xacml4j/v30/types/ISO8601DayTimeDuration.java",
    "org/xacml4j/v30/types/ISO8601YearMonthDuration.java",
]
def safe(text, temporary):
    return text.replace(str(repo), "<repo>").replace(str(temporary), "<probe-temp>").replace(str(Path.home()), "<home>")

log = ["Scope: seven unmodified a8a90d46 source files, compiled on JDK 21.",
       "Current infrastructure classes and dependency jars supply the other types.",
       "This is not a complete historical reactor gate."]
with tempfile.TemporaryDirectory(prefix="xacml4j-task45-history-") as directory:
    temporary = Path(directory).resolve()
    classes = temporary / "classes"
    classes.mkdir()
    sources = []
    hashes = {}
    for relative in relative_sources:
        archived = evidence / ("a8a90d46-" + Path(relative).name + ".txt")
        original = subprocess.run(["rtk", "proxy", "git", "show", "a8a90d46:xacml-core/src/main/java/" + relative],
                                  capture_output=True, check=True).stdout
        assert archived.read_bytes() == original, "Archived source differs from a8a90d46: " + relative
        source = temporary / "src" / relative
        source.parent.mkdir(parents=True, exist_ok=True)
        source.write_bytes(archived.read_bytes())
        assert source.read_bytes() == archived.read_bytes()
        hashes[relative] = hashlib.sha256(source.read_bytes()).hexdigest()
        sources.append(str(source))
    (evidence / "historical-source-hashes.json").write_text(json.dumps(hashes, indent=2) + "\n")
    javac = Path(os.environ["JAVA_HOME"]) / "bin/javac"
    java = Path(os.environ["JAVA_HOME"]) / "bin/java"
    compile_result = subprocess.run(["rtk", "proxy", str(javac), "--release", "21", "-classpath", classpath,
                                     "-d", str(classes), *sources, str(evidence / "HistoricalArithmeticProbe.java")],
                                    capture_output=True, text=True)
    log.append("javac exit: " + str(compile_result.returncode))
    log.append(safe(compile_result.stdout + compile_result.stderr, temporary))
    if compile_result.returncode:
        (evidence / "04-historical-arithmetic-probe.log").write_text("\n".join(log) + "\n")
        raise SystemExit(compile_result.returncode)
    run_result = subprocess.run(["rtk", "proxy", str(java), "-classpath", str(classes) + os.pathsep + classpath,
                                 "org.xacml4j.evidence.HistoricalArithmeticProbe",
                                 str(evidence / "arithmetic-case-inputs.csv"), str(classes)],
                                capture_output=True, text=True)
    log.append("java exit: " + str(run_result.returncode))
    log.append(safe(run_result.stderr, temporary))
    if run_result.returncode:
        log.append(safe(run_result.stdout, temporary))
        (evidence / "04-historical-arithmetic-probe.log").write_text("\n".join(log) + "\n")
        raise SystemExit(run_result.returncode)
    (evidence / "baseline-comparison.csv").write_text(run_result.stdout)
    rows = list(csv.DictReader(run_result.stdout.splitlines()))
    assert len(rows) == 184, len(rows)
    counts = {}
    for row in rows:
        count = counts.setdefault(row["function"], {"rows": 0, "same": 0, "different": 0, "errors": 0})
        count["rows"] += 1
        count["same" if row["current_expected"] == row["a8a90d46_actual"] else "different"] += 1
        count["errors"] += row["a8a90d46_actual"].startswith("ERROR:")
    result = {"baseline": "a8a90d46", "scope": "seven unmodified arithmetic sources with current infrastructure jars",
              "rows": len(rows), "functions": counts}
    (evidence / "baseline-comparison-summary.json").write_text(json.dumps(result, indent=2) + "\n")
    log.append(json.dumps(result, indent=2))
    (evidence / "04-historical-arithmetic-probe.log").write_text("\n".join(log) + "\n")
    print(json.dumps(result))
