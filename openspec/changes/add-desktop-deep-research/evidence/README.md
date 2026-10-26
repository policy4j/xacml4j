# Evidence

Planning, characterization and execution evidence. The production orchestrator
is implemented and its 31 execution tests pass with a synthetic desktop boundary.
The compiled native bridge, full collection, reusable skill and both-provider
production acceptance remain pending. One Claude characterization report is
collected and validated; ChatGPT has zero submissions.

The browser surface (task group 5, added 2026-10-09) is planned and
researched only. Its 63 named tests and all of its live checks are UNRUN,
and nothing of it is implemented. The plan is in its fourth version after
eight reviews, each of which asked for changes; no reviewer has approved
it, and the fourth version has not been read by one.

## Requirements to proof

The 31 named execution tests first failed with NOT_IMPLEMENTED in
`tests/009-execution-red/`, then all passed with zero skips in
`tests/010-execution-green/`. Collection tests, the native bridge and all
production live checks remain UNRUN. The local
capability probe is PARTIAL and cannot satisfy any live acceptance requirement.

| Capability / requirement | Tasks | Named tests or live checks | Run status |
| --- | --- | --- | --- |
| desktop-research-execution / Verify provider readiness and scope | 2.1, 2.2, 2.3, 4.1, 4.2 | `test_doctor_reports_observed_capabilities`, `test_start_blocks_without_research_readiness`; live `provider_readiness` | PASS synthetic: 010; native/live UNRUN |
| desktop-research-execution / Submit only to an identified research conversation | 2.1, 2.2, 2.3, 4.1, 4.2 | `test_start_requires_observed_receipt`, `test_focus_change_preserves_draft_and_stops_input`; live `research_submission` | PASS synthetic: 010; native/live UNRUN |
| desktop-research-execution / Handle provider interaction states explicitly | 2.1, 2.2, 2.3 | `test_authorized_plan_can_start_research`, `test_unresolved_question_or_plan_scope_stops`, `test_quota_state_does_not_resubmit` | PASS synthetic: 010; native/live UNRUN |
| desktop-research-execution / Resume observations without duplicate submission | 2.1, 2.2, 2.3, 4.1, 4.2 | `test_wait_timeout_preserves_job_and_resume_does_not_submit`, `test_crash_after_send_requires_reconciliation`; live `timeout_resume` | PASS synthetic: 010; native/live UNRUN |
| desktop-research-execution / Serialize desktop actions and isolate job data | 2.1, 2.2, 2.3 | `test_desktop_lock_serializes_processes`, `test_batch_preserves_per_provider_outcomes`, `test_default_diagnostics_redact_unrelated_data` | PASS synthetic: 010; native/live UNRUN |
| desktop-research-execution / Constrain and verify public source scope | 2.1, 2.2, 2.3, 4.1, 4.2 | `test_public_source_selection_is_in_receipt`, `test_start_blocks_on_unconstrainable_private_source`, `test_start_blocks_when_source_scope_unverifiable`, `test_source_selection_side_effects_restored`; live `source_scope_verified` | PASS synthetic: 010; native/live UNRUN |
| desktop-research-execution / Rebind the existing conversation safely | 2.1, 2.2, 2.3, 4.1, 4.2 | `test_pre_send_binding_bootstrap_and_upgrade`, `test_missing_or_ambiguous_rebind_preserves_state`, `test_wrong_conversation_blocks_mutation_and_collection`; live `conversation_rebind_after_reopen` | PASS synthetic: 010; native/live UNRUN |
| desktop-research-execution / Make requests and batch reruns idempotent | 2.1, 2.2, 2.3 | `test_start_retry_after_killed_start_does_not_resubmit`, `test_provider_uncertainty_blocks_across_checkouts`, `test_batch_rerun_resumes_without_resubmission`, `test_concurrent_job_updates_keep_state` | PASS synthetic: 010; native/live UNRUN |
| desktop-research-execution / Own interactions and observation transitions explicitly | 2.1, 2.2, 2.3 | `test_transition_table_and_command_ownership`, `test_intent_and_multiline_entry_guard_send` | PASS synthetic: 010; native/live UNRUN |
| desktop-research-execution / Observe without taking user focus | 2.1, 2.2, 2.3 | `test_wait_polling_never_activates`, `test_mutation_deferred_while_user_active`, `test_transaction_lock_is_cross_checkout_and_temp_independent`, `test_bridge_guard_schema_and_modal_handling` | PASS synthetic: 010; native/live UNRUN |
| desktop-research-execution / Distinguish trusted live proof from synthetic output | 2.1, 2.2, 2.3 | `test_synthetic_receipts_are_rejected_by_live_audit`, `test_target_trust_version_and_caller_isolation` | PASS synthetic: 010; native/live UNRUN |
| desktop-research-collection / Establish provider completion before collecting | 3.1, 3.2, 4.1, 4.2 | `test_collect_requires_identified_final_research_result`, `test_collect_rejects_in_progress_or_pending_interaction`; live `provider_completion` | UNRUN |
| desktop-research-collection / Save cited reports and supported exports with integrity | 3.1, 3.2, 4.1, 4.2 | `test_native_export_is_complete_and_hashed`, `test_copy_collection_rejects_stale_clipboard_and_missing_sources`, `test_failed_export_keeps_provider_completion`; live `native_export`, `report_collection` | UNRUN |
| desktop-research-collection / Maintain resumable local report storage | 3.1, 3.2, 4.1, 4.2 | `test_index_keeps_reports_separate`, `test_tampered_report_and_unsafe_output_are_rejected`; live `dual_provider_collection` | UNRUN |
| desktop-research-collection / Require complete observations and exact citation provenance | 3.1, 3.2 | `test_incomplete_observation_cannot_establish_completion`, `test_copy_rejects_foreign_write_and_preserves_clipboard`, `test_exact_public_citation_provenance_is_required`, `test_storage_index_capture_and_retry_preserve_boundaries` | UNRUN |
| browser-research-session / Select the surface explicitly | 5.4, 5.5, 5.14 | `test_surface_is_recorded_and_never_substituted`, `test_job_cannot_change_surface`, `test_browser_only_provider_requires_browser_surface`, `test_request_id_conflict_is_rejected_before_page_access`; live `browser_surface_selected` | UNRUN |
| browser-research-session / Keep the provider reserved until the outcome is known | 5.4, 5.5 | `test_provider_reservation_blocks_across_surfaces`, `test_local_failure_after_possible_send_keeps_reservation`, `test_reservation_ends_only_on_positive_outcome` | UNRUN |
| browser-research-session / Use a dedicated browser session that the operator signed in | 5.3, 5.4, 5.5, 5.6, 5.7, 5.10, 5.14 | `test_browser_start_blocks_when_signed_out`, `test_login_opens_profile_without_driver_or_input`, `test_transaction_owns_a_new_page_and_closes_others`, `test_challenge_overlay_blocks_without_bypass`, `test_browser_boundary_never_touches_session_secrets`, `test_driver_error_text_is_not_forwarded`; live `browser_readiness` | UNRUN |
| browser-research-session / Verify the target and the preconditions of each action at activation | 5.3, 5.4, 5.5, 5.6, 5.9 | `test_off_origin_page_blocks_mutation`, `test_navigation_before_input_cancels_and_after_input_is_uncertain`, `test_control_in_rendered_content_is_never_a_target`, `test_ambiguous_control_blocks`, `test_entry_sends_no_submit_key_and_self_submission_is_uncertain`, `test_every_submitting_action_reverifies_model_and_sources`, `test_first_send_requires_conversation_without_user_turn`, `test_plan_start_reverifies_plan_and_sources`, `test_guard_does_not_trust_page_script`, `test_driver_launch_ignores_caller_environment`, `test_unlocked_driver_blocks`, `test_uncharacterized_interface_blocks`, `test_account_change_blocks_browser_job` | UNRUN |
| browser-research-session / Run headed or headless under the same guards | 5.3, 5.4, 5.5, 5.10, 5.14 | `test_headless_requires_characterized_profile`, `test_headless_eligibility_needs_a_separate_observation_without_input`, `test_headed_and_headless_share_guards`; live `browser_headless_observation` | UNRUN |
| browser-research-session / Bind a browser job to its conversation address | 5.4, 5.5, 5.10, 5.14 | `test_conversation_address_binding_recorded`, `test_browser_restart_rebinds_by_address`, `test_address_mismatch_is_unobservable`, `test_address_and_account_are_not_printed`; live `browser_restart_rebind`, `browser_submission` | UNRUN |
| browser-research-session / Continue a conversation with a follow-up chat turn | 5.4, 5.5, 5.11, 5.14 | `test_follow_up_appends_one_turn_in_parent_conversation`, `test_follow_up_fails_closed_without_parent_binding`, `test_follow_up_retry_does_not_duplicate_turn`, `test_follow_up_blocks_while_provider_or_conversation_is_held`, `test_follow_up_offers_chat_turns_only`, `test_follow_up_collects_only_the_child_answer`, `test_follow_up_reverifies_model_and_sources`; live `browser_follow_up` | UNRUN |
| browser-research-session / Keep page content out of output and storage | 5.4, 5.5, 5.6, 5.8, 5.9 | `test_browser_default_output_is_closed_schema`, `test_unrelated_page_text_is_never_stored`, `test_nothing_is_stored_before_binding`, `test_workflow_never_reads_browser_profile_storage`, `test_driver_writes_no_unrequested_file`, `test_characterization_probes_return_no_page_text` | UNRUN |
| browser-research-session / Collect the browser report from the bound conversation | 5.4, 5.11, 5.14 | `test_browser_download_is_bound_to_the_job`, `test_extra_download_fails_the_collection_attempt`, `test_browser_extraction_keeps_link_targets`, `test_account_writing_export_needs_explicit_request`; live `browser_native_export`, `browser_report_collection` | UNRUN |
| browser-research-session / Require the operator's acknowledgement of provider terms for live browser operations | 5.2, 5.4, 5.5, 5.7, 5.9 | `test_gated_browser_operations_require_terms_acknowledgement`, `test_ungated_operations_start_no_driver`, `test_terms_acknowledgement_needs_successful_biometric_authentication`, `test_acknowledgement_is_bound_to_the_account`, `test_expired_or_withdrawn_acknowledgement_blocks`, `test_batch_checks_acknowledgement_per_entry` | UNRUN |
| browser-research-session / Apply the execution rules to every provider on the browser surface | 5.4, 5.5, 5.14 | `test_browser_plan_requires_hash_approval`, `test_browser_private_source_blocks`, `test_memory_and_chat_history_count_as_private_sources`, `test_browser_model_selection_is_verified`, `test_profile_lock_serializes_processes_without_waiting`, `test_browser_observation_loads_address_without_focus`, `test_fixture_receipts_are_rejected_by_live_audit`; live `browser_plan_approval` | UNRUN |

