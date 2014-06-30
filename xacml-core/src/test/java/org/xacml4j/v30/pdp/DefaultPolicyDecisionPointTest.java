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

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Collections;

import org.easymock.Capture;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.spi.audit.PolicyDecisionAuditor;
import org.xacml4j.v30.spi.pdp.PolicyDecisionCache;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.repository.PolicyRepository;
import org.xacml4j.v30.spi.repository.PolicyRepositoryListener;
import org.xacml4j.v30.spi.xpath.XPathProvider;


public class DefaultPolicyDecisionPointTest
{
	private PolicyDecisionPoint pdp;

	private PolicyRepository repository;
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
		this.repository = control.createMock(PolicyRepository.class);
		this.policyDomain = control.createMock(CompositeDecisionRule.class);
		this.decisionAuditor = control.createMock(PolicyDecisionAuditor.class);
		this.decisionCache = control.createMock(PolicyDecisionCache.class);
		this.xpathProvider = control.createMock(XPathProvider.class);
		this.pdpBuilder = PolicyDecisionPointBuilder
		.builder("testPdp")
			.decisionAuditor(decisionAuditor)
			.policyRepository(repository)
			.decisionCache(decisionCache)
			.decisionCacheTTL(10)
			.pip(pip)
			.rootPolicy(policyDomain)
			.xpathProvider(xpathProvider);
	}

	@Test
	public void testRequestEvaluationPolicyDomainEvaluatesToPermitAndRequestReturnEvaluatedPolicyIdsFalse()
	{

		RequestContext req = new RequestContext(false, Collections.<Category>emptyList());

		Capture<PolicyRepositoryListener> c = new Capture<PolicyRepositoryListener>();
		repository.addPolicyRepositoryListener(capture(c));

		expect(decisionCache.getDecision(req)).andReturn(null);
		Capture<EvaluationContext> rootContext = new Capture<EvaluationContext>();
		expect(policyDomain.createContext(capture(rootContext))).andReturn(control.createMock(EvaluationContext.class));
		Capture<EvaluationContext> policyContext = new Capture<EvaluationContext>();
		expect(policyDomain.evaluate(capture(policyContext))).andReturn(Decision.PERMIT);
		Capture<Result> result0 = new Capture<Result>();
		Capture<PolicyDecisionPoint> pdp1 = new Capture<PolicyDecisionPoint>();
		decisionAuditor.audit(capture(pdp1), capture(result0), eq(req));
		Capture<Result> result1 = new Capture<Result>();
		decisionCache.putDecision(eq(req), capture(result1), eq(10));

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

		RequestContext req = new RequestContext(false, Collections.<Category>emptyList());

		Capture<PolicyRepositoryListener> c = new Capture<PolicyRepositoryListener>();
		repository.addPolicyRepositoryListener(capture(c));

		expect(decisionCache.getDecision(req)).andReturn(null);
		Capture<EvaluationContext> rootContext = new Capture<EvaluationContext>();
		expect(policyDomain.createContext(capture(rootContext))).andReturn(control.createMock(EvaluationContext.class));
		Capture<EvaluationContext> policyContext = new Capture<EvaluationContext>();
		expect(policyDomain.evaluate(capture(policyContext))).andReturn(Decision.DENY);

		Capture<Result> result0 = new Capture<Result>();
		Capture<PolicyDecisionPoint> pdp1 = new Capture<PolicyDecisionPoint>();

		decisionAuditor.audit(capture(pdp1), capture(result0), eq(req));
		Capture<Result> result1 = new Capture<Result>();
		decisionCache.putDecision(eq(req), capture(result1), eq(10));

		control.replay();
		this.pdp = pdpBuilder.build();

		ResponseContext res = pdp.decide(req);
		assertEquals(1, res.getResults().size());
		assertEquals(result0.getValue(), result1.getValue());
		assertSame(pdp, pdp1.getValue());

		control.verify();
	}
}
