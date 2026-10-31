# Browser surface research

Task 5.1 (`policy4j-971.5.1`), 2026-10-09. Research for a browser lane of
this change: drivers, prior open-source work, official API routes, provider
terms, and what is installed locally. One inline lane by the conductor
(Claude Opus 5.5) with MCP research tools. No provider page was opened, no
package was installed, no client configuration was changed.
JDK, Maven, XACML conformance and JMH are not applicable.

## Result

PARTIAL: primary sources support the tooling and terms statements; every
statement about a provider's web interface is still unobserved.

| Question | Answer | Label |
| --- | --- | --- |
| Is there a maintained open-source driver for headed, headless and signed-in use? | Yes. Playwright MCP covers all three, with a persistent profile by default. Playwright CLI, Chrome DevTools MCP and agent-browser cover them too. | SUPPORTED: README files at recorded revisions |
| Does a project already do this for the three providers? | Not one reviewed project. Oracle does it for ChatGPT, including Deep Research and a fail-closed follow-up. Small scripts exist per provider. | SUPPORTED for Oracle; PARTIAL for the rest and for the absence |
| Is there an official programmatic route? | OpenAI and Google document one. Not established for Anthropic. | SUPPORTED for two; UNVERIFIED for the third |
| Do the consumer terms allow automated access? | Each of the three texts restricts it. The clauses are quoted in `research/README.md` Q8. | SUPPORTED: provider pages; a reading, not legal advice |
| What is installed? | Node, Chrome and Edge; `@playwright/mcp@0.0.73` and `chrome-devtools-mcp@0.25.0` globally. | SUPPORTED: `local-probe.txt` |

## Tool lanes

| Lane | Status |
| --- | --- |
| GitHub MCP, repository search and file fetch | SUPPORTED |
| DuckDuckGo MCP, search and fetch | SUPPORTED |
| OpenAI documentation MCP, Claude Code documentation MCP | SUPPORTED for one lookup each |
| Context7 | UNAVAILABLE: gateway error, DNS resolution failed; one call |
| Tavily | UNAVAILABLE: same gateway error; one call |
| Deep research MCP | NOT RUN |

## Files

- `sources.md`: sources, revisions, blob ids, and the limits of the run.
- `command.txt`: the tool calls.
- `local-probe.txt`: local versions and package names.
- `sha256.txt`: hashes of the three large fetched files.

## What it led to

Design decisions D9 to D15, the capability `browser-research-session`
(11 requirements, 37 scenarios), task group 5, and the finding that the
terms question also covers the desktop surface that was approved on
2026-10-04 without it.
