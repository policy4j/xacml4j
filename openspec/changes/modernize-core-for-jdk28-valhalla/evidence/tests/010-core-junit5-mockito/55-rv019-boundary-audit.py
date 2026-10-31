from pathlib import Path
import json
import re

EV = Path(__file__).resolve().parent
CURRENT = Path("xacml-core/src/test/java")
BASELINE = EV / "before/java"
method_pattern = re.compile(r"\bpublic\s+void\s+(\w+)\s*\(")

def method_name(source, position):
    methods = list(method_pattern.finditer(source, 0, position))
    assert methods, "Throw site must be inside a test method"
    return methods[-1].group(1)

def method_body(source, name):
    starts = list(method_pattern.finditer(source))
    for index, match in enumerate(starts):
        if match.group(1) == name:
            end = starts[index + 1].start() if index + 1 < len(starts) else len(source)
            return source[match.start():end]
    raise AssertionError(name)

def inventory(root, pattern):
    found = []
    for path in sorted(root.rglob("*.java")):
        text = path.read_text()
        for match in re.finditer(pattern, text):
            name = method_name(text, match.start())
            left = text.rfind(";", 0, match.start()) + 1
            right = text.find(";", match.end()) + 1
            statement = re.sub(r"\s+", " ", text[left:right]).strip()
            call_start = max(statement.rfind("expect("), statement.rfind("when("))
            if statement.startswith("doThrow("):
                call_start = 0
            elif " doThrow(" in statement:
                call_start = statement.rfind("doThrow(")
            if call_start != -1:
                statement = statement[call_start:]
            found.append({"file":path.relative_to(root).as_posix(),"method":name,
                          "line":text.count("\n",0,match.start())+1,"statement":statement,
                          "body":method_body(text,name)})
    return found

before = inventory(BASELINE, r"\bandThrow\s*\(")
after = inventory(CURRENT, r"\b(?:thenThrow|doThrow)\s*\(")
current = {(item["file"],item["method"]):item for item in after}
assert len(current) == len(after), "Review methods with multiple throw sites separately"
special = {
    ("org/xacml4j/v30/policy/RuleTest.java","testPermitRuleEvaluateIfApplicableWithTargetEvaluationException"):
        "FIXED: Target.match spy throws EvaluationException directly; isMatch catches it. No context status is set in the stub.",
    ("org/xacml4j/v30/policy/PolicyTest.java","testIsApplicableTargetException"):
        "FIXED: Target.match spy throws NullPointerException directly; isMatch catches it.",
    ("org/xacml4j/v30/policy/PolicySetTest.java","testIsApplicableTargetException"):
        "FIXED: Target.match spy throws NullPointerException directly; isMatch catches it.",
    ("org/xacml4j/v30/policy/MatchTest.java","testMatchEvaluationFailedToResolveAttributeException"):
        "PRESERVED caller boundary: real mustBePresent AttributeReference rethrows the same resolver exception (AttributeReference.java:125-129); Match.match catches it (Match.java:131-137). Missing-attribute code, Failed message and captured Rule remain asserted.",
}
exception_rows = []
for item in before:
    key = (item["file"],item["method"])
    assert key in current, "Lost baseline exception test: " + repr(key)
    new = current[key]
    if key in special:
        disposition = special[key]
    else:
        disposition = "PRESERVED: same collaborator method throws to the same production caller; no model replacement moved this exception."
    if key in special and key[0].endswith(("RuleTest.java","PolicyTest.java","PolicySetTest.java")):
        assert "doThrow(" in new["body"] and ".when(throwingTarget).match(" in new["body"]
        assert "setEvaluationStatus(" not in new["body"]
    exception_rows.append({"file":item["file"],"method":item["method"],"before_line":item["line"],
                           "before":item["statement"],"after_line":new["line"],"after":new["statement"],
                           "disposition":disposition})
original_keys = {(item["file"],item["method"]) for item in before}
status_rows = []
for item in after:
    key = (item["file"],item["method"])
    if key in original_keys:
        continue
    old_text = (BASELINE / item["file"]).read_text()
    old_body = method_body(old_text,item["method"])
    assert "INDETERMINATE" in old_body and "andReturn(" in old_body
    assert "ModelFixtures.evaluationError()" in item["statement"]
    old_line = old_text.count("\n",0,old_text.index(old_body)) + 1
    status_rows.append({"file":item["file"],"method":item["method"],"before_method_line":old_line,
                        "after_line":item["line"],"after":item["statement"],
                        "disposition":"PRESERVED result: baseline returned INDETERMINATE, without throwing. The real Match/Condition catches a fixture expression failure and returns that same result to its caller. This test asserts the result, not the caller's exception catch."})
