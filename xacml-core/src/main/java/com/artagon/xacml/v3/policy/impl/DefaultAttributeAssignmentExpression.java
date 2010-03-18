package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.Expression;
import com.artagon.xacml.v3.XacmlObject;
import com.artagon.xacml.v3.policy.AttributeAssigmentExpression;
import com.artagon.xacml.v3.policy.PolicyVisitor;

final class DefaultAttributeAssignmentExpression extends XacmlObject 
	implements AttributeAssigmentExpression
{
	private AttributeCategoryId category;
	private String attributeId;
	private String issuer;
	private Expression expression;

	/**
	 * Constructs attribute assignment
	 * 
	 * @param attributeId an attribute id
	 * @param expression an attribute assignment
	 * expression
	 * @param category an attribute category
	 * @param issuer an attribute issuer
	 */
	public DefaultAttributeAssignmentExpression(
			String attributeId, 
			Expression expression, 
			AttributeCategoryId category, 
			String issuer)
	{
		Preconditions.checkNotNull(attributeId);
		Preconditions.checkNotNull(expression);
		this.attributeId = attributeId;
		this.expression = expression;
		this.category = category;
		this.issuer = issuer;
	}
	
	@Override
	public String getAttributeId(){
		return attributeId;
	}
	
	@Override
	public AttributeCategoryId getCategory(){
		return category;
	}
	
	@Override
	public String getIssuer(){
		return issuer;
	}
	
	@Override
	public AttributeValue evaluate(EvaluationContext context) 
		throws EvaluationException
	{
		AttributeValue attribute = (AttributeValue)expression.evaluate(context);
		return attribute;
	}

	@Override
	public void accept(PolicyVisitor v) 
	{
		v.visitEnter(this);
		expression.accept(v);
		v.visitLeave(this);
	}
}
