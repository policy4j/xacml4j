package com.artagon.xacml.v30.spi.repository;

import com.artagon.xacml.v30.Policy;
import com.artagon.xacml.v30.PolicySet;

public interface PolicyRepositoryListener 
{
	void policyAdded(Policy p);
	void policyRemoved(Policy p);
	void policySetAdded(PolicySet p);
	void policySetRemoved(PolicySet p);
}
