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
import org.xacml4j.v30.spi.observe.DecisionObservation;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import org.xacml4j.v30.types.Entity;

/**
 * An implementation of {@link EvaluationContext} which
 * delegates all invocation to the given context.
 * <p>
 * A query about the rule, policy, policy set, or reference being evaluated is answered by
 * the nearest level that evaluates one; every other query that a level does not answer itself
 * goes to the root in one call (change optimize-pdp-evaluation, design D17). Only the contexts
 * of this package are walked through: a context of another implementation, or a subclass of
 * the public {@link Rule.RuleEvaluationContext} declared elsewhere, stops the walk and is
 * called as before, so its overrides keep working. That context is found once, when a context
 * is created, and a query about a level walks the parent fields only up to it (design D29).
 *
 * @author Giedrius Trumpickas
 */
abstract class DescendantEvaluationContext implements EvaluationContext
{
	private final static Logger LOG = LoggerFactory.getLogger(DescendantEvaluationContext.class);
	private final EvaluationContext parent;
	/**
	 * The first context above this one that is not one of this package's contexts: the root
	 * context, or a context of another implementation. The parent never changes, so it is
	 * found once, here, instead of by a walk per query (design D29)
	 */
	private final EvaluationContext top;
	private Status evaluationStatus;

	protected DescendantEvaluationContext(
			EvaluationContext context){
		Preconditions.checkNotNull(context);
		this.parent = context;
		this.top = isOwn(context) ? ((DescendantEvaluationContext)context).top : context;
	}

	@Override
	public Optional<Entity> resolveEntity(CategoryId categoryId) {
		return top.resolveEntity(categoryId);
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
		this.evaluationStatus = status;
		if(startsPass() && parent.getEvaluationStatus().isPresent()){
			// A failure inside an extended Indeterminate pass keeps the status of the failure
			// that started the pass
			return;
		}
		if(getCurrentDecisionRule().isPresent()){
			parent.setEvaluationStatus(getCurrentDecisionRule().get(), status);
		}
		parent.setEvaluationStatus(status);
	}

	/**
	 * Records a status in this context and passes it up as {@link #setEvaluationStatus(Status)} does,
	 * by the given decision rule of this level: a policy context names its policy while it evaluates one
	 * of its rules, which is then the current rule (design D47)
	 */
	final void setEvaluationStatusOf(DecisionRule level, Status status){
		this.evaluationStatus = status;
		if(startsPass() && parent.getEvaluationStatus().isPresent()){
			return;
		}
		parent.setEvaluationStatus(level, status);
		parent.setEvaluationStatus(status);
	}

	/**
	 * Tells whether this context starts an extended Indeterminate pass: it is in one, and its parent
	 * is not
	 */
	private boolean startsPass(){
		return isExtendedIndeterminateEval() && !parent.isExtendedIndeterminateEval();
	}

	public Optional<Status> getEvaluationStatus(){
		return Optional.ofNullable(evaluationStatus);
	}
	@Override
	public void setEvaluationStatus(DecisionRule rule, Status status) {
		if(rule == null){
			// No rule: the status of the decision, as at the root. It goes up level by level, so an
			// extended Indeterminate pass keeps the status of the failure that started it
			setEvaluationStatus(status);
			return;
		}
		top.setEvaluationStatus(rule, status);
	}

	@Override
	public Optional<Status> getEvaluationStatus(DecisionRule rule) {
		return top.getEvaluationStatus(rule);
	}

	protected EvaluationContext getParent(){
		return parent;
	}

