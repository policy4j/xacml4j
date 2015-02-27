package org.xacml4j.v30.pdp;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.spi.repository.PolicyReferenceResolver;
import org.xacml4j.v30.types.StringExp;
import org.xacml4j.v30.types.XacmlTypes;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;


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
		context.setResolvedDesignatorValue(k, StringExp.of("aaa").toBag());
		assertEquals(StringExp.of("aaa").toBag(), context.resolve(k));
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
