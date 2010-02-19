package com.artagon.xacml.v3.policy;

import com.artagon.xacml.v3.Decision;

public interface DecisionRuleResponseExpression extends PolicyElement
{
	/**
	 * Unique identifier
	 * 
	 * @return an identifier
	 */
	String getId();
	
	/**
	 * Gets {@link Effect} instance
	 * 
	 * @return {@link Effect} instance
	 */
	Effect getEffect();
	
	/**
	 * Tests if this decision info expression
	 * is applicable for a given {@link Decision}
	 * 
	 * @param result a decision result
	 * @return <code>true</code> if an expression is applicable
	 */
	boolean isApplicable(Decision result);
}
