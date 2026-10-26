# Highest-model production-entrypoint red tests

Delegated test writer: Codex GPT-6, task `policy4j-971.2.3` claimed by the
conductor. Only the synthetic desktop boundary was injected; the production
CLI parser, storage, mirrored receipts, and file locks ran normally.

`SUPPORTED`: Python 3.14.7 unittest ran all 34 execution contract test methods
in 3.616 seconds. The original 31 methods passed. The three added methods
failed with 36 failed subtests, zero errors, and zero skips. Exit status was 1.
The full normalized output is `execution-red.txt`; `sha256.txt` retains the
exact test file, unchanged pre-implementation orchestrator, and output hashes.
JDK, Maven, XACML conformance, and JMH are not applicable to this Python
desktop-workflow contract.

The failing methods are exactly:

- `test_highest_research_model_verified_before_input`: both provider frontends
  must retain the highest enabled Research-compatible selection in durable
  `job.json` and its ledger mirror before `enter`, include the same selection
  in the entry payload, and preserve exact evidence in the submitted receipt.
  The fake menu contains stronger disabled and incompatible choices, so menu
  order and an unavailable higher rank cannot substitute for capability order.
- `test_model_unavailable_or_unverifiable_blocks_submission`: 13 cases for
  each provider cover absent evidence, unsupported selection, partial choices
  or ranking, missing ranking basis, uncharacterized version, provider mismatch,
  unverified selection or mode verification, lower or Auto selection, no enabled
  compatible model, and an unranked enabled model. Each requires a `MODEL_*`
  reason with no input or send event.
- `test_model_change_before_input_blocks_submission`: four changes for each
  provider expose a lower model, unverified selection, missing immediate input
  verification, or incomplete choices during a fresh `inspect`. Each must stop
  before `enter` and `send`.

## Boundary contract for the implementation lane

`prepare` returns `model_evidence` with boolean `selection_supported`,
`choices_complete`, `ranking_complete`, `selection_verified`, and
`verified_after_mode`; public `ranking_basis`, `ranking_provider`,
`ranking_version`, and `selected_model`; and `available_choices` entries with
`name`, boolean `enabled` and `research_compatible`, and characterized integer
`rank` (higher is stronger). The fake highest selectable model is
`fixture-high`; the fake lower model is `fixture-low`.

After preparation the real workflow calls `inspect` with
`purpose: model-pre-input`, `model_policy: highest-available`, and the exact
`selected_model`. The boundary returns fresh model evidence with
`verified_before_input: true`. The workflow validates this fresh result before
input, then durably records both verification flags, exact available choices,
ranking basis, and an evidence `timestamp` no later than the input intent.
Top-level receipt and entry payload fields are `selected_model` and
`model_policy`. Read-only status must preserve the recorded model evidence.

The positive fixture explicitly carries a distinct unknown managed worker:
`worker_model: {kind: provider-managed, selected_model: null, verified: false}`.
That metadata must be retained separately and does not block selection of the
strongest selectable frontend model. `engine_kind: selected-model` identifies
the selectable frontend contract, not an independently managed worker.
Model side effects are represented as
`restoration: {required: true, original_model: fixture-low}`.

Residual limit: this run proves that the current production implementation
lacks the new selection guards and establishes their executable contract.
It does not run native desktop UI, select an actual model, or submit real
research questions. It is red evidence, not live acceptance.
