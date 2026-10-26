FINAL REPORT (Agy, Gemini 4 Argon (High), role policy4j_arch_review)

**Verdict**: `APPROVE WITH CHANGES`

### Findings (Severity First)

1. **[HIGH] Prompt fact & Step 2 invert `070-maintainer-rulings/summary.md` attribution (`SUPPORTED`)**
   - **Evidence**: `rtk git diff HEAD feature_xacml_20 -- openspec/changes/modernize-core-for-jdk28-valhalla/evidence/builds/070-maintainer-rulings/summary.md` outputs `-Recorded on the maintainer's instruction.` / `+Recorded by Claude Code (claude-opus-5-5) on the maintainer's instruction.` ([summary.md](openspec/changes/modernize-core-for-jdk28-valhalla/evidence/builds/070-maintainer-rulings/summary.md#L45), fixed on integration in `8bfb15ff`).
   - **Impact**: Step 2 ("Take feature's version of the 070 attribution line") restores forbidden Claude attribution.
   - **Fix**: Keep integration's `070-maintainer-rulings/summary.md` untouched.

2. **[HIGH] Step 2 (`git restore --source=feature_xacml_20`) & Step 4 drop/conflict with uncommitted main-checkout state (`SUPPORTED`)**
   - **Evidence**: `rtk git cat-file -p feature_xacml_20:.beads/issues.jsonl` has tasks `policy4j-the.5.1`–`5.5` only (no `5.6`) and lacks the untracked `.beads/*.db` SQLite store ([BEADS.md](docs/ai/BEADS.md#L5-L6)). `feature_xacml_20:openspec/changes/add-desktop-deep-research/evidence/README.md` stops at run `020`, missing the main checkout's dirty edits and untracked runs `021`–`028`.
   - **Impact**: Restoring from `a0ec4683` ports stale `.beads` and `add-desktop-deep-research` files; resetting `feature_xacml_20` in Step 4 while checked out in the dirty main checkout fails or clobbers live work.
   - **Fix**: First `br sync --flush-only` and commit/stash dirty work in the main checkout (or copy live `.beads/` and `add-desktop-deep-research` runs `021`–`028` before moving the branch).

3. **[MEDIUM] Unscrubbed local home paths before `git push` in Step 4 (`SUPPORTED`)**
   - **Evidence**: `rtk git show -s d7d4fb66` warns: *"Evidence files and issues.jsonl contain a local home path; scrub them before this branch goes to a public remote."* Confirmed in `feature_xacml_20:.beads/issues.jsonl` (`"source_repo_path":"<checkouts>/xacml4j"`), violating [OPENSPEC.md](docs/ai/OPENSPEC.md#L123).
   - **Fix**: Scrub `<home>/...` paths in `.beads/issues.jsonl` and `add-desktop-deep-research/evidence/**` before pushing.

4. **[MEDIUM] Step 4 `git worktree remove` breaks NTM `policy4j` path assumption (`SUPPORTED`)**
   - **Evidence**: [SWARMS.md](docs/ai/SWARMS.md#L147-L161) and [evidence/README.md](openspec/changes/modernize-core-for-jdk28-valhalla/evidence/README.md#L8-L9) require `<checkouts>/xacml4j-worktrees/policy4j` because NTM maps session `policy4j` to `<NTM_PROJECTS_BASE>/policy4j` and an unrelated `policy4j` repo exists next to the main checkout.
   - **Fix**: Either keep the worktree, symlink it to the main checkout, or update `NTM_PROJECTS_BASE`/`SWARMS.md`.

5. **[MEDIUM] Unaccounted `RFC822Name.java` edit in worktree blocks `git worktree remove` (`SUPPORTED`)**
   - **Evidence**: `rtk git diff -- xacml-core/src/main/java/org/xacml4j/v30/types/RFC822Name.java` shows an untested `instanceof` pattern edit at [RFC822Name.java](xacml-core/src/main/java/org/xacml4j/v30/types/RFC822Name.java#L280-L283) using spaces in a tab-indented file ([AGENTS.md](AGENTS.md#L85)).
   - **Fix**: Revert `RFC822Name.java` or fix its tab indentation and fold it into a tested commit before Step 3.

6. **[LOW] Prompt fact on `5bfa4eba` parents & `add-desktop-deep-research` line citation (`SUPPORTED`)**
   - **Evidence**: `rtk git cat-file -p 5bfa4eba` shows one parent (`dd1320e2`), not two (`befdd116` is `dd1320e2~1`). `feature_xacml_20:openspec/changes/add-desktop-deep-research/design.md:4` cites `AGENTS.md:128` (now [AGENTS.md](AGENTS.md#L133-L136) on integration).

### Answers to Q1–Q4

- **Q1 (Land now vs. at 10.1)**: Land now. Carrying two lines duplicates OpenSpec/tooling edits and strands `.beads/` off integration. Cost/risk is main-checkout dirty-state collision, mitigated by syncing the main checkout first and keeping donor refs (`archive/feature_xacml_20-pre-integration` + `wip/feature_xacml_20-donor`).
- **Q2 (Replace vs. merge commit)**: **Replace** (move `feature_xacml_20` to integration + tag `archive/feature_xacml_20-pre-integration` at `a0ec4683`). A merge commit puts broken donor product commits (`1b1e67ac`, `a211db4c`) into ancestry, polluting `git log`/`git blame` and confusing slice diffs in Phases 5–7. `origin/feature_xacml_20` (`d78d9586`) is already an ancestor of integration, so `git push origin feature_xacml_20` is a fast-forward.
- **Q3 (What can lose work/break)**: Step 2 (`070` attribution regression; stale `.beads` / `add-desktop-deep-research` from `a0ec4683`), Step 4 (dirty main checkout collision, unscrubbed home paths on push, NTM worktree removal, dirty `RFC822Name.java`). Fixes in Findings 1–5.
- **Q4 (Shorten/reorder)**: Sync and commit/stash dirty main-checkout work in Step 1; drop the `070` restore from Step 2; commit `5.6` and the port in the worktree *before* Step 3 so the gate tests exact committed state.

### L4 OpenSpec Consistency Deliverables

#### 1. Exact Text Changes (`design.md` & `tasks.md`)

**[design.md](openspec/changes/modernize-core-for-jdk28-valhalla/design.md#L61-L65) (`D1`)**
- Old:
  > Create an integration branch from `a8a90d46` and re-apply the refactor in reviewed slices, taking code from the current working tree (the donor) only after its tests and contract checks pass.
- New:
  > Create an integration line from `a8a90d46`, replace `feature_xacml_20` with it once Phases 1–4 are green (preserving `a0ec4683` as tag `archive/feature_xacml_20-pre-integration` and `wip/feature_xacml_20-donor`), and re-apply remaining slices on `feature_xacml_20` from the donor refs only after tests and contract checks pass.

**[design.md](openspec/changes/modernize-core-for-jdk28-valhalla/design.md#L261-L272) (`Migration Plan` step 5 & `Open Questions`)**
- Old:
  > 5. Merge the integration branch back to `feature_xacml_20` (or replace it) only after the maintainer confirms the donor has no remaining unique work.
  > ...
  > - Should the integration branch replace `feature_xacml_20`, or merge into it with `-s ours` after every donor hunk is accounted for?
- New:
  > 5. Replace `feature_xacml_20` with the integration line after Phase 4 (tagging `a0ec4683` as `archive/feature_xacml_20-pre-integration`); delete `wip/feature_xacml_20-donor` only at task 10.1 after confirming no unique donor work remains.
  > ...
  > - *(Resolved 2026-10-24)* `integration/modernize-core-jdk28-valhalla` replaced `feature_xacml_20` after Phase 4; `archive/feature_xacml_20-pre-integration` (`a0ec4683`) and `wip/feature_xacml_20-donor` (`b9f114a3`) remain read-only donor refs until task 10.1.

**[tasks.md](openspec/changes/modernize-core-for-jdk28-valhalla/tasks.md#L96) (`10.1`)**
- Old:
  > `- [ ] 10.1 Decide with the maintainer how the integration branch lands (replace or merge into feature_xacml_20) and confirm the donor branch has no unique work left`
- New:
  > `- [ ] 10.1 Confirm the donor refs (wip/feature_xacml_20-donor and archive/feature_xacml_20-pre-integration) have no unique work left after the post-Phase-4 replacement of feature_xacml_20`

#### 2. `openspec/changes/add-desktop-deep-research` Assessment
Belongs on the single branch as-is once runs `021`–`028` and the 4 dirty docs from the main checkout are included. Its `proposal.md`, `design.md`, `tasks.md`, and `specs/` have zero references to `feature_xacml_20` or Java/Maven; only `design.md:4` has a minor line-number drift (`AGENTS.md:128` → `133`).

#### 3. Landing Evidence Recording
- Record `5.6` test gate in `evidence/tests/087-type-capability-generics/` and the single-branch landing gate (`mvn test`, conformance, `openspec validate --strict` for both changes, `scripts/agent-doctor.sh`) in `evidence/builds/088-single-branch-landing/`.
- Record plan/landing reviews in `evidence/reviews/plan/rv094-single-branch-landing-<lane>/`.
- Add rows `5.6`, `10.1 (landing)`, `087`, `088`, and `rv094+` to `evidence/README.md`.

#### 4. Missing Group-5 Task for `5.6`
Add to [tasks.md](openspec/changes/modernize-core-for-jdk28-valhalla/tasks.md#L56) (matching bead `policy4j-the.5.6`):
- `- [ ] 5.6 Parameterize TypeCapability<T> and Value<?> generic bounds across ValueType, DynamicType, TypeCapabilityFactory, TypeFactory, TypeToString, TypeToGSon, and TypeToXacml30`

### Recommended Plan Changes
1. **Step 1**: Tag `archive/feature_xacml_20-pre-integration` (`a0ec4683`) and `archive/desktop-research-snapshot` (`5bfa4eba`); flush `.beads` (`br sync --flush-only`) and commit/stash dirty main-checkout work first.
2. **Step 2**: Port from the updated main-checkout ref; **do not** restore `070-maintainer-rulings/summary.md`; merge `.gitignore` (`.doctor/` + `/outputs/`); add task `5.6` and `D1`/`10.1` updates.
3. **Step 3**: Resolve `RFC822Name.java`, commit `5.6` and the port separately, scrub local `<home>/...` paths, then run the full gate (`087`/`088`).
4. **Step 4**: Switch main checkout to detached HEAD before `git branch -f feature_xacml_20 <new-tip>`, then `git checkout feature_xacml_20`; keep or symlink the `policy4j` worktree path for NTM.

### Open Questions for Maintainer
1. Should `RFC822Name.java` be included (with tab indentation fixed) in the `5.6` commit or reverted?
2. Should we scrub `<home>/...` paths from `.beads/issues.jsonl` and `add-desktop-deep-research/evidence/**` before pushing to `origin`?

END FINAL REPORT L4
