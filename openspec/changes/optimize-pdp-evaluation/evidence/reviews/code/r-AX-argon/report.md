# Review Report: R-AX-argon (Codex Astra's Cycle AX — Shared Configuration and Response Factories)

- **Reviewer**: Lane `agyx_1` (Gemini Argon)
- **Author**: Codex Astra (`cod_3`)
- **Verdict**: **APPROVE** (`0 P1`, `0 P2`, `0 P3`)

---

## Findings (P1–P3)

- **P1**: None.
- **P2**: None.
- **P3**: None.

---

## Detailed Verification by Check Point

### 1. The Configuration ([`RootEvaluationContext.Configuration`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`)) — `SUPPORTED` (source inspection + [`RootEvaluationContextTest`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/test/java/org/xacml4j/v30/policy/RootEvaluationContextTest.java`))

- **Equality, `hashCode`, and `toString` of a root are unchanged**:
  - [`RootEvaluationContext.toString()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`) formats `configuration.defaultXPathVersion`, `configuration.resolver.getClass().getSimpleName()`, and `configuration.clock` under the same keys (`"defaultXPathVersion"`, `"resolver"`, `"clock"`) and in the same order as before AX.
  - [`RootEvaluationContext.hashCode()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`) passes `configuration.defaultXPathVersion` and `configuration.resolver` to `Objects.hashCode(...)` in the identical slot positions as before.
  - [`RootEvaluationContext.equals(Object)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`) compares the underlying component values (`configuration.defaultXPathVersion`, `configuration.resolver`, and `configuration.clock`) rather than `Configuration` reference identity, so contexts constructed via legacy constructors (each creating its own `Configuration` at [`RootEvaluationContext.java:243`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`)) compare equal to contexts sharing a `Configuration`.
- **Per-decision state is not shared**:
  - `combinedDecisionCacheTTL` is a `volatile Duration` field on [`RootEvaluationContext`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`), initialized from `configuration.defaultDecisionCacheTTL` in the private constructor ([`RootEvaluationContext.java:271`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`)). Calls to [`setDecisionCacheTTL(Duration)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`) and [`clear()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`) only mutate the per-decision `combinedDecisionCacheTTL` field; `configuration.defaultDecisionCacheTTL` (`java.time.Duration`, immutable) is never modified.
  - `validateFuncParamsAtRuntime` ([`RootEvaluationContext.java:117, 272, 411`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`)) and `recordEvaluatedPolicies` ([`RootEvaluationContext.java:122, 273`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`)) remain instance fields on `RootEvaluationContext` (which also pack into the 3-byte alignment gap after the 12-byte object header and `boolean precheckedMatch`, costing 0 layout bytes while keeping `RootEvaluationContext` at 80 B).
  - `generation` ([`RootEvaluationContext.java:92, 260, 270`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`)) is captured per decision via `prepared.generation().held` when each `RootEvaluationContext` is constructed.
- **Thread safety of the shared object**:
  - [`RootEvaluationContext.Configuration`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`) has four `private final` fields (`clock`, `defaultDecisionCacheTTL`, `defaultXPathVersion`, `resolver`) and no mutators.
  - In [`DefaultPolicyDecisionPointContextFactory`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PolicyDecisionPointContextFactory.java`), `evaluationConfiguration` is `private final` and constructed once at [`PolicyDecisionPointContextFactory.java:133-134`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PolicyDecisionPointContextFactory.java`) with `Clock.systemUTC()`, `Duration.ofSeconds(30)`, `XPathVersion.XPATH1`, and the PDP's `DefaultPolicyReferenceResolver` (which was already shared across all decisions of the PDP).
- **Public shape, visibility, class vs. record, and naming**:
  - **Could `Configuration` be package-private?** No. [`RootEvaluationContext`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`) is in package `org.xacml4j.v30.policy` (`xacml-core`), whereas [`PolicyDecisionPointContextFactory`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PolicyDecisionPointContextFactory.java`) is in package `org.xacml4j.v30.pdp` (`xacml-pdp`). Cross-package and cross-module access requires `public` visibility on `Configuration` and the two `RootEvaluationContext(Configuration, ...)` constructors ([`RootEvaluationContext.java:248-261`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`)).
  - **Could `Configuration` be a `record`?** A `public record` would automatically expose four public component accessors (`clock()`, `defaultDecisionCacheTTL()`, `defaultXPathVersion()`, `resolver()`) and a public structural `equals`/`hashCode`/`toString` contract on `Configuration`. Making `Configuration` a `public static final class` with `private final` fields ([`RootEvaluationContext.java:127-141`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`)) keeps it an opaque immutable handle with the narrowest possible public API footprint.
  - **Naming**: `RootEvaluationContext.Configuration`, its constructor parameters (`clock`, `defaultDecisionCacheTTL`, `defaultXPathVersion`, `resolver`), and the factory field `evaluationConfiguration` ([`PolicyDecisionPointContextFactory.java:79`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PolicyDecisionPointContextFactory.java`)) are clear, fluent, and match the existing constructor parameter vocabulary.

