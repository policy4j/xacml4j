# rv077 lane grok_4_7 — adversarial combining and security review

Requested model: grok-4.7. Actual model: Grok 4.7 (xAI). Tasks 4.6 and 4.7. Issue `policy4j-the.4.6` stays with conductor `codex-modernization-resume`. Read-only. No code, tests, specs, or Beads were edited. No Maven, Git, or subagents ran.

Verdict: do not close 4.6 or gate 4.7. Live code still fails the written specs for integer divide, NaN and signed zeros, XPath function errors, PIP/XPath status, and profile-correct `on-permit-apply-second`. Run 061 decisions D1–D3, D11, D16–D18 are pending and are not approval to keep legacy behavior. Follow the written spec and OASIS over fixtures and swarm recommendations. Existing code is the baseline, not the target semantics.

Coverage: cited source reads are SUPPORTED. Serena and ripwire were not used. OASIS HTML/PDF was not re-opened. Conformance fixtures were not re-read.

## Approved versus pending

In force: `function-semantics` (integer divide/mod, IEEE NaN and signed-zero equality, XPath functions are Indeterminate), `attribute-resolution` heading "Resolver failures are processing errors" (`spec.md:43`) distinct from MustBePresent (`spec.md:23`), and `combining-algorithm-registry` (the URN must resolve).

Pending, not inferred: D1–D18. D2(a) (NaN equals NaN) and D3(a) (PIP fail-open) contradict the written specs. Task 4.6's phrase "NaN equality" is not a decision to make NaN equal NaN.

Spec defect: `combining-algorithm-registry` requires the on-permit URN (`spec.md:7-11`) and also says the URN set stays identical to `a8a90d46` (`spec.md:17-24`). Task 6.3 says the `a8a90d46` set plus that URN. Both cannot hold.

## Findings

1. High. `on-permit-apply-second` is not profile-correct, and the safety net does not check the profile. `DefaultXacml30DecisionCombiningAlgorithms.java:33-71` never registers it. `DecisionCombiningAlgorithmRegistryTest.java:393-397` is `@Disabled` and only asserts non-null. `OnPermitApplySecondPolicyCombiningAlgorithm.java:62-64` rejects any length other than 2 with a fresh processing-error and `INDETERMINATE_DP`. Lines 67-68 return NotApplicable for a NotApplicable first child. Lines 71-72 return the second decision only when the first is Permit. Lines 70-94 still evaluate the second child when the first is Deny or Indeterminate, then overwrite status with a blank processing-error (`74-92`) or return NotApplicable (`94`). Source-reconciled CS01: child0 Deny yields NotApplicable for two children, and child2 for three. The two-child path returns a typed Indeterminate when child1 is Permit, Deny, or Indeterminate. Length other than 2 never reaches child2. The class comment (`37-44`) is a policy-level condition, not CS01. `OnPermitApplySecondPolicyTest.java` covers an empty list and first-Permit cases only. Minimal fix: implement that table, preserve the selected child's status, then register. Minimal test: two children and child0 Deny yield NotApplicable; three children and child0 Deny yield child2. Do not assert that child0 Deny returns Deny. Do not turn the null-check green by itself.

2. High. PIP and non-syntax XPath failures fail open. `DefaultEvaluationContextHandler.java:197-206` rethrows only a syntax `PathEvaluationException`; every other exception becomes `Optional.empty()`. `AttributeReference.java:132-142` turns a non-syntax exception into `forMissingRef` when MustBePresent is true, and into an empty result otherwise. `AttributeDesignator.java:93-100` and `AttributeSelector.java:77-83` then map absence to `missing-attribute` or an empty bag. The written heading at `attribute-resolution/spec.md:43` calls resolver failure a processing error. XACML 3.0 §5.29 and §7.3.5 use missing-attribute only when the attribute is absent. Minimal fix: propagate resolver and non-syntax XPath failures as `processing-error` with MustBePresent ignored; keep missing-attribute for a resolution that selects nothing. Minimal test: resolver throws, MustBePresent true and false, both Indeterminate `processing-error`. D3 does not authorize fail-open.

3. High. `and`, `or`, and `n-of` do not continue past Indeterminate. `XacmlFuncSpec.java:48` defaults `evaluateArguments` to true, but `JavaMethodToFunctionSpecConverter.java:81-105` and `:232-241` never read it and never call `lazyArgEval()`. `FunctionSpecBuilder.java:70` leaves `lazyArgumentEvaluation` false, so `FunctionSpecBuilder.java:383` pre-evaluates every argument and `:391-394` rethrows the first `EvaluationException`. `LogicalFunctions.java:59` and `:97` ask for lazy evaluation and are ignored. `n-of` (`:136-170`) evaluates inside the method and does not catch an error. XACML 3.0 §A.3.5 says a later False (`and`), True (`or`), or enough Trues (`n-of`) decides. The written function spec does not state this; 4.6/4.7 do not gate it. A.3.5 was not re-opened. Minimal fix: honor `evaluateArguments`, set `n-of` lazy, and continue after Indeterminate until the result is decided. Minimal test: `or(Indeterminate, true)` is true; `and(Indeterminate, false)` is false; `n-of(1, Indeterminate, true)` is true.

