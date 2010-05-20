package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.MatchResult;
import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicyIDReference;
import com.artagon.xacml.v3.PolicyResolutionException;
import com.artagon.xacml.v3.PolicyVisitor;
import com.artagon.xacml.v3.VersionMatch;
import com.google.common.base.Preconditions;

public final class DefaultPolicyIDReference extends 
	BaseCompositeDecisionRuleIDReference implements PolicyIDReference
{
	public DefaultPolicyIDReference(
			String id, 
			VersionMatch version,
			VersionMatch earliest, 
			VersionMatch latest) {
		super(id, version, earliest, latest);
	}
	
	public DefaultPolicyIDReference(String id, VersionMatch version) {
		super(id, version, null, null);
	}

	@Override
	public boolean isReferenceTo(Policy policy) {
		return policy != null && matches(policy.getId(), policy.getVersion());
	}

	@Override
	public EvaluationContext createContext(EvaluationContext context)
	{
		if(context.getCurrentPolicyIDReference() ==  this){
			return context;
		}
		PolicyIDReferenceEvaluationContext refContext = new PolicyIDReferenceEvaluationContext(context, this);
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
}
