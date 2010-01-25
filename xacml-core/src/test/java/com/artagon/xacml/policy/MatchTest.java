package com.artagon.xacml.policy;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.oasis.xacml.azapi.constants.AzCategoryIdSubjectRecipient;

import com.artagon.xacml.policy.function.DefaultFunctionSpecBuilder;
import com.artagon.xacml.policy.function.XacmlFunction;
import com.artagon.xacml.policy.type.BooleanType;
import com.artagon.xacml.policy.type.XacmlDataType;
import com.artagon.xacml.policy.type.IntegerType;

public class MatchTest extends XacmlPolicyTestCase
{
	
	private FunctionSpec function;
	private IntegerType paramType;
	private BooleanType booleanType;
	
	@Before
	public void init(){
		this.paramType = XacmlDataType.INTEGER.getType();
		this.booleanType = XacmlDataType.BOOLEAN.getType();
		
		DefaultFunctionSpecBuilder b = new DefaultFunctionSpecBuilder(XacmlFunction.INTEGER_EQUAL.getXacmlId());
		b.withParam(paramType).withParam(paramType);
		this.function = b.build(new MockFunctionImplementation(booleanType.create(Boolean.TRUE)));
	}
	
	@Test
	public void testMatchEvaluation() throws PolicyEvaluationException
	{
		IntegerType t = XacmlDataType.INTEGER.getType();
		AttributeDesignator designator = new AttributeDesignator(AzCategoryIdSubjectRecipient.AZ_CATEGORY_ID_SUBJECT_RECIPIENT,
				"testId", "testIssuer", t, true);
		attributeService.addAttribute(AzCategoryIdSubjectRecipient.AZ_CATEGORY_ID_SUBJECT_RECIPIENT, "testId", "testIssuer", t, Collections.<Attribute>singleton(t.create(10L)));
		Match m = new Match(function, t.create(10L), designator);
		assertEquals(MatchResult.MATCH, m.match(context));
	}
}
