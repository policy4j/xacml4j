package com.artagon.xacml.v30;

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

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.Advice;
import com.artagon.xacml.v30.AdviceExpression;
import com.artagon.xacml.v30.AttributeAssignment;
import com.artagon.xacml.v30.AttributeAssignmentExpression;
import com.artagon.xacml.v30.AttributeCategories;
import com.artagon.xacml.v30.Decision;
import com.artagon.xacml.v30.Effect;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.XacmlException;
import com.artagon.xacml.v30.types.BooleanType;
import com.artagon.xacml.v30.types.IntegerType;

public class AdviceExpressionTest 
{
	private EvaluationContext context;
	
	@Before
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
		expect(attrExp0.getAttributeId()).andReturn("attributeId0").times(2);
		expect(attrExp0.getCategory()).andReturn(AttributeCategories.SUBJECT_ACCESS);
		expect(attrExp0.getIssuer()).andReturn("issuer0");
		expect(attrExp0.evaluate(context)).andReturn(IntegerType.INTEGER.create(1));
		expect(attrExp1.getAttributeId()).andReturn("attributeId1").times(2);
		expect(attrExp1.getCategory()).andReturn(AttributeCategories.RESOURCE);
		expect(attrExp1.getIssuer()).andReturn("issuer1");
		expect(attrExp1.evaluate(context)).andReturn(BooleanType.BOOLEAN.create(false));
		replay(attrExp0, attrExp1, context);
		AdviceExpression exp = new AdviceExpression("test",Effect.DENY, Arrays.asList(attrExp0, attrExp1));
		Advice advice = exp.evaluate(context);
		Iterator<AttributeAssignment> it = advice.getAttributes().iterator();
		AttributeAssignment a0 = it.next();
		assertEquals("issuer0", a0.getIssuer());
		assertEquals("attributeId0", a0.getAttributeId());
		assertEquals(AttributeCategories.SUBJECT_ACCESS, a0.getCategory());
		assertEquals(IntegerType.INTEGER.create(1), a0.getAttribute());
		AttributeAssignment a1 = it.next();
		assertEquals("issuer1", a1.getIssuer());
		assertEquals("attributeId1", a1.getAttributeId());
		assertEquals(AttributeCategories.RESOURCE, a1.getCategory());
		assertEquals(BooleanType.BOOLEAN.create(false), a1.getAttribute());
		verify(attrExp0, attrExp1, context);
	}
}
