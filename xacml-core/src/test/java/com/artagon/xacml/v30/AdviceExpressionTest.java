package com.artagon.xacml.v30;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.core.AttributeCategories;
import com.artagon.xacml.v30.types.BooleanType;
import com.artagon.xacml.v30.types.IntegerType;

public class AdviceExpressionTest 
{
	private EvaluationContext context;
	private AttributeAssignmentExpression attrExp0;
	private AttributeAssignmentExpression attrExp1;
	private IMocksControl c;
	
	@Before
	public void init(){
		this.c = createControl();
		this.context = createStrictMock(EvaluationContext.class);
		this.attrExp0 = c.createMock(AttributeAssignmentExpression.class);
		this.attrExp1 = c.createMock(AttributeAssignmentExpression.class);
		this.context = c.createMock(EvaluationContext.class);
	}
	
	@Test
	public void testCreateAdviceExpression() throws XacmlException
	{
		expect(attrExp0.getAttributeId()).andReturn("testId0");
		c.replay();
		AdviceExpression exp = new AdviceExpression("test",Effect.DENY, Collections.singletonList(attrExp0));
		assertTrue(exp.isApplicable(Decision.DENY));
		assertFalse(exp.isApplicable(Decision.PERMIT));
		assertFalse(exp.isApplicable(Decision.INDETERMINATE));
		assertEquals("test", exp.getId());
		c.verify();
	}
	
	@Test
	public void testEvaluateAdviceExpression() throws XacmlException
	{
	
		expect(attrExp0.getAttributeId()).andReturn("attributeId0").times(2);
		expect(attrExp0.getCategory()).andReturn(AttributeCategories.SUBJECT_ACCESS);
		expect(attrExp0.getIssuer()).andReturn("issuer0");
		expect(attrExp0.evaluate(context)).andReturn(IntegerType.INTEGER.create(1));
		
		expect(attrExp1.getAttributeId()).andReturn("attributeId1").times(3);
		expect(attrExp1.getCategory()).andReturn(AttributeCategories.RESOURCE).times(2);
		expect(attrExp1.getIssuer()).andReturn("issuer1").times(2);
		expect(attrExp1.evaluate(context)).andReturn(BooleanType.BOOLEAN.bagOf(false, true));
		
		c.replay();
		
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
		
		c.verify();
	}
}
