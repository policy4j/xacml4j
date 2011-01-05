package com.artagon.xacml.v3.pdp;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.easymock.Capture;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.ResponseContext;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.spi.audit.PolicyDecisionAuditor;
import com.artagon.xacml.v3.spi.dcache.PolicyDecisionCache;
import com.artagon.xacml.v3.spi.pip.PolicyInformationPoint;
import com.artagon.xacml.v3.spi.repository.PolicyRepository;
import com.artagon.xacml.v3.spi.xpath.XPathProvider;

public class DefaultPolicyDecisionPointTest 
{
	private PolicyDecisionPoint pdp;
	
	private PolicyRepository repository;
	private PolicyInformationPoint pip;
	private CompositeDecisionRule policyDomain;
	private PolicyDecisionPointContextFactory factory;
	private PolicyDecisionCache decisionCache;
	private PolicyDecisionAuditor decisionAuditor;
	private XPathProvider xpathProvider;
	
	private IMocksControl control;
	
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
	@Ignore
	public void testRequestEvaluationPolicyDomainEvaluatesToPermitAndRequestReturnEvaluatedPolicyIdsFalse()
	{
		RequestContext req = new RequestContext(false, Collections.<Attributes>emptyList());
		expect(decisionCache.getDecision(req)).andReturn(null);
		Capture<EvaluationContext> rootContext = new Capture<EvaluationContext>();
		expect(policyDomain.createContext(capture(rootContext))).andReturn(control.createMock(EvaluationContext.class));
		Capture<EvaluationContext> policyContext = new Capture<EvaluationContext>();
		expect(policyDomain.evaluate(capture(policyContext))).andReturn(Decision.PERMIT);
		Capture<Result> result0 = new Capture<Result>();
		decisionAuditor.audit(capture(result0), eq(req));
		Capture<Result> result1 = new Capture<Result>();
		decisionCache.putDecision(eq(req), capture(result1));
		control.replay();
		ResponseContext res = pdp.decide(req);
		assertEquals(1, res.getResults().size());
		assertEquals(result0.getValue(), result1.getValue());
		control.verify();
	}
}
