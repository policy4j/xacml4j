# rpf-D25-fable: plan review of D25 (Claude Fable 5.1, NTM worktree lane, read-only)

**Lane cc_2 (Claude Fable 5.1), plan review of D25 (Cycle S, bound policy-reference resolution) and task group 24, read-only, 2026-10-27.** Read: `design.md:574-592` (D25 with the maintainer's decisions), `design.md:535-572` (D24 as decided), `tasks.md:144-162`, Astra's draft `report-D25-astra.md`, `DefaultPolicyReferenceResolver`, `AbstractPolicyRepository`, `PolicyIDReference`, `PolicySetIDReference`, `BaseCompositeDecisionRuleIDReference`, `PolicySet`, `RootEvaluationContext`, `PolicyDecisionPointContextFactory`, `DefaultPolicyDecisionPoint`, `DefaultPolicyReferenceResolverCacheTest`, profile 021 (`top-cpu-synthetic_1000-all.md:78,89`, `top-cpu-rsa2008-all.md`), and the XACML 3.0 core table of contents (§7.13 Policy Set evaluation, §7.14 the Indeterminate-target table, §7.15 PolicySetIdReference and PolicyIdReference evaluation, §7.17 Authorization decision, §7.18 Obligations and advice). No build run.

**Verdict: APPROVE WITH CHANGES.** The two maintainer decisions (internal only; fix the late-refill race) are right and the "Kept" list is complete. The text cannot be implemented as written: it names no home for the "prepared evaluation entries" (P1), its gain is the complement of D24 and must be scoped so (P2), and it needs D24's registry as its one mechanism instead of a second one in the resolver (P2). The race fix is correct once the increment comes first (P2).

**Answers to the five questions**

1. Home of the entries: yes, internally, in D24's per-PDP registry. Each policy set gets one prepared child list in which a reference child is a package-private `BoundReference` entry; the index of D24 is positions into that same list. `PolicySet.combineDecisions` (`PolicySet.java:177`) fetches the prepared list once per policy-set evaluation (the root walk of D17 plus one identity-keyed probe), not once per reference: on synthetic:1000 one probe replaces the 1,000 `ConcurrentHashMap.get` of today, in rsa2008's top-level set one replaces 12. Shape in P1-1. The zero-lookup form (a prepared tree threaded through the engine's own context objects, the parent entry attaching the child's node when it creates the child's context) is possible later; it is not needed to realize the gain.
2. Version ranges: already correct today, because the listener matches the event's policy against the reference's constraints (`DefaultPolicyReferenceResolver.java:134-162` through `isReferenceTo`, `BaseCompositeDecisionRuleIDReference.java:104-108`), so a new version inside a range empties the entry. The generation check is race-free only with the order "increment, then remove" and a check-and-store that is atomic per key (P2-4). Links need no matching at all: a generation bump invalidates them.
3. One mechanism. D24 already resolves reference children at build time, keeps a per-PDP identity-keyed registry, a listener, and a version counter (`design.md:555-558`, decisions at 562-566). D25's links are the resolved references of those same entries. Two structures would resolve every reference twice and could pair an index of generation g with a link of generation g+1 inside one evaluation (P2-3).
4. The gain: cycle K removed the forwarding hops, not the probe (`RootEvaluationContext.resolve`, `RootEvaluationContext.java:374-402`, still calls `resolver.resolve`, and `PolicyIDReference.evaluate:92`, `PolicySetIDReference.evaluate:96` still match), so the two 021 rows still exist and their share of a non-indexed synthetic scan is higher than 6.6% plus 1.7% after batch 2 (unmeasured). But the rows are synthetic-only (the rsa2008 table has no resolver row; rsa2008 holds 20 references in 4 sets), and D24 with the index on removes the probes of every skipped child, leaving one per decision. D25 pays only where D24 does not apply: index off, the `Any` bucket, custom algorithm ids. The text must say so (P2-2).
5. Clean and minimal after the changes: D25 becomes (a) the generation and the race fix inside the resolver, and (b) `BoundReference` entries plus two package-private context factories on the reference classes, both inside D24's registry. The draft's link key, its matched invalidation of links, and the "cache-disabled" and "custom resolver" clauses are surplus for the default PDP (P3).

**Findings**

P1-1. No concrete home for the prepared entries. `design.md:577-578` says "in its prepared evaluation entries" and 586-587 "a package-private hook in `PolicySet`", but the engine has only `PolicySet.decisionRules` (shared, immutable) and the public combiner list (`PolicySet.java:177`, `DenyOverrides.java:61-75` calls `d.createContext` and `d.evaluate` on each element). Without a defined structure the implementer either adds a field to shared objects or a per-reference map, both excluded. Fix, the shape to write into D25 (all package-private in `org.xacml4j.v30.policy`, created by the PDP as D24 decided):

```java
/** One PDP's prepared view of the policy sets it evaluated; identity-keyed, generation-stamped (D24, D25). */
final class PreparedPolicySets {
	private final AtomicLong generation = new AtomicLong();
	private final ConcurrentMap<Identity, PreparedPolicySet> sets = new ConcurrentHashMap<>();
	void invalidate() { generation.incrementAndGet(); sets.clear(); }       // the PDP's listener, after the resolver's
	PreparedPolicySet of(PolicySet set, EvaluationContext root) { ... }   // one probe per policy-set evaluation
}

/** The children of one policy set in declaration order; a reference child is a BoundReference. */
final class PreparedPolicySet {
	final List<CompositeDecisionRule> children;   // what combineDecisions hands to a built-in algorithm
	final TargetIndex index;                       // D24, or null when the opt-in is off
}

/** A reference child whose target is resolved once per generation (D25). */
final class BoundReference implements CompositeDecisionRule {
	private final PolicyIDReference ref;                   // a twin class, or a sealed pair, for PolicySetIDReference
	private final PreparedPolicySets prepared;
	private volatile Link link;                             // record Link(Policy target, long generation)

	public EvaluationContext createContext(EvaluationContext context) {
		Link l = link;
		if (l == null || l.generation != prepared.generation()) {
			l = bind(context);                                // resolver probe once per generation; null when it fails
		}
		return l == null ? ref.createContext(context)         // today's path, same errors (§7.15)
				: ref.contextFor(context, l.target);           // new package-private: cycle check, reference context, target.createContext
	}
	public Decision evaluate(EvaluationContext context) {
		Preconditions.checkArgument(context.getCurrentPolicyIDReference() == ref);
		CompositeDecisionRule target = context.getCurrentPolicy();          // the policy the context was built for
		return target == null ? Decision.INDETERMINATE : target.evaluate(context);
	}
	// isMatch alike; getId, getReference, accept delegate to ref
	private Link bind(EvaluationContext context) {
		long before = prepared.generation();
		Policy target = resolve(context);                    // context.resolve(ref) as today; null on failure
		if (target == null || !ref.isReferenceTo(target)) return null;   // the match moves from every decision to the bind
		Link l = new Link(target, before);
		if (before == prepared.generation()) link = l;        // an overlapped bind is used once, not kept
		return l;
	}
}
```

`evaluate` reads the target from the context, not from the link, so a rebind between `createContext` and `evaluate` cannot pair a context with another target; the `isReferenceTo` check (the 1.7% row) runs once per bind instead of once per decision. `contextFor` builds the same `PolicyIDReferenceEvaluationContext` (`PolicyIDReference.java:165-169`, cycle check included) and the same policy context, so ancestry, `getReferencedCompositeDecisionRules` (`EvaluationContext.java:360-370`), status routing and §7.18 staging are unchanged. `PolicySet.getDecisions()` keeps returning the original children. The prepared list exists for every default PDP; only `index` depends on the `targetIndex()` opt-in.

P2-2. Expected gain overstated. `design.md:576-577` carries PR8's 6.61% and 2.38% as if they were D25's; they are synthetic-only rows of profile 021 (`top-cpu-synthetic_1000-all.md:78,89`; nothing comparable in `top-cpu-rsa2008-all.md`) and D24 removes them for every skipped child. Fix: "Expected: the probe and the match of the reference children that D24 does not skip (index off, `Any` bucket, custom algorithm ids); on profile 021 that was 6.6% plus 1.7% of a non-indexed synthetic:1000 scan, nothing measurable on rsa2008; measured with the batch pair with the index off."

P2-3. Two mechanisms. `design.md:585-587` puts the links "in `DefaultPolicyReferenceResolver` and the default PDP", while D24 keeps a core registry reached through the root context (`design.md:555-558`). Fix: the links live in D24's registry entries (P1-1); the resolver keeps its reference cache, gains the generation and the race fix, and stays the only caller of the repository; the PDP registers the registry's listener after the resolver's (`AbstractPolicyRepository.java:158-160` keeps registration order, and the single-thread executor at 167-193 runs the callbacks in that order), so a link that rebinds after the registry bump reads a resolver cache the event has already cleaned. Task 24.2 then depends on 23.2; say it, or fold 24.2 into 23.2 as the step that builds the prepared lists before the index.

P2-4. Race fix, exact recipe. Today `resolve` reads the cache, asks the repository, and puts (`DefaultPolicyReferenceResolver.java:78-90, 103-115`); a removal that arrives between the repository call and the put leaves the removed policy cached (`DefaultPolicyReferenceResolverCacheTest.java:130-168`). Fix: each listener callback and `clearRefCache()` increment a generation first, then remove or clear; `resolve` reads the generation before the repository call and stores with an atomic per-key check:

```java
long before = generation.get();
Policy found = repository.getPolicy(ref.getId(), ref.getVersion(), ref.getEarliestVersion(), ref.getLatestVersion());
if (found != null && enableRefCache) {
	policyIDRefCache.compute(ref, (k, cached) -> generation.get() == before ? found : cached);
}
```

An increment before the check rejects the stale store; an increment after it is followed by the listener's removal of the fresh entry. Dispatch stays asynchronous, so a lookup finished before the callback runs still returns the pre-event policy until then; D25 says so already. Delete `removalDuringALookupLeavesTheLookedUpPolicyCached` and pin the fix by behavior: after a removal during a lookup, the next resolution asks the repository again and the removed policy is not returned.

P3-5. The link "key" (`design.md:580`) restates `BaseCompositeDecisionRuleIDReference.equalsTo` (`BaseCompositeDecisionRuleIDReference.java:124-129`); with one entry per reference object there is no key. Drop the sentence, and drop "invalidates matching links" (581-583): the generation bump invalidates every link, matching stays with the resolver's cache.

P3-6. "Cache-disabled mode" and "custom resolvers" (`design.md:583-584, 589`): the default PDP always builds the cache-enabled default resolver (`PolicyDecisionPointContextFactory.java:96`), so neither reaches a PDP evaluation. Keep the resolver's unit behavior (`disabledCacheAsksTheRepositoryEveryTime` already pins it) and remove "cache-disabled and custom resolution" from task 24.1; "nested response order" is `NestedResponseStagingPdpTest`, reuse it rather than add one.

P3-7. Kept list wording: cyclic references are not a §7.15 error in the standard; the engine's two reference classes also differ (`PolicyIDReference.isReferenceCyclic` returns a boolean, `PolicyIDReference.java:135-146`; `PolicySetIDReference.isReferenceCyclic` throws `IllegalStateException`, `PolicySetIDReference.java:148`). Write "missing references per §7.15 and cyclic references as the engine reports them today", which the shared context classes of P1-1 guarantee.

P3-8. Task 24.1 additions that pin decisions: a policy replaced by a new version inside a reference's range changes the decision after the repository event; two PDPs over two repositories sharing one root object decide differently (pins the per-PDP registry). Both compare full responses, as the D24 differential test does.

**Residual risk.** Shape and race analysis are from source reading; no run. The per-set identity probe is cheaper than the per-child probes it replaces on both corpora, but its own cost is unmeasured until the batch pair; the zero-lookup form stays available if it shows.
