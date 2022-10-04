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

import com.google.common.truth.Truth;
import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.*;
import org.xacml4j.v30.types.XacmlTypes;

import java.util.Optional;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;


public class AttributeSelectorTest
{
	private EvaluationContext context;

	@Before
	public void init() throws Exception
	{
		this.context = createStrictMock(EvaluationContext.class);
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
		Capture<AttributeSelectorKey> c = new Capture<AttributeSelectorKey>();
		expect(context.resolve(capture(c))).andReturn(Optional.of(
				XacmlTypes.DATE.of("1992-03-21").toBag()));
		replay(context);
		Expression v = ref.evaluate(context);
		assertEquals(XacmlTypes.DATE.of("1992-03-21").toBag(), v);
		assertEquals(ref.getReferenceKey(), c.getValue());
		verify(context);
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
		Capture<AttributeSelectorKey> c = new Capture<AttributeSelectorKey>();
		expect(context.resolve(capture(c))).andReturn(Optional.of(
				XacmlTypes.DATE.of("1992-03-21").toBag()));
		replay(context);
		Expression v = ref.evaluate(context);
		assertEquals(XacmlTypes.DATE.of("1992-03-21").toBag(), v);
		assertEquals(ref.getReferenceKey(), c.getValue());
		verify(context);
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
		Capture<AttributeSelectorKey> c = new Capture<AttributeSelectorKey>();
		expect(context.resolve(capture(c))).andReturn(Optional.of(
				XacmlTypes.DATE.emptyBag()));
		replay(context);
		Truth.assertThat(ref.evaluate(context)).isEqualTo(XacmlTypes.DATE.emptyBag());
		verify(context);

	}

	@Test(expected=AttributeReferenceEvaluationException.class)
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
		Capture<AttributeSelectorKey> c = new Capture<AttributeSelectorKey>();
		expect(context.resolve(capture(c))).andReturn(Optional.empty());
		replay(context);
		ref.evaluate(context);
		verify(context);
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
		Capture<AttributeSelectorKey> c = new Capture<AttributeSelectorKey>();
		expect(context.resolve(capture(c))).andReturn(
				Optional.of(XacmlTypes.DATE.emptyBag()));
		replay(context);
		Expression v = ref.evaluate(context);
		assertEquals(v, XacmlTypes.DATE.bag().build());
		assertEquals(ref.getReferenceKey(), c.getValue());
		verify(context);
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

		Capture<AttributeSelectorKey> c = new Capture<AttributeSelectorKey>();
		expect(context.resolve(capture(c))).andThrow(
				AttributeReferenceEvaluationException.forSelector(ref.getReferenceKey()));
		replay(context);
		Expression v = ref.evaluate(context);
		assertEquals(v, XacmlTypes.DATE.bag().build());
		assertEquals(ref.getReferenceKey(), c.getValue());
		verify(context);
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
		Capture<AttributeSelectorKey> c = new Capture<AttributeSelectorKey>();
		expect(context.resolve(capture(c))).andThrow(new NullPointerException());
		replay(context);
		Expression v = ref.evaluate(context);
		assertEquals(v, XacmlTypes.DATE.bag().build());
		assertEquals(ref.getReferenceKey(), c.getValue());
		verify(context);
	}

	@Test(expected=AttributeReferenceEvaluationException.class)
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
		Capture<AttributeSelectorKey> c = new Capture<AttributeSelectorKey>();
		expect(context.resolve(capture(c))).andThrow(new NullPointerException());
		replay(context);
		ref.evaluate(context);
		verify(context);
	}

	@Test(expected=AttributeReferenceEvaluationException.class)
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
		Capture<AttributeSelectorKey> c = new Capture<AttributeSelectorKey>();
		expect(context.resolve(capture(c))).andThrow(
				AttributeReferenceEvaluationException.forSelector(ref.getReferenceKey()));
		replay(context);
		ref.evaluate(context);
		verify(context);
	}
}

