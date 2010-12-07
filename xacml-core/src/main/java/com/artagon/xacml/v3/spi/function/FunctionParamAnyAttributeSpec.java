package com.artagon.xacml.v3.spi.function;

import java.util.ListIterator;

import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.Expression;
import com.artagon.xacml.v3.FunctionParamSpec;
import com.artagon.xacml.v3.ValueType;

final class FunctionParamAnyAttributeSpec implements FunctionParamSpec
{
	@Override
	public boolean isValidParamType(ValueType type) {
		return (type instanceof AttributeValueType);
	}

	@Override
	public boolean isVariadic() {
		return false;
	}

	@Override
	public boolean validate(ListIterator<Expression> it) {
		if(!it.hasNext()){
			return false;
		}
		Expression exp = it.next();
		return isValidParamType(exp.getEvaluatesTo());
	}
	
}
