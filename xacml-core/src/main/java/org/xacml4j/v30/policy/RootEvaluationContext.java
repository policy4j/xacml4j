package org.xacml4j.v30.policy;

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

import java.time.Clock;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.Advice;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.CompositeDecisionRuleIDReference;
import org.xacml4j.v30.Content;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.DecisionRuleResponse;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.PolicyResolutionException;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.ValueExpression;
import org.xacml4j.v30.XPathVersion;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;


public final class RootEvaluationContext implements EvaluationContext
{
	protected final Logger LOG = LoggerFactory.getLogger(RootEvaluationContext.class);

	private final XPathVersion defaultXPathVersion;
	private final EvaluationContextHandler contextHandler;
	private final PolicyReferenceResolver resolver;
	private final ConcurrentMap<String, Advice> denyAdvices;
	private final ConcurrentMap<String, Obligation> denyObligations;
	private final ConcurrentMap<String, Advice> permitAdvices;
	private final ConcurrentMap<String, Obligation> permitObligations;
	private final ConcurrentMap<String, CompositeDecisionRule> evaluatedPolicies;
	private final ConcurrentMap<DecisionRule, Status> evaluationStatusMap;

	private Clock clock;

	private boolean validateFuncParamsAtRuntime;
	private Status evaluationStatus;
	private AtomicReference<Duration> combinedDecisionCacheTTL;
	private final boolean extendedIndeterminateEval = false;

	public RootEvaluationContext(
			boolean validateFuncParamsAtRuntime,
			Duration defaultDecisionCacheTTL,
			XPathVersion defaultXPathVersion,
			PolicyReferenceResolver referenceResolver,
			EvaluationContextHandler contextHandler) {
		this(Clock.systemUTC(), validateFuncParamsAtRuntime,
				defaultDecisionCacheTTL,
				defaultXPathVersion,
				referenceResolver,
				contextHandler);
	}

	public RootEvaluationContext(
			Clock clock,
			boolean validateFuncParamsAtRuntime,
			Duration defaultDecisionCacheTTL,
			XPathVersion defaultXPathVersion,
			PolicyReferenceResolver referenceResolver,
			EvaluationContextHandler contextHandler) {
		Preconditions.checkNotNull(contextHandler);
		Preconditions.checkNotNull(referenceResolver);
		this.denyAdvices = new ConcurrentHashMap<>();
		this.denyObligations = new ConcurrentHashMap<>();
		this.permitAdvices = new ConcurrentHashMap<>();
		this.permitObligations = new ConcurrentHashMap<>();
		this.evaluationStatusMap = new ConcurrentHashMap<>();
		this.contextHandler = contextHandler;
		this.resolver = referenceResolver;
		this.clock = java.util.Objects.requireNonNull(clock);
		this.evaluatedPolicies = new ConcurrentSkipListMap<>();
		this.combinedDecisionCacheTTL = new AtomicReference<>(defaultDecisionCacheTTL);
		this.defaultXPathVersion = defaultXPathVersion;
		this.validateFuncParamsAtRuntime = validateFuncParamsAtRuntime;
	}

	public RootEvaluationContext(
			boolean validateFuncParamsAtRuntime,
			Duration defaultDecisionCacheTTL,
			PolicyReferenceResolver referenceResolver,
			EvaluationContextHandler handler){
		this(validateFuncParamsAtRuntime,
				defaultDecisionCacheTTL,
				XPathVersion.XPATH1,
				referenceResolver,
				handler);
	}

	@Override
	public void setEvaluationStatus(DecisionRule rule, Status status) {
		this.evaluationStatusMap.put(rule, status);
	}

	@Override
	public Optional<Status> getEvaluationStatus(DecisionRule rule) {
		return Optional.ofNullable(evaluationStatusMap.get(rule));
	}

	@Override
	public XPathVersion getXPathVersion() {
		return defaultXPathVersion;
	}

	@Override
	public Clock getClock(){
		return clock;
	}

	@Override
	public boolean isExtendedIndeterminateEval() {
		return extendedIndeterminateEval;
	}

	@Override
	public EvaluationContext createExtIndeterminateEvalContext() {
		return new DescendantEvaluationContext(this){
			@Override
			public EvaluationContext createExtIndeterminateEvalContext() {
				return this;
			}

			@Override
			public boolean isExtendedIndeterminateEval() {
				return true;
			}
		};
	}

