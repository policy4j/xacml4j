# rv061: rv056 fix verification, lane g43 (Agy)

Date: 2026-10-02. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity;
pane shows Gemini 3.8 Flash (High) (self-label unreliable). Session
`policy4j--g1`, pane 3, 32 tool calls. Read-only: regenerated g43 patch
(1,842 lines, 21 files) and evidence 042/rv056.

Verdict: APPROVE.

- 4.43 FIXED: `FunctionInvocationException.statusFor` and
  `DefaultFunctionInvocation.invocationCause` keep the first
  `EvaluationException` status in the cause chain; every constructor uses
  it; other failures stay processing-error. FunctionInvocationStatusTest
  8/0, PDP lane test 2/0. The higher-order split (runtime parameter
  validation off -> the inner syntax-error surfaces; on -> processing-error
  from pre-validation) follows from the fix; the oracle in
  HigherOrderVariadicFunctionsTest encodes it.
- 4.39 FIXED: `RFC822Name.matches` returns false for an empty pattern; row in
  FunctionSafetyNet43Test.
- 4.33 change in the lane `AttributeDesignatorFunctions` is identical to the
  main worktree version; keeping the main version changes nothing.
(Reported line numbers appear to be patch offsets.)
