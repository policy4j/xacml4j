<!-- Plan re-review RVT2 of adopt-conformance-test-vectors, Codex GPT-6.1 Terra, read-only; report from the NTM pane, paths made repository-relative. -->

## RVT1 dispositions

| Finding | Status | Review |
| --- | --- | --- |
| 1. Comparator module | **Resolved** | [D3](openspec/changes/adopt-conformance-test-vectors/design.md:42) moves the entry point to `xacml-test` main code. The JSON dependency remains a separate issue below. |
| 2. Model loses fields | **Partly resolved** | [D3](openspec/changes/adopt-conformance-test-vectors/design.md:45) selects lossless JAXB and JSON representations, but does not reconcile that rule with the existing public model-only entry point or legacy field sets. |
| 3. Equivalence gate too early | **Resolved** | [Tasks 2.1–2.6](openspec/changes/adopt-conformance-test-vectors/tasks.md:9) require a frozen record and repeat the check after comparator replacement and before folding in the old runners. Compatibility details remain below. |
| 4. JSON envelopes and validation | **Partly resolved** | [D10](openspec/changes/adopt-conformance-test-vectors/design.md:120) requires published envelopes, but the validation rule mishandles expected-invalid documents and does not characterize actual response serialization. |
| 5. Patch redistribution basis | **Partly resolved** | [D7](openspec/changes/adopt-conformance-test-vectors/design.md:94) and task 8.2 defer the patch and derived OASIS JSON, but the proposal still promises them. |
| 6. Duplicate rule | **Resolved** | The [provenance spec](openspec/changes/adopt-conformance-test-vectors/specs/conformance-vector-provenance/spec.md:23) requires whole-case equality and a shared-file negative control. |
| 7. Case kinds | **Resolved** | The [harness spec](openspec/changes/adopt-conformance-test-vectors/specs/conformance-suite-harness/spec.md:3) defines rejection and validation kinds and a decision-only mode. |
| 8. Special verbs | **Resolved** | [D4](openspec/changes/adopt-conformance-test-vectors/design.md:54) adds structured expectations and fixture guards; task 2.3 ports the existing guard tests. |
| 9. PIP condition | **Resolved** | [D5](openspec/changes/adopt-conformance-test-vectors/design.md:64) represents the subject condition; task 2.3 assigns positive and negative tests. |
| 10. Named tests and review evidence | **Resolved** | [Tasks 2.2–2.5](openspec/changes/adopt-conformance-test-vectors/tasks.md:10) assign the named tests and guard migration; [D8](openspec/changes/adopt-conformance-test-vectors/design.md:101) adds per-case review records. The evidence index itself is stale. |
| 11. RSA-header option | **Resolved by maintainer decision** | [D9](openspec/changes/adopt-conformance-test-vectors/design.md:108) records the explicit, narrow exception and RVT1’s objection. No further approval is needed for that specified future edit. |
| 12. Later 2.0 index | **Resolved** | [Task 8.2](openspec/changes/adopt-conformance-test-vectors/tasks.md:46) assigns the comparison and disposition before any later increment. |

## New and remaining findings

### P1 — The public model comparator cannot satisfy D3’s wire-only contract

[D3](openspec/changes/adopt-conformance-test-vectors/design.md:44) says all three public utilities delegate to the format-selected, lossless entry point and that `ResponseContext` is not a comparison representation. But [XacmlPolicyTestSupport.assertResponse](xacml-test/src/main/java/org/xacml4j/v30/XacmlPolicyTestSupport.java:174) accepts only two `ResponseContext` objects. It receives no format and cannot recover repeated obligations or categories already merged by [Result](xacml-core/src/main/java/org/xacml4j/v30/Result.java:279).

**Smallest fix:** Specify a legacy model overload that preserves that public method’s existing comparison semantics, alongside the lossless wire comparators used by the new runner. Assign tests for existing model callers, including distinctions their current comparator does and does not check.

### P1 — Legacy 3.0 comparison and new `full` comparison have different field sets

