package com.artagon.xacml.spring;

import org.junit.Ignore;

import com.artagon.xacml.v3.spi.function.XacmlFuncParam;
import com.artagon.xacml.v3.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v3.spi.function.XacmlFuncSpec;
import com.artagon.xacml.v3.spi.function.XacmlFunctionProvider;
import com.artagon.xacml.v3.types.BooleanType.BooleanValue;
import com.artagon.xacml.v3.types.BooleanType;
import com.artagon.xacml.v3.types.IntegerType;
import com.artagon.xacml.v3.types.XacmlDataTypes;

@Ignore
@XacmlFunctionProvider(description="Provider2")
public class TestStaticFunctions 
{
	@XacmlFuncSpec(id="test1_provider2")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue test1(
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER)IntegerType.IntegerValue a, 
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER)IntegerType.IntegerValue b) 
	{
		return BooleanType.Factory.create(a.equals(b));
	}
	
	
}
