package org.xacml4j.v30.pdp;

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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.types.IntegerExp;
import org.xacml4j.v30.types.XacmlTypes;


public class AttributeDesignatorTest
{
	private EvaluationContext context;

	@Before
	public void init(){
		this.context = createStrictMock(EvaluationContext.class);
	}

	@Test
	public void testMustBePresentTrueAttributeDoesNotExistAndContextHandlerReturnsEmptyBag() throws EvaluationException {
		AttributeDesignator desig = AttributeDesignator
				.builder()
				.category(Categories.SUBJECT_RECIPIENT)
				.attributeId("testId")
				.issuer("testIssuer")
				.dataType(XacmlTypes.INTEGER)
				.mustBePresent(true)
				.build();
		Capture<AttributeDesignatorKey> c = new Capture<AttributeDesignatorKey>();
		expect(context.resolve(capture(c))).andReturn(XacmlTypes.INTEGER.emptyBag());
		replay(context);
		Expression v = desig.evaluate(context);
		assertEquals(XacmlTypes.INTEGER.bagType(), v.getEvaluatesTo());
		assertEquals(XacmlTypes.INTEGER.emptyBag(), v);
		verify(context);
	}

	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testMustBePresentTrueAttributeDoesNotExistAndContextHandlerReturnsNull() throws EvaluationException
	{
		AttributeDesignator desig = AttributeDesignator
				.builder()
				.category(Categories.SUBJECT_RECIPIENT)
				.attributeId("testId")
				.issuer("testIssuer")
				.mustBePresent(true)
				.dataType(XacmlTypes.INTEGER)
				.build();

		Capture<AttributeDesignatorKey> c = new Capture<AttributeDesignatorKey>();
		expect(context.resolve(capture(c))).andReturn(null);
		replay(context);
		try{
			desig.evaluate(context);
		}catch(AttributeReferenceEvaluationException e){
			assertSame(c.getValue(), e.getReference());
			assertTrue(e.getStatus().isFailure());
			throw e;
		}
		verify(context);
	}

	@Test
	public void testMustBePresentTrueAttributeDoesExist() throws EvaluationException
	{
		AttributeDesignator desig = AttributeDesignator
				.builder()
				.category(Categories.SUBJECT_RECIPIENT)
				.attributeId("testId")
				.issuer("testIssuer")
				.dataType(XacmlTypes.INTEGER)
				.mustBePresent(true)
				.build();
		Capture<AttributeDesignatorKey> c = new Capture<AttributeDesignatorKey>();
		expect(context.resolve(capture(c))).andReturn(
				XacmlTypes.INTEGER.bagOf(
						IntegerExp.valueOf(1), IntegerExp.valueOf(2)));

		replay(context);
		Expression v = desig.evaluate(context);
		assertEquals(XacmlTypes.INTEGER.bagType(), v.getEvaluatesTo());
		assertEquals(XacmlTypes.INTEGER.bagOf(
				IntegerExp.valueOf(1), IntegerExp.valueOf(2)), v);
	}

	@Test
	public void testMustBePresentFalseAttributeDoesNotExistAndContextHandlerReturnsEmptyBag() throws EvaluationException
	{
		AttributeDesignator desig = AttributeDesignator
				.builder()
				.category(Categories.SUBJECT_RECIPIENT)
				.attributeId("testId")
				.issuer("testIssuer")
				.dataType(XacmlTypes.INTEGER)
				.mustBePresent(false)
				.build();
		Capture<AttributeDesignatorKey> c = new Capture<AttributeDesignatorKey>();
		expect(context.resolve(capture(c))).andReturn(XacmlTypes.INTEGER.emptyBag());
		replay(context);
		Expression v = desig.evaluate(context);
		assertNotNull(v);
		assertEquals(XacmlTypes.INTEGER.bagType(), v.getEvaluatesTo());
		assertEquals(XacmlTypes.INTEGER.emptyBag(), v);
		verify(context);
	}

	@Test
	public void testMustBePresentFalseAttributeDoesNotExistAndContextHandlerReturnsNull() throws EvaluationException
	{
		AttributeDesignator desig = AttributeDesignator
				.builder()
				.category(Categories.SUBJECT_RECIPIENT)
				.attributeId("testId")
				.issuer("testIssuer")
				.dataType(XacmlTypes.INTEGER)
				.mustBePresent(false)
				.build();
		Capture<AttributeDesignatorKey> c = new Capture<AttributeDesignatorKey>();
		expect(context.resolve(capture(c))).andReturn(null);
		replay(context);
		Expression v = desig.evaluate(context);
		assertNotNull(v);
		assertEquals(XacmlTypes.INTEGER.bagType(), v.getEvaluatesTo());
		assertEquals(XacmlTypes.INTEGER.emptyBag(), v);
		verify(context);
	}

	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testMustBePresentTrueContextHandlerThrowsAttributeReferenceEvaluationException() throws EvaluationException
	{
		AttributeDesignator desig = AttributeDesignator
				.builder()
				.category(Categories.SUBJECT_RECIPIENT)
				.attributeId("testId")
				.issuer("testIssuer")
				.dataType(XacmlTypes.INTEGER)
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
				.category(Categories.SUBJECT_RECIPIENT)
				.attributeId("testId")
				.issuer("testIssuer")
				.dataType(XacmlTypes.INTEGER)
				.mustBePresent(false)
				.build();
		Capture<AttributeDesignatorKey> c = new Capture<AttributeDesignatorKey>();
		expect(context.resolve(capture(c))).andThrow(new AttributeReferenceEvaluationException(
				desig.getReferenceKey(), "Errror"));
		replay(context);
		Expression v = desig.evaluate(context);
		assertNotNull(v);
		assertEquals(XacmlTypes.INTEGER.bagType(), v.getEvaluatesTo());
		assertEquals(XacmlTypes.INTEGER.emptyBag(), v);
		verify(context);
	}

	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testMustBePresentTrueContextHandlerThrowsRuntimeException() throws EvaluationException
	{
		AttributeDesignator desig = AttributeDesignator
				.builder()
				.category(Categories.SUBJECT_RECIPIENT)
				.attributeId("testId")
				.issuer("testIssuer")
				.dataType(XacmlTypes.INTEGER)
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
				.category(Categories.SUBJECT_RECIPIENT)
				.attributeId("testId")
				.issuer("testIssuer")
				.dataType(XacmlTypes.INTEGER)
				.mustBePresent(false)
				.build();
		Capture<AttributeDesignatorKey> c = new Capture<AttributeDesignatorKey>();
		expect(context.resolve(capture(c))).andThrow(new NullPointerException("Null"));
		replay(context);
		Expression v = desig.evaluate(context);
		assertNotNull(v);
		assertEquals(XacmlTypes.INTEGER.bagType(), v.getEvaluatesTo());
		assertEquals(XacmlTypes.INTEGER.emptyBag(), v);
		verify(context);
	}
}
