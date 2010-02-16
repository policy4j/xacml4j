package com.artagon.xacml.v3.policy.function.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.artagon.xacml.v3.policy.EvaluationException;
import com.artagon.xacml.v3.policy.FunctionFactory;
import com.artagon.xacml.v3.policy.FunctionSpec;
import com.artagon.xacml.v3.policy.XacmlPolicyTestCase;
import com.artagon.xacml.v3.policy.function.AnnotationBasedFunctionFactory;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.artagon.xacml.v3.policy.type.IntegerType;

public class LogicalFunctionsTest extends XacmlPolicyTestCase
{
	@Test
	public void basicAndFunctionTest() throws EvaluationException
	{
		assertEquals(DataTypes.BOOLEAN.create(false), 
				LogicalFunctions.and(context, DataTypes.BOOLEAN.create(false), DataTypes.BOOLEAN.create(false)));
		assertEquals(DataTypes.BOOLEAN.create(false), 
				LogicalFunctions.and(context, DataTypes.BOOLEAN.create(true), DataTypes.BOOLEAN.create(false)));
		assertEquals(DataTypes.BOOLEAN.create(true), 
				LogicalFunctions.and(context, DataTypes.BOOLEAN.create(true), DataTypes.BOOLEAN.create(true)));
	}
	
	@Test
	public void testBasicOrFunction() throws EvaluationException
	{
		assertEquals(DataTypes.BOOLEAN.create(false), 
				LogicalFunctions.or(context, DataTypes.BOOLEAN.create(false), DataTypes.BOOLEAN.create(false)));
		assertEquals(DataTypes.BOOLEAN.create(true), 
				LogicalFunctions.or(context, DataTypes.BOOLEAN.create(true), DataTypes.BOOLEAN.create(false)));
		assertEquals(DataTypes.BOOLEAN.create(true), 
				LogicalFunctions.or(context, DataTypes.BOOLEAN.create(true), DataTypes.BOOLEAN.create(true)));
	}
	
	@Test
	public void testNOfFunction() throws EvaluationException
	{
		IntegerType.IntegerValue n = DataTypes.INTEGER.create(0);
		assertEquals(DataTypes.BOOLEAN.create(true), 
				LogicalFunctions.nof(context, n));
		n = DataTypes.INTEGER.create(1);
		assertEquals(DataTypes.BOOLEAN.create(true), 
				LogicalFunctions.nof(context, n, DataTypes.BOOLEAN.create(true)));
		assertEquals(DataTypes.BOOLEAN.create(false), 
				LogicalFunctions.nof(context, n, DataTypes.BOOLEAN.create(false)));
		n = DataTypes.INTEGER.create(2);
		assertEquals(DataTypes.BOOLEAN.create(true), 
				LogicalFunctions.nof(context, n, 
						DataTypes.BOOLEAN.create(true), DataTypes.BOOLEAN.create(true)));
		assertEquals(DataTypes.BOOLEAN.create(false), 
				LogicalFunctions.nof(context, n, 
						DataTypes.BOOLEAN.create(true), DataTypes.BOOLEAN.create(false)));
	}
	
	@Test(expected=EvaluationException.class)
	public void testNOfFunctionInderterminate() throws EvaluationException
	{
		IntegerType.IntegerValue n = DataTypes.INTEGER.create(4);
		 
		assertEquals(DataTypes.BOOLEAN.create(false), 
				LogicalFunctions.nof(context, n, DataTypes.BOOLEAN.create(false)));
	}
	
	
	@Test
	public void testParseFunctionViaAnnotiationFactory() throws EvaluationException
	{
		FunctionFactory f = new AnnotationBasedFunctionFactory(LogicalFunctions.class);
		FunctionSpec andFunc = f.getFunction("urn:oasis:names:tc:xacml:1.0:function:and");
		FunctionSpec orFunc = f.getFunction("urn:oasis:names:tc:xacml:1.0:function:or");
		FunctionSpec notFunc = f.getFunction("urn:oasis:names:tc:xacml:1.0:function:not");
		
		assertEquals(DataTypes.BOOLEAN.create(Boolean.TRUE),  andFunc.invoke(context, DataTypes.BOOLEAN.create(Boolean.TRUE), DataTypes.BOOLEAN.create(Boolean.TRUE)));
		assertEquals(DataTypes.BOOLEAN.create(Boolean.TRUE),  orFunc.invoke(context, DataTypes.BOOLEAN.create(Boolean.TRUE), DataTypes.BOOLEAN.create(Boolean.FALSE)));
		assertEquals(DataTypes.BOOLEAN.create(Boolean.FALSE),  notFunc.invoke(context, DataTypes.BOOLEAN.create(Boolean.TRUE)));
	}
}