---

### 2. The Response Factories ([`Obligation.of`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/Obligation.java`), [`Advice.of`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/Advice.java`), [`DecisionRuleResponse`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/DecisionRuleResponse.java`)) — `SUPPORTED` (source inspection + unit and compatibility tests)

- **XACML 3.0 §7.18, deduplication, grouping, and order**:
  - [`DecisionRuleResponse.attributes(ImmutableList<AttributeAssignment>)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/DecisionRuleResponse.java`) returns `ImmutableSetMultimap.of()` for empty input (`:72-74`), `ImmutableSetMultimap.of(assignment.getAttributeId(), assignment)` for singleton input (`:75-78`), and populates `ImmutableSetMultimap.builder()` in list order for 2+ assignments (`:79-84`).
  - Guava's `ImmutableSetMultimap.Builder` preserves first-seen key (`attributeId`) insertion order and, within each key's value set, preserves first-seen distinct `AttributeAssignment` insertion order while dropping duplicates according to `AttributeAssignment.equals`/`hashCode` — producing the exact same `ImmutableSetMultimap` representation and iteration order as `ImmutableSetMultimap.copyOf(LinkedHashMultimap)` in the builder path ([`DecisionRuleResponse.java:52, 141`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/DecisionRuleResponse.java`)).
  - [`DecisionRuleResponse.hashCode`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/DecisionRuleResponse.java`) computes `31 * (31 + java.util.Objects.hashCode(id)) + attributes.hashCode()`, which is algebraically identical to `com.google.common.base.Objects.hashCode(id, attributes)` without allocating an `Object[2]` varargs array.
- **External builder and expression subclasses (R-AN compatibility)**:
  - [`DecisionRuleResponse.java:38-39`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/DecisionRuleResponse.java`) retains `protected final String id` and `protected final Multimap<String, AttributeAssignment> attributes`, and [`DecisionRuleResponse.Builder`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/DecisionRuleResponse.java`) retains `protected String id`, `protected Effect fullFillOn`, and `protected Multimap<String, AttributeAssignment> attributes = LinkedHashMultimap.create()`. [`ExternalResponseBuilderCompatibilityTest`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/test/java/org/xacml4j/v30/policy/external/ExternalResponseBuilderCompatibilityTest.java`) passes.
  - [`ObligationExpression`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/ObligationExpression.java`) and [`AdviceExpression`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/AdviceExpression.java`) gate `constant` on `getClass() == ObligationExpression.class` / `AdviceExpression.class` and call virtual `getId()`, `getEffect()`, and `evaluateAttributeAssignments(context)` when `constant` is empty. [`ExternalObligationExpressionCompatibilityTest`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/test/java/org/xacml4j/v30/policy/external/ExternalObligationExpressionCompatibilityTest.java`) passes.
- **Detached caller collection**:
  - [`DecisionRuleResponse(String, Effect, Collection<AttributeAssignment>)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/DecisionRuleResponse.java`) calls `ImmutableList.copyOf(assignments)` and converts the result into an `ImmutableSetMultimap`. On the engine path ([`BaseDecisionRuleResponseExpression.evaluateAttributeAssignments`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/BaseDecisionRuleResponseExpression.java`)), `assignments` is already an `ImmutableList`, so `ImmutableList.copyOf` is an O(1) non-allocating pass-through; for external callers passing a mutable `Collection`, `ImmutableList.copyOf` validates non-null elements and detaches the response from subsequent mutations of the caller's collection.

---

### 3. [`AttributeAssignment.hashCode()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/AttributeAssignment.java`) — `SUPPORTED` (source inspection + [`AttributeAssignmentTest`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/test/java/org/xacml4j/v30/policy/AttributeAssignmentTest.java`))

- In [`AttributeAssignment.java:54-59`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/AttributeAssignment.java`), `attributeId` and `attribute` (`b.value`) are non-null `final` fields, while `category` and `issuer` are nullable `final` fields.
- The four-field polynomial fold at [`AttributeAssignment.java:108-113`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/AttributeAssignment.java`):
  ```java
  int hash = 31 + attributeId.hashCode();
  hash = 31 * hash + java.util.Objects.hashCode(category);
  hash = 31 * hash + attribute.hashCode();
  return 31 * hash + java.util.Objects.hashCode(issuer);
  ```
  computes the exact same `int` as `Arrays.hashCode(new Object[]{attributeId, category, attribute, issuer})` (which `com.google.common.base.Objects.hashCode(attributeId, category, attribute, issuer)` delegated to) for every combination of null and non-null `category` and `issuer`, without allocating `Object[4]`.
- Not caching the hash in a field on `AttributeAssignment` is also the right choice: `attribute` has the extensible interface type `Value`, and custom `Value` implementations (including subclasses of `OtherValue`, whose `protected Object value` field is mutable) need not have immutable hash inputs.

---

### 4. Tests and [`ResponseFactoryTest`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/test/java/org/xacml4j/v30/ResponseFactoryTest.java`) — `SUPPORTED` (source inspection + Maven test execution)

- **Do the tests pin outcomes?**
  - [`AdviceExpressionTest.evaluatedAssignmentsKeepDistinctValuesInAttributeOrder`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/test/java/org/xacml4j/v30/policy/AdviceExpressionTest.java`) and [`ObligationExpressionTest.literalAndEvaluatedAssignmentsKeepTheirContentAndOrder`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/test/java/org/xacml4j/v30/policy/ObligationExpressionTest.java`) pin evaluated `Advice` and `Obligation` attribute IDs and values when expressions produce duplicate values and interleaved attribute IDs.
  - [`AttributeAssignmentTest.hashKeepsTheFieldOrderWithAndWithoutOptionalFields`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/test/java/org/xacml4j/v30/policy/AttributeAssignmentTest.java`) pins `equals` and the 4-field `Arrays.hashCode` contract across `{null, CategoryId.ACTION} × {null, "issuer"}`.
  - [`RootEvaluationContextTest.contextConfigurationKeepsDecisionTTLsIndependent`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/test/java/org/xacml4j/v30/policy/RootEvaluationContextTest.java`) pins `equals`, `hashCode`, `toString`, `getClock`, `getXPathVersion`, and per-decision TTL isolation across shared-configuration and legacy-constructed root contexts.
- **Is `ResponseFactoryTest` comparing factories with builders an outcome or a mechanism?**
  - It is an **outcome** test (cross-API value equivalence), reinforced by direct concrete outcome assertions:
    1. Both `Obligation.builder(...).attributes(...).build()` and `Obligation.of(...)` (and `Advice.builder` / `Advice.of`) are public construction APIs for value objects (`Obligation` and `Advice`). Verifying that both public entry points produce equal value objects (`equals`, `hashCode`, `toString`) with identical query results (`getFulfillOn()`, `getAttributes()`, `getAttribute("a")`) pins the public API contract that `of(...)` is a behavioral drop-in equivalent to `builder(...).attributes(...).build()`.
    2. In addition to builder equivalence, [`ResponseFactoryTest.java:56, 62, 65-67, 70-71`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/test/java/org/xacml4j/v30/ResponseFactoryTest.java`) directly asserts concrete outcomes independent of the builder: caller-list detachment (`input.clear()` at `:56`), effect preservation (`:62`), empty lookup for `"missing"` (`:65`), unmodifiable `getAttributes()` view (`:66`), and the exact deduplicated, attribute-grouped sequence `List.of(a, otherValue, otherCategory, otherIssuer, b)` (`:70-71`).
    3. It does not inspect private fields, internal multimap types, or allocation counts.

---

## Verification Executed

1. `JAVA_HOME="$(jenv prefix 25)" rtk mvn -o -q -Dlicense.skipUpdateLicense=true -pl xacml-pdp -am -Dtest=ObligationExpressionTest,AdviceExpressionTest,ObligationTest,AdviceTest,AttributeAssignmentTest,ResponseFactoryTest,ExternalResponseBuilderCompatibilityTest,ExternalObligationExpressionCompatibilityTest,RootEvaluationContextTest,RootEvaluationContextCharacterizationTest,ContextChainQueryCharacterizationTest,NestedResponseStagingTest,NestedResponseStagingPdpTest,SingleRequestPathPdpTest,PreparedPolicySetsPdpTest -Dsurefire.failIfNoSpecifiedTests=false test` — **PASS** (`SUPPORTED`, exit code `0`).
2. `ast-grep scan` on all 8 modified production Java files — **PASS** (`SUPPORTED`, exit code `0`, `0` diagnostics).
