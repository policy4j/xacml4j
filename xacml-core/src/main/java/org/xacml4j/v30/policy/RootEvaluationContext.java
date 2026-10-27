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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

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
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.DecisionRuleResponse;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.PolicyResolutionException;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.ValueExp;
import org.xacml4j.v30.XPathVersion;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import org.xacml4j.v30.types.Entity;


public final class RootEvaluationContext implements EvaluationContext
{
	private static final Logger LOG = LoggerFactory.getLogger(RootEvaluationContext.class);

	// The maps below are created on their first use and published with a
	// compare-and-set (change optimize-pdp-evaluation, D9). A decision creates the
	// advice and obligation maps of its effect when the result reads them, and the
	// getters return the maps' own values views, so a kept view behaves as before
	// (reviews rv006 to rv010). A missing map compares and hashes as an empty one.
	private static final VarHandle DENY_ADVICES = handle("denyAdvices", ConcurrentMap.class);
	private static final VarHandle PERMIT_ADVICES = handle("permitAdvices", ConcurrentMap.class);
	private static final VarHandle DENY_OBLIGATIONS = handle("denyObligations", ConcurrentMap.class);
	private static final VarHandle PERMIT_OBLIGATIONS = handle("permitObligations", ConcurrentMap.class);
	private static final VarHandle EVALUATED_POLICIES = handle("evaluatedPolicies", ConcurrentMap.class);
	private static final VarHandle EVALUATION_STATUS_MAP = handle("evaluationStatusMap", ConcurrentMap.class);

