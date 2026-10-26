# Plan review: `add-desktop-deep-research`

**Verdict: REQUEST_CHANGES.** The plan is candid about what is unproven, and several of its safety rules are sound. Six gaps are architectural rather than live-test obligations:

- conversation identity is undefined;
- the commands cannot drive the declared states;
- duplicate prevention stops at the job id;
- the source-scope control is missing from the specs;
- the single acceptance send is also the first contact with the real UI;
- automation taking focus from an active user is not addressed.

## Lane

- **Profile:** `policy4j_arch_review_claude_fable`, requested model `fable`, effort `max` (`.agents/cao-profiles/policy4j_arch_review_claude_fable.md:10-13`).
- **Runtime identity:** Claude Fable 5.1, model id `claude-fable-5-1`, reasoning effort max.
- **Independence:** the artifacts attribute planning to a Codex/GPT lane (`openspec/changes/add-desktop-deep-research/research/README.md:83`, `openspec/changes/add-desktop-deep-research/proposal.md:77-78`). This is therefore the different-family review that `docs/ai/OPENSPEC.md:40-42` and `docs/ai/SWARMS.md:53` require.
- **Lane count:** this is one lane. `openspec/changes/add-desktop-deep-research/tasks.md:12` asks for architecture and privacy review; privacy architecture is covered here, but it is not a second lane.
- **Java scope:** module boundaries, public API and JEP 401 readiness are untouched (`openspec/changes/add-desktop-deep-research/proposal.md:36`).

## Checks performed

Eleven `Read` calls and nothing else: `AGENTS.md`, `docs/ai/OPENSPEC.md`, `docs/ai/SWARMS.md`, the profile, and the seven listed change artifacts, each in full. No shell, search, git, `openspec`, build, test, MCP, callback or desktop action ran. No file was written, including no plan-mode file.

## Findings

HIGH means correct the plan before approval. MEDIUM means correct it before the affected task group starts. LOW means clarify. Product behavior I could not check is marked UNVERIFIED.

### HIGH

**1. Conversation identity is undefined, unproven, and has no re-location procedure**

- **Evidence:**
  - The handle is primary and the marker only corroborates (`openspec/changes/add-desktop-deep-research/design.md:78-82`).
  - A missing identity keeps the job uncertain (`openspec/changes/add-desktop-deep-research/design.md:92-93`).
  - Resume depends on "the same observed conversation" (`openspec/changes/add-desktop-deep-research/specs/desktop-research-execution/spec.md:72-75`).
  - The probe saw 18 and 11 Accessibility nodes, no research controls and one OCR label (`openspec/changes/add-desktop-deep-research/research/README.md:43-49`).
  - The artifacts alternate between a "dedicated job window" (`openspec/changes/add-desktop-deep-research/design.md:71-74`) and a "new dedicated conversation" (`openspec/changes/add-desktop-deep-research/specs/desktop-research-execution/spec.md:25`).
- **Gap:** nothing says what a handle is for either app, which channel yields it, or how long it is valid. Nothing says how a job's conversation is found and re-verified before `status`, a plan action or `collect`. One window shows different conversations over time, so window identity does not bind collection. If no handle is observable, every job stays `SUBMISSION_UNCERTAIN` and the objective cannot be met.
- **Why it is not delegated:** `openspec/changes/add-desktop-deep-research/design.md:227-228` delegates controls and collection to live tests, not the identity model.
- **Correction:** in D4, define ordered handle classes, each with its validity and the actions it permits:
  - a provider-stable identifier observed without sharing;
  - a session binding of verified app identity, PID plus process start time, window id, and the marker seen in the submitted user turn;
  - re-location after relaunch by marker, accepted only on exactly one match.
- **Correction, continued:** treat "marker in the submitted user turn" as eligible primary evidence and "marker echoed by the model" as corroboration only. Add two scenarios:
  - the conversation cannot be re-located: observation unobservable, provider state kept, no resubmission;
  - the window shows a different conversation: no mutation, no collection.
- **Proof:** named fake-boundary tests for both scenarios. Add a live `conversation_rebind_after_relaunch` check per provider, done by relaunching the app between receipt and collection at no extra submission.

