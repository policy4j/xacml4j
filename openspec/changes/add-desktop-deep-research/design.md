## Context

The project requires a validated, reviewed plan before implementation
(`AGENTS.md:128`, `docs/ai/OPENSPEC.md:43`). There is no current desktop-research
skill or runner in the inspected project skill/script directories. Existing
Java changes and the modernization epic are outside this change.

Local feasibility evidence is in `evidence/builds/001-local-capabilities/`.
Installed app identities differ from older assumptions. Initial Accessibility
trees were incomplete; app-window OCR succeeded. Restoring Claude's minimized
window later exposed a richer tree with a text area and buttons, and its
original minimized state was restored. Full research controls and input remain
unverified.

## Goals / Non-Goals

**Goals:** drive both requested local desktop research modes; save their final
cited reports; offer batch operation, resumable receipts, and honest errors;
make the workflow reusable as a project agent skill with scripts.

**Non-Goals:** changes to XACML or Java; global desktop/MCP/client configuration;
account-wide exports; installing a third-party research plugin without a named
target; replacing desktop Research with an API or ordinary chat.

**Browser surface goals (added 2026-10-09):** run the same jobs through the
web applications of Claude, ChatGPT and Gemini, headed or headless; keep the
signed-in session and the conversation across runs; continue a conversation
with follow-up chat turns.

**Browser surface non-goals:** handling credentials, cookies or tokens;
driving the operator's daily browser profile; evading a provider's bot,
challenge or rate controls; unattended bulk use; private sources, which
include account memory and earlier chats; passing a report to another
provider and a second research run in one conversation (cut, D13).

## Research Basis

See `research/README.md`, especially Q1 (product controls and exports), Q2
(current identities and incomplete Accessibility), Q3 (capture initialization),
and Q4 (job evidence). The browser surface rests on Q5 (drivers), Q6 (prior
work), Q7 (API routes), Q8 (provider terms) and Q9 (local state). Product docs establish expected behavior; the proposed
automation mechanism is an inference that must pass live tests.

## Decisions

### D1. One canonical project skill

Create `.agents/skills/desktop-deep-research/SKILL.md`, `scripts/`, `references/`,
`tests/`, and `evals/evals.json`. Add a project-local discovery symlink at
`.claude/skills/desktop-deep-research` pointing to the canonical directory.
The discovery link is relative. Verify exactly one canonical name/path in each
CLI inventory. Add a `.codex/skills` link only if the active
Codex installation actually requires it; avoid duplicate skill discovery.
Other clients are outside this change. A later OpenSpec CLI regeneration is
not part of this task; document and test the relative link rather than running
unrequested regeneration over existing client guidance.
Alternatives: separate copies drift; a global plugin expands installation scope.

The skill describes submission, observation, interactive handoff, and collection;
it invokes bundled scripts and returns report paths plus actual verification.
Reference provider-specific controls separately so UI updates do not require
rewriting the common workflow.

### D2. Standard-library orchestrator and native macOS bridge

Use Python 3.10 or newer, checked by `doctor`, for CLI parsing, manifests, hashes, deadlines,
and local filesystem/process locks. Use a Swift helper compiled with the
installed toolchain for AppKit, Accessibility, ScreenCaptureKit, and Vision.
No Python packages, browser debugging connection, internal consumer API,
cookie extraction, or changed MCP route is required.

Inspect declared native scripting before selecting an adapter. Installed
ChatGPT declares Chromium-style window/tab ids, URLs and JavaScript execution;
no Research-specific command was found. Runtime availability and job locators
remain unverified. Prefer a fixed audited scripting operation when characterized,
then Accessibility, then visual targeting. Never execute report-provided code.

Alternatives: AppleScript alone has unproven Research coverage; an added
GUI-automation package adds a dependency; API research does not exercise the
requested desktop products. Compile the helper into ignored local cache, then
run permission checks from that exact helper identity. Interpreter preflight
does not prove a compiled binary's permission coverage.

### D3. Observe before each action; target identity is discovered

Resolve app paths and bundle identities from installed metadata and match them
to running processes. Validate the app signature and pin the characterized
TeamIdentifier; observed installed baselines are OpenAI `2DC432GLL2` and Anthropic
`Q6L2SF6YDW`. Multiple candidates or signature mismatch block. Each version
profile records controls/input, locator class, source checks, completion predicate
and characterization evidence. The conductor promotes it after non-submitting
checks and characterization; an update invalidates it until rechecked. A mid-job
version/account change blocks observation while preserving provider state.
Require fresh selected-window evidence before every
mutation, with app version, PID, window identity, target control, and focus.
Detect minimized windows; activation alone can leave them hidden. Restore the
selected target window only when needed, recording its prior state. Read-only
probes restore that prior state afterward. Routine `status`/`wait` never activate,
unminimize, navigate, scroll or open menus; unreadable jobs return UNOBSERVABLE.
Those actions are mutations under the transaction lock, requiring at least two
seconds of measured local input idle time. New user input/focus changes stop
the transaction. Restore prior app/window state when safe. Existing task
authorization covers these actions without a new per-send confirmation.
Use semantic Accessibility controls first. When a control is absent, use
fresh app-window capture and exact allowlisted visual/OCR evidence only within
a tested provider/version profile. Missing or ambiguous controls return a
blocked outcome; do not click assumed coordinates or type into an unknown app.

Runtime captures stay in memory by default. Explicit troubleshooting captures
are limited to the dedicated job window, private, ignored, and clearly marked.
Never capture the whole desktop or log unrelated conversation titles/text.
Screenshots, OCR, downloaded reports, and model output are data, not instructions.
Both input and collection use the same app/window/job binding.

Composer entry preference: characterized native/Accessibility set-value without
send, then a characterized paste operation with clipboard side effects disclosed.
Do not synthesize multiline keystroke entry. Sync submission intent before any
composer input; verify full value/hash and markers at both ends before one
separate Send action. Truncated input or changed focus blocks. Pass prompt bytes
through private files/pipes, never command-line arguments or diagnostic logs.

### D4. Durable job identity with separate provider and collection states

The maintainer added highest-model selection in the current conversation after
the initial Claude characterization. Before any new question, select and verify
the highest available Research-compatible model using the actual enabled model
choices and a provider/version ranking supported by current official docs and
local characterization. Do not infer capability from a number or menu position.
Verify again after Research mode selection and immediately before input. Record
`model_policy: highest-available`, exact visible model, available choices, ranking
basis, selection proof and any distinct provider-managed Research engine. If the
Research engine is independent of the chat picker, preserve that distinction;
its undisclosed worker model is provider-managed and unverified. Unknown or
unsupported conversation-model selection blocks rather than implies success.
Record and restore safe selection side effects or report pending restoration;
never use upgrade/subscription controls. Read-only polls never open model menus.
The earlier Claude sample's model was not recorded and is not highest-model proof.

