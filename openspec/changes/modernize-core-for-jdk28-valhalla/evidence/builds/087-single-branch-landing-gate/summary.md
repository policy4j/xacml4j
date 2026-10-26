# 087: gate of the single-branch landing candidate (task 10.1)

Date: 2026-10-24. Tree: the integration worktree at `ad1fcec7` plus the
uncommitted landing port (lane P1) and the uncommitted maintainer work of
bead `policy4j-the.5.6` and `RFC822Name.java`. Nothing is staged or committed.

## Port (lane P1, Codex GPT-6.1-Sol, persona `policy4j_implementer_codex_sol`)

Packet decisions come from the plan review rv094. Lane report:
`port-lane-report.md`.

- 205 files restored from `a0ec4683` with `git restore --worktree --overlay`:
  `.beads/` (4), `.agents/skills/desktop-deep-research/` (5),
  `openspec/changes/add-desktop-deep-research/` (196). The conductor checked
  them again (`port-check.txt`): 205 expected, 0 mismatches, 0 extra files.
- Six tracked files changed: `.gitignore` (`.doctor/`), design.md (D1
  landing ruling, Migration Plan, Rollback, first open question resolved),
  tasks.md (10.1 text, 7.3 donor-ref check, new tasks 5.6 and 5.7),
  `docs/ai/SWARMS.md` (NTM sessions run in the main checkout as `xacml4j`;
  donor references), `scripts/gen-cao-profiles.py` (the same NTM text), and
  `.agents/CAO_PROFILES.md` (regenerated; the only generated file that
  changed).
- Not changed: model pins (bead `policy4j-fq3t`), product files, and the
  shared files where integration is newer, run 070 included.

## Gate

| Check | Result |
| --- | --- |
| Full reactor `clean test`, Oracle GraalVM 21.0.6+8.1 (build 21.0.6+8-LTS-jvmci-23.1-b55), Maven 3.9.16 offline (`toolchain.txt`) | PASS: 8,009 tests, 0 failures, 0 errors, 18 skipped (6 modules with tests), unchanged from run 086; BUILD SUCCESS in 37 s (`surefire-summary.txt`) |
| Conformance | PASS: 937 tests, 0 failures, 12 skipped, unchanged |
| Build left the tree unchanged | PASS: `git status --porcelain` identical before and after |
| `openspec validate modernize-core-for-jdk28-valhalla --strict` | PASS (`openspec-validate.txt`) |
| `openspec validate add-desktop-deep-research --strict` | PASS (`openspec-validate.txt`) |
| desktop-research unit tests (`python3 -m unittest discover -s .agents/skills/desktop-deep-research/tests`) | 34 methods, 31 pass; the 3 that fail are the named red tests of that change's run `tests/013-highest-model-red` (task 2.3 not implemented). Expected; not a landing regression |
| `scripts/agent-doctor.sh` | ast-grep PASS, ripwire PASS (7/7), tgrep PASS with WARN (snapshot index older than 310 files); Serena FAIL, see below (`agent-doctor.txt`) |

Serena does not start in this worktree or in the main checkout. Its log
ends in `KeyError: 'languages'` while it loads `.serena/project.yml`. That
file has an uncommitted edit dated 2026-10-20, identical in both checkouts,
that replaces the key `languages` with `language_servers`; the installed
Serena 1.5.3 still reads `languages`. The landing does not touch the file
(it stays uncommitted, see `landing-procedure.md`). Fixing it is the
maintainer's choice: restore the committed file, or upgrade Serena.

## Changes after the review (rv095-rv098)

Made by the conductor; the gate, both validations, and the port check ran
again afterwards with the same results (the receipts in this run are from
that last run).

- `TypeToString.forType`: the method reference `Types.capabilities::get`
  is back to the lambda of the committed code. The method reference loaded
  the capability registry even for a null type (rv098, MEDIUM).
- design.md: new D13 for the 5.6 generic bounds; Migration Plan steps in
  time order. tasks.md: gate 5.5 names 5.1-5.7; 5.6 names the `Value<?>`
  signatures; the duplicated clause of 7.3 is gone (rv096).
- docs/ai/SWARMS.md: a check of `NTM_PROJECTS_BASE`, a reminder never to
  name a session `policy4j`, a fish form of `ntm save`, no "after the
  landing" wording; the generator text matches and `.agents/CAO_PROFILES.md`
  was regenerated (only its NTM text changed). docs/ai/TESTING.md: the line
  of work is `feature_xacml_20` (rv095).
- `landing-procedure.md`: every block stops at the first failure; the
  tracker and its database are copied before `br sync`; `RFC822Name.java`
  is carried to the main checkout before the worktree copy is restored;
  sessions and `br` must be stopped (rv097).
- Evidence: home and checkout paths replaced by `<checkouts>` and `<home>`;
  raw receipts added (rv096).

## Landing

`landing-procedure.md` is the maintainer's procedure (local only). The push
waits for bead `policy4j-the.12` (home paths in 203 tracked evidence files of
integration and in `.beads/issues.jsonl`).

Limits: the gate is `clean test`; the jars were not built. JMH is not
applicable (no product change in the port; 5.6 changes generic signatures
only). The 5.6 code has its own review lane in this landing's review.
