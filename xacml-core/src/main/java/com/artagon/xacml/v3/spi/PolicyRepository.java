package com.artagon.xacml.v3.spi;


import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicySet;
import com.artagon.xacml.v3.VersionMatch;

public interface PolicyRepository 
{
	/**
	 * Queries policy repository for a given
	 * policy via policy identifier and version
	 * match constraint
	 * 
	 * @param id a policy identifier
	 * @param version a policy version match constraint
	 * @return an {@link Iterable} to found policies
	 */
	Iterable<Policy> getPolicies(String id, VersionMatch version);
	
	
	/**
	 * Queries policy repository for a given
	 * policy set via policy set identifier and version
	 * match constraint
	 * 
	 * @param id a policy identifier
	 * @param version a policy version match constraint
	 * @return an {@link Iterable} to found policies
	 */
	Iterable<PolicySet> getPolicySets(String id, VersionMatch version);
	
	Iterable<Policy> getPolicies(String id, VersionMatch version, 
			VersionMatch earliest, VersionMatch latest);
	
	Iterable<PolicySet> getPolicySets(String id, VersionMatch version, 
			VersionMatch earliest, VersionMatch latest);
	
	/**
	 * Adds a new version of a given policy
	 * 
	 * @param policy a new version of the policy
	 */
	void add(Policy policy);
	
	/**
	 * Adds a new version of a given policy set
	 * 
	 * @param policySet a new version of policy set
	 */
	void add(PolicySet policySet);
}
