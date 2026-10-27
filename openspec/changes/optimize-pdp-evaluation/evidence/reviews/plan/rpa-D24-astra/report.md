# rpa-D24-astra (Codex Astra, NTM worktree lane, read-only)

FINAL REPORT (PR-astra, Codex Astra)

**Verdict: REJECT.** D24 needs changes before implementation. The findings below are source-derived counterexamples; they were not executed.

1. **P1 — Key prefetch changes decisions and observable effects.** [design.md:550](openspec/changes/optimize-pdp-evaluation/design.md:550)

   Resolving the key before combining is not equivalent to visiting children in order. For example, an unconditional first child under first-applicable returns Permit without visiting a later indexed child. The proposed prefetch nevertheless calls its PIP and adds supplied values to `Result.getResolvedAttributes()`.

   The failure fallback can change the decision too: a PIP throws on its first call and supplies the matching value on its second. Linear evaluation returns Indeterminate; index prefetch consumes the failure, falls back, and the second lookup can return Permit. Absence and failures are explicitly not memoized (`DefaultEvaluationContextHandler.java:81,246,256`); normal match evaluation records the failure (`Match.java:129`).

   **Fix:** obtain index values through side-effect-free inspection of the request or an already established decision memo. If that cannot supply a trustworthy bag, evaluate the original child list without a preliminary PIP call. Apply this restriction to both policy-set and rule indexing.

2. **P1 — Failed reference preparation is retried instead of preserving the reference error.** [design.md:543](openspec/changes/optimize-pdp-evaluation/design.md:543)

   Putting an unresolved reference in `Any` does not undo its preparation lookup. `PolicyIDReference.createContext()` resolves it again at line 78; the resolver caches only successes (`DefaultPolicyReferenceResolver.java:87`). A repository returning missing or throwing on preparation, then returning a Permit policy, produces Permit where the linear path produced Indeterminate. The approved change in resolution frequency does not authorize losing §7.15 errors.

   **Fix:** prepare reference metadata from established, successful, validated default bindings; leave uncached or failed references on their original evaluation path. Make that integration with D25 explicit. Preserve reference kind, all version constraints, and the existing context/cycle checks; do not substitute the resolved policy directly for the reference.

3. **P1 — Separate listener invalidation can publish a permanently stale index.** [design.md:555](openspec/changes/optimize-pdp-evaluation/design.md:555)

   A counter around index construction does not coordinate it with the resolver cache. Allowed sequence:

   - The index listener clears its registry and advances its generation.
   - A rebuild reads the old target from the resolver cache and publishes under that new generation.
   - The resolver listener clears its cache afterward.

   Both callbacks have finished, but the index still describes the old policy. A newly matching Deny policy can remain excluded while linear resolution returns it. Repository callbacks are separately dispatched, and the executor is configurable (`AbstractPolicyRepository.java:74,167`).

   An already acquired index can also outlive invalidation while candidate references subsequently resolve to different targets.

   **Fix:** coordinate resolver invalidation, index invalidation, and publication under one generation contract. Selection and evaluation must use compatible target versions. Specify safe concurrent identity lookup and atomic publication, and make D25’s binding/invalidation work a dependency. Independent listeners plus a build counter are insufficient.

4. **P2 — Structural eligibility can bypass a public `Target` override.** [design.md:543](openspec/changes/optimize-pdp-evaluation/design.md:543)

   `Target` has a public constructor and overridable `match()` (`Target.java:46,67`). A subclass can expose an apparently indexable equality through inherited `getAnyOf()` while its `match()` returns Match. Skipping that child can remove a Deny decision.

   **Fix:** require the engine’s concrete target implementation for structural classification; overridden behavior goes to `Any`. Also specify that function provenance identifies the actual default `string-equal` implementation and compatible invocation configuration. D15’s `isDirect()` alone is insufficient: its binding eligibility includes other public provider methods (`DefaultFunctionInvocation.java:164,182`).

5. **P2 — The approved package-private plumbing has no accessible cross-package entry point.** [design.md:557](openspec/changes/optimize-pdp-evaluation/design.md:557)

   The caller is in `org.xacml4j.v30.pdp`; `RootEvaluationContext` and the proposed registry are in `org.xacml4j.v30.policy`. The current call is at `PolicyDecisionPointContextFactory.java:171`. A package-private factory there cannot be called from the PDP package. Public methods on a package-private `TargetIndexes` class do not solve this.

   **Fix:** specify a concrete, accessible construction bridge before implementation, preserving module dependency direction. If it adds public API, record that for a maintainer decision. The fluent, default-off `targetIndex()` builder method itself fits the surrounding API.

6. **P2 — The proposed differential comparator does not prove “full responses equal.”** [tasks.md:146](openspec/changes/optimize-pdp-evaluation/tasks.md:146)

   Fable’s draft names `XacmlPolicyTestSupport.assertResponse`. Its `assertResult()` omits resolved attributes (`XacmlPolicyTestSupport.java:186`), its collection comparison ignores order (`:253`), and `Status.equals()` omits the error (`Status.java:183`). The extra PIP resolution in finding 1 could therefore pass this comparison.

   **Fix:** compare resolved attributes, complete status/error details, and contractually observable order. Keep the corpus differential, then add focused decision/status/public-API cases for the failures above, version changes during rebuild, custom target/function behavior, and only-one-applicable with zero, one, and multiple matching positions. Use independent equivalent PIP/repository state for stateful comparisons. Reuse existing staging and combining tests; no mechanism-count tests are needed.

7. **P3 — The custom-algorithm guarantee contradicts the approved ID-based allow-list.** [design.md:569](openspec/changes/optimize-pdp-evaluation/design.md:569)

   The recorded maintainer choice trusts built-in algorithm IDs. A custom implementation carrying such an ID therefore qualifies; “custom algorithms … evaluate every child” is too broad.

   **Fix:** document that unfamiliar IDs fall back and implementations using recognized IDs are trusted to preserve those algorithms’ semantics. Do not silently add a different provenance policy.

The underlying target restriction is sound **once resolution and extension effects are excluded**: first-Match No-match in every AllOf makes the first AnyOf No-match, and evaluation returns before staging or policy-ID publication (`Target.java:69`, `MatchAllOf.java:66`, `BaseCompositeDecisionRule.java:119`). The supplied ordinary, ordered, and legacy combiners tolerate those omissions. Only-one-applicable is safe because it counts matching **targets**, not final Permit/Deny results; preserve distinct child positions even when they reference the same object (`OnlyOneApplicablePolicyCombiningAlgorithm.java:52`). Excluding on-permit-apply-second is correct. These conclusions follow the checked [XACML 3.0 §§7.6–7.7, §§7.11–7.15, §7.18 and Annex C](https://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html).

Verification: read D24, task group 23, Fable’s full draft, the requested evaluation paths, test helpers, and the official standard. No files changed; no builds or tests run. Runtime behavior and performance remain **UNVERIFIED**.
