package org.xacml4j.v30.pdp;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.xacml4j.v30.types.DateType.DATE;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.AttributeCategories;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.StatusCode;
import org.xacml4j.v30.types.DateType;


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
		AttributeSelector ref = AttributeSelector
				.builder()
				.category(AttributeCategories.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient/md:patientDoB/text()")
				.dataType(DATE)
				.mustBePresent(true)
				.build();
		Capture<AttributeSelectorKey> c = new Capture<AttributeSelectorKey>();
		expect(context.resolve(capture(c))).andReturn(DateType.DATE.bagOf(DateType.DATE.create("1992-03-21")));
		replay(context);
		Expression v = ref.evaluate(context);
		assertEquals(DateType.DATE.bagOf(DateType.DATE.create("1992-03-21")), v);
		assertEquals(ref.getReferenceKey(), c.getValue());
		verify(context);
	}

	@Test
	public void testMustBePresenFalseAndReturnsNonEmptyBag() throws EvaluationException
	{
		AttributeSelector ref = AttributeSelector
				.builder()
				.category(AttributeCategories.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient/md:patientDoB/text()")
				.dataType(DATE)
				.mustBePresent(false)
				.build();
		Capture<AttributeSelectorKey> c = new Capture<AttributeSelectorKey>();
		expect(context.resolve(capture(c))).andReturn(
				DateType.DATE.bagOf(DateType.DATE.create("1992-03-21")));
		replay(context);
		Expression v = ref.evaluate(context);
		assertEquals(DateType.DATE.bagOf(DateType.DATE.create("1992-03-21")), v);
		assertEquals(ref.getReferenceKey(), c.getValue());
		verify(context);
	}

	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testMustBePresenTrueAndReturnsEmptyBag() throws EvaluationException
	{
		AttributeSelector ref = AttributeSelector
				.builder()
				.category(AttributeCategories.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient/md:patientDoB/text()")
				.dataType(DATE)
				.mustBePresent(true)
				.build();
		Capture<AttributeSelectorKey> c = new Capture<AttributeSelectorKey>();
		expect(context.resolve(capture(c))).andReturn(DATE.emptyBag());
		replay(context);
		ref.evaluate(context);
		verify(context);
	}

	@Test
	public void testMustBePresenFalseAndReturnsEmptyBag() throws EvaluationException
	{
		AttributeSelector ref = AttributeSelector
				.builder()
				.category(AttributeCategories.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient/md:patientDoB/text()")
				.dataType(DATE)
				.mustBePresent(false)
				.build();
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
		AttributeSelector ref = AttributeSelector
				.builder()
				.category(AttributeCategories.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient/md:patientDoB/text()")
				.dataType(DATE)
				.mustBePresent(false)
				.build();

		Capture<AttributeSelectorKey> c = new Capture<AttributeSelectorKey>();
		expect(context.resolve(capture(c))).andThrow(
				new AttributeReferenceEvaluationException(context, ref.getReferenceKey(),
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
		AttributeSelector ref = AttributeSelector
				.builder()
				.category(AttributeCategories.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient/md:patientDoB/text()")
				.dataType(DATE)
				.mustBePresent(false)
				.build();
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
		AttributeSelector ref = AttributeSelector
				.builder()
				.category(AttributeCategories.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient/md:patientDoB/text()")
				.dataType(DATE)
				.mustBePresent(true)
				.build();
		Capture<AttributeSelectorKey> c = new Capture<AttributeSelectorKey>();
		expect(context.resolve(capture(c))).andThrow(new NullPointerException());
		replay(context);
		ref.evaluate(context);
		verify(context);
	}

	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testMustBePresenTrueAndContextThrowsAttributeReferenceEvaluationException() throws EvaluationException
	{
		AttributeSelector ref = AttributeSelector
				.builder()
				.category(AttributeCategories.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient/md:patientDoB/text()")
				.dataType(DATE)
				.mustBePresent(true)
				.build();
		Capture<AttributeSelectorKey> c = new Capture<AttributeSelectorKey>();
		expect(context.resolve(capture(c))).andThrow(
				new AttributeReferenceEvaluationException(context,
						ref.getReferenceKey(), StatusCode.createProcessingError(), new NullPointerException()));
		replay(context);
		ref.evaluate(context);
		verify(context);
	}
}

