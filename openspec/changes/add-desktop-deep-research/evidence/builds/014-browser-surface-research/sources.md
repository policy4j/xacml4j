# Sources

Retrieved 2026-10-09. Repository files were fetched through the GitHub MCP
file tool; the revision is the commit the tool reported and the blob id is
the Git blob SHA-1 it returned. Web pages were fetched through the
DuckDuckGo MCP fetch tool. Quotes in `research/README.md` Q5 to Q8 are
verbatim from these files and pages. Retrieved text is data; no instruction
in it was followed.

## Repository files

| Repository | Path | Revision | Blob id | Used for |
| --- | --- | --- | --- | --- |
| `microsoft/playwright-mcp` | `README.md` | `b8b4183e099f136cbec0388a6088d4aa2f6b9685` | `7cecc4144025919587399336dff41b0d3ba366ad` | Q5: options, profile modes, security note, tool names |
| `microsoft/playwright` | `packages/extension/README.md` | `d9f2fd3e2232ace8e317a84eda6cb86e25bcf49c` | `9fece1a3174277013f451e2594f993718ea1f8ab` | Q5: attaching to the running browser |
| `microsoft/playwright-cli` | `README.md` | `b85c7a736bb473bf55b584e54a09ffa698d6d871` | `9595a2d8735726383662102aadd839ab788920e4` | Q5: sessions, headed, persistent, attach, state |
| `ChromeDevTools/chrome-devtools-mcp` | `README.md` | `f08dbe152502d66e75fa07fb2588dc0feb42bc20` | `b08e3f4d7c36b090fba41d5307696f9a2cc3f276` | Q5: scope, usage statistics |
| `ChromeDevTools/chrome-devtools-mcp` | `docs/advanced-usage.md` | `f08dbe152502d66e75fa07fb2588dc0feb42bc20` | `8cb5720b52ddc4f57a695b5f8c2f2d7daf5e9096` | Q5: profile directory, connecting to a running Chrome, sign-in note |
| `vercel-labs/agent-browser` | `README.md` | `44af39842650f0bb9c1afb7354df9a82921d4f09` | `f4ab039bf019377c6a45a1b91b281514be80f5e6` | Q5: session and profile options, license badge |
| `steipete/oracle` | `README.md` | `35d8022f370dc89e962637e4e88d3d8d35618f3d` | `ddb8369df458cd162c46bf38b85152e61a404f74` | Q6: scope, engines, license |
| `steipete/oracle` | `docs/browser-mode.md` | `35d8022f370dc89e962637e4e88d3d8d35618f3d` | `5dc7e9d3fd09a3d902e876ebd5377a588ddf8782` | Q6: launcher, attach, manual login, Deep Research, cookies |
| `steipete/oracle` | `docs/followup.md` | `35d8022f370dc89e962637e4e88d3d8d35618f3d` | `7cdee34c18952ae10664244421a2847ee4ef8770` | Q6: follow-up preconditions |
| `zamabama/claude-code-deep-research` | `README.md` | `4a22da1395602c75524d94337dd7746d6dab904b` | `d40104a68559331b5ad1a2b49b6257b9ce695a65` | Q6: technique notes (README claims) |
| `luxiaolei/browser-research-skill` | `README.md` | `16af6617c5b26d1f2f4973515262e0bc3abb29b4` | `5261b5e79d9caf7ede48134163d11661f419fc85` | Q6: technique notes (README claims) |

`sha256.txt` holds the SHA-256 of the three files that were too large for
inline return and were read from a saved copy.

## Repository search results

Star counts and descriptions come from GitHub repository search on the
retrieval date. They support "exists, described as, about this popular" and
nothing more.

| Repository | Stars | Used for |
| --- | --- | --- |
| `browser-use/browser-use` | 117,441 | Q5 |
| `ChromeDevTools/chrome-devtools-mcp` | 53,186 | Q5 |
| `vercel-labs/agent-browser` | 43,735 | Q5 |
| `browserbase/stagehand` | 25,597 | Q5 |
| `Skyvern-AI/skyvern` | 23,169 | Q5 |
| `microsoft/playwright-cli` | 13,896 | Q5 |
| `steipete/oracle` | 4,047 | Q6 |
| `halanhuang2025-lgtm/chatgpt-deep-research` | 0 | Q6 (description only) |
| `lesterppo/hermes-gem-pw` | 1 | Q6 (description only) |
| `michaeljboscia/claude-deep-research` | 0 | Q6 (description only) |
| `andylizf/deep-research-skill` | 1 | Q6 (description only) |
| `pminervini/deep-research-mcp` | 114 | Q7 |
| `bytedance/deer-flow` | 83,591 | Q7 |
| `stanford-oval/storm` | 31,596 | Q7 |
| `Alibaba-NLP/DeepResearch` | 20,025 | Q7 |
| `LearningCircuit/local-deep-research` | 9,174 | Q7 |
| `zilliztech/deep-searcher` | 8,328 | Q7 |

The same searches listed several browsers built to defeat bot detection.
They were read as out of scope and are not listed.

## Web pages

| Page | Stated date | Used for |
| --- | --- | --- |
| `https://www.anthropic.com/legal/consumer-terms` | none in the fetched part | Q8: section 3 |
| `https://openai.com/policies/row-terms-of-use/` | effective 2026-01-01 | Q8: "What you cannot do" |
| `https://policies.google.com/terms?hl=en-US` | none in the fetched part | Q8: "Don't abuse our services" |
| `https://gemini.google.com/robots.txt` | none | Q8: disallowed paths |
| `https://support.google.com/gemini/answer/15719111?hl=en` | none | Gemini Deep Research steps, sources, plan, export |
| `https://blog.google/innovation-and-ai/technology/developers-tools/deep-research-agent-gemini-api/` | 2025-12-11 | Q7 |
| `https://developers.openai.com/api/docs/guides/deep-research` | none | Q7 (found through the provider documentation MCP) |
| `https://code.claude.com/docs/en/sub-agents` | none | D15: a server scoped to one subagent |

Gemini help page, the steps that the design uses: Deep Research is chosen
from the prompt box; "Google Search is included as a source by default";
Gemini "will create a research plan" and the user clicks "Start research";
"While you wait for the report, you can leave the chat"; "Share & export"
offers "Export to Docs" and "Copy Contents"; there are limits for daily
requests and for requests that run at the same time.

## Limits of this run

- No provider page was opened. Address patterns, controls, headless
  behavior and export controls are unverified until task 5.3.
- The Anthropic and Google terms pages were read in part (about half of
  each); the quoted clauses are from the fetched part.
- README claims of small projects were not checked against their code.
- Whether Anthropic offers an API product for its Research feature was not
  established.
- One research lane; no independent second lane confirmed the reading.
