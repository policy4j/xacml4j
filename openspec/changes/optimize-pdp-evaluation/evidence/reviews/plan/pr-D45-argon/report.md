# Plan Review PR-D45-argon: Design D45 (Shared Expensive Condition Subexpressions Within One Decision)

- **Reviewer**: Lane `agyx_1` (Agy Gemini 4 Argon)
- **Target**: [`D45-DRAFT-sol.md`](`.ntm/worktrees/xacml4j--batch2/agyx_1/D45-DRAFT-sol.md`) ([`pr20-sol-d45-draft.md`](`.ntm/worktrees/xacml4j--batch2/agyx_1/openspec/changes/optimize-pdp-evaluation/research/pr20-sol-d45-draft.md`)), background [`pr19-sol-condition-analysis.md`](`.ntm/worktrees/xacml4j--batch2/agyx_1/openspec/changes/optimize-pdp-evaluation/research/pr19-sol-condition-analysis.md`) and [`RulesCorpus.java`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/RulesCorpus.java`)
- **Saved Report**: [`REPORT-PR-D45-argon.md`](`.ntm/worktrees/xacml4j--batch2/agyx_1/REPORT-PR-D45-argon.md`)
- **Verdict**: **APPROVE WITH CHANGES**

---

## Executive Summary

Codex Sol's D45 draft is well-bounded in its XACML 3.0 eligibility rules, structural canonicalization, and threshold math, but has **three P1 design gaps** and **two P2 execution-path omissions** that must be tightened in the plan text before implementation:

1. **[P1-1] Prepared scope contradiction (`Condition` vs `Policy`)**: [`D45-DRAFT-sol.md:25`](`.ntm/worktrees/xacml4j--batch2/agyx_1/D45-DRAFT-sol.md`) keys preparation in [`Generation`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/PreparedPolicySets.java`) by exact [`Condition`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/Condition.java`) identity, while [`:33-34`](`.ntm/worktrees/xacml4j--batch2/agyx_1/D45-DRAFT-sol.md`) requires at least `4` occurrences in the prepared scope. In `repeated-conditions` ([`RulesCorpus.java:259-270`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/RulesCorpus.java`)), the repeated subexpression (`subject.email`, not `subject.department` as written at [`:8`](`.ntm/worktrees/xacml4j--batch2/agyx_1/D45-DRAFT-sol.md`)) appears **once per `Condition`** across **100 `Rule`s of one `Policy`**, and [`Condition`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/Condition.java`) has no back-pointer to its enclosing [`Policy`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/Policy.java`). Preparing a single `Condition` in isolation sees occurrence count `1 < 4` and selects nothing. Preparation must scan the enclosing `Policy` (`context.getCurrentPolicy()`), weakly keyed by `Policy` in `Generation` (matching [`Indexes.policies`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/PreparedPolicySets.java`)).
2. **[P1-2] Sharing `RootEvaluationContext.misses` breaks `hasMisses()` unless reference misses are marked in $O(1)$**: [`RootEvaluationContext.hasMisses()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`) returns `m != null && !m.isEmpty()`, which [`Policy.Prepared.children`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/Policy.java`) and [`PolicySet.Prepared.children`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySet.java`) check to disable [`TargetIndex`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/TargetIndex.java`). If expression memo entries are written into `misses` without changing how `hasMisses()` detects a reference miss, the first memoized expression makes `!m.isEmpty()` true and silently disables `TargetIndex` for the rest of the decision. Recording the existing [`Missing.INSTANCE`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`) sentinel as a key in `misses` inside [`remember(ref, outcome)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`) and checking `m != null && m.containsKey(Missing.INSTANCE)` in `hasMisses()` keeps `hasMisses()` $O(1)$ with zero new fields and zero allocations.
3. **[P1-3] D34 `DecisionObserver` attribute callbacks and deciding-thread confinement**: In an observed decision (`root.getDecisionObservation().isEnabled()`), every [`AttributeDesignator`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/AttributeDesignator.java`) evaluation must reach [`DefaultEvaluationContextHandler.resolve`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java`) so `attributeRequested` and `attributeResolved(..., REQUEST_MEMO, ...)` fire under each rule's evaluation span. Skipping subtree evaluation on rules `2..N` during an observed decision would drop those events. D45 must be gated on `root.isDecidingThread() && !root.getDecisionObservation().isEnabled()`, matching [`TargetBeforeContext.precheck`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/TargetBeforeContext.java`).
4. **[P2-1] Reaching nested selected `Apply` roots inside `Condition.evaluate` without touching unselected `Apply` calls or breaking lazy `and`/`or`**: The selected subexpression in `repeated-conditions` is nested inside `and(string-regexp-match(pattern, SELECTED), string-equal(...))`. D45 must specify how `Condition.evaluate` dispatches to `SELECTED` (a prepared `Expression` spine for qualifying `Condition`s that clones only the ancestor `Apply` spine down to `SharedApply` roots, leaving `Apply.evaluate`, lazy `and`/`or` short-circuiting, and unselected `Condition`s untouched).
5. **[P2-2] Single request-entity check on memo miss and fast skip for non-qualifying policies**: A memo hit in `root` already proves the subtree's designators resolved from `root`'s request entity to singleton `StringVal` bags in this decision, so only a memo miss should check `resolveEntity` / `entity.resolve`. And a construction-time `boolean` flag on [`Policy`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/Policy.java`) (fitting in the 3 bytes of alignment padding next to `canRespond`) avoids touching `Generation` or allocating weak-map entries for policies without candidate string-normalization chains.

---

## Detailed Review by Assessment Area

### 1. Soundness Against XACML 3.0 (§§7.3.5, 7.4, 7.8, 7.18, 7.19, A.3.5, A.3.10, A.3.13)

- **Allow-list & pure functions (§7.4, §A.3.10, §A.3.13)**: `SUPPORTED`.
  - [`BagFunctions.stringOneAndOnly`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/BagFunctions.java`), [`StringConversionFunctions.normalizeSpace`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/StringConversionFunctions.java`), and [`StringConversionFunctions.normalizeToLowerCase`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/StringConversionFunctions.java`) are pure, stateless functions over `BagOfValues` / `StringVal` that do not read `EvaluationContext` (clock, timezone, XPath, status) and are built with `writesNoResponses()` (so §7.18 obligation/advice staging cannot occur).
- **Request-backed positive singleton bags only & success-only memo (§7.3.5, §7.19)**: `SUPPORTED` (with **P1-3** and **P2-2**).
  - Restricting memo use and population to subtrees whose [`AttributeDesignator`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/AttributeDesignator.java`) leaves resolve from the request [`Entity`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/types/Entity.java`) (`root.hasRequestAttributeResolution()`) to a present singleton bag of exact [`StringVal`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/types/StringVal.java`) guarantees that:
    1. PIP resolvers are never called or bypassed for that attribute, and [`DefaultEvaluationContextHandler.getResolvedDesignators()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java`) (`designatorCache`, which records only PIP-resolved bags) is unaffected.
    2. Missing attributes (`MustBePresent="true"` or `"false"`), empty bags, and multi-valued bags never enter the memo; `string-one-and-only` over an empty or multi-valued bag falls through to normal evaluation on every occurrence and throws the exact `FunctionInvocationException` / `AttributeReferenceEvaluationException`, preserving `context.setEvaluationStatus`, `setEvaluationStatusIfAbsent`, and extended-Indeterminate passes (`isExtendedIndeterminateEval()`).
  - **Gap (P1-3)**: When a D34 [`DecisionObserver`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/spi/observe/DecisionObserver.java`) is active (`root.getDecisionObservation().isEnabled()`), [`DefaultEvaluationContextHandler.resolve`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java`) emits `attributeRequested` and `attributeResolved(..., REQUEST_MEMO, ...)` on every designator evaluation. D45 must therefore bail out when `root.getDecisionObservation().isEnabled()` or `!root.isDecidingThread()`.
- **Lazy `and`/`or`, operand order, and `VariableReference` (§7.8, §A.3.5)**: `SUPPORTED` (with **P2-1**).
  - Selected subexpressions must be evaluated at the exact point in the condition AST where normal evaluation reaches them (via a prepared `Expression` spine), never pre-evaluated at `Condition` entry, so short-circuiting `and`/`or` ([`BooleanFunctions.java`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/BooleanFunctions.java`)) and left-to-right operand error precedence remain untouched.

