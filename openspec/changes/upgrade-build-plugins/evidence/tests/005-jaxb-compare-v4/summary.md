# 005: JAXB comparison after review rv012 (finding N3)

Date: 2026-10-27. bash 3.2, BSD diff (macOS). No Maven run.

Finding N3 (Codex GPT-6 Astra, rv012): for two files with the same name and different binary
content, `diff` exits 1 and prints only `Binary files ... differ`, which the run 004 comparator
did not count. Change: every diff line that is not a changed-text row (`< `, `> `), a hunk header,
or `---` now counts as a difference; this also catches `\ No newline at end of file`.

| Check | Result |
| --- | --- |
| run 004 comparator on two trees whose `data.bin` differs after a NUL byte | exit 0, "other changed lines: 0" (the defect) |
| new comparator on the same trees | exit 1, "other lines differ in ./data.bin: 1" |
| `compare-generated-test.sh` (adds `binary-difference` and `missing-final-newline`) | 11 of 11 PASS |
| run 003 trees, 0.15.1 against 0.15.3 | 110 files in both, 0 other changed lines (exit 0) |

Raw output: `output.txt`.
