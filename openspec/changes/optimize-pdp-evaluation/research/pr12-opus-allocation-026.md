# PR12: allocation analysis of profile 026 (Claude Opus 5.5, 2026-10-27)

=
FINAL REPORT (policy4j_implementer, Claude Opus 5.5): lane cc_1, PA-opus, allocation analysis of profile 026 (read-only)

## Summary

- **Method.** I attributed every own-code allocation site in the three allocation profiles of run 026. Each site is the
  deepest `org/xacml4j` frame of the allocating stack, with its own-code callers.
- **The largest remaining costs:**
  - **Bag construction.** About 34% of rsa2008 and 30% of first-deny. Every designator bag is grouped through a new
    `LinkedHashMap`, and every value is hashed through `Objects.hash` varargs.
  - **Per-decision maps.** First-deny's 12.4% of `ConcurrentHashMap` plus its nodes, tables, and views make about 25%.
    Some of these maps are created only to be read empty.
  - **Request iteration through Guava multimap iterators.** These iterators are most of `Iterators$9`.
  - **Copies on the function-call path.**
- **Proposal.** Fourteen items in four new cycles: U (exact hashing and iteration), X (small bags without a map),
  Y (per-decision state created only when read), and Z (function calls). Two more items need maintainer decisions.
  - Expected removal: about 49% of rsa2008 and about 54% of first-deny allocation samples (shares of profile 026;
    overlapping, so to be confirmed by the batch JMH pair, D12).
  - synthetic:1000 all is 94.8% the per-child policy and reference contexts that D24/D25 remove. These cycles take only
    about 1-2% of it now. After R and S, its residue looks like first-deny.
- **Not re-proposed:** D24/D25 (contexts of skipped and pinned children) and D29/T (`top` once per context, scan
  parameters once per binding).
- **Cycle letters:** R, S, T, V, and W are taken (`design.md:555,649,676,688`). U, X, Y, and Z are free.

Shares are async-profiler allocation samples (TLAB-sampled, roughly bytes). Sets: rsa2008 `all` (35,294 samples),
synthetic:1000 `first-deny` (56,244), and synthetic:1000 `all` (12,192). Line numbers are from the main checkout
today (the profiled jar sha256 is `6664805f…`). Paths are relative to `xacml-core/src/main/java/org/xacml4j/v30/`
unless they start with `pdp/`, which is `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/`.

## The candidates the packet named

- **rsa2008 `Object[]` 15.4%.** This is not mainly D23 or `Apply`.
  - `Value.OtherValue.hashCode` uses `Objects.hash` varargs: 6.32%. It is called from `BagStorages$Builder.add`
    (3.14%) and `BagStorages$Grouped.<init>` (3.18%).
  - The any-of runtime validation lists grow their arrays: 3.72%.
  - The `ArrayList` in `FunctionSpecImpl.normalize`: 1.32%.
  - **The D23 spread parameters (`DefaultFunctionInvocation.parameters`): 1.16%.**
  - The `ArrayList` in `FunctionSpecImpl.evaluate`: 0.92%.
  - The `StagedResponses.staged` list: 0.91%.
  - Result and obligation lists: about 0.5%.
  - `Apply`'s array is the `Expression[]` (below), not an `Object[]`.
- **`Iterators$9` (9.2% rsa, 9.4% first-deny)** is Guava's `Iterators.singletonIterator`. It is the iterator of a
  one-element `ImmutableList` or `ImmutableSet`, made by for-each loops and by `ImmutableMultimap.values()`, which
  makes one inner iterator per key. Sources (rsa / first-deny):
  - `AttributeIndex.find(Predicate)`, called per decision through `Category.getIncludeInResultAttributes`:
    2.37 / 3.78.
  - `BagOfValues$Builder.attributes`, iterating an attribute's one value: 2.11 / –.
  - `RequestContext.containsAttributeValues`, from the XPath multiple-content-selector check: 2.04 / 3.69.
  - `LegacyDenyOverridesPolicyCombineAlgorithm.doCombine`, over one-child lists: 1.41 / –.
  - `RequestContext.getIncludeInResultAttributes`: 0.96 / 1.92.
  - `BaseDecisionRule.evaluateObligations`: 0.20 / –.
