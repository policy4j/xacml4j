package com.artagon.xacml.v3.policy;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.Decision;

public final class DefaultPolicyIDSetReference extends BaseCompositeDecisionRuleIDReference 
	implements PolicySetIDReference
{

	public DefaultPolicyIDSetReference(String id, VersionMatch version,
			VersionMatch earliest, VersionMatch latest) {
		super(id, version, earliest, latest);
	}

	@Override
	public EvaluationContext createContext(EvaluationContext context)
			throws EvaluationException {
		if(context.getCurrentPolicySetIDReference() ==  this){
			return context;
		}
		PolicySet policySet = context.getCurrentPolicySet();
		return new PolicySetIDReferenceEvaluationContext(context, this, policySet);
	}

	@Override
	public Decision evaluate(EvaluationContext context) {
		Preconditions.checkArgument(context.getCurrentPolicySetIDReference() == this);
		Preconditions.checkArgument(context.getCurrentPolicySet() != null);
		Preconditions.checkArgument(context.getCurrentPolicySet().getId().equals(getId()));
		return context.getCurrentPolicySet().evaluate(context);
	}

	@Override
	public Decision evaluateIfApplicable(EvaluationContext context) {
		Preconditions.checkArgument(context.getCurrentPolicySetIDReference() == this);
		Preconditions.checkArgument(context.getCurrentPolicySet() != null);
		Preconditions.checkArgument(context.getCurrentPolicySet().getId().equals(getId()));
		return context.getCurrentPolicySet().evaluateIfApplicable(context);
	}

	@Override
	public MatchResult isApplicable(EvaluationContext context) {
		Preconditions.checkArgument(context.getCurrentPolicySetIDReference() == this);
		Preconditions.checkArgument(context.getCurrentPolicySet() != null);
		Preconditions.checkArgument(context.getCurrentPolicySet().getId().equals(getId()));
		return context.getCurrentPolicySet().isApplicable(context);
	}

	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}
	
}
