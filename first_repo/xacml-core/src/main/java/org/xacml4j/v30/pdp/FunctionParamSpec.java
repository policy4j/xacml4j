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

import java.util.ListIterator;

import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ValueType;
import org.xacml4j.v30.spi.function.FunctionParamSpecVisitor;


public interface FunctionParamSpec
{
	/**
	 * Validates if the "sequence" of expressions
	 * from the current position is valid according
	 * this specification. Iterator will be advanced to
	 * the next expression after "sequence"
	 *
	 * @param it an iterator
	 * @return {@code true} if sequence of
	 * expressions starting at the current position
	 * is valid according this spec
	 */
	boolean validate(ListIterator<Expression> it);

	Expression getDefaultValue();
	
	boolean isOptional();
	
	/**
	 * Tests if instances of a given value type
	 * can be used as values for a function
	 * parameter specified by this specification
	 *
	 * @param type a value type
	 * @return {@code true}
	 */
	boolean isValidParamType(ValueType type);

	/**
	 * Tests if this parameter is variadic
	 *
	 * @return {@code true} if a function
	 * parameter represented by this object is
	 * variadic
	 */
	boolean isVariadic();
	
	void accept(FunctionParamSpecVisitor v);
}
