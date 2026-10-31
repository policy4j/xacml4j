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

import org.xacml4j.v30.AttributeReferenceEvaluationException;
import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ValueExpTypeInfo;
import org.xacml4j.v30.types.ValueType;

/**
 * A base class for XACML attribute references
 * in the XACML policies
 *
 * @author Giedrius Trumpickas
 */
public abstract class AttributeReference implements Expression
{
	private final boolean mustBePresent;

	/**
	 * Constructs attribute reference
	 *
	 * @param b attribute reference builder
	 * data type
	 */
	protected AttributeReference(Builder<?> b){
		this.mustBePresent = b.mustBePresent;
	}

	@Override
	public ValueExpTypeInfo getEvaluatesTo(){
		return getReferenceKey().getDataType().bagType();
	}

	/**
	 * Gets attribute reference key
	 *
	 * @return an attribute reference key
	 */
	public abstract  <T extends AttributeReferenceKey>  T getReferenceKey();

	/**
	 * Gets bag returned by this reference
	 * attribute XACML primitive data type
	 *
	 * @return {@link ValueType}
	 */
	public ValueType getDataType(){
		return getReferenceKey().getDataType();
	}

	/**
	 * Gets attribute category.
	 *
	 * @return attribute category
	 */
	public CategoryId getCategory(){
		return getReferenceKey().getCategory();
	}

	/**
	 * Governs whether this reference evaluates
	 * to an empty bag or {@link EvaluationException}
	 * is thrown during this reference evaluation
	 *
	 * @return {@code true} if attribute
	 * must be present
	 */
	public boolean isMustBePresent(){
		return mustBePresent;
	}

	@Override
	public BagOfValues evaluate(EvaluationContext context)
		throws EvaluationException
	{
		Optional<BagOfValues> v = resolveImpl(context);
		if(!v.isPresent() &&
				isMustBePresent()){
			throw AttributeReferenceEvaluationException
					.forMissingRef(getReferenceKey());
		}
		// Not orElse: its argument is evaluated even when the bag is present (D14)
		return v.isPresent() ? v.get() : getDataType().emptyBag();
	}

	private Optional<BagOfValues> resolveImpl(EvaluationContext context)
	{
		try{
			return doContextResolve(context);
		}catch(EvaluationException e){
			// Only a missing attribute follows MustBePresent (XACML 3.0 7.3.5).
			if(e.getStatus().getStatusCode().isMissingAttributeError()){
				if(isMustBePresent()){
					throw e;
				}
				return Optional.empty();
			}
			if(e.getStatus().getStatusCode().isSyntaxError()){
				// The status code stays; the response message is a fixed text.
				throw AttributeReferenceEvaluationException
						.forSyntaxError(getReferenceKey(), e);
			}
			// An XPath evaluation error or a resolver failure is a processing
			// error with a fixed response message, whatever MustBePresent is.
			throw AttributeReferenceEvaluationException
					.forResolverFailure(getReferenceKey(), e);
		}catch(RuntimeException e){
			throw AttributeReferenceEvaluationException
					.forResolverFailure(getReferenceKey(), e);
		}
	}

	protected abstract Optional<BagOfValues> doContextResolve(EvaluationContext context);


	public static abstract class Builder<T>
	{
		private boolean mustBePresent;

		public T mustBePresent(boolean present){
			this.mustBePresent = present;
			return getThis();
		}

		protected abstract T getThis();
	}
}
