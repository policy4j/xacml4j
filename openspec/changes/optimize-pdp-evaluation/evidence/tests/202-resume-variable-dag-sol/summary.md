# P-CPU2 candidate: bounded response-capability variable walk

Result: candidate ready for conductor integration and independent review.
Agent `cod_2`; bead `policy4j-cwtu.42.14.1`; claimed actor `ntm-sol-g1`.
Assigned model: Codex GPT-6.1 Sol; assigned effort: max. Runtime model identity
was not independently surfaced by this session.
Base: `759d90a6c75510d55127d8e22870f9edd985c991`.
Change: `optimize-pdp-evaluation`, task 42.14, P-CPU2 prerequisite.

## Correction and owned files

- `xacml-core/src/main/java/org/xacml4j/v30/policy/ResponseWriters.java`:
  each walk owns a lazily allocated identity set for exact engine
  `VariableDefinition` nodes. Recursive arguments and target matches share
  that walk. Repeated definitions are skipped; a writing path still stops
  the entire walk immediately. Custom definitions are classified as possible
  response writers before consulting the memo. No memo survives a walk.
- `xacml-core/src/main/java/org/xacml4j/v30/policy/Policy.java` and
  `PolicySet.java`: each existing prepared-child gate reads the stored
  `builtInAlgorithm` flag instead of repeating class-set membership.
- `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/NestedResponseStagingPdpTest.java`:
  16-level shared-DAG outcomes, nested obligation/advice order over repeated
  decisions with indexing off/on, winning/losing custom-definition response
  staging, custom algorithm fallback with indexing enabled, and an independent
  deterministic traversal probe. No host-load timing threshold.

## Executed verification

The accepted builds used the shared serialized runner with explicit
`--maven /opt/homebrew/bin/mvn`, offline mode, disabled license updates,
and `clean test`. The green and module logs identify Apache Maven 3.10.0
and Oracle JDK 25.0.4. Existing compiler/deprecation/Unsafe warnings remain
in the raw output.

| Run / retained raw output | Result |
| --- | --- |
| `baseline-outcomes-maven310-clean.log` | CHARACTERIZATION: all 6 new outcome cases pass before the production correction; zero failures/errors/skips. This was not a red behavior result. |
| `red-traversal-probe-maven310-clean.log` | Initial probe failed at 4,098 cumulative reads, including 2 fixture-construction reads. Retained for audit; the measurement was isolated before correcting production. |
| `red-traversal-isolated-maven310-clean.log` | Meaningful RED: 1 test, 1 assertion failure, 0 errors/skips. A 12-level DAG with 13 exact definitions reads its shared leaf 4,096 times in one real response-capability walk; expected 1. Setup reads are excluded. |
| `green-focused-maven310-clean.log` | GREEN: full focused class, 20 tests, zero failures/errors/skips. Probe reads are 1 after one walk and 2 cumulatively after two independent walks. |
| `module-gate-maven310-clean.log` | GREEN: core 7,357 (2 skipped), XML 254 (0 skipped), JSON 24 (1 skipped), PDP 814 (3 skipped). Total 8,449; zero failures/errors; 6 skipped. JAXB has no tests. |

The probe invokes the real production walker. Its inline spy preserves the
exact `VariableDefinition` class and real getter; it measures traversal
work separately from the response outcome tests. The outcomes and probe
were recorded before production edits. The same corrected probe and owned
production files passed the focused class and affected module suites.

Earlier `baseline-outcomes.log` stopped at dependency resolution under
the old default Maven path. `baseline-outcomes-offline-settings.log`
stopped at a test compilation error (a missing test-fixture `.build()`,
corrected before the meaningful baseline). Neither is regression evidence.
The canceled old-settings retry did not produce another build log.
The temporary settings file was removed; the confirmed Maven 3.10 runs
used no custom settings, downloads, POM edits, or cached-origin edits.

