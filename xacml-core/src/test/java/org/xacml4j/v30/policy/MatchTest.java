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

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

import org.easymock.Capture;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeReferenceEvaluationException;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.MatchResult;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.StatusCode;
import org.xacml4j.v30.policy.function.FunctionInvocation;
import org.xacml4j.v30.policy.function.FunctionSpecBuilder;
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
	private Rule rule;
	@Before
	public void init()
	{
		this.c = createControl();
		this.ref = c.createMock(AttributeDesignator.class);
		this.context = c.createMock(EvaluationContext.class);
		this.invocation = c.createMock(FunctionInvocation.class);
		this.spec = FunctionSpecBuilder.builder("testFunctionId1")
		                               .param(XacmlTypes.INTEGER)
		                               .param(XacmlTypes.INTEGER)
		                               .build(XacmlTypes.BOOLEAN, invocation);
		this.context = c.createMock(EvaluationContext.class);
		this.rule = c.createMock(Rule.class);
	}

	@Test
	public void testMatchEvaluation() throws EvaluationException
	{
		expect(ref.getDataType()).andReturn(XacmlTypes.INTEGER);
		expect(ref.evaluate(context)).andReturn(XacmlTypes.INTEGER.bagBuilder().attribute(XacmlTypes.INTEGER.of(2), XacmlTypes.INTEGER.of(1)).build());
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false);
		expect(invocation.invoke(spec, context,
		                         ImmutableList.<Expression>builder().add(XacmlTypes.INTEGER.of(1), XacmlTypes.INTEGER.of(2)).build()))
				.andReturn(XacmlTypes.BOOLEAN.of(false));
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false);
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
		Capture<Status> statusCapture = Capture.newInstance();
		Capture<Rule> ruleCapture = Capture.newInstance();
		expect(context.getCurrentRule()).andReturn(rule);
		context.setEvaluationStatus(capture(ruleCapture), capture(statusCapture));
		c.replay();
		Match m = Match
				.builder()
				.predicate(spec)
				.attribute(XacmlTypes.INTEGER.of(1))
				.attrRef(ref)
				.build();
		assertEquals(MatchResult.INDETERMINATE, m.match(context));
		assertEquals(StatusCode.missingAttributeError(), statusCapture.getValue().getStatusCode());
		assertEquals("Failed", statusCapture.getValue().getMessage().orElse(null));
		c.verify();
	}
}
