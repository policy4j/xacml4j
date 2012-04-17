package com.artagon.xacml.v30.pdp;

import java.util.Collection;
import java.util.LinkedList;

import com.artagon.xacml.v30.AttributeCategory;


public class AdviceExpression extends BaseDecisionRuleResponseExpression
{
	/**
	 * Constructs advice expression with a given identifier
	 * @param id an advice identifier
	 * @param appliesTo an effect when this advice is applicable
	 * @param attributeExpressions a collection of attribute
	 * assignment expression for this advice
	 * @exception XacmlSyntaxException
	 */
	public AdviceExpression(String id, 
			Effect appliesTo,
			Collection<AttributeAssignmentExpression> attributeExpressions)  
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
		return Advice
				.builder(getId(), getEffect())
				.attributes(attributes)
				.create();
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
		if(!(o instanceof AdviceExpression)){
			return false;
		}
		AdviceExpression ox = (AdviceExpression)o;
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
	
		public Builder withAttributeAssigment(
				String id, Expression expression)
		{
			attributes.add(new AttributeAssignmentExpression(id, expression));
			return this;
		}
		
		public Builder withAttributeAssigment(
				String id, AttributeCategory category, Expression expression)
		{
			attributes.add(new AttributeAssignmentExpression(id, expression, category, null));
			return this;
		}
	
		public Builder withAttributeAssigment(
				String id, AttributeCategory category, String issuer, Expression expression)
		{
			attributes.add(new AttributeAssignmentExpression(id, expression, category, issuer));
			return this;
		}
		
		public AdviceExpression build(){
			return new AdviceExpression(id, appliesTo, attributes);
		}
	}
}
