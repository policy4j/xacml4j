# rv001: code review of the plugin change (gate task 1.3)

Date: 2026-10-27. Lane: `policy4j_code_review_codex_astra` role, Codex CLI, GPT-6 Astra (a family other
than the author's, Claude Opus 5.5), read-only. Report: `report.md` (part B; part A is optimize group 1).

Verdict B: APPROVE WITH CHANGES (2 minor). Every version matched Maven Central metadata of 2026-10-26.

| # | Severity | Finding | Resolution |
| --- | --- | --- | --- |
| B1 | minor | the spec promised unchanged headers and identical JAXB sources, against the evidence | spec separates the default build, the JAXB comment allowance, and the opt-in header update |
| B2 | minor | the characterization script misleads when rerun on the new tree | `characterize-v2.sh` forces versions and the update; its run is `tests/003-characterization-v2` |

Re-review: rv002-plugins-fixes (Astra, lane rv010).
