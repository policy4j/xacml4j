package com.artagon.xacml.v3;

import static com.artagon.xacml.v3.types.BooleanType.BOOLEAN;
import static com.artagon.xacml.v3.types.IntegerType.INTEGER;
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

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.AttributeAssignment;
import com.artagon.xacml.v3.AttributeAssignmentExpression;
import com.artagon.xacml.v3.AttributeCategories;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.Effect;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.Obligation;
import com.artagon.xacml.v3.ObligationExpression;
import com.artagon.xacml.v3.XacmlException;

public class ObligationExpressionTest 
{
	private EvaluationContext context;
	
	@Before
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
		expect(attrExp0.getAttributeId()).andReturn("attributeId0").times(2);
		expect(attrExp0.getCategory()).andReturn(AttributeCategories.SUBJECT_ACCESS);
		expect(attrExp0.getIssuer()).andReturn("issuer0");
		expect(attrExp0.evaluate(context)).andReturn(INTEGER.create(1));
		expect(attrExp1.getAttributeId()).andReturn("attributeId1").times(2);
		expect(attrExp1.getCategory()).andReturn(AttributeCategories.RESOURCE);
		expect(attrExp1.getIssuer()).andReturn("issuer1");
		expect(attrExp1.evaluate(context)).andReturn(BOOLEAN.create(false));
		replay(attrExp0, attrExp1, context);
		ObligationExpression exp = new ObligationExpression("test", Effect.DENY, Arrays.asList(attrExp0, attrExp1));
		Obligation obligation = exp.evaluate(context);
		Iterator<AttributeAssignment> it = obligation.getAttributes().iterator();
		AttributeAssignment a0 = it.next();
		assertEquals("issuer0", a0.getIssuer());
		assertEquals("attributeId0", a0.getAttributeId());
		assertEquals(AttributeCategories.SUBJECT_ACCESS, a0.getCategory());
		assertEquals(INTEGER.create(1), a0.getAttribute());
		AttributeAssignment a1 = it.next();
		assertEquals("issuer1", a1.getIssuer());
		assertEquals("attributeId1", a1.getAttributeId());
		assertEquals(AttributeCategories.RESOURCE, a1.getCategory());
		assertEquals(BOOLEAN.create(false), a1.getAttribute());
		verify(attrExp0, attrExp1, context);
	}
}
