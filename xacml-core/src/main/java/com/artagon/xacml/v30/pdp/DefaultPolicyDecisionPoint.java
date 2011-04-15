package com.artagon.xacml.v30.pdp;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v30.Attributes;
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
		Result r = decisionCache.getDecision(request);
		if(r != null){
			if(log.isDebugEnabled()){
				log.debug("Found decision result in the decision cache");
				log.debug("Decision request=\"{}\" " +
						"result=\"{}\"", request,  r);
			}
			if(isDecisionAuditEnabled()){
				decisionAuditor.audit(this, r, request);
			}
			return r;
		}
		EvaluationContext evalContext = context.createEvaluationContext(request);
		CompositeDecisionRule rootPolicy = context.getDomainPolicy();
		long start = System.currentTimeMillis();
		Decision decision = rootPolicy.evaluate(rootPolicy.createContext(evalContext));
		avgDecisionTime.set(System.currentTimeMillis() - start);
		r = createResult(evalContext, 
				decision, 
				request.getIncludeInResultAttributes(), 
				request.isReturnPolicyIdList());
		if(log.isDebugEnabled()){
			log.debug("Decision request=\"{}\" " +
					"result=\"{}\"", request,  r);
		}
		if(isDecisionAuditEnabled()){
			decisionAuditor.audit(this, r, request);
		}
		if(isDecisionCacheEnabled()){
			decisionCache.putDecision(request, r);
		}
		return r;
	}
	
	private Result createResult(
			EvaluationContext context, 
			Decision decision, 
			Collection<Attributes> includeInResult, 
			boolean returnPolicyIdList)
	{
		decisionCount.incrementAndGet();
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
	public long getDecisionAverageTime() {
		return avgDecisionTime.get();
	}
}
