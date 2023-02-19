package org.xacml4j.v30.policy;

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

import java.util.Optional;

import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeReferenceEvaluationException;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.ExpressionVisitor;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * The {@link AttributeDesignator} retrieves a bag of values for a
 * named attribute from the request context. A named attribute is
 * considered present if there is at least one attribute that
 * matches the criteria set out below.
 *
 *
 * The {@link AttributeDesignator} returns a bag containing all
 * the attribute values that are matched by the named attribute. In the
 * event that no matching attribute is present in the context, the
 * {@link AttributeDesignator#isMustBePresent()} governs whether it
 * evaluates to an empty bag or {@link EvaluationException} exception.
 *
 * See XACML 3.0 core section 7.3.5.
 *
 * @author Giedrius Trumpickas
 */
public class AttributeDesignator extends AttributeReference
{

	private final AttributeDesignatorKey designatorKey;

	private AttributeDesignator(Builder b){
		super(b);
		this.designatorKey = java.util.Objects.requireNonNull(b.key);
	}

	public static Builder builder(){
		return new Builder();
	}

	@Override
	public AttributeDesignatorKey getReferenceKey() {
		return designatorKey;
	}


	@Override
	public void accept(ExpressionVisitor visitor) {
		AttributeDesignatorVisitor v = (AttributeDesignatorVisitor) visitor;
		v.visitEnter(this);
		v.visitLeave(this);
	}

	@Override
	public String toString(){
		return MoreObjects.toStringHelper(this)
		.add("designatorKey", designatorKey)
		.add("mustBePresent", isMustBePresent())
		.toString();
	}

	@Override
	protected Optional<BagOfValues> doContextResolve(EvaluationContext context) {
		Optional<BagOfValues> v =  context.resolve(designatorKey);
		if(!v.isPresent() &&
				isMustBePresent()){
			throw AttributeReferenceEvaluationException.forDesignator(designatorKey);
		}
		return v;
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(!(o instanceof AttributeDesignator)){
			return false;
		}
		AttributeDesignator d = (AttributeDesignator)o;
		return designatorKey.equals(d.designatorKey) &&
		isMustBePresent() == d.isMustBePresent();
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(
				designatorKey,
				isMustBePresent());
	}

	public interface AttributeDesignatorVisitor extends ExpressionVisitor
	{
		void visitEnter(AttributeDesignator v);
		void visitLeave(AttributeDesignator v);
	}

	public static class Builder extends AttributeReference.Builder<Builder>
	{
		private AttributeDesignatorKey key;

		public Builder key(AttributeDesignatorKey key){
			this.key = java.util.Objects.requireNonNull(key);
			return this;
		}

		public AttributeDesignator build(){
			return new AttributeDesignator(this);
		}

		@Override
		protected Builder getThis() {
			return this;
		}
	}
}
