FINAL REPORT (Claude Opus 5.5, persona policy4j_arch_review)

Verdict: APPROVE WITH CHANGES.

**Wrong facts**
- F1: The 070 attribution is the wrong way round. `git diff integration/modernize-core-jdk28-valhalla feature_xacml_20 -- …/070-maintainer-rulings/summary.md` prints `-Recorded on the maintainer's instruction.` (integration) and `+Recorded by Claude Code (claude-opus-5-5) …` (feature). Worktree line 45 agrees. If the port takes feature's line, it adds the attribution the maintainer forbids.
- F2: `5bfa4eba` has one parent, not two. `git cat-file -p` shows only dd1320e2, which is integration's second commit.
- All other facts reproduced: tips, base, counts, table rows, and main's dirty files (checked by blob hash).

**Q1.** Land now. Keeping two lines already doubles the work: dcbf57ce and ad1fcec7 have the same subject, and 11 files have drifted. Integration's 23 commits are on no remote. CI ignores pushes to feature (main.yml:8-11). Cost: origin then shows unfinished work.

**Q2.** Replace. A merge would make 1b1e67ac an ancestor of the branch. merge-base(branch, donor) would move from a8a90d46 to 1b1e67ac, and `git diff branch...donor` would drop from 522 to 368 product files. The 2023 rewrite would then look merged although the merge threw it away, which misleads task 7.3. Both strategies push as a fast-forward. Replace drops the history of the ported files; cite the tag in the commit message.

**Q3. Findings**
- H1: F1 (fix: plan change 1).
- H2: `git worktree remove` deletes ignored files without warning. 146 raw evidence logs (608.6 MB) exist only in the worktree. The global rule `~/.config/git/ignore:32` (`*.log`) ignores them, neither branch tracks them, and the evidence cites them (`tests/069-…/command.txt:15-16`; OPENSPEC.md:108) (fix: 6).
- H3: After removal, running SWARMS.md:154 from main gives the base that line 157 forbids. `ntm spawn policy4j` would then start agents in the unrelated `<checkouts>/policy4j` repository, which exists. The same text is in gen-cao-profiles.py:781-785 (fix: 3).
- M4: The plan does not say how to move the branch.
  - `git branch -f` refuses, because main has the branch checked out.
  - `git update-ref` leaves main's index on the old tree, so the next commit in main reverts the landing.
  - `git reset --keep` in main works. It keeps the untracked files, and keeps the 6 modified files if each one is identical in OLD and NEW. If not, it stops and changes nothing (see stage 4).
- M5: The plan does not cover two dirty files in the worktree:
  - `.serena/project.yml`: Serena rewrote it automatically; blob 14ce71e3, the same in both checkouts.
  - `RFC822Name.java`: license-header whitespace changed at lines 13 and 18.
  
  `git worktree remove` refuses while they are dirty, and `--force` discards them. If the Serena file is committed, main's branch move stops until main restores its copy (fix: 5).
- M6: After the port, a `br` call in the worktree starts a second database from the older JSONL. Worktree removal then deletes it (fix: 4).
- M7: The port reads `feature_xacml_20` by name. A commit on feature before the move gets lost (fix: 2, 4).
- L8: No backup exists before the move.
- L9: Stop panes, Serena, tgrep and the IDE before removal.
- L10: Keep `_donor-backup-20261001`, which holds the D1 patches.
- L11: `git restore` defaults to no-overlay mode, which can delete files; name the three directories exactly (fix: 2).
- Snapshot branch: no unique content. 136 of its 142 files equal feature's tip, and the other 6 blobs come from d7d4fb66. Delete it after the tag exists. The donor's product tree equals a211db4c.

**Q4.** Fold stage 1 into stage 2. Only `.gitignore` needs a hand-merge (add the `.doctor/` lines). The port changes no product file, so run the reactor gate once, on the 5.6 commit. Move the model-pin update to its own change. In stage 3, review only the non-product diff.

**Stage 4** (M = main checkout, W = worktree)
```
OLD=a0ec46838420052332f4e0495b10f54d437633f3
ntm kill policy4j--<label>; (cd $W && mcp.sh -m serena stop)   # all sessions in W and M; close W in the IDE
git -C $M rev-parse feature_xacml_20      # = $OLD
(cd $M && br sync --flush-only)
rsync -am --include='*/' --include='*.log' --exclude='*' $W/openspec/ <archive>/openspec/   # 146 files
# commit the port, then 5.6, in W
git -C $W status --porcelain              # empty
git -C $W diff --quiet $OLD HEAD -- .beads .serena/project.yml openspec/changes/add-desktop-deep-research   # exit 0, else restore them in M
NEW=$(git -C $W rev-parse HEAD)
git -C $M tag -a archive/feature_xacml_20-2026-10-24 $OLD -m "Before the D1 landing"
git -C $M bundle create <backup>/xacml4j.bundle --all; git -C $M bundle verify <backup>/xacml4j.bundle
git -C $M fetch origin
git -C $M merge-base --is-ancestor origin/feature_xacml_20 $NEW   # exit 0
git -C $M reset --keep $NEW
git -C $M status -sb                      # same M and ?? entries; ahead of origin
git -C $M push origin feature_xacml_20    # no force
git -C $M ls-remote origin refs/heads/feature_xacml_20   # = $NEW
git -C $W status --porcelain --ignored    # ignored, expendable files only
git -C $M worktree remove $W              # never --force
git -C $M worktree list                   # main only
git -C $M branch -D wip/beads-desktop-research-snapshot
```

**Plan changes**
1. Correct F1. Keep integration's version of every both-changed file except `.gitignore`.
2. Port with `git restore --source=a0ec4683 --worktree --overlay -- .beads .agents/skills/desktop-deep-research openspec/changes/add-desktop-deep-research`.
3. In the port, rewrite SWARMS.md:147-223 and gen-cao-profiles.py:781-785, then regenerate (this updates CAO_PROFILES.md:118-122). New rule: swarms run in the main checkout as session `xacml4j`, with base `<checkouts>`; never start a `policy4j` session there.
4. Between the port and the move, make no commits on feature and do not run `br` in the worktree.
5. Decide what to do with `RFC822Name.java` and `.serena/project.yml` before the gate.
6. Copy the ignored logs out before removal. Put the tag under `archive/`, not with the `xacml-*` release tags.
7. Move the donor clause of task 10.1 to task 7.3. Amend D1, Migration Plan step 5, and Open Question 1. A D1 amendment is enough; I found no ADR directory.
8. After landing, commit main's `.beads/` and the desktop work on the single line.

**Open questions**
1. Push the tag and the donor branch? That publishes the 2023 rewrite, which does not compile.
2. Where do the 608.6 MB of logs live long-term?
3. Is the session name `xacml4j` acceptable?
4. Should task 5.6 get its OpenSpec task before it lands?
5. Delete the integration branch after landing?

**Verification:** read-only `rtk git` and `rtk rg`, plus Python over their output.

**Residual risk:** The refusals and aborts above come from the git 2.50.1 documentation; I did not run them. UNVERIFIED: the remote state, the staged/unstaged split in main's index, and what NTM does when the base directory is missing. The untracked and ignored file lists come from rg, so they are PARTIAL.

END FINAL REPORT L1
