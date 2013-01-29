package org.xacml4j.v30;

/**
 * An interface for a composite decision rules
 *
 * @author Giedrius Trumpickas
 */
public interface CompositeDecisionRule extends DecisionRule
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
