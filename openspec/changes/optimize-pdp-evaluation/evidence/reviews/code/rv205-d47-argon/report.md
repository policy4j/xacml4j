# Code Review Report — `rv205-d47-argon`

- **Change**: `optimize-pdp-evaluation`
- **Design / Task**: D47 (`42.14`)
- **Beads**: `policy4j-cwtu.42.14`, `policy4j-cwtu.42.14.1`
- **Base commit**: `759d90a6c75510d55127d8e22870f9edd985c991` (`feature_xacml_20`)
- **Reviewer**: `agyx_1` (Agy Gemini 4 Argon High, independent adversarial reviewer)
- **Date**: 2026-10-31

---

## Verdicts by Candidate

| Candidate | Author / Worktree | Files | Verdict | Blocking Findings |
| --- | --- | --- | --- | --- |
| **Candidate 1 (Terra)** | GPT-5.6-codex high (`W/cod_1`) | `Rule.java`, `RulesInPolicyContextPdpTest.java` | **REJECT** | **2 P2** soundness gaps in `Rule.EngineOnly.provenance` |
| **Candidate 2 (Sol)** | Claude Opus 4.7 xhigh (`W/cod_2`) | `ResponseWriters.java`, `Policy.java`, `PolicySet.java`, `NestedResponseStagingPdpTest.java` | **APPROVE** | 0 P0 / 0 P1 / 0 P2 |

---

## Candidate File Verification (SHA-256)

All six candidate files in `W/cod_1` and `W/cod_2` were read and hashed against the expected SHA-256 digests (`SUPPORTED`):

1. **Candidate 1 — Terra (`W/cod_1`)**
   - `xacml-core/src/main/java/org/xacml4j/v30/policy/Rule.java`:
     `717be6326fecf2702afad2818a4a1f2a37fbcdfc3dcf052977e6effb8fd0c094` (match)
   - `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/RulesInPolicyContextPdpTest.java`:
     `ea5410ab8431c02a90dfb4b229743eeb2306680c92f938bbace5e2aec3b8737c` (match)
2. **Candidate 2 — Sol (`W/cod_2`)**
   - `xacml-core/src/main/java/org/xacml4j/v30/policy/ResponseWriters.java`:
     `1780fa60f86fa433d9fef565ab1ca76a69879c4e7ac1455098096ae2feec4042` (match)
   - `xacml-core/src/main/java/org/xacml4j/v30/policy/Policy.java`:
     `8b7afdbb6cfbed64489a4994e3a691c60104aadd0422ce281783f7c78d287953` (match)
   - `xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySet.java`:
     `02e5e984a495c1021939a971694381c6ad17eb0c3bb41044d921c75f254cabda` (match)
   - `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/NestedResponseStagingPdpTest.java`:
     `fe3380aed3aea8e95b72975a15f0af3fc4bb4561c3a3f48fe1a2d4e3612b4219` (match)

---

## Candidate 1 Review: Terra (`W/cod_1`) — **REJECT**

### Blocking Findings

#### [P2-1] `Rule.EngineOnly.provenance` fails to mark built-in `attribute-designator` and `attribute-selector` function applications as `RUNTIME_VALUE`

- **Location**:
  - `W/cod_1/xacml-core/src/main/java/org/xacml4j/v30/policy/Rule.java:315-317, 335-347`
  - Supporting engine files:
    - `xacml-core/src/main/java/org/xacml4j/v30/policy/function/XacmlDefaultFunctions.java:66`
    - `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/AttributeDesignatorFunctions.java:46-145`
    - `xacml-core/src/main/java/org/xacml4j/v30/policy/FunctionSpecBuilder.java:309-312`
