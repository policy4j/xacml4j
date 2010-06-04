package com.artagon.xacml.v3.policy.spi;

import com.artagon.xacml.v3.policy.Policy;
import com.artagon.xacml.v3.policy.PolicySet;

public interface PolicyStore 
{
	Policy getPolicy(String policyId);
	PolicySet getPolicySet(String policySetId);
}
