# rv021: task 2.4 fix verification, xacml-pdp lane (Grok, adversarial)

Date: 2026-10-01. Lane: adversarial code reviewer, Grok Build grok-4.7
(xhigh), role `.ntm/roles/policy4j_code_review.md`, session cleared first.
Session `policy4j--g1`, pane 0. Read-only; compared the repaired lane, the
baseline and the patch; read the lane's mutation logs. Did not run Maven.

Scope: the reviewer's own rv015 findings after the lane repair.

Verdict: APPROVE. New weakening: none.

Per finding (migrated lines):
1. FIXED, DefaultPolicyReferenceResolverTest :72, :79-80, :88, :95-96:
   getPolicy times(2), getPolicySet times(1), one listener registration,
   verifyNoMoreInteractions; a no-op policyRemoved and a missing registration
   now fail.
2. FIXED, DefaultEvaluationContextHandlerTest :218-221, :276-278: cache counts;
   mutation-selector-cache.log fails on a cache bypass.
3. FIXED, :406-408: verify(pip, times(1)); mutation log fails on a second call.
4. FIXED, DefaultPolicyInformationPointTest :198-203, :242-247: InOrder plus
   verifyNoMoreInteractions.
5. FIXED, :218-222 (still @Disabled): times(2) getAttributes.
6. FIXED, AnnotatedResolverFactoryTest :113-116, :158; both mutation logs fail
   as intended.
7. FIXED, DefaultEvaluationContextHandlerTest :292-294, :306-308, :321-323.
   The reviewer agrees with the conductor: the baseline status expectation was
   unreachable after the throw; the post-throw getEntity verify is stronger
   than the baseline.
8. FIXED, DefaultResolverRegistryTest :79, :102, :140, :176, :200 and
   ResolverRegistryBuilderTest :46: verifyNoInteractions.

Residual (accepted): the finding 3 test has no verifyNoMoreInteractions on
the callback or context; the requested pip count is present.
Lane run: xacml-pdp 73 tests, 0 failures, 0 errors, 3 skipped.
