package com.artagon.xacml.v3.spi;

import java.util.Collection;

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.EvaluationContext;

/**
 * A collection of one or more policy or policy sets
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
	 * Gets policy domain mode
	 * 
	 * @return {@link Type} a policy domain mode
	 */
	Type getMode();
	
	/**
	 * Evaluates given context against top level 
	 * policies stored in this policy domain
	 * 
	 * @param context an evaluation context
	 * @return {@link Decision}
	 */
	Decision evaluate(EvaluationContext context);	
	
	/**
	 * Adds top level policy to this domain
	 * 
	 * @param p a top level policy
	 */
	void add(CompositeDecisionRule p);
	
	/**
	 * Removes top level policy from this domain
	 * 
	 * @param p a policy to be removed
	 */
	void remove(CompositeDecisionRule p);
	
	/**
	 * Gets domain policies
	 * 
	 * @return a collection of policies 
	 * in this domain
	 */
	Collection<CompositeDecisionRule> getDomainPolicies();
	
}
