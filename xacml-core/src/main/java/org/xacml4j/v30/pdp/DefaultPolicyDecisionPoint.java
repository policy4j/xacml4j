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

	private final String id;
	private final PolicyDecisionPointContextFactory factory;

	private final AtomicBoolean auditEnabled;
	private final AtomicBoolean cacheEnabled;

	private final Timer decisionTimer;
	private final Histogram decisionHistogram;
	private final Counter permitDecisions;
	private final Counter denyDecisions;
	private final Counter indeterminateDecisions;
	private final Counter notApplicableDecisions;

	DefaultPolicyDecisionPoint(
			String id,
			PolicyDecisionPointContextFactory factory)
		throws NotCompliantMBeanException
	{
		super(PolicyDecisionPointMBean.class);
		this.id = Preconditions.checkNotNull(id);
		this.factory = Preconditions.checkNotNull(factory);
		this.auditEnabled = new AtomicBoolean(factory.isDecisionAuditEnabled());
		this.cacheEnabled = new AtomicBoolean(factory.isDecisionCacheEnabled());
		final MetricRegistry registry = MetricsSupport.getOrCreate();
		this.decisionTimer = registry.timer(name("pdp", id, "timer"));
		this.decisionHistogram = registry.histogram(name("pdp", id, "histogram"));
		this.permitDecisions = registry.counter(name("pdp", id, "count-permit"));
		this.denyDecisions = registry.counter(name("pdp", id, "count-deny"));
		this.indeterminateDecisions = registry.counter(name("pdp", id, "count-indeterminate"));
		this.notApplicableDecisions = registry.counter(name("pdp", id, "count-not-applicable"));
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
		} finally {
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
		Timer.Context timerContext = decisionTimer.time();
		try
		{
			PolicyDecisionCache decisionCache = context.getDecisionCache();
			PolicyDecisionAuditor decisionAuditor = context.getDecisionAuditor();
			Result r = null;
			if (isDecisionCacheEnabled()) {
				r = decisionCache.getDecision(request);
			}
			if (r == null) {
				EvaluationContext evalContext = context.createEvaluationContext(request);
				CompositeDecisionRule rootPolicy = context.getDomainPolicy();
				Decision decision = rootPolicy.evaluate(rootPolicy.createContext(evalContext));
				r = createResult(
						evalContext,
						decision,
						request.getIncludeInResultAttributes(),
						getResolvedAttributes(evalContext),
						request.isReturnPolicyIdList());
				if (isDecisionCacheEnabled()) {
					decisionCache.putDecision(
							request, r,
							evalContext.getDecisionCacheTTL());
				}
			}
			if(isDecisionAuditEnabled()){
				decisionAuditor.audit(this, r, request);
			}
			incrementDecisionCounters(r.getDecision());
			return r;
		} finally {
			MDCSupport.cleanXacmlRequestId();
			decisionHistogram.update(timerContext.stop());
		}
	}

	private void incrementDecisionCounters(Decision d) {
		switch (d) {
			case PERMIT:
				permitDecisions.inc();
				break;
			case DENY:
				denyDecisions.inc();
				break;
			case NOT_APPLICABLE:
				notApplicableDecisions.inc();
				break;
			default:
				indeterminateDecisions.inc();
		}
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
		for(Map.Entry<AttributeDesignatorKey, BagOfAttributeExp> entry : desig.entrySet()){
			BagOfAttributeExp v = entry.getValue();
			Collection<Attribute> values = attributes.get(entry.getKey().getCategory());
			values.add(Attribute
					.builder(entry.getKey().getAttributeId())
					.issuer(entry.getKey().getIssuer())
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
