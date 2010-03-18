package com.artagon.xacml.v3.policy;

import java.util.Collection;
import java.util.List;

public interface Policy extends CompositeDecisionRule, Versionable
{
	/**
	 * Gets policy version
	 * 
	 * @return {@link Version} instance
	 */
	Version getVersion();
	
	PolicyDefaults getDefaults();
	
	/**
	 * Gets rule target
	 * 
	 * @return {@link Target} or
	 * <code>null</code> if rule 
	 * matches any request
	 */
	Target getTarget();
	
	/**
	 * Gets all policy rules
	 * 
	 * @return a collection of {@link Rule}
	 * instances
	 */
	List<Rule> getRules();
	
	/**
	 * Gets policy variable definitions
	 * 
	 * @return a collection of {@link VariableDefinition} instances
	 */
	Collection<VariableDefinition> getVariableDefinitions();
	
	/**
	 * Gets {@link VariableDefinition} by identifier
	 * 
	 * @param variableId variable identifier
	 * @return {@link VariableDefinition} or <code>null</code>
	 * if variable definition can not be resolved
	 */
	VariableDefinition getVariableDefinition(String variableId);	
	
	/**
	 * Gets policy obligations
	 * 
	 * @return collection of {@link ObligationExpression}
	 * instances for this policy
	 */
	Collection<ObligationExpression> getObligationExpressions();
	
	/**
	 * Gets decision advice expressions
	 * 
	 * @return collection of {@link AdviceExpression}
	 * instances for this policy
	 */
	Collection<AdviceExpression> getAdviceExpressions();
}