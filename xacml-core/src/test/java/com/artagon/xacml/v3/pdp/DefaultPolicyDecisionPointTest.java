package com.artagon.xacml.v3.pdp;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.ResponseContext;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.spi.PolicyDomain;
import com.artagon.xacml.v3.spi.PolicyInformationPoint;
import com.artagon.xacml.v3.spi.PolicyRepository;
import com.artagon.xacml.v3.spi.XPathProvider;

public class DefaultPolicyDecisionPointTest 
{
	private PolicyDecisionPoint pdp;
	
	private PolicyRepository repository;
	private PolicyInformationPoint pip;
	private PolicyDomain policyDomain;
	private PolicyDecisionPointContextFactory factory;
	private PolicyDecisionCache decisionCache;
	private PolicyDecisionAuditor decisionAuditor;
	private XPathProvider xpathProvider;
	
	@Before
	public void init()
	{
		this.pip = createStrictMock(PolicyInformationPoint.class);
		this.repository = createStrictMock(PolicyRepository.class);
		this.policyDomain = createStrictMock(PolicyDomain.class);
		this.decisionAuditor = createStrictMock(PolicyDecisionAuditor.class);
		this.decisionCache = createStrictMock(PolicyDecisionCache.class);
		this.xpathProvider = createStrictMock(XPathProvider.class);
		this.factory = new DefaultPolicyDecisionPointContextFactory(
				policyDomain, 
				repository, 
				decisionAuditor, 
				decisionCache, 
				xpathProvider, 
				pip);
		this.pdp = new DefaultPolicyDecisionPoint(factory);
	}
	
	@Test
	public void testRequestEvaluationPolicyDomainEvaluatesToPermitAndRequestReturnEvaluatedPolicyIdsFalse()
	{
		RequestContext req = new RequestContext(false, Collections.<Attributes>emptyList());
		expect(decisionCache.getDecision(req)).andReturn(null);
		Capture<EvaluationContext> context = new Capture<EvaluationContext>();
		expect(policyDomain.evaluate(capture(context))).andReturn(Decision.PERMIT);
		Capture<Result> result0 = new Capture<Result>();
		decisionAuditor.audit(capture(result0), eq(req));
		Capture<Result> result1 = new Capture<Result>();
		decisionCache.putDecision(eq(req), capture(result1));
		replay(repository, pip, policyDomain, decisionCache, decisionAuditor);
		ResponseContext res = pdp.decide(req);
		assertEquals(1, res.getResults().size());
		assertEquals(result0.getValue(), result1.getValue());
		verify(repository, pip, policyDomain, decisionCache, decisionAuditor);
	}
}
