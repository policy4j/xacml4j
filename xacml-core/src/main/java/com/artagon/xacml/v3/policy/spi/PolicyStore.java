package com.artagon.xacml.v3.policy.spi;

import java.util.Collection;

import com.artagon.xacml.v3.policy.CompositeDecisionRule;
import com.artagon.xacml.v3.policy.Policy;
import com.artagon.xacml.v3.policy.PolicySet;

public interface PolicyStore 
{
	Collection<CompositeDecisionRule> getDecisionRules();
	Policy getPolicyById(String policyId);
	PolicySet getPolicySetById(String policyId);
}