**2. The D5 commands cannot drive the D4 states, and there is no transition table**

- **Evidence:**
  - States include `AWAITING_CLARIFICATION`, `AWAITING_PLAN` and a commanded `CANCELLED` (`openspec/changes/add-desktop-deep-research/design.md:84-93`).
  - The seven commands have no approve, answer, cancel, list or re-send, and `status` is "no submission" (`openspec/changes/add-desktop-deep-research/design.md:100-108`).
  - The spec has the workflow "evaluate the plan" and "begin research" (`openspec/changes/add-desktop-deep-research/specs/desktop-research-execution/spec.md:46-56`).
  - The plan's research says ChatGPT can ask clarifying questions and presents a plan (`openspec/changes/add-desktop-deep-research/research/README.md:13-14`).
  - Provider output is "data, not instructions" (`openspec/changes/add-desktop-deep-research/design.md:73`), and default diagnostics redact conversation text (`openspec/changes/add-desktop-deep-research/specs/desktop-research-execution/spec.md:95-97`).
- **Gap:**
  - No command owns the plan-start click or states its lock and evidence gate.
  - No evaluator is named. A standard-library script cannot judge whether a plan is "within the authorized question". If the calling agent judges, provider-authored text drives a GUI action and must be shown despite redaction.
  - `CANCELLED` has no command, scenario or test.
  - There is no outcome for "reconciled: not sent", and `start` always creates a new job.
  - The quota outcome has no named state (`openspec/changes/add-desktop-deep-research/specs/desktop-research-execution/spec.md:64-68`).
  - `BLOCKED` does not record whether a prompt may have been sent.
  - There is no `list`; a caller that lost the id must read the run directory by hand.
- **Correction:** add a D4 transition table giving, for each state and event, the next state, the guard, whether the desktop is mutated, and the owning command. State one v1 rule for plans and questions. I recommend human handoff only: the tool records `AWAITING_*` and never clicks, with a `respond --job` command added later if automation is wanted. Add `cancel` or make `CANCELLED` observed-only. Add `list`. Record `prompt_possibly_sent` on every blocked or failed job.
- **Proof:** one named test per transition row. A test that only `start` (and `respond`, if added) can reach the input boundary. Live receipts that record the actual state path and who resolved each plan or question.

**3. Duplicate prevention stops at the job id**

- **Evidence:**
  - `start` is "new dedicated research job; print id and observed state", and `batch` queues jobs (`openspec/changes/add-desktop-deep-research/design.md:104-108`).
  - Recovery is defined "on restart" for a known job (`openspec/changes/add-desktop-deep-research/design.md:90-92`), and the only scenario is "when the job is resumed" (`openspec/changes/add-desktop-deep-research/specs/desktop-research-execution/spec.md:84-89`).
  - The lock is shared across checkouts, but jobs live in the skill's `runs/` (`openspec/changes/add-desktop-deep-research/design.md:117-118`, `openspec/changes/add-desktop-deep-research/design.md:126-127`).
  - Worktrees are a normal mode in this project (`docs/ai/SWARMS.md:166-167`).
- **Gap:**
  - If `start` is killed after the send but before the caller has the id, the natural retry is another `start`, which is a second submission. An agent tool timeout is enough to cause this.
  - Rerunning `batch` after a crash resubmits earlier entries.
  - `openspec/changes/add-desktop-deep-research/specs/desktop-research-execution/spec.md:75` can be read globally, but nothing enforces it for a new `start`.
  - A second checkout cannot see the first checkout's uncertain job.
  - There is no per-job lock, so concurrent `wait` and `collect` can lose updates to `job.json`.
- **Correction:**
  - Keep a user-scoped submission ledger beside the lock, consulted under the lock by `start` and `batch`.
  - Persist and print the job id before the first desktop mutation.
  - Refuse `start` while that provider has an unresolved `SUBMISSION_UNCERTAIN` job, or a non-terminal job with the same question hash, unless an explicit override is given.
  - Write a batch manifest mapping entries to job ids before first input, and make a rerun resume it.
  - Add a per-job lock with a revision check, and sync the write-ahead record to disk.
