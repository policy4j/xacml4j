package com.artagon.xacml.policy.function;

import com.artagon.xacml.policy.BagOfAttributes;
import com.artagon.xacml.policy.function.annotations.XacmlFunc;
import com.artagon.xacml.policy.function.annotations.XacmlFuncParam;
import com.artagon.xacml.policy.function.annotations.XacmlFuncReturnType;
import com.artagon.xacml.policy.type.DataTypes;
import com.artagon.xacml.policy.type.IntegerType;
import com.artagon.xacml.policy.type.BooleanType.BooleanValue;

public class TestFunctions 
{
	@XacmlFunc(id="test1")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue test1(
			@XacmlFuncParam(type=DataTypes.INTEGER)IntegerType.IntegerValue a, 
			@XacmlFuncParam(type=DataTypes.INTEGER)IntegerType.IntegerValue b)
	{
		return DataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="test2")
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static IntegerType.IntegerValue test2(
			@XacmlFuncParam(type=DataTypes.INTEGER, isBag=true)BagOfAttributes<IntegerType.IntegerValue> bag)
	{
		return DataTypes.INTEGER.create(bag.size());
	}
}
