package org.xacml4j.v30.pdp;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.spi.repository.PolicyReferenceResolver;
import org.xacml4j.v30.types.StringExp;
import org.xacml4j.v30.types.XacmlTypes;


public class RootEvaluationContextTest
{
	private EvaluationContextHandler handler;
	private PolicyReferenceResolver resolver;
	private IMocksControl c;

	@Before
	public void init(){
		this.c = createControl();
		this.handler = c.createMock(EvaluationContextHandler.class);
		this.resolver = c.createMock(PolicyReferenceResolver.class);
	}

	@Test
	public void testSetAndGetDecisionCacheTTLWithDefaultTTLZero()
	{
		RootEvaluationContext context = new RootEvaluationContext(false, 0, resolver, handler);
		c.replay();
		assertEquals(0, context.getDecisionCacheTTL());
		context.setDecisionCacheTTL(20);
		assertEquals(20, context.getDecisionCacheTTL());
		context.setDecisionCacheTTL(10);
		assertEquals(10, context.getDecisionCacheTTL());
		context.setDecisionCacheTTL(-1);
		assertEquals(0, context.getDecisionCacheTTL());
		context.setDecisionCacheTTL(10);
		assertEquals(0, context.getDecisionCacheTTL());
		c.verify();
	}

	@Test
	public void testSetAndGetDecisionCacheTTLWithDefaultTTL()
	{
		RootEvaluationContext context = new RootEvaluationContext(false, 10, resolver, handler);
		c.replay();
		assertEquals(10, context.getDecisionCacheTTL());
		context.setDecisionCacheTTL(20);
		assertEquals(10, context.getDecisionCacheTTL());
		context.setDecisionCacheTTL(5);
		assertEquals(5, context.getDecisionCacheTTL());
		context.setDecisionCacheTTL(-1);
		assertEquals(0, context.getDecisionCacheTTL());
		context.setDecisionCacheTTL(10);
		assertEquals(0, context.getDecisionCacheTTL());
		c.verify();
	}

	@Test
	public void testResolveDesignatorValueValueIsInContext() throws EvaluationException
	{
		RootEvaluationContext context = new RootEvaluationContext(false, 0, resolver, handler);
		c.replay();
		AttributeDesignatorKey k = AttributeDesignatorKey
				.builder()
				.category(Categories.SUBJECT_ACCESS)
				.attributeId("testId")
				.dataType(XacmlTypes.STRING)
				.issuer("test")
				.build();
		context.setResolvedDesignatorValue(k, StringExp.valueOf("aaa").toBag());
		assertEquals(StringExp.valueOf("aaa").toBag(), context.resolve(k));
		c.verify();
	}

	@Test
	public void testResolveDesignatorValueValueIsNotInContext() throws EvaluationException
	{
		RootEvaluationContext context = new RootEvaluationContext(false, 0, resolver, handler);
		AttributeDesignatorKey k = AttributeDesignatorKey
				.builder()
				.category(Categories.SUBJECT_ACCESS)
				.attributeId("testId")
				.dataType(XacmlTypes.STRING)
				.issuer("test")
				.build();
		expect(handler.resolve(context, k)).andReturn(StringExp.bag().value("aaa", "ccc").build());

		c.replay();
		assertEquals(StringExp.bag().value("aaa", "ccc").build(), context.resolve(k));
		assertEquals(StringExp.bag().value("aaa", "ccc").build(), context.resolve(k));
		c.verify();
	}
}
