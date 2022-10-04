package org.xacml4j.v30.policy;

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
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.types.XacmlTypes;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;


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
		RootEvaluationContext context = new RootEvaluationContext(false, Duration.ZERO, resolver, handler);
		c.replay();
		assertThat(context.getDecisionCacheTTL(), is(Duration.ofSeconds(0)));
		context.setDecisionCacheTTL(20);
		assertThat(context.getDecisionCacheTTL(), is(Duration.ofSeconds(0)));
		context.setDecisionCacheTTL(10);
		assertThat(context.getDecisionCacheTTL(), is(Duration.ofSeconds(0)));
		context.setDecisionCacheTTL(-1);
		assertThat(context.getDecisionCacheTTL(), is(Duration.ofSeconds(0)));
		context.setDecisionCacheTTL(10);
		assertThat(context.getDecisionCacheTTL(), is(Duration.ofSeconds(0)));
		c.verify();
	}

	@Test
	public void testSetAndGetDecisionCacheTTLWithDefaultTTL()
	{
		RootEvaluationContext context = new RootEvaluationContext(false, Duration.ofSeconds(10), resolver, handler);
		c.replay();
		assertThat(context.getDecisionCacheTTL(), is(Duration.ofSeconds(10)));
		context.setDecisionCacheTTL(Duration.ofSeconds(20));
		assertThat(context.getDecisionCacheTTL(), is(Duration.ofSeconds(10)));
		context.setDecisionCacheTTL(Duration.ofSeconds(5));
		assertThat(context.getDecisionCacheTTL(), is(Duration.ofSeconds(5)));
		context.setDecisionCacheTTL(-1);
		assertThat(context.getDecisionCacheTTL(), is(Duration.ZERO));
		context.setDecisionCacheTTL(10);
		assertThat(context.getDecisionCacheTTL(), is(Duration.ZERO));
		c.verify();
	}

	@Test
	public void testResolveDesignatorValueValueIsNotInContext() throws EvaluationException
	{
		RootEvaluationContext context = new RootEvaluationContext(false, Duration.ZERO, resolver, handler);
		AttributeDesignatorKey k = AttributeDesignatorKey
				.builder()
				.category(CategoryId.SUBJECT_ACCESS)
				.attributeId("testId")
				.dataType(XacmlTypes.STRING)
				.issuer("test")
				.build();
		BagOfValues expectedValue = XacmlTypes.STRING.bag()
		                                             .value("aaa", "ccc")
		                                             .build();

		expect(handler.resolve(context, k)).andReturn(Optional.of(expectedValue));

		c.replay();
		assertThat(context.resolve(k).get(), is(expectedValue));
		c.verify();
	}
}