- **`HashMap$Node[]` (5.4 / 6.1), `LinkedHashMap$Entry` (5.4 / 3.2), `LinkedHashMap` (4.6 / 4.8), and
  `LinkedEntrySet` (1.6 / 1.8)** are almost all from the bag grouping map of `BagStorages$Builder`. The map is created
  per builder (`BagStorages.java:50`) and filled by `merge` per value (`:59`); `Grouped` reads its entry set
  (`:94,122`). The rest (about 0.9% rsa) is from `Result$Builder.addObligation`.
- **`ConcurrentHashMap` and its nodes (rsa 3.3 + 4.7 + 2.2; first-deny 12.4 + 5.1 + 6.2 + 1.3 views).** Yes, these
  are the per-decision maps of the root context and the handler (first-deny):
  - `RootEvaluationContext.evaluatedPolicies`: map 2.41, nodes 2.62, table 3.05.
  - The handler's eager `requestDesignators` (D17): 2.47, 2.45, 3.17.
  - `denyObligationsMap` and `denyAdvicesMap`, created by the result's reads: 2.51 + 2.50, plus views 1.27.
  - The handler's `designatorCache`, created by `getResolvedDesignators()` only to test `isEmpty()`: 2.53.
- **`Expression[]` 4.2% (rsa).**
  - `Apply.evaluate`, `invocationArguments.toArray(...)` (`Apply.java:264`): 2.59.
  - The varargs array of `DefaultFunctionInvocation.parameters` (`:362-366`): 0.95.
  - `HigherOrderFunctions.anyOf`, `arguments.clone()` (`:71`): 0.67.
- **`StagedResponses` 3.8% (rsa) and 1.6% (first-deny).** One per matching composite rule inside a policy set
  (`BaseCompositeDecisionRule.java:127`): `PolicySet.evaluate` 2.94% and `Policy.evaluate` 0.82% in rsa.
- **First-deny's fixed cost per decision:**
  - The maps above: about 25%.
  - Request iteration: 12.2%.
  - Logging strings: 8.3%.
  - Result and response objects and builders: about 5%.
  - Structural per-decision objects: about 9%, plus about 5% of policy, reference, and rule contexts.

## Ranked proposals (gain per risk)

Shares are rsa2008 / first-deny / synthetic:1000 all.

| Rank | Cycle | Item | Site | Removes (approx.) | Risk |
| --- | --- | --- | --- | --- | --- |
| 1 | U | U1: hash fold in `Value.OtherValue.hashCode` | `types/Value.java:128-130` | 6.4 / 3.7 / 0.2 | none (same values) |
| 2 | Y | Y1: reads of matching obligations and advice create no map | `policy/RootEvaluationContext.java:435-442` | 1.3 / 6.3 / – | none |
| 3 | X | X1: bags of up to 8 values grouped without a hash map | `BagStorages.java:48-82,84-129` | 15.5 / 15.8 / 0.6 | low |
| 4 | U | U2: combining loops by index | `combine/impl/DenyOverrides.java:73`, `Legacy*Combine*:57,61,58,67` | 2.8 / 2.5 / – | none |
| 5 | U | U3: index loops in `BagOfValues.Builder.attributes` | `BagOfValues.java:411-417` | 3.0 / – / – | none |
| 6 | Y | Y4: include-in-result attributes recorded when the index is built | `AttributeIndex.java:339-372`, `Category.java:105-107` | 4.1 / 3.8 / – | low (additive accessor) |
| 7 | Y | Y5: the request keeps its categories as a list | `RequestContext.java:285-295,341-356` | 3.7 / 8.4 / 0.2 | low (benchmark overstates) |
| 8 | Y | Y2: evaluated policies recorded only when asked | `RootEvaluationContext.java:253-256`, `pdp/DefaultPolicyDecisionPoint.java:162-171,229-232` | 4.2 / 8.1 / – | low, decision |
| 9 | X | X2: the first value of a bag on demand | `BagOfValues.java:72,84,164-170,335` | 1.0 / 1.3 / – | none |
| 10 | Z | Z2: higher-order validation without lists | `function/impl/HigherOrderFunctions.java:227-281` | 3–4 / – / – | low |
| 11 | Z | Z1: normalized arguments evaluated in place | `function/FunctionSpecBuilder.java:510-512` | 1.5 / – / – | low |
| 12 | Y | Y7: lazy builders in `Result.Builder` | `Result.java:265-267` | 1.8 / – / – | none |
| 13 | Y | Y6: `ResponseContext.Builder` without an eager list builder | `ResponseContext.java:72-90` | – / 2.0 / – | none |
| 14 | Y | Y3: resolved designators tested without creating the cache | `pdp/DefaultEvaluationContextHandler.java:159-161`, `pdp/DefaultPolicyDecisionPoint.java:243-247` | 0.6 / 2.5 / – | low, decision |
| – | (after R, S) | `StagedResponses` only for subtrees that can respond | `BaseCompositeDecisionRule.java:127,166` | ≤3.8 / 1.6 / – | medium |
| – | decision | Logging: correlation id and MDC strings | `pdp/PolicyDecisionPointContextFactory.java:108-109`, `pdp/MDCSupport.java:57,65` | ≤2.1 / ≤8.3 / – | decision |