- **Mechanism**:
  1. `Rule.EngineOnly.provenance` (`Rule.java:315-317`) marks only `AttributeDesignator.class` and `AttributeSelector.class` AST nodes as `RUNTIME_VALUE`.
  2. For `Apply.class` (`Rule.java:335-347`), if `FunctionSpecBuilder.usesBuiltInInvocation(apply.function())` returns `true`, `provenance` ORs the provenance of `apply.getArguments()` and returns `flags & RUNTIME_VALUE` (`:346`).
  3. However, `XacmlDefaultFunctions.PROVIDERS` (`XacmlDefaultFunctions.java:66`) includes `AttributeDesignatorFunctions.class`, which provides two built-in XACML 3.0 functions with default invocation:
     - `urn:oasis:names:tc:xacml:3.0:function:attribute-designator` (`AttributeDesignatorFunctions.designator`, `AttributeDesignatorFunctions.java:46-84`), which resolves a request `Entity` via `context.resolveEntity(category)` (`:62`) or uses an `Entity` argument (`:67`) and resolves an attribute bag via `entity.resolve(key.build())` (`:75`).
     - `urn:oasis:names:tc:xacml:3.0:function:attribute-selector` (`AttributeDesignatorFunctions.selector`, `AttributeDesignatorFunctions.java:87-123`), which resolves an `AttributeSelectorKey` from the request/context via `context.resolve(selectorKey)` (`:114`) or `entity.resolve(selectorKey)` (`:110`).
  4. Because `AttributeDesignatorFunctions` uses the default built-in `FunctionInvocation`, `FunctionSpecBuilder.usesBuiltInInvocation(apply.function())` (`FunctionSpecBuilder.java:309-312`) returns `true`.
  5. When `Apply(attribute-designator, AnyURI.of(CategoryId.SUBJECT_ACCESS.getId()), AnyURI.of("urn:example:runtime"), AnyURI.of(XacmlTypes.STRING.getTypeId()), BooleanVal.of(true))` is constructed with engine literal arguments:
     - `AttributeDesignatorFunctions.resolve` (`AttributeDesignatorFunctions.java:140-145`) statically resolves the return type to `XacmlTypes.STRING.bagType()` (because `params.get(2)` is an `AnyURI` literal), so `Apply.withTypeAssertions` (`Apply.java:121-143`) wraps no `TypeAssertion`.
     - Every argument in `apply.getArguments()` is a built-in `Value<?>` in `org.xacml4j.v30.types` with provenance `0` (`Rule.java:318-320`).
     - Therefore, `provenance(Apply(attribute-designator, ...))` returns `0 & RUNTIME_VALUE == 0` instead of `RUNTIME_VALUE`!
  6. When that `Apply(attribute-designator, ...)` is passed as the bag argument to a higher-order built-in function such as `Apply(any-of, new FunctionReference(string-equal), StringVal.of("policy"), Apply(attribute-designator, ...))`, the outer `Apply` sees `flags = FUNCTION_REFERENCE | 0 | 0 = FUNCTION_REFERENCE`, does not trigger `(flags & (RUNTIME_VALUE | FUNCTION_REFERENCE)) == (RUNTIME_VALUE | FUNCTION_REFERENCE)` (`Rule.java:344-345`), and returns `0`. Thus `Rule.inPolicyContext` is `true`.
