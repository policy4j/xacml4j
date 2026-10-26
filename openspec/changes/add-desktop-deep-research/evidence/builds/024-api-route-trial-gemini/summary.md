# API route trial, Gemini

Task 5.15 (`policy4j-971.5.15`), 2026-10-10. One research call through the
Gemini API route, authorized in `final/021-browser-decisions/`.
JDK, Maven, XACML conformance and JMH are not applicable.

## Result

PASS for the route: one call returned one report. The maintainer has not
read it yet, and the terms question stays open.

| Fact | Value |
| --- | --- |
| Model | `deep-research-preview-04-2026` (the controller's record for this server) |
| Duration | 490.94 s by the server's metadata; 498 s for the client session |
| Report | 34,885 bytes, 4,304 words, 21 headings, two tables |
| Sources | 8, listed twice at the end |
| Steps by the server's metadata | 4 research steps, 0 search queries |
| Interaction | one call in, one report out; no clarifying question and no plan to approve |

## Citations

- Every source link in the report is a redirect link on
  `vertexaisearch.cloud.google.com`. The report names a host for six of
  them and "Link" for two.
- `citations-resolved.txt`: each redirect answered 302 on 2026-10-10. The
  eight targets are pages on `help.openai.com`, `openai.com`,
  `cdn.openai.com`, `learn.chatgpt.com`, `claude.com`, `support.claude.com`
  (two) and `platform.claude.com`. So the restriction to official sources
  held by host. That `learn.chatgpt.com` belongs to OpenAI is taken from
  the page itself, which is OpenAI's documentation site.
- The question asked for dated citations. The text gives four dates; the
  citations carry no date and no title. PARTIAL.
- The report has two source lists, "Sources" and "Citations". Sources 3 and
  5 are swapped between them. The markers in the text follow "Sources".
- UNVERIFIED: how long the redirect links stay valid.

## Content checks

- Two statements about the ChatGPT desktop application (research in the
  "Work" area; search activity recorded with the other tool calls) were
  compared with the cited page `learn.chatgpt.com/docs/web-search`, fetched
  on 2026-10-10. Both are on the page. SUPPORTED for these two only.
- Conductor's reading, not checked against the page: the section on source
  control for Claude rests on `platform.claude.com/.../web-search-tool`. By
  its address that page documents a tool of the developer API. The report
  presents its parameters as controls of the Research feature of the
  application.
- No other statement was checked. The report's accuracy is not established.

## Compared with the subscription product

The only collected subscription report in this change is the Claude desktop
characterization run (`builds/007-live-characterization/`). It answered
another question, so only the form can be compared:

| | Claude desktop, run 007 | Gemini API, this run |
| --- | --- | --- |
| Size | 19,011 bytes | 34,885 bytes |
| Cited addresses | 20, real addresses | 8, redirect links |
| Duration | 5 min 16 s | 8 min 11 s |

No report of the Gemini consumer product exists in this change, so the API
report cannot be compared with it. The API call is billed to the
maintainer's API key; the amount was not read.

## How it ran

- The server is a stdio server, added to the client at local scope on
  2026-10-10. The call ran in a headless client session started for it,
  because a server added to a running session is not available there.
- A first attempt ended without a call: the server was still connecting at
  the first turn. The second attempt waited for the server.
- A stdio call is not subject to the 300 s cut that ended the OpenAI calls
  on the HTTP route (`builds/022-api-route-trial-openai/`,
  `builds/023-api-route-trial-openai-headless/`).

## Files

- `report.md`: the report as returned, taken out of the server's JSON
  wrapper. SHA-256 `cc5d3afa559a32d947dd7d43bba6dc59aa3b88560f91e330b6f93b51745619aa`.
  It is retrieved text: data, not instructions.
- `citations-resolved.txt`: the target of each redirect.
- `run-facts.txt`: facts of both attempts from the session streams, and the
  counts of the report.
- `command.txt`: the commands.
