# Code Review Report: R-AY-argon (Cycle AY — P-FIXED3 R3 to R6)

- **Reviewer**: Lane `agyx_1` (Gemini Argon)
- **Target**: Codex Astra's cycle AY (`PATCH-AY.diff`, `REPORT-I-AY-astra.md`) in change `optimize-pdp-evaluation` (`P-FIXED3` items R3, R4, R5, R6, and `Attribute.hashCode` fold)
- **Verdict**: **APPROVE**

---

## Executive Summary

Cycle AY cleanly implements `P-FIXED3` items R3 through R6 and the `Attribute.hashCode` fold:

1. **R5 (`Result` / `ResponseContext`)**: Only plain `PERMIT`, `DENY`, and `NOT_APPLICABLE` results with `Status.ok()` and all five payload collections empty reuse canonical `Result` instances (`Result.java:48-50, 106-128`, `ResponseContext.java:33-35`). All `INDETERMINATE*` decisions, explicit statuses (including statuses carrying a `Throwable`), non-empty payloads, and `Result.Builder` builds continue to allocate distinct `Result` instances.
2. **R6 and `Attribute.hashCode` fold (`Attribute`)**: `Attribute.hashCode()` (`Attribute.java:135-140`) expands the 4-element polynomial fold inline without allocating `Object[4]`, producing bit-for-bit identical hashes to `com.google.common.base.Objects.hashCode(attributeId, issuer, includeInResult, values)` without caching on extensible `Value` elements. `Attribute.Builder.values(Iterable)` (`Attribute.java:193-205`) indexes `BagOfValues` via `bag.valueAt(i)`, matching `BagStorage.iterator()` element-for-element across duplicates, grouped order, `-0.0`/`0.0`, and `NaN`.
3. **R3 (`ResolverContext` / `ResolverCacheKey`)**: `ResolverContext.Default.getCacheKey()` (`ResolverContext.java:149-166`) reads a single declared context key directly via `resolvedKeys.get(keys.get(0))` and `ResolverCacheKey.of(String, BagOfValues)` (`ResolverCacheKey.java:79-82`), avoiding `SingletonImmutableBiMap.values()` allocation while preserving lazy resolution, retry-on-all-empty semantics, `isCacheKeyAvailable()`, multi-key order, and byte-identical `ResolverCacheKey` equality/hash/serialization.
4. **R4 (`BaseDecisionRuleResponseExpression`) & D48 Compatibility**: Literal `AttributeAssignmentExpression`s with exact built-in scalar/path `Value` or `BagOfValues` expressions are pre-built once per expression inside mixed obligation/advice expressions (`BaseDecisionRuleResponseExpression.java:75-107, 162-179, 207-229`), while custom expressions and subclasses remain dynamic. The `ImmutableList<AttributeAssignment>` returned by `evaluateAttributeAssignments(context)` is already grouped in first-seen `attributeId` runs, feeding directly into D48's zero-copy fast path in `DecisionRuleResponse`.
5. **Tests**: Focused unit tests in `ResultTest`, `AttributeTest`, `ResolverContextTest`, and `ObligationExpressionTest` pin the new invariants; 9 focused test classes and the full reactor (core 7,288, PDP 793, conformance 1,818) pass with zero regressions.

No P1 or P2 findings. One minor P3 observation is noted below.

---

## Findings

### [P3-1] `BaseDecisionRule.allConstant` checks `instanceof ValueExp` whereas `BaseDecisionRuleResponseExpression.isLiteral` checks exact `LITERAL_TYPES` (excluding `Entity` and custom `ValueExp`s)

