## ADDED Requirements

### Requirement: Select the surface explicitly

The workflow SHALL run a job on the surface that the caller names, `desktop`
or `browser`, and SHALL store that surface with the job. It SHALL NOT move a
job to another surface and SHALL NOT start a browser job because a desktop
job was blocked. A provider that has no desktop surface SHALL be accepted on
the browser surface only. A request id SHALL stand for one fixed combination
of surface, provider, source scope, question, kind and parent job; a retry
with the same id and another combination SHALL be rejected before any page
is opened.

#### Scenario: The caller names the browser surface
- **GIVEN** a request that names a provider and the browser surface
- **WHEN** the job is allocated and started
- **THEN** the job and every receipt record the browser surface
- **AND** no desktop application is inspected or driven for that job
- **Test:** `test_surface_is_recorded_and_never_substituted`; live `browser_surface_selected` for three providers.

#### Scenario: A job is resumed on another surface
- **GIVEN** a job that was allocated on one surface
- **WHEN** a command addresses that job with the other surface
- **THEN** the command is rejected and the job is unchanged
- **Test:** `test_job_cannot_change_surface`.

#### Scenario: A provider has no desktop surface
- **GIVEN** a provider that is supported in the browser only
- **WHEN** a request names it without the browser surface
- **THEN** the request is rejected before a job is allocated
- **Test:** `test_browser_only_provider_requires_browser_surface`.

#### Scenario: A request id is reused for another request
- **GIVEN** a request id that already stands for a job
- **WHEN** it is retried with another surface, parent job, kind or question
- **THEN** the retry is rejected as a conflict and no page is opened
- **Test:** `test_request_id_conflict_is_rejected_before_page_access`.

### Requirement: Keep the provider reserved until the outcome is known

A job SHALL reserve its provider on both surfaces from its first input
intent until an observation shows positively that the provider finished the
job or that nothing was sent. A challenge, a signed-out page, an
unrecognized interface, a driver failure or a timeout SHALL be recorded as
an observation result and SHALL NOT change the provider state or end the
reservation. While a provider is reserved, a new submission or follow-up to
that provider SHALL block on either surface.

#### Scenario: The provider is reserved on the other surface
- **GIVEN** a provider has a possibly sent or active job on the desktop surface
- **WHEN** a new request names the same provider on the browser surface
- **THEN** it blocks until the existing job is reconciled, with no page action
- **Test:** `test_provider_reservation_blocks_across_surfaces`.

#### Scenario: A challenge follows a possible send
- **GIVEN** a browser job whose prompt may have been sent
- **WHEN** the next observation meets a challenge, a signed-out page or a driver failure
- **THEN** the observation result is recorded, the provider state is unchanged, and a new request to that provider still blocks on both surfaces
- **Test:** `test_local_failure_after_possible_send_keeps_reservation`.

#### Scenario: The job is shown to be unsent or finished
- **GIVEN** a reserved provider
- **WHEN** an observation shows positively that nothing was sent, or that the provider finished the job
- **THEN** the reservation ends and a new request is admitted
- **Test:** `test_reservation_ends_only_on_positive_outcome`.

### Requirement: Use a dedicated browser session that the operator signed in

The workflow SHALL drive a browser profile that is dedicated to it and to one
provider, and SHALL NOT drive, copy or attach to the operator's own browser
profile. Signing in SHALL be done by the operator in a window that no driver
controls. The session SHALL be kept only by the browser in that profile. The
workflow's own components SHALL NOT request, receive, log, export or print
the value of a credential, a cookie or a session token, SHALL NOT type a
credential, SHALL NOT save a storage-state export and SHALL NOT copy the
profile. Every transaction SHALL act only on a page that the workflow opened
for it, after closing any other page of the profile. A signed-out session
SHALL block with a specific reason. A challenge SHALL be recognized as a
full-page interstitial, an unknown dialog or overlay, or a composer that
another element covers; it SHALL block and SHALL NOT be solved, skipped or
evaded. Text of a driver error SHALL NOT be forwarded into output or
receipts.

