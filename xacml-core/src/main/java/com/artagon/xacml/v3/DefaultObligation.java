package com.artagon.xacml.v3;

import java.util.Collection;

import com.artagon.xacml.v3.policy.AttributeAssignment;

/**
 * In many applications, policies specify actions that MUST be performed, 
 * either instead of, or in addition to, actions that MAY be performed. 
 * XACML provides facilities to specify actions that MUST be performed in 
 * conjunction with policy evaluation via {@link DefaultObligation}. 
 * There are no standard definitions for these actions in version 3.0 of XACML. 
 * Therefore, bilateral agreement between a PAP and the PEP that will 
 * enforce its policies is required for correct interpretation. 
 * PEPs that conform to v3.0 of XACML are required to deny access unless 
 * they understand and can discharge all of the {@link DefaultObligation}  associated 
 * with the applicable policy. 
 * 
 * @author Giedrius Trumpickas
 *
 */
public final class DefaultObligation extends BaseDecisionRuleResponse implements Obligation
{
	public DefaultObligation(String id, Collection<AttributeAssignment> attributes) {
		super(id, attributes);
	}	
}