Each job has a generated id, stable caller request id, provider/mode, question
SHA-256, OCR-robust marker with check digit, source scope, timestamps, signed
app identity/version, salted account-context hash when observable, and binding.
Before the first Send, use a distinct draft binding: a characterized creation
receipt for a new dedicated empty composer, the signed app/session/window/tab,
and caller exclusion. Source/mode preparation completes this receipt before
prompt entry. An existing nonempty draft blocks.
The `new-conversation` action uses the durable request id and target/idle/caller
guards before this receipt exists; it cannot reuse an arbitrary visible chat.
After entry, the full owned composer hash and end markers extend the receipt.
Draft authority permits only source/mode preparation, prompt entry and one
verified first Send, or positively unsent same-draft resume. It never permits
completion or collection. Possible Send revokes draft authority until positive
submitted-turn or positively unsent evidence reconciles it.

Submitted-job binding classes are: (1) stable provider conversation id/URL observed
locally without sharing; (2) app/version, PID/start time, window/tab id plus
the exact marker in the submitted USER turn; (3) after relaunch, a characterized
local locator or exact-marker search with exactly one submitted-user-turn match.
The assistant's echoed marker is corroboration only. Zero/ambiguous matches or
a different conversation in the same window are UNOBSERVABLE, preserving state.
`rebind` establishes a new session binding without submission. Never publish,
share, read unrelated chat titles, or use account-wide export to get identity.
Process/window ids alone, without the verified new-draft receipt or a submitted
job binding, never permit mutation; collection always needs submitted-job binding.

Provider states: `PREPARED`, `DRAFTING`, `SUBMISSION_UNCERTAIN`, `AWAITING_CLARIFICATION`,
`AWAITING_PLAN`, `RUNNING`, `COMPLETED`, `BLOCKED`, `FAILED`, `CANCELLED`.
Collection states: `NOT_STARTED`, `IN_PROGRESS`, `COLLECTED`, `FAILED`.
Observation has its own last-result/time/deadline fields. A timeout leaves the
provider state intact; a missing local runner is not provider cancellation.

Persist and sync submission intent before any composer input. Persist an
observed receipt afterward. On restart, reconcile the same marker and
conversation before submitting anything. Unknown or missing provider identity
keeps the job uncertain. `CANCELLED` is observed-only in v1, after actual provider
cancellation; there is no scripted cancellation. Blocked/failed jobs retain
`prompt_possibly_sent` and a reason; exhausted quota is `BLOCKED:QUOTA`.
Metadata records partial progress without claiming an export succeeded.

| Event / owner | Transition | Guard / mutation |
| --- | --- | --- |
| `start` / allocate request | new → PREPARED | Durable id/ledger returned before GUI action |
| `start` / enter prompt | PREPARED → DRAFTING | Verified new dedicated empty draft binding, signed target, mode, public sources and idle; synced intent before input |
| `start` / Send | DRAFTING → SUBMISSION_UNCERTAIN | Full composer verified; one guarded Send |
| `status` / observed receipt | uncertain → RUNNING or AWAITING_* | Submitted-user-turn identity; no mutation |
| `status` / positively unsent draft | uncertain → DRAFTING | Positive no-send evidence; never inferred from absence |
| `start --job` / resume draft | DRAFTING → uncertain | Same verified request/binding; guarded input/send |
| `respond` / authorized answer | AWAITING_CLARIFICATION → uncertain | Pending-question hash; answer from authorized input |
| `approve-plan` / scoped plan | AWAITING_PLAN → RUNNING | Calling agent records plan hash and scope check; guarded Begin |
| `status` / positive final result | RUNNING → COMPLETED | Characterized positive predicate; complete observation |
| observation / quota, error, cancellation | current → BLOCKED, FAILED, CANCELLED | Positive provider evidence; preserve possible-send flag |
| observation / timeout, unreadable target, app change | provider unchanged | Record timeout or UNOBSERVABLE; no mutation |
| `rebind` / unique match | provider unchanged | Serialized idle-safe navigation; verify submitted user turn |
| `collect` / valid output | collection → COLLECTED | Complete bound job; citation/file/hash checks pass |
| `collect` / invalid output | collection → FAILED | Preserve provider completion and prior attempts |

