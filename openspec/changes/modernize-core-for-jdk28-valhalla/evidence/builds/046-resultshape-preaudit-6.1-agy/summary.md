# Run 046: donor pre-audit for task 6.1, ValueExprType result shapes (Agy)

Date: 2026-10-02. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity;
pane shows Gemini 3.8 Flash (High) (report self-label unreliable). Session
`policy4j--g1`, pane 3, 79 tool calls. Read-only: donor `b9f114a3`. Raw
report: [agy-report.txt](agy-report.txt) (A 22 defects, B interactions with
group 4, C tests).

Reported top defects: `ValueExpr.asSingle()` takes the first element with no
cardinality check (a Condition on an empty or multi-valued boolean bag picks
a value instead of Indeterminate); `AttributeReference.evaluatesTo()` declares
a single type while evaluation returns a bag (against D7); parameter specs
validate AST node kinds instead of ValueExprType;
`FunctionParamValueTypeSequenceSpec.java:121` compares a ValueType with an
Expr; `BaseDecisionRuleResponseExpression` never adds the built assignment.

## Conductor spot checks (2026-10-02)

Confirmed in the donor:
- `b9f114a3:xacml-core/src/main/java/org/xacml4j/v30/types/ValueExpr.java:86-88`:
  `asSingle()` is `stream().findFirst()`.
- `b9f114a3:xacml-core/src/main/java/org/xacml4j/v30/policy/BaseDecisionRuleResponseExpression.java:110-117`:
  each `AttributeAssignment.Builder b` is filled and never added to `attr`, so
  every obligation and advice would carry zero assignments.
Other rows unverified. The "17 CRITICAL" count reflects the reviewer's
scale, not a conductor rating.

Use: 6.1 implements D7 (declared result shapes; `requireExactlyOne()`;
`first()` only where the spec allows) against the 4.1/4.2/4.3 safety-net
tests; donor code is reference only. The obligation-assignment defect is a
regression the conformance suite catches (obligation and advice cases).
