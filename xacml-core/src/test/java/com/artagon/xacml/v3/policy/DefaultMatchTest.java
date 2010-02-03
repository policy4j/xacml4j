package com.artagon.xacml.v3.policy;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import static org.easymock.EasyMock.*;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.CategoryId;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.artagon.xacml.v3.policy.type.IntegerType;
import com.artagon.xacml.v3.policy.type.BooleanType.BooleanValue;

public class DefaultMatchTest extends XacmlPolicyTestCase
{
	private FunctionSpec function;
	private Match m;
	
	@Before
	public void init(){	
		FunctionSpec spec = createStrictMock(FunctionSpec.class);
	}
	
	@Test
	public void testMatchEvaluation() throws EvaluationException
	{
		IntegerType t = DataTypes.INTEGER.getType();
		AttributeDesignator designator = new AttributeDesignator(CategoryId.SUBJECT_RECIPIENT,
				"testId", "testIssuer", t, true);
		attributeService.addAttribute(CategoryId.SUBJECT_RECIPIENT, "testId", "testIssuer", t, Collections.<AttributeValue>singleton(t.create(10L)));
		DefaultMatch m = new DefaultMatch(function, t.create(10L), designator);
		assertEquals(MatchResult.MATCH, m.match(context));
	}
}
