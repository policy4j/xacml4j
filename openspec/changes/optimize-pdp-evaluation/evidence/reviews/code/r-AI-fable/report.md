=== BEGIN REPORT R-AI-fable ===

FINAL REPORT (policy4j_implementer_claude_fable, Claude Fable 5.1)

Read-only review of Codex Terra's cycle AI (D41, target before context). Inputs: `report-I-AI-terra.md`,
`patch-AI.diff` (797 lines, 21 files), and the merged sources of the main checkout
(`TargetBeforeContext.java` 197 lines, identical to the patch). Line numbers below are the main
checkout's. No file was edited except this report; no build was run (review lane). Serena was
UNAVAILABLE (connection timeout); inventories are `rtk rg` plus the opened source.

## Verdict

APPROVE WITH CHANGES. No P1. The precheck is a sound under-approximation of the XACML 3.0 section
7.7 target semantics for the default handler: it declares "cannot match" only through the same
NOMATCH chain, the same `scan` fast path, and the same bag instance that the normal path would use,
and it falls back to the normal path on every uncertain input. The one P2 is the time cost: a child
whose target matches now has its target evaluated twice, which explains Terra's rsa2008 smoke
(bytes -2.3%, time +28.5%); the D12 pair decides, and the fix is to reuse the precheck's MATCH.
Seven P3s: two contracts for one property, two public helpers, a redundant loop, a class-init hack,
a dead callback branch, an exception guard that can fail a decision, and a vacuous test.

## Checklist

1. Annex C order and short-circuits. `TargetBeforeContext.evaluateIfMatch` (:68-73) is a drop-in
   for `d.evaluate(d.createContext(context))`: the loops, the return points, and the accumulators of
   the eleven bases are untouched (DenyOverrides.java:80, PermitOverrides.java:58,
   FirstApplicable.java:57, DenyUnlessPermit.java:57 and :67 (stream form), PermitUnlessDeny.java:55,
   OnPermitApplySecondPolicyCombiningAlgorithm.java:71,78,81, LegacyDenyOverridesPolicy:62,
   LegacyDenyOverridesRule:74, LegacyPermitOverridesPolicy:66, LegacyPermitOverridesRule:63). The 19
   other concrete algorithms extend these bases (`rtk rg "extends"` over `combine/impl`).
   OnlyOneApplicable (:58) skips a child only when `isDefinitelyNoMatch` holds; that child would have
   answered NOMATCH at :60 and reached neither the second-MATCH nor the INDETERMINATE branch (:61-70),
   so the "exactly one" rule and its order are kept. `DecisionCombiningAlgorithms.evaluateIfMatch`
   (combine/DecisionCombiningAlgorithms.java:66-69) is unchanged, so custom algorithms keep the old
   path. Call counts for a rejected child: `evaluate` once (BaseDecisionRule.java:160-163), `isMatch`
   once inside it (:161 to :191-193), no `createContext`, no `evaluateRule`; the normal path also
   calls `isMatch` once per NOMATCH child (BaseCompositeDecisionRule.java:119, Rule.java:113). D34:
   the precheck is off while the decision is observed (TargetBeforeContext.java:157), and
   `DecisionObservation.isEnabled()` (DecisionObservation.java:112) gates every callback, so no
   sequence changes; after an observer failure `isEnabled()` is false and callbacks are suppressed
   anyway, so the precheck switching on then is harmless.

