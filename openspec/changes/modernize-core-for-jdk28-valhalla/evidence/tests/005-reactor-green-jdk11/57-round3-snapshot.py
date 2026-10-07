from pathlib import Path
import hashlib
import json

repo = Path.cwd()
ev = Path(__file__).parent
allowed = [
    "xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/HigherOrderFunctions.java",
    "xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/HigherOrderFunctionIdentifierTest.java",
    "xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/DurationFunctionIdentifierTest.java",
]
conditional = [
    "xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java",
    "xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionParamValueTypeSequenceSpec.java",
    "xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/JavaMethodToFunctionSpecConverter.java",
    "xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java",
]
files = set()
for module in repo.glob("xacml-*"):
    if module.is_dir():
        for source in (module / "src").rglob("*"):
            if source.is_file():
                files.add(source)
        if (module / "pom.xml").is_file():
            files.add(module / "pom.xml")
files.add(repo / "pom.xml")
previous = ev.parent / "003-xacml-xml-compile-jdk11"
files.update(path for path in previous.rglob("*") if path.is_file())
hashes = {str(path.relative_to(repo)): hashlib.sha256(path.read_bytes()).hexdigest()
          for path in sorted(files)}
for relative in allowed + conditional:
    path = repo / relative
    (ev / ("57-before-" + path.name + ".txt")).write_bytes(path.read_bytes())
for name in ("summary.md", "surefire-summary.txt", "command.txt", "conformance-blockers.md", "scope-check.txt"):
    (ev / ("57-before-round3-" + name)).write_bytes((ev / name).read_bytes())
(ev / "57-round3-before.json").write_text(json.dumps({
    "allowed_existing": allowed, "conditional_existing_not_yet_authorized_by_red": conditional,
    "hashes": hashes,
}, indent=2) + "\n")
print(f"Round 3 snapshot: {len(hashes)} source, POM, fixture and run 003 files; seven source copies; five previous reports retained")