	@Override
	public Optional<Status> getEvaluationStatus() {
		return Optional.ofNullable(evaluationStatus);
	}


	@Override
	public Duration getDecisionCacheTTL() {
		Duration v  = combinedDecisionCacheTTL != null?combinedDecisionCacheTTL.get():null;
		return v != null?v:Duration.ZERO;
	}

	@Override
	public void setDecisionCacheTTL(Duration ttl) {
		combinedDecisionCacheTTL.accumulateAndGet(ttl,
		                                          (a, b)-> Duration.ofMillis(Math.min(a.toMillis(), b.toMillis())));
	}

	@Override
	public final void addEvaluationResult(CompositeDecisionRule policy, Decision result) {
		this.evaluatedPolicies.put(policy.getId(), policy);
	}

	@Override
	public boolean isValidateFuncParamsAtRuntime() {
		return validateFuncParamsAtRuntime;
	}

	@Override
	public void setValidateFuncParamsAtRuntime(boolean validate){
		this.validateFuncParamsAtRuntime = validate;
	}

	@Override
	public void addAdvices(Decision d, Iterable<Advice> advices)
	{
		java.util.Objects.requireNonNull(d, "decision");
		if(d.isIndeterminate() ||
				d == Decision.NOT_APPLICABLE){
			return;
		}
		for(Advice a : advices){
			LOG.debug("Adding advice=\"{}\"", a);
			addAndMergeConcurrent(a, ()-> (Decision.PERMIT.equals(d) ? permitAdvices :
			                               ((Decision.DENY.equals(d))?denyAdvices:null)));
		}
	}




	@Override
	public void addObligations(Decision d, Iterable<Obligation> obligations)
	{
		java.util.Objects.requireNonNull(d, "decision");
		if(d.isIndeterminate() ||
				d == Decision.NOT_APPLICABLE){
			return;
		}
		for(Obligation a : obligations){
			addAndMergeConcurrent(a, ()-> (Decision.PERMIT.equals(d) ? permitObligations :
			                               ((Decision.DENY.equals(d))?denyObligations:null)));
		}
	}

	private <T extends DecisionRuleResponse> void addAndMergeConcurrent(T response,
	                                                                    Supplier<ConcurrentMap<String, T>> mapSupplier)
	{
		LOG.debug("Processing {}=\"{}\"", response.getClass().getSimpleName(), response);
		ConcurrentMap<String, T> rules = mapSupplier.get();
		if(rules == null){
			return;
		}
		rules.putIfAbsent(response.getId(), response);
		rules.computeIfPresent(response.getId(), (id,r)->r.merge(response));
	}

	@Override
	public Optional<BagOfValues> resolve(AttributeReferenceKey ref)
			throws EvaluationException {
		return contextHandler.resolve(this, ref);
	}

	@Override
	public <C extends Content> Optional<C> resolve(CategoryId categoryId, Content.Type type) {
		return contextHandler.<C>getContent(
				categoryId)
				.filter(
						c -> c.getType().equals(type));
	}

	/**
	 * Implementation always return {@code null}
	 */
	@Override
	public EvaluationContext getParentContext() {
		return null;
	}

	/**
	 * Implementation always returns {@code null}
	 */
	@Override
	public Policy getCurrentPolicy() {
		return null;
	}

	@Override
	public DecisionRule getCurrentRule() {
		return null;
	}

	/**
	 * Implementation always returns {@code null}
	 */
	@Override
	public PolicyIDReference getCurrentPolicyIDReference() {
		return null;
	}

	/**
	 * Implementation always returns {@code null}
	 */
	@Override
	public PolicySetIDReference getCurrentPolicySetIDReference() {
		return null;
	}

	/**
	 * Implementation always returns {@code null}
	 */
	@Override
	public PolicySet getCurrentPolicySet() {
		return null;
	}

	@Override
	public final ValueExpression getVariableEvaluationResult(
			String variableId){
		return null;
	}

	@Override
	public final void setVariableEvaluationResult(String variableId, ValueExpression value) {
	}

	@Override
	public final CompositeDecisionRule resolve(CompositeDecisionRuleIDReference ref)
			throws PolicyResolutionException
	{
		if(ref instanceof PolicyIDReference){
			return resolve((PolicyIDReference)ref);
		}
		if(ref instanceof PolicySetIDReference){
			return resolve((PolicySetIDReference)ref);
		}
		throw new PolicyResolutionException(this,
				"Failed to resolve reference");
	}

