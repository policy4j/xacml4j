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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.CompositeDecisionRuleIDReference;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.MatchResult;
import org.xacml4j.v30.PolicyResolutionException;

import com.google.common.base.Preconditions;

public final class PolicyIDReference extends
	BaseCompositeDecisionRuleIDReference implements PolicyElement
{
	private final static Logger log = LoggerFactory.getLogger(PolicyIDReference.class);

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
		if(context.getCurrentPolicyIDReference() ==  this){
			return context;
		}
		PolicyIDReferenceEvaluationContext refContext = new PolicyIDReferenceEvaluationContext(context);
		try{
			CompositeDecisionRule resolvedPolicy = context.resolve(this);
			if(resolvedPolicy == null){
				if(log.isDebugEnabled()){
					log.debug(String.format(
							"Failed to resolve policy reference=\"%s\"",
							this));
				}
				return refContext;
			}
			if(log.isDebugEnabled()){
				log.debug("Found matching policy " +
						"to the policy reference\"{}\"", this);
			}
			return resolvedPolicy.createContext(refContext);
		}catch(PolicyResolutionException e){
			if(log.isDebugEnabled()){
				log.debug(String.format(
						"Failed to resolve policy reference=\"%s\"",
						this), e);
			}
			return refContext;
		}
	}

	@Override
	public Decision evaluate(EvaluationContext context) {
		Preconditions.checkNotNull(context);
		Preconditions.checkArgument(context.getCurrentPolicyIDReference() == this);
		if(!isReferenceTo(context.getCurrentPolicy())){
			return Decision.INDETERMINATE;
		}
		CompositeDecisionRule p = context.getCurrentPolicy();
		Preconditions.checkNotNull(p);
		return p.evaluate(context);
	}

	@Override
	public MatchResult isMatch(EvaluationContext context) {
		Preconditions.checkNotNull(context);
		Preconditions.checkArgument(context.getCurrentPolicyIDReference() == this);
		if(!isReferenceTo(context.getCurrentPolicy())){
			return MatchResult.INDETERMINATE;
		}
		CompositeDecisionRule p = context.getCurrentPolicy();
		Preconditions.checkNotNull(p);
		return p.isMatch(context);
	}

	@Override
	public void accept(PolicyVisitor v) {
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
				if(log.isDebugEnabled()){
					log.debug("Policy reference=\"{}\" " +
							"cycle detected", ref);
				}
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
			DelegatingEvaluationContext
	{
		/**
		 * Creates policy evaluation context with a given parent context
		 *
		 * @param context a parent evaluation context
		 * @exception IllegalArgumentException if enclosing context
		 * {@link EvaluationContext#getCurrentPolicySet()} returns
		 * {@code null} or given policy ID reference is {@code null}
		 */
		PolicyIDReferenceEvaluationContext(EvaluationContext context){
			super(context);
			Preconditions.checkArgument(context.getCurrentPolicy() == null);
			Preconditions.checkArgument(!isReferenceCyclic(PolicyIDReference.this, context));
		}

		@Override
		public EvaluationContext getParentContext() {
			return getDelegate();
		}

		@Override
		public PolicyIDReference getCurrentPolicyIDReference() {
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
