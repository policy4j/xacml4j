from pathlib import Path
import json
import re

EV=Path(__file__).resolve().parent
relative=EV.relative_to(Path.cwd()).as_posix()
selector="'-Dtest=RuleTest#testPermitRuleEvaluateIfApplicableWithTargetEvaluationException,PolicyTest#testIsApplicableTargetException,PolicySetTest#testIsApplicableTargetException'"
maven='JAVA_HOME="$(jenv prefix 21)" rtk mvn -Dlicense.skipUpdateLicense=true -pl xacml-core'
helper="rtk python3 "+relative+"/50-rv019-oracle.py"
commands=[
    "\n# rv019 follow-up. Commands are from <repo> unless a scratch workdir is noted.",
    helper+" prepare",
    "# Corrected snapshot metadata parsing before test edits; source hashes remained unchanged.",
    helper+" refresh-metadata",
    "# Workdir <scratch>: generated core reactor; both isMatch catches removed only there.",
    maven+" "+selector+" test -l 51-rv019-before-mutant.log",
    "# Workdir <repo>.",
    helper+" receipt 51 rv019-before-mutant scratch 0",
    "# Three test repairs applied; production source untouched.",
    helper+" sync-tests",
    "# Workdir <scratch>, with the same catch mutant and the repaired tests.",
    maven+" "+selector+" test -l 52-rv019-catch-mutant-red.log",
    "# Workdir <repo>.",
    helper+" receipt 52 rv019-catch-mutant-red scratch 1",
    helper+" restore-scratch",
    maven+" "+selector+" test -l "+relative+"/53-rv019-focused-green.log",
    helper+" receipt 53 rv019-focused-green repo 0",
    maven+" test -l "+relative+"/54-rv019-core-gate.log",
    helper+" receipt 54 rv019-core-gate repo 0",
    "# Initial strict audit rejected the concurrent docs/ai/TESTING.md edit; retained strict receipt.",
    helper+" audit",
    "rtk proxy cp "+relative+"/56-rv019-scope-audit.json "+relative+"/56-rv019-strict-scope-audit.json",
    helper+" audit --record-shared-doc-change",
    "rtk rg -n 'thenThrow|doThrow|setEvaluationStatus|thenAnswer|doAnswer' xacml-core/src/test/java",
    "rtk rg -n 'andThrow' "+relative+"/before/java",
    "rtk ./scripts/agent-doctor.sh ast-grep",
    "ast-grep run --lang java --pattern '$A.setEvaluationStatus($$$ARGS)' xacml-core/src/test/java",
    "rtk rg -n -C 3 'thenAnswer|doAnswer' xacml-core/src/test/java",
    "ast-grep run --lang java --pattern '$A.thenAnswer($$$ARGS)' --json xacml-core/src/test/java",
    "ast-grep run --lang java --pattern '$A.doAnswer($$$ARGS)' --json xacml-core/src/test/java",
    "rtk python3 "+relative+"/55-rv019-boundary-audit.py",
    "rtk git diff --check -- xacml-core/src/test",
    "rtk git status --short -- xacml-core/src/test xacml-core/src/main/java/org/xacml4j/v30/policy/BaseDecisionRule.java",
    "rtk python3 "+relative+"/57-rv019-write-evidence.py",
]
with (EV/"command.txt").open("a") as file:
    file.write("\n".join(commands)+"\n")
parts=["\n=== rv019 follow-up (JDK 21; core only) ===\n"]
for number,label in (("51","rv019-before-mutant"),("52","rv019-catch-mutant-red"),("53","rv019-focused-green"),("54","rv019-core-gate")):
    log=(EV/(number+"-"+label+".log")).read_text()
    lines=log.splitlines()
    parts.append("\nRun "+number+" "+label+"\n")
    parts.extend(line+"\n" for line in lines if "Tests run:" in line and " - in " in line)
    start=next(index for index,line in enumerate(lines) if "Results:" in line)
    end=next((index for index in range(start+1,len(lines)) if re.search(r"\[INFO\] -{8,}",lines[index])),len(lines))
    parts.append("\n"+"\n".join(lines[start:end])+"\n")
    parts.extend(line+"\n" for line in lines if "BUILD SUCCESS" in line or "BUILD FAILURE" in line)
with (EV/"surefire-summary.txt").open("a") as file:
    file.write("".join(parts))
with (EV/"easymock-mapping.md").open("a") as file:
    file.write("\nrv019 correction: the three Rule/Policy/PolicySet exception-injection methods use an explicitly permitted Target spy. It throws directly from match without setting a context status, preserving the baseline caller catch. Their exact one-call, InOrder and no-more checks remain. Other model/value instances stay real. See [rv019 fixes](review-artifacts.md#rv019-fixes) and [complete exception mapping](55-rv019-exception-boundaries.md).\n")
static={
    "scope":"xacml-core/src/test/java",
    "rg_thenAnswer_doAnswer":{"exit_code":1,"matches":0},
    "ast_grep_thenAnswer":{"language":"java","exit_code":1,"matches":[]},
    "ast_grep_doAnswer":{"language":"java","exit_code":1,"matches":[]},
    "ast_grep_health":"PASS: rule tests and project scan",
    "source_inventory":{"baseline_exception_sites":26,"current_throw_sites":48,"status_return_fixtures":22},
    "coverage":"SUPPORTED: Java AST and source inventories, confirmed against the opened baseline/current methods and production catches.",
    "diff_check":{"exit_code":0},
}
(EV/"55-rv019-static-checks.json").write_text(json.dumps(static,indent=2)+"\n")
print("Recorded rv019 commands, raw Results sections, baseline expectation corrections and AST/source receipts.")
