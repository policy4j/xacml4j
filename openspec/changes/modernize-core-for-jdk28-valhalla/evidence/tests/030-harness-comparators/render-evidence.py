"""Render run 030 from completed Maven logs, captured reports, and scoped hashes."""
import csv
import difflib
import hashlib
import json
import re
import xml.etree.ElementTree as ET
from collections import Counter
from pathlib import Path

RUN = Path(__file__).resolve().parent
REPO = RUN.parents[5]
COUNTS = ("tests", "failures", "errors", "skipped")
SOURCES = {
    "xacml-test/src/main/java/org/xacml4j/v20/Xacml20TestUtility.java": "00-before-Xacml20TestUtility.java",
    "xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java": "00-before-Xacml30TestUtility.java",
    "xacml-test/src/main/java/org/xacml4j/v30/XacmlPolicyTestSupport.java": "00-before-XacmlPolicyTestSupport.java",
    "xacml-test/src/test/java/org/xacml4j/v20/Xacml30TestUtilityTest.java": "00-before-Xacml30TestUtilityTest.java",
    "xacml-test/src/test/java/org/xacml4j/v20/Xacml20TestUtilityTest.java": None,
    "xacml-test/src/test/java/org/xacml4j/v30/XacmlPolicyTestSupportTest.java": None,
    "xacml-conformance-tests/src/test/manifest/conformance-manifest.csv": "00-before-conformance-manifest.csv",
}


def read_json(name):
    return json.loads((RUN / name).read_text())


def write_json(name, value):
    (RUN / name).write_text(json.dumps(value, indent=2) + "\n")


def normalize(value):
    return value.replace(str(REPO), "<repo>").replace(str(Path.home()), "<home>")


def write_csv(name, fields, rows):
    with (RUN / name).open("w", newline="") as output:
        writer = csv.DictWriter(output, fieldnames=fields)
        writer.writeheader()
        writer.writerows(rows)


def count_row(counts):
    return " | ".join(str(counts[key]) for key in COUNTS)


def license_header(content):
    start = content.index(b"/*")
    return content[start:content.index(b"*/", start) + 2]


modules = read_json("11-full-reactor-results.json")
cases = read_json("11-final-case-results.json")
probe = read_json("09-conformance-gate-cases.json")
prior_modules = read_json("00-prior-module-counts.json")
prior_cases = read_json("00-prior-case-results.json")
assert all(entry["counts"]["failures"] == entry["counts"]["errors"] == 0 for entry in modules.values())
assert all(row["outcome"] in {"passed", "skipped"} for row in cases["cases"])
assert read_json("11-before-full-gate.json") == read_json("11-after-full-gate.json")

# Only completed logs are redacted. Fresh Surefire XML remains in target/, not copied to evidence.
for path in RUN.glob("*.log"):
    path.write_text(normalize(path.read_text()))

start = read_json("00-source-hashes.json")
end = read_json("11-after-full-gate.json")
scope_keys = lambda data: {key: value for key, value in data.items()
                          if key.startswith("xacml-") or key == "pom.xml"}
start, end = scope_keys(start), scope_keys(end)
modified = [key for key in sorted(start.keys() & end.keys()) if start[key] != end[key]]
added = sorted(end.keys() - start.keys())
removed = sorted(start.keys() - end.keys())
assert set(modified + added) == set(SOURCES) and not removed, (modified, added, removed)
assert all(hashlib.sha256((REPO / key).read_bytes()).hexdigest() == digest for key, digest in end.items())
protected = [key for key in start if key.startswith("xacml-conformance-tests/src/test/resources/")
             or key.startswith("xacml-xml-jaxb/src/main/resources/xsd/")]