- **Proof:** `test_start_retry_after_killed_start_does_not_resubmit`, `test_start_refuses_while_provider_job_uncertain`, `test_batch_rerun_resumes_without_resubmission`, `test_ledger_visible_across_checkouts`, `test_concurrent_job_updates_keep_state`.

**4. The public source-scope control is not normative**

- **Evidence:**
  - D5 defines the default, the UI selection and the blocked outcome (`openspec/changes/add-desktop-deep-research/design.md:110-115`).
  - The requirement titled "readiness and scope" lists identity, version, permissions, window, controls and account readiness, but not sources (`openspec/changes/add-desktop-deep-research/specs/desktop-research-execution/spec.md:3-8`).
  - Scope appears only at plan time (`openspec/changes/add-desktop-deep-research/specs/desktop-research-execution/spec.md:58-62`).
  - The evidence index names no source-scope test (`openspec/changes/add-desktop-deep-research/evidence/README.md:13`), although the tasks mention one (`openspec/changes/add-desktop-deep-research/tasks.md:17-19`).
- **Gap:** verification audits requirements (`docs/ai/OPENSPEC.md:49-51`), so the completion audit can pass with this control absent. Three decisions are also missing:
  - the accepted values of `--source-scope`;
  - what happens when the selection cannot be observed ("when supported" leaves it open);
  - whether selecting sources, enabling web search (`openspec/changes/add-desktop-deep-research/research/README.md:20-22`) or toggling research mode changes persistent UI state, which the non-goals forbid (`openspec/changes/add-desktop-deep-research/design.md:19`).
- **Correction:** add a requirement "Constrain and verify source scope before submission". Accept only `public-web` in v1. Scenarios:
  - verified public-only selection: the receipt holds the evidence;
  - private source enabled and not constrainable: blocked, no prompt;
  - selection unobservable: blocked;
  - persistent toggles are restored or never touched.
- **Proof:** `test_start_blocks_when_source_scope_unverifiable`, `test_start_blocks_on_unconstrainable_private_source`, `test_source_selection_side_effects_restored`. Live `source_scope_verified` for both providers. A post-collection check that `sources.json` holds only public http(s) URLs.

**5. There is no live characterization; the one acceptance send is also first contact**

- **Evidence:**
  - Live sends are "bound to one submission per provider" (`openspec/changes/add-desktop-deep-research/design.md:172`).
  - Approval authorizes only the acceptance questions (`openspec/changes/add-desktop-deep-research/design.md:148-151`).
  - Task 2.3 lists "observed send receipt", but its check is preflight and readiness (`openspec/changes/add-desktop-deep-research/tasks.md:19`).
  - The first sends are tasks 4.1 and 4.2 (`openspec/changes/add-desktop-deep-research/tasks.md:31-32`).
  - Input and collection are unverified (`openspec/changes/add-desktop-deep-research/research/README.md:59-61`).
- **Gap:** groups 2 and 3 fix the state model, the fakes and the specs before any real send, receipt, handle, plan, completion or export has been observed. The fakes therefore encode guesses. A first-contact defect in the acceptance run leaves an uncertain or failed job and no authorized retry.
- **Correction:** add a task 1.4 before 2.1, authorized and budgeted by the maintainer. Per provider it runs non-submitting control discovery, then a stated small number of characterization submissions with a public throwaway prompt, never counted as acceptance. Its output is:
  - the provider/version profile;
  - the handle class available and the input method;
  - completion-predicate candidates and copy/export behavior;
  - a go/no-go decision.
- **Correction, continued:** state the acceptance retry budget and re-authorization rule in D7. Add one read-only research item: list each bundle's declared URL schemes, services and scripting dictionary before committing to OCR-driven control (UNVERIFIED whether any exist).
- **Proof:** an `evidence/builds/<NNN>-live-characterization/` run with redacted profiles and the decision. The fakes in 2.1 and 3.1 are derived from those recorded observations.

**6. Automation taking focus from an active user is unaddressed**

