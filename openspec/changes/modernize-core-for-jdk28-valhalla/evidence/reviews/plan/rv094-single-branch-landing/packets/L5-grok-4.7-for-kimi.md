Change: openspec/changes/modernize-core-for-jdk28-valhalla   Tasks: 10.1 (landing), design D1   Beads: none yet
Role: Grok 4.7, adversarial lane in place of Kimi (lane L5)   Reply to: this pane
Goal: Review and improve the draft plan below that makes the integration branch the single line of work. Do not carry it out.
Files you own: none. This lane is read-only. Files you must not touch: every file.
Read first: AGENTS.md, docs/ai/SWARMS.md, docs/ai/BEADS.md, docs/ai/OPENSPEC.md, and in the change: design.md (D1, "Migration Plan", "Open Questions"), tasks.md (groups 5, 7, 10).

## Rules for this lane

- Read-only. Use only file reads and read-only commands: `rtk git log|show|diff|ls-tree|merge-base|rev-list|cat-file`, `rtk rg`. No builds, no `br` writes, no `ntm`, no network.
- This pane runs in the worktree `<checkouts>/xacml4j-worktrees/policy4j` (branch `integration/modernize-core-jdk28-valhalla`). Both branches are visible from here as git refs: use `git show feature_xacml_20:<path>` and `git diff <a> <b> -- <path>`.
- The main checkout `<checkouts>/xacml4j` (branch `feature_xacml_20`) has uncommitted work owned by another session. Do not touch it.
- Verify a fact before you build on it. Mark anything you did not verify UNVERIFIED.
- Some facts below are summaries. If a command shows a fact is wrong, say so first: that is the most valuable finding.

## Verified facts (2026-10-24)

Branches and commits:

| Ref | Commit | Note |
| --- | --- | --- |
| `feature_xacml_20` | `a0ec4683` | 15 commits ahead of `origin/feature_xacml_20` (`d78d9586`), 0 behind; 10 commits not on integration |
| `integration/modernize-core-jdk28-valhalla` | `ad1fcec7` | 23 commits not on feature; phases 1-4 done |
| merge base | `a8a90d46` | `origin/feature_xacml_20` is an ancestor of integration |
| `wip/feature_xacml_20-donor` | `b9f114a3` | `a8a90d46` + `1b1e67ac` + two WIP commits (staged, then unstaged and untracked work of 2026-10-01) |
| `wip/beads-desktop-research-snapshot` | `5bfa4eba` | not an ancestor of feature; parents `dd1320e2`, `befdd116` |
| `master` | `02f21e45` | equals `origin/master` |

Feature-only commits (newest first): `a0ec4683` desktop deep research plan; `dcbf57ce` code-intelligence guidance; `f85ee561`, `94f64243`, `e3c20899`, `947746e9`, `3e30f1c9` tracker updates; `d7d4fb66` agent tooling, tracker, OpenSpec changes; `a211db4c` and `1b1e67ac` "Refactorings" (the 2023 core rewrite, the "donor").

- Since `a8a90d46`, only `1b1e67ac` and `a211db4c` change product paths (`xacml-*`, `pom.xml`) on feature (`git log a8a90d46..feature_xacml_20 -- 'xacml-*' pom.xml`).
- `feature_xacml_20` does not compile: syntax errors in 6 `xacml-core` files (`EntitySchema.java`, `AttributesSchema.java`, `UInt64.java`, `XPathFunctions.java`, `PolicySet.java`, `Int32.java`), all last changed by `a211db4c`. design.md says the old HEAD does not compile. Task 7.1 already names the `AttributesSchema.java:101` construct and the `EntitySchema` recursion.
- Integration is green on JDK 21 (Maven 3.9, offline): 8,009 tests, 0 failures, 18 skipped; conformance 937 with 12 skipped (evidence run 086). The test count was reproduced on 2026-10-24 with the 5.6 work below in the tree, before the `RFC822Name` edit.