#### Scenario: The dedicated profile is signed out
- **GIVEN** the provider's page in the dedicated profile shows no signed-in account
- **WHEN** readiness is checked or a submission is requested
- **THEN** the outcome is a sign-in-required block and no prompt is entered
- **Test:** `test_browser_start_blocks_when_signed_out`; live `browser_readiness` for three providers.

#### Scenario: Sign-in is requested
- **GIVEN** the operator asks to sign in to a provider
- **WHEN** the sign-in command runs
- **THEN** the dedicated profile opens without a driver and the command sends nothing to the page
- **Test:** `test_login_opens_profile_without_driver_or_input`.

#### Scenario: The profile restores earlier pages
- **GIVEN** the dedicated profile opens with pages left from an earlier session
- **WHEN** a transaction starts
- **THEN** those pages are closed and every action goes to the page that the transaction opened
- **Test:** `test_transaction_owns_a_new_page_and_closes_others`.

#### Scenario: A challenge covers the page
- **GIVEN** the provider shows an interstitial, an unknown dialog or an overlay on an otherwise known page
- **WHEN** an action or an observation is due, headed or headless
- **THEN** the challenge is recorded and no control on that page is used
- **Test:** `test_challenge_overlay_blocks_without_bypass`.

#### Scenario: A session secret is requested from the boundary
- **GIVEN** any command on the browser surface
- **WHEN** the browser boundary is invoked
- **THEN** no cookie, storage or credential operation is requested and no such value appears in a receipt
- **Test:** `test_browser_boundary_never_touches_session_secrets`.

#### Scenario: The driver reports an error with page text
- **GIVEN** a driver error whose text contains page content or an address
- **WHEN** the command reports the failure
- **THEN** the output and the receipt carry a fixed reason code and none of that text
- **Test:** `test_driver_error_text_is_not_forwarded`.

### Requirement: Verify the target and the preconditions of each action at activation

For each action the workflow SHALL verify, immediately before it activates a
control, that the page is the one it opened, on the provider's allowed
origin, with the landmarks of the recorded interface profile, the recorded
account and no challenge, and that the driver and its launch environment are
the recorded ones. It SHALL also verify the preconditions of that action: an
entry SHALL send no submit key and SHALL be read back after a recorded
settle time, with no new user turn and no generation; a first send SHALL
require a conversation without a user turn and the complete prompt in the
composer; a plan start SHALL require the approved plan; an answer SHALL
require the recorded pending question. Every action that submits content
or starts research SHALL read the model and the source set again and SHALL
require the recorded model and public sources only. A navigation or a loss
of the target that is seen before the input event SHALL cancel the action.
A guard SHALL NOT depend on script that runs in the page. A control SHALL be
accepted only in the top-level document, inside a characterized application
region, and only when exactly one element matches; an element inside a
conversation turn, a report, an embedded panel or a frame SHALL NOT be a
target of a mode, model, source or send action. A target SHALL be resolved
again for every action. Links inside a report SHALL NOT be opened. Page
content SHALL be treated as data. A failed check SHALL block the action and
SHALL NOT be retried; if input may have been sent the job SHALL record that.

#### Scenario: The page leaves the allowed origin
- **GIVEN** a navigation or redirect ends on another origin
- **WHEN** the next action is due
- **THEN** it blocks without input and the receipt names the origin check
- **Test:** `test_off_origin_page_blocks_mutation`.

#### Scenario: The page navigates between observation and send
- **GIVEN** an observation that passed every check
- **WHEN** the page navigates or reloads before the input event of the send step
- **THEN** the send is cancelled and nothing is sent
- **AND** a navigation that begins after the input event leaves the job with a possible send and no second send
- **Test:** `test_navigation_before_input_cancels_and_after_input_is_uncertain`.

#### Scenario: Rendered content imitates a control
- **GIVEN** an element with the role and name of a send, mode, model or source control inside an answer, a report, an embedded panel or a frame
- **WHEN** the control is resolved
- **THEN** that element is not accepted as the target
- **Test:** `test_control_in_rendered_content_is_never_a_target`.

