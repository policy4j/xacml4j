package com.artagon.xacml.v30;

import com.google.common.base.Preconditions;

public class AttributeAssignmentExpression extends XacmlObject 
	implements PolicyElement
{
	private AttributeCategory category;
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
	public AttributeAssignmentExpression(
			String attributeId, 
			Expression expression, 
			AttributeCategory category, 
			String issuer)
	{
		Preconditions.checkNotNull(attributeId);
		Preconditions.checkNotNull(expression);
		this.attributeId = attributeId;
		this.expression = expression;
		this.category = category;
		this.issuer = issuer;
	}
	
	public AttributeAssignmentExpression(String attributeId, 
			Expression expression){
		this(attributeId, expression, null, null);
	}
	
	/**
	 * Gets attribute identifier
	 * 
	 * @return attribute identifier
	 */
	public String getAttributeId(){
		return attributeId;
	}
	
	/**
	 * An optional category of the attribute. 
	 * If category is not specified, the attribute has no category
	 * 
	 * @return category identifier or <code>null</code>
	 */
	public AttributeCategory getCategory(){
		return category;
	}
	

	/**
	 * Gets an issuer of the attribute.
	 * If issuer is not specified, the attribute
	 * has not issuer
	 * 
	 * @return attribute issuer or <code>null</code>
	 */
	public String getIssuer(){
		return issuer;
	}
	
	public AttributeValue evaluate(EvaluationContext context) 
		throws EvaluationException
	{
		AttributeValue attribute = (AttributeValue)expression.evaluate(context);
		Preconditions.checkState(attribute != null);
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
