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

import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Optional;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeReferenceEvaluationException;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.truth.Truth;


@ExtendWith(MockitoExtension.class)
public class AttributeDesignatorTest
{
	private EvaluationContext context;
	private AttributeDesignatorKey.Builder builder;

	@BeforeEach
	public void init(){
		this.context = mock(EvaluationContext.class);
		this.builder = AttributeDesignatorKey
				.builder()
				.category(CategoryId.SUBJECT_RECIPIENT)
				.attributeId("testId")
				.issuer("testIssuer")
				.dataType(XacmlTypes.INTEGER);
	}

	@Test
	public void testMustBePresentTrueAttributeDoesNotExistAndContextHandlerReturnsEmptyBag() throws EvaluationException
	{
		AttributeDesignator desig = AttributeDesignator
			.builder()
			.key(builder.build())
			.mustBePresent(true)
			.build();
		ArgumentCaptor<AttributeDesignatorKey> capture = ArgumentCaptor.forClass(AttributeDesignatorKey.class);
		when(context
			.resolve(capture.capture()))
			.thenReturn(Optional.empty());
		ArgumentCaptor<Supplier<Status>> status = ArgumentCaptor.forClass(Supplier.class);
		doNothing().when(context).setEvaluationStatusIfAbsent(status.capture());

		InOrder order = inOrder(context);
		AttributeReferenceEvaluationException e = assertThrows(
			AttributeReferenceEvaluationException.class, () -> desig.evaluate(context));
		order.verify(context
			).resolve(capture.capture());
		order.verify(context).setEvaluationStatusIfAbsent(status.capture());
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context);
		Truth.assertThat(capture.getValue()).isSameInstanceAs(e.getReference());
		Truth.assertThat(e.getStatus().getStatusCode().getValue()).isEqualTo(StatusCodeId.MISSING_ATTRIBUTE);
		assertThat(e.getStatus().getStatusCode().getValue(), is(StatusCodeId.MISSING_ATTRIBUTE));

