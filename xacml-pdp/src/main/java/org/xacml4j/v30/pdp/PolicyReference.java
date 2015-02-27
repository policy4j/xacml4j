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

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.*;

/**
 * A references to the {@link Policy}
 *
 * @author Giedrius Trumpickas
 */
public final class PolicyReference extends
	BaseCompositeDecisionRuleIDReference implements PolicyElement
{
	private final static Logger log = LoggerFactory.getLogger(PolicyReference.class);

	private PolicyReference(Builder b) {
		super(b);
	}

	public static Builder builder(){
		return new Builder();
	}

	public static Builder builder(String id){
		return new Builder().id(id);
	}

	/**
	 * Test this references points to a given policy
	 *
	 * @param policy a policy
	 * @return {@code true} if a this references
	 * points to a given policy
	 */
	@Override
	public boolean isReferenceTo(CompositeDecisionRule policy) {
		Policy p = (Policy)policy;
		return p != null &&
		matches(p.getId(), p.getVersion());
	}

	@Override
	public DecisionRuleEvaluationContext createContext(EvaluationContext ctx)
	{
        DecisionRuleEvaluationContext context = (DecisionRuleEvaluationContext)ctx;
		if(context.getCurrentPolicyIDReference() == this){
			return context;
		}
		PolicyIDReferenceEvaluationContext refContext = new PolicyIDReferenceEvaluationContext(context);
		try{
			CompositeDecisionRule resolvedPolicy = context.resolve(this);
			if(resolvedPolicy == null){
				if(log.isDebugEnabled()){
					log.debug("Failed to resolve policy references=\"{}\"", this);
				}
				return refContext;
			}
			if(log.isDebugEnabled()){
				log.debug("Found matching policy " +
						"to the policy references\"{}\"", this);
			}
			return (DecisionRuleEvaluationContext)resolvedPolicy.createContext(refContext);
		}catch(PolicyResolutionException e){
			if(log.isDebugEnabled()){
				log.debug("Failed to resolve policy references=\"{}\"", this, e);
			}
			return refContext;
		}
	}

	@Override
	public Decision evaluate(EvaluationContext ctx) {
        DecisionRuleEvaluationContext context = (DecisionRuleEvaluationContext)ctx;
		Preconditions.checkArgument(context.getCurrentPolicyIDReference() == this);
		if(!isReferenceTo(context.getCurrentPolicy())){
			return Decision.INDETERMINATE;
		}
		CompositeDecisionRule p = context.getCurrentPolicy();
		Preconditions.checkNotNull(p);
		return p.evaluate(context);
	}

	@Override
	public MatchResult isMatch(EvaluationContext ctx) {
        DecisionRuleEvaluationContext context = (DecisionRuleEvaluationContext)ctx;
		Preconditions.checkArgument(context.getCurrentPolicyIDReference() == this);
		if(!isReferenceTo(context.getCurrentPolicy())){
			return MatchResult.INDETERMINATE;
		}
		CompositeDecisionRule p = context.getCurrentPolicy();
		Preconditions.checkNotNull(p);
		return p.isMatch(context);
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
        void visitEnter(PolicyReference r);
        void visitLeave(PolicyReference r);
    }

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		return (o instanceof PolicyReference)
				&& ((PolicyReference)o).equalsTo(this);
	}

	/**
	 * Validates if a given references is cyclic
	 * in a given evaluation context
	 *
	 * @param ref a references
	 * @param context an evaluation context
	 * @return {@code true} if given references is cyclic; returns {@code false} otherwise
	 */
	private static boolean isReferenceCyclic(PolicyReference ref,
			DecisionRuleEvaluationContext context)
	{
		CompositeDecisionRuleIDReference otherRef = context.getCurrentPolicyIDReference();
		if(otherRef != null){
			if(ref.equals(otherRef)){
				if(log.isDebugEnabled()){
					log.debug("Policy references=\"{}\" cycle detected", ref);
				}
				return true;
			}
			return isReferenceCyclic(ref, context.getParentContext());
		}
		return false;
	}

	/**
	 * An {@link EvaluationContext} implementation
	 * to evaluate {@link PolicySetReference} decisions
	 *
	 * @author Giedrius Trumpickas
	 */
	class PolicyIDReferenceEvaluationContext extends
			DelegatingEvaluationContext
	{
		/**
		 * Creates policy evaluation context with a given parent context
		 *
		 * @param context a parent evaluation context
		 * @exception IllegalArgumentException if enclosing context
		 * {@link org.xacml4j.v30.pdp.DecisionRuleEvaluationContext#getCurrentPolicySet()} returns
		 * {@code null} or given policy ID references is {@code null}
		 */
		PolicyIDReferenceEvaluationContext(DecisionRuleEvaluationContext context){
			super(context);
			Preconditions.checkArgument(context.getCurrentPolicy() == null);
			Preconditions.checkArgument(!isReferenceCyclic(PolicyReference.this, context));
		}

		@Override
		public DecisionRuleEvaluationContext getParentContext() {
			return getDelegate();
		}

		@Override
		public PolicyReference getCurrentPolicyIDReference() {
			return PolicyReference.this;
		}
	}

	public static class Builder extends BaseCompositeDecisionRuleIDReference.Builder<Builder>
	{
		@Override
		protected Builder getThis() {
			return this;
		}

		public PolicyReference build(){
			return new PolicyReference(this);
		}
	}

}
