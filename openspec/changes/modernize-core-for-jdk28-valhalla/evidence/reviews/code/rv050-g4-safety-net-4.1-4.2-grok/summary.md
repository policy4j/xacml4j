# rv050: tasks 4.1 and 4.2 adversarial review, safety-net tests (Grok)

Date: 2026-10-02. Lane: adversarial code reviewer, Grok Build grok-4.7
(xhigh), role `.ntm/roles/policy4j_code_review.md`, session cleared first.
Session `policy4j--g1`, pane 0. Read-only (lane g41 patch, both tests, the
resolution and condition sources, run 036/038 evidence, XACML 3.0 OS 5.26,
5.29, 5.30, 7.3.5, 7.9-7.11). `ast-grep scan` on both files: no findings.
Report text from the Grok session's chat history.

Verdict: REQUEST_CHANGES (3 MEDIUM, 1 LOW).

1. MEDIUM, `AttributeResolutionSafetyNetTest.java:201-206`: the
   `resolver-failure-*-current` rows expect the same outcomes as the
   `missing-*` rows, so deleting PIP resolution
   (`DefaultEvaluationContextHandler.java:147-162`) stays green; the mock is
   never verified and the selector overload is never stubbed. NotApplicable
   status is always `ok` (`DefaultPolicyDecisionPoint.java:177-183`). Fix: a
   row where the request lacks the attribute and the PIP returns it
   (Permit), and `verify(pip).resolve(...)` on the failure rows.
2. MEDIUM, `:249-276`: the attribute-designator function is tested only with
   the attribute present and mustBePresent=false; the disabled 4.33 test
   checks NotApplicable but not an empty bag, and only for false. Fix: for an
   existing category and for an entity, attribute absent with false ->
   NotApplicable/ok and with true -> Indeterminate/missing-attribute; a
   disabled absent-category row with true -> missing-attribute; assert bag
   size 0.
3. MEDIUM, `:290-301`, `:397`: the issuer mismatch case uses
   MustBePresent=false (NotApplicable either way), so a null attribute issuer
   matching any designator issuer (`AttributeIndex.java:108`) stays green;
   every condition case uses Effect Permit, so Deny-rule results (7.11 Table
   4: Indeterminate{D}) are unpinned. Fix: issuer set, attribute issuer null,
   MustBePresent=true -> Indeterminate/missing-attribute; absent, true and
   indeterminate conditions with Effect Deny -> Deny, Deny, Indeterminate{D}.
4. LOW, `ConditionSafetyNetTest.java:79-113`: the 4.32 fixture uses
   `BagOfValues` signatures that task 5.2 renames; keep, update mechanically.

Confirmed locked: the six matrix rows (decision and status), five condition
outcomes, MustBePresent on the AttributeDesignator element, disabled tests
4.32/4.33/4.34 with correct ids and decisions; Mockito only for the PIP; LGPL
headers; tests only.
Residual: Category and DataType are not varied as match keys; the PDP
returns Decision.INDETERMINATE_P on Result (the XML response prints plain
Indeterminate).
Resolution: follow-up packet to the implementer (lane g41).