Scoped ast-grep scan and strict OpenSpec validation passed in the
implementation session. Final source diff review and tracked-file
fingerprints confirm only the four owned tracked files changed. Final
status is retained in `final-state.json`; `before-state.json` and
`red-ready-state.json` identify the original and definitive-red sources.
The current production diff equals the diff captured at module-gate start;
the test SHA-256 still matches the definitive-red/green test source.
No builds were run during finalization.

The preserved older `batch2/cc_1` candidate was inspected only.
Its complete tracked diff SHA-256 remains
`7140db828fda6f4c1c7512205ab4d9dc492e2c2fe11f2a1bc30e2dcc2aed9153`;
its four corresponding source hashes are unchanged. All other tracked
paths, including `Rule.java`, match the original fingerprint.

## Limits

Performance, allocation measurements, and JMH: **UNRUN**, reserved for the
conductor's quiet-host phase. The traversal count establishes the repeated
work defect and its correction; it is not a latency or throughput result.
Combined reactor, conformance, and independent candidate review remain
conductor gates. This candidate does not claim acceptance of the complete
D47 change. Recursion depth and arbitrary expression graphs outside shared
variable definitions were not redesigned.

No task ticks, Beads updates/closure, staging, commits, or branch changes
were made. The conductor retains task and evidence-index ownership.

## SHA-256

| Repository-relative file | SHA-256 |
| --- | --- |
| `xacml-core/src/main/java/org/xacml4j/v30/policy/ResponseWriters.java` | `1780fa60f86fa433d9fef565ab1ca76a69879c4e7ac1455098096ae2feec4042` |
| `xacml-core/src/main/java/org/xacml4j/v30/policy/Policy.java` | `8b7afdbb6cfbed64489a4994e3a691c60104aadd0422ce281783f7c78d287953` |
| `xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySet.java` | `02e5e984a195c1021939a971694381c6ad17eb0c3bb41044d921c75f254cabda` |
| `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/NestedResponseStagingPdpTest.java` | `fe3380aed3aea8e95b72975a15f0af3fc4bb4561c3a3f48fe1a2d4e3612b4219` |
| `openspec/changes/optimize-pdp-evaluation/evidence/tests/202-resume-variable-dag-sol/baseline-outcomes.log` | `8581f749eee7de5103b33c82b8c9acbc2a9a0a37c75fcb6a3a6ff18f65d9cfd2` |
| `openspec/changes/optimize-pdp-evaluation/evidence/tests/202-resume-variable-dag-sol/baseline-outcomes-offline-settings.log` | `a9338d4070c21586189b091c24631ada7c2de893292842a2be1f26b9e637b6ef` |
| `openspec/changes/optimize-pdp-evaluation/evidence/tests/202-resume-variable-dag-sol/baseline-outcomes-maven310-clean.log` | `49baec45619adc01e70d41eb113398dd61147ba1ab937523c72e26f0211a045a` |
| `openspec/changes/optimize-pdp-evaluation/evidence/tests/202-resume-variable-dag-sol/red-traversal-probe-maven310-clean.log` | `99a82b5bbae0f44eb00a926d314fd369793cd0e2bc4817146cae81bae40ac2c1` |
| `openspec/changes/optimize-pdp-evaluation/evidence/tests/202-resume-variable-dag-sol/red-traversal-isolated-maven310-clean.log` | `2c579c5ea88f211c8203663f09e5aa5bdc7df6484f7ef1fce6e992f8f80cd966` |
| `openspec/changes/optimize-pdp-evaluation/evidence/tests/202-resume-variable-dag-sol/green-focused-maven310-clean.log` | `802e7e059da5e199ed5c9ee504192a79ce523a66a1f7b9a53aba705294cf27bf` |
| `openspec/changes/optimize-pdp-evaluation/evidence/tests/202-resume-variable-dag-sol/module-gate-maven310-clean.log` | `8109e48790bf08de9b80125e018e3bbcfce1608cbd5ebd1066edacb3288f01f9` |
