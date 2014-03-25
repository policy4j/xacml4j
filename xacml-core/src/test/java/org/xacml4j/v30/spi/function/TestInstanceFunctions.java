package org.xacml4j.v30.spi.function;


import org.junit.Ignore;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.types.BooleanExp;
import org.xacml4j.v30.types.IntegerExp;


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
		return BooleanExp.valueOf(a.equals(b));
	}

	@XacmlFuncSpec(id="test2")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public IntegerExp test2(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)BagOfAttributeExp bag)
	{
		return IntegerExp.valueOf(bag.size());
	}
}

