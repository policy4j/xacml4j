package org.xacml4j.v30.pdp;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.content.XPathProvider;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.spi.audit.PolicyDecisionAuditor;
import org.xacml4j.v30.spi.pdp.PolicyDecisionCache;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.repository.PolicyRepository;
import org.xacml4j.v30.spi.repository.PolicyRepositoryListener;


public class DefaultPolicyDecisionPointTest
{
	private PolicyDecisionPoint pdp;

	private PolicyRepository repository;
	private PolicyInformationPoint pip;
	private CompositeDecisionRule policyDomain;
	private PolicyDecisionCache decisionCache;
	private PolicyDecisionAuditor decisionAuditor;
	private XPathProvider xpathProvider;


	private PolicyDecisionPointBuilder pdpBuilder;

	@BeforeEach
	public void init()
	{
		this.pip = Mockito.mock(PolicyInformationPoint.class);
		this.repository = Mockito.mock(PolicyRepository.class);
		this.policyDomain = Mockito.mock(CompositeDecisionRule.class);
		this.decisionAuditor = Mockito.mock(PolicyDecisionAuditor.class);
		this.decisionCache = Mockito.mock(PolicyDecisionCache.class);
		this.xpathProvider = Mockito.mock(XPathProvider.class);
		this.pdpBuilder = PolicyDecisionPointBuilder
		.builder("testPdp")
			.decisionAuditor(decisionAuditor)
			.policyRepository(repository)
			.decisionCache(decisionCache)
			.pip(pip)
			.rootPolicy(policyDomain)
			.xpathProvider(xpathProvider);
	}

	@Test
	public void testRequestEvaluationPolicyDomainEvaluatesToPermitAndRequestReturnEvaluatedPolicyIdsFalse()
	{
		RequestContext req = RequestContext
				.builder()
				.returnPolicyIdList(false)
				.build();

		ArgumentCaptor<PolicyRepositoryListener> c = ArgumentCaptor.forClass(PolicyRepositoryListener.class);
		// The listener registration is verified after the builder constructs the PDP.

		when(decisionCache.getDecision(req)).thenReturn(null);
		ArgumentCaptor<EvaluationContext> rootContext = ArgumentCaptor.forClass(EvaluationContext.class);
		EvaluationContext evaluationContext = Mockito.mock(EvaluationContext.class);
		when(policyDomain.createContext(Mockito.any())).thenReturn(evaluationContext);
		ArgumentCaptor<EvaluationContext> policyContext = ArgumentCaptor.forClass(EvaluationContext.class);
		when(policyDomain.evaluate(Mockito.any())).thenReturn(Decision.PERMIT);
		ArgumentCaptor<Result> result0 = ArgumentCaptor.forClass(Result.class);
		ArgumentCaptor<PolicyDecisionPoint> pdp1 = ArgumentCaptor.forClass(PolicyDecisionPoint.class);
		// Captured and verified after decide().
		ArgumentCaptor<Result> result1 = ArgumentCaptor.forClass(Result.class);
		// Captured and verified after decide().

		this.pdp = pdpBuilder.build();
		Mockito.verify(repository).addPolicyRepositoryListener(c.capture());

		ResponseContext res = pdp.decide(req);
		Mockito.verify(decisionCache).getDecision(req);
		Mockito.verify(policyDomain).createContext(rootContext.capture());
		Mockito.verify(policyDomain).evaluate(policyContext.capture());
		Mockito.verify(decisionAuditor).audit(pdp1.capture(), result0.capture(), eq(req));
		Mockito.verify(decisionCache).putDecision(eq(req), result1.capture(), eq(Duration.ofSeconds(30)));
		assertEquals(1, res.getResults().size());
		assertEquals(result0.getValue(), result1.getValue());
		assertSame(pdp, pdp1.getValue());
		Mockito.verifyNoInteractions(evaluationContext);
		Mockito.verifyNoMoreInteractions(pip, repository, policyDomain, decisionAuditor, decisionCache, xpathProvider);
	}

	@Test
	public void testRequestEvaluationPolicyDomainEvaluatesToDenyAndRequestReturnEvaluatedPolicyIdsFalse()
	{
		RequestContext req = RequestContext
				.builder()
				.returnPolicyIdList(false)
				.build();

		ArgumentCaptor<PolicyRepositoryListener> c = ArgumentCaptor.forClass(PolicyRepositoryListener.class);
		// The listener registration is verified after the builder constructs the PDP.

		when(decisionCache.getDecision(req)).thenReturn(null);
		ArgumentCaptor<EvaluationContext> rootContext = ArgumentCaptor.forClass(EvaluationContext.class);
		EvaluationContext evaluationContext = Mockito.mock(EvaluationContext.class);
		when(policyDomain.createContext(Mockito.any())).thenReturn(evaluationContext);
		ArgumentCaptor<EvaluationContext> policyContext = ArgumentCaptor.forClass(EvaluationContext.class);
		when(policyDomain.evaluate(Mockito.any())).thenReturn(Decision.DENY);

		ArgumentCaptor<Result> result0 = ArgumentCaptor.forClass(Result.class);
		ArgumentCaptor<PolicyDecisionPoint> pdp1 = ArgumentCaptor.forClass(PolicyDecisionPoint.class);

		// Captured and verified after decide().
		ArgumentCaptor<Result> result1 = ArgumentCaptor.forClass(Result.class);
		ArgumentCaptor<RequestContext> req1 = ArgumentCaptor.forClass(RequestContext.class);
		// Captured and verified after decide().
		this.pdp = pdpBuilder.build();
		Mockito.verify(repository).addPolicyRepositoryListener(c.capture());

		ResponseContext res = pdp.decide(req);
		Mockito.verify(decisionCache).getDecision(req);
		Mockito.verify(policyDomain).createContext(rootContext.capture());
		Mockito.verify(policyDomain).evaluate(policyContext.capture());
		Mockito.verify(decisionAuditor).audit(pdp1.capture(), result0.capture(), eq(req));
		Mockito.verify(decisionCache).putDecision(req1.capture(), result1.capture(), eq(Duration.ofSeconds(30)));
		assertEquals(1, res.getResults().size());
		assertEquals(result0.getValue(), result1.getValue());
		assertSame(pdp, pdp1.getValue());
		Mockito.verifyNoInteractions(evaluationContext);
		Mockito.verifyNoMoreInteractions(pip, repository, policyDomain, decisionAuditor, decisionCache, xpathProvider);
	}
}
