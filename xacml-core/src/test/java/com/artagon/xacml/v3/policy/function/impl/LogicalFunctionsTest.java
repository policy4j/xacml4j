package com.artagon.xacml.v3.policy.function.impl;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import com.artagon.xacml.v3.policy.EvaluationException;
import com.artagon.xacml.v3.policy.XacmlPolicyTestCase;
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
	}
}
