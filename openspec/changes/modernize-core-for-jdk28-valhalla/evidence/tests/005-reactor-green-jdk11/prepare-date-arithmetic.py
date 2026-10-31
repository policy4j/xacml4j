from pathlib import Path
import hashlib
import json

repo = Path.cwd()
ev = Path(__file__).parent
allowed = ["xacml-core/src/main/java/org/xacml4j/v30/types/" + name + ".java"
           for name in ("ISO8601Date", "ISO8601DateTime", "ISO8601Time",
                        "ISO8601Duration", "ISO8601DayTimeDuration", "ISO8601YearMonthDuration")]
allowed.append("xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DateTimeArithmeticFunctions.java")
old = json.loads((ev / "27-protected-before-baseline-restoration.json").read_text())
protected = {name: digest for name, digest in old.items() if name not in allowed}
changed = [name for name, digest in protected.items()
           if not (repo / name).is_file()
           or hashlib.sha256((repo / name).read_bytes()).hexdigest() != digest]
assert not changed, changed
before = {name: (repo / name).read_text() for name in allowed if (repo / name).is_file()}
(ev / "34-approved-arithmetic-source-before.json").write_text(json.dumps(before, indent=2) + "\n")
(ev / "34-protected-before-date-arithmetic.json").write_text(json.dumps(protected, indent=2) + "\n")
(ev / "31-summary-before-date-arithmetic.md").write_bytes((ev / "summary.md").read_bytes())
print(f"Protected snapshot: {len(protected)} files; existing changed files: {len(changed)}")
print(f"Approved arithmetic production sources captured: {len(before)}")
