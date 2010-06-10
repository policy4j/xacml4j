package com.artagon.xacml.v3;

import static org.easymock.EasyMock.createMock;
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

import com.artagon.xacml.v3.types.XacmlDataTypes;

public class DefaultObligationExpressionTest 
{
	private EvaluationContext context;
	
	public void init(){
		this.context = createMock(EvaluationContext.class);
	}
	
	@Test
	public void testCreateObligationExpression() throws XacmlException
	{
		AttributeAssignmentExpression attrExp = createMock(AttributeAssignmentExpression.class);
		ObligationExpression exp = new ObligationExpression("test",Effect.DENY, Collections.singletonList(attrExp));
		assertTrue(exp.isApplicable(Decision.DENY));
		assertFalse(exp.isApplicable(Decision.PERMIT));
		assertFalse(exp.isApplicable(Decision.INDETERMINATE));
		assertEquals("test", exp.getId());
	}
	
	@Test
	public void testEvaluateObligationExpression() throws XacmlException
	{
		AttributeAssignmentExpression attrExp0 = createMock(AttributeAssignmentExpression.class);
		AttributeAssignmentExpression attrExp1 = createMock(AttributeAssignmentExpression.class);
		ObligationExpression exp = new ObligationExpression("test",Effect.DENY, Arrays.asList(attrExp0, attrExp1));
		expect(attrExp0.getAttributeId()).andReturn("attributeId0");
		expect(attrExp0.getCategory()).andReturn(AttributeCategoryId.SUBJECT_ACCESS);
		expect(attrExp0.getIssuer()).andReturn("issuer0");
		expect(attrExp0.evaluate(context)).andReturn(XacmlDataTypes.INTEGER.create(1));
		expect(attrExp1.getAttributeId()).andReturn("attributeId1");
		expect(attrExp1.getCategory()).andReturn(AttributeCategoryId.RESOURCE);
		expect(attrExp1.getIssuer()).andReturn("issuer1");
		expect(attrExp1.evaluate(context)).andReturn(XacmlDataTypes.BOOLEAN.create(false));
		replay(attrExp0, attrExp1);
		Obligation obligation = exp.evaluate(context);
		Iterator<AttributeAssignment> it = obligation.getAttributes().iterator();
		AttributeAssignment a0 = it.next();
		assertEquals("issuer0", a0.getIssuer());
		assertEquals("attributeId0", a0.getAttributeId());
		assertEquals(AttributeCategoryId.SUBJECT_ACCESS, a0.getCategory());
		assertEquals(XacmlDataTypes.INTEGER.create(1), a0.getAttribute());
		AttributeAssignment a1 = it.next();
		assertEquals("issuer1", a1.getIssuer());
		assertEquals("attributeId1", a1.getAttributeId());
		assertEquals(AttributeCategoryId.RESOURCE, a1.getCategory());
		assertEquals(XacmlDataTypes.BOOLEAN.create(false), a1.getAttribute());
		verify(attrExp0, attrExp1);
	}
}
