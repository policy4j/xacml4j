package org.xacml4j.v30.spi.repository;

import org.xacml4j.v30.pdp.Policy;
import org.xacml4j.v30.pdp.PolicySet;

public interface PolicyRepositoryListener
{
	void policyAdded(Policy p);
	void policyRemoved(Policy p);
	void policySetAdded(PolicySet p);
	void policySetRemoved(PolicySet p);
}
