Change: openspec/changes/modernize-core-for-jdk28-valhalla   Tasks: 10.1 (landing), 5.6   Beads: policy4j-the.10.1, policy4j-the.5.6
Role: Codex GPT-6-Astra, persona policy4j_code_review_codex_astra (lane R4)   Reply to: this pane
Goal: Review one part of the single-branch landing candidate in this worktree. Read-only.
Files you own: none. Files you must not touch: every file. Read-only commands only: file reads, `rtk rg`, `rtk git diff|log|show|ls-tree|ls-files|rev-parse|cat-file|merge-base|status`. No builds, no `br`, no `ntm`, no network, no file writes, no heredocs.
Read first: AGENTS.md; docs/ai/SWARMS.md; the change's design.md (D1, "Migration Plan") and tasks.md (5.6, 5.7, 7.3, 10.1).

## Context (verified by the conductor, 2026-10-24)

- This worktree is branch `integration/modernize-core-jdk28-valhalla` at `ad1fcec7`. Nothing is staged or committed. The maintainer ruled to land now: `feature_xacml_20` (at `a0ec4683`, checked out in the main checkout `<checkouts>/xacml4j`) moves to a commit of this worktree. No merge commit.
- Plan review: `openspec/changes/modernize-core-for-jdk28-valhalla/evidence/reviews/plan/rv094-single-branch-landing/` (summary, five lane reports).
- Port and gate: `.../evidence/builds/087-single-branch-landing-gate/` (summary, port lane report, `landing-procedure.md`, receipts).
- Port content: 205 untracked files restored byte-identical from `a0ec4683` (`.beads/`, `.agents/skills/desktop-deep-research/`, `openspec/changes/add-desktop-deep-research/`), and six tracked files: `.gitignore`, the change's `design.md` and `tasks.md`, `docs/ai/SWARMS.md`, `scripts/gen-cao-profiles.py`, `.agents/CAO_PROFILES.md` (regenerated). `rtk git diff -- <file>` shows each.
- Also uncommitted, not part of the port: 8 Java files of bead 5.6, `RFC822Name.java` (a separate maintainer edit), `.serena/project.yml` (the same edited file as in the main checkout).
- Gate (run 087): full reactor on JDK 21, 8,009 tests, 0 failures, 18 skipped; conformance 937 with 12 skipped; both strict validations pass.
- The main checkout has uncommitted edits to `.beads/issues.jsonl`, `.serena/project.yml`, and four `add-desktop-deep-research` docs, plus 8 untracked evidence folders of that change; another session owns them. A scratch-repository probe showed that `git reset --keep <target>` keeps a local edit when the target has the file unchanged from HEAD, and aborts without a change when the target lacks it.
- Known and accepted, do not report again: model pins are unchanged on purpose (bead policy4j-fq3t); home paths in 203 tracked evidence files and `.beads/issues.jsonl` block the push (bead policy4j-the.12); 3 red tests in the desktop skill are that change's planned red tests (its run 013); Serena fails on the `languages` key of the uncommitted `.serena/project.yml`.

## Report

Finish with one block that starts with `FINAL REVIEW (<lane>, <model>)` and ends with the line `END FINAL REVIEW <lane>`. Inside: verdict APPROVE or REQUEST_CHANGES; findings, severity first (HIGH, MEDIUM, LOW), each with file:line or a command and its output as evidence and a concrete fix; what you checked; residual risk. At most 700 words. Do not write files; print the block in this pane.

## Your lane (R4): the 5.6 code and RFC822Name.java (authors: the maintainer, and Claude for the follow-up edits)

- `rtk git diff -- xacml-core xacml-json xacml-xml`: 9 Java files. Task 5.6 says:
  compile-time API change only (generic bounds of `ValueType.ofAny` and
  `DynamicType.ofAny` become `<T extends Value<?>>`, `ValueType` becomes a
  `Function<Object, Value<?>>`, the `TypeCapability` family is self-typed). Is it
  behavior-neutral? Look for changed erasures or bridge methods that break binary
  callers, unchecked casts that now hide a wrong type, raw types left behind, and
  public API changes the task text does not mention.
- `ValueType.apply` has a known bug for a one-element `Object[]` (task 5.7, not part
  of 5.6): confirm that 5.6 neither fixes nor worsens it.
- `RFC822Name.java`: an IDE edit (pattern `instanceof` in `equals`, whitespace in
  the license block). Is `equals` still correct (symmetry, the `getEvaluatesTo()`
  comparison)? Does the indentation follow AGENTS.md (match the file: tabs)? Can the
  license plugin rewrite the header lines?
- The gate ran with these files: 8,009 tests, 0 failures. Name any test that would
  be needed to pin the 5.6 contract and is missing.
