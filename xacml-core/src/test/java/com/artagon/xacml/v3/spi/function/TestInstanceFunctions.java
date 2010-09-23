package com.artagon.xacml.v3.spi.function;


import org.junit.Ignore;

import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.types.BooleanType;
import com.artagon.xacml.v3.types.BooleanValue;
import com.artagon.xacml.v3.types.IntegerType;
import com.artagon.xacml.v3.types.IntegerValue;

@XacmlFunctionProvider(description="TestInstanceFunctions")
@Ignore
public class TestInstanceFunctions 
{
	@XacmlFuncSpec(id="test1")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public BooleanValue test1(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerValue a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerValue b) 
	{
		return BooleanType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFuncSpec(id="test2")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public IntegerValue test2(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)BagOfAttributeValues bag)
	{
		return IntegerType.INTEGER.create(bag.size());
	}
}

