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

import java.io.Serializable;

import com.google.common.base.Supplier;


/**
 * Represents XACML attribute value
 *
 * @author Giedrius Trumpickas
 */
public interface Value<T>
		extends ValueExp, Serializable, Supplier<T>
{
	/**
	 * Gets attribute type
	 *
	 * @return {@link ValueType}
	 */
	ValueType getEvaluatesTo();


	/**
	 * Gets attribute expression value
	 *
	 * @return an attribute expression value
	 */
	T value();

	default T get(){
		return  value();
	}

	/**
	 * Creates bag with this attribute in the bag
	 *
	 * @return {@link BagOfValues}
	 */
	default BagOfValues toBag(){
		return this.getEvaluatesTo()
		           .bagBuilder()
		           .attribute(this)
		           .build();
	}

	default void accept(ExpressionVisitor v) {
		if(!(v instanceof ValueExpVisitor)){
			return;
		}
		((ValueExpVisitor) v).visitEnter(this);
		((ValueExpVisitor) v).visitLeave(this);
	}

	interface ValueExpVisitor extends ExpressionVisitor
	{
		default void visitEnter(Value value){
			value.accept(this);
		}
		default void visitLeave(Value value){
			value.accept(this);
		}
	}
}
