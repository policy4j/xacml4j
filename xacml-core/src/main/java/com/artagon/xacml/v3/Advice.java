package com.artagon.xacml.v3;

import java.util.Collection;





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
			Collection<AttributeAssignment> attributes) 
	{
		super(adviceId, attributes);
	}
}
