=== BEGIN REPORT R-TERRA-opus ===
FINAL REPORT (policy4j_implementer, Claude Opus 5.5)

Lane cc_1, R-TERRA-opus: a read-only review of Codex Terra's three parts as merged in the main checkout. I read the
patch (`patch-terra-ext-r-aa.diff`, 14 files), Terra's three reports, design.md ("Fix: the extended-Indeterminate
pass" `:1491-1499`, D36 `:1482-1489`), and tasks groups 38 and 39 (`tasks.md:262-270`). The XACML text was fetched
from the official errata01 specification (see Verification).

## Verdicts

| Part | Verdict |
| --- | --- |
| 1. The extended-Indeterminate fix | **REJECT** (P1-1; the rest of the fix is right) |
| 2. The D24/D25 fixes and R-UX P2-1 | **APPROVE** (two optional P3s) |
| 3. D36, staging only for subtrees that can respond | **APPROVE WITH CHANGES** (P2-1: as written it saves nothing on XML policies) |

**A correction of my own earlier claim.**
- In D34b, finding 1, I wrote that evaluating descendants' advice and obligations inside a pass was work whose pass
  value "maps the same". That is wrong whenever a descendant's matching obligation or advice fails (P1-1 below).
- That claim fed the maintainer's decision "advice and obligations no longer run inside a pass". The D34c sentence
  "so `responsesStaged` never fires there" must change with P1-1.

## Part 1: the extended-Indeterminate fix

### [P1-1] Skipping descendants' advice and obligations in a pass changes extended values and can turn an Indeterminate decision into Permit
- **Where:**
  - `DescendantEvaluationContext.java:130` forwards the pass state. This is right, and it is what activates the
    skips below.
  - `Rule.java:124`: `if(!context.isExtendedIndeterminateEval()){ evaluateAdvicesAndObligations … }`.
  - `BaseCompositeDecisionRule.java:132`: the same guard for child policies and policy sets.
- **The specification** (XACML 3.0 errata01, fetched):
  - §7.14: "If the target of a policy or policy set evaluates to 'Indeterminate', the value of the policy or policy
    set as a whole is determined by the value of the combining algorithm according to Table 7." So the pass computes
    the children's values.
  - §7.18: "When such a rule, policy, or policy set is evaluated, the obligation or advice expression SHALL be
    evaluated …".
  - §7.18: "If any of the attribute assignment expressions in an obligation or advice expression with a matching
    FulfillOn or AppliesTo attribute evaluates to 'Indeterminate', then the whole rule, policy, or policy set SHALL
    be 'Indeterminate'."

  So a failing matching obligation is part of a descendant's value inside the pass too. Returning it is what the
  pass must prevent, and §7.18 already rules that out: no obligations are returned "if their evaluated result is
  'Indeterminate' or 'NotApplicable'".
- **Failure scenario,** traced in the merged source (no run). Root R uses deny-overrides over [CS, S]:
  - **The tree:**
    - CS is a policy set with permit-overrides whose target is Indeterminate (a missing `MustBePresent` attribute).
      Its children are P1 and P2.
    - P1 has a Permit rule with a Permit obligation whose assignment reads a missing `MustBePresent` attribute.
    - P2 has a Deny rule. S has a Permit rule.
  - **Before the fix,** which is also the specified result:
    - P1's rule evaluates the obligation, it fails, and the rule is Indeterminate{P} (`Rule.java:125-132`).
    - permit-overrides(Ind{P}, Deny) gives Ind{DP} (`PermitOverrides.java`, `atLeastOneIndeterminateP &&
      (… || atLeastOneDeny)`), and Table 7 keeps Ind{DP}.
    - deny-overrides(Ind{DP}, Permit) gives **Indeterminate** (`DenyOverrides.java:105-107`).
  - **After the fix:**
    - P1's rule skips the obligation and is Permit.
    - permit-overrides returns Permit at P1, and Table 7 gives Ind{P}.
    - deny-overrides(Ind{P}, Permit) gives **Permit** (`DenyOverrides.java:112-114`).

  The decision fails open: had CS's target matched, its value would have been Ind{DP}, which could be Deny.
