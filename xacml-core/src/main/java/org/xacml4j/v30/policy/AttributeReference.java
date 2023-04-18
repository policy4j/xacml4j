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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.AttributeReferenceEvaluationException;
import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ValueType;
import org.xacml4j.v30.ValueExpTypeInfo;

/**
 * A base class for XACML attribute references
 * in the XACML policies
 *
 * @author Giedrius Trumpickas
 */
public abstract class AttributeReference implements Expression
{
	private final static Logger LOG = LoggerFactory.getLogger(AttributeReference.class);

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
		LOG.debug("Evaluating ref={} result={}", getReferenceKey(), v);
		if(!v.isPresent() &&
				isMustBePresent()){
			LOG.debug("Failed to evaluate ref={}", this);
			throw AttributeReferenceEvaluationException
					.forMissingRef(getReferenceKey());
		}
		return v.orElse(
				getDataType()
						.emptyBag());
	}

	private Optional<BagOfValues> resolveImpl(EvaluationContext context)
	{
		try{
			return doContextResolve(context);
		}catch(AttributeReferenceEvaluationException e){
			LOG.debug(e.getMessage(), e);
			if(isMustBePresent()){
				throw e;
			}
			return Optional.empty();
		}catch(Exception e){
			LOG.debug(e.getMessage(), e);
			if(isMustBePresent()){
				throw AttributeReferenceEvaluationException
						.forMissingRef(getReferenceKey());
			}
			return Optional.empty();
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
