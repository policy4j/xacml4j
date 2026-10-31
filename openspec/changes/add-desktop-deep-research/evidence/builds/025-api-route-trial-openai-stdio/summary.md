# API route trial, OpenAI: third attempt, stdio server

Task 5.15 (`policy4j-971.5.15`), 2026-10-10. The OpenAI call again, this
time through a stdio research server, so that the client's 300 s cut on
the HTTP route (`builds/023-api-route-trial-openai-headless/`) does not
apply.
JDK, Maven, XACML conformance and JMH are not applicable.

## Result

FAILED: no report. The call ran 432 s and was not cut by the client. The
provider ended the research task with a rate-limit error for the model.

- The tool result, complete, is in `provider-error.txt`: "Research task
  failed", code `rate_limit_exceeded`, "Rate limit reached for gpt-5.5-pro
  ... on tokens per min (TPM): Limit 1000000, Used 988729, Requested
  48026."
- So within one minute this one research task used nearly all of the
  organization's one million tokens per minute for that model, and its next
  request went over.
- The client session ran from 12:04:17 to 12:11:38 EDT and called the tool
  once with the query only (`run-facts.txt`).

## What this shows

- SUPPORTED: on a stdio server with a per-server timeout the client does
  not end a call at 300 s. This call ran 432 s; the Gemini call ran 498 s
  (`builds/024-api-route-trial-gemini/`).
- SUPPORTED: with `gpt-5.5-pro` and this account's limit the research task
  does not complete. It is a limit of the account for the model, not of
  the client or the controller.
- UNVERIFIED: whether the two earlier provider tasks, which show "failed"
  after the client ended their calls, would have met the same limit.
- UNVERIFIED: the cost. Three provider tasks ran for five to seven minutes
  each on the maintainer's API key with the pro model, the last one at
  close to one million tokens per minute at its end. The amounts were not
  read; the usage page of the provider account has them.

## Open for the maintainer

Whether to try the route with another model of the controller's catalog
(`gpt-5.5`, `gpt-5.4` and smaller, each with its own rate limit and
price), to raise the account's limit, or to judge the API routes on the
Gemini report alone. No further OpenAI call is made without that answer.

Answered on 2026-10-10 in the client's question dialog. The options were
"Try gpt-5.5 (Recommended)", "Try gpt-5.4-mini", "Stop the OpenAI leg" and
"I raise the limit first". The maintainer chose "Try gpt-5.5": one more
call through the same kind of stdio server with the non-pro model. That is
run `builds/026-api-route-trial-openai-gpt55/`.

## Files

- `provider-error.txt`: the tool result with the organization identifier
  removed.
- `run-facts.txt`: facts from the session's message stream and its times,
  with the same removal.
- `command.txt`: the commands.
