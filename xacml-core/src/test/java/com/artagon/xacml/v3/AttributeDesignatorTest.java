package com.artagon.xacml.v3;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.types.IntegerType;
import com.artagon.xacml.v3.types.XacmlDataTypes;

public class AttributeDesignatorTest
{
	private IntegerType type;
	private EvaluationContext context;
	
	@Before
	public void init(){
		this.type = XacmlDataTypes.INTEGER.getType();	
		this.context = createStrictMock(EvaluationContext.class);
	}
	
	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testMustBePresentTrueAttributeDoesNotExistAndContextHandlerReturnsEmptyBag() throws EvaluationException
	{
		AttributeDesignator desig = new AttributeDesignator(
				AttributeCategoryId.SUBJECT_RECIPIENT, "testId", "testIssuer", XacmlDataTypes.INTEGER.getType(), true);
		expect(context.resolve(desig)).andReturn(XacmlDataTypes.INTEGER.emptyBag());
		replay(context);
		try{
			desig.evaluate(context);
		}catch(AttributeReferenceEvaluationException e){
			assertSame(desig, e.getReference());
			assertSame(context, e.getEvaluationContext());
			assertTrue(e.getStatusCode().isFailure());
			throw e;
		}
		verify(context);
	}
	
	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testMustBePresentTrueAttributeDoesNotExistAndContextHandlerReturnsNull() throws EvaluationException
	{
		AttributeDesignator desig = new AttributeDesignator(
				AttributeCategoryId.SUBJECT_RECIPIENT, "testId", "testIssuer", XacmlDataTypes.INTEGER.getType(), true);
		expect(context.resolve(desig)).andReturn(null);
		replay(context);
		try{
			desig.evaluate(context);
		}catch(AttributeReferenceEvaluationException e){
			assertSame(desig, e.getReference());
			assertSame(context, e.getEvaluationContext());
			assertTrue(e.getStatusCode().isFailure());
			throw e;
		}
		verify(context);
	}

	
	@Test
	public void testMustBePresentTrueAttributeDoesExist() throws EvaluationException
	{
		AttributeDesignator desig = new AttributeDesignator(
				AttributeCategoryId.SUBJECT_RECIPIENT, "testId", "testIssuer", XacmlDataTypes.INTEGER.getType(), true);
		BagOfAttributeValues<AttributeValue> bag = XacmlDataTypes.INTEGER.bag(XacmlDataTypes.INTEGER.create(1), XacmlDataTypes.INTEGER.create(2));
		expect(context.resolve(desig)).andReturn(bag);
		replay(context);
		Expression v = desig.evaluate(context);
		assertEquals(type.bagOf(), v.getEvaluatesTo());
		assertEquals(XacmlDataTypes.INTEGER.bag(
				XacmlDataTypes.INTEGER.create(1), XacmlDataTypes.INTEGER.create(2)), v);
	}
	
	@Test
	public void testMustBePresentFalseAttributeDoesNotExistAndContextHandlerReturnsEmptyBag() throws EvaluationException
	{
		AttributeDesignator desig = new AttributeDesignator(AttributeCategoryId.SUBJECT_RECIPIENT,
				"testId", "testIssuer",  type, false);
		expect(context.resolve(desig)).andReturn(XacmlDataTypes.INTEGER.emptyBag());
		replay(context);
		Expression v = desig.evaluate(context);
		assertNotNull(v);
		assertEquals(type.bagOf(), v.getEvaluatesTo());
		assertEquals(XacmlDataTypes.INTEGER.emptyBag(), v);
		verify(context);
	}
	
	@Test
	public void testMustBePresentFalseAttributeDoesNotExistAndContextHandlerReturnsNull() throws EvaluationException
	{
		AttributeDesignator desig = new AttributeDesignator(AttributeCategoryId.SUBJECT_RECIPIENT,
				"testId", "testIssuer",  type, false);
		expect(context.resolve(desig)).andReturn(null);
		replay(context);
		Expression v = desig.evaluate(context);
		assertNotNull(v);
		assertEquals(type.bagOf(), v.getEvaluatesTo());
		assertEquals(XacmlDataTypes.INTEGER.emptyBag(), v);
		verify(context);
	}
	
	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testMustBePresentTrueContextHandlerThrowsAttributeReferenceEvaluationException() throws EvaluationException
	{
		AttributeDesignator desig = new AttributeDesignator(AttributeCategoryId.SUBJECT_RECIPIENT,
				"testId", "testIssuer",  type, true);
		expect(context.resolve(desig)).andThrow(new AttributeReferenceEvaluationException(context, desig, "Errror"));
		replay(context);
		desig.evaluate(context);
		verify(context);
	}
	
	@Test
	public void testMustBePresentFalseContextHandlerThrowsAttributeReferenceEvaluationException() throws EvaluationException
	{
		AttributeDesignator desig = new AttributeDesignator(AttributeCategoryId.SUBJECT_RECIPIENT,
				"testId", "testIssuer",  type, false);
		expect(context.resolve(desig)).andThrow(new AttributeReferenceEvaluationException(context, desig, "Errror"));
		replay(context);
		Expression v = desig.evaluate(context);
		assertNotNull(v);
		assertEquals(type.bagOf(), v.getEvaluatesTo());
		assertEquals(XacmlDataTypes.INTEGER.emptyBag(), v);
		verify(context);
	}
	
	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testMustBePresentTrueContextHandlerThrowsRuntimeException() throws EvaluationException
	{
		AttributeDesignator desig = new AttributeDesignator(AttributeCategoryId.SUBJECT_RECIPIENT,
				"testId", "testIssuer",  type, true);
		expect(context.resolve(desig)).andThrow(new NullPointerException("Null"));
		replay(context);
		desig.evaluate(context);
		verify(context);
	}
	
	@Test
	public void testMustBePresentFalseContextHandlerThrowsRuntimeException() throws EvaluationException
	{
		AttributeDesignator desig = new AttributeDesignator(AttributeCategoryId.SUBJECT_RECIPIENT,
				"testId", "testIssuer",  type, false);
		expect(context.resolve(desig)).andThrow(new NullPointerException("Null"));
		replay(context);
		Expression v = desig.evaluate(context);
		assertNotNull(v);
		assertEquals(type.bagOf(), v.getEvaluatesTo());
		assertEquals(XacmlDataTypes.INTEGER.emptyBag(), v);
		verify(context);
	}
}
