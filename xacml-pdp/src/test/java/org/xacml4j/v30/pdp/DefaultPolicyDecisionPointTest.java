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

import org.easymock.Capture;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.*;
import org.xacml4j.v30.spi.audit.PolicyDecisionAuditor;
import org.xacml4j.v30.spi.pdp.PolicyDecisionCache;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.repository.PolicyReferenceResolver;
import org.xacml4j.v30.xpath.XPathProvider;

import java.util.concurrent.TimeUnit;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;


public class DefaultPolicyDecisionPointTest
{
	private PolicyDecisionPoint pdp;

	private PolicyReferenceResolver repository;
	private PolicyInformationPoint pip;
	private CompositeDecisionRule policyDomain;
	private PolicyDecisionCache decisionCache;
	private PolicyDecisionAuditor decisionAuditor;
	private XPathProvider xpathProvider;

	private IMocksControl control;

	private PolicyDecisionPointBuilder pdpBuilder;

	@Before
	public void init()
	{
		this.control = createControl();
		this.pip = control.createMock(PolicyInformationPoint.class);
		this.repository = control.createMock(PolicyReferenceResolver.class);
		this.policyDomain = control.createMock(CompositeDecisionRule.class);
		this.decisionAuditor = control.createMock(PolicyDecisionAuditor.class);
		this.decisionCache = control.createMock(PolicyDecisionCache.class);
		this.xpathProvider = control.createMock(XPathProvider.class);
		this.pdpBuilder = PolicyDecisionPointBuilder
		.builder("testPdp")
			.decisionAuditor(decisionAuditor)
			.policyResolver(repository)
			.decisionCache(decisionCache)
			.decisionCacheTTL(10)
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

		expect(decisionCache.getDecision(req)).andReturn(null);
		Capture<DecisionRuleEvaluationContext> rootContext = new Capture<DecisionRuleEvaluationContext>();
		expect(policyDomain.createContext(capture(rootContext))).andReturn(control.createMock(DecisionRuleEvaluationContext.class));
		Capture<DecisionRuleEvaluationContext> policyContext = new Capture<DecisionRuleEvaluationContext>();
		expect(policyDomain.evaluate(capture(policyContext))).andReturn(Decision.PERMIT);
		Capture<Result> result0 = new Capture<Result>();
		Capture<PolicyDecisionPoint> pdp1 = new Capture<PolicyDecisionPoint>();
		decisionAuditor.audit(capture(pdp1), capture(result0), eq(req));
		Capture<Result> result1 = new Capture<Result>();
		decisionCache.putDecision(eq(req), capture(result1), eq(TimeUnit.SECONDS), eq(10));

		control.replay();

		this.pdp = pdpBuilder.build();

		ResponseContext res = pdp.decide(req);
		assertEquals(1, res.getResults().size());
		assertEquals(result0.getValue(), result1.getValue());
		assertSame(pdp, pdp1.getValue());
		control.verify();
	}

	@Test
	public void testRequestEvaluationPolicyDomainEvaluatesToDenyAndRequestReturnEvaluatedPolicyIdsFalse()
	{
		RequestContext req = RequestContext
				.builder()
				.returnPolicyIdList(false)
				.build();


		expect(decisionCache.getDecision(req)).andReturn(null);
		Capture<DecisionRuleEvaluationContext> rootContext = new Capture<DecisionRuleEvaluationContext>();
		expect(policyDomain.createContext(capture(rootContext))).andReturn(control.createMock(DecisionRuleEvaluationContext.class));
		Capture<DecisionRuleEvaluationContext> policyContext = new Capture<DecisionRuleEvaluationContext>();
		expect(policyDomain.evaluate(capture(policyContext))).andReturn(Decision.DENY);

		Capture<Result> result0 = new Capture<Result>();
		Capture<PolicyDecisionPoint> pdp1 = new Capture<PolicyDecisionPoint>();

		decisionAuditor.audit(capture(pdp1), capture(result0), eq(req));
		Capture<Result> result1 = new Capture<Result>();
		decisionCache.putDecision(eq(req), capture(result1), eq(TimeUnit.SECONDS), eq(10));

		control.replay();
		this.pdp = pdpBuilder.build();

		ResponseContext res = pdp.decide(req);
		assertEquals(1, res.getResults().size());
		assertEquals(result0.getValue(), result1.getValue());
		assertSame(pdp, pdp1.getValue());

		control.verify();
	}
}