## Gate tasks to proof

| Task | Run(s) | Status |
| --- | --- | --- |
| 1.1 | `builds/001-local-capabilities/`, `builds/002-plan-validation/`, `builds/003-revised-plan-validation/`, `builds/004-binding-plan-validation/`, `reviews/plan/` | DONE: planning/probes only |
| 1.2 | `reviews/plan/rv001-architecture-claude/`, `reviews/plan/rv002-architecture-agy/`, `reviews/plan/rv003-corrected-plan-agy/`, `reviews/plan/rv004-binding-plan-agy/` | DONE: planning review only |
| 1.3 | `final/006-maintainer-approval/` | DONE: user go-ahead and scoped budget recorded |
| 1.4 | `builds/007-live-characterization/`, `builds/008-characterization-disposition/` | DONE: Claude conditional go with one verified collected sample; ChatGPT no-go/protected draft; production corrections and remaining budget recorded |
| 2.1 | `tests/009-execution-red/` | DONE: all 31 execution-spec names implemented as tests; 31 NOT_IMPLEMENTED failures, zero skips; complete output retained |
| 2.2 | `tests/010-execution-green/` | DONE: 31 execution tests pass through production orchestration; desktop synthetic; native/export gates pending |
| 2.4 | `reviews/code/rv<next>-<scope>-<lane>/`, `tests/<next>-execution-gate/` | UNRUN |
| 3.4 | `reviews/<lane>/rv<next>-<scope>-<lane>/`, `tests/<next>-collection-skill-gate/` | UNRUN |
| 4.3 | `final/<next>-live-acceptance/`, `reviews/final/rv<next>-<scope>-<lane>/` | UNRUN |
| 4.4 | `final/<next>-completion-audit/` | UNRUN |
| 5.1 | `builds/014-browser-surface-research/` to `builds/020-browser-plan-revalidation/`, `reviews/plan/rv005-browser-plan-codex/` to `reviews/plan/rv012-browser-plan-confirm-codex/` | OPEN: eight reviews asked for changes; the plan was revised three times; no approving review exists |
| 5.2 | `final/<next>-browser-approval/` | UNRUN: waits for the maintainer |
| 5.9 | `reviews/code/rv<next>-<scope>-<lane>/`, `tests/<next>-browser-bridge-gate/` | UNRUN |
| 5.13 | `reviews/code/rv<next>-<scope>-<lane>/`, `tests/<next>-browser-gate/` | UNRUN |