- **Location**: [BaseDecisionRuleResponseExpression.java:75-80](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/BaseDecisionRuleResponseExpression.java`), [BaseDecisionRuleResponseExpression.java:217-229](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/BaseDecisionRuleResponseExpression.java`), [BaseDecisionRule.java:90-97](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/BaseDecisionRule.java`)
- **Evidence**: `SUPPORTED` (confirmed in opened source and `ExternalObligationExpressionCompatibilityTest`)
- **Details**:
  - `LITERAL_TYPES` ([`BaseDecisionRuleResponseExpression.java:75-80`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/BaseDecisionRuleResponseExpression.java`)) lists the 17 built-in scalar and path `Value` implementation classes from [`XacmlTypes`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/types/XacmlTypes.java`), intentionally omitting [`Entity.class`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/types/Entity.java`) (which can wrap a DOM `Node` inside `Content`) and custom `ValueExp` implementations.
  - Meanwhile, [`BaseDecisionRule.allConstant`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/BaseDecisionRule.java`) still checks `a.getExpression() instanceof ValueExp`. If a rule has an `ObligationExpression` or `AdviceExpression` whose assignment is an `Entity` or custom `ValueExp`, `BaseDecisionRule.constantObligations` / `constantAdvice` is `true` while `ObligationExpression.constant` ([`ObligationExpression.java:45-47`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/ObligationExpression.java`)) is `Optional.empty()`.
  - **Impact**: Benign. [`ObligationExpression.evaluate`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/ObligationExpression.java`) and [`AdviceExpression.evaluate`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/AdviceExpression.java`) use `constant.orElseGet(() -> ... evaluateAttributeAssignments(context))`, so `BaseDecisionRule.evaluateObligations(null)` / `evaluateAdvice(null)` only runs when every assignment is `instanceof ValueExp` (whose `.evaluate(null)` returns `this` without touching `context`). Still, if `BaseDecisionRule.allConstant` is touched in a later cleanup, delegating to `exp.isConstant()` would keep the two predicates aligned.

---

## Detailed Point-by-Point Verification

### 1. R5: Canonical Plain Results ([`Result.java:48-50, 106-128`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/Result.java`), [`ResponseContext.java:33-35`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/ResponseContext.java`))

- **Static initialization order** (`SUPPORTED`):
  - In [`Result.java:47-50`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/Result.java`), `OK = Status.ok().build()` is declared on line 47 before `PERMIT`, `DENY`, and `NOT_APPLICABLE` on lines 48–50, which invoke `ok(Decision.*).build()`. `ok(Decision)` reads `OK`, so static initialization order is strictly top-down with no null hazard.
  - In [`ResponseContext.java:33-35`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/ResponseContext.java`), `PERMIT`, `DENY`, and `NOT_APPLICABLE` wrap `Result.plain(Decision.*)`, unifying the `Result` instances inside `ResponseContext.of(Result.of(...))` with `Result.of(...)`.
- **Gate conditions & error/status isolation** (`SUPPORTED`):
  - [`Result.of(Decision, ImmutableList<Obligation>, ImmutableList<Advice>, Collection<Category>, Collection<Category>, ImmutableList<CompositeDecisionRuleIDReference>)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/Result.java`) checks `Preconditions.checkArgument(!decision.isIndeterminate(), ...)` first (throwing `NullPointerException` on `null` decision and `IllegalArgumentException` on `INDETERMINATE*`), then calls `.isEmpty()` on each of the five collections (`obligations`, `advice`, `includeInResult`, `resolvedAttributes`, `policyIdentifiers`), preserving `NullPointerException` if any collection is `null`.
  - Only when all five collections are empty does it return `plain(decision)` (`PERMIT`, `DENY`, or `NOT_APPLICABLE`).
  - [`Result.of(Decision, Status, Collection<Category>, Collection<Category>)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/Result.java`) (used by [`DefaultPolicyDecisionPoint.java:244`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java`) on the failure path) and [`Result.Builder.build()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/Result.java`) always allocate `new Result(...)`. Thus, explicit statuses, status messages, status details, and attached `Throwable` errors on [`Status.getError()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/Status.java`) are never canonicalized away.
- **Immutability, equality, hash, and identity** (`SUPPORTED`):
  - `Result` is `final`. All fields on a plain `Result` are immutable: `decision` (`Decision` enum), `status` ([`Status.java:39`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/Status.java`), final fields, `detail = null`, `error = null`, `code` is immutable `StatusCode`), `obligations = ImmutableMap.of()`, `associatedAdvice = ImmutableMap.of()`, `attributes = ImmutableList.of()`, `resolvedAttributes = ImmutableList.of()`, `policyIdentifiers = ImmutableList.of()`.
  - Every getter returns an `ImmutableCollection` or `ImmutableList` view (`ImmutableMap.values()`, `ImmutableList`), so callers cannot mutate a shared `Result`.
  - [`Result.hashCode()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/Result.java`) lazily caches a deterministic `int` (`h == 0` guard); concurrent reads/writes of a deterministic 32-bit `int` in Java are benign.
  - Repository-wide search (`rtk rg`) confirms no production or test caller relies on `Result` object identity (`!=` / `==`) to distinguish separate plain evaluations.

