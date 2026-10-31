# rv094: plan review of the single-branch landing (task 10.1, D1)

Date: 2026-10-24. Five read-only NTM lanes in the integration worktree,
session `policy4j--landing-plan`, one packet each (`packets/`), reports in
`lanes/`. Each lane had the same verified facts, the draft plan, four common
questions, and its own focus.

| Lane | CLI and model | Focus | Verdict |
| --- | --- | --- | --- |
| L1 | Claude Code, Claude Opus 5.5, persona `policy4j_arch_review` | git mechanics and the landing sequence | APPROVE WITH CHANGES |
| L2 | Codex CLI 0.164.0, GPT-6.1-Sol, persona `policy4j_arch_review_codex_sol` | path inventory and port commands | APPROVE WITH CHANGES |
| L3 | Codex CLI 0.164.0, GPT-6-Astra, persona `policy4j_arch_review_codex_astra` | tracker and generated tooling | APPROVE WITH CHANGES |
| L4 | Agy, Gemini 4 Argon (High), role `policy4j_arch_review` | OpenSpec consistency | APPROVE WITH CHANGES |
| L5 | Grok Build 1.0.67, Grok 4.7 (xhigh), role `policy4j_arch_review` | adversarial | APPROVE WITH CHANGES |

The maintainer asked for Kimi in the adversarial lane. Kimi Code returned
`403` ("subscription does not have access") for `kimi-code/k3.1-pro` and
`kimi-code/kimi-for-coding` before the run (error text in `command.txt`;
CLI versions in `cli-versions.txt`), so the lane is recorded as failed and
Grok took it, as the plan allowed.

## Packet facts that were wrong

- Run 070: the packet had the two versions the wrong way round. Integration
  has "Recorded on the maintainer's instruction."; feature adds the Claude
  attribution line. Lanes L1, L2, L4, and L5 found it. Integration's file is
  kept.
- `wip/beads-desktop-research-snapshot` (`5bfa4eba`) has one parent,
  `dd1320e2`, not two (L1-L5).

## Answers

- Q1, land now: all five lanes yes. Two lines already drifted in 11 files;
  the product tree of the 2023 rewrite is safe on
  `wip/feature_xacml_20-donor` (L5: `git diff --quiet` is 0 for `pom.xml` and
  every module among `b9f114a3`, `a211db4c`, and the feature tip).
- Q2, replace or merge: all five lanes replace. A merge makes `1b1e67ac` an
  ancestor of the branch, so the merge base with the donor moves and the
  rewrite looks merged although phases 5-7 re-apply it by hand (L1: product
  files in `branch...donor` would drop from 522 to 368). A default merge also
  takes the 382 feature-only product paths without a conflict, including the
  core that does not compile (L5). `origin/feature_xacml_20` is an ancestor of
  integration, so the push is a fast-forward either way.

## Findings and their disposition

| Finding | Lanes | Check by the conductor | Disposition |
| --- | --- | --- | --- |
| 146 raw `.log` files (580 MiB) exist only in the worktree; the global ignore rule `*.log` hides them; `git worktree remove` deletes them | L1 | SUPPORTED: `git ls-files --others --ignored` in the worktree, 146 files, 580.4 MiB | copy them to the same paths in the main checkout before the worktree is removed |
| The main checkout has uncommitted tracker and research edits and 8 untracked evidence folders; moving `feature_xacml_20` must keep them | L1-L5 | SUPPORTED by a scratch-repository probe (`command.txt`): `git reset --keep` keeps a local edit when the target commit has the file unchanged, and aborts without a change when the target lacks it | port `.beads/` and `add-desktop-deep-research` byte-identical to `a0ec4683`; move the branch with `git reset --keep` |
| A `.beads/` in the worktree can start a second tracker | L1, L3, L5 | rule in `docs/ai/BEADS.md` | L3 and L5 proposed to leave `.beads/` out; rejected, because the probe shows that the branch move then aborts. Instead nobody runs `br` in the worktree, and the worktree goes at the landing |
| `git worktree remove` refuses dirty files; `.serena/project.yml` and `RFC822Name.java` are dirty | L1, L2, L3, L5 (both files); L4 (`RFC822Name.java`) | SUPPORTED: both checkouts hold the same `.serena/project.yml` blob (`14ce71e3`), so the worktree copy can be dropped; `RFC822Name.java` is a maintainer edit | maintainer decision; it must be committed or moved, not dropped |
| After the worktree goes, `ntm spawn policy4j` with the documented base would start agents in an unrelated repository of that name | L1, L3, L4 | SUPPORTED: the docs name the base; the other repository exists | port rewrites the NTM procedure of `docs/ai/SWARMS.md` and the generator text: session `xacml4j` in the main checkout |
| Home paths in tracked files block a push to the public remote | L4 | SUPPORTED: 203 tracked files on integration (199 Surefire files under `evidence/tests`, 4 under `evidence/reviews`), added by `dd1320e2` and `97d4fe37`; `.beads/issues.jsonl` on feature; 0 on `origin/feature_xacml_20` | the landing stays local; the push waits for a scrub and a rewrite of the unpushed commits (maintainer) |
| Model pins and profile regeneration are a separate change | L1, L2, L3, L5 | the CAO Agy value is a model selector as well as a label (L3, `gen-cao-profiles.py:98`) | pins are not changed in the landing |
| Tasks 5-7 do not name 327 of the 586 donor product paths; run 026 puts 237 of them under task 6.5 | L5 | not checked | follow-up before group 5; task 7.3 stays the backstop |
| `git restore` without `--overlay` can delete files in the target paths | L1 | the paths do not exist in the worktree | the port uses `--overlay` and checks that the paths are absent first |
| The tag and the branch move need a backup | L1 | - | tag `archive/feature_xacml_20-2026-10-24` and a `git bundle` before the move |

## Plan after the review

1. Port (one writer, Codex GPT-6.1-Sol): the 205 feature-only files
   byte-identical to `a0ec4683`; `.gitignore` gets `.doctor/`; the other 10
   shared files keep integration's content; D1, Migration Plan, Open
   Questions, tasks 10.1 and 7.3 record the landing; tasks 5.6 and 5.7 are
   added; the NTM procedure moves to the main checkout. No model pins.
2. Gate: full reactor and conformance on JDK 21, both strict validations, the
   desktop-research unit tests, `scripts/agent-doctor.sh`.
3. Review of the port and of the 5.6 code by families other than their
   authors.
4. Landing by the maintainer, local only; the push waits for the home-path
   scrub.

Limits: the lanes were read-only and ran no build. Grok waited about an hour
on permission prompts that its narrow pane did not show. Agy's renderer cut
its report in the pane; `lanes/L4-*.md` comes from Agy's conversation
store. L2, L3, and L5 marked runtime claims UNVERIFIED.
