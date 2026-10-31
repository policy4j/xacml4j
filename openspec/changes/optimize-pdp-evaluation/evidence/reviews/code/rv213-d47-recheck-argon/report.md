# Code Review Report — `rv213-d47-recheck-argon`

- **Change**: `optimize-pdp-evaluation`
- **Design / Task**: D47 (`42.14`)
- **Bead**: `policy4j-cwtu.42.14`
- **Base commit**: `759d90a6c75510d55127d8e22870f9edd985c991` (`feature_xacml_20`)
- **Author / Worktree**: Codex GPT-6.1 Terra (`W/cod_1`, actor `ntm-terra-g1`; runtime model/effort not independently surfaced)
- **Author Evidence**: `W/cod_1/openspec/changes/optimize-pdp-evaluation/evidence/tests/205-d47-correction-terra/summary.md`
- **Reviewer**: `agyx_1` (Agy Gemini 4 Argon High, plain review packet)
- **Date**: 2026-10-31
- **Verdict**: **APPROVE** (`0` P0, `0` P1, `0` P2, `0` P3)

---

## 1. Candidate SHA-256 Verification

All three candidate files in `W/cod_1` were hashed before review and matched the packet digests (`SUPPORTED`):

| File | SHA-256 |
| --- | --- |
| `xacml-core/src/main/java/org/xacml4j/v30/policy/Rule.java` | `6ee7c549d08f243104cfbbec72d079062c883ed3c6c899863cc4b39fce0f338a` |
| `xacml-core/src/main/java/org/xacml4j/v30/policy/TargetBeforeContext.java` | `761d116cb211a9be7a7197834195152cc8062d4e8b5c3f278967d0c4749ea6b7` |
| `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/RulesInPolicyContextPdpTest.java` | `356969aaccd956d90b4c261cad271b6b95bde663ee8810a475f0263763b96c59` |

---

## 2. Findings

None (`0` P0, `0` P1, `0` P2, `0` P3).

---

## 3. Focus Questions (Hypotheses Tested Against Source)

### Question 1: Are `rv205` P2-1 and P2-2 closed on every path, including `attribute-selector`, a bag of `Entity` values, and a variable holding either?

**Refuted as a failure path — both `rv205` P2-1 and P2-2 are closed on every reachable path (`SUPPORTED`).**

