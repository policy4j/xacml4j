from pathlib import Path
import json
import re
import sys

repo = Path.cwd()
ev = Path(__file__).parent
log = ev / sys.argv[1]
output = re.sub(r"\x1b\[[0-9;]*m", "", log.read_text().replace(str(repo), "<repo>"))
log.write_text(output)
errors = {}
lines = output.splitlines()
for index, line in enumerate(lines):
    match = re.match(r"^\[ERROR\] <repo>/(.+\.java):\[(\d+),(\d+)\] (.*)", line)
    if match:
        path, number, column, message = match.groups()
        key = (path, number, column)
        if key not in errors:
            errors[key] = {
                "file": path, "line": int(number), "column": int(column),
                "message": message,
                "context": "\n".join(lines[index:index + 6]),
            }
registry = ev / "compile-diagnostics.json"
data = json.loads(registry.read_text()) if registry.exists() else {}
data[log.name] = list(errors.values())
registry.write_text(json.dumps(data, indent=2) + "\n")
for error in list(errors.values())[:35]:
    print(f"{error['file']}:{error['line']}:{error['column']} -> {error['message']}")
if len(errors) > 35:
    print(f"Remaining {len(errors) - 35} locations are recorded in compile-diagnostics.json")
print(f"Compiler locations: {len(errors)}")
for line in lines:
    if ("Tests run:" in line and " - in " not in line) or "BUILD " in line:
        print(line)
if "BUILD FAILURE" in output:
    print("\n".join(lines[-18:]))
