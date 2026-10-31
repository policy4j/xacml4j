
**APPROVE WITH CHANGES — all five production findings resolved; one P3 documentation correction remains.**

Reviewed the merged PDP fix, supplied patch/report, and revised tests. Unrelated `JsonContent` hunks were excluded.

| Previous finding | Re-check |
|---|---|
| P1: missing inline dependency edges | Resolved. Active same-handler ancestry takes `resolveWith`, never `outcome(running)` (`DefaultEvaluationContextHandler.java:312`). |
| P2: stale initial cache miss | Resolved. After ownership, `resolveWith` checks published positive state before calling the PIP (`:330`). |
| P2: successive flights conflated by key | Resolved. The wait-edge structure is removed. |
| P2: executor rejection strands a flight | Resolved. Execution is inline; completion/removal is covered by `catch(Throwable)` and `finally`, including `Error` (`:314–322`). |
| P2: selector failure mapped separately per caller | Resolved. Mapping occurs inside the shared attempt (`:282`); request-content mapping remains outside through the same helper (`:214,418`). |

The liveness argument holds for **this handler’s own joins**: resolver dependencies execute under ancestry, where joining another flight is prohibited. Repeated keys terminate through the ancestry cycle check. A top-level waiter therefore cannot close a same-handler dependency cycle.

Top-level coalescing retains one published bag and the same mapped exception object for attached callers (`:310,316,319,346,358`). Nested duplicates may perform separate attempts and retain their own missing/failure outcomes; positive bags still use the publication winner. That narrower contract must be recorded.

**P3 — Design and method documentation still describe the superseded implementation.**

- `design.md:879` promises general overlapping-call coalescing.
- `design.md:887,897` still requires a wait graph and executor seam.
- `DefaultEvaluationContextHandler.java:292` still begins “One attempt … per key … at a time,” although nested duplicates are allowed.

**Fix:** document top-level coalescing, inline nested duplicates, ancestry-based termination, and removal of the graph/seam. Correct the method’s opening sentence. No new test is needed for this documentation correction.

The revised tests are useful public-outcome coverage: stale-miss publication, mutually dependent attempts completing, shared mapped selector exceptions, shared failure followed by retry, and a readable cache whose writes fail (`DefaultEvaluationContextHandlerTest.java:1039,1209,1241,1276`; `DefaultPolicyInformationPointTest.java:218`). P3’s previous “never put” assertion has been replaced.

**Verification:** no edits or fresh builds/tests/JVMs. Fable reports PDP 236 and conformance 938/12 skipped, green. The wait-cycle conclusion is from source inspection; independent runtime reproduction remains unrun.
