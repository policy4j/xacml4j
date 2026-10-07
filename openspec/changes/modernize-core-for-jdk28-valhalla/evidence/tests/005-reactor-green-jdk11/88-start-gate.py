from pathlib import Path
import hashlib
import json
import sys
import time

repo = Path.cwd()
ev = Path(__file__).parent
prefix = sys.argv[1]
assert prefix in ("88", "89")
path = ev / (prefix + "-gate-start.json")
assert not path.exists(), "Do not overwrite an earlier full gate receipt"
snapshot = json.loads((ev / "84-round4-gate-before.json").read_text())
for name, digest in snapshot["hashes"].items():
    if name not in snapshot["allowed_existing"]:
        assert hashlib.sha256((repo / name).read_bytes()).hexdigest() == digest, name
source = repo / snapshot["allowed_existing"][0]
receipt = {"start_ns": time.time_ns(), "source_sha256": hashlib.sha256(source.read_bytes()).hexdigest(), "protected_files_unchanged": len(snapshot["hashes"]) - 1}
assert receipt["source_sha256"] == json.loads((ev / "87-repository-after/summary.json").read_text())["source_sha256"]
assert json.loads((ev / "87-repository-after/summary.json").read_text())["passed"] == 30
path.write_text(json.dumps(receipt, indent=2) + "\n")
print(f"Starting gate {prefix}: all {receipt['protected_files_unchanged']} protected files unchanged; test matches the 30-run green source")