#### Scenario: Two elements match a control
- **GIVEN** two elements in the application region match the characterized control
- **WHEN** the control is resolved
- **THEN** the action blocks as ambiguous
- **Test:** `test_ambiguous_control_blocks`.

#### Scenario: The composer submits during entry
- **GIVEN** a composer that submits on a key, by itself, or by itself after a delay within the settle time
- **WHEN** the prompt is entered
- **THEN** no submit key is sent by the workflow, and if a user turn or a generation appears the job records a possible send and does not send again
- **Test:** `test_entry_sends_no_submit_key_and_self_submission_is_uncertain`.

#### Scenario: Model or sources change after preparation
- **GIVEN** a job whose model and public sources were verified at preparation
- **WHEN** a send, an answer or a plan start is due and the page now shows a lower model, a private source, or memory that is not shown off
- **THEN** the action is refused before any input
- **Test:** `test_every_submitting_action_reverifies_model_and_sources`.

#### Scenario: The conversation is not new at the first send
- **GIVEN** the page for a new job already shows a user turn
- **WHEN** the first send is due
- **THEN** it is refused and nothing is entered or sent
- **Test:** `test_first_send_requires_conversation_without_user_turn`.

#### Scenario: The plan changes after approval
- **GIVEN** an approved plan hash and a page that now shows another plan or an added source
- **WHEN** the plan start is due
- **THEN** it is refused, with no keyboard or pointer input needed for the change to be noticed
- **Test:** `test_plan_start_reverifies_plan_and_sources`.

#### Scenario: The page replaces built-in functions
- **GIVEN** a page whose script wraps the functions that activate a control
- **WHEN** an action is guarded and performed
- **THEN** the guard result does not depend on page script and the outcome is established by the next observation
- **Test:** `test_guard_does_not_trust_page_script`.

#### Scenario: The launch environment is hostile
- **GIVEN** environment variables of the caller that would configure the driver or load code into it
- **WHEN** a command starts the driver
- **THEN** none of them reaches the driver process
- **Test:** `test_driver_launch_ignores_caller_environment`.

#### Scenario: The driver is not the locked one
- **GIVEN** a driver script or library tree that differs from the lock
- **WHEN** readiness is checked or an action is due
- **THEN** the workflow blocks and reports the mismatch
- **Test:** `test_unlocked_driver_blocks`.

#### Scenario: The interface is not the characterized one
- **GIVEN** a provider page that lacks the landmarks of the recorded interface profile
- **WHEN** an action is due
- **THEN** it blocks as uncharacterized and no control is guessed
- **Test:** `test_uncharacterized_interface_blocks`.

#### Scenario: The signed-in account changes during a job
- **GIVEN** a job with a recorded account
- **WHEN** a later observation finds another account
- **THEN** observation is blocked and the provider state is kept
- **Test:** `test_account_change_blocks_browser_job`.

### Requirement: Run headed or headless under the same guards

The workflow SHALL run headed unless the caller asks for headless. It SHALL
accept headless only for a provider whose interface profile records a
characterized headless run. That record SHALL come from a separate
characterization observation without input, made after the headed
characterization of the provider. Guards, receipts and outcomes SHALL be the same
in both modes. The workflow SHALL NOT alter the browser's identity or
fingerprint and SHALL NOT bring a headed window to the front.

#### Scenario: Headless is requested without characterization
- **GIVEN** a provider interface profile without a recorded headless run
- **WHEN** a headless run is requested
- **THEN** it is rejected before any page action
- **Test:** `test_headless_requires_characterized_profile`.

#### Scenario: Headless eligibility is established
- **GIVEN** a provider with a headed characterization and no headless record
- **WHEN** the headless characterization observation runs on an existing job's page
- **THEN** it sends no input, and the profile records headless as allowed only if every check passed
- **Test:** `test_headless_eligibility_needs_a_separate_observation_without_input`.

