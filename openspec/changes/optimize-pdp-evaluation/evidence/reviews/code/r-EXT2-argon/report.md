# Code Review: `FIX-EXT2` — Corrected Extended-Indeterminate Fix (`R-EXT2-argon`)

- **Verdict**: **APPROVE WITH CHANGES** (`0 P1`, `1 P2`, `1 P3`)
- **Reviewed checkout**: `the main checkout` (merged `FIX-EXT2`)
- **Evidence coverage**: `SUPPORTED` (inspected merged sources in `xacml-core` and `xacml-pdp`, [REPORT-FIX-EXT2-opus.md](`.ntm/worktrees/xacml4j--batch2/cc_1/REPORT-FIX-EXT2-opus.md`), [patch-FIX-EXT2.diff](file://<scratchpad>/patch-FIX-EXT2.diff), and [report.md](`openspec/changes/optimize-pdp-evaluation/evidence/reviews/code/r-TERRA-opus/report.md`)).

---

## 1. Review Answers (Items 1–6)

### 1.1 Advice and obligations inside a pass; do any responses of a pass reach a `Result`?

- **Evaluation inside a pass (`SUPPORTED`)**:
  - [`Rule.evaluate`](`xacml-core/src/main/java/org/xacml4j/v30/policy/Rule.java`) and [`BaseCompositeDecisionRule.evaluate`](`xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java`) evaluate `evaluateAdvicesAndObligations(...)` unconditionally when the rule or composite reaches `PERMIT` or `DENY`, including inside an extended-Indeterminate pass.
  - Under OASIS XACML 3.0 errata01 §7.12–§7.14 and §7.18, an obligation or advice expression failure turns that rule or composite into its extended Indeterminate value (`INDETERMINATE_P`, `INDETERMINATE_D`, or `INDETERMINATE_DP`), closing the `R-TERRA` `P1-1` fail-open bug.
- **No responses from a pass ever reach a `Result` (`SUPPORTED`)**:
  - **Enclosed pass (`isEnclosed(context) == true`)**: [`BaseCompositeDecisionRule.createExtIndeterminateEvalContext`](`xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java`) wraps `extContext` in `new StagedResponses(extContext)` whenever `canRespond() && isEnclosed(context)` holds. Descendants inside the pass stage and publish into that pass-level [`StagedResponses`](`xacml-core/src/main/java/org/xacml4j/v30/policy/StagedResponses.java`) wrapper, which [`BaseCompositeDecisionRule.getExtendedIndeterminate`](`xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java`) discards without calling `publishResponses()`.
  - **Unenclosed root pass (`isEnclosed(context) == false`)**: When the root `Policy` or `PolicySet` itself starts an extended-Indeterminate pass, [`BaseCompositeDecisionRule.getExtendedIndeterminate`](`xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java`) returns only `INDETERMINATE_{D,P,DP}` or `NOT_APPLICABLE`. [`DefaultPolicyDecisionPoint.createResult`](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java`) only copies `getMatchingObligations` and `getMatchingAdvices` into the `Result` when `decision == PERMIT || decision == DENY`.

### 1.2 Pass-state forwarding and evaluated-policy guard

- **Correct (`SUPPORTED`)**:
  - [`DescendantEvaluationContext.isExtendedIndeterminateEval`](`xacml-core/src/main/java/org/xacml4j/v30/policy/DescendantEvaluationContext.java`) forwards `parent.isExtendedIndeterminateEval()`.
  - Both [`BaseCompositeDecisionRule.evaluate`](`xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java`) and [`DescendantEvaluationContext.addEvaluationResult`](`xacml-core/src/main/java/org/xacml4j/v30/policy/DescendantEvaluationContext.java`) guard `addEvaluationResult` with `!isExtendedIndeterminateEval()`.

### 1.3 The new status rule (`startsPass()`): exactness, status comparison, and conformance risk

- **Is `startsPass()` exact? Not yet — see `[P2-1]` below**:
  - The 1-argument [`DescendantEvaluationContext.setEvaluationStatus(Status)`](`xacml-core/src/main/java/org/xacml4j/v30/policy/DescendantEvaluationContext.java`) checks `if (startsPass() && parent.getEvaluationStatus().isPresent()) { return; }`.
  - However, the 2-argument [`DescendantEvaluationContext.setEvaluationStatus(DecisionRule, Status)`](`xacml-core/src/main/java/org/xacml4j/v30/policy/DescendantEvaluationContext.java`) delegates directly to `top.setEvaluationStatus(rule, status)`.
  - When [`Match.match`](`xacml-core/src/main/java/org/xacml4j/v30/policy/Match.java`) or [`Condition.evaluate`](`xacml-core/src/main/java/org/xacml4j/v30/policy/Condition.java`) catches an exception while evaluating a **`Policy` or `PolicySet`** target, `context.getCurrentRule()` is `null`. Calling `top.setEvaluationStatus(null, status)` directly:
    1. **Bypasses `startsPass()`** when a nested `Policy` or `PolicySet` target fails inside a pass, overwriting `RootEvaluationContext.evaluationStatus` with the inner target's status (reproduced right now in [`ExtendedIndeterminatePassPdpTest.aNestedIndeterminateTargetStillComputesItsOwnExtendedValue`](`xacml-pdp/src/test/java/org/xacml4j/v30/pdp/ExtendedIndeterminatePassPdpTest.java`), where `result.getStatus().getMessage()` is `"inner-target"` instead of `"outer-target"`).
    2. **Leaves `this.evaluationStatus == null` on the outer `DescendantEvaluationContext`** whenever the outer `Policy`/`PolicySet` target fails via an `EvaluationException` rather than `MustBePresent` (e.g., a PIP resolver error, an XPath error, a `Match` function exception, or `FailingExpression` in [`NestedResponseStagingTest`](`xacml-core/src/test/java/org/xacml4j/v30/policy/NestedResponseStagingTest.java`)). Then `parent.getEvaluationStatus().isPresent()` is `false` at the pass boundary, so any descendant failure inside the pass overwrites the outer target's status.
- **Which status does an `Indeterminate` result carry now, compared with before Terra's fix?**
  - **Before Terra's fix**: `DescendantEvaluationContext` neither forwarded `isExtendedIndeterminateEval()` nor stopped status propagation at a pass boundary. Every target, condition, obligation, or advice failure inside an extended-Indeterminate pass overwrote `RootEvaluationContext.evaluationStatus`; the final `Indeterminate` result carried the status of the **last failure inside the pass** (and kept the outer failure's status only when nothing inside the pass failed).
  - **Now (with `FIX-EXT2` + `[P2-1]` fixed)**: An extended-Indeterminate pass preserves the status of the **outer failure (target, condition, or composite obligation/advice) that triggered the pass** and absorbs any failures that occur inside the pass at the pass boundary (`startsPass()`). If a later sibling *outside* the pass fails during normal evaluation, its status updates the decision status as before.
- **Conformance risk (`SUPPORTED`)**:
  - **None.** Under XACML 3.0 errata01 (§7.10, §7.12–§7.14, §7.19), the extended-Indeterminate pass only classifies the potential effect (`{P}`, `{D}`, `{DP}`) of an element whose own target, condition, or obligation already made it `Indeterminate`. Preserving the status of the failure that caused the `Indeterminate` decision rather than clobbering it with a hypothetical failure under a non-matching target is more faithful to the spec, and all 938 active conformance tests (`938/12`) pass.

### 1.4 A nested `Indeterminate` target still combines its own children

- **Correct (`SUPPORTED`)**:
  - [`BaseCompositeDecisionRule.evaluate`](`xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java`) no longer short-circuits to `INDETERMINATE_DP` when `isMatch(context) == MatchResult.INDETERMINATE` inside an outer pass; it invokes `getExtendedIndeterminate(createExtIndeterminateEvalContext(context))` at line 154 and combines its own children per §7.13/§7.14.

### 1.5 The five tests in `ExtendedIndeterminatePassPdpTest`

- [`aPassEvaluatesTheResponsesOfItsChildrenAndReturnsNone`](`xacml-pdp/src/test/java/org/xacml4j/v30/pdp/ExtendedIndeterminatePassPdpTest.java`): verifies `INDETERMINATE_P`, the outer `"target"` status message, `resolvedAttributeIds(result)` equal to `Set.of("rule-response", "policy-response")` (confirming pass-resolved attributes are recorded in the context while their responses are discarded), and empty obligations/advice.
- [`aNestedIndeterminateTargetStillComputesItsOwnExtendedValue`](`xacml-pdp/src/test/java/org/xacml4j/v30/pdp/ExtendedIndeterminatePassPdpTest.java`): verifies `INDETERMINATE_P`, **but omits a status assertion** (`[P3-1]`), which hid `[P2-1]` (`result.getStatus().getMessage()` is currently `"inner-target"` instead of `"outer-target"`).
- [`aFailingObligationInThePassMakesItsPolicyIndeterminateAndKeepsTheStatus`](`xacml-pdp/src/test/java/org/xacml4j/v30/pdp/ExtendedIndeterminatePassPdpTest.java`): verifies `INDETERMINATE_DP` (`deny-overrides(Ind{D}, Permit)`) and that the status message remains `"outer-target"`.
- [`aFailingObligationBelowAnIndeterminateTargetKeepsTheDecisionIndeterminate`](`xacml-pdp/src/test/java/org/xacml4j/v30/pdp/ExtendedIndeterminatePassPdpTest.java`): reproduces the exact `R-TERRA` `P1-1` `R/CS/S` fail-open topology and verifies `Decision.INDETERMINATE_DP` instead of `PERMIT`.
- [`theChildrenOfAPassAreNotInThePolicyIdentifierList`](`xacml-pdp/src/test/java/org/xacml4j/v30/pdp/ExtendedIndeterminatePassPdpTest.java`): effective test — `root` (`permit-overrides`) evaluates to `Decision.PERMIT` via sibling `s` with `returnPolicyIdList(true)`, so [`DefaultPolicyDecisionPoint.createResult`](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java`) populates `result.getPolicyIdentifiers()` from `context.getEvaluatedPolicies()`, confirming `s` is present and pass-evaluated `p` is absent.

### 1.6 The `NestedResponseStagingTest` change

- **Correct (`SUPPORTED`)**:
  - In [`NestedResponseStagingTest.childWithAnIndeterminateTargetAddsNoResponsesToThePermitOfItsSibling`](`xacml-core/src/test/java/org/xacml4j/v30/policy/NestedResponseStagingTest.java`), removing the two internal `aloneContext` response-list assertions on the unenclosed root `PolicySet` (`alone`) while keeping the `INDETERMINATE_P` decision, status, and enclosed `enclosing` (`PERMIT`) response assertions matches the `canRespond() && isEnclosed(context)` contract in [`BaseCompositeDecisionRule.createExtIndeterminateEvalContext`](`xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java`).

---

## 2. Findings

### `[P2-1]` `DescendantEvaluationContext.setEvaluationStatus(DecisionRule, Status)` bypasses `setEvaluationStatus(Status)` and `startsPass()` when `rule == null`

- **Location**:
  - [`DescendantEvaluationContext.java:104-131`](`xacml-core/src/main/java/org/xacml4j/v30/policy/DescendantEvaluationContext.java`)
  - Callers: [`Match.java:129-133`](`xacml-core/src/main/java/org/xacml4j/v30/policy/Match.java`) and [`Condition.java:96-100`](`xacml-core/src/main/java/org/xacml4j/v30/policy/Condition.java`)
- **Problem**:
  [`DescendantEvaluationContext.setEvaluationStatus(DecisionRule, Status)`](`xacml-core/src/main/java/org/xacml4j/v30/policy/DescendantEvaluationContext.java`) currently does:
  ```java
  @Override
  public void setEvaluationStatus(DecisionRule rule, Status status) {
  	top.setEvaluationStatus(rule, status);
  }
  ```
  When `Match.match(context)` catches `EvaluationException` (or `Condition.evaluate(context)` catches a non-`EvaluationException` runtime `Exception`) while evaluating a `Policy` or `PolicySet`, `context.getCurrentRule()` is `null`. Jumping directly to `top.setEvaluationStatus(null, status)` ([`RootEvaluationContext.java:244-250`](`xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`)) sets `RootEvaluationContext.evaluationStatus` without walking the `DescendantEvaluationContext` chain. This causes two bugs:
  1. **Nested `Policy`/`PolicySet` target failure inside a pass overwrites the outer status**:
     In [`ExtendedIndeterminatePassPdpTest.aNestedIndeterminateTargetStillComputesItsOwnExtendedValue`](`xacml-pdp/src/test/java/org/xacml4j/v30/pdp/ExtendedIndeterminatePassPdpTest.java`), when `child` (`Policy`) evaluates `target("inner-target")`:
     - [`AttributeDesignator.doContextResolve`](`xacml-core/src/main/java/org/xacml4j/v30/policy/AttributeDesignator.java`) first calls 1-arg `context.setEvaluationStatusIfAbsent(...)`, which stops at `startsPass()`.
     - Two lines later (`AttributeDesignator.java:98`), it throws `AttributeReferenceEvaluationException`.
     - [`Match.match`](`xacml-core/src/main/java/org/xacml4j/v30/policy/Match.java`) catches `EvaluationException` and calls `context.setEvaluationStatus(context.getCurrentRule(), e.getStatus())` with `getCurrentRule() == null`.
     - `DescendantEvaluationContext.setEvaluationStatus(null, e.getStatus())` jumps straight to `top.setEvaluationStatus(null, e.getStatus())`, **overwriting `"outer-target"` with `"inner-target"`**.
  2. **Outer `Policy`/`PolicySet` target failure that does not go through `MustBePresent` leaves `parent.getEvaluationStatus()` empty**:
     When an outer `Policy` or `PolicySet` target fails because a PIP resolver throws `EvaluationException`, an XPath `AttributeSelector` throws `EvaluationException`, a `Match` predicate throws `EvaluationException`, or a custom `AttributeReference` throws `EvaluationException` (such as `FailingExpression` in [`NestedResponseStagingTest.java:87`](`xacml-core/src/test/java/org/xacml4j/v30/policy/NestedResponseStagingTest.java`)), 1-arg `setEvaluationStatusIfAbsent` is never called; only `Match.match` calls `context.setEvaluationStatus(null, e.getStatus())`. Because that call bypasses `DescendantEvaluationContext.setEvaluationStatus(Status)`, `this.evaluationStatus` on the outer `DescendantEvaluationContext` remains `null`. When the extended-Indeterminate pass starts, `parent.getEvaluationStatus().isPresent()` at [`DescendantEvaluationContext.java:106`](`xacml-core/src/main/java/org/xacml4j/v30/policy/DescendantEvaluationContext.java`) is `false`, so any obligation, advice, condition, or target failure inside the pass overwrites the outer target's status!
- **Fix**:
  In [`DescendantEvaluationContext.java:128-131`](`xacml-core/src/main/java/org/xacml4j/v30/policy/DescendantEvaluationContext.java`), route `rule == null` through `setEvaluationStatus(status)` (matching how [`RootEvaluationContext.setEvaluationStatus(DecisionRule, Status)`](`xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`) treats `rule == null` as the decision status):
  ```java
  @Override
  public void setEvaluationStatus(DecisionRule rule, Status status) {
  	if(rule == null){
  		setEvaluationStatus(status);
  		return;
  	}
  	top.setEvaluationStatus(rule, status);
  }
  ```

---

### `[P3-1]` Assert outer status in `aNestedIndeterminateTargetStillComputesItsOwnExtendedValue` and update the stale D34 sentence in `design.md`

- **Location**:
  - [`ExtendedIndeterminatePassPdpTest.java:92-100`](`xacml-pdp/src/test/java/org/xacml4j/v30/pdp/ExtendedIndeterminatePassPdpTest.java`)
  - [`design.md:1158`](`openspec/changes/optimize-pdp-evaluation/design.md`)
- **Problem**:
  1. [`ExtendedIndeterminatePassPdpTest.aNestedIndeterminateTargetStillComputesItsOwnExtendedValue`](`xacml-pdp/src/test/java/org/xacml4j/v30/pdp/ExtendedIndeterminatePassPdpTest.java`) asserts `Decision.INDETERMINATE_P` and empty responses, but omits asserting `result.getStatus()`, which allowed `[P2-1]` to pass unnoticed.
  2. In [`design.md:1158`](`openspec/changes/optimize-pdp-evaluation/design.md`), the D34 `responsesStaged` note still reads *"Advice and obligations do not run inside a pass (Terra's fix), so `responsesStaged` never fires there."*
- **Fix**:
  1. In [`ExtendedIndeterminatePassPdpTest.java:92-100`](`xacml-pdp/src/test/java/org/xacml4j/v30/pdp/ExtendedIndeterminatePassPdpTest.java`), add:
     ```java
     assertThat(result.getStatus().getCode().getValue()).isEqualTo(StatusCodeIds.MISSING_ATTRIBUTE);
     assertThat(result.getStatus().getMessage()).contains("outer-target");
     ```
  2. Apply the replacement text from [`REPORT-FIX-EXT2-opus.md:74-76`](`.ntm/worktrees/xacml4j--batch2/cc_1/REPORT-FIX-EXT2-opus.md`) to [`design.md:1158`](`openspec/changes/optimize-pdp-evaluation/design.md`) so D34 suppresses `responsesStaged` when `context.isExtendedIndeterminateEval()` is `true`.
