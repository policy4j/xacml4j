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

import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.CompositeDecisionRuleIDReference;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.MatchResult;
import org.xacml4j.v30.PolicyElement;
import org.xacml4j.v30.PolicyResolutionException;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.spi.observe.DecisionObservation;

import com.google.common.base.Preconditions;

public final class PolicyIDReference extends
	BaseCompositeDecisionRuleIDReference implements PolicyElement
{
	private PolicyIDReference(Builder b) {
		super(b);
	}

	public static Builder builder(){
		return new Builder();
	}

	public static Builder builder(String id){
		return new Builder().id(id);
	}

	/**
	 * Test this reference points to a given policy
	 *
	 * @param policy a policy
	 * @return {@code true} if a this reference
	 * points to a given policy
	 */
	@Override
	public boolean isReferenceTo(CompositeDecisionRule policy) {
		Policy p = (Policy)policy;
		return p != null &&
		matches(p.getId(), p.getVersion());
	}

	@Override
	public CompositeDecisionRuleIDReference getReference() {
		return this;
	}

	@Override
	public EvaluationContext createContext(EvaluationContext context)
	{
		Preconditions.checkNotNull(context);
		if(isReferenceContext(context)){
			return context;
		}
		if(context.getCurrentPolicy() != null) return invalidOrCyclicContext(context);
		if(isReferenceCyclic(this, context)) return cyclicContext(context);
		try{
			CompositeDecisionRule resolvedPolicy = context.resolve(this);
			if(resolvedPolicy == null){
				return new PolicyIDReferenceEvaluationContext(context);
			}
			return enter(context, resolvedPolicy);
		}catch(PolicyResolutionException e){
			return new PolicyIDReferenceEvaluationContext(context);
		}
	}

	EvaluationContext createContext(EvaluationContext context, CompositeDecisionRule target){
		Preconditions.checkNotNull(context);
		Preconditions.checkNotNull(target);
		if(isReferenceContext(context)) return context;
		if(context.getCurrentPolicy() != null){
			return isPolicyCyclic(target, context, true) ? cyclicContext(context) : invalidContext(context);
		}
		if(isReferenceCyclic(this, context)) return cyclicContext(context);
		return enter(context, target);
	}

	private boolean isReferenceContext(EvaluationContext context){
		return context.getCurrentPolicyIDReference() == this && !isEvaluating(context);
	}

	private boolean isEvaluating(EvaluationContext context){
		// The parent fields: getParentContext() skips the delegate of a staging wrapper
		for(EvaluationContext c = context; c instanceof DescendantEvaluationContext d; c = d.getParent()){
			if(c instanceof PolicyIDReferenceEvaluationContext r && r.reference() == this) return r.evaluating;
			if(c instanceof ReferencedPolicyContext r && r.reference() == this) return true;
		}
		return false;
	}

	void setEvaluating(EvaluationContext context, boolean evaluating){
		for(EvaluationContext c = context; c instanceof DescendantEvaluationContext d; c = d.getParent()){
			if(c instanceof PolicyIDReferenceEvaluationContext r && r.reference() == this){
				r.evaluating = evaluating;
				return;
			}
			if(c instanceof ReferencedPolicyContext r && r.reference() == this) return;
		}
	}

	private EvaluationContext invalidOrCyclicContext(EvaluationContext context){
		try{
			CompositeDecisionRule target = context.resolve(this);
			if(target != null && isPolicyCyclic(target, context, true)) return cyclicContext(context);
		}catch(RuntimeException e){
			return invalidContext(context);
		}
		return invalidContext(context);
	}

	private EvaluationContext invalidContext(EvaluationContext context){
		return failedContext(context, Status.syntaxError().message("Policy reference inside a policy").build());
	}

	private EvaluationContext cyclicContext(EvaluationContext context){
		return failedContext(context, Status.processingError().message("Cyclic reference detected").build());
	}

	private EvaluationContext failedContext(EvaluationContext context, Status status){
		PolicyIDReferenceEvaluationContext failed = new PolicyIDReferenceEvaluationContext(context, true);
		failed.setEvaluationStatus(status);
		return failed;
	}

	boolean isFailed(EvaluationContext context){
		return context instanceof PolicyIDReferenceEvaluationContext reference && reference.failed;
	}

	/**
	 * Creates the context of the referenced policy below a context of this reference. In an
	 * unobserved decision one context is both (cycle AP).
	 */
	private EvaluationContext enter(EvaluationContext context, CompositeDecisionRule policy){
		// The current policy was checked before resolving the reference.
		if(isPolicyCyclic(policy, context, false)) return cyclicContext(context);
		if(policy.getClass() == Policy.class && DescendantEvaluationContext.isUnobserved(context)){
			return new ReferencedPolicyContext((Policy)policy, context);
		}
		return policy.createContext(new PolicyIDReferenceEvaluationContext(context));
	}

	boolean canCheckTarget(EvaluationContext context, CompositeDecisionRule target){
		return context.getCurrentPolicyIDReference() != this && context.getCurrentPolicy() == null
				&& !isPolicyCyclic(target, context, false) && !isReferenceCyclic(this, context);
	}

	private static boolean isPolicyCyclic(CompositeDecisionRule target, EvaluationContext context,
			boolean includeCurrent){
		EvaluationContext top = DescendantEvaluationContext.topOf(context);
		// The parent fields: getParentContext() skips the delegate of a staging wrapper
		for(EvaluationContext c = context; c != top; c = ((DescendantEvaluationContext)c).getParent()){
			if(c instanceof Policy.PolicyDescendantEvaluationContext p && p.policy() == target) return true;
		}
		EvaluationContext first = includeCurrent || context != top ? top : top.getParentContext();
		for(EvaluationContext c = first; c != null; c = c.getParentContext()){
			if(c.getCurrentPolicy() == target) return true;
		}
		return false;
	}

	@Override
	public Decision evaluate(EvaluationContext context) {
		Preconditions.checkNotNull(context);
		Preconditions.checkArgument(context.getCurrentPolicyIDReference() == this);
		DecisionObservation o = RootEvaluationContext.observationOf(context);
		if(!o.isEnabled()){
			return evaluateReferenced(context);
		}
		Object parent = o.evaluationStarted(context, this);
		Decision d;
		try{
			d = evaluateReferenced(context);
		}catch(Throwable t){
			o.evaluationFailed(context, this, parent, t);
			throw t;
		}
		o.evaluationFinished(context, this, parent, d);
		return d;
	}

	private Decision evaluateReferenced(EvaluationContext context) {
		if(isFailed(context) || !isReferenceTo(context.getCurrentPolicy())){
			return Decision.INDETERMINATE;
		}
		CompositeDecisionRule p = context.getCurrentPolicy();
		Preconditions.checkNotNull(p);
		setEvaluating(context, true);
		try{
			return p.evaluate(context);
		}finally{
			setEvaluating(context, false);
		}
	}

	@Override
	public MatchResult isMatch(EvaluationContext context) {
		Preconditions.checkNotNull(context);
		Preconditions.checkArgument(context.getCurrentPolicyIDReference() == this);
		if(isFailed(context) || !isReferenceTo(context.getCurrentPolicy())){
			return MatchResult.INDETERMINATE;
		}
		CompositeDecisionRule p = context.getCurrentPolicy();
		Preconditions.checkNotNull(p);
		return p.isMatch(context);
	}

	public void accept(PolicyTreeVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		return (o instanceof PolicyIDReference)
				&& ((PolicyIDReference)o).equalsTo(this);
	}

	/**
	 * Validates if a given reference is cyclic
	 * in a given evaluation context
	 *
	 * @param ref a reference
	 * @param context an evaluation context
	 * @return {@code true} if given reference is cyclic; returns {@code false} otherwise
	 */
	private static boolean isReferenceCyclic(PolicyIDReference ref,
			EvaluationContext context)
	{
		CompositeDecisionRuleIDReference otherRef = context.getCurrentPolicyIDReference();
		if(otherRef != null){
			if(ref.equals(otherRef)){
				return true;
			}
			return isReferenceCyclic(ref, context.getParentContext());
		}
		return false;
	}

	/**
	 * An {@link EvaluationContext} implementation
	 * to evaluate {@link PolicySetIDReference} decisions
	 *
	 * @author Giedrius Trumpickas
	 */
	class PolicyIDReferenceEvaluationContext extends
	                                         DescendantEvaluationContext
	{
		private final boolean failed;
		private boolean evaluating;
		/**
		 * Creates a policy reference context with a given parent context
		 *
		 * @param context a parent evaluation context
		 */
		PolicyIDReferenceEvaluationContext(EvaluationContext context){
			this(context, false);
		}

		PolicyIDReferenceEvaluationContext(EvaluationContext context, boolean failed){
			super(context);
			this.failed = failed;
		}

		@Override
		public EvaluationContext getParentContext() {
			return getParent();
		}

		/**
		 * The reference of this context, which answers {@link #getCurrentPolicyIDReference()} (design D17)
		 */
		final PolicyIDReference reference() {
			return PolicyIDReference.this;
		}
	}

	/**
	 * The context of the referenced policy that is also the context of this reference (cycle AP)
	 */
	final class ReferencedPolicyContext extends Policy.PolicyDescendantEvaluationContext
	{
		ReferencedPolicyContext(Policy policy, EvaluationContext context){
			policy.super(context);
		}

		/**
		 * The reference of this context, which answers {@link #getCurrentPolicyIDReference()}
		 */
		PolicyIDReference reference() {
			return PolicyIDReference.this;
		}
	}

	public static class Builder extends BaseCompositeDecisionRuleIDReference.Builder<Builder>
	{
		@Override
		protected Builder getThis() {
			return this;
		}

		public PolicyIDReference build(){
			return new PolicyIDReference(this);
		}
	}

}
