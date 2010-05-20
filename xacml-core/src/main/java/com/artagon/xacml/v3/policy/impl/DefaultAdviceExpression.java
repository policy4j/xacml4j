package com.artagon.xacml.v3.policy.impl;

import java.util.Collection;

import com.artagon.xacml.v3.Advice;
import com.artagon.xacml.v3.AdviceExpression;
import com.artagon.xacml.v3.AttributeAssigmentExpression;
import com.artagon.xacml.v3.AttributeAssignment;
import com.artagon.xacml.v3.Effect;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.PolicyVisitor;


public final class DefaultAdviceExpression extends BaseDecisionRuleResponseExpression implements AdviceExpression
{
	/**
	 * Constructs advice expression with a given identifier
	 * @param id an advice identifier
	 * @param appliesTo an effect when this advice is applicable
	 * @param attributeExpressions a collection of attribute
	 * assignment expression for this advice
	 */
	public DefaultAdviceExpression(String id, Effect appliesTo,
			Collection<AttributeAssigmentExpression> attributeExpressions) {
		super(id, appliesTo, attributeExpressions);
	}	
	
	/* (non-Javadoc)
	 * @see com.artagon.xacml.v3.policy.AdviceExpression#evaluate(com.artagon.xacml.v3.policy.EvaluationContext)
	 */
	public Advice evaluate(EvaluationContext context) throws EvaluationException
	{
		Collection<AttributeAssignment> attributes = evaluateAttributeAssingments(context);
		return new DefaultAdvice(getId(), attributes);
	}
	
	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		for(AttributeAssigmentExpression exp : getAttributeAssignmentExpressions()){
			exp.accept(v);
		}
		v.visitLeave(this);
	}
}