## Runs

| Run | Kind | Command / method | Result |
| --- | --- | --- | --- |
| `builds/001-local-capabilities/` | Local app and permission probes | Swift, app plist/signing queries, local app launch, app-window capture/OCR | PARTIAL: identities, permission and window access; no research-mode, send or collection proof |
| `builds/002-plan-validation/` | Initial structural checks | OpenSpec 1.3.1 strict validation; initial scenario mapping | PASS of initial packet: 8 requirements, 19 scenarios/tests; superseded for current-plan checking |
| `reviews/plan/rv001-architecture-claude/` | Independent initial architecture/privacy review | Native Claude Fable 5.1, restricted Read, plan mode | REQUEST_CHANGES: 6 HIGH, 12 MEDIUM, 9 LOW; disposition retained; corrected plan reviewed in rv003/rv004 |
| `reviews/plan/rv002-architecture-agy/` | Independent initial architecture review | Native Gemini 3.1 Pro High inline snapshot | APPROVE old snapshot; 1 LOW; advertised permission-mode and raw marker limitations retained |
| `builds/003-revised-plan-validation/` | Corrected-packet structural checks | OpenSpec 1.3.1 strict validation; current scenario/evidence mapping | PASS: 15 requirements, 41 scenarios/tests, no missing/invalid mapping; planning structure only |
| `reviews/plan/rv003-corrected-plan-agy/` | Independent corrected-packet review | Same native Gemini conversation, inline frozen packet | APPROVE with no original HIGH/MEDIUM unresolved; subsequently identified draft bootstrap gap requires final correction/review |
| `builds/004-binding-plan-validation/` | Draft-bootstrap structural checks | OpenSpec strict validation and current mapping | PASS: 15 requirements, 42 scenarios/tests, zero mapping issues; all implementation/live checks UNRUN |
| `reviews/plan/rv004-binding-plan-agy/` | Independent final binding review | Same native Gemini conversation, narrow frozen packet | APPROVE: bootstrap gap resolved in plan; no prior HIGH/MEDIUM reopened; permission-enforcement limitation retained |
| `builds/005-final-plan-audit/` | Final planning bookkeeping audit | Strict validation, semantic input hashes and completed-task evidence checks | PASS: only planning tasks 1.1/1.2 checked; reviewed semantic files unchanged; production files absent |
| `final/006-maintainer-approval/` | User authorization | Current conversation reply to plan approval request | Go-ahead to try sample reports within the reviewed public-source scope and budget |
| `builds/007-live-characterization/` | Native desktop characterization | Scoped AX, in-memory OCR, private durable receipts, one Send and bound document Copy | PARTIAL: Claude Research completed and Markdown collected with 20 exact native citation matches; ChatGPT unsent draft preserved; production/live acceptance UNRUN |
| `builds/008-characterization-disposition/` | Current go/no-go and budget audit | Native AX composer reread and independent private report hash/state check | Task 1.4 complete as characterization disposition only; both-provider production acceptance remains UNRUN |
| `tests/009-execution-red/` | Execution scenario tests | Production CLI loader with synthetic desktop only; real filesystem and OS locks | RED: 31 NOT_IMPLEMENTED failures, zero skips; no production or live acceptance |

