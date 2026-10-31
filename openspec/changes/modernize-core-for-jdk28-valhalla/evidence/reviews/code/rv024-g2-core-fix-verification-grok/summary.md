# rv024: tasks 2.2/2.3 fix verification, xacml-core (Grok, adversarial)

Date: 2026-10-01. Lane: adversarial code reviewer, Grok Build grok-4.7
(xhigh), role `.ntm/roles/policy4j_code_review.md`, session cleared first.
Session `policy4j--g1`, pane 0. Read-only; did not run Maven (the
catch-deletion proof is the implementer's receipt 52 in run 010).

Scope: the reviewer's own rv019 findings after the repair (Target spies that
throw from match(), allowed by D4 for exception injection only).

Verdict: APPROVE. New weakening: none.

- Finding 1 (HIGH), RuleTest isMatch EvaluationException catch: FIXED
  (`RuleTest.java:495-513`; production `BaseDecisionRule.java:134-136`).
- Finding 2 (MEDIUM), PolicyTest and PolicySetTest isMatch Exception catch:
  FIXED (`PolicyTest.java:177-190`, `PolicySetTest.java:174-187`; production
  `BaseDecisionRule.java:138-141`).
- Spot-check of five other exception/status mappings (MatchTest,
  ConditionTest, AttributeDesignatorTest, MatchAllOfTest, TargetTest): each
  still forces the same caller boundary or the same MatchResult.
