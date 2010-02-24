package com.artagon.xacml.v3.policy;

import java.util.Collection;


public interface PolicySet extends CompositeDecisionRule, Versionable
{
	/**
	 * Gets rule target
	 * 
	 * @return {@link Target} or
	 * <code>null</code> if rule 
	 * matches any request
	 */
	Target getTarget();
	
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
