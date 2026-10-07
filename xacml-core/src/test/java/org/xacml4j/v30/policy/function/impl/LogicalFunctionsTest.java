package org.xacml4j.v30.policy.function.impl;

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


import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;
import org.xacml4j.v30.types.IntegerVal;
import org.xacml4j.v30.types.XacmlTypes;


@ExtendWith(MockitoExtension.class)
public class LogicalFunctionsTest
{
	private FunctionProvider f;

	FunctionSpec andFunc;
	FunctionSpec orFunc;
	FunctionSpec notFunc;


	private EvaluationContext context;

	@BeforeEach
	public void init() throws Exception
	{

		this.f = FunctionProviderBuilder.builder().withDefaultFunctions().build();
		this.andFunc = f.getFunction("urn:oasis:names:tc:xacml:1.0:function:and").get();
		this.orFunc = f.getFunction("urn:oasis:names:tc:xacml:1.0:function:or").get();
		this.notFunc = f.getFunction("urn:oasis:names:tc:xacml:1.0:function:not").get();
		this.context = mock(EvaluationContext.class);
	}


	@Test
	public void testAndFunction() throws EvaluationException
	{
		assertEquals(XacmlTypes.BOOLEAN.ofAny(false),
		             LogicalFunctions.and(context, XacmlTypes.BOOLEAN.ofAny(false), XacmlTypes.BOOLEAN.ofAny(false)));
		verifyNoMoreInteractions(context);


		assertEquals(XacmlTypes.BOOLEAN.ofAny(false),
				LogicalFunctions.and(context, XacmlTypes.BOOLEAN.ofAny(true), XacmlTypes.BOOLEAN.ofAny(false)));
		verifyNoMoreInteractions(context);


		assertEquals(XacmlTypes.BOOLEAN.ofAny(true),
				LogicalFunctions.and(context, XacmlTypes.BOOLEAN.ofAny(true), XacmlTypes.BOOLEAN.ofAny(true)));
		verifyNoMoreInteractions(context);


		assertEquals(XacmlTypes.BOOLEAN.ofAny(false),
				LogicalFunctions.and(context, XacmlTypes.BOOLEAN.ofAny(false), XacmlTypes.BOOLEAN.ofAny(true)));
		verifyNoMoreInteractions(context);
	}

	@Test
	public void testLazyAndFunctionParamEvaluation() throws EvaluationException
	{
		Expression p1 = mock(Expression.class);
		Expression p2 = mock(Expression.class);
		Expression p3 = mock(Expression.class);

		when(p1.evaluate(context)).thenReturn(XacmlTypes.BOOLEAN.ofAny(false));
		InOrder order = inOrder(context, p1, p2, p3);

		assertEquals(XacmlTypes.BOOLEAN.ofAny(false),
				LogicalFunctions.and(context, p1, p2, p3));
		order.verify(p1).evaluate(context);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(p1, times(1)).evaluate(context);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context, p1, p2, p3);


		when(p1.evaluate(context)).thenReturn(XacmlTypes.BOOLEAN.ofAny(true));
		when(p2.evaluate(context)).thenReturn(XacmlTypes.BOOLEAN.ofAny(false));


