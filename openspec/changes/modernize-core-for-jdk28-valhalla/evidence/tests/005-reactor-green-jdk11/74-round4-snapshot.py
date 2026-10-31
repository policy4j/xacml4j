from pathlib import Path
import hashlib
import json

repo = Path.cwd()
ev = Path(__file__).parent
allowed = [
    "xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/HigherOrderFunctions.java",
    "xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/HigherOrderVariadicFunctionsTest.java",
]
assert not (ev / "74-round4-before.json").exists()
files = set()
for module in repo.glob("xacml-*"):
    if module.is_dir():
        files.update(path for path in (module / "src").rglob("*") if path.is_file())
        if (module / "pom.xml").is_file():
            files.add(module / "pom.xml")
files.add(repo / "pom.xml")
previous = ev.parent / "003-xacml-xml-compile-jdk11"
files.update(path for path in previous.rglob("*") if path.is_file())
hashes = {str(path.relative_to(repo)): hashlib.sha256(path.read_bytes()).hexdigest() for path in sorted(files)}
for relative in allowed:
    (ev / ("74-before-" + Path(relative).name + ".txt")).write_bytes((repo / relative).read_bytes())
for name in ("summary.md", "surefire-summary.txt", "command.txt", "review-fixes.md", "conformance-blockers.md", "scope-check.txt"):
    (ev / ("74-before-round4-" + name)).write_bytes((ev / name).read_bytes())
(ev / "74-round4-before.json").write_text(json.dumps({"allowed_existing": allowed, "hashes": hashes}, indent=2) + "\n")
print(f"Snapshot: {len(hashes)} source/POM/resource/run003 files; two authorized files; prior six reports retained")
