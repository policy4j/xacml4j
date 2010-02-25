package com.artagon.xacml.v3.policy;


public interface PolicyResolver 
{
	Policy resolve(EvaluationContext context, 
			PolicyIDReference ref) 
		throws PolicyResolutionException;
	
	PolicySet resolve(EvaluationContext context, 
			PolicySetIDReference ref) 
		throws PolicyResolutionException;
}
