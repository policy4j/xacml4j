package com.artagon.xacml.v3.spi;

import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicyIDReference;
import com.artagon.xacml.v3.PolicyResolutionException;
import com.artagon.xacml.v3.PolicySet;
import com.artagon.xacml.v3.PolicySetIDReference;

public interface PolicyReferenceResolver 
{
	Policy resolve(EvaluationContext context, 
			PolicyIDReference ref) 
		throws PolicyResolutionException;
	
	PolicySet resolve(EvaluationContext context, 
			PolicySetIDReference ref) 
		throws PolicyResolutionException;
}
