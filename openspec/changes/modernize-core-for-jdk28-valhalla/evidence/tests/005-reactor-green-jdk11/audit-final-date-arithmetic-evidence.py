from pathlib import Path
import json

repo = Path.cwd()
ev = Path(__file__).parent
names = ["summary.md", "conformance-blockers.md", "command.txt", "surefire-summary.txt", "scope-check.txt",
         "34-date-arithmetic-before.txt", "35-date-arithmetic-after.txt", "36-reactor-verify-date-arithmetic.txt",
         "37-duration-bag-function-diagnostics.txt", "38-jdk-version.txt", "36-conformance-testcases.json"]
for name in names:
    assert str(repo) not in (ev / name).read_text(), "Checkout path not normalized: " + name
diagnostics = json.loads((ev / "compile-diagnostics.json").read_text())
for name in names[5:8]:
    assert not diagnostics[name], name
assert "Tests run: 65, Failures: 20, Errors: 0, Skipped: 0" in (ev / names[5]).read_text()
assert "Tests run: 65, Failures: 0, Errors: 0, Skipped: 0" in (ev / names[6]).read_text()
assert "BUILD FAILURE" in (ev / names[7]).read_text()
assert "IIC150 unchanged fixture import reproduced" in (ev / names[8]).read_text()
assert 'openjdk version "11.0.32.1"' in (ev / names[9]).read_text()
assert "required-duration-bag-alias-fix.patch" in (ev / "conformance-blockers.md").read_text()
summary = (ev / "summary.md").read_text()
assert "Result: **BLOCKED**" in summary and "| Total | 727 | 0 | 1 | 9 |" in summary
for name in names[5:8]:
    assert name in (ev / "command.txt").read_text() and name in (ev / "surefire-summary.txt").read_text()
assert (ev / "31-blockers-before-date-arithmetic.md").is_file()
print("Final evidence audit: PASS; red/green/gate receipts, compiler inventory, JDK, blocker and path normalization checked")