assert len(before) == 26 and len(after) == 48 and len(status_rows) == 22
answers = []
for path in CURRENT.rglob("*.java"):
    text = path.read_text()
    for match in re.finditer(r"\b(?:thenAnswer|doAnswer)\s*\(",text):
        answers.append({"file":path.relative_to(CURRENT).as_posix(),"line":text.count("\n",0,match.start())+1})
assert not answers
result = {"coverage":"SUPPORTED: source inventory and baseline/current method comparison; exception paths confirmed in opened production source and the JDK 21 gate.",
          "baseline_exception_sites":len(before),"current_throw_sites":len(after),"baseline_exception_mappings":exception_rows,
          "baseline_status_return_fixtures":status_rows,"thenAnswer_doAnswer_sites":answers,
          "direct_model_exception_sites_fixed":3,"attribute_reference_boundary_preserved":1,
          "remaining_interface_exception_sites_unchanged":22}
(EV / "55-rv019-exception-boundaries.json").write_text(json.dumps(result,indent=2)+"\n")
lines = [
    "# rv019: exception and status boundary audit", "",
    "Coverage: **SUPPORTED** for the source inventory below. rtk rg located",
    "the candidates; baseline/current methods and production catches were opened.",
    "The full JDK 21 core gate executes these tests. This is not a whole-engine",
    "mutation-coverage claim.", "",
    "All 26 original throw sites remain mapped. Three Target exception tests",
    "are fixed with the explicitly permitted spy. The real AttributeReference",
    "replacement rethrows at the original caller boundary; 22 interface",
    "exception sites keep their original method/caller. No thenAnswer/doAnswer",
    "site remains in core tests after the sole status-setting answer is removed.", "",
    "ModelFixtures default answers only reject unexpected resolver, handler",
    "and visitor calls; they do not pre-populate a context status.", "",
    "Paths below are relative to each java/ root: baseline is run 010 before/,",
    "current is xacml-core/src/test/java/.", "",
    "## Original exception expectations", "",
    "| Test | Baseline injection | Current injection | Disposition |",
    "| --- | --- | --- | --- |",
]
tick=chr(96)
for row in exception_rows:
    name=Path(row["file"]).stem+"."+row["method"]
    lines.append("| "+tick+name+tick+" | "+tick+":"+str(row["before_line"])+" "+row["before"].replace("|","\\|")+tick+" | "+tick+":"+str(row["after_line"])+" "+row["after"].replace("|","\\|")+tick+" | "+row["disposition"]+" |")
lines += ["", "## Model result fixtures", "",
          "These 22 baseline model expectations returned Indeterminate rather",
          "than throwing. The new inner fixture failure is caught inside the",
          "substituted model, so its caller still receives Indeterminate before",
          "combining results. Outer MATCH/NOMATCH decisions remain asserted.",
          "They do not claim to test BaseDecisionRule.isMatch's exception catches.", "",
          "| Test | Baseline method | Current fixture | Result |",
          "| --- | --- | --- | --- |"]
for row in status_rows:
    name=Path(row["file"]).stem+"."+row["method"]
    lines.append("| "+tick+name+tick+" | "+tick+":"+str(row["before_method_line"])+tick+" | "+tick+":"+str(row["after_line"])+" "+row["after"].replace("|","\\|")+tick+" | Same Indeterminate result at the model boundary; no baseline exception to relocate. |")
lines += ["", "The three repaired methods preserve exact one-call verification, InOrder",
          "and no-more-interactions checks. Rule retains both complete Bad status",
          "assertions. PolicySet retains its processing-error status assertion;",
          "Policy now also asserts that status.", ""]
(EV / "55-rv019-exception-boundaries.md").write_text("\n".join(lines))
print(json.dumps({k:result[k] for k in ("baseline_exception_sites","current_throw_sites","direct_model_exception_sites_fixed","attribute_reference_boundary_preserved","remaining_interface_exception_sites_unchanged","thenAnswer_doAnswer_sites")}))
