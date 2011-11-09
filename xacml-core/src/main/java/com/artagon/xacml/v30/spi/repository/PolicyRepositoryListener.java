package com.artagon.xacml.v30.spi.repository;

import com.artagon.xacml.v30.pdp.Policy;
import com.artagon.xacml.v30.pdp.PolicySet;

public interface PolicyRepositoryListener 
{
	void policyAdded(Policy p);
	void policyRemoved(Policy p);
	void policySetAdded(PolicySet p);
	void policySetRemoved(PolicySet p);
}
