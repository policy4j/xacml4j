package com.artagon.xacml.v3.pdp;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.CompositeDecisionRuleIDReference;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationContextFactory;
import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.ResponseContext;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.Status;
import com.artagon.xacml.v3.StatusCode;
import com.artagon.xacml.v3.spi.PolicyDomain;
import com.artagon.xacml.v3.spi.XPathProvider;
import com.google.common.base.Preconditions;

public final class DefaultPolicyDecisionPoint implements PolicyDecisionPoint, 
	PolicyDecisionCallback
{
	private final static Logger log = LoggerFactory.getLogger(DefaultPolicyDecisionPoint.class);
	
	private EvaluationContextFactory factory;
	private PolicyDomain policyDomain;
	private RequestContextHandlerChain requestProcessingPipeline;
	private PolicyDecisionCache decisionCache;
	private PolicyDecisionAuditor decisionAuditor;
	
	public DefaultPolicyDecisionPoint(
			List<RequestContextHandler> handlers,
			EvaluationContextFactory factory,  
			PolicyDomain policyRepository, 
			PolicyDecisionCache cache, 
			PolicyDecisionAuditor auditor)
	{
		Preconditions.checkNotNull(factory);
		Preconditions.checkNotNull(policyRepository);
		Preconditions.checkNotNull(cache);
		Preconditions.checkNotNull(auditor);
		this.factory = factory;
		this.policyDomain = policyRepository;
		this.requestProcessingPipeline = new RequestContextHandlerChain(handlers);
		this.decisionCache = cache;
		this.decisionAuditor = auditor;
	}
	
	public DefaultPolicyDecisionPoint(
			EvaluationContextFactory factory,  
			PolicyDomain policyRepostory, 
			PolicyDecisionCache cache, 
			PolicyDecisionAuditor auditor)
	{
		this(Collections.<RequestContextHandler>emptyList(), 
				factory, policyRepostory, cache, auditor);
	}
	
	public DefaultPolicyDecisionPoint(
			EvaluationContextFactory factory,  
			PolicyDomain policyRepostory)
	{
		this(Collections.<RequestContextHandler>emptyList(), 
				factory, policyRepostory);
	}
	
	public DefaultPolicyDecisionPoint(
			List<RequestContextHandler> handlers,
			EvaluationContextFactory factory,  
			PolicyDomain policyRepostory)
	{
		this(handlers, factory, policyRepostory, 
				new NoCachePolicyDecisionCache(), 
				new NoAuditPolicyDecisionPointAuditor());
	}
	
	@Override
	public ResponseContext decide(RequestContext request)
	{
		if(log.isDebugEnabled()){
			log.debug("Processing decision " +
					"request=\"{}\"", request);
		}
		Collection<Result> results = requestProcessingPipeline.handle(request, this);
		return new ResponseContext(results);			
	}
	
	@Override
	public Result requestDecision(RequestContext request) 
	{
		Result r = decisionCache.getDecision(request);
		if(r != null){
			if(log.isDebugEnabled()){
				log.debug("Found decision result in the decision cache");
				log.debug("Decision request=\"{}\" " +
						"result=\"{}\"", request,  r);
			}
			decisionAuditor.audit(r, request);
			return r;
		}
		EvaluationContext context = factory.createContext(request);
		Decision decision = policyDomain.evaluate(context);
		r = createResult(context, decision, 
				request.getIncludeInResultAttributes(), request.isReturnPolicyIdList());
		if(log.isDebugEnabled()){
			log.debug("Decision request=\"{}\" " +
					"result=\"{}\"", request,  r);
		}
		decisionAuditor.audit(r, request);
		decisionCache.putDecision(request, r);
		return r;
	}
	
	private Result createResult(
			EvaluationContext context, 
			Decision decision, Collection<Attributes> includeInResult, 
			boolean returnPolicyIdList)
	{
		if(decision == Decision.NOT_APPLICABLE){
			return new Result(decision, 
					new Status(StatusCode.createOk()), includeInResult);
		}
		if(decision.isIndeterminate()){
			StatusCode status = (context.getEvaluationStatus() == null)?
					StatusCode.createProcessingError():context.getEvaluationStatus();
			return new Result(decision, new Status(status), includeInResult);
		}
		return new Result(
				decision, 
				new Status(StatusCode.createOk()),
				context.getAdvices(), 
				context.getObligations(), 
				includeInResult, 
				(returnPolicyIdList?
						context.getEvaluatedPolicies():
							Collections.<CompositeDecisionRuleIDReference>emptyList()));
	}

	@Override
	public XPathProvider getXPathProvider() {
		return factory.getXPathProvider();
	}	
}
