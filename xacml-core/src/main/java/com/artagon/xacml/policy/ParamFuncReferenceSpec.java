package com.artagon.xacml.policy;

import java.util.List;
import java.util.ListIterator;

public class ParamFuncReferenceSpec implements ParamSpec
{
	private ParamSpec returnType;
	private List<ParamSpec> parameters;
	
	@Override
	public boolean isValidParamType(ValueType type) {
		return false;
	}
	
	@Override
	public boolean validate(ListIterator<Expression> it) {
		// TODO Auto-generated method stub
		return false;
	}
}