- **Evidence:**
  - Claude exposed nodes only "after activation" and had "no qualifying visible window" for capture (`openspec/changes/add-desktop-deep-research/research/README.md:43-49`).
  - Polling "can interleave observations" outside the lock (`openspec/changes/add-desktop-deep-research/design.md:121-122`).
  - The only user-interaction risk listed is the user intervening (`openspec/changes/add-desktop-deep-research/design.md:213`, `openspec/changes/add-desktop-deep-research/specs/desktop-research-execution/spec.md:38-42`).
- **Gap:** the plan handles the user taking focus from the automation, not the reverse. If observation needs activation or visibility, as the plan's own probe suggests, each `wait` poll during a long run can move keyboard focus into the provider's composer. The user's keystrokes then land in the job conversation and may be sent. Activation, un-minimizing, navigation and scrolling are not classed as mutations. Neither is opening the composer menu that `doctor` may need (`openspec/changes/add-desktop-deep-research/design.md:102-103`).
- **Correction:** in D3 and D5:
  - make observation non-activating by default, returning unobservable instead of activating;
  - class activation, navigation, scrolling and menu opening as mutations under the lock;
  - require an attended precondition for any mutation (operator confirmation or measured input idle time), and restore the previously frontmost app;
  - add the scenario "user is active when a mutation is due": busy or deferred, no activation.
- **Proof:** `test_wait_polling_never_activates`, `test_mutation_deferred_while_user_active`. Live receipts record activation counts during the expired wait.

### MEDIUM

**7. Artifacts do not carry synthetic-versus-live provenance**

- **Evidence:** tests inject a fake desktop boundary into the production CLI and are "labeled offline" (`openspec/changes/add-desktop-deep-research/design.md:162-164`). Retained live proof is "summarized/redacted" (`openspec/changes/add-desktop-deep-research/evidence/README.md:40-42`).
- **Gap:** no receipt or manifest field separates fake-boundary output from live output, or ties a live receipt to the helper build. The injection seam can therefore produce `COLLECTED` receipts with no desktop. The committed live-evidence set is undefined, which risks either leaking the conversation handle or keeping unverifiable prose.
- **Correction and proof:**
  - Stamp every receipt and manifest with `boundary: live|synthetic`, the helper binary hash and its source hash. Synthetic mode writes only to temporary output.
  - Define the committed set: transition log, counts, file hashes, a salted hash of the handle, app identity and version, and the public source URLs.
  - Decide whether the two public acceptance reports are retained.
  - Test `test_synthetic_receipts_are_stamped_and_rejected_by_live_audit`; task 4.3 checks the helper hash against source.

**8. Reconciliation, duplicate counts and batch lack live or independent proof**

- **Evidence:** crash-after-send and batch have fake-boundary tests only (`openspec/changes/add-desktop-deep-research/specs/desktop-research-execution/spec.md:84-110`). Task 4.3 inspects "duplicate-submission counts" with no stated oracle (`openspec/changes/add-desktop-deep-research/tasks.md:33`).
- **Correction and proof:**
  - Inside the single acceptance submission, force the uncertain path by bounding receipt observation to zero or stopping the process at the send step. Then reconcile with `status`, and name the check live `uncertain_send_reconciliation`.
  - Define the duplicate oracle (exactly one conversation carries the marker, checked without logging other titles), or label the count self-attested.
  - Run the two acceptance jobs through `batch`.

**9. App identity has no trust anchor, and version profiles have no promotion procedure**

- **Evidence:** identities are discovered from installed metadata (`openspec/changes/add-desktop-deep-research/design.md:62-67`). The ChatGPT bundle declares `com.openai.codex` and no map is fixed (`openspec/changes/add-desktop-deep-research/research/README.md:35-37`). Version drift is a listed risk (`openspec/changes/add-desktop-deep-research/design.md:207`).
- **Gap:** with neither name nor bundle id pinned, discovery is ambiguous when several copies or similarly named bundles exist. "Tested provider/version profile" has no definition, storage or approval step, so the visual path either blocks after each app update or the rule gets waived. Nothing covers a version change mid-job.
- **Correction and proof:**
  - Pin each provider's code-signing identity in its profile and verify it on the running process. Several candidates block.
  - Define profile promotion: a non-submitting `doctor` conformance check, recorded evidence, and a named approver.
  - A version change mid-job blocks observation, keeps state and never resubmits.
  - Tests for signature mismatch, two candidates, untested version and mid-job change.

