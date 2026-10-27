# rpa2-D24-D25-astra (Codex Astra, NTM worktree lane, read-only)

FINAL REPORT (PR2-astra, Codex Astra)

**Verdict: REJECT.** The request lookup, eligibility, API, and comparator revisions address their earlier findings. Failed binding and snapshot consistency remain unresolved.

1. **P1 — Failed binding still causes a second resolution.** [design.md:565](openspec/changes/optimize-pdp-evaluation/design.md:565)

   Preparation resolves a reference, then puts a failed binding into `Any` on the original `createContext` path. That path resolves again (`PolicyIDReference.java:78`). The resolver caches only successful results (`DefaultPolicyReferenceResolver.java:87`).

   A repository that fails once and then supplies a Permit policy therefore still gives Indeterminate on the linear path and Permit on the prepared path. Calling the entry “bound” does not fix the previous P1.

   **Fix:** resolve once at the normal reference visit, use that result to construct the context, and retain only successful bindings for subsequent preparation. Alternatively, explicitly carry the preparation failure to that decision’s original evaluation point without retrying or caching the failure across decisions. Pin this exact decision/status case.

2. **P1 — An immutable index is paired with entries that can change targets.** [design.md:574](openspec/changes/optimize-pdp-evaluation/design.md:574), [design.md:634](openspec/changes/optimize-pdp-evaluation/design.md:634)

   “Finishes with [the snapshot]” conflicts with an old-generation entry falling back or rebinding. Classification uses the old target; evaluation can use its replacement.

   Example: two Deny children initially target `x` and `y`. For request `x`, the index selects the first. After repository updates swap their targets, fallback evaluates the first against `y`; the second remains excluded. The result becomes NotApplicable although both complete versions produce Deny.

   **Fix:** pin each successfully bound target immutably to the snapshot used for candidate selection. Invalidation replaces future snapshots; it must not rebind entries in an acquired snapshot. Keep the original reference/context/cycle behavior around that pinned target. Add a response-level case with invalidation between selection and evaluation.

3. **P2 — D25 still specifies the rejected two-listener arrangement.** [design.md:647](openspec/changes/optimize-pdp-evaluation/design.md:647)

   D24 requires one listener, but D25 says to register the registry’s listener after the resolver’s. Registration order is not completion order with the repository’s configurable executor (`AbstractPolicyRepository.java:74,167`). The resolver also currently registers itself in its constructor (`DefaultPolicyReferenceResolver.java:71`).

   **Fix:** make both sections require the same single callback: resolver cleanup first, registry invalidation second. Explicitly prevent a second resolver/registry registration. The agreed single-callback ordering addresses the earlier dispatch problem; the remaining D25 text does not.

4. **P2 — Registry publication needs an explicit atomic boundary.** [design.md:573](openspec/changes/optimize-pdp-evaluation/design.md:573)

   A generation check followed by `putIfAbsent` is insufficient: a builder checks generation `g`, invalidation increments and clears, then the builder inserts its old snapshot after the clear. The proposed “rejected or cleared” argument misses this ordering.

   **Fix:** make generation validation and publication atomic with invalidation. One simple design atomically replaces a generation object containing its own map; late builders can then populate only the retired map. Otherwise specify the shared synchronization explicitly. Verify the observable result after this interleaving, not generation counters.

5. **P2 — Index opt-out contradicts D25’s default prepared path.** [design.md:592](openspec/changes/optimize-pdp-evaluation/design.md:592), [design.md:642](openspec/changes/optimize-pdp-evaluation/design.md:642)

   D24 says a PDP without `targetIndex()` evaluates the original list. D25 says prepared lists exist for every default PDP and provide its benefit with indexing off. Following D24 literally disables D25 in the default configuration.

   **Fix:** distinguish preparation from filtering: eligible default evaluation uses prepared children; `targetIndex()` controls candidate filtering only. Custom contexts and combiners receive original children.

6. **P3 — Registry visibility remains contradictory.** [design.md:630](openspec/changes/optimize-pdp-evaluation/design.md:630)

   D25 still calls `PreparedPolicySets` package-private, contrary to the approved public type at line 600.

   **Fix:** update D25 to the approved public class with package-private implementation details. No further API decision is needed.

The earlier findings otherwise close at plan level:

- **Request prefetch:** the default path through `resolveEntity` → handler `getEntity` → `request::getEntity` → `Entity.resolve` cannot call the PIP or populate its resolved-attribute cache. Missing request values correctly retain linear evaluation. Sources: `RootEvaluationContext.java:284`, `DefaultEvaluationContextHandler.java:332`, `PolicyDecisionPointContextFactory.java:170`, `Entity.java:204`.
- **Overrides and algorithm eligibility:** exact classes address the previous extension bypass and ID ambiguity (`design.md:577,588`). The approved scan probes are accepted under the stated trusted-extension boundary.
- **Cross-package API:** the approved public registry and root constructor resolve accessibility (`design.md:600`).
- **Differential comparison:** `Result.equals` plus error-chain and order comparisons, using independent equivalent fixtures, addresses the comparator gap (`design.md:611`, `tasks.md:146`). The concurrency cases above still need explicit coverage.

Failed resolution must retain Indeterminate/processing-error under [XACML 3.0 §7.15](https://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html).

Verification: reviewed the main checkout’s current D24/D25 and tasks 23/24, both Fable reports, and the relevant main-tree source. No files edited; no builds or tests run. Counterexamples are static analysis; runtime behavior remains **UNVERIFIED**.
