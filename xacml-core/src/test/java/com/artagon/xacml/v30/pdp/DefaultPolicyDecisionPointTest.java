package com.artagon.xacml.v30.pdp;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.easymock.Capture;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.Attributes;
import com.artagon.xacml.v30.CompositeDecisionRule;
import com.artagon.xacml.v30.Decision;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.RequestContext;
import com.artagon.xacml.v30.ResponseContext;
import com.artagon.xacml.v30.Result;
import com.artagon.xacml.v30.spi.audit.PolicyDecisionAuditor;
import com.artagon.xacml.v30.spi.dcache.PolicyDecisionCache;
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
		.builder()
			.withDecisionAuditor(decisionAuditor)
			.withPolicyRepository(repository)
			.withDecisionCache(decisionCache)
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
		expect(policyDomain.evaluate(capture(policyContext))).andReturn(Decision.PERMIT);
		Capture<Result> result0 = new Capture<Result>();
		decisionAuditor.audit(capture(result0), eq(req));
		Capture<Result> result1 = new Capture<Result>();
		decisionCache.putDecision(eq(req), capture(result1));
		
		control.replay();
		
		this.pdp = pdpBuilder.build();
		
		ResponseContext res = pdp.decide(req);
		assertEquals(1, res.getResults().size());
		assertEquals(result0.getValue(), result1.getValue());
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
		expect(policyDomain.evaluate(capture(policyContext))).andReturn(Decision.DENY);
		Capture<Result> result0 = new Capture<Result>();
		decisionAuditor.audit(capture(result0), eq(req));
		Capture<Result> result1 = new Capture<Result>();
		decisionCache.putDecision(eq(req), capture(result1));
		
		control.replay();
		
		this.pdp = pdpBuilder.build();
		
		ResponseContext res = pdp.decide(req);
		assertEquals(1, res.getResults().size());
		assertEquals(result0.getValue(), result1.getValue());
		
		control.verify();
	}
}
