package com.artagon.xacml.v3;

import java.util.Collection;


public class AdviceExpression extends BaseDecisionRuleResponseExpression
{
	/**
	 * Constructs advice expression with a given identifier
	 * @param id an advice identifier
	 * @param appliesTo an effect when this advice is applicable
	 * @param attributeExpressions a collection of attribute
	 * assignment expression for this advice
	 * @exception PolicySyntaxException
	 */
	public AdviceExpression(String id, 
			Effect appliesTo,
			Collection<AttributeAssignmentExpression> attributeExpressions) 
		throws PolicySyntaxException 
	{
		super(id, appliesTo, attributeExpressions);
	}	
	
	/**
	 * Evaluates this advice expression by evaluating
	 * all {@link AttributeAssignmentExpression}
	 * 
	 * @param context an evaluation context
	 * @return {@link Advice} instance
	 * @throws EvaluationException if an evaluation error
	 * occurs
	 */
	public Advice evaluate(EvaluationContext context) throws EvaluationException
	{
		Collection<AttributeAssignment> attributes = evaluateAttributeAssingments(context);
		return new Advice(getId(), attributes);
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