#### Scenario: The same job is observed headed and headless
- **GIVEN** a provider whose profile allows headless
- **WHEN** a job is observed once in each mode
- **THEN** both receipts carry the same guard evidence and the mode that was used
- **Test:** `test_headed_and_headless_share_guards`; live `browser_headless_observation` where allowed.

### Requirement: Bind a browser job to its conversation address

After submission the workflow SHALL record the conversation address that the
provider assigned, with the job marker found in the submitted user turn.
Observation, interaction and collection SHALL open that address and SHALL
require exactly one marker match in a user turn. After a browser restart the
workflow SHALL rebind in the same way, with no submission. Another
conversation at the address, no match or several matches SHALL be reported
as unobservable and SHALL keep the provider state. The address SHALL NOT
appear in command output.

#### Scenario: Submission yields an address
- **GIVEN** a prompt was sent in a new dedicated conversation
- **WHEN** the receipt is observed
- **THEN** the job stores the conversation address and the verified marker
- **Test:** `test_conversation_address_binding_recorded`; live `browser_submission` for three providers.

#### Scenario: The browser was restarted
- **GIVEN** a job with an address binding and a closed browser
- **WHEN** status or rebind is requested
- **THEN** the address is reopened, the marker is verified and nothing is sent
- **Test:** `test_browser_restart_rebinds_by_address`; live `browser_restart_rebind` for three providers.

#### Scenario: The address does not show the job
- **GIVEN** the address shows another conversation, no marker or several markers
- **WHEN** observation or collection is requested
- **THEN** the result is unobservable and the provider state is kept
- **Test:** `test_address_mismatch_is_unobservable`.

#### Scenario: Command output is read
- **GIVEN** a job with an address binding
- **WHEN** a command prints its result
- **THEN** the output contains neither the address nor a value derived from the account
- **Test:** `test_address_and_account_are_not_printed`.

### Requirement: Continue a conversation with a follow-up chat turn

The workflow SHALL send a follow-up as a child job with its own marker and
request id: one ordinary chat turn in the parent job's conversation. It
SHALL refuse unless the parent is completed on the browser surface, the
parent's address opens the same conversation with the parent's marker turn,
the profile and account are unchanged, nothing is generating or pending, the
composer is empty, the parent has no other unfinished child, the provider
is not reserved by another job, and the private-source rule holds. A
retried follow-up SHALL be reconciled and SHALL NOT add a second turn. The
workflow SHALL NOT start a second research run in a conversation.

A child job SHALL differ from a research job only as follows. It SHALL use
the parent's bound conversation in place of a new dedicated conversation.
Its completion SHALL be the characterized end of the ordinary answer that
follows the child's user turn; that result SHALL be saved and indexed as an
answer and SHALL NOT be labeled a research report. An answer MAY have no
citations; links that it has SHALL come from link targets. Collecting a
child job SHALL save only the answer that follows the child's marker turn.
The model and source rules SHALL apply to a child as to a new job.

#### Scenario: A follow-up turn is sent
- **GIVEN** a completed parent job with a verified address binding
- **WHEN** a follow-up is requested with a new request id
- **THEN** one new user turn with the child's marker follows the parent's turn in the same conversation
- **AND** the child job records its parent
- **Test:** `test_follow_up_appends_one_turn_in_parent_conversation`; live `browser_follow_up` for three providers.

#### Scenario: The parent conversation cannot be proved
- **GIVEN** the parent's address shows another conversation, or the parent is not completed
- **WHEN** a follow-up is requested
- **THEN** it is refused before any input
- **Test:** `test_follow_up_fails_closed_without_parent_binding`.

#### Scenario: A follow-up is retried after a possible send
- **GIVEN** a follow-up whose send outcome is uncertain
- **WHEN** the same request id is retried
- **THEN** the existing child job is reconciled and no second turn is sent
- **Test:** `test_follow_up_retry_does_not_duplicate_turn`.

