package com.artagon.xacml.v3.spi;

import java.util.Collection;

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.EvaluationContext;

/**
 * A collection of one or more policy or policy sets
 * with an ability to resolve references
 * 
 * @author Giedrius Trumpickas
 */
public interface PolicyDomain 
{
	public enum Type
	{
		FIRST_APPLICABLE,
		ONLY_ONE_APPLICABLE,
		DENY_OVERRIDES;
	}
	
	/**
	 * Gets policy domain name
	 * 
	 * @return policy domain name
	 */
	String getName();
	
	/**
	 * Gets policy store mode
	 * 
	 * @return {@link Type} a policy store mode
	 */
	Type getMode();
	
	/**
	 * Evaluates given context against top level 
	 * policies stored in this policy store
	 * 
	 * @param context an evaluation context
	 * @return {@link Decision}
	 */
	Decision evaluate(EvaluationContext context);	
	
	/**
	 * Adds top level policy to this store
	 * 
	 * @param p a top level policy
	 */
	void add(CompositeDecisionRule p);
	
	Collection<CompositeDecisionRule> getDomainPolicies();
	
}
