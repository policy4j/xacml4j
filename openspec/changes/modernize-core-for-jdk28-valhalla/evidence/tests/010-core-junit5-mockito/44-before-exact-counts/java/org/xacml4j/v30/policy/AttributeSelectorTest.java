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
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xacml4j.v30.AttributeReferenceEvaluationException;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.truth.Truth;


@ExtendWith(MockitoExtension.class)
public class AttributeSelectorTest
{
	private EvaluationContext context;

	@BeforeEach
	public void init() throws Exception
	{
		this.context = mock(EvaluationContext.class);
	}

	@Test
	public void testMustBePresenTrueAndReturnsNonEmptyBag() throws EvaluationException
	{
		AttributeSelectorKey refKey =
				AttributeSelectorKey
				.builder()
				.category(CategoryId.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient/md:patientDoB/text()")
				.dataType(XacmlTypes.DATE)
				.build();
		AttributeSelector ref = AttributeSelector
				.builder()
				.key(refKey)
				.mustBePresent(true)
				.build();
		ArgumentCaptor<AttributeSelectorKey> c = ArgumentCaptor.forClass(AttributeSelectorKey.class);
		when(context.resolve(c.capture())).thenReturn(Optional.of(
				XacmlTypes.DATE.ofAny("1992-03-21").toBag()));
		InOrder order = inOrder(context);
		Expression v = ref.evaluate(context);
		order.verify(context).resolve(c.capture());
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context);
		assertEquals(XacmlTypes.DATE.ofAny("1992-03-21").toBag(), v);
		assertEquals(ref.getReferenceKey(), c.getValue());

	}

