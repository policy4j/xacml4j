from pathlib import Path
import collections
import json
import re
import subprocess

EV = Path(__file__).resolve().parent
REL = EV.relative_to(Path.cwd()).as_posix()
MODULES = ("xacml-core", "xacml-pdp", "xacml-xml-jaxb", "xacml-xml",
           "xacml-json", "xacml-test", "xacml-conformance-tests")
SOURCES = [module + "/src" for module in MODULES]
checks = []


def run(label, argv, expected):
    result = subprocess.run(argv, text=True, capture_output=True)
    (EV / (label + ".stdout.txt")).write_text(result.stdout)
    (EV / (label + ".stderr.txt")).write_text(result.stderr)
    check = {"name": label, "argv": argv, "exit_code": result.returncode}
    checks.append(check)
    assert result.returncode == expected, check
    return result.stdout


run("08-legacy-api-rg", ["rtk", "rg", "-n", "-i",
    r"EasyMock|org\.easymock|org\.junit\.(experimental|internal|runners|runner|rules|Assume|Assert|Test|Before|After|Ignore|Rule|ClassRule)|org\.hamcrest\.junit|junit\.framework",
    "--glob", "*.java", *SOURCES], 1)
run("08-legacy-annotation-rg", ["rtk", "rg", "-n",
    r"@Before([[:space:]]|\()|@After([[:space:]]|\()|@Ignore([[:space:]]|\()|@Rule([[:space:]]|\()|@ClassRule([[:space:]]|\()|@RunWith|@Test[[:space:]]*\([[:space:]]*expected|ExpectedException|TemporaryFolder",
    "--glob", "*.java", *SOURCES], 1)
rule = REL + "/legacy-test-stack.yml"
actual = json.loads(run("08-legacy-ast", ["ast-grep", "scan", "--rule", rule,
                                        "--inspect", "summary", "--json=compact", *SOURCES], 0))
assert actual == []
control = json.loads(run("08-positive-control-ast", ["ast-grep", "scan", "--rule", rule,
    "--json=compact", REL + "/LegacyScanControl.java"], 1))
assert len(control) >= 30
checks[-2]["matches"] = 0
checks[-1]["matches"] = len(control)

annotations = json.loads(run("08-method-annotations-ast", ["ast-grep", "run", "--lang", "java",
    "--kind", "marker_annotation, annotation", "--json=compact", *SOURCES], 0))
tests = collections.defaultdict(collections.Counter)
for node in annotations:
    annotation = re.match(r"@(Test|ParameterizedTest|RepeatedTest|TestFactory|TestTemplate)\b", node["text"])
    if annotation:
        tests[node["file"]][annotation.group(1)] += 1
rows = {module: {file: dict(counts) for file, counts in sorted(tests.items())
                 if file.startswith(module + "/")} for module in MODULES}
source_counts = {module: sum(sum(counts.values()) for counts in files.values())
                 for module, files in rows.items()}
(EV / "08-source-method-counts.json").write_text(json.dumps(rows, indent=2) + "\n")
receipt = {"checks": checks, "language": "Java", "scope": SOURCES,
           "java_files": sum(len(list(Path(src).rglob("*.java"))) for src in SOURCES),
           "source_test_method_annotations": source_counts,
           "coverage": "SUPPORTED: Java import/identifier/annotation AST; positive control and independent rg."}
(EV / "08-static-checks.json").write_text(json.dumps(receipt, indent=2) + "\n")
print(json.dumps({"legacy_matches": 0, "positive_control_diagnostics": len(control),
                  "java_files": receipt["java_files"], "source_method_annotations": source_counts}, indent=2))
