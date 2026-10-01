# 002 integration branch (task 1.2)

Date: 2026-10-01. Result: PASS.

- Branch `integration/modernize-core-jdk28-valhalla` created at `a8a90d46`
  ("Fixed designator functions") as a git worktree at
  `<checkouts>/xacml4j-worktrees/policy4j`. The donor checkout is untouched.
- The first commit on the branch adds the agent tooling, the OpenSpec change,
  and the generated CAO profiles and NTM personas (no production code).
- Beads stays shared: `br` resolves the main checkout's `.beads` from any
  worktree (`br where`), so the worktree does not carry its own `.beads`.
- NTM sessions for this work are named `policy4j--<label>` with
  `NTM_PROJECTS_BASE` set to the worktrees directory (docs/ai/SWARMS.md).

Side fix: `br` 0.7.0 could not open the shared database ("busy (recovery in
progress)") after the upgrade from 0.5.12. With no `br` process running, the
old database files were moved to the backup directory and the database was
rebuilt from `issues.jsonl` (`br sync --import-only`: 68 created); doctor
reports healthy and `br ready` returns `policy4j-the.1.1`.
