package com.artagon.xacml.policy;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.DataTypes;
import com.artagon.xacml.policy.combine.RuleFirstApplicableCombiningAlgorithm;
import com.artagon.xacml.policy.type.BooleanType;

public class DefaultPolicyTest extends XacmlPolicyTestCase 
{

	private Policy policy;
	private Collection<Rule> rules;
	
	@Before
	public void init_policy(){
		BooleanType type = dataTypes.getDataType(DataTypes.BOOLEAN);
		this.rules = new LinkedList<Rule>();
		rules.add(new DefaultRule("test1", null, new Condition(type.create(Boolean.TRUE)), Effect.PERMIT));
		this.policy = new DefaultPolicy("policy1", rules, new RuleFirstApplicableCombiningAlgorithm());
	}
	
	@Test
	public void testPolicyEvaluation()
	{
		EvaluationContext policyContext = policy.createContext(context);
		DecisionResult r = policy.evaluate(policyContext);
		assertEquals(DecisionResult.PERMIT, r);
	}
}
