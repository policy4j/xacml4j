package com.artagon.xacml.v30.pdp;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v30.Attribute;
import com.artagon.xacml.v30.AttributeCategory;
import com.artagon.xacml.v30.AttributeDesignatorKey;
import com.artagon.xacml.v30.Attributes;
import com.artagon.xacml.v30.BagOfAttributeValues;
import com.artagon.xacml.v30.CompositeDecisionRule;
import com.artagon.xacml.v30.CompositeDecisionRuleIDReference;
import com.artagon.xacml.v30.Decision;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.MDCSupport;
import com.artagon.xacml.v30.RequestContext;
import com.artagon.xacml.v30.ResponseContext;
import com.artagon.xacml.v30.Result;
import com.artagon.xacml.v30.Status;
import com.artagon.xacml.v30.StatusCode;
import com.artagon.xacml.v30.spi.audit.PolicyDecisionAuditor;
import com.artagon.xacml.v30.spi.pdp.PolicyDecisionCache;
import com.artagon.xacml.v30.spi.pdp.RequestContextHandler;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * A default implementation of {@link PolicyDecisionPoint}
 * 
 * @author Giedrius Trumpickas
 */
public final class DefaultPolicyDecisionPoint 
	extends StandardMBean implements PolicyDecisionPoint, PolicyDecisionCallback
{
	private final static Logger log = LoggerFactory.getLogger(DefaultPolicyDecisionPoint.class);
		
	private AtomicBoolean auditEnabled;
	private AtomicBoolean cacheEnabled;
	private AtomicLong decisionCount;
	private AtomicLong avgDecisionTime;
	
	private String id;
	private PolicyDecisionPointContextFactory factory;
	
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
		this.decisionCount = new AtomicLong(0);
		this.avgDecisionTime = new AtomicLong(0);
	}
	
	@Override
	public ResponseContext decide(RequestContext request)
	{
		MDCSupport.setPdpContext(this);
		try
		{
			if(log.isDebugEnabled()){
				log.debug("Processing decision " +
						"request=\"{}\"", request);
			}
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
		long start = System.currentTimeMillis();
		Result r =  null;
		if(isDecisionCacheEnabled()){
			r = decisionCache.getDecision(request);
		}
		if(r != null){
			if(isDecisionAuditEnabled()){
				decisionAuditor.audit(this, r, request);
			}
			avgDecisionTime.set(System.currentTimeMillis() - start);
			return r;
		}
		EvaluationContext evalContext = context.createEvaluationContext(request);
		CompositeDecisionRule rootPolicy = context.getDomainPolicy();
		Decision decision = rootPolicy.evaluateIfApplicable(rootPolicy.createContext(evalContext));
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
		avgDecisionTime.set(System.currentTimeMillis() - start);
		return r;
	}
	
	private Result createResult(
			EvaluationContext context, 
			Decision decision, 
			Collection<Attributes> includeInResult, 
			Collection<Attributes> resolvedAttributes,
			boolean returnPolicyIdList)
	{
		decisionCount.incrementAndGet();
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
	
	private Collection<Attributes> getResolvedAttributes(EvaluationContext context){
		Map<AttributeDesignatorKey, BagOfAttributeValues> desig = context.getResolvedDesignators();
		Multimap<AttributeCategory, Attribute> attributes = HashMultimap.create();
		for(AttributeDesignatorKey k : desig.keySet()){
			BagOfAttributeValues v = desig.get(k);
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

	@Override
	public long getDecisionCount() {
		return decisionCount.get();
	}

	@Override
	public long getAverageDecisionTime() {
		return avgDecisionTime.get();
	}
}