**10. The bridge contract and privacy boundary are unstated**

- **Evidence:** Python orchestrates and Swift acts (`openspec/changes/add-desktop-deep-research/design.md:48-58`). Fresh evidence is required before each mutation, and logs are allowlisted (`openspec/changes/add-desktop-deep-research/design.md:63-73`, `openspec/changes/add-desktop-deep-research/specs/desktop-research-execution/spec.md:112-116`).
- **Gap:**
  - Verify-then-act across two processes is a check-then-act race.
  - Where filtering happens is unspecified. If raw OCR or Accessibility text reaches Python, it can reach logs and stdout, which the invoking agent ingests and sends to its model provider.
  - There is no closed list of permitted UI actions, and no rule for unexpected sheets or dialogs inside the right window.
  - The helper inherits Accessibility and Screen Recording, yet its "ignored local cache" has no location, ignore rule or integrity check. Only `runs/.gitignore` is declared.
- **Correction and proof:**
  - Each mutating bridge call carries its precondition (verified identity, PID and start time, window, control, focus). It re-verifies and hit-tests in-process immediately before the event and returns before/after evidence.
  - Bridge output is a closed schema of control ids, flags, geometry and hashes, with marker matching done inside the bridge.
  - Add an action allowlist as a SHALL. An unexpected modal blocks and is never dismissed.
  - Build the helper into an owner-only user cache keyed by source hash, and verify it before each run.
  - Tests for precondition mismatch, schema closure and modal handling.

**11. The input method is not chosen, and the write-ahead point is too late**

- **Evidence:** intent is persisted "before sending" (`openspec/changes/add-desktop-deep-research/design.md:90`, `openspec/changes/add-desktop-deep-research/specs/desktop-research-execution/spec.md:25-29`).
- **Gap:** setting the field value, synthesized keystrokes and paste fail differently. Keystrokes depend on global focus for the whole entry, and a newline in a multi-line prompt may send a partial prompt (UNVERIFIED per app). Paste puts the prompt on the system pasteboard and overwrites the user's clipboard. Entry can therefore submit, or leave a draft, before intent is recorded.
- **Correction and proof:**
  - D3 names the permitted input methods in preference order.
  - The write-ahead record precedes the first composer input.
  - Before a separate single send action, read the composer back, or place the marker at both ends of the prompt as a truncation check.
  - Tests `test_intent_persisted_before_first_input`, `test_multiline_prompt_cannot_submit_during_entry`, `test_truncated_entry_blocks_send`.

**12. Copy-path binding and citation provenance are undefined**

- **Evidence:** "track pasteboard change count and bind the copied response to the verified job" (`openspec/changes/add-desktop-deep-research/design.md:132-137`). The scenario rejects only preexisting content (`openspec/changes/add-desktop-deep-research/specs/desktop-research-collection/spec.md:36-41`).
- **Gap:**
  - The change count shows that some process wrote, not which one. A foreign write between click and read is read and may be saved.
  - No binding predicate is given.
  - The channel for source URLs is open. URLs read by OCR become fabricated citations when misread.
  - "Incomplete citation extraction" has no rule.
  - The report stays on the clipboard, and the user's clipboard is overwritten without notice.
- **Correction and proof:**
  - Define the predicate: exactly one change-count step, under the lock, within a bounded window after a Copy control verified in the bound conversation, plus content corroboration against text observed in the job's report.
  - Read every report flavor on the pasteboard (plain, HTML, rich text) and take URLs from link targets, never from OCR.
  - Define completeness: every citation marker resolves, with count parity against the sources panel when observable.
  - Clear the pasteboard only if the change count is still the one the helper observed, and disclose the overwrite in the skill text.
  - Tests `test_copy_rejects_foreign_clipboard_write`, `test_sources_never_derived_from_ocr`, `test_clipboard_cleared_only_when_unchanged`.

