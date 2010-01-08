package com.artagon.xacml.policy;

import java.util.ListIterator;

import com.artagon.xacml.util.Preconditions;

public class ParamFuncReferenceSpec implements ParamSpec
{
	private FunctionFamilySpec family;
	
	public ParamFuncReferenceSpec(FunctionFamilySpec family){
		Preconditions.checkNotNull(family);
		this.family = family;
	}
	
	@Override
	public boolean isValidParamType(ValueType type) {
		return false;
	}
	
	@Override
	public boolean validate(ListIterator<Expression> it) {
		Expression exp = it.next();
		if(exp instanceof FunctionReferenceExpression){
			FunctionReferenceExpression fexp = (FunctionReferenceExpression)exp;
			return family.isMemeberOf(fexp.getSpec());
		}
		return false;
	}
}
