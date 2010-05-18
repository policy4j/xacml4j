package com.artagon.xacml.v3.policy.spi;

import java.util.Collection;

import com.artagon.xacml.v3.policy.Policy;
import com.artagon.xacml.v3.policy.PolicySet;

public interface PolicyStore 
{
	Collection<PolicySet> getPolicySets();
	Policy getPolicy(String policyId);
	PolicySet getPolicySet(String policyId);
}
