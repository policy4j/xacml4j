package com.artagon.xacml.v30;



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
	public Advice(String adviceId, 
			Effect effect,
			Iterable<AttributeAssignment> attributes){
		super(adviceId, effect, attributes);
	}
	
	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof Advice)){
			return false;
		}
		Advice a = (Advice)o;
		return id.equals(a.id) && 
		attributes.equals(a.attributes);
	}
}
