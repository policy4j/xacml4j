FINAL REPORT (Codex GPT-6.1-Sol, persona policy4j_arch_review_codex_sol)

**APPROVE WITH CHANGES.**

Packet corrections:

- **070 attribution is reversed:** integration is already attribution-free; feature adds Claude attribution. Both blobs confirm this at line 45.
- `5bfa4eba` has one parent, `dd1320e2` (`rtk git show -s --format=%P`).
- The identical product entry is a shared deletion: `xacml-json/src/test/resources/policy.json`.

Below, M means `openspec/changes/modernize-core-for-jdk28-valhalla`.

Findings, severity first:

- **HIGH — Steps 2/4 need an owner handoff.** Ref-based restoration cannot preserve uncommitted main-checkout work. The proposed commits also leave integration’s `RFC822Name.java` and `.serena/project.yml` unresolved. Require owner exports, reconciliation, and disposition of every residual, including ignored files, before branch movement or worktree removal. Evidence: `rtk git diff --raw HEAD` lists ten dirty files; `docs/ai/BEADS.md:58` requires tracker export. Main’s live contents remain **UNVERIFIED**.
- **MEDIUM — Keep integration’s 070 unchanged.** Taking feature’s line would reintroduce forbidden attribution (`M/evidence/builds/070-maintainer-rulings/summary.md:45`).
- **MEDIUM — Maven does not cover the imported Python implementation.** Its own `tasks.md:2` names unittest. Model regeneration also writes/deletes profiles without updating `SWARMS.md` (`scripts/gen-cao-profiles.py:920–955`); separate that repair.

Inventory independently reproduced using `rtk git diff --no-renames --name-status` for B→F, B→I, I→F: B=`a8a90d46`, F=`a0ec4683`, I=`ad1fcec7`.

| Classification | Count |
|---|---:|
| Both, identical: tooling/plans; evidence; product deletion | 175; 1,954; 1 |
| Both, different: product; non-product | 203; 11 |
| Feature-only: product; non-product | 382; 205 |
| Integration-only: product; OpenSpec/evidence | 96; 175 |

Union: **3,202 paths**. The 205 additions comprise `.beads` 4, desktop implementation 5, desktop change 196, including 188 evidence files. `ls-tree -r` confirms all are `100644`. Both `CLAUDE.md` entries are `120000`, targeting `AGENTS.md`. Mode/type summaries found no existing-file changes. `-M` detected 68 feature renames, including archived-source matches; none touches the port. Repository ignore rules exclude none of the 205; effective global/local rules remain **UNVERIFIED**.

All eleven content rules, supported by `rtk git diff I F -- <path>`:

| File | Rule |
|---|---|
| `.gitignore` | Combine: retain integration; append `.doctor/`; preserve `/outputs/`. |
| `AGENTS.md` | Integration: Jupiter, Mockito, release 21. |
| `docs/ai/TESTING.md` | Integration; update landing branch reference. |
| `M/design.md` | Integration, including D12; amend landing decisions. |
| `M/proposal.md` | Integration: preserve `request-limits`. |
| `M/tasks.md` | Integration: preserve 4.7/6.8; separate landing from donor exhaustion. |
| `M/specs/attribute-resolution/spec.md` | Integration. |
| `M/specs/conformance-coverage/spec.md` | Integration. |
| `M/specs/function-semantics/spec.md` | Integration. |
| `M/evidence/README.md` | Integration; append landing proof. |
| `M/evidence/builds/070-maintainer-rulings/summary.md` | Integration unchanged. |

**Q1:** Land early after preservation and approval. Phases 5–9 remain incomplete (`M/tasks.md:51–96`). Feature pushes receive no existing workflow gate (`.github/workflows/main.yml:8`).

**Q2:** Prefer replacement with durable old-tip and donor refs. A merge preserves ancestry/blame for imported files but records rejected donor changes as merged. Replacement keeps slice replay explicit. Remote fast-forward is supported: `merge-base origin/feature_xacml_20 I=d78d9586`; recheck before pushing.

**Q3:** Address the findings and verify untracked additions explicitly.

**Q4:** Separate model repair; use one final reactor run and its included conformance results.

Recommended order:

1. Freeze writers; obtain owner exports and landing approval; map 5.6 into OpenSpec. Keep donor-exhaustion verification at 7.3, covering both preservation refs.
2. Run this copy-only sequence in Bash from this worktree. **Proposed commands; not executed:**

```bash
set -euo pipefail
F=a0ec46838420052332f4e0495b10f54d437633f3
P=(.beads .agents/skills/desktop-deep-research openspec/changes/add-desktop-deep-research)
E=$(rtk proxy mktemp -d "${TMPDIR:-/tmp}/policy4j-port.XXXXXXXX")
test "$(rtk proxy git show -s --format=%H HEAD)" = ad1fcec740908b4b715db9cdeadab3398d294f9f
test "$(rtk proxy git show -s --format=%H feature_xacml_20)" = "$F"
for p in "${P[@]}"; do
  test ! -e "$p"
  test ! -L "$p"
done
snap() {
  rtk proxy git diff --no-ext-diff --no-textconv --binary HEAD -- 'xacml-*' pom.xml .serena/project.yml >"$E/$1.protected"
  rtk proxy git diff --no-ext-diff --no-textconv --cached --binary >"$E/$1.index"
  rtk proxy git ls-files --others --exclude-standard -- 'xacml-*' pom.xml >"$E/$1.extra"
  rtk proxy git hash-object --no-filters --stdin-paths <"$E/$1.extra" >"$E/$1.hashes"
}
snap before
rtk proxy git ls-tree -r "$F" -- "${P[@]}" >"$E/tree"
rtk proxy git ls-tree -r --name-only "$F" -- "${P[@]}" >"$E/names"
test "$(rtk proxy wc -l <"$E/names")" -eq 205
rtk git restore --source="$F" --worktree -- "${P[@]}"
rtk proxy git check-ignore --no-index --stdin <"$E/names" >"$E/ignored" || test "$?" -eq 1
test ! -s "$E/ignored"
rtk proxy git ls-files --others --exclude-standard -- "${P[@]}" >"$E/actual"
rtk proxy cmp "$E/names" "$E/actual"
while IFS=$'\t' read -r h p; do
  test -f "$p"
  test ! -L "$p"
  test ! -x "$p"
  test "$(rtk proxy git hash-object --no-filters -- "$p")" = "${h##* }"
done <"$E/tree"
snap after
for k in protected index extra hashes; do
  rtk proxy cmp "$E/before.$k" "$E/after.$k"
done
```

3. Apply approved table edits and audited owner-export deltas; repeat preservation comparisons. Defer model regeneration. Run:

```bash
rtk openspec validate modernize-core-for-jdk28-valhalla --strict
rtk openspec validate add-desktop-deep-research --strict
rtk python3 -m unittest discover -s .agents/skills/desktop-deep-research/tests -v
JAVA_HOME="$(jenv prefix 21)" rtk mvn -o -Dlicense.skipUpdateLicense=true clean test
scripts/agent-doctor.sh
```

4. Review, then maintainer-only commits/landing. Remove the worktree after verified handoff.

Open decisions: replacement approval; export owner; disposition of RFC822Name, local configuration, and preservation refs.

Verification: read-only source/ref/diff/tree checks; refs and dirty path inventory remained unchanged. No files changed by this lane. Builds, tests, fresh gates, model availability and live owner exports remain **UNVERIFIED**. Historical test receipts were not rerun.

END FINAL REPORT L2
