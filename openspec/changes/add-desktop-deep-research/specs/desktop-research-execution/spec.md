## ADDED Requirements

### Requirement: Select the highest available research model

Before entering a question, the workflow SHALL select and verify the highest
capability selectable conversation model available to the signed-in account and
compatible with the requested Research mode. It SHALL use a characterized provider/version ranking
and a complete observation of enabled choices, rather than infer capability from
numeric versions, menu order, an Auto label, or price. It SHALL reverify the
selected model after choosing Research mode and immediately before input.

The receipt SHALL record the visible model name, model policy
`highest-available`, ranking basis, available-choice evidence and selection
verification. A selectable conversation model and a separately managed research
engine SHALL be recorded distinctly; changing a chat picker SHALL NOT be claimed
to select an independently managed research engine. An undisclosed worker model
SHALL be recorded as provider-managed and unverified. Unsupported conversation-model selection,
incomplete choices, unknown ranking or an unverified selection SHALL block
submission with a specific model-selection reason. No upgrade or subscription
change is authorized. Selection side effects SHALL be restored safely or
reported pending. Existing pre-steering reports SHALL NOT be represented as
verified highest-model reports.

#### Scenario: The strongest enabled compatible model is selectable
- **GIVEN** complete observed choices and a characterized capability ranking for the selected provider/version
- **WHEN** a Research question is prepared
- **THEN** the strongest enabled compatible choice is selected, verified after mode selection and recorded before input
- **AND** the final receipt preserves the exact visible model and ranking basis
- **Test:** `test_highest_research_model_verified_before_input`; live `highest_model_selection` for both providers.

#### Scenario: The model cannot be selected or ranked completely
- **GIVEN** a missing conversation-model selector, incomplete choices, unknown ranking or an unverified selected model
- **WHEN** a submission is requested
- **THEN** it blocks with a specific reason without prompt entry or a silent lower-model fallback
- **Test:** `test_model_unavailable_or_unverifiable_blocks_submission`.

#### Scenario: Research mode changes or resets the model
- **GIVEN** a selected model and preparation that changes or resets it
- **WHEN** the complete input guard is checked
- **THEN** input stops until the highest compatible selection is reverified
- **AND** the prior sample's unrecorded model is not used as proof
- **Test:** `test_model_change_before_input_blocks_submission`.

### Requirement: Verify provider readiness and scope

The workflow SHALL check the selected desktop application's installed identity,
version, local permissions, usable window, research controls, and account
readiness before submission. It SHALL report unavailable or incomplete coverage
and SHALL NOT silently substitute ordinary chat, an API, or a web browser.

#### Scenario: Readiness succeeds
- **GIVEN** the selected desktop app exposes usable research controls
- **WHEN** readiness is checked
- **THEN** the result identifies the app, version, permissions, and research-mode evidence
- **AND** no prompt is submitted by the readiness check
- **Test:** `test_doctor_reports_observed_capabilities`; live `provider_readiness` for both providers.

#### Scenario: Research controls or permissions are unavailable
- **GIVEN** a required permission, window, sign-in, or research control is missing
- **WHEN** a submission is requested
- **THEN** the job records the specific blocked state and no prompt is sent
- **Test:** `test_start_blocks_without_research_readiness`.

### Requirement: Submit only to an identified research conversation

The workflow SHALL use a new dedicated conversation, preserve existing drafts,
verify target focus and selected research mode before input, and associate the
submitted question with a unique local job marker and observed conversation
identity. It SHALL record submission intent before sending and SHALL distinguish
an attempted send from an observed provider receipt.

#### Scenario: Observed research submission
- **GIVEN** readiness succeeds and a bounded question is authorized
- **WHEN** the workflow submits it in a dedicated research conversation
- **THEN** the saved receipt matches the question marker and observed conversation
- **AND** the selected mode is ChatGPT Deep Research or Claude Research as requested
- **Test:** `test_start_requires_observed_receipt`; live `research_submission` for both providers.

