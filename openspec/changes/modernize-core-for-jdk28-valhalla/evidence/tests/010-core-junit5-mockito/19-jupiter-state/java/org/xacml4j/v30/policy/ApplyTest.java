package org.xacml4j.v30.policy;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xacml4j.v30.CoreException;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.types.XacmlTypes;

@ExtendWith(MockitoExtension.class)
public class ApplyTest
{
	private FunctionSpec function;
	private EvaluationContext context;

	@BeforeEach
	public void init(){
		this.function = mock(FunctionSpec.class);
		this.context = mock(EvaluationContext.class);
	}

	@Test
	public void testApplyEvaluationFunctionThrowsRuntimeException() throws CoreException
	{
		when(function.invoke(context, XacmlTypes.INTEGER.ofAny(10L)))
		.thenThrow(new IllegalArgumentException());
		InOrder order = inOrder(function, context);
		Apply apply = Apply.builder(function).param(XacmlTypes.INTEGER.ofAny(10L)).build();
		assertThrows(EvaluationException.class, () -> apply.evaluate(context));
		order.verify(function).invoke(context, XacmlTypes.INTEGER.ofAny(10L));
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(function, context);
	}

	@Test
	public void testApplyEvaluationFunctionThrowsFunctionInvocationException() throws CoreException
	{
		when(function.invoke(context, XacmlTypes.INTEGER.ofAny(10L))).
		thenThrow(new FunctionInvocationException(function, new IllegalArgumentException()));
		InOrder order = inOrder(function, context);
		Apply apply = Apply.builder(function).param(XacmlTypes.INTEGER.ofAny(10L)).build();
		assertThrows(FunctionInvocationException.class, () -> apply.evaluate(context));
		order.verify(function).invoke(context, XacmlTypes.INTEGER.ofAny(10L));
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(function, context);
	}
}