poms = [key for key in start if key.endswith("pom.xml")]
assert all(start[key] == end[key] for key in protected + poms)
headers = {}
patches = []
for key, backup in SOURCES.items():
    current = (REPO / key).read_bytes()
    before = (RUN / backup).read_bytes() if backup else b""
    patches.append("".join(difflib.unified_diff(before.decode().splitlines(keepends=True),
                                              current.decode().splitlines(keepends=True),
                                              fromfile="a/" + key, tofile="b/" + key)))
    if key.endswith(".java"):
        baseline = before if backup else (RUN / "00-before-Xacml20TestUtility.java").read_bytes()
        headers[key] = license_header(current) == license_header(baseline)
assert all(headers.values())
(RUN / "task.patch").write_text("".join(patches))
write_json("source-audit.json", {
    "modified_since_packet_start": modified, "added_since_packet_start": added, "removed": removed,
    "all_other_module_sources_unchanged": True,
    "protected_fixture_and_xsd_files_unchanged": len(protected), "poms_unchanged": len(poms),
    "license_headers_identical_or_copied_from_module_template": headers,
    "no_source_or_pom_change_during_final_clean_verify": True,
    "current_bytes_match_final_gate_snapshot": True,
})

index = lambda rows: {(row["suite"], row["case_id"]): row for row in rows}
old_rows = index(prior_cases["cases"])
probe_rows = index(probe["cases"])
new_rows = index(cases["cases"])
assert len(old_rows) == len(probe_rows) == len(new_rows) == 880
assert old_rows.keys() == new_rows.keys()
delta = [{"suite": suite, "case_id": case_id, "before": old_rows[suite, case_id]["outcome"],
          "strict_probe": probe_rows[suite, case_id]["outcome"], "after": row["outcome"],
          "disposition": row["disposition"], "enabled": row["enabled"], "bug_id": row["bug_id"]}
         for (suite, case_id), row in new_rows.items()]
changed = [row for row in delta if row["before"] != row["after"]]
assert {(row["suite"], row["case_id"]) for row in changed} == {
    ("oasis30", "IIIE302"), ("oasis30", "IIIA030"), ("oasis30", "IIIA330")}
write_csv("per-case-delta.csv", list(delta[0]), delta)
write_json("case-delta.json", {"baseline": "run 024 final case receipts (historical)",
                              "changed_cases": changed, "unchanged_cases": 877})
pending = [new_rows["oasis30", key] for key in ("IIA022", "IIA024", "IIIA340")]
assert all(row["enabled"] == "false" and row["bug_id"] == "policy4j-the.4.16" for row in pending)
manifest_before = index(list(csv.DictReader((RUN / "00-before-conformance-manifest.csv").open())))
manifest_after = index(list(csv.DictReader((REPO / next(key for key in SOURCES if key.endswith(".csv"))).open())))
manifest_changes = [{"suite": suite, "case_id": case_id, "before": before, "after": manifest_after[suite, case_id]}
                    for (suite, case_id), before in manifest_before.items()
                    if before != manifest_after[suite, case_id]]
assert len(manifest_changes) == 3
assert all(manifest_before["oasis30", row["case_id"]] == manifest_after["oasis30", row["case_id"]] for row in pending)
write_json("manifest-changes.json", {"changed_rows": manifest_changes, "unchanged_rows": 877,
                                     "pending_4_16_rows_byte_content_unchanged": True})

coverage = {}
for suite in ("oasis20", "oasis30"):
    coverage[suite] = {"nonempty_expected_obligations": [], "nonempty_status_details": [], "included_attributes": []}
    for row in cases["cases"]:
        if row["suite"] != suite or row["outcome"] != "passed":
            continue
        root = ET.parse(REPO / "xacml-conformance-tests/src/test/resources" / row["expected_response_file"]).getroot()
        name = lambda element: element.tag.rsplit("}", 1)[-1]
        if any(name(element) == "Obligation" for element in root.iter()):
            coverage[suite]["nonempty_expected_obligations"].append(row["case_id"])
        if any(name(element) == "StatusDetail" and (len(element) or (element.text or "").strip())
               for element in root.iter()):
            coverage[suite]["nonempty_status_details"].append(row["case_id"])
        if any(name(element) == "Attributes" for element in root.iter()):
            coverage[suite]["included_attributes"].append(row["case_id"])
