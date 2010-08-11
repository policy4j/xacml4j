package com.artagon.xacml.v3;

/**
 * A XACML policy or policy set ID reference resolver
 * 
 * @author Giedrius Trumpickas
 */
public interface PolicyReferenceResolver 
{
	/**
	 * Resolves a given {@link PolicyIDReference}
	 * to the {@link Policy} instance
	 * 
	 * @param context an evaluation context
	 * @param ref a policy reference
	 * @return {@link Policy} 
	 * @throws PolicyResolutionException if a
	 * reference resolution fails
	 */
	Policy resolve(EvaluationContext context, 
			PolicyIDReference ref) throws PolicyResolutionException;
	
	/**
	 * Resolves a given {@link PolicySetIDReference}
	 * to the {@link PolicySet} instance
	 * 
	 * @param context an evaluation context
	 * @param ref a policy reference
	 * @return {@link PolicySet} 
	 * @throws PolicyResolutionException if a
	 * reference resolution fails
	 */
	PolicySet resolve(EvaluationContext context, 
			PolicySetIDReference ref) throws PolicyResolutionException;
}