| `tests/010-execution-green/` | Durable orchestration tests | Production CLI, real storage/locks/journal/process exit, synthetic desktop only | PASS: 31 tests, zero skips; native/collection/live remain UNRUN |
| `tests/013-highest-model-red/` | Highest-model scenario tests | Production CLI with synthetic desktop only | RED: 3 added tests fail, 31 pass; implementation belongs to task 2.3 |
| `builds/014-browser-surface-research/` | Browser surface research | GitHub, DuckDuckGo and provider documentation MCP tools; read-only local probe | PARTIAL: drivers, prior work, API routes and provider terms from primary sources; no provider page opened; Context7 and Tavily UNAVAILABLE |
| `builds/015-browser-plan-validation/` | Browser plan structural checks | OpenSpec 1.6.0 strict validation; requirement-to-test-to-index mapping | PASS as first written: 11 requirements, 37 scenarios and tests, no mapping issue; planning structure only |
| `reviews/plan/rv005-browser-plan-codex/` | Independent review of the first browser plan | Codex CLI one-shot, read-only, packet only | REQUEST_CHANGES: 3 HIGH, 6 MEDIUM; all accepted and the plan revised |
| `builds/016-browser-research-crosscheck/` | Research cross-check and driver probe | Tavily and Context7 after a gateway restart; npm registry view; help output of the candidate driver version | SUPPORTED: three more single-provider projects, none covering all three; options of `0.0.73` match the design; cause of the failed lanes of run 014 recorded |
| `builds/017-browser-plan-revalidation/` | Revised browser plan structural checks | OpenSpec 1.6.0 strict validation; mapping check | PASS: 11 requirements, 52 scenarios and tests, twelve tasks, no mapping issue; planning structure only |
| `reviews/plan/rv006-browser-plan-swarm-codex-astra/` | Swarm review of the second plan: architecture and state machine | NTM pane, persona `policy4j_arch_review_codex_astra`, GPT-6 Astra | REQUEST_CHANGES: 3 HIGH, 4 MEDIUM; all accepted |
| `reviews/plan/rv007-browser-plan-swarm-codex-sol/` | Swarm review: driver and bridge security | NTM pane, persona `policy4j_arch_review_codex_sol`, GPT-6.1 Sol | REQUEST_CHANGES: 3 HIGH, 3 MEDIUM; all accepted; the driver was changed |
| `reviews/plan/rv008-browser-plan-swarm-codex-luna/` | Swarm review: specification and tasks | NTM pane, persona `policy4j_arch_review_codex_luna`, GPT-6 Luna | REQUEST_CHANGES: 2 MEDIUM, 1 LOW; all accepted |
| `reviews/plan/rv009-browser-plan-swarm-claude-opus/` | Swarm review: privacy and data flow | NTM pane, persona `policy4j_arch_review_claude_opus`, Claude Opus 5.5 (same family as the author) | REQUEST_CHANGES: 3 HIGH, 3 MEDIUM; all accepted, one by cutting the feature |
| `reviews/plan/rv010-browser-plan-swarm-claude-fable/` | Swarm review: provider terms and acknowledgement | NTM pane, persona `policy4j_arch_review_claude_fable`, Claude Fable 5.1 | REQUEST_CHANGES: 2 HIGH, 3 MEDIUM, 2 LOW; four accepted in full, three in part |
| `reviews/plan/rv011-browser-plan-swarm-grok/` | Swarm review: adversarial lane | NTM pane, Grok 4.7, role prompt sent with the packet | REQUEST_CHANGES: 5 HIGH, 2 MEDIUM; all accepted, two by cutting features |
| `builds/018-browser-driver-source-check/` | Check of reviewer claims in the installed driver package | Read-only search of the package source; library documentation through Context7 | SUPPORTED: automatic console log and download files, environment configuration with a code file, evaluate without arguments, keychain switches; library options for the new design documented |
| `builds/019-browser-plan-revalidation/` | Third plan structural checks | OpenSpec 1.6.0 strict validation; mapping check | PASS: 11 requirements, 60 scenarios and tests, fourteen tasks, no mapping issue; planning structure only |
| `reviews/plan/rv012-browser-plan-confirm-codex/` | Confirming review of the third plan | Codex CLI one-shot, read-only, packet only | REQUEST_CHANGES: 3 HIGH, 4 MEDIUM, 1 LOW; all accepted; the architecture was found sound in principle |
| `builds/020-browser-plan-revalidation/` | Fourth plan structural checks | OpenSpec 1.6.0 strict validation; mapping check | PASS: 11 requirements, 63 scenarios and tests, fourteen tasks, no mapping issue; planning structure only |

Every run retains its summary, command or invocation receipt, and supporting
output. Actual review runs use fresh numbered children; parent lane paths and
`<next>` placeholders above do not preassign a future run number. Capability
outputs contain only fixed allowlisted labels and counts. Full live reports
remain private user-owned artifacts outside skills/Git; retained proof is
summarized/redacted. Original hashes and normalized-path qualifications remain
with reviewer receipts.

## Not applicable

- Maven/module tests, JDK migration, XACML conformance, and JMH: NOT APPLICABLE;
  additive agent tooling has no Java runtime or build effect.
- No archive or Git history action is requested.