	@Override
	public boolean isExtendedIndeterminateEval(){
		return parent.isExtendedIndeterminateEval();
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
	static boolean isOwn(EvaluationContext c){
		return c instanceof DescendantEvaluationContext d
				&& (!(d instanceof Rule.RuleEvaluationContext) || Rule.isOwnContext(d));
	}

	static EvaluationContext topOf(EvaluationContext context){
		return isOwn(context) ? ((DescendantEvaluationContext)context).top : context;
	}

	/**
	 * Tells whether a context belongs to an unobserved decision of a {@link RootEvaluationContext}:
	 * no observer sees its contexts, and every level above it is one of this package's contexts
	 * (cycle AP)
	 */
	static boolean isUnobserved(EvaluationContext context){
		return topOf(context) instanceof RootEvaluationContext root && !root.getDecisionObservation().isEnabled();
	}

	@Override
	public DecisionObservation getDecisionObservation(){
		return top.getDecisionObservation();
	}

	@Override
	public Clock getClock(){
		return top.getClock();
	}

	@Override
	public boolean isValidateFuncParamsAtRuntime() {
		return top.isValidateFuncParamsAtRuntime();
	}

	@Override
	public void setValidateFuncParamsAtRuntime(boolean validate){
		top.setValidateFuncParamsAtRuntime(validate);
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
		for(EvaluationContext c = this; c != top; c = ((DescendantEvaluationContext)c).parent){
			if(c instanceof Policy.PolicyDescendantEvaluationContext p){
				return p.policy();
			}
		}
		return top.getCurrentPolicy();
	}

	/**
	 * Answered by the nearest rule context, or the nearest policy context that evaluates one of its
	 * rules itself (design D47)
	 */
	@Override
	public DecisionRule getCurrentRule() {
		for(EvaluationContext c = this; c != top; c = ((DescendantEvaluationContext)c).parent){
			if(c instanceof Rule.RuleEvaluationContext r){
				return r.rule();
			}
			if(c instanceof Policy.PolicyDescendantEvaluationContext p && p.rule != null){
				return p.rule;
			}
		}
		return top.getCurrentRule();
	}

	@Override
	public void addEvaluationResult(CompositeDecisionRule policySet, Decision result) {
		// A root that records nothing is asked first, without the walk for the pass (cycle AU)
		if(top instanceof RootEvaluationContext root && !root.recordsEvaluatedPolicies()){
			return;
		}
		if(!isExtendedIndeterminateEval()){
			top.addEvaluationResult(policySet, result);
		}
	}

	/**
	 * Answered by the nearest policy set context
	 */
	@Override
	public CompositeDecisionRule getCurrentPolicySet() {
		for(EvaluationContext c = this; c != top; c = ((DescendantEvaluationContext)c).parent){
			if(c instanceof PolicySet.PolicySetDescendantEvaluationContext s){
				return s.policySet();
			}
		}
		return top.getCurrentPolicySet();
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
		return top.resolve(ref);
	}


	/**
	 * Answered by the nearest policy reference context
	 */
	@Override
	public CompositeDecisionRuleIDReference getCurrentPolicyIDReference() {
		for(EvaluationContext c = this; c != top; c = ((DescendantEvaluationContext)c).parent){
			if(c instanceof PolicyIDReference.PolicyIDReferenceEvaluationContext r){
				return r.reference();
			}
			if(c instanceof PolicyIDReference.ReferencedPolicyContext r){
				return r.reference();
			}
		}
		return top.getCurrentPolicyIDReference();
	}

	/**
	 * Answered by the nearest policy set reference context
	 */
	@Override
	public CompositeDecisionRuleIDReference getCurrentPolicySetIDReference() {
		for(EvaluationContext c = this; c != top; c = ((DescendantEvaluationContext)c).parent){
			if(c instanceof PolicySetIDReference.PolicySetIDReferenceEvaluationContext r){
				return r.reference();
			}
			if(c instanceof PolicySetIDReference.ReferencedPolicySetContext r){
				return r.reference();
			}
		}
		return top.getCurrentPolicySetIDReference();
	}

	/**
	 * Delegates call to {@link EvaluationContext} defaultProvider
	 */
	@Override
	public TimeZone getTimeZone() {
		return top.getTimeZone();
	}

	@Override
	public ZonedDateTime getCurrentDateTime() {
		return top.getCurrentDateTime();
	}

	@Override
	public final Optional<BagOfValues> resolve(
			AttributeReferenceKey ref)
			throws EvaluationException {
		return top.resolve(ref);
	}

	@Override
	public Collection<CompositeDecisionRuleIDReference> getEvaluatedPolicies() {
		return top.getEvaluatedPolicies();
	}

	@Override
	public Duration getDecisionCacheTTL() {
		return top.getDecisionCacheTTL();
	}

	@Override
	public void setDecisionCacheTTL(int ttl) {
		top.setDecisionCacheTTL(ttl);
	}

	@Override
	public Map<AttributeDesignatorKey, BagOfValues> getResolvedDesignators() {
		return top.getResolvedDesignators();
	}

	@Override
	public boolean hasResolvedDesignators() {
		// A context of another implementation may answer getResolvedDesignators() itself (review R-YZ)
		return isOwn(this) ? top.hasResolvedDesignators() : !getResolvedDesignators().isEmpty();
	}

	@Override
	public Map<AttributeSelectorKey, BagOfValues> getResolvedSelectors() {
		return top.getResolvedSelectors();
	}

	@Override
	public Collection<Obligation> getMatchingObligations(Decision decision) {
		return top.getMatchingObligations(decision);
	}

	@Override
	public void setDecisionCacheTTL(Duration ttl) {
		top.setDecisionCacheTTL(ttl);
	}

	@Override
	public Collection<Advice> getMatchingAdvices(Decision decision) {
		return top.getMatchingAdvices(decision);
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
