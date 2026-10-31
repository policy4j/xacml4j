<!-- Plan re-review RVT3 of adopt-conformance-test-vectors, Codex GPT-6.1 Terra, read-only; report from the NTM pane, paths made repository-relative. -->

## RVT2 dispositions

| RVT2 finding | Status | Review |
| --- | --- | --- |
| 1. Public model comparator | **Partly resolved** | [D3](openspec/changes/adopt-conformance-test-vectors/design.md:42) keeps the public method unchanged, but the runner’s wire-only path does not yet reproduce the old model comparison. See P1 below. |
| 2. Legacy field sets | **Partly resolved** | [D3](openspec/changes/adopt-conformance-test-vectors/design.md:52) defines the modes and the 3.0 nested-code control, but `legacy-model` is absent from the descriptor’s mode list and task 2.4. |
| 3. Expected-invalid JSON | **Resolved** | The [JSON requirement](openspec/changes/adopt-conformance-test-vectors/specs/conformance-suite-harness/spec.md:119) compares the module verdict with expected validity and adds valid and invalid controls. |
| 4. Stale proposal | **Resolved** | The [proposal](openspec/changes/adopt-conformance-test-vectors/proposal.md:19) now describes the revised comparison, deferred sources, 72 cases and RSA exception. |
| 5. JSON end-to-end path | **Resolved** | [D10](openspec/changes/adopt-conformance-test-vectors/design.md:138) and task 5.1 specify published request, PDP, public marshaller and wire comparison. |
| 6. JSON comparator dependency | **Resolved** | [D3](openspec/changes/adopt-conformance-test-vectors/design.md:48) supplies the JSON comparator from the conformance module; task 5.1 assigns its dependencies and implementation. |
| 7. Deprecated-case inventory | **Resolved for the RVT2 inventory finding** | [Task 3.3](openspec/changes/adopt-conformance-test-vectors/tasks.md:21) migrates the existing checks and requires a 72-file/row bijection. Its allowance for unrun disabled cases conflicts with the maintainer’s instruction; see P1 below. |
| 8. RSA ledger drift | **Partly resolved** | [D9](openspec/changes/adopt-conformance-test-vectors/design.md:130) adds the baseline and exact-delta checks, but still asserts upstream equality before verification. |
| 9. Comparison semantics and diagnostics | **Partly resolved** | The [spec](openspec/changes/adopt-conformance-test-vectors/specs/conformance-suite-harness/spec.md:53) defines typed equality, bag multiplicity and multiple-difference diagnostics. The new JSON comparator’s corresponding controls are not assigned. |
| 10. Per-file redistribution basis | **Resolved in the requirement** | The [provenance spec](openspec/changes/adopt-conformance-test-vectors/specs/conformance-vector-provenance/spec.md:18) requires origin and basis per file or uniform group. A coverage check for those records is still unassigned. |
| 11. Evidence index | **Partly resolved** | The [index](openspec/changes/adopt-conformance-test-vectors/evidence/README.md:5) maps the revised gates, but attributes JSON comparator coverage to task 5.1 without assigning the controls there, and omits task 5.2 from adopted-case review evidence. |

## New and remaining findings

### P1 — `legacy-model` has no implementable path in the stated runner design

[D3](openspec/changes/adopt-conformance-test-vectors/design.md:52) requires `legacy-model` for the RSA and example suites, while excluding `ResponseContext` from runner comparison. [D1](openspec/changes/adopt-conformance-test-vectors/design.md:27) lists only `full`, `decision-status` and `decision` as descriptor values, and [task 2.4](openspec/changes/adopt-conformance-test-vectors/tasks.md:12) assigns no `legacy-model` implementation.

This is more than a missing mode name. The old RSA and example path compares the parsed expected response directly with the PDP’s model response ([XacmlPolicyTestSupport](xacml-test/src/main/java/org/xacml4j/v30/XacmlPolicyTestSupport.java:93)). The current [XACML 2.0 response marshaller](xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml20ResponseContextMarshaller.java:138) writes the top-level status code and message but omits nested codes and status detail. A wire-only comparison through that marshaller cannot reproduce all distinctions in the old model status comparison.

