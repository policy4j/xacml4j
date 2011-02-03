package com.artagon.xacml.v30;

import java.util.Collection;



public class ObligationExpression extends BaseDecisionRuleResponseExpression
{
	public ObligationExpression(String id, Effect effect,
			Collection<AttributeAssignmentExpression> attributeExpressions)  
	{
		super(id, effect, attributeExpressions);
	}
	
	public Obligation evaluate(EvaluationContext context) throws EvaluationException
	{
		Collection<AttributeAssignment> attributes = evaluateAttributeAssingments(context);
		return new Obligation(getId(), getEffect(), attributes);
	
	}

	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		for(AttributeAssignmentExpression exp : getAttributeAssignmentExpressions()){
			exp.accept(v);
		}
		v.visitLeave(this);
	}
	
	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(!(o instanceof ObligationExpression)){
			return false;
		}
		ObligationExpression ox = (ObligationExpression)o;
		return id.equals(ox.id) 
			&& effect.equals(ox.effect) 
			&& attributeExpressions.equals(ox.attributeExpressions);
	}
}
