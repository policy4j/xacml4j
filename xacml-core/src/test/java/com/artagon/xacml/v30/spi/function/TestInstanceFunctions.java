package com.artagon.xacml.v30.spi.function;


import org.junit.Ignore;

import com.artagon.xacml.v30.pdp.BagOfAttributeExp;
import com.artagon.xacml.v30.types.BooleanExp;
import com.artagon.xacml.v30.types.BooleanType;
import com.artagon.xacml.v30.types.IntegerExp;
import com.artagon.xacml.v30.types.IntegerType;

@XacmlFunctionProvider(description="TestInstanceFunctions")
@Ignore
public class TestInstanceFunctions 
{
	@XacmlFuncSpec(id="test1")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public BooleanExp test1(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp b) 
	{
		return BooleanType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFuncSpec(id="test2")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public IntegerExp test2(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)BagOfAttributeExp bag)
	{
		return IntegerType.INTEGER.create(bag.size());
	}
}

