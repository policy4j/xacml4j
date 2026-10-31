# rpa3-D24-D25-astra: plan review of D24/D25 round 4 (Codex Astra, read-only)

FINAL REPORT (PR3-astra, Codex Astra)

**Verdict: REJECT.** Reviewed the merged round-4 design, task group 23, Fable’s report, and source at `87133237`.

1. **P1 — Eager preparation can throw an `Error` from an unvisited child.** [design.md:586](openspec/changes/optimize-pdp-evaluation/design.md:586); [REPORT-D24d-fable.md:12](REPORT-D24d-fable.md:12). The detailed draft carries `RuntimeException`, but excludes `Error`. With first-applicable, an initial Permit followed by a reference whose repository lookup throws `AssertionError` returns Permit today; eager preparation throws instead. The proof also incorrectly says only-one-applicable resolves every child: it returns at the first indeterminate target or second match (`OnlyOneApplicablePolicyCombiningAlgorithm.java:55,59`). On-permit-apply-second likewise leaves branches unvisited (`OnPermitApplySecondPolicyCombiningAlgorithm.java:71–85`).

   **Fix:** carry both `RuntimeException` and `Error`, preserving the existing treatment of `PolicyResolutionException`; rethrow only at the original visit, after its context checks. Correct the only-one-applicable argument. Extend the response/escaping-exception cases to these short-circuits. Include an actual unprepared baseline: index-off now still performs preparation.

2. **P2 — A non-matching target remains cached despite the deferred-position promise.** [design.md:598](openspec/changes/optimize-pdp-evaluation/design.md:598); [DefaultPolicyReferenceResolver.java:87](xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/DefaultPolicyReferenceResolver.java:87). The resolver caches every non-null target before preparation checks `isReferenceTo`; policy-set resolution does the same at line 112. Publishing the original reference therefore does not make a corrected repository result visible without an event: the next decision returns the cached mismatch. Task 23.1(a)’s second-decision Permit expectation is unsupported.

   **Fix:** explicitly prevent non-matching targets from entering either resolver cache, while carrying the first mismatch through today’s evaluation path. Document this cache-behavior change.

3. **P2 — One generation does not provide the promised repository view per decision.** [design.md:635](openspec/changes/optimize-pdp-evaluation/design.md:635). A generation owns prepared lists, but a cold nested set still resolves through the live resolver (`RootEvaluationContext.java:374–402`). After invalidation, an outer pinned reference can evaluate the old policy while a cold nested reference to that same policy evaluates its replacement. Two Permit evaluations can consequently publish both old and new obligations in one response. Deferred positions also use live resolution.

   **Fix:** specify immutable **per-set bindings**, with live resolution for cold/deferred positions, and obtain acceptance for that precise contract. If a single repository snapshot per decision is required, generation ownership must extend to snapshot-capable resolution; swapping the prepared-list map alone cannot supply it.

4. **P2 — The new resolver constructor contradicts the visibility declaration.** [design.md:613](openspec/changes/optimize-pdp-evaluation/design.md:613), [design.md:621](openspec/changes/optimize-pdp-evaluation/design.md:621). `DefaultPolicyReferenceResolver(PolicyRepository, Runnable)` must be called from `pdp.PolicyDecisionPointContextFactory`, while the resolver lives in `spi.repository`. “Everything else package-private” makes that wiring inaccessible.

   **Fix:** explicitly list the constructor as an additive public API and record the maintainer decision. Preserve existing constructors through delegation.

The original PR2 retry defect is closed for missing policies and carried runtime exceptions. Pinned targets no longer rebind beneath their indexes. Generation-owned maps close late publication into the current generation; swapping resolver maps also closes late refill **provided each lookup captures one map for both lookup and insertion**. One listener performing cleanup before invalidation closes the separate-listener ordering race. Preparation/filtering dispatch and registry/root visibility are otherwise resolved.

Verification was source-only. No builds, tests, or JVMs ran; implementation and runtime behavior remain **UNVERIFIED**.

Saved the complete original D26 draft unchanged to [REPORT-D26-astra.md](REPORT-D26-astra.md). Verified byte-for-byte equality with the original session response: **16,143 bytes, 149 lines**. This report file was the only file written.