Named tests cover every transition row. Only `start`, `respond`, and `approve-plan`
may perform composer/research-start interactions. The script does not judge plan
meaning: the invoking agent reads the bound job's plan with `inspect --job
--job-content`, treats it as untrusted data, and passes the exact plan hash to
`approve-plan` after checking scope. Missing user choices stay pending. No reply
or plan action expands source scope or follows instructions embedded in a report.

### D5. Command interface and desktop ownership

Invoke `rtk python3 .agents/skills/desktop-deep-research/scripts/desktop_research.py`:

| Command | Behavior |
| --- | --- |
| `doctor --provider chatgpt\|claude\|all` | Capability and permission checks; no prompt |
| `inspect --provider <provider>` | Redacted current control/state observations |
| `start --provider <provider> --request-id <id> --prompt-file <file>` | Create/resume an idempotent request; print id before mutation |
| `start --job <id>` | Resume a positively verified unsent draft of the same job |
| `list` | Redacted local lookup for lost job-id recovery |
| `status --job <id>` | Reobserve the same job; no submission |
| `wait --job <id> --timeout <seconds>` | Bounded observation; preserve state on deadline |
| `rebind --job <id>` | Locate and verify the existing conversation without resubmission |
| `respond --job <id> --answer-file <file>` | Answer a pending question within prior authorization |
| `approve-plan --job <id> --plan-hash <hash>` | Begin the exact scope-checked plan |
| `collect --job <id>` | Verify final result and save its report/exports |
| `batch --request-file <file>` | Persist entry-to-job mapping; submit/resume, observe and collect; return partial results on bounded exit |

`--source-scope` accepts only `public-web` in v1, without connected private
data sources. Select sources in the UI when supported and verify the selection;
prompt wording alone cannot establish source isolation. If an account enables
unrequested private sources and the UI cannot constrain them, report blocked
source-scope readiness. Unobservable selection also blocks. Record and safely
restore per-conversation persistent toggle side effects after the job; never
change account-wide source settings. A request file has a stable batch id and
entry idempotency keys, provider/question/scope; validate it before input.
Same-provider jobs run serially and each job keeps its own outcome.

Use fixed owner-only platform user application data, independent of checkout and
temporary-directory settings, for the ledger and OS advisory locks. Lock order:
ledger → job → desktop when multiple locks are needed. Hold the desktop lock
for each whole transaction: verify mode/sources and enter/send; verify Copy and
copy/read/clear; or export/validate. Per-job locks/revision checks prevent lost
updates; atomic replacements sync file and parent directory.

Under the ledger lock, request-id/batch-id retries resume existing jobs. Persist
and print ids before first mutation. An uncertain provider job blocks a new
start even from another checkout. Existing nonterminal provider/question/scope
fingerprints resume; completed same-id retries return prior results. A different
id may intentionally repeat a completed question within the authorized budget.
No id overrides uncertainty. OS process exit releases locks; stored PIDs do not
prove ownership. Read-only polling interleaves without changing focus.

### D6. Scoped collection and private artifacts

Default output: `reports/<job-id>/` within owner-only platform user application
data, outside skill discovery and repositories. An explicitly selected project
output root is self-ignoring; preserve the dirty root `.gitignore`. Use restrictive
modes and reject traversal/symlink targets. Save `job.json`, a local
prompt file, `report.md` or `report.txt`, `sources.json`, optional actual native
exports, `manifest.json`, and `index.md` with report-derived text escaped. Number
collection-attempt directories; preserve prior attempts. Removing a skill or
worktree leaves default reports intact. Reports are not skill resources.

Prefer report-specific native Markdown export, then verified report-specific
Copy/extraction. Preserve original bytes and mark plaintext honestly. For Copy,
track pasteboard change count and bind the copied response to the verified job;
do not read or restore unrelated preexisting clipboard contents. Require source
URLs from the copied response or its observed sources panel. Missing citations
is incomplete collection. Require exactly one pasteboard change within a bound
after the verified Copy, and corroborate copied content against the bound report
text/hash. Reject competing writes. Read plain/HTML/rich flavors; obtain URLs
from actual link targets or native source controls, never OCR. Every citation
marker resolves; require source-count parity when fully observable. Clear only
the unchanged copied value, preserving competing writers' contents. Disclose
Copy/paste side effects. All absence checks need a complete-observation flag;
hidden, occluded, scrolled or partial views cannot establish completion. Require
a positive characterized final-result predicate as well.

For downloads, bind the export action to the job,
check stable completed files and file signatures, and never identify a download
merely by the newest filename. Prefer a characterized save panel into the attempt
directory; otherwise compare an in-memory before/after snapshot without logging
unrelated filenames. Check Downloads access in `doctor`; copy verified exports
and preserve originals. Unexpected dialogs block except the characterized save
panel. Hash saved bytes independently, then transition
to `COLLECTED`. Recheck hashes when returning an existing result.

Alternatives: whole-chat selection/account export includes unrelated data;
blind clipboard reads can collect stale data; converting arbitrary text to
Markdown hides formatting/citation loss.

### D7. Keep approval and acceptance distinct

The maintainer approves this reviewed plan before any production skill/script
implementation. Approval covers two public research submissions per provider:
one characterization and one production-runner acceptance. Characterization
prompt: "Using only public official help sources for this provider, explain how
its desktop Research mode is enabled and how reports are collected. Include
this job marker." Acceptance uses the comparative Test Strategy question.
Retain templates and used budget with approval. Proven unsent input can retry
the same job; possibly sent input consumes budget until reconciled. No new job
replaces uncertainty or active research. Extra confirmed submissions beyond the
approved budget need user authorization. Existing authorization covers ordinary
steps within budget; no fresh per-send confirmation. Required native macOS
permission grants remain operator actions. Reuse existing authorization for ordinary
steps; stop only for genuinely missing choices, permissions, or expanded scope.
Do not claim completion if either requested provider lacks a live collection
receipt. Planning, mocks, and blocked-provider outcomes are useful progress,
but do not satisfy the original two-provider objective.

### D8. Bridge contract, provenance and data boundaries

Every mutating bridge call carries a closed action and preconditions: signed
identity/version, PID/start time, window/control, job binding, focus and local
input generation. Creation uses the authorized request/target guards; pre-send
input uses the dedicated draft receipt; later actions require the submitted-job
binding. Recheck and hit-test in-process immediately before the event;
return before/after evidence. Actions are activate/restore-window,
new-conversation, select-research/public-sources, set-composer, send/answer/
begin-research, navigate-bound-job, copy-final and export-final. Unexpected
dialogs block. Never accept arbitrary keys, coordinates, shell or JavaScript
from an agent or retrieved report.

Filter UI text and match markers inside the bridge. Default output is a closed
schema of ids, flags, geometry, hashes and allowlisted controls; raw UI text never
reaches default stdout. `--job-content` returns only the verified job's public
plan/question/report. Persisted troubleshooting capture requires an explicit user
request, is cropped to the job region without sidebar/account chrome, and never
prints to stdout. Whole-desktop capture and account sharing/export are prohibited.

Build the helper in owner-only user cache keyed by source/build hash and verify
ownership/integrity before use. `doctor --self-test` exercises actual helper
label/coordinate/schema/filter and guard logic. All receipts/manifests stamp
`boundary=live|synthetic`, helper source/binary hashes and profile id. Synthetic
mode writes only temporary jobs and cannot satisfy live acceptance. Detect
caller ancestry belonging to the target app and exclude its window, or block
when that exclusion cannot be established. Never quit a caller-hosting app.

Retain sanitized transitions, actual interaction resolver, submission counts,
salted binding/account hashes, app/profile/build identity, file hashes and public
URLs. Full reports stay private local artifacts. The independent duplicate
oracle is exactly one submitted-user-turn marker match, without logging other
titles; otherwise label counts self-attested. Reports are secondary synthesis:
OpenSpec research still fetches cited primary sources through native research MCP.

ADR decision: no separate ADR is proposed. This project schema does not require
an ADR, and all decisions are confined to additive local tooling. Revisit if
the implementation requires global client integration or an external service.

### D9. A browser surface behind the same orchestrator

Added 2026-10-09 at the maintainer's request. Research: `research/README.md`
Q5 to Q9. Reviewed in rv005 to rv012; this text is the fourth version.
Decisions D1 to D8 describe the desktop surface and stay as they are; where
D2 excludes a browser connection, read "on the desktop surface".

A job has a surface, `desktop` or `browser`, chosen by the caller and stored
in the job. The orchestrator, ledger, job states, marker, request and batch
idempotency, source scope, model policy and collection rules are the same
for both. Only the boundary differs: `--surface browser` loads a second
boundary (`scripts/browser_bridge.py`) with the same closed action set
(`doctor`, `inspect`, `create`, `prepare`, `enter`, `send`, `observe`,
`rebind`, `answer`, `begin`, `restore`, `collect`).

- Providers on the browser surface: `claude` (claude.ai), `chatgpt`
  (chatgpt.com), `gemini` (gemini.google.com). Gemini has no desktop surface
  in this change.
- No substitution. A job never changes surface. A blocked desktop job is not
  retried in the browser by the workflow; the caller starts a new request.
- Reservation. A job reserves its provider, on both surfaces, from its first
  input intent until an observation shows positively that the provider
  finished the job (completed, cancelled, failed or out of quota by the
  provider's own message) or that nothing was sent. A local result is not
  such an outcome: a challenge, a signed-out page, an unknown interface, a
  driver failure or a timeout is an observation result. It leaves the
  provider state and the reservation as they were. Both surfaces spend the
  same quota and write to the same conversation history.
- Request identity. A request id stands for one fixed tuple: surface,
  provider, source scope, question hash, kind (research or follow-up) and
  parent job. A retry with the same id and another tuple is rejected before
  any page is opened.
- New and changed commands:

| Command | Behavior |
| --- | --- |
| `--surface desktop\|browser` on `doctor`, `inspect`, `start`, `batch` entries | Selects the boundary; default `desktop` for `claude` and `chatgpt`; `gemini` requires `browser` |
| `login --provider <provider>` | Prepares the dedicated profile and opens it in the system browser without a driver, for the operator to sign in; sends nothing |
| `acknowledge-terms --provider <provider>`, `withdraw-terms --provider <provider>` | Records or removes the operator's acknowledgement (D14) |
| `doctor --local` | Checks the driver, the browser and the profile directory; opens no page and uses no network |
| `start ... --headless` | Runs the browser without a window where the provider profile allows it (D11) |
| `follow-up --job <id> --request-id <id> --prompt-file <file>` | Child job: one chat turn in the parent's conversation (D13) |

Alternatives: a separate skill and store for the browser would duplicate the
ledger and lose the shared reservation; letting an agent drive page tools
directly gives up the closed action set of D8 and puts page text into the
agent's context (D10).

The existing orchestrator ends the reservation of a job that is `BLOCKED`
after a possible send. That is a defect of the approved desktop code under
this rule; it is recorded as its own issue for task group 2.

### D10. Driver: the Playwright library from a lock file, in a script the bridge owns

The maintainer named Playwright MCP. The first two versions of this plan
made its server the driver, with the bridge as its client. The swarm review
read the candidate version's source, and the conductor confirmed four points
in the installed package (run 018):

- The server writes console messages of the page to a log file, and saves
  every download to its output directory, by itself and before a client can
  filter anything.
- It takes configuration from environment variables. One of them names a
  file that the server loads as code.
- Its evaluate tool takes script text and an optional element. It has no
  channel for arguments.
- Its element references stay valid across snapshots while role and name are
  unchanged, so an old reference is not rejected (reviewers' reading of the
  source; not separately confirmed).

Each point breaks a guarantee of this workflow (nothing stored before
binding, a closed interface, a guard with data, a fresh target). They follow
from what the server is for: giving a language model a browser. So the
server is not the driver of jobs.

The driver is a script in the skill, `scripts/browser_driver.mjs`, on
`playwright-core` from a lock file. The Python bridge starts it and speaks a
closed JSON protocol with it over stdio: the action set of D9, nothing else.

Launch contract:

- the absolute path of a Node binary that `doctor --local` verified;
- a fixed environment built by the bridge: a minimal `PATH`, `HOME`, a
  private temporary directory, the locale. No variable of the caller is
  passed on; this excludes Playwright, Node, debug and proxy variables;
- a private working directory; standard error is discarded;
- every option comes in the request. The driver reads no configuration file
  and no environment option;
- a persistent browser context on the provider's profile directory, with the
  installed system browser channel, headed or headless;
- downloads denied, except during the one armed collection step of D12.

Provenance, established by task 5.3 before any use:

- a lock file in the skill with `playwright-core` and its dependency closure
  and the registry's integrity values; installation from it with install
  scripts disabled, into owner-only application data; the bridge refuses a
  tree that does not match;
- no browser is downloaded;
- receipts record the lock-file hash, the hash of the driver script, the
  Node version, and the browser's channel, version and code signature.

What the library provides, from its documentation (run 018) and to be shown
on fixtures in task 5.3: a persistent context with a channel, headed or
headless; removal of default launch switches; denying downloads and waiting
for the download of one action; locators that fail when more than one
element matches; arguments for evaluated functions; input through the
browser's input path after actionability checks.

The browser and the driver are trusted components. The browser keeps the
signed-in session in the dedicated profile, and the driver has full
authority over that browser. The closed protocol limits what the bridge
asks for; it does not limit what the driver could do. That is the reason
for the lock, the launch contract and the review of the driver script.

Playwright MCP's place. It is not used inside jobs and it is never given a
dedicated profile. The other way to meet the maintainer's wording is an
agent that drives Playwright MCP tools directly, guided by a skill. That is
far less work. It cannot give the storage rule, the closed output or the
action guards of this design, and it puts every page the agent sees into the
agent's context. The choice between the two was the first question of task
5.2. The maintainer chose the scripted bridge on 2026-10-10
(`evidence/final/021-browser-decisions/`).

A provider profile is data: origin, the role and name of each control, the
conversation address pattern, the completion predicate, the report
container. Characterization (task 5.10) produces it and review promotes it.
A page that lacks the profile's landmarks is `INTERFACE_UNCHARACTERIZED`.
Web applications have no version to pin, so this check runs on every action.

Other alternatives:

- Playwright CLI and Chrome DevTools MCP: the same class of tool as the MCP
  server, made for an agent at the keyboard.
- Oracle: mature for ChatGPT, including Deep Research and follow-up, MIT. It
  has no Claude web mode and no Gemini interface automation, and it owns its
  session store. Its browser-mode documentation is a reference for D11 and
  D13.
- agent-browser, browser-use, Stagehand, Skyvern: a credential vault or a
  language model in the loop that this workflow does not want.
- Browser tools that an agent client brings itself, such as an extension
  that controls the operator's open browser: they act in the daily profile.
- Anti-detection browsers: excluded (D11).

### D11. Session: a dedicated profile, operator sign-in, headed or headless

- One browser profile directory per provider, in the owner-only application
  data next to the ledger, outside the repository and skill discovery, and
  excluded from backups. Never the operator's daily profile, and never a
  copy of it.
- Sign-in is the operator's action. `login` writes the profile's settings
  before the first start (no sync, no password saving, no session restore),
  opens the system browser on that profile with no driver attached, and
  waits for the window to close.
- Where the session lives. The browser stores the session in the dedicated
  profile, and the driver that controls the browser can reach it (D10).
  This workflow's own components never request, receive, log, export or
  print a credential, a cookie or a token value, never type a credential,
  never save a storage-state export and never copy the profile.
- Key handling. Playwright starts Chrome with `--use-mock-keychain` and
  `--password-store=basic` (confirmed in the candidate source, run 018).
  With them the driven browser could not read cookies that the sign-in
  window protected with the system keychain. The driver removes these two
  switches, so both use the system keychain. Task 5.3 proves this on a
  local fixture profile before any provider is involved.
- Gemini. Its profile holds the session of a Google account, which is more
  than Gemini. The browser surface accepts Gemini only when the operator
  states, in the acknowledgement of D14, that the account is used for this
  purpose only.
- Attaching to a running browser is not used: it would hand the driver
  every signed-in site of the daily profile.
- What the profile holds. A browser profile keeps what a browser keeps:
  cache, history and site data of the pages it loaded, including pages
  loaded before a job was bound. That is the browser's storage, protected
  like the session. The workflow never reads it, and the operator can
  clear or delete it. The storage rule of D13 is about the workflow's own
  files.
- Page ownership. Every transaction starts with a new page that the driver
  opened. Any other page of the context is closed first, and a restored
  session is discarded. Every action goes to that page; there is no tab
  operation in the protocol.
- Readiness is a closed probe on the provider's origin: signed in or not,
  and whether the account is the one recorded. A signed-out page gives
  `AUTH_REQUIRED`.
- Headed is the default, and the driver does not bring the window to the
  front. Whether a headed start and observation leave the operator's
  foreground application in front is not established; task 5.3 checks it
  on a fixture with another application active, including a browser
  restart. If it does not hold, the headed default returns to the
  maintainer.
- `--headless` is accepted only when the provider profile records a
  characterized headless run. That record comes from a step of its own in
  task 5.10: after the headed characterization, one observation of an
  existing job's page without any input. Headless stays off for a
  provider until that step passes. Guards and receipts are the same in
  both modes.
- No evasion. A challenge is recognized positively: a full-page
  interstitial, an unknown dialog or overlay, or a composer that cannot
  take input because another element covers it. Before any input intent it
  gives `BLOCKED` with reason `CHALLENGE`. After an intent it is an
  observation result, and state and reservation stay (D9). The workflow
  does not solve or skip a challenge, does not change the user agent or
  fingerprint, and uses no stealth build. Quota stays `BLOCKED:QUOTA`.
- Locks. The order is ledger, job, profile. The profile lock is taken
  without waiting; a busy profile gives `PROFILE_BUSY`. Nothing waits for a
  profile while it holds the ledger. The desktop lock and the two-second
  input-idle rule do not apply: the driver does not use the operating
  system's input stream and does not activate a window.
- No driver session outlives a command. `wait` is a series of short
  observations at least thirty seconds apart. Gemini's help page says a
  report continues when the chat is left; for the other two this is a
  characterization question.

### D12. Trust and guards for a browser target

A browser job replaces the signed-application evidence of D3 with:

- the exact origin of the top-level page, from a fixed list per provider;
- the driver identity of D10 and the browser's code signature and version;
- the provider profile id, and that the account is unchanged during a job;
- the page that the driver opened itself, in the dedicated profile.

A redirect to another origin, a sign-in page, a profile mismatch or a
challenge blocks. Links inside a report are never opened. Everything read
from a page is untrusted data, and no instruction found in a page is acted
on. Provenance stamps gain `surface` and a third boundary value, `fixture`.

Where the checks run. The page's own script is not trusted with a guard: a
page can replace the functions that an injected script would call. So no
guard logic and no counter runs as page script. The driver reads through
locator queries and activates through the browser's input path. The last
read and the input event are separate steps in the driver; what a page
does in response to the click is seen only by the next observation, which
is why a send is `SUBMISSION_UNCERTAIN` until its receipt (D4).

Preconditions, verified by the driver immediately before the activation of
each action. A failed one blocks; it is not retried.

| Action | Preconditions beyond origin, owned page, landmarks, unchanged account and no challenge |
| --- | --- |
| `enter` | The composer is empty, or holds exactly the owned draft. Entry is one fill operation; the driver sends no Enter and no characterized submit key. Then it waits the settle time that characterization recorded and reads the page again: the value is read back and hashed, no new user turn exists and nothing is generating |
| first `send` | The conversation has no user turn. The composer hash equals the prompt hash |
| follow-up `send` | The parent's marker turn is present, the child's marker is absent, nothing is generating, and the rest as for the first send |
| `begin` | The plan shown has the approved hash. A source that the plan interface adds blocks. This check is not left to the calling agent |
| `answer` | The pending question has the recorded hash. The composer hash equals the answer hash |
| `collect` by download | Downloads are enabled for this one click. Exactly one download from the owned page arrives in the wait; any other new file fails the attempt. The saved name is generated, not the provider's |

A fill cannot be made unable to submit: a page's own input handler can
submit, also with a delay. So the contract is what the driver can see. If a
user turn or a generation appears during entry or the settle time, the
page submitted by itself; the job records the possible send and never
sends again. A provider whose composer can submit on entry without that
showing within the settle time is a no-go for automatic send; fixtures
cover a delayed self-submission.

Every action that submits content or starts research (`send`, `answer`,
`begin`) reads the model and the source evidence again, including memory
and earlier-chat reference (D13), and requires what `prepare` recorded.

Navigation. From the observation until the input event the driver listens
for a navigation of the page and for the target leaving the document. If
either is seen first, the action is cancelled. A navigation that begins
after the input event went out cannot cancel it; the job is then
uncertain, as after any send.

A composer can change the text it is given. The hash is taken over a
normal form that characterization defines per provider; a provider whose
composer does not return the entered text in that form is a no-go.

Controls. A control is accepted only in the top-level document of the
allowed origin, inside an application region of the provider profile, and
only if exactly one element matches; the locator fails otherwise. An
element inside a conversation turn, a report, a canvas or artifact panel,
or any frame is never a target of a mode, model, source or send action.

No element reference outlives an observation. Each action resolves its
target again, and carries the bridge's observation number; the driver
refuses an action whose number is older than the last navigation or
reload of the page.

What remains: the milliseconds between the last read and the input event,
and a page that changes after the click. Operator input in a headed window
is not detected; the window belongs to the workflow, and the read-back of
the composer before send covers the case that matters.

The fixture level of the test strategy has a hostile page for each rule: a
redirect between observation and send; a control with the right role and
name inside answer content, and inside a frame; two matching controls; a
composer that submits on Enter or by itself; a restored second tab; a
challenge drawn as an overlay; a plan replaced after its approval; a second
download; a page that replaces the click function; hostile environment
variables at launch.

### D13. Context: conversation address and follow-up turns

Conversation. After the first Send, the bridge reads the conversation
address that the provider assigns and stores it as the submitted binding
(class 1 of D4), with the marker found in the submitted user turn. Every
later observation and collection opens that address in a new page and
requires exactly one marker match in a user turn. After a browser restart
`rebind` does the same. Another conversation at that address, zero matches
or several matches give UNOBSERVABLE and keep the provider state.
Temporary-chat modes are not used; a job must be reopenable.

Follow-up. `follow-up` creates a child job with its own marker and request
id: one ordinary chat turn in the parent's conversation. It fails closed.
All of these must hold:

- the parent is `COMPLETED`, on the browser surface, with a verified
  address binding, the same profile and the same account;
- the address opens the same conversation, with the parent's marker turn
  present and nothing generating or pending;
- the composer is empty, the parent has no unfinished child, and the
  provider has no other reserved job (D9);
- the private-source rule below holds for the conversation.

Then the usual sequence runs: intent, entry, read-back, one Send, receipt by
the child's marker in a new user turn after the parent's. A retry with the
same request id reconciles and never adds a second turn.

A child job differs from a research job in four stated ways. They are
exceptions to the rules that D4, D6 and the desktop specs give for a
research job, and they hold for child jobs only:

1. It opens no new conversation. Its draft binding is the verified empty
   composer of the parent's bound conversation.
2. Its completion is the characterized end of an ordinary answer that
   follows the child's user turn. The result is an answer, not a research
   report; it is saved and indexed as `answer`.
3. An answer may have no citations. The links it has are still taken from
   link targets.
4. Collection saves the child's answer only: the turn that follows the
   child's marker turn.

The model rule and the source rule apply to a child as to a new job.

Private sources include memory. Account memory, reference to earlier chats,
search over past chats and connected applications are private sources. A
prompt such as "summarize my other conversations" would otherwise collect
them through sanctioned commands. Before entry, for a new job and for a
child, the interface must show them off for this conversation by a
characterized indicator; otherwise the job blocks. The workflow changes no
account-wide setting, so the operator turns such features off in the
account, or the provider stays blocked.

Not in this change. Two things from the first plan are cut and recorded as
follow-up work:

- Passing a collected report to another provider (`--context-job`). A
  report is hostile text for the next model, and the next model's plan is
  read by the calling agent; the reviews found no control for that path
  that does not involve the operator.
- A second research run inside one conversation. Its clarification turns
  need an ownership rule that the address binding does not give.

Printed and stored content. A command prints the closed summary, hashes and
file paths. The driver writes no file except the one armed download. The
bridge keeps two things from an observation: facts about the characterized
controls and, once the marker binding holds, the content of the bound job
(question, plan, answer or report). The rest, including the titles of other
conversations, is dropped before anything is written or returned, and
the workflow stores nothing of a page before the binding holds. What the
browser keeps in its own profile is described in D11. A wider capture for
troubleshooting follows D3 and D8.

What this boundary is. The calling agent runs with the operator's rights,
so the job's private files are not hidden from it. The rule limits what is
written at all and what commands print. Commands print `account_unchanged`
and no account value; the conversation address is not printed.

### D14. Provider terms and the operator's acknowledgement

What the texts say, and what is inferred. Research Q8 has the quotes and the
date. This is a reading, not a legal opinion. The texts are the ones served
to a reader in the United States; a provider can have other terms for other
regions and for business accounts.

- Anthropic. Text: the consumer terms cover "Claude.ai, Claude Pro, and
  other products and services", "along with any associated apps, software,
  and websites". They forbid, "except when you are accessing our Services
  via an Anthropic API Key or where we otherwise explicitly permit it, to
  access the Services through automated or non-human means, whether through
  a bot, script, or otherwise", and "to crawl, scrape, or otherwise harvest
  data or information from our Services other than as permitted". Inference:
  both surfaces of this change are scripted access to those services, and
  collection is within the second clause. No permission is known.
- OpenAI. Text: the terms cover ChatGPT "along with any associated software
  applications and websites". They forbid to "automatically or
  programmatically extract data or Output", and to "circumvent any rate
  limits or restrictions or bypass any protective measures". Inference:
  collecting a report is such extraction on either surface; the no-evasion
  rule of D11 keeps the second clause. A scripted submission is not named.
- Google. Text: the terms forbid "using automated means to access content
  from any of our services in violation of the machine-readable
  instructions on our web pages"; the robots file of the Gemini site
  disallows `/app/` and `/chat/` for every agent. Inference: a scripted
  browser on those paths is within that wording. A robots file is written
  for crawlers, and the text does not say how Google applies it to an
  account holder's own session.

What an acknowledgement is. It records that a person who can unlock this
computer authenticated, on a stated date, for a stated provider, after the
command showed the address of the terms. It stands for the account holder's
acceptance that the use may not comply and that the provider may restrict
or close the account. It is not a permission from the provider, does not
make the use compliant, and does not prove that anyone read the text.

How it is given:

- `acknowledge-terms` prints the provider, the address of its terms and
  what the acknowledgement means, then asks for biometric authentication
  through a small compiled helper of its own: one source file with that
  one call, built and cached like the helper of D2 and independent of the
  desktop bridge. The system's dialog names the provider and the date. There is no password fallback: a password dialog is a window
  that automation could drive. On a computer without biometrics the
  acknowledgement cannot be given this way; what replaces it is a question
  for task 5.2.
- On success the bridge writes a plain receipt: provider, operating-system
  user, date, the terms address, and for Gemini the dedicated-account
  statement of D11. The first gated operation adds the account it finds; a
  different account later needs a new acknowledgement.
- It is valid for thirty days. `withdraw-terms` removes it.
- The receipt is not signed. A key that the system releases only after
  authentication needs entitlements that a locally compiled command-line
  helper cannot carry, according to Apple's developer forum answers (a
  secondary source, not tested here). A signed helper application would be
  its own change.

What the gate does. An agent that uses this workflow's commands cannot
start live browser use for a provider until a person authenticated for that
provider and account within the period. What it does not do: it does not
stop a process with the operator's rights that writes the receipt file or
drives a browser by other means. The approval receipt of task 5.2 in the
change's evidence is the second record.

Gated: every operation that starts the driver against a provider. These are
`doctor` with a page probe, `inspect`, `start`, `status`, `wait`, `rebind`,
`respond`, `approve-plan`, `follow-up`, `collect`, each entry of a `batch`
before that entry's driver starts, and the characterization probes of D15.
Not gated: `login`, `list`, `audit`, `doctor --local`, `acknowledge-terms`
and `withdraw-terms`. A gated operation without a valid acknowledgement
stops with `TERMS_ACKNOWLEDGEMENT_REQUIRED` before the driver starts.

The desktop surface. By the Anthropic and OpenAI texts the same question
exists for the desktop applications. The approval of 2026-10-04 and the
characterization run of task 1.4 came before anyone raised it. This plan
does not change the desktop specs. It asks the maintainer in task 5.2
whether the desktop surface continues as approved, gets the same gate as
its own task, or stops; tasks 4.1 and 4.2 wait for that answer.

Use stays attended and small: one job per provider at a time, the budget
rule of D7, no parallel pages.

Other routes. Where a provider offers a programmatic interface, that is a
supported route under that interface's own terms. OpenAI documents deep
research for its API, and Google offers Gemini Deep Research through the
Interactions API (research Q7). The maintainer's MCP controller lists
research servers for both, with credentials present. No call was made in
this work, so their use is UNVERIFIED here. They use other quota and can use
other models, so they do not replace the subscription products. The skill
names them when a run is refused.

### D15. MCP configuration and characterization

- No stanza is added to `.mcp.json`, `.codex/config.toml` or
  `.grok/config.toml`. The maintainer's MCP controller lists a `playwright`
  server; the workflow does not use it (D10).
- Characterization (task 5.10) gives no agent a provider page and returns
  no outline of one. An outline cannot be redacted before the regions are
  known, and the regions are what characterization is for; the entries of
  a conversation list are controls whose names are titles.
- It works by probes. A reviewed vocabulary file in the skill lists
  candidate controls as role and name pattern: words of the applications'
  own interface, taken from the providers' help pages. For each candidate
  the driver returns the number of matches, whether the match is unique,
  the chain of ancestor roles, and whether the control is enabled. It
  returns no name and no text that it found. The operator reads the first
  result of every provider before an agent does. A new vocabulary entry
  needs review.
- Regions start from seeds that the operator reviews: the composer is the
  form around the one text box that takes the prompt, and menus are
  anchored to it.
- Fixtures. Before characterization the fixture pages are invented: they
  test the bridge's rules (D12) and are labeled `structural`. After it,
  fixture pages are generated from the reviewed profiles and labeled
  `profile`; the second gate runs both.
- ADR decision: unchanged, none proposed. Two reviewers asked for a record
  of the driver's execution boundary and of the reservation rule. D9 and
  D10 hold both; the maintainer can ask for separate records in task 5.2.

## Test Strategy

Invoke the production CLI with temporary local jobs and inject only the desktop
boundary. Assert saved state and artifacts independently; fake boundary tests
are labeled offline. Cover every named scenario in the two delta specs.
Include cross-process/cross-checkout transaction locks, request/batch retry
idempotency, concurrent updates, crash recovery, source-scope refusal,
focus changes, quota, questions/plans, stale clipboard, partial downloads,
citations, path safety, and tampered hashes. Compile and self-test the actual
Swift helper. Provider fixtures come from characterization, never guessed controls.

Live test question for each provider: "Using only public official OpenAI and
Anthropic sources, compare ChatGPT Deep Research and Claude Research: available
desktop interfaces, initiation, source control, and report export. Include
dated citations and this job marker." One acceptance submission per provider
after the separate authorized characterization run.
Verify the mode and receipt, deliberately expire an observation wait, resume
the same conversation, close/reopen only the dedicated window and rebind without
resubmission. Never quit a caller-hosting app. Collect and independently inspect
the report/sources. Run both acceptance jobs via a persisted batch; force zero
receipt-observation budget after Send to exercise uncertainty/reconciliation.
Record non-activating wait counts, source evidence and submitted-user-turn
duplicate oracle. Do not use private connectors or upload repository files.

Skill evaluations cover: (1) both-provider cited report collection; (2) resume
an existing job after timeout without another submit; (3) honest refusal when
research mode is absent. Run with-skill and baseline lanes and store their
outputs. Force synthetic boundaries in all six lanes (three with-skill/baseline
pairs, one trial each) and deny real desktop access. Assertions check both cited
reports, zero resume submissions, and an honest blocked outcome respectively.
These evaluations supplement, not replace, live desktop acceptance.

Browser surface, three levels, each stamped in its receipts:

1. `synthetic`: every scenario of `browser-research-session` through the
   production command entrypoints with an injected browser boundary and an
   injected authentication boundary, in a temporary store. This covers the
   decisions of the orchestrator: surface selection, reservation, request
   identity, address binding, follow-up preconditions and retry, the terms
   gate, the headless rule, the profile lock and closed output.
2. `fixture`: the real bridge and the real driver script on the locked
   library, headless, against pages served on the loopback interface. This
   covers the launch contract, page ownership, the preconditions of each
   action, the region rule, entry without submission, download binding,
   link targets, and that the driver writes no file of its own. The hostile
   pages of D12 are part of it. One check runs headed, with another
   application in front (D11). Before characterization the pages are
   `structural`; afterwards `profile` pages are added (D15). It proves the
   bridge and the driver, not a provider.
3. `live`: after task 5.2 and the gate of task 5.9, per provider one
   attended characterization by probes and one acceptance run of the
   comparative question above, with the provider list in the question
   extended to the three providers. The acceptance run expires a wait,
   restarts the browser, rebinds by address, collects, and sends one
   follow-up turn. A second observation runs headless if the profile allows
   it; no second submission.

Neither `synthetic` nor `fixture` receipts pass the live audit. A synthetic
test shows a decision of the orchestrator; it does not show that the driver
or a provider behaves that way.

## Benchmark Plan

JMH: NOT APPLICABLE; no Java runtime path changes. Record bounded polling and
desktop-step timings in live receipts for diagnosis, not as a throughput claim.

## Code Intelligence Plan

No Java symbols or API renames. Focused `rtk rg` checks references and discovery
links. Python tests and Swift compilation verify the script interfaces. A
Java compile or incomplete JDTLS inventory adds no proof of desktop behavior.

## Execution Plan

One conductor owns this change. A single writer owns the canonical skill,
scripts, tests, discovery link, and `docs/ai/DESKTOP_RESEARCH.md`; no concurrent
desktop writers. One task group at a time, characterization/failing test first.
Independent plan review uses a project architecture-review profile from another
model family. Code gates use independent reviewers per `docs/ai/SWARMS.md`;
security review covers the skill, captured data, prompt submission, and exports.
All reviewer reports, including failures, go into this change's evidence.
Reviewer lanes never control desktop apps or mutate source. Close sessions when
their terminal reports have been retained.

Browser surface (task group 5): the same single-writer rule. Order: plan and
reviews (5.1); maintainer approval (5.2); driver provenance and launch
contract (5.3); failing tests (5.4); orchestrator changes (5.5); driver
script, bridge and structural fixtures (5.6); acknowledgement and sign-in
preparation (5.7); characterization probes (5.8); first gate (5.9);
attended characterization (5.10); provider profiles and browser collection
(5.11); documentation (5.12); second gate (5.13); live acceptance (5.14).
No provider page is opened before the first gate has passed and the
operator has given the acknowledgement. The files are
`scripts/desktop_research.py`, new `scripts/browser_bridge.py` and
`scripts/browser_driver.mjs`, `browser/` (lock file, vocabulary),
`references/browser-profiles/`, `tests/` and `tests/fixtures/`. Task 5.11
waits for the collection validator of task 3.2 and task 5.12 for the skill
and documents of task 3.3. Nothing in group 5 waits for the desktop bridge
of task 2.3. Tasks 5.5 and 2.3 both edit the orchestrator, so they are not
worked at the same time. No lane signs in,
handles a credential, or opens a provider page without the operator present.

## Risks / Trade-offs

- UI/version drift → fresh target evidence and provider/version tests.
- Incomplete Accessibility → app-window capture; blocked if no reliable target.
- Different helper permissions → preflight the exact compiled binary.
- Provider ambiguity after a send → reconcile; never automatic resubmission.
- Account/source restrictions → explicit per-provider blocked status.
- Citation/format loss → preserve actual format; fail incomplete collection.
- User intervention while automation runs → recheck focus and stop safely.
- Native capture initialization → initialize frameworks on the main actor;
  retain and diagnose failed probes rather than treating permissions as success.
- Provider terms (both surfaces) → the account can be restricted or closed;
  an acknowledgement after biometric authentication, attended small use,
  other routes named (D14). The risk is accepted, not removed.
- The acknowledgement is a record, not a barrier against a process with the
  operator's rights → stated in D14; a signed helper would be its own change.
- The last read and the input event are separate steps → preconditions per
  action in the driver, strict locators, no page-script guard, hostile
  fixtures; the remaining gap is stated in D12.
- A provider reads account memory or earlier chats into an answer → these
  count as private sources and must be shown off, or the job blocks (D13).
- Web interface drift without a version → landmark check on every action;
  an unknown page blocks instead of guessing (D10).
- Headless is refused or challenged by a provider → headed is the default;
  a challenge blocks and is never bypassed (D11).
- A signed-in profile on disk is a session credential → owner-only
  directory excluded from backups, one profile per provider, no copies, no
  storage-state files, system keychain; a Google profile only for a
  dedicated account; the operator can sign out or delete it at any time.
- The driver has full authority over the browser → a locked library, a
  reviewed driver script, a fixed launch environment, a closed protocol
  (D10).
- Page text reaches an agent or the disk → closed output, nothing stored
  before binding, no file written by the driver, characterization by
  probes that return counts (D13, D15). The private files themselves are
  readable by the calling agent; that is stated in D13.
- A follow-up lands in the wrong conversation → fail-closed preconditions
  and a marker receipt after the parent's turn (D13).
- A local failure after a possible send frees the provider too early → the
  reservation rule of D9.
- A library update changes behavior → lock file and the fixture level of
  the test strategy.
- The plan is larger than an agent that drives browser tools directly →
  the trade is stated in D10 and is the maintainer's first decision.

## Migration and Rollback

Additive files only. Removing the discovery link disables the skill. Local
reports remain user-owned; rollback never deletes them. Do not change global
settings, installed app bundles, Java files, or other OpenSpec changes.
Helper-cache removal is optional operator cleanup after verifying no runner
uses it. Advisory lock files may remain; never delete a lock path while jobs
or runners are active. Ledger and report deletion require separate authority.
Document any helper-specific macOS grants and their manual revocation in
System Settings; rollback does not automate permission-database changes.

Browser surface: new files, and two changes to the shared orchestrator
that also reach desktop jobs. The reservation rule of D9 keeps a provider
reserved for a desktop job that may have been sent and then failed
locally (issue `policy4j-971.2.5`), and a request id is tied to the tuple
of D9. The existing execution tests are the regression check, and task
5.5 adds reservation tests for both surfaces. Tasks 4.1 and 4.2 wait for
the maintainer's answer on the terms question (D14).
Rollback leaves the profile directories and the acknowledgement receipts in
place; removing a profile directory signs that profile out and is the
operator's action, after no job of that provider is pending. Conversations
created by jobs stay in the provider account; the workflow archives and
deletes nothing there.

## Open Questions

Maintainer approval is required before implementation. The user may name a
specific plugin instead of the built-in modes; otherwise the built-in modes
remain the stated assumption. Account entitlement, reliable controls, and
successful collection are live verification obligations, not settled facts.

Browser surface, decided by the maintainer on 2026-10-10
(`evidence/final/021-browser-decisions/`). The driver and budget answers
hold for the case that the surface is built:

- Driver: the scripted bridge on the Playwright library (D10).
- Scope: both cuts of D13 are accepted.
- Gemini: through its API only. Gemini leaves the browser surface, so no
  Google session is put in a driven browser. This overrides the Gemini text
  of D9, D11 and D14 and of the spec, which is revised with the next plan
  version.
- Budget: per provider one characterization run, one acceptance run and one
  follow-up turn, for Claude and ChatGPT.
- On a computer without biometric authentication the browser surface is
  unavailable. Nothing weaker replaces the biometric step of D14.

Browser surface, still open for the approval of task 5.2:

- Terms: does the maintainer accept the risk of D14 for a provider? On
  2026-10-10 the maintainer first chose to try the programmatic routes of
  D14 and then took that trial out of this change
  (`evidence/final/028-api-trial-withdrawn/`). The question is open.
- Desktop surface: continue as approved, add the same gate as its own
  task, or stop (D14)? It waits with the terms question.
- Does a browser acceptance run count toward the original two-provider
  desktop objective, in particular for ChatGPT, whose desktop adapter is
  blocked by the protected draft? Open.
- Are separate decision records wanted for the driver boundary and the
  reservation rule (D15)?

Open until characterization: the conversation address pattern of each
provider, whether a run continues after the page closes, which providers
serve the normal interface headless, each provider's export controls,
whether each interface shows memory and earlier-chat reference as off for
a conversation, and the normal form of each composer's text.
