"""Check final artifact links, redaction, source preservation, and scoped Git diff."""
import difflib
import hashlib
import json
import re
import subprocess
from pathlib import Path

RUN = Path(__file__).resolve().parent
REPO = RUN.parents[5]
audit = json.loads((RUN / "source-audit.json").read_text())
sources = audit["modified_since_packet_start"] + audit["added_since_packet_start"]
gate = json.loads((RUN / "11-after-full-gate.json").read_text())
assert all(hashlib.sha256((REPO / key).read_bytes()).hexdigest() == value
           for key, value in gate.items())

before = (RUN / "00-before-Xacml30TestUtilityTest.java").read_text().splitlines()
after = (REPO / "xacml-test/src/test/java/org/xacml4j/v20/Xacml30TestUtilityTest.java").read_text().splitlines()
removed_assertions = [line for tag, i, j, _, _ in difflib.SequenceMatcher(
    None, before, after, autojunk=False).get_opcodes() if tag in {"delete", "replace"}
    for line in before[i:j] if re.search(r"\bassert[A-Z]", line)]
assert not removed_assertions

local_links = []
unredacted = []
for path in RUN.iterdir():
    if not path.is_file():
        continue
    content = path.read_text()
    if str(Path.home()) in content or str(REPO) in content:
        unredacted.append(path.name)
    if path.suffix == ".md":
        for link in re.findall(r"\]\(([^)]+)\)", content):
            if link.startswith(("https://", "http://", "#")):
                continue
            target = link.split("#", 1)[0]
            assert (path.parent / target).is_file(), (path.name, link)
            local_links.append({"document": path.name, "target": link})
assert not unredacted, unredacted

commands = [
    ["rtk", "git", "status", "--short", "--", *sources, str(RUN.relative_to(REPO))],
    ["rtk", "git", "diff", "--check", "--", *sources],
]
receipts = []
for command in commands:
    result = subprocess.run(command, cwd=REPO, capture_output=True, text=True, timeout=30)
    assert result.returncode == 0, result.stderr
    receipts.append({"command": command, "exit_code": result.returncode,
                     "stdout": result.stdout, "stderr": result.stderr})
result = {
    "current_bytes_match_final_gate": True,
    "existing_3_0_comparator_test_assertions_removed": len(removed_assertions),
    "broken_local_links": [], "unredacted_checkout_or_home_paths": [],
    "local_links_checked": len(local_links), "git_receipts": receipts,
}
(RUN / "final-checks.json").write_text(json.dumps(result, indent=2) + "\n")
print(json.dumps({key: value for key, value in result.items() if key != "git_receipts"}, indent=2))