- **Reachable Failure Scenario**:
  - Using `RulesInPolicyContextPdpTest`'s own `ParentPolicyValue` (`RulesInPolicyContextPdpTest.java:537-571`, which returns `StringVal.of(context.getParentContext().getCurrentPolicy().getId())` when `context.getParentContext().getCurrentPolicy() != null` and `StringVal.of("none")` otherwise) and `requestWithRuntimeValue()` (`:511-525`):
  - Construct a `Policy` (`id = "policy"`, `first-applicable`) with one `PERMIT` `Rule` whose condition is:
    ```java
    Apply.builder(function(XacmlHigherOrderFunctions.ANY_OF))
        .param(new FunctionReference(function(XacmlStringFunctions.STRING_EQUAL)))
        .param(StringVal.of("policy"))
        .param(Apply.builder(function("urn:oasis:names:tc:xacml:3.0:function:attribute-designator"))
            .param(AnyURI.of(CategoryId.SUBJECT_ACCESS.getId()))
            .param(AnyURI.of("urn:example:runtime"))
            .param(AnyURI.of(XacmlTypes.STRING.getTypeId()))
            .param(BooleanVal.of(true))
            .build())
        .build()
    ```
  - **Observed (`observed = true`)**: `PolicyDescendantEvaluationContext.enterRule` returns `false` (`Policy.java:401`); the rule runs in a `RuleEvaluationContext` whose `getParentContext()` is `PolicyDescendantEvaluationContext("policy")`; `HigherOrderFunctions.anyOf` invokes `FunctionSpec.invoke(context, StringVal.of("policy"), ParentPolicyValue)`, which calls `ParentPolicyValue.evaluate(context)` -> `StringVal.of("policy")`; decision is **`Decision.PERMIT`**.
  - **Unobserved (`observed = false`)**: `Rule.inPolicyContext` is `true`, so `enterRule` borrows `PolicyDescendantEvaluationContext("policy")` whose `getParentContext()` is `RootEvaluationContext` (`getCurrentPolicy() == null`); `ParentPolicyValue.evaluate(context)` returns `StringVal.of("none")`; decision is **`Decision.NOT_APPLICABLE`**.
  - Observed and unobserved decisions diverge (`PERMIT` vs `NOT_APPLICABLE`).

---

#### [P2-2] `Rule.EngineOnly.provenance` treats `Entity` literals as engine-only (`0`) without inspecting their nested `Attribute` bags for custom `Value` implementations

- **Location**:
  - `W/cod_1/xacml-core/src/main/java/org/xacml4j/v30/policy/Rule.java:318-329`
  - Supporting engine files:
    - `xacml-core/src/main/java/org/xacml4j/v30/types/Entity.java:43-83, 213-235`
    - `xacml-core/src/main/java/org/xacml4j/v30/Attribute.java:44-90`
- **Mechanism**:
  1. In `Rule.EngineOnly.provenance` (`Rule.java:318-320`):
     ```java
     if(e instanceof Value<?>){
         return type.getPackage() == Value.class.getPackage() ? 0 : UNSAFE;
     }
     ```
  2. `org.xacml4j.v30.types.Entity` (`Entity.java:43`) is `public final class Entity extends Value.SelfValue<Entity>` in `Value.class.getPackage()`.
  3. Unlike primitive `Value` classes in `org.xacml4j.v30.types`, an `Entity` is a container that holds `Attribute`s (`Entity.attributes()`, `Entity.java:213-215`), and each `Attribute` holds a `BagOfValues` of `Value<?>` instances (`Attribute.getValues()`, `Entity.resolve(AttributeDesignatorKey)` at `Entity.java:225-235`).
  4. While `Rule.EngineOnly.provenance` inspects the elements of a `BagOfValues` (`Rule.java:321-329`), it checks `if(e instanceof Value<?>)` first (`:318`) and returns `0` for every `Entity` instance without inspecting its attributes or nested values.
  5. Consequently, an `Entity` literal in a policy expression (or in a `BagOfValues` of `Entity` values) can hold a custom `Value` (such as `new ParentPolicyValue()`) inside an attribute bag, and `provenance` returns `0`.
- **Reachable Failure Scenario**:
  - Build a literal `Entity` containing `Attribute.builder("urn:example:runtime").value(new ParentPolicyValue()).build()`.
  - In a `Rule` condition, extract that attribute bag via `Apply(attribute-designator, entityLiteral, AnyURI.of("urn:example:runtime"), AnyURI.of(XacmlTypes.STRING.getTypeId()), BooleanVal.of(true))` and pass it to `Apply(any-of, new FunctionReference(string-equal), StringVal.of("policy"), ...)`.
  - Both `provenance(entityLiteral)` and `provenance(Apply(attribute-designator, ...))` return `0`, so `Rule.inPolicyContext` is `true` even if `[P2-1]` were fixed only for category-URI `attribute-designator` calls.
  - Observed evaluation yields **`Decision.PERMIT`**; unobserved evaluation borrows the policy context and yields **`Decision.NOT_APPLICABLE`**.