The [harness spec](openspec/changes/adopt-conformance-test-vectors/specs/conformance-suite-harness/spec.md:33) requires the same per-field results, while [full comparison](openspec/changes/adopt-conformance-test-vectors/specs/conformance-suite-harness/spec.md:50) includes nested status codes. The existing [3.0 comparator](xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java:202) checks only the top-level code. Two responses with the same top-level code and different nested codes therefore pass the old method but must fail `full`. D3’s statement that the existing JAXB comparison already checks nested codes is too broad.

A read-only scan of the 487 active 3.0 expected XML responses found no nested status codes, status details or status messages. That does not establish runtime equivalence or the behavior of other callers.

**Smallest fix:** Define explicit legacy field sets or modes for each old comparator through the equivalence gate, and specify when a case may move to `full`. Add a control that the old 3.0 mode accepts a nested-code difference while `full` rejects it. Define a “not compared” result for skipped rows in the frozen record.

### P1 — Expected-invalid JSON is classified as a product defect

The [JSON requirement](openspec/changes/adopt-conformance-test-vectors/specs/conformance-suite-harness/spec.md:96) says *any* document `xacml-json` cannot read must receive a defect bead. Yet the new case kinds explicitly include validation documents expected to be invalid, and the selected AuthzForce material includes [15 invalid requests and 11 invalid responses](openspec/changes/adopt-conformance-test-vectors/research/tv1-opus-java-engines.md:124). Correct rejection would be classified as a defect under the literal requirement.

**Smallest fix:** Classify outcomes against the row’s expected validity: rejection of an invalid document passes; rejection of a valid one or acceptance of an invalid one is a defect. Add valid and invalid controls for both request and response validation. Record the applicable JSON Profile version and distinguish its rules from AuthzForce extensions before adopting each expectation.

### P1 — The proposal still describes a different change

The [proposal](openspec/changes/adopt-conformance-test-vectors/proposal.md:15) still calls for a model-level comparator in the conformance module; [its source list](openspec/changes/adopt-conformance-test-vectors/proposal.md:27) promises patch #1 and XML-derived JSON; and [its impact section](openspec/changes/adopt-conformance-test-vectors/proposal.md:73) says no existing vendored files are edited. These conflict with D3, D7, D9 and task 3.3. It also omits the 72 additional cases and the stronger field-equivalence gate.

**Smallest fix:** Update the proposal’s changes, verification and impact to match the revised design: legacy and wire comparison paths, deferred sources, the 72 cases, the authorized 34-file exception, and the changed `xacml-test` artifact. Align its download rule with D7’s separate permission for each source, file and size.

### P2 — JSON evaluation does not specify how the actual response reaches the wire comparator

[Task 5.1](openspec/changes/adopt-conformance-test-vectors/tasks.md:31) characterizes reading envelopes, but JSON evaluation also needs an actual PDP response in the profile format. The existing [JSON response marshaller](xacml-json/src/main/java/org/xacml4j/v30/marshal/json/JsonResponseContextMarshaller.java:66) emits an inner `Result` object directly, or a `Response` object containing that object, whereas D10 expects a top-level `Response` array. The request adapter also [expects `Category`](xacml-json/src/main/java/org/xacml4j/v30/marshal/json/RequestContextAdapter.java:52), leaving profile category forms uncharacterized.

**Smallest fix:** Add an end-to-end task and controls using published request forms and the public response marshaller. Compare the actual wire document with the published expectation; record unsupported valid forms as separate product defects and disable affected cases until fixed. Do not construct a replacement envelope in the harness and count it as wire-format conformance.

### P2 — The JSON comparator’s compile dependency and implementation task are unspecified

[D3](openspec/changes/adopt-conformance-test-vectors/design.md:45) puts the JSON-document comparator in `xacml-test` main code. That module has no direct compile dependency on `xacml-json` or Gson in its [POM](xacml-test/pom.xml:12); `xacml-pdp` declares `xacml-json` at [runtime scope](xacml-pdp/pom.xml:24). A test dependency added downstream in the conformance module cannot supply the upstream main compile classpath. Task 2.4 assigns the JAXB comparator but does not explicitly assign the JSON-document comparator.

**Smallest fix:** Choose and record the JSON comparison implementation and its compile dependency, or define an injected format strategy supplied downstream. Assign its implementation and tests to a task, and update the artifact-impact statement.

### P2 — Adding the 72 cases needs an explicit inventory migration

