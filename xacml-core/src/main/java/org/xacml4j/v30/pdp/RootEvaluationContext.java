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

import com.google.common.base.*;
import com.google.common.base.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.*;
import org.xacml4j.v30.spi.repository.PolicyReferenceResolver;

import java.time.Clock;

import java.util.*;
import java.util.Optional;


public final class RootEvaluationContext implements EvaluationContext
{
	protected final Logger log = LoggerFactory.getLogger(RootEvaluationContext.class);

	private final XPathVersion defaultXPathVersion;
	private final EvaluationContextHandler contextHandler;
	private final PolicyReferenceResolver resolver;
	private final Map<String, Advice> denyAdvices;
	private final Map<String, Obligation> denyObligations;
	private final Map<String, Advice> permitAdvices;
	private final Map<String, Obligation> permitObligations;
	private final List<CompositeDecisionRuleIDReference> evaluatedPolicies;
	private Clock clock;

	private boolean validateFuncParamsAtRuntime;
	private Optional<Status> evaluationStatus;
	private Integer combinedDecisionCacheTTL = null;
	private final boolean extendedIndeterminateEval = false;

	public RootEvaluationContext(
			boolean validateFuncParamsAtRuntime,
			int defaultDecisionCacheTTL,
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
			int defaultDecisionCacheTTL,
			XPathVersion defaultXPathVersion,
			PolicyReferenceResolver referenceResolver,
			EvaluationContextHandler contextHandler) {
		Preconditions.checkNotNull(contextHandler);
		Preconditions.checkNotNull(referenceResolver);
		this.denyAdvices = new LinkedHashMap<>();
		this.denyObligations = new LinkedHashMap<>();
		this.permitAdvices = new LinkedHashMap<>();
		this.permitObligations = new LinkedHashMap<>();
		this.contextHandler = contextHandler;
		this.resolver = referenceResolver;
		this.clock = java.util.Objects.requireNonNull(clock);
		this.evaluatedPolicies = new LinkedList<>();
		this.combinedDecisionCacheTTL = (defaultDecisionCacheTTL > 0)?defaultDecisionCacheTTL:null;
		this.defaultXPathVersion = defaultXPathVersion;
		this.validateFuncParamsAtRuntime = validateFuncParamsAtRuntime;
	}

	public RootEvaluationContext(
			boolean validateFuncParamsAtRuntime,
			int defaultDecisionCacheTTL,
			PolicyReferenceResolver referenceResolver,
			EvaluationContextHandler handler){
		this(validateFuncParamsAtRuntime,
				defaultDecisionCacheTTL,
				XPathVersion.XPATH1,
				referenceResolver,
				handler);
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
		return new DelegatingEvaluationContext(this){
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
		return evaluationStatus;
	}

	@Override
	public void setEvaluationStatus(Status status){
		this.evaluationStatus = Optional.ofNullable(status);
	}

	@Override
	public int getDecisionCacheTTL() {
		return (combinedDecisionCacheTTL != null && combinedDecisionCacheTTL >= 0 )? combinedDecisionCacheTTL:0;
	}

	@Override
	public void setDecisionCacheTTL(int ttl) {
		if(combinedDecisionCacheTTL == null){
			this.combinedDecisionCacheTTL = ttl;
			return;
		}
		this.combinedDecisionCacheTTL = (ttl > 0)?Math.min(this.combinedDecisionCacheTTL, ttl):0;
	}

	@Override
	public final void addEvaluationResult(CompositeDecisionRule policy, Decision result) {
		this.evaluatedPolicies.add(policy.getReference());
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
			addAndMege(a, ()-> (Decision.PERMIT.equals(d)?permitAdvices:
					((Decision.DENY.equals(d))?denyAdvices:Collections.emptyMap())));
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
			addAndMege(a, ()-> (Decision.PERMIT.equals(d)?permitObligations:
					((Decision.DENY.equals(d))?denyObligations:Collections.emptyMap())));
		}
	}

	private <T extends DecisionRuleResponse> void addAndMege(T response, Supplier<Map<String, T>> mapSupplier)
	{
		Optional.ofNullable(mapSupplier.get())
				.map(v->Optional.ofNullable(v.get(
						response.getId())).map(r->r.merge(response)));
	}

	@Override
	public Optional<BagOfAttributeValues> resolve(AttributeReferenceKey ref)
			throws EvaluationException {
		return contextHandler.resolve(this, ref);
	}

	@Override
	public <C extends Content> Optional<C> resolve(Optional<CategoryId> categoryId, Content.Type type) {
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
		if(log.isDebugEnabled()){
			log.debug("Trying to resolve " +
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
		if(log.isDebugEnabled()){
			log.debug("Trying to resolve " +
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
		return Collections.unmodifiableList(evaluatedPolicies);
	}

	@Override
	public Map<AttributeDesignatorKey, BagOfAttributeValues> getResolvedDesignators() {
		return contextHandler.getResolvedDesignators();
	}

	@Override
	public Map<AttributeSelectorKey, BagOfAttributeValues> getResolvedSelectors() {
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