---

### Answers to the Three Focus Questions

#### Question 1: Does `Rule.EngineOnly` close `r-D47-sol` P2 on every path by which a request or PIP value (or custom value) reaches code that receives the rule context?

- **No** — because of **[P2-1]** (`AttributeDesignatorFunctions` built-in `attribute-designator` and `attribute-selector` functions) and **[P2-2]** (`Entity` container literals holding custom `Value` instances in their attributes).
- **What about a first-order built-in `Apply` over a runtime `AttributeDesignator`/`AttributeSelector` (or a `VariableReference` to one)?**
  - Tracing the invocation pipeline proves that a **first-order** built-in `Apply` (one with no `FunctionReference` argument) **cannot** invoke `Value.evaluate(context)` on a runtime `Value` inside a resolved `BagOfValues`:
    1. When `Apply.evaluate(context)` (`Apply.java:279-295`) invokes `FunctionSpecImpl.invokeWithList` (`FunctionSpecBuilder.java:583-597`) -> `invokeDirect` (`:655-683`), `invokeFast` (`:604-621`), or `doInvoke` (`:715-719, 920-945`), each AST argument `Expression` in `apply.getArguments()` is evaluated **once** via `arg.evaluate(context)`.
    2. If `arg` is an `AttributeDesignator` or `AttributeSelector`, `AttributeReference.evaluate(context)` (`AttributeReference.java:105-116`) resolves and returns a `BagOfValues` without calling `evaluate(context)` on the bag's elements.
    3. If `arg` is `Apply(type-one-and-only, designator)` (or a `VariableReference` to a `VariableDefinition` holding `Apply(type-one-and-only, designator)`), evaluating `arg.evaluate(context)` invokes `BagFunctions.oneAndOnlyImpl(bag)` (`BagFunctions.java:79-88`), which returns `bag.value()` **without calling `value.evaluate(context)`**.
    4. Furthermore, 14 of the 15 `*-one-and-only` methods in `BagFunctions.java` declare `final` concrete return types in `org.xacml4j.v30.types` (e.g., `StringVal`, `IntegerVal`, `BooleanVal`), so any custom `Value.OtherValue` in the bag fails the bytecode return check with `ClassCastException` -> `FunctionInvocationException` -> `INDETERMINATE` (as tested in `RulesInPolicyContextPdpTest.java:196-206`).
    5. Even for the one non-final return type, `BagFunctions.ipAddressOneAndOnly` (`BagFunctions.java:700-705`, returning `IPAddress`, which is `public class IPAddress extends Value.SelfValue<IPAddress>` at `IPAddress.java:40`), evaluating the inner `Apply(ipAddress-one-and-only, designator)` (or `VariableReference` -> `VariableDefinition.evaluate(context)` at `VariableDefinition.java:115-126`) **is itself** the single `arg.evaluate(context)` call performed by the outer `Apply`. The outer `Apply` passes the returned `ValueExp` (`value0`) directly to `direct.invokeDirect` / `fast.invokeFast` / `DefaultFunctionInvocation.invoke` (`DefaultFunctionInvocation.java:207-228, 487-551`) **without calling `value0.evaluate(context)` a second time**.
    6. In the entire built-in function library, `value.evaluate(context)` is called on an element extracted from a `BagOfValues` **only** when that `Value<?>` is passed as an `Expression` parameter to a nested `FunctionSpec.invoke(context, ...)` or `BoundCall.invoke(context, ...)` (`FunctionSpecBuilder.java:507, 611-614, 661-662, 677, 924, 942`). Only `Match.match` (`Match.java:131-132`) and `HigherOrderFunctions` (`HigherOrderFunctions.java:78-79, 114, 201, 227, 276-277, 317, 344, 365-366, 377`; `DecidingOperands.java:202-204`), which require a `FunctionReference` argument, do this.
