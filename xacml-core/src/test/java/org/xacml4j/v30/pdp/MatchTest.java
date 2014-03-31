package org.xacml4j.v30.pdp;

import static junit.framework.Assert.assertEquals;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.MatchResult;
import org.xacml4j.v30.StatusCode;
import org.xacml4j.v30.spi.function.FunctionInvocation;
import org.xacml4j.v30.spi.function.FunctionSpecBuilder;
import org.xacml4j.v30.types.BooleanExp;
import org.xacml4j.v30.types.IntegerExp;
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
		expect(ref.evaluate(context)).andReturn(XacmlTypes.INTEGER.bagOf(IntegerExp.valueOf(2), IntegerExp.valueOf(1)));
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false).times(2);
		expect(invocation.invoke(spec, context,
				ImmutableList.<Expression>builder().add(IntegerExp.valueOf(1), IntegerExp.valueOf(2)).build()))
				.andReturn(BooleanExp.valueOf(false));
		expect(invocation.invoke(spec, context,
				ImmutableList.<Expression>builder().add(IntegerExp.valueOf(1), IntegerExp.valueOf(1)).build()))
				.andReturn(BooleanExp.valueOf(true));
		c.replay();
		Match m = Match
				.builder()
				.predicate(spec)
				.attribute(IntegerExp.valueOf(1))
				.attrRef(ref)
				.build();
		assertEquals(MatchResult.MATCH, m.match(context));
		c.verify();
	}

	@Test
	public void testMatchEvaluationFailedToResolveAttributeException() throws EvaluationException
	{
		expect(ref.getDataType()).andReturn(XacmlTypes.INTEGER);
		expect(ref.evaluate(context)).andThrow(new AttributeReferenceEvaluationException(
				AttributeDesignatorKey.builder()
				.category(Categories.RESOURCE)
				.dataType(XacmlTypes.INTEGER)
				.attributeId("testId")
				.build(), "Failed"));
		context.setEvaluationStatus(StatusCode.createMissingAttributeError());
		c.replay();
		Match m = Match
				.builder()
				.predicate(spec)
				.attribute(IntegerExp.valueOf(1))
				.attrRef(ref)
				.build();
		assertEquals(MatchResult.INDETERMINATE, m.match(context));
		c.verify();
	}
}
