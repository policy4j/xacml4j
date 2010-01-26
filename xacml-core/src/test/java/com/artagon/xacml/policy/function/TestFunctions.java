package com.artagon.xacml.policy.function;

import com.artagon.xacml.policy.BagOfAttributes;
import com.artagon.xacml.policy.function.annotations.XacmlFunc;
import com.artagon.xacml.policy.function.annotations.XacmlFuncParam;
import com.artagon.xacml.policy.function.annotations.XacmlFuncReturnType;
import com.artagon.xacml.policy.type.IntegerType;
import com.artagon.xacml.policy.type.XacmlDataType;
import com.artagon.xacml.policy.type.BooleanType.BooleanValue;

public class TestFunctions 
{
	@XacmlFunc(id="test1")
	@XacmlFuncReturnType(type=XacmlDataType.BOOLEAN)
	public static BooleanValue test1(
			@XacmlFuncParam(type=XacmlDataType.INTEGER)IntegerType.IntegerValue a, 
			@XacmlFuncParam(type=XacmlDataType.INTEGER)IntegerType.IntegerValue b)
	{
		return XacmlDataType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="test2")
	@XacmlFuncReturnType(type=XacmlDataType.INTEGER)
	public static IntegerType.IntegerValue test2(
			@XacmlFuncParam(type=XacmlDataType.INTEGER, isBag=true)BagOfAttributes<IntegerType.IntegerValue> bag)
	{
		return XacmlDataType.INTEGER.create(bag.size());
	}
}
