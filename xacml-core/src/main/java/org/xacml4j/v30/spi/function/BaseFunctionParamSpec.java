package org.xacml4j.v30.spi.function;

import org.xacml4j.v30.Expression;
import org.xacml4j.v30.XacmlSyntaxException;
import org.xacml4j.v30.pdp.FunctionParamSpec;

abstract class BaseFunctionParamSpec implements FunctionParamSpec
{
	private boolean optional = false;
	private Expression defaultValue;
	private boolean variadic;
	
	protected BaseFunctionParamSpec(){
	}
	
	protected BaseFunctionParamSpec(
			boolean optional, 
			boolean variadic,
			Expression defaultValue){
		if(!optional && defaultValue != null){
			throw new XacmlSyntaxException("Function parameter can't be required " +
					"and have default value at the same time");
		}
		this.optional = optional;
		this.variadic = variadic;
		this.defaultValue = defaultValue;
	}
	
	@Override
	public final boolean isOptional(){
		return optional;
	}
	
	@Override
	public final boolean isVariadic(){
		return variadic;
	}
	
	@Override
	public final Expression getDefaultValue(){
		return defaultValue;
	}
}
