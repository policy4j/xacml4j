FINAL REPORT (Codex GPT-6.1-Sol, persona policy4j_implementer_codex_sol)

Landing preparation is in place: 205 files ported unchanged and six tracked files updated. Nothing was staged or committed. Two check findings remain: existing untracked conductor evidence and failures in the ported Python suite.

Changed tracked files:

- [.gitignore](.gitignore:64): added the two `.doctor/` lines after `/outputs/`.
- [design.md](openspec/changes/modernize-core-for-jdk28-valhalla/design.md:79): recorded D1’s landing ruling, revised migration and rollback text, and resolved the first open question.
- [tasks.md](openspec/changes/modernize-core-for-jdk28-valhalla/tasks.md:58): added unchecked 5.6/5.7, extended 7.3, and replaced unchecked 10.1.
- [SWARMS.md](docs/ai/SWARMS.md:145): updated NTM location, session examples, donor references, and POSIX/fish setup.
- [gen-cao-profiles.py](scripts/gen-cao-profiles.py:781): updated the generated NTM instructions.
- [CAO_PROFILES.md](.agents/CAO_PROFILES.md:118): regenerated the matching NTM text.

Ported files: `.beads/` **4**, `.agents/skills/desktop-deep-research/` **5**, and `openspec/changes/add-desktop-deep-research/` **196**. Every file remains untracked and byte-identical to `a0ec4683`; the complete inventory appears below.

Verification:

1. `rtk git status --short`: tracked scope **PASS**—exactly the original 10 modified files plus the six above. The strict untracked-only condition **FAILS** because the pre-existing conductor directory `openspec/changes/modernize-core-for-jdk28-valhalla/evidence/reviews/plan/rv094-single-branch-landing/` remains alongside the three ported paths.
2. `rtk git diff --cached --name-only`: **PASS**, empty.
3. `rtk git ls-tree -r a0ec4683 -- .beads .agents/skills/desktop-deep-research openspec/changes/add-desktop-deep-research`, followed by `rtk git hash-object <path>` for every entry: **PASS, 205/205**, zero mismatches, missing files, or extra files. Raw blob hashes also matched.
4. `rtk git diff --stat -- 'xacml-*' pom.xml .serena/project.yml`: **PASS**, unchanged from baseline: 10 files, 210 insertions, 167 deletions. The protected binary diff’s SHA-256 also remained unchanged.
5. `rtk openspec validate modernize-core-for-jdk28-valhalla --strict`: **PASS**, exit 0.
6. `rtk openspec validate add-desktop-deep-research --strict`: **PASS**, exit 0.
7. `PYTHONDONTWRITEBYTECODE=1 rtk python3 -m unittest discover -s .agents/skills/desktop-deep-research/tests -v`: **FAIL**, exit 1; 34 methods ran in 3.704 seconds, 31 passed, and three methods produced 36 failed subtests:
   - `test_highest_research_model_verified_before_input`
   - `test_model_change_before_input_blocks_submission`
   - `test_model_unavailable_or_unverifiable_blocks_submission`

`rtk python3 scripts/gen-cao-profiles.py` succeeded after an approved rerun resolved the sandbox denial. It generated 66 profiles, 59 personas, six sets, and eight role files; removed zero stale files. Only the README’s NTM text changed; no generated-file restoration was needed.

Additional checks passed: scoped `git diff --check`, unchanged model pins, unchanged SWARMS text outside its NTM section, unchanged existing checked tasks, and unchanged remaining open questions. HEAD and `feature_xacml_20` still resolve to the required starting hashes. No `br` command or Maven build ran.

Residual risk: the ported model-selection tests remain red and were left unchanged as instructed. The conductor’s reactor gate and landing review remain outstanding. The existing untracked evidence prevents a literal clean result for the untracked-path check.

<details>
<summary>Complete 205-file port inventory</summary>

Each entry’s reason is **P: restore byte-identically from a0ec4683**. Paths are relative to the directory heading.

`.beads/` — 4 files:

```text
.gitignore — P
config.yaml — P
issues.jsonl — P
metadata.json — P
```

`.agents/skills/desktop-deep-research/` — 5 files:

```text
.gitignore — P
scripts/DesktopResearchBridge.swift — P
scripts/desktop_research.py — P
scripts/native_bridge.py — P
tests/test_execution.py — P
```

`openspec/changes/add-desktop-deep-research/` — 196 files:

```text
.openspec.yaml — P
design.md — P
evidence/README.md — P
evidence/builds/001-local-capabilities/command.txt — P
evidence/builds/001-local-capabilities/followup-command.txt — P
evidence/builds/001-local-capabilities/probe-output.txt — P
evidence/builds/001-local-capabilities/signature-command.txt — P
evidence/builds/001-local-capabilities/signature-output.json — P
evidence/builds/001-local-capabilities/summary.md — P
evidence/builds/002-plan-validation/command.txt — P
evidence/builds/002-plan-validation/mapping-output.txt — P
evidence/builds/002-plan-validation/summary.md — P
evidence/builds/002-plan-validation/validation.json — P
evidence/builds/003-revised-plan-validation/command.txt — P
evidence/builds/003-revised-plan-validation/mapping-output.json — P
evidence/builds/003-revised-plan-validation/summary.md — P
evidence/builds/003-revised-plan-validation/validation.json — P
evidence/builds/004-binding-plan-validation/command.txt — P
evidence/builds/004-binding-plan-validation/mapping-output.json — P
evidence/builds/004-binding-plan-validation/summary.md — P
evidence/builds/004-binding-plan-validation/validation.json — P
evidence/builds/005-final-plan-audit/audit-output.json — P
evidence/builds/005-final-plan-audit/command.txt — P
evidence/builds/005-final-plan-audit/summary.md — P
evidence/builds/005-final-plan-audit/validation.json — P
evidence/builds/007-live-characterization/bind-claude-sample.swift — P
evidence/builds/007-live-characterization/citation-comparison-receipt.json — P
evidence/builds/007-live-characterization/close-claude-preparation-menu.swift — P
evidence/builds/007-live-characterization/collect-claude-native.swift — P
evidence/builds/007-live-characterization/command.txt — P
evidence/builds/007-live-characterization/copy-claude-document.swift — P
evidence/builds/007-live-characterization/disable-claude-connectors.swift — P
evidence/builds/007-live-characterization/finish-claude-restoration.swift — P
evidence/builds/007-live-characterization/hover-claude-connectors.swift — P
evidence/builds/007-live-characterization/hover-claude-source-submenu.swift — P
evidence/builds/007-live-characterization/independent-validation-receipt.json — P
evidence/builds/007-live-characterization/inspect-claude-document.swift — P
evidence/builds/007-live-characterization/inspect-claude-source-children.swift — P
evidence/builds/007-live-characterization/inspect.swift — P
evidence/builds/007-live-characterization/markdown-copy-receipt.json — P
evidence/builds/007-live-characterization/native-collection-receipt.json — P
evidence/builds/007-live-characterization/new-chatgpt-window.swift — P
evidence/builds/007-live-characterization/observe-claude-sample.swift — P
evidence/builds/007-live-characterization/ocr-claude-controls.swift — P
evidence/builds/007-live-characterization/open-claude-document-menu.swift — P
evidence/builds/007-live-characterization/open-claude-document.swift — P
evidence/builds/007-live-characterization/prepare-claude-prompt.swift — P
evidence/builds/007-live-characterization/profiles.json — P
evidence/builds/007-live-characterization/read-claude-status.swift — P
evidence/builds/007-live-characterization/receipts.json — P
evidence/builds/007-live-characterization/restoration-receipt.json — P
evidence/builds/007-live-characterization/restore-claude-mode.swift — P
evidence/builds/007-live-characterization/restore-claude-sources.swift — P
evidence/builds/007-live-characterization/scroll-claude-connectors.swift — P
evidence/builds/007-live-characterization/select-claude-chat.swift — P
evidence/builds/007-live-characterization/send-claude-sample.swift — P
evidence/builds/007-live-characterization/summary.md — P
evidence/builds/007-live-characterization/verify-claude-owned-draft.swift — P
evidence/builds/008-characterization-disposition/command.txt — P
evidence/builds/008-characterization-disposition/observations.json — P
evidence/builds/008-characterization-disposition/summary.md — P
evidence/builds/014-browser-surface-research/command.txt — P
evidence/builds/014-browser-surface-research/local-probe.txt — P
evidence/builds/014-browser-surface-research/sha256.txt — P
evidence/builds/014-browser-surface-research/sources.md — P
evidence/builds/014-browser-surface-research/summary.md — P
evidence/builds/015-browser-plan-validation/command.txt — P
evidence/builds/015-browser-plan-validation/mapping.txt — P
evidence/builds/015-browser-plan-validation/summary.md — P
evidence/builds/015-browser-plan-validation/validate.txt — P
evidence/builds/016-browser-research-crosscheck/command.txt — P
evidence/builds/016-browser-research-crosscheck/driver-help-0.0.73.txt — P
evidence/builds/016-browser-research-crosscheck/driver-probe.txt — P
evidence/builds/016-browser-research-crosscheck/summary.md — P
evidence/builds/017-browser-plan-revalidation/command.txt — P
evidence/builds/017-browser-plan-revalidation/mapping.txt — P
evidence/builds/017-browser-plan-revalidation/summary.md — P
evidence/builds/017-browser-plan-revalidation/validate.txt — P
evidence/builds/018-browser-driver-source-check/command.txt — P
evidence/builds/018-browser-driver-source-check/findings.txt — P
evidence/builds/018-browser-driver-source-check/summary.md — P
evidence/builds/019-browser-plan-revalidation/command.txt — P
evidence/builds/019-browser-plan-revalidation/mapping.txt — P
evidence/builds/019-browser-plan-revalidation/summary.md — P
evidence/builds/019-browser-plan-revalidation/validate.txt — P
evidence/builds/020-browser-plan-revalidation/command.txt — P
evidence/builds/020-browser-plan-revalidation/mapping.txt — P
evidence/builds/020-browser-plan-revalidation/summary.md — P
evidence/builds/020-browser-plan-revalidation/validate.txt — P
evidence/final/006-maintainer-approval/command.txt — P
evidence/final/006-maintainer-approval/receipt.json — P
evidence/final/006-maintainer-approval/summary.md — P
evidence/reviews/plan/rv001-architecture-claude/command.txt — P
evidence/reviews/plan/rv001-architecture-claude/disposition.md — P
evidence/reviews/plan/rv001-architecture-claude/init.json — P
evidence/reviews/plan/rv001-architecture-claude/original-hashes.json — P
evidence/reviews/plan/rv001-architecture-claude/report.md — P
evidence/reviews/plan/rv001-architecture-claude/result.json — P
evidence/reviews/plan/rv001-architecture-claude/scope-receipt.json — P
evidence/reviews/plan/rv001-architecture-claude/summary.md — P
evidence/reviews/plan/rv002-architecture-agy/command.txt — P
evidence/reviews/plan/rv002-architecture-agy/event-counts.json — P
evidence/reviews/plan/rv002-architecture-agy/exit.json — P
evidence/reviews/plan/rv002-architecture-agy/init.json — P
evidence/reviews/plan/rv002-architecture-agy/invocation.json — P
evidence/reviews/plan/rv002-architecture-agy/original-hashes.json — P
evidence/reviews/plan/rv002-architecture-agy/packet-hashes-after.json — P
evidence/reviews/plan/rv002-architecture-agy/packet-hashes-before.json — P
evidence/reviews/plan/rv002-architecture-agy/packet-hashes-final-audit.json — P
evidence/reviews/plan/rv002-architecture-agy/prompt.txt — P
evidence/reviews/plan/rv002-architecture-agy/report.md — P
evidence/reviews/plan/rv002-architecture-agy/result.json — P
evidence/reviews/plan/rv002-architecture-agy/summary.md — P
evidence/reviews/plan/rv003-corrected-plan-agy/command.txt — P
evidence/reviews/plan/rv003-corrected-plan-agy/exit.json — P
evidence/reviews/plan/rv003-corrected-plan-agy/init.json — P
evidence/reviews/plan/rv003-corrected-plan-agy/invocation.json — P
evidence/reviews/plan/rv003-corrected-plan-agy/model-receipt.json — P
evidence/reviews/plan/rv003-corrected-plan-agy/original-hashes.json — P
evidence/reviews/plan/rv003-corrected-plan-agy/packet-hashes-after.json — P
evidence/reviews/plan/rv003-corrected-plan-agy/packet-hashes-before.json — P
evidence/reviews/plan/rv003-corrected-plan-agy/prompt.txt — P
evidence/reviews/plan/rv003-corrected-plan-agy/receipt-checks.json — P
evidence/reviews/plan/rv003-corrected-plan-agy/report.md — P
evidence/reviews/plan/rv003-corrected-plan-agy/result.json — P
evidence/reviews/plan/rv003-corrected-plan-agy/session-receipt.json — P
evidence/reviews/plan/rv003-corrected-plan-agy/summary.md — P
evidence/reviews/plan/rv004-binding-plan-agy/command.txt — P
evidence/reviews/plan/rv004-binding-plan-agy/coordinator-summary.md — P
evidence/reviews/plan/rv004-binding-plan-agy/exit.json — P
evidence/reviews/plan/rv004-binding-plan-agy/init.json — P
evidence/reviews/plan/rv004-binding-plan-agy/invocation.json — P
evidence/reviews/plan/rv004-binding-plan-agy/model-receipt.json — P
evidence/reviews/plan/rv004-binding-plan-agy/original-hashes.json — P
evidence/reviews/plan/rv004-binding-plan-agy/packet-hashes-after.json — P
evidence/reviews/plan/rv004-binding-plan-agy/packet-hashes-before.json — P
evidence/reviews/plan/rv004-binding-plan-agy/packet-sections.json — P
evidence/reviews/plan/rv004-binding-plan-agy/prompt.txt — P
evidence/reviews/plan/rv004-binding-plan-agy/receipt-checks.json — P
evidence/reviews/plan/rv004-binding-plan-agy/report.md — P
evidence/reviews/plan/rv004-binding-plan-agy/result.json — P
evidence/reviews/plan/rv004-binding-plan-agy/session-receipt.json — P
evidence/reviews/plan/rv004-binding-plan-agy/summary.md — P
evidence/reviews/plan/rv005-browser-plan-codex/command.txt — P
evidence/reviews/plan/rv005-browser-plan-codex/packet.txt — P
evidence/reviews/plan/rv005-browser-plan-codex/prompt-header.md — P
evidence/reviews/plan/rv005-browser-plan-codex/report.md — P
evidence/reviews/plan/rv005-browser-plan-codex/summary.md — P
evidence/reviews/plan/rv006-browser-plan-swarm-codex-astra/command.txt — P
evidence/reviews/plan/rv006-browser-plan-swarm-codex-astra/packet.md — P
evidence/reviews/plan/rv006-browser-plan-swarm-codex-astra/report.md — P
evidence/reviews/plan/rv006-browser-plan-swarm-codex-astra/summary.md — P
evidence/reviews/plan/rv007-browser-plan-swarm-codex-sol/command.txt — P
evidence/reviews/plan/rv007-browser-plan-swarm-codex-sol/packet.md — P
evidence/reviews/plan/rv007-browser-plan-swarm-codex-sol/report.md — P
evidence/reviews/plan/rv007-browser-plan-swarm-codex-sol/summary.md — P
evidence/reviews/plan/rv008-browser-plan-swarm-codex-luna/command.txt — P
evidence/reviews/plan/rv008-browser-plan-swarm-codex-luna/packet.md — P
evidence/reviews/plan/rv008-browser-plan-swarm-codex-luna/report.md — P
evidence/reviews/plan/rv008-browser-plan-swarm-codex-luna/summary.md — P
evidence/reviews/plan/rv009-browser-plan-swarm-claude-opus/command.txt — P
evidence/reviews/plan/rv009-browser-plan-swarm-claude-opus/packet.md — P
evidence/reviews/plan/rv009-browser-plan-swarm-claude-opus/report.md — P
evidence/reviews/plan/rv009-browser-plan-swarm-claude-opus/summary.md — P
evidence/reviews/plan/rv010-browser-plan-swarm-claude-fable/command.txt — P
evidence/reviews/plan/rv010-browser-plan-swarm-claude-fable/packet.md — P
evidence/reviews/plan/rv010-browser-plan-swarm-claude-fable/report.md — P
evidence/reviews/plan/rv010-browser-plan-swarm-claude-fable/summary.md — P
evidence/reviews/plan/rv011-browser-plan-swarm-grok/command.txt — P
evidence/reviews/plan/rv011-browser-plan-swarm-grok/packet.md — P
evidence/reviews/plan/rv011-browser-plan-swarm-grok/report.md — P
evidence/reviews/plan/rv011-browser-plan-swarm-grok/summary.md — P
evidence/reviews/plan/rv012-browser-plan-confirm-codex/command.txt — P
evidence/reviews/plan/rv012-browser-plan-confirm-codex/packet.txt — P
evidence/reviews/plan/rv012-browser-plan-confirm-codex/prompt-header.md — P
evidence/reviews/plan/rv012-browser-plan-confirm-codex/report.md — P
evidence/reviews/plan/rv012-browser-plan-confirm-codex/summary.md — P
evidence/tests/009-execution-red/command.txt — P
evidence/tests/009-execution-red/output.txt — P
evidence/tests/009-execution-red/receipt.json — P
evidence/tests/009-execution-red/summary.md — P
evidence/tests/010-execution-green/command.txt — P
evidence/tests/010-execution-green/output.txt — P
evidence/tests/010-execution-green/receipt.json — P
evidence/tests/010-execution-green/summary.md — P
evidence/tests/013-highest-model-red/command.txt — P
evidence/tests/013-highest-model-red/execution-red.txt — P
evidence/tests/013-highest-model-red/runtime.txt — P
evidence/tests/013-highest-model-red/sha256.txt — P
evidence/tests/013-highest-model-red/summary.md — P
proposal.md — P
research/README.md — P
specs/browser-research-session/spec.md — P
specs/desktop-research-collection/spec.md — P
specs/desktop-research-execution/spec.md — P
tasks.md — P
```

</details>

END FINAL REPORT P1