- **Minimal Fix for `[P2-1]` and `[P2-2]`**:
  1. For `[P2-2]`: In `Rule.EngineOnly.provenance(Expression, Map)` (`Rule.java:318-320`), before returning `0` for `Value<?>`, check `if(e instanceof Entity entity)` and inspect every `Attribute` and `Value<?>` inside `entity.attributes()` (or treat `Entity` with any attributes as `UNSAFE`):
     ```java
     if(e instanceof Entity entity){
         for(Attribute attr : entity.attributes()){
             for(Value<?> v : attr.getValues()){
                 if(provenance(v, walked) != 0) return UNSAFE;
             }
         }
         return 0;
     }
     ```
  2. For `[P2-1]`: Mark built-in `Apply` nodes whose function resolves runtime attributes (`AttributeDesignatorFunctions`: `urn:oasis:names:tc:xacml:3.0:function:attribute-designator` and `urn:oasis:names:tc:xacml:3.0:function:attribute-selector`, or via a helper on `FunctionSpecBuilder` checking `AttributeDesignatorFunctions`) as producing `RUNTIME_VALUE` in `Rule.EngineOnly.provenance` (`Rule.java:346`):
     ```java
     int produced = FunctionSpecBuilder.resolvesRuntimeAttributes(apply.function()) ? RUNTIME_VALUE : 0;
     return (flags & RUNTIME_VALUE) | produced;
     ```

---

#### Question 2: Are the provenance flags (`UNSAFE`, `RUNTIME_VALUE`, `FUNCTION_REFERENCE`) sound across a diamond of `VariableDefinition`s, a cycle of `VariableDefinition`s, a `FunctionReference` held by a `VariableDefinition`, and a literal bag?

- **Diamond of `VariableDefinition`s (`SUPPORTED` — sound)**:
  - `Rule.EngineOnly.provenance` (`Rule.java:356-362`) memoizes the full integer bitmask `result` per `VariableDefinition` identity in `IdentityHashMap<VariableDefinition, Integer> walked` (`walked.put(definition, result)` at `:361`).
  - When a shared `VariableDefinition` is reached along multiple edges of a DAG, subsequent visits read `cached != null` (`:357`) and return the exact bitmask (`0`, `UNSAFE`, `RUNTIME_VALUE`, `FUNCTION_REFERENCE`, or `RUNTIME_VALUE | FUNCTION_REFERENCE`) in $O(1)$ time without losing any provenance bit.
- **Cycle of `VariableDefinition`s (`SUPPORTED` — sound)**:
  - Before recursing into `definition.getExpression()`, line 359 records `walked.put(definition, UNSAFE)`.
  - Any cyclic back-edge to an active `VariableDefinition` finds `cached == UNSAFE` (`1`), returns `UNSAFE` immediately, and causes every enclosing `Apply` (`Rule.java:341`) and `expression(Expression)` (`Rule.java:305`) to evaluate `(flags & UNSAFE) != 0` and reject the rule (`inPolicyContext = false`).
- **`FunctionReference` held by a `VariableDefinition` (`SUPPORTED` — sound)**:
  - Although XACML XML schema `VariableDefinition` children are `ExpressionType` (which includes `Function`), if a `VariableDefinition` `v` holds `new FunctionReference(spec)`:
    - If `spec` is not a built-in function (`!FunctionSpecBuilder.usesBuiltInInvocation(spec)`), `Rule.java:332` returns `UNSAFE`.
    - If `spec` is a built-in function, `Rule.java:333` returns `FUNCTION_REFERENCE` (`4`), which is stored in `walked` for `v` and returned by every `VariableReference` pointing to `v`.
    - If that `VariableReference` is passed to an `Apply` alongside any argument with `RUNTIME_VALUE` (`2`), `flags` in the `Apply` loop (`Rule.java:339-345`) has `(flags & (RUNTIME_VALUE | FUNCTION_REFERENCE)) == (RUNTIME_VALUE | FUNCTION_REFERENCE)` and returns `UNSAFE`.
    - Conversely, if `FunctionReference` was already consumed as an operand of an inner built-in `Apply` inside `v` (where no sibling operand had `RUNTIME_VALUE`), that inner `Apply` returns a `ValueExp` (never a `FunctionReference`), so `Rule.java:346` (`return flags & RUNTIME_VALUE;`) soundly strips `FUNCTION_REFERENCE` while preserving `RUNTIME_VALUE`.
