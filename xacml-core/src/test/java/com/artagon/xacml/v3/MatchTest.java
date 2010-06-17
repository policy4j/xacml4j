package com.artagon.xacml.v3;

import static junit.framework.Assert.assertEquals;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.types.XacmlDataTypes;

public class MatchTest extends XacmlPolicyTestCase
{
	private FunctionSpec spec;
	private AttributeDesignator ref;
	private EvaluationContext context;
	
	@Before
	public void init()
	{	
		this.spec = createStrictMock(FunctionSpec.class);
		this.ref = createStrictMock(AttributeDesignator.class);
		this.context = createStrictMock(EvaluationContext.class);
	}
	
	@Test
	public void testMatchEvaluation() throws EvaluationException
	{
		AttributeValue int1 = XacmlDataTypes.INTEGER.create(1);
		AttributeValue int2 = XacmlDataTypes.INTEGER.create(2);
		BagOfAttributeValues<AttributeValue> v = XacmlDataTypes.INTEGER.bag(int2, int1);
		expect(spec.getNumberOfParams()).andReturn(2);
		expect(spec.getParamSpecAt(0)).andReturn(new ParamValueTypeSpec(XacmlDataTypes.INTEGER.getType()));
		expect(spec.getParamSpecAt(1)).andReturn(new ParamValueTypeSpec(XacmlDataTypes.INTEGER.getType()));
		expect(ref.getDataType()).andReturn(XacmlDataTypes.INTEGER.getType());
		expect(ref.evaluate(context)).andReturn(v);
		expect(spec.invoke(context, int1, int2)).andReturn(XacmlDataTypes.BOOLEAN.create(false));
		expect(spec.invoke(context, int1, int1)).andReturn(XacmlDataTypes.BOOLEAN.create(true));
		replay(spec, ref, context);
		Match m = new Match(spec, int1, ref);
		assertEquals(MatchResult.MATCH, m.match(context));
		verify(spec, ref, context);
	}
	
	@Test
	public void testMatchEvaluationFailedToResolveAttributeException() throws EvaluationException
	{
		expect(spec.getNumberOfParams()).andReturn(2);
		expect(spec.getParamSpecAt(0)).andReturn(new ParamValueTypeSpec(XacmlDataTypes.INTEGER.getType()));
		expect(spec.getParamSpecAt(1)).andReturn(new ParamValueTypeSpec(XacmlDataTypes.INTEGER.getType()));
		expect(ref.getDataType()).andReturn(XacmlDataTypes.INTEGER.getType());
		expect(ref.evaluate(context)).andThrow(new AttributeReferenceEvaluationException(context, ref, "Failed"));
		context.setEvaluationStatus(StatusCode.createMissingAttribute());
		replay(spec, ref, context);
		Match m = new Match(spec, XacmlDataTypes.INTEGER.create(1), ref);
		assertEquals(MatchResult.INDETERMINATE, m.match(context));
		verify(spec, ref, context);
	}
}