		assertEquals(StatusCodeId.MISSING_ATTRIBUTE, status.getValue().get().getStatusCode().getValue());
	}

	@Test
	public void testMustBePresentTrueAttributeDoesNotExistAndContextHandlerReturnsEmptyOptional() throws EvaluationException
	{
		AttributeDesignator desig = AttributeDesignator
				.builder()
				.key(builder.build())
				.mustBePresent(true)
				.build();

		ArgumentCaptor<AttributeDesignatorKey> c = ArgumentCaptor.forClass(AttributeDesignatorKey.class);
		when(context.resolve(c.capture())).thenReturn(Optional.empty());
		ArgumentCaptor<Supplier<Status>> status = ArgumentCaptor.forClass(Supplier.class);
		doNothing().when(context).setEvaluationStatusIfAbsent(status.capture());
		InOrder order = inOrder(context);
		AttributeReferenceEvaluationException e = assertThrows(
				AttributeReferenceEvaluationException.class, () -> desig.evaluate(context));
		order.verify(context).resolve(c.capture());
		order.verify(context).setEvaluationStatusIfAbsent(status.capture());
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context);
		Truth.assertThat(c.getValue()).isSameInstanceAs(e.getReference());
		Truth.assertThat(e.getStatus().getStatusCode().getValue()).isEqualTo(StatusCodeId.MISSING_ATTRIBUTE);
		assertEquals(StatusCodeId.MISSING_ATTRIBUTE, status.getValue().get().getStatusCode().getValue());
	}


	@Test
	public void testMustBePresentTrueAttributeDoesExist() throws EvaluationException
	{
		AttributeDesignator desig = AttributeDesignator
				.builder()
				.key(builder.build())
				.mustBePresent(true)
				.build();
		ArgumentCaptor<AttributeDesignatorKey> c = ArgumentCaptor.forClass(AttributeDesignatorKey.class);
		when(context.resolve(c.capture())).thenReturn(Optional.of(
				XacmlTypes.INTEGER.bagBuilder().attribute(
						XacmlTypes.INTEGER.ofAny(1), XacmlTypes.INTEGER.ofAny(2)).build()));
		InOrder order = inOrder(context);
		Expression v = desig.evaluate(context);
		order.verify(context).resolve(c.capture());
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context);
		assertEquals(XacmlTypes.INTEGER.bagType(), v.getEvaluatesTo());
		assertEquals(XacmlTypes.INTEGER.bagBuilder().attribute(
				XacmlTypes.INTEGER.ofAny(1), XacmlTypes.INTEGER.ofAny(2)).build(), v);
		assertEquals(builder.build(), c.getValue());
	}

	@Test
	public void testMustBePresentFalseAttributeDoesNotExistAndContextHandlerReturnsEmptyBag() throws EvaluationException
	{
		AttributeDesignator desig = AttributeDesignator
				.builder()
				.key(builder.build())
				.mustBePresent(false)
				.build();
		ArgumentCaptor<AttributeDesignatorKey> c = ArgumentCaptor.forClass(AttributeDesignatorKey.class);
		when(context.resolve(c.capture())).thenReturn(Optional.of(XacmlTypes.INTEGER.emptyBag()));
		InOrder order = inOrder(context);
		Expression v = desig.evaluate(context);
		assertNotNull(v);
		assertEquals(XacmlTypes.INTEGER.bagType(), v.getEvaluatesTo());
		assertEquals(XacmlTypes.INTEGER.bagBuilder().build(), v);
		order.verify(context).resolve(c.capture());
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context);
		assertEquals(builder.build(), c.getValue());
	}

	@Test
	public void testMustBePresentFalseAttributeDoesNotExistAndContextHandlerReturnsNull() throws EvaluationException
	{
		AttributeDesignator desig = AttributeDesignator
				.builder()
				.key(builder.build())
				.mustBePresent(false)
				.build();
		ArgumentCaptor<AttributeDesignatorKey> c = ArgumentCaptor.forClass(AttributeDesignatorKey.class);
		when(context.resolve(c.capture())).thenReturn(null);
		InOrder order = inOrder(context);
		Expression v = desig.evaluate(context);
		assertNotNull(v);
		assertEquals(XacmlTypes.INTEGER.bagType(), v.getEvaluatesTo());
		assertEquals(XacmlTypes.INTEGER.bagBuilder().build(), v);
		order.verify(context).resolve(c.capture());
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context);
		assertEquals(builder.build(), c.getValue());
	}

	@Test
	public void testMustBePresentTrueContextHandlerThrowsAttributeReferenceEvaluationException() throws EvaluationException
	{
		AttributeDesignator desig = AttributeDesignator
				.builder()
				.key(builder.build())
				.mustBePresent(true)
				.build();
		ArgumentCaptor<AttributeDesignatorKey> c = ArgumentCaptor.forClass(AttributeDesignatorKey.class);
		when(context.resolve(c.capture())).thenThrow(new AttributeReferenceEvaluationException(desig.getReferenceKey(), "Errror"));
		InOrder order = inOrder(context);
		assertThrows(AttributeReferenceEvaluationException.class, () -> desig.evaluate(context));
		order.verify(context).resolve(c.capture());
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context);
		assertEquals(builder.build(), c.getValue());
	}

	@Test
	public void testMustBePresentFalseContextHandlerThrowsAttributeReferenceEvaluationException() throws EvaluationException
	{
		AttributeDesignator desig = AttributeDesignator
				.builder()
				.key(builder.build())
				.mustBePresent(false)
				.build();
		ArgumentCaptor<AttributeDesignatorKey> c = ArgumentCaptor.forClass(AttributeDesignatorKey.class);
		when(context.resolve(c.capture())).thenThrow(
				AttributeReferenceEvaluationException.forDesignator(builder.build()));
		InOrder order = inOrder(context);
		Expression v = desig.evaluate(context);
		assertNotNull(v);
		assertEquals(XacmlTypes.INTEGER.bagType(), v.getEvaluatesTo());
		assertEquals(XacmlTypes.INTEGER.bagBuilder().build(), v);
		order.verify(context).resolve(c.capture());
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context);
		assertEquals(builder.build(), c.getValue());
	}

	@Test
	public void testMustBePresentTrueContextHandlerThrowsRuntimeException() throws EvaluationException
	{
		AttributeDesignator desig = AttributeDesignator
				.builder()
				.key(builder.build())
				.mustBePresent(true)
				.build();
		ArgumentCaptor<AttributeDesignatorKey> c = ArgumentCaptor.forClass(AttributeDesignatorKey.class);
		when(context.resolve(c.capture())).thenThrow(new NullPointerException("Null"));
		InOrder order = inOrder(context);
		assertThrows(AttributeReferenceEvaluationException.class, () -> desig.evaluate(context));
		order.verify(context).resolve(c.capture());
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context);
		assertEquals(builder.build(), c.getValue());
	}

	@Test
	public void testMustBePresentFalseContextHandlerThrowsRuntimeException() throws EvaluationException
	{
		AttributeDesignator desig = AttributeDesignator
				.builder()
				.key(builder.build())
				.mustBePresent(false)
				.build();
		ArgumentCaptor<AttributeDesignatorKey> c = ArgumentCaptor.forClass(AttributeDesignatorKey.class);
		when(context.resolve(c.capture())).thenThrow(new NullPointerException("Null"));
		InOrder order = inOrder(context);
		Expression v = desig.evaluate(context);
		assertNotNull(v);
		assertEquals(XacmlTypes.INTEGER.bagType(), v.getEvaluatesTo());
		assertEquals(XacmlTypes.INTEGER.bagBuilder().build(), v);
		order.verify(context).resolve(c.capture());
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context);
		assertEquals(builder.build(), c.getValue());
	}
}
