package com.artagon.xacml.v3.policy.impl;

import java.util.ListIterator;

import com.artagon.xacml.v3.Expression;
import com.artagon.xacml.v3.ValueType;
import com.artagon.xacml.v3.XacmlObject;
import com.google.common.base.Preconditions;

public class ParamFuncReferenceSpec extends XacmlObject implements ParamSpec
{
	private FunctionFamily family;
	
	public ParamFuncReferenceSpec(FunctionFamily family){
		Preconditions.checkNotNull(family);
		this.family = family;
	}
	
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
		if(exp instanceof DefaultFunctionReference){
			DefaultFunctionReference fexp = (DefaultFunctionReference)exp;
			return family.isMemeberOf(fexp.getSpec());
		}
		return false;
	}
}
