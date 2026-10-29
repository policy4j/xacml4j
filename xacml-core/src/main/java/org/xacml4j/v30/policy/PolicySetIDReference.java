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

/**
 * A XACML {@link PolicySet} reference
 *
 * @author Giedrius Trumpickas
 */
public class PolicySetIDReference extends BaseCompositeDecisionRuleIDReference
	implements PolicyElement
{

	private PolicySetIDReference(Builder b) {
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
	 * points to a given policies
	 */
	@Override
	public boolean isReferenceTo(CompositeDecisionRule policy) {
		PolicySet p = (PolicySet)policy;
		return p != null &&
		matches(p.getId(), p.getVersion());
	}

	/**
	 * Creates an {@link EvaluationContext} to evaluate this reference.
	 */
	@Override
	public EvaluationContext createContext(EvaluationContext context)
	{
		Preconditions.checkNotNull(context);
		if(isReferenceContext(context)){
			return context;
		}
		if(isReferenceCyclic(this, context)) return cyclicContext(context);
		try{
			CompositeDecisionRule policySet = context.resolve(this);
			if(policySet == null){
				return new PolicySetIDReferenceEvaluationContext(context);
			}
			return enter(context, policySet);
		}catch(PolicyResolutionException e){
			return new PolicySetIDReferenceEvaluationContext(context);
		}
	}

	EvaluationContext createContext(EvaluationContext context, CompositeDecisionRule target){
		Preconditions.checkNotNull(context);
		Preconditions.checkNotNull(target);
		if(isReferenceContext(context)) return context;
		if(isReferenceCyclic(this, context)) return cyclicContext(context);
		return enter(context, target);
	}

	private boolean isReferenceContext(EvaluationContext context){
		return context.getCurrentPolicySetIDReference() == this && !isEvaluating(context);
	}

	private boolean isEvaluating(EvaluationContext context){
		// The parent fields: getParentContext() skips the delegate of a staging wrapper
		for(EvaluationContext c = context; c instanceof DescendantEvaluationContext d; c = d.getParent()){
			if(c instanceof PolicySetIDReferenceEvaluationContext r && r.reference() == this) return r.evaluating;
			if(c instanceof ReferencedPolicySetContext r && r.reference() == this) return true;
		}
		return false;
	}

	void setEvaluating(EvaluationContext context, boolean evaluating){
		for(EvaluationContext c = context; c instanceof DescendantEvaluationContext d; c = d.getParent()){
			if(c instanceof PolicySetIDReferenceEvaluationContext r && r.reference() == this){
				r.evaluating = evaluating;
				return;
			}
			if(c instanceof ReferencedPolicySetContext r && r.reference() == this) return;
		}
	}

	private EvaluationContext cyclicContext(EvaluationContext context){
		PolicySetIDReferenceEvaluationContext cycle = new PolicySetIDReferenceEvaluationContext(context, true);
		cycle.setEvaluationStatus(Status.processingError().message("Cyclic reference detected").build());
		return cycle;
	}

	boolean isCyclic(EvaluationContext context){
		return context instanceof PolicySetIDReferenceEvaluationContext reference && reference.cyclic;
	}

	/**
	 * Creates the context of the referenced policy set below a context of this reference. In an
	 * unobserved decision one context is both (cycle AP).
	 */
	private EvaluationContext enter(EvaluationContext context, CompositeDecisionRule policySet){
		// For an external context, PolicySet.createContext checks its current policy set below.
		if(isPolicySetCyclic(policySet, context, false)) return cyclicContext(context);
		if(policySet.getClass() == PolicySet.class && DescendantEvaluationContext.isUnobserved(context)){
			return new ReferencedPolicySetContext((PolicySet)policySet, context);
		}
		EvaluationContext reference = new PolicySetIDReferenceEvaluationContext(context);
		EvaluationContext entered = policySet.createContext(reference);
		return policySet.getClass() == PolicySet.class && entered == reference ? cyclicContext(context) : entered;
	}

	boolean canCheckTarget(EvaluationContext context, CompositeDecisionRule target){
		if(context.getCurrentPolicySetIDReference() == this || context.getCurrentPolicy() != null
				|| isPolicySetCyclic(target, context, true)) return false;
		return !isReferenceCyclic(this, context);
	}

	private static boolean isPolicySetCyclic(CompositeDecisionRule target, EvaluationContext context,
			boolean includeCurrent){
		EvaluationContext top = DescendantEvaluationContext.topOf(context);
		// The parent fields: getParentContext() skips the delegate of a staging wrapper
		for(EvaluationContext c = context; c != top; c = ((DescendantEvaluationContext)c).getParent()){
			if(c instanceof PolicySet.PolicySetDescendantEvaluationContext p && p.policySet() == target) return true;
		}
		EvaluationContext first = includeCurrent || context != top ? top : top.getParentContext();
		for(EvaluationContext c = first; c != null; c = c.getParentContext()){
			if(c.getCurrentPolicySet() == target) return true;
		}
		return false;
	}

	@Override
	public Decision evaluate(EvaluationContext context) {
		Preconditions.checkNotNull(context);
		Preconditions.checkArgument(context.getCurrentPolicySetIDReference() == this);
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
		if(isCyclic(context) || !isReferenceTo(context.getCurrentPolicySet())){
			return Decision.INDETERMINATE;
		}
		CompositeDecisionRule ps = context.getCurrentPolicySet();
		Preconditions.checkNotNull(ps);
		setEvaluating(context, true);
		try{
			return ps.evaluate(context);
		}finally{
			setEvaluating(context, false);
		}
	}

	@Override
	public MatchResult isMatch(EvaluationContext context) {
		Preconditions.checkNotNull(context);
		Preconditions.checkArgument(context.getCurrentPolicySetIDReference() == this);
		if(isCyclic(context) || !isReferenceTo(context.getCurrentPolicySet())){
			return MatchResult.INDETERMINATE;
		}
		CompositeDecisionRule ps = context.getCurrentPolicySet();
		Preconditions.checkNotNull(ps);
		return ps.isMatch(context);
	}

	public void accept(PolicyTreeVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}

	@Override
	public CompositeDecisionRuleIDReference getReference() {
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		return (o instanceof PolicySetIDReference)
				&& ((PolicySetIDReference)o).equalsTo(this);
	}

	/**
	 * A static helper method to detect cyclic references
	 *
	 * @param ref a policy set id reference
	 * @param context an evaluation context
	 * @return {@code true} if a given reference
	 */
	private static boolean isReferenceCyclic(PolicySetIDReference ref,
			EvaluationContext context)
	{
		if(context.getCurrentPolicySetIDReference() != null){
			if(ref.equals(context.getCurrentPolicySetIDReference())){
				return true;
			}
			return isReferenceCyclic(ref, context.getParentContext());
		}
		return false;
	}

	/**
	 * The context of this reference
	 */
	class PolicySetIDReferenceEvaluationContext extends DescendantEvaluationContext
	{
		private final boolean cyclic;
		private boolean evaluating;

		PolicySetIDReferenceEvaluationContext(
				EvaluationContext context) {
			this(context, false);
		}

		PolicySetIDReferenceEvaluationContext(EvaluationContext context, boolean cyclic) {
			super(context);
			this.cyclic = cyclic;
		}

		/**
		 * The reference of this context, which answers {@link #getCurrentPolicySetIDReference()} (design D17)
		 */
		final PolicySetIDReference reference() {
			return PolicySetIDReference.this;
		}

		@Override
		public EvaluationContext getParentContext() {
			return getParent();
		}
	}

	/**
	 * The context of the referenced policy set that is also the context of this reference, made
	 * after the check that it is not cyclic (cycle AP)
	 */
	final class ReferencedPolicySetContext extends PolicySet.PolicySetDescendantEvaluationContext
	{
		ReferencedPolicySetContext(PolicySet policySet, EvaluationContext context){
			policySet.super(context);
		}

		/**
		 * The reference of this context, which answers {@link #getCurrentPolicySetIDReference()}
		 */
		PolicySetIDReference reference() {
			return PolicySetIDReference.this;
		}
	}

	public static class Builder extends BaseCompositeDecisionRuleIDReference.Builder<Builder>
	{
		@Override
		protected Builder getThis() {
			return this;
		}

		public PolicySetIDReference build(){
			return new PolicySetIDReference(this);
		}
	}
}
