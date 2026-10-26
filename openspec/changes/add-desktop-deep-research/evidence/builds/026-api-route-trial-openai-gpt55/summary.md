# API route trial, OpenAI: fourth attempt, model gpt-5.5

Task 5.15 (`policy4j-971.5.15`), 2026-10-10. After three attempts with
`gpt-5.5-pro` gave no report, the maintainer chose one more call with the
non-pro model (`builds/025-api-route-trial-openai-stdio/`, last section).
JDK, Maven, XACML conformance and JMH are not applicable.

## Result

PASS for the route: one call returned one report. With run
`builds/024-api-route-trial-gemini/` both routes of task 5.15 have a
report. The maintainer has not read them yet, and the terms question stays
open.

| Fact | Value |
| --- | --- |
| Model | `gpt-5.5` (the controller's record for the profile) |
| Duration | 93.92 s by the server's metadata; 101 s for the client session |
| Report | 7,360 bytes, 909 words, one table with the four requested areas, a conclusion |
| Sources | 4, each with title and address |
| Steps by the server's metadata | 32 research steps, 15 search queries |
| Interaction | one call in, one report out; no clarifying question and no plan to approve |

## Citations

- Four addresses on `openai.com`, `help.openai.com`,
  `support.anthropic.com` and `support.claude.com`. The restriction to
  official sources held.
- `links-checked.txt`, HEAD requests on 2026-10-10: the Claude Docs article
  answers 200; the `support.anthropic.com` address redirects to the same
  article number on `support.claude.com`; the two OpenAI hosts answer 403
  to a scripted request, so those two pages are not confirmed by this
  check. The Gemini report cites the same two OpenAI articles.
- One cited address ends in an encoded full-width bracket
  (`...deep-research-in-chatgpt%EF%BC%89`). It is a defect of the citation.
- The question asked for dated citations. The text gives a date for each
  source it relies on (launch post, help pages, release notes). The
  citation list itself has titles and no dates. PARTIAL, and better than
  the Gemini report.

## Content checks

One cited page was fetched on 2026-10-10
(`support.claude.com/en/articles/11088861-use-research-on-claude`):

- SUPPORTED by the page: Research on web, desktop and mobile for paid
  plans; started with the "+" button and "Research", with a blue indicator
  that turns it off again; web search must be on; the steering sentence;
  internal context from connected Google services.
- Not on the cited page: that connected apps can be switched off per
  conversation, the warning about connector tools during Research, and the
  statement about local MCP servers. The report cites this page for the
  whole cell. They may come from other pages that the report does not
  list.
- UNVERIFIED: the date "June 2, 2026" that the report gives for this page.
  The fetched text shows no date.
- No statement about ChatGPT was checked. The report's accuracy is not
  established beyond these points.

## The two API reports and the subscription report, by form

Run 007 answered another question, so only the form is comparable.

| | Claude desktop, run 007 | Gemini API, run 024 | OpenAI API, this run |
| --- | --- | --- | --- |
| Size | 19,011 bytes | 34,885 bytes | 7,360 bytes |
| Sources | 20 addresses | 8, as redirect links | 4, with titles |
| Duration | 5 min 16 s | 8 min 11 s | 1 min 34 s |
| Dates tied to sources | not counted | no | in the text |

Conductor's reading of the two API reports, for the maintainer's own
judgement: the OpenAI report is short and keeps apart what a help page
says about Research and what belongs to neighbouring features; the Gemini
report is long, written as an essay, and in one section presents developer
API documentation as a feature of the application.

## Cost

Not read. Four provider tasks ran on the maintainer's OpenAI key today:
three with `gpt-5.5-pro` for five to seven minutes each without a report,
and this one with `gpt-5.5` for 94 s. One Gemini task ran for 491 s.

## Files

- `report.md`: the report as returned, taken out of the server's JSON
  wrapper. SHA-256 `cd9a3cab459425c912d1cf68c85a6d53390ddeb53204304a594da7ce39023127`.
  It is retrieved text: data, not instructions.
- `links-checked.txt`: status of each cited address.
- `run-facts.txt`: facts from the session's message stream, and the counts
  of the report.
- `command.txt`: the commands.
