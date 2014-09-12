package org.xacml4j.v30.pdp;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static org.xacml4j.v30.pdp.MetricsSupport.name;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;

import org.xacml4j.v30.*;
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
 * A default implementation of {@link org.xacml4j.v30.PolicyDecisionPoint}
 *
 * @author Giedrius Trumpickas
 */
final class DefaultPolicyDecisionPoint
	extends StandardMBean implements PolicyDecisionPoint, PolicyDecisionCallback
{

    private final static String PDP_NAME = "pdp";
    private final static String HISTOGRAM_PROPERTY = "histogram";
    private final static String COUNT_PERMIT_PROPERTY = "count-permit";
    private final static String COUNT_INDETERMINATE_PROPERTY = "count-indeterminate";
    private final static String COUNT_DENY_PROPERTY = "count-deny";
    private final static String TIMER_PROPERTY = "timer";

	private final String id;
	private final PolicyDecisionPointContextFactory factory;

	private final AtomicBoolean auditEnabled;
	private final AtomicBoolean cacheEnabled;

	private final Timer decisionTimer;
	private final Histogram decisionHistogram;
	private final Counter permitDecisions;
	private final Counter denyDecisions;
	private final Counter indeterminateDecisions;

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
		final MetricRegistry registry = MetricsSupport.getOrCreate();
		this.decisionTimer = registry.timer(name(PDP_NAME, id, TIMER_PROPERTY));
		this.decisionHistogram = registry.histogram(name(PDP_NAME, id, HISTOGRAM_PROPERTY));
		this.permitDecisions = registry.counter(name(PDP_NAME, id, COUNT_PERMIT_PROPERTY));
		this.denyDecisions = registry.counter(name(PDP_NAME, id, COUNT_DENY_PROPERTY));
		this.indeterminateDecisions = registry.counter(name(PDP_NAME, id, COUNT_INDETERMINATE_PROPERTY));
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
				incrementDecisionCounters(r.getDecision());
				decisionHistogram.update(timerContext.stop());
				return r;
			}
			RootEvaluationContext evalContext = context.createEvaluationContext(request);
			CompositeDecisionRule rootPolicy = context.getDomainPolicy();
			Decision decision = rootPolicy.evaluate(rootPolicy.createContext(evalContext));
			incrementDecisionCounters(decision);
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
                        TimeUnit.SECONDS,
						evalContext.getDecisionCacheTTL());
			}
			decisionHistogram.update(timerContext.stop());
			return r;
		}finally{
			MDCSupport.cleanXacmlRequestId();
		}
	}

	private void incrementDecisionCounters(Decision d){
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
			RootEvaluationContext context,
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
		for(Map.Entry<AttributeDesignatorKey, BagOfAttributeExp> entry : desig.entrySet()){
			final AttributeDesignatorKey k = entry.getKey();
			final BagOfAttributeExp v = entry.getValue();
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