[Task 3.3](openspec/changes/adopt-conformance-test-vectors/tasks.md:21) adds the cases, but the existing [inventory](xacml-conformance-tests/src/test/java/org/xacml4j/v20/ConformanceManifest.java:176) excludes their directory and its [test](xacml-conformance-tests/src/test/java/org/xacml4j/v20/ConformanceManifestTest.java:140) pins 487 `oasis30` rows. Leaving those checks intact rejects the added rows; dropping them without replacement can let a case disappear. A read-only filename comparison found 72 deprecated requests and no case-ID overlap with the current `oasis30` manifest.

**Smallest fix:** Assign migration of the inventory and its negative tests. Require a bijection between the 72 deprecated request files and rows, preserve the existing active inventory checks, and record an attempted outcome or a specific disabled reason and bead for each case.

### P2 — Regenerating the RSA ledger can bless unrelated fixture drift

[D9](openspec/changes/adopt-conformance-test-vectors/design.md:110) limits the authorized change to removing the project header from 34 files. However, the specified [manifest generator](xacml-conformance-tests/src/test/java/org/xacml4j/v20/GenerateVendoredFixtureManifest.java:44) recomputes the ledger for **all** conformance resources and JAXB schemas. A post-regeneration integrity test alone would accept unrelated drift incorporated by that regeneration. D9 also asserts that header removal restores upstream bytes even when upstream hashes are unavailable.

**Smallest fix:** Before editing, verify and retain the existing ledger and fixture baseline. Prove that exactly the 34 authorized files changed and each differs only by removal of the identified comment; require the ledger delta to contain only those 34 entries. Claim upstream-byte equality only for files independently verified against authorized upstream downloads.

### P2 — Comparison semantics and diagnostics need executable controls

The [lossless comparison requirement](openspec/changes/adopt-conformance-test-vectors/specs/conformance-suite-harness/spec.md:50) names fields but does not specify their equality rules. Existing tests require, for example, integer `1` and `01` to compare equal and value bags to be unordered while retaining duplicates ([Xacml30TestUtilityTest](xacml-test/src/test/java/org/xacml4j/v20/Xacml30TestUtilityTest.java:91)). The spec also requires a mismatch to name **every** differing field, while the current comparator uses sequential assertions that stop at the first mismatch.

**Smallest fix:** Specify the legacy typed-value and collection semantics, then define applicable semantics for new full and JSON comparisons. Assign tests for equivalent lexical forms, reordered bags, changed multiplicity, and a response with two simultaneous differences whose diagnostic names both fields and the case.

### P2 — Source-level licenses do not establish rights in every selected file

The [adoption spec](openspec/changes/adopt-conformance-test-vectors/specs/conformance-vector-provenance/spec.md:18) requires a documented basis but gives the source project’s license as its example, while D7 lists candidate sources by repository license. The research qualifies those grants: [TV4](openspec/changes/adopt-conformance-test-vectors/research/tv4-astra-fingerprints-licenses.md:21) distinguishes AuthzForce-owned cases from inherited OASIS material; [TV2](openspec/changes/adopt-conformance-test-vectors/research/tv2-terra-profiles-other-engines.md:63) calls for checking generated-content terms for the Curity policy.

**Smallest fix:** Require the adoption record to identify the origin and applicable redistribution basis of each selected file or demonstrably uniform file group, including inherited and generated material. Defer any file whose applicable basis remains unclear.

### P3 — The evidence index still maps the old plan

The [evidence index](openspec/changes/adopt-conformance-test-vectors/evidence/README.md:5) maps JSON to task 6 and the adoption checker to tasks 4–8, omits the revised field-equivalence checks and does not identify evidence for the RSA exception or 72 cases. Task 9.2 postpones the requirement-to-test-to-evidence audit until closeout.

**Smallest fix:** Update the index now to map the revised requirements, assigned tests and gates to their actual task numbers, including the exception and deprecated-case inventory evidence.

The maintainer’s three answers resolve the former scope questions. Future external downloads still require the distinct permissions stated in D7. This was a static, read-only review; no files were changed and no Maven, tests, benchmarks, downloads, Git writes or `br` writes were run.

**Verdict: REJECT.** The comparator and JSON requirements still conflict with existing interfaces or with their own validation cases, and the proposal must describe the change being approved.
