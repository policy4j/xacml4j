package com.artagon.xacml.v3.policy;


/**
 * An interface for a decisions which contain
 * other decision rules. Example of an implementation
 * of such interface is {@link Policy} or {@link PolicySet}
 * 
 * @author Giedrius Trumpickas
 */
public interface CompositeDecisionRule  extends DecisionRule
{
	/**
	 * Gets a {@link CompositeDecisionRuleIDReference} 
	 * to this composite decision rule
	 * 
	 * @return {@link CompositeDecisionRuleIDReference} instance
	 * pointing to this composite decision rule
	 */
	CompositeDecisionRuleIDReference getReference();
}