		assertEquals(XacmlTypes.BOOLEAN.ofAny(false),
				LogicalFunctions.and(context, p1, p2, p3));
		order.verify(p1).evaluate(context);
		order.verify(p2).evaluate(context);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(p1, times(2)).evaluate(context);
		verify(p2, times(1)).evaluate(context);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context, p1, p2, p3);


		when(p1.evaluate(context)).thenReturn(XacmlTypes.BOOLEAN.ofAny(true));
		when(p2.evaluate(context)).thenReturn(XacmlTypes.BOOLEAN.ofAny(true));
		when(p3.evaluate(context)).thenReturn(XacmlTypes.BOOLEAN.ofAny(false));


		assertEquals(XacmlTypes.BOOLEAN.ofAny(false),
				LogicalFunctions.and(context, p1, p2, p3));
		order.verify(p1).evaluate(context);
		order.verify(p2).evaluate(context);
		order.verify(p3).evaluate(context);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(p1, times(3)).evaluate(context);
		verify(p2, times(2)).evaluate(context);
		verify(p3, times(1)).evaluate(context);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context, p1, p2, p3);

	}

	@Test
	public void testOrFunction() throws EvaluationException
	{
		assertEquals(XacmlTypes.BOOLEAN.ofAny(false),
				LogicalFunctions.or(context, XacmlTypes.BOOLEAN.ofAny(false),
						XacmlTypes.BOOLEAN.ofAny(false)));
		verifyNoMoreInteractions(context);


		assertEquals(XacmlTypes.BOOLEAN.ofAny(true),
				LogicalFunctions.or(context, XacmlTypes.BOOLEAN.ofAny(true), XacmlTypes.BOOLEAN.ofAny(false)));
		verifyNoMoreInteractions(context);


		assertEquals(XacmlTypes.BOOLEAN.ofAny(true),
				LogicalFunctions.or(context, XacmlTypes.BOOLEAN.ofAny(true), XacmlTypes.BOOLEAN.ofAny(true)));
		verifyNoMoreInteractions(context);


		assertEquals(XacmlTypes.BOOLEAN.ofAny(true),
				LogicalFunctions.or(context,XacmlTypes.BOOLEAN.ofAny(false), XacmlTypes.BOOLEAN.ofAny(true)));
		verifyNoMoreInteractions(context);
	}

	@Test
	public void testLazyOrFunctionParamEvaluation() throws EvaluationException
	{
		Expression p1 = mock(Expression.class);
		Expression p2 = mock(Expression.class);
		Expression p3 = mock(Expression.class);

		when(p1.evaluate(context)).thenReturn(XacmlTypes.BOOLEAN.ofAny(true));
		InOrder order = inOrder(context, p1, p2, p3);

		assertEquals(XacmlTypes.BOOLEAN.ofAny(true),
				LogicalFunctions.or(context, p1, p2, p3));
		order.verify(p1).evaluate(context);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(p1, times(1)).evaluate(context);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context, p1, p2, p3);


		when(p1.evaluate(context)).thenReturn(XacmlTypes.BOOLEAN.ofAny(false));
		when(p2.evaluate(context)).thenReturn(XacmlTypes.BOOLEAN.ofAny(true));


		assertEquals(XacmlTypes.BOOLEAN.ofAny(true),
				LogicalFunctions.or(context, p1, p2, p3));
		order.verify(p1).evaluate(context);
		order.verify(p2).evaluate(context);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(p1, times(2)).evaluate(context);
		verify(p2, times(1)).evaluate(context);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context, p1, p2, p3);


		when(p1.evaluate(context)).thenReturn(XacmlTypes.BOOLEAN.ofAny(false));
		when(p2.evaluate(context)).thenReturn(XacmlTypes.BOOLEAN.ofAny(false));


		assertEquals(XacmlTypes.BOOLEAN.ofAny(false),
				LogicalFunctions.or(context, p1, p2));
		order.verify(p1).evaluate(context);
		order.verify(p2).evaluate(context);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(p1, times(3)).evaluate(context);
		verify(p2, times(2)).evaluate(context);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context, p1, p2, p3);

	}

	@Test
	public void testNOfFunction() throws EvaluationException
	{
		IntegerVal n = XacmlTypes.INTEGER.ofAny(0);
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true),
				LogicalFunctions.nof(context, n));
		verifyNoMoreInteractions(context);

		n = XacmlTypes.INTEGER.ofAny(1);
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true),
				LogicalFunctions.nof(context, n, XacmlTypes.BOOLEAN.ofAny(true)));
		verifyNoMoreInteractions(context);

		assertEquals(XacmlTypes.BOOLEAN.ofAny(false),
				LogicalFunctions.nof(context, n, XacmlTypes.BOOLEAN.ofAny(false)));
		verifyNoMoreInteractions(context);

		n = XacmlTypes.INTEGER.ofAny(2);
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true),
				LogicalFunctions.nof(context, n,
						XacmlTypes.BOOLEAN.ofAny(true), XacmlTypes.BOOLEAN.ofAny(true)));
		verifyNoMoreInteractions(context);

		assertEquals(XacmlTypes.BOOLEAN.ofAny(false),
				LogicalFunctions.nof(context, n,
						XacmlTypes.BOOLEAN.ofAny(true), XacmlTypes.BOOLEAN.ofAny(false)));
		verifyNoMoreInteractions(context);

		n = XacmlTypes.INTEGER.ofAny(2);
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true),
				LogicalFunctions.nof(context, n,
						XacmlTypes.BOOLEAN.ofAny(true),
						XacmlTypes.BOOLEAN.ofAny(false),
						XacmlTypes.BOOLEAN.ofAny(true)));
		verifyNoMoreInteractions(context);
	}

	@Test
	public void testNOfFunctionInderterminate() throws EvaluationException
	{
		IntegerVal n = XacmlTypes.INTEGER.ofAny(4);
		assertThrows(IllegalArgumentException.class, () -> {
			assertEquals(XacmlTypes.BOOLEAN.ofAny(false),
				LogicalFunctions.nof(context, n, XacmlTypes.BOOLEAN.ofAny(false)));
		});
		verifyNoMoreInteractions(context);
	}
}
