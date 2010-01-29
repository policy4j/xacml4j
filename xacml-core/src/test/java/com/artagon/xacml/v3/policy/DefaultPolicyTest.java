package com.artagon.xacml.v3.policy;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.policy.combine.RuleDenyOverridesCombiningAlgorithm;
import com.artagon.xacml.v3.policy.type.BooleanType;
import com.artagon.xacml.v3.policy.type.DataTypes;

public class DefaultPolicyTest extends XacmlPolicyTestCase 
{

	private Policy policy;
	private Collection<Rule> rules;
	
	@Before
	public void init_policy()
	{
		BooleanType type = DataTypes.BOOLEAN.getType();
		this.rules = new LinkedList<Rule>();
		rules.add(new DefaultRule("PermitRule", null, new DefaultCondition(type.create(Boolean.TRUE)), Effect.PERMIT));
		rules.add(new DefaultRule("DenyRule", null, new DefaultCondition(type.create(Boolean.TRUE)), Effect.DENY));
		this.policy = new DefaultPolicy("policy1", rules, new RuleDenyOverridesCombiningAlgorithm());
	}
	
	@Test
	public void testPolicyEvaluation()
	{
		EvaluationContext policyContext = policy.createContext(context);
		Decision r = policy.evaluate(policyContext);
		assertEquals(Decision.DENY, r);
	}
}
