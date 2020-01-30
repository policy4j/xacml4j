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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfAttributeValues;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.ExpressionVisitor;

import java.util.Optional;

/**
 * XACML attribute selector expression
 *
 * @author Giedrius Trumpickas
 */
public class AttributeSelector extends
	AttributeReference
{
	private final static Logger LOG = LoggerFactory.getLogger(AttributeSelector.class);

	private final AttributeSelectorKey selectorKey;

	private AttributeSelector(Builder b){
		super(b);
		this.selectorKey = java.util.Objects.requireNonNull(b.selectorKey);
	}

	public static Builder builder(){
		return new Builder();
	}

	@Override
	public AttributeSelectorKey getReferenceKey() {
		return selectorKey;
	}

	@Override
	public String toString(){
		return MoreObjects.toStringHelper(this)
		.add("selectorKey", selectorKey)
		.add("mustBePresent", isMustBePresent())
		.toString();
	}

	@Override
	protected Optional<BagOfAttributeValues> doContextResolve(EvaluationContext context)
	{
		if(LOG.isDebugEnabled()){
			LOG.debug("Resolving SelectorKey=\"{}\"", selectorKey);
		}
		return context.resolve(selectorKey);
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(!(o instanceof AttributeSelector)){
			return false;
		}
		AttributeSelector s = (AttributeSelector)o;
		return selectorKey.equals(s.selectorKey) &&
		isMustBePresent() == s.isMustBePresent();
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(selectorKey, isMustBePresent());
	}

	@Override
	public void accept(ExpressionVisitor visitor) {
		AttributeSelectorVisitor v = (AttributeSelectorVisitor) visitor;
		v.visitEnter(this);
		v.visitLeave(this);
	}


	public interface AttributeSelectorVisitor extends ExpressionVisitor
	{
		void visitEnter(AttributeSelector v);
		void visitLeave(AttributeSelector v);
	}

	public static class Builder extends AttributeReference.Builder<Builder>
	{
		private AttributeSelectorKey selectorKey;

		public Builder key(AttributeSelectorKey key){
			this.selectorKey = java.util.Objects.requireNonNull(key);
			return this;
		}

		public AttributeSelector build(){
			return new AttributeSelector(this);
		}

		@Override
		protected Builder getThis() {
			return this;
		}
	}
}
