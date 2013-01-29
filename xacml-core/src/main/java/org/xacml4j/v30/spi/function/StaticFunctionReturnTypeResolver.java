package org.xacml4j.v30.spi.function;

import java.util.List;

import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ValueType;
import org.xacml4j.v30.pdp.FunctionSpec;

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
