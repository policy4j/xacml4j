package com.artagon.xacml.v3.policy.impl;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.junit.Test;

import com.artagon.xacml.v3.Advice;
import com.artagon.xacml.v3.AdviceExpression;
import com.artagon.xacml.v3.AttributeAssignment;
import com.artagon.xacml.v3.AttributeAssignmentExpression;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.Effect;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.XacmlException;
import com.artagon.xacml.v3.policy.type.DataTypes;

public class AdviceExpressionTest 
{
	private EvaluationContext context;
	
	public void init(){
		this.context = createStrictMock(EvaluationContext.class);
	}
	
	@Test
	public void testCreateAdviceExpression() throws XacmlException
	{
		AttributeAssignmentExpression attrExp = createStrictMock(AttributeAssignmentExpression.class);
		AdviceExpression exp = new AdviceExpression("test",Effect.DENY, Collections.singletonList(attrExp));
		assertTrue(exp.isApplicable(Decision.DENY));
		assertFalse(exp.isApplicable(Decision.PERMIT));
		assertFalse(exp.isApplicable(Decision.INDETERMINATE));
		assertEquals("test", exp.getId());
	}
	
	@Test
	public void testEvaluateAdviceExpression() throws XacmlException
	{
		AttributeAssignmentExpression attrExp0 = createStrictMock(AttributeAssignmentExpression.class);
		AttributeAssignmentExpression attrExp1 = createStrictMock(AttributeAssignmentExpression.class);
		AdviceExpression exp = new AdviceExpression("test",Effect.DENY, Arrays.asList(attrExp0, attrExp1));
		expect(attrExp0.getAttributeId()).andReturn("attributeId0");
		expect(attrExp0.getCategory()).andReturn(AttributeCategoryId.SUBJECT_ACCESS);
		expect(attrExp0.getIssuer()).andReturn("issuer0");
		expect(attrExp0.evaluate(context)).andReturn(DataTypes.INTEGER.create(1));
		expect(attrExp1.getAttributeId()).andReturn("attributeId1");
		expect(attrExp1.getCategory()).andReturn(AttributeCategoryId.RESOURCE);
		expect(attrExp1.getIssuer()).andReturn("issuer1");
		expect(attrExp1.evaluate(context)).andReturn(DataTypes.BOOLEAN.create(false));
		replay(attrExp0, attrExp1);
		Advice advice = exp.evaluate(context);
		Iterator<AttributeAssignment> it = advice.getAttributes().iterator();
		AttributeAssignment a0 = it.next();
		assertEquals("issuer0", a0.getIssuer());
		assertEquals("attributeId0", a0.getAttributeId());
		assertEquals(AttributeCategoryId.SUBJECT_ACCESS, a0.getCategory());
		assertEquals(DataTypes.INTEGER.create(1), a0.getAttribute());
		AttributeAssignment a1 = it.next();
		assertEquals("issuer1", a1.getIssuer());
		assertEquals("attributeId1", a1.getAttributeId());
		assertEquals(AttributeCategoryId.RESOURCE, a1.getCategory());
		assertEquals(DataTypes.BOOLEAN.create(false), a1.getAttribute());
		verify(attrExp0, attrExp1);
	}
}