Path classification of every path changed since `a8a90d46` on either side (`git diff --no-renames --name-status a8a90d46 <ref>`, blobs compared with `git rev-parse <ref>:<path>`). "product" = `xacml-*` and `pom.xml`; "generated" = `.agents/cao-profiles/`, `.ntm/`:

| Side | Blob | Area | Files |
| --- | --- | --- | --- |
| both | same | tooling (`.agent`, `.agents`, `.claude`, `.codex`, `.grok`, `.kimi`, `.serena`, `docs`, `scripts`, `tools`, generated, 3 root files, 15 openspec docs) | 175 |
| both | same | openspec evidence | 1,954 |
| both | same | product | 1 |
| both | differs | product | 203 |
| both | differs | non-product | 11 (listed below) |
| feature-only | - | product | 382 |
| feature-only | - | non-product | 205 (listed below) |
| integration-only | - | product | 96 |
| integration-only | - | openspec (incl. evidence) | 175 |

Non-product, feature-only (205 files, all status A on feature):

- `openspec/changes/add-desktop-deep-research/**`: 8 docs (`.openspec.yaml`, proposal, design, tasks, research/README.md, 3 specs) and 188 evidence files.
- `.agents/skills/desktop-deep-research/**`: 5 files (`.gitignore`, `scripts/DesktopResearchBridge.swift`, `scripts/desktop_research.py`, `scripts/native_bridge.py`, `tests/test_execution.py`).
- `.beads/`: `.gitignore`, `config.yaml`, `issues.jsonl`, `metadata.json`. Integration has no `.beads/` at all.

Non-product, changed on both sides with different content (11 files; `git diff --numstat integration feature`, insertions/deletions as feature relative to integration):

| File | +/- | Content |
| --- | --- | --- |
| `.gitignore` | 3/2 | integration: comment "Agent tooling (local state). `.serena/.gitignore` keeps only project.yml." then `/.tgrep/`, `/outputs/`. feature: comment "Local agent tooling state (tgrep trigram index)", `/.tgrep/`, then "# br doctor per-run artifacts", `.doctor/` |
| `AGENTS.md` | 10/14 | feature describes JUnit 4 + EasyMock and `--enable-preview`; integration describes JUnit 5 + Mockito and `<release>21</release>` (task 1.6, 2.7) |
| `docs/ai/TESTING.md` | 24/48 | same split: feature is pre-migration, integration post-migration |
| modernize change `design.md` | 0/36 | feature lacks 36 lines that integration has |
| modernize change `proposal.md` | 0/3 | feature lacks the `request-limits` capability line |
| modernize change `tasks.md` | 1/2 | feature has 4.7 unchecked and no task 6.8 |
| modernize change `specs/attribute-resolution/spec.md` | 1/87 | feature has one older wording |
| modernize change `specs/conformance-coverage/spec.md` | 0/21 | |
| modernize change `specs/function-semantics/spec.md` | 0/250 | |
| modernize change `evidence/README.md` | 5/37 | feature lacks runs 071-086 and their reviews |
| modernize change `evidence/builds/070-maintainer-rulings/summary.md` | 1/1 | integration: "Recorded by Claude Code (claude-opus-5-5) on the maintainer's instruction."; feature: "Recorded on the maintainer's instruction." The maintainer's standing rule: no Claude author attribution in files. |

Uncommitted work (must survive; nobody commits except the maintainer):

- Worktree (integration): 8 Java files of bead `policy4j-the.5.6` (generic bounds of `ValueType.ofAny` and the `TypeCapability` family: `TypeCapability`, `TypeCapabilityFactory`, `types/DynamicType`, `types/TypeFactory`, `types/TypeToString`, `types/ValueType`, `TypeToGSon`, `TypeToXacml30`), not yet covered by an OpenSpec task; `types/RFC822Name.java` (a later IDE edit: `instanceof` pattern in `equals`, whitespace; not part of 5.6, not yet tested); `.serena/project.yml`.
- Main checkout (feature): `.beads/issues.jsonl`, `.serena/project.yml`, 4 docs of `add-desktop-deep-research` (design, proposal, tasks, evidence/README.md), and 8 untracked evidence folders (`builds/022`-`027`, `final/021`, `final/028`).

