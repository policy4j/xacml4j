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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xacml4j.v30.CoreException;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.ValueExp;
import org.xacml4j.v30.ValueExpTypeInfo;
import org.xacml4j.v30.policy.function.FunctionCategory;
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
		// The arguments are checked once, when the expression is built.
		order.verify(function).validateParametersAndThrow(List.of(XacmlTypes.INTEGER.ofAny(10L)));
		order.verify(function).invoke(context, XacmlTypes.INTEGER.ofAny(10L));
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(function, times(1)).invoke(context, XacmlTypes.INTEGER.ofAny(10L));
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
		// The arguments are checked once, when the expression is built.
		order.verify(function).validateParametersAndThrow(List.of(XacmlTypes.INTEGER.ofAny(10L)));
		order.verify(function).invoke(context, XacmlTypes.INTEGER.ofAny(10L));
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(function, times(1)).invoke(context, XacmlTypes.INTEGER.ofAny(10L));
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(function, context);
	}

	/**
	 * Bead policy4j-the.4.56: the check of the arguments can fail in
	 * another way than by a mismatch. The arguments are then not shown to
	 * fit: the expression is not type correct, its evaluation is
	 * processing-error, and the function is not invoked.
	 */
	@Test
	public void testCheckOfTheArgumentsThatFailsIsATypeError()
	{
		when(function.getId()).thenReturn("urn:test:function");
		doThrow(new IllegalStateException("validator failure")).when(function)
				.validateParametersAndThrow(List.of(XacmlTypes.INTEGER.ofAny(10L)));

		Apply apply = Apply.builder(function).param(XacmlTypes.INTEGER.ofAny(10L)).build();

		assertFalse(apply.isTypeCorrect());
		EvaluationException e = assertThrows(EvaluationException.class, () -> apply.evaluate(context));
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, e.getStatus().getStatusCode().getValue());
		assertEquals(Optional.of("Function=\"urn:test:function\" is applied to arguments that do not fit it"),
				e.getStatus().getMessage());
		verify(function, never()).invoke(any(), any(Expression[].class));
		verifyNoInteractions(context);
	}

	/** Design D23: a custom function still receives one and two operands on its varargs method. */
	@Test
	public void customFunctionReceivesOneAndTwoOperandsOnItsVarargsMethod()
	{
		RecordingFunction custom = new RecordingFunction();
		Expression a = XacmlTypes.INTEGER.ofAny(1L);
		Expression b = XacmlTypes.INTEGER.ofAny(2L);

		Apply.builder(custom).param(a).build().evaluate(context);
		Apply.builder(custom).param(a, b).build().evaluate(context);

		assertEquals(List.of(List.of(a), List.of(a, b)), custom.calls);
	}

	/** A custom function that records the calls of its varargs method; the list method is not called. */
	private static final class RecordingFunction implements FunctionSpec
	{
		private final List<List<Expression>> calls = new ArrayList<>();

		@Override
		public String getId(){
			return "urn:xacml4j:test:recording";
		}

		@Override
		public FunctionCategory getCategory(){
			return FunctionCategory.UNKNOWN;
		}

		@Override
		public Optional<String> getLegacyId(){
			return Optional.empty();
		}

		@Override
		public FunctionParamSpec getParamSpecAt(int index){
			throw new UnsupportedOperationException();
		}

		@Override
		public int getNumberOfParams(){
			return 2;
		}

		@Override
		public boolean isVariadic(){
			return false;
		}

		@Override
		public boolean isRequiresLazyParamEval(){
			return false;
		}

		@Override
		public boolean validateParameters(List<Expression> arguments){
			return true;
		}

		@Override
		public void validateParametersAndThrow(List<Expression> arguments){
		}

		@Override
		public ValueExpTypeInfo resolveReturnType(List<Expression> arguments){
			return XacmlTypes.BOOLEAN;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T extends ValueExp> T invoke(EvaluationContext context, Expression... arguments){
			calls.add(List.of(arguments));
			return (T) XacmlTypes.BOOLEAN.ofAny(true);
		}

		@Override
		public <T extends ValueExp> T invokeWithList(EvaluationContext context, List<Expression> arguments){
			throw new UnsupportedOperationException();
		}
	}
}
