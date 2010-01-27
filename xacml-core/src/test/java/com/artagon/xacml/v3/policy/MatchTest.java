package com.artagon.xacml.v3.policy;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.CategoryId;
import com.artagon.xacml.v3.policy.function.DefaultFunctionSpecBuilder;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.artagon.xacml.v3.policy.type.IntegerType;
import com.artagon.xacml.v3.policy.type.BooleanType.BooleanValue;

public class MatchTest extends XacmlPolicyTestCase
{
	
	private FunctionSpec function;
	
	@Before
	public void init(){	
		DefaultFunctionSpecBuilder b = new DefaultFunctionSpecBuilder("test1");
		b.withParam(DataTypes.INTEGER.getType()).withParam(DataTypes.INTEGER.getType());
		BooleanValue expectedReturn = DataTypes.BOOLEAN.create(Boolean.TRUE);
		this.function = b.build(DataTypes.BOOLEAN.getType(), new MockFunctionImplementation<BooleanValue>(expectedReturn));
	}
	
	@Test
	public void testMatchEvaluation() throws EvaluationException
	{
		IntegerType t = DataTypes.INTEGER.getType();
		AttributeDesignator designator = new AttributeDesignator(CategoryId.SUBJECT_RECIPIENT,
				"testId", "testIssuer", t, true);
		attributeService.addAttribute(CategoryId.SUBJECT_RECIPIENT, "testId", "testIssuer", t, Collections.<AttributeValue>singleton(t.create(10L)));
		Match m = new Match(function, t.create(10L), designator);
		assertEquals(MatchResult.MATCH, m.match(context));
	}
}