#### Scenario: Another job holds the provider or the conversation
- **GIVEN** a parent with an unfinished child, or a provider reserved by another job
- **WHEN** a follow-up is requested
- **THEN** it blocks until that job has a known outcome
- **Test:** `test_follow_up_blocks_while_provider_or_conversation_is_held`.

#### Scenario: A research run is asked for in a follow-up
- **GIVEN** a follow-up request that asks for research mode
- **WHEN** the request is validated
- **THEN** it is rejected as not offered and nothing is sent
- **Test:** `test_follow_up_offers_chat_turns_only`.

#### Scenario: A follow-up answer is collected
- **GIVEN** a completed child whose answer has no citations
- **WHEN** the child is collected
- **THEN** only the answer after the child's marker turn is saved, labeled as an answer and not as a research report
- **AND** the parent's report and its collection state are unchanged
- **Test:** `test_follow_up_collects_only_the_child_answer`.

#### Scenario: A follow-up would use a lower model or a private source
- **GIVEN** the parent's conversation now shows a lower model, an enabled private source, or memory or earlier-chat reference that is not shown off
- **WHEN** a follow-up is prepared
- **THEN** it blocks before entry with the model or source reason
- **Test:** `test_follow_up_reverifies_model_and_sources`.

### Requirement: Keep page content out of output and storage

On the browser surface, command output SHALL contain only the closed
summary, hashes, flags and file paths. The driver SHALL write no file except
a download that a collection step armed. From a page observation the
workflow SHALL keep only facts about characterized controls and, once the
marker binding holds, the content of the bound job. In its own files it
SHALL NOT store other page text or the titles of other conversations, and
SHALL NOT store page content before the binding holds; it SHALL NOT print
them. What the browser itself keeps in the dedicated profile, such as
cache, history and site data, is outside this requirement, and the
workflow SHALL NOT read it. A wider capture SHALL need an
explicit operator request and SHALL be limited to the job's region. Content
of the bound job SHALL be returned only on the explicit job-content request
and SHALL be labeled as untrusted. Interface characterization SHALL work by
probing candidate controls from a reviewed list and SHALL return, for each
candidate, only counts, uniqueness, ancestor roles and enabled state; it
SHALL NOT return a name or text found on the page.

#### Scenario: The page shows unrelated conversations
- **GIVEN** a provider page whose side panel lists other conversations
- **WHEN** readiness, status or collection output is printed
- **THEN** the output has only allowed fields and none of the page text
- **Test:** `test_browser_default_output_is_closed_schema`.

#### Scenario: A job is observed and collected
- **GIVEN** a bound job on a page that also shows other conversations
- **WHEN** the job is observed and collected
- **THEN** the job's private files hold the bound job's content and control facts, and no other page text
- **Test:** `test_unrelated_page_text_is_never_stored`.

#### Scenario: The binding does not hold yet
- **GIVEN** a page observation before the job marker is verified
- **WHEN** the observation ends
- **THEN** no page content was written for that job
- **Test:** `test_nothing_is_stored_before_binding`.

#### Scenario: The profile holds data of earlier pages
- **GIVEN** a dedicated profile whose cache, history and site data hold content of earlier pages
- **WHEN** any command runs
- **THEN** no component of the workflow reads those files and none of that content reaches output or the job's files
- **Test:** `test_workflow_never_reads_browser_profile_storage`.

#### Scenario: The page logs text and starts a download by itself
- **GIVEN** a page that writes text to its console and starts a download that no collection step asked for
- **WHEN** any command runs against it
- **THEN** no file with that text or that download exists afterwards
- **Test:** `test_driver_writes_no_unrequested_file`.

#### Scenario: An interface is characterized
- **GIVEN** a provider page with conversations, a conversation list and a report
- **WHEN** candidate controls are probed
- **THEN** each result holds counts, uniqueness, ancestor roles and enabled state, and no name or text from the page
- **Test:** `test_characterization_probes_return_no_page_text`.

### Requirement: Collect the browser report from the bound conversation

