package com.artagon.xacml.v30.spi.function;

import java.util.ListIterator;

import com.artagon.xacml.v30.BagOfAttributeExpType;
import com.artagon.xacml.v30.Expression;
import com.artagon.xacml.v30.ValueType;
import com.artagon.xacml.v30.pdp.FunctionParamSpec;


final class FunctionParamAnyBagSpec implements FunctionParamSpec
{
	@Override
	public boolean isValidParamType(ValueType type) {
		return (type instanceof BagOfAttributeExpType);
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
