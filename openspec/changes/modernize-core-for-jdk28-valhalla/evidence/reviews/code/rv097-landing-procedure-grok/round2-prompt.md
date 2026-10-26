Round 2 for lane R3 (same rules as round 1: read-only, no builds, no file writes; prefer file reads).

The conductor rewrote `openspec/changes/modernize-core-for-jdk28-valhalla/evidence/builds/087-single-branch-landing-gate/landing-procedure.md` after your review. Read the new file in full.

Your findings and what changed:
1. HIGH RFC822Name: step 7 now carries the edit by patch when it is uncommitted and stops if the patch applied nothing; step 8 restores the worktree copy only when the main checkout holds the edit.
2. HIGH tracker: step 2 copies `.beads/` (issues.jsonl, beads.db*, .br_history) to the backup before `br sync --flush-only`; step 1 stops if a `br` process runs. Empty lock files are not treated as a failure.
3. MEDIUM checks: every block runs in `( set -eu ... )` and `stop` exits the block; the three checks use `test -z` and `-eq 146`.
4. MEDIUM writers: step 1 stops if any tmux pane runs inside a checkout; the text says to kill the sessions first and to run from outside both checkouts.
Also from your residual risks: the tag step accepts an existing tag on OLD; step 1 checks `symbolic-ref HEAD`; step 9 says to refresh the tgrep index.

Try to break the new version: a block that still continues after a failure, a check that passes in the bad case, `set -e` traps (commands in `&&`/`||` lists, `$(...)`, `grep` exit 1), quoting, bash versus zsh differences, and anything that can lose work.

Finish with one block that starts with `FINAL REVIEW (R3 round 2, <model>)` and ends with the line `END FINAL REVIEW R3-2
