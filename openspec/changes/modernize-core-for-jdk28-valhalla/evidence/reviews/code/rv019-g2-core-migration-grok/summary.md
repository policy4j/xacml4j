# rv019: tasks 2.2 and 2.3 review, xacml-core JUnit 5 + Mockito (Grok, adversarial)

Date: 2026-10-01. Lane: adversarial code reviewer, Grok Build grok-4.7
(xhigh), role `.ntm/roles/policy4j_code_review.md`, session cleared first.
Session `policy4j--g1`, pane 0. Read-only, risk-focused (real-instance
substitutions, strict mocks, caches, generated count checks, exception tests).

Verdict: REQUEST_CHANGES.

Findings:
1. HIGH, `RuleTest.java:501-513` (baseline `RuleTest.java:402`): the stubbed
   inner expression sets the evaluation status and then throws; Match swallows
   the exception, so BaseDecisionRule.isMatch's catch never runs. Deleting
   that catch leaves INDETERMINATE_P and both "Bad" status assertions green.
   Fix: throw from Target.match and do not set the status in the stub.
2. MEDIUM, `PolicyTest.java:173-183`, `PolicySetTest.java:171-183` (baseline
   throws at `PolicyTest.java:160`, `PolicySetTest.java:159`): the
   NullPointerException is now caught by MatchAllOf.evaluate
   (`MatchAllOf.java:98-100`), so isMatch's catch (Exception)
   (`BaseDecisionRule.java:138-141`) is not required; Status.equals treats the
   inner processing-error status as the expected one. Fix: throw the
   NullPointerException from Target.match.

Checked (reviewer): compared migrated tests with run 010 `before/` and the
production paths in Match, MatchAllOf, MatchAnyOf, Rule, BaseDecisionRule,
DescendantEvaluationContext, Status, CoreException; read
44-exact-cumulative-verification.py, ModelFixtures, PolicyIDReferenceTest,
FirstApplicableTest, DenyOverridesTest, AttributeDesignatorTest,
ConditionTest, DefaultFunctionSpecTest. Other real-model substitutions opened
still force the mocked decision and short-circuit.

Residual (reviewer): the 470 generated count lines were not all read; the
760-test gate was not re-executed; PolicyTest.testEvaluateTargetMatchConditionTrue
stubs false as in the baseline.

Resolution: repair packet 2.3b to the implementer lane.
