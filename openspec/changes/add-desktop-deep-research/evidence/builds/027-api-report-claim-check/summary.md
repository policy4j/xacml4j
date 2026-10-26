# Claims of the two API reports against their cited pages

2026-10-10. Read-only check of the reports of runs
`builds/024-api-route-trial-gemini/` and
`builds/026-api-route-trial-openai-gpt55/`. It was made before the
maintainer took the API route out of the tests
(`final/028-api-trial-withdrawn/`) and is kept as a record. It is not proof
for a task or requirement of this change.
JDK, Maven, XACML conformance and JMH are not applicable.

## Result

| Report | Claims checked | Supported | Other |
| --- | --- | --- | --- |
| OpenAI, `gpt-5.5` | 20 | 14 | 3 not on the cited page, 2 without a citation, 1 not checked |
| Gemini | 22 | 16 | 1 supported for another feature than stated, 1 misapplied, 1 partial, 3 not checked |

- The OpenAI report's unsupported points are extra statements that cite a
  page which does not contain them. Nothing it says was found wrong.
- The Gemini report's section on source control for Claude is built on the
  documentation of a tool of the developer API and presents it as a control
  of the Research feature of the application (claim G16).
- Two dates given by the reports for Anthropic pages could not be seen in
  the fetched text.

`claims.md` has every claim with its page.

## Limits

- Five of the eight pages were fetched in part. A claim marked "not
  checked" may be further down its page.
- Fetched text is the page as the fetch tool extracted it; dates and other
  page metadata can be missing from it.
- The check covers the claims listed, not every sentence of the reports.

## Found on the way

These are statements of provider pages as fetched on 2026-10-10. They
touch this change's plan and were not followed up.

- Release notes of Anthropic, October 7, 2026: "Max and Team plans now
  include monthly API credits for running your own apps and agents on the
  Claude Platform."
- Release notes of Anthropic, August 25, 2026: memory "is on by default for
  Free, Pro, and Max plans". September 16, 2026: Cowork "available from any
  conversation ... without choosing a mode first", rolling out gradually.
  Both bear on D13 (memory as a private source) and on the interface that
  was characterized on 2026-10-04.
- OpenAI's documentation page on web search: in the desktop application
  research is the "Deep research plugin" in the "Work" area. That differs
  from the start by command or tools menu on the help page, and bears on
  the desktop adapter for ChatGPT.

## Files

- `claims.md`: the pages, the claims and the result for each.
- `command.txt`: the fetches.
