package com.artagon.xacml.v30.pdp;

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

import com.artagon.xacml.v30.spi.audit.PolicyDecisionAuditor;
import com.artagon.xacml.v30.spi.pdp.PolicyDecisionCache;
import com.artagon.xacml.v30.spi.pip.PolicyInformationPoint;
import com.artagon.xacml.v30.spi.repository.PolicyRepository;
import com.artagon.xacml.v30.spi.repository.PolicyRepositoryListener;
import com.artagon.xacml.v30.spi.xpath.XPathProvider;

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
			.withDecisionAuditor(decisionAuditor)
			.withPolicyRepository(repository)
			.withDecisionCache(decisionCache)
			.withDecisionCacheTTL(10)
			.withPolicyInformationPoint(pip)
			.withRootPolicy(policyDomain)
			.withXPathProvider(xpathProvider);
	}
	
	@Test
	public void testRequestEvaluationPolicyDomainEvaluatesToPermitAndRequestReturnEvaluatedPolicyIdsFalse()
	{
		
		RequestContext req = new RequestContext(false, Collections.<Attributes>emptyList());
		
		Capture<PolicyRepositoryListener> c = new Capture<PolicyRepositoryListener>();
		repository.addPolicyRepositoryListener(capture(c));
		
		expect(decisionCache.getDecision(req)).andReturn(null);
		Capture<EvaluationContext> rootContext = new Capture<EvaluationContext>();
		expect(policyDomain.createContext(capture(rootContext))).andReturn(control.createMock(EvaluationContext.class));
		Capture<EvaluationContext> policyContext = new Capture<EvaluationContext>();
		expect(policyDomain.evaluateIfApplicable(capture(policyContext))).andReturn(Decision.PERMIT);
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
		
		RequestContext req = new RequestContext(false, Collections.<Attributes>emptyList());
		
		Capture<PolicyRepositoryListener> c = new Capture<PolicyRepositoryListener>();
		repository.addPolicyRepositoryListener(capture(c));
		
		expect(decisionCache.getDecision(req)).andReturn(null);
		Capture<EvaluationContext> rootContext = new Capture<EvaluationContext>();
		expect(policyDomain.createContext(capture(rootContext))).andReturn(control.createMock(EvaluationContext.class));
		Capture<EvaluationContext> policyContext = new Capture<EvaluationContext>();
		expect(policyDomain.evaluateIfApplicable(capture(policyContext))).andReturn(Decision.DENY);
		
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