Tooling drift (both sides identical, so the port does not fix it):

- `scripts/gen-cao-profiles.py` pins Kimi `kimi-code/k3` (lines 102, 487, 785) and names `gemini-3.1-pro-high` for Agy under NTM (lines 96, 808); the CAO Agy variant is labelled "Gemini 3.1 Pro (High)" (line 98). The same text is in `.agents/CAO_PROFILES.md` and `docs/ai/SWARMS.md:180,207`.
- `agy models` today lists `gemini-4-argon-{low,medium,high}`, `gemini-3.8-flash-{low,medium,high}`, Claude Opus/Sonnet 5.5, and no Gemini 3.1 Pro.
- The newest Kimi Code model is `kimi-code/k3.1-pro` (released 2026-10-23). The Kimi account returns 403 for every model today.

## Draft plan under review

0. Maintainer decisions: land now instead of after phase 9 (changes D1 and task 10.1); keep the donor branch.
1. Inventory (read-only): classify every feature-only and both-changed path. (The facts above are the result of a first pass.)
2. Port (one writer, in the worktree, nothing staged or committed): bring the 205 feature-only non-product files over with `git restore --source=feature_xacml_20 --worktree -- <paths>` (writes the working tree, not the index); keep integration's version of the 11 both-changed files except where feature adds something (the `.doctor/` ignore line, the 070 attribution line); update the stale model pins in `scripts/gen-cao-profiles.py` and regenerate with `rtk python3 scripts/gen-cao-profiles.py`; edit D1, the migration plan, and task 10.1 to record the landing. Gate: full reactor tests on JDK 21 unchanged (8,009 / 0 / 18), conformance 937 / 12 skipped, `openspec validate --strict` for both changes, `scripts/agent-doctor.sh`.
3. Review of the port by other model families than the writer; reports stored as evidence.
4. Landing (maintainer only): commit the port and the 5.6 work separately in the worktree; tag the old `feature_xacml_20` tip; move `feature_xacml_20` to the new integration commit and push (no force needed, `origin/feature_xacml_20` is an ancestor); remove the `policy4j` worktree; keep `wip/feature_xacml_20-donor` until task 7.3; decide `wip/beads-desktop-research-snapshot`.
5. Continue phases 5-10 on the single branch.

## Questions for every lane

Q1. Land now with the replace strategy, or keep the two lines until task 10.1 as D1 planned? What does landing early cost or risk?
Q2. Replace (move the branch, keep the old tip as a tag) or a merge commit (merge `feature_xacml_20` into integration, resolved to integration's product code and the ported files)? Consider history, blame, future donor re-application in phases 5-7, and the push.
Q3. What in this plan can lose work or break the build, the tracker, or the agent tooling? Name the step and the fix.
Q4. Which steps can be removed, merged, or reordered to make the plan shorter without losing a check?

## Your lane (L5): adversarial

Kimi K3.1 Pro was requested for this lane. Its account returns 403, so you
take the lane.

- Try to break the plan. Look for lost work: content only on feature that the
  plan drops, commits whose only remaining copy would be the tag, and donor
  content that is not on `wip/feature_xacml_20-donor` (compare product trees:
  `git diff --stat b9f114a3 a211db4c -- 'xacml-*' pom.xml`).
- Test the claim that phases 5-7 cover the donor's product changes: list donor
  packages or areas that no task in tasks.md groups 5-7 names.
- Look for coupling: does anything on integration (build, tests, scripts, CI)
  read a path that only feature has, or the reverse?
- Give Q2 your strongest argument for each side before you choose.

## Report

Finish with one block that starts with `FINAL REPORT (Grok 4.7, adversarial lane in place of Kimi)` and ends with the
line `END FINAL REPORT L5`. Inside: a verdict on the plan (APPROVE AS IS,
APPROVE WITH CHANGES, or REJECT); answers to Q1-Q4; findings, severity first,
each with its evidence (command and output, or file:line); the plan changes you
recommend as a numbered list; open questions for the maintainer. At most 900
words. Do not write files; print the block in this pane.
