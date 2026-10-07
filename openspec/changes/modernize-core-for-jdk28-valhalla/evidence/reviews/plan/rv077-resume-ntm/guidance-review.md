# Guidance Review: Research Tools

**Reviewer**: `agy_gemini_pro_3_1` (Gemini 3.1 Pro High)

| Finding | Severity | Source evidence | Recommendation | Change applied | Residual risk |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Missing exposed tools** | Low | `AGENTS.md` table lists `tavily_search`, `tavily_extract`, `tavily_research`. Conductor inventory also exposes `crawl` and `map` for Tavily. | Add `crawl` and `map` to the Tavily tools list in `AGENTS.md` to accurately reflect the exposed inventory. | None (Read-only) | Agents might miss `crawl`/`map` for site analysis by over-relying on the table instead of discovering via MCP. |
| **Tool name precision** | Low | `AGENTS.md` lists `resolve_library_id` and `query_docs`. Conductor inventory mentions "context7 resolve/query". | Verify exact tool names exported by the MCP server and update `AGENTS.md` if the names are just `resolve` and `query`. | None (Read-only) | Agents might hallucinate the tool names if they do not inspect the MCP schema first. |
| **Health vs. Exposure distinction** | None | `RESEARCH.md` accurately states that tool presence in `AGENTS.md` is not a health guarantee, and explicitly cites Tavily's HTTP 429 quota failure on 2026-10-04. | Maintain this clear distinction between a configured server and a healthy, quota-available service. | None (Read-only) | None. The guidance correctly steers agents to fallback on `duckduckgo` after a quota failure. |
| **Trust and secret boundaries** | None | `RESEARCH.md` commands: "Generalize outbound queries... never private source, proprietary payloads, credentials". "Do not read or print secret-bearing configuration fields." | Keep these strict data-exfiltration and secret-reading boundaries in place. | None (Read-only) | None. The boundaries are strictly and explicitly defined. |
| **Portable paths & Minimality** | None | `AGENTS.md` and `RESEARCH.md` use minimal, portable paths and do not prescribe hardcoded home directories or OS-specific paths. | Keep paths relative to the repository root. | None (Read-only) | None. |

DONE_GUIDANCE_REVIEW_20261004
