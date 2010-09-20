package com.artagon.xacml.v3.spi.function;


import org.junit.Ignore;

import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.types.BooleanType;
import com.artagon.xacml.v3.types.BooleanType.BooleanValue;
import com.artagon.xacml.v3.types.IntegerType;
import com.artagon.xacml.v3.types.XacmlDataTypes;

@XacmlFunctionProvider(description="TestInstanceFunctions")
@Ignore
public class TestInstanceFunctions 
{
	@XacmlFuncSpec(id="test1")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public BooleanValue test1(
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER)IntegerType.IntegerValue a, 
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER)IntegerType.IntegerValue b) 
	{
		return BooleanType.Factory.create(a.equals(b));
	}
	
	@XacmlFuncSpec(id="test2")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER)
	public IntegerType.IntegerValue test2(
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerType.IntegerValue> bag)
	{
		return IntegerType.Factory.create(bag.size());
	}
}

