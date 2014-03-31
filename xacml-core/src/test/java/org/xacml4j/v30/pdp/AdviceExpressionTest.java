package org.xacml4j.v30.pdp;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.Advice;
import org.xacml4j.v30.AttributeAssignment;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.types.BooleanExp;
import org.xacml4j.v30.types.IntegerExp;


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
		AdviceExpression exp = AdviceExpression.builder("test",Effect.DENY).attribute(attrExp0).build();
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
		expect(attrExp0.getCategory()).andReturn(Categories.SUBJECT_ACCESS);
		expect(attrExp0.getIssuer()).andReturn("issuer0");
		expect(attrExp0.evaluate(context)).andReturn(IntegerExp.valueOf(1));

		expect(attrExp1.getAttributeId()).andReturn("attributeId1").times(2);
		expect(attrExp1.getCategory()).andReturn(Categories.RESOURCE).times(1);
		expect(attrExp1.getIssuer()).andReturn("issuer1").times(1);
		expect(attrExp1.evaluate(context)).andReturn(BooleanExp.bag().value(false, true).build());

		c.replay();

		AdviceExpression exp = AdviceExpression.builder("test",Effect.DENY).attribute(attrExp0, attrExp1).build();
		Advice advice = exp.evaluate(context);
		Iterator<AttributeAssignment> it = advice.getAttributes().iterator();
		AttributeAssignment a0 = it.next();
		assertEquals("issuer0", a0.getIssuer());
		assertEquals("attributeId0", a0.getAttributeId());
		assertEquals(Effect.DENY, advice.getFulfillOn());
		assertEquals(Categories.SUBJECT_ACCESS, a0.getCategory());
		assertEquals(IntegerExp.valueOf(1), a0.getAttribute());
		AttributeAssignment a1 = it.next();
		assertEquals("issuer1", a1.getIssuer());
		assertEquals("attributeId1", a1.getAttributeId());
		assertEquals(Categories.RESOURCE, a1.getCategory());
		assertEquals(BooleanExp.valueOf(false), a1.getAttribute());

		c.verify();
	}
}
