package com.artagon.xacml.policy.function;

import com.artagon.xacml.policy.Function;
import com.artagon.xacml.policy.ValueType;
import com.artagon.xacml.util.Preconditions;

/**
 * An XACML function with statically defined
 * return type.
 * 
 * @author Giedrius Trumpickas
 */
public abstract class StaticallyTypedFunction <R extends ValueType> implements Function 
{
	private R returnType;
	
	protected StaticallyTypedFunction(R returnType){
		Preconditions.checkNotNull(returnType);
		this.returnType = returnType;
	}
	
	/**
	 * Gets function return type
	 * 
	 * @return function return type
	 */
	public final R getReturnType(){
		return returnType;
	}
}
