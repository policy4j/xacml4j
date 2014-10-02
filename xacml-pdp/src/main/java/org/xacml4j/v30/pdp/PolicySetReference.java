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

import org.xacml4j.v30.*;

import com.google.common.base.Preconditions;

/**
 * A XACML {@link PolicySet} reference
 *
 * @author Giedrius Trumpickas
 */
public final class PolicySetReference extends BaseCompositeDecisionRuleIDReference
	implements PolicyElement
{

	private PolicySetReference(Builder b) {
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
		PolicySet p = (PolicySet) policy;
		return p != null &&
				matches(p.getId(), p.getVersion());
	}

	/**
	 * Creates an {@link DecisionRuleEvaluationContext} to evaluate this reference.
	 */
	@Override
	public DecisionRuleEvaluationContext createContext(EvaluationContext ctx)
	{
		Preconditions.checkNotNull(ctx);
        DecisionRuleEvaluationContext context = (DecisionRuleEvaluationContext)ctx;
		if(context.getCurrentPolicySetIDReference() == this){
			return context;
		}
		PolicySetIDReferenceEvaluationContext refContext = new PolicySetIDReferenceEvaluationContext(context);
		try{
			CompositeDecisionRule policySet = refContext.resolve(this);
			if(policySet == null){
				return refContext;
			}
			return (DecisionRuleEvaluationContext)policySet.createContext(refContext);
		}catch(PolicyResolutionException e){
			return refContext;
		}
	}

	@Override
	public Decision evaluate(EvaluationContext ctx) {
		Preconditions.checkNotNull(ctx);
        DecisionRuleEvaluationContext context = (DecisionRuleEvaluationContext)ctx;
		Preconditions.checkArgument(context.getCurrentPolicySetIDReference() == this);
		if(!isReferenceTo(context.getCurrentPolicySet())){
			return Decision.INDETERMINATE;
		}
		CompositeDecisionRule ps = context.getCurrentPolicySet();
		Preconditions.checkNotNull(ps);
		return ps.evaluate(context);
	}

	@Override
	public MatchResult isMatch(EvaluationContext ctx) {
		Preconditions.checkNotNull(ctx);
        DecisionRuleEvaluationContext context = (DecisionRuleEvaluationContext)ctx;
		Preconditions.checkArgument(context.getCurrentPolicySetIDReference() == this);
		if(!isReferenceTo(context.getCurrentPolicySet())){
			return MatchResult.INDETERMINATE;
		}
		CompositeDecisionRule ps = context.getCurrentPolicySet();
		Preconditions.checkNotNull(ps);
		return ps.isMatch(context);
	}

	@Override
	public void accept(PolicyVisitor pv) {
        if(!(pv instanceof Visitor)){
            return;
        }
        Visitor v = (Visitor)pv;
		v.visitEnter(this);
		v.visitLeave(this);
	}

    public interface Visitor extends PolicyVisitor{
        void visitEnter(PolicySetReference r);
        void visitLeave(PolicySetReference r);
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
		return (o instanceof PolicySetReference)
				&& ((PolicySetReference)o).equalsTo(this);
	}

	/**
	 * A static helper method to detect cyclic references
	 *
	 * @param ref a policy set id reference
	 * @param context an evaluation context
	 * @return {@code true} if a given reference
	 */
	private static boolean isReferenceCyclic(PolicySetReference ref,
	                                         DecisionRuleEvaluationContext context)
	{
		if(context.getCurrentPolicySetIDReference() != null){
			if(ref.equals(context.getCurrentPolicySetIDReference())){
				throw new IllegalStateException("Cyclic reference detected");
			}
			return isReferenceCyclic(ref, context.getParentContext());
		}
		return false;
	}

	class PolicySetIDReferenceEvaluationContext extends DelegatingEvaluationContext
	{
		PolicySetIDReferenceEvaluationContext(
				DecisionRuleEvaluationContext context) {
			super(context);
			Preconditions.checkArgument(!isReferenceCyclic(PolicySetReference.this, context));
		}

		@Override
		public PolicySetReference getCurrentPolicySetIDReference() {
			return PolicySetReference.this;
		}

		@Override
		public DecisionRuleEvaluationContext getParentContext() {
			return getDelegate();
		}
	}

	public static class Builder extends BaseCompositeDecisionRuleIDReference.Builder<Builder>
	{
		@Override
		protected Builder getThis() {
			return this;
		}

		public PolicySetReference build(){
			return new PolicySetReference(this);
		}
	}
}
