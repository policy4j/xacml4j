package com.artagon.xacml.policy;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.policy.combine.RuleDenyOverridesCombiningAlgorithm;
import com.artagon.xacml.policy.type.BooleanType;
import com.artagon.xacml.policy.type.DataTypes;

public class DefaultPolicyTest extends XacmlPolicyTestCase 
{

	private Policy policy;
	private Collection<Rule> rules;
	
	@Before
	public void init_policy()
	{
		BooleanType type = DataTypes.BOOLEAN.getType();
		this.rules = new LinkedList<Rule>();
		rules.add(new DefaultRule("PermitRule", null, new Condition(type.create(Boolean.TRUE)), Effect.PERMIT));
		rules.add(new DefaultRule("DenyRule", null, new Condition(type.create(Boolean.TRUE)), Effect.DENY));
		this.policy = new DefaultPolicy("policy1", rules, new RuleDenyOverridesCombiningAlgorithm());
	}
	
	@Test
	public void testPolicyEvaluation()
	{
		EvaluationContext policyContext = policy.createContext(context);
		DecisionResult r = policy.evaluate(policyContext);
		assertEquals(DecisionResult.DENY, r);
	}
}
