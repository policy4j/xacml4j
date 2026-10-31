# API route trial, OpenAI: first attempt

Task 5.15 (`policy4j-971.5.15`), 2026-10-10. One research call through the
OpenAI API route, authorized in `final/021-browser-decisions/`.
JDK, Maven, XACML conformance and JMH are not applicable.

## Result

FAILED: no report. The client ended the call after 300 s, and the provider
task is reported as failed. The trial is not done; this run says nothing
about the quality of the route.

- The call went to the research server that is wired to the client. The
  server's log names the model `gpt-5.5-pro` and shows that the provider
  task started and was polled eleven times (`server-log-excerpt.txt`).
- The client ended the call: "timed out after 300s". The server logged the
  cancelled request.
- `research_status` for the task afterwards: "status: failed". No report.
- Cost: the provider task ran for about five minutes on the maintainer's
  API key. The amount was not read.

## Cause

Corrected by `builds/023-api-route-trial-openai-headless/`: the call was
repeated with the variable named below set, and the client ended it at
300 s again. That the limit is on the client's side is SUPPORTED. That it
is the idle window described here is UNVERIFIED. The text below is the
reading at the time of this run.

From the client's documentation (page `en/mcp`,
read 2026-10-10): "A tool call to an MCP server that sends no response and
no progress notification for the idle window aborts with an error instead
of waiting for the wall-clock limit." and "The idle window defaults to five
minutes for HTTP, SSE, WebSocket, and claude.ai connector servers, and to 30
minutes for stdio servers."

- The server is wired over HTTP through the local gateway, with no
  per-server `timeout`. It sent no progress notification.
- `MCP_TOOL_TIMEOUT` is one hour in the maintainer's settings. That is the
  wall-clock limit; it does not change the idle window.
- The conductor read the one-hour setting and started the call without
  checking the idle window. The same limit had ended a call on 2026-10-08.

UNVERIFIED: why the provider task failed. The second task of 2026-10-08,
which the client had also ended, shows "failed" too. That fits a server
that stops the provider task when its request is cancelled, but the
server's source was not read.

## What the trial needs

As read at the time; run 023 tried the first of these and it did not change
the limit on the HTTP route. One of these, in the maintainer's client
configuration, then a new session:

- `CLAUDE_CODE_MCP_TOOL_IDLE_TIMEOUT` in milliseconds in the settings'
  environment, next to `MCP_TOOL_TIMEOUT`. It also covers the Gemini server,
  which is a stdio server with a 30-minute idle window.
- A per-server `timeout` on the server's entry, which is also the floor of
  its idle window. The entry is written by the MCP controller, so a manual
  value there can be overwritten.

No configuration was changed in this run.

## Files

- `command.txt`: the call and the read-only checks.
- `server-log-excerpt.txt`: the server's lines for this call, with the
  provider's task identifier shortened, and the two status answers.
