package com.artagon.xacml.v3;

import static junit.framework.Assert.assertEquals;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.spi.function.FunctionInvocation;
import com.artagon.xacml.v3.spi.function.FunctionSpecBuilder;
import com.artagon.xacml.v3.types.BooleanType;
import com.artagon.xacml.v3.types.IntegerType;
import com.artagon.xacml.v3.types.XacmlDataTypes;

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
		this.builder = new FunctionSpecBuilder("testFunction");
		this.invocation = createStrictMock(FunctionInvocation.class);
		this.spec = builder.withParam(IntegerType.Factory.getInstance()).withParam(IntegerType.Factory.getInstance()).build(
				XacmlDataTypes.BOOLEAN.getDataType(), invocation);
	}
	
	@Test
	public void testMatchEvaluation() throws EvaluationException
	{
		AttributeValue int1 = IntegerType.Factory.create(1);
		AttributeValue int2 = IntegerType.Factory.create(2);
		BagOfAttributeValues v = IntegerType.Factory.bagOf(int2, int1);
		
		expect(ref.getDataType()).andReturn(IntegerType.Factory.getInstance());
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
		expect(ref.getDataType()).andReturn(XacmlDataTypes.INTEGER.getDataType());
		expect(ref.evaluate(context)).andThrow(new AttributeReferenceEvaluationException(context, ref, "Failed"));
		context.setEvaluationStatus(StatusCode.createMissingAttribute());
		replay(invocation, ref, context);
		Match m = new Match(spec, XacmlDataTypes.INTEGER.create(1), ref);
		assertEquals(MatchResult.INDETERMINATE, m.match(context));
		verify(invocation, ref, context);
	}
}
