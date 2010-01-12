package com.artagon.xacml.policy.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.DataTypes;
import com.artagon.xacml.Functions;
import com.artagon.xacml.policy.FunctionFactory;
import com.artagon.xacml.policy.FunctionImplementation;
import com.artagon.xacml.policy.BaseFunctionSpec;
import com.artagon.xacml.policy.PolicyEvaluationException;
import com.artagon.xacml.policy.type.BooleanType;

public class EqualFunctionFactoryTest extends DefaultFunctionFactoryTestCase
{
	private FunctionFactory factory;
	
	@Before
	public void init(){
		this.factory = new EqualFunctionFactory(types);
	}
	
	@Test
	public void testAllFunctions(){
		assertNotNull(factory.getFunction(Functions.ANYURI_EQUAL));
		assertNotNull(factory.getFunction(Functions.BOOLEAN_EQUAL));
	}
	
	@Test
	public void testBooleanEquals() throws PolicyEvaluationException
	{
		BooleanType type = dataTypes.getDataType(DataTypes.BOOLEAN);
		BaseFunctionSpec spec = factory.getFunction(Functions.BOOLEAN_EQUAL);
		FunctionImplementation func = spec.getImplementation();
		assertEquals(type.create(Boolean.TRUE), func.invoke(context, type.create(Boolean.FALSE), type.create(Boolean.FALSE)));
		assertEquals(type.create(Boolean.FALSE), func.invoke(context, type.create(Boolean.TRUE), type.create(Boolean.FALSE)));
	}
}
