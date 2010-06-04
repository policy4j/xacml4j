package com.artagon.xacml.v3.policy;

import com.artagon.xacml.v3.EvaluationContext;



public interface PolicyReferenceResolver 
{
	Policy resolve(EvaluationContext context, 
			PolicyIDReference ref) 
		throws PolicyResolutionException;
	
	PolicySet resolve(EvaluationContext context, 
			PolicySetIDReference ref) 
		throws PolicyResolutionException;
}
