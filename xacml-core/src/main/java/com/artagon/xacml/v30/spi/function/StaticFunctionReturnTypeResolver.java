package com.artagon.xacml.v30.spi.function;

import java.util.List;

import com.artagon.xacml.v30.Expression;
import com.artagon.xacml.v30.ValueType;
import com.artagon.xacml.v30.pdp.FunctionSpec;
import com.google.common.base.Preconditions;

final class StaticFunctionReturnTypeResolver implements FunctionReturnTypeResolver
{
	private ValueType returnType;
	
	public StaticFunctionReturnTypeResolver(ValueType returnType){
		Preconditions.checkNotNull(returnType);
		this.returnType = returnType;
	}
	
	@Override
	public ValueType resolve(FunctionSpec spec, 
			List<Expression> arguments) {
		return returnType;
	}
}
