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
	private PolicyDecisionCache requestCache = new NullDecisionCache();
	
	public DefaultPolicyDecisionPoint(
			List<RequestContextHandler> handlers,
			EvaluationContextFactory factory,  
			PolicyDomain policyRepository)
	{
		Preconditions.checkNotNull(factory);
		Preconditions.checkNotNull(policyRepository);
		this.factory = factory;
		this.policyDomain = policyRepository;
		this.requestProcessingPipeline = new RequestContextHandlerChain(handlers);
	}
	
	public DefaultPolicyDecisionPoint(
			EvaluationContextFactory factory,  
			PolicyDomain policyRepostory)
	{
		this(Collections.<RequestContextHandler>emptyList(), factory, policyRepostory);
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
		Result r = requestCache.getDecision(request);
		if(r != null){
			if(log.isDebugEnabled()){
				log.debug("Found decision result in the decision cache");
				log.debug("Decision request=\"{}\" " +
						"result=\"{}\"", request,  r);
			}
			return r;
		}
		EvaluationContext context = factory.createContext(request);
		Decision decision = policyDomain.evaluate(context);
		Result result = createResult(context, decision, 
				request.getIncludeInResultAttributes(), request.isReturnPolicyIdList());
		if(log.isDebugEnabled()){
			log.debug("Decision request=\"{}\" " +
					"result=\"{}\"", request,  result);
		}
		requestCache.putDecision(request, result);
		return result;
	}
	
	private Result createResult(EvaluationContext context, 
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
	
	private class NullDecisionCache implements PolicyDecisionCache
	{
		@Override
		public Result getDecision(RequestContext req) {
			return null;
		}

		@Override
		public void putDecision(RequestContext req, Result res) {
		}
	}
}
