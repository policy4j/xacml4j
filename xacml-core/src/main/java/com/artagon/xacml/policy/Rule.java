package com.artagon.xacml.policy;

public interface Rule extends Decision
{
	/**
	 * Gets rule effect.
	 * 
	 * @return rule effect
	 */
	Effect getEffect();

	/**
	 * Gets rule condition.
	 * 
	 * @return boolean
	 */
	Condition getCondition();
}