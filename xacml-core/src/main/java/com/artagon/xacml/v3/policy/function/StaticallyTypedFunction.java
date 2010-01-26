package com.artagon.xacml.v3.policy.function;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.policy.Function;
import com.artagon.xacml.v3.policy.ValueType;

/**
 * An XACML function with statically defined
 * return type.
 * 
 * @author Giedrius Trumpickas
 */
public abstract class StaticallyTypedFunction  implements Function 
{
	private ValueType returnType;
	
	protected StaticallyTypedFunction(ValueType returnType){
		Preconditions.checkNotNull(returnType);
		this.returnType = returnType;
	}
	
	/**
	 * Gets function return type
	 * 
	 * @return function return type
	 */
	public final ValueType getReturnType(){
		return returnType;
	}
}
