package com.artagon.xacml.v3.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.Decision;

public final class DefaultPolicyIDReference extends 
	BaseCompositeDecisionRuleIDReference implements PolicyIDReference
{
	private final static Logger log = LoggerFactory.getLogger(DefaultPolicyIDReference.class);
	
	public DefaultPolicyIDReference(String id, VersionMatch version,
			VersionMatch earliest, VersionMatch latest) {
		super(id, version, earliest, latest);
	}
	
	public DefaultPolicyIDReference(String id, VersionMatch version) {
		super(id, version, null, null);
	}

	@Override
	public EvaluationContext createContext(EvaluationContext context)
	{
		if(context.getCurrentPolicyIDReference() ==  this){
			return context;
		}
		Policy resolvedPolicy = null;
		try{
			resolvedPolicy = context.resolve(this);
		}catch(PolicyResolutionException e){
			log.debug("Failed to resolve Policy ID reference=\"{}\"", this);
		}
		return new PolicyIDReferenceEvaluationContext(context, this, resolvedPolicy);
	}

	@Override
	public Decision evaluate(EvaluationContext context) {
		Preconditions.checkArgument(context.getCurrentPolicyIDReference() == this);
		if(context.getCurrentPolicy() == null){
			return Decision.INDETERMINATE;
		}
		Preconditions.checkArgument(context.getCurrentPolicy().getId().equals(getId()));
		return context.getCurrentPolicy().evaluate(context);
	}

	@Override
	public Decision evaluateIfApplicable(EvaluationContext context) {
		Preconditions.checkArgument(context.getCurrentPolicyIDReference() == this);
		if(context.getCurrentPolicy() == null){
			return Decision.INDETERMINATE;
		}
		Preconditions.checkArgument(context.getCurrentPolicy().getId().equals(getId()));
		return context.getCurrentPolicy().evaluateIfApplicable(context);
	}

	@Override
	public MatchResult isApplicable(EvaluationContext context) {
		Preconditions.checkArgument(context.getCurrentPolicyIDReference() == this);
		if(context.getCurrentPolicy() == null){
			return MatchResult.INDETERMINATE;
		}
		Preconditions.checkArgument(context.getCurrentPolicy().getId().equals(getId()));
		return context.getCurrentPolicySet().isApplicable(context);
	}

	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}	
}
