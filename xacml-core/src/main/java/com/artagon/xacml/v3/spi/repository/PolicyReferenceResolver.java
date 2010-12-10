package com.artagon.xacml.v3.spi.repository;

import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicyIDReference;
import com.artagon.xacml.v3.PolicyResolutionException;
import com.artagon.xacml.v3.PolicySet;
import com.artagon.xacml.v3.PolicySetIDReference;

/**
 * A XACML {@link PolicyIDReference} or 
 * {@link PolicySetIDReference} resolution capability
 * 
 * @author Giedrius Trumpickas
 */
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
