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
import static org.junit.Assert.assertNotNull;


public class AttributeDesignatorTest
{
	private EvaluationContext context;
	private AttributeDesignatorKey.Builder builder;

	@Before
	public void init(){
		this.context = createStrictMock(EvaluationContext.class);
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
		Capture<AttributeDesignatorKey> c = new Capture<AttributeDesignatorKey>();
		expect(context
				.resolve(capture(c)))
				.andReturn(Optional.empty());
		replay(context);
		try{
			desig.evaluate(context);
		}catch(AttributeReferenceEvaluationException e){
			Truth.assertThat(c.getValue()).isSameInstanceAs(e.getReference());
			Truth.assertThat(e.getStatus().getStatusCode().getValue()).isEqualTo(StatusCodeId.MISSING_ATTRIBUTE);
			throw e;
		}
		verify(context);
	}

	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testMustBePresentTrueAttributeDoesNotExistAndContextHandlerReturnsEmptyOptional() throws EvaluationException
	{
		AttributeDesignator desig = AttributeDesignator
				.builder()
				.key(builder.build())
				.mustBePresent(true)
				.build();

		Capture<AttributeDesignatorKey> c = new Capture<AttributeDesignatorKey>();
		expect(context.resolve(capture(c))).andReturn(Optional.empty());
		replay(context);
		try{
			desig.evaluate(context);
		}catch(AttributeReferenceEvaluationException e){
			Truth.assertThat(c.getValue()).isSameInstanceAs(e.getReference());
			Truth.assertThat(e.getStatus().getStatusCode().getValue()).isEqualTo(StatusCodeId.MISSING_ATTRIBUTE);
			throw e;
		}
		verify(context);
	}


	@Test
	public void testMustBePresentTrueAttributeDoesExist() throws EvaluationException
	{
		AttributeDesignator desig = AttributeDesignator
				.builder()
				.key(builder.build())
				.mustBePresent(true)
				.build();
		Capture<AttributeDesignatorKey> c = new Capture<AttributeDesignatorKey>();
		expect(context.resolve(capture(c))).andReturn(Optional.of(
				XacmlTypes.INTEGER.bag().attribute(
						XacmlTypes.INTEGER.of(1), XacmlTypes.INTEGER.of(2)).build()));

		replay(context);
		Expression v = desig.evaluate(context);
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
		Capture<AttributeDesignatorKey> c = new Capture<>();
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
		Capture<AttributeDesignatorKey> c = new Capture<AttributeDesignatorKey>();
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
		Capture<AttributeDesignatorKey> c = new Capture<AttributeDesignatorKey>();
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
		Capture<AttributeDesignatorKey> c = new Capture<AttributeDesignatorKey>();
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
		Capture<AttributeDesignatorKey> c = new Capture<AttributeDesignatorKey>();
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
		Capture<AttributeDesignatorKey> c = new Capture<AttributeDesignatorKey>();
		expect(context.resolve(capture(c))).andThrow(new NullPointerException("Null"));
		replay(context);
		Expression v = desig.evaluate(context);
		assertNotNull(v);
		assertEquals(XacmlTypes.INTEGER.bagType(), v.getEvaluatesTo());
		assertEquals(XacmlTypes.INTEGER.bag().build(), v);
		verify(context);
	}
}
