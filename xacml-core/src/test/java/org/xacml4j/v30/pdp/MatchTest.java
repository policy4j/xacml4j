package org.xacml4j.v30.pdp;

import static junit.framework.Assert.assertEquals;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.xacml4j.v30.types.BooleanType.BOOLEAN;
import static org.xacml4j.v30.types.IntegerType.INTEGER;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.AttributeCategories;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.MatchResult;
import org.xacml4j.v30.StatusCode;
import org.xacml4j.v30.pdp.AttributeDesignator;
import org.xacml4j.v30.pdp.AttributeReferenceEvaluationException;
import org.xacml4j.v30.pdp.FunctionSpec;
import org.xacml4j.v30.pdp.Match;
import org.xacml4j.v30.spi.function.FunctionInvocation;
import org.xacml4j.v30.spi.function.FunctionSpecBuilder;

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
		this.spec = builder.param(INTEGER).param(INTEGER).build(
				BOOLEAN, invocation);
	}

	@Test
	public void testMatchEvaluation() throws EvaluationException
	{
		expect(ref.getDataType()).andReturn(INTEGER);
		expect(ref.evaluate(context)).andReturn(INTEGER.bagOf(INTEGER.create(2), INTEGER.create(1)));
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false).times(2);
		expect(invocation.invoke(spec, context,
				ImmutableList.<Expression>builder().add(INTEGER.create(1), INTEGER.create(2)).build()))
				.andReturn(BOOLEAN.create(false));
		expect(invocation.invoke(spec, context,
				ImmutableList.<Expression>builder().add(INTEGER.create(1), INTEGER.create(1)).build()))
				.andReturn(BOOLEAN.create(true));
		c.replay();
		Match m = new Match(spec, INTEGER.create(1), ref);
		assertEquals(MatchResult.MATCH, m.match(context));
		c.verify();
	}

	@Test
	public void testMatchEvaluationFailedToResolveAttributeException() throws EvaluationException
	{
		expect(ref.getDataType()).andReturn(INTEGER);
		expect(ref.evaluate(context)).andThrow(new AttributeReferenceEvaluationException(context,
				new AttributeDesignatorKey(AttributeCategories.RESOURCE, "testId", INTEGER, null), "Failed"));
		context.setEvaluationStatus(StatusCode.createMissingAttributeError());
		c.replay();
		Match m = new Match(spec, INTEGER.create(1), ref);
		assertEquals(MatchResult.INDETERMINATE, m.match(context));
		c.verify();
	}
}
