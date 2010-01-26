package com.artagon.xacml.policy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.policy.function.DefaultFunctionSpecBuilder;
import com.artagon.xacml.policy.type.BooleanType;
import com.artagon.xacml.policy.type.DataTypes;
import com.artagon.xacml.policy.type.IntegerType;

public class ApplyTest extends XacmlPolicyTestCase 
{
	private FunctionSpec function;
	private IntegerType paramType;
	private BooleanType booleanType;
	
	@Before
	public void init(){
		this.paramType = DataTypes.INTEGER.getType();
		this.booleanType = DataTypes.BOOLEAN.getType();


		DefaultFunctionSpecBuilder b = new DefaultFunctionSpecBuilder("test1");
		b.withParam(paramType).withParam(paramType);
		this.function = b.build(new MockFunctionImplementation(booleanType.create(Boolean.FALSE)));
	}
	
	@Test
	public void testApplyWithValidFunctionAndValidParameters() throws EvaluationException
	{
		Apply apply = function.createApply(paramType.create(10L), paramType.create(11L));
		Value v = apply.evaluate(context);
		assertNotNull(v);
		assertEquals(booleanType.create(Boolean.FALSE), v);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testApplyWithValidFunctionAndInValidParameters() throws EvaluationException
	{
		Apply apply = function.createApply(paramType.create(10L));
		apply.evaluate(context);
	}
}
