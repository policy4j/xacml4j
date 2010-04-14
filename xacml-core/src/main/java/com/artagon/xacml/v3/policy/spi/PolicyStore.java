package com.artagon.xacml.v3.policy.spi;

import java.util.Collection;

import com.artagon.xacml.v3.policy.Policy;
import com.artagon.xacml.v3.policy.PolicySet;
import com.artagon.xacml.v3.policy.CompositeDecisionRule;

public interface PolicyStore 
{
	Collection<CompositeDecisionRule> getDecisionRules();
	Policy getPolicyById(String policyId);
	PolicySet getPolicySetById(String policyId);
}
