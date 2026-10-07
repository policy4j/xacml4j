"""Record every observed failure; disable only reproduced out-of-scope cases."""
import csv
import json
from collections import Counter
from pathlib import Path

RUN = Path(__file__).resolve().parent
REPO = RUN.parents[5]
MANIFEST = REPO / "xacml-conformance-tests/src/test/manifest/conformance-manifest.csv"
CORE = "xacml-core/src/main/java/org/xacml4j/"
PDP = "xacml-pdp/src/main/java/org/xacml4j/"
XML = "xacml-xml/src/main/java/org/xacml4j/"
SUPPORT = "xacml-test/src/main/java/org/xacml4j/"
RESOURCES = "xacml-conformance-tests/src/test/resources/"


def classify(row):
    suite, case = row["suite"], row["case_id"]
    classification = "engine defect"
    bug = "BUG-TBD-" + case
    evidence = []
    if case == "IIA004":
        classification = "fixture/spec issue"
        reason = "Deliberately invalid policy omits AttributeId; importer rejects it; IIA004Special.txt permits validation-time rejection but the response-only path cannot evaluate it"
        evidence = [RESOURCES + row["policy_files"].split(";")[0] + ":1", RESOURCES + str(Path(row["request_file"]).parent / "IIA004Special.txt") + ":3"]
    elif case == "IIA005":
        reason = "Request omits AttributeId; XML mapping throws IllegalArgumentException before the PDP can return the expected syntax-error Indeterminate"
        evidence = [CORE + "v30/Attribute.java:163", XML + ("v30/xml/Xacml20RequestContextUnmarshaller.java:257" if suite == "oasis20" else "v30/xml/Xacml30RequestContextFromJaxbToObjectModelMapper.java:504")]
    elif case == "IIC012":
        classification = "fixture/spec issue"
        reason = "Fixture declares a non-boolean Condition; static policy validation rejects its integer result before the supplied runtime Indeterminate response can be produced"
        evidence = [RESOURCES + row["policy_files"].split(";")[0] + ":17", CORE + "v30/policy/Condition.java:59"]
    elif case == "IIC020":
        reason = "integer-divide returns DoubleVal; integer-greater-than-or-equal then fails invocation type matching and returns Indeterminate instead of Permit"
        evidence = [CORE + "v30/policy/function/impl/ArithmeticFunctions.java:166"]
    elif case in {"IIA022", "IIA024"}:
        reason = "Integer response value 56 is serialized as the IntegerVal debug string instead of the XML Schema integer lexical value 56"
        evidence = [CORE + "v30/types/TypeToString.java:198", XML + "v30/xml/TypeToXacml30.java:341"]
    elif case == "IIA023":
        classification = "fixture/spec issue"
        reason = "Fixture expects Permit but request dateTime 1056-11-05T19:08:12-14:30 uses a timezone beyond the XML Schema maximum -14:00; JAXP rejects -870 minutes"
        evidence = [RESOURCES + row["request_file"] + ":42", "https://www.w3.org/TR/xmlschema-2/#dateTime-timezones"]
    elif case in {"IIC300", "IIC310", "IIC320"}:
        operation = {"IIC300": "starts-with", "IIC310": "ends-with", "IIC320": "contains"}[case]
        reason = f"string-{operation} applies the fixture's search string and candidate in the reverse order; the true condition becomes false and returns NotApplicable instead of Permit"
        evidence = [CORE + "v30/policy/function/impl/StringFunctions.java:54", RESOURCES + row["policy_files"].split(";")[0] + ":19"]
    elif case in {"IIC302", "IIC303", "IIC312", "IIC313", "IIC322", "IIC323", "IIC330", "IIC331", "IIC332", "IIC333", "IIC334", "IIC335"}:
        cause = next(line.removeprefix("Caused by: ") for line in row["detail"].splitlines() if "Invalid XACML functionId=" in line)
        function_id = cause.split('functionId="', 1)[1].split('"', 1)[0]
        reason = f"Default function provider cannot resolve {function_id}; policy import fails with PolicySyntaxException"
        evidence = [CORE + "v30/marshal/PolicyUnmarshallerSupport.java:71", CORE + "v30/policy/function/impl/StringFunctions.java:63"]
    elif case == "IID029":
        classification = "fixture/spec issue"
        reason = "Policy1 requires action-id in access-subject while the request supplies action-id in the action category; initial Target matching becomes Indeterminate instead of the expected Permit"
        evidence = [RESOURCES + "xacml3.0-ct-v.0.4/IID029Policy1.xml:13", RESOURCES + row["request_file"] + ":19"]
    elif case == "IID300":
        reason = "3.0 policy permit-overrides loses the Indeterminate-P plus Deny combination and returns Deny instead of the expected Indeterminate"
        evidence = [CORE + "v30/policy/combine/impl/PermitOverrides.java:93", RESOURCES + row["policy_files"].split(";")[0] + ":10"]
    elif case == "IIIA340":
        reason = "Positive infinity is serialized as Infinity instead of the XML Schema double lexical form INF in included attributes"
        evidence = [CORE + "v30/types/TypeToString.java:177"]
    elif case == "IIIC001":
        reason = "2.0 hierarchy response loses ResourceId urn:root; the XML mapper reads only included resource attributes but this single-resource request does not include them"
        evidence = [XML + "v30/xml/Xacml20ResponseContextMarshaller.java:156", XML + "v30/xml/Xacml20RequestContextUnmarshaller.java:202"]
    elif case in {"IIIC002", "IIIC003"}:
        count = "3" if case == "IIIC002" else "7"
        reason = f"Configured default multiple-resource chain does not expand hierarchical Children/Descendants scope; expected {count} results but received one; IIICSpecial.txt describes the required resource tree"
        evidence = [PDP + "v30/pdp/request/MultipleResourcesHandler.java:42", RESOURCES + str(Path(row["request_file"]).parent / "IIICSpecial.txt") + ":1"]
    elif suite == "oasis20" and case in {"IIIG001", "IIIG002", "IIIG003", "IIIG004", "IIIG005"}:
        reason = "Legacy XPath over full request Attribute/Subject nodes is transformed into category Content lookup; it finds no request nodes or fails invocation instead of the vendored expected decision"
        evidence = [CORE + "v30/policy/function/impl/XPathFunctions.java:76", RESOURCES + row["policy_files"].split(";")[0] + ":20"]
    elif case == "IIIE301":
        classification = "fixture/spec issue"
        reason = "Draft fixture uses urn:oasis:names:tc:xacml:3.0:multiple:content-selector; engine recognizes the final profile:multiple:content-selector identifier; expected two results but received one"
        bug = "policy4j-the.3.7"
        evidence = [RESOURCES + row["request_file"] + ":65", PDP + "v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java:59"]
    elif case == "IIIE302":
        classification = "harness defect"
        reason = "xacml-test compares included category lists by position; actual subject-first and expected resource-first categories fail despite order-independent XACML category meaning; comparison is outside this lane"
        bug = "policy4j-the.3.7"
        evidence = [SUPPORT + "v20/Xacml30TestUtility.java:86"]
    elif case == "IIIE303":
        reason = "RequestReference JAXB IDREF resolves to AttributesType; XML request mapper casts it to String and throws ClassCastException before evaluation"
        bug = "policy4j-the.3.7"
        evidence = [XML + "v30/xml/Xacml30RequestContextFromJaxbToObjectModelMapper.java:495"]
    elif case in {"IIIG300", "IIIG301"}:
        expected, actual = (2, 5) if case == "IIIG300" else (3, 4)
        reason = f"Returned PolicyIdentifierList contains {actual} policy references instead of the expected {expected}; non-applicable/evaluated reference tracking includes extra policies"
        evidence = [CORE + "v30/policy/RootEvaluationContext.java:201", SUPPORT + "v20/Xacml30TestUtility.java:128"]
    else:
        raise AssertionError("Unclassified failure: " + suite + "/" + case)
    return {"classification": classification, "reason": reason, "bug_id": bug, "evidence": evidence}