- Terra's own test pins the deviation:
  - `ExtendedIndeterminatePassPdpTest.java:100-114`: `aFailingObligationInThePassDoesNotChangeTheExtendedDecisionOrStatus`
    expects `INDETERMINATE_D` at `:110`.
  - The specified value is Ind{DP}: the deny policy is Ind{D} by its failing Deny obligation, the permit policy is
    Permit, and deny-overrides(Ind{D}, Permit) is Ind{DP}.
- **Fix:**
  1. Keep the forwarding at `DescendantEvaluationContext.java:130` and the evaluated-policy guard at `:205`.
  2. Evaluate advice and obligations whatever the pass state: drop the guard at `Rule.java:124`, and at
     `BaseCompositeDecisionRule.java:132` keep it only around `context.addEvaluationResult(this, decision)` (`:143`).
     - Inside a pass the responses go to the pass's staging, which is never published, because a pass's value is
       never Permit or Deny.
     - For an unenclosed root they reach the root context, which an Indeterminate result does not read
       (`DefaultPolicyDecisionPoint.java:212-221`).
  3. To keep the target's status (the one wanted change), a failure inside a pass sets the status only if none is
     set. Today it overwrites, at `BaseDecisionRule.java:176`, `:219`, `:260`; use `setEvaluationStatusIfAbsent`
     when `context.isExtendedIndeterminateEval()`.
  4. Tests:
     - `:110` becomes `INDETERMINATE_DP`, keeping the status assertion.
     - Add the R/CS/S scenario above; it expects Indeterminate.
     - `aPassDoesNotResolveResponseAttributes…` (`:81`) expects the attributes that the pass's obligations read.

### [P2-1] The policy-id leak fix has no effective test
- **Where:** `ExtendedIndeterminatePassPdpTest.java:82` and `:96`, and `:114`.
- **Issue:**
  - These assert `getPolicyIdentifiers().isEmpty()` on Indeterminate results.
  - An Indeterminate or NotApplicable result never carries policy ids (`DefaultPolicyDecisionPoint.java:205-221`;
    ids are added only on the Permit/Deny path at `:229-232`).
  - So the assertions pass with or without the guard at `DescendantEvaluationContext.java:205`.
- **Fix:** put the pass under a root that decides Permit, with `ReturnPolicyIdList`. Use deny-overrides over [a set
  with an Indeterminate target and a permitting child P, a permitting sibling S]. The ids contain S, and not P.

### Other part-1 checks

| Check | Result |
| --- | --- |
| Pass state reaches descendants | **Right**: `DescendantEvaluationContext.java:130` forwards to the parent |
| Evaluated-policy record does not leak | **Right**: `:205` and the guard at `BaseCompositeDecisionRule.java:143`. The test is vacuous (P2-1) |
| Nested Indeterminate target combines its own children (§7.13, §7.14) | **Right.** Tables 5, 6 and 7 make a policy's or policy set's value for an Indeterminate target "the value of the combining algorithm". The nested element takes `BaseCompositeDecisionRule.java:118` (Indeterminate) to `:152` `getExtendedIndeterminate(…)`, which combines its children; nothing shortcuts on the pass state. `ExtendedIndeterminatePassPdpTest.java:88-97` pins Ind{P} |
| `NestedResponseStagingTest` lost expectation (`:100`) | **Not a bug per §7.18.** The old expectation showed the rule's obligation *evaluated* (§7.18: "SHALL be evaluated") and held in the root context of an unenclosed Indeterminate policy, which no result reads (`DefaultPolicyDecisionPoint.java:212-221`). §7.18 forbids *returning* it, and it was never returned. With P1-1 the obligation is evaluated again; assert on the PDP result, not on the root's internal lists |
| Two assertions removed from the reference tests | **Right.** `verify(context, never()).isExtendedIndeterminateEval()` encoded the bug: a descendant must ask. `verifyNoMoreInteractions(context, algorithm)` was an over-specified interaction check, which the pragmatic test rule does not keep. The D19 assertions (`never().addAdvices/addObligations`) remain |

## Part 2: the D24/D25 fixes (Argon's R-R) and R-UX P2-1

