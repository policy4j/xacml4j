package com.artagon.xacml.v3;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.types.XacmlDataTypes;

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
		AttributeSelector ref = new AttributeSelector(
				AttributeCategoryId.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patientDoB/text()", 
				XacmlDataTypes.DATE.getDataType(), true);
		expect(context.resolve(ref)).andReturn(XacmlDataTypes.DATE.bagOf(XacmlDataTypes.DATE.fromXacmlString("1992-03-21")));
		replay(context);
		Expression v = ref.evaluate(context);
		assertEquals(XacmlDataTypes.DATE.bagOf(XacmlDataTypes.DATE.fromXacmlString("1992-03-21")), v);
		verify(context);
	}
	
	@Test
	public void testMustBePresenFalseAndReturnsNonEmptyBag() throws EvaluationException
	{
		AttributeSelector ref = new AttributeSelector(
				AttributeCategoryId.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patientDoB/text()", 
				XacmlDataTypes.DATE.getDataType(), false);
		expect(context.resolve(ref)).andReturn(XacmlDataTypes.DATE.bagOf(XacmlDataTypes.DATE.fromXacmlString("1992-03-21")));
		replay(context);
		Expression v = ref.evaluate(context);
		assertEquals(XacmlDataTypes.DATE.bagOf(XacmlDataTypes.DATE.fromXacmlString("1992-03-21")), v);
		verify(context);
	}
	
	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testMustBePresenTrueAndReturnsEmptyBag() throws EvaluationException
	{
		AttributeSelector ref = new AttributeSelector(
				AttributeCategoryId.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patientDoB/text()", 
				XacmlDataTypes.DATE.getDataType(), true);
		expect(context.resolve(ref)).andReturn(XacmlDataTypes.DATE.emptyBag());
		replay(context);
		ref.evaluate(context);
		verify(context);
	}
	
	@Test
	public void testMustBePresenFalseAndReturnsEmptyBag() throws EvaluationException
	{
		AttributeSelector ref = new AttributeSelector(
				AttributeCategoryId.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patientDoB/text()", 
				XacmlDataTypes.DATE.getDataType(), false);
		expect(context.resolve(ref)).andReturn(XacmlDataTypes.DATE.emptyBag());
		replay(context);
		Expression v = ref.evaluate(context);
		assertEquals(v, XacmlDataTypes.DATE.emptyBag());
		verify(context);
	}
	
	@Test
	public void testMustBePresenFalseAndContextThrowsAttributeReferenceEvaluationException() throws EvaluationException
	{
		AttributeSelector ref = new AttributeSelector(
				AttributeCategoryId.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patientDoB/text()", 
				XacmlDataTypes.DATE.getDataType(), false);
		expect(context.resolve(ref)).andThrow(new AttributeReferenceEvaluationException(context, ref, 
				StatusCode.createProcessingError(), new NullPointerException()));
		replay(context);
		Expression v = ref.evaluate(context);
		assertEquals(v, XacmlDataTypes.DATE.emptyBag());
		verify(context);
	}
	
	@Test
	public void testMustBePresenFalseAndContextThrowsRuntimeException() throws EvaluationException
	{
		AttributeSelector ref = new AttributeSelector(
				AttributeCategoryId.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patientDoB/text()", 
				XacmlDataTypes.DATE.getDataType(), false);
		expect(context.resolve(ref)).andThrow(new NullPointerException());
		replay(context);
		Expression v = ref.evaluate(context);
		assertEquals(v, XacmlDataTypes.DATE.emptyBag());
		verify(context);
	}
	
	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testMustBePresenTrueAndContextThrowsRuntimeException() throws EvaluationException
	{
		AttributeSelector ref = new AttributeSelector(
				AttributeCategoryId.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patientDoB/text()", 
				XacmlDataTypes.DATE.getDataType(), true);
		expect(context.resolve(ref)).andThrow(new NullPointerException());
		replay(context);
		ref.evaluate(context);
		verify(context);
	}
	
	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testMustBePresenTrueAndContextThrowsAttributeReferenceEvaluationException() throws EvaluationException
	{
		AttributeSelector ref = new AttributeSelector(
				AttributeCategoryId.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patientDoB/text()", 
				XacmlDataTypes.DATE.getDataType(), true);
		expect(context.resolve(ref)).andThrow(new AttributeReferenceEvaluationException(context, ref, 
				StatusCode.createProcessingError(), new NullPointerException()));
		replay(context);
		ref.evaluate(context);
		verify(context);
	}
}