**13. Completion rests on absence under partial observability**

- **Evidence:** completion needs final-result evidence "and the absence of pending plan/question/generation controls" (`openspec/changes/add-desktop-deep-research/research/README.md:73-74`, `openspec/changes/add-desktop-deep-research/specs/desktop-research-collection/spec.md:5-13`).
- **Gap:** a blank, occluded, scrolled, minimized or locked-screen observation also shows no pending controls. The positive evidence is undefined beyond what the spec excludes "alone".
- **Correction and proof:** every observation carries a completeness flag. Absence counts only when the observation is complete; otherwise the result is unobservable. Each provider profile lists the positive predicate. Test `test_incomplete_observation_cannot_establish_completion` with blank, occluded and scrolled fixtures.

**14. Report location and capture scope cross trust boundaries**

- **Evidence:** runs default to the skill's `runs/` (`openspec/changes/add-desktop-deep-research/design.md:126-130`), reachable through the discovery link (`openspec/changes/add-desktop-deep-research/design.md:34-36`). Troubleshooting captures are "limited to the dedicated job window" (`openspec/changes/add-desktop-deep-research/design.md:70-73`). Rollback "never deletes" reports (`openspec/changes/add-desktop-deep-research/design.md:219-220`).
- **Gap:**
  - Untrusted provider output sits inside a tree that agents load as skill resources.
  - A rollback that removes the added skill directory rather than only the link deletes the reports. So do `git clean -x` and worktree cleanup (`docs/ai/SWARMS.md:214-215`).
  - A window capture includes sidebar titles and account chrome, so it does not meet "limited to the selected job" (`openspec/changes/add-desktop-deep-research/specs/desktop-research-execution/spec.md:95-97`).
  - An agent can set the troubleshooting flag itself.
- **Correction and proof:**
  - Default the output root outside any skill tree: user-scoped, or a dedicated self-ignoring directory.
  - Generate `index.md` from manifests with report-derived strings escaped.
  - Crop persisted captures to the conversation region.
  - Make troubleshooting capture an operator-only opt-in that never prints to stdout.
  - Use attempt-numbered collection directories so a retry never overwrites (`openspec/changes/add-desktop-deep-research/specs/desktop-research-collection/spec.md:52-56`).
  - One test for each.

**15. Desktop lock semantics are loose**

- **Evidence:** "one mutating desktop action at a time" (`openspec/changes/add-desktop-deep-research/specs/desktop-research-execution/spec.md:93-94`). The lock is held "during every desktop mutation" in a "temporary/cache directory" (`openspec/changes/add-desktop-deep-research/design.md:117-122`).
- **Gap:** this permits releasing the lock between steps of one logical transaction, such as verify mode, enter, send, or click Copy then read the pasteboard. Another process can then interleave. A path derived from a temporary directory can differ between sessions with different temp roots, which removes mutual exclusion (UNVERIFIED for the sandboxes in use).
- **Correction and proof:** the lock spans the whole transaction. Its path is fixed, user-scoped and independent of temp-directory settings, with ownership and mode checked. Lock order is job, then desktop. Add an interleaving test at transaction granularity and a path test under an altered temp environment.

**16. Skill evaluations can drive the real desktop**

- **Evidence:** evaluation 1 is both-provider cited collection, run with-skill and baseline (`openspec/changes/add-desktop-deep-research/design.md:177-180`). Live sends are bound to one per provider (`openspec/changes/add-desktop-deep-research/design.md:172`). There are to be no concurrent desktop writers (`openspec/changes/add-desktop-deep-research/design.md:195-197`).
- **Gap:** with-skill lanes would spend real submissions. Baseline lanes may improvise GUI automation outside the lock. Pass criteria, graders and trial counts are missing.
- **Correction and proof:** run all evaluation lanes with the synthetic boundary forced and real desktop control denied. Define assertions per prompt and the trial count, and store transcripts.

