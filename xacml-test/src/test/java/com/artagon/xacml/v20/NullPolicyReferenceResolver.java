package com.artagon.xacml.v20;

import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicyIDReference;
import com.artagon.xacml.v3.PolicyReferenceResolver;
import com.artagon.xacml.v3.PolicyResolutionException;
import com.artagon.xacml.v3.PolicySet;
import com.artagon.xacml.v3.PolicySetIDReference;

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
