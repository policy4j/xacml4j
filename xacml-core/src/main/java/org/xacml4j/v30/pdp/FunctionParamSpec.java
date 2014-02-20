package org.xacml4j.v30.pdp;

import java.util.ListIterator;

import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ValueType;


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
}
