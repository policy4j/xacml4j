package com.artagon.xacml.v3.policy;

import static junit.framework.Assert.assertEquals;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.policy.type.DataTypes;

public class DefaultMatchTest extends XacmlPolicyTestCase
{
	private FunctionSpec spec;
	private AttributeDesignator ref;
	
	@Before
	public void init()
	{	
		this.spec = createStrictMock(FunctionSpec.class);
		this.ref = createStrictMock(AttributeDesignator.class);
	}
	
	@Test
	public void testMatchEvaluation() throws EvaluationException
	{
		expect(spec.getNumberOfParams()).andReturn(2);
		expect(spec.getParamSpecAt(0)).andReturn(new ParamValueTypeSpec(DataTypes.INTEGER.getType()));
		expect(spec.getParamSpecAt(1)).andReturn(new ParamValueTypeSpec(DataTypes.INTEGER.getType()));
		expect(ref.getDataType()).andReturn(DataTypes.INTEGER.getType());
		expect(ref.evaluate(context)).andReturn(DataTypes.INTEGER.bag(DataTypes.INTEGER.create(2), DataTypes.INTEGER.create(1)));
		expect(spec.invoke(context, DataTypes.INTEGER.create(1), DataTypes.INTEGER.create(2))).andReturn(DataTypes.BOOLEAN.create(false));
		expect(spec.invoke(context, DataTypes.INTEGER.create(1), DataTypes.INTEGER.create(1))).andReturn(DataTypes.BOOLEAN.create(true));
		replay(spec, ref);
		Match m = new DefaultMatch(spec, DataTypes.INTEGER.create(1), ref);
		assertEquals(MatchResult.MATCH, m.match(context));
		verify(spec, ref);
	}
}