---

### 2. R6 and the Hash Fold ([`Attribute.java:135-140, 193-205`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/Attribute.java`))

- **Bag-indexed copy in `Attribute.Builder.values(Iterable)`** (`SUPPORTED`):
  - [`Attribute.java:193-205`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/Attribute.java`) checks `Preconditions.checkNotNull(values)` first, then uses `for (int i = 0; i < bag.size(); i++) valueBuilder.add(bag.valueAt(i))` when `values instanceof BagOfValues bag`.
  - [`BagOfValues.iterator()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java`) delegates to [`BagStorage.iterator()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/BagStorage.java`), which simply yields `valueAt(index++)` from `0` to `size() - 1`.
  - Therefore, the indexed loop adds the exact same `Value<?>` elements in the exact same grouped multiset order, preserving multiplicity, insertion order of distinct groups, `DoubleVal` `-0.0` vs. `0.0` (`Double.doubleToLongBits` in [`BagStorages.java:244-292`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/BagStorages.java`)), and `Double.NaN` without allocating the `BagStorage$1` iterator.
- **Unboxed polynomial `Attribute.hashCode()`** (`SUPPORTED`):
  - Previously, `Attribute.hashCode()` called `com.google.common.base.Objects.hashCode(attributeId, issuer, includeInResult, values)` (`Arrays.hashCode(new Object[]{attributeId, issuer, Boolean.valueOf(includeInResult), values})`).
  - Now ([`Attribute.java:135-140`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/Attribute.java`)):
    ```java
    int result = 31 + attributeId.hashCode();
    result = 31 * result + java.util.Objects.hashCode(issuer);
    result = 31 * result + Boolean.hashCode(includeInResult);
    return 31 * result + values.hashCode();
    ```
  - Because `attributeId` (`:55`) and `values` (`:59`, `ImmutableList`) are non-null while `issuer` may be `null`, this is algebraically and bit-for-bit identical to `Arrays.hashCode(new Object[]{attributeId, issuer, includeInResult, values})` across all inputs, without allocating `Object[4]`.
  - The hash is intentionally not cached on `Attribute` because `Value` is an extensible interface (custom `Value` implementations could have mutable state).

---

### 3. R3: Single-Key `ResolverCacheKey` ([`ResolverContext.java:149-166`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/ResolverContext.java`), [`ResolverCacheKey.java:79-82`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/ResolverCacheKey.java`))

- **Single declared key fast path** (`SUPPORTED`):
  - When `getDescriptor().getKeyRefs()` has `keys.size() == 1` and `resolvedKeys.get(keys.get(0))` returns a non-null `BagOfValues bag`, [`ResolverContext.Default.getCacheKey()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/ResolverContext.java`) calls `ResolverCacheKey.of(getDescriptor().getId(), bag)`.
  - This bypasses `resolvedKeys.values()` on Guava's `SingletonImmutableBiMap`, eliminating the `inverse()` `SingletonImmutableBiMap` and `SingletonImmutableSet` allocations on every single-key PIP resolution (`pip:8 independent` drops 240 B/decision).
- **Missing keys, retries, availability, and multi-key order** (`SUPPORTED`):
  - If `resolvedKeys == null || resolvedKeys.isEmpty()`, lines 152–154 still call `this.resolvedKeys = resolveAllContextRefs()`, preserving lazy resolution and D27's retry-when-all-keys-were-absent behavior.
  - If the single declared key resolved to `Optional.empty()`, `resolvedKeys` is `ImmutableMap.of()`, `resolvedKeys.get(keys.get(0))` returns `null`, and control falls through to `ResolverCacheKey.of(getDescriptor().getId(), resolvedKeys.values())`, which sees `keys.isEmpty()` and uses `NO_KEYS` (`BagOfValues[0]`), identical to before.
  - Multi-key descriptors (`keys.size() != 1`) still take `ResolverCacheKey.of(getDescriptor().getId(), resolvedKeys.values())` in declared `getKeyRefs()` order.
  - [`isCacheKeyAvailable()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/ResolverContext.java`) is untouched (`!getDescriptor().hasKeyRefs() || (resolvedKeys != null && !resolvedKeys.isEmpty())`), so [`DefaultPolicyInformationPoint.hasKey`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/DefaultPolicyInformationPoint.java`) never triggers premature key resolution.
- **Equality, hash, serialized form, D27 flights, and task-30 memo** (`SUPPORTED`):
  - [`ResolverCacheKey.of(String, BagOfValues)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/ResolverCacheKey.java`) constructs `new ResolverCacheKey(resolverId, new BagOfValues[]{Preconditions.checkNotNull(key)})`.
  - The backing fields (`String resolverId`, `BagOfValues[] keys`, `transient int hashCode = -1`), `serialVersionUID`, `equals` (`Arrays.deepEquals`), and `hashCode` (`Arrays.deepHashCode`) are unchanged and byte-identical to `ResolverCacheKey.of(resolverId, List.of(key))`.
  - Task-30's per-decision `DecisionSets` (`ConcurrentHashMap<ResolverCacheKey, AttributeSet>` in [`DefaultEvaluationContextHandler.java:361-386`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java`)), external `PolicyInformationPointCacheProvider` caches, and D27 flights match the exact same keys.

---

### 4. R4: Mixed-Literal Response Preparation ([`BaseDecisionRuleResponseExpression.java:75-107, 154-251`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/BaseDecisionRuleResponseExpression.java`)) & D48 Compatibility

- **Exact built-in literal reuse vs. custom expressions** (`SUPPORTED`):
  - [`prepare(AttributeAssignmentExpression)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/BaseDecisionRuleResponseExpression.java`) pre-evaluates an assignment only when `expression.getClass() == AttributeAssignmentExpression.class && isLiteral(expression.getExpression())`.
  - `isLiteral(Expression)` ([`BaseDecisionRuleResponseExpression.java:217-229`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/BaseDecisionRuleResponseExpression.java`)) requires either `LITERAL_TYPES.contains(exp.getClass())` or `exp.getClass() == BagOfValues.class` where every element's `.getClass()` is in `LITERAL_TYPES`.
  - Subclasses of `AttributeAssignmentExpression`, custom `Expression` / `ValueExp` implementations, and dynamic expressions (`AttributeDesignator`, `AttributeSelector`, `FunctionInvocation`, `VariableReference`) get `PreparedAssignment(expression, Optional.empty())` and are evaluated dynamically on each decision via `evaluateAttributeAssignment(context, exp)` ([`:168`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/BaseDecisionRuleResponseExpression.java`)).
- **Protected multimap, subclass dispatch, and §7.18 ordering** (`SUPPORTED`):
  - `protected final Multimap<String, AttributeAssignmentExpression> attributeExpressions` ([`:83, 102`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/BaseDecisionRuleResponseExpression.java`)) is preserved intact as an `ImmutableListMultimap`.
  - `assignments` ([`:84, 103-107`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/BaseDecisionRuleResponseExpression.java`)) iterates `this.attributeExpressions.values()` at construction time, so `assignments` is already ordered by first-seen `attributeId` and by insertion order within each `attributeId`, eliminating per-decision `Iterators$9` / `ArrayItr` allocations over the multimap view.
  - Subclasses of `BaseDecisionRuleResponseExpression`, `ObligationExpression`, or `AdviceExpression` that override `evaluateAttributeAssignments(EvaluationContext)` or `evaluateAttributeAssignment(EvaluationContext, AttributeAssignmentExpression)` continue to have their overrides called (`ExternalObligationExpressionCompatibilityTest` passes).
  - In `assign(...)` ([`:247-250`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/BaseDecisionRuleResponseExpression.java`)), dynamic `BagOfValues` results are now indexed via `bag.valueAt(i)` rather than `for (Value<?> val : bag)`, saving a `BagStorage` iterator on dynamic multi-valued obligations/advice as well.
- **Compatibility with D48 (`DecisionRuleResponse`)** (`SUPPORTED`):
  - Nothing in R4 conflicts with D48 ([`design.md:1985-2087`](`.ntm/worktrees/xacml4j--batch2/agyx_1/openspec/changes/optimize-pdp-evaluation/design.md`)); in fact, R4 complements D48 directly:
    1. `evaluateAttributeAssignments(context)` returns `ImmutableList<AttributeAssignment>` (`constantAssignments` when all assignments are literal, or `values.build()` when mixed/dynamic). When D48's `DecisionRuleResponse(String, Effect, Collection<AttributeAssignment>)` checks whether the input is already a deduplicated contiguous-key sequence, `ImmutableList.copyOf(assignments)` is a zero-copy reference reuse on that `ImmutableList<AttributeAssignment>`.
    2. Because `assignments` in `BaseDecisionRuleResponseExpression` was flattened from `attributeExpressions.values()` (`ImmutableListMultimap<String, AttributeAssignmentExpression>`), all `AttributeAssignment`s produced by `evaluateAttributeAssignments(context)` are *already* grouped into contiguous runs by first-seen `attributeId`. Thus, D48's fast-path scan will always succeed unless a single `attributeId` has duplicate `AttributeAssignment` values.

---

### 5. Tests ([`ResultTest.java`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/test/java/org/xacml4j/v30/policy/ResultTest.java`), [`AttributeTest.java`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/test/java/org/xacml4j/v30/AttributeTest.java`), [`ResolverContextTest.java`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/ResolverContextTest.java`), [`ObligationExpressionTest.java`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/test/java/org/xacml4j/v30/policy/ObligationExpressionTest.java`))

- [`ResultTest.testPlainResultFactoryReusesOnlyEmptyOkDecisions`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/test/java/org/xacml4j/v30/policy/ResultTest.java`) pins same-instance reuse across `Result.of(...)`, `Result.plain(...)`, and `ResponseContext.{PERMIT,DENY,NOT_APPLICABLE}.getOnlyResult()`, and verifies that explicit `Status.ok().build()`, `INDETERMINATE*`, and each of the five payload collections (`obligations`, `advice`, `includeInResult`, `resolvedAttributes`, `policyIdentifiers`) allocate a distinct `Result`.
- [`AttributeTest.testValuesFromBagPreservesOrderDuplicatesAndSpecialDoubles`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/test/java/org/xacml4j/v30/AttributeTest.java`) and [`AttributeTest.testHashCodeMatchesLegacyObjectArrayFormula`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/test/java/org/xacml4j/v30/AttributeTest.java`) pin `BagOfValues` copying (including duplicate strings, `-0.0`, `0.0`, `NaN`) and `Arrays.hashCode` equivalence with and without `issuer`.
- [`ResolverContextTest.testGetCacheKeySingleAndMissingKeysMatchCollectionKey`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/ResolverContextTest.java`) pins `isCacheKeyAvailable()` before/after resolution, equality and hash equivalence between `ResolverCacheKey.of(id, bag)` and `ResolverCacheKey.of(id, List.of(bag))`, and retry behavior when a single key is initially missing and then becomes available.
- [`ObligationExpressionTest.testMixedLiteralAndDynamicAssignmentsReuseLiteralOnly`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/test/java/org/xacml4j/v30/policy/ObligationExpressionTest.java`) and [`ObligationExpressionTest.testCustomValueExpAssignmentRemainsDynamic`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/test/java/org/xacml4j/v30/policy/ObligationExpressionTest.java`) pin same-instance reuse of literal scalar and bag `AttributeAssignment`s alongside a dynamic `AttributeDesignator`, first-seen attribute ID grouping, and dynamic re-evaluation of custom `ValueExp` implementations.

---

## Verification Summary

- **Code intelligence**:
  - `scripts/agent-doctor.sh`: `ast-grep` PASS, `ripwire` PASS (7/7), `serena` PASS, `tgrep` PASS (after `tgrep index .`).
  - `ast-grep scan`: exited 0 with 0 findings.
  - `rtk rg`: verified all call sites of `Result.of`, `ResponseContext.{PERMIT,DENY,NOT_APPLICABLE}`, `ResolverCacheKey.of`, and `evaluateAttributeAssignments` (`SUPPORTED`).
- **Tests executed**:
  - `JAVA_HOME="$(jenv prefix 25)" rtk mvn -o -q -Dlicense.skipUpdateLicense=true -pl xacml-pdp -am -Dtest=ResultTest,ResponseContextTest,AttributeTest,ResolverContextTest,ResolverCacheKeyTest,ObligationExpressionTest,AdviceExpressionTest,ExternalObligationExpressionCompatibilityTest,ExternalResponseBuilderCompatibilityTest -Dsurefire.failIfNoSpecifiedTests=false test` — PASS (exit 0).
- **Git status**: clean worktree (`PATCH-AY.diff` and `REPORT-*.md` untracked only; no production or test files modified).
