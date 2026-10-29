<!-- Plan re-review RVT5 of adopt-conformance-test-vectors, Codex GPT-6.1 Terra, read-only; report from the NTM pane, paths made repository-relative. -->

## RVT4 dispositions

| RVT4 finding | Status | Review |
| --- | --- | --- |
| 1. Moving model rows to `full` loses status checks | **Resolved** | The [equivalence requirement](openspec/changes/adopt-conformance-test-vectors/specs/conformance-suite-harness/spec.md:41) retains `legacy-model` as a second check; task 2.7 assigns nested-code and status-detail controls. |
| 2. Wire-only wording conflicts with model mode | **Resolved** | [D3](openspec/changes/adopt-conformance-test-vectors/design.md:44), the spec and the proposal now state the exception and dispatch. The field list needs a wording correction noted below. |
| 3. Baseline changes conflict with final equality | **Resolved in the contract** | The [spec](openspec/changes/adopt-conformance-test-vectors/specs/conformance-suite-harness/spec.md:43) keeps the frozen record unchanged and defines reviewed per-row overrides. Verification of that new path needs the control below. |
| 4. The 72 attempts precede their rows | **Resolved** | [Task 3.3](openspec/changes/adopt-conformance-test-vectors/tasks.md:22) adds enabled rows and review records first, then records all 72 attempts before fixing or disabling failures. |
| 5. Review gate precedes mode transitions | **Resolved** | The transitions are now [task 2.7](openspec/changes/adopt-conformance-test-vectors/tasks.md:15), followed by the review, reactor and conformance gate in 2.8. |
| 6. Legacy-model diagnostics | **Resolved** | The [spec](openspec/changes/adopt-conformance-test-vectors/specs/conformance-suite-harness/spec.md:76) scopes that mode to the old verdict and first difference, adds the case ID, and names a two-difference control. |

## New findings

### P2 — The new override path lacks a negative gate control

The [spec](openspec/changes/adopt-conformance-test-vectors/specs/conformance-suite-harness/spec.md:43) permits reviewed, approved overrides with old and new outcomes, reason, approver and date. [Task 2.7](openspec/changes/adopt-conformance-test-vectors/tasks.md:15) compares outcomes against the frozen record plus overrides, but assigns no control proving that the gate rejects an unapproved, stale or incorrectly matched override. This is a gap in the planned verification of the mechanism that permits changes to the 880 outcomes.

**Smallest fix:** Add a scenario and task-level control: an unoverridden change fails; an approved override for the exact row and old/new outcomes passes; missing approval, a stale old outcome, a wrong new outcome or duplicate overrides fail. Retain the original frozen record and link the human review and approval evidence.

### P3 — The model-exception wording and evidence map need alignment

The parenthetical field list in the [lossless comparison requirement](openspec/changes/adopt-conformance-test-vectors/specs/conformance-suite-harness/spec.md:64) follows “the model responses as today’s utility does,” which can be read as saying the legacy model preserves repeated obligations and categories. [D3](openspec/changes/adopt-conformance-test-vectors/design.md:46) correctly states that the model merges them. D3 and task 2.4 also retain unqualified “every difference” wording despite the new first-difference exception.

The [evidence index](openspec/changes/adopt-conformance-test-vectors/evidence/README.md:7) omits task 2.7 from the outcome and lossless-comparison mappings after the renumbering, and omits task 5.2 from adopted-case review records.

**Smallest fix:** Attach the field list to the wire-comparison clause, state that `legacy-model` retains its lossy model semantics, scope the diagnostics wording consistently, and update the task mappings.

This was a static, read-only review. No files were changed; no Maven, tests, benchmarks, downloads, Git writes or `br` writes were run.

**Verdict: APPROVE WITH CHANGES.** Add the override controls and correct the artifact wording and evidence map. This is plan review, not implementation acceptance.
