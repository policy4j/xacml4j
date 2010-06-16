package com.artagon.xacml.v3.spi;

import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicySet;

public interface PolicyStore 
{
	Policy getPolicy(String policyId);
	PolicySet getPolicySet(String policyId);
}
