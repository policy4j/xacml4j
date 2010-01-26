package com.artagon.xacml.v30.policy;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.CategoryId;
import com.artagon.xacml.v30.policy.AttributeDesignator;
import com.artagon.xacml.v30.policy.AttributeValue;
import com.artagon.xacml.v30.policy.EvaluationException;
import com.artagon.xacml.v30.policy.FunctionSpec;
import com.artagon.xacml.v30.policy.Match;
import com.artagon.xacml.v30.policy.MatchResult;
import com.artagon.xacml.v30.policy.function.DefaultFunctionSpecBuilder;
import com.artagon.xacml.v30.policy.type.BooleanType;
import com.artagon.xacml.v30.policy.type.DataTypes;
import com.artagon.xacml.v30.policy.type.IntegerType;

public class MatchTest extends XacmlPolicyTestCase
{
	
	private FunctionSpec function;
	private IntegerType paramType;
	private BooleanType booleanType;
	
	@Before
	public void init(){
		this.paramType = DataTypes.INTEGER.getType();
		this.booleanType = DataTypes.BOOLEAN.getType();
		
		DefaultFunctionSpecBuilder b = new DefaultFunctionSpecBuilder("test1");
		b.withParam(paramType).withParam(paramType);
		this.function = b.build(new MockFunctionImplementation(booleanType.create(Boolean.TRUE)));
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
