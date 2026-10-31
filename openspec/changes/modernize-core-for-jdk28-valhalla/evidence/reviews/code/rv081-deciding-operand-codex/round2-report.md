1. **MEDIUM — An overruled status can become the status of a later obligation failure.**  
   **Location:** `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DecidingOperands.java:81`.

   Concrete scenario: a Permit rule has condition `or(M, T)` and an obligation assignment containing `integer-divide(1, 0)`. `M` records missing-attribute; this return makes the condition succeed while leaving that status recorded. The obligation then fails with processing-error. Under the supplied fact that its failure handler uses `setEvaluationStatusIfAbsent`, the resulting Indeterminate reports missing-attribute.

   This diff makes that path reachable: previously, eager evaluation failed the condition before reaching the obligation. The round-1 condition/target finding is answered, but tracking this remaining path separately does not correct the newly reachable wrong status.

   **Suggested fix:** make the obligation failure handler overwrite the context status with the escaping exception’s status, as `Condition.evaluate` does. Add a decision-level regression for this exact scenario, expecting Indeterminate{P} with processing-error.

The remaining checks:

- **Logical combinations:** `or` and `and` preserve order, stop at the first deciding value, and otherwise rethrow the first failure. For valid nonnegative thresholds, `atLeast` is correct: before index `i`, `size - i` includes the current operand. Its upper bound therefore has no off-by-one error. Failed operands remain possible true values; enough actual true values decide true, and an insufficient upper bound decides false.
- **Higher-order functions:** all six match the supplied A.3.12 semantics. `all-of-any` implements ∀a∃b; `any-of-all` implements ∀b∃a, preserving predicate argument order. Array reuse is safe in the shown synchronous traversal: every position is assigned before a predicate invocation, including after an earlier failure.
- **Failures that must escape immediately:** failure of the `n-of` integer operand cannot be deferred because the threshold is unknown. Its evaluation and the shown threshold checks correctly occur outside the combiner. The supplied contract does not establish another forbidden category of runtime operand failure.
- **Lazy invocation:** the annotation wiring and `Expression` threshold parameter are consistent. Null variadic arguments correctly become empty operands, including `n-of(0)`. The diff does not show enough invocation-layer code to establish whether laziness bypasses any argument validation; there is no demonstrated validation defect here.
- **Tests:** the obligation-status row above is missing. Additional useful rows are a failing integer threshold with zero boolean evaluations, and counted higher-order predicate calls proving that evaluation stops after a deciding result. Existing higher-order tests assert outcomes without pinning call counts.

Verification was limited to the supplied diff and facts. The 7,709-test reactor result is supplied evidence; it does not cover the obligation-status scenario.

VERDICT: REQUEST_CHANGES
codex exit=0