- **Literal bag (`BagOfValues`) (`PARTIAL` — sound for non-`Entity` values; unsound for `Entity` elements per `[P2-2]`)**:
  - `Rule.java:321-329` iterates `0 .. bag.size() - 1` and ORs `provenance(bag.valueAt(i), walked)`.
  - If a literal `BagOfValues` contains a direct custom `Value` (`type.getPackage() != Value.class.getPackage()`), `provenance(bag.valueAt(i), walked)` returns `UNSAFE` (`1`), which propagates out of `BagOfValues` and rejects any enclosing `Apply` even when no `RUNTIME_VALUE` is present (necessary because a higher-order function over a literal bag calls `value.evaluate(context)` on its elements).
  - However, if a literal `BagOfValues` contains an `Entity` whose attributes contain a custom `Value`, `provenance(entity)` currently returns `0` (`[P2-2]`).

---

#### Question 3: Is excluding every rule with a `Match` in its target necessary? If a narrower proof exists that resolves no PIP at construction and changes no public API, state it precisely; if not, explain why.

1. **Why excluding every `Match` is necessary if only `Rule.java` is changed (`Policy.enterRule` unconditional on precheck result)**:
   - In `Match.match(EvaluationContext context)` (`Match.java:120-137`), if `attributes` is resolved via a PIP or if `predicate.scan(context, value, attributes)` returns `BagStorage.Scan.NONE` (which happens whenever a request bag contains a custom `Value` or uses a predicate without a direct bag scan), the fallback loop at `Match.java:130-132` calls:
     ```java
     Value match = (bound != null) ? bound.invoke(context, attributes.valueAt(i))
             : predicate.invoke(context, value, attributes.valueAt(i));
     ```
     Both `bound.invoke` (`FunctionSpecBuilder.java:507`) and `predicate.invoke` (`FunctionSpecBuilder.java:662`) call `attributes.valueAt(i).evaluate(context)`.
   - At `Rule` construction time, without resolving PIPs or inspecting a `RequestContext`, a static check on `Rule` alone cannot know whether the attribute bag resolved for a `Match`'s `AttributeDesignator`/`AttributeSelector` at decision time will come from a PIP or contain a custom `Value`.
   - Because `Policy.PolicyDescendantEvaluationContext.enterRule(DecisionRule child)` (`Policy.java:397-405`) currently checks only `rule.inPolicyContext`, setting `inPolicyContext = true` on a rule with a `Match` allows `Rule.evaluateRule` to call `isMatch(context)` -> `target.match(context)` -> `Match.match(context)` in the borrowed policy context whenever `TargetBeforeContext.precheck` returns `UNKNOWN`.
