package com.artagon.xacml.v3.policy;

import java.util.Collection;

public interface Rule extends DecisionRule
{
	/**
	 * Gets decision policy description
	 * 
	 * @return description
	 */
	String getDescription();
	
	/**
	 * Gets rule target
	 * 
	 * @return {@link Target} or
	 * <code>null</code> if rule 
	 * matches any request
	 */
	Target getTarget();
	
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
	
	/**
	 * Gets decision obligations
	 * 
	 * @return collection of {@link ObligationExpression}
	 * instances
	 */
	Collection<ObligationExpression> getObligationExpressions();
	
	/**
	 * Gets decision advice expressions
	 * 
	 * @return collection of {@link AdviceExpression}
	 * instances
	 */
	Collection<AdviceExpression> getAdviceExpressions();
}