	@Test
	public void testMustBePresenFalseAndReturnsNonEmptyBag() throws EvaluationException
	{
		AttributeSelectorKey refKey =
				AttributeSelectorKey
						.builder()
						.category(CategoryId.SUBJECT_RECIPIENT)
						.xpath("/md:record/md:patient/md:patientDoB/text()")
						.dataType(XacmlTypes.DATE)
						.build();
		AttributeSelector ref = AttributeSelector
				.builder()
				.key(refKey)
				.mustBePresent(false)
				.build();
		ArgumentCaptor<AttributeSelectorKey> c = ArgumentCaptor.forClass(AttributeSelectorKey.class);
		when(context.resolve(c.capture())).thenReturn(Optional.of(
				XacmlTypes.DATE.ofAny("1992-03-21").toBag()));
		InOrder order = inOrder(context);
		Expression v = ref.evaluate(context);
		order.verify(context).resolve(c.capture());
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context);
		assertEquals(XacmlTypes.DATE.ofAny("1992-03-21").toBag(), v);
		assertEquals(ref.getReferenceKey(), c.getValue());
	}

	@Test
	public void testMustBePresentTrueAndReturnsEmptyBag() throws EvaluationException
	{
		AttributeSelectorKey refKey =
				AttributeSelectorKey
						.builder()
						.category(CategoryId.SUBJECT_RECIPIENT)
						.xpath("/md:record/md:patient/md:patientDoB/text()")
						.dataType(XacmlTypes.DATE)
						.build();
		AttributeSelector ref = AttributeSelector
				.builder()
				.key(refKey)
				.mustBePresent(true)
				.build();
		ArgumentCaptor<AttributeSelectorKey> c = ArgumentCaptor.forClass(AttributeSelectorKey.class);
		when(context.resolve(c.capture())).thenReturn(Optional.of(
				XacmlTypes.DATE.emptyBag()));
		InOrder order = inOrder(context);
		Truth.assertThat(ref.evaluate(context)).isEqualTo(XacmlTypes.DATE.emptyBag());
		order.verify(context).resolve(c.capture());
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context);

		assertEquals(refKey, c.getValue());
	}

	@Test
	public void testMustBePresentTrueAndReturnsEmptyOptional() throws EvaluationException
	{
		AttributeSelectorKey refKey =
				AttributeSelectorKey
						.builder()
						.category(CategoryId.SUBJECT_RECIPIENT)
						.xpath("/md:record/md:patient/md:patientDoB/text()")
						.dataType(XacmlTypes.DATE)
						.build();
		AttributeSelector ref = AttributeSelector
				.builder()
				.key(refKey)
				.mustBePresent(true)
				.build();
		ArgumentCaptor<AttributeSelectorKey> c = ArgumentCaptor.forClass(AttributeSelectorKey.class);
		when(context.resolve(c.capture())).thenReturn(Optional.empty());
		ArgumentCaptor<Supplier<Status>> status = ArgumentCaptor.forClass(Supplier.class);
		doNothing().when(context).setEvaluationStatusIfAbsent(status.capture());
		InOrder order = inOrder(context);
		assertThrows(AttributeReferenceEvaluationException.class, () -> ref.evaluate(context));
		order.verify(context).resolve(c.capture());
		order.verify(context).setEvaluationStatusIfAbsent(status.capture());
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context);
		assertEquals(refKey, c.getValue());
		assertEquals(org.xacml4j.v30.StatusCodeId.MISSING_ATTRIBUTE, status.getValue().get().getStatusCode().getValue());
	}

	@Test
	public void testMustBePresentFalseAndReturnsEmptyBag() throws EvaluationException
	{
		AttributeSelectorKey refKey =
				AttributeSelectorKey
						.builder()
						.category(CategoryId.SUBJECT_RECIPIENT)
						.xpath("/md:record/md:patient/md:patientDoB/text()")
						.dataType(XacmlTypes.DATE)
						.build();
		AttributeSelector ref = AttributeSelector
				.builder()
				.key(refKey)
				.mustBePresent(false)
				.build();
		ArgumentCaptor<AttributeSelectorKey> c = ArgumentCaptor.forClass(AttributeSelectorKey.class);
		when(context.resolve(c.capture())).thenReturn(
				Optional.of(XacmlTypes.DATE.emptyBag()));
		InOrder order = inOrder(context);
		Expression v = ref.evaluate(context);
		assertEquals(v, XacmlTypes.DATE.bagBuilder().build());
		assertEquals(ref.getReferenceKey(), c.getValue());
		order.verify(context).resolve(c.capture());
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context);
	}

	@Test
	public void testMustBePresentFalseAndContextThrowsAttributeReferenceEvaluationException() throws EvaluationException
	{
		AttributeSelectorKey refKey =
				AttributeSelectorKey
						.builder()
						.category(CategoryId.SUBJECT_RECIPIENT)
						.xpath("/md:record/md:patient/md:patientDoB/text()")
						.dataType(XacmlTypes.DATE)
						.build();

		AttributeSelector ref = AttributeSelector
				.builder()
				.key(refKey)
				.mustBePresent(false)
				.build();

		ArgumentCaptor<AttributeSelectorKey> c = ArgumentCaptor.forClass(AttributeSelectorKey.class);
		when(context.resolve(c.capture())).thenThrow(
				AttributeReferenceEvaluationException.forSelector(ref.getReferenceKey()));
		InOrder order = inOrder(context);
		Expression v = ref.evaluate(context);
		assertEquals(v, XacmlTypes.DATE.bagBuilder().build());
		assertEquals(ref.getReferenceKey(), c.getValue());
		order.verify(context).resolve(c.capture());
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context);
	}

	@Test
	public void testMustBePresentFalseAndContextThrowsRuntimeException() throws EvaluationException
	{
		AttributeSelectorKey refKey =
				AttributeSelectorKey
						.builder()
						.category(CategoryId.SUBJECT_RECIPIENT)
						.xpath("/md:record/md:patient/md:patientDoB/text()")
						.dataType(XacmlTypes.DATE)
						.build();

		AttributeSelector ref = AttributeSelector
				.builder()
				.key(refKey)
				.mustBePresent(false)
				.build();
		ArgumentCaptor<AttributeSelectorKey> c = ArgumentCaptor.forClass(AttributeSelectorKey.class);
		when(context.resolve(c.capture())).thenThrow(new NullPointerException());
		InOrder order = inOrder(context);
		Expression v = ref.evaluate(context);
		assertEquals(v, XacmlTypes.DATE.bagBuilder().build());
		assertEquals(ref.getReferenceKey(), c.getValue());
		order.verify(context).resolve(c.capture());
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context);
	}

	@Test
	public void testMustBePresentTrueAndContextThrowsRuntimeException() throws EvaluationException
	{
		AttributeSelectorKey refKey =
				AttributeSelectorKey
						.builder()
						.category(CategoryId.SUBJECT_RECIPIENT)
						.xpath("/md:record/md:patient/md:patientDoB/text()")
						.dataType(XacmlTypes.DATE)
						.build();
		AttributeSelector ref = AttributeSelector
				.builder()
				.key(refKey)
				.mustBePresent(true)
				.build();
		ArgumentCaptor<AttributeSelectorKey> c = ArgumentCaptor.forClass(AttributeSelectorKey.class);
		when(context.resolve(c.capture())).thenThrow(new NullPointerException());
		InOrder order = inOrder(context);
		assertThrows(AttributeReferenceEvaluationException.class, () -> ref.evaluate(context));
		order.verify(context).resolve(c.capture());
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context);
		assertEquals(refKey, c.getValue());
	}

	@Test
	public void testMustBePresentTrueAndContextThrowsAttributeReferenceEvaluationException() throws EvaluationException
	{

		AttributeSelectorKey refKey =
				AttributeSelectorKey
						.builder()
						.category(CategoryId.SUBJECT_RECIPIENT)
						.xpath("/md:record/md:patient/md:patientDoB/text()")
						.dataType(XacmlTypes.DATE)
						.build();
		AttributeSelector ref = AttributeSelector
				.builder()
				.key(refKey)
				.mustBePresent(true)
				.build();
		ArgumentCaptor<AttributeSelectorKey> c = ArgumentCaptor.forClass(AttributeSelectorKey.class);
		when(context.resolve(c.capture())).thenThrow(
				AttributeReferenceEvaluationException.forSelector(ref.getReferenceKey()));
		InOrder order = inOrder(context);
		assertThrows(AttributeReferenceEvaluationException.class, () -> ref.evaluate(context));
		order.verify(context).resolve(c.capture());
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context);
		assertEquals(refKey, c.getValue());
	}
}

