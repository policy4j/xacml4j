package com.artagon.xacml.policy.function;

import com.artagon.xacml.policy.type.BooleanType;
import com.artagon.xacml.policy.type.IntegerType;
import com.artagon.xacml.policy.type.XacmlDataType;
import com.artagon.xacml.policy.type.BooleanType.BooleanValue;

public class TestClass 
{
	
	@XacmlStandartFunctionDef(functionId=XacmlFunction.INTEGER_EQUAL)
	public BooleanValue eq(
			@XacmlFuncSimpleArg(type=XacmlDataType.INTEGER)IntegerType.IntegerValue a, 
			@XacmlFuncSimpleArg(type=XacmlDataType.INTEGER)IntegerType.IntegerValue b)
	{
		return XacmlDataType.BOOLEAN.create(Boolean.FALSE);
	}
	
	@XacmlStandartFunctionDef(functionId=XacmlFunction.BOOLEAN_EQUAL)
	public BooleanValue eq(
			@XacmlFuncSimpleArg(type=XacmlDataType.BOOLEAN)BooleanType.BooleanValue a, 
			@XacmlFuncSimpleArg(type=XacmlDataType.BOOLEAN)BooleanType.BooleanValue b)
	{
		return XacmlDataType.BOOLEAN.create(Boolean.FALSE);
	}
}
