package org.xacml4j.v30;

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


public class AttributeReferenceEvaluationException extends EvaluationException
{
	private static final long serialVersionUID = 4604041385261391333L;

	/** The response-visible status message of a resolution failure. */
	public static final String RESOLUTION_FAILED_MESSAGE = "Attribute resolution failed";

	/** The response-visible status message of a reference that is not valid. */
	public static final String INVALID_REFERENCE_MESSAGE = "Attribute reference is invalid";

	private AttributeReferenceKey ref;

	/** The factory that made an exception. A subclass can not set it. */
	private enum Origin { OTHER, RESOLVER_FAILURE, SYNTAX_ERROR }

	private Origin origin = Origin.OTHER;

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


	/**
	 * Creates an exception for a failure of an attribute or content
	 * resolver, or of the reference evaluation itself. Its status is
	 * processing-error and never missing-attribute, so the failure does
	 * not depend on {@code MustBePresent}.
	 * <p>
	 * The status message is a fixed text, because a status message is
	 * returned to the caller in the response. The message of the cause can
	 * hold back-end details; it is not copied. The cause stays in this
	 * exception and in the status error for logs and audit only.
	 *
	 * @param ref a reference being resolved
	 * @param t a failure cause
	 * @return {@link AttributeReferenceEvaluationException}
	 */
	public static AttributeReferenceEvaluationException forResolverFailure(AttributeReferenceKey ref, Throwable t){
		Preconditions.checkNotNull(t);
		if(madeBy(t, Origin.RESOLVER_FAILURE)){
			// A nested resolution has mapped this failure already.
			return (AttributeReferenceEvaluationException)t;
		}
		return new AttributeReferenceEvaluationException(
				t,
				Status.processingError()
						.message(RESOLUTION_FAILED_MESSAGE)
						.build(),
				ref,
				String.format("Failed to resolve ref=\"%s\" - %s", ref, causeName(t)),
				Origin.RESOLVER_FAILURE);
	}

	/**
	 * Creates an exception for a reference that the engine found to be
	 * not valid, for example a selector path with a syntax error. Its
	 * status is syntax-error with a fixed message; the cause stays in this
	 * exception for logs only.
	 *
	 * @param ref a reference being resolved
	 * @param t a syntax error cause
	 * @return {@link AttributeReferenceEvaluationException}
	 */
	public static AttributeReferenceEvaluationException forSyntaxError(AttributeReferenceKey ref, Throwable t){
		Preconditions.checkNotNull(t);
		if(madeBy(t, Origin.SYNTAX_ERROR)){
			return (AttributeReferenceEvaluationException)t;
		}
		return new AttributeReferenceEvaluationException(
				t,
				Status.syntaxError()
						.message(INVALID_REFERENCE_MESSAGE)
						.build(),
				ref,
				String.format("Invalid ref=\"%s\" - %s", ref, causeName(t)),
				Origin.SYNTAX_ERROR);
	}

	/**
	 * Names a cause in the message of the mapped failure. The cause is
	 * foreign code, so no method that it can override is called on it: an
	 * exception from its {@code getMessage()} must not replace the failure
	 * that is being mapped. Its message is still available from
	 * {@link #getCause()}.
	 */
	private static String causeName(Throwable t){
		return t.getClass().getName();
	}

	/**
	 * Reads the private field directly. An overridable method could let a
	 * subclass claim an origin, and with it a status and a status message
	 * of its own choice.
	 */
	private static boolean madeBy(Throwable t, Origin origin){
		return t instanceof AttributeReferenceEvaluationException
				&& ((AttributeReferenceEvaluationException)t).origin == origin;
	}

	/**
	 * Keeps the message of the given status. The other constructors copy
	 * the exception message into the status.
	 */
	private AttributeReferenceEvaluationException(
			Throwable cause,
			Status status,
			AttributeReferenceKey key,
			String message,
			Origin origin){
		super(status, message, cause);
		Preconditions.checkNotNull(key);
		this.ref = key;
		this.origin = origin;
	}

	/**
	 * Tells whether {@link #forResolverFailure} made this exception. Only
	 * then is its status known to be processing-error with the fixed
	 * response message.
	 *
	 * @return {@code true} for a mapped resolver failure
	 */
	public final boolean isResolverFailure(){
		return origin == Origin.RESOLVER_FAILURE;
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
				"AttributeId=\"%s\", CategoryId=\"%s\", DataType=\"%s\"");
		return new AttributeReferenceEvaluationException(
				Status
						.missingAttribute(designatorKey)
						.build(),
				designatorKey,
				(messageText != null)?b .append("- %s").toString():b.toString(),
				designatorKey.getAttributeId(),
				designatorKey.getCategory(),
				designatorKey.getDataType()
						.getShortTypeId(),
				messageText);
	}

	public static AttributeReferenceEvaluationException forSelector(AttributeSelectorKey selectorKey, Supplier<String> message)
	{
		String messageText = message !=  null?message.get():null;
		StringBuilder b = new StringBuilder(
				"Path=\"%s\", Type=\"%s\" CategoryId=\"%s\", ContextSelectorId=\"%s\", DataType=\"%s\"");
		return new AttributeReferenceEvaluationException(
				Status
						.missingAttribute(selectorKey)
						.build(),
				selectorKey,
				(messageText != null)?b .append("- %s").toString():b.toString(),
				selectorKey.getPath(),
				selectorKey.getPathType(),
				selectorKey.getCategory(),
				selectorKey.getContextSelectorId(),
				selectorKey.getDataType().getShortTypeId(),
				messageText);
	}



}