def main():
    attempt = json.loads((RUN / "06-case-results.json").read_text())["cases"]
    failures = [row for row in attempt if row["outcome"] in {"failed", "error"}]
    classifications = {(row["suite"], row["case_id"]): classify(row) for row in failures}
    rows = list(csv.DictReader(MANIFEST.open()))
    for row in rows:
        key = row["suite"], row["case_id"]
        if key in classifications:
            item = classifications[key]
            row["enabled"] = "false"
            row["disposition"] = "special-harness" if row["case_id"] in {"IID029", "IID030"} else "skipped"
            row["reason"] = item["classification"].capitalize() + ": " + item["reason"]
            row["bug_id"] = item["bug_id"]
    for row in rows:
        assert all("," not in value and "\n" not in value for value in row.values()), row
    with MANIFEST.open("w", newline="") as stream:
        writer = csv.DictWriter(stream, fieldnames=list(rows[0]), lineterminator="\n")
        writer.writeheader()
        writer.writerows(rows)
    details = [{"suite": row["suite"], "case_id": row["case_id"],
                "observed_outcome": row["outcome"], "message": row["message"],
                **classifications[(row["suite"], row["case_id"])],
                "status": "disabled pending conductor bug triage"} for row in failures]
    (RUN / "conformance-blockers.json").write_text(json.dumps(details, indent=2) + "\n")
    lines = ["# Conformance blockers", "", "All 880 cases were tried in run 04. Run 06 reran all cases after owned harness fixes.",
             "The 44 remaining failures are outside the granted source scope. No engine, fixture or expected-response changes were made.",
             "IID029/IID030 retain special-harness disposition; enabled=false records the blocked 3.0 IID029 fixture explicitly.",
             "BUG-TBD-* identifiers await the conductor. IIIE301-303 use existing policy4j-the.3.7.", "",
             "| Suite | Case | Classification | Bug | Cause and evidence |", "| --- | --- | --- | --- | --- |"]
    for item in details:
        references = "; ".join(item["evidence"])
        lines.append(f"| {item['suite']} | {item['case_id']} | {item['classification']} | {item['bug_id']} | {item['reason']}. {references}; 06-case-results.json |")
    lines += ["", "## Resolved harness failures", "",
              "- oasis30/IIE001 and IIE002: draft omitted referenced-policy files. Manifest now lists both dependencies using canonical filename case. Original response assertions pass in run 06.",
              "- oasis20/IIE003 and oasis30/IIE003: execute method 1 of IIE003Special.txt. Assert the invalid PolicyId2 static-type rejection; import PolicyId1 and compare the original response. Both pass in run 06.",
              "- The one extra RSA closing brace introduced during runner replacement was a harness compile error in run 03; removed before run 04.", "",
              "## Classification limits", "",
              "The observed import errors, response differences and stack traces are fresh run 06 evidence. Root-cause classifications use the cited source and fixtures; policy-reference tracking and hierarchical-profile implementation details need maintainer review.",
              "IIA023's timezone range was checked against W3C XML Schema Part 2, section 3.2.7.3: https://www.w3.org/TR/xmlschema-2/#dateTime-timezones.", ""]
    (RUN / "conformance-blockers.md").write_text("\n".join(lines))
    print(json.dumps({"failures": len(details), "classification_counts": dict(Counter(item["classification"] for item in details)),
                      "disposition_counts": {suite: dict(Counter(row["disposition"] for row in rows if row["suite"] == suite)) for suite in {row["suite"] for row in rows}}}, indent=2))


if __name__ == "__main__":
    main()