	private final XPathVersion defaultXPathVersion;
	private final EvaluationContextHandler contextHandler;
	private final PolicyReferenceResolver resolver;
	private volatile ConcurrentMap<String, Advice> denyAdvices;
	private volatile ConcurrentMap<String, Advice> permitAdvices;
	private volatile ConcurrentMap<String, Obligation> denyObligations;
	private volatile ConcurrentMap<String, Obligation> permitObligations;
	private volatile ConcurrentMap<String, CompositeDecisionRule> evaluatedPolicies;
	private volatile ConcurrentMap<DecisionRule, Status> evaluationStatusMap;

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
		this.contextHandler = contextHandler;
		this.resolver = referenceResolver;
		this.clock = java.util.Objects.requireNonNull(clock);
		this.combinedDecisionCacheTTL = new AtomicReference<>(defaultDecisionCacheTTL);
		this.defaultXPathVersion = defaultXPathVersion;
		this.validateFuncParamsAtRuntime = validateFuncParamsAtRuntime;
	}

	private static VarHandle handle(String field, Class<?> type) {
		try {
			return MethodHandles.lookup().findVarHandle(RootEvaluationContext.class, field, type);
		} catch (ReflectiveOperationException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	/** @return the given new value, or the value another thread set first */
	private <T> T created(VarHandle field, T value) {
		@SuppressWarnings("unchecked")
		T witness = (T) field.compareAndExchange(this, null, value);
		return witness != null ? witness : value;
	}

	private ConcurrentMap<String, Advice> denyAdvicesMap() {
		ConcurrentMap<String, Advice> m = denyAdvices;
		return m != null ? m : created(DENY_ADVICES, new ConcurrentHashMap<>());
	}

	private ConcurrentMap<String, Advice> permitAdvicesMap() {
		ConcurrentMap<String, Advice> m = permitAdvices;
		return m != null ? m : created(PERMIT_ADVICES, new ConcurrentHashMap<>());
	}

	private ConcurrentMap<String, Obligation> denyObligationsMap() {
		ConcurrentMap<String, Obligation> m = denyObligations;
		return m != null ? m : created(DENY_OBLIGATIONS, new ConcurrentHashMap<>());
	}

	private ConcurrentMap<String, Obligation> permitObligationsMap() {
		ConcurrentMap<String, Obligation> m = permitObligations;
		return m != null ? m : created(PERMIT_OBLIGATIONS, new ConcurrentHashMap<>());
	}

	private ConcurrentMap<String, CompositeDecisionRule> evaluatedPolicies() {
		ConcurrentMap<String, CompositeDecisionRule> m = evaluatedPolicies;
		return m != null ? m : created(EVALUATED_POLICIES, new ConcurrentHashMap<>());
	}

	private ConcurrentMap<DecisionRule, Status> evaluationStatusMap() {
		ConcurrentMap<DecisionRule, Status> m = evaluationStatusMap;
		return m != null ? m : created(EVALUATION_STATUS_MAP, new ConcurrentHashMap<>());
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
	public void setEvaluationStatus(Status status) {
		LOG.debug("Setting decisionRuleId=\"{}\"", status);
		this.evaluationStatus = status;
	}

	@Override
	public void setEvaluationStatus(DecisionRule rule, Status status) {
		if(rule == null){
			this.evaluationStatus = status;
			return;
		}
		evaluationStatusMap().put(rule, status);
	}

	@Override
	public void setEvaluationStatusIfAbsent(java.util.function.Supplier<Status> supplier)
	{
		if(evaluationStatus == null){
			Status status = supplier.get();
			this.evaluationStatus = status;
		}
	}

	@Override
	public Optional<Status> getEvaluationStatus(DecisionRule rule) {
		// The concurrent map rejected a null rule; so does the missing one
		java.util.Objects.requireNonNull(rule);
		Map<DecisionRule, Status> m = evaluationStatusMap;
		return m == null ? Optional.empty() : Optional.ofNullable(m.get(rule));
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
		return this;
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
		if (!result.isIndeterminate() && result != Decision.NOT_APPLICABLE) {
			evaluatedPolicies().put(policy.getId(), policy);
		}
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
			addAndMergeConcurrent(a, Decision.PERMIT.equals(d) ? permitAdvicesMap() :
			                         ((Decision.DENY.equals(d))?denyAdvicesMap():null));
		}
	}

	@Override
	public Optional<Entity> resolveEntity(CategoryId categoryId) {
		return contextHandler.getEntity(categoryId);
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
			addAndMergeConcurrent(a, Decision.PERMIT.equals(d) ? permitObligationsMap() :
			                         ((Decision.DENY.equals(d))?denyObligationsMap():null));
		}
	}

	private <T extends DecisionRuleResponse> void addAndMergeConcurrent(T response,
	                                                                    ConcurrentMap<String, T> rules)
	{
		LOG.debug("Processing {}=\"{}\"", response.getClass().getSimpleName(), response);
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
	public final ValueExp getVariableEvaluationResult(
			String variableId){
		return null;
	}

	@Override
	public final void setVariableEvaluationResult(String variableId, ValueExp value) {
	}

	@Override
	public final CompositeDecisionRule resolve(CompositeDecisionRuleIDReference ref)
			throws PolicyResolutionException
	{
		if(ref instanceof PolicyIDReference){
			return resolveEntity((PolicyIDReference)ref);
		}
		if(ref instanceof PolicySetIDReference){
			return resolveEntity((PolicySetIDReference)ref);
		}
		throw new PolicyResolutionException(this);
	}

	private Policy resolveEntity(PolicyIDReference ref)
		throws PolicyResolutionException {
		Policy p =	resolver.resolve(ref);
		if(LOG.isDebugEnabled()){
			LOG.debug("Trying to resolve " +
					"Policy reference=\"{}\"", ref);
		}
		if(p == null){
			throw new PolicyResolutionException(this);
		}
		return p;
	}

	private PolicySet resolveEntity(PolicySetIDReference ref)
			throws PolicyResolutionException {
		PolicySet p = resolver.resolve(ref);
		if(LOG.isDebugEnabled()){
			LOG.debug("Trying to resolve " +
					"PolicySet reference=\"{}\"", ref);
		}
		if(p == null){
			throw new PolicyResolutionException(this);
		}
		return p;
	}

	@Override
	public Collection<CompositeDecisionRuleIDReference> getEvaluatedPolicies() {
		// In the order of the policy ids, as the sorted map kept them before
		Map<String, CompositeDecisionRule> m = evaluatedPolicies;
		if(m == null || m.isEmpty()){
			// A new modifiable list, as the stream collector returned (review RVC2)
			return new ArrayList<>(0);
		}
		String[] ids = m.keySet().toArray(new String[0]);
		Arrays.sort(ids);
		List<CompositeDecisionRuleIDReference> references = new ArrayList<>(ids.length);
		for(String id : ids){
			CompositeDecisionRule policy = m.get(id);
			if(policy != null){
				references.add(policy.getReference());
			}
		}
		return references;
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
		return (decision == Decision.PERMIT)?permitObligationsMap().values():denyObligationsMap().values();
	}

	@Override
	public Collection<Advice> getMatchingAdvices(final Decision decision) {
		return (decision == Decision.PERMIT)?permitAdvicesMap().values():denyAdvicesMap().values();
	}

	@Override
	public String toString() {
		return MoreObjects
				.toStringHelper(this)
				.add("defaultXPathVersion", defaultXPathVersion)
				.add("contextHandler", contextHandler.getClass().getSimpleName())
				.add("resolver", resolver.getClass().getSimpleName())
				.add("clock", clock)
				.add("validateFuncParamsAtRuntime", validateFuncParamsAtRuntime)
				.add("evaluationStatus", evaluationStatus)
				.add("combinedDecisionCacheTTL", combinedDecisionCacheTTL)
				.add("extendedIndeterminateEval", extendedIndeterminateEval)
				.add("denyAdvices", denyAdvices())
				.add("denyObligations", denyObligations())
				.add("permitAdvices", permitAdvices())
				.add("permitObligations", permitObligations())
				.add("evaluatedPolicies", orEmpty(evaluatedPolicies))
				.toString();
	}

	@Override
	public int hashCode() {
		// A missing map hashes as an empty one: both give 0
		return Objects.hashCode(defaultXPathVersion,
				contextHandler,
				resolver,
				denyAdvices(),
				denyObligations(),
				permitAdvices(),
				permitObligations(),
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
			&& denyAdvices().equals(c.denyAdvices())
			&& denyObligations().equals(c.denyObligations())
			&& permitAdvices().equals(c.permitAdvices())
			&& permitObligations().equals(c.permitObligations())
			&& orEmpty(evaluatedPolicies).equals(orEmpty(c.evaluatedPolicies))
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
		Map<String, CompositeDecisionRule> m = evaluatedPolicies;
		if(m != null){
			m.clear();
		}
	}

	private static <K, V> Map<K, V> orEmpty(Map<K, V> m) {
		return m != null ? m : Collections.emptyMap();
	}

	private Map<String, Advice> denyAdvices() {
		return orEmpty(denyAdvices);
	}

	private Map<String, Obligation> denyObligations() {
		return orEmpty(denyObligations);
	}

	private Map<String, Advice> permitAdvices() {
		return orEmpty(permitAdvices);
	}

	private Map<String, Obligation> permitObligations() {
		return orEmpty(permitObligations);
	}
}
