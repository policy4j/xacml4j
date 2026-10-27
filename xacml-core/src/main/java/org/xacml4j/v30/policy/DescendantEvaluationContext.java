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
import org.xacml4j.v30.types.Entity;

/**
 * An implementation of {@link EvaluationContext} which
 * delegates all invocation to the given context.
 * <p>
 * A query about the rule, policy, policy set, or reference being evaluated is answered by
 * the nearest level that evaluates one; every other query that a level does not answer itself
 * goes to the root in one call. The parent fields are walked in a loop instead of calling each
 * level in turn (change optimize-pdp-evaluation, design D17). Only the contexts of this package
 * are walked through: a context of another implementation, or a subclass of the public
 * {@link Rule.RuleEvaluationContext} declared elsewhere, stops the walk and is called as before,
 * so its overrides keep working.
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
	public Optional<Entity> resolveEntity(CategoryId categoryId) {
		return top().resolveEntity(categoryId);
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
		top().setEvaluationStatus(rule, status);
	}

	@Override
	public Optional<Status> getEvaluationStatus(DecisionRule rule) {
		return top().getEvaluationStatus(rule);
	}

	protected EvaluationContext getParent(){
		return parent;
	}

	/**
	 * Tests if a context is one of this package's contexts, which answer a query the way
	 * this class does unless they override it for their own level
	 *
	 * @param c an evaluation context
	 * @return {@code true} for a context of this package; {@code false} for a context of
	 * another implementation or a subclass of {@link Rule.RuleEvaluationContext} declared
	 * elsewhere
	 */
	private static boolean isOwn(EvaluationContext c){
		return c instanceof DescendantEvaluationContext d
				&& (!(d instanceof Rule.RuleEvaluationContext) || Rule.isOwnContext(d));
	}

	/**
	 * The first context above this one that is not one of this package's contexts
	 *
	 * @return the root context, or a context of another implementation
	 */
	private EvaluationContext top(){
		EvaluationContext c = parent;
		while(isOwn(c)){
			c = ((DescendantEvaluationContext)c).parent;
		}
		return c;
	}

	@Override
	public Clock getClock(){
		return top().getClock();
	}

	@Override
	public boolean isValidateFuncParamsAtRuntime() {
		return top().isValidateFuncParamsAtRuntime();
	}

	@Override
	public void setValidateFuncParamsAtRuntime(boolean validate){
		top().setValidateFuncParamsAtRuntime(validate);
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
	 * Answered by the nearest policy context
	 */
	@Override
	public CompositeDecisionRule getCurrentPolicy() {
		EvaluationContext c = this;
		while(!(c instanceof Policy.PolicyDescendantEvaluationContext p)){
			c = ((DescendantEvaluationContext)c).parent;
			if(!isOwn(c)){
				return c.getCurrentPolicy();
			}
		}
		return p.policy();
	}

	/**
	 * Answered by the nearest rule context
	 */
	@Override
	public DecisionRule getCurrentRule() {
		EvaluationContext c = this;
		while(!(c instanceof Rule.RuleEvaluationContext r)){
			c = ((DescendantEvaluationContext)c).parent;
			if(!isOwn(c)){
				return c.getCurrentRule();
			}
		}
		return r.rule();
	}

	@Override
	public void addEvaluationResult(CompositeDecisionRule policySet, Decision result) {
		top().addEvaluationResult(policySet, result);
	}

	/**
	 * Answered by the nearest policy set context
	 */
	@Override
	public CompositeDecisionRule getCurrentPolicySet() {
		EvaluationContext c = this;
		while(!(c instanceof PolicySet.PolicySetDescendantEvaluationContext s)){
			c = ((DescendantEvaluationContext)c).parent;
			if(!isOwn(c)){
				return c.getCurrentPolicySet();
			}
		}
		return s.policySet();
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
		return top().resolve(ref);
	}


	/**
	 * Answered by the nearest policy reference context
	 */
	@Override
	public CompositeDecisionRuleIDReference getCurrentPolicyIDReference() {
		EvaluationContext c = this;
		while(!(c instanceof PolicyIDReference.PolicyIDReferenceEvaluationContext r)){
			c = ((DescendantEvaluationContext)c).parent;
			if(!isOwn(c)){
				return c.getCurrentPolicyIDReference();
			}
		}
		return r.reference();
	}

	/**
	 * Answered by the nearest policy set reference context
	 */
	@Override
	public CompositeDecisionRuleIDReference getCurrentPolicySetIDReference() {
		EvaluationContext c = this;
		while(!(c instanceof PolicySetIDReference.PolicySetIDReferenceEvaluationContext r)){
			c = ((DescendantEvaluationContext)c).parent;
			if(!isOwn(c)){
				return c.getCurrentPolicySetIDReference();
			}
		}
		return r.reference();
	}

	/**
	 * Delegates call to {@link EvaluationContext} defaultProvider
	 */
	@Override
	public TimeZone getTimeZone() {
		return top().getTimeZone();
	}

	@Override
	public ZonedDateTime getCurrentDateTime() {
		return top().getCurrentDateTime();
	}

	@Override
	public final Optional<BagOfValues> resolve(
			AttributeReferenceKey ref)
			throws EvaluationException {
		return top().resolve(ref);
	}

	@Override
	public Collection<CompositeDecisionRuleIDReference> getEvaluatedPolicies() {
		return top().getEvaluatedPolicies();
	}

	@Override
	public Duration getDecisionCacheTTL() {
		return top().getDecisionCacheTTL();
	}

	@Override
	public void setDecisionCacheTTL(int ttl) {
		top().setDecisionCacheTTL(ttl);
	}

	@Override
	public Map<AttributeDesignatorKey, BagOfValues> getResolvedDesignators() {
		return top().getResolvedDesignators();
	}

	@Override
	public Map<AttributeSelectorKey, BagOfValues> getResolvedSelectors() {
		return top().getResolvedSelectors();
	}

	@Override
	public Collection<Obligation> getMatchingObligations(Decision decision) {
		return top().getMatchingObligations(decision);
	}

	@Override
	public void setDecisionCacheTTL(Duration ttl) {
		top().setDecisionCacheTTL(ttl);
	}

	@Override
	public Collection<Advice> getMatchingAdvices(Decision decision) {
		return top().getMatchingAdvices(decision);
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
