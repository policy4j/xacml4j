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
import static org.junit.Assert.assertEquals;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.*;
import org.xacml4j.v30.spi.function.FunctionInvocation;
import org.xacml4j.v30.spi.function.FunctionSpecBuilder;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.collect.ImmutableList;

public class MatchTest
{
	private FunctionSpec spec;
	private AttributeDesignator ref;
	private EvaluationContext context;
	private FunctionSpecBuilder builder;
	private FunctionInvocation invocation;
	private IMocksControl c;

	@Before
	public void init()
	{
		this.c = createControl();
		this.ref = c.createMock(AttributeDesignator.class);
		this.context = c.createMock(EvaluationContext.class);
		this.builder = FunctionSpecBuilder.builder("testFunction");
		this.invocation = c.createMock(FunctionInvocation.class);
		this.spec = builder.param(XacmlTypes.INTEGER).param(XacmlTypes.INTEGER).build(
				XacmlTypes.BOOLEAN, invocation);
	}

	@Test
	public void testMatchEvaluation() throws EvaluationException
	{
		expect(ref.getDataType()).andReturn(XacmlTypes.INTEGER);
		expect(ref.evaluate(context)).andReturn(XacmlTypes.INTEGER.bag().attribute(XacmlTypes.INTEGER.of(2), XacmlTypes.INTEGER.of(1)).build());
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false).times(2);
		expect(invocation.invoke(spec, context,
				ImmutableList.<Expression>builder().add(XacmlTypes.INTEGER.of(1), XacmlTypes.INTEGER.of(2)).build()))
				.andReturn(XacmlTypes.BOOLEAN.of(false));
		expect(invocation.invoke(spec, context,
				ImmutableList.<Expression>builder().add(XacmlTypes.INTEGER.of(1), XacmlTypes.INTEGER.of(1)).build()))
				.andReturn(XacmlTypes.BOOLEAN.of(true));
		c.replay();
		Match m = Match
				.builder()
				.predicate(spec)
				.attribute(XacmlTypes.INTEGER.of(1))
				.attrRef(ref)
				.build();
		assertEquals(MatchResult.MATCH, m.match(context));
		c.verify();
	}

	@Test
	public void testMatchEvaluationFailedToResolveAttributeException() throws EvaluationException
	{
		AttributeDesignatorKey key  = AttributeDesignatorKey.builder()
				.category(CategoryId.RESOURCE)
				.dataType(XacmlTypes.INTEGER)
				.attributeId("testId")
				.build();
		expect(ref.getDataType()).andReturn(XacmlTypes.INTEGER);
		expect(ref.evaluate(context)).andThrow(new AttributeReferenceEvaluationException(key, "Failed"));
		
		context.setEvaluationStatus(Status.missingAttribute(key).build());
		c.replay();
		Match m = Match
				.builder()
				.predicate(spec)
				.attribute(XacmlTypes.INTEGER.of(1))
				.attrRef(ref)
				.build();
		assertEquals(MatchResult.INDETERMINATE, m.match(context));
		c.verify();
	}
}
