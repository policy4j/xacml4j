# rv003: plan review, JMH method (task 1.1)

Date: 2026-10-26. Lane: Codex CLI 0.164.0, GPT-6.1 Terra (max), with the role
`.ntm/roles/policy4j_jmh.md`, read-only, in NTM session
`xacml4j--pdp-jmh-plan`. Report: `report.md` (taken from the Codex session
record).

Verdict: REJECT, repairable within the harness.

| # | Severity | Finding | Resolution in the artifacts |
| --- | --- | --- | --- |
| F1 | blocker | No lifecycle or isolation contract for load repositories; no path for pre-populated stores | D3, D4 `StoreSession`, unique repository ids, `StoreCapability.LOAD`; D7 |
| F2 | major | Decision checks do not cover the timed lookups | D5 lookup check; spec scenario for unreferenced policy and wrong version |
| F3 | major | Synthetic scaling claim wrong for Deny | D9; per-request reporting |
| F4 | major | Lookup workload is not the resolver workload | D7 queries from references, with constraints and kinds |
| F5 | major | `gc.alloc.rate.norm` is not always reported | D11 "not reported"; spec "Benchmark set"; research Q2 |
| F6 | major | `-t` safety not proven | D8 default `-t 1`, `CONCURRENT_READS`, `verifyConcurrent` |
| F7 | major | Failed combinations vanish from results | D11 `-foe true`, completeness check; smoke test `shouldFailOnError` |
| F8 | major | Baseline not reproducible from a commit id | D11 `env.txt` content |
| F9 | minor | Cursor overhead and offsets | D8 cursor; `cursor` control method in D7 |
| F10 | minor | Load operation labels | D7 table and lifecycle note |
| F11 | minor | JMH smoke tests overlap parallel builds | Test Strategy; Execution Plan (lanes do not run tests) |
