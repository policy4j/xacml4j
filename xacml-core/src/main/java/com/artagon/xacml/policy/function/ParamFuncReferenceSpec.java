package com.artagon.xacml.policy.function;

import java.util.ListIterator;

import com.artagon.xacml.policy.Expression;
import com.artagon.xacml.policy.FunctionFamily;
import com.artagon.xacml.policy.FunctionReferenceExpression;
import com.artagon.xacml.policy.ParamSpec;
import com.artagon.xacml.policy.ValueType;
import com.artagon.xacml.util.Preconditions;

public class ParamFuncReferenceSpec implements ParamSpec
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