1. **Built-in `attribute-designator` and `attribute-selector` functions (`rv205` P2-1)**:
   - **As a `FunctionReference` argument to a higher-order function**: Refuted. `AttributeDesignatorFunctions.resolve` (`AttributeDesignatorFunctions.java:140-145`) always returns a bag type (`getType(...).bagType()` at `:142` or `DynamicType.INSTANCE.bagType()` at `:144`, both having `isBag() == true`). Every built-in higher-order function rejects a `FunctionReference` whose return type is a bag (`HigherOrderFunctions.validateArguments` at `HigherOrderFunctions.java:436, 486` and `MapFunctionResolverValidator.resolve` at `:622-624`). Neither `attribute-designator` nor `attribute-selector` can be invoked via `FunctionReference`.
   - **As an `Apply` expression (`Rule.java:350-362`)**:
     - Every trusted built-in `Apply` (`FunctionSpecBuilder.usesBuiltInInvocation(apply.function()) == true` at `Rule.java:352`) that is not already `UNSAFE` returns `RUNTIME_VALUE` (`Rule.java:361`), without relying on a function-ID allow/deny list.
     - Tracing `urn:oasis:names:tc:xacml:3.0:function:attribute-selector` (`AttributeDesignatorFunctions.selector`, `AttributeDesignatorFunctions.java:89-123`) by source:
       - If its `dataType` argument (`arguments.get(2)`) is an `AnyURI` literal, `AttributeDesignatorFunctions.resolve` (`:141-143`) resolves a concrete bag type and `provenance(Apply(attribute-selector, ...))` returns `RUNTIME_VALUE` (`Rule.java:361`) (or `UNSAFE` if any argument is `UNSAFE`). If `dataType` is not an `AnyURI` literal, `resolve` returns `DynamicType.INSTANCE.bagType()` (`:144`); passing that `Apply` to a typed parameter wraps it in a `TypeAssertion` (`Apply.java:132`), which `Rule.EngineOnly.provenance` rejects with `UNSAFE` (`Rule.java:379`), and passing it to an untyped bag parameter (`@XacmlFuncParamAnyBag` / `@XacmlFuncParamVarArg(typeId="")`) still scores `RUNTIME_VALUE` (`Rule.java:361`).
       - When `Apply(attribute-selector, ...)` is evaluated directly in a borrowed policy context (not inside a higher-order call):
         - For an `Entity` first argument (`AttributeDesignatorFunctions.java:109-111`), `((Entity)categoryOrEntity).resolve(selectorKey)` (`Entity.java:236-239`) calls `content.resolve(selectorKey, () -> this)` (`Content.java:85`), which does not receive `EvaluationContext` (`XmlContent.convertNodeToXacml` at `XmlContent.java:459-470` and `JsonContent.resolve` at `JsonContent.java:159, 171-174` materialize standard `Value` instances via `TypeToString`).
         - For an `AnyURI` category first argument (`AttributeDesignatorFunctions.java:104-106, 113-115`), `context.resolve(selectorKey)` calls `DescendantEvaluationContext.resolve` (`DescendantEvaluationContext.java:352-356`), which delegates to `top.resolve(selectorKey)` (`RootEvaluationContext.java:510-513`). `RootEvaluationContext.resolve` passes `this` (`RootEvaluationContext`, never the borrowed policy context) to `DefaultEvaluationContextHandler.doSelectorResolve` (`DefaultEvaluationContextHandler.java:287-304`) and `resolveViaPip` (`:528-535`).
       - When the bag produced by `Apply(attribute-selector, ...)` is passed directly, through another built-in `Apply` (such as `string-union`), or through a `VariableReference` (`Rule.java:363-378`) into any higher-order function, the higher-order `Apply` combines `RUNTIME_VALUE` (`2`) from the bag operand with `FUNCTION_REFERENCE` (`4`) from the `FunctionReference` operand and returns `UNSAFE` (`Rule.java:359-360`), keeping a real `RuleEvaluationContext`.

2. **`Entity` literals, a bag of `Entity` values, and a `VariableDefinition` holding either (`rv205` P2-2)**:
   - An `Entity` literal (`Entity.java:43`, `public final class Entity extends Value.SelfValue<Entity>` in `org.xacml4j.v30.types`) scores `0` at `Rule.java:333-335`, a literal `BagOfValues` of `Entity` instances scores `0` at `Rule.java:336-344`, and a `VariableDefinition` holding either caches and returns `0` at `Rule.java:375-377`.
   - Every path that can reach an `Entity` or its nested `Attribute` bags was traced:
     - **Attribute extraction from an `Entity` (or from an `Entity` obtained via `VariableReference` or `Apply`)**: Only `AttributeDesignatorFunctions.designator` (`AttributeDesignatorFunctions.java:61, 75`) and `AttributeDesignatorFunctions.selector` (`:109-110`) extract attribute bags from an `Entity`. Both are `Apply` expressions whose provenance is `RUNTIME_VALUE` (`Rule.java:361`), and a `VariableDefinition` holding that extraction caches and returns `RUNTIME_VALUE` (`Rule.java:375-377`). Any higher-order function over the extracted bag (or over a variable holding it) therefore sees `RUNTIME_VALUE | FUNCTION_REFERENCE` (`Rule.java:359-360`) and returns `UNSAFE`.
     - **Direct higher-order call over a literal bag of `Entity` values (or a `VariableReference` to one)**: Across all built-in providers (`XacmlDefaultFunctions`), the only function taking `Entity` that returns a non-bag `Value` (and is therefore accepted as a `FunctionReference` by `HigherOrderFunctions.validateArguments` at `HigherOrderFunctions.java:436, 486`) is `urn:oasis:names:tc:xacml:1.0:function:entity-equal` (`EqualityPredicates.eq(Entity, Entity)`, `EqualityPredicates.java:53-61`). When a higher-order function invokes `entity-equal` on `Entity` elements from the bag, `FunctionSpecBuilder.invoke` (`FunctionSpecBuilder.java:662`) calls `entity.evaluate(context)`. Because `Entity` is `final` and inherits `Value.evaluate(EvaluationContext)` (`Value.java:103-105`), `entity.evaluate(context)` returns `this` without inspecting `context` or evaluating nested `Attribute` values, and `Entity.equals` (`Entity.java:260-271`) compares `attributeIndex` and `content` via `equals(Object)` without an `EvaluationContext`.
     - **Target `Match` over `Entity`**: `TargetBeforeContext.safeValues` (`TargetBeforeContext.java:166-184`) returns `false` for `XacmlTypes.ENTITY`, so `TargetBeforeContext.prepare` (`:140`) returns `Plan.EMPTY` and `precheck` returns `UNKNOWN`. Because `Rule.EngineOnly.target` sets `targetHasMatch = true` (`Rule.java:288`), `TargetBeforeContext.evaluateIfMatch` (`TargetBeforeContext.java:87`) does not borrow the policy context and evaluates the rule in its own `RuleEvaluationContext`.

