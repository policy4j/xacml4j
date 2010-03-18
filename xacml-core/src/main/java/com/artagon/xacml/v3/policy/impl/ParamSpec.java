package com.artagon.xacml.v3.policy.impl;

import java.util.ListIterator;

import com.artagon.xacml.v3.Expression;
import com.artagon.xacml.v3.policy.ValueType;

public interface ParamSpec 
{	
	/**
	 * Validates if the "sequence" of expressions 
	 * from the current position is valid according 
	 * this specification. Iterator will be advanced to 
	 * the next expression after "sequence"
	 * 
	 * @param it an iterator
	 * @return <code>true</code> if sequence of
	 * expressions starting at the current position
	 * is valid according this spec
	 */
	boolean validate(ListIterator<Expression> it);
	
	/**
	 * Tests if instances of a given value type
	 * can be used as values for a function
	 * parameter specified by this specification
	 * 
	 * @param type a value type
	 * @return <code>true</code>
	 */
	boolean isValidParamType(ValueType type);
	
	boolean isVariadic();
}
