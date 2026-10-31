# rv002: plan review, architecture (task 1.1)

Date: 2026-10-26. Lane: Agy 1.4.7, Gemini 4 Argon (high), with the role
`.ntm/roles/policy4j_arch_review.md`, read-only, in NTM session
`xacml4j--pdp-jmh-plan`. Report: `report.md` (taken from the Agy conversation
store; paths rewritten to repository-relative form).

Verdict: APPROVE WITH CHANGES.

| # | Severity | Finding | Resolution in the artifacts |
| --- | --- | --- | --- |
| F1 | major | The in-memory null defect also hits `getPolicySets`; lookups must dispatch by type | Context; D7 query kinds; spec "Same answers" by type |
| F2 | major | No shared contract for request selection between the test and benchmark lanes | D4 `PdpFixture.requests`; D13 `selected()` |
| F3 | major | `PolicyStores.find` cannot pass `probe:a:b` | D4 `PolicyStores.select`, `SelectedStore` |
| F4 | major | Synthetic request ids collide for n < 3 | D9 slot ids; spec "Slots and outcomes" |
| F5 | minor | `PolicySource` record with `byte[]`; "request bytes" wording | D4 final class; spec "Deterministic output" |
| F6 | minor | Snapshot store should use a direct executor and override `get` | D10 |
| F7 | minor | Discover marshalling providers once | D4 |
| F8 | minor | deny-overrides stops at the first Deny | D9 |
| F9 | nit | Task 2.6 reference, scaling result file, shade output file, sorted directory files | research Q3, Benchmark Plan, D2, D9 |

Not adopted as written: the claim that `MDCSupport` calls
`Math.abs(RANDOM.nextLong())` and that the context factory creates
`new Random(...)` per call. The source has one shared static `Random` in each
class (`MDCSupport.java:49,56`, `PolicyDecisionPointContextFactory.java:68,111`);
the contention point stands and is handled by `optimize-pdp-evaluation`
group 1.
