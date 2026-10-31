# Live desktop characterization, in progress

Date: 2026-10-04. Task: 1.4, claimed by codex-desktop-research.
JDK/Maven/conformance/JMH: NOT APPLICABLE.

One public characterization question was submitted in Claude's Research mode.
The submitted USER heading and body contain the exact job marker; the owned
composer is empty afterward. Research shows a lead researcher, a research
result card, growing source counters, and Stop response during execution.
Research then completed in 5m 16s. A unique Research complete indicator, absent
Stop response, bound USER turn and unique document card established completion.
The document's Copy action produced Markdown; independent comparison checked
the full document's end markers, all native-text words, twenty exact cited URLs
against twenty-five native AXLink targets, and the saved hash. Clipboard contents
were restored. The report is COLLECTED as Markdown, 19,011 bytes, SHA-256
2f404a2f040846d2fe25f7508c71b890e193b34d43e9d23e6b1633201b1e1469.
Native PDF/DOCX downloads remain UNRUN.
ChatGPT has zero submissions. Its newly created window exposes the same
nonempty 12-character composer value as an existing window. The existing draft
is preserved pending the user's specific draft-clear choice.

## Observed interface

- ChatGPT 26.928.31416, com.openai.codex, TeamIdentifier 2DC432GLL2;
  Claude 2.19675.0, com.anthropic.claudefordesktop, Q6L2SF6YDW.
  Both app signatures passed deep/strict verification. The interpreter has
  Accessibility and screen-capture access; compiled-helper permission coverage
  remains UNRUN.
- ChatGPT is a caller ancestor. The native New window command created exactly
  one window while preserving both existing AX window identities. Its generic
  scripting dictionary was not usable at runtime (zero scripting windows
  despite visible AX windows). No caller window received input.
- Claude's native New chat command produced a dedicated empty composer after
  selecting its Claude tab. Research was selected via the Add menu and its
  selected state was observed. Native AXPress on Connectors did not expand the
  HTML submenu; AXShowMenu opened an irrelevant native context menu. A guarded
  hover, corroborated by AX window hit testing and fresh in-memory OCR,
  exposed the actual submenu. Native control coverage is PARTIAL for hover.
- Ten connector toggles were initially enabled. Each was changed to zero and
  independently reread; original states are retained in the private job.
  Memory was initially enabled and was disabled and reread. These are
  conversation controls; global connector configuration was not changed.
- Scrolling to the final connector row exposed all twelve rows without
  changing the inventory. Ten are toggles; two are unselected attachment
  submenus with no toggle value. A submenu exposed attachment choices rather
  than selected source checkboxes. Official MCP documentation describes this
  interface as resources/prompts selected for attachment. Claude's official
  Research guidance states that advanced Research cannot invoke local MCP
  tools. The receipt distinguishes these facts from direct toggle observation.
- A separate Web search toggle was not exposed in the inspected interface.
  Research mode selection is observed; web-search dependency is documented.
  This does not claim a directly observed Web search switch. Source preparation
  recorded a partial list observation before input; the attachment distinction
  and fresh full source check were completed before Send. Future production
  preparation must perform the complete characterization-derived check before
  any prompt entry.
- AX set-value returned success but removed exactly one trailing newline.
  Full comparison proved every other prompt character and both end markers
  unchanged. The durable input intent preceded entry. One separate guarded
  Send followed a durable possible-send intent; attempts is one. Submitted
  USER-turn evidence upgraded the binding. No resend occurred.
- Initial event-idle probes using the null event type were unsuitable. Current
  mutation probes use UInt32.max for all event types, require two seconds of
  measured idle, and recheck focus/input. Deferred probes waited at most ten
  seconds for a quiet interval before acting.
- One OCR probe crashed without CoreGraphics initialization; initializing
  CGMainDisplayID corrected it. Menu state changes sometimes invalidate old
  AX objects; fresh rereads were required. These observations are not tests of
  a production bridge.

## Evidence and limits

`receipts.json` retains redacted native observations. The Swift sources are
characterization probes, not the canonical skill or production runner.
Private job receipts and the interim assistant observation are owner-only in
user Application Support, outside Git/skill discovery. No image was persisted.
No private source names, unrelated chat text, app configuration or clipboard
contents were logged. `observed-assistant.txt` is an interim observation, not a
collected report. `report.md`, `sources.json` and `manifest.json` hold the
validated private report, exact native link targets and collection receipt.
The ten connector toggles were restored and freshly observed at their original
values. Research was turned off and reread. Preparation menus were closed.
Memory's control was no longer exposed after submission, including during a
guarded temporary Research enable/disable cycle; Memory restoration is
UNOBSERVABLE, not claimed. Global settings were not changed. Original states
and this limitation remain in the private job. This is an open characterization
task, not a completed production workflow.

Remaining authorized budget: Claude zero additional characterization sends,
one production acceptance send; ChatGPT one characterization and one production
acceptance send. All planned production tests, compiled-helper self-tests,
skill discovery/evaluations, independent code/security gates and dual-provider
acceptance remain UNRUN. Task 1.4 remains open for ChatGPT characterization,
interface/profile bookkeeping and the post-submission Memory-restoration limit.

## Official source interpretation

[Claude connector guidance](https://support.claude.com/en/articles/11175166-get-started-with-custom-connectors-using-remote-mcp)
describes conversation-specific connector toggles and the advanced Research
local-MCP limitation. [Official MCP connection guidance](https://modelcontextprotocol.io/docs/2026-07-28/develop/connect-remote-servers)
separates user-selected resource/prompt attachments from tool permissions.
These documents support interpretation; runtime source-toggle observations are
retained separately. Sources were fetched during this run.
