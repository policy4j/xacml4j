package com.artagon.xacml.policy.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.policy.FunctionFactory;
import com.artagon.xacml.policy.FunctionSpec;
import com.artagon.xacml.policy.PolicyEvaluationException;
import com.artagon.xacml.policy.type.AnyURIType;
import com.artagon.xacml.policy.type.BooleanType;
import com.artagon.xacml.policy.type.XacmlDataType;
import com.artagon.xacml.policy.type.StringType;

public class EqualFunctionFactoryTest extends DefaultFunctionFactoryTestCase
{
	private FunctionFactory factory;
	
	@Before
	public void init(){
		this.factory = new XacmlEqualFunctionFactory();
	}
	
	@Test
	public void testAllFunctions(){
		assertNotNull(factory.getFunction(XacmlFunction.ANYURI_EQUAL.getXacmlId()));
		assertNotNull(factory.getFunction(XacmlFunction.BOOLEAN_EQUAL.getXacmlId()));
		assertNotNull(factory.getFunction(XacmlFunction.STRING_EQUAL.getXacmlId()));
		assertNotNull(factory.getFunction(XacmlFunction.INTEGER_EQUAL.getXacmlId()));
		assertNotNull(factory.getFunction(XacmlFunction.DOUBLE_EQUAL.getXacmlId()));
		assertNotNull(factory.getFunction(XacmlFunction.BASE64BINARY_EQUAL.getXacmlId()));
		assertNotNull(factory.getFunction(XacmlFunction.HEXBINARY_EQUAL.getXacmlId()));
		assertNotNull(factory.getFunction(XacmlFunction.RFC833NAME_EQUAL.getXacmlId()));
		assertNotNull(factory.getFunction(XacmlFunction.DATE_EQUAL.getXacmlId()));
		assertNotNull(factory.getFunction(XacmlFunction.DATETIME_EQUAL.getXacmlId()));
		assertNotNull(factory.getFunction(XacmlFunction.DAYTIMEDURATION_EQUAL.getXacmlId()));
		assertNotNull(factory.getFunction(XacmlFunction.YEARMONTHDURATION_EQUAL.getXacmlId()));
		assertNotNull(factory.getFunction(XacmlFunction.TIME_EQUAL.getXacmlId()));
	}
	
	@Test
	public void testBooleanEquals() throws PolicyEvaluationException
	{
		BooleanType type = XacmlDataType.BOOLEAN.getType();
		FunctionSpec spec = factory.getFunction(XacmlFunction.BOOLEAN_EQUAL.getXacmlId());
		assertNotNull(spec);
		assertEquals(type.create(Boolean.TRUE), spec.invoke(context, type.create(Boolean.FALSE), type.create(Boolean.FALSE)));
		assertEquals(type.create(Boolean.FALSE), spec.invoke(context, type.create(Boolean.TRUE), type.create(Boolean.FALSE)));
	}
	
	@Test
	public void testAnyURIEquals() throws Exception
	{
		AnyURIType type = XacmlDataType.ANYURI.getType();
		BooleanType typeBool = XacmlDataType.BOOLEAN.getType();
		FunctionSpec spec = factory.getFunction(XacmlFunction.ANYURI_EQUAL.getXacmlId());
		assertEquals(typeBool.create(Boolean.TRUE), spec.invoke(context, type.create(new URI("test")), type.create(new URI("test"))));
		assertEquals(typeBool.create(Boolean.FALSE), spec.invoke(context, type.create(new URI("test1")), type.create(new URI("test2"))));
	}
	
	@Test
	public void testStringEquals() throws Exception
	{
		StringType type = XacmlDataType.STRING.getType();
		BooleanType typeBool = XacmlDataType.BOOLEAN.getType();
		FunctionSpec spec = factory.getFunction(XacmlFunction.STRING_EQUAL.getXacmlId());
		assertEquals(typeBool.create(Boolean.TRUE), spec.invoke(context, type.create("test"), type.create("test")));
		assertEquals(typeBool.create(Boolean.FALSE), spec.invoke(context, type.create("test1"), type.create("test2")));
	}
}