The workflow SHALL collect a browser report only from the bound, completed
conversation. It SHALL prefer a file that the provider offers for that
report, and otherwise SHALL extract the bound answer from the page. A
download SHALL be accepted only as the single download that follows the
collection step's own activation; any other new file SHALL fail the attempt.
A saved file SHALL get a generated name. Source addresses SHALL come from
link targets in that answer or from the offered file. The workflow SHALL NOT
use the system clipboard for browser collection by default, and SHALL NOT
use an export that creates or shares a document in the provider account
unless the caller asks for that export by name. For a research report the
rules of `desktop-research-collection` for completion, hashes, storage and
citation provenance SHALL apply; for a child job the exceptions of the
follow-up requirement SHALL apply.

#### Scenario: The provider offers a report file
- **GIVEN** a completed bound report with a download control
- **WHEN** collection runs
- **THEN** the one file that this activation produced is saved under a generated name in the attempt folder and hashed
- **Test:** `test_browser_download_is_bound_to_the_job`; live `browser_native_export` where offered.

#### Scenario: A second file appears during collection
- **GIVEN** the page starts another download while the report download is awaited
- **WHEN** collection runs
- **THEN** the attempt fails and neither file is recorded as the report
- **Test:** `test_extra_download_fails_the_collection_attempt`.

#### Scenario: The report is extracted from the page
- **GIVEN** a completed bound report without a usable download
- **WHEN** collection runs
- **THEN** the saved text comes from the bound answer only and every citation has a link target from that answer
- **Test:** `test_browser_extraction_keeps_link_targets`; live `browser_report_collection` for three providers.

#### Scenario: An export would write to the provider account
- **GIVEN** the only offered export creates a document in the provider's cloud storage or a shared link
- **WHEN** collection runs without a request for that export
- **THEN** that control is not used and collection falls back to extraction or reports the gap
- **Test:** `test_account_writing_export_needs_explicit_request`.

### Requirement: Require the operator's acknowledgement of provider terms for live browser operations

Every live operation that starts the driver against a provider SHALL require
a valid acknowledgement for that provider: readiness with a page probe,
inspection, submission, status, wait, rebind, answer, plan start, follow-up,
collection, each entry of a batch, and interface characterization. Sign-in
without a driver, listing, audit, the local readiness check and the
acknowledgement commands themselves SHALL NOT require it. An acknowledgement
SHALL be recorded only after the operating system authenticated a person
biometrically for a request that names the provider. It SHALL record the
provider, the operating-system user, the date and the address of the terms,
SHALL be bound to the account found by the first gated operation, SHALL
expire after a fixed period and SHALL be removable by the operator. Without
a valid acknowledgement a gated operation SHALL stop before the driver
starts and SHALL name the provider's programmatic route where one is
recorded. Listing, audit and the local readiness check SHALL use no
network.

#### Scenario: No acknowledgement is recorded
- **GIVEN** a provider without a recorded acknowledgement
- **WHEN** any gated live browser operation is requested
- **THEN** it stops with the acknowledgement reason before the driver starts
- **AND** the result names the provider's programmatic route if one is recorded
- **Test:** `test_gated_browser_operations_require_terms_acknowledgement`.

#### Scenario: An ungated operation runs without acknowledgement
- **GIVEN** a provider without a recorded acknowledgement
- **WHEN** sign-in, listing, audit or the local readiness check is requested
- **THEN** it runs and starts no driver against the provider
- **AND** listing, audit and the local readiness check use no network
- **Test:** `test_ungated_operations_start_no_driver`.

#### Scenario: Authentication does not succeed
- **GIVEN** the acknowledgement command runs and biometric authentication fails, is cancelled or is not available
- **WHEN** the command ends
- **THEN** nothing is recorded, whatever kind of terminal invoked it
- **Test:** `test_terms_acknowledgement_needs_successful_biometric_authentication`.

