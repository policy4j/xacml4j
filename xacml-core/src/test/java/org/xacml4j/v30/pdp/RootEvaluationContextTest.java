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

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.BagOfAttributeValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.spi.repository.PolicyReferenceResolver;
import org.xacml4j.v30.types.XacmlTypes;

import java.util.Optional;


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
		assertThat(context.getDecisionCacheTTL(), is(0));
		context.setDecisionCacheTTL(20);
		assertThat(context.getDecisionCacheTTL(), is(20));
		context.setDecisionCacheTTL(10);
		assertThat(context.getDecisionCacheTTL(), is(10));
		context.setDecisionCacheTTL(-1);
		assertThat(context.getDecisionCacheTTL(), is(0));
		context.setDecisionCacheTTL(10);
		assertThat(context.getDecisionCacheTTL(), is(0));
		c.verify();
	}

	@Test
	public void testSetAndGetDecisionCacheTTLWithDefaultTTL()
	{
		RootEvaluationContext context = new RootEvaluationContext(false, 10, resolver, handler);
		c.replay();
		assertThat(context.getDecisionCacheTTL(), is(10));
		context.setDecisionCacheTTL(20);
		assertThat(context.getDecisionCacheTTL(), is(10));
		context.setDecisionCacheTTL(5);
		assertThat(context.getDecisionCacheTTL(), is(5));
		context.setDecisionCacheTTL(-1);
		assertThat(context.getDecisionCacheTTL(), is(0));
		context.setDecisionCacheTTL(10);
		assertThat(context.getDecisionCacheTTL(), is(0));
		c.verify();
	}

	@Test
	public void testResolveDesignatorValueValueIsNotInContext() throws EvaluationException
	{
		RootEvaluationContext context = new RootEvaluationContext(false, 0, resolver, handler);
		AttributeDesignatorKey k = AttributeDesignatorKey
				.builder()
				.category(CategoryId.SUBJECT_ACCESS)
				.attributeId("testId")
				.dataType(XacmlTypes.STRING)
				.issuer("test")
				.build();
		BagOfAttributeValues expectedValue = XacmlTypes.STRING.bag().value("aaa", "ccc").build();

		expect(handler.resolve(context, k)).andReturn(Optional.of(expectedValue));

		c.replay();
		assertThat(context.resolve(k), is(expectedValue));
		assertThat(context.resolve(k), is(expectedValue));
		c.verify();

		assertThat(context.getResolvedDesignators().keySet(), hasItem(k));
		assertThat(context.getResolvedDesignators().values(), hasItem(expectedValue));
	}
}
