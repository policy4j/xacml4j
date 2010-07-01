package com.artagon.xacml.v3.spi.function;

import java.util.ListIterator;

import com.artagon.xacml.v3.Expression;
import com.artagon.xacml.v3.FunctionReference;
import com.artagon.xacml.v3.ValueType;
import com.artagon.xacml.v3.XacmlObject;

final class FunctionParamFuncReferenceSpec extends XacmlObject implements FunctionParamSpec
{		
	@Override
	public boolean isValidParamType(ValueType type) {
		return false;
	}
	
	public boolean isVariadic() {
		return false;
	}
	
	@Override
	public boolean validate(ListIterator<Expression> it) {
		Expression exp = it.next();
		return (exp instanceof FunctionReference);
	}
}
