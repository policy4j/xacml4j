# Worktree and remaining-work audit

Conductor inspection on 2026-10-29, main `feature_xacml_20` at
`759d90a6c75510d55127d8e22870f9edd985c991`. `inventory.json` is a live
snapshot while the five new NTM lanes were working. Its untracked counts
are Git status entries, which may represent directories. It is not a
complete untracked-file inventory or a frozen implementation candidate.

## Worktrees

Git reports twelve attached worktrees: main, six preserved batch2 trees,
and five new isolated `remaining-g1-20261029` trees. No branch was switched,
staged, committed, merged, pushed, or removed during this audit.

| Preserved batch2 tree | Remaining source work | Disposition |
| --- | --- | --- |
| `cod_1` | D52 higher-order preparation, eight owned source/test paths including new `HigherOrderCall` | Frozen original reviewed independently; mutable custom metadata defect rejected by Astra and Argon. Correction is being authored in a new tree. |
| `cc_1` | P-CPU2 response-capability variable-DAG walk and stored algorithm membership reuse | New Sol lane implements/tests a bounded correction against current main. |
| `cod_3` | D49 string hash rejection, D50 small-request category scan, D51 bag cardinality | Retained candidate; controlled measurements and final acceptance remain pending. |
| `agyx_1`, `cod_2` | Their tracked Java edit already matches main byte for byte | Preserve reports and untracked material; do not treat these trees as new source to integrate. |
| `cc_2` | Large older overlay: 148 tracked modified paths; 119 already match current main byte for byte | Preserve. Review the remaining differences individually; wholesale integration would mix stale tracker/docs/source. |

The matching counts above were checked by comparing each tracked modified
path's file bytes against main. They do not establish that untracked
reports or sources are redundant. Main production sources had no edits at
the inventory snapshot. The pre-existing `.gemini/` directory is protected.

## Remaining work

At the snapshot, the shared Beads export contains 516 issues: 274 closed,
228 open, and 14 in progress. These are tracker states, not acceptance
counts. The export was flushed from the shared main database, without
importing an older worktree's JSONL.

| Active change | Checked / total checklist entries | Outstanding focus |
| --- | --- | --- |
| `optimize-pdp-evaluation` | 102 / 145 | D47 correction and prerequisite, corrected D52, D49–D51 measurements, D55, batch measurements and historical review gates, then the final stop-rule audit |
| `modernize-core-for-jdk28-valhalla` | 29 / 60 | Test-first task 5.7 correction, identifier/value/registry migration, expression/model migration, JDK28 compatibility gate, then audited value-class conversions |
| `add-regex-engine-spi` | 11 / 23 | Remaining review/measurement and implementation gates; reconcile checked tasks with stale Beads statuses only after proof inspection |
| `upgrade-dependencies` | 7 / 20 | Remaining upgrades and compatibility gates; task 3.1 tracker discrepancy needs evidence reconciliation |
| `add-pdp-jmh-benchmarks` | 16 / 21 | Remaining harness/review/measurement closeout gates |
| `improve-xacml-parsing` | 3 / 35 | Approved implementation and verification work remains |
| `adopt-conformance-test-vectors` | 2 / 27 | Proposal/approval boundary and manifest/harness work remain |
| `add-desktop-deep-research` | 6 / 31 | Separate desktop work; outside this optimization/modernization wave |
| `adopt-jdk25-baseline`, `upgrade-build-plugins` | 9 / 9; 6 / 6 | Checked lists and closed epics do not establish archive readiness; archive gates remain separate |

Two other active proposal directories have no task list yet. The twelve
active changes passed strict structural validation in the initial audit;
that validation does not prove implementation or runtime behavior.

Optimization group 42 now has its 21 checklist issues and bounded worker
packets. Groups 36–41 have parent records but lack 24 task-level records.
Several historical checked entries remain open in Beads; some parent
statuses also disagree with unchecked gates. No historical implementation
or gate is accepted merely to reconcile those records.

## Current execution boundary

The requested Terra, Sol, Astra, Luna, and Agy Argon lanes were identified
from their actual CLI model headers. NTM's inferred Codex model field is
PARTIAL: it reports Astra for all Codex panes. Launch receipts and the
source-bound lane reports are retained separately.

Fresh regression and module results belong to their source-bound author
runs. Reviews, a combined main-tree reactor/conformance gate, and quiet
controlled C2 JMH remain necessary before accepting hot-path changes.
There is no performance acceptance in this audit. The full optimization
and modernization effort remains unfinished.
