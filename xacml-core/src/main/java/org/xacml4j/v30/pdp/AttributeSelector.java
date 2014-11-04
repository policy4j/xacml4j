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
import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.ExpressionVisitor;

import com.google.common.base.Objects;

public class AttributeSelector extends
	AttributeReference
{
	private final static Logger log = LoggerFactory.getLogger(AttributeSelector.class);

	private final AttributeSelectorKey selectorKey;

	private AttributeSelector(Builder b){
		super(b);
		this.selectorKey = b.keyBuilder.build();
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
		return Objects.toStringHelper(this)
		.add("selectorKey", selectorKey)
		.add("mustBePresent", isMustBePresent())
		.toString();
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

	@Override
	public BagOfAttributeExp evaluate(EvaluationContext context)
			throws EvaluationException
	{
		BagOfAttributeExp v = null;
		try{
			v =  selectorKey.resolve(context);
		}catch(EvaluationException e){
			if(isMustBePresent()){
				throw e;
			}
			return getDataType().bagType().createEmpty();
		}catch(Exception e){
			if(isMustBePresent()){
				throw new AttributeReferenceEvaluationException(selectorKey);
			}
			return getDataType().bagType().createEmpty();
		}
		if((v == null ||
				v.isEmpty())
				&& isMustBePresent()){
			if(log.isDebugEnabled()){
				log.debug("Failed to resolve xpath=\"{}\", category=\"{}\"",
						selectorKey.getPath(),
						selectorKey.getCategory());
			}
			throw new AttributeReferenceEvaluationException(selectorKey);
		}
		return ((v == null)?getDataType().bagType().createEmpty():v);
	}

	public interface AttributeSelectorVisitor extends ExpressionVisitor
	{
		void visitEnter(AttributeSelector v);
		void visitLeave(AttributeSelector v);
	}

	public static class Builder extends AttributeReference.Builder<Builder>
	{
		private final AttributeSelectorKey.Builder keyBuilder = AttributeSelectorKey.builder();

		public Builder xpath(String xpath){
			keyBuilder.xpath(xpath);
			return this;
		}

		public Builder contextSelectorId(String id){
			keyBuilder.contextSelectorId(id);
			return this;
		}

		public AttributeSelector build(){
			return new AttributeSelector(this);
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
