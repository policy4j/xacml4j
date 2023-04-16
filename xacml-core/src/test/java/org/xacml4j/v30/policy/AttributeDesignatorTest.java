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
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import java.util.function.Supplier;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import static org.hamcrest.CoreMatchers.*;
import org.junit.Before;
import org.junit.Test;
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


public class AttributeDesignatorTest
{
	private EvaluationContext context;
	private AttributeDesignatorKey.Builder builder;

	private IMocksControl control;
	@Before
	public void init(){
		this.control = EasyMock.createStrictControl();
		this.context = control.createMock(EvaluationContext.class);
		this.builder = AttributeDesignatorKey
				.builder()
				.category(CategoryId.SUBJECT_RECIPIENT)
				.attributeId("testId")
				.issuer("testIssuer")
				.dataType(XacmlTypes.INTEGER);
	}

	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testMustBePresentTrueAttributeDoesNotExistAndContextHandlerReturnsEmptyBag() throws EvaluationException
	{
		AttributeDesignator desig = AttributeDesignator
				.builder()
				.key(builder.build())
				.mustBePresent(true)
				.build();
		Capture<AttributeDesignatorKey> capture = Capture.newInstance();
		expect(context
				.resolve(capture(capture)))
				.andReturn(Optional.empty());
		Capture<Supplier<Status>> status = Capture.newInstance();
		context.setEvaluationStatusIfAbsent(capture(status));

		control.replay();
		try{
			desig.evaluate(context);
		}catch(AttributeReferenceEvaluationException e){
			control.verify();
			Truth.assertThat(capture.getValue()).isSameInstanceAs(e.getReference());
			Truth.assertThat(e.getStatus().getStatusCode().getValue()).isEqualTo(StatusCodeId.MISSING_ATTRIBUTE);
			assertThat(e.getStatus().getStatusCode().getValue(), is(StatusCodeId.MISSING_ATTRIBUTE));
			throw e;
		}

	}

	@Test
	public void testMustBePresentTrueAttributeDoesNotExistAndContextHandlerReturnsEmptyOptional() throws EvaluationException
	{
		AttributeDesignator desig = AttributeDesignator
				.builder()
				.key(builder.build())
				.mustBePresent(true)
				.build();

		Capture<AttributeDesignatorKey> c = Capture.newInstance();
		expect(context.resolve(capture(c))).andReturn(Optional.empty());
		Capture<Supplier<Status>> status = Capture.newInstance();
		context.setEvaluationStatusIfAbsent(capture(status));
		control.replay();
		try{
			desig.evaluate(context);
		}catch(AttributeReferenceEvaluationException e){
			control.verify();
			Truth.assertThat(c.getValue()).isSameInstanceAs(e.getReference());
			Truth.assertThat(e.getStatus().getStatusCode().getValue()).isEqualTo(StatusCodeId.MISSING_ATTRIBUTE);
		}
	}


	@Test
	public void testMustBePresentTrueAttributeDoesExist() throws EvaluationException
	{
		AttributeDesignator desig = AttributeDesignator
				.builder()
				.key(builder.build())
				.mustBePresent(true)
				.build();
		Capture<AttributeDesignatorKey> c = Capture.newInstance();
		expect(context.resolve(capture(c))).andReturn(Optional.of(
				XacmlTypes.INTEGER.bag().attribute(
						XacmlTypes.INTEGER.of(1), XacmlTypes.INTEGER.of(2)).build()));
		control.replay();
		Expression v = desig.evaluate(context);
		control.verify();
		assertEquals(XacmlTypes.INTEGER.bagType(), v.getEvaluatesTo());
		assertEquals(XacmlTypes.INTEGER.bag().attribute(
				XacmlTypes.INTEGER.of(1), XacmlTypes.INTEGER.of(2)).build(), v);
	}

