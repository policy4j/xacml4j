package com.artagon.xacml.v3;

import java.util.Collection;

public class ObligationExpression extends BaseDecisionRuleResponseExpression
{
	public ObligationExpression(String id, Effect effect,
			Collection<AttributeAssignmentExpression> attributeExpressions) throws PolicySyntaxException 
	{
		super(id, effect, attributeExpressions);
	}
	
	public Obligation evaluate(EvaluationContext context) throws EvaluationException
	{
		Collection<AttributeAssignment> attributes = evaluateAttributeAssingments(context);
		try{
			return new Obligation(getId(), attributes);
		}catch(PolicySyntaxException e){
			throw new EvaluationException(
					StatusCode.createProcessingError(), context, e);
		}
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
