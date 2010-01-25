package com.artagon.xacml.policy.function;

import com.artagon.xacml.policy.BagOfAttributes;
import com.artagon.xacml.policy.function.annotations.XacmlFuncSpec;
import com.artagon.xacml.policy.function.annotations.XacmlFuncArg;
import com.artagon.xacml.policy.function.annotations.XacmlFuncVarArg;
import com.artagon.xacml.policy.type.BooleanType;
import com.artagon.xacml.policy.type.IntegerType;
import com.artagon.xacml.policy.type.StringType;
import com.artagon.xacml.policy.type.XacmlDataType;
import com.artagon.xacml.policy.type.BooleanType.BooleanValue;

public class XacmlEqualPredicates 
{
	
	@XacmlFuncSpec(functionId="test")
	public static BooleanValue eq(
			@XacmlFuncArg(type=XacmlDataType.INTEGER)IntegerType.IntegerValue a, 
			@XacmlFuncArg(type=XacmlDataType.INTEGER)IntegerType.IntegerValue b)
	{
		return XacmlDataType.BOOLEAN.create(Boolean.FALSE);
	}
	
	@XacmlFuncSpec(functionId="test")
	public static BooleanValue eq(
			@XacmlFuncArg(type=XacmlDataType.BOOLEAN)BooleanType.BooleanValue a, 
			@XacmlFuncArg(type=XacmlDataType.BOOLEAN)BooleanType.BooleanValue b)
	{
		return XacmlDataType.BOOLEAN.create(Boolean.FALSE);
	}
	
	@XacmlFuncSpec(functionId="test")
	public static BooleanValue eq(
			@XacmlFuncVarArg(
					type=XacmlDataType.INTEGER, min=2, max=Integer.MAX_VALUE)IntegerType.IntegerValue ...values)
	{
		Long sum = 0L;
		for(IntegerType.IntegerValue v : values){
			sum += v.getValue();
		}
		return XacmlDataType.INTEGER.create(sum);
	}
	
	public static IntegerType.IntegerValue count(
			@XacmlFuncArg(type=XacmlDataType.STRING, isBag=true)BagOfAttributes<StringType.StringValue> bag)
	{
		return XacmlDataType.INTEGER.create(0);
	}
	

}
