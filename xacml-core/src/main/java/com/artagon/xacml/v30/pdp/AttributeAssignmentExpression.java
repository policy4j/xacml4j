package com.artagon.xacml.v30.pdp;

import com.artagon.xacml.v30.AttributeCategory;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.EvaluationException;
import com.artagon.xacml.v30.Expression;
import com.artagon.xacml.v30.ValueExpression;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class AttributeAssignmentExpression implements PolicyElement
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
	private AttributeAssignmentExpression(Builder b){
		this.attributeId = b.id;
		this.expression = b.expression;
		this.category = b.category;
		this.issuer = b.issuer;
	}

	public static Builder builder(String id)
	{
		return new Builder().id(id);
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

	public ValueExpression evaluate(EvaluationContext context)
		throws EvaluationException
	{
		ValueExpression val =  (ValueExpression)expression.evaluate(context);
		Preconditions.checkState(val != null);
		return val;
	}

	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("attributeId", attributeId)
		.add("category", category)
		.add("issuer", issuer)
		.add("expression", expression)
		.toString();
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(
				attributeId,
				category,
				issuer,
				expression);
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof AttributeAssignmentExpression)){
			return false;
		}
		AttributeAssignmentExpression e = (AttributeAssignmentExpression)o;
		return attributeId.equals(e.attributeId) &&
		Objects.equal(category, e.category) &&
		Objects.equal(issuer, e.issuer) &&
		expression.equals(e.expression);

	}

	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}

	public static class Builder
	{
		private String id;
		private String issuer;
		private Expression expression;
		private AttributeCategory category;

		public Builder id(String id){
			Preconditions.checkArgument(!Strings.isNullOrEmpty(id));
			this.id = id;
			return this;
		}

		public Builder issuer(String issuer){
			this.issuer = issuer;
			return this;
		}

		public Builder category(AttributeCategory category){
			this.category = category;
			return this;
		}

		public Builder expression(Expression exp){
			Preconditions.checkNotNull(exp);
			this.expression = exp;
			return this;
		}

		public AttributeAssignmentExpression build(){
			Preconditions.checkState(id != null);
			Preconditions.checkState(expression != null);
			return new AttributeAssignmentExpression(this);
		}
	}
}
