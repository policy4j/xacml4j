package org.xacml4j.v30.pdp;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

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
		andThrow(new FunctionInvocationException(function, new IllegalArgumentException()));
		replay(function);
		Apply apply = Apply.builder(function).param(IntegerExp.valueOf(10L)).build();
		apply.evaluate(context);
		verify(function);
	}
}