#### Scenario: Draft or focus changes before input
- **GIVEN** an existing draft or changed foreground target is detected
- **WHEN** the workflow is preparing or entering a question
- **THEN** it preserves the draft, stops input, and records an actionable interruption
- **Test:** `test_focus_change_preserves_draft_and_stops_input`.

### Requirement: Handle provider interaction states explicitly

The workflow SHALL distinguish preparation, submission uncertainty, clarifying
questions, proposed plans, active research, provider completion, quota/error
states, and incomplete collection. It SHALL follow a proposed plan only within
the authorized question and source scope, and SHALL stop for questions or scope
expansion that cannot be answered from the authorized inputs.

#### Scenario: Plan remains within the authorized scope
- **GIVEN** the provider proposes a plan within the question and allowed sources
- **WHEN** the workflow evaluates the plan
- **THEN** it can begin research and records the observed transition
- **Test:** `test_authorized_plan_can_start_research`.

#### Scenario: Provider needs an unprovided choice
- **GIVEN** the provider asks a question or adds a source outside the authorized scope
- **WHEN** status is observed
- **THEN** the workflow records the pending interaction and does not invent a reply
- **Test:** `test_unresolved_question_or_plan_scope_stops`.

#### Scenario: Provider quota is exhausted
- **GIVEN** the provider shows a quota or subscription limitation
- **WHEN** status is observed
- **THEN** the job records that provider outcome without repeated submission or upgrade actions
- **Test:** `test_quota_state_does_not_resubmit`.

### Requirement: Resume observations without duplicate submission

The workflow SHALL persist jobs atomically and resume status checks against the
same observed conversation. An observation timeout, script exit, or missing
local process SHALL NOT itself establish provider failure or permit resubmission.
After an uncertain send, it SHALL reconcile provider state before another send.

#### Scenario: Wait expires while research continues
- **GIVEN** an identified research job remains active
- **WHEN** a bounded wait expires and the command is later resumed
- **THEN** it re-observes the same job and retains the recorded provider state
- **AND** it does not send the question again
- **Test:** `test_wait_timeout_preserves_job_and_resume_does_not_submit`; live `timeout_resume`.

#### Scenario: Process stops after sending but before receipt persistence
- **GIVEN** submission intent was saved and the process stops at an uncertain send
- **WHEN** the job is resumed
- **THEN** matching provider evidence recovers the receipt or the job remains uncertain
- **AND** no automatic duplicate is submitted
- **Test:** `test_crash_after_send_requires_reconciliation`.

### Requirement: Serialize desktop actions and isolate job data

The workflow SHALL allow only one mutating desktop action at a time across
processes and checkouts. Batch execution SHALL queue jobs in declared order and
retain an independent outcome per job. Inspections and evidence SHALL be
limited to the selected job, and default diagnostics SHALL redact conversation
text, prompts, account data, and unrelated clipboard contents.

#### Scenario: Concurrent commands contend for desktop ownership
- **GIVEN** a process owns an active desktop action
- **WHEN** another process requests input or collection
- **THEN** it waits within a bound or returns a clear busy outcome without GUI input
- **Test:** `test_desktop_lock_serializes_processes`.

#### Scenario: One batch provider is blocked
- **GIVEN** a batch targets both providers and one lacks research availability
- **WHEN** the batch executes
- **THEN** the other provider can proceed and each job keeps its own result
- **AND** a partial batch is not reported as fully collected
- **Test:** `test_batch_preserves_per_provider_outcomes`.

#### Scenario: Diagnostic inspection sees unrelated data
- **GIVEN** the selected app contains unrelated chats or clipboard contents
- **WHEN** a readiness check or diagnostic inspection runs
- **THEN** logs retain only capability flags, allowlisted controls, and job metadata
- **Test:** `test_default_diagnostics_redact_unrelated_data`.

### Requirement: Constrain and verify public source scope

The workflow SHALL accept only public-web sources in this capability, verify
the actual source selection before input, and block if private sources cannot
be excluded or selection cannot be observed. Prompt wording alone SHALL NOT
prove source isolation. Per-conversation persistent toggle side effects SHALL
be recorded and safely restored; account-wide source settings SHALL NOT change.

