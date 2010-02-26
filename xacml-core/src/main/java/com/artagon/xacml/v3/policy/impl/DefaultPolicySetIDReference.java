package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.MatchResult;
import com.artagon.xacml.v3.policy.PolicyResolutionException;
import com.artagon.xacml.v3.policy.PolicySet;
import com.artagon.xacml.v3.policy.PolicySetIDReference;
import com.artagon.xacml.v3.policy.PolicyVisitor;
import com.artagon.xacml.v3.policy.VersionMatch;

public final class DefaultPolicySetIDReference extends BaseCompositeDecisionRuleIDReference 
	implements PolicySetIDReference
{

	public DefaultPolicySetIDReference(String id, VersionMatch version,
			VersionMatch earliest, VersionMatch latest) {
		super(id, version, earliest, latest);
	}

	@Override
	public EvaluationContext createContext(EvaluationContext context)
	{
		if(context.getCurrentPolicySetIDReference() ==  this){
			return context;
		}
		PolicySetIDReferenceEvaluationContext refContext = new PolicySetIDReferenceEvaluationContext(context, this);
		try{
			PolicySet policySet = context.resolve(this);
			return policySet.createContext(refContext);
		}catch(PolicyResolutionException e){
			return refContext;
		}
	}
	
	@Override
	public Decision evaluate(EvaluationContext context) {
		Preconditions.checkArgument(context.getCurrentPolicySetIDReference() == this);
		if(context.getCurrentPolicySet() == null){
			return Decision.INDETERMINATE;
		}
		Preconditions.checkArgument(context.getCurrentPolicySet().getId().equals(getId()));
		return context.getCurrentPolicySet().evaluate(context);
	}

	@Override
	public Decision evaluateIfApplicable(EvaluationContext context) {
		Preconditions.checkArgument(context.getCurrentPolicySetIDReference() == this);
		if(context.getCurrentPolicySet() == null){
			return Decision.INDETERMINATE;
		}
		Preconditions.checkArgument(context.getCurrentPolicySet().getId().equals(getId()));
		return context.getCurrentPolicySet().evaluateIfApplicable(context);
	}

	@Override
	public MatchResult isApplicable(EvaluationContext context) {
		Preconditions.checkArgument(context.getCurrentPolicySetIDReference() == this);
		if(context.getCurrentPolicySet() == null){
			return MatchResult.INDETERMINATE;
		}
		Preconditions.checkArgument(context.getCurrentPolicySet().getId().equals(getId()));
		return context.getCurrentPolicySet().isApplicable(context);
	}
	
	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}
}
