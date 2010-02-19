package com.artagon.xacml.v3;

import java.util.Collection;

import com.artagon.xacml.v3.policy.AttributeAssignment;

/**
 * In some applications it is helpful to specify supplemental 
 * information about a decision. XACML provides facilities 
 * to specify supplemental information about a decision with 
 * the {@link DefaultAdvice}. Such advice may be safely ignored by the PEP.
 * 
 * @author Giedrius Trumpickas
 */
public final class DefaultAdvice extends BaseDecisionRuleResponse implements Advice
{
	public DefaultAdvice(String adviceId, Collection<AttributeAssignment> attributes) {
		super(adviceId, attributes);
	}
}
