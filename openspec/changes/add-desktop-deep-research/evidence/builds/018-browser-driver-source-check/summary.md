# Driver source check

Task 5.1 (`policy4j-971.5.1`), 2026-10-09. The swarm reviews rv006, rv007
and rv009 made claims about the candidate driver. Before the plan was
changed for them, the conductor (Claude Opus 5.5) checked each claim in the
package that is installed on the maintainer's machine: `@playwright/mcp`
0.0.73 with `playwright-core` 1.60.0-alpha-1777669338000. Read-only search
of the bundled source file; no server and no browser was started.
JDK, Maven, XACML conformance and JMH are not applicable.

## Result

SUPPORTED for four claims, PARTIAL for one. Line numbers are those of
`playwright-core/lib/coreBundle.js` in that package (`findings.txt`).

| Claim | Found | Label |
| --- | --- | --- |
| The server saves downloads by itself | A listener for the page's download event calls a handler that asks for an output file with the prefix "download" (lines 57927, 57990) | SUPPORTED |
| The server writes console messages to a file | A log file object for the console is created per tab and lines are appended to a file (lines 57466, 57933, 58034) | SUPPORTED |
| Configuration comes from environment variables, one of which loads code | More than thirty `PLAYWRIGHT_MCP_` variables are read, among them one for an init page (line 65423); init pages are loaded with `require` (line 57964) | SUPPORTED |
| The evaluate tool has no argument channel | Its schema has a function text, an optional element and a file name (line 59413 and the lines before it) | SUPPORTED |
| Chrome is started with a mock keychain | The default switches include `--password-store=basic` and `--use-mock-keychain` (lines 32165, 32166) | SUPPORTED |
| An old element reference is not rejected | Not located with certainty in the bundle | PARTIAL: taken from two reviewers' reading of the source |

## What the library offers instead

From the library's documentation through Context7 (`/microsoft/playwright`)
and the same bundle: a persistent context with a browser channel, headed or
headless; an option to leave out default launch switches; a download
setting with the values accept and deny, and waiting for the download event
of one action; a strict-mode error when a locator matches more than one
element. These are documentation and source facts. Task 5.3 shows each on a
local fixture.

## What it led to

Design D10 names the Playwright library, in a script that the bridge owns,
as the driver, with a launch contract. Design D11 removes the two keychain
switches. The MCP server is not used inside jobs.
