from pathlib import Path
import hashlib
import json
import re
import subprocess

repo = Path.cwd()
ev = Path(__file__).parent

def normalize(value):
    return re.sub(r"\x1b\[[0-9;]*m", "", value.replace(str(repo), "<repo>"))

def run(command):
    result = subprocess.run(command, capture_output=True, text=True)
    return result.returncode, normalize(result.stdout + result.stderr)

protected = json.loads((ev / "34-protected-before-date-arithmetic.json").read_text())
changed = [name for name, digest in protected.items()
           if not (repo / name).is_file()
           or hashlib.sha256((repo / name).read_bytes()).hexdigest() != digest]
before = json.loads((ev / "34-approved-arithmetic-source-before.json").read_text())
date = "xacml-core/src/main/java/org/xacml4j/v30/types/ISO8601Date.java"
date_time = "xacml-core/src/main/java/org/xacml4j/v30/types/ISO8601DateTime.java"
date_exact = (repo / date).read_text() == before[date].replace("duration.toDuration()", "duration.toPeriod()")
expected_dt = before[date_time]
for operation in ("plus", "minus"):
    pattern = r"(    public ISO8601DateTime " + operation + r"\(ISO8601YearMonthDuration v\)\{\n).*?(    \}\n)"
    expected_dt, count = re.subn(pattern, lambda m: m.group(1) + "        return ISO8601DateTime.of(value."
                                 + operation + "(v.toPeriod()));\n" + m.group(2), expected_dt, flags=re.DOTALL)
    assert count == 1
date_time_exact = (repo / date_time).read_text() == expected_dt
unchanged_authorized = [name for name in before if name not in (date, date_time)]
assert all((repo / name).read_text() == before[name] for name in unchanged_authorized)

xml = "xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30RequestContextFromJaxbToObjectModelMapper.java"
old_xml = json.loads((ev / "27-approved-source-before.json").read_text())[xml]
expected_xml = old_xml.replace("\t\tObject v = content.getContent().get(0);",
    '\t\tObject v = o.stream()\n\t\t\t\t.filter(value -> !(value instanceof String) || !((String)value).trim().isEmpty())\n\t\t\t\t.findFirst().orElse("");', 1)
xml_exact = (repo / xml).read_text() == expected_xml
pom_exact = (repo / "xacml-test/pom.xml").read_bytes() == (ev / "14-approved-pom.txt").read_bytes()

existing = [
    "xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java",
    "xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java",
    "xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionLegacyHandler.java",
    "xacml-pdp/src/test/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandlerTest.java",
    "xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXacmlXPathHandlerTest.java",
    "xacml-test/src/main/java/org/xacml4j/v30/ExpectedAttributeResolverBuilder.java",
    "xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java",
    "xacml-conformance-tests/src/test/java/org/xacml4j/v20/Xacml20ConformanceAttributeResolver.java",
    date_time, xml, date,
]
new = {
    "xacml-test/src/test/java/org/xacml4j/v30/ExpectedAttributeResolverBuilderTest.java": existing[5],
    "xacml-test/src/test/java/org/xacml4j/v20/Xacml30TestUtilityTest.java": existing[6],
    "xacml-conformance-tests/src/test/java/org/xacml4j/v20/Xacml20ConformanceAttributeResolverTest.java": existing[7],
    "xacml-core/src/test/java/org/xacml4j/v30/types/ISO8601DateTimeArithmeticTest.java": "xacml-core/src/test/java/org/xacml4j/v30/types/ISO8601DateTimeTest.java",
    "xacml-xml/src/test/java/org/xacml4j/v30/xml/Xacml30RequestContentTest.java": "xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30RequestContextUnmarshaller.java",
    "xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/DateTimeArithmeticBoundaryTest.java": "xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/DateTimeValueArithmeticFunctionTest.java",
}
header = re.compile(r"/\*.*?#L%.*?\*/", re.DOTALL)
changed_headers = []
for name in existing:
    code, original = run(["rtk", "proxy", "git", "show", "HEAD:" + name])
    assert code == 0
    if header.search(original).group() != header.search((repo / name).read_text()).group():
        changed_headers.append(name)
