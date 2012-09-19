package com.artagon.xacml.v30.pdp;

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

import com.artagon.xacml.v30.AttributeCategories;
import com.artagon.xacml.v30.AttributeDesignatorKey;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.EvaluationException;
import com.artagon.xacml.v30.Expression;
import com.artagon.xacml.v30.types.IntegerType;

public class AttributeDesignatorTest
{
	private IntegerType type;
	private EvaluationContext context;
	
	@Before
	public void init(){
		this.type = IntegerType.INTEGER;	
		this.context = createStrictMock(EvaluationContext.class);
	}
	
	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testMustBePresentTrueAttributeDoesNotExistAndContextHandlerReturnsEmptyBag() throws EvaluationException
	{
		AttributeDesignator desig = new AttributeDesignator(
				AttributeCategories.SUBJECT_RECIPIENT, "testId", "testIssuer",
				IntegerType.INTEGER, true);
		Capture<AttributeDesignatorKey> c = new Capture<AttributeDesignatorKey>();
		expect(context.resolve(capture(c))).andReturn(IntegerType.INTEGER.emptyBag());
		replay(context);
		try{
			desig.evaluate(context);
		}catch(AttributeReferenceEvaluationException e){
			assertSame(c.getValue(), e.getReference());
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
				AttributeCategories.SUBJECT_RECIPIENT, "testId", "testIssuer", 
				IntegerType.INTEGER, true);
		Capture<AttributeDesignatorKey> c = new Capture<AttributeDesignatorKey>();
		expect(context.resolve(capture(c))).andReturn(null);
		replay(context);
		try{
			desig.evaluate(context);
		}catch(AttributeReferenceEvaluationException e){
			assertSame(c.getValue(), e.getReference());
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
				AttributeCategories.SUBJECT_RECIPIENT, "testId", "testIssuer", 
				IntegerType.INTEGER, true);
		Capture<AttributeDesignatorKey> c = new Capture<AttributeDesignatorKey>();
		expect(context.resolve(capture(c))).andReturn(
				IntegerType.INTEGER.bagOf(
						IntegerType.INTEGER.create(1), IntegerType.INTEGER.create(2)));
				
		replay(context);
		Expression v = desig.evaluate(context);
		assertEquals(type.bagType(), v.getEvaluatesTo());
		assertEquals(IntegerType.INTEGER.bagOf(
				IntegerType.INTEGER.create(1), IntegerType.INTEGER.create(2)), v);
	}
	
	@Test
	public void testMustBePresentFalseAttributeDoesNotExistAndContextHandlerReturnsEmptyBag() throws EvaluationException
	{
		AttributeDesignator desig = new AttributeDesignator(AttributeCategories.SUBJECT_RECIPIENT,
				"testId", "testIssuer",  type, false);
		Capture<AttributeDesignatorKey> c = new Capture<AttributeDesignatorKey>();
		expect(context.resolve(capture(c))).andReturn(IntegerType.INTEGER.emptyBag());
		replay(context);
		Expression v = desig.evaluate(context);
		assertNotNull(v);
		assertEquals(type.bagType(), v.getEvaluatesTo());
		assertEquals(IntegerType.INTEGER.emptyBag(), v);
		verify(context);
	}
	
	@Test
	public void testMustBePresentFalseAttributeDoesNotExistAndContextHandlerReturnsNull() throws EvaluationException
	{
		AttributeDesignator desig = new AttributeDesignator(AttributeCategories.SUBJECT_RECIPIENT,
				"testId", "testIssuer",  type, false);
		Capture<AttributeDesignatorKey> c = new Capture<AttributeDesignatorKey>();
		expect(context.resolve(capture(c))).andReturn(null);
		replay(context);
		Expression v = desig.evaluate(context);
		assertNotNull(v);
		assertEquals(type.bagType(), v.getEvaluatesTo());
		assertEquals(IntegerType.INTEGER.emptyBag(), v);
		verify(context);
	}
	
	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testMustBePresentTrueContextHandlerThrowsAttributeReferenceEvaluationException() throws EvaluationException
	{
		AttributeDesignator desig = new AttributeDesignator(AttributeCategories.SUBJECT_RECIPIENT,
				"testId", "testIssuer",  type, true);
		Capture<AttributeDesignatorKey> c = new Capture<AttributeDesignatorKey>();
		expect(context.resolve(capture(c))).andThrow(new AttributeReferenceEvaluationException(context, desig.getReferenceKey(), "Errror"));
		replay(context);
		desig.evaluate(context);
		verify(context);
	}
	
	@Test
	public void testMustBePresentFalseContextHandlerThrowsAttributeReferenceEvaluationException() throws EvaluationException
	{
		AttributeDesignator desig = new AttributeDesignator(AttributeCategories.SUBJECT_RECIPIENT,
				"testId", "testIssuer",  type, false);
		Capture<AttributeDesignatorKey> c = new Capture<AttributeDesignatorKey>();
		expect(context.resolve(capture(c))).andThrow(new AttributeReferenceEvaluationException(context, 
				desig.getReferenceKey(), "Errror"));
		replay(context);
		Expression v = desig.evaluate(context);
		assertNotNull(v);
		assertEquals(type.bagType(), v.getEvaluatesTo());
		assertEquals(IntegerType.INTEGER.emptyBag(), v);
		verify(context);
	}
	
	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testMustBePresentTrueContextHandlerThrowsRuntimeException() throws EvaluationException
	{
		AttributeDesignator desig = new AttributeDesignator(AttributeCategories.SUBJECT_RECIPIENT,
				"testId", "testIssuer",  type, true);
		Capture<AttributeDesignatorKey> c = new Capture<AttributeDesignatorKey>();
		expect(context.resolve(capture(c))).andThrow(new NullPointerException("Null"));
		replay(context);
		desig.evaluate(context);
		verify(context);
	}
	
	@Test
	public void testMustBePresentFalseContextHandlerThrowsRuntimeException() throws EvaluationException
	{
		AttributeDesignator desig = new AttributeDesignator(AttributeCategories.SUBJECT_RECIPIENT,
				"testId", "testIssuer",  type, false);
		Capture<AttributeDesignatorKey> c = new Capture<AttributeDesignatorKey>();
		expect(context.resolve(capture(c))).andThrow(new NullPointerException("Null"));
		replay(context);
		Expression v = desig.evaluate(context);
		assertNotNull(v);
		assertEquals(type.bagType(), v.getEvaluatesTo());
		assertEquals(IntegerType.INTEGER.emptyBag(), v);
		verify(context);
	}
}
