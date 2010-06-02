package com.artagon.xacml.v3.policy.impl;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeReferenceEvaluationException;
import com.artagon.xacml.v3.AttributeSelector;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.Expression;
import com.artagon.xacml.v3.policy.type.DataTypes;

public class DefaultAttributeSelectorTest
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
		AttributeSelector ref = new DefaultAttributeSelector(
				AttributeCategoryId.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patientDoB/text()", 
				DataTypes.DATE.getType(), true);
		expect(context.resolve(ref)).andReturn(DataTypes.DATE.bag(DataTypes.DATE.fromXacmlString("1992-03-21")));
		replay(context);
		Expression v = ref.evaluate(context);
		assertEquals(DataTypes.DATE.bag(DataTypes.DATE.fromXacmlString("1992-03-21")), v);
		verify(context);
	}
	
	@Test
	public void testMustBePresenFalseAndReturnsNonEmptyBag() throws EvaluationException
	{
		AttributeSelector ref = new DefaultAttributeSelector(
				AttributeCategoryId.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patientDoB/text()", 
				DataTypes.DATE.getType(), false);
		expect(context.resolve(ref)).andReturn(DataTypes.DATE.bag(DataTypes.DATE.fromXacmlString("1992-03-21")));
		replay(context);
		Expression v = ref.evaluate(context);
		assertEquals(DataTypes.DATE.bag(DataTypes.DATE.fromXacmlString("1992-03-21")), v);
		verify(context);
	}
	
	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testMustBePresenTrueAndReturnsEmptyBag() throws EvaluationException
	{
		AttributeSelector ref = new DefaultAttributeSelector(
				AttributeCategoryId.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patientDoB/text()", 
				DataTypes.DATE.getType(), true);
		expect(context.resolve(ref)).andReturn(DataTypes.DATE.emptyBag());
		replay(context);
		ref.evaluate(context);
		verify(context);
	}
	
	@Test
	public void testMustBePresenFalseAndReturnsEmptyBag() throws EvaluationException
	{
		AttributeSelector ref = new DefaultAttributeSelector(
				AttributeCategoryId.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patientDoB/text()", 
				DataTypes.DATE.getType(), false);
		expect(context.resolve(ref)).andReturn(DataTypes.DATE.emptyBag());
		replay(context);
		Expression v = ref.evaluate(context);
		assertEquals(v, DataTypes.DATE.emptyBag());
		verify(context);
	}
	
	
	
	
}