	private Policy resolve(PolicyIDReference ref)
		throws PolicyResolutionException {
		Policy p =	resolver.resolve(ref);
		if(LOG.isDebugEnabled()){
			LOG.debug("Trying to resolve " +
					"Policy reference=\"{}\"", ref);
		}
		if(p == null){
			throw new PolicyResolutionException(this,
					"Failed to resolve PolicySet reference");
		}
		return p;
	}

	private PolicySet resolve(PolicySetIDReference ref)
			throws PolicyResolutionException {
		PolicySet p = resolver.resolve(ref);
		if(LOG.isDebugEnabled()){
			LOG.debug("Trying to resolve " +
					"PolicySet reference=\"{}\"", ref);
		}
		if(p == null){
			throw new PolicyResolutionException(this,
					"Failed to resolve PolicySet reference");
		}
		return p;
	}

	@Override
	public Collection<CompositeDecisionRuleIDReference> getEvaluatedPolicies() {
		return evaluatedPolicies.values()
		                        .stream()
		                        .map(v->v.getReference())
		                        .collect(Collectors.toList());
	}

	@Override
	public Map<AttributeDesignatorKey, BagOfValues> getResolvedDesignators() {
		return contextHandler.getResolvedDesignators();
	}

	@Override
	public Map<AttributeSelectorKey, BagOfValues> getResolvedSelectors() {
		return contextHandler.getResolvedSelectors();
	}

	@Override
	public Collection<Obligation> getMatchingObligations(final Decision decision) {
		return (decision == Decision.PERMIT)?permitObligations.values():denyObligations.values();
	}

	@Override
	public Collection<Advice> getMatchingAdvices(final Decision decision) {
		return (decision == Decision.PERMIT)?permitAdvices.values():denyAdvices.values();
	}

	@Override
	public String toString() {
		return MoreObjects
				.toStringHelper(this)
				.add("defaultXPathVersion", defaultXPathVersion)
				.add("contextHandler", contextHandler)
				.add("resolver", resolver)
				.add("denyAdvices", denyAdvices)
				.add("denyObligations", denyObligations)
				.add("permitAdvices", permitAdvices)
				.add("permitObligations", permitObligations)
				.add("evaluatedPolicies", evaluatedPolicies)
				.add("clock", clock)
				.add("validateFuncParamsAtRuntime", validateFuncParamsAtRuntime)
				.add("evaluationStatus", evaluationStatus)
				.add("combinedDecisionCacheTTL", combinedDecisionCacheTTL)
				.add("extendedIndeterminateEval", extendedIndeterminateEval)
				.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(defaultXPathVersion,
				contextHandler,
				resolver,
				denyAdvices,
				denyObligations,
				permitAdvices,
				permitObligations,
				evaluatedPolicies,
				validateFuncParamsAtRuntime,
				evaluationStatus,
				combinedDecisionCacheTTL,
				extendedIndeterminateEval);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof RootEvaluationContext)) {
			return false;
		}
		final RootEvaluationContext c = (RootEvaluationContext) o;
		return Objects.equal(defaultXPathVersion, c.defaultXPathVersion)
			&& Objects.equal(contextHandler, c.contextHandler)
			&& Objects.equal(resolver, c.resolver)
			&& Objects.equal(denyAdvices, c.denyAdvices)
			&& Objects.equal(denyObligations, c.denyObligations)
			&& Objects.equal(permitAdvices, c.permitAdvices)
			&& Objects.equal(permitObligations, c.permitObligations)
			&& Objects.equal(evaluatedPolicies, c.evaluatedPolicies)
			&& Objects.equal(clock, c.clock)
			&& Objects.equal(validateFuncParamsAtRuntime, c.validateFuncParamsAtRuntime)
			&& Objects.equal(evaluationStatus, c.evaluationStatus)
			&& Objects.equal(combinedDecisionCacheTTL, c.combinedDecisionCacheTTL)
			&& Objects.equal(extendedIndeterminateEval, c.extendedIndeterminateEval);
	}

	/**
	 * Clears context state
	 */
	public void clear() {
		this.combinedDecisionCacheTTL = null;
		this.evaluatedPolicies.clear();
	}
}