2. **A narrower package-private proof DOES exist across `Rule` and `TargetBeforeContext` (resolves no PIP at construction and changes no public API)**:
   - Every built-in rule combining algorithm enters a rule exclusively through `TargetBeforeContext.evaluateIfMatch(EvaluationContext context, DecisionRule child)` (`TargetBeforeContext.java:80-94`):
     ```java
     public static Decision evaluateIfMatch(EvaluationContext context, DecisionRule child){
         Optional<MatchResult> known = precheck(context, child);
         if(known.isPresent() && known.get() == MatchResult.NOMATCH){
             return root(context).evaluatePrechecked(context, child, known.get());
         }
         // After the precheck, which leaves an active rule to its own evaluation
         if(context instanceof Policy.PolicyDescendantEvaluationContext policy && policy.enterRule(child)){
             try{
                 return evaluate(context, child, known);
             }finally{
                 policy.exitRule();
             }
         }
         return evaluate(context, child, known);
     }
     ```
   - Notice what `TargetBeforeContext.evaluateIfMatch` already computes at line 81 **before** `policy.enterRule(child)` at line 86:
     - When `known` is `MatchResult.MATCH` (`known.isPresent()` at line 86, since `NOMATCH` already returned at line 83), `TargetBeforeContext.precheck` (`TargetBeforeContext.java:191-238`) has **already** matched the rule's entire `Target` from the request entity via `match.predicate().scan(context, match.getAttributeValue(), bag.get())` (`:226-228`), which requires `scan != BagStorage.Scan.NONE` and never invokes `Value.evaluate(context)` on bag elements.
     - Then `evaluate(context, child, known)` (`TargetBeforeContext.java:96-99`) calls `root(context).evaluatePrechecked(context, child, MatchResult.MATCH)` (`RootEvaluationContext.java:618-629`), which records `prechecked = child; precheckedMatch = true`.
     - When `Rule.evaluateRule(context)` calls `isMatch(context)` (`BaseDecisionRule.java:210-221`), line 217-218 executes:
       ```java
       Optional<MatchResult> prechecked = TargetBeforeContext.takePrechecked(context, this);
       MatchResult r = prechecked.isPresent() ? prechecked.get() : match(context);
       ```
       Because `prechecked` is `Optional.of(MatchResult.MATCH)`, **`match(context)` (`target.match(context)`) is never called during `Rule.evaluateRule(context)`**!
   - **Precise narrower condition (no PIP at construction, no public API change)**:
     - At `Rule` construction time, split target eligibility from body (`condition` / `obligationExpressions` / `adviceExpressions`) eligibility, or allow `Rule.EngineOnly.target(target)` when `target` is structural-only (`null` or exact `Target`/`MatchAnyOf`/`MatchAllOf`/`Match`), recording whether the target contains any `Match` (`hasTargetMatch`).
     - At decision time in `TargetBeforeContext.evaluateIfMatch` (`TargetBeforeContext.java:86`), allow `policy.enterRule(child)` when `rule.inPolicyContext` holds **and** either `!rule.hasTargetMatch` or `known.isPresent()` (i.e. `known` is `MatchResult.MATCH`). When `rule.hasTargetMatch && known.isEmpty()` (`precheck` returned `UNKNOWN`, e.g., because the attribute needs PIP resolution, the bag contains a custom `Value` where `scan` returned `Scan.NONE`, or runtime parameter validation is enabled), skip `policy.enterRule(child)` so the rule creates its own `RuleEvaluationContext`.
     - Note: because Candidate 1 (`cod_1`) is scoped to `Rule.java` and `RulesInPolicyContextPdpTest.java` without editing `TargetBeforeContext.java` or `Policy.java`, excluding rules with a `Match` in `Rule.EngineOnly.target` is required if `TargetBeforeContext.java` is not touched; however, excluding every rule with a target `Match` disables D47 for all rules that have standard target `Match`es in production policies unless `TargetBeforeContext.evaluateIfMatch` gates on `known.isPresent()`.

---

## Candidate 2 Review: Sol (`W/cod_2`) — **APPROVE**

### Source Verification

1. **`ResponseWriters.java` (`W/cod_2/xacml-core/src/main/java/org/xacml4j/v30/policy/ResponseWriters.java:40-128`)**:
   - Replaces static stateless recursion with a short-lived per-walk `ResponseWriters` instance allocated in `canWrite(Target)` (`:54`) and `canWrite(Expression)` (`:73`), holding a lazily allocated identity set `Set<VariableDefinition> variables = Collections.newSetFromMap(new IdentityHashMap<>())` (`:42, 102-104`).
   - Preserves fail-safe rejection of custom subclasses:
     - `reference.getClass() == VariableReference.class` (`:96`) and `definition.getClass() != VariableDefinition.class` (`:99`, returning `true` before touching `variables`).
   - Soundness for diamonds and cycles (`:106`, `return variables.add(definition) && writes(definition.getExpression());`):
     - Because `writes(Expression)` is a pure existential search (`canWrite` returns `true` on the first reachable response-writing node and aborts the entire walk immediately), any `VariableDefinition` whose subtree returns `false` is proven response-silent, and any `VariableDefinition` currently on the recursion stack that is re-entered via a cycle adds no new non-cyclic expressions beyond those already being explored on the active stack.
     - Therefore, returning `false` when `variables.add(definition)` is `false` is exact for both shared-variable DAGs ($O(V + E)$ traversal instead of $O(2^d)$) and cyclic variable graphs, while remain thread-safe (`ResponseWriters` walk state is stack-confined to the constructor call).
