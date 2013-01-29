package org.xacml4j.v30.pdp;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.AttributeCategories;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.spi.repository.PolicyReferenceResolver;
import org.xacml4j.v30.types.StringType;


public class BaseEvaluationContextTest 
{
	private EvaluationContext context;
	private EvaluationContextHandler handler;
	private PolicyReferenceResolver resolver;
	private IMocksControl c;
	
	@Before
	public void init(){
		this.c = createControl();
		this.handler = c.createMock(EvaluationContextHandler.class);
		this.resolver = c.createMock(PolicyReferenceResolver.class);
		this.context = createMockBuilder(
				BaseEvaluationContext.class)
		.withConstructor(false, 0, handler, resolver)
		.createMock();
	}
	
	@Test
	public void testSetAndGetDecisionCacheTTLWithDefaultTTLZero()
	{
		EvaluationContext context = createMockBuilder(
				BaseEvaluationContext.class)
		.withConstructor(false, 0, handler, resolver)
		.createMock();
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
		EvaluationContext context = createMockBuilder(
				BaseEvaluationContext.class)
		.withConstructor(false, 10, handler, resolver)
		.createMock();
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
		c.replay();
		AttributeDesignatorKey k = AttributeDesignatorKey
				.builder()
				.category(AttributeCategories.SUBJECT_ACCESS)
				.attributeId("testId")
				.dataType(StringType.STRING)
				.issuer("test")
				.build();
		context.setResolvedDesignatorValue(k, StringType.STRING.bagOf("aaa"));
		assertEquals(StringType.STRING.bagOf("aaa"), context.resolve(k));
		c.verify();
	}
	
	@Test
	public void testResolveDesignatorValueValueIsNotInContext() throws EvaluationException
	{
		AttributeDesignatorKey k = AttributeDesignatorKey
				.builder()
				.category(AttributeCategories.SUBJECT_ACCESS)
				.attributeId("testId")
				.dataType(StringType.STRING)
				.issuer("test")
				.build();
		expect(handler.resolve(context, k)).andReturn(StringType.STRING.bagOf("aaa", "ccc"));
		c.replay();
		assertEquals(StringType.STRING.bagOf("aaa", "ccc"), context.resolve(k));
		assertEquals(StringType.STRING.bagOf("aaa", "ccc"), context.resolve(k));
		c.verify();
	}
}
