
**D27: REJECT. JSON izog corrections: APPROVE.**

Reviewed the merged main working tree at HEAD `8713323705611502ad0e1576554fdaa4e3fbf374`, the supplied patches/report, and both D27 drafts. Custom contexts and resolvers are assumed to be trusted in-process code.

Findings 1–5 concern `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java`.

1. **P1 — Missing inline dependency edges permit deadlock.**  
   **`DefaultEvaluationContextHandler.java:325`** records a dependency only when another caller already owns the requested flight. With thread 1 owning A and running B inline, and thread 2 owning C, B can wait for C while C waits for A. The map contains `B→C` and `C→A`, but omits `A→B`; both waits pass admission and never finish. Ancestry alone cannot close this cross-thread cycle.

   **Fix:** record parent-to-child dependencies for newly owned inline flights too, under the same admission monitor; remove them on exit.  
   **Test:** two callers, dependencies `A→B→C→A`; require bounded completion with the existing cycle/fallback outcome.

2. **P2 — A stale cache miss can return absence/failure after a bag was published.**  
   **`DefaultEvaluationContextHandler.java:325`** installs a new flight without rechecking positive state. A caller can miss the cache, pause in the request path, then resume after another caller has published a bag and removed its flight. Its new PIP attempt can return absence or throw, despite the decision already having an authoritative bag. `publish` only reconciles positive results (`:345`).

   **Fix:** after winning ownership, recheck applicable published positive state before executing the attempt.  
   **Test:** pause one caller after its initial miss, let another publish, then resume the first against a PIP miss/failure; both must receive the published bag.

3. **P2 — Wait edges identify keys rather than active flight instances.**  
   **`DefaultEvaluationContextHandler.java:382`** can mistake a completed attempt for a later retry. Suppose B’s edge to A₁ remains until its awakened waiter runs `release`; A₁ completed absent and was removed. A₂ retries and needs B. Admission sees `A→B→A` and returns false absence, although B was waiting for the already completed A₁.

   **Fix:** identify dependency nodes by flight identity and retire completed nodes/edges atomically under the monitor.  
   **Test:** retry an absent key while another resolution is returning from its completed flight; assert the dependent result, without inspecting graph state.

4. **P2 — Executor submission failure strands the installed flight.**  
   **`DefaultEvaluationContextHandler.java:330`** places completion/removal solely inside the submitted runnable. If the approved executor seam rejects submission or throws before running it, attached callers wait forever and subsequent calls keep finding that incomplete flight.

   **Fix:** guard submission; complete exceptionally and conditionally remove the flight when submission fails. Keep task cleanup for accepted work.  
   **Test:** a rejecting runner followed by an accepted retry; assert failure propagation and successful later resolution. The current direct runner does not reject, but the seam is incomplete.

5. **P2 — Selector failures are still remapped separately for each attached caller.**  
   **`DefaultEvaluationContextHandler.java:224`** catches `PathEvaluationException` outside the shared flight. A path error from the context-selector entity fallback is shared internally, then owner and waiter each construct a different `AttributeReferenceEvaluationException`. This misses the requested owner-exception sharing contract.

   **Fix:** normalize fallback path failures inside the shared attempt before completing it; retain the outer mapping for request-content failures.  
   **Test:** overlapping failing selector lookups receive the same final mapped exception, with unchanged syntax/processing status and message.

6. **P3 — One added test pins mechanism rather than the public outcome.**  
   **`DefaultPolicyInformationPointTest.java:228`** verifies “never put” and no context interactions. Replace that assertion with an observable outcome—for example, a cache hit still returns its bag when cache writes fail. Also, submitting the second lookup immediately before releasing the first (`DefaultEvaluationContextHandlerTest.java:1057`) does not establish attachment; the failure-sharing test should pin the shared exception once overlap is established.

**HashMap-chain judgment:** a chain is sufficient while each active flight has at most one synchronous dependency. Guava is not required for that invariant. The present chain is insufficient because it omits inline edges and conflates successive flights of one key.

**Other checks:** flight installation uses `putIfAbsent`; resolver work runs outside map callbacks and the admission monitor. Entered attempts complete/remove on `Error`; ordinary mapped designator failures and errors are rethrown from the shared future. Handler identity is included in scoped ancestry. `ResolverContext` retains lazy dependency timing and empty-result retries (`ResolverContext.java:149,172`). The hash now matches deep equality and discards old serialized hashes (`ResolverCacheKey.java:67,77`); the legacy-stream test is useful. Attribute/content cache writes occur only on misses (`DefaultPolicyInformationPoint.java:83,159`).

**JSON re-check: both earlier P2s resolved.**

- Unsupported conversion is required only for a selected non-null scalar. Missing/null selections remain absent; actual unsupported conversion remains processing-error (`JsonContent.java:159,172`).
- The value configuration removes `AS_PATH_LIST` while retaining list results, and context-relative selection uses that same configuration (`JsonContent.java:90,98`).
- The two tests pin those public outcomes, including context-relative values under path-list configuration (`JsonContentTest.java:214,223`). No further finding in this correction.

**Verification:** read-only source/diff review. No files changed and no builds, tests, or JVMs started. Fable reports 229 PDP and 938 conformance tests with zero failures; those are supplied results. The schedules above remain **UNVERIFIED by runtime reproduction**.