**Smallest fix:** Permit a `legacy-model` runner adapter to compare the original model responses using the unchanged utility until equivalence is established, or specify and prove another lossless path. Add all legacy modes to the descriptor and implementation task. Add a control in which model responses differ in nested status or detail but their current marshalled output loses that difference.

### P1 — Task 3.3 permits the 72 cases to remain unattempted

The maintainer directed that the 72 deprecated-identifier cases run. [Task 3.3](openspec/changes/adopt-conformance-test-vectors/tasks.md:21) instead permits each case to be either run **or** disabled with a reason and bead. The gate could therefore pass with cases disabled before any attempt, despite the stated scope.

**Smallest fix:** Require an initial attempt of each of the 72 cases and retain its individual outcome, including parse or execution failures. After that characterization, a failing case may be fixed or disabled with its specific reason and bead.

### P2 — The frozen record cannot represent a passing rejection without a comparison

The revised [equivalence requirement](openspec/changes/adopt-conformance-test-vectors/specs/conformance-suite-harness/spec.md:33) describes “not compared” for a skipped row. But [ConformanceCaseRunner](xacml-conformance-tests/src/test/java/org/xacml4j/v20/ConformanceCaseRunner.java:143) can pass `IIA004` and `IIC012` after the expected static rejection and return before comparing a response. Both IDs occur in the 2.0 and 3.0 manifests.

**Smallest fix:** Record test outcome and comparison state separately: for example, `pass` plus `not compared: expected policy rejection`, including the phase and defect. Add equivalence controls for those four rows.

### P2 — The final move to `full` lacks an outcome gate

The [spec](openspec/changes/adopt-conformance-test-vectors/specs/conformance-suite-harness/spec.md:40) allows rows to move to `full` after the last equivalence check, one at a time, with the move recorded. Its own nested-code scenario demonstrates that legacy and full modes can produce different outcomes. No task requires the final 880 outcomes to be compared with the frozen baseline after those moves.

**Smallest fix:** Assign the mode transitions and a final per-row outcome comparison against the frozen record. Keep a row in its legacy mode or fix the underlying issue when a move changes its outcome; record any separately approved baseline change.

### P2 — JSON comparator controls are not assigned

[Task 2.4](openspec/changes/adopt-conformance-test-vectors/tasks.md:12) assigns `ResponseComparatorTest` before the JSON comparator exists. [Task 5.1](openspec/changes/adopt-conformance-test-vectors/tasks.md:31) creates that comparator and an end-to-end adapter, but does not assign JSON controls for the common comparison contract. The [evidence index](openspec/changes/adopt-conformance-test-vectors/evidence/README.md:8) nevertheless maps `ResponseComparatorTest` to JSON coverage.

**Smallest fix:** Extend or create the downstream comparator test in task 5.1. Exercise JSON typed values, reordered bags, duplicate multiplicity, comparison modes and multiple reported differences; then update the evidence mapping.

### P2 — No assigned check proves per-file provenance coverage

The new [provenance requirement](openspec/changes/adopt-conformance-test-vectors/specs/conformance-vector-provenance/spec.md:18) requires an origin and basis for every adopted file or demonstrably uniform group. [VectorAdoptionCheckTest](openspec/changes/adopt-conformance-test-vectors/tasks.md:13) is assigned only duplicate checks, while the descriptor test is assigned suite-level metadata and case-review checks. A file without a mapped basis has no named structural negative control.

**Smallest fix:** Assign a completeness check that rejects an adopted file lacking an origin and basis record or membership in a documented group. Test a missing and a partially covered group. Keep human review responsible for whether the stated basis actually applies.

### P3 — D9 still claims unverified upstream equality

[D9](openspec/changes/adopt-conformance-test-vectors/design.md:123) says removing the header returns the files to upstream content, then correctly limits equality claims to files checked against an upstream copy. The first claim remains unsupported for files without that comparison.

**Smallest fix:** Change the heading and unconditional sentence to say the project header is removed. Retain the conditional upstream-hash check. The maintainer’s exception already authorizes the specified removal.

This was a static, read-only review. No files were changed; no Maven, tests, benchmarks, downloads, Git writes or `br` writes were run.

**Verdict: REJECT.** The legacy-model path and the requirement to attempt all 72 cases need correction before plan approval.
