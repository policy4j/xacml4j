# rv011: group 1 round 6 review, repository listener test barrier (Grok, adversarial)

Date: 2026-10-01. Lane: adversarial code reviewer, Grok Build grok-4.7
(xhigh), role `.ntm/roles/policy4j_code_review.md`, session cleared first.
Session `policy4j--g1`, pane 0. Read-only; ran git diff/--check, rg,
ast-grep scan, and javap on EasyMock 5.1.0 (ReplayState, MocksBehavior,
MocksControl); did not run Maven.

Scope: test-only fix in `xacml-pdp/.../InMemoryPolicyRepositoryTest.java`
(+40/-3) that made the full gate stable (packet 1.3k; run 005 "Round 4 gate":
26/30 passes before, 30/30 after).

Verdict: APPROVE. Findings: none.

Reviewer statements:
- Production code (AbstractPolicyRepository, InMemoryPolicyRepository) has
  no diff. One forwarding listener per test calls the EasyMock listener, then
  counts down; a rejected (wrong method or argument) call throws before
  countDown, so it cannot open the latch. EasyMock 5.1.0 default mocks are not
  nice. Policy.equals includes id and version.
- All four c.verify() calls (`:175, :200, :219, :257`) follow a bounded
  five-second await with an explicit timeout message; counts 4, 2, 1, 4
  match the notifications production enqueues (add/remove notify only on
  change; putIfAbsent does not re-notify). CountDownLatch gives
  happens-before to verify. No Thread.sleep; expectations and repository
  assertions unchanged; header and tabs kept.
- An extra callback after the wait is not reliably detected; acceptable
  because these tests enqueue exactly N notifications, and an extra call that
  lands before verify is still reported by MocksBehavior.verify.

Found outside scope (pre-existing production defect):
`AbstractPolicyRepository.java:188-191` notifyPolicySetRemoved submits
policySetAdded. No test removes a policy set. Tracked as a beads bug.
