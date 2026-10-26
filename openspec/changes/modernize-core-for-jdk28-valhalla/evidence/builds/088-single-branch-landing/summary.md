# 088: single-branch landing (task 10.1)

Date: 2026-10-26. Run by the conductor on the maintainer's instruction
("run it all, local only"), with the procedure of run 087
(`landing-procedure.md`, reviewed in rv097). The script that ran is
`landing-run.sh`; its output and the refs afterwards are in
`refs-after-landing.txt`.

Result: LANDED, local only. `feature_xacml_20` is the single line of work.

| Ref | Before | After |
| --- | --- | --- |
| `feature_xacml_20` | `a0ec4683` | `265be3a0` (signed) |
| `integration/modernize-core-jdk28-valhalla` | `ad1fcec7` | `265be3a0` |
| tag `archive/feature_xacml_20-2026-10-24` | - | `a0ec4683` (signed, annotated) |
| `wip/feature_xacml_20-donor` | `b9f114a3` | unchanged |
| `origin/feature_xacml_20` | `d78d9586` | unchanged; an ancestor of `265be3a0`, so a push is a fast-forward |

Commits: `3255af26` "Land the integration line as feature_xacml_20 (D1,
task 10.1)" (the port of run 087 and its evidence) and `265be3a0` "Tighten
the generic bounds of ValueType.ofAny and TypeCapability
(policy4j-the.5.6)" (8 files, reviewed in rv098).

Steps: 1 checks (no tmux pane or `br` process in a checkout; branch at
OLD); 2 bundle backup (verified), copies of the tracker and its database and
of the main checkout's dirty files, tracker export (nothing new); 3 two
signed commits; 4 candidate checks; 5 tag; 6 `git reset --keep` in the main
checkout (916 files updated; the set of dirty files unchanged); 7 the 146
ignored logs copied to the main checkout, uncommitted worktree edits carried
by patch; 8 worktree restored, then removed (`git worktree list` shows the
main checkout only).

Deviations from the reviewed procedure, each one stricter or asked for:

- Step 2 records the list of dirty files again after `br sync`, because the
  export can change that list; step 6 compares against the second list.
- Step 4 stopped: three files had new uncommitted IDE edits in the worktree
  (`AttributeIndex.java`, `BagOfValuesType.java`,
  `JsonEntityMarshallingTest.java`, edited 06:29-06:43), next to
  `RFC822Name.java`. The maintainer chose to carry them over. Steps 4, 7,
  and 8 then carried every modified product file (4 files) instead of
  `RFC822Name.java` alone; step 8 checks that the worktree edits did not
  change after the patch of step 7 was taken. The four files are
  uncommitted in the main checkout.

Gate after the landing, in the main checkout with the carried edits: full
reactor `clean test` on JDK 21 (Oracle GraalVM 21.0.6, Maven 3.9.16
offline): 8,009 tests, 0 failures, 0 errors, 18 skipped; conformance 937
with 12 skipped (`surefire-summary.txt`); the build changed no file; both
changes pass `openspec validate --strict`.

Not done, by decision: no push (bead `policy4j-the.12`, home paths in
tracked evidence). The backup bundle and copies stay outside the repository.
