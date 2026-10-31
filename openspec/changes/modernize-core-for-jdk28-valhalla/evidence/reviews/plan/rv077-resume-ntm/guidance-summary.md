# Research guidance change

Maintainer request: use available MCP research tools and document them in
project AGENTS.md. Applies in donor and integration checkouts; global client
configuration is untouched.

Changed AGENTS.md with a compact Research Tools table and linked
docs/ai/RESEARCH.md. Both new sections are identical across checkouts.
CLAUDE.md remains a symlink to AGENTS.md.

Evidence: conductor callable inventory exposes Context7, Tavily,
DuckDuckGo, Deep Research, arXiv and OpenAI/Claude native docs tools.
Presence is not a health claim. Gemini search and Google/GitHub provider
docs were not exposed to this conductor. Availability is per client/lane.

Observed research calls:
- Tavily extraction succeeded earlier, then HTTP 429 rate limiting.
- DuckDuckGo fetched the official OASIS 3.0 core text successfully.
- Deep Research failed with provider token rate limiting; no synthesis
  accepted as evidence.
- DuckDuckGo's focused integer rounding search returned no results; this
  is not proof of absence and no client installation was changed.

Adversarial review: guidance-review.md, Gemini Pro 3.1.
- LOW missing Tavily map/crawl: corrected to exact tavily_map/tavily_crawl.
- LOW Context7 name precision: exact callable schemas confirm
  resolve_library_id and query_docs already correct; no change needed.
- No portability, minimality or trust-boundary finding.

Verification: focused git diff --check passed. Full plugin path-reference
gate was invoked; it reports pre-existing local paths in generated profile
sources and old evidence, plus a vendored fixture file URI. These are
outside this guidance edit. The new guidance contains only project-relative
links. Complete gate result is retained in guidance-path-gate.log;
do not claim the repository-wide gate passes while those findings remain.
