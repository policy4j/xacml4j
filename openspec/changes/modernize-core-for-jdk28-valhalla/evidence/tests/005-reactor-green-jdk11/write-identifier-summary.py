from pathlib import Path
import json
import re

ev = Path(__file__).parent
old = (ev / "36-summary-before-identifiers.md").read_text()
arithmetic = old.split("## Arithmetic approval, red tests and fixes\n", 1)[1].split("## Conformance totals and dispositions\n", 1)[0].strip()
inventory = old.split("| File | Change |", 1)[1].split("Earlier accepted xacml-xml", 1)[0].strip()
new_rows = [
    "| `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/BagFunctions.java` | Eight standard 3.0 duration bag names; retain eight 1.0 aliases; exact approved patch. |",
    "| `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/SetFunctions.java` | Ten standard 3.0 duration set names; retain ten 1.0 aliases; add annotation import. |",
    "| `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/HigherOrderFunctions.java` | Four standard 3.0 higher-order names; retain four 1.0 aliases; annotations only. |",
    "| `xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/DurationFunctionIdentifierTest.java` | 36 parameterized standard/legacy registry checks; red before repairs. |",
    "| `xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/HigherOrderFunctionIdentifierTest.java` | Eight standard/legacy and three unchanged standard registry checks; red before repairs. |",
]
counts = json.loads((ev / "46-module-counts.json").read_text())
module_rows = ["| xacml parent | 0 | 0 | 0 | 0 | SUCCESS; no tests |"]
for name in ("xacml-core", "xacml-xml-jaxb", "xacml-xml", "xacml-json", "xacml-pdp", "xacml-test", "xacml-conformance-tests"):
    numbers = counts.get(name, [0,0,0,0])
    module_rows.append("| " + name + " | " + " | ".join(str(number) for number in numbers) + " | SUCCESS" + ("; no tests" if name.endswith("jaxb") else "") + " |")