#### Scenario: Public-only selection is verified
- **GIVEN** the provider exposes a verifiable public-only selection
- **WHEN** input is prepared
- **THEN** the receipt retains source-scope evidence before any prompt entry
- **Test:** `test_public_source_selection_is_in_receipt`; live `source_scope_verified` for both providers.

#### Scenario: Private sources cannot be excluded
- **GIVEN** an unrequested private source is enabled and cannot be excluded
- **WHEN** input is requested
- **THEN** the job blocks without prompt entry
- **Test:** `test_start_blocks_on_unconstrainable_private_source`.

#### Scenario: Source selection is unobservable
- **GIVEN** actual source selection cannot be observed completely
- **WHEN** input is requested
- **THEN** the job blocks without treating prompt wording as proof
- **Test:** `test_start_blocks_when_source_scope_unverifiable`.

#### Scenario: Per-conversation toggles have persistent effects
- **GIVEN** an authorized per-conversation selection temporarily changes a toggle
- **WHEN** the job no longer needs that selection
- **THEN** its prior state is restored safely or the pending restoration is reported
- **Test:** `test_source_selection_side_effects_restored`.

### Requirement: Rebind the existing conversation safely

The workflow SHALL bind actions to a stable locally observed conversation id
or a verified session plus the job marker in its submitted user turn. After
relaunch it SHALL accept exactly one verified match. An assistant-echoed marker
or a window id alone SHALL NOT permit an action. Sharing/publication or
account-wide export SHALL NOT be used to obtain a binding.

Before first submission, input SHALL require a verified newly created dedicated
empty-composer receipt with target/caller/mode/source guards. After entry it
SHALL verify the complete owned draft. Possible Send SHALL suspend draft input
authority until reconciliation. Draft binding SHALL NOT authorize completion
or collection and SHALL be upgraded only by verified submitted-job evidence.

#### Scenario: A new conversation has no submitted turn yet
- **GIVEN** a new dedicated empty composer is verified or an unrelated nonempty draft is visible
- **WHEN** first input and the later receipt are processed
- **THEN** only the verified dedicated draft can accept input, possible Send disables further draft input, and completion/collection require submitted-job binding
- **Test:** `test_pre_send_binding_bootstrap_and_upgrade`.

#### Scenario: The job cannot be relocated
- **GIVEN** reopening the app yields zero or ambiguous submitted-user-turn matches
- **WHEN** observation is resumed
- **THEN** it reports UNOBSERVABLE, preserves provider state and never resubmits
- **Test:** `test_missing_or_ambiguous_rebind_preserves_state`.

#### Scenario: The same window shows another conversation
- **GIVEN** the window is now displaying a different conversation
- **WHEN** an action or collection is requested
- **THEN** the workflow rejects it until the bound job is verified
- **Test:** `test_wrong_conversation_blocks_mutation_and_collection`; live `conversation_rebind_after_reopen` for both providers.

### Requirement: Make requests and batch reruns idempotent

The workflow SHALL allocate and expose durable job ids before desktop mutation,
resume a stable request/batch id instead of submitting it twice, and consult a
ledger shared across checkouts. A new request SHALL NOT bypass an unresolved
uncertain provider job. Concurrent job updates SHALL preserve every acknowledged
state transition through serialization and revision checking.

#### Scenario: The caller loses the id after input
- **GIVEN** a process exits after possible submission before the caller retains its id
- **WHEN** the same request is retried
- **THEN** it returns/reconciles the original job without another submission
- **Test:** `test_start_retry_after_killed_start_does_not_resubmit`.

#### Scenario: A new id tries to bypass uncertainty
- **GIVEN** a provider has an unresolved possibly sent job
- **WHEN** another request is made with a different id or from another checkout
- **THEN** it blocks until the existing job is reconciled
- **Test:** `test_provider_uncertainty_blocks_across_checkouts`.

