package org.xacml4j.v30.pdp;

import static org.xacml4j.v30.pdp.MetricsSupport.name;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;

import org.xacml4j.v30.Advice;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Entity;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.spi.audit.PolicyDecisionAuditor;
import org.xacml4j.v30.spi.pdp.PolicyDecisionCache;
import org.xacml4j.v30.spi.pdp.RequestContextHandler;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * A default implementation of {@link PolicyDecisionPoint}
 *
 * @author Giedrius Trumpickas
 */
final class DefaultPolicyDecisionPoint
	extends StandardMBean implements PolicyDecisionPoint, PolicyDecisionCallback
{

	private String id;
	private PolicyDecisionPointContextFactory factory;

	private AtomicBoolean auditEnabled;
	private AtomicBoolean cacheEnabled;

	private MetricRegistry registry;
	
	private Timer decisionTimer;
	private Histogram decisionHistogram;
	private Counter permitDecisions;
	private Counter denyDecisions;
	private Counter indeterminateDecisions;
	
	DefaultPolicyDecisionPoint(
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
		this.registry = MetricsSupport.getOrCreate();
		this.decisionTimer = registry.timer(name("pdp", id, "timer"));
		this.decisionHistogram = registry.histogram(name("pdp", id, "histogram"));
		this.permitDecisions = registry.counter(name("pdp", id, "count-permit"));
		this.denyDecisions = registry.counter(name("pdp", id, "count-deny"));
		this.indeterminateDecisions = registry.counter(name("pdp", id, "count-indeterminate"));
	}

	@Override
	public ResponseContext decide(RequestContext request)
	{
		MDCSupport.setPdpContext(this);
		try
		{
			PolicyDecisionPointContext context = factory.createContext(this);
			RequestContextHandler chain = context.getRequestHandlers();
			return ResponseContext
					.builder()
					.results(chain.handle(request, context))
					.build();
		}finally{
			MDCSupport.cleanPdpContext();
		}
	}

	@Override
	public String getId(){
		return id;
	}

	@Override
	public Result requestDecision(
			PolicyDecisionPointContext context,
			RequestContext request)
	{
		MDCSupport.setXacmlRequestId(context.getCorrelationId(), request);
		try
		{
			PolicyDecisionCache decisionCache = context.getDecisionCache();
			PolicyDecisionAuditor decisionAuditor = context.getDecisionAuditor();
			Timer.Context timerContext = decisionTimer.time();
			Result r =  null;
			if(isDecisionCacheEnabled()){
				r = decisionCache.getDecision(request);
			}
			if(r != null){
				if(isDecisionAuditEnabled()){
					decisionAuditor.audit(this, r, request);
				}
				incrementDecionCounters(r.getDecision());
				decisionHistogram.update(timerContext.stop());
				return r;
			}
			EvaluationContext evalContext = context.createEvaluationContext(request);
			CompositeDecisionRule rootPolicy = context.getDomainPolicy();
			Decision decision = rootPolicy.evaluate(rootPolicy.createContext(evalContext));
			incrementDecionCounters(decision);
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
			decisionHistogram.update(timerContext.stop());
			return r;
		}finally{
			MDCSupport.cleanXacmlRequestId();
		}
	}
	
	private void incrementDecionCounters(Decision d){
		if(d == Decision.PERMIT){
			permitDecisions.inc();
			return;
		}
		if(d == Decision.DENY){
			denyDecisions.inc();
			return;
		}
		indeterminateDecisions.inc();
	}

	private Result createResult(
			EvaluationContext context,
			Decision decision,
			Collection<Category> includeInResult,
			Collection<Category> resolvedAttributes,
			boolean returnPolicyIdList)
	{
		if(decision == Decision.NOT_APPLICABLE){
			return Result
					.ok(decision)
					.includeInResultAttr(includeInResult)
					.resolvedAttr(resolvedAttributes)
					.build();
		}
		if(decision.isIndeterminate()){
			Status status = (context.getEvaluationStatus() == null)?
					Status.processingError().build():context.getEvaluationStatus();
			return Result
					.builder(decision, status)
					.includeInResultAttr(includeInResult)
					.resolvedAttr(resolvedAttributes)
					.build();
		}
		Iterable<Advice> advice = context.getMatchingAdvices(decision);
		Iterable<Obligation> obligation = context.getMatchingObligations(decision);
		Result.Builder b = Result.ok(decision)
				.advice(advice)
				.obligation(obligation)
				.includeInResultAttr(includeInResult)
				.resolvedAttr(resolvedAttributes);
		if(returnPolicyIdList){
			b.evaluatedPolicies(
					context.getEvaluatedPolicies());
		}
		return b.build();
	}

	/**
	 * Gets all attributes from an {@link EvaluationContext} which were resolved
	 * and not present in the original access decision request
	 *
	 * @param context an evaluation context
	 * @return a collection of {@link Attribute} instances
	 */
	private Collection<Category> getResolvedAttributes(EvaluationContext context){
		Map<AttributeDesignatorKey, BagOfAttributeExp> desig = context.getResolvedDesignators();
		Multimap<CategoryId, Attribute> attributes = HashMultimap.create();
		for(AttributeDesignatorKey k : desig.keySet()){
			BagOfAttributeExp v = desig.get(k);
			Collection<Attribute> values = attributes.get(k.getCategory());
			values.add(Attribute
					.builder(k.getAttributeId())
					.issuer(k.getIssuer())
					.values(v.values())
					.build());
		}
		Collection<Category> result = new LinkedList<Category>();
		for(CategoryId c : attributes.keySet()){
			result.add(Category
					.builder(c)
					.entity(Entity.builder().attributes(attributes.get(c)).build())
					.build());
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
	public void close(){
	}
}
