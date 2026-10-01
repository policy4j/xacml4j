# 001 donor preservation (task 1.1)

Date: 2026-10-01. Tool: git plumbing (no checkout, no change to HEAD, index, or
working tree of the donor checkout).

Result: PASS.

- Branch `wip/feature_xacml_20-donor` = `b9f114a3` (tree `f5b92e71`), parent
  `1e48fda5` (tree `b4d0e10d`), parent HEAD `1b1e67ac`.
  - `1e48fda5`: staged snapshot (tree written from the real index).
  - `b9f114a3`: unstaged and untracked work (temporary index built from the
    staged tree plus `git add -A`).
- Patch files and an untracked-file archive are stored outside the repository
  in `<checkouts>/xacml4j-worktrees/_donor-backup-20261001/`
  (`staged.patch`, `unstaged.patch`, `untracked.lst`, `untracked.tar.gz`,
  `state.txt`).

Verification:

| Check | Expected | Observed |
| --- | --- | --- |
| HEAD after | `1b1e67ac` | `1b1e67ac` |
| Real index tree after | `b4d0e10d` | `b4d0e10d` |
| `git status --porcelain` lines after | 386 | 386 |
| Tree of working tree (fresh temp index, `add -A`) | `f5b92e71` | `f5b92e71` |
| `git diff --binary HEAD donor~1` vs `staged.patch` | identical | identical (`cmp`) |

Ignored files (build output, local tool state) are not part of the donor by
design.