write_json("field-coverage.json", coverage)

classes = modules["xacml-test"]["classes"]
old_classes = prior_modules["xacml-test"]["classes"]
class_rows = [{"class": key, "before": old_classes.get(key, {}).get("tests", 0),
               "after": row["tests"], "failures": row["failures"], "errors": row["errors"],
               "skipped": row["skipped"]} for key, row in classes.items()]
assert old_classes.keys() <= classes.keys()
assert all(row["after"] >= row["before"] for row in class_rows)
write_csv("test-class-counts.csv", list(class_rows[0]), class_rows)

# Keep exact Maven Results blocks and every class/aggregate count; omit large DEBUG streams here.
parts = []
log_totals = {}
for path in sorted(RUN.glob("[0-9][0-9]-*.log")):
    if "probe" in path.name:
        continue
    parts.append("===== " + path.name + " =====")
    module = ""
    in_results = False
    for line in path.read_text().splitlines():
        phase = re.search(r"--- surefire:[^:]+:test .* @ ([^ ]+) ---", line)
        if phase:
            module = phase.group(1)
        if "Results:" in line:
            in_results = True
            parts.append("Maven Results: " + module)
        if "Tests run:" in line or "BUILD SUCCESS" in line or "BUILD FAILURE" in line:
            parts.append(line)
        elif in_results:
            parts.append(line)
        total = re.search(r"Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)$", line)
        if total:
            in_results = False
            if path.name == "11-full-reactor-clean-verify.log":
                log_totals[module] = dict(zip(COUNTS, map(int, total.groups())))
    parts.append("")
assert log_totals == {module: entry["counts"] for module, entry in modules.items()}
(RUN / "surefire-summary.txt").write_text("\n".join(parts) + "\n")

totals = {key: sum(entry["counts"][key] for entry in modules.values()) for key in COUNTS}
module_table = "\n".join("| " + key + " | " + count_row(entry["counts"]) + " |" for key, entry in modules.items())
suite_rows = []
for suite, outcomes in cases["outcomes"].items():
    dispositions = cases["dispositions"][suite]
    special = [row for row in cases["cases"] if row["suite"] == suite and row["disposition"] == "special-harness"]
    special_pass = sum(row["outcome"] == "passed" for row in special)
    special_skip = sum(row["outcome"] == "skipped" for row in special)
    suite_rows.append("| " + " | ".join(map(str, (suite,
        sum(outcomes.values()), outcomes.get("passed", 0), outcomes.get("skipped", 0),
        dispositions.get("executed", 0), f"{special_pass} pass / {special_skip} skip",
        dispositions.get("skipped", 0)))) + " |")
suite_table = "\n".join(suite_rows)
class_table = "\n".join("| " + row["class"].rsplit(".", 1)[-1] + " | " + str(row["before"])
                         + " | " + str(row["after"]) + " |" for row in class_rows)