2. Context-free proof set (`prepare`/`safeValues`, :87-143; evaluation :167-191). Accepted:
   exactly `Target`, `MatchAnyOf`, `MatchAllOf`, `Match` (no subclasses), non-empty at each level;
   reference exactly `AttributeDesignator`; predicate of the built-in spec class
   (`FunctionSpecBuilder.FunctionSpecImpl`, any annotated function including custom providers, see
   P3-4); designator type one of 16 built-in `XacmlTypes` with a literal of that type's own value
   class. Excluded: any `AttributeSelector` (whole plan empty), custom structure classes, custom
   `FunctionSpec` implementations and literals, ENTITY and XPATH; variables and ContextSelectorId
   cannot occur in a designator `Match`. At decision time it also needs the handler capability, no
   observation, no runtime parameter validation (:156-157), a present entity (:177), a present bag
   (:179; an entity bag is never empty, AttributeIndex.java:297-298), every bag value of the
   literal's exact class (:182), and `scan` not NONE (:185). The "cannot match" verdict is the same
   chain as Target.match (Target.java:68-73), MatchAnyOf.match (:56-66) and MatchAllOf.match (:63-75,
   break at the first NOMATCH; an INDETERMINATE before it makes the precheck bail), through the same
   `predicate.scan` that Match.match tries first (Match.java:113-121, FunctionSpecBuilder.java:382-385,
   DefaultFunctionInvocation.java:159-173, IEEE doubles at :163-165), on the same bag: both paths
   read `request.getEntity(category)` then `entity.resolve(key)` (DefaultEvaluationContextHandler
   .java:537-539 and :312-320 via RootEvaluationContext.java:389-391), and the entity keeps the first
   bag per id, type and issuer (AttributeIndex.java:288-300), so section 7.3.5 holds. The information
   point memo that the handler checks first (:275-280) can hold a key only when the entity lacked it
   (:312-320), so it never disagrees with the precheck.

3. Statuses written by failing targets. A target the precheck rejects writes no status in the normal
   path either: every visited Match resolved a present bag of the literal's class, so the designator
   (AttributeDesignator.java:91-99) and the scan cannot throw. The one case where a NOMATCH target
   still writes a status, an INDETERMINATE Match before the first NOMATCH of an AllOf
   (MatchAllOf.java:65-69, reaching the root through DescendantEvaluationContext.java:105-116), is
   exactly a case the precheck bails on (:177, :179, :182, :185). Not pinned by a test (P3-7c).

4. Section 7.18 staging, D36, extended Indeterminate. A prechecked child returns NOT_APPLICABLE
   before `evaluateRule`, so nothing is staged, no applicable decision is recorded
   (BaseCompositeDecisionRule.java:131-146) and no policy id is added. In an extended Indeterminate
   pass the same algorithms combine the same children over the same request bags, so the same children
   are rejected; the pass flag need not be consulted. D36's `targetOrConditionCanWrite` cannot fire for
   a rejected target because Match.match would take the same scan path and never `invoke`.

5. References. Only pins are eligible (PinnedReference.java:44-49); a raw reference fails the class
   test (TargetBeforeContext.java:146-149). `PolicyIDReference.canCheckTarget` (:96-99) bails where
   `createContext(context, target)` (:89-94) would re-enter or where the reference context's
   constructor (:174-178) would throw (current policy set, cycle); `PolicySetIDReference
   .canCheckTarget` (:100-108) bails where `createContext` (:93-98) re-enters, where
   `PolicySetDescendantEvaluationContext` (:339-345) would throw, or where the cycle walk throws
   (:183-191, caught). A pin consults `isMissed` (:45) as `createContext`, `evaluate` and `isMatch` do
   (:59, :67, :84), and the decision's misses cannot change between precheck and evaluation: the
   snapshot runs only in `resolve(ref)` (RootEvaluationContext.java:480-484), which pins never call.

6. Index paths. `Policy.Prepared.children` and `PolicySet.Prepared.children` are untouched
   (Policy.java:196-215, PolicySet.java:197-200, :225-262); the precheck filters the candidates the
   index returns (iterator branch, candidates are not an `ImmutableList`), and both honor
   `isValidateFuncParamsAtRuntime`.

