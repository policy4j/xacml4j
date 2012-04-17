package com.artagon.xacml.v30.pdp;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * In some applications it is helpful to specify supplemental 
 * information about a decision. XACML provides facilities 
 * to specify supplemental information about a decision with 
 * the {@link Advice}. Such advice may be safely ignored by the PEP.
 * 
 * @author Giedrius Trumpickas
 */
public class Advice extends BaseDecisionRuleResponse
{
	private Advice(Builder b){
		super(b);
	}
	
	public static Builder builder(String id, Effect appliesTo){
		return new Builder(id, appliesTo);
	}
	
	@Override
	public boolean equals(Object o){
		if(o == null){
			return false;
		}
		if(o == this){
			return true;
		}
		if(!(o instanceof Advice)){
			return false;
		}
		Advice a = (Advice)o;
		return id.equals(a.id) &&
				attributes.equals(a.attributes);
	}
	
	public Advice merge(Advice a)
	{
		Preconditions.checkArgument(Objects.equal(getFullfillOn(), a.getFullfillOn()));
		Preconditions.checkArgument(a.getId().equals(getId()));
		return new Advice.Builder(getId(), getFullfillOn())
		.attributes(getAttributes()).attributes(a.getAttributes()).create();
		
	}
	
	public static class Builder extends BaseBuilder<Builder>
	{	
		private Builder(String id, Effect effect){
			super(id, effect);
		}
		
		public Advice create(){
			return new Advice(this);
		}

		@Override
		protected Builder getThis() {
			return this;
		}
	}
}
