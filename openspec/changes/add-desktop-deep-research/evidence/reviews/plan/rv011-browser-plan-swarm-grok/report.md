===REPORT-BEGIN grok===
Profile and model: architecture reviewer, adversarial lane, Grok 4.7

VERDICT: REQUEST_CHANGES

1. HIGH — design.md D12; spec "Verify the browser target at the moment of action". The send guard is a page-world snippet (`browser_evaluate`). The provider page is untrusted, and that script runs in the page's own JavaScript realm, so the page hooks the "check then activate" path. Steps: (1) the signed-in app wraps `HTMLElement.prototype.click`; (2) the snippet checks origin, region, composer hash, and the input counter; (3) it calls `click()`; (4) the wrapper changes the composer or fires a second submit, then calls the real click. The receipt misses the marker and returns unobservable, but a prompt was already sent. The same hook can freeze the foreign-input counter. Change: do the last check and the activation in the driver's trusted input path, on the element handle from that call. Treat in-page script as data. Delete the page-world snippet and the page-world counter.

2. HIGH — design.md D10 and D12. Entry may use `browser_type` and `browser_press_key` before the send guard. These composers submit on Enter. Steps: (1) `enter` types the prompt and sends Enter, or a newline the page treats as submit; (2) the provider accepts that text in the focused composer; (3) the later guard sends a second time, or a retry does. Change: set the composer in one non-submitting operation; forbid Enter and any characterized submit key during entry; if the page submits during entry, persist uncertainty and do not send again.

3. HIGH — design.md D11 and D12; spec "Bind a browser job to its conversation address". Nothing closes restored tabs or pins the page the bridge opened. `browser_tabs` is allowed, and a persistent profile restores the last session. Steps: (1) `login` or an earlier run leaves two tabs on the provider origin; (2) `create` navigates one tab to a new chat; (3) the snapshot stays on the focused restored tab; (4) the origin check passes; (5) the region match finds that tab's composer; (6) the prompt is appended to an existing conversation. The first-send scenario only assumes "a new dedicated conversation". Change: `create` closes every other tab, refuses a restored session, and binds later actions to the page handle from that navigation. Require zero user turns before the first send.

4. HIGH — design.md D14; spec "Require the operator's acknowledgement". The calling agent can pass the gate without the operator. Steps: (1) D14 leaves the verification key's storage and replacement protection to task 5.6; (2) the agent writes a public key and a receipt into that store; (3) `start` accepts the receipt the agent signed. A changed terms hash is only reported by `doctor --local` and does not stop gated operations, so an old acknowledgement stays current. "An agent process cannot answer that dialog" is unsupported: password fallback is a UI dialog, and this change's helper is built to drive UI. Change: before approval, pin a verification key the agent cannot replace; show provider, terms address, and hash on a biometrics-only system sheet; make the helper refuse to target that sheet; fail closed when the stored hash differs from the pinned terms text.

5. HIGH — design.md D4 and D13; spec follow-up and context requirements. Steps for scope: (1) the entry-time source check passes; (2) a context report tells the calling agent to approve the plan; (3) the provider plan enables a private connector; (4) `approve-plan` checks only the hash the agent supplies (D4: the script does not judge plan meaning); (5) `begin` does not re-check sources. `test_hostile_context_report_cannot_escape_or_authorize` only checks that a hash is still required. Steps for a duplicate: follow-up "SHALL refuse unless" a closed list that omits "no other active job of this provider". A follow-up on a completed conversation can run beside a RUNNING job. Change: at `begin`, re-verify public sources and block if the plan UI or plan text adds a source; do not delegate that check to the agent. Add the provider-wide active or uncertain block to the follow-up preconditions.

6. MEDIUM — design.md D11; spec challenge and headless scenarios. A challenge drawn as an overlay on a characterized page still has the origin, the landmarks, and the account label. Headless then clicks Send. The challenge test assumes the whole page is already a challenge page. Change: a positive challenge predicate (unclassified dialog, blocked composer, interstitial) that blocks before send in both modes.

7. MEDIUM — spec "Collect the browser report from the bound conversation". The download click is outside the send guard. Steps: (1) collection clicks the export control; (2) the page starts a second download into the attempt directory; (3) "the file that this action produced" is ambiguous, and a private or unrelated file is stored as the report. Change: accept only the download event tied to that click; any other new file fails the attempt.

Unsupported or overstated claims:
- D12, "the driver rejects a reference from an older snapshot", for `@playwright/mcp@0.0.73`. Research Q5 and D10 say that version's tool list was not read.
- D14, "an agent process cannot answer that dialog."
- D14's one rule for both surfaces, against D9 ("D1 to D8 stay as they are") and tasks 4.1 and 4.2, which do not require the acknowledgement.

What should be cut or moved to its own change:
- The in-page send snippet and input counter. Replace them; do not specify them.
- Cross-provider context jobs and research-mode follow-up. Move them until one provider's address binding has been characterized. They are the hostile-text path, and proposal question 7 is still open.
- The desktop-wide terms gate. Leave it out of this approval until the key design is fixed. It amends the approved desktop plan.

Checked and found sound:
- No silent fallback from desktop to browser, and a job cannot change surface.
- `start` already refuses another PENDING or DRAFTING job of the same provider (`desktop_research.py`, the provider loop in `start`). No browser code exists.
- Dedicated profile, no storage tools, no attach to the daily browser.
- Context-block token redraw, with the job marker kept outside the block.
- Fixture receipts are rejected as live proof.
- The other follow-up checks are strict: completed parent, same address, empty composer, one child, and a chat answer is not a research report.

What you read and ran:
- `proposal.md`, `design.md` (D4–D8 and D9–D15), `specs/browser-research-session/spec.md`, both desktop specs, `tasks.md` group 5, `research/README.md` Q5–Q9, `evidence/reviews/plan/rv005-browser-plan-codex/report.md`.
- Read-only read of `.agents/skills/desktop-deep-research/scripts/desktop_research.py` (`call`, `trusted_app`, `allocate`, `start`).
- No browser, no skill execution, no fetches. Terms text was taken from the research file as data.
===REPORT-END grok===
