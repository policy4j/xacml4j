package com.artagon.xacml.v3.policy;

import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.context.Decision;
import com.google.common.base.Preconditions;

public class PolicyIDReference extends 
	BaseCompositeDecisionRuleIDReference implements PolicyElement
{
	public PolicyIDReference(
			String id, 
			VersionMatch version,
			VersionMatch earliest, 
			VersionMatch latest) {
		super(id, version, earliest, latest);
	}
	
	public PolicyIDReference(String id, VersionMatch version) {
		super(id, version, null, null);
	}

	public boolean isReferenceTo(Policy policy) {
		return policy != null && matches(policy.getId(), policy.getVersion());
	}

	@Override
	public EvaluationContext createContext(EvaluationContext context)
	{
		if(context.getCurrentPolicyIDReference() ==  this){
			return context;
		}
		PolicyIDReferenceEvaluationContext refContext = new PolicyIDReferenceEvaluationContext(context);
		try{
			Policy resolvedPolicy = context.resolve(this);
			return resolvedPolicy.createContext(refContext);
		}catch(PolicyResolutionException e){
			return refContext;
		}
	}

	@Override
	public Decision evaluate(EvaluationContext context) {
		Preconditions.checkArgument(context.getCurrentPolicyIDReference() == this);
		if(!isReferenceTo(context.getCurrentPolicy())){
			return Decision.INDETERMINATE;
		}
		Policy p = context.getCurrentPolicy();
		Preconditions.checkState(p != null);
		return p.evaluate(context);
	}

	@Override
	public Decision evaluateIfApplicable(EvaluationContext context) {
		Preconditions.checkArgument(context.getCurrentPolicyIDReference() == this);
		if(!isReferenceTo(context.getCurrentPolicy())){
			return Decision.INDETERMINATE;
		}
		Policy p = context.getCurrentPolicy();
		Preconditions.checkState(p != null);
		return p.evaluateIfApplicable(context);
	}

	@Override
	public MatchResult isApplicable(EvaluationContext context) {
		Preconditions.checkArgument(context.getCurrentPolicyIDReference() == this);
		if(!isReferenceTo(context.getCurrentPolicy())){
			return MatchResult.INDETERMINATE;
		}
		Policy p = context.getCurrentPolicy();
		Preconditions.checkState(p != null);
		return p.isApplicable(context);
	}

	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
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
		 * @param policyIDRef a policy reference
		 * @exception IllegalArgumentException if enclosing context
		 * {@link EvaluationContext#getCurrentPolicySet()} returns
		 * <code>null</code> or given policy ID reference is
		 * <code>null</code>
		 */
		PolicyIDReferenceEvaluationContext(EvaluationContext context) 
		{
			super(context);
			Preconditions.checkNotNull(context.getCurrentPolicySet());
			Preconditions.checkArgument(context.getCurrentPolicy() == null);
		}
		
		@Override
		public PolicyIDReference getCurrentPolicyIDReference() {
			return PolicyIDReference.this;
		}
	}

}