7. The new public contract. `usesRequestEntityForDesignators` (EvaluationContextHandler.java:69-71)
   and `hasContextFreeRequestDesignators` (:82-84) are both default false and both true only in the
   default handler (DefaultEvaluationContextHandler.java:220-227). Resolving a present request
   designator has, in tree, only two effects beyond the bag: the D34 callbacks (handled by disabling
   the precheck) and the per-decision request memo (no decision effect); nothing in `xacml-pdp` main
   sets the decision TTL, which is read once at DefaultPolicyDecisionPoint.java:265. The index already
   omits resolution for unselected children (it combines the candidates only, Policy.java:198,
   PolicySet.java:199), so "entity lookups may be omitted" is already assumed by the first contract;
   the second adds only the TTL clause and a second switch. Merge them (P3-1).

8. Precheck state. RootEvaluationContext.java:90-92 and :506-533: a plain field plus a volatile owner
   thread, written and read on the deciding thread only; the window is `rule.evaluate(context)`,
   which returns at BaseDecisionRule.java:160-163 without evaluating children or user code (a pin's
   `evaluate`, PinnedReference.java:66-69, goes straight to `target.evaluate` when unobserved).
   `checkState(precheckedThread == null)` (:513, :525) turns a concurrent use of the public helpers
   on one root into an IllegalStateException (P3-6).

9. rsa2008 time. See P2-1.

## Findings

