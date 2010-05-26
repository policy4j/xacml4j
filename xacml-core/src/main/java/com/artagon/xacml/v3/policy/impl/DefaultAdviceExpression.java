package com.artagon.xacml.v3.policy.impl;

import java.util.Collection;

import com.artagon.xacml.v3.Advice;
import com.artagon.xacml.v3.AdviceExpression;
import com.artagon.xacml.v3.AttributeAssigmentExpression;
import com.artagon.xacml.v3.AttributeAssignment;
import com.artagon.xacml.v3.Effect;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.PolicySyntaxException;
import com.artagon.xacml.v3.PolicyVisitor;
import com.artagon.xacml.v3.StatusCode;


public final class DefaultAdviceExpression extends BaseDecisionRuleResponseExpression implements AdviceExpression
{
	/**
	 * Constructs advice expression with a given identifier
	 * @param id an advice identifier
	 * @param appliesTo an effect when this advice is applicable
	 * @param attributeExpressions a collection of attribute
	 * assignment expression for this advice
	 * @exception PolicySyntaxException
	 */
	public DefaultAdviceExpression(String id, Effect appliesTo,
			Collection<AttributeAssigmentExpression> attributeExpressions) throws PolicySyntaxException 
	{
		super(id, appliesTo, attributeExpressions);
	}	
	
	@Override
	public Advice evaluate(EvaluationContext context) throws EvaluationException
	{
		Collection<AttributeAssignment> attributes = evaluateAttributeAssingments(context);
		try{
			return new DefaultAdvice(getId(), attributes);
		}catch(PolicySyntaxException e){
			throw new EvaluationException(
					StatusCode.createProcessingError(), context, e);
		}
		
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
