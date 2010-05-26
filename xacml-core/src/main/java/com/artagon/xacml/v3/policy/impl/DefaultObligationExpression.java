package com.artagon.xacml.v3.policy.impl;

import java.util.Collection;

import com.artagon.xacml.v3.AttributeAssigmentExpression;
import com.artagon.xacml.v3.AttributeAssignment;
import com.artagon.xacml.v3.Effect;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.Obligation;
import com.artagon.xacml.v3.ObligationExpression;
import com.artagon.xacml.v3.PolicySyntaxException;
import com.artagon.xacml.v3.PolicyVisitor;
import com.artagon.xacml.v3.StatusCode;

final class DefaultObligationExpression extends BaseDecisionRuleResponseExpression implements ObligationExpression
{
	public DefaultObligationExpression(String id, Effect effect,
			Collection<AttributeAssigmentExpression> attributeExpressions) throws PolicySyntaxException 
	{
		super(id, effect, attributeExpressions);
	}
	
	@Override
	public Obligation evaluate(EvaluationContext context) throws EvaluationException
	{
		Collection<AttributeAssignment> attributes = evaluateAttributeAssingments(context);
		try{
			return new DefaultObligation(getId(), attributes);
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