module_rows.append("| Total | 774 | 0 | 0 | 9 | BUILD SUCCESS |")
summary = '''# Task 1.3 continued: full reactor green on JDK 11

Result: **PASS**. The full reactor compiles and verifies on JDK 11, including
xacml-test and the existing conformance suites: **774 Surefire tests,
0 failures, 0 errors, 9 existing skips**. OASIS 2.0 and 3.0 each complete
347 case executions; all 17 RSA interop cases and both examples pass.

JDK: `openjdk version "11.0.32.1" 2026-08-18 LTS`;
Zulu11.90+205-CA, build 11.0.32.1+1-LTS. Raw version line: 38-jdk-version.txt.
Fresh final-gate Surefire JVM properties: 46-jdk-properties.json.
Branch: integration/modernize-core-jdk28-valhalla; HEAD
befdd116bd11385ac8d007732fa8e4d347af3143; core API base a8a90d46.

## Final full-reactor gate

```sh
JAVA_HOME="$(jenv prefix 11)" rtk mvn -Dlicense.skipUpdateLicense=true verify -l openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/005-reactor-green-jdk11/46-reactor-verify-higher-order-identifiers.txt
```

| Module | Tests run | Failures | Errors | Skipped | Reactor result |
| --- | ---: | ---: | ---: | ---: | --- |
''' + "\n".join(module_rows) + '''

These are fresh run 46 Maven Results, retained in surefire-summary.txt and
46-module-counts.json. Surefire counts grouped conformance methods; individual
case counts below come from their retained production execution output.

## Approved identifier repairs, tests first

The conductor approved required-duration-bag-alias-fix.patch and expanded
scope to missing/wrong identifiers and aliases only. The requested family
scan found ten related duration set names and, after IIC164 was reached,
four higher-order names. Each of the 22 added 3.0 identifiers and retained
1.0 aliases is listed with its original file:line, focused test and normative
reference in identifier-audit.md and 46-identifier-audit.json. The original
1.0 annotations are confirmed at a8a90d46 in 46-identifier-baseline.txt.

| Error / original source location -> fix | Red receipt | Green receipt |
| --- | --- | --- |
| IIC150 import; BagFunctions.java:491/499/507/516/529/538/547/557 -> exact approved eight 3.0 duration bag annotations plus eight 1.0 legacy aliases. | Run 39 before either duration provider edit: 36 failures (18 missing 3.0 names, 18 canonical/legacy metadata expectations). | Run 41: all 42 focused tests pass; run 46 repeats all 36 duration lookup checks and evaluates IIC150 successfully. |
| Same-family SetFunctions.java:585/594/603/612/621/630/639/648/657/666 -> ten 3.0 duration set annotations plus ten 1.0 legacy aliases. | Same run 39. | All duration lookup checks pass in runs 41/46. Later duration-set conformance cases beyond the unchanged loop bounds remain UNVERIFIED. |
| Run 40 SetFunctions.java:586/596/606/616/626/636/646/656/666/676 -> add import for XacmlLegacyFunc; every compiler diagnostic retained in compile-diagnostics.json. | Temporary edit compile failure, ten locations. | Run 41 and final gate compile without diagnostics. |
| IIC164 import; HigherOrderFunctions.java:54/72/90/169 -> four 3.0 names (any-of, all-of, any-of-any, map), plus four 1.0 legacy aliases. | Run 43 directly reproduces unchanged IIC164 import; run 44 before production edit: 11 tests / 8 failures / 3 passes. | Run 45: all 53 focused tests pass; run 46 repeats all 11 registry checks and evaluates IIC164 successfully. |

Production method bodies and type/parameter annotations match the captured
pre-write source exactly. Bag aliases match the approved patch byte for byte.
Identifier tests use the default production FunctionProvider and assert
presence, canonical/legacy metadata and object identity for each old/new name.
No assertion, expected result or fixture was changed.

References: [OASIS XACML 3.0 Standard](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html),
Appendix A.3.10/A.3.11 for bag/set names, A.3.12/A.4 for higher-order names,
Tables 10.2.8/10.2.9 for the duration versioned names. No type or algorithm
identifier needed a repair; no production semantic change was made in this
identifier continuation. Decisions and receipts: identifier-approval.md.

## Conformance case totals and dispositions

| Suite | Surefire methods | Cases executed | Failed case IDs |
| --- | ---: | ---: | --- |
| OASIS 2.0 | 10 | 347 | None |
| OASIS 3.0 | 10 | 347 | None |
| RSA 2008 interop | 17 | 17 | None |
| Examples | 2 | 2 | None |
| Resolver characterization | 2 | Not conformance cases | None |

OASIS/RSA: **711 executed, 711 passed, 0 failed**. Including examples:
**713 executed, 713 passed**. Each OASIS suite invokes IIIG006 twice, as before.
46-conformance-testcases.json records every method, emitted case ID and
failure field. IIC232 is reached by both suites; IIC150 and IIC164 pass.

The 30 explicit case exclusions in each OASIS suite (60 total), inherited
group bounds, RSA performance exclusion and Surefire's 9 skips are unchanged.
The exclusion sets are extracted again and compared to run 36 in
46-existing-case-exclusions.json. Suite and fixture hashes are unchanged.
The passed gate certifies the currently configured coverage, not all vendored
cases. The XACML 3.0 IIC loop still stops before 233; later duration set cases
IIC340-IIC349 are outside that loop and were not added to this task's gate.

| Previously observed failure | Classification / cause | Final disposition |
| --- | --- | --- |
| IIC104, both suites | Pre-existing core day-time subtraction sign defect | PASS; earlier approved sign restoration retained. |
| IIC106/IIC107, both suites | Pre-existing date/year-month conversion defect | PASS after approved Period fix. |
| IIIF001 and both IIIG006 invocations, OASIS 3.0 | Pre-existing XML whitespace/content selection defect | PASS after approved content mapper fix. |
| IIA001/IIB001/IIC001/IID001/IIE001/IIIA006 errors, run 22 | Pre-existing harness/test-support optional collection comparison defects | PASS; owned comparator repair retains assertions. |
| IIC150, run 36 | Pre-existing core duration bag registration; unchanged policy's required 3.0 name absent | PASS after conductor-approved aliases. |
| IIC164, run 42 | Pre-existing core higher-order registration; unchanged policy's required 3.0 any-of name absent | PASS after approved identifier-class repair. |

Import failures print no execution marker: IIC149 in run 36 and IIC163 in
run 42 are the last successful markers, not the failing cases. Actual failed
IDs were directly reproduced by production imports in runs 37 and 43.
No adaptation regression or new semantic failure is left by the final gate.
conformance-blockers.md marks every recorded blocker resolved; no proposal
needs further authorization to obtain this configured green baseline.

## Prior approved arithmetic checks retained

''' + arithmetic + '''

## Changed project files in the continued lane

The continued task 1.3 lane changed **23 project files** plus this run 005
directory. Five project files changed/appeared in the latest identifier
continuation; all earlier repairs and accepted xacml-xml review files stayed
unchanged. The original xacml-xml adaptation evidence in run 003 is preserved.

| File | Change |
''' + inventory + "\n" + "\n".join(new_rows) + '''

The xacml-test POM change is the exact conductor-approved
junit:junit:4.13.2 compile-scope patch. No other POM edit was made. Original
continued compiler errors -> fixes follow below; all raw diagnostics remain
in compile-diagnostics.json, including the temporary missing import in run 40.

## Evidence, scope and remaining risk

command.txt records exact Maven and evidence commands; surefire-summary.txt
retains Maven Results and per-module totals through final run 46. Logs and
receipts normalize the checkout path to <repo>. Earlier runs and run 003
are unchanged; 36-summary-before-identifiers.md and
36-blockers-before-identifiers.md retain the former blocked state.

scope-check.txt records 3,436 protected files with unchanged hashes, 14
existing edited Java license headers unchanged and eight new test headers
matching their module templates. The three provider sources match only
the expected annotation/alias edits and one import. Prior arithmetic/XML/POM
hashes remain unchanged. Diff whitespace checks and reverse checks of the
approved patches pass. Git has no staged changes. Shared fixture restoration,
other-lane XML work, evidence and the pre-existing JSON fixture deletion were
preserved; their presence in git status is not attributed to this lane.

Residual risk: inherited exclusions and loop bounds still limit conformance
coverage. Alias lookup tests and the configured suites do not certify all
3.0 variadic higher-order semantics, map signatures or semantic open questions
in tasks 4.5/4.6; these bodies were preserved. Broader semantics and vendored
cases outside the current harness remain UNVERIFIED. No commit, br command,
task tick, issue closure or evidence-index edit was performed.

''' + (ev / "compiler-fixes.md").read_text() + '''

## Identifier-continuation compiler errors (run 40), corrected

SetFunctions.java:586/596/606/616/626/636/646/656/666/676 (column 10):
`cannot find symbol: class XacmlLegacyFunc` -> add the annotation import.
These ten errors have the same root cause. Runs 41, 42, 44, 45 and 46
have zero compiler diagnostics; intentional red lookup runs fail assertions.
'''
(ev / "summary.md").write_text(summary)
blockers = '''# Conformance blocker status: RESOLVED, JDK 11 reactor green

Final gate: 46-reactor-verify-higher-order-identifiers.txt, BUILD SUCCESS;
774 tests / 0 failures / 0 errors / 9 existing skips. OASIS 2.0 347 pass,
OASIS 3.0 347 pass, RSA 17 pass; two examples pass. No failed case remains
in the configured suites. Case evidence: 46-conformance-testcases.json.

| Case / earlier blocker | Cause and classification | Approval / repair | Final status |
| --- | --- | --- | --- |
| IIA001/IIB001/IIC001/IID001/IIE001/IIIA006, run 22 | Pre-existing harness/test-support absent optional collection comparison | Owned test utility repair; original assertions preserved | RESOLVED |
| IIC104, both suites | Pre-existing core day-time subtraction sign error | Conductor-approved sign restoration; focused red/green tests | RESOLVED |
| IIIF001/IIIG006, XACML 3.0 | Pre-existing XML mapper chose leading whitespace rather than DOM content | Conductor-approved content selection; focused red/green tests | RESOLVED; both IIIG006 invocations pass |
| IIC106/IIC107, both suites | Pre-existing date/year-month conversion, plus characterized combined-period calendar clipping | Conductor-approved date patch and arithmetic-class scope; runs 34 red / 35 green | RESOLVED |
| IIC150, run 36 | Pre-existing core missing 3.0 duration bag identifiers; import stops before evaluation | required-duration-bag-alias-fix.patch APPROVED/APPLIED; run 39 red / 41 green | RESOLVED |
| IIC164, run 42 | Pre-existing core missing 3.0 any-of and sibling higher-order identifiers; import stops before evaluation | Conductor-approved identifier-class scope; run 44 red / 45 green | RESOLVED |

The exact JUnit compile-scope POM patch, core sign patch, XML content patch,
date Period patch and duration bag alias patch are applied as approved.
The identifier family scan also repairs ten duration set IDs and four
higher-order IDs with retained legacy aliases. No method body was changed
by the identifier repairs. Their normative references and all 47 registry
tests are in identifier-audit.md / 46-identifier-audit.json.

No new semantic failure was revealed in run 46. Semantics outside previously
approved date/time arithmetic remain untouched. Additional semantic design
questions in tasks 4.5/4.6 and unexecuted cases beyond inherited bounds are
UNVERIFIED; they are not described as passing by this gate.

The 30 explicit case exclusions per OASIS suite, loop bounds, fixture and
expected-result files are unchanged. 46-existing-case-exclusions.json equals
the run 36 exclusions; scope-check.txt confirms preserved source/input hashes.
Each suite continues to execute IIIG006 twice and counts it twice.

No new repair proposal or authorization is required to obtain the configured
green gate. Prior blocked states remain archived; no tracking command,
commit, task tick, issue closure or fixture/result change was made.
'''
(ev / "conformance-blockers.md").write_text(blockers)
print("summary.md and conformance-blockers.md updated with final green gate and all repair dispositions")
