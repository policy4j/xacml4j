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
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

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
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.PolicyResolutionException;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.ValueExp;
import org.xacml4j.v30.XPathVersion;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * An implementation of {@link EvaluationContext} which
 * delegates all invocation to the given context
 *
 * @author Giedrius Trumpickas
 */
abstract class DescendantEvaluationContext implements EvaluationContext
{
	private final static Logger LOG = LoggerFactory.getLogger(DescendantEvaluationContext.class);
	private final EvaluationContext parent;
	private Status evaluationStatus;

	protected DescendantEvaluationContext(
			EvaluationContext context){
		Preconditions.checkNotNull(context);
		this.parent = context;
	}

	@Override
	public void setEvaluationStatusIfAbsent(java.util.function.Supplier<Status> supplier)
	{
		if(evaluationStatus == null){
			setEvaluationStatus(supplier.get());
		}
	}

	@Override
	public void setEvaluationStatus(Status status) {
		if(getCurrentDecisionRule().isPresent()){
			parent.setEvaluationStatus(getCurrentDecisionRule().get(), status);
		}
		parent.setEvaluationStatus(status);
		this.evaluationStatus = status;
	}

	public Optional<Status> getEvaluationStatus(){
		return Optional.ofNullable(evaluationStatus);
	}
	@Override
	public void setEvaluationStatus(DecisionRule rule, Status status) {
		this.parent.setEvaluationStatus(rule, status);
	}

	@Override
	public Optional<Status> getEvaluationStatus(DecisionRule rule) {
		return parent.getEvaluationStatus(rule);
	}

	protected EvaluationContext getParent(){
		return parent;
	}

	@Override
	public Clock getClock(){
		return parent.getClock();
	}

	@Override
	public boolean isValidateFuncParamsAtRuntime() {
		return parent.isValidateFuncParamsAtRuntime();
	}

	@Override
	public void setValidateFuncParamsAtRuntime(boolean validate){
		parent.setValidateFuncParamsAtRuntime(validate);
	}

	@Override
	public void addAdvices(Decision d, Iterable<Advice> advices){
		parent.addAdvices(d, advices);
	}

	@Override
	public void addObligations(Decision d, Iterable<Obligation> obligations){
		parent.addObligations(d, obligations);
	}

	/**
	 * Delegates call to {@link EvaluationContext} defaultProvider
	 */
	@Override
	public CompositeDecisionRule getCurrentPolicy() {
		return parent.getCurrentPolicy();
	}


	@Override
	public DecisionRule getCurrentRule() {
		return parent.getCurrentRule();
	}

	@Override
	public void addEvaluationResult(CompositeDecisionRule policySet, Decision result) {
		parent.addEvaluationResult(policySet, result);
	}

	/**
	 * Delegates call to {@link EvaluationContext} defaultProvider
	 */
	@Override
	public CompositeDecisionRule getCurrentPolicySet() {
		return parent.getCurrentPolicySet();
	}

	/**
	 * Delegates call to {@link EvaluationContext} defaultProvider
	 */
	@Override
	public ValueExp getVariableEvaluationResult(String variableId) {
		return parent.getVariableEvaluationResult(variableId);
	}

	/**
	 * Delegates call to {@link EvaluationContext} defaultProvider
	 */
	@Override
	public void setVariableEvaluationResult(String variableId, ValueExp value) {
		parent.setVariableEvaluationResult(variableId, value);
	}

	/**
	 * Delegates call to {@link EvaluationContext} defaultProvider
	 */
	@Override
	public CompositeDecisionRule resolve(CompositeDecisionRuleIDReference ref)
			throws PolicyResolutionException
	{
		return parent.resolve(ref);
	}


	/**
	 * Delegates call to {@link EvaluationContext} defaultProvider
	 */
	@Override
	public CompositeDecisionRuleIDReference getCurrentPolicyIDReference() {
		return parent.getCurrentPolicyIDReference();
	}

	/**
	 * Delegates call to {@link EvaluationContext} defaultProvider
	 */
	@Override
	public CompositeDecisionRuleIDReference getCurrentPolicySetIDReference() {
		return parent.getCurrentPolicySetIDReference();
	}

	/**
	 * Delegates call to {@link EvaluationContext} defaultProvider
	 */
	@Override
	public TimeZone getTimeZone() {
		return parent.getTimeZone();
	}

	@Override
	public ZonedDateTime getCurrentDateTime() {
		return parent.getCurrentDateTime();
	}

	@Override
	public final Optional<BagOfValues> resolve(
			AttributeReferenceKey ref)
			throws EvaluationException {
		return parent.resolve(ref);
	}

	@Override
	public <C extends Content> Optional<C> resolve(CategoryId categoryId, Content.Type type) {
		return parent.resolve(categoryId, type);
	}

	@Override
	public Collection<CompositeDecisionRuleIDReference> getEvaluatedPolicies() {
		return parent.getEvaluatedPolicies();
	}

	@Override
	public Duration getDecisionCacheTTL() {
		return parent.getDecisionCacheTTL();
	}

	@Override
	public void setDecisionCacheTTL(int ttl) {
		parent.setDecisionCacheTTL(ttl);
	}

	@Override
	public Map<AttributeDesignatorKey, BagOfValues> getResolvedDesignators() {
		return parent.getResolvedDesignators();
	}

	@Override
	public Map<AttributeSelectorKey, BagOfValues> getResolvedSelectors() {
		return parent.getResolvedSelectors();
	}

	@Override
	public Collection<Obligation> getMatchingObligations(Decision decision) {
		return parent.getMatchingObligations(decision);
	}

	@Override
	public void setDecisionCacheTTL(Duration ttl) {
		parent.setDecisionCacheTTL(ttl);
	}

	@Override
	public Collection<Advice> getMatchingAdvices(Decision decision) {
		return parent.getMatchingAdvices(decision);
	}

	@Override
	public EvaluationContext getParentContext() {
		return parent;
	}

	@Override
	public XPathVersion getXPathVersion() {
		return parent.getXPathVersion();
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("delegate", parent).toString();
	}

	@Override
	public int hashCode() {
		return parent.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return o == this ||
				(o instanceof DescendantEvaluationContext) &&
						parent.equals(((DescendantEvaluationContext) o).parent);
	}
}
