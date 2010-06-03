package com.artagon.xacml.v3.policy.impl;

import static junit.framework.Assert.assertEquals;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.AttributeDesignator;
import com.artagon.xacml.v3.AttributeReferenceEvaluationException;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.Expression;
import com.artagon.xacml.v3.FunctionSpec;
import com.artagon.xacml.v3.Match;
import com.artagon.xacml.v3.MatchResult;
import com.artagon.xacml.v3.policy.XacmlPolicyTestCase;
import com.artagon.xacml.v3.policy.type.DataTypes;

public class DefaultMatchTest extends XacmlPolicyTestCase
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
		AttributeValue int1 = DataTypes.INTEGER.create(1);
		AttributeValue int2 = DataTypes.INTEGER.create(2);
		BagOfAttributeValues<AttributeValue> v = DataTypes.INTEGER.bag(int2, int1);
		expect(spec.getNumberOfParams()).andReturn(2);
		expect(spec.getParamSpecAt(0)).andReturn(new ParamValueTypeSpec(DataTypes.INTEGER.getType()));
		expect(spec.getParamSpecAt(1)).andReturn(new ParamValueTypeSpec(DataTypes.INTEGER.getType()));
		expect(ref.getDataType()).andReturn(DataTypes.INTEGER.getType());
		expect(ref.evaluate(context)).andReturn(v);
		expect(spec.invoke(eq(context), (Expression[])anyObject())).andReturn(DataTypes.BOOLEAN.create(false));
		expect(spec.invoke(eq(context), (Expression[])anyObject())).andReturn(DataTypes.BOOLEAN.create(true));
		replay(spec, ref);
		Match m = new Match(spec, int1, ref);
		assertEquals(MatchResult.MATCH, m.match(context));
		verify(spec, ref);
	}
	
	@Test
	public void testMatchEvaluationFailedToResolveAttributeException() throws EvaluationException
	{
		expect(spec.getNumberOfParams()).andReturn(2);
		expect(spec.getParamSpecAt(0)).andReturn(new ParamValueTypeSpec(DataTypes.INTEGER.getType()));
		expect(spec.getParamSpecAt(1)).andReturn(new ParamValueTypeSpec(DataTypes.INTEGER.getType()));
		expect(ref.getDataType()).andReturn(DataTypes.INTEGER.getType());
		expect(ref.evaluate(context)).andThrow(new AttributeReferenceEvaluationException(context, ref, "Failed"));
		replay(spec, ref);
		Match m = new Match(spec, DataTypes.INTEGER.create(1), ref);
		assertEquals(MatchResult.INDETERMINATE, m.match(context));
		verify(spec, ref);
	}
}
