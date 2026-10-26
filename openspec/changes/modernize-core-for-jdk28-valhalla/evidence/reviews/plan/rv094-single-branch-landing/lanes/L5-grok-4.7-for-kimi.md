FINAL REPORT (Grok 4.7, adversarial lane in place of Kimi)

**Verdict: APPROVE WITH CHANGES.** Replace is the right landing. As written, the plan can drop the main checkout's uncommitted research, restore a forbidden attribution line, and fork beads. Product bytes are not the hole the prompt expected.

**Fact corrections**

1. Run 070 is backwards in the packet. `git diff -U1 integration/modernize-core-jdk28-valhalla feature_xacml_20 -- .../070-maintainer-rulings/summary.md` shows integration has `Recorded on the maintainer's instruction.` and feature has `Recorded by Claude Code (claude-opus-5-5)...`. Step 2 would put the Claude line back.

2. Donor product bytes match the feature tip. `git diff --quiet` exits 0 for `pom.xml` and every `xacml-*` module among `b9f114a3`, `a211db4c`, and `feature_xacml_20`. `a211db4c` is not an ancestor of the donor (`merge-base --is-ancestor` exits 1). The quoted pathspec `'xacml-*'` matches nothing, so an empty `--stat` from that command is not evidence. Zero paths exist only on the donor.

3. `wip/beads-desktop-research-snapshot` has one parent, `dd1320e2` (`rev-list --parents -1`), not two. Zero paths are missing from both tips. 293 blobs match neither tip, including `.beads/issues.jsonl` and the desktop-research docs. That is a stale 2026-10-07 tree, not a second product donor.

**Q1.** Land now with replace, after the loss fixes below. Waiting for task 10.1 does not protect code: the product tree is already on `wip/feature_xacml_20-donor`. Early landing voids D1's rule to land only after the donor has no unique work (`design.md` lines 260-263). `docs/ai/SWARMS.md:159-161` still calls `feature_xacml_20` the donor, so a later diff against that name shows an empty donor. Keeping two lines avoids the checkout hazard and leaves a non-compiling branch live.

**Q2.** Replace: `origin/feature_xacml_20` is an ancestor of integration, so the push fast-forwards, and the green history stays the first parent. A normal merge does not. Since `a8a90d46`, feature changed 586 product paths (131 A, 87 D, 368 M) and integration 300; 382 are feature-only. Git would take those 382 without a conflict, including the non-compiling core and deletes integration did not make. Slice map `evidence/builds/026-donor-slice-map/summary.md` lines 71-109 says integration must win on JUnit 5, the JDK 21 POM, and the conformance harness. The history argument for a merge is real: the 10 commits (`integration..feature`) stay reachable without a tag. The only safe form is `git merge -s ours` after the port commit. A default merge breaks the build. Choose replace, and push an annotated tag of `a0ec4683` in the same step. The tag keeps the commit object. The product tree is already on the donor.

**Q3.**

1. Step 4 moves `feature_xacml_20` while `git worktree list` shows it checked out at `<checkouts>/xacml4j` (`a0ec4683`). That checkout's tracked diff is `.beads/issues.jsonl`, `.serena/project.yml`, and four `add-desktop-deep-research` docs. Evidence folders `builds/022`-`027`, `final/021`, and `final/028` are on disk there and in neither tip (`git cat-file -e` exits 128 on three samples). `git branch -f` fails while the branch is checked out. Fix: copy those paths out first. Do not `reset --hard`.

2. The 205-file restore includes `.beads/`. Integration has no `.beads/issues.jsonl` (cat-file exits 128). `evidence/builds/002-integration-branch/summary.md:10-11` says `br` uses the main checkout's `.beads` and this worktree must not carry one. Fix: leave `.beads/` out.

3. "Where feature adds something" is unsafe on the other shared files. The AGENTS.md and TESTING.md additions are pre-migration JUnit 4 text. The attribute-resolution "addition" is the remnant of 87 integration lines (PIP failures, XPath errors, XML content equality). The tasks.md and evidence README additions are the old unchecked gate. Take only `.doctor/` in `.gitignore`. Keep `/outputs/`, which feature deletes, and keep integration's 070 line.