for name, template in new.items():
    if header.search((repo / name).read_text()).group() != header.search((repo / template).read_text()).group():
        changed_headers.append(name)

scope = [f"Protected source/input/previous evidence files compared: {len(protected)}",
         f"Protected files changed: {len(changed)}", *changed,
         f"ISO8601Date matches approved two-line Period patch: {date_exact}",
         f"ISO8601DateTime matches only two Period replacements since arithmetic snapshot: {date_time_exact}",
         "Previously approved day-time subtraction sign fix is retained in that snapshot.",
         f"Other approved arithmetic sources unchanged: {len(unchanged_authorized)}",
         f"XML mapper retains the exact approved content patch: {xml_exact}",
         f"Approved POM matches exact approved snapshot: {pom_exact}",
         f"Existing edited Java license headers checked: {len(existing)}",
         f"New Java headers matched to module templates: {len(new)}",
         f"Changed license headers: {len(changed_headers)}", *changed_headers,
         "Continued lane project-file inventory:", *existing, *new, "xacml-test/pom.xml"]
checks = [
    ["rtk", "git", "diff", "--quiet", "a8a90d46", "--", "xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/BagFunctions.java", "xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/XacmlDefaultFunctions.java"],
    ["rtk", "git", "diff", "--quiet", "a8a90d46", "--", "xacml-conformance-tests/src/test/java/org/xacml4j/v20/Xacml30ConformanceTest.java", "xacml-conformance-tests/src/test/resources/xacml3.0-ct-v.0.4/IIC150Policy.xml"],
    ["rtk", "git", "diff", "--check", "--", *existing, "xacml-test/pom.xml"],
    ["rtk", "git", "apply", "--reverse", "--check", str(ev / "required-pom-fix.patch")],
    ["rtk", "git", "apply", "--reverse", "--check", str(ev / "required-xml-content-fix.patch")],
    ["rtk", "git", "apply", "--reverse", "--check", str(ev / "required-date-period-fix.patch")],
    ["rtk", "git", "apply", "--check", str(ev / "required-duration-bag-alias-fix.patch")],
    ["rtk", "git", "status", "--short"],
    ["rtk", "git", "diff", "--cached", "--name-only"],
]
failed_checks = []
check_commands = []
for command in checks:
    code, output = run(command)
    rendered = " ".join(command)
    check_commands.append(rendered)
    scope.extend(["", rendered, f"Exit code: {code}", output.rstrip()])
    if code:
        failed_checks.append(rendered)
previous_scope = ev / "31-scope-before-date-arithmetic.txt"
if not previous_scope.exists():
    previous_scope.write_bytes((ev / "scope-check.txt").read_bytes())
(ev / "scope-check.txt").write_text(normalize("\n".join(scope)) + "\n")

gate_counts = {}
summary_lines = []
for log_name in ("34-date-arithmetic-before.txt", "35-date-arithmetic-after.txt", "36-reactor-verify-date-arithmetic.txt"):
    summary_lines.extend(["", "Run: " + log_name])
    module = "unknown"
    in_results = False
    for line in (ev / log_name).read_text().splitlines():
        match = re.search(r"--- .* @ ([^ ]+) ---", line)
        if match:
            module = match.group(1)
        count = re.search(r"Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)", line)
        if log_name.startswith("36-") and count and " - in " not in line:
            gate_counts[module] = [int(number) for number in count.groups()]
        if "[INFO] Results:" in line:
            in_results = True
            summary_lines.extend(["Module: " + module, line])
        elif in_results:
            summary_lines.append(line)
            if count:
                in_results = False
        elif count and " - in " not in line:
            summary_lines.extend(["Module: " + module, line])
        elif "Reactor Summary" in line or "BUILD SUCCESS" in line or "BUILD FAILURE" in line or re.search(r"\.\.\..*(SUCCESS|FAILURE|SKIPPED)", line):
            summary_lines.append(line)