#### Scenario: A batch is rerun after interruption
- **GIVEN** the batch manifest already maps entries to durable jobs
- **WHEN** that batch is rerun
- **THEN** it resumes each existing job and keeps an independent result per entry
- **Test:** `test_batch_rerun_resumes_without_resubmission`.

#### Scenario: Observation and collection update concurrently
- **GIVEN** two processes update the same job
- **WHEN** those updates complete
- **THEN** no acknowledged provider/collection transition is lost
- **Test:** `test_concurrent_job_updates_keep_state`.

### Requirement: Own interactions and observation transitions explicitly

Only authorized submission, answer, and plan-start commands SHALL reach the
input boundary. The invoking agent SHALL evaluate only the verified job's plan
as untrusted data and record its exact hash before beginning it. Unknown choices
SHALL remain pending. Every blocked/failed outcome SHALL retain whether input
may have been sent; quota and observed cancellation SHALL have explicit reasons.

#### Scenario: All state transitions are exercised
- **GIVEN** each permitted event and state from the design transition table
- **WHEN** its command is invoked with valid or invalid guards
- **THEN** only the documented transition occurs and unauthorized commands never input
- **Test:** `test_transition_table_and_command_ownership`.

#### Scenario: Input entry is interrupted or truncated
- **GIVEN** a multiline question is entered into the dedicated composer
- **WHEN** entry is interrupted, truncated, or focus changes
- **THEN** intent was synced before first input and no unverified Send occurs
- **Test:** `test_intent_and_multiline_entry_guard_send`.

### Requirement: Observe without taking user focus

Routine observation SHALL NOT activate, restore, navigate, scroll or open menus.
Those actions SHALL be treated as mutations within the whole transaction lock
and SHALL defer while local user input is active. Mutating calls SHALL verify
their target, job binding, focus and control immediately before input; unexpected
dialogs SHALL block. Whole-desktop capture SHALL NOT occur.

#### Scenario: Waiting while the job is hidden
- **GIVEN** the job cannot be observed without activation
- **WHEN** a routine wait poll runs
- **THEN** it returns UNOBSERVABLE without changing focus or provider state
- **Test:** `test_wait_polling_never_activates`.

#### Scenario: The user is active when a mutation is due
- **GIVEN** local user input is active
- **WHEN** a desktop mutation is due
- **THEN** it defers with no activation or input
- **Test:** `test_mutation_deferred_while_user_active`.

#### Scenario: Transaction steps could interleave
- **GIVEN** two processes attempt submission or clipboard transactions
- **WHEN** the first transaction owns desktop control
- **THEN** none of its verify/input/observe steps interleave with the second
- **Test:** `test_transaction_lock_is_cross_checkout_and_temp_independent`.

#### Scenario: A bridge precondition or dialog changes
- **GIVEN** the expected target/control/focus changes or an unexpected dialog appears
- **WHEN** input would otherwise occur
- **THEN** the workflow returns a guarded interruption without dismissing or acting on the dialog
- **Test:** `test_bridge_guard_schema_and_modal_handling`.

### Requirement: Distinguish trusted live proof from synthetic output

Receipts SHALL identify live versus synthetic boundaries, helper/profile identity
and build/source hashes. Synthetic output SHALL NOT satisfy a live completion
audit. Ambiguous app candidates, signature/version/account changes and inability
to exclude the invoking agent's own window SHALL block affected actions.

#### Scenario: Synthetic artifacts reach the acceptance audit
- **GIVEN** a test boundary produced a collected result
- **WHEN** live acceptance is audited
- **THEN** the synthetic stamp prevents acceptance as desktop proof
- **Test:** `test_synthetic_receipts_are_rejected_by_live_audit`.

#### Scenario: Target trust or caller isolation fails
- **GIVEN** signature, candidate, version/account continuity or caller-window isolation cannot be verified
- **WHEN** the workflow would act or continue observation
- **THEN** it blocks the affected action and preserves prior provider state
- **Test:** `test_target_trust_version_and_caller_isolation`.
