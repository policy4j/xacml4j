package org.xacml4j.v30.pdp;

import org.xacml4j.v30.Effect;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Obligation;


public class ObligationExpression extends
	BaseDecisionRuleResponseExpression
{
	public ObligationExpression(Builder b)
	{
		super(b);
	}

	public Obligation evaluate(EvaluationContext context) throws EvaluationException{
		return Obligation
				.builder(getId(), getEffect())
				.attributes(evaluateAttributeAssingments(context))
				.build();
	}

	public static Builder builder(String id, Effect appliesTo){
		return new Builder().id(id).effect(appliesTo);
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

	public static class Builder extends BaseDecisionRuleResponseExpressionBuilder<Builder>
	{

		@Override
		protected Builder getThis() {
			return this;
		}

		public ObligationExpression build(){
			return new ObligationExpression(this);
		}
	}

}