## The cycles

### Cycle U: hashing and iteration without temporary objects (exact, mechanical)

- **U1.**
  - Replace `Objects.hash(value, getEvaluatesTo())` with
    `31 * (31 + Objects.hashCode(value)) + Objects.hashCode(getEvaluatesTo())`. The value is the same; this is D22's
    fold.
  - Every bag hashes every value twice: in `HashMap.merge` and in the multiset hash of `Grouped`.
  - The same fold applies to the other uncached value hashes: `RFC822Name:267`, `ISO8601Duration:127`, `DNSName:165`,
    `IPAddress:206`, `PortRange:247`, `Path:166`. They are not visible in this profile, and I claim no gain for them.
  - Must stay: hash values (used by the multiset hash and by maps) and equality.
- **U2.**
  - Change `for (D d : decisions)` to `for (int i = 0; i < decisions.size(); i++)` in the combining algorithms. The
    lists are the immutable child lists of a policy or policy set.
  - Must stay: Annex C order and every early exit, which are unchanged.
  - A caller of the public static `doCombine` that passes a non-random-access list still works, but in O(n²). Guard
    with `RandomAccess` if that matters.
- **U3.**
  - `BagOfValues.Builder.attributes` iterates the same `Iterable` twice: one pass checks types, one adds. Keep both
    passes, as index loops when the argument is a random-access `List`. The two passes keep the all-or-nothing type
    check.
  - Must stay: atomicity and order.

### Cycle X: small bags without a hash map

- **X1. `BagStorages.Builder` collects values in an array.**
  - At `build()`, with at most `LINEAR_LOOKUP_LIMIT` (8) values, it groups by `equals` with a linear scan: first
    occurrence order, a count per distinct value. Above 8 values, it groups through the `LinkedHashMap` as today.
  - `Grouped`, `Values`, `Integers`, `Doubles`, and `Booleans` take the distinct values and counts instead of
    `Map<Value, Integer>`. They are private classes, so there is no API change.
  - Must stay, all identical:
    - The grouped iteration order (distinct values in first-occurrence order, each repeated by its count).
    - The multiset hash (Σ hash ^ count).
    - Equality, and the serialized form (`Multiset`).
    - Overflow checks: counts ≤ size.
  - The equality-based scan groups exactly as the map does for value types whose `equals` and `hashCode` are
    consistent. `DoubleVal` inherits `OtherValue`'s `Double.equals` and `Double.hashCode` (0.0 ≠ −0.0, NaN = NaN). The
    rsa and synthetic bags hold one to three values.
  - Note for the maintainer rule: this is a grouping loop inside the existing storage, not a cache, pool, or lock.
  - After X, a one-value bag still makes six objects: two builders, `Values`, `int[]`, `Value[]`, `BagOfValues`
    (about 10% of rsa2008 and 9% of first-deny). A single-value storage could remove `int[]` and a builder later.
