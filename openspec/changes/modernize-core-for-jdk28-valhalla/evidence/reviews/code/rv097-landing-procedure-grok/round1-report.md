FINAL REVIEW (R3, Grok 4.7)

Verdict: REQUEST_CHANGES

Findings

1. HIGH — Step 8 deletes the only copy of `RFC822Name.java`. `landing-procedure.md:108-110` always runs `git restore` on that file. The carry in `:100-102` is a comment ("only if"), not a command. The worktree has the edit (`git status`: `M xacml-core/src/main/java/org/xacml4j/v30/types/RFC822Name.java`). The main checkout does not (`git -C "$M" status --porcelain` has no `RFC822Name`). `git restore` puts back HEAD, then `worktree remove` deletes the directory. Fix: one failing conditional — if `git diff --quiet -- RFC822Name.java` fails, write `"$B/rfc822name.patch"`, `git -C "$M" apply` it, confirm the apply, and only then restore. If you commit the file instead, delete the restore line.

2. HIGH — Step 1 rewrites the tracker file before anything copies the old bytes. `:22` runs `br sync --flush-only` in `$M`, which exports the SQLite DB over `.beads/issues.jsonl` (`docs/ai/BEADS.md`). Step 2 then rsyncs `git status --porcelain` only. That status lists `M .beads/issues.jsonl` and does not list the ignored DB. Ignored and present: `.beads/beads.db`, `beads.db-wal`, `beads.db-shm`, and `.br_history/issues.*.jsonl`. Empty lock files are also present (`.write.lock`, `.br-db-write-*.lock`, `.br-jsonl-write-*.lock`); the procedure does not abort on them. A flush that disagrees with the dirty file leaves `$B` holding the new file, and the DB is not in `$B` or the bundle. Fix: copy `issues.jsonl`, `beads.db*`, and `.br_history` into `$B` first; abort if any `.beads/*.lock` exists; flush only after that.

3. MEDIUM — Three "must" checks exit 0 when the bad case happens. `git diff --name-only -- 'xacml-*'` printed the nine dirty Java paths and exited 0, so `:45` `git diff --cached --name-only` does not stop a commit that stages product files. `:72` `git status --porcelain` exits 0 while extra dirt remains. `:99` `grep -c '\.log$'` exits 0 for any count other than zero, so a short copy still passes and `:111` `worktree remove` then deletes the worktree. `git diff --quiet` and `merge-base --is-ancestor` do fail the shell; these three do not. Fix: `test -z "$(git diff --cached --name-only -- 'xacml-*' pom.xml .serena)"`, `test -z "$(git status --porcelain | grep -v -e '.serena/project.yml' -e 'RFC822Name.java')"`, and `test "$(… | grep -c '\.log$')" -eq 146`.

4. MEDIUM — Writers are not stopped. `:18` runs `ntm list`. The kill instruction is a comment. A live session can change `$M` or `$W` after the backup and after the step 4 tree check. Fix: kill those sessions and abort if any client still has `$M` or `$W` as its cwd.

What I checked

- `$M` is on `feature_xacml_20` at `a0ec4683`. Dirty tracked paths are `issues.jsonl`, `.serena/project.yml`, and four `add-desktop-deep-research` docs, plus untracked runs 021–028. All of those tracked paths sit under the step 4 pathspec.
- `HEAD:.serena/project.yml` and `a0ec4683:.serena/project.yml` are both `c8cff824…`. `git diff --no-index` of the two worktree files was empty, so the Serena `restore` does not drop a unique edit, and `reset --keep` can keep `$M`'s edit.
- `origin/feature_xacml_20` is an ancestor of `ad1fcec7` (`merge-base --is-ancestor`, exit 0). `a0ec4683` is not (exit 1). `reset --keep` is the right move: `branch -f` cannot retarget a checked-out branch, and `--hard` would drop `$M`'s edits. The quiet diff is the right precondition; a conflicting edit outside that list makes `reset --keep` abort with no change.
- Port path names: 205 in `a0ec4683` and 205 untracked, no name only on one side. Ignored files under `openspec` in `$W`: 146, all `*.log`. `$M` has none, so the log rsync does not clobber `$M`. No `pre-commit` hook (only `pre-commit.sample`). Tag `archive/feature_xacml_20-2026-10-24` is absent.

Residual risk

Content equality of the 205 files is the port lane's hash check, not re-hashed here. Step 6 compares dirty names, not bytes. After a successful reset, `$M`'s `.tgrep` index still describes the old tree until reindex. If step 6 aborts, the step 5 tag already exists and a rerun dies on `git tag`; continue when the tag already points at `a0ec4683`. Step 1 does not assert `symbolic-ref HEAD` is `refs/heads/feature_xacml_20` (it is today).

END FINAL REVIEW R3
