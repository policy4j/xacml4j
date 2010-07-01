package com.artagon.xacml.v3;

import java.util.ListIterator;

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
