# Browser research cross-check and driver probe

Task 5.1 (`policy4j-971.5.1`), 2026-10-09. Follow-up to run 014 for two
reasons: the maintainer asked why research MCP tools were reported as
unavailable, and review rv005 asked for evidence about the exact driver
version. Conductor: Claude Opus 5.5. Read-only; no browser was started and
no provider page was opened.
JDK, Maven, XACML conformance and JMH are not applicable.

## Why two research servers failed in run 014

SUPPORTED by the MCP controller's diagnostics and a shell check:

- Tavily and Context7 are remote routes through the local MCP gateway. Both
  calls failed there with "backends required DNS resolution which failed".
- The hosts of both services resolved from the shell at the same time, and
  the controller's check of the gateway reported the Tavily and
  Gemini-search routes as failing while the deep-research route passed.
- So the fault was the gateway process, not the services and not the
  network. Run 014 called them "UNAVAILABLE" without that cause, and did not
  try the client's own Context7 connector, which does not use the gateway.
- The gateway was restarted through its controller (`restart` of the proxy
  entry). After that the controller reported the routes as passing and both
  tools answered in this client.

## Cross-check results

| Lane | Result | Label |
| --- | --- | --- |
| Tavily search, after the restart | Three more projects that drive these web applications with Playwright: an archived ChatGPT automation MCP server, a Gemini image skill with a persistent profile, and a published Gemini Deep Research skill. Two of them warn about interface changes and about terms of service. None covers the three providers | SUPPORTED: search results and README excerpts; code not read |
| Context7, client connector and gateway | The library entry `microsoft/playwright-mcp` exists, version `v0.0.80`; an orchestrator project for sharing one Chrome between agents is listed | SUPPORTED for the listing only |
| Deep research MCP | Not run. The controller reports its route as passing | NOT RUN |

The findings are in `research/README.md` Q5 and Q6. They agree with run 014
and change no decision.

## Driver probe for the candidate version

SUPPORTED, local, read-only (`driver-probe.txt`, `driver-help-0.0.73.txt`):

- The npm registry lists `@playwright/mcp` `0.0.73` with an integrity value;
  the newest version is `0.0.83`.
- The installed package is `0.0.73`, Apache-2.0, binary `playwright-mcp`,
  with `playwright` and `playwright-core` `1.60.0-alpha-1777669338000`.
  The installed copy has no recorded integrity; the design does not use it.
- The help output of `0.0.73` has every option that design D10 and D11 use:
  `--headless`, `--user-data-dir`, `--isolated`, `--storage-state`,
  `--cdp-endpoint`, `--extension`, `--allowed-origins`, `--caps`,
  `--output-dir`, `--browser`, `--snapshot-mode`, `--secrets`,
  `--init-script`, `--config`. It lacks `--idle-timeout` and
  `--profile-dir-name`, which the README of the main branch lists.
- The tool list and schemas of `0.0.73` were not read: that needs a running
  server and belongs to task 5.3.

## Other observations

- The maintainer's MCP controller lists a `playwright` server (on demand,
  stopped) and research servers for OpenAI and Gemini models with
  credentials present. None was called.
- Task numbers changed after rv005: characterization is now task 5.8. Run
  014 still says 5.3.