assert gate_counts == {"xacml-core": [556,0,0,4], "xacml-xml": [34,0,0,0], "xacml-json": [5,0,0,1],
                       "xacml-pdp": [71,0,0,4], "xacml-test": [20,0,0,0], "xacml-conformance-tests": [41,0,1,0]}
(ev / "36-module-counts.json").write_text(json.dumps(gate_counts, indent=2) + "\n")
surefire_path = ev / "surefire-summary.txt"
old_summary = surefire_path.read_text().split("\nRun: 34-date-arithmetic-before.txt", 1)[0].rstrip()
surefire_path.write_text(normalize(old_summary + "\n" + "\n".join(summary_lines)) + "\n")

case_report = json.loads((ev / "36-conformance-testcases.json").read_text())
for case in case_report["org.xacml4j.v20.Xacml30ConformanceTest"]["testcases"]:
    if case["failure"]:
        assert case["case_ids"][-1] == "IIC149"
        case["failed_case_id"] = "IIC150"
        case["failure_phase"] = "policy import before decision and execution marker; directly reproduced in run 37"
        case["failure_classification"] = "pre-existing core bag-function identifier registration defect"
(ev / "36-conformance-testcases.json").write_text(json.dumps(case_report, indent=2) + "\n")
for suite_name, expected_count in (("Xacml20ConformanceTest",347), ("Xacml30ConformanceTest",264)):
    suite = case_report["org.xacml4j.v20." + suite_name]
    ids = [name for case in suite["testcases"] for name in case["case_ids"]]
    assert len(ids) == expected_count, (suite_name, len(ids))
    assert all(name in ids for name in ("IIC104", "IIC106", "IIC107", "IIIF001", "IIIG006"))
    assert ids.count("IIIG006") == 2
(ev / "36-existing-case-exclusions.json").write_bytes((ev / "31-existing-case-exclusions.json").read_bytes())

test_source = repo / "xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/DateTimeArithmeticBoundaryTest.java"
rows = re.findall(r'\{Operation\.(\w+), "([^"]+)", "([^"]+)", "([^"]+)"\}', test_source.read_text())
red_log = (ev / "34-date-arithmetic-before.txt").read_text()
failed_rows = set(re.findall(r'DateTimeArithmeticBoundaryTest\.arithmeticPreservesSignedCalendarBoundaries\[([^\n]+)\]  Time elapsed:.*<<< FAILURE!', red_log))
operation_names = {
    "DATE_ADD_YEAR_MONTH": "date-add-yearMonthDuration",
    "DATE_SUBTRACT_YEAR_MONTH": "date-subtract-yearMonthDuration",
    "DATETIME_ADD_YEAR_MONTH": "dateTime-add-yearMonthDuration",
    "DATETIME_SUBTRACT_YEAR_MONTH": "dateTime-subtract-yearMonthDuration",
    "DATETIME_ADD_DAY_TIME": "dateTime-add-dayTimeDuration",
    "DATETIME_SUBTRACT_DAY_TIME": "dateTime-subtract-dayTimeDuration",
}
operations = []
for operation, start, duration, expected in rows:
    label = f"{operation}: {start}, {duration} -> {expected}"
    operations.append({"function": operation_names[operation], "start": start, "duration": duration,
                       "expected": expected, "test": "arithmeticPreservesSignedCalendarBoundaries[" + label + "]",
                       "red_run": "FAIL" if label in failed_rows else "PASS", "green_run": "PASS"})
assert len(rows) == 50 and len(failed_rows) == 20
(ev / "36-arithmetic-operation-tests.json").write_text(json.dumps(operations, indent=2) + "\n")
inventory = []
for operation, function in operation_names.items():
    matching = [item for item in operations if item["function"] == function]
    failures = sum(item["red_run"] == "FAIL" for item in matching)
    inventory.append(f"| {function} | `{operation}` | {len(matching)} | {failures} | All pass |")
