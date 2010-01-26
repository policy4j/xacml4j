package com.artagon.xacml.policy;

import java.util.Collection;

/**
 * In some applications it is helpful to specify supplemental 
 * information about a decision. XACML provides facilities 
 * to specify supplemental information about a decision with 
 * the {@link Advice}. Such advice may be safely ignored by the PEP.
 * 
 * @author Giedrius Trumpickas
 */
public final class Advice extends BaseDecisionResponse
{
	public Advice(String adviceId, Collection<AttributeAssignment> attributes) {
		super(adviceId, attributes);
	}

	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		for(AttributeAssignment attr : getAttributes()){
			attr.accept(v);
		}
		v.visitLeave(this);
	}
}