4. Step 4 commits the 5.6 work and deletes the worktree. `git diff --name-only` here also shows `RFC822Name.java` and a 156-line `.serena/project.yml` change. `git add -A` would commit both. Removal drops them. 5.6 has no OpenSpec task and overlaps task 5.3. Fix: do not commit 5.6 in this landing. Stash those two files before any clean.

5. The tag is not pushed, and those 10 commits are not on origin. Push the tag with the branch.

6. Profile regeneration rewrites `.agents/cao-profiles/`, `.ntm/personas.toml`, `.ntm/roles/`, and `.agents/CAO_PROFILES.md` (`scripts/gen-cao-profiles.py` lines 927-939). It does not edit `docs/ai/SWARMS.md:180` or `:207`. A wrong pin fails no JDK 21 test. The Kimi 403 is an account failure. Take regeneration out of this landing.

**Q4.** Drop the new inventory and the model regeneration. Keep one restore check: no `xacml-*`, `pom.xml`, or `.beads/` path. Keep the JDK 21 gate and `openspec validate --strict`. One review of that diff is enough. Delete the worktree only after the main checkout is on the new commit and the uncommitted files are saved.

**Phases 5-7 do not name the whole donor.** Of 586 donor product paths, 327 are outside `identifier`, `types`, `policy/function`, `policy/combine`, `annotations`, `model`, and `schemas`. Unnamed areas include `v30` root (60), the policy AST outside function and combine (58), PDP SPI (33), PDP runtime (15), core `marshal` (13), `xacml-xml` (22), `xacml-json` (17), `util`, `content`, conformance tests and resources, `xacml-test`, `xacml-proto`, `xacml-xml-jaxb` resources, and `pom.xml`. Evidence 026 maps 493 core/pdp/json/xml files onto the tasks and puts 237 into task 6.5, whose text is only Entity, Record, designators, and adapters (`tasks.md` line 65). Design line 52 already excludes making proto a reactor module. Vendored fixtures and POM preview hunks stay dropped (026 conflicts 2 and 8). Task 7.3 is the only backstop.

**Plan changes**

1. Rewrite D1 and task 10.1 in this port: the donor ref is `wip/feature_xacml_20-donor` plus the pushed tag. Update `SWARMS.md:159-161`. No new ADR. D1 already owns the decision.
2. Restore feature-only non-product paths except `.beads/`. From the 11 shared files, add only `.doctor/`.
3. Before the branch move, copy the main checkout's dirty docs, `issues.jsonl`, and evidence `022`-`027`, `021`, and `028` outside both checkouts.
4. Push tag `feature_xacml_20-pre-landing` at `a0ec4683` with the fast-forward.
5. Leave 5.6, `RFC822Name.java`, and `.serena/project.yml` uncommitted. Do not regenerate profiles.
6. Before group 5, paste the 026 file lists into tasks 6.5 and 7.x, and name proto, vendored fixtures, and POM preview hunks as out of scope.

**Open questions.** Which session may copy the main checkout's dirty files? Should 5.6 get a task before any commit? May the snapshot be deleted once those copies are saved?

**Verification.** Read-only `git rev-list`, `merge-base --is-ancestor`, `diff --quiet`, `diff --name-status`, `diff -U`, `cat-file -e`, `worktree list`, and `ls-tree`, plus reads of `design.md`, `tasks.md`, `SWARMS.md`, evidence 002 and 026, and `gen-cao-profiles.py`. No Maven, no `br`, no network. The 8,009-test figure and today's `agy models` and Kimi 403 claims are UNVERIFIED here.

**Residual risk.** I did not run `br where`, so a worktree `.beads` shadowing the main database is inferred from evidence 002. I did not inspect all 293 snapshot blobs. `cat-file` covered three of the eight evidence folders.

END FINAL REPORT L5