#### Scenario: Another account is signed in
- **GIVEN** an acknowledgement bound to one account
- **WHEN** a gated operation finds another account in the profile
- **THEN** it stops with the acknowledgement reason
- **Test:** `test_acknowledgement_is_bound_to_the_account`.

#### Scenario: The acknowledgement is old or withdrawn
- **GIVEN** an acknowledgement past its period, or one that the operator withdrew
- **WHEN** a gated operation is requested
- **THEN** it stops with the acknowledgement reason
- **Test:** `test_expired_or_withdrawn_acknowledgement_blocks`.

#### Scenario: A batch names several providers
- **GIVEN** a batch with one acknowledged and one unacknowledged provider
- **WHEN** the batch runs
- **THEN** the unacknowledged entry stops before its driver starts and the other entry proceeds
- **Test:** `test_batch_checks_acknowledgement_per_entry`.

### Requirement: Apply the execution rules to every provider on the browser surface

For Claude, ChatGPT and Gemini on the browser surface the workflow SHALL
apply the requirements of `desktop-research-execution` for model selection,
public source scope, interaction states, idempotent requests, observation
transitions and provenance, with these stated substitutions and no others.
The browser target evidence of this capability SHALL replace the
signed-application evidence. A lock on the provider's profile SHALL replace
the desktop lock; it SHALL be taken without waiting, and a busy profile
SHALL give a busy outcome with no page action. The local input-idle rule
SHALL NOT apply. Observation MAY load the bound conversation address in the
workflow's own page and SHALL NOT bring a window to the front. A child job
SHALL follow the exceptions of the follow-up requirement. Account memory,
reference to earlier chats, search over past chats and connected
applications SHALL count as private sources: unless the interface shows them
off for the conversation, the job SHALL block. A plan that the provider
proposes SHALL be started only through the plan-hash approval. Receipts
SHALL carry the surface and one of the boundary values `live`, `fixture` or
`synthetic`; only `live` SHALL pass a live audit.

#### Scenario: A provider proposes a research plan in the browser
- **GIVEN** a browser job whose provider shows a plan and a start control
- **WHEN** status is observed and the plan hash is approved
- **THEN** research starts only after the approval and the transition is recorded
- **Test:** `test_browser_plan_requires_hash_approval`; live `browser_plan_approval` where a plan is shown.

#### Scenario: A private source is enabled in the browser
- **GIVEN** a provider page with a connected private source that cannot be deselected
- **WHEN** input is prepared
- **THEN** the job blocks without prompt entry
- **Test:** `test_browser_private_source_blocks`.

#### Scenario: Memory or earlier chats can reach the answer
- **GIVEN** a provider page that does not show account memory and earlier-chat reference as off for the conversation
- **WHEN** input is prepared for a new job
- **THEN** the job blocks without prompt entry and no account setting is changed
- **Test:** `test_memory_and_chat_history_count_as_private_sources`.

#### Scenario: The model cannot be verified in the browser
- **GIVEN** a browser page whose model choices are incomplete or unranked
- **WHEN** a submission is requested
- **THEN** it blocks with a model-selection reason
- **Test:** `test_browser_model_selection_is_verified`.

#### Scenario: Two commands want the same profile
- **GIVEN** a process that holds a provider's profile for a transaction
- **WHEN** another process requests an operation on that provider
- **THEN** it returns a busy outcome without a page action, and proceeds after the first process exits
- **Test:** `test_profile_lock_serializes_processes_without_waiting`.

#### Scenario: A browser job is observed while the operator works elsewhere
- **GIVEN** a bound browser job and an operator using other applications
- **WHEN** status or wait observes the job
- **THEN** the bound address is loaded in the workflow's own page, no window is brought to the front, and the input-idle rule is not consulted
- **Test:** `test_browser_observation_loads_address_without_focus`.

#### Scenario: A fixture receipt reaches the live audit
- **GIVEN** a job produced against local fixture pages
- **WHEN** the live audit runs
- **THEN** the fixture stamp is rejected as live proof
- **Test:** `test_fixture_receipts_are_rejected_by_live_audit`.
