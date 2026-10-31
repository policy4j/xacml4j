from pathlib import Path
import hashlib
import json

repo = Path.cwd()
ev = Path(__file__).parent
allowed = ["xacml-pdp/src/test/java/org/xacml4j/v30/spi/repository/InMemoryPolicyRepositoryTest.java"]
assert not (ev / "84-round4-gate-before.json").exists()
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
    (ev / ("84-before-" + Path(relative).name + ".txt")).write_bytes((repo / relative).read_bytes())
for name in ("summary.md", "surefire-summary.txt", "command.txt", "review-fixes.md", "conformance-blockers.md", "scope-check.txt", "round4-gate-blocker-proposal.md"):
    (ev / ("84-before-round4-gate-" + name)).write_bytes((ev / name).read_bytes())
(ev / "84-round4-gate-before.json").write_text(json.dumps({"allowed_existing": allowed, "hashes": hashes}, indent=2) + "\n")
print(f"Snapshot: {len(hashes)} source/POM/resource/run003 files; only the repository test is authorized; seven prior reports retained")
