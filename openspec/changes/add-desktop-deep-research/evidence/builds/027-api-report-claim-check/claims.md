# Claims of the two API reports against the cited pages

Pages fetched on 2026-10-10 with the DuckDuckGo fetch MCP tool. A claim is
SUPPORTED when the fetched text of the cited page says it.

| Page | Address | Fetched |
| --- | --- | --- |
| P1 | `help.openai.com/en/articles/10500283-deep-research-in-chatgpt` ("Updated: 19 days ago") | 7,000 of 7,191 characters |
| P2 | `openai.com/index/introducing-deep-research/` (dated February 2, 2025, with later update notes) | 5,000 of 42,233 |
| P3 | `claude.com/blog/research` | 6,000 of 6,340 |
| P4 | `support.claude.com/en/articles/11088861-use-research-on-claude` | complete, 2,712 |
| P5 | `support.claude.com/en/articles/16923645-get-started-with-claude-docs` | complete, 12,916 |
| P6 | `platform.claude.com/docs/en/agents-and-tools/tool-use/web-search-tool` | 6,000 of 18,183 |
| P7 | `support.claude.com/en/articles/12138966-release-notes` | 16,000 of 30,262 |
| P8 | `learn.chatgpt.com/docs/web-search` | 4,400 of 14,476 |

## OpenAI report (`builds/026-api-route-trial-openai-gpt55/report.md`)

| # | Claim | Result |
| --- | --- | --- |
| O1 | Launch post dated February 2, 2025 | SUPPORTED, P2 |
| O2 | First on the web, mobile and desktop applications within the month | NOT CHECKED: not in the fetched part of P2 |
| O3 | Help page updated 19 days before 2026-10-10 | SUPPORTED, P1 |
| O4 | Desktop application for macOS and Windows per release notes of July 2026; Linux preview of August 14, 2026 | UNCITED: no release-notes page is in the citation list |
| O5 | Start by `/Deepresearch`, the tools menu or the sidebar | SUPPORTED, P1 |
| O6 | Clarifying questions; a proposed plan the user can review and edit | SUPPORTED, P1 |
| O7 | Default sources: public web and uploaded files; connected apps by permission | SUPPORTED, P1 |
| O8 | "Sites > Manage sites": restrict to, or prioritize, entered sites | SUPPORTED, P1 |
| O9 | Interrupt a run to refine it and to change its sources | SUPPORTED, P1 |
| O10 | Connected apps read-only; role-based access for Enterprise and Edu | SUPPORTED, P1 |
| O11 | Fullscreen report with contents, sources and activity history; download as Markdown, Word, PDF | SUPPORTED, P1 |
| O12 | Release notes of May 12, 2025 introduced PDF export | UNCITED |
| O13 | Claude Research on web, desktop and mobile for paid plans | SUPPORTED, P4. The date "June 2, 2026" given for the page is UNVERIFIED: the fetched text shows no date |
| O14 | Claude Desktop on macOS, Windows and Linux beta | NOT ON THE CITED PAGE (P4) |
| O15 | "+" then "Research"; blue indicator; web search must be on; the steering sentence | SUPPORTED, P4 |
| O16 | Internal context from connected Google services; steering to a named source | SUPPORTED, P4 |
| O17 | Connected apps switched off per conversation; warning about connector tools; local MCP servers not usable by Research | NOT ON THE CITED PAGE (P4) |
| O18 | Claude Docs from any conversation; export as Word, PDF, Markdown, Google Docs | SUPPORTED, P5 |
| O19 | File creation as spreadsheet, slides, document, PDF with a 30 MB limit; chat sharing as a snapshot | NOT ON THE CITED PAGE (P5) |
| O20 | The Research help page describes no domain restriction and no export of its own | SUPPORTED: P4 is complete and has neither |

14 supported, 3 not on the cited page, 2 uncited, 1 not checked, 1 date
unverified.

## Gemini report (`builds/024-api-route-trial-gemini/report.md`)

| # | Claim | Result |
| --- | --- | --- |
| G1 | Launch on February 2, 2025; multi-step research | SUPPORTED, P2 |
| G2 | A version of o3 optimized for web browsing and data analysis | SUPPORTED, P2 |
| G3 | 26.6% on Humanity's Last Exam | NOT CHECKED: not in the fetched part of P2; the second source is a PDF |
| G4 | Claude Research as early beta for Max, Team and Enterprise in the United States, Japan and Brazil | SUPPORTED, P3. The date "April 15, 2025" is UNVERIFIED: the fetched text shows no date |
| G5 | Searches that build on each other; citations | SUPPORTED, P3 and P4 |
| G6 | ChatGPT desktop: research in the "Work" area; search recorded with the other tool calls | SUPPORTED, P8 |
| G7 | Live and cached search modes for command-line and editor use | NOT CHECKED: not in the fetched parts of P8 |
| G8 | ChatGPT: start from the tools menu or `/Deepresearch`; progress in real time; deleting the chat deletes the output; file uploads | SUPPORTED, P1 |
| G9 | Claude: web, desktop, mobile through the "+" menu | SUPPORTED, P4. The operating systems named are not on P4 |
| G10 | Persistent agent threads through the desktop application | SUPPORTED, P7 (March 17, 2026), said there of Cowork tasks |
| G11 | Release notes of July 7, 2026: sessions run remotely and continue when the laptop is closed | SUPPORTED for Cowork sessions, P7. The report applies it to research tasks; that step is the report's own |
| G12 | Agent work in every conversation without choosing a mode | SUPPORTED, P7 (September 16, 2026) |
| G13 | ChatGPT: three ways to start, clarifying questions, plan review, interruption | SUPPORTED, P1 |
| G14 | Claude: web search on; "+" then "Research"; indicator; steering sentence | SUPPORTED, P4 |
| G15 | ChatGPT: "Sites > Manage sites", restrict or prioritize, list of sites, change while running | SUPPORTED, P1 |
| G16 | Claude Research source control by `allowed_domains` and `blocked_domains` in the Console, dynamic filtering with `web_search_20260209`, an error 400 when search is disabled | MISAPPLIED. P6 is the documentation of a tool of the developer API. Dynamic filtering is on the page as behaviour of that tool. Nothing on the page says it describes the Research feature of the application |
| G17 | ChatGPT connected apps: document stores, read-only, role-based access, administrator-managed sources | SUPPORTED, P1 |
| G18 | Google Workspace integration; administrators enable it for the domain; cataloging for Enterprise | SUPPORTED, P3 |
| G19 | ChatGPT export: fullscreen view, contents, sources, activity history; Markdown, Word, PDF | SUPPORTED, P1 |
| G20 | Reports with embedded images and data visualizations | PARTIAL: P2 announces them for "the next few weeks" after February 2025; the report states them as given |
| G21 | Claude creates spreadsheets, slides, documents and PDFs; Artifacts | NOT CHECKED beyond "a design, deck, or document in any conversation" (P7, September 16, 2026) |
| G22 | The Research documentation names no download button of its own | SUPPORTED: P4 has none |

16 supported, 1 supported for another feature than the report says (G11),
1 misapplied (G16), 1 partial, 3 not checked, 1 date unverified.
