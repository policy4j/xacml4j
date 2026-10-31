from pathlib import Path
import json
import re
import subprocess

repo = Path.cwd()
ev = Path(__file__).parent
checks = [
    ("xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java",
     'final static String MULTIPLE_CONTENT_SELECTOR', 1),
    ("xacml-conformance-tests/src/test/resources/xacml3.0-ct-v.0.4/IIIE301Request.xml",
     'AttributeId="urn:oasis:names:tc:xacml:3.0:multiple:content-selector"', 1),
    ("xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java", 'public static void assertAttributesType', 11),
    ("xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java", 'public static void assertResults', 9),
    ("xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30RequestContextFromJaxbToObjectModelMapper.java",
     'private RequestReference create', 8),
    ("xacml-core/src/main/java/org/xacml4j/util/DateTimeDurationUtils.java", 'public static Duration toXmlDayTimeDuration', 3),
    ("xacml-core/src/main/java/org/xacml4j/util/DateTimeDurationUtils.java", 'public static boolean isZero', 11),
    ("xacml-core/src/main/java/org/xacml4j/v30/types/ISO8601Duration.java", 'public final int getSeconds', 3),
]
rows = []
for name, marker, length in checks:
    command = ["rtk", "proxy", "git", "show", "a8a90d46:" + name]
    result = subprocess.run(command, text=True, capture_output=True)
    assert result.returncode == 0, result.stderr
    original = result.stdout.splitlines()
    current = (repo / name).read_text().splitlines()
    indices = [i for i, line in enumerate(original) if marker in line]
    assert len(indices) == 1, (name, marker, indices)
    index = indices[0]
    snippet = original[index:index + length]
    current_index = next(i for i, line in enumerate(current) if marker in line)
    assert current[current_index:current_index + length] == snippet, (name, marker)
    rows.append({"file": name, "baseline": "a8a90d46", "baseline_line": index + 1,
                 "current_line": current_index + 1, "snippet_unchanged": True,
                 "command": " ".join(command), "snippet": snippet})
(ev / "55-review-baseline-snippets.json").write_text(json.dumps(rows, indent=2) + "\n")
print("Confirmed eight case/gap source snippets unchanged from a8a90d46")
