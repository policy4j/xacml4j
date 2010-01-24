package com.artagon.xacml.policy.function;

import java.util.ListIterator;

import com.artagon.xacml.policy.AttributeDataType;
import com.artagon.xacml.policy.Expression;
import com.artagon.xacml.policy.ParamSpec;
import com.artagon.xacml.policy.ValueType;

public class ParamAnyAttributeTypeSpec implements ParamSpec
{
	@Override
	public boolean validate(ListIterator<Expression> it) {
		Expression exp = it.next();
		return isValidParamType(exp.getEvaluatesTo());
	}

	@Override
	public boolean isValidParamType(ValueType type) {
		return type instanceof AttributeDataType;
	}
}