summary = f"""# Harness comparators — bugs 3.10, 3.11 and IIIE302 (3.7)

Result: the final JDK 21 full reactor `clean verify` passed. {totals['tests']} tests,
{totals['failures']} failures, {totals['errors']} errors and {totals['skipped']} skips.
Conformance: 851 executed fixture cases passed; 29 remain skipped. The stricter
assignment comparison exposed two XML output defects; both are recorded below.

JDK: `Java version: 21.0.6, vendor: Oracle Corporation, runtime: /Library/Java/JavaVirtualMachines/graalvm-21.jdk/Contents/Home`.
Maven: Apache Maven 3.9.16. Each Maven run selected `jenv prefix 21` and passed
`-Dlicense.skipUpdateLicense=true`. See [command.txt](command.txt).

## Changes and test proof

| Issue | Change | Tests and receipts |
| --- | --- | --- |
| 3.10, XACML 2.0 obligations | Use the actual obligation list. Compare multisets without collapsing duplicate IDs or reusing a match. Check obligation ID and FulfillOn, assignment ID, data type, parsed value and extension attributes. Normalize absent/empty obligations. | `Xacml20TestUtilityTest`: all-field negative controls, duplicate IDs/counts, permutations, missing/extra obligations, typed values. Original source: 16 tests, 14 failures and 1 error in [01-comparators-red-results.json](01-comparators-red-results.json). Final: 18/0/0. |
| 3.10, XACML 2.0 status | Check nullable status and the full nested StatusCode chain; compare StatusDetail DOM fields rather than object identity. Keep decision and ResourceId checks. | `nestedStatusCodesMustMatch`, `statusDetailsCompareDomFieldsRatherThanNodeIdentity`, `resourceIdAndDecisionStillMustMatch`; red in run 01, green in run 08. |
| 3.10, RSA/examples | Match obligations by ID plus the full multiset of assignment values and metadata. Retain the final object equality assertion. | `XacmlPolicyTestSupportTest`: six characterization invocations already passed before the edit and still pass. The original ID matcher was followed by object equality, which already rejected ordinary value/type/issuer/category/ID differences. This is an explicit matcher change, not a claim that these six controls were previously false positives. |
| 3.11, typed values | Parse registered data types on both sides and compare value objects. Concatenate XML text chunks before parsing; strings retain case/whitespace. Unknown extension types retain exact lexical comparison; invalid registered lexical forms fail. | `lexicalFormsCompareByDataType`, `differentValuesOfTheSameTypeRemainRejected`, `stringsKeepWhitespaceAndTextChunkBoundariesDoNotMatter`, `obligationAndAdviceAssignmentsAlsoUseTypedValues`; 27.50/27.5 and integer 1/01 positive controls, 27.5/27.6 and abc/ABC negative controls. Red in run 01, final green in run 08. |
| 3.11, missing Content | Both absent Content values match; a one-sided absent value fails in either direction. Compare present Content type and DOM structure. | `absentContentIsSafeAndPresenceIsComparedSymmetrically`, `contentComparesTypeAndDomStructure`: original NPE/mismatch in run 01, green in run 08. |
| IIIE302, category order | Match included category lists as full multisets by Category, XML ID, attributes and Content. Attribute/value order is ignored; duplicate counts and all fields remain checked. | `includedCategoriesAttributesAndValuesAreMultisets`, `categoryMatchingKeepsAllAttributeFields`, `valueBagsAreUnorderedButKeepDuplicatesAndMetadata`: red in run 01, green in run 08. IIIE302 re-enabled and passed in runs 05, 09, 10 and 11. |
| Inherited assignment fields | Check `getOtherAttributes()` in both the public assignment assertion and the aggregate multiset matcher, for 2.0 and 3.0. | `assignmentExtensionAttributesMustMatch`: direct/response paths for 2.0, direct/obligation/advice paths for 3.0. All five failed before the fix in [07-assignment-metadata-red-results.json](07-assignment-metadata-red-results.json). Same values pass; changed values, missing fields and changed namespaces fail after the fix. |

Source entry points: `Xacml20TestUtility.java:76` (status), `:98` (obligations),
`:113` (assignments); `Xacml30TestUtility.java:90` (categories), `:133` (typed
content), `:152` (boolean lexical parsing), `:259` (assignments);
`XacmlPolicyTestSupport.java:201` (model obligation matcher).
The complete packet diff is [task.patch](task.patch).

XACML 2.0 `ResultType` carries Decision, Status, Obligations and ResourceId
(`xacml-xml-jaxb/target/generated-sources/xjc/org/oasis/xacml/v20/jaxb/context/ResultType.java:52-58`).
It has no included categories, AssociatedAdvice or policy identifier list to
compare. StatusMessage remains outside the oracle, matching the existing 3.0
utility (`Xacml30TestUtility.java:206` checks only the top-level status code).
The new 2.0 nested-code/StatusDetail checks are stronger than that 3.0 status
check. This packet does not widen 3.0 status comparison.

Typed lexical/value comparison follows [XML Schema Part 2, sections 2.2 and 2.3](https://www.w3.org/TR/xmlschema-2/#datatype),
with strict strings from [section 3.2.1](https://www.w3.org/TR/xmlschema-2/#string).
The four boolean lexical forms are defined by [section 3.2.2](https://www.w3.org/TR/xmlschema-2/#boolean).
Native Tavily extraction of the 1.1 page returned unrelated SVG material;
the official W3C 1.0 source was checked through the web fallback.

## Red and green runs

| Run | Fresh coverage | Tests | Failures | Errors | Skips | Result |
| --- | --- | ---: | ---: | ---: | ---: | --- |
| 01 | Three comparator classes, production unchanged | 70 | 25 | 2 | 0 | Expected red |
| 02 | Three comparator classes after initial repair | 70 | 1 | 0 | 0 | Boolean parser defect exposed |
| 03 | 3.0 comparator, before boolean oracle repair | 50 | 3 | 0 | 0 | Expected red |
| 04 | Three comparator classes | 72 | 0 | 0 | 0 | Green |
| 05 | Conformance, before assignment extension audit | 901 | 0 | 0 | 27 | Green; superseded by stronger oracle |
| 06 | Full clean reactor, before assignment extension audit | 5421 | 0 | 0 | 33 | Green; superseded |
| 07 | Three comparator classes, before extension-field fix | 77 | 5 | 0 | 0 | Expected red |
| 08 | Three comparator classes | 77 | 0 | 0 | 0 | Green |
| 09 | Full conformance with stronger assignment checks | 901 | 2 | 0 | 27 | IIIA030 and IIIA330 exposed |
| 10 | Conformance after recording the two blockers | 901 | 0 | 0 | 29 | Green |
| 11 | Final full clean reactor | {totals['tests']} | {totals['failures']} | {totals['errors']} | {totals['skipped']} | Green |

Counts above are Surefire invocations, including parameterized invocations and
skips. Focused receipts filter to the requested classes, so stale unrelated
target reports are not counted. Full run 11 follows `clean` and its XML totals
are checked against the Maven Results lines in [surefire-summary.txt](surefire-summary.txt).

## Final reactor counts

| Module | Tests run | Failures | Errors | Skipped |
| --- | ---: | ---: | ---: | ---: |
{module_table}
| Total | {count_row(totals)} |

Root and generated JAXB modules have no test classes. Their build phases pass.
Receipts: [11-full-reactor-results.json](11-full-reactor-results.json),
[11-full-reactor-clean-verify.log](11-full-reactor-clean-verify.log).

| xacml-test class | Historical run 024 | Final run 11 |
| --- | ---: | ---: |
{class_table}

No existing test invocations or assertions were removed. The module grew from
30 to 83 invocations: 18 new 2.0 controls, 29 added 3.0 invocations, six new
model-support controls. See [test-class-counts.csv](test-class-counts.csv).

## Conformance results and changed cases

| Suite | Manifest cases | Executed/pass | Actual skips | Executed disposition | Special harness | Skipped disposition |
| --- | ---: | ---: | ---: | ---: | --- | ---: |
{suite_table}
| Total | 880 | 851 | 29 | 848 | 3 pass / 1 skip | 28 |

The conformance module's 901 tests comprise 880 manifest case invocations and
21 harness/guard/helper tests. The two OASIS suite `testName` methods are
included in that helper count, not the 851 executed fixture cases.

| Case | Historical run 024 | Stronger-oracle run 09 | Final run 11 | Classification |
| --- | --- | --- | --- | --- |
| oasis30/IIIE302 | Skipped | Passed | Passed | Harness category-order defect fixed; executed with bug cleared |
| oasis30/IIIA030 | Passed | Failed | Skipped | Engine/XML output defect; obligation assignment2 loses XPathCategory; BUG-TBD-IIIA030 |
| oasis30/IIIA330 | Passed | Failed | Skipped | Engine/XML output defect; advice assignment2 loses XPathCategory; BUG-TBD-IIIA330 |

Those are the only three outcome/manifest changes; the other 877 are unchanged.
[per-case-delta.csv](per-case-delta.csv) records all 880 outcomes before, during
the strict probe and after. [11-final-case-results.csv](11-final-case-results.csv)
records every final disposition, reason and outcome.

No newly exposed OASIS 2.0 failure. All 362 enabled cases passed, including the
15 with nonempty expected obligations listed in [field-coverage.json](field-coverage.json).
Among enabled 3.0 fixtures, {len(coverage['oasis30']['nonempty_expected_obligations'])} carry expected obligations and one carries
included attributes (IIIE302). No enabled fixture in either suite supplies a
nonempty StatusDetail; status-detail proof is focused unit-test coverage only.

IIA022, IIA024 and IIIA340 remain disabled with policy4j-the.4.16, unchanged.
They were not executed or temporarily re-enabled. Typed-value and absent-Content
unit tests now cover the harness portions; their engine serialization fixes
remain in Luna's lane. The other inherited exclusions were also preserved.

## Blockers and residual risk

The two new case blockers are reproduced through the existing PDP harness and
actual XML response marshaller in [09-assignment-metadata-probe.json](09-assignment-metadata-probe.json).
Expected assignments retain `XPathCategory=urn:oasis:names:tc:xacml:3.0:attribute-category:resource`;
both actual assignments have an empty extension map. The read-only cause is
`Xacml30RequestContextFromJaxbToObjectModelMapper.java:424-433`: it copies the
typed value's data type/content, but discards `av.getOtherAttributes()`.
`TypeToXacml30.java:466` supplies XPathCategory to that typed-value map.
Fixing the mapper is outside this packet. No fixtures or expectations changed.
See [conformance-blockers.md](conformance-blockers.md) for the proposals.

A separate pre-existing core parser observation was found while building a
typed oracle: `BooleanVal.java:49` uses Java `Boolean.parseBoolean`, interpreting
XML Schema `1` and arbitrary invalid strings as false. Run 03 demonstrates the
three comparator failures before the harness normalizes the four legal boolean
forms and rejects invalid ones. Core was not edited. Broader engine/conformance
impact is UNVERIFIED; the conductor can file a separate bug if needed.

Mixed DOM content is compared structurally and in sequence, not by a semantic
XML canonicalizer. Registered-type equality uses the current core value types;
this packet does not repair their semantics. Remaining disabled cases are not
claimed as passing. No JMH run is needed for a test-oracle change.

## Scope and headers

[source-audit.json](source-audit.json) compares packet-start and final hashes:
only the six named xacml-test Java files and three manifest rows changed.
All other module source files, {len(poms)} poms, and {len(protected)} vendored
fixture/XSD files retained their bytes. Existing LGPL headers are byte-identical;
new test headers copy the module template. Before/after run 11 snapshots are
identical; current source bytes match the final gate snapshot. The shared tree's
older dirty files were preserved. No commit, beads command, task tick or issue
closure was performed. Evidence uses `<repo>`/`<home>` in place of checkout/home
paths.
Final artifact checks: [final-checks.json](final-checks.json) records the scoped
Git status/diff check, zero deleted existing 3.0 comparator-test assertions,
valid local evidence links and no unredacted checkout/home paths.
"""
(RUN / "summary.md").write_text(summary)
print(json.dumps({"final_modules": {key: entry["counts"] for key, entry in modules.items()},
                  "total": totals, "cases": cases["outcomes"], "changed_cases": changed,
                  "source_files_changed": len(SOURCES), "poms_preserved": len(poms),
                  "vendored_files_preserved": len(protected)}, indent=2))
