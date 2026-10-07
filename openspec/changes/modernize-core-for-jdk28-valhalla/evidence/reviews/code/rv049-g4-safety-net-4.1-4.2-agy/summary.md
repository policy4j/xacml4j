# rv049: tasks 4.1 and 4.2 review, attribute-resolution and condition safety-net tests (Agy)

Date: 2026-10-02. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity;
pane shows Gemini 3.8 Flash (High); the report labels itself
claude-3-7-sonnet (unreliable self-label). Session `policy4j--g1`, pane 3.
Read-only: lane g41 patch (407 lines) and its baseline.

Scope: ConditionSafetyNetTest (xacml-core, 4 tests, 1 disabled) and
AttributeResolutionSafetyNetTest (xacml-pdp, 12 tests incl. a 6-row matrix,
3 disabled). Tests only; no production change (reviewer diff check).

Verdict: APPROVE (2 LOW).

Coverage (reviewer): every item of tasks 4.1 and 4.2 asserted with decision
and status code where the task says so: designator result typed as a bag;
MustBePresent x missing x resolver failure (today's behavior pinned in rows
named `...-current`, fail-closed expectations disabled with
policy4j-the.4.34); issuer filtering (match, mismatch, designator without
issuer); attribute-designator function by category URI and by entity;
condition absent/true/false, empty and multi-valued one-and-only ->
Indeterminate with processing-error; non-boolean declared expressions
rejected. Run 036 Table B rows omitted are non-material (Deny-effect
variants, a multi-value designator evaluate). Disabled tests carry 4.32,
4.33, 4.34. Mockito only for the PIP.

Findings (line numbers as reported; they may be patch offsets):
1. LOW: the 4.32 selector test sits in ConditionSafetyNetTest and builds a
   RootEvaluationContext with an anonymous EvaluationContextHandler
   (internal classes); move it when tasks 6.1/6.5 replace them.
2. LOW: issuer and attribute-designator function tests assert the decision
   but not StatusCodeId.OK.
