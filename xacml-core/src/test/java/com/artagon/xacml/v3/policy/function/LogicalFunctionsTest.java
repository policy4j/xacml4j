package com.artagon.xacml.v3.policy.function;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.FunctionSpec;
import com.artagon.xacml.v3.XacmlPolicyTestCase;
import com.artagon.xacml.v3.policy.spi.FunctionProvider;
import com.artagon.xacml.v3.policy.spi.function.AnnotiationBasedFunctionProvider;
import com.artagon.xacml.v3.types.IntegerType;
import com.artagon.xacml.v3.types.XacmlDataTypes;

public class LogicalFunctionsTest extends XacmlPolicyTestCase
{
	private FunctionProvider f;
	
	FunctionSpec andFunc;
	FunctionSpec orFunc;
	FunctionSpec notFunc;
	
	@Before
	public void init(){
		this.f = new AnnotiationBasedFunctionProvider(LogicalFunctions.class);
		this.andFunc = f.getFunction("urn:oasis:names:tc:xacml:1.0:function:and");
		this.orFunc = f.getFunction("urn:oasis:names:tc:xacml:1.0:function:or");
		this.notFunc = f.getFunction("urn:oasis:names:tc:xacml:1.0:function:not");
	}
	
	
	@Test
	public void testAndFunction() throws EvaluationException
	{
		assertEquals(XacmlDataTypes.BOOLEAN.create(false), 
				LogicalFunctions.and(context, XacmlDataTypes.BOOLEAN.create(false), XacmlDataTypes.BOOLEAN.create(false)));
		assertEquals(XacmlDataTypes.BOOLEAN.create(false), 
				LogicalFunctions.and(context, XacmlDataTypes.BOOLEAN.create(true), XacmlDataTypes.BOOLEAN.create(false)));
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), 
				LogicalFunctions.and(context, XacmlDataTypes.BOOLEAN.create(true), XacmlDataTypes.BOOLEAN.create(true)));
	}
	
	@Test
	public void testBasicOrFunction() throws EvaluationException
	{
		assertEquals(XacmlDataTypes.BOOLEAN.create(false), 
				LogicalFunctions.or(context, XacmlDataTypes.BOOLEAN.create(false), XacmlDataTypes.BOOLEAN.create(false)));
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), 
				LogicalFunctions.or(context, XacmlDataTypes.BOOLEAN.create(true), XacmlDataTypes.BOOLEAN.create(false)));
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), 
				LogicalFunctions.or(context, XacmlDataTypes.BOOLEAN.create(true), XacmlDataTypes.BOOLEAN.create(true)));
	}
	
	@Test
	public void testNOfFunction() throws EvaluationException
	{
		IntegerType.IntegerValue n = XacmlDataTypes.INTEGER.create(0);
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), 
				LogicalFunctions.nof(context, n));
		n = XacmlDataTypes.INTEGER.create(1);
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), 
				LogicalFunctions.nof(context, n, XacmlDataTypes.BOOLEAN.create(true)));
		assertEquals(XacmlDataTypes.BOOLEAN.create(false), 
				LogicalFunctions.nof(context, n, XacmlDataTypes.BOOLEAN.create(false)));
		n = XacmlDataTypes.INTEGER.create(2);
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), 
				LogicalFunctions.nof(context, n, 
						XacmlDataTypes.BOOLEAN.create(true), XacmlDataTypes.BOOLEAN.create(true)));
		assertEquals(XacmlDataTypes.BOOLEAN.create(false), 
				LogicalFunctions.nof(context, n, 
						XacmlDataTypes.BOOLEAN.create(true), XacmlDataTypes.BOOLEAN.create(false)));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testNOfFunctionInderterminate() throws EvaluationException
	{
		IntegerType.IntegerValue n = XacmlDataTypes.INTEGER.create(4);
		 
		assertEquals(XacmlDataTypes.BOOLEAN.create(false), 
				LogicalFunctions.nof(context, n, XacmlDataTypes.BOOLEAN.create(false)));
	}
}
