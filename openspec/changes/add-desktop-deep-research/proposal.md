## Why

Running research in the local ChatGPT and Claude desktop apps currently has no
project workflow for submitting a bounded question, following the same job,
and collecting the final report with its citations. A project skill and local
scripts should make that workflow repeatable while preserving existing chats
and accurately reporting missing controls, interruptions, and incomplete exports.

## What Changes

- Add the `desktop-deep-research` agent skill, shared by the project's Codex
  and Claude skill directories, with scripts and provider-specific guidance.
- Drive ChatGPT's built-in Deep Research and Claude's built-in Research mode
  through their local desktop interfaces. This is the planning assumption;
  a named third-party plugin requires its own observed adapter and verification.
- Provide `doctor`, `inspect`, `start`, `list`, `status`, `wait`, `rebind`,
  `respond`, `approve-plan`, `collect`, and `batch`
  commands. Save resumable job receipts and collect Markdown reports, source
  links, and supported native exports into local run directories.
- Detect the installed application identity and version, verify research-mode
  selection before submitting, preserve drafts, and handle clarifying questions,
  research plans, quota limits, and uncertain submission outcomes explicitly.
- Serialize desktop actions, verify focus before input, and capture only the
  selected research job. Keep local reports and private UI data out of Git.

Added on 2026-10-09 at the maintainer's request (task group 5):

- Add a browser surface next to the desktop surface. The caller names it
  (`--surface browser`); the workflow never falls back to it by itself.
  It drives the web applications of Claude, ChatGPT and Gemini with the
  Playwright library from a lock file, in a script that the bridge owns,
  headed by default and headless where a provider's interface has been
  characterized that way. The maintainer named Playwright MCP; review
  found that its server stores page output by itself and takes code from
  its environment, so it is not the driver (design D10). The choice
  returns to the maintainer in task 5.2.
- Add Gemini Deep Research as a third provider, on the browser surface only.
- Keep the session: a dedicated browser profile per provider in which the
  operator signs in once. The workflow handles no credential, cookie or token.
- Keep the conversation: bind a job to its conversation address, reopen it
  after a browser restart, and add `follow-up` chat turns as child jobs in
  the same conversation. Passing a report to another provider and a second
  research run in one conversation were planned first and are cut after
  review (design D13); they are follow-up work.
- Keep the caller's context small: commands return a closed summary and file
  paths. Only the content of the bound job is stored; other page text is
  not written anywhere.
- Require the operator's acknowledgement of each provider's terms, given
  after biometric authentication, before any live browser operation, and
  name the providers' programmatic interfaces as the other route.

## Capabilities

### New Capabilities

- `desktop-research-execution`: submit and follow research jobs in both local
  desktop apps, with bounded waits, restart recovery, and truthful status.
- `desktop-research-collection`: collect completed job reports with citations,
  integrity metadata, and an index without accepting unrelated clipboard data.
- `browser-research-session`: run the same jobs through a signed-in browser
  for three providers, headed or headless, with a verified target and a
  conversation that can be reopened and continued.

### Modified Capabilities

None. Existing XACML capabilities and the Java build are unaffected.

## Research Questions

1. Which research modes and export formats are documented for each product?
2. Which installed app identities, permissions, and UI controls are reachable
   from the current local execution host?
3. Can a dependency-free macOS bridge support both Accessibility and visual
   inspection without reading unrelated chats or changing client configuration?
4. What proves submission, completion, and collection after an interruption?
5. Which open-source drivers run a signed-in browser headed or headless, and
   which projects already drive these three research products?
6. Which official programmatic routes exist, and what do the providers'
   consumer terms say about automated access?
7. What identifies a conversation in each web application, and what must hold
   before a follow-up turn may be sent into it?

## Verification

- Standard-library Python tests exercise job persistence, atomic transitions,
  serialization, ambiguity after submission, timeout recovery, and collection
  identity/integrity through the production command entrypoints.
- Compile and test the Swift bridge against the installed macOS frameworks;
  use no new Python, Node, MCP, or GUI-automation dependency.
- After approval, run at most one public characterization question and one
  production acceptance question per desktop app, using the design's templates.
  Verify research mode, observed submission, the same job's completion, and a
  nonempty collected report with source URLs. Retain provider-specific receipts.
- Exercise resume after an observation timeout without a second submission.
- Evaluate the skill on three workflow prompts and baseline counterparts;
  all six evaluations use synthetic boundaries with real GUI access denied.
  Offline fixtures do not count as desktop acceptance.
- Browser surface: the named tests of `browser-research-session` run through
  the production command entrypoints with a synthetic browser boundary. The
  browser bridge is then run with the real driver script on the locked
  library, headless, against fixture pages served on the loopback
  interface, including hostile pages; these receipts are stamped `fixture`
  and are not live proof. Live proof per provider is one attended
  characterization and one acceptance run that includes a restart rebind and
  one follow-up turn, after the approval of task 5.2 and the gate of 5.9.
- Run `openspec validate add-desktop-deep-research --strict` and audit every
  scenario against indexed evidence before claiming completion.
- Maven, XACML conformance, JDK changes, and JMH are NOT APPLICABLE because
  this change touches agent tooling only.

## Impact

Intended project files are limited to the new skill, its project-local discovery
links, `docs/ai/DESKTOP_RESEARCH.md`, and this change's artifacts. Generated jobs,
the shared ledger, and the helper cache live in owner-only application data
outside skill discovery and the repository; preserve the dirty root `.gitignore`.
No global MCP/client settings, plugins, packages, Java source,
vendored standards, or generated CAO/NTM profiles change. No Git history action
is requested. Implementation requires maintainer approval under `AGENTS.md`
and `docs/ai/OPENSPEC.md`.

Browser surface: it adds one runtime dependency, Node with the
`playwright-core` library from a lock file, and a browser profile directory
per provider in owner-only application data. The Verification section says
that no new Python, Node, MCP or GUI-automation dependency is used; that holds
for the desktop surface only. No project or global MCP client file changes
(design D15). The browser surface is automated access to consumer services,
which the providers' published terms restrict (research Q8, design D14); by
the same texts the question exists for the desktop surface, and tasks 4.1
and 4.2 now wait for the maintainer's answer on it. None of the browser
surface is implemented before the approval of task 5.2.

## Tracking

Beads epic: `policy4j-971`. Planning task: `policy4j-971.1.1` (claimed by
`codex-desktop-research`). Task `X.Y` maps to `policy4j-971.X.Y`.
The maintainer gave the go-ahead to try sample reports on 2026-10-04;
`evidence/final/006-maintainer-approval/` retains the receipt and scoped budget.
No implementation task is complete yet.

Browser surface: task group 5, issues `policy4j-971.5.1` to
`policy4j-971.5.14`. The maintainer asked for it on 2026-10-09. That request
covers research and the plan (task 5.1). The approval of 2026-10-04 does not
cover browser submissions; task 5.2 records a new one.
On 2026-10-10 the maintainer decided the driver, the two cuts, Gemini
through its API only, and the budget
(`evidence/final/021-browser-decisions/`). A trial of the programmatic
routes, started the same day, was taken out by the maintainer and is not
part of this change (`evidence/final/028-api-trial-withdrawn/`). The terms
question is open. No approval to build the browser surface exists.