(ev / "arithmetic-operations.md").write_text("\n".join([
    "# Every existing arithmetic operation checked", "",
    "Each row is exercised through the production DateTimeArithmeticFunctions entrypoint by",
    "DateTimeArithmeticBoundaryTest.arithmeticPreservesSignedCalendarBoundaries; the parameter",
    "label identifies the operation and exact operands. All 50 independent expected values and",
    "red/green dispositions are retained in 36-arithmetic-operation-tests.json.", "",
    "| XACML operation | Test parameter label | Checks | Red failures | Green result |",
    "| --- | --- | ---: | ---: | --- |", *inventory, "",
    "Every operation covers positive, negative and zero durations. Calendar operations cover",
    "leap/non-leap February clipping in both directions, negative equivalents and combined",
    "year/month periods from February 29. Day-time operations cover month and leap-day rollover",
    "in both directions and retain the time-zone offset.", "",
    "The prior ISO8601DateTimeArithmeticTest also checks the four type-level operations:",
    "- plus(ISO8601DayTimeDuration): addPositive/Negative/ZeroDayTimeDuration.",
    "- minus(ISO8601DayTimeDuration): subtractPositive/Negative/ZeroDayTimeDuration.",
    "- plus(ISO8601YearMonthDuration): addPositive/Negative/ZeroYearMonthDuration.",
    "- minus(ISO8601YearMonthDuration): subtractPositive/Negative/ZeroYearMonthDuration.", "",
    "DateTimeValueArithmeticFunctionTest.testProvidedFunctions verifies registration for all six",
    "functions and their legacy identifiers. Those 3 tests and the prior 12 tests pass in run 35.", "",
    "Time duration arithmetic and date day-time arithmetic are not defined by XACML 3.0 A.3.7",
    "and are not supplied by the opened production class. No new operation was introduced;",
    "ISO8601Time and the duration classes remain unchanged. See arithmetic-reference.md."
]) + "\n")

command_path = ev / "command.txt"
marker = "# Conductor-approved expanded date arithmetic (runs 34 onward)"
prefix = command_path.read_text().split(marker, 1)[0].rstrip()
continuation = (ev / "date-arithmetic-commands.txt").read_text().rstrip()
command_path.write_text(normalize(prefix + "\n\n" + marker + "\n" + continuation + "\n\n# Final evidence and scope checks\n"
    + "rtk python3 " + str(ev.relative_to(repo)) + "/finalize-date-arithmetic.py\n" + "\n".join(check_commands) + "\n"
    + "rtk python3 " + str(ev.relative_to(repo)) + "/audit-final-date-arithmetic-evidence.py\n"))

blockers_archive = ev / "31-blockers-before-date-arithmetic.md"
if not blockers_archive.exists():
    blockers_archive.write_bytes((ev / "conformance-blockers.md").read_bytes())
(ev / "conformance-blockers.md").write_bytes((ev / "36-conformance-blockers.md").read_bytes())
(ev / "summary.md").write_text((ev / "36-gate-summary.md").read_text().rstrip() + "\n\n"
                              + (ev / "compiler-fixes.md").read_text())

print("Final gate: Tests run 727 / Failures 0 / Errors 1 / Skipped 9; BUILD FAILURE")
print("Case results: OASIS 2.0 347 completed; OASIS 3.0 264 completed + IIC150 import error; RSA 17 passed")
print("Arithmetic: 50 boundary checks, 20 red failures before fixes, all green afterward")
print(f"Protected: {len(protected)} compared / {len(changed)} changed; headers: {len(existing)} existing + {len(new)} new / {len(changed_headers)} changed")
print(f"Exact patches: date={date_exact}, dateTime={date_time_exact}, XML={xml_exact}, POM={pom_exact}; failed checks={len(failed_checks)}")
assert not changed and not changed_headers and date_exact and date_time_exact and xml_exact and pom_exact and not failed_checks
