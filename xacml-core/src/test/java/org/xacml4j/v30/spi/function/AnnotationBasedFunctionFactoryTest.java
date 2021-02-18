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

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.FunctionProvider;
import org.xacml4j.v30.pdp.FunctionSpec;
import org.xacml4j.v30.types.XacmlTypes;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class AnnotationBasedFunctionFactoryTest
{
	private FunctionProvider f1;
	private FunctionProvider f2;
	private EvaluationContext context;

	@Before
	public void init() throws Exception
	{
		this.f1 = FunctionProvider.emptyBuilder().fromClass(TestFunctions.class).build();
		this.f2 = FunctionProvider.emptyBuilder().fromClass(TestInstanceFunctions.class).build();
		this.context = createStrictMock(EvaluationContext.class);
	}

	@Test
	public void testTest1And2FunctionsStaticProvider()
	{
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false).times(2);
		replay(context);
		FunctionSpec spec1 = f1.getFunction("test1").get();
		assertEquals(XacmlTypes.BOOLEAN.of(Boolean.FALSE),
				spec1.invoke(context, XacmlTypes.INTEGER.of(1), XacmlTypes.INTEGER.of(2)));

		FunctionSpec spec2 = f1.getFunction("test2").get();
		assertEquals(XacmlTypes.INTEGER.of(2),
				spec2.invoke(context, XacmlTypes.INTEGER.bag().value(1, 2).build()));
		verify(context);

	}

	@Test
	public void testTest1And2FunctionsInstanceProvider()
	{
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false).times(2);
		replay(context);
		FunctionSpec spec1 = f2.getFunction("test1").get();
		assertNotNull(spec1);
		assertEquals(XacmlTypes.BOOLEAN.of(Boolean.FALSE),
				spec1.invoke(context, XacmlTypes.INTEGER.of(1), XacmlTypes.INTEGER.of(2)));

		FunctionSpec spec2 = f2.getFunction("test2").get();
		assertEquals(XacmlTypes.INTEGER.of(2),
				spec2.invoke(context, XacmlTypes.INTEGER.bag().value(1, 2).build()));
		verify(context);

	}

	@Test
	public void testLazyParamEvaluationPassingEvaluationContext()
	{
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false).times(3);
		replay(context);
		FunctionSpec spec3 = f1.getFunction("test3").get();
		FunctionSpec spec4 = f1.getFunction("test4").get();
		spec3.invoke(context, XacmlTypes.INTEGER.of(10), XacmlTypes.INTEGER.of(10));
		spec3.invoke(context, XacmlTypes.INTEGER.of(10));
		spec4.invoke(context, XacmlTypes.INTEGER.of(10));
		verify(context);

	}

	@Test
	public void testVarArgFunctionInvocation()
	{
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true).times(6);
		replay(context);
		FunctionSpec spec5 = f1.getFunction("test5VarArg").get();
		FunctionSpec spec6 = f1.getFunction("test6VarArg").get();

		spec5.invoke(context, XacmlTypes.INTEGER.of(10));
		spec5.invoke(context, XacmlTypes.INTEGER.of(10), XacmlTypes.BOOLEAN.of(false));
		spec5.invoke(context, XacmlTypes.INTEGER.of(10), XacmlTypes.BOOLEAN.of(false), XacmlTypes.BOOLEAN.of(false));
		spec5.invoke(context, XacmlTypes.INTEGER.of(10), XacmlTypes.BOOLEAN.of(false), XacmlTypes.BOOLEAN.of(false), XacmlTypes.BOOLEAN.of(false));

		spec6.invoke(context, XacmlTypes.INTEGER.of(10), XacmlTypes.INTEGER.of(10));
		spec6.invoke(context, XacmlTypes.INTEGER.of(0), XacmlTypes.INTEGER.of(10), XacmlTypes.BOOLEAN.of(false));
		verify(context);

	}

}
