package org.xacml4j.v30.pdp;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.types.IntegerExp;

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

	@Test(expected=EvaluationException.class)
	public void testApplyEvaluationFunctionThrowsRuntimeException() throws XacmlException
	{
		List<Expression> params = ImmutableList.<Expression>builder()
		.add(IntegerExp.valueOf(10L))
		.build();
		expect(function.invoke(context, params))
		.andThrow(new IllegalArgumentException());
		replay(function);
		Apply apply = Apply.builder(function).param(IntegerExp.valueOf(10L)).build();
		apply.evaluate(context);
		verify(function);
	}

	@Test(expected=FunctionInvocationException.class)
	public void testApplyEvaluationFunctionThrowsFunctionInvocationException() throws XacmlException
	{
		List<Expression> params = ImmutableList.<Expression>builder()
		.add(IntegerExp.valueOf(10L))
		.build();
		expect(function.invoke(context, params)).
		andThrow(new FunctionInvocationException(context, function, new IllegalArgumentException()));
		replay(function);
		Apply apply = Apply.builder(function).param(IntegerExp.valueOf(10L)).build();
		apply.evaluate(context);
		verify(function);
	}
}
