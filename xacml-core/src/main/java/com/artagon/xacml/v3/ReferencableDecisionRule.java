package com.artagon.xacml.v3;

public interface ReferencableDecisionRule extends DecisionRule
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
