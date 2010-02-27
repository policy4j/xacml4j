package com.artagon.xacml.v3.policy.impl;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Test;

import com.artagon.xacml.v3.Advice;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.policy.AdviceExpression;
import com.artagon.xacml.v3.policy.AttributeAssigmentExpression;
import com.artagon.xacml.v3.policy.Effect;
import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.EvaluationException;
import com.artagon.xacml.v3.policy.type.DataTypes;

public class DefaultAdviceExpressionTest 
{
	private EvaluationContext context;
	
	public void init(){
		this.context = createStrictMock(EvaluationContext.class);
	}
	
	@Test
	public void testCreateAdviceExpression()
	{
		AttributeAssigmentExpression attrExp = createStrictMock(AttributeAssigmentExpression.class);
		AdviceExpression exp = new DefaultAdviceExpression("test",Effect.DENY, Collections.singletonList(attrExp));
		assertTrue(exp.isApplicable(Decision.DENY));
		assertFalse(exp.isApplicable(Decision.PERMIT));
		assertFalse(exp.isApplicable(Decision.INDETERMINATE));
		assertEquals("test", exp.getId());
	}
	
	@Test
	public void testEvaluateAdviceExpression() throws EvaluationException
	{
		AttributeAssigmentExpression attrExp = createStrictMock(AttributeAssigmentExpression.class);
		AdviceExpression exp = new DefaultAdviceExpression("test",Effect.DENY, Collections.singletonList(attrExp));
		expect(attrExp.getAttributeId()).andReturn("attributeId");
		expect(attrExp.getCategory()).andReturn(AttributeCategoryId.SUBJECT_ACCESS);
		expect(attrExp.getIssuer()).andReturn("issuer");
		expect(attrExp.evaluate(context)).andReturn(DataTypes.INTEGER.create(1));
		replay(attrExp);
		Advice advice = exp.evaluate(context);
		assertEquals(1, advice.getAttributes().size());
		verify(attrExp);
	}
}
