package com.artagon.xacml.v3.pdp;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.same;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.artagon.xacml.v3.Advice;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.CompositeDecisionRuleIDReference;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationContextFactory;
import com.artagon.xacml.v3.Obligation;
import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.ResponseContext;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.spi.PolicyDomain;
import com.artagon.xacml.v3.spi.PolicyInformationPoint;
import com.artagon.xacml.v3.spi.PolicyRepository;

@Ignore
public class DefaultPolicyDecisionPointTest 
{
	private PolicyDecisionPoint pdp;
	
	private PolicyRepository repository;
	private PolicyInformationPoint pip;
	private PolicyDomain policyDomain;
	private EvaluationContextFactory factory;
	private PolicyDecisionCache decisionCache;
	private PolicyDecisionAuditor decisionAuditor;
	private RequestContextHandler handler;
	
	@Before
	public void init(){
		this.pip = createStrictMock(PolicyInformationPoint.class);
		this.repository = createStrictMock(PolicyRepository.class);
		this.policyDomain = createStrictMock(PolicyDomain.class);
		this.decisionAuditor = createStrictMock(PolicyDecisionAuditor.class);
		this.decisionCache = createStrictMock(PolicyDecisionCache.class);
		this.handler = createStrictMock(RequestContextHandler.class);
		this.factory = createStrictMock(EvaluationContextFactory.class);
		this.pdp = new DefaultPolicyDecisionPoint(
				Collections.singletonList(handler), 
				factory, policyDomain, decisionCache, decisionAuditor);
	}
	
	@Test
	public void testRequestEvaluationPolicyDomainEvaluatesToPermitAndRequestReturnEvaluatedPolicyIdsFalse()
	{
		RequestContext req = new RequestContext(false, Collections.<Attributes>emptyList());
		EvaluationContext context = createStrictMock(EvaluationContext.class);
		Capture<PolicyDecisionCallback> pdpCallback = new Capture<PolicyDecisionCallback>();
		handler.handle(same(req), capture(pdpCallback));
		expect(decisionCache.getDecision(req)).andReturn(null);
		expect(factory.createContext(req)).andReturn(context);
		expect(policyDomain.evaluate(context)).andReturn(Decision.PERMIT);
		expect(context.getAdvices()).andReturn(Collections.<Advice>emptyList());
		expect(context.getObligations()).andReturn(Collections.<Obligation>emptyList());
		Capture<Result> result = new Capture<Result>();
		decisionAuditor.audit(capture(result), same(req));
		decisionCache.putDecision(same(req), capture(result));
		replay(repository, pip, policyDomain, factory, context, decisionCache, decisionAuditor, handler);
		ResponseContext res = pdp.decide(req);
		assertEquals(1, res.getResults().size());
		verify(repository, pip, policyDomain, factory, context, decisionCache, decisionAuditor, handler);
	}
	
	@Test
	public void testRequestEvaluationPolicyDomainEvaluatesToPermitAndRequestReturnEvaluatedPolicyIdsTrue()
	{
		RequestContext req = new RequestContext(true, Collections.<Attributes>emptyList());
		EvaluationContext context = createStrictMock(EvaluationContext.class);
		expect(decisionCache.getDecision(req)).andReturn(null);
		expect(factory.createContext(req)).andReturn(context);
		expect(policyDomain.evaluate(context)).andReturn(Decision.PERMIT);
		expect(context.getAdvices()).andReturn(Collections.<Advice>emptyList());
		expect(context.getObligations()).andReturn(Collections.<Obligation>emptyList());
		expect(context.getEvaluatedPolicies()).andReturn(Collections.<CompositeDecisionRuleIDReference>emptyList());
		Capture<Result> result = new Capture<Result>();
		decisionAuditor.audit(capture(result), same(req));
		decisionCache.putDecision(same(req), capture(result));
		replay(repository, pip, policyDomain, factory, context, decisionCache, decisionAuditor, handler);
		ResponseContext res = pdp.decide(req);
		assertEquals(1, res.getResults().size());
		verify(repository, pip, policyDomain, factory, context, decisionCache, decisionAuditor, handler);
	}
	
	@Test
	public void testRequestEvaluationPolicyDomainEvaluatesToDenyAndRequestReturnEvaluatedPolicyIdsFalse()
	{
		RequestContext req = new RequestContext(false, Collections.<Attributes>emptyList());
		EvaluationContext context = createStrictMock(EvaluationContext.class);
		expect(decisionCache.getDecision(req)).andReturn(null);
		expect(factory.createContext(req)).andReturn(context);
		expect(policyDomain.evaluate(context)).andReturn(Decision.DENY);
		expect(context.getAdvices()).andReturn(Collections.<Advice>emptyList());
		expect(context.getObligations()).andReturn(Collections.<Obligation>emptyList());
		Capture<Result> result = new Capture<Result>();
		decisionAuditor.audit(capture(result), same(req));
		decisionCache.putDecision(same(req), capture(result));
		replay(repository, pip, policyDomain, factory, context, decisionCache, decisionAuditor, handler);
		ResponseContext res = pdp.decide(req);
		assertEquals(1, res.getResults().size());
		verify(repository, pip, policyDomain, factory, context, decisionCache, decisionAuditor, handler);
	}
}
