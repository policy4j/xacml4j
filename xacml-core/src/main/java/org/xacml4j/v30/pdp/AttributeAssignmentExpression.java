package org.xacml4j.v30.pdp;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.xacml4j.v30.Categories;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ValueExpression;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class AttributeAssignmentExpression implements PolicyElement
{
	private final CategoryId category;
	private final String attributeId;
	private final String issuer;
	private final Expression expression;

	/**
	 * Constructs attribute assignment
	 *
	 * @param b attribute assignment builder
	 */
	private AttributeAssignmentExpression(Builder b){
		Preconditions.checkNotNull(b.id);
		Preconditions.checkNotNull(b.expression);
		this.attributeId = b.id;
		this.expression = b.expression;
		this.category = b.category;
		this.issuer = b.issuer;
	}

	public static Builder builder(String id) {
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
	 * @return category identifier or {@code null}
	 */
	public CategoryId getCategory(){
		return category;
	}

	/**
	 * Gets an issuer of the attribute.
	 * If issuer is not specified, the attribute
	 * has not issuer
	 *
	 * @return attribute issuer or {@code null}
	 */
	public String getIssuer(){
		return issuer;
	}

	public Expression getExpression(){
		return expression;
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
		private CategoryId category;

		public Builder id(String id){
			Preconditions.checkArgument(!Strings.isNullOrEmpty(id));
			this.id = id;
			return this;
		}

		public Builder issuer(String issuer){
			this.issuer = issuer;
			return this;
		}

		public Builder category(CategoryId category){
			this.category = category;
			return this;
		}
		
		public Builder category(String category){
			this.category = !Strings.isNullOrEmpty(category)?Categories.parse(category):null;
			return this;
		}

		public Builder expression(Expression exp){
			Preconditions.checkNotNull(exp);
			this.expression = exp;
			return this;
		}

		public AttributeAssignmentExpression build(){
			return new AttributeAssignmentExpression(this);
		}
	}
}
