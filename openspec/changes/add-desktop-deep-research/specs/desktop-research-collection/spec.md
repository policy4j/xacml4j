## ADDED Requirements

### Requirement: Establish provider completion before collecting

The workflow SHALL collect only the completed final research result from the
identified job. Stable text, a timer, a Copy button, or an ordinary chat reply
alone SHALL NOT establish research completion.

#### Scenario: Identified final research report is ready
- **GIVEN** the same job has provider-specific final-result evidence and no pending research interaction
- **WHEN** collection begins
- **THEN** the receipt records the completion evidence for that job
- **Test:** `test_collect_requires_identified_final_research_result`; live `provider_completion` for both providers.

#### Scenario: Research or a clarification is still pending
- **GIVEN** the provider is still researching or awaiting a plan/question response
- **WHEN** collection is requested
- **THEN** it reports incomplete state and does not label partial content as a final report
- **Test:** `test_collect_rejects_in_progress_or_pending_interaction`.

### Requirement: Save cited reports and supported exports with integrity

The workflow SHALL save a nonempty report, source URLs, collection method,
provider and app identity, job marker, timestamps, and content hashes. It SHALL
preserve supported native exports and identify copied or extracted text by its
actual format. It SHALL verify each saved file independently before recording
the job as collected.

#### Scenario: Native report export succeeds
- **GIVEN** a completed report exposes a supported download format
- **WHEN** the export is collected
- **THEN** the workflow verifies the completed file, content type, source links, job identity, and hash
- **AND** it records only the formats actually saved
- **Test:** `test_native_export_is_complete_and_hashed`; live `native_export` when exposed.

#### Scenario: Report-specific Copy or extraction succeeds
- **GIVEN** the identified final report exposes Copy or isolated response extraction
- **WHEN** the workflow collects that report
- **THEN** it binds the result to the job and saves cited Markdown or accurately labeled plain text
- **AND** it rejects preexisting clipboard content and incomplete citation extraction
- **Test:** `test_copy_collection_rejects_stale_clipboard_and_missing_sources`; live `report_collection` for both providers.

#### Scenario: Export fails after research completes
- **GIVEN** provider completion was observed but file validation fails
- **WHEN** collection ends
- **THEN** the job retains provider completion and records collection failure
- **AND** retrying collection does not start another research job
- **Test:** `test_failed_export_keeps_provider_completion`.

### Requirement: Maintain resumable local report storage

The workflow SHALL store each job in a separate local directory with atomic
metadata and an index. Default generated prompts, screenshots, and reports
SHALL be ignored by Git and readable only by the local user. Collection SHALL
reject path traversal and unexpected symbolic links, preserve existing files,
and verify hashes when reporting an existing collected result.

#### Scenario: Two provider reports are collected
- **GIVEN** two identified provider jobs complete and collect successfully
- **WHEN** their local index is read
- **THEN** it lists separate reports, sources, receipts, and collection results
- **Test:** `test_index_keeps_reports_separate`; live `dual_provider_collection`.

#### Scenario: Existing report is changed or an unsafe output is requested
- **GIVEN** saved content no longer matches its hash or an output path escapes the chosen run directory
- **WHEN** the workflow reports or writes collection results
- **THEN** it reports integrity failure or rejects the unsafe destination without overwriting user files
- **Test:** `test_tampered_report_and_unsafe_output_are_rejected`.

### Requirement: Require complete observations and exact citation provenance

Partial observations SHALL NOT establish completion from absent controls.
Source URLs SHALL come from native link targets, exported content or verified
source controls, never OCR. Copy collection SHALL reject competing clipboard
writes, corroborate copied content against the bound result, and clear only an
unchanged copied value. Saved citations SHALL resolve and public-only reports
SHALL exclude nonpublic source URLs.

#### Scenario: Completion is inferred from an incomplete view
- **GIVEN** a view is hidden, occluded, scrolled or incompletely represented
- **WHEN** completion is checked
- **THEN** missing controls do not establish a completed report
- **Test:** `test_incomplete_observation_cannot_establish_completion`.

#### Scenario: Another writer changes the clipboard
- **GIVEN** report Copy and another clipboard writer overlap
- **WHEN** collection reads or clears copied data
- **THEN** uncorroborated data is rejected and the other writer's value is preserved
- **Test:** `test_copy_rejects_foreign_write_and_preserves_clipboard`.

#### Scenario: Citations lack exact link provenance
- **GIVEN** citation links are missing, misread by OCR, unresolved or nonpublic
- **WHEN** the final report is validated
- **THEN** collection remains incomplete and no invented URL is accepted
- **Test:** `test_exact_public_citation_provenance_is_required`.

#### Scenario: Storage or capture would enter the skill trust boundary
- **GIVEN** collection, indexing, troubleshooting capture or retry is requested
- **WHEN** artifacts are saved
- **THEN** reports remain outside skill discovery, index text is escaped, persisted captures omit unrelated chrome, and previous attempts remain intact
- **Test:** `test_storage_index_capture_and_retry_preserve_boundaries`.
