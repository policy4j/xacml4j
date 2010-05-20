package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.MatchResult;
import com.artagon.xacml.v3.PolicyResolutionException;
import com.artagon.xacml.v3.PolicySet;
import com.artagon.xacml.v3.PolicySetIDReference;
import com.artagon.xacml.v3.PolicyVisitor;
import com.artagon.xacml.v3.VersionMatch;
import com.google.common.base.Preconditions;

public final class DefaultPolicySetIDReference extends BaseCompositeDecisionRuleIDReference 
	implements PolicySetIDReference
{

	public DefaultPolicySetIDReference(String id, VersionMatch version,
			VersionMatch earliest, VersionMatch latest) {
		super(id, version, earliest, latest);
	}
	
	public DefaultPolicySetIDReference(String id, VersionMatch version) {
		super(id, version, null, null);
	}

	public boolean isReferenceTo(PolicySet policySet) {
		return policySet != null && matches(policySet.getId(), policySet.getVersion());
	}

	@Override
	public EvaluationContext createContext(EvaluationContext context)
	{
		if(context.getCurrentPolicySetIDReference() ==  this){
			return context;
		}
		PolicySetIDReferenceEvaluationContext refContext = new PolicySetIDReferenceEvaluationContext(context, this);
		try
		{
			PolicySet policySet = refContext.resolve(this);
			return policySet.createContext(refContext);
		}catch(PolicyResolutionException e){
			return refContext;
		}
	}
	
	@Override
	public Decision evaluate(EvaluationContext context) {
		Preconditions.checkArgument(context.getCurrentPolicySetIDReference() == this);
		if(!isReferenceTo(context.getCurrentPolicySet())){
			return Decision.INDETERMINATE;
		}
		PolicySet ps = context.getCurrentPolicySet();
		Preconditions.checkState(ps != null);
		return ps.evaluate(context);
	}

	@Override
	public Decision evaluateIfApplicable(EvaluationContext context) {
		Preconditions.checkArgument(context.getCurrentPolicySetIDReference() == this);
		if(!isReferenceTo(context.getCurrentPolicySet())){
			return Decision.INDETERMINATE;
		}
		PolicySet ps = context.getCurrentPolicySet();
		Preconditions.checkState(ps != null);
		return ps.evaluateIfApplicable(context);
	}

	@Override
	public MatchResult isApplicable(EvaluationContext context) {
		Preconditions.checkArgument(context.getCurrentPolicySetIDReference() == this);
		if(!isReferenceTo(context.getCurrentPolicySet())){
			return MatchResult.INDETERMINATE;
		}
		PolicySet ps = context.getCurrentPolicySet();
		Preconditions.checkState(ps != null);
		return ps.isApplicable(context);
	}
	
	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}
}
