package com.artagon.xacml.policy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.DataTypes;
import com.artagon.xacml.Functions;
import com.artagon.xacml.policy.type.BooleanType;
import com.artagon.xacml.policy.type.IntegerType;

public class ApplyTest extends XacmlPolicyTestCase 
{
	private List<Expression> params2Args;
	private List<Expression> params1Args;
	private FunctionSpec function;
	private IntegerType paramType;
	private BooleanType booleanType;
	
	@Before
	public void init(){
		this.paramType = dataTypes.getDataType(DataTypes.INTEGER);
		this.booleanType = dataTypes.getDataType(DataTypes.BOOLEAN);
		this.params2Args = new LinkedList<Expression>();
		this.params2Args.add(paramType.create(10L));
		this.params2Args.add(paramType.create(11L));
		this.params1Args = new LinkedList<Expression>();
		ExplicitFunctionSpecBuilder b = new ExplicitFunctionSpecBuilder(Functions.INTEGER_EQUAL);
		b.withParam(paramType).withParam(paramType).withReturnType(booleanType);
		this.function = b.build(new MockFunctionImplementation(booleanType.create(Boolean.FALSE)));
	}
	
	@Test
	public void testApplyWithValidFunctionAndValidParameters() throws PolicyEvaluationException
	{
		Apply apply = new Apply(function, params2Args);
		Value v = apply.evaluate(context);
		assertNotNull(v);
		assertEquals(booleanType.create(Boolean.FALSE), v);
	}
	
	@Test(expected=PolicyEvaluationIndeterminateException.class)
	public void testApplyWithValidFunctionAndInValidParameters() throws PolicyEvaluationException
	{
		Apply apply = new Apply(function, params1Args);
		apply.evaluate(context);
	}
}
