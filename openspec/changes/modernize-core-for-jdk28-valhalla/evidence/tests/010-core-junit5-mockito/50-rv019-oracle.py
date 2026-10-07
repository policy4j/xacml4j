from pathlib import Path
import difflib
import hashlib
import json
import re
import shutil
import sys
import tempfile
import xml.etree.ElementTree as ET

EV = Path(__file__).resolve().parent
ROOT = Path.cwd()
STATE = EV / "50-rv019-state.json"
SOURCE = Path("xacml-core/src/main/java/org/xacml4j/v30/policy/BaseDecisionRule.java")
TESTS = [
    Path("xacml-core/src/test/java/org/xacml4j/v30/policy/RuleTest.java"),
    Path("xacml-core/src/test/java/org/xacml4j/v30/policy/PolicyTest.java"),
    Path("xacml-core/src/test/java/org/xacml4j/v30/policy/PolicySetTest.java"),
]

def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def inventory():
    paths = list(Path("xacml-core/src").rglob("*"))
    paths += list(Path(".").glob("*/pom.xml"))
    paths += [Path("pom.xml"), Path("AGENTS.md"), Path("docs/ai/TESTING.md")]
    return {p.as_posix(): sha(p) for p in paths if p.is_file()}

def header(path):
    match = re.search(r"/\*.*?#L%\s*\*/", path.read_text(), re.S)
    return hashlib.sha256(match.group(0).encode()).hexdigest() if match else None

def write(path, data):
    path.write_text(json.dumps(data, indent=2) + "\n")

action = sys.argv[1]
if action == "prepare":
    assert not STATE.exists(), "Do not overwrite an existing follow-up state"
    scratch = Path(tempfile.mkdtemp(prefix="policy4j-rv019-", dir="/private/tmp"))
    shutil.copy2("pom.xml", scratch / "pom.xml")
    for module in Path(".").glob("*/pom.xml"):
        if module.parent.name != "xacml-core":
            (scratch / module.parent).mkdir(parents=True, exist_ok=True)
            shutil.copy2(module, scratch / module)
    shutil.copytree("xacml-core", scratch / "xacml-core", ignore=shutil.ignore_patterns("target", ".DS_Store"))
    data = {
        "scratch_name": scratch.name,
        "before": inventory(),
        "test_headers": {p.as_posix(): header(p) for p in TESTS},
        "test_methods": {p.as_posix(): re.findall(r"@Test\s+public void (\w+)", p.read_text()) for p in TESTS},
        "production_before_sha256": sha(SOURCE),
    }
    original = SOURCE.read_text()
    start = original.index("\t\ttry{", original.index("public MatchResult isMatch"))
    end = original.index("\n\t}\n", start)
    mutant = original[:start] + "\t\treturn (target == null)?MatchResult.MATCH:target.match(context);" + original[end:]
    (scratch / SOURCE).write_text(mutant)
    data["mutant_sha256"] = sha(scratch / SOURCE)
    write(STATE, data)
    (EV / "50-rv019-catch-mutant.patch").write_text("".join(difflib.unified_diff(
        original.splitlines(True), mutant.splitlines(True),
        fromfile=SOURCE.as_posix(), tofile="<scratch>/" + SOURCE.as_posix())))
    print(json.dumps({"scratch_name":scratch.name, "production_unchanged":sha(SOURCE)==data["production_before_sha256"], "protected_files":len(data["before"])}))
elif action == "refresh-metadata":
    data = json.loads(STATE.read_text())
    assert all(sha(p) == data["before"][p.as_posix()] for p in TESTS)
    data["test_headers"] = {p.as_posix(): header(p) for p in TESTS}
    data["test_methods"] = {p.as_posix(): re.findall(r"@Test\s+public void (\w+)", p.read_text()) for p in TESTS}
    assert all(data["test_headers"].values()) and all(data["test_methods"].values())
    write(STATE, data)
    print(json.dumps({"test_methods": {p:len(names) for p,names in data["test_methods"].items()}, "headers_captured":True}))
elif action == "sync-tests":
    data = json.loads(STATE.read_text())
    scratch = Path("/private/tmp") / data["scratch_name"]
    assert sha(SOURCE) == data["production_before_sha256"]
    assert sha(scratch / SOURCE) == data["mutant_sha256"]
    for path in TESTS:
        shutil.copy2(path, scratch / path)
    print("Copied the three repaired tests into the catch-mutant scratch copy.")
