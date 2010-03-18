package com.artagon.xacml.v3.policy.impl;

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

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.Expression;
import com.artagon.xacml.v3.policy.AttributeDesignator;
import com.artagon.xacml.v3.policy.AttributeReferenceEvaluationException;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.artagon.xacml.v3.policy.type.IntegerType;

public class DefaultAttributeDesignatorTest
{
	private IntegerType type;
	private EvaluationContext context;
	
	@Before
	public void init(){
		this.type = DataTypes.INTEGER.getType();	
		this.context = createStrictMock(EvaluationContext.class);
	}
	
	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testMustBePresentTrueAttributeDoesNotExist() throws EvaluationException
	{
		AttributeDesignator desig = new DefaultAttributeDesignator(
				AttributeCategoryId.SUBJECT_RECIPIENT, "testId", "testIssuer", DataTypes.INTEGER.getType(), true);
		expect(context.resolve(AttributeCategoryId.SUBJECT_RECIPIENT, 
				"testId", DataTypes.INTEGER.getType(), "testIssuer")).andReturn(DataTypes.INTEGER.emptyBag());
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
		AttributeDesignator desig = new DefaultAttributeDesignator(
				AttributeCategoryId.SUBJECT_RECIPIENT, "testId", "testIssuer", DataTypes.INTEGER.getType(), true);
		expect(context.resolve(AttributeCategoryId.SUBJECT_RECIPIENT, 
				"testId", DataTypes.INTEGER.getType(), "testIssuer")).andReturn(DataTypes.INTEGER.bag(
						DataTypes.INTEGER.create(1), DataTypes.INTEGER.create(2)));
		replay(context);
		Expression v = desig.evaluate(context);
		assertEquals(type.bagOf(), v.getEvaluatesTo());
		assertEquals(DataTypes.INTEGER.bag(
				DataTypes.INTEGER.create(1), DataTypes.INTEGER.create(2)), v);
	}
	
	@Test
	public void testMustBePresentFalseAttributeDoesNotExistWithId() throws EvaluationException
	{
		AttributeDesignator desig = new DefaultAttributeDesignator(AttributeCategoryId.SUBJECT_RECIPIENT,
				"testId", "testIssuer",  type, false);
		expect(context.resolve(AttributeCategoryId.SUBJECT_RECIPIENT, 
				"testId", DataTypes.INTEGER.getType(), "testIssuer")).andReturn(DataTypes.INTEGER.emptyBag());
		replay(context);
		Expression v = desig.evaluate(context);
		assertNotNull(v);
		assertEquals(type.bagOf(), v.getEvaluatesTo());
		assertEquals(DataTypes.INTEGER.emptyBag(), v);
		verify(context);
	}
}
