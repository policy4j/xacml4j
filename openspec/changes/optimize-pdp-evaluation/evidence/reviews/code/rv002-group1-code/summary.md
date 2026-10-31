# rv002: code review of group 1 (gate task 1.3)

Date: 2026-10-27. Lane: Agy 1.4.7, Gemini 4 Argon (high), role `policy4j_code_review`, read-only, second
packet in the rv007 pane. Report: `report.md`.

Verdict: APPROVE (2 minor, 2 nits).

| # | Severity | Finding | Resolution |
| --- | --- | --- | --- |
| 1 | minor | no test of hex past 9 or of MDC cleanup on exceptions | tests added (see rv001) |
| 2 | minor | `RootEvaluationContext` puts evaluated policies into a `ConcurrentSkipListMap`, whose `put` draws from `ThreadLocalRandom`, per decision | outside group 1 (thread-local, no contention); a candidate for the first profiling round (task 2.1) |
| 3 | nit | the rule misses `SecureRandom.getInstance`, `SplittableRandom`, `RandomGenerator` | added |
| 4 | nit | the evidence index and tasks were not updated | updated with this gate |
