package com.artagon.xacml.v3.pdp;

import java.util.Collection;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.ResponseContext;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.Status;
import com.artagon.xacml.v3.StatusCode;
import com.artagon.xacml.v3.policy.CompositeDecisionRule;
import com.artagon.xacml.v3.policy.CompositeDecisionRuleIDReference;
import com.google.common.base.Preconditions;

/**
 * A default implementation of {@link PolicyDecisionPoint}
 * 
 * @author Giedrius Trumpickas
 */
public final class DefaultPolicyDecisionPoint 
	implements PolicyDecisionPoint, PolicyDecisionCallback
{
	private final static Logger log = LoggerFactory.getLogger(DefaultPolicyDecisionPoint.class);
	
	private PolicyDecisionPointContextFactory factory;
	
	public DefaultPolicyDecisionPoint(
			PolicyDecisionPointContextFactory factory)
	{
		Preconditions.checkNotNull(factory);
		this.factory = factory;
	}
	
	@Override
	public ResponseContext decide(RequestContext request)
	{
		if(log.isDebugEnabled()){
			log.debug("Processing decision " +
					"request=\"{}\"", request);
		}
		PolicyDecisionPointContext context = factory.createContext(this);
		RequestContextHandler chain = context.getRequestHandlers();
		Collection<Result> results = chain.handle(request, context);
		return new ResponseContext(results);			
	}
	
	@Override
	public Result requestDecision(
			PolicyDecisionPointContext context, 
			RequestContext request) 
	{
		PolicyDecisionCache decisionCache = context.getDecisionCache();
		PolicyDecisionAuditor decisionAuditor = context.getDecisionAuditor();
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
		EvaluationContext evalContext = context.createEvaluationContext(request);
		CompositeDecisionRule rootPolicy = context.getDomainPolicy();
		Decision decision = rootPolicy.evaluate(rootPolicy.createContext(evalContext));
		r = createResult(evalContext, 
				decision, 
				request.getIncludeInResultAttributes(), 
				request.isReturnPolicyIdList());
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
			Decision decision, 
			Collection<Attributes> includeInResult, 
			boolean returnPolicyIdList)
	{
		if(decision == Decision.NOT_APPLICABLE){
			return new Result(decision, 
					Status.createSuccess(), 
					includeInResult);
		}
		if(decision.isIndeterminate()){
			StatusCode status = (context.getEvaluationStatus() == null)?
					StatusCode.createProcessingError():context.getEvaluationStatus();
			return new Result(decision, new Status(status), includeInResult);
		}
		return new Result(
				decision, 
				Status.createSuccess(),
				context.getAdvices(), 
				context.getObligations(), 
				includeInResult, 
				(returnPolicyIdList?
						context.getEvaluatedPolicies():
							Collections.<CompositeDecisionRuleIDReference>emptyList()));
	}
}
