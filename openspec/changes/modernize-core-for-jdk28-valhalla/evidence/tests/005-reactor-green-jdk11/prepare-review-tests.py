from pathlib import Path
import hashlib
import json
import re

repo = Path.cwd()
ev = Path(__file__).parent
previous = json.loads((ev / "43-protected-before-higher-order-identifiers.json").read_text())
assert all((repo / name).is_file() and hashlib.sha256((repo / name).read_bytes()).hexdigest() == digest
           for name, digest in previous.items())
owned = ["xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java",
         "xacml-pdp/src/test/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandlerTest.java",
         "xacml-core/src/main/java/org/xacml4j/v30/types/ISO8601DateTime.java",
         "xacml-core/src/main/java/org/xacml4j/util/DateTimeDurationUtils.java"]
protected = {name: digest for name, digest in previous.items() if name not in owned}
for pattern in ("xacml-core/src/**/*.java", "xacml-pdp/src/**/*.java", "xacml-test/src/**/*.java",
                "xacml-xml/src/**/*.java", "xacml-conformance-tests/src/test/java/**/*.java"):
    for path in repo.glob(pattern):
        name = str(path.relative_to(repo))
        if name not in owned:
            protected[name] = hashlib.sha256(path.read_bytes()).hexdigest()
(ev / "48-protected-before-review.json").write_text(json.dumps(protected, indent=2) + "\n")
(ev / "48-review-source-before.json").write_text(json.dumps({name: (repo / name).read_text() for name in owned}, indent=2) + "\n")
for name in ("summary.md", "conformance-blockers.md", "scope-check.txt", "surefire-summary.txt", "command.txt"):
    archive = ev / ("46-before-review-" + name)
    assert not archive.exists()
    archive.write_bytes((ev / name).read_bytes())
print(f"Review snapshot: {len(protected)} protected hashes; four existing sources/tests authorized for writes")
