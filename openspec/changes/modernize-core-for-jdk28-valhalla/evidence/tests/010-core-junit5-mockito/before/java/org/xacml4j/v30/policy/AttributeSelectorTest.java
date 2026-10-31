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

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.Optional;
import java.util.function.Supplier;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.AttributeReferenceEvaluationException;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.truth.Truth;


public class AttributeSelectorTest
{
	private EvaluationContext context;
	private IMocksControl control;

	@Before
	public void init() throws Exception
	{
		this.control = EasyMock.createStrictControl();
		this.context = control.createMock(EvaluationContext.class);
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
		Capture<AttributeSelectorKey> c = Capture.newInstance();
		expect(context.resolve(capture(c))).andReturn(Optional.of(
				XacmlTypes.DATE.ofAny("1992-03-21").toBag()));
		control.replay();
		Expression v = ref.evaluate(context);
		control.verify();
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
		Capture<AttributeSelectorKey> c = Capture.newInstance();
		expect(context.resolve(capture(c))).andReturn(Optional.of(
				XacmlTypes.DATE.ofAny("1992-03-21").toBag()));
		control.replay();
		Expression v = ref.evaluate(context);
		control.verify();
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
		Capture<AttributeSelectorKey> c = Capture.newInstance();
		expect(context.resolve(capture(c))).andReturn(Optional.of(
				XacmlTypes.DATE.emptyBag()));
		control.replay();
		Truth.assertThat(ref.evaluate(context)).isEqualTo(XacmlTypes.DATE.emptyBag());
		control.verify();

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
		Capture<AttributeSelectorKey> c = Capture.newInstance();
		expect(context.resolve(capture(c))).andReturn(Optional.empty());
		Capture<Supplier<Status>> status = Capture.newInstance();
		context.setEvaluationStatusIfAbsent(capture(status));
		control.replay();
		ref.evaluate(context);
		control.verify();
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
		Capture<AttributeSelectorKey> c = Capture.newInstance();
		expect(context.resolve(capture(c))).andReturn(
				Optional.of(XacmlTypes.DATE.emptyBag()));
		replay(context);
		Expression v = ref.evaluate(context);
		assertEquals(v, XacmlTypes.DATE.bagBuilder().build());
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

		Capture<AttributeSelectorKey> c = Capture.newInstance();
		expect(context.resolve(capture(c))).andThrow(
				AttributeReferenceEvaluationException.forSelector(ref.getReferenceKey()));
		replay(context);
		Expression v = ref.evaluate(context);
		assertEquals(v, XacmlTypes.DATE.bagBuilder().build());
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
		Capture<AttributeSelectorKey> c = Capture.newInstance();
		expect(context.resolve(capture(c))).andThrow(new NullPointerException());
		replay(context);
		Expression v = ref.evaluate(context);
		assertEquals(v, XacmlTypes.DATE.bagBuilder().build());
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
		Capture<AttributeSelectorKey> c = Capture.newInstance();
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
		Capture<AttributeSelectorKey> c = Capture.newInstance();
		expect(context.resolve(capture(c))).andThrow(
				AttributeReferenceEvaluationException.forSelector(ref.getReferenceKey()));
		replay(context);
		ref.evaluate(context);
		verify(context);
	}
}

