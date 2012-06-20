package com.artagon.xacml.v30;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;


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
public class Obligation 
	extends BaseDecisionRuleResponse
{
	Obligation(Builder b) 
	{
		super(b);
	}
	
	public static Builder builder(String id, Effect appliesTo){
		return new Builder(id, appliesTo);
	}
	
	public static Builder builder(String id){
		return new Builder(id, null);
	}
	
	
	/**
	 * Combines this obligation attributes with a 
	 * given obligation attributes
	 * 
	 * @param a an obligation
	 * @return a new obligation instance with combined attributes
	 */
	@SuppressWarnings("deprecation")
	public Obligation merge(Obligation o)
	{
		Preconditions.checkArgument(getId().equals(o.getId()));
		Preconditions.checkArgument(Objects.equal(getFullfillOn(), o.getFullfillOn()));
		return new Obligation.Builder(getId(), getFullfillOn())
		.attributes(getAttributes())
		.attributes(o.getAttributes())
		.build();
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
	
	public static class Builder extends BaseBuilder<Builder>
	{	
		private Builder(String id, Effect effect){
			super(id, effect);
		}
		
		@Override
		protected Builder getThis() {
			return this;
		}

		public Obligation build(){
			return new Obligation(this);
		}
	}
}