4. High against the written spec. `integer-divide` is a double. `ArithmeticFunctions.java:160-167` declares `xs:double` and returns `DoubleVal`. `IntegerVal.java:109-111` uses `long` division (toward zero) and then widens, so `5/2` is the double 2.0. `function-semantics/spec.md:3-11` requires `xs:integer`. D1 is pending; option (a) matches that spec. Minimal fix: return `IntegerVal`; keep `%` for `integer-mod` (`IntegerVal.java:114-116`). Minimal test: `5/2` is integer 2; divisor 0 is Indeterminate. Divide-by-zero status was not traced past `Preconditions.checkArgument`.

5. Medium against the written spec. `double-equal` uses Java equality. `EqualityPredicates.java:70-76` calls `_eq` (`:193-195`), and `Value.java:133-142` uses `value.equals`. `Double.equals` treats NaN as equal to NaN, and `Double.equals(+0.0, -0.0)` is false. Signed zeros are not already equal on this path. `function-semantics/spec.md:29-36` and XACML 3.0 §A.3.1 require NaN unequal and `0.0` equal to `-0.0`. Do not implement D2(a). Minimal fix: IEEE `==`, which is false for NaN and true for signed zeros. Do not use `Double.equals`. Minimal test: `double-equal(NaN, NaN)` is false and `double-equal(0.0, -0.0)` is true. This lane did not execute the call.

6. Medium, bounded. A.3.15 was not re-read. `XPathFunctions.java:61-66` returns 0 when content is absent or the type differs. Comments at `:88` and `:127` say missing Content is false for node-equal and node-match. `function-semantics/spec.md:92-104` says Indeterminate, not 0 or false. `XPathProvider.java:97-128` and `PathEvaluationException.java:42-47` throw `processing-error` for an evaluation failure; that path may already escape. The miss path does not. If A.3.15 really says 0/false for missing Content, the written spec is stricter until the maintainer amends it. Minimal test: the two scenarios already in the function spec.

7. Medium, recommendation only. D16/D17 are not decided, and no bound is in the written specs. `RegExpFunctions.java:130-145` runs `Matcher.find` with no length or time bound. If `cacheRegExps` is true, `:125-141` stores every pattern in an unbounded per-thread `HashMap`. `MultipleResourcesViaXPathExpressionHandler.java:151-160` collects every node path with no cap. On `PathEvaluationException`, `:162-166` returns the original category. Do not treat D16(a) or D17(a) as approved.

Checked, not filed: `DenyOverrides` and `PermitOverrides` track extended Indeterminate; `DenyUnlessPermit.java:51-58` and `PermitUnlessDeny.java:50-56` collapse Indeterminate as those algorithms require; `evaluateIfMatch` (`DecisionCombiningAlgorithms.java:50-52`) matches `createContext` plus `evaluate`. Obligation order was not traced.

## Recommendations

1. Split 4.6 into `xs:integer` divide, IEEE equality (NaN false, signed zeros true), and CS01 `combine()` before registration. Child0 Deny is NotApplicable for two children and child2 for three, not Deny.
2. Add PIP/XPath processing-error tests to the 4.7 gate. Run 068 does not cover them.
3. Amend the registry stability scenario so it allows the one new URN.
4. Leave regex and multi-resource caps out of 4.6 until the maintainer accepts a limit.
5. If A.3.15 disagrees with the XPath function spec, amend the spec; do not restore 0/false under the current text.

## Uncertainty and unrun gates

- The CS01 child0=Deny row is the source-reconciled correction. This lane did not re-open the OASIS text. A.3.15 and A.3.5 were not re-opened; those findings stay on the lines cited.
- `FunctionSpecImpl.evaluate` was not opened. Eager pre-evaluation is cited at `FunctionSpecBuilder.java:383`.
- `forMissingRef` construction, divide-by-zero status, and obligation isolation were not traced.
- No JDK 21 or JDK 28 run. Gate 4.7 is unrun. Run 068 does not complete 4.6 or 4.7.
- Disabled registry test, integer-divide caller audit, IIC020, IIC350/IIC358, and the disabled PIP PDP tests were not executed.
- No BLOCKER.

DONE_GROK_4_7_20261004