- **X2.**
  - `BagOfValues` builds `Optional.of(first)` for every bag (`:84`), but only `single()` reads it. Its callers are
    `pdp/DefaultEvaluationContextHandler.java:285` (a context-selector entity) and the static `value(BagOfValues)`.
  - Compute it in `single()` and drop the field. It is transient and not in `serialPersistentFields`, so the
    serialized form does not change.

### Cycle Y: per-decision state created only when it is read

- **Y1.**
  - `getMatchingObligations` and `getMatchingAdvices` call `…Map()`, which creates an empty `ConcurrentHashMap` to
    return `.values()`.
  - Read the field instead: `m == null ? ImmutableList.of() : m.values()` (or use `orEmpty(...)`, already in the
    class).
  - Must stay: §7.18 content and order, which are unchanged. The result only iterates the collection.
- **Y2.**
  - `addEvaluationResult` puts every applicable policy into a `ConcurrentHashMap`.
  - The map is read only by the result when `ReturnPolicyIdList` is true (`DefaultPolicyDecisionPoint.java:229-232`),
    and by the debug line's count (`:162-165`).
  - Fix: the root context takes the request's `isReturnPolicyIdList()` from
    `PolicyDecisionPointContextFactory$1.createEvaluationContext` (or records when debug logging is on), and records
    only then. The debug line prints the count only when it was recorded.
  - Must stay: the `PolicyIdentifierList` when it is requested, in the same sorted order (`:405-422`).
  - Decision: what `EvaluationContext.getEvaluatedPolicies()` returns when nothing is recorded. It is public API, and
    its only callers in the repository are the two above.
- **Y3.**
  - `getResolvedAttributes` creates the designator cache through `getResolvedDesignators()` (rv008 keeps that view
    live) only to test `isEmpty()`.
  - Fix: a non-creating test, `EvaluationContext.hasResolvedDesignators()`, as a default method that the handler
    answers from `designatorCache != null && !designatorCache.isEmpty()`.
  - Must stay: rv008 for callers that keep the map.
  - Decision: it adds a default method to the public interface.
- **Y4.**
  - `RequestContext.getIncludeInResultAttributes` (per decision) scans every attribute of every category through
    `Entity.find(Predicate)`, which uses multimap iterators and builds a new `ArrayList`.
  - Fix: `AttributeIndex.Builder` already visits every attribute. The index records its include-in-result
    attributes, or just whether it has any. The request loop skips categories without them, through a new
    `Entity.hasIncludeInResultAttributes()` (an additive public method).
  - `Category.getIncludeInResultAttributes()` keeps returning a fresh `ArrayList`, so its mutability does not change.
  - Must stay: which categories and attributes the result includes, and their order.
- **Y5.**
  - `containsAttributeValues` and `getIncludeInResultAttributes` iterate `entities.values()` of an
    `ImmutableListMultimap`. That makes an outer iterator, a list iterator, and one inner iterator per category.
  - Fix: the request keeps `ImmutableList<Category>` built at construction, and both methods loop by index.
  - The profile overstates the gain, because the benchmark reuses request objects and the list is built at
    construction. In production, one list per request replaces about three iterations of about six iterators.
- **Y6.**
  - `ResponseContext.Builder` creates `ImmutableList.builder()` eagerly (`Object[4]`, 1.25%) and copies the handler's
    singleton set through an iterator (`Collections$1`, 0.96%).
  - Fix: no builder until the first add; a single `results(Collection)` on a fresh builder becomes
    `ImmutableList.copyOf(collection)`.
  - Must stay: the result order.
- **Y7.**
  - `Result.Builder` creates two `ImmutableMap.Builder`s and one `ImmutableList.Builder` per result.
  - Fix: create them on first add, as D22 does for obligations and advice; `build()` uses `ImmutableMap.of()` and
    `ImmutableList.of()` when they are absent.
  - Must stay: D22 snapshots and the serialized form.

