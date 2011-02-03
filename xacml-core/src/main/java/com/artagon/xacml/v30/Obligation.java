package com.artagon.xacml.v30;

import java.util.Collection;


/**
 * In many applications, policies specify actions that MUST be performed, 
 * either instead of, or in addition to, actions that MAY be performed. 
 * XACML provides facilities to specify actions that MUST be performed in 
 * conjunction with policy evaluation via {@link Obligation}. 
 * There are no standard definitions for these actions in version 3.0 of XACML. 
 * Therefore, bilateral agreement between a PAP and the PEP that will 
 * enforce its policies is required for correct interpretation. 
 * PEPs that conform to v3.0 of XACML are required to deny access unless 
 * they understand and can discharge all of the {@link Obligation}  associated 
 * with the applicable policy. 
 * 
 * @author Giedrius Trumpickas

 */
public class Obligation extends BaseDecisionRuleResponse
{
	public Obligation(String id, 
			Effect effect,
			Collection<AttributeAssignment> attributes) 
	{
		super(id, effect, attributes);
	}
	
	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof Obligation)){
			return false;
		}
		Obligation a = (Obligation)o;
		return id.equals(a.id) && 
		attributes.equals(a.attributes);
	}
	
}
