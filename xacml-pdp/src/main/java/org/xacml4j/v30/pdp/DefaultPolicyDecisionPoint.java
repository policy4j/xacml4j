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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.Advice;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.pdp.PolicyDecisionPointContextFactory.DefaultPolicyDecisionPointContextFactory;
import org.xacml4j.v30.policy.RootEvaluationContext;
import org.xacml4j.v30.spi.audit.PolicyDecisionAuditor;
import org.xacml4j.v30.spi.observe.DecisionObservation;
import org.xacml4j.v30.spi.observe.DecisionObserver;
import org.xacml4j.v30.spi.pdp.PolicyDecisionCache;
import org.xacml4j.v30.spi.pdp.RequestContextHandler;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * A default implementation of {@link PolicyDecisionPoint}
 *
 * @author Giedrius Trumpickas
 */
final class DefaultPolicyDecisionPoint
	implements PolicyDecisionPoint
{
	private final static Logger LOG = LoggerFactory.getLogger(DefaultPolicyDecisionPoint.class);

	private final String id;
	private final DefaultPolicyDecisionPointContextFactory factory;
	private final DecisionObserver<?> observer;
	/** Only PDP configuration is shared; the direct call carries its correlation separately. */
	private final PolicyDecisionPointContext singleRequestContext;


	private final Histogram decisionHistogram;
	private final Counter permitDecisions;
	private final Counter denyDecisions;
	private final Counter indeterminateDecisions;

	DefaultPolicyDecisionPoint(
			String id,
			MetricRegistry registry,
			DefaultPolicyDecisionPointContextFactory factory)
	{
		this(id, registry, factory, DecisionObserver.none());
	}

	DefaultPolicyDecisionPoint(
			String id,
			MetricRegistry registry,
			DefaultPolicyDecisionPointContextFactory factory,
			DecisionObserver<?> observer)
	{
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(factory);
		Preconditions.checkNotNull(observer);
		this.id = id;
		this.factory = factory;
		this.observer = observer;
		this.singleRequestContext = factory.createContext((context, request) ->
				decide(context, request, 1, DecisionObservation.none(), factory.nextCorrelation()), 0);
		this.decisionHistogram = registry.histogram(name("pdp", id, "histogram"));
		this.permitDecisions = registry.counter(name("pdp", id, "count-permit"));
		this.denyDecisions = registry.counter(name("pdp", id, "count-deny"));
		this.indeterminateDecisions = registry.counter(name("pdp", id, "count-indeterminate"));
	}

	@Override
	public ResponseContext decide(RequestContext request)
	{
		if(observer == DecisionObserver.none() && factory.canHandleDirectly(request)){
			return ResponseContext.of(singleRequestContext.requestDecision(request));
		}
		return (observer == DecisionObserver.none())?
				respond(request, new NumberedDecisions()):
				decide(observer, request);
	}

	/** A decide call that the observer observes (design D34) */
	private <S> ResponseContext decide(DecisionObserver<S> observer, RequestContext request)
	{
		S token = Preconditions.checkNotNull(observer.requestStarted(this, request), "token");
		ResponseContext response;
		try{
			response = respond(request, new ObservedDecisions<>(observer, token));
		}catch(Throwable t){
			requestFailed(observer, token, t);
			throw t;
		}
		observer.requestFinished(token, response);
		return response;
	}

	private ResponseContext respond(RequestContext request, PolicyDecisionCallback decisions)
	{
		PolicyDecisionPointContext context = factory.createContext(decisions);
		RequestContextHandler chain =
				context.getRequestHandlers();
		return ResponseContext
				.builder()
				.results(chain.handle(request, context))
				.build();
	}

	/** Reports a failed decide call; an exception of the observer is added to the failure as suppressed */
	private static <S> void requestFailed(DecisionObserver<S> observer, S token, Throwable failure)
	{
		try{
			observer.requestFailed(token, failure);
		}catch(Throwable t){
			if(t != failure){
				failure.addSuppressed(t);
			}
		}
	}

	@Override
	public String getId(){
		return id;
	}

	/** The identifier with the prefix of this decision point, as {@link PolicyDecisionPointContext#getCorrelationId()} */
	@Override
	public String getCorrelationId(long correlation){
		return factory.getCorrelationId(correlation);
	}

	/**
	 * The callback of one decide call. It numbers the individual decisions
	 * of the call from 1; only the thread that handles the call uses it.
	 */
	private final class NumberedDecisions implements PolicyDecisionCallback
	{
		private int decisions;

		@Override
		public Result requestDecision(PolicyDecisionPointContext context, RequestContext request){
			return decide(context, request, ++decisions, DecisionObservation.none());
		}
	}

	/** The callback of one observed decide call, as {@link NumberedDecisions}, which observes each decision */
	private final class ObservedDecisions<S> implements PolicyDecisionCallback
	{
		private final DecisionObserver<S> observer;
		private final S request;
		private int decisions;

		ObservedDecisions(DecisionObserver<S> observer, S request){
			this.observer = observer;
			this.request = request;
		}

		@Override
		public Result requestDecision(PolicyDecisionPointContext context, RequestContext individualRequest){
			int number = ++decisions;
			return DefaultPolicyDecisionPoint.this.requestDecision(context, individualRequest, number,
					DecisionObservation.start(observer, request, individualRequest, context.getCorrelation(), number));
		}
	}

	/** An observed decision: exactly one of its finish and its failure is reported */
	private Result requestDecision(
			PolicyDecisionPointContext context,
			RequestContext request,
			int number,
			DecisionObservation observation)
	{
		Result r;
		try{
			r = decide(context, request, number, observation);
		}catch(Throwable t){
			observation.decisionFailed(t);
			throw t;
		}
		observation.decisionFinished(r);
		return r;
	}

	private Result decide(
			PolicyDecisionPointContext context,
			RequestContext request,
			int number,
			DecisionObservation observation)
	{
		return decide(context, request, number, observation, 0);
	}

	private String getCorrelationId(PolicyDecisionPointContext context, long correlation){
		return context == singleRequestContext ? factory.getCorrelationId(correlation) : context.getCorrelationId();
	}

	private Result decide(
			PolicyDecisionPointContext context,
			RequestContext request,
			int number,
			DecisionObservation observation,
			long correlation)
	{
		PolicyDecisionCache decisionCache = context.getDecisionCache();
		PolicyDecisionAuditor decisionAuditor = context.getDecisionAuditor();;
		Result r =  null;
		if(context.isDecisionCacheEnabled()){
			r = decisionCache.getDecision(request);
		}
		if(r != null){
			observation.decisionCacheHit(r);
			if(LOG.isDebugEnabled()){
				LOG.debug("Request id=\"{}-{}\" decision=\"{}\" from the decision cache",
						getCorrelationId(context, correlation), number, r.getDecision());
			}
			if(context.isDecisionAuditEnabled()){
				decisionAuditor.audit(this, r, request);
			}
			incrementDecisionCounters(r.getDecision());
			return r;
		}
		EvaluationContext evalContext = context.createEvaluationContext(request, observation);
		CompositeDecisionRule rootPolicy = context.getDomainPolicy();
		Decision decision = rootPolicy.evaluate(rootPolicy.createContext(evalContext));
		// An observer exception that an engine guard absorbed fails the decision before its result is built
		observation.throwObserverFailure();
		// One debug line per decision; the policy elements log none (design D18)
		if(LOG.isDebugEnabled()){
			LOG.debug("Request id=\"{}-{}\" decision=\"{}\" evaluated policies={}",
					getCorrelationId(context, correlation), number, decision, evalContext.getEvaluatedPolicies().size());
		}
		incrementDecisionCounters(decision);
		r = createResult(evalContext,
				decision,
				request.getIncludeInResultAttributes(),
				getResolvedAttributes(evalContext),
				request.isReturnPolicyIdList());
		if(context.isDecisionAuditEnabled()){
			decisionAuditor.audit(this, r, request);
		}
		if(context.isDecisionCacheEnabled()){
			decisionCache.putDecision(
					request, r,
					evalContext.getDecisionCacheTTL());
		}
		return r;
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

	/**
	 * The result of a decision, made without a builder from the immutable lists of the root context
	 * of the engine (cycle AU)
	 */
	private Result createResult(
			EvaluationContext context,
			Decision decision,
			Collection<Category> includeInResult,
			Collection<Category> resolvedAttributes,
			boolean returnPolicyIdList)
	{
		if(decision == Decision.NOT_APPLICABLE){
			return Result.of(decision, ImmutableList.of(), ImmutableList.of(),
					includeInResult, resolvedAttributes, ImmutableList.of());
		}
		if(decision.isIndeterminate()){
			Status status = context.getEvaluationStatus()
			                       .orElseGet(() -> Status.processingError()
			                                              .build());
			return Result.of(decision, status, includeInResult, resolvedAttributes);
		}
		if(context instanceof RootEvaluationContext root){
			// The responses are one per id already, as the policy identifiers are
			return Result.of(decision, root.matchingObligations(decision), root.matchingAdvices(decision),
					includeInResult, resolvedAttributes,
					returnPolicyIdList ? root.policyIdentifiers() : ImmutableList.of());
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
	 * <p>
	 * The attributes of a category are kept in a hash set, which drops equal attributes, and come in
	 * its order. A decision usually resolves attributes of one category: their set is the one that the
	 * hash multimap of the categories makes for a category, built without the multimap, its views and
	 * a list (cycle AZ). A second category builds all of them in the multimap, from the start.
	 *
	 * @param context an evaluation context
	 * @return a collection of {@link Attribute} instances
	 */
	private Collection<Category> getResolvedAttributes(EvaluationContext context){
		if(!context.hasResolvedDesignators()){
			// The usual case: every attribute came from the request, and the view of the
			// resolved designators is not created for it (change optimize-pdp-evaluation, D9, D32)
			return Collections.emptyList();
		}
		Map<AttributeDesignatorKey, BagOfValues> desig = context.getResolvedDesignators();
		Iterator<AttributeDesignatorKey> keys = desig.keySet().iterator();
		if(!keys.hasNext()){
			return Collections.emptyList();
		}
		AttributeDesignatorKey first = keys.next();
		CategoryId category = first.getCategory();
		// As HashMultimap.create() makes the set of a key
		Set<Attribute> attributes = Sets.newHashSetWithExpectedSize(2);
		attributes.add(attribute(first, desig.get(first)));
		while(keys.hasNext()){
			AttributeDesignatorKey k = keys.next();
			if(!category.equals(k.getCategory())){
				return byCategory(desig);
			}
			attributes.add(attribute(k, desig.get(k)));
		}
		return ImmutableList.of(category(category, attributes));
	}

	/** The resolved attributes, grouped by category */
	private static Collection<Category> byCategory(Map<AttributeDesignatorKey, BagOfValues> desig){
		Multimap<CategoryId, Attribute> attributes = HashMultimap.create();
		for(AttributeDesignatorKey k : desig.keySet()){
			Collection<Attribute> values = attributes.get(k.getCategory());
			values.add(attribute(k, desig.get(k)));
		}
		Collection<Category> result = new LinkedList<Category>();
		for(CategoryId c : attributes.keySet()){
			result.add(category(c, attributes.get(c)));
		}
		return result;
	}

	private static Attribute attribute(AttributeDesignatorKey k, BagOfValues v){
		return Attribute
				.builder(k.getAttributeId())
				.issuer(k.getIssuer())
				.values(v)
				.build();
	}

	private static Category category(CategoryId c, Collection<Attribute> attributes){
		return Category.of(c, Entity.of(attributes));
	}

	@Override
	public void close(){
		factory.close();
	}
}