### Cycle Z: function calls with fewer copies

- **Z1.**
  - `invokeNormalized` evaluates into a new list unless the call is a pair
    (`(pair && !validate) ? evaluateInPlace : evaluate`).
  - The normalized list is always a new `ArrayList` owned by the call: from `normalize` (`:615`) or `pairList`
    (`:428`). So evaluate in place whenever no validator saw the list (`!validate`), and keep the copy for validated
    calls (rv009).
  - Must stay: evaluation order, the lazy functions (`evaluateParameters`), and the mapping of failures.
- **Z2.**
  - `requireValidArguments` and `validateArguments` run on every any-of, all-of, and map call. Each call copies
    `ref` and the arguments into a growing `ArrayList`, then builds a second list of `PrimitiveTypeExpression`s and
    a list iterator.
  - The check was added on purpose, for data types known only at evaluation (`339658a54`, policy4j-the.4.61), so keep
    it.
  - Fix: validate over the argument array. One exactly sized `Expression[]` of the primitive view, the same wrappers,
    and one `Arrays.asList(view).listIterator()`. Check the ref directly. The build-time validator (`:310-362`) adapts
    once.
  - Must stay: the same result for every input, and the syntax-error status and message.

## Needs a decision, or waits for another cycle

- **`StagedResponses`.**
  - It can be elided when no rule or policy below the composite can produce obligations or advice. That is a flag
    computed when the policy is built, conservatively true for references.
  - It touches the evaluation path that R and S (D24/D25) are changing, and D19. So rank it after R and S.
- **Logging.**
  - Per decide call: the correlation id `prefix + "-" + toHexString(seq)` and the MDC `PDP_ID` put. Per decision: the
    MDC request id `correlationId + "-" + number`.
  - Logback's MDC makes a `HashMap` node on every put after a remove.
  - Ways to reduce it, for example putting the request number under its own key, or keeping the key between the
    decisions of one call, change what log lines show. So this is the maintainer's call.
- **Not proposed:**
  - The `requestDesignators` map of D17. It exists for §7.3.5, which requires one bag per attribute per decision. On
    first-deny it is 8.1% (map, nodes, table). Only a cheaper map type, or dropping the request-bag cache after X,
    could reduce it, and both contradict or reopen D17.
  - The structural per-decision objects (about 9% of first-deny): `RootEvaluationContext`, the handler, the factory
    context and its lambda, `NumberedDecisions`, `Budget`, `AtomicReference`, and the handler chain's singleton set.
    Removing them means merging objects.
  - The `AtomicReference` of the TTL (0.59% of first-deny).
  - `Rule` contexts (0.9% of rsa2008, 1.9% of first-deny).

## Verification and evidence

- **Profile:** `openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/026-profile-decide-batch3/alloc-*/`
  (the `top-*.md` files and `collapsed-alloc.csv.gz`), read-only.
- **Scratch scripts** (not evidence; standard library only, run with `python3 -I`):
  - `scratchpad/pa/alloc_sites.py`
    → `sites.md` in the same directory (sites per type and per frame).
  - `callers.py` → `callers.md` in the same directory (own-code callers of chosen frames).
- **Source:** every site was opened in the main checkout. The callers of `getEvaluatedPolicies`, `single()`,
  `requireValidArguments`, and the `ServiceLoader` uses were found with `rg` (text lane; Serena not used, so they are
  PARTIAL).
- No code was changed, and no build or JVM was run.

## Residual risk

1. The shares overlap and shift when sites go away, so the totals are estimates. Measure the batch with one JMH pair
   (D12).
2. Y5, and part of Y4, look larger in the benchmark than in production, because the fixture reuses request objects.
3. X1 is the largest item and changes the core bag storage. It needs the existing bag, equality, serialization, and
   conformance tests.
4. Y2 and Y3 change what a public method returns or add one. The maintainer decides.
5. Line numbers are from the main checkout today. Files that changed after the profiled jar was built could shift
   them slightly.