| Check | Result |
| --- | --- |
| Locks gone from `RootEvaluationContext` | **Right.** `RESOLUTION_LOCKS`, `ACTIVE_LOOKUPS`, and the lock path in `isMissed` and `snapshot` are removed. One deciding thread per root context holds: D26 workers cannot reach the context, and no engine code calls it from another thread. The two tests that shared one decision context across threads are removed, as directed |
| `PreparedPolicySets` map | **Right.** `ConcurrentMap<PolicySetIdentity, PreparedList>` (`PreparedPolicySets.java:60`); first preparer wins (`PolicySet.java:241`, `putIfAbsent`) |
| Lock-free read path | **Yes.** `ConcurrentHashMap.get` takes no lock |
| Allocation per lookup | **None on a hit.** The key is a final field built once per set (`PolicySet.java:68`, used at `:227`). `PolicySetIdentity` hashes with `System.identityHashCode` and compares with `==` (`PreparedPolicySets.java:63-73`). `generation()` returns a stored `Optional`. A miss allocates the list, the pins, the `PreparedList`, and the map node, once per set and generation |
| `PreparedList` is a Guava `ImmutableList` | **Yes** (`PreparedPolicySets.java:79`), so U2's index loop now covers prepared sets (R-UX P2-1 closed). The 23.4 candidate lists must follow |
| Resolver skips the cache read when its cache is off | **Yes** (`DefaultPolicyReferenceResolver.java:79`, `:103`) |

- **[P3-1] (optional) Guava has this mechanism.** `new MapMaker().weakKeys().makeMap()` is a concurrent map with
  identity keys, so it needs no wrapper record. That matches the maintainer's library-first rule. The record is
  correct and small.
- **[P3-2] Document the single-thread assumption** at `RootEvaluationContext.isMissed` and `snapshot`, now that the
  locks are gone. For example: "called on the deciding thread only".

## Part 3: D36, staging only for subtrees that can respond

### [P2-1] As written, every policy and policy set loaded from XML counts as able to respond, so D36 saves nothing on rsa2008, synthetic, or conformance
- **Where:**
  - `BaseDecisionRule.java:121-124` (`target != null || condition != null || …`);
  - `Policy.java:93-94`, `PolicySet.java:85-86`.
- **Issue:**
  - XACML requires a `<Target>` in every policy and policy set, and the mappers build a `Target` object for every
    element, empty or not (`Xacml30PolicyFromJaxbToObjectModelMapper.java:486-494`,
    `Xacml20PolicyFromJaxbToObjectModelMapper.java:245-250`). So `target != null` holds for every XML policy, and
    `canRespond` is always true.
  - rsa2008 has 6 policies and 18 policy sets; 24 of its 30 targets are empty `<Target/>`.
  - The synthetic corpus gives every policy and every rule a target (`SyntheticCorpus.java:116`, `:126-133`,
    `:141-152`) and has no advice or obligations at all. It is entirely silent, yet every policy stages.
  - So the expected 3.8% rsa2008 and 1.6% first-deny (D36, `design.md:1488`) are zero as implemented.
- **Which extension points can really write responses?** Only code that gets an evaluation context below the staging
  wrapper and calls `addAdvices` or `addObligations`:
  - a function that is not the engine's own, invoked from a rule's or a child's target or condition, or from a
    variable or an assignment expression;
  - a custom `Expression` class;
  - a custom combining algorithm, which receives the staged context;
  - a subclass of `Policy` or `PolicySet` that overrides `combineDecisions` (both classes are public and not final,
    `Policy.java:64`, `PolicySet.java:62`).

  Two things are irrelevant to staging:
  - Attribute resolvers write through `ResolverContext.getEvaluationContext()`, which is the **root** context: the
    handler is called with the root (`RootEvaluationContext.resolve` passes `this`). They bypass staging today
    whatever D36 decides.
  - D26 parallel functions get no evaluation context.
