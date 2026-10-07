package org.xacml4j.v30.policy.function.impl;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2026 Xacml4J.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.ValueExp;
import org.xacml4j.v30.policy.Apply;
import org.xacml4j.v30.policy.FunctionInvocationException;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * policy4j-the.4.43: function invocation keeps the status of the first
 * {@link EvaluationException} in the cause chain. A plain runtime failure
 * stays processing-error. XACML 3.0 A.3.9 (boolean-from-string) and the
 * attribute-designator function.
 */
@ExtendWith(MockitoExtension.class)
public class FunctionInvocationStatusTest
{
	private static final String BOOLEAN_FROM_STRING =
			"urn:oasis:names:tc:xacml:3.0:function:boolean-from-string";
	private static final String ATTRIBUTE_DESIGNATOR =
			"urn:oasis:names:tc:xacml:3.0:function:attribute-designator";
	private static final String ATTRIBUTE_ID = "urn:test:attribute";
	private static final FunctionProvider FUNCTIONS =
			FunctionProvider.builder().withDefaultFunctions().build();

	@Test
	public void booleanFromStringTrueKeepsSyntaxErrorThroughApply()
	{
		EvaluationContext context = mock(EvaluationContext.class);
		Apply apply = Apply.builder(BOOLEAN_FROM_STRING, FUNCTIONS::getFunction)
				.param(XacmlTypes.STRING.ofAny("TRUE"))
				.build();
		EvaluationException error = assertThrows(EvaluationException.class,
				() -> apply.evaluate(context));
		assertEquals(StatusCodeId.SYNTAX_ERROR, status(error));
	}

	@Test
	public void attributeDesignatorApplyKeepsMissingAttributeWhenTheAttributeIsAbsent()
	{
		EvaluationContext context = mock(EvaluationContext.class);
		when(context.resolveEntity(CategoryId.RESOURCE))
				.thenReturn(Optional.of(Entity.builder().build()));
		Apply apply = Apply.builder(ATTRIBUTE_DESIGNATOR, FUNCTIONS::getFunction)
				.param(XacmlTypes.ANYURI.ofAny(CategoryId.RESOURCE.getId()),
						XacmlTypes.ANYURI.ofAny(ATTRIBUTE_ID),
						XacmlTypes.ANYURI.ofAny(XacmlTypes.STRING.getTypeId()),
						BooleanVal.of(true))
				.build();
		EvaluationException error = assertThrows(EvaluationException.class,
				() -> apply.evaluate(context));
		assertEquals(StatusCodeId.MISSING_ATTRIBUTE, status(error));
		verify(context, times(1)).resolveEntity(CategoryId.RESOURCE);
		verify(context, times(1)).isValidateFuncParamsAtRuntime();
		verifyNoMoreInteractions(context);
	}

	@Test
	public void runtimeExceptionBecomesProcessingError()
	{
		FunctionInvocationException error = invokeThrowing(new RuntimeException("boom"));
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, status(error));
	}

	@Test
	public void invocationKeepsTheFirstEvaluationExceptionWhenTheRootIsNotOne()
	{
		EvaluationException missing = new EvaluationException(
				Status.missingAttribute(ATTRIBUTE_ID).build(),
				new IllegalStateException("inner"));
		FunctionInvocationException error = invokeThrowing(
				new RuntimeException("wrapper", missing));
		assertEquals(StatusCodeId.MISSING_ATTRIBUTE, status(error));
	}

	@Test
	public void fourArgumentConstructorKeepsTheFirstEvaluationExceptionStatus()
	{
		FunctionSpec spec = FUNCTIONS.getFunction(ATTRIBUTE_DESIGNATOR).orElseThrow();
		EvaluationException missing = new EvaluationException(
				Status.missingAttribute(ATTRIBUTE_ID).build(),
				new IllegalStateException("inner"));
		FunctionInvocationException error = new FunctionInvocationException(
				spec, new RuntimeException("wrapper", missing),
				"Failed to invoke function=\"%s\"", "attribute-designator");
		assertEquals(StatusCodeId.MISSING_ATTRIBUTE, status(error));
	}

	@Test
	public void fourArgumentConstructorKeepsTheOuterStatusAheadOfANestedSyntaxException()
	{
		FunctionSpec spec = FUNCTIONS.getFunction(BOOLEAN_FROM_STRING).orElseThrow();
		EvaluationException outer = new EvaluationException(
				Status.processingError().message("outer").build(),
				new SyntaxException("inner lexical"));
		FunctionInvocationException error = new FunctionInvocationException(spec, outer);
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, status(error));
	}

	@Test
	public void plainRuntimeExceptionConstructorStaysProcessingError()
	{
		FunctionSpec spec = FUNCTIONS.getFunction(BOOLEAN_FROM_STRING).orElseThrow();
		FunctionInvocationException error = new FunctionInvocationException(
				spec, new RuntimeException("boom"),
				"Failed to invoke function=\"%s\"", "boolean-from-string");
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, status(error));
		assertEquals("Failed to invoke function=\"boolean-from-string\"",
				error.getStatus().getMessage().orElseThrow());
	}

	@Test
	public void messageConstructorStaysProcessingError()
	{
		FunctionSpec spec = FUNCTIONS.getFunction(BOOLEAN_FROM_STRING).orElseThrow();
		FunctionInvocationException error = new FunctionInvocationException(spec, "Fail");
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, status(error));
		assertEquals("Fail", error.getStatus().getMessage().orElseThrow());
	}

	private static FunctionInvocationException invokeThrowing(RuntimeException failure)
	{
		@SuppressWarnings("unchecked")
		PolicyToPlatformFunctionInvocation<ValueExp> invocation =
				mock(PolicyToPlatformFunctionInvocation.class);
		FunctionSpec spec = mock(FunctionSpec.class);
		EvaluationContext context = mock(EvaluationContext.class);
		when(spec.getNumberOfParams()).thenReturn(0);
		when(spec.isVariadic()).thenReturn(false);
		when(invocation.invoke(new Object[0])).thenThrow(failure);
		DefaultFunctionInvocation invoker = new DefaultFunctionInvocation(invocation, false);
		FunctionInvocationException error = assertThrows(FunctionInvocationException.class,
				() -> invoker.invoke(spec, context, java.util.List.of()));
		verify(spec, times(1)).getNumberOfParams();
		verify(spec, times(2)).isVariadic();
		verify(invocation, times(1)).invoke(new Object[0]);
		verifyNoMoreInteractions(spec, invocation, context);
		return error;
	}

	private static StatusCodeId status(EvaluationException error)
	{
		return error.getStatus().getStatusCode().getValue();
	}
}
