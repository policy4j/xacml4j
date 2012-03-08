package com.artagon.xacml.v30.pdp;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;

import com.artagon.xacml.v30.AttributeCategory;
import com.artagon.xacml.v30.Status;
import com.artagon.xacml.v30.StatusCode;
import com.artagon.xacml.v30.spi.audit.PolicyDecisionAuditor;
import com.artagon.xacml.v30.spi.pdp.PolicyDecisionCache;
import com.artagon.xacml.v30.spi.pdp.RequestContextHandler;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;

/**
 * A default implementation of {@link PolicyDecisionPoint}
 * 
 * @author Giedrius Trumpickas
 */
public final class DefaultPolicyDecisionPoint 
	extends StandardMBean implements PolicyDecisionPoint, PolicyDecisionCallback
{	
	
	private String id;
	private PolicyDecisionPointContextFactory factory;
	
	private AtomicBoolean auditEnabled;
	private AtomicBoolean cacheEnabled;

	private Timer decisionTimer;
	
	public DefaultPolicyDecisionPoint(
			String id,
			PolicyDecisionPointContextFactory factory) 
		throws NotCompliantMBeanException
	{
		super(PolicyDecisionPointMBean.class);
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(factory);
		this.id = id;
		this.factory = factory;
		this.auditEnabled = new AtomicBoolean(factory.isDecisionAuditEnabled());
		this.cacheEnabled = new AtomicBoolean(factory.isDecisionCacheEnabled());
		this.decisionTimer = Metrics.newTimer(DefaultPolicyDecisionPoint.class, "decision-stats", getId());
	}
	
	@Override
	public ResponseContext decide(RequestContext request)
	{
		MDCSupport.setPdpContext(this);
		try
		{
			PolicyDecisionPointContext context = factory.createContext(this);
			RequestContextHandler chain = context.getRequestHandlers();
			Collection<Result> results = chain.handle(request, context);
			return new ResponseContext(results);
		}finally{
			MDCSupport.cleanPdpContext();
		}
	}
	
	public String getId(){
		return id;
	}
	
	@Override
	public Result requestDecision(
			PolicyDecisionPointContext context, 
			RequestContext request) 
	{
		PolicyDecisionCache decisionCache = context.getDecisionCache();
		PolicyDecisionAuditor decisionAuditor = context.getDecisionAuditor();
		Result r =  null;
		TimerContext timerContext = decisionTimer.time();
		if(isDecisionCacheEnabled()){
			r = decisionCache.getDecision(request);
		}
		if(r != null){
			if(isDecisionAuditEnabled()){
				decisionAuditor.audit(this, r, request);
			}
			timerContext.stop();
			return r;
		}
		EvaluationContext evalContext = context.createEvaluationContext(request);
		CompositeDecisionRule rootPolicy = context.getDomainPolicy();
		Decision decision = rootPolicy.evaluateIfMatch(rootPolicy.createContext(evalContext));
		r = createResult(evalContext, 
				decision, 
				request.getIncludeInResultAttributes(), 
				getResolvedAttributes(evalContext),
				request.isReturnPolicyIdList());
		if(isDecisionAuditEnabled()){
			decisionAuditor.audit(this, r, request);
		}
		if(isDecisionCacheEnabled()){
			decisionCache.putDecision(
					request, r, 
					evalContext.getDecisionCacheTTL());
		}
		timerContext.stop();
		return r;
	}
	
	private Result createResult(
			EvaluationContext context, 
			Decision decision, 
			Collection<Attributes> includeInResult, 
			Collection<Attributes> resolvedAttributes,
			boolean returnPolicyIdList)
	{
		if(decision == Decision.NOT_APPLICABLE){
			return new Result(decision, 
					Status.createSuccess(), 
					includeInResult, 
					resolvedAttributes);
		}
		if(decision.isIndeterminate()){
			StatusCode status = (context.getEvaluationStatus() == null)?
					StatusCode.createProcessingError():context.getEvaluationStatus();
			return new Result(decision, new Status(status), 
					includeInResult, 
					resolvedAttributes);
		}
		return new Result(
				decision, 
				Status.createSuccess(),
				context.getAdvices(), 
				context.getObligations(), 
				includeInResult, 
				resolvedAttributes,
				(returnPolicyIdList?
						context.getEvaluatedPolicies():
							Collections.<CompositeDecisionRuleIDReference>emptyList()));
	}
	
	/**
	 * Gets all attributes from an {@link EvaluationContext} which were resolved
	 * and not present in the original access decision request
	 * 
	 * @param context an evaluation context
	 * @return a collection of {@link Attribute} instances
	 */
	private Collection<Attributes> getResolvedAttributes(EvaluationContext context){
		Map<AttributeDesignatorKey, BagOfAttributeExp> desig = context.getResolvedDesignators();
		Multimap<AttributeCategory, Attribute> attributes = HashMultimap.create();
		for(AttributeDesignatorKey k : desig.keySet()){
			BagOfAttributeExp v = desig.get(k);
			Collection<Attribute> values = attributes.get(k.getCategory());
			values.add(new Attribute(k.getAttributeId(), k.getIssuer(), false, v.values()));
		}
		Collection<Attributes> result = new LinkedList<Attributes>();
		for(AttributeCategory c : attributes.keySet()){
			
			result.add(new Attributes(c, attributes.get(c)));
		}
		return result;
	}

	@Override
	public boolean isDecisionAuditEnabled() {
		return auditEnabled.get();
	}

	@Override
	public void setDecisionAuditEnabled(boolean enabled) {
		this.auditEnabled.set(enabled);
	}

	@Override
	public boolean isDecisionCacheEnabled() {
		return cacheEnabled.get();
	}

	@Override
	public void setDecisionCacheEnabled(boolean enabled) {
		this.cacheEnabled.set(enabled);		
	}
}
