package com.artagon.xacml.v30.pdp;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.EvaluationException;
import com.artagon.xacml.v30.ValueExpression;
import com.artagon.xacml.v30.types.BooleanType;
import com.artagon.xacml.v30.types.IntegerType;
import com.google.common.collect.ImmutableList;

public class ApplyTest 
{
	private FunctionSpec function;
	private EvaluationContext context;
	
	@Before
	public void init(){
		this.function = createStrictMock(FunctionSpec.class);
		this.context = createStrictMock(EvaluationContext.class);
	}
	
	@Test
	public void testApplyEvaluationWithValidFunctionAndValidParameters() throws XacmlException
	{
		List<Expression> params = ImmutableList.<Expression>builder()
		.add(IntegerType.INTEGER.create(10L))
		.add(IntegerType.INTEGER.create(11L))
		.build();
		expect(function.validateParameters(params)).andReturn(true);
		replay(function);
		Apply apply = Apply.builder(function).params(params).build();
		verify(function);
		reset(function);
		expect(function.invoke(context, params)).andReturn(BooleanType.BOOLEAN.create(false));
		replay(function);
		ValueExpression v = apply.evaluate(context);
		assertNotNull(v);
		assertEquals(BooleanType.BOOLEAN.create(false), v);
		verify(function);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCreateApplyWithValidFunctionAndInvalidParameters() throws XacmlException
	{
		List<Expression> params = ImmutableList.<Expression>builder()
		.add(IntegerType.INTEGER.create(10L))
		.build();
		expect(function.validateParameters(params)).andReturn(false);
		expect(function.getId()).andReturn("testId");
		replay(function);
		Apply.builder(function).param(IntegerType.INTEGER.create(10L)).build();
		verify(function);
	}
	
	@Test(expected=EvaluationException.class)
	public void testApplyEvaluationFunctionThrowsRuntimeException() throws XacmlException
	{
		List<Expression> params = ImmutableList.<Expression>builder()
		.add(IntegerType.INTEGER.create(10L))
		.build();
		expect(function.validateParameters(params)).andReturn(true);
		expect(function.invoke(context, params))
		.andThrow(new IllegalArgumentException());
		replay(function);
		Apply apply = Apply.builder(function).param(IntegerType.INTEGER.create(10L)).build();
		apply.evaluate(context);
		verify(function);
	}
	
	@Test(expected=EvaluationException.class)
	public void testApplyEvaluationFunctionParamValidationFails() throws XacmlException
	{
		List<Expression> params = ImmutableList.<Expression>builder()
		.add(IntegerType.INTEGER.create(10L))
		.build();
		expect(function.validateParameters(params)).andReturn(true);
		expect(function.invoke(context, params))
		.andThrow(new IllegalArgumentException());
		replay(function);
		Apply apply = Apply.builder(function).param(IntegerType.INTEGER.create(10L)).build();
		apply.evaluate(context);
		verify(function);
	}
	
	@Test(expected=FunctionInvocationException.class)
	public void testApplyEvaluationFunctionThrowsFunctionInvocationException() throws XacmlException
	{
		List<Expression> params = ImmutableList.<Expression>builder()
		.add(IntegerType.INTEGER.create(10L))
		.build();
		expect(function.validateParameters(params)).andReturn(true);
		expect(function.invoke(context, params)).
		andThrow(new FunctionInvocationException(context, function, new IllegalArgumentException()));
		replay(function);
		Apply apply = Apply.builder(function).param(IntegerType.INTEGER.create(10L)).build();
		apply.evaluate(context);
		verify(function);
	}
}
