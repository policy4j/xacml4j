package com.artagon.xacml.v3.policy;

import java.util.Collection;

import com.artagon.xacml.v3.DefaultObligation;

public final class DefaultObligationExpression extends BaseDecisionResponseExpression implements ObligationExpression
{
	public DefaultObligationExpression(String id, Effect effect,
			Collection<AttributeAssignmentExpression> attributeExpressions) {
		super(id, effect, attributeExpressions);
	}
	
	/* (non-Javadoc)
	 * @see com.artagon.xacml.v3.policy.ObligationExpression#evaluate(com.artagon.xacml.v3.policy.EvaluationContext)
	 */
	public DefaultObligation evaluate(EvaluationContext context) throws EvaluationException
	{
		Collection<AttributeAssignment> attributes = evaluateAttributeAssingments(context);
		return new DefaultObligation(getId(), attributes);
	}

	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		for(AttributeAssignmentExpression exp : getAttributeAssignmentExpressions()){
			exp.accept(v);
		}
		v.visitLeave(this);
	}
}
