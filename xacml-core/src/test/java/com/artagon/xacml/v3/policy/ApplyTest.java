package com.artagon.xacml.v3.policy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.policy.function.DefaultFunctionSpecBuilder;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.artagon.xacml.v3.policy.type.BooleanType.BooleanValue;

public class ApplyTest extends XacmlPolicyTestCase 
{
	private FunctionSpec function;
	
	@Before
	public void init(){
		BooleanValue expectedReturn = DataTypes.BOOLEAN.create(Boolean.FALSE);
		DefaultFunctionSpecBuilder b = new DefaultFunctionSpecBuilder("test1");
		b.withParam(DataTypes.INTEGER.getType()).withParam(DataTypes.INTEGER.getType());
		this.function = b.build(DataTypes.BOOLEAN.getType(), new MockFunctionImplementation<BooleanValue>(expectedReturn));
	}
	
	@Test
	public void testApplyWithValidFunctionAndValidParameters() throws EvaluationException
	{
		Apply apply = function.createApply(DataTypes.INTEGER.create(10L), DataTypes.INTEGER.create(11L));
		Value v = apply.evaluate(context);
		assertNotNull(v);
		assertEquals(DataTypes.BOOLEAN.create(Boolean.FALSE), v);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testApplyWithValidFunctionAndInValidParameters() throws EvaluationException
	{
		Apply apply = function.createApply(DataTypes.INTEGER.create(10L));
		apply.evaluate(context);
	}
}
