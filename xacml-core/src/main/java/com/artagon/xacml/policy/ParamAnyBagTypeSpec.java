package com.artagon.xacml.policy;

import java.util.ListIterator;

public class ParamAnyBagTypeSpec implements ParamSpec 
{
	@Override
	public boolean validate(ListIterator<Expression> it) {
		Expression exp = it.next();
		return isValidParamType(exp.getEvaluatesTo());
	}
	
	@Override
	public boolean isValidParamType(ValueType type) {
		return type instanceof BagOfAttributesType<?>;
	}
}
