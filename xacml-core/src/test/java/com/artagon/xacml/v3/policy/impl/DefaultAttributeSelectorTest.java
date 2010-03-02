package com.artagon.xacml.v3.policy.impl;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import javax.xml.xpath.XPath;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.policy.AttributeSelector;
import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.EvaluationException;
import com.artagon.xacml.v3.policy.Expression;
import com.artagon.xacml.v3.policy.type.DataTypes;

public class DefaultAttributeSelectorTest
{
	private EvaluationContext context;
	
	@Before
	public void init(){
		this.context = createStrictMock(EvaluationContext.class);
	}
	
	@Test(expected=EvaluationException.class)
	public void testMustBePresentTrueAttributeDoesNotExist() throws EvaluationException
	{
		XPath xpath = createStrictMock(XPath.class);
		AttributeSelector desig = new DefaultAttributeSelector(
				AttributeCategoryId.SUBJECT_RECIPIENT, xpath, DataTypes.INTEGER.getType(), true);
		expect(context.resolveAttributeSelector(
				AttributeCategoryId.SUBJECT_RECIPIENT, xpath, DataTypes.INTEGER.getType())).andReturn(
						DataTypes.INTEGER.emptyBag());
		replay(context);
		desig.evaluate(context);
		verify(context);
	}
	
	public void testMustBePresentTrueAttributeDoesExist() throws EvaluationException
	{
		XPath xpath = createStrictMock(XPath.class);
		AttributeSelector desig = new DefaultAttributeSelector(
				AttributeCategoryId.SUBJECT_RECIPIENT, xpath, DataTypes.INTEGER.getType(), true);
		expect(context.resolveAttributeSelector(
				AttributeCategoryId.SUBJECT_RECIPIENT, xpath, DataTypes.INTEGER.getType())).andReturn(
						DataTypes.INTEGER.bag(DataTypes.INTEGER.create(1), DataTypes.INTEGER.create(2)));
		replay(context);
		Expression v = desig.evaluate(context);
		assertEquals(DataTypes.INTEGER.bag(DataTypes.INTEGER.create(1), DataTypes.INTEGER.create(2)), v);
		verify(context);
	}
	
	public void testMustBePresentFalseAttributeDoesNotExist() throws EvaluationException
	{
		XPath xpath = createStrictMock(XPath.class);
		AttributeSelector desig = new DefaultAttributeSelector(
				AttributeCategoryId.SUBJECT_RECIPIENT, xpath, DataTypes.INTEGER.getType(), true);
		expect(context.resolveAttributeSelector(
				AttributeCategoryId.SUBJECT_RECIPIENT, xpath, DataTypes.INTEGER.getType())).andReturn(
						DataTypes.INTEGER.emptyBag());
		replay(context);
		Expression v = desig.evaluate(context);
		assertEquals(DataTypes.INTEGER.emptyBag(), v);
		verify(context);
	}
	
}

