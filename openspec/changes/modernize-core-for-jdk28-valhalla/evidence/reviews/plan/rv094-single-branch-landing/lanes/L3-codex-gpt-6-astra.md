FINAL REPORT (Codex GPT-6-Astra, persona policy4j_arch_review_codex_astra)

**APPROVE WITH CHANGES.**

Packet correction: `5bfa4eba` has **one parent**, `dd1320e2`; `befdd116` is its grandparent. Verified by `rtk git rev-list --parents --max-count=3 wip/beads-desktop-research-snapshot`. This is a linear snapshot series, not a two-parent merge.

**Findings, severity first**

- **HIGH — Step 2 can establish a stale second tracker.** Feature’s committed metadata names `beads.db` and `issues.jsonl`; its config contains only `issue_prefix: policy4j`. The contract makes SQLite local and requires exporting it before session end ([BEADS.md:5](docs/ai/BEADS.md:5), lines 58, 73). Carry **none of `.beads/` in the initial port**. Keep the main checkout’s tracker authoritative, with one designated owner handling claims for worktree work. At cutover, reconcile its dirty JSONL with the live database, back up both, flush, and carry that verified fresh export. Do not initialize another database in the worktree.

- **HIGH — Step 4 removes a directory that the documented tooling still targets.** `SWARMS.md:147–161` routes `policy4j` sessions into this worktree and warns about an unrelated repository with that name elsewhere. Removing it requires revised session names/base paths and restarting clients from the retained checkout. Also, the current worktree still contains `RFC822Name.java` and `.serena/project.yml` changes beyond the proposed two commits (`rtk git diff --name-only HEAD`). Every remaining tracked, untracked, and ignored asset needs an explicit disposition before removal. A tag preserves committed history only.

- **MEDIUM — The pin update is broader than three substitutions.** At `scripts/gen-cao-profiles.py:98`, the old CAO Agy text is the executable **model selector as well as a label**. NTM personas separately use `Gemini 3.8 Flash (High)` at line 99; the Pro selector appears in `agyx` instructions. Regeneration rewrites every CAO profile, personas, role files, `.ntm/.gitignore`, and the generated README, and deletes stale generated-prefix files (lines 920–954). It also changes descriptions through `v.label` (line 639). `SWARMS.md` requires a separate manual update. Exact generated-content parity remains **UNVERIFIED** without running the generator.

- **MEDIUM — The proposed tooling gate does not verify model selection.** `agent-doctor.sh:234–238` checks ast-grep, ripwire, tgrep, and Serena. A stale tgrep snapshot can pass with a warning (lines 82–108). It does not validate CAO/NTM launches or account access. Matching `git ls-tree` blob IDs confirm that the three MCP configurations and doctor script are identical across branches. Their commands resolve the launch checkout dynamically (`.mcp.json:8,16,24`, `.codex/config.toml:7,11,15`, `.grok/config.toml:7,11,15`); restart and verify them after cutover.

**Q1 — Land early?** Yes, after the preservation and tracker checks below. Separate branch consolidation from modernization completion. Amend D1, Migration Plan, Open Questions, and task 10.1; retain the donor-hunk audit in task 7.3. Currently, `design.md:262` and `tasks.md:96` condition landing on exhausting unique donor work. Early landing costs a coordinated ownership/tooling transition. Existing CI supplies no feature-branch push gate and still selects JDK 8 (`.github/workflows/main.yml:8–11,23–26`).

**Q2 — Replace or merge?** Prefer replacement with durable preservation refs. Verified divergence is `10 / 23`; the recorded remote-tracking feature tip is an integration ancestor (`merge-base --is-ancestor`, exit 0). A normal push is therefore possible **if the remote has not advanced**. Replacement retains integration’s reviewable history; the old-tip tag preserves donor blame/history separately. A resolved merge retains both ancestries but records discarded donor changes as merged, complicating later donor accounting. Either strategy still requires explicit per-hunk review in phases 5–7.

**Q3–Q4 — Recommended shorter plan**

1. Freeze a commit/path manifest and approve the D1 amendment. Arrange owner-controlled preservation of both checkouts’ current work, including untracked research evidence. Main-checkout dirty contents remain **UNVERIFIED here**.
2. Combine inventory with the port allowlist: exclude the four tracker files initially; preserve integration’s product and modernization documents. Give 5.6 an OpenSpec task and reconcile its canonical tracker entry before committing it.
3. Make model updates a separately tracked change/commit. Packet-based targets: Kimi `kimi-code/k3.1-pro`, label `Kimi K3.1 Pro`; selectable NTM Agy `--agyx=1:gemini-4-argon-high`; CAO label `Agy Gemini 4 Argon (High)`. Verify CAO’s accepted model-selector spelling before setting it. Preserve the separate Flash persona constraint until tested. Availability and the reported Kimi 403 remain **UNVERIFIED**; changing a pin does not establish access.
4. Review the final candidate, run reactor/conformance and both strict validations, and index evidence. Resolve the RFC822Name disposition first. For the separate generator change, review the complete output diff, require repeat-generation stability, and verify actual provider/model selection.
5. During a short tracker freeze, incorporate the fresh authoritative export, preserve historical refs, and perform maintainer-only landing. Verify tracker IDs, states, dependencies, and claims afterward. Update checkout routing and run tooling checks there. Defer worktree removal until preservation is proven.

**Open questions:** Which checkout/path remains canonical? Who owns the tracker freeze? Should RFC822Name land or remain preserved separately? Keep both WIP branches until their content audits resolve disposal.

Verification was static: required documents, generator/configuration sources, Git blobs, ancestry, and diffs. No files changed; no builds, regeneration, tracker operations, NTM, or network calls ran. Runtime acceptance remains UNVERIFIED.

END FINAL REPORT L3
