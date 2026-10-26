# rv097: adversarial review of the landing procedure (Grok)

Date: 2026-10-24 and 2026-10-25. Lane R3: Grok Build 1.0.70, Grok 4.7
(xhigh), role `policy4j_code_review` sent in front of the packet, read-only,
in NTM session `policy4j--landing-review`. The maintainer asked for Kimi
K3.1 Pro here; its account returned 403 (rv094 `command.txt`), so Grok took
the lane. Author of the reviewed file: the conductor
(`builds/087-single-branch-landing-gate/landing-procedure.md`).

Final verdict: APPROVE (round 2).

| Round | Verdict | Findings | Result |
| --- | --- | --- | --- |
| 1 | REQUEST_CHANGES | HIGH: step 8 always restored `RFC822Name.java`, while step 7 carried it only in a comment, so the only copy could be lost. HIGH: `br sync` rewrote `.beads/issues.jsonl` before any backup, and the tracker database was in no backup. MEDIUM: three "must" checks exited 0 in the bad case. MEDIUM: writers were not stopped | all fixed: every block runs in `( set -eu ... )` and stops at the first failure; the tracker and its database are copied before the export; `RFC822Name.java` is carried by patch before the worktree copy is restored; step 1 stops when a tmux pane runs in a checkout or `br` runs; an existing tag is accepted only on OLD; the branch is checked with `symbolic-ref` |
| 2 | APPROVE | all four fixed, checked with probes of `set -e` in bash 3.2.57 and zsh 5.9 and an `rsync -n` run; LOW: the tmux check passes when no tmux server runs (the intended pass) | the procedure notes that commits and tags are signed (`commit.gpgsign` and `tag.gpgsign` are true) |

The Grok CLI asks before every tool call. The conductor approved read-only
calls only, one at a time; a script approved them automatically only when
every command was on a read-only list. One write the lane made: scratch
count files in the system temporary directory, which it deleted.

Limits: no branch was moved; the procedure was checked by reading and by
probes, not run.
