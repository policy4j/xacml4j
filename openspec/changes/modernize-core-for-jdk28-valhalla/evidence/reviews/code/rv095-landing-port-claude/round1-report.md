FINAL REVIEW (R1, Claude Opus 5.5)

**Verdict: APPROVE.** The NTM instructions are correct for the layout after the landing. `.agents/CAO_PROFILES.md` is what the edited generator writes, and nothing outside the NTM text changed. There are four LOW findings. LOW-1 belongs in the landing commit or the first commit after it.

**Findings**

**LOW-1: stale branch line.** `docs/ai/TESTING.md:9-10` says the work proceeds "on the integration branch". After the landing, phases 5-10 continue on `feature_xacml_20` (`design.md:84`), and the integration branch stays at the landing commit.
- Fix: change it to "on `feature_xacml_20` (design D1)".
- If the fix goes into the landing, add `docs/ai/TESTING.md` to the step-3 `git add` list (`landing-procedure.md:40-44`). An uncommitted edit makes `git worktree remove` refuse in step 8.

**LOW-2: the fish form covers only the setup.** `docs/ai/SWARMS.md:209` uses `"${TMPDIR:-/tmp}"`, which fish rejects as a parse error. That fails safe, but fan-in breaks for a fish user, while `:159-163` now offers fish forms.
- Fix: add `# fish: ntm save xacml4j--<label> --all -o $TMPDIR/ntm-<label>`.
- I did not run this; the claim comes from fish syntax rules.

**LOW-3: the guard against the old name is text only.** With the documented base, `policy4j` maps to `<checkouts>/policy4j`, which is a separate repository (`rtk git -C <checkouts>/policy4j rev-parse --show-toplevel` returns that directory).
- The old name is easy to type. Persona and set names keep `policy4j_` (`CAO_PROFILES.md:86-116`), and recent sessions were `policy4j--*` (rv094 `summary.md:4`). With `--worktrees`, NTM would then make branches in that repository (`SWARMS.md:183`).
- A wrong base also gives no warning. the user NTM config (line 5) sets `projects_base` to `<home>/Projects`, and a failed POSIX substitution gives `.`.
- Fix: add a check after the setup that fails when the base is wrong:
  - POSIX: `test "$(rtk git -C "$NTM_PROJECTS_BASE/xacml4j" rev-parse --abbrev-ref HEAD)" = feature_xacml_20 && echo base-ok`
  - fish: `test (rtk git -C "$NTM_PROJECTS_BASE/xacml4j" rev-parse --abbrev-ref HEAD) = feature_xacml_20; and echo base-ok`
  - Repeat "never `policy4j`" as a comment in the spawn block (`:182-193`).

**LOW-4: dated text in the generator.** `scripts/gen-cao-profiles.py:781` ("After the landing, ...") and `SWARMS.md:148` ("run after the landing") stay in every regeneration.
- Fix: describe the setup as it is after the landing, keep the history in D1, and regenerate.

**What I checked**

- **Diff of the four files** (`rtk git diff`): `.gitignore` adds two lines, equal to `a0ec4683:.gitignore:63-64`. Neither tree tracks a `.doctor/` path. Every SWARMS hunk is inside "NTM procedure" (lines 145-232).
- **Setup forms:** quoting is correct in both shells, and `rtk git rev-parse --show-toplevel` prints only the path. With the base set to the parent of the main checkout, `xacml4j` and `xacml4j--<label>` both map to the main checkout. A labeled session kept its directory before: `policy4j--landing-plan` ran in the worktree.
- **Generator:** the README is an f-string. With `PREFIX = "policy4j"` (line 50), lines 781-790 render exactly as `CAO_PROFILES.md:118-127`, and the static text matches the rest of the file.
  - Counts agree: 59 personas, 6 sets, 66 profiles.
  - The last commit on these files, `4129a0ba`, changed the script and its outputs together.
  - `rtk git status` shows no other generated file changed.
  - I did not run the generator; the packet does not allow it.
- **Other tracked files** (`rtk rg --hidden`, with evidence and conformance fixtures excluded; the tracker and the ported change searched separately): no file presents the worktree or `policy4j--` sessions as current practice. The other hits are:
  - general worktree guidance: `AGENTS.md:210-219`, `.codex/config.toml:3`, `.grok/config.toml:3`
  - history from the time of the proposal: `proposal.md:3,21-22`
  - closed beads: `.beads/issues.jsonl:45,132`
- **User config:** CAO `extra_dirs` points to the profiles in the main checkout (`~/.aws/cli-agent-orchestrator/settings.json:27,34`), so removing the worktree does not break profile discovery. The NTM config does not mention the worktree.
- **Refs:** `feature_xacml_20` = `a0ec4683` and the donor branch = `b9f114a3`. The archive tag does not exist until step 5, as planned.

**Residual risk**

- UNVERIFIED, because I could not run `ntm`: what NTM does with a missing project directory, and where `--worktrees` creates its checkouts.
- My claim that the generated file is exact rests on comparing the text and on the port lane's run, not on a run of my own.
- Serena did not connect in this session. This lane needed only text tools.
- An empty search does not prove that a reference is absent.

END FINAL REVIEW R1
