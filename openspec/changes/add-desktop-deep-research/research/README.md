# Research

Status: REQUIRED

Retrieved 2026-10-04. The initial research below covers pre-approval product
guidance and local feasibility. Later live observations are in
`evidence/builds/007-live-characterization/` and
`evidence/builds/008-characterization-disposition/`: one Claude Research report
is collected; ChatGPT remains unsent behind its protected-draft guard.
Both-provider production acceptance is still unverified.

## Questions and answers

### Q1. Which research modes and export formats are documented?

- ChatGPT: Deep Research accepts a question, can ask clarifying questions,
  presents a proposed plan, and produces a cited report. Completed reports
  support Markdown, Word, and PDF downloads. Source: [OpenAI Deep Research
  help](https://help.openai.com/en/articles/10500283-deep-research-in-chatgpt).
- The current unified desktop app contains Chat, Work, and Codex. Source:
  [OpenAI release notes](https://help.openai.com/en/articles/6825453-chatgpt-release-notes).
  The installed bundle identity must therefore be discovered and verified.
- Claude: Research is documented for paid plans on the web, desktop, and
  mobile; web search must be enabled. The composer plus menu enables
  Research. Source: [Claude Research help](https://support.claude.com/en/articles/11088861-use-research-on-claude).
- A per-report Claude download format was not established. Collection must
  use an observed report-specific download, Copy action, or isolated final
  response extraction and report the actual format. Account-wide data export
  is unsuitable because it includes unrelated conversations: [Claude export
  help](https://support.claude.com/en/articles/9450526-export-your-claude-data).
- Confidence: high for fetched product guidance; PARTIAL for local availability.
- Drift: account entitlement, workspace policy, app version, and UI differ.

### Q2. What is reachable locally?

- Both app bundles are installed. Observed versions: ChatGPT `26.928.31416`;
  Claude `2.19675.0`.
- The bundle named ChatGPT declares `com.openai.codex`; Claude declares
  `com.anthropic.claudefordesktop`. These are observations, not a hard-coded
  universal map.
- Signature metadata reported TeamIdentifier `2DC432GLL2` for ChatGPT and
  `Q6L2SF6YDW` for Claude. This probe read signing metadata; it did not perform
  full signature validation. Runtime `doctor` must validate before trusting it.
- ChatGPT declares `scripting.sdef`. The installed dictionary exposes generic
  Chromium-style window/tab ids, tab URLs and JavaScript execution, with no
  Research-specific command found. Declared capability is not a tested runtime
  research locator. Claude declares no scripting dictionary in its plist.
- Swift preflight returned `accessibility_trusted=true` and
  `screen_recording_granted=true` for this execution context. Future compiled
  helpers can have a different permission identity and must recheck.
- Claude was running. ChatGPT was launched successfully and was then observed
  under the identity read from its plist. Window queries returned success.
- Accessibility inspection was PARTIAL: ChatGPT exposed two windows and 18
  nodes; Claude exposed one window and 11 nodes after activation, including
  an `AXWebArea`. No allowlisted research controls were found in these trees.
- A capture limited to a ChatGPT window, processed in memory by Vision OCR,
  found an allowlisted `New chat` label. Claude had no qualifying visible
  window in that capture. This proves limited visual inspection, not research
  mode availability, account sign-in, text entry, or collection.
- A later Claude window query showed `minimized=true`. Native activation,
  raising, and unminimizing succeeded. Once restored, its Accessibility tree
  exposed 148 nodes, including a text area and 18 buttons; exact allowlisted
  controls included `Claude`, `Code`, and `Send`. The original minimized state
  was then restored successfully. This verifies limited window control and
  richer inspection, while Research selection and actual send remain UNVERIFIED.
- Evidence: `evidence/builds/001-local-capabilities/`.
- Confidence: high for the recorded observations; PARTIAL for full control.

### Q3. Is a dependency-free local bridge viable?

- Installed Swift can invoke AppKit, Accessibility, ScreenCaptureKit, and
  Vision; the corrected app-window OCR probe succeeded. The first capture
  attempt aborted with `CGS_REQUIRE_INIT`; initialization of AppKit/CoreGraphics
  and main-actor capture corrected that particular probe.
- Design inference: a Swift helper plus Python standard-library orchestration
  is a viable candidate. Reliable input and research-specific collection remain
  UNVERIFIED and require live acceptance in both apps.
- Native `computer-use` and browser-control MCP tools were UNAVAILABLE in this
  session. Local macOS automation is a separate execution path, not a claim
  that those tools are connected. No MCP or client setting was changed.

### Q4. What proves submission, completion, and recovery?

- A launch, successful key press, ordinary assistant reply, copied text, or
  observation timeout cannot prove a submitted/completed research job.
- Design inference: bind each job to a unique prompt marker and an observed
  conversation identity; persist submission intent before input. Reconcile
  uncertain submission against that same job before allowing another submit.
- Completion requires provider-specific final-result evidence and the absence
  of pending plan/question/generation controls for that identified job.
- Collection requires nonempty final content, source URLs, matching job identity,
  file hashes, and independently checked saved artifacts. An export failure
  preserves provider completion and leaves collection incomplete.

## Browser surface (added 2026-10-09)

The maintainer asked for a browser lane: research started in the web
applications of Claude, ChatGPT and Gemini, headed or headless, with Playwright
MCP, and with the conversation context kept. Retrieved 2026-10-09. Run:
`evidence/builds/014-browser-surface-research/` (sources, revisions, quotes).
Star counts are those shown on the retrieval date. No web interface was
opened; every statement about a provider page is documentation or a
third-party claim until task 5.10 observes it.

### Q5. Which open-source drivers run a signed-in browser, headed or headless?

- Playwright MCP (`microsoft/playwright-mcp`, revision `b8b4183e`). SUPPORTED
  from its README:
  - "`--headless` run browser in headless mode, headed by default".
  - Three profile modes: "persistent profile like a regular browser
    (default), in isolated contexts for testing sessions, or connect to your
    existing browser using the browser extension." The persistent profile keeps
    "all the logged in information"; `--user-data-dir` sets its location.
  - "A persistent profile can only be used by one browser instance at a time".
  - `--isolated` with `--storage-state`, `--cdp-endpoint`, and `--extension`
    ("Connect to a running browser instance (Edge/Chrome only)").
  - "Playwright MCP is **not** a security boundary." `--allowed-origins` "*does
    not* serve as a security boundary and *does not* affect redirects."
  - Tools include `browser_navigate`, `browser_snapshot`, `browser_click`,
    `browser_type`, `browser_wait_for`, `browser_tabs`, and also
    `browser_evaluate` and `browser_run_code_unsafe`. Cookie and storage tools
    are opt-in (`--caps=storage`).
  - The README itself says that for coding agents the CLI form is "more
    token-efficient: they avoid loading large tool schemas and verbose
    accessibility trees into the model context".
- Playwright CLI (`microsoft/playwright-cli`, revision `b85c7a73`, 13.9k
  stars). SUPPORTED: headless by default, `--headed`; named sessions (`-s=`);
  `--persistent` and `--profile=<path>`; `attach --cdp=<url>` and
  `attach --extension`; `state-save` / `state-load`.
- Playwright extension (`microsoft/playwright`, `packages/extension`, revision
  `d9f2fd3e`). SUPPORTED: it connects to tabs of the browser that is already
  running, "using your existing cookies, sessions, and browser state". The
  operator picks the tab; each client sees only its own tab group.
- Chrome DevTools MCP (`ChromeDevTools/chrome-devtools-mcp`, revision
  `f08dbe15`, 53k stars). SUPPORTED: Puppeteer based; own profile directory,
  `--isolated`, `--headless`; `--autoConnect` attaches to a running Chrome
  (144 or newer) after a permission dialog. Two statements matter here:
  "Some accounts may prevent sign-in when the browser is controlled via
  WebDriver", and "Chrome requires you to use a non-default user data
  directory when enabling the remote debugging port". Usage statistics are on
  by default (`--no-usage-statistics`).
- agent-browser (`vercel-labs/agent-browser`, 43.7k stars, Apache-2.0).
  SUPPORTED: CLI with `--headed`, `--profile <name|path>`, `--session` with
  `--restore`, `--state`, `--cdp`, `--auto-connect`, `--allowed-domains`. It
  also has an "auth vault" that stores credentials.
- Agent frameworks with a language model in the loop: browser-use (117k
  stars), Stagehand (25.6k), Skyvern (23k). PARTIAL: read from repository
  descriptions only. They need their own model key and decide actions at run
  time.
- Several popular repositories are browsers built to defeat bot detection.
  They are out of scope: this change does not evade a provider's controls.
- These statements describe the main branch of each project. For the
  candidate driver version `0.0.73`, run 016 compared them with the
  package's own help output: `--headless`, `--user-data-dir`, `--isolated`,
  `--storage-state`, `--cdp-endpoint`, `--extension`, `--allowed-origins`,
  `--caps`, `--output-dir`, `--browser`, `--snapshot-mode`, `--secrets` and
  `--init-script` are there; `--idle-timeout` and `--profile-dir-name` are
  not. Its tool list was not read (PARTIAL).
- Cross-check through Context7 (run 016): the library entry for
  `microsoft/playwright-mcp` lists version `v0.0.80`. It also lists
  `mediar-ai/playwright-mcp-orchestrator`, described as letting several
  agents share one Chrome with tab isolation while keeping cookies and
  sign-ins (description only).

### Q6. Which open-source projects already drive these research products?

- Oracle (`steipete/oracle`, revision `35d8022f`, 4,047 stars, MIT). SUPPORTED
  from `docs/browser-mode.md` and `docs/followup.md`:
  - It "launches Chrome itself and drives the ChatGPT web UI over CDP", or
    attaches to a running Chrome. Manual-login mode uses "a persistent
    automation profile"; the operator signs in once in that window.
  - `--browser-research deep` activates ChatGPT Deep Research and "captures
    the final report from the Deep Research report surface instead of trusting
    the assistant tool-call wrapper."
  - Follow-up "reopens the parent's exact ChatGPT conversation, and submits the
    new prompt there." It is "fail-closed: Oracle refuses to submit if the
    saved URL is not a recoverable HTTPS ChatGPT conversation, the page has no
    stable prior turns, or the browser lands on a different conversation." It
    "disables Deep Research for the resumed turn".
  - Copying cookies from the daily profile is off by default because of "the
    token-rotation risk".
  - It has no Claude web mode. Its Gemini mode is "a cookie-based Gemini
    client", not interface automation, and has no follow-up.
- Single-provider scripts with zero or one star, read as technique notes
  only (PARTIAL: README claims, not reviewed code):
  - `zamabama/claude-code-deep-research` (revision `4a22da13`): Python and
    Playwright on claude.ai; a profile directory per account; handles
    clarifying questions; says it uses the artifact's "Download as Markdown"
    link.
  - `halanhuang2025-lgtm/chatgpt-deep-research`: says the ChatGPT report sits
    in a cross-origin frame and reads it through the accessibility snapshot.
  - `luxiaolei/browser-research-skill`: agent-browser scripts for Gemini,
    ChatGPT and Grok.
  - `lesterppo/hermes-gem-pw`, `michaeljboscia/claude-deep-research`,
    `andylizf/deep-research-skill`: description only.
- Found by the Tavily cross-check (run 016), descriptions and README text
  only:
  - `cbusillo/chatgpt-automation-mcp` (archived): an MCP server that drives
    the ChatGPT web interface with Playwright. Its guidance file says the
    "ChatGPT UI changes frequently without notice" and that a Deep Research
    run "can take 2-6 hours".
  - `hustlerv369/banana`: drives gemini.google.com with Playwright and a
    persistent profile. Its README says "Google keeps you logged in reliably
    in real Chrome; bundled Chromium gets served the logged-out page", and
    that "automating web apps may conflict with their Terms of Service".
  - A published skill, "Gemini Deep Research Automator", described as using
    Playwright and CDP with a preflight, plan confirmation and extraction.
- No project was found that covers all three providers with durable job
  identity, duplicate-submission control and citation checks. Absence is
  PARTIAL: GitHub repository search, one DuckDuckGo search and one Tavily
  search.

### Q7. Which official programmatic routes exist?

- OpenAI documents a deep research guide for its API, with background mode
  for long runs: `https://developers.openai.com/api/docs/guides/deep-research`
  (SUPPORTED: provider documentation search).
- Google: "Gemini Deep Research agent, available via the Interactions API"
  (Google blog, 2025-12-11:
  `https://blog.google/innovation-and-ai/technology/developers-tools/deep-research-agent-gemini-api/`).
- `pminervini/deep-research-mcp` (114 stars) wraps "OpenAI Deep Research,
  Gemini Deep Research, Tavily Research, DR-Tulu, and Open Deep Research".
  The maintainer's MCP controller lists a server of that name as running,
  and research servers for OpenAI and Gemini models with credentials
  present. No call was made in this work: that these routes work here is
  UNVERIFIED.
- An Anthropic API product named Research was not established by this run
  (UNVERIFIED either way).
- Open-source research engines that run on a model key, for comparison:
  `bytedance/deer-flow` (83.6k stars), `stanford-oval/storm` (31.6k),
  `Alibaba-NLP/DeepResearch` (20k), `LearningCircuit/local-deep-research`
  (9.2k), `zilliztech/deep-searcher` (8.3k). PARTIAL: descriptions only.
- These routes use other quotas and prices than a consumer subscription and
  can use other models. They do not exercise the web products. Each has its
  own terms, which this run did not read.

### Q8. What do the consumer terms say about automated access?

Fetched 2026-10-09 from the providers' own pages. This is a reading of
published text, not legal advice.

- Anthropic, Consumer Terms of Service, section 3. You may not: "Except when
  you are accessing our Services via an Anthropic API Key or where we
  otherwise explicitly permit it, to access the Services through automated or
  non-human means, whether through a bot, script, or otherwise." Also: "To
  crawl, scrape, or otherwise harvest data or information from our Services
  other than as permitted under these Terms."
  (`https://www.anthropic.com/legal/consumer-terms`)
- OpenAI, Terms of Use, effective 2026-01-01. You may not: "Automatically or
  programmatically extract data or Output", nor "circumvent any rate limits
  or restrictions or bypass any protective measures or safety mitigations".
  (`https://openai.com/policies/row-terms-of-use/`)
- Google, Terms of Service. You must not abuse the services by "using
  automated means to access content from any of our services in violation of
  the machine-readable instructions on our web pages (for example, robots.txt
  files that disallow crawling, training, or other activities)".
  `https://gemini.google.com/robots.txt` reads `Disallow: /app/` and
  `Disallow: /chat/` for every user agent.
  (`https://policies.google.com/terms`)
- The three texts restrict different things. What follows is inference
  from the quotes, per provider:
  - Anthropic: the text names access by automated means, and the terms cover
    "any associated apps, software, and websites". Scripted use of the web
    application and of the desktop application are both within that wording.
  - OpenAI: the text names programmatic extraction of Output, and the terms
    cover "any associated software applications and websites". Collecting a
    report is within that wording on both surfaces. A scripted submission is
    not named.
  - Google: the text ties the restriction to the site's robots file, which
    disallows the Gemini application paths. A scripted browser there is
    within the wording. The text does not say how Google applies a robots
    file, which is written for crawlers, to an account holder's own session.
    Nothing follows for a desktop application.
- The account holder carries the risk (restriction or closure of the
  account). Whether a given plan carries a permission is for the maintainer
  to check. An acknowledgement in this workflow accepts possible
  non-compliance; it is not a permission. Design D14.

### Q9. What is on this machine?

- Node `v26.11.1`, npx `11.6.4`; Google Chrome `155.0.8059.40`; Microsoft
  Edge is installed.
- `@playwright/mcp@0.0.73` and `chrome-devtools-mcp@0.25.0` are installed
  globally; Playwright MCP profile directories exist in the user cache, so it
  has run here before.
- No browser tool is in the project MCP file. The session that did this
  research had a Chrome-extension tool set from the client; it was not used.
- The maintainer's MCP controller lists a `playwright` server, on demand and
  stopped at the time of the probe (run 016).
- The npm registry lists `@playwright/mcp` `0.0.73` with an integrity value
  and `0.0.83` as the newest version (run 016).
- Evidence: `evidence/builds/014-browser-surface-research/local-probe.txt`,
  `evidence/builds/016-browser-research-crosscheck/`.

## Tool lanes

| Lane | Profile / model | Report | Status |
| --- | --- | --- | --- |
| Browser surface: GitHub MCP (repository search, file fetch) | Conductor / Claude Opus 5.5 | Q5, Q6, Q7 | SUPPORTED: primary files at recorded revisions |
| Browser surface: DuckDuckGo fetch | Conductor / Claude Opus 5.5 | Q7, Q8, Gemini help page | SUPPORTED: provider pages fetched |
| Browser surface: provider documentation MCP (OpenAI, Claude Code) | Conductor / Claude Opus 5.5 | Q7, D15 | SUPPORTED for the two lookups made |
| Browser surface: Tavily, Context7 through the local gateway | Conductor / Claude Opus 5.5 | Q5, Q6 cross-check (run 016) | UNAVAILABLE in run 014: gateway error, "backends required DNS resolution which failed". Cause: the local gateway process, not the services; the hosts resolved from the shell. SUPPORTED in run 016 after the gateway was restarted through its controller |
| Browser surface: Context7 through the client connector | Conductor / Claude Opus 5.5 | Q5 cross-check (run 016) | SUPPORTED: it does not use the gateway; not tried in run 014 |
| Browser surface: independent deep-research synthesis | Not run | None | NOT RUN: available (the controller reports its route as passing); an independent synthesis is still missing |
| Local and primary-source research | Conductor / GPT-6 | This file | PARTIAL: probes and docs, no live research run |
| OpenAI documentation MCP | Provider-native documentation search/fetch | This file | SUPPORTED for skill format; research queries returned unrelated changelog hits |
| Claude Code documentation MCP | Provider-native documentation search | This file | PARTIAL: desktop Research was outside useful coverage |
| Context7 | Official Claude platform library | This file | PARTIAL: returned API MCP guidance, not consumer Research control |
| Tavily | Primary-source search/extraction | This file | SUPPORTED: product help pages retrieved |
| Independent Gemini search | Not exposed | This file | UNAVAILABLE |

The provider-native docs lanes were tried first. Tavily retrieved the official
consumer help pages after those lanes did not answer desktop Research questions.
This was one inline research lane, not a research swarm. No independent source
proved local Research controls; that remains a live-test obligation.

## Skill format

[OpenAI Build skills](https://learn.chatgpt.com/docs/build-skills), fetched through
the provider-native MCP, specifies `SKILL.md` with `name` and `description`,
optional scripts/resources, and repository discovery under `.agents/skills`.
Use one canonical skill and a project-local Claude discovery link; do not
install a global plugin or change desktop extensions just to load this workflow.

## Synthesis

Not applicable: one inline research report. Full automation and both live
collection runs are still unverified. Planning cannot be promoted to acceptance.

Browser surface, one inline lane, no independent second lane:

- A maintained open-source driver exists for every mode asked for. Playwright
  covers headed, headless and a persistent profile. Its MCP server is built
  for an agent at the controls: at the candidate version it logs the page's
  console and saves downloads by itself, and takes configuration, including
  a code file, from environment variables (run 018). The library under it
  has none of this unless a script asks for it, so the design uses the
  library (D10).
- The nearest prior work (Oracle) documents the two ideas this change
  needs: a dedicated profile with operator sign-in instead of copied
  cookies, and a follow-up that reopens the saved conversation address and
  refuses when it cannot prove it is the same conversation. Its code was not
  read.
- No reviewed project covers Claude, ChatGPT and Gemini together with the
  job, duplicate and citation guarantees of this change. The gap is the
  provider adapters, not the driver.
- The published consumer terms of the three providers each restrict some
  automated use, in different words (Q8). That is a decision for the
  account holder, recorded before any live run. The programmatic interfaces
  of Q7 are supported routes under their own terms; they are not the same
  products.
