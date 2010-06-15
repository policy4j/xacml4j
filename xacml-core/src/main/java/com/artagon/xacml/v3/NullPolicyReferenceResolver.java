package com.artagon.xacml.v3;


public class NullPolicyReferenceResolver implements PolicyReferenceResolver
{

	@Override
	public Policy resolve(EvaluationContext context, PolicyIDReference ref)
			throws PolicyResolutionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PolicySet resolve(EvaluationContext context, PolicySetIDReference ref)
			throws PolicyResolutionException {
		// TODO Auto-generated method stub
		return null;
	}
	
}
