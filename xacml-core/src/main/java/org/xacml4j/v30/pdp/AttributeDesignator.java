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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.ExpressionVisitor;

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
	private final static Logger log = LoggerFactory.getLogger(AttributeDesignator.class);

	private final AttributeDesignatorKey designatorKey;

	private AttributeDesignator(Builder b){
		super(b);
		this.designatorKey = b.keyBuilder.build();
	}

	public static Builder builder(){
		return new Builder();
	}

	@Override
	public AttributeDesignatorKey getReferenceKey() {
		return designatorKey;
	}

	/**
	 * Evaluates this attribute designator by resolving
	 * attribute via {@link EvaluationContext#resolve(org.xacml4j.v30.AttributeDesignatorKey)}
	 *
	 * @return {@link BagOfAttributeExp} instance
	 * @exception EvaluationException if attribute can't be resolved
	 * and {@link AttributeDesignator#mustBePresent} is true
	 */
	@Override
	public BagOfAttributeExp evaluate(EvaluationContext context)
			throws EvaluationException
	{
		BagOfAttributeExp v;
		try {
			v = designatorKey.resolve(context);
		} catch (AttributeReferenceEvaluationException e) {
			if (log.isDebugEnabled()) {
				log.debug("Reference=\"{}\" evaluation failed with error=\"{}\"",
				          this, e.getMessage());
			}
			if (isMustBePresent()) {
				throw e;
			}
			return getDataType().bagType().createEmpty();
		} catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("Reference=\"{}\" evaluation failed with error=\"{}\"",
				          this, e.getMessage());
			}
			if (isMustBePresent()) {
				throw new AttributeReferenceEvaluationException(designatorKey);
			}
			if (log.isDebugEnabled()) {
				log.debug("Returning an empty bag for attributeId=\"{}\", category=\"{}\"",
				          designatorKey.getAttributeId(), designatorKey.getCategory());
			}
			return getDataType().bagType().createEmpty();
		}
		if (v == null) {
			 if (isMustBePresent()) {
				 if (log.isDebugEnabled()) {
					 log.debug("Failed to resolve attributeId=\"{}\", category=\"{}\"",
					           designatorKey.getAttributeId(), designatorKey.getCategory());
				 }
				 throw new AttributeReferenceEvaluationException(designatorKey);
			 } else {
			 	if (log.isDebugEnabled()) {
				    log.debug("Returning an empty bag for attributeId=\"{}\", category=\"{}\"",
				              designatorKey.getAttributeId(), designatorKey.getCategory());
			    }
			 	return getDataType().bagType().createEmpty();
			 }
		}
		return v;
	}

	@Override
	public void accept(ExpressionVisitor visitor) {
		AttributeDesignatorVisitor v = (AttributeDesignatorVisitor) visitor;
		v.visitEnter(this);
		v.visitLeave(this);
	}

	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("designatorKey", designatorKey)
		.add("mustBePresent", isMustBePresent())
		.toString();
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
		private final AttributeDesignatorKey.Builder keyBuilder = AttributeDesignatorKey.builder();

		public Builder attributeId(String attributeId){
			keyBuilder.attributeId(attributeId);
			return this;
		}

		public Builder issuer(String issuer){
			this.keyBuilder.issuer(issuer);
			return this;
		}

		public AttributeDesignator build(){
			return new AttributeDesignator(this);
		}

		@Override
		protected Builder getThis() {
			return this;
		}

		@Override
		protected AttributeReferenceKey.Builder<?> getBuilder() {
			return keyBuilder;
		}
	}
}
