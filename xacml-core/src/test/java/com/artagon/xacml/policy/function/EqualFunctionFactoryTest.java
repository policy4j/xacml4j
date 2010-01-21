package com.artagon.xacml.policy.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.Functions;
import com.artagon.xacml.policy.FunctionFactory;
import com.artagon.xacml.policy.FunctionSpec;
import com.artagon.xacml.policy.PolicyEvaluationException;
import com.artagon.xacml.policy.type.AnyURIType;
import com.artagon.xacml.policy.type.BooleanType;
import com.artagon.xacml.policy.type.DataTypes;
import com.artagon.xacml.policy.type.StringType;

public class EqualFunctionFactoryTest extends DefaultFunctionFactoryTestCase
{
	private FunctionFactory factory;
	
	@Before
	public void init(){
		this.factory = new EqualFunctionFactory();
	}
	
	@Test
	public void testAllFunctions(){
		assertNotNull(factory.getFunction(Functions.ANYURI_EQUAL));
		assertNotNull(factory.getFunction(Functions.BOOLEAN_EQUAL));
		assertNotNull(factory.getFunction(Functions.STRING_EQUAL));
		assertNotNull(factory.getFunction(Functions.INTEGER_EQUAL));
		assertNotNull(factory.getFunction(Functions.DOUBLE_EQUAL));
		assertNotNull(factory.getFunction(Functions.BASE64BINARY_EQUAL));
		assertNotNull(factory.getFunction(Functions.HEXBINARY_EQUAL));
		assertNotNull(factory.getFunction(Functions.RFC833NAME_EQUAL));
		assertNotNull(factory.getFunction(Functions.DATE_EQUAL));
		assertNotNull(factory.getFunction(Functions.DATETIME_EQUAL));
		assertNotNull(factory.getFunction(Functions.DAYTIMEDURATION_EQUAL));
		assertNotNull(factory.getFunction(Functions.YEARMONTHDURATION_EQUAL));
		assertNotNull(factory.getFunction(Functions.TIME_EQUAL));
	}
	
	@Test
	public void testBooleanEquals() throws PolicyEvaluationException
	{
		BooleanType type = DataTypes.BOOLEAN.getType();
		FunctionSpec spec = factory.getFunction(Functions.BOOLEAN_EQUAL);
		assertEquals(type.create(Boolean.TRUE), spec.invoke(context, type.create(Boolean.FALSE), type.create(Boolean.FALSE)));
		assertEquals(type.create(Boolean.FALSE), spec.invoke(context, type.create(Boolean.TRUE), type.create(Boolean.FALSE)));
	}
	
	@Test
	public void testAnyURIEquals() throws Exception
	{
		AnyURIType type = DataTypes.ANYURI.getType();
		BooleanType typeBool = DataTypes.BOOLEAN.getType();
		FunctionSpec spec = factory.getFunction(Functions.ANYURI_EQUAL);
		assertEquals(typeBool.create(Boolean.TRUE), spec.invoke(context, type.create(new URI("test")), type.create(new URI("test"))));
		assertEquals(typeBool.create(Boolean.FALSE), spec.invoke(context, type.create(new URI("test1")), type.create(new URI("test2"))));
	}
	
	@Test
	public void testStringEquals() throws Exception
	{
		StringType type = DataTypes.STRING.getType();
		BooleanType typeBool = DataTypes.BOOLEAN.getType();
		FunctionSpec spec = factory.getFunction(Functions.STRING_EQUAL);
		assertEquals(typeBool.create(Boolean.TRUE), spec.invoke(context, type.create("test"), type.create("test")));
		assertEquals(typeBool.create(Boolean.FALSE), spec.invoke(context, type.create("test1"), type.create("test2")));
	}
}
