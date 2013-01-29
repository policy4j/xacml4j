package org.xacml4j.v30.spi.function;

import java.util.ListIterator;

import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ValueType;
import org.xacml4j.v30.pdp.FunctionParamSpec;
import org.xacml4j.v30.pdp.FunctionReference;


final class FunctionParamFuncReferenceSpec implements FunctionParamSpec
{
	@Override
	public boolean isValidParamType(ValueType type) {
		return false;
	}

	@Override
	public boolean isVariadic() {
		return false;
	}

	@Override
	public boolean validate(ListIterator<Expression> it) {
		Expression exp = it.next();
		return (exp instanceof FunctionReference);
	}
}
