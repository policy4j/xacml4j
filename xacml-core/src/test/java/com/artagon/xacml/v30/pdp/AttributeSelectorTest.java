package com.artagon.xacml.v30.pdp;

import static com.artagon.xacml.v30.types.DateType.DATE;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.AttributeCategories;
import com.artagon.xacml.v30.AttributeSelectorKey;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.EvaluationException;
import com.artagon.xacml.v30.Expression;
import com.artagon.xacml.v30.StatusCode;
import com.artagon.xacml.v30.types.DateType;

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
				AttributeCategories.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patientDoB/text()", 
				DATE, true);
		Capture<AttributeSelectorKey> c = new Capture<AttributeSelectorKey>();
		expect(context.resolve(capture(c))).andReturn(DateType.DATE.bagOf(DateType.DATE.fromXacmlString("1992-03-21")));
		replay(context);
		Expression v = ref.evaluate(context);
		assertEquals(DateType.DATE.bagOf(DateType.DATE.fromXacmlString("1992-03-21")), v);
		assertEquals(ref.getReferenceKey(), c.getValue());
		verify(context);
	}
	
	@Test
	public void testMustBePresenFalseAndReturnsNonEmptyBag() throws EvaluationException
	{
		AttributeSelector ref = new AttributeSelector(
				AttributeCategories.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patientDoB/text()", 
				DATE, false);
		Capture<AttributeSelectorKey> c = new Capture<AttributeSelectorKey>();
		expect(context.resolve(capture(c))).andReturn(
				DateType.DATE.bagOf(DateType.DATE.fromXacmlString("1992-03-21")));
		replay(context);
		Expression v = ref.evaluate(context);
		assertEquals(DateType.DATE.bagOf(DateType.DATE.fromXacmlString("1992-03-21")), v);
		assertEquals(ref.getReferenceKey(), c.getValue());
		verify(context);
	}
	
	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testMustBePresenTrueAndReturnsEmptyBag() throws EvaluationException
	{
		AttributeSelector ref = new AttributeSelector(
				AttributeCategories.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patientDoB/text()", 
				DATE, true);
		Capture<AttributeSelectorKey> c = new Capture<AttributeSelectorKey>();
		expect(context.resolve(capture(c))).andReturn(DATE.emptyBag());
		replay(context);
		ref.evaluate(context);
		verify(context);
	}
	
	@Test
	public void testMustBePresenFalseAndReturnsEmptyBag() throws EvaluationException
	{
		AttributeSelector ref = new AttributeSelector(
				AttributeCategories.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patientDoB/text()", 
				DATE, false);
		Capture<AttributeSelectorKey> c = new Capture<AttributeSelectorKey>();
		expect(context.resolve(capture(c))).andReturn(DATE.emptyBag());
		replay(context);
		Expression v = ref.evaluate(context);
		assertEquals(v, DATE.emptyBag());
		assertEquals(ref.getReferenceKey(), c.getValue());
		verify(context);
	}
	
	@Test
	public void testMustBePresenFalseAndContextThrowsAttributeReferenceEvaluationException() throws EvaluationException
	{
		AttributeSelector ref = new AttributeSelector(
				AttributeCategories.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patientDoB/text()", 
				DATE, false);
		Capture<AttributeSelectorKey> c = new Capture<AttributeSelectorKey>();
		expect(context.resolve(capture(c))).andThrow(new AttributeReferenceEvaluationException(context, 
				new AttributeSelectorKey(AttributeCategories.SUBJECT_RECIPIENT, 
						"/md:record/md:patient/md:patientDoB/text()", 
						DATE, null), 
				StatusCode.createProcessingError(), new NullPointerException()));
		replay(context);
		Expression v = ref.evaluate(context);
		assertEquals(v, DATE.emptyBag());
		assertEquals(ref.getReferenceKey(), c.getValue());
		verify(context);
	}
	
	@Test
	public void testMustBePresenFalseAndContextThrowsRuntimeException() throws EvaluationException
	{
		AttributeSelector ref = new AttributeSelector(
				AttributeCategories.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patientDoB/text()", 
				DATE, false);
		Capture<AttributeSelectorKey> c = new Capture<AttributeSelectorKey>();
		expect(context.resolve(capture(c))).andThrow(new NullPointerException());
		replay(context);
		Expression v = ref.evaluate(context);
		assertEquals(v, DATE.emptyBag());
		assertEquals(ref.getReferenceKey(), c.getValue());
		verify(context);
	}
	
	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testMustBePresenTrueAndContextThrowsRuntimeException() throws EvaluationException
	{
		AttributeSelector ref = new AttributeSelector(
				AttributeCategories.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patientDoB/text()", 
				DATE, true);
		Capture<AttributeSelectorKey> c = new Capture<AttributeSelectorKey>();
		expect(context.resolve(capture(c))).andThrow(new NullPointerException());
		replay(context);
		ref.evaluate(context);
		verify(context);
	}
	
	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testMustBePresenTrueAndContextThrowsAttributeReferenceEvaluationException() throws EvaluationException
	{
		AttributeSelector ref = new AttributeSelector(
				AttributeCategories.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patientDoB/text()", 
				DATE, true);
		Capture<AttributeSelectorKey> c = new Capture<AttributeSelectorKey>();
		expect(context.resolve(capture(c))).andThrow(new AttributeReferenceEvaluationException(context, 
				new AttributeSelectorKey(
				AttributeCategories.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patientDoB/text()", 
				DATE, null), 
				StatusCode.createProcessingError(), new NullPointerException()));
		replay(context);
		ref.evaluate(context);
		verify(context);
	}
}

