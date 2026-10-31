# Local desktop capability probe

Date: 2026-10-04. Conductor: GPT-6. JDK: NOT APPLICABLE.

Result: PARTIAL. The installed Swift execution context reported Accessibility
and screen-recording access. Both app bundles were read; window Accessibility
queries succeeded. Native desktop/browser MCP tools were not exposed.

ChatGPT's installed bundle id is `com.openai.codex`, version `26.928.31416`;
Claude's is `com.anthropic.claudefordesktop`, version `2.19675.0`.
ChatGPT was launched with `rtk open -a ChatGPT`. The initial `com.openai.chat`
lookup returned no process and was corrected using the installed plist.
Claude was activated and its reversible `AXManualAccessibility` attribute was
enabled for inspection. `AXEnhancedUserInterface` returned unsupported attribute.

Accessibility trees exposed few nodes and no exact allowlisted Research control.
The first ScreenCaptureKit probe aborted (`CGS_REQUIRE_INIT`, exit 134).
The corrected main-actor capture initialized AppKit/CoreGraphics and succeeded
for a ChatGPT window. In-memory OCR returned only the allowlisted `New chat`
label; no image or arbitrary recognized text was persisted. Claude had no
qualifying visible window during that capture.

A later all-window inventory reported ChatGPT: 13 windows, 2 visible, 3 above
the size threshold; Claude: 12 windows, 0 visible, 4 above the size threshold.
This confirms the visibility limitation for that observation. It does not
explain why the windows were hidden or establish Research availability.

A follow-up Claude Accessibility query established that its main window was
minimized. Activation, raising, and unminimizing returned success. Once restored,
the tree had 148 nodes, including one text area and 18 buttons; fixed allowlisted
controls included `Claude`, `Code`, and `Send`. No text was entered. The original
minimized state was restored successfully afterward. This adds verified limited
window control and richer inspection, not proof of research-mode selection.

This proves limited local observation and app activation only. It does not
prove sign-in, Research availability, focus-safe input, submission, completion,
or report collection. No prompt, research request, account export, client/MCP
configuration change, package installation, or Java test/build was performed.

Authority: the user's request to control these local desktop apps authorized
the reversible launch, activation and scoped inspection probes. No additional
prompt was submitted. Implementation approval is a separate project gate.

Signing metadata reported OpenAI TeamIdentifier `2DC432GLL2` and Anthropic
`Q6L2SF6YDW`; full cryptographic signature validation was not run. ChatGPT
declares `scripting.sdef`, whose generic Chromium window/tab id, URL and
JavaScript declarations were read. No Research-specific command was found.
Claude declares no scripting dictionary. Native scripting runtime and a stable
research-job locator remain UNVERIFIED. See `signature-command.txt` and
`signature-output.json` for the bounded metadata probe.

`probe-output.txt` is a transcription of relevant tool stdout, preserving the
observed values. It excludes private UI text, process ids, transient addresses,
and checkout paths. `command.txt` retains the exact portable commands for app
metadata, permission preflight, and the corrected visual probe.
