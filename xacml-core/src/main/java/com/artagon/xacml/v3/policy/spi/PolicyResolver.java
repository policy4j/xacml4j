package com.artagon.xacml.v3.policy.spi;

import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.Policy;
import com.artagon.xacml.v3.policy.PolicyIDReference;
import com.artagon.xacml.v3.policy.PolicyResolutionException;
import com.artagon.xacml.v3.policy.PolicySet;
import com.artagon.xacml.v3.policy.PolicySetIDReference;


public interface PolicyResolver 
{
	Policy resolve(EvaluationContext context, 
			PolicyIDReference ref) 
		throws PolicyResolutionException;
	
	PolicySet resolve(EvaluationContext context, 
			PolicySetIDReference ref) 
		throws PolicyResolutionException;
}