P2-1 (performance; TargetBeforeContext.java:167-191 with BaseDecisionRule.java:190-201). A child
whose target matches now evaluates its target twice: the precheck (per Match: `resolveEntity`,
`entity.resolve` with a `Lookup` allocation and a map get, AttributeIndex.java:288-292, the class
loop, the scan) and then the normal path (`createContext`, `isMatch`, Match.match, `context.resolve`
through the root and the handler's two memo lookups, the scan again). Per child it also walks the
context chain for `getCurrentPolicy`, `getCurrentPolicySet` or `getCurrentRule`
(DescendantEvaluationContext.java:206-246) and, for a pin, `getCurrentPolicyIDReference` plus the
recursive cycle walk (PolicyIDReference.java:150-160, quadratic in depth), which the reference
context's constructor repeats. In rsa2008 every one of the 36 target matches is a string-equal on a
designator and no policy uses a selector (`rtk rg -c AttributeSelector` over
`rsa2008-interop/*Policy*.xml`: no match), so every target is eligible, and the upper targets of its
deny-overrides and first-applicable trees match for most requests: the double work dominates and the
rejections save bytes, not time. Terra's one-fork smoke agrees: rsa2008 4,149.273 to 4,053.977 B/op
(-2.3%) but 3.479 to 4.471 us/op (+28.5%); synthetic:100, one matching child of 100, 5,740 to 2,670
B/op and 5.279 to 5.232 us/op. One fork is indicative; the D12 pair decides, and if it confirms, the
cycle should not ship as is. Fix: reuse the precheck's work for the MATCH case too. When every AnyOf
matched cleanly, record (child, MATCH) in the same root slot and let `BaseDecisionRule.isMatch`
consume it once (clear on read) instead of re-evaluating the target; the normal path then creates the
context and runs `evaluateRule` with `isMatch` answered. This is safe: a clean MATCH from the
precheck means no visited Match could have written a status (an INDETERMINATE AllOf makes the
precheck bail), the bag is the entity's first bag either way, and observation is off. With that, a
matching child costs one target evaluation plus its context, as before, and a rejected child costs
one evaluation and no context. Remove the duplicate class loop (P3-3) in the same change, and
measure the pair before and after.

P3-1 (API; EvaluationContextHandler.java:82-84, RootEvaluationContext.java:502-504,
DefaultEvaluationContextHandler.java:225-227). Two switches for one property (checklist 7). Fold the
TTL clause into the javadoc of `usesRequestEntityForDesignators`, delete
`hasContextFreeRequestDesignators` and the root's delegate, and let the precheck read
`hasRequestAttributeResolution()`. Keep Terra's `requestResolutionEffectsKeepTheDecisionCacheTtl`
as the pin of the merged contract with the flag false.

P3-2 (API surface; TargetBeforeContext.java:59, :68, :76; DecisionCombiningAlgorithms.java:66-69).
Two public `evaluateIfMatch` helpers with different behavior, and the eleven imports flipped to the
new one. Make the old helper delegate to the precheck (one line), revert the ten import changes, and
call the helper at the two direct-call sites (DenyOverrides.java:80, LegacyDenyOverridesRule
CombineAlgorithm.java:74). Custom algorithms then get the same path, and `TargetBeforeContext`
needs only the one caller.

P3-3 (hot path; TargetBeforeContext.java:181-183). The class loop over the bag duplicates the check
inside the scan (BagStorage.java:65-67; DefaultFunctionInvocation.java:161, :164-170 return NONE on
a mismatch). Remove it; NONE already makes the precheck bail.

P3-4 (class init; TargetBeforeContext.java:61-63). `BUILT_IN_SPEC` builds a whole
`XacmlDefaultFunctions` by reflection (FunctionProviderBuilder.java:98-106 creates a new provider)
at the first `BaseDecisionRule` construction, to read one class. Replace it with a package-visible
capability, for example `default boolean hasDirectScan()` on `FunctionSpec` overridden by
`FunctionSpecImpl` (FunctionSpecBuilder.java:305, :371, :382-385); the plan can then also leave out
predicates whose scan can only return NONE (no direct comparison, such as string-regexp-match)
instead of discovering that per decision.

P3-5 (dead code and javadoc; BaseDecisionRule.java:191-194, EvaluationContextHandler.java:78). The
`targetMatched` call in the prechecked branch can only run against a disabled observation
(TargetBeforeContext.java:157), and "Callers must still preserve observation callbacks" is met by
not prechecking observed decisions. Drop the call, or keep it with a comment that states the rule.

P3-6 (robustness; RootEvaluationContext.java:513, :525; TargetBeforeContext.java:193). The
`checkState` guard makes a concurrent or nested use of the public helpers on one root fail the
decision; degrade instead (`if(precheckedThread != null) return rule.evaluate(rule.createContext
(context));`), or document the single-thread contract on the helper. `catch(RuntimeException |
Error)` swallows OutOfMemoryError and StackOverflowError; narrow it to RuntimeException.

P3-7 (tests). (a) `aHandlerWithoutThePrecheckCapabilityKeepsItsNormalResolution` is vacuous: the
AssertionError from `doThrow` is swallowed at TargetBeforeContext.java:193, the mock's `getEntity`
answers empty so the precheck bails at :177 regardless, and the decision is PERMIT with or without
the precheck. Replace it with an outcome test: capability false, entity holding a value that does not
match, `resolve` answering a bag that matches: PERMIT only when the precheck is off; and its mirror
with the capability true. (b) No PDP test compares precheck on and off on one tree: the `decide`
modes toggle only the index (TargetIndexPdpTest.java:575-596). An observed decision switches the
precheck off, so deciding the existing parameterized trees with and without an observer and
asserting equal results pins both paths cheaply. (c) Pin checklist 3: an AllOf with a missing
must-be-present designator before a non-matching Match, decided with the precheck on and off, same
decision and status. (d) `aNonMatchingChildKeepsItsObservationSequence` records an observed, hence
unprechecked, decision; it documents the rule of P3-5 rather than the precheck.

## Residual risk and what was not verified

- No JMH pair was run in this lane; the time analysis is structural plus Terra's one-fork smoke
  (PARTIAL). The pair for rsa2008 and synthetic:100, before and after the P2-1 reuse, decides.
- The equivalence argument rests on the default handler (`request.getEntity` plus the entity's kept
  bag). A foreign handler opts in only by returning true from the capability; its own resolution
  effects are its responsibility, which is why one documented contract (P3-1) matters.
- Serena UNAVAILABLE; the subclass inventory of the combining algorithms and the callers of the two
  helpers come from `rtk rg` and the opened files.

=== END REPORT R-AI-fable ===
