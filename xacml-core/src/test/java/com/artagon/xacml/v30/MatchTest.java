package com.artagon.xacml.v30;

import static junit.framework.Assert.assertEquals;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.AttributeCategories;
import com.artagon.xacml.v30.AttributeDesignator;
import com.artagon.xacml.v30.AttributeDesignatorKey;
import com.artagon.xacml.v30.AttributeReferenceEvaluationException;
import com.artagon.xacml.v30.AttributeValue;
import com.artagon.xacml.v30.BagOfAttributeValues;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.EvaluationException;
import com.artagon.xacml.v30.FunctionSpec;
import com.artagon.xacml.v30.Match;
import com.artagon.xacml.v30.MatchResult;
import com.artagon.xacml.v30.StatusCode;
import com.artagon.xacml.v30.spi.function.FunctionInvocation;
import com.artagon.xacml.v30.spi.function.FunctionSpecBuilder;
import com.artagon.xacml.v30.types.BooleanType;
import com.artagon.xacml.v30.types.IntegerType;

public class MatchTest
{
	private FunctionSpec spec;
	private AttributeDesignator ref;
	private EvaluationContext context;
	private FunctionSpecBuilder builder;
	private FunctionInvocation invocation;
	
	@Before
	public void init()
	{	
		this.ref = createStrictMock(AttributeDesignator.class);
		this.context = createStrictMock(EvaluationContext.class);
		this.builder = FunctionSpecBuilder.create("testFunction");
		this.invocation = createStrictMock(FunctionInvocation.class);
		this.spec = builder.withParam(IntegerType.INTEGER).withParam(IntegerType.INTEGER).build(
				BooleanType.BOOLEAN, invocation);
	}
	
	@Test
	public void testMatchEvaluation() throws EvaluationException
	{
		AttributeValue int1 = IntegerType.INTEGER.create(1);
		AttributeValue int2 = IntegerType.INTEGER.create(2);
		BagOfAttributeValues v = IntegerType.INTEGER.bagOf(int2, int1);
		
		expect(ref.getDataType()).andReturn(IntegerType.INTEGER);
		expect(ref.evaluate(context)).andReturn(v);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false);
		expect(invocation.invoke(spec, context, int1, int2)).andReturn(BooleanType.BOOLEAN.create(false));
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false);
		expect(invocation.invoke(spec, context, int1, int1)).andReturn(BooleanType.BOOLEAN.create(true));
		replay(invocation, ref, context);
		Match m = new Match(spec, int1, ref);
		assertEquals(MatchResult.MATCH, m.match(context));
		verify(invocation, ref, context);
	}
	
	@Test
	public void testMatchEvaluationFailedToResolveAttributeException() throws EvaluationException
	{
		expect(ref.getDataType()).andReturn(IntegerType.INTEGER);
		expect(ref.evaluate(context)).andThrow(new AttributeReferenceEvaluationException(context, 
				new AttributeDesignatorKey(AttributeCategories.RESOURCE, "testId", IntegerType.INTEGER, null), "Failed"));
		context.setEvaluationStatus(StatusCode.createMissingAttribute());
		replay(invocation, ref, context);
		Match m = new Match(spec, IntegerType.INTEGER.create(1), ref);
		assertEquals(MatchResult.INDETERMINATE, m.match(context));
		verify(invocation, ref, context);
	}
}
