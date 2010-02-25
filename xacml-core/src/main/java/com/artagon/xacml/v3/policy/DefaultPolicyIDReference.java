package com.artagon.xacml.v3.policy;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.Decision;

public final class DefaultPolicyIDReference extends 
	BaseCompositeDecisionRuleIDReference implements PolicyIDReference
{

	public DefaultPolicyIDReference(String id, VersionMatch version,
			VersionMatch earliest, VersionMatch latest) {
		super(id, version, earliest, latest);
	}
	
	public DefaultPolicyIDReference(String id, VersionMatch version) {
		super(id, version, null, null);
	}

	@Override
	public EvaluationContext createContext(EvaluationContext context)
			throws EvaluationException {
		if(context.getCurrentPolicyIDReference() ==  this){
			return context;
		}
		return new PolicyIDReferenceEvaluationContext(context, this);
	}

	@Override
	public Decision evaluate(EvaluationContext context) {
		Preconditions.checkArgument(context.getCurrentPolicyIDReference() == this);
		Preconditions.checkArgument(context.getCurrentPolicy() != null);
		Preconditions.checkArgument(context.getCurrentPolicy().getId().equals(getId()));
		return context.getCurrentPolicy().evaluate(context);
	}

	@Override
	public Decision evaluateIfApplicable(EvaluationContext context) {
		Preconditions.checkArgument(context.getCurrentPolicyIDReference() == this);
		Preconditions.checkArgument(context.getCurrentPolicy() != null);
		Preconditions.checkArgument(context.getCurrentPolicy().getId().equals(getId()));
		return context.getCurrentPolicy().evaluateIfApplicable(context);
	}

	@Override
	public MatchResult isApplicable(EvaluationContext context) {
		Preconditions.checkArgument(context.getCurrentPolicyIDReference() == this);
		Preconditions.checkArgument(context.getCurrentPolicy() != null);
		Preconditions.checkArgument(context.getCurrentPolicy().getId().equals(getId()));
		return context.getCurrentPolicySet().isApplicable(context);
	}
}