---

### Question 2: `rv206`'s P3 — can the `string-union` rows fail for the right reason, and is any remaining row unable to fail?

**Confirmed that the `string-union` rows fail for the right reason (`SUPPORTED`); among all 56 rows, only the 2 literal-bag control rows in `repeatedConditionsAndHigherOrderRulesKeepTheirResponses` (`runtime=false`) guard eligibility via their reflective assertion rather than decision divergence (`SUPPORTED`).**

1. **`string-union` rows (`RulesInPolicyContextPdpTest.java:180-181`, 4 rows: `request`/`pip` $\times$ `indexed=false`/`true`)**:
   - `SetFunctions.stringUnion` (`SetFunctions.java:152-157`) calls `BagOfValues.union` (`BagOfValues.java:213-225`), which copies distinct `Value<?>` elements from both bags into the result bag without calling `Value.evaluate(context)`.
   - Consequently, `ParentPolicyValue` remains un-evaluated inside the union bag until `HigherOrderFunctions.anyOfPair` (`HigherOrderFunctions.java:86-94`) -> `DecidingOperands.firstDeciding` (`DecidingOperands.java:204`) -> `FunctionReference.invoke` (`FunctionReference.java:121`) -> `FunctionSpecBuilder.invoke` (`FunctionSpecBuilder.java:662`) calls `ParentPolicyValue.evaluate(context)`.
   - In an ordinary `RuleEvaluationContext`, `ParentPolicyValue.evaluate(context)` returns `StringVal.of("policy")`, matching `"policy"` and producing `Decision.PERMIT` with the `"permit"` obligation. If `Rule.EngineOnly.provenance` fails to return `RUNTIME_VALUE` from `Apply` (`Rule.java:361`), the rule borrows the policy context, `ParentPolicyValue.evaluate(context)` returns `StringVal.of("none")`, and all 4 rows fail with `Decision.NOT_APPLICABLE` (matching the author's mutation run `02-propagation-mutant-red.txt`: 20 run, 4 failures).
2. **Remaining rows in `RulesInPolicyContextPdpTest.java` (56 total across 9 test methods)**:
   - 54 of the 56 rows fail at the `ResponseContext` / `Decision` level if their corresponding eligibility, precheck-guard, lazy-PIP, or status-isolation invariant is broken.
   - The remaining 2 rows are `repeatedConditionsAndHigherOrderRulesKeepTheirResponses(runtime=false, indexed=false|true)` (`RulesInPolicyContextPdpTest.java:363-395`). When `runtime=false`, `bag` is `XacmlTypes.STRING.bagOf(StringVal.of("policy"))` (`:371`). Because `StringVal.of("policy").evaluate(context)` returns `"policy"` in both a rule context and a policy context, an eligibility regression that prevents borrowing would not change the `Decision.PERMIT` outcome of those 2 rows; instead, those 2 rows fail at the reflective assertion `assertTrue((boolean)eligibility.invoke(rule))` (`:376-381`) if `Rule.EngineOnly.provenance(Apply)` fails to strip `FUNCTION_REFERENCE` (`Rule.java:361`), while their outcome assertions verify that D45's shared condition (`normalizedPresent()`) produces the same response when rule `"yes"` borrows the policy context.

---

### Question 3: XACML 3.0 §§5.27, 7.11, and 7.19 — do a prechecked target and an ordinary target give the same `Match`, `No-match`, and `Indeterminate` answers, including a missing attribute with `MustBePresent="true"` and a custom value in the bag?

**Confirmed (`SUPPORTED`).**

1. **Scope of `TargetBeforeContext.prepare` and `precheck` (`TargetBeforeContext.java:129-240`)**:
   - `prepare` (`:129-148`) creates a non-empty `Plan` only when `target`, every `MatchAnyOf`, every `MatchAllOf`, every `Match`, and every `AttributeDesignator` have their exact engine classes, the literal passes `safeValues` (`:161-186`), and `match.predicate().hasDirectScan(match.getAttributeValue())` is `true`. Any `AttributeSelector`, custom predicate, or non-scannable literal gets `Plan.EMPTY`, for which `precheck` returns `UNKNOWN` (`:200`).
   - `precheck` (`:203-214`) also returns `UNKNOWN` unless `top` is a `RootEvaluationContext` on the deciding thread with `hasRequestAttributeResolution()`, observation disabled, runtime parameter validation disabled, and a valid parent context (`context.getCurrentPolicy() != null && context.getCurrentRule() != resolved` for a `Rule` at `:209`).
2. **Missing attribute with `MustBePresent="true"` (or `"false"`) (XACML 3.0 §§5.27, 7.3.5, 7.11, 7.19.3)**:
   - In `precheck` (`TargetBeforeContext.java:224-227`), if `context.resolveEntity(key.getCategory())` is empty (`:225`) or `entity.get().resolve(key)` is empty (`:227`), `precheck` immediately returns `UNKNOWN` (`Optional.empty()`). Note that `AttributeIndex.resolve` (`AttributeIndex.java:321-334`) returns `Optional.empty()` whenever the request entity has no values matching `(attributeId, dataType, issuer)`.
   - Because `((Rule)child).targetNeedsPrecheck()` is `true` for every rule whose target contains a `Match` (`Rule.java:101-103, 288`), `TargetBeforeContext.evaluateIfMatch` (`TargetBeforeContext.java:87`) sees `known.isPresent() == false` and does not call `policy.enterRule(child)`.
   - The rule then evaluates in its own `RuleEvaluationContext` (`:95, 99`). In `Match.match` (`Match.java:120, 138-145`), `attributeRef.evaluate(context)` (`AttributeReference.java:104-111`) queries the PIP if available and, if still absent with `MustBePresent="true"`, throws `AttributeReferenceEvaluationException.forMissingRef(getReferenceKey())` (`urn:oasis:names:tc:xacml:1.0:status:missing-attribute`). `Match.match` records the status on the rule and context (`Match.java:139-143`) and returns `MatchResult.INDETERMINATE`, which propagates through `MatchAllOf.match` (`MatchAllOf.java:69-72`), `MatchAnyOf.match` (`MatchAnyOf.java:61-63`), `Target.match` (`Target.java:71-72`), and `Rule.evaluateRule` (`Rule.java:152-155`, returning `INDETERMINATE_P` or `INDETERMINATE_D`).
3. **Custom value in the bag (request or PIP)**:
   - If the attribute is resolved from a PIP, it is absent from the request entity, so `precheck` returns `UNKNOWN` at `TargetBeforeContext.java:227`.
   - If the attribute is present in the request entity and its bag contains any custom `Value` subclass (such as `ParentPolicyValue`), `match.predicate().scan(context, match.getAttributeValue(), bag.get())` (`TargetBeforeContext.java:228`) delegates to `DefaultFunctionInvocation.scan` (`DefaultFunctionInvocation.java:271-293`) and `BagStorage`:
     - `BagStorage.equal` (`BagStorage.java:65-67`) checks `valueAt(i).getClass() != literal.getClass()` across all elements before comparing any element;
     - `BagStorage.compareDoubles` (`:76-78`) and `compareIntegers` (`:86-88`) check `instanceof DoubleVal` / `instanceof IntegerVal` (both `final` classes) across all elements first;
     - `BagStorages.compareTemporal` (`BagStorages.java:405-410`) checks `value.getClass() != type || value instanceof ISO8601Time time && !time.isTimeZoneDefined()` across all elements first.
   - Any custom `Value` element (or timezone-less `ISO8601Time`) produces `BagStorage.Scan.NONE`, causing `precheck` to return `UNKNOWN` (`TargetBeforeContext.java:229`) and `evaluateIfMatch` (`:87`) to evaluate the rule in its own `RuleEvaluationContext`.
4. **Truth-table and short-circuit equivalence (XACML 3.0 §5.27, §7.11 Tables 1–3)**:
   - Because `precheck` aborts with `UNKNOWN` on any `Match` that is not a proven `Scan.MATCH` or `Scan.NO_MATCH` over present request values of exact built-in classes, no `Match` visited on a completed `precheck` path can evaluate to `Indeterminate`.
   - With `Indeterminate` excluded on every completed `precheck` path, the boolean short-circuit loops in `precheck` (`TargetBeforeContext.java:215-236`) match XACML 3.0 §7.11 Tables 1–3 and the engine's ordinary evaluation order:
     - `MatchAllOf` (`:220-231` vs `MatchAllOf.java:66-79`): stops at the first `Scan.NO_MATCH` (`allMatch = false`), returning `MATCH` iff all `Match` elements in the `MatchAllOf` match.
     - `MatchAnyOf` (`:217-234` vs `MatchAnyOf.java:58-70`): stops at the first matching `MatchAllOf` (`anyMatches = true`), returning `NOT_MATCHED` at `:234` if every `MatchAllOf` in the `MatchAnyOf` has a proven `NO_MATCH`.
     - `Target` (`:215-236` vs `Target.java:69-75`): stops at the first `MatchAnyOf` that is `NOT_MATCHED` (`:234`), and returns `MATCHED` (`:236`) iff every `MatchAnyOf` has at least one matching `MatchAllOf`.
   - When `precheck` returns `MATCHED`, `TargetBeforeContext.evaluateIfMatch` (`:87-94`) enters the borrowed policy context, `RootEvaluationContext.evaluatePrechecked` (`RootEvaluationContext.java:618-628`) records `prechecked = child; precheckedMatch = true`, and `BaseDecisionRule.isMatch` (`BaseDecisionRule.java:213-220`) consumes that `MATCH` once via `takePrechecked` before `target.match(context)` can run, clearing `prechecked` in `finally` (`RootEvaluationContext.java:626-628`).

---

## 4. Verification Performed

- **SHA-256 verification (`SUPPORTED`)**: Verified all three candidate files in `W/cod_1` (`Rule.java`, `TargetBeforeContext.java`, `RulesInPolicyContextPdpTest.java`).
- **Source and diff inspection (`SUPPORTED`)**: Inspected `rtk git diff 759d90a6` in `W/cod_1` and traced call paths across `Rule`, `TargetBeforeContext`, `Policy`, `DescendantEvaluationContext`, `RootEvaluationContext`, `BaseDecisionRule`, `Target`, `MatchAnyOf`, `MatchAllOf`, `Match`, `AttributeReference`, `AttributeIndex`, `Entity`, `Content`, `XmlContent`, `JsonContent`, `DefaultEvaluationContextHandler`, `AttributeDesignatorFunctions`, `HigherOrderFunctions`, `DecidingOperands`, `EqualityPredicates`, `BagFunctions`, `SetFunctions`, `DefaultFunctionInvocation`, `BagOfValues`, `BagStorage`, and `BagStorages`.
- **Static analysis (`SUPPORTED`)**:
  - `ast-grep scan` on `Rule.java`, `TargetBeforeContext.java`, and `RulesInPolicyContextPdpTest.java`: exit `0`, `0` findings.
  - `rtk git diff --check` in `W/cod_1`: exit `0`, clean.
  - `ripwire --exclude=/evidence/ --edit-check` on `targetNeedsPrecheck` (`new-symbol`, `callers="1"`, `incompatible="0"`), `evaluatesInPolicyContext` (`unchanged`, `callers="1"`, `incompatible="0"`), and `TargetBeforeContext.java:evaluateIfMatch` (`unchanged`, `callers="0"`, `incompatible="0"`).
  - `ripwire --exclude=/evidence/ --quality-delta` on `W/cod_1`: exit `2` due to test-helper clone/duplication rows, `short-horizon-churn`, `verbosity` on `Rule`/`EngineOnly`/`RulesInPolicyContextPdpTest`/`TargetBeforeContext`, and `complexity` `24` (bar `15`) on `Rule.EngineOnly.provenance`; inspected and confirmed non-defective.
- **Code intelligence coverage (`PARTIAL`)**: `tgrep` has no index in this worktree (`UNAVAILABLE`), and Serena serves the client root checkout rather than `W/cod_1` (`UNAVAILABLE`); confirmed all symbol references with `rtk rg`, `ripwire`, and direct source reads.

---

## 5. What Was Not Verified & Residual Risk

1. **No builds, tests, or benchmarks run by reviewer (`UNVERIFIED`)**:
   - Per packet instructions, no Maven build, JUnit test, or JVM command was executed by the reviewer.
   - Author-supplied results in `W/cod_1/openspec/changes/optimize-pdp-evaluation/evidence/tests/205-d47-correction-terra/summary.md` and `candidate-ready.json`:
     - Focused `RulesInPolicyContextPdpTest`: `56` run, `0` failures, `0` errors, `0` skipped (`03-focused-green.txt`), plus prior-candidate red run (`44` run, `4` failures in `01-focused-red.txt`) and two temporary mutation runs killed (`20` run, `4` failures in `02-propagation-mutant-red.txt`; `10` run, `4` failures in `04-unguarded-target-mutant-red.txt`).
     - Affected modules (`05-modules-clean.txt` and `06-modules-continue-after-date-error.txt`): `xacml-core` `7,357` run (`0` failures, `1` error in `ISO8601DateTest.testCreateFromCalendar`, `2` skipped — the separate zero-based `Calendar.MONTH` defect), `xacml-xml` `254` run (`0` failures/errors/skips), `xacml-json` `24` run (`0` failures/errors, `1` skipped), `xacml-pdp` `851` run (`0` failures/errors, `3` skipped).
   - **Unrun gates**: Combined build with `W/cod_2` (Sol) and the `ISO8601Date` fix, full reactor and OASIS 2.0/3.0 + RSA 2008 conformance suites, and the controlled JMH benchmark pair.
2. **Runtime `attribute-selector` function test execution (`UNVERIFIED` at runtime, `SUPPORTED` by source trace)**:
   - The author did not run a runtime fixture for `urn:oasis:names:tc:xacml:3.0:function:attribute-selector`; its eligibility and evaluation paths were verified by source inspection above.
3. **Allocation and latency gate (`UNVERIFIED`)**:
   - Marking every trusted built-in `Apply` result as `RUNTIME_VALUE` (`Rule.java:361`) conservatively excludes rules where a higher-order function takes a nested built-in `Apply` over literals as its bag argument, while `TargetBeforeContext.evaluateIfMatch` (`TargetBeforeContext.java:87`) restores policy-context borrowing for rules whose target `Match`es are proven `MATCH` by the precheck. The net allocation and latency effect across the D47 benchmark corpora (`rsa2008`, `narrow`, `multi-valued`, `responses`) remains to be measured by the unrun controlled JMH pair.
