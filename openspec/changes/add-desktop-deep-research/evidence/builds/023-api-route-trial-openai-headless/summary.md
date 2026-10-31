# API route trial, OpenAI: second attempt, HTTP route, headless session

Task 5.15 (`policy4j-971.5.15`), 2026-10-10. The maintainer asked to make
the configuration changes of run 022 and to continue. This run repeats the
OpenAI call on the same HTTP route in a new headless client session that
had the idle-timeout variable set.
JDK, Maven, XACML conformance and JMH are not applicable.

## Result

FAILED: no report. The client ended the call at 300 s again. The variable
did not change the limit on this route. The cause given in run 022 is
therefore not established.

- Before the call: `CLAUDE_CODE_MCP_TOOL_IDLE_TIMEOUT` was added with the
  value 3600000 to the environment block of the maintainer's client
  settings, and it was also set in the environment of the session's
  command. `MCP_TOOL_TIMEOUT` was 3600000 as before.
- The session called the tool once with the query only (`run-facts.txt`).
  The tool result was the error "MCP server "deep-research-mcp" tool
  "deep_research" timed out after 300s".
- The gateway's log shows the order: the client sent
  `notifications/cancelled`, and the `tools/call` request ended 13 ms
  later after 300,058 ms (`gateway-log-excerpt.txt`). So a timer of the
  client ended the call, not the gateway and not the server.
- Cost: a second provider task of about five minutes on the maintainer's
  API key, model `gpt-5.5-pro`. The amount was not read.

## What is known about the limit

- SUPPORTED: it is on the client's side, it is 300 s, and it applied to
  the HTTP route in an interactive session (run 022) and in a headless one
  (this run), client version 2.1.296.
- SUPPORTED: a call to a stdio server in the same kind of session ran for
  498 s (`builds/024-api-route-trial-gemini/`).
- UNVERIFIED: which timer it is. The client's documentation describes an
  idle window of five minutes for HTTP servers and names the variable that
  was set here as its override. Either the override does not take effect
  on this route, or another timer applies. The client's debug log was not
  taken.
- Not tried: a per-server `timeout` on the HTTP entry, which the
  documentation describes as the floor of the idle window. That entry is
  written by the maintainer's MCP controller and was left as it is.

## Consequence

The next OpenAI attempt uses a stdio server with a per-server timeout
(`builds/<next>-api-route-trial-openai-stdio/`).

## Files

- `run-facts.txt`: facts from the session's message stream and its times.
  Its last script line names a "report" file; that file holds only the
  72-character error text.
- `gateway-log-excerpt.txt`: the gateway's two lines for the end of the
  call, session identifiers removed.
- `command.txt`: the commands.