	@Test
	public void testMustBePresentFalseAttributeDoesNotExistAndContextHandlerReturnsEmptyBag() throws EvaluationException
	{
		AttributeDesignator desig = AttributeDesignator
				.builder()
				.key(builder.build())
				.mustBePresent(false)
				.build();
		Capture<AttributeDesignatorKey> c = Capture.newInstance();
		expect(context.resolve(capture(c))).andReturn(Optional.of(XacmlTypes.INTEGER.emptyBag()));
		replay(context);
		Expression v = desig.evaluate(context);
		assertNotNull(v);
		assertEquals(XacmlTypes.INTEGER.bagType(), v.getEvaluatesTo());
		assertEquals(XacmlTypes.INTEGER.bag().build(), v);
		verify(context);
	}

	@Test
	public void testMustBePresentFalseAttributeDoesNotExistAndContextHandlerReturnsNull() throws EvaluationException
	{
		AttributeDesignator desig = AttributeDesignator
				.builder()
				.key(builder.build())
				.mustBePresent(false)
				.build();
		Capture<AttributeDesignatorKey> c = Capture.newInstance();
		expect(context.resolve(capture(c))).andReturn(null);
		replay(context);
		Expression v = desig.evaluate(context);
		assertNotNull(v);
		assertEquals(XacmlTypes.INTEGER.bagType(), v.getEvaluatesTo());
		assertEquals(XacmlTypes.INTEGER.bag().build(), v);
		verify(context);
	}

	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testMustBePresentTrueContextHandlerThrowsAttributeReferenceEvaluationException() throws EvaluationException
	{
		AttributeDesignator desig = AttributeDesignator
				.builder()
				.key(builder.build())
				.mustBePresent(true)
				.build();
		Capture<AttributeDesignatorKey> c = Capture.newInstance();
		expect(context.resolve(capture(c))).andThrow(new AttributeReferenceEvaluationException(desig.getReferenceKey(), "Errror"));
		replay(context);
		desig.evaluate(context);
		verify(context);
	}

	@Test
	public void testMustBePresentFalseContextHandlerThrowsAttributeReferenceEvaluationException() throws EvaluationException
	{
		AttributeDesignator desig = AttributeDesignator
				.builder()
				.key(builder.build())
				.mustBePresent(false)
				.build();
		Capture<AttributeDesignatorKey> c = Capture.newInstance();
		expect(context.resolve(capture(c))).andThrow(
				AttributeReferenceEvaluationException.forDesignator(builder.build()));
		replay(context);
		Expression v = desig.evaluate(context);
		assertNotNull(v);
		assertEquals(XacmlTypes.INTEGER.bagType(), v.getEvaluatesTo());
		assertEquals(XacmlTypes.INTEGER.bag().build(), v);
		verify(context);
	}

	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testMustBePresentTrueContextHandlerThrowsRuntimeException() throws EvaluationException
	{
		AttributeDesignator desig = AttributeDesignator
				.builder()
				.key(builder.build())
				.mustBePresent(true)
				.build();
		Capture<AttributeDesignatorKey> c = Capture.newInstance();
		expect(context.resolve(capture(c))).andThrow(new NullPointerException("Null"));
		replay(context);
		desig.evaluate(context);
		verify(context);
	}

	@Test
	public void testMustBePresentFalseContextHandlerThrowsRuntimeException() throws EvaluationException
	{
		AttributeDesignator desig = AttributeDesignator
				.builder()
				.key(builder.build())
				.mustBePresent(false)
				.build();
		Capture<AttributeDesignatorKey> c = Capture.newInstance();
		expect(context.resolve(capture(c))).andThrow(new NullPointerException("Null"));
		replay(context);
		Expression v = desig.evaluate(context);
		assertNotNull(v);
		assertEquals(XacmlTypes.INTEGER.bagType(), v.getEvaluatesTo());
		assertEquals(XacmlTypes.INTEGER.bag().build(), v);
		verify(context);
	}
}
