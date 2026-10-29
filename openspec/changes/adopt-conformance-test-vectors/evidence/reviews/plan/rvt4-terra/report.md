<!-- Plan re-review RVT4 of adopt-conformance-test-vectors, Codex GPT-6.1 Terra, read-only; report from the NTM pane, paths made repository-relative. -->

## RVT3 dispositions

| RVT3 finding | Status | Review |
| --- | --- | --- |
| 1. `legacy-model` path | **Partly resolved** | [D1 and D3](openspec/changes/adopt-conformance-test-vectors/design.md:27) and task 2.4 now assign the mode and adapter, but D3 and the spec still contain blanket wire-only statements that conflict with it. |
| 2. The 72 cases may remain unattempted | **Resolved in task scope** | [Task 3.3](openspec/changes/adopt-conformance-test-vectors/tasks.md:22) requires an attempt and retained outcome for every case. Its ordering and evidence description need correction below. |
| 3. Passing rejection without comparison | **Resolved** | The [equivalence requirement](openspec/changes/adopt-conformance-test-vectors/specs/conformance-suite-harness/spec.md:33) records outcome and comparison state separately, with controls for both case IDs in both suites. |
| 4. No outcome gate after moving to `full` | **Resolved for outcomes** | [Task 2.8](openspec/changes/adopt-conformance-test-vectors/tasks.md:16) adds the final per-row outcome comparison. The new P1 below concerns preservation of field sensitivity after that move. |
| 5. JSON comparator controls | **Resolved** | [Task 5.1](openspec/changes/adopt-conformance-test-vectors/tasks.md:32) assigns `JsonResponseComparatorTest` and its controls. |
| 6. Per-file provenance coverage | **Resolved** | The [provenance scenario](openspec/changes/adopt-conformance-test-vectors/specs/conformance-vector-provenance/spec.md:28) and task 2.5 assign missing-file and partial-group controls. |
| 7. Unverified upstream equality | **Resolved** | [D9](openspec/changes/adopt-conformance-test-vectors/design.md:125) now limits upstream-equality claims to verified files. |

## New and remaining findings

### P1 — Moving RSA and example rows to `full` can weaken their existing status checks

[Task 2.8](openspec/changes/adopt-conformance-test-vectors/tasks.md:16) moves legacy rows to `full` when their current outcomes match the frozen record. The old RSA and example path compares model statuses directly through [XacmlPolicyTestSupport](xacml-test/src/main/java/org/xacml4j/v30/XacmlPolicyTestSupport.java:186); status equality includes nested codes and detail. The current [2.0 marshaller](xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml20ResponseContextMarshaller.java:138) omits both.

For example, an actual model status with the expected top-level code but an extra nested code would fail the old model comparison. After that actual response is marshalled, the nested difference is gone, so a wire comparison can accept it. Today’s outcome can match while that future regression becomes invisible.

**Smallest fix:** Keep `legacy-model` as an additional check for those rows when enabling `full`, or defer their move until a separately approved lossless 2.0 marshalling change exists. Add controls proving that the post-move path still rejects altered nested codes and status details.

### P2 — The model exception conflicts with the blanket wire-only contract

[D3](openspec/changes/adopt-conformance-test-vectors/design.md:44) says the runner does not use `ResponseContext` for comparison, then assigns a model-response comparison to `legacy-model`. The [spec](openspec/changes/adopt-conformance-test-vectors/specs/conformance-suite-harness/spec.md:61) likewise requires XML responses to be compared at wire level before defining the model mode.

**Smallest fix:** State the `legacy-model` exception explicitly in the normative wire requirement, D3 and the proposal, and describe how the entry point dispatches to it.

### P2 — Approved baseline changes conflict with the final equality requirement

The [equivalence requirement](openspec/changes/adopt-conformance-test-vectors/specs/conformance-suite-harness/spec.md:41) says every final outcome must equal the original frozen record, but also permits a changed outcome after an approved baseline change. As written, an approved change still cannot equal the original record.

**Smallest fix:** Keep the original record immutable and define a reviewed, approved override for each changed row; compare against the original plus those overrides. Alternatively, disallow baseline changes in this change.

### P2 — The 72-case attempt is ordered before the rows that the runner needs

[D2](openspec/changes/adopt-conformance-test-vectors/design.md:35) creates a dynamic test for each descriptor row. [Task 3.3](openspec/changes/adopt-conformance-test-vectors/tasks.md:22) says to run all 72 and only **then** add their rows and review records, without specifying another way to feed them to the runner.

**Smallest fix:** Add the 72 enabled characterization rows and required review records first, run each once and retain its outcome, then fix or disable failures. Update the [evidence gate](openspec/changes/adopt-conformance-test-vectors/evidence/README.md:17) to require all 72 attempt outcomes; it currently says an outcome **or** a disabled reason is sufficient.

### P2 — The group review precedes the final mode transitions

[Task 2.7](openspec/changes/adopt-conformance-test-vectors/tasks.md:15) is the independent review and reactor gate, but task 2.8 changes comparison modes afterward, before work proceeds to group 3. The group’s final behavior therefore is not part of its named review gate.

**Smallest fix:** Place the group 2 review and reactor gate after task 2.8, or add an equivalent gate after the transitions and before group 3.

### P2 — Legacy-model diagnostics lack a control for multiple differences

The [comparison spec](openspec/changes/adopt-conformance-test-vectors/specs/conformance-suite-harness/spec.md:73) requires a mismatch to name the case and every differing field. The unchanged [model utility](xacml-test/src/main/java/org/xacml4j/v30/XacmlPolicyTestSupport.java:174) has no case argument and stops at its first failed assertion. The assigned two-difference scenario covers `full`; the new `legacy-model` control changes one nested code.

**Smallest fix:** Specify how the adapter adds case context and gathers diagnostics while preserving the old verdict, and add a `legacy-model` control with two differences. If the all-differences rule is intended only for `full`, scope it explicitly.

This was a static, read-only review. No files were changed; no Maven, tests, benchmarks, downloads, Git writes or `br` writes were run.

**Verdict: REJECT.** The proposed move to `full` can discard status distinctions checked by the existing RSA and example path despite a passing outcome gate.
