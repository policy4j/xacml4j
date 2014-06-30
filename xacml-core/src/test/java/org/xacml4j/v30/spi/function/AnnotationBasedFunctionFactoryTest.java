package org.xacml4j.v30.spi.function;

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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.pdp.FunctionSpec;
import org.xacml4j.v30.types.BooleanExp;
import org.xacml4j.v30.types.IntegerExp;



public class AnnotationBasedFunctionFactoryTest
{
	private FunctionProvider f1;
	private FunctionProvider f2;
	private EvaluationContext context;

	@Before
	public void init() throws Exception
	{
		this.f1 = new AnnotationBasedFunctionProvider(TestFunctions.class);
		this.f2 = new AnnotationBasedFunctionProvider(new TestInstanceFunctions());
		this.context = createStrictMock(EvaluationContext.class);
	}

	@Test
	public void testTest1And2FunctionsStaticProvider() throws Exception
	{
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false).times(2);
		replay(context);
		FunctionSpec spec1 = f1.getFunction("test1");
		assertEquals(BooleanExp.valueOf(Boolean.FALSE),
				spec1.invoke(context, IntegerExp.valueOf(1), IntegerExp.valueOf(2)));

		FunctionSpec spec2 = f1.getFunction("test2");
		assertEquals(IntegerExp.valueOf(2),
				spec2.invoke(context, IntegerExp.bag().value(1, 2).build()));
		verify(context);

	}

	@Test
	public void testTest1And2FunctionsInstanceProvider() throws Exception
	{
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false).times(2);
		replay(context);
		FunctionSpec spec1 = f2.getFunction("test1");
		assertNotNull(spec1);
		assertEquals(BooleanExp.valueOf(Boolean.FALSE),
				spec1.invoke(context, IntegerExp.valueOf(1), IntegerExp.valueOf(2)));

		FunctionSpec spec2 = f2.getFunction("test2");
		assertEquals(IntegerExp.valueOf(2),
				spec2.invoke(context, IntegerExp.bag().value(1, 2).build()));
		verify(context);

	}

	@Test
	public void testLazyParamEvaluationPassingEvaluationContext() throws Exception
	{
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false).times(3);
		replay(context);
		FunctionSpec spec3 = f1.getFunction("test3");
		FunctionSpec spec4 = f1.getFunction("test4");
		spec3.invoke(context, IntegerExp.valueOf(10), IntegerExp.valueOf(10));
		spec3.invoke(context, IntegerExp.valueOf(10));
		spec4.invoke(context, IntegerExp.valueOf(10));
		verify(context);

	}

	@Test
	public void testVarArgFunctionInvocation() throws Exception
	{
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true).times(6);
		replay(context);
		FunctionSpec spec5 = f1.getFunction("test5VarArg");
		FunctionSpec spec6 = f1.getFunction("test6VarArg");

		spec5.invoke(context, IntegerExp.valueOf(10));
		spec5.invoke(context, IntegerExp.valueOf(10), BooleanExp.valueOf(false));
		spec5.invoke(context, IntegerExp.valueOf(10), BooleanExp.valueOf(false), BooleanExp.valueOf(false));
		spec5.invoke(context, IntegerExp.valueOf(10), BooleanExp.valueOf(false), BooleanExp.valueOf(false), BooleanExp.valueOf(false));

		spec6.invoke(context, IntegerExp.valueOf(10), IntegerExp.valueOf(10));
		spec6.invoke(context, IntegerExp.valueOf(10), IntegerExp.valueOf(10), BooleanExp.valueOf(false));
		verify(context);

	}

}