2. **`Policy.java` (`W/cod_2/xacml-core/src/main/java/org/xacml4j/v30/policy/Policy.java:123, 235`) and `PolicySet.java` (`W/cod_2/xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySet.java:92, 241`)**:
   - In `Policy` constructor (`:123`), `this.builtInAlgorithm = BUILT_IN_ALGORITHMS.contains(combiningAlgorithm.getClass())` is assigned before `this.responseSilent = isResponseSilent(this)` (`:130`). Replacing `!BUILT_IN_ALGORITHMS.contains(policy.combiningAlgorithm.getClass())` with `!policy.builtInAlgorithm` at `Policy.java:235` is an exact semantic equivalence.
   - In `PolicySet` constructor (`:92`), `this.builtInAlgorithm = ALGORITHMS.contains(combiningAlgorithm.getClass())` is assigned before `this.responseSilent = isResponseSilent(this)` (`:99`). Replacing `!ALGORITHMS.contains(set.combiningAlgorithm.getClass())` with `!set.builtInAlgorithm` at `PolicySet.java:241` is an exact semantic equivalence.
3. **`NestedResponseStagingPdpTest.java` (`W/cod_2/xacml-pdp/src/test/java/org/xacml4j/v30/pdp/NestedResponseStagingPdpTest.java:448-580`)**:
   - Adds decision-level tests for a 16-level shared-DAG of `VariableDefinition`s with a custom response-writing leaf (`:448-478`), a custom `VariableDefinition` subclass reached after a 16-level shared DAG (`:482-512`), custom rule/policy combining algorithms with target indexing enabled (`:516-542`), and a deterministic `CountingExpression` traversal bound (`:564-578`).

---

## Verification Scope & Unrun Gates

- **Verified by reviewer (`SUPPORTED`)**:
  - Static source inspection and SHA-256 verification of all 6 candidate files in `W/cod_1` and `W/cod_2` against base `759d90a6c75510d55127d8e22870f9edd985c991`.
  - Complete call-path trace across `Rule`, `Policy`, `PolicySet`, `TargetBeforeContext`, `BaseDecisionRule`, `Match`, `Apply`, `FunctionSpecBuilder`, `DefaultFunctionInvocation`, `BagFunctions`, `HigherOrderFunctions`, `AttributeDesignatorFunctions`, `Entity`, `VariableReference`, `VariableDefinition`, and `ResponseWriters`.
- **Supplied author test evidence (not re-executed by reviewer)**:
  - Terra (`W/cod_1/openspec/changes/optimize-pdp-evaluation/evidence/tests/201-resume-d47-terra/summary.md`): targeted `RulesInPolicyContextPdpTest` (`8` tests, `0` failures) and `RuleTest` (`29` tests, `0` failures).
  - Sol (`W/cod_2/openspec/changes/optimize-pdp-evaluation/evidence/tests/202-resume-variable-dag-sol/summary.md`): targeted `NestedResponseStagingPdpTest` (`14` tests, `0` failures).
- **Unrun (`UNVERIFIED` / `UNRUN`)**:
  - Combined integration of Terra + Sol after fixing Terra's `[P2-1]` and `[P2-2]`.
  - Full Maven reactor test suite and OASIS 2.0/3.0 + RSA 2008 conformance suites.
  - Controlled JMH before/after benchmark gate for D47.
