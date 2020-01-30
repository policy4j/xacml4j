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

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import org.xacml4j.v30.*;


public class AttributeReferenceEvaluationException extends EvaluationException
{
	private static final long serialVersionUID = 4604041385261391333L;

	private AttributeReferenceKey ref;
	
	public AttributeReferenceEvaluationException(AttributeDesignatorKey ref,
			String format, Object ... args){
		this(Status
				.missingAttribute(ref)
				.build(), ref,
				String.format(format, args));
	}
	
	public AttributeReferenceEvaluationException(
			Status status, 
			AttributeReferenceKey key, 
			String format, Object ... args){
		super(status, String.format(format, args));
		Preconditions.checkNotNull(key);
		this.ref = key;
	}


	public AttributeReferenceKey getReference(){
		return ref;
	}


	public static AttributeReferenceEvaluationException forMissingRef(AttributeReferenceKey ref) {
		if (ref instanceof AttributeSelectorKey) {
			return forSelector((AttributeSelectorKey) ref);
		}
		if (ref instanceof AttributeDesignatorKey) {
			return forDesignator((AttributeDesignatorKey) ref);
		}
		return new AttributeReferenceEvaluationException(Status
				.processingError().build(),
				ref,
				"Unsupported attribute reference type=\"%s\"",
				ref.getClass().getName());
	}


	public static AttributeReferenceEvaluationException forSelector(AttributeSelectorKey selectorKey, Throwable t){
		return forSelector(selectorKey, ()->t.getMessage());
	}

	public static AttributeReferenceEvaluationException forDesignator(AttributeDesignatorKey selectorKey, Throwable t){
		return forDesignator(selectorKey, ()->t.getMessage());
	}

	public static AttributeReferenceEvaluationException forDesignator(AttributeDesignatorKey selectorKey){
		return forDesignator(selectorKey, ()->null);
	}

	public static AttributeReferenceEvaluationException forSelector(AttributeSelectorKey selectorKey){
		return forSelector(selectorKey, ()->null);
	}

	public static AttributeReferenceEvaluationException forDesignator(AttributeDesignatorKey designatorKey, Supplier<String> message)
	{
		String messageText = message !=  null?message.get():null;
		StringBuilder b = new StringBuilder(
				"Designator.AttributeId=\"%s\", Designator.CategoryId=\"%s\", Designator.DataType=\"%s\"");
		return new AttributeReferenceEvaluationException(
				Status
						.missingAttribute(designatorKey)
						.build(),
				designatorKey,
				(messageText != null)?b .append("- %s").toString():b.toString(),
				designatorKey.getAttributeId(),
				designatorKey.getCategory(),
				designatorKey.getDataType()
						.getAbbrevDataTypeId(),
				messageText);
	}

	public static AttributeReferenceEvaluationException forSelector(AttributeSelectorKey selectorKey, Supplier<String> message)
	{
		String messageText = message !=  null?message.get():null;
		StringBuilder b = new StringBuilder(
				"Selector.Path=\"%s\", Selector.CategoryId=\"%s\", Selector.ContextSelectorId=\"%s\", Selector.DataType=\"%s\"");
		return new AttributeReferenceEvaluationException(
				Status
						.missingAttribute(selectorKey)
						.build(),
				selectorKey,
				(messageText != null)?b .append("- %s").toString():b.toString(),
				selectorKey.getPath(),
				selectorKey.getCategory(),
				selectorKey.getContextSelectorId(),
				selectorKey.getDataType().getAbbrevDataTypeId(),
				messageText);
	}



}
