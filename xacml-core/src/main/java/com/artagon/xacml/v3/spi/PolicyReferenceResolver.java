package com.artagon.xacml.v3.spi;

import com.artagon.xacml.v3.policy.Policy;
import com.artagon.xacml.v3.policy.PolicyIDReference;
import com.artagon.xacml.v3.policy.PolicyResolutionException;
import com.artagon.xacml.v3.policy.PolicySet;
import com.artagon.xacml.v3.policy.PolicySetIDReference;

public interface PolicyReferenceResolver 
{
	/**
	 * Resolves a given {@link PolicyIDReference}
	 * 
	 * @param ref a policy reference
	 * @return {@link Policy} instance
	 * @throws PolicyResolutionException
	 */
	Policy resolve(PolicyIDReference ref) 
		throws PolicyResolutionException;
	
	/**
	 * Resolves a given {@link PolicySetIDReference}
	 * 
	 * 
	 * @param ref a policy reference
	 * @return {@link PolicySet} instance
	 * @throws PolicyResolutionException
	 */
	PolicySet resolve(PolicySetIDReference ref) 
		throws PolicyResolutionException;
}
