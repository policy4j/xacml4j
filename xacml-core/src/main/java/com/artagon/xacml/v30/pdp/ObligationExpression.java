package com.artagon.xacml.v30.pdp;

import java.util.Collection;
import java.util.LinkedList;

import com.artagon.xacml.v30.AttributeCategory;
import com.artagon.xacml.v30.Effect;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.EvaluationException;
import com.artagon.xacml.v30.Obligation;


public class ObligationExpression extends 
	BaseDecisionRuleResponseExpression
{
	public ObligationExpression(String id, Effect effect,
			Collection<AttributeAssignmentExpression> attributeExpressions)  
	{
		super(id, effect, attributeExpressions);
	}
	
	public Obligation evaluate(EvaluationContext context) throws EvaluationException{
		return Obligation
				.builder(getId(), getEffect())
				.attributes(evaluateAttributeAssingments(context))
				.build();
	}
	
	public static Builder builder(String id, Effect applieTo){
		return new Builder(id, applieTo);
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
	
	public static class Builder 
	{
		private String id;
		private Effect appliesTo;
		private Collection<AttributeAssignmentExpression> attributes = new LinkedList<AttributeAssignmentExpression>();
	
		private Builder(String id, Effect appliesTo){
			this.id = id;
			this.appliesTo = appliesTo;
		}
	
		public Builder attributeAssigment(
				String id, Expression expression){
			attributes.add(new AttributeAssignmentExpression(id, expression));
			return this;
		}
		
		public Builder appliesTo(Effect effect){
			this.appliesTo = effect;
			return this;
		}
		
		public Builder id(String id){
			this.id = id;
			return this;
		}
	
		public Builder withAttributeAssigment(
				String id, Expression expression, 
				AttributeCategory category, String issuer)
		{
			attributes.add(new AttributeAssignmentExpression(id, expression, category, issuer));
			return this;
		}
		
		public ObligationExpression build(){
			return new ObligationExpression(id, appliesTo, attributes);
		}
	}
}