**17. The invoking agent may be hosted by the target app**

- **Evidence:** the plan's research says the installed ChatGPT bundle is the unified app containing Codex (`openspec/changes/add-desktop-deep-research/research/README.md:17-19`, `openspec/changes/add-desktop-deep-research/research/README.md:35-36`). The skill is for Codex and Claude sessions (`openspec/changes/add-desktop-deep-research/proposal.md:11-12`).
- **Gap:** if the calling session runs inside the app being driven (UNVERIFIED for either app), "new dedicated conversation" can navigate or type into the caller's own window.
- **Correction and proof:** `doctor` detects whether the caller's process ancestry belongs to the target app, then excludes that window or blocks. One scenario and one test.

**18. The proposal promises Swift bridge tests; the design and tasks only compile it**

- **Evidence:** `openspec/changes/add-desktop-deep-research/proposal.md:52-53`, `openspec/changes/add-desktop-deep-research/design.md:167`, `openspec/changes/add-desktop-deep-research/tasks.md:2-3`.
- **Correction and proof:** add a helper self-test (label matching, coordinate mapping, schema, filtering) to the gate with its exact command, or amend the proposal.

### LOW

**19. Downloads.** `openspec/changes/add-desktop-deep-research/design.md:137-139` leaves four things undecided:
- save-panel handling;
- the Downloads-folder permission check in `doctor`;
- binding by before/after snapshot without logging unrelated names;
- whether the original stays in the user's Downloads.

**20. Design-only prohibitions.** Never sharing or publishing to get a handle (`openspec/changes/add-desktop-deep-research/design.md:82`) and never capturing the whole desktop (`openspec/changes/add-desktop-deep-research/design.md:72`) should be SHALL NOTs so the audit covers them.

**21. Spec wording.**
- "Independent outcome per provider" should read per job (`openspec/changes/add-desktop-deep-research/specs/desktop-research-execution/spec.md:94-95`).
- Live `provider_readiness` omits "for both providers" (`openspec/changes/add-desktop-deep-research/specs/desktop-research-execution/spec.md:15`).
- `batch` does not say whether it waits and collects, or how it handles two jobs on one provider (`openspec/changes/add-desktop-deep-research/design.md:108`).

**22. Evidence process.**
- `evidence/reviews/security/` (`openspec/changes/add-desktop-deep-research/tasks.md:20`) is not a defined lane (`docs/ai/OPENSPEC.md:94`).
- Preassigned run numbers (`openspec/changes/add-desktop-deep-research/tasks.md:17-34`) collide with "number runs in order; never reuse" on any rerun (`docs/ai/OPENSPEC.md:101-102`).
- `builds/002-plan-validation/` is referenced but absent from Runs (`openspec/changes/add-desktop-deep-research/evidence/README.md:26-38`), although validation precedes review (`docs/ai/OPENSPEC.md:39-42`).
- The planning probe launched ChatGPT, activated Claude and captured a window (`openspec/changes/add-desktop-deep-research/research/README.md:41-49`). Confirm that run 001 records who authorized that (UNVERIFIED; the run is not in this packet).

**23. Skill discovery.**
- Require a relative symlink; an absolute one embeds a checkout path (`docs/ai/OPENSPEC.md:123-124`).
- The repository's documented Codex skill location is `.codex/skills/` (`docs/ai/OPENSPEC.md:61`). The canonical `.agents/skills` placement rests on an external document this lane could not check (`openspec/changes/add-desktop-deep-research/research/README.md:97-101`, UNVERIFIED).
- Define the duplicate-discovery check for task 3.3 (`openspec/changes/add-desktop-deep-research/design.md:37-38`).
- Confirm the link survives `openspec init` regeneration (`AGENTS.md:148-150`).
- State that Agy, Grok and Kimi are out of scope (`docs/ai/SWARMS.md:19-20`).

**24. Research policy.** Say whether a desktop report may back an OpenSpec `research/` answer. `AGENTS.md:170-171` requires fetched sources, and a provider report is a secondary synthesis. `docs/ai/DESKTOP_RESEARCH.md` is not linked from `AGENTS.md`, and linking it would widen `openspec/changes/add-desktop-deep-research/proposal.md:67-68`.