elif action == "receipt":
    data = json.loads(STATE.read_text())
    scratch = Path("/private/tmp") / data["scratch_name"]
    number, label, where, exit_code = sys.argv[2:6]
    work = scratch if where == "scratch" else ROOT
    log_name = number + "-" + label + ".log"
    log = (work / log_name).read_text() if where == "scratch" else (EV / log_name).read_text()
    log = log.replace(str(ROOT), "<repo>").replace(str(scratch), "<scratch>")
    log = log.replace(str(Path.home()), "<home>")
    (EV / log_name).write_text(log)
    totals = dict.fromkeys(("tests", "failures", "errors", "skipped"), 0)
    failures = []
    classes = []
    reports = sorted((work / "xacml-core/target/surefire-reports").glob("TEST-*.xml"))
    if label == "rv019-focused-green":
        names = {"TEST-org.xacml4j.v30.policy." + p.stem + ".xml" for p in TESTS}
        reports = [p for p in reports if p.name in names]
        assert len(reports) == 3
    jdk_versions = set()
    for report in reports:
        xml = ET.parse(report).getroot()
        for prop in xml.findall("properties/property"):
            if prop.get("name") == "java.version":
                jdk_versions.add(prop.get("value"))
        counts = {k:int(xml.get(k, "0")) for k in totals}
        for key, value in counts.items():
            totals[key] += value
        classes.append({"class":xml.get("name"), **counts})
        for test in xml.findall("testcase"):
            for kind in ("failure", "error"):
                fail = test.find(kind)
                if fail is not None:
                    failures.append({"class":test.get("classname"),"test":test.get("name"),"kind":kind,
                                     "type":fail.get("type"),"message":fail.get("message")})
    receipt = {"run":number,"label":label,"workspace":"<scratch>" if where=="scratch" else "<repo>",
               "jdk_versions":sorted(jdk_versions),"surefire_report_count":len(reports),
               "exit_code":int(exit_code),"totals":totals,"classes":classes,"failed_tests":failures,
               "production_unchanged":sha(SOURCE)==data["production_before_sha256"],
               "catch_mutant_active_in_scratch":sha(scratch / SOURCE)==data["mutant_sha256"],
               "maven_result_lines":[line for line in log.splitlines() if "Tests run:" in line or "Results:" in line or "BUILD SUCCESS" in line or "BUILD FAILURE" in line]}
    write(EV / (number + "-" + label + "-receipt.json"), receipt)
    print(json.dumps({k:v for k,v in receipt.items() if k not in ("classes","maven_result_lines")}, indent=2))
elif action == "restore-scratch":
    data = json.loads(STATE.read_text())
    scratch = Path("/private/tmp") / data["scratch_name"]
    assert sha(SOURCE) == data["production_before_sha256"]
    assert sha(scratch / SOURCE) == data["mutant_sha256"]
    shutil.copy2(SOURCE, scratch / SOURCE)
    receipt = {"worktree_production_sha256":sha(SOURCE),"scratch_production_sha256":sha(scratch/SOURCE),
               "task_start_production_sha256":data["production_before_sha256"],
               "scratch_restored_exact_bytes":(scratch/SOURCE).read_bytes()==SOURCE.read_bytes(),
               "worktree_production_ever_edited":False}
    write(EV / "50-rv019-production-restoration.json", receipt)
    print(json.dumps(receipt, indent=2))
elif action == "audit":
    data = json.loads(STATE.read_text())
    after = inventory()
    changed = [p for p in sorted(set(data["before"])|set(after)) if data["before"].get(p)!=after.get(p)]
    allowed = {p.as_posix() for p in TESTS}
    headers = {p.as_posix():header(p)==data["test_headers"][p.as_posix()] for p in TESTS}
    methods = {p.as_posix():re.findall(r"@Test\s+public void (\w+)",p.read_text())==data["test_methods"][p.as_posix()] for p in TESTS}
    result = {"changed_paths":changed,"outside_three_test_files":[p for p in changed if p not in allowed],
              "headers_unchanged":headers,"methods_unchanged":methods,
              "production_unchanged":sha(SOURCE)==data["production_before_sha256"]}
    result["recorded_shared_change"] = []
    if "--record-shared-doc-change" in sys.argv:
        assert result["outside_three_test_files"] == ["docs/ai/TESTING.md"]
        result["recorded_shared_change"] = [{"path":"docs/ai/TESTING.md",
            "before_sha256":data["before"]["docs/ai/TESTING.md"],
            "after_sha256":after["docs/ai/TESTING.md"],"authorship":"UNATTRIBUTED", "preserved":True}]
    result["result"] = "PASS" if not result["outside_three_test_files"] and all(headers.values()) and all(methods.values()) and result["production_unchanged"] else "FAIL"
    if result["recorded_shared_change"] and all(headers.values()) and all(methods.values()) and result["production_unchanged"]:
        result["result"] = "PASS_CORE_SCOPE_WITH_RECORDED_SHARED_DOC_CHANGE"
    write(EV / "56-rv019-scope-audit.json", result)
    print(json.dumps(result, indent=2))
    assert result["result"].startswith("PASS"), "Follow-up scope audit failed"
else:
    raise ValueError(action)