- **Can built-in targets and conditions be classified exactly?** **Yes.**
  1. A composite's *own* target and condition run before its staging exists (`BaseCompositeDecisionRule.java:118`,
     `:123`, against `:127`). Their writes go through the parent's staging. So they count only in the parent's
     aggregate, not for the element's own staging decision.
  2. Walk the target, condition, and variable-definition expressions once at build time (policies are immutable).
     Such an expression cannot write responses when its tree holds only engine classes (`Apply`, `Match`,
     `AttributeDesignator`, `AttributeSelector`, `VariableReference`, `FunctionReference`, `TypeAssertion`, values)
     and every function in it comes from the engine's own provider classes. `FunctionSpecBuilder` can mark specs it
     builds from those classes. Only custom `Expression` classes and other providers' functions stay conservative.
  3. An empty target is silent.

  With this, the synthetic corpus and the silent parts of rsa2008 skip staging. Everything that holds advice,
  obligations, a custom algorithm, or extension code still stages.
- **Fix:** apply items 1 to 3 above, and count subclasses as able to respond (P3-1). Then measure with the batch.

### [P3-1] Subclasses of `Policy` and `PolicySet` use their parent class's flag for their own staging
- **Where:** `Policy.java:93` (with its `canRespond()` override), `PolicySet.java:85`.
- **Issue:**
  - The parent's aggregate already treats a subclass child conservatively (`PolicySet.java:205-209`,
    `getClass() == …`).
  - But a subclass's own staging uses the flag computed in `Policy`'s or `PolicySet`'s constructor.
  - A subclass that overrides `combineDecisions` to write responses would skip staging and leak unfiltered responses.
- **Fix:** `|| getClass() != Policy.class` (respectively `PolicySet.class`) in the flag.

### Other part-3 checks

| Check | Result |
| --- | --- |
| Flag computed at build time | **Yes**, immutable final fields (`Policy.java:93`, `PolicySet.java:85`) |
| §7.18 order and filtering kept | **Yes.** Staging is unchanged where it is created. A skipped subtree has, by the (conservative) rule, nothing that writes. Pinned by `NestedResponseStagingPdpTest` (order across prepared decisions, losing-rule filtering, custom combiner, condition-written responses) |
| Prepared and pinned positions | **Right.** References count as able to respond (`PolicySet.java:205-209`), and a pinned target decides its own staging with its own flag. A later per-generation refinement from pinned targets is possible |
| `canRespond() && isEnclosed(…)` | **Right**: the ancestry walk is skipped too (`BaseCompositeDecisionRule.java:127`, `:166`) |
| Interaction with P1-1 | Restoring evaluation of advice and obligations in passes needs no change to D36, which already counts advice and obligation expressions |

## Verification

- **The specification:** XACML 3.0 errata01 core
  (`https://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html`), fetched
  2026-10-27.
  - Quoted: §7.10 (Extended Indeterminate), §7.12 and Table 5, §7.13 and Table 6, §7.14 and Table 7, and §7.18
    (all sentences).
  - The fetched page text ended inside Appendix A.3.9, so Annex C could not be quoted. The combining behavior in the
    scenario is taken from the engine's `DenyOverrides.java:73-116` and `PermitOverrides.java:53-99`, which the
    conformance suite pins.
- **Read in the main checkout:** `DescendantEvaluationContext`, `Rule` (`:118-146`), `BaseCompositeDecisionRule`,
  `BaseDecisionRule`, `Policy`, `PolicySet`, `PreparedPolicySets`, `RootEvaluationContext`,
  `DefaultPolicyReferenceResolver`, `DefaultPolicyDecisionPoint` (`:198-234`), `FirstApplicable`,
  `DecisionCombiningAlgorithms`, the two XML mappers' target creation, `SyntheticCorpus` (`:107-153`), and Terra's
  tests.
- **rsa2008 element counts:** `rtk rg` over the vendored `rsa2008-interop` policies: 12 rules, 30 targets (24 empty),
  7 conditions, 8 obligation elements, 6 policies, 18 policy sets.
- **No build or test was run** (read-only review). The P1-1 scenario is traced in the source, not executed. It
  would be the first test of the fix.

## Residual risk

- P1-1 is a rare configuration: an Indeterminate target above a descendant whose matching obligation fails. But it
  fails open. Until it is fixed, a decision can be Permit where the specification gives Indeterminate.
- The D36 savings are zero until P2-1 is fixed. The batch measurement should not credit D36 before then.
=== END REPORT R-TERRA-opus ===