**25. Approval receipt.** `openspec/changes/add-desktop-deep-research/tasks.md:13` should enumerate the exact prompts, submission budgets, permissions to grant and attended windows. Require a confirmation just before each live send.

**26. Rollback residue.** `openspec/changes/add-desktop-deep-research/design.md:217-221` omits the helper cache, the lock file and the granted macOS permissions.

**27. Small specifics.**
- If OCR matching is used, give the marker an OCR-robust alphabet and a check digit (`openspec/changes/add-desktop-deep-research/design.md:78-80`).
- Add a salted account hash to detect an account switch on resume.
- Add a minimum Python version and toolchain check to `doctor` (`openspec/changes/add-desktop-deep-research/design.md:48-50`).

**ADR.** I agree none is required (`openspec/changes/add-desktop-deep-research/design.md:156-158`). The trust-boundary decisions from findings 6, 9 and 10 should land as a threat-model section in `docs/ai/DESKTOP_RESEARCH.md`.

## Delegated to implementation or live acceptance (not counted as findings)

- Whether research controls are reachable, account entitlement, and input reliability (`openspec/changes/add-desktop-deep-research/design.md:227-228`, `openspec/changes/add-desktop-deep-research/research/README.md:59-61`).
- Claude's per-report export format (`openspec/changes/add-desktop-deep-research/research/README.md:23-26`).
- The compiled helper's permission identity (`openspec/changes/add-desktop-deep-research/design.md:56-58`).
- Codex and Claude discovery verification (`openspec/changes/add-desktop-deep-research/tasks.md:26`).
- Provider-specific completion evidence, apart from the completeness rule in finding 13.

## What holds

- Provider, collection and observation states are separate, and a timeout is not a failure (`openspec/changes/add-desktop-deep-research/design.md:84-88`).
- Intent is written ahead, and there is no automatic resubmission (`openspec/changes/add-desktop-deep-research/design.md:90-92`).
- Ordinary chat, an API or a browser is never silently substituted (`openspec/changes/add-desktop-deep-research/specs/desktop-research-execution/spec.md:7-8`).
- Mocks cannot satisfy acceptance (`openspec/changes/add-desktop-deep-research/design.md:152-154`).
- The lock is released by the OS rather than tracked by a PID file, and downloads are never picked by newest filename (`openspec/changes/add-desktop-deep-research/design.md:119-121`, `openspec/changes/add-desktop-deep-research/design.md:137-139`).
- Evidence is labelled planning-only (`openspec/changes/add-desktop-deep-research/evidence/README.md:3-4`).

## Unrun gates and unverified items

- `openspec validate add-desktop-deep-research --strict` was not run by this lane, and no validation run is indexed.
- Python unit tests, Swift compilation, provider live checks and skill evaluations are unrun, as expected before approval.
- A second review lane and maintainer approval are pending (`openspec/changes/add-desktop-deep-research/tasks.md:12-13`).
- Not inspected, so local-probe facts above are cited from the plan's own summaries:
  - `evidence/builds/001-local-capabilities/`;
  - the external URLs in the research file;
  - the current state of `.agents/skills`, `.claude/skills` and `.codex/skills`;
  - `openspec/config.yaml` and the schema;
  - the Beads epic.

## Conditions for APPROVE

1. Resolve findings 1–6 in the artifacts, with the named scenarios and tests.
2. For findings 7–18, either edit the plan or add explicit tasks.
3. Rerun strict validation and index it.
4. Have an independent lane review the deltas.
5. Store this report as a run under `openspec/changes/add-desktop-deep-research/evidence/reviews/plan/` in the `rvNNN-<scope>-<lane>` form (`docs/ai/OPENSPEC.md:101-102`).

Residual risk after these corrections: feasibility on both apps stays unproven until the characterization task runs. D7 already prevents a completion claim if either provider fails (`openspec/changes/add-desktop-deep-research/design.md:152-154`).

DESKTOP_RESEARCH_PLAN_REVIEW_DONE