### 2. Identity: Function Binding, Complete Designator Key, and Preparation Map vs Guava `Interners`

- **Binding by identity**: `SUPPORTED`.
  - [`Apply.equals`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/Apply.java`) delegates to [`FunctionSpecImpl.equals`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java`), which compares `functionId` strings only. Comparing `FunctionSpec` by reference identity (`==` / `System.identityHashCode`) after verifying built-in provenance prevents any custom function with the same XACML ID from colliding with or entering the allow-list.
  - As noted in **P3-1**, [`DefaultFunctionInvocation`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java`) (`:218-222`) already caches one canonical `Unary` binding per `Method` in `BINDINGS` only when `factory == FunctionInvocationFactory.defaultFactory() && instance == null && isBindable(m)`. Checking `FunctionSpecImpl.direct` against the three built-in `Method` bindings in `BINDINGS` proves provenance in $O(1)$ without new reflection.
- **Complete `AttributeDesignator` key with `MustBePresent`**: `SUPPORTED`.
  - [`AttributeDesignator`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/AttributeDesignator.java`) is keyed by `(AttributeDesignatorKey, isMustBePresent())` with exact class `AttributeDesignator.class`, `ref.getCategory() != null`, and `XacmlTypes.STRING.equals(ref.getDataType())`.
- **Preparation-owned map in `Generation` vs Guava `Interners`**: `SUPPORTED`.
  - A preparation-owned `HashMap<SharedExprKey, SharedExprKey>` (`computeIfAbsent(k, x -> x)`) or `ConcurrentHashMap` in `Generation` is strictly better than Guava `Interners`: it avoids global retention across generations, avoids `WeakReference` wrapper churn, and operates on the custom structural key `SharedExprKey` rather than `Expression.equals` (where `Apply.equals` only compares function IDs and [`BagOfValues.equals`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java`) uses order-insensitive multiset equality).

### 3. Root Memo in `RootEvaluationContext.misses` & Namespace Isolation (`isMissed`, `hasMisses`)

- **Namespace isolation for `isMissed(ref)` and `snapshot(ref, current)`**: `SUPPORTED`.
  - Reference keys are [`CompositeDecisionRuleIDReference`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/CompositeDecisionRuleIDReference.java`) (`PolicyIDReference`, `PolicySetIDReference`), while expression keys are the package-private final `SharedExprKey`. Neither implements the other's type, so `isMissed(ref)` and `snapshot(ref, current)` can never match an expression entry.
- **Critical defect on `hasMisses()` (P1-2)**: `NEEDS CHANGE`.
  - [`RootEvaluationContext.hasMisses()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`) currently checks `m != null && !m.isEmpty()`. Both [`Policy.Prepared.children`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/Policy.java`) and [`PolicySet.Prepared.children`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySet.java`) disable `TargetIndex` whenever `decision.hasMisses()` returns `true`.
  - Fix in [`RootEvaluationContext.remember`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`) by recording `m.putIfAbsent(Missing.INSTANCE, Missing.INSTANCE)` when a reference miss is stored, and changing `hasMisses()` to `m != null && m.containsKey(Missing.INSTANCE)`.

### 4. Zero Bytes Per Decision When Nothing Qualifies

- **Root context**: `SUPPORTED`. `RootEvaluationContext` adds no field or eager holder; `misses` remains `null` until a reference miss or a selected shared subexpression writes to it.
- **Per-rule / per-policy path when nothing qualifies (P2-2)**: `NEEDS CHANGE`.
  - If `Condition.evaluate` probed a `MapMaker().weakKeys()` map in `Generation` on every rule even for policies without candidate `string-normalize-*` chains (`rsa2008`, `narrow`, `wide`, `multi-valued`, `responses`), the first decision in a generation would allocate weak-map entries for non-qualifying policies/conditions and every decision would pay a weak-map lookup per rule.
  - Computing a construction-time `boolean mayShareConditions` on [`Policy`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/Policy.java`) (which fits in the 3 bytes of alignment padding next to `private final boolean canRespond`, adding 0 bytes to `Policy`) lets `Condition.evaluate` skip `Generation` in one branch whenever the policy has no candidate subexpressions.

### 5. Selection Threshold (`>= 4` Occurrences, `>= 2` Allocating Transformations) and the ~17 KB Estimate

- **Threshold and byte math**: `SUPPORTED`.
  - In `rules:100` (`repeated-conditions`, [`RulesCorpus.java:169-172`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/RulesCorpus.java`), [`:259-270`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/RulesCorpus.java`)), the request value `"  Alice@Example.ORG "` undergoes:
    - `normalizeSpace`: `substring(2, 19)` $\rightarrow$ `byte[17]` (32 B) + `String` (24 B) + `StringVal` (24 B) = **80 B/rule**.
    - `normalizeToLowerCase`: `toLowerCase(Locale.ROOT)` $\rightarrow$ `byte[17]` (32 B) + `String` (24 B) + `StringVal` (24 B) + internal temp (~16 B) = **~96 B/rule**.
    - Across 100 rules, evaluating the chain 100 times allocates $100 \times 176\text{ B} = \mathbf{17,600\text{ B}}$.
  - Initializing `RootEvaluationContext.misses` (`new ConcurrentHashMap<>()`) on the first rule costs **176 B** (`ConcurrentHashMap` 64 B + `Node[16]` 80 B + 1 `Node` 32 B), saving $99 \times 176\text{ B} = \mathbf{17,424\text{ B}}$ (**~17.0 KB/op**, ~9% of the current 194,344 B/op; the other ~176.3 KB/op is `Pattern.compile` + `Matcher` in `RegularExpressionFunctions.regexpMatchImpl`).
  - Requiring $\ge 4$ occurrences and $\ge 2$ allocating string transformations guarantees that even a 4-occurrence policy saves $\sim 528\text{ B} - 176\text{ B} = +352\text{ B}$ net, whereas a 1-transformation or 2-occurrence chain could break even or regress by the 176 B map initialization cost.

### 6. Simpler Alternatives Considered

1. **Policy-authored `<VariableDefinition>` / `<VariableReference>` (XACML 3.0 §5.15, §7.8)**:
   - [`PolicyDescendantEvaluationContext.varDefEvalResults`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/Policy.java`) and [`VariableDefinition.evaluate`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/VariableDefinition.java`) already cache shared expressions within a `Policy`, but cannot help policies (including `RulesCorpus` `repeated-conditions` and existing XML/JSON policies) that inline repeated `<Apply>` trees across rules without `<VariableDefinition>`.
2. **Per-`Policy` construction-time preparation vs `Generation` sidecar**:
   - Because `Policy`, `Rule`, `Condition`, and `Apply` never contain `PolicyIDReference` / `PolicySetIDReference`, a `Policy`'s condition AST is invariant across repository generations. However, keeping the prepared sidecar in `Generation` (weakly keyed by `Policy`, gated by a zero-cost `boolean mayShareConditions` on `Policy`) avoids adding a reference field to `Policy` or `Condition` while still sharing canonical `SharedExprKey`s across policies in the same generation.
3. **Value relative to Regex Caching (D6 / `add-regex-engine-spi`)**:
   - On `repeated-conditions` today, regex compilation/matching is ~90.7% (~176.3 KB/op) of allocation and D45 is ~9.0% (~17.4 KB/op). Once regex caching lands, D45 eliminates ~97% of the remaining per-rule allocation on `repeated-conditions` (~18 KB/op $\rightarrow$ ~0.6 KB/op). With the simplifications in **P1-1**, **P1-2**, **P2-1**, and **P2-2**, D45 is small (~150–200 lines in `xacml-core`) and worth landing.

### 7. Answers to the Three Open Questions (`D45-DRAFT-sol.md:80-84`)

1. **Question 1 (`string-normalize-to-upper-case`)**: **Stay strictly on the three OASIS standard functions** (`string-one-and-only`, `string-normalize-space`, `string-normalize-to-lower-case`) for the initial cut. [`StringConversionFunctions.normalizeToUpperCase`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/StringConversionFunctions.java`) is a non-standard `urn:xacml4j:...` extension that delegates to [`StringVal.toUpperCase()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/types/StringVal.java`) (`String.toUpperCase()` with default locale rather than `Locale.ROOT`).
2. **Question 2 (Threshold `4` vs `2` occurrences)**: **Keep the threshold at `4` occurrences (with `>= 2` allocating string transformations)**. Lazy creation of `RootEvaluationContext.misses` (`ConcurrentHashMap` + `Node[16]` + `Node`) costs 176 B, which equals the ~144–176 B saved by a 2nd occurrence; `2` occurrences can break even or regress when the 2nd rule short-circuits, whereas `4` occurrences guarantees a net savings of ~250–350 B.
3. **Question 3 (`DecisionObserver` / counter hook)**: **Keep preparation and memoization strictly internal to `xacml-core` with no new observer callback or public counter**, and bypass D45 when `DecisionObservation.isEnabled()` is `true` (**P1-3**) so D34's per-rule `attributeRequested` / `attributeResolved` event stream stays identical.

---

## Findings and Exact Text Changes

### [P1-1] Key preparation by `Policy` (scanning all `Rule` conditions of the policy) rather than isolated `Condition` identity, and fix the `subject.email` attribute name

- **Location**: [`D45-DRAFT-sol.md:6-11`](`.ntm/worktrees/xacml4j--batch2/agyx_1/D45-DRAFT-sol.md`), [`:25-26`](`.ntm/worktrees/xacml4j--batch2/agyx_1/D45-DRAFT-sol.md`), [`:33-35`](`.ntm/worktrees/xacml4j--batch2/agyx_1/D45-DRAFT-sol.md`)
- **Exact text to change**:
  - In **Context** ([`D45-DRAFT-sol.md:8`](`.ntm/worktrees/xacml4j--batch2/agyx_1/D45-DRAFT-sol.md`)), replace:
    ```markdown
      `string-normalize-to-lower-case(string-normalize-space(string-one-and-only(subject.department)))`
    ```
    with:
    ```markdown
      `string-normalize-to-lower-case(string-normalize-space(string-one-and-only(subject.email)))`
    ```
  - In **Preparation and Canonical Identity** ([`D45-DRAFT-sol.md:25-26, 33-35`](`.ntm/worktrees/xacml4j--batch2/agyx_1/D45-DRAFT-sol.md`)), replace:
    ```markdown
    - Keep preparation in the D24/D25 generation (`PreparedPolicySets.java:61`), weakly keyed by exact `Condition` identity (`Condition.java:44`, `Condition.java:123`).
    - Build one immutable `PreparedConditions` sidecar lazily on first eligible evaluation, and drop it automatically on `PreparedPolicySets.invalidate()` (`PreparedPolicySets.java:51`).
    ```
    with:
    ```markdown
    - Keep preparation in the D24/D25 generation (`PreparedPolicySets.java:61`), weakly keyed by the enclosing `Policy` (`context.getCurrentPolicy()`, `new MapMaker().weakKeys().makeMap()`, matching `Indexes.policies` at `PreparedPolicySets.java:75`), with a single-`Condition` fallback only when `context.getCurrentPolicy()` is not a `Policy`.
    - When a qualifying `Policy` is first evaluated in a generation, scan all `Rule` conditions of `policy.getRules()` together to count occurrences across the policy's rules, build one immutable `PreparedConditions` sidecar mapping each qualifying `Condition` (by identity) to its prepared predicate, and drop it automatically on `PreparedPolicySets.invalidate()` (`PreparedPolicySets.java:51`).
    ```

### [P1-2] Preserve $O(1)$ `RootEvaluationContext.hasMisses()` via the `Missing.INSTANCE` sentinel when sharing `misses`

- **Location**: [`D45-DRAFT-sol.md:39-44`](`.ntm/worktrees/xacml4j--batch2/agyx_1/D45-DRAFT-sol.md`), [`RootEvaluationContext.java:714-717`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`), [`:752-757`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`)
- **Exact text to change**:
  - In **Per-Decision Memo in `RootEvaluationContext`** ([`D45-DRAFT-sol.md:39, 43`](`.ntm/worktrees/xacml4j--batch2/agyx_1/D45-DRAFT-sol.md`)), replace:
    ```markdown
    - Preserve `isMissed`, reference replay and `hasMisses`: expression entries are not reference misses.
    ```
    with:
    ```markdown
    - Preserve $O(1)$ `isMissed`, reference replay, and `hasMisses`: `SharedExprKey` is a package-private final class disjoint from `CompositeDecisionRuleIDReference` and `Missing`; `remember(CompositeDecisionRuleIDReference, Object)` (`RootEvaluationContext.java:752`) also records `m.putIfAbsent(Missing.INSTANCE, Missing.INSTANCE)`, and `hasMisses()` (`RootEvaluationContext.java:714`) checks `m != null && m.containsKey(Missing.INSTANCE)` instead of `!m.isEmpty()`, so expression memo entries never report a reference miss or disable `TargetIndex` (`Policy.java:209`, `PolicySet.java:255`).
    ```

### [P1-3] Bypass D45 when `DecisionObservation.isEnabled()` is true or off the deciding thread

- **Location**: [`D45-DRAFT-sol.md:52`](`.ntm/worktrees/xacml4j--batch2/agyx_1/D45-DRAFT-sol.md`)
- **Exact text to change**:
  - In **Exactness and Guard Conditions** ([`D45-DRAFT-sol.md:52`](`.ntm/worktrees/xacml4j--batch2/agyx_1/D45-DRAFT-sol.md`)), replace:
    ```markdown
    - Keep the existing evaluation path when `context.isValidateFuncParamsAtRuntime()` holds, when `DescendantEvaluationContext.topOf(context)` is not `RootEvaluationContext` with a present `Generation` and `hasRequestAttributeResolution()`, or when any foreign expression or function implementation is present.
    ```
    with:
    ```markdown
    - Keep the existing evaluation path when `context.isValidateFuncParamsAtRuntime()` holds, when `DescendantEvaluationContext.topOf(context)` is not `RootEvaluationContext` `root` with `root.generation().isPresent()`, `root.isDecidingThread()`, `root.hasRequestAttributeResolution()`, and `!root.getDecisionObservation().isEnabled()` (preserving D34's per-rule `attributeRequested` / `attributeResolved` callbacks in observed decisions, as `TargetBeforeContext` does at `TargetBeforeContext.java:186-187`), or when any foreign expression or function implementation is present.
    ```

### [P2-1] Specify prepared `Expression` spine replacement for qualifying `Condition`s so nested selected roots are reached without touching `Apply.evaluate` or lazy `and`/`or`

- **Location**: [`D45-DRAFT-sol.md:36-41`](`.ntm/worktrees/xacml4j--batch2/agyx_1/D45-DRAFT-sol.md`), [`:68-73`](`.ntm/worktrees/xacml4j--batch2/agyx_1/D45-DRAFT-sol.md`)
- **Exact text to change**:
  - In **Preparation and Canonical Identity** ([`D45-DRAFT-sol.md:36-38`](`.ntm/worktrees/xacml4j--batch2/agyx_1/D45-DRAFT-sol.md`)), add:
    ```markdown
    - For each `Condition` that contains at least one selected subexpression reachable exclusively through a spine of type-correct, built-in `Apply` nodes (`Apply.class`, `FunctionSpecImpl` with no custom `FunctionParametersValidator`), `PreparedConditions` builds a prepared `Expression` spine for that `Condition` that replaces each selected root `Apply` with a package-private `SharedApply` `Expression` (wrapping the original `Apply`, its `SharedExprKey`, and its referenced `AttributeDesignatorKey` array) and clones only the ancestor `Apply` spine above it; `Condition.evaluate` evaluates that prepared predicate when active, leaving `Apply.evaluate`, lazy `and`/`or` short-circuiting, and unselected `Apply` subtrees untouched.
    ```

### [P2-2] Check request-entity singleton `StringVal` bags only on memo miss, and gate `Generation` lookup with a zero-byte-cost `Policy` flag

- **Location**: [`D45-DRAFT-sol.md:23`](`.ntm/worktrees/xacml4j--batch2/agyx_1/D45-DRAFT-sol.md`), [`:40-42`](`.ntm/worktrees/xacml4j--batch2/agyx_1/D45-DRAFT-sol.md`), [`:68-73`](`.ntm/worktrees/xacml4j--batch2/agyx_1/D45-DRAFT-sol.md`)
- **Exact text to change**:
  - In **Per-Decision Memo in `RootEvaluationContext`** ([`D45-DRAFT-sol.md:40-42`](`.ntm/worktrees/xacml4j--batch2/agyx_1/D45-DRAFT-sol.md`)), replace:
    ```markdown
    - Read and write the memo only at selected shared subexpression roots; unselected expressions do not touch the root map.
    - When a condition has no selected subexpressions, evaluation allocates `0` extra bytes.
    - Store only successful `ValueExp` results of request-backed singleton `StringVal` evaluations. Never store `EvaluationException`, `Indeterminate`, missing-attribute results, empty bags, multi-valued bags, or PIP-backed results.
    ```
    with:
    ```markdown
    - Read and write the memo only at selected `SharedApply` roots: on a memo hit in `root`, return the cached `StringVal` immediately (since `DescendantEvaluationContext.topOf(context) == root` and `root`'s request entities are immutable for the decision, a present entry in `root` already proves the request-backed singleton `StringVal` guard succeeded on the first evaluation in this decision); on a memo miss, verify that each referenced `AttributeDesignatorKey` resolves from `root.resolveEntity(key.getCategory())` to a present singleton bag of exact `StringVal` before evaluating and storing the `StringVal` result.
    - Compute a construction-time `boolean mayShareConditions` on `Policy` (`Policy.java:79`, fitting in the existing alignment padding beside `canRespond`) that is `true` only when the policy has at least 4 rules whose conditions contain a $\ge 2$-step `string-normalize-*` chain; when `false`, `Condition.evaluate` skips `Generation` in one branch with `0` extra bytes and no weak-map entry.
    - Store only successful `ValueExp` results of request-backed singleton `StringVal` evaluations. Never store `EvaluationException`, `Indeterminate`, missing-attribute results, empty bags, multi-valued bags, or PIP-backed results.
    ```

### [P3-1] Use `DefaultFunctionInvocation`'s existing `Unary` binding for $O(1)$ provenance verification and resolve the Open Questions

- **Location**: [`D45-DRAFT-sol.md:19-21`](`.ntm/worktrees/xacml4j--batch2/agyx_1/D45-DRAFT-sol.md`), [`:80-84`](`.ntm/worktrees/xacml4j--batch2/agyx_1/D45-DRAFT-sol.md`)
- **Exact text to change**:
  - In **Eligibility Allow-List** ([`D45-DRAFT-sol.md:21`](`.ntm/worktrees/xacml4j--batch2/agyx_1/D45-DRAFT-sol.md`)), note that built-in provenance for the three unary functions can be checked in $O(1)$ via `FunctionSpecImpl.direct` (`FunctionSpecBuilder.java:371`) matching the canonical `Unary` binding in `DefaultFunctionInvocation.BINDINGS` (`DefaultFunctionInvocation.java:74, 218-222`) with `validator == null` and `writesNoResponses()`.
  - Replace **Open Questions** ([`D45-DRAFT-sol.md:80-84`](`.ntm/worktrees/xacml4j--batch2/agyx_1/D45-DRAFT-sol.md`)) with recorded answers:
    1. Keep the initial allow-list strictly on the three OASIS standard functions (`string-one-and-only`, `string-normalize-space`, `string-normalize-to-lower-case`); omit non-standard `string-normalize-to-upper-case`.
    2. Keep the selection threshold at `>= 4` occurrences and `>= 2` allocating string transformations (lazy creation of `RootEvaluationContext.misses` costs 176 B, which matches a single occurrence's ~144–176 B).
    3. Keep preparation and memoization internal to `xacml-core` with no `DecisionObserver` or public API changes, and bypass D45 when `DecisionObservation.isEnabled()` is `true`.
