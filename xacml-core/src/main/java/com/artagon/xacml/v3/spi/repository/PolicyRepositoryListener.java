package com.artagon.xacml.v3.spi.repository;

import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicySet;

public interface PolicyRepositoryListener 
{
	void policyAdded(Policy p);
	void policyRemoved(Policy p);
	void policySetAdded(PolicySet p);
	void policySetRemoved(PolicySet p);
}
