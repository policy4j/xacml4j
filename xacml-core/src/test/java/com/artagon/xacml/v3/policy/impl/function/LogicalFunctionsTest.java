package com.artagon.xacml.v3.policy.impl.function;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.policy.EvaluationException;
import com.artagon.xacml.v3.policy.FunctionSpec;
import com.artagon.xacml.v3.policy.XacmlPolicyTestCase;
import com.artagon.xacml.v3.policy.spi.FunctionProvider;
import com.artagon.xacml.v3.policy.spi.function.ReflectionBasedFunctionProvider;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.artagon.xacml.v3.policy.type.IntegerType;

public class LogicalFunctionsTest extends XacmlPolicyTestCase
{
	private FunctionProvider f;
	
	FunctionSpec andFunc;
	FunctionSpec orFunc;
	FunctionSpec notFunc;
	
	@Before
	public void init(){
		this.f = new ReflectionBasedFunctionProvider(LogicalFunctions.class);
		this.andFunc = f.getFunction("urn:oasis:names:tc:xacml:1.0:function:and");
		this.orFunc = f.getFunction("urn:oasis:names:tc:xacml:1.0:function:or");
		this.notFunc = f.getFunction("urn:oasis:names:tc:xacml:1.0:function:not");
	}
	
	
	@Test
	public void testAndFunction() throws EvaluationException
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
}
