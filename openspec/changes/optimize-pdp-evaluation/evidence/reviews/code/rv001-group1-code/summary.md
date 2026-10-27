# rv001: code review of group 1 (gate task 1.3)

Date: 2026-10-27. Lane: `policy4j_code_review_codex_astra` role, Codex CLI, GPT-6 Astra (a family other
than the author's, Claude Opus 5.5), read-only. Report: `report.md` (part A; part B is the plugin review).

Verdict A: APPROVE WITH CHANGES (1 minor).

| # | Severity | Finding | Resolution |
| --- | --- | --- | --- |
| A1 | minor | the rule misses `java.lang.Math.random()` and a statically imported `randomUUID()` | rule extended (qualified and receiver-less forms, `SecureRandom.getInstance`, `SplittableRandom`, `RandomGenerator`); 6 more invalid rule cases |
| residual | - | tests miss concurrency, hex past 9, exception cleanup | `CorrelationIdTest` gained `sequenceIsHexadecimal`, `concurrentCallsGetDistinctCorrelationIds`, `requestIdIsRemovedWhenEvaluationThrows` (7 tests pass) |

Re-review: rv003-group1-fixes (Astra, lane rv010).
