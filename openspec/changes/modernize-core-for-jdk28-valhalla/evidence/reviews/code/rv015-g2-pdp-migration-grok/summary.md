# rv015: task 2.4 review, xacml-pdp JUnit 5 + Mockito lane (Grok, adversarial)

Date: 2026-10-01. Lane: adversarial code reviewer, Grok Build grok-4.7
(xhigh), role `.ntm/roles/policy4j_code_review.md`, session cleared first.
Session `policy4j--g1`, pane 0. Read-only: read design D4, the lane summary,
all 20 baseline and migrated sources; compared per-method expect / times /
anyTimes / Capture / verify / when / ArgumentCaptor / assertThrows counts and
read every method whose counts changed; checked Mockito 5.24.0
MockHandlerImpl with javap. Did not run Maven.

Verdict: REQUEST_CHANGES.

Findings (line numbers: migrated lane copy; baseline lines in parentheses):
1. HIGH, DefaultPolicyReferenceResolverTest :69, :84 (baseline :72-73,
   :89-90): baseline requires getPolicy exactly twice (second resolve after
   policyRemoved must miss the cache) and getPolicySet exactly once
   (policySetRemoved must keep the policy-set cache). The migration stubs with
   thenReturn and only assertSame, so a no-op policyRemoved still passes.
   Direct `addPolicyRepositoryListener(Mockito.any())` calls invoke the mock
   and nothing verifies the constructor registration. Fix: delete the direct
   calls; verify listener registration once, getPolicy times(2), getPolicySet
   times(1).
2. HIGH, DefaultEvaluationContextHandlerTest :212-219, :266-273 (baseline
   :224-233, :285-295): the second resolve is the cache path; baseline fails if
   the PIP is hit again; migration asserts the same bag twice, so a cache miss
   passes. Fix: verify getEntity and pip.resolve counts as in the baseline.
3. HIGH, DefaultEvaluationContextHandlerTest :407-420 (baseline :448-458):
   baseline's only oracle was c.verify() (pip.resolve once, re-entrant); the
   migration verifies nothing. Fix: verify(pip, times(1)).resolve(context, ref).
4. MEDIUM, DefaultPolicyInformationPointTest :251-261 (baseline :256-270):
   four recorded expectations (cache.getAttributes, context.resolve,
   context.getClock, cache.putAttributes); only putAttributes verified. Fix:
   the InOrder block used in testMatchingResolverFoundAndResultIsCacheable.
5. MEDIUM, DefaultPolicyInformationPointTest :220-239 (disabled; baseline
   :226-238): the migration verifies a different contract than the baseline
   recorded (two getAttributes, others commented out). Fix: verify(cache,
   times(2)).getAttributes(any()) only.
6. MEDIUM, AnnotatedResolverFactoryTest :90-112, :147-154 (baseline :93-118,
   :153, :161-170): control.verify() was the only proof that both designators
   and getClock were resolved, and that resolve2 does not touch the context.
   Fix: verify both resolves and getClock plus verifyNoMoreInteractions;
   verifyNoInteractions(context) for resolve2.
7. LOW, DefaultEvaluationContextHandlerTest :280-294, :299-314, :320-335:
   verify/assertNotNull after the throwing call inside assertThrows never run
   (same as rv014 finding 1). Fix: lambda wraps only handler.resolve.
8. LOW, DefaultResolverRegistryTest :64-79 and ResolverRegistryBuilderTest
   :44-46: strict empty controls used to fail any EvaluationContext call; no
   assertion remains. Fix: verifyNoInteractions(context) where the baseline
   called control.verify().

Checked, no weakening (reviewer): request-handler strict sites (InOrder,
times(N), verifyNoMoreInteractions; getAllValues where needed);
RequestContextHandlerChainTest counts still constrain the system under test
although the test calls the mock directly (rv014 finding 2 disagrees on
severity; the repair removes the direct calls); DefaultPolicyDecisionPointTest
verifications; InMemoryPolicyRepositoryTest latch (rv011) kept; assertThrows
types match; @Disabled kept on the five former @Ignore sites.

Residual (reviewer): verifyNoMoreInteractions missing on
DefaultPolicyDecisionPointTest and the InMemoryPolicyRepositoryTest listener;
pre-existing duplicated r0 assertion in
MultipleResourcesViaRepeatingAttributesHandlerTest:119 (baseline unchanged).
