### Verdict: **APPROVE WITH CHANGES**

Codex Terra's implementation of the opt-in restricted target index (tasks 23.4 and 23.5, [`report-I-IDX-terra.md`](file://<scratchpad>/report-I-IDX-terra.md) and [`report-PORT-IDX-terra.md`](file://<scratchpad>/report-PORT-IDX-terra.md)) satisfies all exactness and eligibility invariants of design D24/D25 (round 5a) and PR5. There are no `P1` blockers, one `P2` allocation finding on the per-decision lookup path in [`TargetIndex.candidates`](`xacml-core/src/main/java/org/xacml4j/v30/policy/TargetIndex.java`), and one minor `P3` javadoc update in [`DecisionObserver.java`](`xacml-core/src/main/java/org/xacml4j/v30/spi/observe/DecisionObserver.java`).

---

### 1. Verification Against Design D24/D25 (Round 5a) and PR5

1. **Eligibility (`SUPPORTED`)**:
   - **Exact classes & structure**: [`TargetIndex.Builder.add`](`xacml-core/src/main/java/org/xacml4j/v30/policy/TargetIndex.java`) and [`inspect`](`xacml-core/src/main/java/org/xacml4j/v30/policy/TargetIndex.java`) check exact runtime classes (`Policy.class`, `PolicySet.class`, `Rule.class`, `Target.class`, first `MatchAnyOf.class`, every `MatchAllOf.class` in that `MatchAnyOf`, and each `MatchAllOf`'s first `Match.class` over `AttributeDesignator.class` with `StringVal`, `MustBePresent="false"`, non-null category, and `XacmlTypes.STRING`).
   - **One key per set**: [`TargetIndex.Builder.add`](`xacml-core/src/main/java/org/xacml4j/v30/policy/TargetIndex.java`) adopts the first eligible child's `AttributeDesignatorKey` (`key = Optional.of(entry.key())`) and places any child with a different key into `any` (`any.set(position)`).
   - **Proven `string-equal` & runtime validation guard**: [`TargetIndex.Builder.isStringEqual`](`xacml-core/src/main/java/org/xacml4j/v30/policy/TargetIndex.java`) checks `predicate.getClass() == BUILT_IN_SPEC` and runs the two live-context `scan` probes (`PROBE` vs `SAME` -> `MATCH` and `PROBE` vs `DIFFERENT` -> `NO_MATCH`), memoized per builder in an `IdentityHashMap<FunctionSpec, Boolean>`. Runtime parameter validation disables index building and lookup at [`Policy.java:209`](`xacml-core/src/main/java/org/xacml4j/v30/policy/Policy.java`), [`PolicySet.java:235,254`](`xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySet.java`), and [`TargetIndex.java:70`](`xacml-core/src/main/java/org/xacml4j/v30/policy/TargetIndex.java`).
   - **Key values from the request only**: [`TargetIndex.candidates`](`xacml-core/src/main/java/org/xacml4j/v30/policy/TargetIndex.java`) reads only `context.resolveEntity(ref.getCategory()).flatMap(entity -> entity.resolve(ref))` and falls back to `children` on absence or exception, so PIP fallback and `getResolvedAttributes()` are untouched.

2. **Exactness (`SUPPORTED`)**:
   - **Ascending position & `ImmutableList`**: `BitSet.nextSetBit` in [`TargetIndex.candidates`](`xacml-core/src/main/java/org/xacml4j/v30/policy/TargetIndex.java`) emits candidates in strictly ascending child order into a Guava `ImmutableList`.
   - **No filtering when the decision has misses (PR5 P1-1)**: Both [`Policy.Prepared.children`](`xacml-core/src/main/java/org/xacml4j/v30/policy/Policy.java`) and [`PolicySet.Prepared.children`](`xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySet.java`) bail out when `decision.hasMisses()` is `true`.
   - **`on-permit-apply-second` never filtered**: [`PolicySet.java:231-232`](`xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySet.java`) sets `indexable = false` when `set.combiningAlgorithm.getClass() == OnPermitApplySecondPolicyCombiningAlgorithm.class`, returning the unfiltered `PreparedList`.
   - **Pinned vs unpinned references**: [`TargetIndex.Builder.add`](`xacml-core/src/main/java/org/xacml4j/v30/policy/TargetIndex.java`) unwraps `PinnedReference.target()` to inspect the pinned target's `Target`, while unpinned `PolicyIDReference` and `PolicySetIDReference` instances fail the `known` class check and go into `any`.
   - **`Policy` rule index prepared only when index is on (PR5 P2-2)**: [`Policy.Prepared.children`](`xacml-core/src/main/java/org/xacml4j/v30/policy/Policy.java`) returns `policy.rules` immediately when `generation.indexes.isEmpty()`.
   - **Identity-keyed weak maps**: [`PreparedPolicySets.java:64,74-75`](`xacml-core/src/main/java/org/xacml4j/v30/policy/PreparedPolicySets.java`) uses `new MapMaker().weakKeys().makeMap()`, which uses identity equivalence (`==` / `System.identityHashCode`) and avoids retaining unreachable policies.
   - **Multi-valued request bags, missing key attribute, issuers, and data types**: `Entity.resolve(ref)` matches the full `AttributeDesignatorKey` (category, attributeId, dataType, issuer); missing request bags return `children`; non-`StringVal` elements return `children`; multi-valued bags union the matching positions for each value into `selected`.

3. **New Public Contract `EvaluationContextHandler.usesRequestEntityForDesignators()` (`SUPPORTED`)**:
   - [`EvaluationContextHandler.usesRequestEntityForDesignators()`](`xacml-core/src/main/java/org/xacml4j/v30/policy/EvaluationContextHandler.java`) defaults to `false` and is overridden to return `true` only by [`DefaultEvaluationContextHandler`](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java`). This cleanly removes the `xacml-core` -> `xacml-pdp` class-name check while keeping custom handlers on the unfiltered path by default.

4. **D34 Interaction (`SUPPORTED`)**:
   - [`Policy.combineDecisions`](`xacml-core/src/main/java/org/xacml4j/v30/policy/Policy.java`) and [`PolicySet.combineDecisions`](`xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySet.java`) emit `childrenSelected` only when `selected != rules` / `children != decisionRules`.
   - [`RootEvaluationContext.observationOf`](`xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`) preserves the fast `instanceof RootEvaluationContext` check for standard decisions and falls back to `top.getDecisionObservation()` (null-safe for both `top` and the returned observation) for foreign contexts.

5. **Mock-Based Test Edits & `TargetIndexPdpTest` (`SUPPORTED`)**:
   - The edits to the 9 mock-based tests only removed `verifyNoInteractions(context)` / `verifyNoMoreInteractions(..., context)` assertions that fired because `RootEvaluationContext.observationOf` now calls `getDecisionObservation()` on a foreign mock `EvaluationContext`. Every functional assertion, call-count check, and collaborator verification (`invocation`, `resolver`, `algorithm`, `cache`) remains intact.
   - [`TargetIndexPdpTest`](`xacml-pdp/src/test/java/org/xacml4j/v30/pdp/TargetIndexPdpTest.java`) and [`PdpFixtureTest`](`xacml-jmh/src/test/java/org/xacml4j/jmh/harness/PdpFixtureTest.java`) pragmatically verify full response equivalence (`Decision`, `Status`, error chain, obligations, advice, and policy identifiers) across `baseline`, `off`, and `on`.

---

### 2. Findings

#### [P2-1] `TargetIndex.candidates` allocates lambdas, a cloned `BitSet`, an iterator, and a new `ImmutableList` on every decision, even for single-valued request bags
- **Location**: [`TargetIndex.java:54-92`](`xacml-core/src/main/java/org/xacml4j/v30/policy/TargetIndex.java`)
- **Problem**:
  In [`TargetIndex.candidates(EvaluationContext)`](`xacml-core/src/main/java/org/xacml4j/v30/policy/TargetIndex.java`):
  ```java
  values = context.resolveEntity(ref.getCategory())
          .flatMap(entity -> entity.resolve(ref));
  ...
  BitSet selected = (BitSet)any.clone();
  for(Value<?> value : values.orElseThrow()){
      if(!(value instanceof StringVal string)) return children;
      List<Integer> found = positions.get(string.get());
      if(found != null) found.forEach(selected::set);
  }
  if(selected.cardinality() == children.size()) return children;
  ImmutableList.Builder<T> result = ImmutableList.builder();
  for(int i = selected.nextSetBit(0); i >= 0; i = selected.nextSetBit(i + 1)){
      result.add(children.get(i));
  }
  return result.build();
  ```
  On every indexed `PolicySet` or `Policy` evaluation, this method:
  1. Allocates a capturing lambda `entity -> entity.resolve(ref)` (`16 B`).
  2. Clones `BitSet any` (`24 B` + `long[]`, e.g., `144 B` for 1,000 children).
  3. Allocates a bag iterator and, on a hit, a capturing method-reference lambda `selected::set` (`16 B`).
  4. Allocates an `ImmutableList.Builder`, its `Object[]` buffer, and a new `ImmutableList<T>` (`88+ B`).
  On `synthetic:1000`, this accounts for ~300 B of the 940 B/op smoke allocation. Yet for a single-valued request bag (`bag.size() == 1`, the standard case in `synthetic:100`, `synthetic:1000`, `rsa2008`, and almost all XACML requests), the candidate `ImmutableList<T>` for a given `String` value is completely static for the lifetime of the `TargetIndex` (`any U positions.get(value)` in ascending order, or `anyChildren` when the value matches no indexed literal).
- **Fix**:
  1. Precompute in `TargetIndex` construction:
     - `private final List<T> anyChildren;` (the `ImmutableList<T>` for `any`, or `children` if `any.cardinality() == children.size()`).
     - `private final Map<String, List<T>> singleValueCandidates;` (for each literal in `positions`, the pre-built `ImmutableList<T>` for `any U positions.get(literal)`, or `children` if its cardinality equals `children.size()`).
  2. In `candidates(EvaluationContext)`, replace `flatMap(entity -> entity.resolve(ref))` with an explicit `Optional<Entity>` check, fast-path `bag.size() == 1` via `singleValueCandidates.getOrDefault(string.get(), anyChildren)` with **zero allocations**, and in the multi-valued loop replace `found.forEach(selected::set)` with an index loop:
     ```java
     Optional<Entity> entity = context.resolveEntity(ref.getCategory());
     if(entity.isEmpty()) return children;
     Optional<BagOfValues> values = entity.get().resolve(ref);
     if(values.isEmpty()) return children;
     List<Value> bag = values.orElseThrow().values();
     if(bag.isEmpty()) return anyChildren;
     if(bag.size() == 1){
         if(!(bag.get(0) instanceof StringVal string)) return children;
         return singleValueCandidates.getOrDefault(string.get(), anyChildren);
     }
     BitSet selected = (BitSet)any.clone();
     for(int i = 0; i < bag.size(); i++){
         if(!(bag.get(i) instanceof StringVal string)) return children;
         List<Integer> found = positions.get(string.get());
         if(found != null){
             for(int j = 0; j < found.size(); j++) selected.set(found.get(j));
         }
     }
     ```

#### [P3-1] `DecisionObserver.childrenSelected` javadoc mentions only policy sets and prepared lists
- **Location**: [`DecisionObserver.java:211-216`](`xacml-core/src/main/java/org/xacml4j/v30/spi/observe/DecisionObserver.java`)
- **Problem**:
  The javadoc on `childrenSelected` still reads: *"A policy set gives its combining algorithm a prepared list of its children instead of its children; not reported when the algorithm gets the children"*. With task 23.4 merged, `Policy` also calls `childrenSelected` when its target index selects a candidate sublist (`design.md:1149-1152`).
- **Fix**:
  Update the javadoc comment at [`DecisionObserver.java:212-213`](`xacml-core/src/main/java/org/xacml4j/v30/spi/observe/DecisionObserver.java`) to:
  *"A policy set or policy gives its combining algorithm a prepared list or an indexed candidate list of its children instead of its original children; not reported when the algorithm gets the original children."*
